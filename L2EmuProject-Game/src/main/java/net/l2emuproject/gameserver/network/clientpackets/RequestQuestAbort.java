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

import net.l2emuproject.gameserver.network.serverpackets.QuestList;
import net.l2emuproject.gameserver.services.quest.Quest;
import net.l2emuproject.gameserver.services.quest.QuestService;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.world.object.L2Player;

public class RequestQuestAbort extends L2GameClientPacket
{
	private static final String	_C__64_REQUESTQUESTABORT	= "[C] 64 RequestQuestAbort";

	private int					_questId;

	/**
	 * packet type id 0x64<p>
	 */
	@Override
	protected void readImpl()
	{
		_questId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		Quest qe = QuestService.getInstance().getQuest(_questId);
		if (qe != null)
		{
			QuestState qs = activeChar.getQuestState(qe.getName());
			if (qs != null)
			{
				qs.exitQuest(true);
				// Is it still necessary?
				if (qe.getName() == "605_AllianceWithKetraOrcs" || qe.getName() == "611_AllianceWithVarkaSilenos")
					activeChar.setAllianceWithVarkaKetra(0);
				sendPacket(new QuestList(activeChar));
			}
			else
			{
				if (_log.isDebugEnabled())
					_log.debug("Player '"+activeChar.getName()+"' try to abort quest "+qe.getName()+" but he didn't have it started.");
			}
		}
		else
		{
			if (_log.isDebugEnabled())
				_log.warn("Quest (id='"+_questId+"') not found.");
		}

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__64_REQUESTQUESTABORT;
	}
}
