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
package quests._636_TruthBeyond;

import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.zone.L2Zone;

/**
 * @author moved to java by DS, jython script by Polo, BiTi and DrLecter
 * @since Converted by L0ngh0rn (2011-04-08)
 */
public final class TruthBeyond extends QuestJython
{
	public static final String	QN				= "_636_TruthBeyond";

	private static final int	ELIAH			= 31329;
	private static final int	FLAURON			= 32010;
	private static final int	ZONE			= 30100;
	private static final int	VISITOR_MARK	= 8064;
	private static final int	FADED_MARK		= 8065;
	private static final int	MARK			= 8067;

	public TruthBeyond(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		addStartNpc(ELIAH);
		addTalkId(ELIAH);
		addTalkId(FLAURON);
		addEnterZoneId(ZONE);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		final QuestState st = player.getQuestState(QN);

		if (st == null)
			return null;

		if ("31329-04.htm".equalsIgnoreCase(event))
		{
			st.set(CONDITION, 1);
			st.setState(State.STARTED);
			st.sendPacket(SND_ACCEPT);
		}
		else if ("32010-02.htm".equalsIgnoreCase(event))
		{
			st.giveItems(VISITOR_MARK, 1);
			st.sendPacket(SND_FINISH);
			st.exitQuest(true);
		}

		return event;
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		final QuestState st = player.getQuestState(QN);

		if (st == null)
			return NO_QUEST;

		if (npc.getNpcId() == ELIAH)
		{
			if (st.getQuestItemsCount(VISITOR_MARK) > 0 || st.getQuestItemsCount(FADED_MARK) > 0 || st.getQuestItemsCount(MARK) > 0)
			{
				st.exitQuest(true);
				return "31329-mark.htm";
			}
			if (st.getState() == State.CREATED)
			{
				if (player.getLevel() > 72)
					return "31329-02.htm";

				st.exitQuest(true);
				return "31329-01.htm";
			}
			else if (st.getState() == State.STARTED)
				return "31329-05.htm";
		}
		else if (st.getState() == State.STARTED)
		{
			if (st.getInt(CONDITION) == 1)
				return "32010-01.htm";
			else
			{
				st.exitQuest(true);
				return "32010-03.htm";
			}
		}

		return NO_QUEST;
	}

	@Override
	public String onEnterZone(L2Character character, L2Zone zone)
	{
		if (character instanceof L2Player)
			if (((L2Player) character).destroyItemByItemId("Mark", VISITOR_MARK, 1, character, false))
				((L2Player) character).addItem("Mark", FADED_MARK, 1, character, true);

		return null;
	}

	public static void main(String[] args)
	{
		new TruthBeyond(636, QN, "The Truth Beyond the Gate", "quest");
	}
}
