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
package net.l2emuproject.gameserver.entity.shot;

import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.system.threadmanager.ThreadPoolManager;
import net.l2emuproject.gameserver.world.object.L2Character;

/**
 * @author NB4L1
 */
public abstract class CharShots implements Runnable
{
	private static final CharShots _instance = new CharShots(null) {
		@Override
		public void scheduleShotRecharge(int delay)
		{
		}
		
		@Override
		public void rechargeShots()
		{
		}
	};
	
	public static CharShots getEmptyInstance()
	{
		return _instance;
	}
	
	protected final L2Character _activeChar;
	
	protected CharShots(L2Character activeChar)
	{
		_activeChar = activeChar;
	}
	
	protected L2Character getActiveChar()
	{
		return _activeChar;
	}
	
	protected ShotState getShotState()
	{
		return ShotState.getEmptyInstance();
	}
	
	@Override
	public void run()
	{
		rechargeShots();
	}
	
	public void scheduleShotRecharge(int delay)
	{
		ThreadPoolManager.getInstance().schedule(this, delay);
	}
	
	public abstract void rechargeShots();
	
	protected boolean canChargeSoulshot(L2ItemInstance consume)
	{
		return false;
	}
	
	protected boolean canChargeSpiritshot(L2ItemInstance consume)
	{
		return false;
	}
	
	protected boolean canChargeBlessedSpiritshot(L2ItemInstance consume)
	{
		return false;
	}
	
	protected boolean canChargeFishshot(L2ItemInstance consume)
	{
		return false;
	}
	
	public boolean isSoulshotCharged()
	{
		return getShotState().isSoulshotCharged();
	}
	
	public boolean isSpiritshotCharged()
	{
		return getShotState().isSpiritshotCharged();
	}
	
	public boolean isBlessedSpiritshotCharged()
	{
		return getShotState().isBlessedSpiritshotCharged();
	}
	
	public boolean isAnySpiritshotCharged()
	{
		return getShotState().isAnySpiritshotCharged();
	}
	
	public boolean isFishshotCharged()
	{
		return getShotState().isFishshotCharged();
	}
	
	public void chargeSoulshot(L2ItemInstance consume)
	{
		getShotState().chargeSoulshot(this, consume);
	}
	
	public void chargeSpiritshot(L2ItemInstance consume)
	{
		getShotState().chargeSpiritshot(this, consume);
	}
	
	public void chargeBlessedSpiritshot(L2ItemInstance consume)
	{
		getShotState().chargeBlessedSpiritshot(this, consume);
	}
	
	public void chargeFishshot(L2ItemInstance consume)
	{
		getShotState().chargeFishshot(this, consume);
	}
	
	public void useSoulshotCharge()
	{
		getShotState().useSoulshotCharge();
	}
	
	public void useSpiritshotCharge()
	{
		getShotState().useSpiritshotCharge();
	}
	
	public void useBlessedSpiritshotCharge()
	{
		getShotState().useBlessedSpiritshotCharge();
	}
	
	public void useFishshotCharge()
	{
		getShotState().useFishshotCharge();
	}
	
	public void clearShotCharges()
	{
		getShotState().clearShotCharges();
	}
}
