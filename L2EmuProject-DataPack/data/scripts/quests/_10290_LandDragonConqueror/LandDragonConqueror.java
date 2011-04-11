package quests._10290_LandDragonConqueror;

import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * Land Dragon Conqueror (10290)
 * 
 * @author malyelfik
 * @since Converted by L0ngh0rn (2011-04-11)
 */
public final class LandDragonConqueror extends QuestJython
{
	public static final String	QN						= "_10290_LandDragonConqueror";

	// NPC
	private static final int	THEODORIC				= 30755;
	private static final int[]	ANTHARAS				=
														{ 29019, 29066, 29067, 29068 };

	// Item
	private static final int	PORTAL_STONE			= 3865;
	private static final int	SHABBY_NECKLACE			= 15522;
	private static final int	MIRACLE_NECKLACE		= 15523;
	private static final int	ANTHARAS_SLAYER_CIRCLET	= 8568;

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("30755-07.htm"))
		{
			st.setState(State.STARTED);
			st.set(CONDITION, 1);
			st.giveItems(SHABBY_NECKLACE, 1);
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
				if (player.getLevel() >= 83 && st.getQuestItemsCount(PORTAL_STONE) >= 1)
					htmltext = "30755-01.htm";
				else if (player.getLevel() < 83)
					htmltext = "30755-02.htm";
				else
					htmltext = "30755-04.htm";
				break;
			case State.STARTED:
				if (st.getInt(CONDITION) == 1 && st.getQuestItemsCount(SHABBY_NECKLACE) >= 1)
					htmltext = "30755-08.htm";
				else if (st.getInt(CONDITION) == 1 && st.getQuestItemsCount(SHABBY_NECKLACE) == 0)
				{
					st.giveItems(SHABBY_NECKLACE, 1);
					htmltext = "30755-09.htm";
				}
				else if (st.getInt(CONDITION) == 2)
				{
					st.takeItems(MIRACLE_NECKLACE, 1);
					st.giveAdena(131236);
					st.addExpAndSp(702557, 76334);
					st.giveItems(ANTHARAS_SLAYER_CIRCLET, 1);
					st.unset(CONDITION);
					st.exitQuest(false);
					st.sendPacket(SND_FINISH);
					htmltext = "30755-10.htm";
				}
				break;
			case State.COMPLETED:
				htmltext = "30755-03.htm";
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
			st.takeItems(SHABBY_NECKLACE, 1);
			st.giveItems(MIRACLE_NECKLACE, 1);
			st.sendPacket(SND_MIDDLE);
			st.set(CONDITION, 2);
		}
	}

	public LandDragonConqueror(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);
		addStartNpc(THEODORIC);
		addTalkId(THEODORIC);
		for (int i : ANTHARAS)
			addKillId(i);

		questItemIds = new int[]
		{ MIRACLE_NECKLACE, SHABBY_NECKLACE };
	}

	public static void main(String[] args)
	{
		new LandDragonConqueror(10290, QN, "Land Dragon Conqueror", "quests");
	}
}