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
package quests._111_ElrokianHuntersProof;

import org.apache.commons.lang.ArrayUtils;

import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.party.L2Party;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.tools.random.Rnd;

/**
 * @author L0ngh0rn
 */
public final class ElrokianHuntersProof extends QuestJython
{
	private static final String	QN			= "_111_ElrokianHuntersProof";

	// NPCs
	private static final int	MARQUEZ		= 32113;
	private static final int	MUSHIKA		= 32114;
	private static final int	ASHAMAH		= 32115;
	private static final int	KIRIKASHIN	= 32116;

	// Quest Item
	private static final int	FRAGMENT	= 8768;

	// Chance
	private static final int[]	CHANCE		=
											{ 25, 75 };

	// MOBs
	private static final int[]	MOBS1		=
											{ 22196, 22197, 22198, 22218 };
	private static final int[]	MOBS2		=
											{ 22200, 22201, 22202, 22219 };
	private static final int[]	MOBS3		=
											{ 22208, 22209, 22210, 22221 };
	private static final int[]	MOBS4		=
											{ 22203, 22204, 22205, 22220 };

	public ElrokianHuntersProof(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(MARQUEZ);

		addTalkId(MARQUEZ);
		addTalkId(MUSHIKA);
		addTalkId(ASHAMAH);
		addTalkId(KIRIKASHIN);

		for (int i : MOBS1)
			addKillId(i);
		for (int i : MOBS2)
			addKillId(i);
		for (int i : MOBS3)
			addKillId(i);
		for (int i : MOBS4)
			addKillId(i);

		questItemIds = new int[]
		{ FRAGMENT };
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		int cond = st.getInt(CONDITION);
		if (st.getState() == State.COMPLETED)
			htmltext = QUEST_DONE;
		else
		{
			L2Party party = st.getPlayer().getParty();
			if (party != null)
			{
				int lvl = st.getPlayer().getLevel();
				L2PcInstance partyLeader = party.getLeader();
				if (lvl >= 75 && partyLeader == player)
				{
					switch (npc.getNpcId())
					{
						case MARQUEZ:
							switch (cond)
							{
								case 0:
									st.set(CONDITION, 1);
									st.sendPacket(SND_ACCEPT);
									st.setState(State.STARTED);
									htmltext = "32113-1.htm";
									break;
								case 3:
									st.set(CONDITION, 4);
									st.sendPacket(SND_MIDDLE);
									htmltext = "32113-2.htm";
									break;
								case 5:
									if (st.getQuestItemsCount(FRAGMENT) >= 50)
									{
										st.takeItems(FRAGMENT, -1);
										st.set(CONDITION, 6);
										st.sendPacket(SND_MIDDLE);
										htmltext = "32113-3.htm";
									}
									break;
							}
							break;
						case MUSHIKA:
							if (cond == 1)
							{
								st.set(CONDITION, 2);
								st.sendPacket(SND_MIDDLE);
								htmltext = "32114-1.htm";
							}
							break;
						case ASHAMAH:
							switch (cond)
							{
								case 2:
									st.set(CONDITION, 3);
									st.sendPacket(SND_MIDDLE);
									htmltext = "32115-1.htm";
									break;
								case 8:
									st.set(CONDITION, 9);
									st.sendPacket(SND_MIDDLE);
									htmltext = "32115-2.htm";
									break;
								case 9:
									st.set(CONDITION, 10);
									st.sendPacket(SND_MIDDLE);
									htmltext = "32115-3.htm";
									break;
								case 11:
									st.set(CONDITION, 12);
									st.sendPacket(SND_MIDDLE);
									st.giveItems(8773, 1);
									htmltext = "32115-5.htm";
									break;
							}
							break;
						case KIRIKASHIN:
							switch (cond)
							{
								case 6:
									st.set(CONDITION, 8);
									st.playSound("EtcSound.elcroki_song_full");
									htmltext = "32116-1.htm";
									break;

								case 12:
									if (st.getQuestItemsCount(8773) >= 1)
									{
										st.takeItems(8773, 1);
										st.giveItems(8763, 1);
										st.giveItems(8764, 100);
										st.giveAdena(1071691);
										st.addExpAndSp(553524, 55538);
										st.sendPacket(SND_FINISH);
										st.exitQuest(false);
										htmltext = "32116-2.htm";
									}
									break;
							}
							break;
					}
				}
			}
		}
		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		String htmltext = "";
		L2Party party = player.getParty();
		if (party == null)
			return htmltext;

		QuestState st = party.getLeader().getQuestState(QN);

		if (st == null)
			return htmltext;
		if (st.getState() != State.STARTED)
			return htmltext;

		int cond = st.getInt(CONDITION);
		final int npcId = npc.getNpcId();

		switch (cond)
		{
			case 4:
				if (ArrayUtils.contains(MOBS1, npcId) && CHANCE[0] > Rnd.get(100))
				{
					st.giveItems(FRAGMENT, 1);
					if (st.getQuestItemsCount(FRAGMENT) <= 49)
						st.sendPacket(SND_ITEM_GET);
					else
					{
						st.set(CONDITION, 5);
						st.sendPacket(SND_MIDDLE);
					}
				}
				break;
			case 10:
				if (ArrayUtils.contains(MOBS2, npcId) && CHANCE[1] > Rnd.get(100))
				{
					st.giveItems(8770, 1);
					if (st.getQuestItemsCount(8770) <= 9)
						st.sendPacket(SND_ITEM_GET);
				}
				else if (ArrayUtils.contains(MOBS3, npcId) && CHANCE[1] > Rnd.get(100))
				{
					st.giveItems(8772, 1);
					if (st.getQuestItemsCount(8772) <= 9)
						st.sendPacket(SND_ITEM_GET);
				}
				else if (ArrayUtils.contains(MOBS4, npcId) && CHANCE[1] > Rnd.get(100))
				{
					st.giveItems(8771, 1);
					if (st.getQuestItemsCount(8771) <= 9)
						st.sendPacket(SND_ITEM_GET);
				}
				else if (st.getQuestItemsCount(8770) >= 10 && st.getQuestItemsCount(8771) >= 10 && st.getQuestItemsCount(8772) >= 10)
				{
					st.set(CONDITION, 11);
					st.sendPacket(SND_MIDDLE);
				}
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new ElrokianHuntersProof(111, QN, "Elrokian Hunters Proof");
	}
}
