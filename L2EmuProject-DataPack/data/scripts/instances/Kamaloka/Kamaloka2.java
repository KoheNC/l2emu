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

public final class Kamaloka2 extends Quest
{
	private class Kama2World extends InstanceWorld
	{
		@SuppressWarnings("unused")
		private L2Npc	RB, GK, mob;
		@SuppressWarnings("unused")
		public int		index;
		@SuppressWarnings("unused")
		public int		templateId;

		public Kama2World(Long time)
		{
			InstanceManager.getInstance().super();
		}
	}

	private static final String		QN				= "Kamaloka2";

	private static final int		EXIT_ID			= 32496;
	private static final int		INSTANCEPENALTY	= 86400000;
	private static final int		MAX_PP_SIZE		= 9;
	private static final int[]		INSTANCE_ID		=
													{ 73, 74, 75, 76, 77, 78, 79 };
	private static final int[]		START_ID		=
													{ 30332, 30071, 30916, 30196, 31981, 31340 };
	private static final int[]		MIN_LVL			=
													{ 24, 34, 44, 54, 64, 73, 76 };
	private static final int[]		MAX_LVL			=
													{ 34, 44, 54, 64, 74, 85, 86 };
	private static final int[]		RB_ID			=
													{ 29129, 29132, 29135, 29138, 29141, 29144, 29147 };
	private static final String[]	TEMPLATE		=
													{
			"Kamaloka-29.xml",
			"Kamaloka-39.xml",
			"Kamaloka-49.xml",
			"Kamaloka-59.xml",
			"Kamaloka-69.xml",
			"Kamaloka-78.xml",	
			"Kamaloka-81.xml"		};

	private static final int[][]	TELEPORT		=
													{
													{ -76426, -174915, -11008 },
													{ -76547, -185548, -11008 },
													{ -43715, -174890, -10976 },
													{ -43731, -185520, -10976 },
													{ -10973, -174906, -10944 },
													{ -10965, -185532, -10944 },
													{ 21731, -174886, -10912 } };
													
	private static final int[][]			RAIDS	= {													
													{ 29129, -86225, -174916, -10042 },
													{ 29132, -86229, -185541, -10042 },
													{ 29135, -53416, -174887, -10010 },
													{ 29138, -53424, -185509, -10010 },
													{ 29141, -20660, -174901, -9978 },
													{ 29144, -20657, -185529, -9978 },
													{ 29147, 12047, -174885, -9946 } };
														
	private static final int[]	KILLLIST			= { 22485, 22486, 22487, 29131, 29130, 25616, 22488, 22489, 22490, 29134, 29133, 25617, 22492, 22491, 22493, 29137, 29136, 25618, 22494, 22495, 22496, 29140, 29139, 25619, 22497, 22498, 22499, 29143, 29142, 25620, 22500, 22501, 22502, 29146, 29145, 25621, 22503, 22504, 22505, 29149, 29148, 25622 };
	private static final int[][][] MOBLIST			=
													{ {
													{ 22485, -77781, -174916, -11012 },
													{ 22486, -77904, -174919, -11012 },
													{ 22486, -77654, -174916, -11012 },
													{ 22486, -77832, -174872, -11012 },
													{ 22486, -77729, -174871, -11012 },
													{ 22486, -77780, -174794, -11012 },
													{ 22486, -77823, -174960, -11012 },
													{ 22486, -77730, -174965, -11012 },
													{ 22486, -77775, -175040, -11012 },
													{ 22487, -80230, -174916, -10749 },
													{ 22487, -80114, -174920, -10749 },
													{ 22487, -79985, -174918, -10749 },
													{ 22487, -80111, -174792, -10749 },
													{ 22487, -80110, -175043, -10749 },
													{ 25616, -82448, -174918, -10486 } },
													{
													{ 22488, -77778, -185416, -11007 },
													{ 22489, -77831, -185497, -11012 },
													{ 22489, -77733, -185497, -11012 },
													{ 22489, -77905, -185541, -11012 },
    { 22489, -77775, -185545, -11012 },
    { 22489, -77650, -185542, -11012 },
    { 22489, -77824, -185585, -11012 },
    { 22489, -77732, -185590, -11012 },
	{ 22489, -77782, -185664, -11012 },
    { 22490, -80234, -185541, -10749 },
    { 22490, -80109, -185541, -10749 },
    { 22490, -79984, -185540, -10749 },
    { 22490, -80109, -185415, -10749 },
    { 22490, -80115, -185663, -10749 },
    { 25617, -82444, -185539, -10486 } },
	{
	{ 22492, -45030, -174949, -10980 },
    { 22491, -45022, -174840, -10980 },
    { 22492, -44929, -174844, -10980 },
    { 22492, -44976, -174759, -10980 },
    { 22492, -45102, -174888, -10980 },
    { 22492, -44983, -174889, -10980 },
    { 22492, -44849, -174886, -10980 },
    { 22492, -44927, -174944, -10980 },
    { 22492, -44984, -175013, -10980 },
    { 22493, -47435, -174886, -10717 },
    { 22493, -47311, -174885, -10717 },
    { 22493, -47185, -174886, -10717 },
    { 22493, -47312, -174760, -10717 },
    { 22493, -47312, -175012, -10717 },
    { 25618, -46645, -174883, -10454 } },
	{
	{ 22494, -44978, -185631, -10980 },
    { 22495, -45033, -185555, -10980 },
    { 22495, -44930, -185555, -10980 },
    { 22495, -45105, -185512, -10980 },
    { 22495, -44976, -185510, -10980 },
    { 22495, -44853, -185511, -10980 },
    { 22495, -45025, -185460, -10980 },
    { 22495, -44932, -185461, -10980 },
    { 22495, -44974, -185383, -10980 },
    { 22496, -47438, -185510, -10717 },
    { 22496, -47313, -185506, -10717 },
    { 22496, -47189, -185510, -10717 },
    { 22496, -47310, -185387, -10717 },
    { 22496, -47311, -185636, -10717 },
    { 25619, -49646, -185506, -10454 } },
	{
	{ 22497, -12256, -174948, -10948 },
    { 22498, -12209, -175029, -10948 },
    { 22498, -12340, -174900, -10948 },
    { 22498, -12209, -174900, -10948 },
    { 22498, -12085, -174902, -10948 },
    { 22498, -12146, -174949, -10948 },
    { 22498, -12258, -174852, -10948 },
    { 22498, -12164, -174854, -10948 },
    { 22498, -12210, -174773, -10948 },
    { 22499, -14662, -174904, -10685 },
    { 22499, -14542, -174903, -10685 },
    { 22499, -14419, -174906, -10685 },
    { 22499, -14544, -174779, -10685 },
    { 22499, -14543, -175024, -10685 },
    { 25620, -16877, -174904, -10422 } },
	{
	{ 22500, -12207, -185402, -10950 },
    { 22501, -12259, -185476, -10948 },
    { 22501, -12161, -185480, -10948 },
    { 22501, -12336, -185526, -10948 },
    { 22501, -12211, -185523, -10948 },
    { 22501, -12086, -185524, -10948 },
    { 22501, -12260, -185578, -10948 },
    { 22501, -12164, -185575, -10948 },
    { 22501, -12212, -185646, -10948 },
    { 22502, -14546, -185405, -10685 },
    { 22502, -14666, -185525, -10685 },
    { 22502, -14544, -185525, -10685 },
    { 22502, -14418, -185528, -10685 },
    { 22502, -14542, -185653, -10685 },
    { 25621, -16871, -185524, -104223 } },
	{
	{ 22503, 20501, -174885, -10916 },
    { 22504, 20366, -174886, -10916 },
    { 22504, 20450, -174834, -10916 },
    { 22504, 20536, -174836, -10916 },
    { 22504, 20617, -174883, -10916 },
    { 22504, 20496, -174765, -10916 },
    { 22504, 20444, -174938, -10916 },
    { 22504, 20548, -174939, -10916 },
    { 22504, 20489, -175010, -10916 },
    { 22505, 18030, -174884, -10653 },
    { 22505, 18168, -174887, -10653 },
    { 22505, 18289, -174884, -10653 },
    { 22505, 18161, -174764, -10653 },
    { 22505, 18160, -175007, -10653 },
    { 25622, 15823, -174884, -10390 } } };
	
	
	public Kamaloka2(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int starters : START_ID)
		{
			addStartNpc(starters);
			addTalkId(starters);
		}
		for (int rbs : RB_ID)
			addKillId(rbs);
		for (int killid : KILLLIST)
			addKillId(killid);

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
			if (!(world instanceof Kama2World))
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
			world = new Kama2World(System.currentTimeMillis() + 2700000);
			world.instanceId = instanceId;
			world.templateId = INSTANCE_ID[index];
			InstanceManager.getInstance().addWorld(world);
			((Kama2World)world).index = index;
			_log.info("Kamaloka " + TEMPLATE[index] + " Instance: " + instanceId + " created by player: " + player.getName());
			spawnMobs((Kama2World) world, index, MOBLIST[index]);
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


	protected void spawnExit(L2Npc npc)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof Kama2World)
		{
			Kama2World world = (Kama2World) tmpworld;
			world.GK = addSpawn(EXIT_ID, npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz(), 0, false, 0, false, world.instanceId);
		}
	}
	
	protected void spawnMobs(Kama2World world, int index, int[][] mobList)
	{
		for (int[] mobStuff : mobList)
		{
			world.mob = addSpawn(mobStuff[0], mobStuff[1], mobStuff[2], mobStuff[3], 0, false, 0, false, world.instanceId);
			world.mob.setIsNoRndWalk(true);
		}

	}
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		if (event.equalsIgnoreCase("0") || event.equalsIgnoreCase("1") || event.equalsIgnoreCase("2") || event.equalsIgnoreCase("3")
				|| event.equalsIgnoreCase("4") || event.equalsIgnoreCase("5") || event.equalsIgnoreCase("6"))
			enterInstance(player, Integer.valueOf(event));
		return "";
	}

	@Override
	public final String onKill(L2Npc npc, L2Player player, boolean isPet)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof Kama2World)
		{
			Kama2World world = (Kama2World) tmpworld;
			if (ArrayUtils.contains(RB_ID, npc.getNpcId()))
			{
				spawnExit(npc);
				InstanceManager.getInstance().getInstance(world.instanceId).setDuration(300000);
			}
			switch (npc.getNpcId())
			{
				case 25616:
					world.RB = addSpawn(RAIDS[0][0], RAIDS[0][1], RAIDS[0][2], RAIDS[0][3], 0, false, 0, false, world.instanceId);
					break;
				case 25617:
					world.RB = addSpawn(RAIDS[1][0], RAIDS[1][1], RAIDS[1][2], RAIDS[1][3], 0, false, 0, false, world.instanceId);
					break;
				case 25618:
					world.RB = addSpawn(RAIDS[2][0], RAIDS[2][1], RAIDS[2][2], RAIDS[2][3], 0, false, 0, false, world.instanceId);
					break;
				case 25619:
					world.RB = addSpawn(RAIDS[3][0], RAIDS[3][1], RAIDS[3][2], RAIDS[3][3], 0, false, 0, false, world.instanceId);
					break;	
				case 25620:
					world.RB = addSpawn(RAIDS[4][0], RAIDS[4][1], RAIDS[4][2], RAIDS[4][3], 0, false, 0, false, world.instanceId);
					break;
				case 25621:
					world.RB = addSpawn(RAIDS[5][0], RAIDS[5][1], RAIDS[5][2], RAIDS[5][3], 0, false, 0, false, world.instanceId);
					break;
				case 25622:
					world.RB = addSpawn(RAIDS[6][0], RAIDS[6][1], RAIDS[6][2], RAIDS[6][3], 0, false, 0, false, world.instanceId);
					break;
			}
		}
		return "";
	}

	public static void main(String[] args)
	{
		new Kamaloka2(-1, QN, "instances");
	}
}