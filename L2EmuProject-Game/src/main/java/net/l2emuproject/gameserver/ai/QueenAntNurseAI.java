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
package net.l2emuproject.gameserver.ai;

import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.instancemanager.grandbosses.QueenAntManager;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.world.geodata.GeoData;
import net.l2emuproject.gameserver.world.object.L2Attackable;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Character.AIAccessor;

/**
 * This class manages AI of Larva for Queen Ant raid
 * 
 * @author hex1r0
 */
public class QueenAntNurseAI extends L2AttackableAI
{
	private long _lastTargetSwitch = 0;
	
	public QueenAntNurseAI(AIAccessor accessor)
	{
		super(accessor);
	}
	
	@Override
	protected void thinkActive()
	{
		L2Npc queen = QueenAntManager.getInstance().getQueenAntInstance();
		
		if (queen != null)
		{
			checkDistance(queen);
		}
	}
	
	@Override
	protected void thinkAttack()
	{
		L2Skill healSkill = SkillTable.getInstance().getInfo(4020, 1);
		
		if (healSkill == null)
			return;
		
		L2Npc newTarget = (L2Npc)_actor.getTarget();
		
		if (System.currentTimeMillis() - _lastTargetSwitch > 30000)
		{
			newTarget = chooseTarget();
			_lastTargetSwitch = System.currentTimeMillis();
		}
		
		if (newTarget == null)
			return;
		
		if (Util.checkIfInRange(healSkill.getCastRange(), _actor, newTarget, true))
		{
			if (!_actor.isSkillDisabled(healSkill.getId()))
			{
				stopFollow();
				clientStopMoving(null);
				_actor.setTarget(newTarget);
				_actor.doCast(healSkill);
			}
		}
		else
		{
			checkDistance(newTarget);
		}
	}
	
	private L2Npc chooseTarget()
	{
		final L2Attackable queen = QueenAntManager.getInstance().getQueenAntInstance();
		final L2Attackable larva = QueenAntManager.getInstance().getLarvaInstance();
		
		if (queen == null)
			return null;
		
		if (larva == null)
			return queen;
		
		final L2Character queenMostHated = queen.getMostHated();
		final L2Character larvaMostHated = larva.getMostHated();
		
		if (queenMostHated == null && larvaMostHated == null)
			return null;
		else if (queenMostHated != null && larvaMostHated == null)
			return queen;
		else if (queenMostHated == null && larvaMostHated != null)
			return larva;
		else if (queen.getHating(queenMostHated) > larva.getHating(larvaMostHated))
			return queen;
		else
			return larva;
	}
	
	private void checkDistance(L2Npc npc)
	{
		if (!GeoData.getInstance().canSeeTarget(_actor, npc))
		{
			_actor.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
		}
		else
		{
			_actor.setRunning();
			startFollow(npc);
		}
	}
}
