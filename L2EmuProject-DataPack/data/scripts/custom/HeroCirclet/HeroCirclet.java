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
package custom.HeroCirclet;

import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class HeroCirclet extends QuestJython
{
	public static final String QN = "HeroCirclet";
	
	private static final int[] npcIds =
	{
		31690,31769,31770,31771,31772
	};

	public HeroCirclet(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);
		for (int i : npcIds)
		{
			addStartNpc(i);
			addTalkId(i);
		}
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
			if (player.getInventory().getItemByItemId(6842) == null)
				st.giveItems(6842, 1);
			else
				htmltext = "already_have_circlet.htm";
		}
		else
			htmltext = "no_hero.htm";

		st.exitQuest(true);
		return htmltext;
	}

	public static void main(String[] args)
	{
		new HeroCirclet(-1, QN, "Hero Circlet", "custom");
	}
}
