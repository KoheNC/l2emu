/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.l2emuproject.gameserver.network.clientpackets;

import java.util.StringTokenizer;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.communitybbs.CommunityBoard;
import net.l2emuproject.gameserver.datatables.ClanTable;
import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.handler.AdminCommandHandler;
import net.l2emuproject.gameserver.model.olympiad.Olympiad;
import net.l2emuproject.gameserver.model.restriction.global.GlobalRestrictions;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.ExHeroList;
import net.l2emuproject.gameserver.network.serverpackets.GMViewPledgeInfo;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2MerchantSummonInstance;
import net.l2emuproject.gameserver.world.object.position.L2CharPosition;

import org.mmocore.network.InvalidPacketException;


/**
 * This class represents a packet sent when player clicks a link in the chat dialog.
 * In HTML files it is <a action="bypass -h command"/>
 * 
 * @version $Revision: 1.12.4.5 $ $Date: 2005/04/11 10:06:11 $
 */
public class RequestBypassToServer extends L2GameClientPacket
{
	private static final String	_C__23_REQUESTBYPASSTOSERVER	= "[C] 23 RequestBypassToServer";

	// S
	private String				_command;

	@Override
	protected void readImpl()
	{
		_command = readS();
	}

	@Override
	protected void runImpl() throws InvalidPacketException
	{
		L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (_command.startsWith("admin_"))
			AdminCommandHandler.getInstance().useAdminCommand(activeChar, _command);
		else if (_command.equals("come_here") && activeChar.getAccessLevel() >= Config.GM_ACCESSLEVEL)
			comeHere(activeChar);
		else if (_command.startsWith("show_clan_info "))
			activeChar.sendPacket(new GMViewPledgeInfo(ClanTable.getInstance().getClanByName(_command.substring(15)), activeChar));
		else if (_command.startsWith("player_help "))
			playerHelp(activeChar, _command.substring(12));
		else if (_command.startsWith("npc_"))
		{
			activeChar.validateBypass(_command);

			int endOfId = _command.indexOf('_', 5);
			String id;
			if (endOfId > 0)
				id = _command.substring(4, endOfId);
			else
				id = _command.substring(4);
			try
			{
				int objectId = Integer.parseInt(id);

				L2Npc npc = activeChar.getTarget(L2Npc.class, objectId);

				if (npc != null && endOfId > 0 && activeChar.isInsideRadius(npc, L2Npc.INTERACTION_DISTANCE, false, false))
				{
					String realCommand = _command.substring(endOfId + 1);

					if (GlobalRestrictions.onBypassFeedback(npc, activeChar, realCommand))
					{
					}
					else
						npc.onBypassFeedback(activeChar, realCommand);
				}

				sendPacket(ActionFailed.STATIC_PACKET);
			}
			catch (NumberFormatException nfe)
			{
			}
		}
		else if (_command.startsWith("summon_"))
		{
			activeChar.validateBypass(_command);

			int endOfId = _command.indexOf('_', 8);
			String id;
			if (endOfId > 0)
				id = _command.substring(7, endOfId);
			else
				id = _command.substring(7);
			try
			{
				int objectId = Integer.parseInt(id);

				L2MerchantSummonInstance summon = activeChar.getTarget(L2MerchantSummonInstance.class, objectId);

				if (summon != null && endOfId > 0 && activeChar.isInsideRadius(summon, L2Npc.INTERACTION_DISTANCE, false, false))
				{
					summon.onBypassFeedback(activeChar, _command.substring(endOfId + 1));
				}
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
			catch (NumberFormatException nfe)
			{
			}
		}
		//  Draw a Symbol
		else if (_command.equals("menu_select?ask=-16&reply=1"))
		{
			activeChar.validateBypass(_command);

			L2Object object = activeChar.getTarget();
			if (object instanceof L2Npc)
				((L2Npc) object).onBypassFeedback(activeChar, _command);
		}
		else if (_command.equals("menu_select?ask=-16&reply=2"))
		{
			//activeChar.validateBypass(_command); // FIXME: shouldn't we validate here too?

			L2Object object = activeChar.getTarget();
			if (object instanceof L2Npc)
				((L2Npc) object).onBypassFeedback(activeChar, _command);
		}
		// Navigate throught Manor windows
		else if (_command.startsWith("manor_menu_select?"))
		{
			//activeChar.validateBypass(_command); // FIXME: shouldn't we validate here too?

			L2Object object = activeChar.getTarget();
			if (object instanceof L2Npc)
				((L2Npc) object).onBypassFeedback(activeChar, _command);
		}
		else if (_command.startsWith("bbs_"))
			CommunityBoard.handleCommands(getClient(), _command);
		else if (_command.startsWith("_bbs"))
			CommunityBoard.handleCommands(getClient(), _command);
		else if (_command.startsWith("_maillist_0_1_0_"))
			CommunityBoard.handleCommands(getClient(), _command);
		else if (_command.startsWith("Quest "))
		{
			activeChar.validateBypass(_command);

			String p = _command.substring(6).trim();
			int idx = p.indexOf(' ');
			if (idx < 0)
				activeChar.processQuestEvent(p, "");
			else
				activeChar.processQuestEvent(p.substring(0, idx), p.substring(idx).trim());
		}
		else if (_command.startsWith("OlympiadArenaChange"))
			Olympiad.bypassChangeArena(_command, activeChar);
		else if (_command.startsWith("_herolist"))
		{
			activeChar.sendPacket(new ExHeroList());
		}

		sendPacket(ActionFailed.STATIC_PACKET);
	}

	private void comeHere(L2Player activeChar)
	{
		L2Object obj = activeChar.getTarget();
		if (obj instanceof L2Npc)
		{
			L2Npc temp = (L2Npc) obj;
			temp.setTarget(activeChar);
			temp.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 0));
		}
	}

	private void playerHelp(L2Player activeChar, String path)
	{
		if (path.indexOf("..") != -1)
			return;

		StringTokenizer st = new StringTokenizer(path);
		String[] cmd = st.nextToken().split("#");

		if (cmd.length > 1)
		{
			int itemId = 0;
			itemId = Integer.parseInt(cmd[1]);
			String filename = "data/html/help/" + cmd[0];
			NpcHtmlMessage html = new NpcHtmlMessage(1, itemId);
			html.setFile(filename);
			html.disableValidation();
			activeChar.sendPacket(html);
		}
		else
		{
			String filename = "data/html/help/" + path;
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setFile(filename);
			html.disableValidation();
			activeChar.sendPacket(html);
		}
	}

	@Override
	public String getType()
	{
		return _C__23_REQUESTBYPASSTOSERVER;
	}
}
