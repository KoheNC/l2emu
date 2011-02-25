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

import net.l2emuproject.gameserver.model.actor.instance.L2ClassMasterInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * 7B 74 00 75 00 74 00 6F 00 72 00 69 00 61 00 6C
 * 00 5F 00 63 00 6C 00 6F 00 73 00 65 00 5F 00 32
 * 00 00 00
 * 
 * Format: (c) S
 * 
 * @author  DaDummy
 */
public class RequestTutorialLinkHtml extends L2GameClientPacket
{
	private static final String	_C__7B_REQUESTTUTORIALLINKHTML	= "[C] 7B equestTutorialLinkHtml";

	private String				_link;

	@Override
	protected void readImpl()
	{
		_link = readS(); // link
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if (player == null)
			return;

		L2ClassMasterInstance.onTutorialLink(player, _link);

		player.onTutorialLink(_link);

		QuestState qs = player.getQuestState("_255_Tutorial");
		if (qs != null)
			qs.getQuest().notifyEvent(_link, null, player);

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__7B_REQUESTTUTORIALLINKHTML;
	}
}
