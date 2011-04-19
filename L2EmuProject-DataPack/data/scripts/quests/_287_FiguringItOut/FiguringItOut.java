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
package quests._287_FiguringItOut;

import net.l2emuproject.gameserver.services.quest.Quest;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.tools.random.Rnd;

/**
 * Figuring It Out! (287)
 * 
 * @author malyelfik
 */
public final class FiguringItOut extends Quest
{
	private static final String	QN					= "_287_FiguringItOut";

	// NPC
	private static final int	LAKI				= 32742;
	private static final int[]	MONSTERS			=
													{ 22771, 22770, 22774, 22769, 22772, 22768, 22773 };
	// Items
	private static final int	VIAL_OF_TANTA_BLOOD	= 15499;

	public FiguringItOut(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);

		addStartNpc(LAKI);
		addTalkId(LAKI);
		for (int i : MONSTERS)
			addKillId(i);

		questItemIds = new int[]
		{ VIAL_OF_TANTA_BLOOD };
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("32742-03.htm"))
		{
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
			st.setState(State.STARTED);
		}
		else if (event.equalsIgnoreCase("Icarus"))
		{
			if (st.getQuestItemsCount(VIAL_OF_TANTA_BLOOD) >= 500)
			{
				st.takeItems(VIAL_OF_TANTA_BLOOD, 500);
				final int i0 = Rnd.get(5);
				switch (i0)
				{
					case 0:
						st.giveItems(10381, 1);
						break;
					case 1:
						st.giveItems(10405, 1);
						break;
					case 2:
					case 3:
						st.giveItems(10405, 4);
						break;
					default:
						st.giveItems(10405, 6);
						break;
				}

				st.sendPacket(SND_FINISH);
				htmltext = "32742-06.html";
			}
			else
				htmltext = "32742-07.html";
		}
		else if (event.equalsIgnoreCase("Moirai"))
		{
			if (st.getQuestItemsCount(VIAL_OF_TANTA_BLOOD) >= 100)
			{
				st.takeItems(VIAL_OF_TANTA_BLOOD, 100);
				final int i0 = Rnd.get(10);

				switch (i0)
				{
					case 0:
						st.giveItems(15776, 1);
						break;
					case 1:
						st.giveItems(15779, 1);
						break;
					case 2:
						st.giveItems(15782, 1);
						break;
					case 3:
						final boolean i2 = Rnd.nextBoolean();
						if (!i2)
							st.giveItems(15785, 1);
						else
							st.giveItems(15788, 1);
						break;
					case 4:
						final int i3 = Rnd.get(10);
						if (i3 < 4)
							st.giveItems(15812, 1);
						else if (i3 < 8)
							st.giveItems(15813, 1);
						else
							st.giveItems(15814, 1);
						break;
					case 5:
						st.giveItems(15646, 5);
						break;
					case 6:
						st.giveItems(15649, 5);
						break;
					case 7:
						st.giveItems(15652, 5);
						break;
					case 8:
						final boolean i1 = Rnd.nextBoolean();
						if (!i1)
							st.giveItems(15655, 5);
						else
							st.giveItems(15658, 5);
						break;
					default:
						final int i = Rnd.get(10);
						if (i < 4)
							st.giveItems(15772, 1);
						else if (i < 7)
							st.giveItems(15773, 1);
						else
							st.giveItems(15774, 1);
						break;
				}

				st.sendPacket(SND_FINISH);
				htmltext = "32742-08.html";
			}
			else
				htmltext = "32742-09.html";
		}
		else if (event.equalsIgnoreCase("32742-11.html"))
		{
			if (st.getQuestItemsCount(VIAL_OF_TANTA_BLOOD) >= 1)
				htmltext = "32742-11.html";
			else
			{
				st.sendPacket(SND_FINISH);
				st.exitQuest(true);
				htmltext = "32742-12.html";
			}
		}
		else if (event.equalsIgnoreCase("32742-13.html"))
		{
			st.takeItems(VIAL_OF_TANTA_BLOOD, -1);
			st.sendPacket(SND_FINISH);
			st.exitQuest(true);
			htmltext = "32742-12.html";
		}
		return htmltext;
	}

	@Override
	public String onTalk(final L2Npc npc, final L2Player player)
	{
		String htmltext = NO_QUEST;
		final QuestState st = player.getQuestState(QN);
		final QuestState prev = player.getQuestState("250_WatchWhatYouEat");

		if (st == null)
			return htmltext;

		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 82 && prev != null && prev.getState() == State.COMPLETED)
					htmltext = "32742-01.htm";
				else
					htmltext = "32742-14.htm";
				break;
			case State.STARTED:
				if (st.getQuestItemsCount(VIAL_OF_TANTA_BLOOD) < 100)
					htmltext = "32742-04.html";
				else
					htmltext = "32742-05.html";
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(final L2Npc npc, final L2Player player, final boolean isPet)
	{
		final L2Player partyMember = getRandomPartyMember(player, "1");
		if (partyMember == null)
			return null;
		final QuestState st = partyMember.getQuestState(QN);
		final int chance = Rnd.get(1000);
		boolean giveItem = false;

		switch (npc.getNpcId())
		{
			case 22771: // Tanta Lizardman Berserker
				if (chance < 159)
					giveItem = true;
				break;
			case 22770: // Tanta Lizardman Soldier
				if (chance < 123)
					giveItem = true;
				break;
			case 22774: // Tanta Lizardman Summoner
				if (chance < 261)
					giveItem = true;
				break;
			case 22769: // Tanta Lizardman Warrior
				if (chance < 689)
					giveItem = true;
				break;
			case 22772: // Tanta Lizardman Archer
				if (chance < 739)
					giveItem = true;
				break;
			case 22768: // Tanta Lizardman Scout
				if (chance < 509)
					giveItem = true;
				break;
			case 22773: // Tanta Lizardman Magician
				if (chance < 737)
					giveItem = true;
				break;
		}

		if (giveItem)
		{
			st.giveItems(VIAL_OF_TANTA_BLOOD, 1);
			st.sendPacket(SND_ITEM_GET);
		}
		return null;
	}

	public static void main(String[] args)
	{
		new FiguringItOut(-1, QN, "Figuring It Out");
	}
}
