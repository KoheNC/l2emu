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

import javolution.util.FastList;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.world.Location;
import net.l2emuproject.gameserver.network.serverpackets.ExCursedWeaponLocation;
import net.l2emuproject.gameserver.network.serverpackets.ExCursedWeaponLocation.CursedWeaponInfo;
import net.l2emuproject.gameserver.services.cursedweapons.CursedWeapon;
import net.l2emuproject.gameserver.services.cursedweapons.CursedWeaponsService;


/**
 * Format: (ch)
 * @author  -Wooden-
 */
public class RequestCursedWeaponLocation extends L2GameClientPacket
{
    private static final String _C__D0_23_REQUESTCURSEDWEAPONLOCATION = "[C] D0:23 RequestCursedWeaponLocation";

    @Override
    protected void readImpl()
    {
    }

    @Override
    protected void runImpl()
    {
        L2Character activeChar = getClient().getActiveChar();
        if (activeChar == null) return;

        Location loc = null;
        FastList<CursedWeaponInfo> list = new FastList<CursedWeaponInfo>();
        for (CursedWeapon cw : CursedWeaponsService.getInstance().getCursedWeapons())
        {
            if (!cw.isActive()) continue;
            loc = cw.getCurrentLocation();
            if (loc != null)
                list.add(new CursedWeaponInfo(loc, cw.getItemId(), cw.isActivated() ? 1 : 0));
        }
        loc = null;

        sendPacket(new ExCursedWeaponLocation(list));
    }

    @Override
    public String getType()
    {
        return _C__D0_23_REQUESTCURSEDWEAPONLOCATION;
    }
}
