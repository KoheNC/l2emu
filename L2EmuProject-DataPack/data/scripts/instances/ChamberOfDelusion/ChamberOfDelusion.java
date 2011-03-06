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
package instances.ChamberOfDelusion;

import instances.ChamberOfDelusionEast.ChamberOfDelusionEast;
import instances.ChamberOfDelusionGreat.ChamberOfDelusionGreat;
import instances.ChamberOfDelusionNorth.ChamberOfDelusionNorth;
import instances.ChamberOfDelusionSouth.ChamberOfDelusionSouth;
import instances.ChamberOfDelusionTower.ChamberOfDelusionTower;
import instances.ChamberOfDelusionWest.ChamberOfDelusionWest;

import java.util.ArrayList;
import java.util.Calendar;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.manager.instances.InstanceManager;
import net.l2emuproject.gameserver.manager.instances.InstanceManager.InstanceWorld;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExShowScreenMessage;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.party.L2Party;
import net.l2emuproject.gameserver.services.quest.Quest;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.tools.random.Rnd;

/**
 * @author Stake
 * <br>based on d0s's script
 */
public class ChamberOfDelusion extends Quest
{
	protected int TEMPLATE_ID;
	protected int GKSTART;
	protected int GKFINISH;
	protected int AENKINEL;
	protected final TeleCoord EXIT_COORDS = new TeleCoord(-114592, -152509, -6723);
	protected final int RESET_HOUR = 6;
	protected final int RESET_MIN = 30;
	protected static final int[] AENKINEL_SPAWN = {-121463, -155094, -6752};
	protected static String QN = "ChamberOfDelusion";
	protected static final String Q_TYPE = "instances";
	protected static final int Q_ID = -1;
	protected static final int GUIDING_TEA_LEAVES = 15341;
	protected static final int TELEPORT[][] = new int[][]{
		{-122368, -152624, -6752},
		{-122368, -153504, -6752},
		{-120496, -154304, -6752},
		{-120496, -155184, -6752},
		{-121440, -151328, -6752},
		{-120496, -153008, -6752},
		{-122368, -154800, -6752},
		{-121440, -153008, -6752},
		{-121440, -154688, -6752} // this is Aenkinel's room
	};
	protected static final int[][] REWARD = {
		{57, (int) (Config.RATE_QUESTS_REWARD_ADENA*100)} // TODO: temp item, need to be set according to official chest rewards
	};
	//private static final Map<Integer, ChamberOfDelusion> dc_scripts = new FastMap<Integer, ChamberOfDelusion>();
	private static final int NIHIL_INVADER_TREASURE_CHESTS[] = {18819, 18820};
	private static final boolean DEBUG = false; // set it true ONLY for testing
	
	protected ChamberOfDelusion(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
	}
	
	private final void registerFirstTalk()
	{
		for(int i : NIHIL_INVADER_TREASURE_CHESTS)
			addFirstTalkId(i);
	}
	
	public final class CDWorld extends InstanceWorld
	{
		public L2Npc _aenkinel;
		public L2Npc[] manager = new L2Npc[9];
		public L2Npc[] chests = new L2Npc[4];
		public int cur_x, cur_y, cur_z = 0;
		public long startTime = 0;
		public byte change_room = 0;
		public byte max_rooms = 6;
		public ArrayList<Integer> roomIndexes = new ArrayList<Integer>();
		
		public CDWorld()
		{
			InstanceManager.getInstance().super();
			for(int i=0;i<TELEPORT.length;++i) {
				roomIndexes.add(i);
			}
		}
	}
	
	public final class TeleCoord
	{
		public final int instanceId;
		public final int x;
		public final int y;
		public final int z;
		public TeleCoord(final int instid, final int tx, final int ty, final int tz)
		{
			instanceId = instid;
			x = tx;
			y = ty;
			z = tz;
		}
		public TeleCoord(final int tx, final int ty, final int tz)
		{
			instanceId = 0;
			x = tx;
			y = ty;
			z = tz;
		}
	}
	
	protected final void spawnState(final CDWorld world)
	{
		world._aenkinel = addSpawn(AENKINEL, AENKINEL_SPAWN[0], AENKINEL_SPAWN[1], AENKINEL_SPAWN[2], 0, false, 0, false, world.instanceId);
		world._aenkinel.setIsNoRndWalk(false);
		for(int i=0;i<TELEPORT.length;i++)
			world.manager[i] = addSpawn(GKFINISH, TELEPORT[i][0], TELEPORT[i][1], TELEPORT[i][2], 0, false, 0, false, world.instanceId);
	}
	
	protected final boolean checkConditions(final L2Player player)
	{
		final L2Party party = player.getParty();
		if(party == null)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.NOT_IN_PARTY_CANT_ENTER));
			return false;
		}
		else if(party.getLeader() != player)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER));
			return false;
		}
		else
		{
			for(final L2Player partyMember : party.getPartyMembers())
			{
				if(partyMember.getLevel() < 80)
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
					sm.addPcName(partyMember);
					party.broadcastToPartyMembers(sm);
					return false;
				}
				else if(!Util.checkIfInRange(1000, player, partyMember, true))
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
					sm.addPcName(partyMember);
					party.broadcastToPartyMembers(sm);
					return false;
				}
				final Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), TEMPLATE_ID);
				if(System.currentTimeMillis() < reentertime)
				{
					final SystemMessage sm = new SystemMessage(2100);
					sm.addPcName(partyMember);
					party.broadcastToPartyMembers(sm);
					return false;
				}
			}
		}
		return true;
	}
	
	protected final void teleportPlayer(final L2Player player, final TeleCoord teleto)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(teleto.instanceId);
		player.teleToLocation(teleto.x, teleto.y, teleto.z);
	}
	
	protected final boolean teleportParty(final L2Player player, final TeleCoord teleto)
	{
		final L2Party party = player.getParty();
		if(party == null)
		{
			teleportPlayer(player, teleto);
		}
		else
		{
			if(party.getLeader() != player)
				return false;
			final InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
			if(tmpworld instanceof CDWorld)
			{
				for(final L2Player partyMember : party.getPartyMembers())
				{
					if(isPlayerInInstancePartyRange(partyMember, player, (CDWorld)tmpworld))
						teleportPlayer(partyMember, teleto);
				}
			}
		}
		return true;
	}
	


	protected final void teleportrnd(final L2Player player, final CDWorld world)
	{
		if(world.roomIndexes.size() < 1)
			return;
		
		final int tp = world.roomIndexes.get(Rnd.get(world.roomIndexes.size()-1)); // subtract 1 as players can't teleport to Aenkinel on random
		final int tp_room[] = TELEPORT[tp];
		teleportParty(player, new TeleCoord(world.instanceId, tp_room[0], tp_room[1], tp_room[2]));
		world.cur_x = tp_room[0];
		world.cur_y = tp_room[1];
		world.cur_z = tp_room[2];
		world.roomIndexes.remove(tp);
	}
	
	protected final void penalty(final CDWorld world)
	{
		final Calendar reenter = Calendar.getInstance();
		reenter.add(Calendar.HOUR, RESET_HOUR);
		reenter.add(Calendar.MINUTE, RESET_MIN);
		final SystemMessage sm = new SystemMessage(SystemMessageId.INSTANT_ZONE_S1_ENTRY_RESTRICTED);
		sm.addString(InstanceManager.getInstance().getInstanceIdName(world.templateId));
		for(int objectId : world.allowed)
		{
			final L2Player player = L2World.getInstance().getPlayer(objectId);
			if(player != null && player.isOnline() > 0)
			{
				InstanceManager.getInstance().setInstanceTime(objectId, world.templateId, reenter.getTimeInMillis());
				player.sendPacket(sm);
			}
		}
	}
	
	protected final int enterInstance(final L2Player player, final String template)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		final CDWorld world;
		if(tmpworld != null)
		{
			if(!(tmpworld instanceof CDWorld) || tmpworld.templateId != TEMPLATE_ID)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return tmpworld.instanceId;
			}
			world = (CDWorld) tmpworld;
			teleportPlayer(player, new TeleCoord(world.instanceId, world.cur_x, world.cur_y, world.cur_z));
			return world.instanceId;
		}
		else
		{
			if(!checkConditions(player))
				return 0;
			final L2Party party = player.getParty();
			final int instanceId = InstanceManager.getInstance().createDynamicInstance(template);
			world = new CDWorld();
			world.instanceId = instanceId;
			world.templateId = TEMPLATE_ID;
			world.status = 0;
			world.startTime = System.currentTimeMillis();
			InstanceManager.getInstance().addWorld(world);
			_log.info("Chamber Of Delusion started: "+template+" - Instance: "+instanceId+" created by player: "+player.getName());
			spawnState((CDWorld) world);
			if(party != null)
			{
				for(final L2Player partyMember : party.getPartyMembers())
				{
					world.allowed.add(partyMember.getObjectId());
					if(partyMember.getQuestState(QN) == null)
						newQuestState(partyMember);
				}
			}
			else
			{
				world.allowed.add(player.getObjectId());
			}
			if(!DEBUG)
				penalty(world);
			
			changeRoom(player, world);
			return instanceId;
		}
	}
	
	protected final void changeRoom(final L2Player player, final CDWorld world)
	{
		world.status++;
		teleportrnd(player, world);
		if(world.status < world.max_rooms)
		{
			final long time = (Rnd.get(3)*60000)+480000;
			startQuestTimer("tproom"+world.status, time, null, player, false);
			if(player.getParty() != null)
				player.getParty().broadcastToPartyMembers(new ExShowScreenMessage((time/60000)+" minutes to the next room.", 10000));
			else
				player.sendPacket(new ExShowScreenMessage((time/60000)+" minutes to the next room.", 10000));
		}
		else if(world.status == world.max_rooms)
		{
			final long time = 3300000 - (System.currentTimeMillis() - world.startTime);
			if(time > 0)
				startQuestTimer("aenkinel", time, null, player, false);
			
			if(DEBUG)
				_log.info("Aenkinel spawn time: "+time);
		}
	}
	
	protected final void spawnChests(final CDWorld world)
	{
		for(int i=0;i<world.chests.length;i++)
		{
			world.chests[i] = addSpawn(NIHIL_INVADER_TREASURE_CHESTS[((i<1)?0:1)], AENKINEL_SPAWN[0]+((Rnd.get(11)-5)*20), AENKINEL_SPAWN[1]+((Rnd.get(11)-5)*20), AENKINEL_SPAWN[2], (Rnd.get(4)+1)*16384, false, 0, false, world.instanceId);
			world.chests[i].setIsNoRndWalk(true);
		}
	}
	
	protected static final boolean isPlayerInInstancePartyRange(final L2Player player, final L2Player partyLeader, final CDWorld world)
	{
		if(Util.checkIfInRange(Config.ALT_PARTY_RANGE, partyLeader, player, true)
				&& world.allowed.contains(player.getObjectId()))
			return true;
		return false;
	}
	
	protected static final boolean isPlayerThePartyLeaderOfCurrentInstance(final L2Player player, final CDWorld world)
	{
		if(player.getParty() != null && player.getParty().getLeader() == player
				&& world.allowed.contains(player.getObjectId()))
			return true;
		return false;
	}
	
	@Override
	public String onKill(final L2Npc npc, final L2Player player, final boolean isPet)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if(!(tmpworld instanceof CDWorld))
			return null;
		
		final CDWorld world = (CDWorld) tmpworld;
		if(npc.getNpcId() == AENKINEL)
		{
			_log.info("Raidboss "+npc.getName()+" has been killed by "+player.getName());
			final L2Party party = player.getParty();
			if(party != null)
			{
				QuestState st;
				for(final L2Player partyMember : party.getPartyMembers())
				{
					st = partyMember.getQuestState(QN);
					if(st != null && isPlayerInInstancePartyRange(partyMember, player, world))
						st.giveItems(GUIDING_TEA_LEAVES, 1);
				}
			}
			spawnChests(world);
		}
		return null;
	}
	
	@Override
	public final String onAdvEvent(final String event, final L2Npc npc, final L2Player player)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if(!(tmpworld instanceof CDWorld))
			return null;
		
		final CDWorld world = (CDWorld) tmpworld;
		if(event.startsWith("tproom"))
		{
			if(DEBUG)
				_log.info("Timed room change: "+event);
			changeRoom(player, world);
		}
		else if(event.equalsIgnoreCase("nextroom"))
		{
			if(!isPlayerThePartyLeaderOfCurrentInstance(player, world))
				return "Only party leader can change room.";
			else if(world.change_room != 0)
				return "You may change room only once.";
			
			if(DEBUG)
				_log.info("Current room: "+world.status);
			else
				world.change_room = 1;
			
			if(world.status < world.max_rooms)
				cancelQuestTimers("tproom"+world.status);
			
			changeRoom(player, world);
		}
		else if(event.equalsIgnoreCase("aenkinel"))
		{
			teleportParty(player, new TeleCoord(world.instanceId, TELEPORT[TELEPORT.length-1][0], TELEPORT[TELEPORT.length-1][1], TELEPORT[TELEPORT.length-1][2]));
		}
		return null;
	}
	
	@Override
	public String onFirstTalk(final L2Npc npc, final L2Player player)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if(!(tmpworld instanceof CDWorld))
			return null;
		
		final CDWorld world = (CDWorld) tmpworld;
		
		final int npcId = npc.getNpcId();
		if(npcId == GKFINISH)
		{
			return npc.getNpcId()+".htm";
		}
		else if(npcId == NIHIL_INVADER_TREASURE_CHESTS[0])
		{
			if(isPlayerThePartyLeaderOfCurrentInstance(player, world))
			{
				String chest_html = "<html><body>"+npc.getName()+":<br>%reward%</body></html>";
				for(int i=0;i<REWARD.length;i++)
					player.addItem(QN, REWARD[i][0], REWARD[i][1], null, true);
				npc.deleteMe();
				return chest_html.replace("%reward%", "Lucky one! You've found me, the real treasure chest.");
			}
			else
				return "Only party leader can take the reward.";
		}
		else if(npcId == NIHIL_INVADER_TREASURE_CHESTS[1])
		{
			if(isPlayerThePartyLeaderOfCurrentInstance(player, world))
			{
				String chest_html = "<html><body>"+npc.getName()+":<br>%reward%</body></html>";
				for(int i=0;i<world.chests.length;i++)
					if(world.chests[i] != null)
						world.chests[i].deleteMe();
				return chest_html.replace("%reward%", "Wrong chest.");
			}
			else
				return "Only party leader can take the reward.";
		}
		return null;
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2Player player)
	{
		final int npcId = npc.getNpcId();
		if(npcId == GKSTART)
		{
			enterInstance(player, QN+".xml");
		}
		else if(npcId == GKFINISH)
		{
			if(!teleportParty(player, EXIT_COORDS))
				return "Only party leader can use this function.";
			
			for(int i=1;i<6;i++)
				cancelQuestTimers("tproom"+i);
			cancelQuestTimers("aenkinel");
		}
		return null;
	}
	
	protected final void initScript()
	{
		//dc_scripts.put(INSTANCE_ID, this);
		addStartNpc(GKSTART);
		addStartNpc(GKFINISH);
		addFirstTalkId(GKFINISH);
		addTalkId(GKSTART);
		addTalkId(GKFINISH);
		addKillId(AENKINEL);
	}
	
	public static void main(final String[] args)
	{
		new ChamberOfDelusion(Q_ID, QN, Q_TYPE).registerFirstTalk();
		new ChamberOfDelusionGreat();
		new ChamberOfDelusionTower();
		new ChamberOfDelusionNorth();
		new ChamberOfDelusionEast();
		new ChamberOfDelusionSouth();
		new ChamberOfDelusionWest();
		_log.info("ChamberOfDelusion has been initialized.");
	}
}