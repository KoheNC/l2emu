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

import java.sql.Connection;
import java.sql.PreparedStatement;

import net.l2emuproject.Config;
import net.l2emuproject.L2DatabaseFactory;
import net.l2emuproject.gameserver.datatables.PetDataTable;
import net.l2emuproject.gameserver.instancemanager.CursedWeaponsManager;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.model.world.L2World;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.InventoryUpdate;
import net.l2emuproject.gameserver.util.FloodProtector.Protected;
import net.l2emuproject.gameserver.util.Util;

public class RequestDestroyItem extends L2GameClientPacket
{
	private static final String _C__REQUESTDESTROYITEM = "[C] 60 RequestDestroyItem c[dq]";

	private int _objectId;
	private long _count;

    @Override
    protected void readImpl()
    {
		_objectId = readD();
		_count = readQ();
	}

    @Override
    protected void runImpl()
	{
		L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
			return;
		else if (!getClient().getFloodProtector().tryPerformAction(Protected.TRANSACTION))
			return;

		if (_count < 1)
		{
			requestFailed(SystemMessageId.CANNOT_DESTROY_NUMBER_INCORRECT);
			return;
		}

		if (activeChar.getPrivateStoreType() != 0)
		{
			requestFailed(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}

		L2ItemInstance itemToRemove = activeChar.getInventory().getItemByObjectId(_objectId);

		// if we can't find the requested item, its actually a cheat
		if (itemToRemove == null)
		{
			requestFailed(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return;
		}

		// Cannot discard item that the skill is consuming
		else if (activeChar.isCastingNow() &&
				activeChar.getCurrentSkill() != null &&
				activeChar.getCurrentSkill().getSkill().getItemConsumeId() == itemToRemove.getItemId())
		{
			requestFailed(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
	        return;
		}

		// Cannot discard item that the skill is consuming
		else if (activeChar.isCastingSimultaneouslyNow() &&
				activeChar.getLastSimultaneousSkillCast() != null &&
				activeChar.getLastSimultaneousSkillCast().getItemConsumeId() == itemToRemove.getItemId())
		{
			requestFailed(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
	        return;
		}

		int itemId = itemToRemove.getItemId();

        if (Config.ALT_STRICT_HERO_SYSTEM && itemToRemove.isHeroItem() &&
        		!activeChar.isGM())
        {
            requestFailed(SystemMessageId.HERO_WEAPONS_CANT_DESTROYED);
            return;
        }
        else if (itemToRemove.isWear() || ((!itemToRemove.isDestroyable() ||
        		CursedWeaponsManager.getInstance().isCursed(itemId)) &&
        		!activeChar.isGM()))
		{
			requestFailed(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
		    return;
		}

        if (!itemToRemove.isStackable() && _count > 1)
        {
        	sendAF();
            Util.handleIllegalPlayerAction(activeChar, "[RequestDestroyItem] count > 1 but item is not stackable! oid: "+_objectId+" owner: "+activeChar.getName(),Config.DEFAULT_PUNISH);
            return;
        }

		if (_count > itemToRemove.getCount())
		{
			requestFailed(SystemMessageId.CANNOT_DESTROY_NUMBER_INCORRECT);
			return;
			//_count = itemToRemove.getCount();
		}

		if (itemToRemove.isEquipped())
		{
			L2ItemInstance[] unequiped =
				activeChar.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getLocationSlot());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance element : unequiped)
				iu.addModifiedItem(element);
			sendPacket(iu);
			activeChar.broadcastUserInfo();
		}

		if (PetDataTable.isPetItem(itemId))
		{
			Connection con = null;
			try
			{
				if (activeChar.getPet() != null && activeChar.getPet().getControlItemId() == _objectId)
				{
					requestFailed(SystemMessageId.PET_SUMMONED_MAY_NOT_DESTROYED);
					return;
					//activeChar.getPet().unSummon(activeChar);
				}

				// if it's a pet control item, delete the pet
				con = L2DatabaseFactory.getInstance().getConnection(con);
				PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
				statement.setInt(1, _objectId);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.warn("Could not delete pet. ObjectId: ", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}

		if (itemToRemove.isTimeLimitedItem())
			itemToRemove.endOfLife();
		L2ItemInstance removedItem = activeChar.getInventory().destroyItem("Destroy", _objectId, _count, activeChar, null);
		if (removedItem == null)
		{
			sendAF();
			return;
		}
		activeChar.getInventory().updateInventory(removedItem);
		L2World.getInstance().removeObject(removedItem);

		sendAF();
	}

	@Override
    public String getType()
	{
		return _C__REQUESTDESTROYITEM;
	}
}
