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
package quests._198_SevenSignEmbryo;

import quests._197_SevenSignTheSacredBookOfSeal.SevenSignTheSacredBookOfSeal;
import net.l2emuproject.gameserver.ai.CtrlIntention;
import net.l2emuproject.gameserver.model.actor.L2Attackable;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.model.entity.Instance;
import net.l2emuproject.gameserver.instancemanager.InstanceManager;
import net.l2emuproject.gameserver.instancemanager.InstanceManager.InstanceWorld;
import net.l2emuproject.gameserver.network.serverpackets.NpcSay;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;

/*
 * rework by lewzer
 */
public final class SevenSignEmbryo extends QuestJython
{
	private class EMBRYOWorld extends InstanceWorld
	{
		private L2Attackable	SHIL, SHIL_G1, SHIL_G2;

		public EMBRYOWorld()
		{
			InstanceManager.getInstance().super();
		}
	}

	private static final String	QN				= "_198_SevenSignEmbryo";

	// NPCs
	private static final int	WOOD			= 32593;
	private static final int	FRANZ			= 32597;
	private static final int	JAINA			= 32582;

	private static final int[]	NPCS			=
												{ WOOD, FRANZ, JAINA };

	// MOBs
	private static final int	SHILENSEVIL1	= 27346;
	private static final int	SHILENSEVIL2	= 27343;

	//INSTANCE	
	private static final int	INSTANCE_ID		= 113;
	private static final int[]	TELEPORT		=
												{ -23778, -8961, -5390 };
	// Quest Items
	private static final int	SCULPTURE		= 14360;
	private static final int	BRACELET		= 15312;
	private static final int	AA				= 5575;

	public SevenSignEmbryo(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(WOOD);

		for (int i : NPCS)
			addTalkId(i);

		addKillId(SHILENSEVIL1);
		addKillId(SHILENSEVIL2);

		questItemIds = new int[]
		{ SCULPTURE, BRACELET, AA };
	}

	private static final void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], true);
	}

	private final synchronized void enterInstance(L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (world.templateId != INSTANCE_ID)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if (inst != null)
				teleportPlayer(player, TELEPORT, world.instanceId);
			return;
		}
		else
		{
			final int instanceId = InstanceManager.getInstance().createDynamicInstance("SanctumSevenSignEmbryo.xml");

			world = new EMBRYOWorld();
			world.instanceId = instanceId;
			world.templateId = INSTANCE_ID;
			InstanceManager.getInstance().addWorld(world);
			world.allowed.add(player.getObjectId());
			teleportPlayer(player, TELEPORT, instanceId);

			_log.info("SevenSigns 7th epic quest " + instanceId + " created by player: " + player.getName());
		}
	}

	protected void exitInstance(L2PcInstance player)
	{
		player.setInstanceId(0);
		player.teleToLocation(146995, 23755, -1984);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("32593-02.htm"))
		{
			st.set(CONDITION, 1);
			st.setState(State.STARTED);
			st.sendPacket(SND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("HideoutoftheDawn"))
		{
			enterInstance(player);
		}
		else if (event.equalsIgnoreCase("32597-10.htm"))
		{
			st.set(CONDITION, 3);
			st.takeItems(SCULPTURE, 1);
			st.sendPacket(SND_MIDDLE);
			Instance instanceObj = InstanceManager.getInstance().getInstance(player.getInstanceId());
			instanceObj.setDuration(300000);
		}
		else if (event.equalsIgnoreCase("32597-05.htm"))
		{
			InstanceWorld tpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tpworld instanceof EMBRYOWorld)
			{
				EMBRYOWorld world = (EMBRYOWorld) tpworld;
				spawnState((EMBRYOWorld) world, player);
			}
		}
		else if (event.equalsIgnoreCase("LEAVE"))
		{
			exitInstance(player);
		}
		return htmltext;
	}

	protected void spawnState(EMBRYOWorld world, L2PcInstance player)
	{
		QuestState st = player.getQuestState(QN);
		world.SHIL = (L2Attackable) addSpawn(SHILENSEVIL1, -23801, -9004, -5385, 0, false, 0, false, world.instanceId);
		world.SHIL.broadcastPacket(new NpcSay(world.SHIL.getObjectId(), 0, world.SHIL.getNpcId(), "You are not the owner of that item!"));
		world.SHIL.setRunning();
		world.SHIL.addDamageHate(player, 0, 999);
		world.SHIL.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, st.getPlayer());
		world.SHIL_G1 = (L2Attackable) addSpawn(SHILENSEVIL2, -23801, -9004, -5385, 0, false, 0, false, world.instanceId);
		world.SHIL_G1.setRunning();
		world.SHIL_G1.addDamageHate(player, 0, 999);
		world.SHIL_G1.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, st.getPlayer());
		world.SHIL_G2 = (L2Attackable) addSpawn(SHILENSEVIL2, -23801, -9004, -5385, 0, false, 0, false, world.instanceId);
		world.SHIL_G2.setRunning();
		world.SHIL_G2.addDamageHate(player, 0, 999);
		world.SHIL_G2.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, st.getPlayer());
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		int cond = st.getInt(CONDITION);
		if (st.getState() == State.COMPLETED)
		{
			htmltext = QUEST_DONE;
		}
		else
		{
			switch (npc.getNpcId())
			{
				case WOOD:
					if (player.getLevel() < 79)
					{
						st.exitQuest(true);
						htmltext = "32593-00.htm";
					}
					QuestState qs = player.getQuestState(SevenSignTheSacredBookOfSeal.QN);
					if (qs.isCompleted() && st.getState() == State.CREATED)
						htmltext = "32593-01.htm";
					else
					{
						switch (cond)
						{
							case 0:
								st.exitQuest(true);
								htmltext = "32593-00.htm";
								break;
							case 1:
								htmltext = "32593-02.htm";
								break;
							case 3:
								st.addExpAndSp(315108096, 34906059);
								st.setState(State.COMPLETED);
								st.exitQuest(false);
								st.sendPacket(SND_FINISH);
								st.giveItems(BRACELET, 1);
								st.giveItems(AA, 1500000);
								st.takeItems(SCULPTURE, 1);
								htmltext = "32593-04.htm";
								break;
						}
					}
					break;
				case FRANZ:
					switch (cond)
					{
						case 1:
							htmltext = "32597-01.htm";
							break;
						case 2:
							htmltext = "32597-06.htm";
							break;
						case 3:
							htmltext = "32597-11.htm";
							break;
					}
					break;
				case JAINA:
					switch (cond)
					{
						case 3:
							htmltext = "32582-01.htm";
							break;
					}
					break;
			}
		}
		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return null;

		if (npc.getNpcId() == SHILENSEVIL1 && st.getInt(CONDITION) == 1)
		{
			InstanceWorld tpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tpworld instanceof EMBRYOWorld)
			{
				EMBRYOWorld world = (EMBRYOWorld) tpworld;
				world.SHIL.deleteMe();
				world.SHIL_G1.deleteMe();
				world.SHIL_G2.deleteMe();
			}
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), player.getName()
					+ "... You may have won this time... But next time, I will surely capture you!"));
			st.giveItems(SCULPTURE, 1);
			st.set(CONDITION, 2);
			player.showQuestMovie(14);
		}
		return null;
	}

	public static void main(String[] args)
	{
		new SevenSignEmbryo(198, QN, "Seven Signs Embryo");
	}
}
