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
package net.l2emuproject.gameserver.skills.effects;

import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.skills.L2Effect;
import net.l2emuproject.gameserver.templates.effects.EffectTemplate;
import net.l2emuproject.gameserver.templates.skills.L2EffectType;
import net.l2emuproject.gameserver.world.Location;
import net.l2emuproject.gameserver.world.geodata.GeoData;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2DefenderInstance;
import net.l2emuproject.gameserver.world.object.instance.L2FortCommanderInstance;
import net.l2emuproject.gameserver.world.object.instance.L2NpcInstance;
import net.l2emuproject.gameserver.world.object.instance.L2PetInstance;
import net.l2emuproject.gameserver.world.object.instance.L2SiegeFlagInstance;
import net.l2emuproject.gameserver.world.object.instance.L2SiegeSummonInstance;
import net.l2emuproject.gameserver.world.object.position.L2CharPosition;

/**
 * @author littlecrow Implementation of the Fear Effect
 */
public final class EffectFear extends L2Effect
{
	public static final int FEAR_RANGE = 500;
	
	private int _dX = -1;
	private int _dY = -1;
	
	public EffectFear(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.FEAR;
	}
	
	/** Notify started */
	@Override
	protected boolean onStart()
	{
		// Fear skills cannot be used by L2Player to L2Player.
		// Heroic Dread, Curse: Fear, Fear, Horror, Sword Symphony, Word of Fear, Mass Curse Fear and Saber Tooth Tiger Fear are the exceptions.
		if (getEffected() instanceof L2Player && getEffector() instanceof L2Player)
		{
			switch (getSkill().getId())
			{
				case 65:
				case 98:
				case 627:
				case 747:
				case 1092:
				case 1169:
				case 1272:
				case 1376:
				case 1381:
					// all ok
					break;
				default:
					return false;
			}
		}
		
		if (getEffected() instanceof L2NpcInstance 
				|| getEffected() instanceof L2DefenderInstance
				|| getEffected() instanceof L2FortCommanderInstance
				|| getEffected() instanceof L2SiegeFlagInstance
				|| getEffected() instanceof L2SiegeSummonInstance)
			return false;
		
		if (!getEffected().isAfraid())
		{
			if (getEffected().isCastingNow() && getEffected().canAbortCast())
				getEffected().abortCast();
			
			if (getEffected().getX() > getEffector().getX())
				_dX = 1;
			if (getEffected().getY() > getEffector().getY())
				_dY = 1;
			
			getEffected().startFear();
			onActionTime();
			return true;
		}
		return false;
	}
	
	/** Notify exited */
	@Override
	protected void onExit()
	{
		getEffected().stopFear(false);
	}
	
	@Override
	protected boolean onActionTime()
	{
		int posX = getEffected().getX();
		int posY = getEffected().getY();
		final int posZ = getEffected().getZ();
		
		if (getEffected().getX() > getEffector().getX())
			_dX = 1;
		if (getEffected().getY() > getEffector().getY())
			_dY = 1;
		
		posX += _dX * FEAR_RANGE;
		posY += _dY * FEAR_RANGE;
		
		final Location destiny = GeoData.getInstance().moveCheck(getEffected().getX(), getEffected().getY(),
				getEffected().getZ(), posX, posY, posZ, getEffected().getInstanceId());
		if (!(getEffected() instanceof L2PetInstance))
			getEffected().setRunning();
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(destiny));
		return true;
	}
}
