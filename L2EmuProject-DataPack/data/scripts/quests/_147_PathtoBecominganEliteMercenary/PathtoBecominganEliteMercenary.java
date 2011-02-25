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
package quests._147_PathtoBecominganEliteMercenary;

import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

import org.apache.commons.lang.ArrayUtils;

/**
 ** @author Gnacik
 **
 ** 2010-09-30 Based on official server Franz
 */
public final class PathtoBecominganEliteMercenary extends QuestJython
{
	private static final String	QN				= "_147_PathtoBecominganEliteMercenary";
	// NPCs
	private static final int[]	MERC_NPCS		=
												{ 36481, 36482, 36483, 36484, 36485, 36486, 36487, 36488, 36489 };
	// Items
	private static final int	CERT_ORDINARY	= 13766;
	private static final int	CERT_ELITE		= 13767;

	public PathtoBecominganEliteMercenary(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int npc : MERC_NPCS)
		{
			addStartNpc(npc);
			addTalkId(npc);
		}
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (ArrayUtils.contains(MERC_NPCS, npc.getNpcId()))
		{
			if (event.equalsIgnoreCase("elite-02.htm"))
			{
				if (st.hasQuestItems(CERT_ORDINARY))
					return "elite-02a.htm";
				st.giveItems(CERT_ORDINARY, 1);
			}
			else if (event.equalsIgnoreCase("elite-04.htm"))
			{
				st.setState(State.STARTED);
				st.set(CONDITION, 1);
				st.sendPacket(SND_ACCEPT);
			}
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

		if (ArrayUtils.contains(MERC_NPCS, npc.getNpcId()))
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (player.getClan() != null && player.getClan().getHasCastle() > 0)
						htmltext = "castle.htm";
					else
						htmltext = "elite-01.htm";
					break;
				case State.STARTED:
					if (st.getInt(CONDITION) < 4)
					{
						htmltext = "elite-05.htm";
					}
					else if (st.getInt(CONDITION) == 4)
					{
						st.unset(CONDITION);
						st.unset("kills");
						st.takeItems(CERT_ORDINARY, -1);
						st.giveItems(CERT_ELITE, 1);
						st.exitQuest(false);
						htmltext = "elite-06.htm";
					}
					break;
				case State.COMPLETED:
					htmltext = QUEST_DONE;
					break;
			}
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new PathtoBecominganEliteMercenary(147, QN, "Path to Becoming an Elite Mercenary");
	}

}
