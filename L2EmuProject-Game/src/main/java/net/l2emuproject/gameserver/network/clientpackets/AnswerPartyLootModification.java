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

import net.l2emuproject.gameserver.entity.party.L2Party;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author JIV
 */
public final class AnswerPartyLootModification extends L2GameClientPacket
{
	private static final String	TYPE	= "[C] D0:79 AnswerPartyLootModification";

	public int					_answer;

	@Override
	protected final void readImpl()
	{
		_answer = readD();
	}

	@Override
	protected final void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		L2Party party = activeChar.getParty();

		if (party != null)
			party.answerLootChangeRequest(activeChar, _answer == 1);
	}

	@Override
	public final String getType()
	{
		return TYPE;
	}

}
