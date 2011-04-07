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
package quests._194_SevenSignContractOfMammon;

import quests._193_SevenSignDyingMessage.SevenSignDyingMessage;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.skills.L2Effect;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author L0ngh0rn
 * @since 2010-06-26 by Gnacik (Based on official server Franz)
 */
public final class SevenSignContractOfMammon extends QuestJython
{
	public static final String	QN				= "_194_SevenSignContractOfMammon";

	// NPCs
	private static final int	ATHEBALDT		= 30760;
	private static final int	COLIN			= 32571;
	private static final int	FROG			= 32572;
	private static final int	TESS			= 32573;
	private static final int	KUTA			= 32574;
	private static final int	CLAUDIA			= 31001;

	// Quest Item
	private static final int	INTRODUCTION	= 13818;
	private static final int	FROG_KING_BEAD	= 13820;
	private static final int	CANDY_POUCH		= 13821;
	private static final int	NATIVES_GLOVE	= 13819;

	public SevenSignContractOfMammon(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(ATHEBALDT);

		addTalkId(ATHEBALDT);
		addTalkId(COLIN);
		addTalkId(FROG);
		addTalkId(TESS);
		addTalkId(KUTA);
		addTalkId(CLAUDIA);

		questItemIds = new int[]
		{ INTRODUCTION, FROG_KING_BEAD, CANDY_POUCH, NATIVES_GLOVE };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("30760-02.htm"))
		{
			st.set(CONDITION, 1);
			st.setState(State.STARTED);
			st.sendPacket(SND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30760-07.htm"))
		{
			st.set(CONDITION, 3);
			st.giveItems(INTRODUCTION, 1);
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32571-04.htm"))
		{
			st.set(CONDITION, 4);
			st.takeItems(INTRODUCTION, 1);
			transformPlayer(player, 6201);
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32571-06.htm") || event.equalsIgnoreCase("32571-14.htm") || event.equalsIgnoreCase("32571-22.htm"))
		{
			if (player.getPlayerTransformation().isTransformed())
				player.getPlayerTransformation().untransform();
		}
		else if (event.equalsIgnoreCase("32571-08.htm"))
			transformPlayer(player, 6201);
		else if (event.equalsIgnoreCase("32572-04.htm"))
		{
			st.set(CONDITION, 5);
			st.giveItems(FROG_KING_BEAD, 1);
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32571-10.htm"))
		{
			st.set(CONDITION, 6);
			st.takeItems(FROG_KING_BEAD, 1);
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32571-12.htm"))
		{
			st.set(CONDITION, 7);
			transformPlayer(player, 6202);
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32571-16.htm"))
			transformPlayer(player, 6202);
		else if (event.equalsIgnoreCase("32573-03.htm"))
		{
			st.set(CONDITION, 8);
			st.giveItems(CANDY_POUCH, 1);
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32571-18.htm"))
		{
			st.set(CONDITION, 9);
			st.takeItems(CANDY_POUCH, 1);
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32571-20.htm"))
		{
			st.set(CONDITION, 10);
			transformPlayer(player, 6203);
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32571-24.htm"))
			transformPlayer(player, 6203);
		else if (event.equalsIgnoreCase("32574-04.htm"))
		{
			st.set(CONDITION, 11);
			st.giveItems(NATIVES_GLOVE, 1);
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32571-26.htm"))
		{
			st.set(CONDITION, 12);
			st.takeItems(NATIVES_GLOVE, 1);
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equals("10"))
		{

			st.set(CONDITION, 2);
			st.sendPacket(SND_MIDDLE);
			player.showQuestMovie(Integer.parseInt(event));
			return null;
		}
		else if (event.equalsIgnoreCase("31001-03.htm"))
		{
			st.addExpAndSp(52518015, 5817677);
			st.unset(CONDITION);
			st.setState(State.COMPLETED);
			st.exitQuest(false);
			st.sendPacket(SND_FINISH);
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

		int cond = st.getInt(CONDITION);

		switch (npc.getNpcId())
		{
			case ATHEBALDT:
				QuestState st2 = player.getQuestState(SevenSignDyingMessage.QN);
				if (st.getState() == State.COMPLETED)
					htmltext = QUEST_DONE;
				else if (st2 != null && st2.getState() == State.COMPLETED && st.getState() == State.CREATED && player.getLevel() >= 79)
					htmltext = "30760-01.htm";
				else
				{
					switch (cond)
					{
						case 1:
							htmltext = "30760-03.htm";
							break;
						case 2:
							htmltext = "30760-05.htm";
							break;
						case 3:
							htmltext = "30760-08.htm";
							break;
						default:
							htmltext = "30760-00.htm";
							st.exitQuest(true);
							break;
					}
				}
				break;
			case COLIN:
				switch (cond)
				{
					case 3:
						htmltext = "32571-01.htm";
						break;
					case 4:
						if (checkPlayer(player, 6201))
							htmltext = "32571-05.htm";
						else
							htmltext = "32571-07.htm";
						break;
					case 5:
						htmltext = "32571-09.htm";
						break;
					case 6:
						htmltext = "32571-11.htm";
						break;
					case 7:
						if (checkPlayer(player, 6202))
							htmltext = "32571-13.htm";
						else
							htmltext = "32571-15.htm";
						break;
					case 8:
						htmltext = "32571-17.htm";
						break;
					case 9:
						htmltext = "32571-19.htm";
						break;
					case 10:
						if (checkPlayer(player, 6203))
							htmltext = "32571-21.htm";
						else
							htmltext = "32571-23.htm";
						break;
					case 11:
						htmltext = "32571-25.htm";
						break;
				}
				break;
			case FROG:
				if (checkPlayer(player, 6201))
				{
					switch (cond)
					{
						case 4:
							htmltext = "32572-01.htm";
							break;
						case 5:
							htmltext = "32572-05.htm";
							break;
					}
				}
				else
					htmltext = "32572-00.htm";
				break;
			case TESS:
				if (checkPlayer(player, 6202))
				{
					switch (cond)
					{
						case 7:
							htmltext = "32573-01.htm";
							break;
						case 8:
							htmltext = "32573-04.htm";
							break;
					}
				}
				else
					htmltext = "32573-00.htm";
				break;
			case KUTA:
				if (checkPlayer(player, 6203))
				{
					switch (cond)
					{
						case 10:
							htmltext = "32574-01.htm";
							break;
						case 11:
							htmltext = "32574-05.htm";
							break;
					}
				}
				else
					htmltext = "32574-00.htm";
				break;
			case CLAUDIA:
				if (cond == 12)
					htmltext = "31001-01.htm";
				break;
		}
		return htmltext;
	}

	private void transformPlayer(L2Player player, Integer transid)
	{
		if (player.getPlayerTransformation().isTransformed())
			player.getPlayerTransformation().untransform();
		player.stopAllEffects();
		SkillTable.getInstance().getInfo(transid, 1).getEffects(player, player);
	}

	private boolean checkPlayer(L2Player player, Integer transid)
	{
		L2Effect effect = player.getFirstEffect(transid);
		if (effect != null)
			return true;
		return false;
	}

	public static void main(String[] args)
	{
		new SevenSignContractOfMammon(194, QN, "Seven Sign Contract Of Mammon");
	}
}
