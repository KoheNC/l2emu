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
package ai.npc;

import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.world.geodata.GeoData;
import net.l2emuproject.gameserver.world.object.L2Npc;
import ai.L2AttackableAIScript;

/**
 * @author SYS & Diamond, Rewritten by Angy
 */
public final class Sandstorm extends L2AttackableAIScript
{
	private static final String		QN			= "Sandstorm";
	private static final int		SANDSTORM	= 32350;

	private static final L2Skill	SKILL1		= SkillTable.getInstance().getInfo(5435, 1);
	private static final L2Skill	SKILL2		= SkillTable.getInstance().getInfo(5494, 1);

	private long					_lastThrow	= 0;

	public Sandstorm(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addAggroRangeEnterId(SANDSTORM);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (npc == null)
			return null;

		if (_lastThrow + 5000 < System.currentTimeMillis())
		{
			if (player != null && !player.isAlikeDead() && !player.isInvul() && player.isVisible() && GeoData.getInstance().canSeeTarget(npc, player))
			{
				npc.setTarget(player);
				npc.doCast(SKILL1);
				npc.doCast(SKILL2);
				_lastThrow = System.currentTimeMillis();
			}
		}

		return super.onAggroRangeEnter(npc, player, isPet);
	}

	public static void main(String[] args)
	{
		new Sandstorm(-1, QN, "ai");
	}
}
