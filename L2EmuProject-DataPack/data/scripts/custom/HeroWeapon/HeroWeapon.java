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
package custom.HeroWeapon;

import org.apache.commons.lang.ArrayUtils;

import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class HeroWeapon extends QuestJython
{
	public static final String QN = "HeroWeapon";
	
	private final static int[] npcIds =
	{
		31690,31769,31770,31771,31772
	};

	private final static int[] weaponIds =
	{
		6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621,9388,9389,9390
	};

	public HeroWeapon(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);
		for (int i : npcIds)
		{
			addStartNpc(i);
			addTalkId(i);
		}
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		QuestState st = player.getQuestState(getName());

		int weaponId = Integer.valueOf(event);
		if (ArrayUtils.contains(weaponIds, weaponId))
			st.giveItems(weaponId, 1);

		st.exitQuest(true);
		return null;
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
			st = newQuestState(player);

		if (player.isHero())
		{
			if (hasHeroWeapon(player))
			{
				htmltext = "already_have_weapon.htm";
				st.exitQuest(true);
			}
			else
				htmltext = "weapon_list.htm";
		}
		else
		{
			htmltext = "no_hero.htm";
			st.exitQuest(true);
		}

		return htmltext;
	}

	private final boolean hasHeroWeapon(L2Player player)
	{
		for (int i : weaponIds)
		{
			if (player.getInventory().getItemByItemId(i) != null)
				return true;
		}

		return false;
	}

	public static void main(String[] args)
	{
		new HeroWeapon(-1, QN, "Hero Weapon", "custom");
	}
}
