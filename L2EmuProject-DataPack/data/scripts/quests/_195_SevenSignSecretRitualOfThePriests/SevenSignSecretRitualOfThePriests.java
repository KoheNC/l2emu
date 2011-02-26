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
package quests._195_SevenSignSecretRitualOfThePriests;

import quests._194_SevenSignContractOfMammon.SevenSignContractOfMammon;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author L0ngh0rn
 */
public final class SevenSignSecretRitualOfThePriests extends QuestJython
{
	public static final String	QN							= "_195_SevenSignSecretRitualOfThePriests";

	// NPCs 
	private static final int	CLAUDIAATHEBALT				= 31001;
	private static final int	JOHN						= 32576;
	private static final int	RAYMOND						= 30289;
	private static final int	LIGHTOFDAWN					= 32575;
	private static final int	DEVICE						= 32578;
	private static final int	DEVICE2						= 36600;
	private static final int	IASONHEINE					= 30969;
	private static final int	PASSWORDENTRYDEVICE			= 32577;
	private static final int	SHKAF						= 32580;
	private static final int	BLACK						= 32579;
	// Quest Items
	private static final int	EMPERORSHUNAIMANCONTRACT	= 13823;
	private static final int	IDENTITYCARD				= 13822;
	// Transformation's skills
	private static final int	GUARDOFDAWN					= 6204;

	public SevenSignSecretRitualOfThePriests(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(CLAUDIAATHEBALT);

		addTalkId(CLAUDIAATHEBALT);
		addTalkId(JOHN);
		addTalkId(RAYMOND);
		addTalkId(LIGHTOFDAWN);
		addTalkId(DEVICE);
		addTalkId(DEVICE2);
		addTalkId(PASSWORDENTRYDEVICE);
		addTalkId(SHKAF);
		addTalkId(BLACK);
		addTalkId(IASONHEINE);

		questItemIds = new int[]
		{ IDENTITYCARD, EMPERORSHUNAIMANCONTRACT };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("31001-02.htm"))
		{
			st.setState(State.STARTED);
			st.sendPacket(SND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31001-05.htm"))
		{
			st.set(CONDITION, 1);
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32576-02.htm"))
		{
			st.giveItems(IDENTITYCARD, 1);
			st.set(CONDITION, 2);
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("30289-04.htm"))
		{
			player.stopAllEffects();
			SkillTable.getInstance().getInfo(6204, 1).getEffects(player, player);
			st.set(CONDITION, 3);
		}
		else if (event.equalsIgnoreCase("30289-07.htm"))
		{
			player.stopAllEffects();
		}
		else if (event.equalsIgnoreCase("30969-03.htm"))
		{
			st.addExpAndSp(52518015, 5817677);
			st.unset(CONDITION);
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

		if (st.getState() == State.COMPLETED)
			htmltext = QUEST_DONE;
		else
		{
			switch (npc.getNpcId())
			{
				case CLAUDIAATHEBALT:
					QuestState st1 = player.getQuestState(SevenSignContractOfMammon.QN);
					if (st1 != null && st1.getState() == State.COMPLETED)
					{
						switch (cond)
						{
							case 0:
								if (player.getLevel() >= 79)
									htmltext = "31001-01.htm";
								else
								{
									htmltext = "31001-0a.htm";
									st.exitQuest(true);
								}
								break;
							case 1:
								htmltext = "31001-06.htm";
								break;
						}
					}
					else
						htmltext = "31001-0b.htm";
					break;
				case JOHN:
					switch (cond)
					{
						case 1:
							htmltext = "32576-01.htm";
							break;
						case 2:
							htmltext = "32576-03.htm";
							break;
					}
					break;
				case RAYMOND:
					switch (cond)
					{
						case 2:
							htmltext = "30289-01.htm";
							break;
						case 3:
							htmltext = "30289-06.htm";
							break;
						case 4:
							htmltext = "30289-08.htm";
							player.stopAllEffects();
							st.giveItems(7128, 1);
							st.sendPacket(SND_FINISH);
							break;
					}
					break;
				case LIGHTOFDAWN:
					if (cond == 3 && st.getQuestItemsCount(IDENTITYCARD) == 1 && player.getFirstEffect(GUARDOFDAWN) != null)
						htmltext = "32575-02.htm";
					else
						htmltext = "32575-01.htm";
					break;
				case DEVICE:
					if (player.getFirstEffect(GUARDOFDAWN) != null)
						htmltext = "32578-03.htm";
					else
						htmltext = "32578-02.htm";
					break;
				case DEVICE2:
					if (player.getFirstEffect(GUARDOFDAWN) != null)
						htmltext = "32578-03.htm";
					else
						htmltext = "32578-02.htm";
					break;
				case PASSWORDENTRYDEVICE:
					if (player.getFirstEffect(GUARDOFDAWN) != null)
						htmltext = "32577-01.htm";
					else
						htmltext = "32577-03.htm";
					break;
				case SHKAF:
					if (st.getQuestItemsCount(EMPERORSHUNAIMANCONTRACT) == 0)
					{
						htmltext = "32580-01.htm";
						st.giveItems(EMPERORSHUNAIMANCONTRACT, 1);
						st.set(CONDITION, 4);
					}
					break;
				case BLACK:
					if (st.getQuestItemsCount(EMPERORSHUNAIMANCONTRACT) == 1)
						htmltext = "32579-01.htm";
					break;
				case IASONHEINE:
					if (st.getQuestItemsCount(EMPERORSHUNAIMANCONTRACT) == 1)
						if(player.getFirstEffect(GUARDOFDAWN) != null)
						{
							player.stopAllEffects();
						}
						htmltext = "30969-01.htm";
					break;
			}
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new SevenSignSecretRitualOfThePriests(195, QN, "Seven Sign Secret Ritual Of The Priests");
	}
}
