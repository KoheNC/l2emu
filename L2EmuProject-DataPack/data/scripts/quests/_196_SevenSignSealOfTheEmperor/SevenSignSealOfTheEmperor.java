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
package quests._196_SevenSignSealOfTheEmperor;

import net.l2emuproject.gameserver.ai.CtrlIntention;
import net.l2emuproject.gameserver.instancemanager.InstanceManager;
import net.l2emuproject.gameserver.instancemanager.InstanceManager.InstanceWorld;
import net.l2emuproject.gameserver.model.actor.instance.L2DoorInstance;
import net.l2emuproject.gameserver.model.entity.Instance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.NpcSay;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.world.object.L2Attackable;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import quests._195_SevenSignSecretRitualOfThePriests.SevenSignSecretRitualOfThePriests;

/**
 * rework by lewzer
 * work in progress -> needs core support to make npc's attack each-other
 */
public final class SevenSignSealOfTheEmperor extends QuestJython
{
	private class SIGNSWorld extends InstanceWorld
	{
		public long[]	storeTime	=
									{ 0, 0 };
		private L2Npc	LIL, LIL_G0, LIL_G1, ANAK, ANAK_G0, ANAK_G1, ANAK_G2;

		public SIGNSWorld()
		{
			InstanceManager.getInstance().super();
		}
	}

	public static final String	QN					= "_196_SevenSignSealOfTheEmperor";

	// NPCs
	private static final int	HEINE				= 30969;
	private static final int	MAMMON				= 32584;
	private static final int	SHUNAIMAN			= 32586;
	private static final int	MAGICAN				= 32598;
	private static final int	WOOD				= 32593;
	private static final int	INSTANCE_ID			= 514;
	private static final int	LEON				= 32587;
	private static final int	PROMICE_OF_MAMMON	= 32585;
	private static final int	DISCIPLES_GK		= 32657;

	//FIGHTING NPCS
	private static final int	LILITH				= 32715;
	private static final int	LILITH_GUARD0		= 32716;
	private static final int	LILITH_GUARD1		= 32717;
	private static final int	ANAKIM				= 32718;
	private static final int	ANAKIM_GUARD0		= 32719;
	private static final int	ANAKIM_GUARD1		= 32720;
	private static final int	ANAKIM_GUARD2		= 32721;

	//DOOR
	private static final int	DOOR				= 17240111;

	// INSTANCE TP
	private static final int[]	TELEPORT			=
													{ -89528, 216056, -7516 };

	private static final int[]	NPCS				=
													{ HEINE, WOOD, MAMMON, MAGICAN, SHUNAIMAN, LEON, PROMICE_OF_MAMMON, DISCIPLES_GK };

	// MOBs
	private static final int	SEALDEVICE			= 27384;

	// QUEST ITEMS
	private static final int	STONE				= 13824;
	private static final int	WATER				= 13808;
	private static final int	SWORD				= 15310;
	private static final int	SEAL				= 13846;
	private static final int	STAFF				= 13809;

	private int					mammonst			= 0;

	public SevenSignSealOfTheEmperor(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(HEINE);

		for (int i : NPCS)
			addTalkId(i);

		addKillId(SEALDEVICE);

		questItemIds = new int[]
		{ STONE, SWORD, WATER, SEAL, STAFF };
	}

	private static final void teleportPlayer(L2Player player, int[] coords, int instanceId)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], true);
	}

	private static final void openDoor(int doorId, int instanceId)
	{
		for (L2DoorInstance door : InstanceManager.getInstance().getInstance(instanceId).getDoors())
			if (door.getDoorId() == doorId)
				door.openMe();
	}

	private final synchronized void enterInstance(L2Player player)
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
			final int instanceId = InstanceManager.getInstance().createDynamicInstance("SanctumSealOfTheEmperor.xml");

			world = new SIGNSWorld();
			world.instanceId = instanceId;
			world.templateId = INSTANCE_ID;
			InstanceManager.getInstance().addWorld(world);
			((SIGNSWorld) world).storeTime[0] = System.currentTimeMillis();
			spawnState((SIGNSWorld) world);
			world.allowed.add(player.getObjectId());
			teleportPlayer(player, TELEPORT, instanceId);

			_log.info("SevenSigns 5th epic quest " + instanceId + " created by player: " + player.getName());
		}
	}

	protected void exitInstance(L2Player player)
	{
		player.setInstanceId(0);
		player.teleToLocation(172008, -17448, -4896);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("30969-05.htm"))
		{
			st.set(CONDITION, 1);
			st.setState(State.STARTED);
			st.sendPacket(SND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32598-02.htm"))
		{
			st.giveItems(STAFF, 1);
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("30969-11.htm"))
		{
			st.set(CONDITION, 6);
			st.sendPacket(SND_MIDDLE);

		}
		else if (event.equalsIgnoreCase("32584-05.htm"))
		{
			st.set(CONDITION, 2);
			st.sendPacket(SND_MIDDLE);
			st.giveItems(STONE, 1);
		}
		else if (event.equalsIgnoreCase("32586-06.htm"))
		{
			st.sendPacket(SND_MIDDLE);
			st.set(CONDITION, 4);
			st.takeItems(STONE, 1);
			st.giveItems(SWORD, 1);
			st.giveItems(WATER, 1);
		}
		else if (event.equalsIgnoreCase("32586-12.htm"))
		{
			st.sendPacket(SND_MIDDLE);
			st.set(CONDITION, 5);
			st.takeItems(SEAL, 4);
			st.takeItems(SWORD, 1);
			st.takeItems(WATER, 1);
			st.takeItems(STAFF, 1);
		}
		else if (event.equalsIgnoreCase("32593-02.htm"))
		{
			st.addExpAndSp(52518015, 5817676);
			st.setState(State.COMPLETED);
			st.exitQuest(false);
			st.sendPacket(SND_FINISH);
		}
		else if (event.equalsIgnoreCase("30969-06.htm"))
		{
			if (mammonst == 0)
			{
				st.addSpawn(MAMMON, player.getX() + 70, player.getY() + 70, player.getZ(), 60000);
				mammonst = 1;
				st.startQuestTimer("despawn", 60000);
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "Who dared to call the Merchant of Mammon?"));
			}
			else
				return "Mammon is already spawned";
		}
		else if (event.equalsIgnoreCase("despawn"))
		{
			mammonst = 0;
			return null;
		}
		else if (event.equalsIgnoreCase("DOORS"))
		{
			InstanceWorld tempworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tempworld instanceof SIGNSWorld)
			{
				SIGNSWorld world = (SIGNSWorld) tempworld;
				attackEachOther(world.LIL, world.ANAK);
				openDoor(DOOR, world.instanceId);
				player.showQuestMovie(12);
			}
		}
		else if (event.equalsIgnoreCase("32587-02.htm"))
		{
			htmltext = "32587-02.htm";
			exitInstance(player);
		}
		return htmltext;
	}

	protected void spawnState(SIGNSWorld world)
	{
		world.LIL = (L2Attackable) addSpawn(LILITH, -83175, 217021, -7504, 0, false, 0, false, world.instanceId);
		world.LIL.setIsNoRndWalk(true);
		world.LIL_G0 = (L2Attackable) addSpawn(LILITH_GUARD0, -83127, 217056, -7504, 0, false, 0, false, world.instanceId);
		world.LIL_G0.setIsNoRndWalk(true);
		world.LIL_G1 = (L2Attackable) addSpawn(LILITH_GUARD1, -83222, 217055, -7504, 0, false, 0, false, world.instanceId);
		world.LIL_G1.setIsNoRndWalk(true);
		world.ANAK = (L2Attackable) addSpawn(ANAKIM, -83179, 216479, -7504, 0, false, 0, false, world.instanceId);
		world.ANAK.setIsNoRndWalk(true);
		world.ANAK_G0 = (L2Attackable) addSpawn(ANAKIM_GUARD0, -83227, 216443, -7504, 0, false, 0, false, world.instanceId);
		world.ANAK_G0.setIsNoRndWalk(true);
		world.ANAK_G1 = (L2Attackable) addSpawn(ANAKIM_GUARD1, -83134, 216443, -7504, 0, false, 0, false, world.instanceId);
		world.ANAK_G1.setIsNoRndWalk(true);
		world.ANAK_G2 = (L2Attackable) addSpawn(ANAKIM_GUARD2, -83179, 216432, -7504, 0, false, 0, false, world.instanceId);
		world.ANAK_G2.setIsNoRndWalk(true);
	}

	private final void attackEachOther(L2Npc attacker, L2Npc target)
	{
		// TODO: THIS NEEDS CORE SUPPORT!
		attacker.setTarget(target);
		attacker.setRunning();
		((L2Attackable) attacker).addDamageHate(target, 0, 9999);
		attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
		target.setTarget(attacker);
		target.setRunning();
		((L2Attackable) target).addDamageHate(attacker, 0, 999);
		target.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker, null);
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;
		int cond = st.getInt(CONDITION);

		switch (npc.getNpcId())
		{
			case HEINE:
				if (player.getLevel() < 79)
				{
					st.exitQuest(true);
					htmltext = "30969-00.htm";
				}
				QuestState qs = player.getQuestState(SevenSignSecretRitualOfThePriests.QN);
				if (qs.isCompleted() && st.getState() == State.CREATED)
					htmltext = "30969-01.htm";
				else
				{
					switch (cond)
					{
						case 0:
							st.exitQuest(true);
							htmltext = "30969-00.htm";
							break;
						case 1:
							htmltext = "30969-05.htm";
							break;
						case 2:
							st.set(CONDITION, 3);
							htmltext = "30969-08.htm";
							break;
						case 5:
							htmltext = "30969-09.htm";
							break;
						case 6:
							htmltext = "30969-11.htm";
							break;
					}
				}
				break;
			case WOOD:
				if (cond == 6)
					htmltext = "32593-01.htm";
				break;
			case MAMMON:
				switch (cond)
				{
					case 1:
						htmltext = "32584-01.htm";
						break;
					case 2:
						htmltext = "32584-05.htm";
						break;
				}
				break;
			case PROMICE_OF_MAMMON:
				switch (cond)
				{
					case 3:
						enterInstance(player);
						break;
					case 4:
						enterInstance(player);
						break;
				}
				break;
			case MAGICAN:
				switch (cond)
				{
					case 4:
						if (st.getQuestItemsCount(STAFF) == 0)
							htmltext = "32598-01.htm";
						else if (st.getQuestItemsCount(STAFF) >= 1)
							htmltext = "32598-03.htm";
						break;
				}
				break;
			case SHUNAIMAN:
				switch (cond)
				{
					case 3:
						htmltext = "32586-01.htm";
						break;
					case 4:
						if (st.getQuestItemsCount(SEAL) <= 3)
							htmltext = "32586-07.htm";
						else if (st.getQuestItemsCount(SEAL) == 4)
							htmltext = "32586-08.htm";
						break;
					case 5:
						htmltext = "32586-12.htm";
						break;
				}
				break;
			case DISCIPLES_GK:
				switch (cond)
				{
					case 4:
						htmltext = "32657-01.htm";
						break;
				}
				break;
			case LEON:
				switch (cond)
				{
					case 5:
						Instance instanceObj = InstanceManager.getInstance().getInstance(player.getInstanceId());
						instanceObj.setDuration(300000);
						htmltext = "32587-01.htm";
						break;
				}
				break;

		}
		if (st.getState() == State.COMPLETED)
			htmltext = QUEST_DONE;
		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2Player player, boolean isPet)
	{
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return null;

		if (npc.getNpcId() == SEALDEVICE)
		{
			if (st.getQuestItemsCount(SEAL) < 3)
			{
				st.sendPacket(SND_ITEM_GET);
				st.giveItems(SEAL, 1);
			}
			else
			{
				st.giveItems(SEAL, 1);
				player.teleToLocation(-89528, 216056, -7516);
				st.sendPacket(SND_MIDDLE);
				player.showQuestMovie(13);

			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new SevenSignSealOfTheEmperor(196, QN, "Seven Signs Seal Of The Emperor");
	}
}