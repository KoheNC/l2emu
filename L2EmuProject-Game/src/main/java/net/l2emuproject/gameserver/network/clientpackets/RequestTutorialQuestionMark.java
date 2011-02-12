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

import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;

/**
 * Format: (c) d
 * 
 * @author DaDummy
 */
public class RequestTutorialQuestionMark extends L2GameClientPacket
{
	private static final String	_C__7D_REQUESTTUTORIALQUESTIONMARK	= "[C] 7D RequestTutorialQuestionMark";
	private int					_id;

	@Override
	protected void readImpl()
	{
		_id = readD(); // id
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		player.onTutorialQuestionMark(_id);

		QuestState qs = player.getQuestState("_255_Tutorial");
		if (qs != null)
			qs.getQuest().notifyEvent("QM" + _id + "", null, player);

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__7D_REQUESTTUTORIALQUESTIONMARK;
	}
}