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
package net.l2emuproject.gameserver.skills.l2skills;

import net.l2emuproject.gameserver.datatables.NpcTable;
import net.l2emuproject.gameserver.model.actor.instance.L2DecoyInstance;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.system.idfactory.IdFactory;
import net.l2emuproject.gameserver.templates.StatsSet;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;

public class L2SkillDecoy extends L2Skill
{
	private final int _npcId;
	private final int _summonTotalLifeTime;
	
	public L2SkillDecoy(StatsSet set)
	{
		super(set);
		_npcId = set.getInteger("npcId", 0);
		_summonTotalLifeTime = set.getInteger("summonTotalLifeTime", 20000);
	}
	
	@Override
	public void useSkill(L2Character caster, L2Character... targets)
	{
		if (caster.isAlikeDead() || !(caster instanceof L2Player))
			return;
		
		if (_npcId == 0)
			return;
		
		final L2Player activeChar = (L2Player)caster;
		
		if (activeChar.getPlayerObserver().inObserverMode())
			return;
		
		if (activeChar.getPet() != null || activeChar.isMounted())
			return;
		
		L2NpcTemplate DecoyTemplate = NpcTable.getInstance().getTemplate(_npcId);
		final L2DecoyInstance Decoy = new L2DecoyInstance(IdFactory.getInstance().getNextId(), DecoyTemplate, activeChar, this);
		Decoy.getStatus().setCurrentHp(Decoy.getMaxHp());
		Decoy.getStatus().setCurrentMp(Decoy.getMaxMp());
		Decoy.setHeading(activeChar.getHeading());
		activeChar.setDecoy(Decoy);
		L2World.getInstance().storeObject(Decoy);
		Decoy.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
	}
	
	/**
	 * @return Returns the itemConsume count over time.
	 */
	public final int getTotalLifeTime()
	{
		return _summonTotalLifeTime;
	}
}
