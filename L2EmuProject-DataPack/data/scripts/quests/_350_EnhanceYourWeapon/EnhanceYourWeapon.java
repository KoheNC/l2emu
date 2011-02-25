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

/*
* made by lewzer
*/

package quests._350_EnhanceYourWeapon;

import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.Quest;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.world.object.L2Npc;

public class EnhanceYourWeapon extends Quest
{
	private static final String	QN						= "_350_EnhanceYourWeapon";
	private static final int	RED_SOUL_CRYSTAL0_ID	= 4629;
	private static final int	GREEN_SOUL_CRYSTAL0_ID	= 4640;
	private static final int	BLUE_SOUL_CRYSTAL0_ID	= 4651;

	private static final int	JUREK					= 30115;
	private static final int	GIDEON					= 30194;
	private static final int	WINONIN					= 30856;

	public EnhanceYourWeapon(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(JUREK);
		addStartNpc(GIDEON);
		addStartNpc(WINONIN);
		addTalkId(JUREK);
		addTalkId(GIDEON);
		addTalkId(WINONIN);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("30115-04.htm") || event.equalsIgnoreCase("30194-04.htm") || event.equalsIgnoreCase("30856-04.htm"))
		{
			st.set(CONDITION, 1);
			st.setState(State.STARTED);
			st.sendPacket(SND_ACCEPT);
		}
		if (event.equalsIgnoreCase("30115-09.htm") || event.equalsIgnoreCase("30194-09.htm") || event.equalsIgnoreCase("30856-09.htm"))
			st.giveItems(RED_SOUL_CRYSTAL0_ID, 1);
		if (event.equalsIgnoreCase("30115-10.htm") || event.equalsIgnoreCase("30194-10.htm") || event.equalsIgnoreCase("30856-10.htm"))
			st.giveItems(GREEN_SOUL_CRYSTAL0_ID, 1);
		if (event.equalsIgnoreCase("30115-11.htm") || event.equalsIgnoreCase("30194-11.htm") || event.equalsIgnoreCase("30856-11.htm"))
			st.giveItems(BLUE_SOUL_CRYSTAL0_ID, 1);
		if (event.equalsIgnoreCase("exit.htm"))
			st.exitQuest(true);
		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);
		int id = st.getState();
		int npcId = npc.getNpcId();
		if (st.getQuestItemsCount(RED_SOUL_CRYSTAL0_ID) == 0 && st.getQuestItemsCount(GREEN_SOUL_CRYSTAL0_ID) == 0
				&& st.getQuestItemsCount(BLUE_SOUL_CRYSTAL0_ID) == 0 && st.getPlayer().getLevel() >= 40)
		{
			switch (npcId)
			{
				case JUREK:
					htmltext = "30115-01.htm";
					break;
				case GIDEON:
					htmltext = "30194-01.htm";
					break;
				case WINONIN:
					htmltext = "30856-01.htm";
					break;
			}
		}
		else
		{
			htmltext = "nocrystal.htm";
		}
		if (id == State.STARTED && st.getQuestItemsCount(RED_SOUL_CRYSTAL0_ID) == 0 && st.getQuestItemsCount(GREEN_SOUL_CRYSTAL0_ID) == 0
				&& st.getQuestItemsCount(BLUE_SOUL_CRYSTAL0_ID) == 0)
		{
			switch (npcId)
			{
				case JUREK:
					htmltext = "30115-21.htm";
					break;
				case GIDEON:
					htmltext = "30194-21.htm";
					break;
				case WINONIN:
					htmltext = "30856-21.htm";
					break;
			}
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new EnhanceYourWeapon(350, QN, "Enhance Your Weapon");
	}
}