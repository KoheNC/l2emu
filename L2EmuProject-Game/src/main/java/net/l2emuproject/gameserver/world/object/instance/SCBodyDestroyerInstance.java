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
package net.l2emuproject.gameserver.world.object.instance;

import net.l2emuproject.gameserver.entity.ai.L2CharacterAI;
import net.l2emuproject.gameserver.entity.ai.SCBodyDestroyerAI;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author hex1r0
 */
public class SCBodyDestroyerInstance extends L2MonsterInstance
{
	public SCBodyDestroyerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	protected L2CharacterAI initAI()
	{
		return new SCBodyDestroyerAI(new AIAccessor());
	}
}