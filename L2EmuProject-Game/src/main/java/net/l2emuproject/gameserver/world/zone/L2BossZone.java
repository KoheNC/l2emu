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
package net.l2emuproject.gameserver.world.zone;

import net.l2emuproject.gameserver.instancemanager.FourSepulchersManager;
import net.l2emuproject.gameserver.instancemanager.grandbosses.AntharasManager;
import net.l2emuproject.gameserver.instancemanager.grandbosses.BaiumManager;
import net.l2emuproject.gameserver.instancemanager.grandbosses.BaylorManager;
import net.l2emuproject.gameserver.instancemanager.grandbosses.BelethManager;
import net.l2emuproject.gameserver.instancemanager.grandbosses.FrintezzaManager;
import net.l2emuproject.gameserver.instancemanager.grandbosses.SailrenManager;
import net.l2emuproject.gameserver.instancemanager.grandbosses.ValakasManager;
import net.l2emuproject.gameserver.instancemanager.grandbosses.VanHalterManager;
import net.l2emuproject.gameserver.instancemanager.lastimperialtomb.LastImperialTombManager;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;

public class L2BossZone extends L2Zone
{
	@Override
	protected void register()
	{
		switch (getBoss())
		{
			case ANTHARAS:
				AntharasManager.getInstance().registerZone(this);
				break;
			case BAIUM:
				BaiumManager.getInstance().registerZone(this);
				break;
			case BAYLOR:
				BaylorManager.getInstance().registerZone(this);
				break;
			case BELETH:
				BelethManager.getInstance().registerZone(this);
				break;
			case FRINTEZZA:
				FrintezzaManager.getInstance().registerZone(this);
				break;
			case FOURSEPULCHERS:
				FourSepulchersManager.getInstance().registerZone(this);
				break;
			case LASTIMPERIALTOMB:
				LastImperialTombManager.getInstance().registerZone(this);
				break;
			case SAILREN:
				SailrenManager.getInstance().registerZone(this);
				break;
			case VALAKAS:
				ValakasManager.getInstance().registerZone(this);
				break;
			case VANHALTER:
				VanHalterManager.getInstance().registerZone(this);
				break;
		}
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		switch (getBoss())
		{
			case SUNLIGHTROOM:
			{
				character.setInsideZone(FLAG_SUNLIGHTROOM, true);
				break;
			}
			case FRINTEZZA:
			{
				if (character instanceof L2PcInstance)
					FrintezzaManager.getInstance().setScarletSpawnTask();
				break;
			}
		}
		
		character.setInsideZone(FLAG_NOSUMMON, true);
		
		super.onEnter(character);
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		switch (getBoss())
		{
			case SUNLIGHTROOM:
			{
				character.setInsideZone(FLAG_SUNLIGHTROOM, false);
				break;
			}
		}
		
		character.setInsideZone(FLAG_NOSUMMON, false);
		
		super.onExit(character);
	}
}
