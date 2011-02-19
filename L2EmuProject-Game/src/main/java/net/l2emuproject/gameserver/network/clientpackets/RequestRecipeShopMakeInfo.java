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

import net.l2emuproject.gameserver.model.L2Object;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.world.L2World;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.RecipeShopItemInfo;

/**
 * This class ...
 * cdd
 * @version $Revision: 1.1.2.1.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestRecipeShopMakeInfo extends L2GameClientPacket
{
	private static final String	_C__B5_RequestRecipeShopMakeInfo	= "[C] b5 RequestRecipeShopMakeInfo";

	private int					_objectId;
	private int					_recipeId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_recipeId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		L2Object obj = null;

		// Get object from target
		if (activeChar.getTargetId() == _objectId)
			obj = activeChar.getTarget();

		// Get object from world
		if (obj == null)
			obj = L2World.getInstance().getPlayer(_objectId);

		if (!(obj instanceof L2PcInstance))
		{
			requestFailed(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}

		sendPacket(new RecipeShopItemInfo((L2PcInstance) obj, _recipeId));

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__B5_RequestRecipeShopMakeInfo;
	}
}
