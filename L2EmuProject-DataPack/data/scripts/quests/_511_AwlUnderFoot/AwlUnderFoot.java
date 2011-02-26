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
package quests._511_AwlUnderFoot;

import gnu.trove.TIntObjectHashMap;
import net.l2emuproject.gameserver.ThreadPoolManager;
import net.l2emuproject.gameserver.events.global.fortsiege.Fort;
import net.l2emuproject.gameserver.manager.instances.InstanceManager;
import net.l2emuproject.gameserver.manager.instances.InstanceManager.InstanceWorld;
import net.l2emuproject.gameserver.model.entity.Instance;
import net.l2emuproject.gameserver.model.party.L2Party;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.skills.SkillHolder;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.L2Playable;
import net.l2emuproject.gameserver.world.object.instance.L2RaidBossInstance;
import net.l2emuproject.tools.random.Rnd;

import org.apache.commons.lang.ArrayUtils;

/**
 * @author Gigiikun
 */
public final class AwlUnderFoot extends QuestJython
{
	private static final String				QN					= "_511_AwlUnderFoot";

	private static final long				REENTERTIME			= 14400000;
	private static final long				RAID_SPAWN_DELAY	= 120000;

	// QUEST ITEMS
	private static final int				DL_MARK				= 9797;

	// REWARDS
	private static final int				KNIGHT_EPALUETTE	= 9912;

	// MONSTER TO KILL -- Only last 3 Raids (lvl ordered) give DL_MARK
	private static final int[]				RAIDS1				=
																{ 25572, 25575, 25578 };
	private static final int[]				RAIDS2				=
																{ 25579, 25582, 25585, 25588 };
	private static final int[]				RAIDS3				=
																{ 25589, 25592, 25593 };

	private static final SkillHolder		RAID_CURSE			= new SkillHolder(5456, 1);

	private TIntObjectHashMap<FortDungeon>	_fortDungeons		= new TIntObjectHashMap<FortDungeon>(21);

	private class FAUWorld extends InstanceWorld
	{
		private FAUWorld()
		{
			InstanceManager.getInstance().super();
		}
	}

	public class FortDungeon
	{
		private final int	INSTANCEID;
		private long		_reEnterTime	= 0;

		public FortDungeon(int iId)
		{
			INSTANCEID = iId;
		}

		public int getInstanceId()
		{
			return INSTANCEID;
		}

		public long getReEnterTime()
		{
			return _reEnterTime;
		}

		public void setReEnterTime(long time)
		{
			_reEnterTime = time;
		}
	}

	public AwlUnderFoot(int questId, String name, String descr)
	{
		super(questId, name, descr);

		_fortDungeons.put(35666, new FortDungeon(22));
		_fortDungeons.put(35698, new FortDungeon(23));
		_fortDungeons.put(35735, new FortDungeon(24));
		_fortDungeons.put(35767, new FortDungeon(25));
		_fortDungeons.put(35804, new FortDungeon(26));
		_fortDungeons.put(35835, new FortDungeon(27));
		_fortDungeons.put(35867, new FortDungeon(28));
		_fortDungeons.put(35904, new FortDungeon(29));
		_fortDungeons.put(35936, new FortDungeon(30));
		_fortDungeons.put(35974, new FortDungeon(31));
		_fortDungeons.put(36011, new FortDungeon(32));
		_fortDungeons.put(36043, new FortDungeon(33));
		_fortDungeons.put(36081, new FortDungeon(34));
		_fortDungeons.put(36118, new FortDungeon(35));
		_fortDungeons.put(36149, new FortDungeon(36));
		_fortDungeons.put(36181, new FortDungeon(37));
		_fortDungeons.put(36219, new FortDungeon(38));
		_fortDungeons.put(36257, new FortDungeon(39));
		_fortDungeons.put(36294, new FortDungeon(40));
		_fortDungeons.put(36326, new FortDungeon(41));
		_fortDungeons.put(36364, new FortDungeon(42));

		for (int i : _fortDungeons.keys())
		{
			addStartNpc(i);
			addTalkId(i);
		}

		for (int i : RAIDS1)
			addKillId(i);
		for (int i : RAIDS2)
			addKillId(i);
		for (int i : RAIDS3)
			addKillId(i);

		for (int i = 25572; i <= 25595; i++)
			addAttackId(i);
	}

	private String checkConditions(L2Player player)
	{
		L2Party party = player.getParty();
		if (party == null)
			return "FortressWarden-03.htm";
		if (party.getLeader() != player)
			return getHtm("FortressWarden-04.htm").replace("%leader%", party.getLeader().getName());
		for (L2Player partyMember : party.getPartyMembers())
		{
			QuestState st = partyMember.getQuestState(QN);
			if (st == null || st.getInt(CONDITION) < 1)
				return getHtm("FortressWarden-05.htm").replace("%player%", partyMember.getName());
			if (!Util.checkIfInRange(1000, player, partyMember, true))
				return getHtm("FortressWarden-06.htm").replace("%player%", partyMember.getName());
		}
		return null;
	}

	private void teleportPlayer(L2Player player, int[] coords, int instanceId)
	{
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2]);
	}

	private String enterInstance(L2Player player, String template, int[] coords, FortDungeon dungeon, String ret)
	{
		//check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		//existing instance
		if (world != null)
		{
			if (!(world instanceof FAUWorld))
			{
				player.sendPacket(new SystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return "";
			}
			teleportPlayer(player, coords, world.instanceId);
			return "";
		}
		//New instance
		else
		{
			if (ret != null)
				return ret;
			ret = checkConditions(player);
			if (ret != null)
				return ret;
			L2Party party = player.getParty();
			int instanceId = InstanceManager.getInstance().createDynamicInstance(template);
			Instance ins = InstanceManager.getInstance().getInstance(instanceId);
			ins.setSpawnLoc(new int[]
			{ player.getX(), player.getY(), player.getZ() });
			world = new FAUWorld();
			world.instanceId = instanceId;
			world.templateId = dungeon.getInstanceId();
			world.status = 0;
			dungeon.setReEnterTime(System.currentTimeMillis() + REENTERTIME);
			InstanceManager.getInstance().addWorld(world);
			_log.info("Fortress AwlUnderFoot started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
			ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRaidTask((FAUWorld) world), RAID_SPAWN_DELAY);

			// teleport players
			if (player.getParty() == null)
			{
				teleportPlayer(player, coords, instanceId);
				world.allowed.add(player.getObjectId());
			}
			else
			{
				for (L2Player partyMember : party.getPartyMembers())
				{
					teleportPlayer(partyMember, coords, instanceId);
					world.allowed.add(partyMember.getObjectId());
					if (partyMember.getQuestState(QN) == null)
						newQuestState(partyMember);
				}
			}
			return getHtm("FortressWarden-08.htm").replace("%clan%", player.getClan().getName());
		}
	}

	private final class SpawnRaidTask implements Runnable
	{
		private FAUWorld	_world;

		public SpawnRaidTask(FAUWorld world)
		{
			_world = world;
		}

		@Override
		public void run()
		{
			try
			{
				int spawnId;
				if (_world.status == 0)
					spawnId = RAIDS1[Rnd.get(RAIDS1.length)];
				else if (_world.status == 1)
					spawnId = RAIDS2[Rnd.get(RAIDS2.length)];
				else
					spawnId = RAIDS3[Rnd.get(RAIDS3.length)];

				L2Npc raid = addSpawn(spawnId, 53319, 245814, -6576, 0, false, 0, false, _world.instanceId);

				if (raid instanceof L2RaidBossInstance)
				{
					//((L2RaidBossInstance)raid).setUseRaidCurse(false);
					// TODO: Raid Curse...
				}
			}
			catch (Exception e)
			{
				_log.warn("Fortress AwlUnderFoot Raid Spawn error: " + e);
			}
		}
	}

	private String checkFortCondition(L2Player player, L2Npc npc, boolean isEnter)
	{
		Fort fortress = npc.getFort();
		FortDungeon dungeon = _fortDungeons.get(npc.getNpcId());
		if (player == null || fortress == null || dungeon == null)
			return "FortressWarden-01.htm";
		if (player.getClan() == null || player.getClan().getHasFort() != fortress.getFortId())
			return "FortressWarden-01.htm";
		else if (fortress.getFortState() == 0)
			return "FortressWarden-02a.htm";
		else if (fortress.getFortState() == 2)
			return "FortressWarden-02b.htm";
		else if (isEnter && dungeon.getReEnterTime() > System.currentTimeMillis())
			return "FortressWarden-07.htm";

		return null;
	}

	private void rewardPlayer(L2Player player)
	{
		QuestState st = player.getQuestState(QN);
		if (st.getInt(CONDITION) == 1)
		{
			st.giveItems(DL_MARK, 140);
			st.sendPacket(SND_ITEM_GET);
		}
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		if (event.equalsIgnoreCase("enter"))
		{
			int[] tele = new int[3];
			tele[0] = 53322;
			tele[1] = 246380;
			tele[2] = -6580;
			return enterInstance(player, "fortdungeon.xml", tele, _fortDungeons.get(npc.getNpcId()), checkFortCondition(player, npc, true));
		}
		QuestState st = player.getQuestState(QN);
		if (st == null)
			st = newQuestState(player);

		int cond = st.getInt(CONDITION);
		if (event.equalsIgnoreCase("FortressWarden-10.htm"))
		{
			if (cond == 0)
			{
				st.set(CONDITION, "1");
				st.setState(State.STARTED);
				st.sendPacket(SND_ACCEPT);
			}
		}
		else if (event.equalsIgnoreCase("FortressWarden-15.htm"))
		{
			st.sendPacket(SND_FINISH);
			st.exitQuest(true);
		}
		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);
		String ret = checkFortCondition(player, npc, false);
		if (ret != null)
			return ret;
		else if (st != null)
		{
			int npcId = npc.getNpcId();
			int cond = 0;
			if (st.getState() == State.CREATED)
				st.set(CONDITION, "0");
			else
				cond = st.getInt(CONDITION);
			if (_fortDungeons.containsKey(npcId) && cond == 0)
			{
				if (player.getLevel() >= 60)
					htmltext = "FortressWarden-09.htm";
				else
				{
					htmltext = "FortressWarden-00.htm";
					st.exitQuest(true);
				}
			}
			else if (_fortDungeons.containsKey(npcId) && cond > 0 && st.getState() == State.STARTED)
			{
				long count = st.getQuestItemsCount(DL_MARK);
				if (cond == 1 && count > 0)
				{
					htmltext = "FortressWarden-14.htm";
					st.takeItems(DL_MARK, count);
					st.rewardItems(KNIGHT_EPALUETTE, count);
				}
				else if (cond == 1 && count == 0)
					htmltext = "FortressWarden-10.htm";
			}
		}
		return htmltext;
	}

	@Override
	public final String onAttack(L2Npc npc, L2Player player, int damage, boolean isPet)
	{
		L2Playable attacker = (isPet ? player.getPet() : player);
		if (attacker.getLevel() - npc.getLevel() >= 9)
		{
			if (attacker.getBuffCount() > 0 || attacker.getDanceCount(true, true) > 0)
			{
				npc.setTarget(attacker);
				npc.doSimultaneousCast(RAID_CURSE.getSkill());
			}
			else if (player.getParty() != null)
			{
				for (L2Player pmember : player.getParty().getPartyMembers())
				{
					if (pmember.getBuffCount() > 0 || pmember.getDanceCount(true, true) > 0)
					{
						npc.setTarget(pmember);
						npc.doSimultaneousCast(RAID_CURSE.getSkill());
					}
				}
			}
		}
		return "";
	}

	@Override
	public final String onKill(L2Npc npc, L2Player player, boolean isPet)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof FAUWorld)
		{
			FAUWorld world = (FAUWorld) tmpworld;
			if (ArrayUtils.contains(RAIDS3, npc.getNpcId()))
			{
				if (player.getParty() != null)
					for (L2Player pl : player.getParty().getPartyMembers())
						rewardPlayer(pl);
				else
					rewardPlayer(player);

				Instance instanceObj = InstanceManager.getInstance().getInstance(world.instanceId);
				instanceObj.setDuration(360000);
				instanceObj.removeNpcs();
			}
			else
			{
				world.status++;
				ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRaidTask(world), RAID_SPAWN_DELAY);
			}
		}
		return "";
	}

	public static void main(String[] args)
	{
		// now call the constructor (starts up the)
		new AwlUnderFoot(511, QN, "Awl Under Foot");
	}
}
