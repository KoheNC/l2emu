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
package custom.VarkaSilenosSupport;

import javolution.util.FastMap;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.WareHouseWithdrawalList;
import net.l2emuproject.gameserver.world.object.L2Npc;

/**
 * @author L0ngh0rn
 */
public final class VarkaSilenosSupport extends QuestJython
{
	private static final String					QN		= "VarkaSilenosSupport";

	// NPCs
	private static final int					ASHAS	= 31377;
	private static final int					NARAN	= 31378;
	private static final int					UDAN	= 31379;
	private static final int					DIYABU	= 31380;
	private static final int					HAGOS	= 31381;
	private static final int					SHIKON	= 31382;
	private static final int					TERANU	= 31383;
	private static final int[]					NPCS	=
														{ ASHAS, NARAN, UDAN, DIYABU, HAGOS, SHIKON, TERANU };

	// Quest Item
	private static final int					SEED	= 7187;

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

	public VarkaSilenosSupport(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		for (int i : NPCS)
			addFirstTalkId(i);
		addTalkId(UDAN);
		addTalkId(HAGOS);
		addTalkId(TERANU);
		addStartNpc(HAGOS);
		addStartNpc(TERANU);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (BUFF.containsKey(event))
		{
			int skillId = BUFF.get(event)[0];
			int level = BUFF.get(event)[1];
			int seeds = BUFF.get(event)[2];

			if (st.getQuestItemsCount(SEED) >= seeds)
			{
				st.takeItems(SEED, seeds);
				npc.setTarget(player);
				npc.doCast(SkillTable.getInstance().getInfo(skillId, level));
				npc.getStatus().setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
				htmltext = "31379-4.htm";
			}
		}
		else if (event.equalsIgnoreCase("Withdraw"))
		{
			if (player.getWarehouse().getSize() == 0)
				htmltext = "31381-0.htm";
			else
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				player.setActiveWarehouse(player.getWarehouse());
				player.sendPacket(new WareHouseWithdrawalList(player, 1));
			}
		}
		else if (event.equalsIgnoreCase("Teleport"))
		{
			switch (player.getAllianceWithVarkaKetra())
			{
				case -4:
					htmltext = "31383-4.htm";
					break;
				case -5:
					htmltext = "31383-5.htm";
					break;
			}
		}
		return htmltext;
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			st = newQuestState(player);
		int aLevel = player.getAllianceWithVarkaKetra();
		switch (npc.getNpcId())
		{
			case ASHAS:
				if (aLevel < 0)
					htmltext = "31377-friend.htm";
				else
					htmltext = "31377-no.htm";
				break;
			case NARAN:
				if (aLevel < 0)
					htmltext = "31378-friend.htm";
				else
					htmltext = "31378-no.htm";
				break;
			case UDAN:
				st.setState(State.STARTED);
				if (aLevel > -1)
					htmltext = "31379-3.htm";
				else if (aLevel > -3 && aLevel < 0)
					htmltext = "31379-1.htm";
				else if (aLevel < -2)
				{
					if (st.getQuestItemsCount(SEED) > 0)
						htmltext = "31379-4.htm";
					else
						htmltext = "31379-2.htm";
				}
				break;
			case DIYABU:
				if (player.getKarma() >= 1)
					htmltext = "31380-pk.htm";
				else if (aLevel >= 0)
					htmltext = "31380-no.htm";
				else if (aLevel == -1 || aLevel == -2)
					htmltext = "31380-1.htm";
				else
					htmltext = "31380-2.htm";
				break;
			case HAGOS:
				if (aLevel >= 0)
					htmltext = "31381-no.htm";
				else if (aLevel == -1)
					htmltext = "31381-1.htm";
				else if (player.getWarehouse().getSize() == 0)
					htmltext = "31381-3.htm";
				else if (aLevel == -2 || aLevel == -3)
					htmltext = "31381-2.htm";
				else
					htmltext = "31381-4.htm";
				break;
			case SHIKON:
				if (aLevel == -2)
					htmltext = "31382-1.htm";
				else if (aLevel == -3 || aLevel == -4)
					htmltext = "31382-2.htm";
				else if (aLevel == -5)
					htmltext = "31382-3.htm";
				else
					htmltext = "31382-no.htm";
				break;
			case TERANU:
				if (aLevel >= 0)
					htmltext = "31383-no.htm";
				else if (aLevel < 0 && aLevel > -4)
					htmltext = "31383-1.htm";
				else if (aLevel == -4)
					htmltext = "31383-2.htm";
				else
					htmltext = "31383-3.htm";
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new VarkaSilenosSupport(6051, QN, "Varka Silenos Support", "custom");
	}
}