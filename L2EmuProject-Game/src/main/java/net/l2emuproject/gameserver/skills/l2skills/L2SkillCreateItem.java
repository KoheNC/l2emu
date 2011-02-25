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

import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.templates.StatsSet;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.tools.random.Rnd;


/**
 * @author Nemesiss
 */
public class L2SkillCreateItem extends L2Skill
{
	private final int[] _createItemId;
	private final int _createItemCount;
	private final int _randomCount;
	
	public L2SkillCreateItem(StatsSet set)
	{
		super(set);
		_createItemId = set.getIntegerArray("create_item_id");
		_createItemCount = set.getInteger("create_item_count");
		_randomCount = set.getInteger("random_count", 0);
	}
	
	@Override
	public void useSkill(L2Character activeChar, L2Character... targets)
	{
		if (activeChar.isAlikeDead())
			return;
		
		if (activeChar instanceof L2PcInstance)
		{
			int count = _createItemCount + Rnd.nextInt(_randomCount);
			int rndid = Rnd.nextInt(_createItemId.length);
			((L2PcInstance)activeChar).addItem("Skill", _createItemId[rndid], count, activeChar, true);
		}
	}
}
