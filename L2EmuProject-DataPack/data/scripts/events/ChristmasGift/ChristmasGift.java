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
package events.ChristmasGift;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.manager.QuestManager;
import net.l2emuproject.gameserver.model.quest.Quest;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author lord_rex
 * based on Elektra's Python script.
 */
public class ChristmasGift extends QuestJython
{
	private static final String	QN			= "ChristmasGift";

	private static final int	NPC_ID		= 31864;

	private static final int	BW_GRADE_ID	= 148;
	private static final int	BA_GRADE_ID	= 2381;
	private static final int	DW_GRADE_ID	= 225;
	private static final int	DA_GRADE_ID	= 396;
	private static final int	CW_GRADE_ID	= 303;
	private static final int	CA_GRADE_ID	= 356;
	private static final int	METAL_HARD	= 5231;
	private static final int	DUAL_CRAFT	= 5126;
	private static final int	COAL_ID		= 1870;

	public ChristmasGift(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(NPC_ID);
		addTalkId(NPC_ID);
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;

		if (event.equalsIgnoreCase("1"))
		{
			st.set(ID, 0);
			st.set(CONDITION, 1);
			st.setState(State.STARTED);
			st.sendPacket(SND_ACCEPT);
			htmltext = "31864-03.htm";
		}

		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2Player player)
	{
		String htmltext = "<html><body>I have no tasks for you right now.</body></html>";

		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			Quest quest = QuestManager.getInstance().getQuest(getName());
			st = quest.newQuestState(player);
		}

		int npcId = npc.getNpcId();

		if (st.getState() == State.CREATED)
		{
			st.setState(State.CREATED);
			st.set(CONDITION, 0);
			st.set("onlyone", "0");
			st.set(ID, 0);
		}
		if (npcId == NPC_ID && st.getInt(CONDITION) == 0)
		{
			if (st.getInt(CONDITION) < 15)
			{
				if (st.getPlayer().getLevel() < 19)
				{
					htmltext = "31864-01.htm";
					st.exitQuest(true);
				}
				else
				{
					htmltext = "31864-02.htm";
					return htmltext;
				}
			}
			else
			{
				htmltext = "31864-01.htm";
				st.exitQuest(true);
			}
		}
		else if ((npcId == NPC_ID && st.getInt(CONDITION) > 0))
		{
			if (st.getPlayer().getLevel() < 19)
				htmltext = "31864-04.htm";
			else
			{
				if (st.getInt(ID) != 291)
				{
					st.set(ID, 291);
					htmltext = "31864-05.htm";
					st.set(CONDITION, 1);
					st.exitQuest(false);
					st.sendPacket(SND_FINISH);
					int n = st.getRandom(100);

					if (n <= 2)
						st.giveItems(BW_GRADE_ID, 1);
					else if (n <= 4)
						st.giveItems(BA_GRADE_ID, 1);
					else if (n <= 8)
						st.giveItems(CW_GRADE_ID, 1);
					else if (n <= 13)
						st.giveItems(CA_GRADE_ID, 1);
					else if (n <= 18)
						st.giveItems(METAL_HARD, 1);
					else if (n <= 24)
						st.giveItems(DUAL_CRAFT, 1);
					else if (n <= 30)
						st.giveItems(DW_GRADE_ID, 1);
					else if (n <= 35)
						st.giveItems(DA_GRADE_ID, 1);
					else
						st.giveItems(COAL_ID, 50);
				}
			}
		}

		return htmltext;
	}

	public static void main(String[] args)
	{
		if (Config.ALLOW_CHRISTMAS_GIFT)
		{
			new ChristmasGift(998, QN, "events");
			_log.info("Events: Christmas Gift is loaded.");
		}
		else
			_log.info("Events: Christmas Gift is disabled.");
	}
}
