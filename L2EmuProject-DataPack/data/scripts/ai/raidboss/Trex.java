/* This program is free software: you can redistribute it and/or modify it under
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
package ai.raidboss;

import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.world.L2Object;
import net.l2emuproject.gameserver.skills.L2Skill;
import ai.L2AttackableAIScript;

/**
 * @author InsOmnia
 */
public class Trex extends L2AttackableAIScript
{
	private static final int[]	npcIds	=
										{ 22215, 22216, 22217 };

	public Trex(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int i = 0; i < npcIds.length; i++)
		{
			addSkillSeeId(npcIds[i]);
			addKillId(npcIds[i]);
		}
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance player, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		for (L2Object obj : targets)
		{
			if (npc != obj)
				return null;
		}
		int trexMaxHp = npc.getMaxHp();
		int skillId = skill.getId();
		int minhp = 0;
		int maxhp = 0;
		double trexCurrentHp = npc.getStatus().getCurrentHp();
		switch (skillId)
		{
			case 3626:
				minhp = (60 * trexMaxHp) / 100;
				maxhp = (100 * trexMaxHp) / 100;
				break;
			case 3267:
				minhp = (25 * trexMaxHp) / 100;
				maxhp = (65 * trexMaxHp) / 100;
				break;
			case 3268:
				minhp = (0 * trexMaxHp) / 100;
				maxhp = (25 * trexMaxHp) / 100;
				break;
		}
		if (trexCurrentHp < minhp || trexCurrentHp > maxhp)
		{
			npc.stopSkillEffects(skillId);
			player.sendMessage("The conditions are not right to use this skill now.");
		}
		return super.onSkillSee(npc, player, skill, targets, isPet);
	}

	public static void main(String[] args)
	{
		new Trex(-1, "Trex", "ai");
	}
}
