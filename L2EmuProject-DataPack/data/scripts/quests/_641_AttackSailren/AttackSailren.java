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
package quests._641_AttackSailren;

import org.apache.commons.lang.ArrayUtils;

import quests._126_TheNameOfEvil2.TheNameOfEvil2;

import net.l2emuproject.gameserver.network.serverpackets.MagicSkillUse;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.tools.random.Rnd;

/* fixed by lewzer
 * @author L0ngh0rn
 */
public final class AttackSailren extends QuestJython
{
	private static final String	QN				= "_641_AttackSailren";

	// NPCs
	private static final int	STATUE			= 32109;

	// MOBs
	private static final int[]	MOBS			=
												{ 22199, 22196, 22197, 22198, 22218, 22223 };

	// Quest Item
	private static final int	GAZKH_FRAGMENT	= 8782;
	private static final int	GAZKH			= 8784;

	// Chance
	private static final int	CHANCE			= 30;

	public AttackSailren(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(STATUE);
		addTalkId(STATUE);
		for (int i : MOBS)
			addKillId(i);

		questItemIds = new int[]
		{ GAZKH_FRAGMENT };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("32109-2.htm"))
			htmltext = "32109-2.htm";
		else if (event.equalsIgnoreCase("32109-3.htm"))
		{
			st.setState(State.STARTED);
			st.set(CONDITION, 1);
			st.sendPacket(SND_ACCEPT);
			htmltext = "32109-3.htm";
		}
		else if (event.equalsIgnoreCase("32109-4.htm"))
		{
			st.takeItems(GAZKH_FRAGMENT, 30);
			st.set(CONDITION, 2);
			st.sendPacket(SND_MIDDLE);
			htmltext = "32109-4.htm";
		}
		else if (event.equalsIgnoreCase("32109-5.htm"))
		{
			npc.broadcastPacket(new MagicSkillUse(npc, player, 5089, 1, 3000, 0));
			SystemMessage sm = new SystemMessage(110);
			sm.addString("Shilen's Protection");
			player.sendPacket(sm);
			st.giveItems(GAZKH, 1);
			st.set(CONDITION, 3);
			st.setState(State.COMPLETED);
			st.sendPacket(SND_FINISH);
			st.set(CONDITION, 0);
			htmltext = "32109-5.htm";
			st.exitQuest(true);
		}
		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (npc.getNpcId() == STATUE)
		{
			int cond = st.getInt(CONDITION);
			switch (cond)
			{
				case 0:
					// Check if player has completed the quest The Name of Evil 2
					if (player.getQuestState(TheNameOfEvil2.QN).getState() != State.COMPLETED)
						return htmltext;
					if (st.getState() == State.COMPLETED && st.getQuestItemsCount(GAZKH) == 1)
						htmltext = QUEST_DONE;
					else
						htmltext = "32109-1.htm";
					break;
				case 1:
					if (st.getQuestItemsCount(GAZKH_FRAGMENT) >= 30)
					{
						startQuestTimer("32109-4.htm", 0, npc, player);
						htmltext = "32109-4.htm";
					}
					else
						htmltext = "<html><body> Please come back once you have 30 Gazkh Fragments. </body></html>";
					break;
				case 2:
					startQuestTimer("32109-5.htm", 0, npc, player);
					htmltext = "32109-5.htm";
					break;
			}
		}
		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2Player player, boolean isPet)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
			return null;

		if (ArrayUtils.contains(MOBS, npc.getNpcId()) && CHANCE >= Rnd.get(100) && st.getInt(CONDITION) == 1 && st.getQuestItemsCount(GAZKH_FRAGMENT) < 30)
		{
			st.giveItems(GAZKH_FRAGMENT, 1);
			st.sendPacket(SND_ITEM_GET);
		}

		return null;
	}

	public static void main(String[] args)
	{
		new AttackSailren(641, QN, "Attack Sailren");
	}
}
