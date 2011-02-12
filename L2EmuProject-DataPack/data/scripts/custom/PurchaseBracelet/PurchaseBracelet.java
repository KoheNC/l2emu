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
package custom.PurchaseBracelet;

import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;

/**
 * @author L0ngh0rn
 * @since Converted by K4N4BS.
 */
public final class PurchaseBracelet extends QuestJython
{
	public static final String	QN					= "PurchaseBracelet";

	// NPCs
	private static final int	ALEXANDRIA			= 30098;

	// Quest Items
	private static final int	ANGEL_BRACELET		= 12779;
	private static final int	DEVIL_BRACELET		= 12780;

	private static final int	BIG_RED_NIMBLE_FISH	= 6471;
	private static final int	GREAT_CODRAN		= 5094;
	private static final int	MEMENTO_MORI		= 9814;
	private static final int	DRAGON_HEART		= 9815;
	private static final int	EARTH_EGG			= 9816;
	private static final int	NONLIVING_NUCLEUS	= 9817;

	public PurchaseBracelet(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		addStartNpc(ALEXANDRIA);
		addTalkId(ALEXANDRIA);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (st.getQuestItemsCount(BIG_RED_NIMBLE_FISH) >= 25 && st.getQuestItemsCount(GREAT_CODRAN) >= 50 && st.getQuestItemsCount(MEMENTO_MORI) >= 4
				&& st.getQuestItemsCount(EARTH_EGG) >= 5 && st.getQuestItemsCount(NONLIVING_NUCLEUS) >= 5 && st.getQuestItemsCount(DRAGON_HEART) >= 3
				&& st.getQuestItemsCount(57) >= 7500000)
		{
			st.takeItems(BIG_RED_NIMBLE_FISH, 25);
			st.takeItems(GREAT_CODRAN, 50);
			st.takeItems(MEMENTO_MORI, 4);
			st.takeItems(EARTH_EGG, 5);
			st.takeItems(NONLIVING_NUCLEUS, 5);
			st.takeItems(DRAGON_HEART, 3);
			st.takeAdena(7500000);

			if (event.equalsIgnoreCase("Little_Angel"))
				st.giveItems(ANGEL_BRACELET, 1);
			else if (event.equalsIgnoreCase("Little_Devil"))
				st.giveItems(DEVIL_BRACELET, 1);
		}
		else
			htmltext = "30098-no.htm";

		st.exitQuest(true);
		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(QN);

		if (st == null)
			st = newQuestState(player);

		return "30098.htm";
	}

	public static void main(String[] args)
	{
		new PurchaseBracelet(8004, QN, "Purchase Bracelet", "custom");
	}
}