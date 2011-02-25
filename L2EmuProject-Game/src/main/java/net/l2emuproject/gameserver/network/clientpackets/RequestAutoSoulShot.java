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

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.ShotTable;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.world.object.L2Summon;

public final class RequestAutoSoulShot extends L2GameClientPacket
{
	private static final String _C__CF_REQUESTAUTOSOULSHOT = "[C] CF RequestAutoSoulShot";

	// format  cd
	private int _shotId;
	private int _type; // 1 = on, 0 = off

	/**
	 * packet type id 0xcf
	 * format: chdd
	 */
	@Override
	protected void readImpl()
	{
		_shotId = readD();
		_type = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
			return;

		if (!Config.ALT_AUTO_FISHING_SHOT && ShotTable.isFishingShot(_shotId))
		{
			sendAF();
			return;
		}

		if (activeChar.getPrivateStoreType() == 0 && activeChar.getActiveRequester() == null && !activeChar.isDead())
		{
			if (_type == 1)
			{
				L2ItemInstance shot = activeChar.getInventory().getItemByItemId(_shotId);
				if (shot == null)
					return;

				if (ShotTable.isBeastShot(_shotId))
				{
					L2Summon summon = activeChar.getPet();
					if (summon != null)
					{
						activeChar.getShots().addAutoSoulShot(_shotId);
						summon.rechargeShot();
					}
				}
				else if (activeChar.getActiveWeaponItem() != null)
				{
					activeChar.getShots().addAutoSoulShot(_shotId);
					activeChar.rechargeShot();
				}
			}
			else if (_type == 0)
			{
				activeChar.getShots().removeAutoSoulShot(_shotId);
			}
		}

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__CF_REQUESTAUTOSOULSHOT;
	}
}
