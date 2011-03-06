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

import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.manager.instances.InstanceManager;
import net.l2emuproject.gameserver.manager.instances.InstanceManager.InstanceWorld;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.party.L2Party;
import net.l2emuproject.gameserver.services.quest.Quest;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

import org.apache.commons.lang.ArrayUtils;

/**
* @author lewzer
* WORK IN PROGRESS
*/

public final class Kamaloka1 extends Quest
{
	private class Kama1World extends InstanceWorld
	{
		@SuppressWarnings("unused")
		private L2Npc	RB, GK;
		@SuppressWarnings("unused")
		public int		index;
		@SuppressWarnings("unused")
		public int		templateId;

		public Kama1World(Long time)
		{
			InstanceManager.getInstance().super();
		}
	}

	private static final String		QN				= "Kamaloka1";

	private static final int		EXIT_ID			= 32496;
	private static final int		INSTANCEPENALTY	= 86400000;
	private static final int		MAX_PP_SIZE		= 6;
	private static final int[]		INSTANCE_ID		=
													{ 57, 60, 63, 66, 69, 72, 58, 61, 64, 67, 70};
	private static final int[]		START_ID		=
													{ 30332, 30071, 30916, 30196, 31981, 31340 };
	private static final int[]		MIN_LVL			=
													{ 18, 28, 38, 48, 58, 68, 21, 31, 41, 51, 61};
	private static final int[]		MAX_LVL			=
													{ 28, 38, 48, 58, 68, 78, 31, 41, 51, 61, 71 };
	private static final int[]		RB_ID			=
													{ 18554, 18558, 18562, 18566, 18571, 18577, 18555, 18559, 18564, 18568, 18573 };
	private static final String[]	TEMPLATE		=
													{
			"Kamaloka-23.xml",
			"Kamaloka-33.xml",
			"Kamaloka-43.xml",
			"Kamaloka-53.xml",
			"Kamaloka-63.xml",
			"Kamaloka-73.xml",	
			"Kamaloka-26.xml",
			"Kamaloka-36.xml",
			"Kamaloka-46.xml",
			"Kamaloka-56.xml",
			"Kamaloka-66.xml"			};

	private static final int[][]	TELEPORT		=
													{
													{ -57109, -219871, -8117 },
													{ -55492, -206143, -8117 },
													{ -49802, -206141, -8117 },
													{ -41201, -219859, -8117 },
													{ -57116, -219857, -8117 },
													{ -55823, -212935, -8071 },
													{ -55556,-206144,-8117 },
													{ -41257,-213143,-8117 },
													{ -41184,-213144,-8117 },
													{ -57102,-206143,-8117 },
													{ -41228,-219860,-8117 } };

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

	}

	private static final void teleportPlayer(L2Player player, int[] coords, int instanceId)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], true);
	}

	private boolean checkConditions(L2Player player, int index)
	{
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
			if (party.getMemberCount() > MAX_PP_SIZE)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER));
				return false;
			}
			for (L2Player partyMember : party.getPartyMembers())
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
			} 
		return true;
	}

	private int enterInstance(L2Player player, int index)
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
			if(party == null)
			{
				teleportPlayer(player, TELEPORT[index], world.instanceId);
				world.allowed.add(player.getObjectId());
			}
			else
			{
				for (L2Player partyMember : party.getPartyMembers())
				{
					InstanceManager.getInstance().setInstanceTime(partyMember.getObjectId(), INSTANCE_ID[index], ((System.currentTimeMillis() + INSTANCEPENALTY)));
					teleportPlayer(partyMember, TELEPORT[index], world.instanceId);
					world.allowed.add(partyMember.getObjectId());
				}
			}
			return instanceId;
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
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		if (event.equalsIgnoreCase("0") || event.equalsIgnoreCase("1") || event.equalsIgnoreCase("2") || event.equalsIgnoreCase("3")
				|| event.equalsIgnoreCase("4") || event.equalsIgnoreCase("5") || event.equalsIgnoreCase("6") || event.equalsIgnoreCase("7") || event.equalsIgnoreCase("8") || event.equalsIgnoreCase("9") || event.equalsIgnoreCase("10"))
			enterInstance(player, Integer.valueOf(event));
		return "";
	}

	@Override
	public final String onKill(L2Npc npc, L2Player player, boolean isPet)
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