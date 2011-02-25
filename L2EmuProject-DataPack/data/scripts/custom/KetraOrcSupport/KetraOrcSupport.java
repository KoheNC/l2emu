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
package custom.KetraOrcSupport;

import javolution.util.FastMap;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.WareHouseWithdrawalList;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author L0ngh0rn
 */
public final class KetraOrcSupport extends QuestJython
{
	private static final String					QN		= "KetraOrcSupport";

	// NPCs
	private static final int					KADUN	= 31370;
	private static final int					WAHKAN	= 31371;
	private static final int					ASEFA	= 31372;
	private static final int					ATAN	= 31373;
	private static final int					JAFF	= 31374;
	private static final int					JUMARA	= 31375;
	private static final int					KURFA	= 31376;
	private static final int[]					NPCS	=
														{ KADUN, WAHKAN, ASEFA, ATAN, JAFF, JUMARA, KURFA };

	// Quest Item
	private static final int					HORN	= 7186;

	// Events
	private static final FastMap<String, int[]>	BUFF	= new FastMap<String, int[]>();

	static
	{
		BUFF.put("1", new int[]
		{ 4359, 1, 2 });
		BUFF.put("2", new int[]
		{ 4360, 1, 2 });
		BUFF.put("3", new int[]
		{ 4345, 1, 3 });
		BUFF.put("4", new int[]
		{ 4355, 1, 3 });
		BUFF.put("5", new int[]
		{ 4352, 1, 3 });
		BUFF.put("6", new int[]
		{ 4354, 1, 3 });
		BUFF.put("7", new int[]
		{ 4356, 1, 6 });
		BUFF.put("8", new int[]
		{ 4357, 1, 6 });
	}

	public KetraOrcSupport(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		for (int i : NPCS)
			addFirstTalkId(i);
		addTalkId(ASEFA);
		addTalkId(KURFA);
		addTalkId(JAFF);
		addStartNpc(KURFA);
		addStartNpc(JAFF);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		int aLevel = player.getAllianceWithVarkaKetra();
		if (BUFF.containsKey(event))
		{
			int skillId = BUFF.get(event)[0];
			int level = BUFF.get(event)[1];
			int horn = BUFF.get(event)[2];

			if (st.getQuestItemsCount(HORN) >= horn)
			{
				st.takeItems(HORN, horn);
				npc.setTarget(player);
				npc.doCast(SkillTable.getInstance().getInfo(skillId, level));
				npc.getStatus().setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
				htmltext = "31372-4.htm";
			}
		}
		else if (event.equalsIgnoreCase("Withdraw"))
		{
			if (player.getWarehouse().getSize() == 0)
				htmltext = "31374-0.htm";
			else
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				player.setActiveWarehouse(player.getWarehouse());
				player.sendPacket(new WareHouseWithdrawalList(player, 1));
			}
		}
		else if (event.equalsIgnoreCase("Teleport"))
		{
			switch (aLevel)
			{
				case 4:
					htmltext = "31376-4.htm";
					break;
				case 5:
					htmltext = "31376-5.htm";
					break;
			}
		}
		return htmltext;
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2Player player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			st = newQuestState(player);

		int aLevel = player.getAllianceWithVarkaKetra();
		long horns = st.getQuestItemsCount(HORN);

		switch (npc.getNpcId())
		{
			case KADUN:
				if (aLevel > 0)
					htmltext = "31370-friend.htm";
				else
					htmltext = "31370-no.htm";
				break;
			case WAHKAN:
				if (aLevel > 0)
					htmltext = "31371-friend.htm";
				else
					htmltext = "31371-no.htm";
				break;
			case ASEFA:
				st.setState(State.STARTED);
				if (aLevel < 1)
					htmltext = "31372-3.htm";
				else if (aLevel < 3 && aLevel > 0)
					htmltext = "31372-1.htm";
				else if (aLevel > 2)
				{
					if (horns > 0)
						htmltext = "31372-4.htm";
					else
						htmltext = "31372-2.htm";
				}
				break;
			case ATAN:
				if (player.getKarma() >= 1)
					htmltext = "31373-pk.htm";
				else if (aLevel <= 0)
					htmltext = "31373-no.htm";
				else if (aLevel == 1 || aLevel == 2)
					htmltext = "31373-1.htm";
				else
					htmltext = "31373-2.htm";
				break;
			case JAFF:
				if (aLevel <= 0)
					htmltext = "31374-no.htm";
				else if (aLevel == 1)
					htmltext = "31374-1.htm";
				else if (player.getWarehouse().getSize() == 0)
					htmltext = "31374-3.htm";
				else if (aLevel == 2 || aLevel == 3)
					htmltext = "31374-2.htm";
				else
					htmltext = "31374-4.htm";
				break;
			case JUMARA:
				if (aLevel == 2)
					htmltext = "31375-1.htm";
				else if (aLevel == 3 || aLevel == 4)
					htmltext = "31375-2.htm";
				else if (aLevel == 5)
					htmltext = "31375-3.htm";
				else
					htmltext = "31375-no.htm";
				break;
			case KURFA:
				if (aLevel <= 0)
					htmltext = "31376-no.htm";
				else if (aLevel > 0 && aLevel < 4)
					htmltext = "31376-1.htm";
				else if (aLevel == 4)
					htmltext = "31376-2.htm";
				else
					htmltext = "31376-3.htm";
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new KetraOrcSupport(6050, QN, "Ketra Orc Support", "custom");
	}
}