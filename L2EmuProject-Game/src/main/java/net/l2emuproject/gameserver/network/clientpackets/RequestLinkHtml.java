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

import org.mmocore.network.InvalidPacketException;

import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.util.FloodProtector.Protected;
import net.l2emuproject.gameserver.util.Util;

/**
 * Lets drink to code!
 * 
 * @author zabbix
 */
public final class RequestLinkHtml extends L2GameClientPacket
{
	private static final String	REQUESTLINKHTML__C__20	= "[C] 20 RequestLinkHtml";

	private String				_link;

	@Override
	protected void readImpl()
	{
		_link = readS();
	}

	@Override
	protected void runImpl() throws InvalidPacketException
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		if (!getClient().getFloodProtector().tryPerformAction(Protected.LINK))
			return;

		if (_link.contains("..") || !_link.contains(".htm"))
		{
			Util.handleIllegalPlayerAction(player, "[RequestLinkHtml] " + player.getName() + "[" + player.getObjectId()
					+ "]: hack? link contains prohibited characters: '" + _link + "', skipped");
			return;
		}

		player.validateLink(_link);

		try
		{
			String filename = "data/html/" + _link;
			NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile(filename);
			html.replace("%objectId%", String.valueOf(player.getLastFolkNPC().getObjectId()));
			sendPacket(html);
		}
		catch (Exception e)
		{
			_log.warn("Bad RequestLinkHtml: " + _link, e);
		}
	}

	@Override
	public String getType()
	{
		return REQUESTLINKHTML__C__20;
	}
}
