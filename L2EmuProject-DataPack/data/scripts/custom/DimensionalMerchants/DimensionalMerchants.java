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
package custom.DimensionalMerchants;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author L0ngh0rn
 * 
 * rewritten by Intrepid
 */
public final class DimensionalMerchants extends QuestJython
{
	private static final String	QN		= "DimensionalMerchants";

	private static final int	NPC		= 32478;

	private static final int[]	ITEM	= { 
		13273, // Hunting Helper Exchange Coupon - 5 Hours
		13383, // Hunting Helper Exchange Coupon - 5 Hours (Event)
		13274, // Hunting Helper Exchange Coupon - 7 Days
		};
	
	private static final int[] HQ_ITEM = {
		14065, // High Quality Hunting Helper Exchange Coupon
		14074  // High Quality Hunting Helper Exchange Coupon (Event)
	};
	
	private static final String[] HUNTING_HELPER = {
		"13017", // White Weasel
		"13018", // Fairy Princess
		"13019", // Wild Beast Fighter
		"13020"  // Fox Shaman
	};
	
	private static final String[] HQ_HUNTING_HELPER = {
		"13548", // Toy Knight
		"13549", // Soul Monk
		"13550", // Owl Monk
		"13551"  // Turtle Ascetic
	};

	public DimensionalMerchants(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(NPC);
		addTalkId(NPC);
		addFirstTalkId(NPC);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return null;
		
		if (event.equalsIgnoreCase(HUNTING_HELPER[0]) || 
				event.equalsIgnoreCase(HUNTING_HELPER[1]) || 
				event.equalsIgnoreCase(HUNTING_HELPER[2]) || 
				event.equalsIgnoreCase(HUNTING_HELPER[3]))
		{
			int coupon5h = (int) st.getQuestItemsCount(ITEM[0]);
			int eventCoupon5h = (int) st.getQuestItemsCount(ITEM[1]);
			int coupon7d = (int) st.getQuestItemsCount(ITEM[2]);
			if (coupon7d >= 1)
			{
				st.takeItems(ITEM[0], 1);
				st.giveItems(Integer.valueOf(event), 1);
				st.exitQuest(true);
				return htmltext;
			}
			else if (coupon5h >= 1)
			{
				st.takeItems(ITEM[2], 1);
				st.giveItems(Integer.valueOf(event), 1);
				st.exitQuest(true);
				return htmltext;
			}
			else if (eventCoupon5h >= 1)
			{
				st.takeItems(ITEM[1], 1);
				st.giveItems(Integer.valueOf(event), 1);
				st.exitQuest(true);
				return htmltext;
			}
			else
			{
				htmltext = "32478-11.htm";
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase(HQ_HUNTING_HELPER[0]) || 
				event.equalsIgnoreCase(HQ_HUNTING_HELPER[1]) || 
				event.equalsIgnoreCase(HQ_HUNTING_HELPER[2]) || 
				event.equalsIgnoreCase(HQ_HUNTING_HELPER[3]))
		{
			int hqCoupon5h = (int) st.getQuestItemsCount(HQ_ITEM[0]);
			int hqEventCoupon5h = (int) st.getQuestItemsCount(HQ_ITEM[1]);
			if (hqCoupon5h >= 1)
			{
				st.takeItems(HQ_ITEM[0], 1);
				st.giveItems(Integer.valueOf(event), 1);
				st.exitQuest(true);
				return htmltext;
			}
			else if (hqEventCoupon5h >= 1)
			{
				st.takeItems(HQ_ITEM[1], 1);
				st.giveItems(Integer.valueOf(event), 1);
				st.exitQuest(true);
				return htmltext;
			}
			else
			{
				htmltext = "32478-11.htm";
				st.exitQuest(true);
			}
		}

		return htmltext;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2Player player)
	{
		if (Config.ALT_ENABLE_DIMENSIONAL_MERCHANTS)
		{
			QuestState st = player.getQuestState(QN);

			if (st == null)
				st = newQuestState(player);

			return "32478.htm";
		}
		return "32478-na.htm";
	}

	public static void main(String[] args)
	{
		new DimensionalMerchants(1003, QN, "custom");
	}
}
