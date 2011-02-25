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

import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.services.duel.DuelService;

/**
 * Format:(ch)
 * just a trigger
 * @author -Wooden-
 */
public final class RequestDuelSurrender extends L2GameClientPacket
{
	private static final String _C__D0_30_REQUESTDUELSURRENDER = "[C] D0:30 RequestDuelSurrender";

	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		DuelService.getInstance().doSurrender(getClient().getActiveChar());
		sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public String getType()
	{
		return _C__D0_30_REQUESTDUELSURRENDER;
	}
}
