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

import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.services.crafting.RecipeService;
import net.l2emuproject.gameserver.world.object.L2Player;

public class RequestRecipeBookOpen extends L2GameClientPacket
{
	private static final String	_C__AC_REQUESTRECIPEBOOKOPEN	= "[C] AC RequestRecipeBookOpen";

	private boolean				_isDwarvenCraft;

	/**
	 * packet type id 0xac
	 * packet format rev656  cd
	 * @param decrypt
	 */
	@Override
	protected void readImpl()
	{
		_isDwarvenCraft = (readD() == 0);
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getActiveChar();
		if (player == null)
			return;

		if (player.getPrivateStoreType() != 0)
		{
			requestFailed(SystemMessageId.PRIVATE_STORE_UNDER_WAY);
			return;
		}

		RecipeService.getInstance().requestBookOpen(player, _isDwarvenCraft);

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__AC_REQUESTRECIPEBOOKOPEN;
	}
}
