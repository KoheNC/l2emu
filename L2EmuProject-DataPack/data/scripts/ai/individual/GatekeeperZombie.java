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
package ai.individual;

import net.l2emuproject.gameserver.ai.CtrlIntention;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.model.actor.L2Attackable;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.model.skill.L2Skill;
import ai.L2AttackableAIScript;

/**
 * @author Psycho(killer1888) / L2jFree
 */

public class GatekeeperZombie extends L2AttackableAIScript
{
	private static final int[] GATEKEEPER = {18343};
	private static final int[] ITEMS      = {8064, 8065, 8067};

	public GatekeeperZombie(int questId, String name, String descr)
	{
		super(questId, name, descr);
		registerMobs(GATEKEEPER);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		boolean isAuthorized = false;
		L2ItemInstance item;

		if (!npc.isInCombat() && !isPet)
		{
			for (int itemId : ITEMS)
			{
				item = player.getInventory().getItemByItemId(itemId);
				if (item != null)
				{
					isAuthorized = true;
					break;
				}
			}
			if (!isAuthorized)
			{
				npc.setTarget(player);
				((L2Attackable) npc).addDamageHate(player, 0, 999);
				L2Skill skill = SkillTable.getInstance().getInfo(5043, 9);
				npc.doCast(skill);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
			else
			{
				L2Attackable actor = (L2Attackable) npc;
    			int hate = actor.getHating(player);
    			if (hate == 1)
    				actor.reduceHate(player, hate);
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new GatekeeperZombie(-1, "GatekeeperZombie", "ai");
	}
}
