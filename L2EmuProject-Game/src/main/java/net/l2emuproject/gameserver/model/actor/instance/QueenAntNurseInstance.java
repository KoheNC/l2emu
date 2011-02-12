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
package net.l2emuproject.gameserver.model.actor.instance;

import net.l2emuproject.gameserver.ai.L2CharacterAI;
import net.l2emuproject.gameserver.ai.QueenAntNurseAI;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author hex1r0
 */
public class QueenAntNurseInstance extends L2MonsterInstance
{
	public QueenAntNurseInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	protected L2CharacterAI initAI()
	{
		return new QueenAntNurseAI(new AIAccessor());
	}
}
