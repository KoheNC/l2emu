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
package instances.Kamaloka;

import org.apache.commons.lang.ArrayUtils;

import net.l2emuproject.gameserver.ai.CtrlIntention;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.Quest;
import net.l2emuproject.gameserver.model.L2Party;
import net.l2emuproject.gameserver.model.entity.Instance;
import net.l2emuproject.gameserver.instancemanager.InstanceManager;
import net.l2emuproject.gameserver.instancemanager.InstanceManager.InstanceWorld;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.util.Util;

/**
* @author lewzer
* WORK IN PROGRESS
*/

public final class Kamaloka1 extends Quest
{
	private class Kama1World extends InstanceWorld
	{
		private L2Npc	RB, GK;
		public int		index;
		public int		templateId;

		public Kama1World(Long time)
		{
			InstanceManager.getInstance().super();
		}
	}

	private static final String		QN				= "Kamaloka1";

	private static final int		EXIT_ID			= 32496;
	private static final int		INSTANCEPENALTY	= 86400000;
	private static final int[]		INSTANCE_ID		=
													{ 57, 60, 63, 66, 69, 72 };
	private static final int[]		START_ID		=
													{ 30332, 30071, 30916, 30196, 31981, 31340 };
	private static final int[]		MIN_LVL			=
													{ 18, 28, 38, 48, 58, 68 };
	private static final int[]		MAX_LVL			=
													{ 28, 38, 48, 58, 68, 78 };
	private static final int[]		RB_ID			=
													{ 18554, 18558, 18562, 18566, 18571, 18577 };
	private static final String[]	TEMPLATE		=
													{
			"Kamaloka-23.xml",
			"Kamaloka-33.xml",
			"Kamaloka-43.xml",
			"Kamaloka-53.xml",
			"Kamaloka-63.xml",
			"Kamaloka-73.xml"						};

	private static final int[][]	TELEPORT		=
													{
													{ -57109, -219871, -8117 },
													{ -55492, -206143, -8117 },
													{ -49802, -206141, -8117 },
													{ -41201, -219859, -8117 },
													{ -57116, -219857, -8117 },
													{ -55823, -212935, -8071 } };
	private static final int[][]	TELEPORT_EXIT	=
													{
													{ -13870, 123767, -3117 },
													{ 18149, 146024, -3100 },
													{ 108449, 221607, -3598 },
													{ 80985, 56373, -1560 },
													{ 85945, -142176, -1341 },
													{ 42673, -47988, -797 } };

	public Kamaloka1(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int starters : START_ID)
		{
			addStartNpc(starters);
			addTalkId(starters);
		}
		for (int rbs : RB_ID)
			addKillId(rbs);

		addStartNpc(EXIT_ID);
		addTalkId(EXIT_ID);
	}

	private static final void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], true);
	}

	private boolean checkConditions(L2PcInstance player, int index)
	{
		/*
			L2Party party = player.getParty();
			if (party == null)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.NOT_IN_PARTY_CANT_ENTER));
				return false;
			}
			if (party.getLeader() != player)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER));
				return false;
			}
			for (L2PcInstance partyMember : party.getPartyMembers())
			{
				if (partyMember.getLevel() < MIN_LVL[index] || partyMember.getLevel() > MAX_LVL[index])
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
					sm.addPcName(partyMember);
					party.broadcastToPartyMembers(sm);
					return false;
				}
				if (!Util.checkIfInRange(1000, player, partyMember, true))
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
					sm.addPcName(partyMember);
					party.broadcastToPartyMembers(sm);
					return false;
				}
				Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), INSTANCE_ID[index]);
				if (System.currentTimeMillis() < reentertime)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.NO_RE_ENTER_TIME_FOR_C1);
					sm.addPcName(partyMember);
					party.broadcastToPartyMembers(sm);
					return false;
				}
			} */
		return true;
	}

	private int enterInstance(L2PcInstance player, int index)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (!(world instanceof Kama1World))
			{
				player.sendPacket(new SystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			teleportPlayer(player, TELEPORT[index], world.instanceId);
			return world.instanceId;
		}
		//New instance
		else
		{
			if (!checkConditions(player, index))
				return 0;
			L2Party party = player.getParty();
			int instanceId = InstanceManager.getInstance().createDynamicInstance(TEMPLATE[index]);
			world = new Kama1World(System.currentTimeMillis() + 1800000);
			world.instanceId = instanceId;
			world.templateId = INSTANCE_ID[index];
			InstanceManager.getInstance().addWorld(world);
			((Kama1World)world).index = index;
			_log.info("Kamaloka " + TEMPLATE[index] + " Instance: " + instanceId + " created by player: " + player.getName());
			//spawnRB((Kama1World) world, index, TELEPORT[index]);
			for (L2PcInstance partyMember : party.getPartyMembers())
			{
				InstanceManager.getInstance().setInstanceTime(partyMember.getObjectId(), INSTANCE_ID[index], ((System.currentTimeMillis() + INSTANCEPENALTY)));
				teleportPlayer(partyMember, TELEPORT[index], world.instanceId);
				world.allowed.add(partyMember.getObjectId());
			}
			return instanceId;
		}
	}

	protected void exitInstance(L2PcInstance player, L2Npc npc)
	{
		player.setInstanceId(0);
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof Kama1World)
		{
			Kama1World world = (Kama1World) tmpworld;
			player.teleToLocation(TELEPORT_EXIT[world.index][0], TELEPORT_EXIT[world.index][1], TELEPORT_EXIT[world.index][2]);
		}
	}

	protected void spawnRB(Kama1World world, int index, int[] spawnCoo)
	{
		world.RB = addSpawn(RB_ID[index], spawnCoo[0], spawnCoo[1], spawnCoo[2], 0, false, 0, false, world.instanceId);
	}

	protected void spawnExit(L2Npc npc)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof Kama1World)
		{
			Kama1World world = (Kama1World) tmpworld;
			world.GK = addSpawn(EXIT_ID, npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz(), 0, false, 0, false, world.instanceId);
		}
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("1") || event.equalsIgnoreCase("2") || event.equalsIgnoreCase("3") || event.equalsIgnoreCase("4")
				|| event.equalsIgnoreCase("5") || event.equalsIgnoreCase("6"))
			enterInstance(player, Integer.valueOf(event));
		return "";
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		int npcID = npc.getNpcId();
		switch (npcID)
		{
			case EXIT_ID:
				exitInstance(player, npc);
				break;
		}
		return "";
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (ArrayUtils.contains(RB_ID, npc.getNpcId()))
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof Kama1World)
			{
				Kama1World world = (Kama1World) tmpworld;
				spawnExit(npc);
				InstanceManager.getInstance().getInstance(world.instanceId).setDuration(300000);
			}
		}
		return "";
	}

	public static void main(String[] args)
	{
		new Kamaloka1(-1, QN, "instances");
	}
}