package quests._10291_FireDragonDestroyer;

import net.l2emuproject.gameserver.services.quest.Quest;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * Fire Dragon Destroyer (10291)
 * 
 * @author malyelfik
 * @since Converted by L0ngh0rn (2011-04-11)
 */
public class FireDragonDestroyer extends Quest
{
	private static final String	QN						= "_10291_FireDragonDestroyer";

	// NPC
	private static final int	KHLEIN					= 31540;
	private static final int	VALAKAS					= 29028;

	// Item
	private static final int	FLOATING_STONE			= 7267;
	private static final int	POOR_NECKLACE			= 15524;
	private static final int	VALOR_NECKLACE			= 15525;
	private static final int	VALAKA_SLAYER_CIRCLET	= 8567;

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("31540-07.htm"))
		{
			st.setState(State.STARTED);
			st.set(CONDITION, 1);
			st.giveItems(POOR_NECKLACE, 1);
			st.sendPacket(SND_ACCEPT);
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

		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 83 && st.getQuestItemsCount(FLOATING_STONE) >= 1)
					htmltext = "31540-01.htm";
				else if (player.getLevel() < 83)
					htmltext = "31540-02.htm";
				else
					htmltext = "31540-04.htm";
				break;
			case State.STARTED:
				if (st.getInt(CONDITION) == 1 && st.getQuestItemsCount(POOR_NECKLACE) >= 1)
					htmltext = "31540-08.htm";
				else if (st.getInt(CONDITION) == 1 && st.getQuestItemsCount(POOR_NECKLACE) == 0)
				{
					st.giveItems(POOR_NECKLACE, 1);
					htmltext = "31540-09.htm";
				}
				else if (st.getInt(CONDITION) == 2)
				{
					st.takeItems(VALOR_NECKLACE, 1);
					st.giveAdena(126549);
					st.addExpAndSp(717291, 77397);
					st.giveItems(VALAKA_SLAYER_CIRCLET, 1);
					st.unset(CONDITION);
					st.exitQuest(false);
					st.sendPacket(SND_FINISH);
					htmltext = "31540-10.htm";
				}
				break;
			case State.COMPLETED:
				htmltext = "31540-03.htm";
				break;
		}

		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2Player player, boolean isPet)
	{
		if (player.getParty() != null)
			for (L2Player partyMember : player.getParty().getPartyMembers())
				rewardPlayer(partyMember);
		else
			rewardPlayer(player);
		return null;
	}

	private final void rewardPlayer(L2Player player)
	{
		QuestState st = player.getQuestState(QN);

		if (st.getInt(CONDITION) == 1)
		{
			st.takeItems(POOR_NECKLACE, 1);
			st.giveItems(VALOR_NECKLACE, 1);
			st.sendPacket(SND_MIDDLE);
			st.set(CONDITION, 2);
		}
	}

	public FireDragonDestroyer(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);
		addStartNpc(KHLEIN);
		addTalkId(KHLEIN);
		addKillId(VALAKAS);

		questItemIds = new int[]
		{ POOR_NECKLACE, VALOR_NECKLACE };
	}

	public static void main(String[] args)
	{
		new FireDragonDestroyer(10291, QN, "Fire Dragon Destroyer", "quests");
	}
}