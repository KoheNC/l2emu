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
package instances.PailakaDevilsLegacy;

import net.l2emuproject.gameserver.ThreadPoolManager;
import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.manager.instances.InstanceManager;
import net.l2emuproject.gameserver.manager.instances.InstanceManager.InstanceWorld;
import net.l2emuproject.gameserver.model.entity.Instance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.MagicSkillUse;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.L2Summon;
import net.l2emuproject.gameserver.world.object.instance.L2MonsterInstance;
import net.l2emuproject.gameserver.world.zone.L2Zone;
import net.l2emuproject.tools.random.Rnd;

import org.apache.commons.lang.ArrayUtils;

public class PailakaDevilsLegacy extends QuestJython
{
	private static final String	QN				= "129_PailakaDevilsLegacy";

	private static final int	MIN_LEVEL		= 61;
	private static final int	MAX_LEVEL		= 67;
	private static final int	INSTANCE_ID		= 44;
	private static final int[]	TELEPORT		=
												{ 76438, -219035, -3752 };

	// NPC
	private static final int	DISURVIVOR		= 32498;
	private static final int	SUPPORTER		= 32501;
	private static final int	DADVENTURER		= 32508;
	private static final int	DADVENTURER2	= 32511;
	private static final int[]	MONSTERS		=
												{ 18623, 18624, 18625, 18626, 18627 };
	private static final int	CHEST			= 32495;
	// BOSS
	private static final int	KAMS			= 18629;
	private static final int	ALKASO			= 18631;
	private static final int	LEMATAN			= 18633;
	private static final int	MINION			= 18634;
	// ITEMS
	private static final int	SWORD			= 13042;
	private static final int	ENCHSWORD		= 13043;
	private static final int	LASTSWORD		= 13044;
	private static final int	KDROP			= 13046;
	private static final int	ADROP			= 13047;
	private static final int[]	HERBS			=
												{ 8601, 8602, 8604, 8605 };
	private static final int[]	CHESTDROP		=
												{ 13033, 13048, 13049, 13059 };
	// REWARDS
	private static final int	PBRACELET		= 13295;

	private static final int[]	AMOUNTS1		=
												{ 1, 2 };
	private static final int[]	AMOUNTS2		=
												{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

	private static final int[]	ITEMS			=
												{ KDROP, ADROP, SWORD, ENCHSWORD, LASTSWORD, 13033, 13032, 13048, 13049, 13059, 13150 };

	private int					status			= 0;

	public PailakaDevilsLegacy(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(DISURVIVOR);

		addTalkId(DISURVIVOR);
		addTalkId(SUPPORTER);
		addTalkId(DADVENTURER);
		addTalkId(DADVENTURER2);
		addKillId(KAMS);
		addKillId(ALKASO);
		addKillId(LEMATAN);
		addKillId(CHEST);
		for (int i : MONSTERS)
			addKillId(i);
		addAttackId(MINION);

		questItemIds = ITEMS;
	}

	private static final void teleportPlayer(L2Player player, int[] coords, int instanceId)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], true);
	}

	private final synchronized void enterInstance(L2Player player)
	{
		//check for existing instances for this player
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
		//New instance
		else
		{
			final int instanceId = InstanceManager.getInstance().createDynamicInstance("PailakaDevilsLegacy.xml");

			world = InstanceManager.getInstance().new InstanceWorld();
			world.instanceId = instanceId;
			world.templateId = INSTANCE_ID;
			InstanceManager.getInstance().addWorld(world);

			world.allowed.add(player.getObjectId());
			teleportPlayer(player, TELEPORT, instanceId);

			_log.info("PailakaDevilsLegacy (Lvl 61-67): " + instanceId + " created by player: " + player.getName());
		}
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		final QuestState st = player.getQuestState(QN);
		if (st == null)
			return NO_QUEST;

		int cond = st.getInt(CONDITION);
		if (event.equalsIgnoreCase("enter"))
		{
			enterInstance(player);
			return null;
		}
		else if (event.equalsIgnoreCase("32498-02.htm"))
		{
			if (cond == 0)
			{
				st.set(CONDITION, 1);
				st.setState(State.STARTED);
				st.sendPacket(SND_ACCEPT);
			}
		}
		else if (event.equalsIgnoreCase("32498-05.htm"))
		{
			st.set(CONDITION, 2);
			st.sendPacket(SND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32501-03.htm"))
		{
			st.set(CONDITION, 3);
			st.sendPacket(SND_MIDDLE);
			st.giveItems(SWORD, 1);
		}
		else if (event.equalsIgnoreCase("first_anim"))
		{
			if (npc != null)
				npc.broadcastPacket(new MagicSkillUse(npc, npc, 5756, 1, 2500, 0));
		}
		else if (event.equalsIgnoreCase("lematanMinions"))
		{
			for (int i = 0; i < 6; i++)
			{
				int radius = 260;
				int x = (int) (radius * Math.cos(i * 0.918));
				int y = (int) (radius * Math.sin(i * 0.918));
				L2Npc mob = addSpawn(MINION, 84982 + x, -208690 + y, -3337, 0, false, 0);
				mob.setInstanceId(npc.getInstanceId());
			}
		}
		else if (event.equalsIgnoreCase("lematanMinions1"))
		{
			if (player.getInstanceId() != 0)
			{
				L2Npc mob = addSpawn(MINION, player.getX() + 50, player.getY() - 50, player.getZ(), 0, false, 0);
				mob.setInstanceId(npc.getInstanceId());
			}
			else
			{
				L2Npc mob = addSpawn(MINION, 84982, -208690, -3337, 0, false, 0);
				mob.setInstanceId(npc.getInstanceId());
			}
		}

		return event;
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2Player player)
	{
		return npc.getNpcId() + ".htm";
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		int npcId = npc.getNpcId();
		String htmltext = "32498-01.htm";
		QuestState st = player.getQuestState(QN);
		if (st == null)
			return htmltext;

		int id = st.getState();
		int cond = st.getInt(CONDITION);

		if (id == State.CREATED)
			st.set(CONDITION, 0);
		if (npcId == DISURVIVOR)
		{
			if (cond == 0)
			{
				if (player.getLevel() < MIN_LEVEL || player.getLevel() > MAX_LEVEL)
				{
					htmltext = "32498-no.htm";
					st.exitQuest(true);
				}
				else
					return htmltext;
			}
			else if (id == State.COMPLETED)
				htmltext = "32498-no.htm";
			else if (cond == 1 || cond == 2)
				htmltext = "32498-06.htm";
			else
				htmltext = "32498-07.htm";
		}
		else if (npcId == SUPPORTER)
		{
			if (cond == 1 || cond == 2)
				htmltext = "32501-01.htm";
			else
				htmltext = "32501-04.htm";
		}
		else if (npcId == DADVENTURER)
		{
			if (st.getQuestItemsCount(SWORD) > 0 && st.getQuestItemsCount(KDROP) == 0)
				htmltext = "32508-01.htm";
			if (st.getQuestItemsCount(ENCHSWORD) > 0 && st.getQuestItemsCount(ADROP) == 0)
				htmltext = "32508-01.htm";
			if (st.getQuestItemsCount(SWORD) == 0 && st.getQuestItemsCount(KDROP) > 0)
				htmltext = "32508-05.htm";
			if (st.getQuestItemsCount(ENCHSWORD) == 0 && st.getQuestItemsCount(ADROP) > 0)
				htmltext = "32508-05.htm";
			if (st.getQuestItemsCount(SWORD) == 0 && st.getQuestItemsCount(ENCHSWORD) == 0)
				htmltext = "32508-05.htm";
			if (st.getQuestItemsCount(KDROP) == 0 && st.getQuestItemsCount(ADROP) == 0)
				htmltext = "32508-01.htm";
			if (player.getPet() != null)
				htmltext = "32508-04.htm";
			if (st.getQuestItemsCount(SWORD) > 0 && st.getQuestItemsCount(KDROP) > 0)
			{
				st.takeItems(SWORD, 1);
				st.takeItems(KDROP, 1);
				st.giveItems(ENCHSWORD, 1);
				htmltext = "32508-02.htm";
			}
			if (st.getQuestItemsCount(ENCHSWORD) > 0 && st.getQuestItemsCount(ADROP) > 0)
			{
				st.takeItems(ENCHSWORD, 1);
				st.takeItems(ADROP, 1);
				st.giveItems(LASTSWORD, 1);
				htmltext = "32508-03.htm";
			}
			if (st.getQuestItemsCount(LASTSWORD) > 0)
				htmltext = "32508-03.htm";
		}
		else if (npcId == DADVENTURER2)
		{
			if (cond == 4)
			{
				if (player.getPet() != null)
					htmltext = "32511-03.htm";
				else if (player.getPet() == null)
				{
					st.takeItems(SWORD, st.getQuestItemsCount(SWORD));
					st.takeItems(ENCHSWORD, st.getQuestItemsCount(ENCHSWORD));
					st.takeItems(LASTSWORD, st.getQuestItemsCount(LASTSWORD));
					st.giveItems(13129, 1);
					st.takeItems(13033, st.getQuestItemsCount(13033));
					st.takeItems(13048, st.getQuestItemsCount(13048));
					st.takeItems(13049, st.getQuestItemsCount(13049));
					st.takeItems(13059, st.getQuestItemsCount(13059));
					st.giveItems(PBRACELET, 1);
					st.addExpAndSp(10810000, 950000);
					st.set(CONDITION, 5);
					st.setState(State.COMPLETED);
					st.sendPacket(SND_FINISH);
					st.exitQuest(false);
					Instance instanceObj = InstanceManager.getInstance().getInstance(player.getInstanceId());
					instanceObj.setDuration(300000);
					htmltext = "32511-01.htm";
					player.getPlayerVitality().setVitalityPoints(20000, true);
				}
			}
			else if (id == State.COMPLETED)
				htmltext = "32511-02.htm";
		}
		return htmltext;
	}

	@Override
	public String onAttack(L2Npc npc, L2Player attacker, int damage, boolean isPet)
	{
		if (npc.getNpcId() == LEMATAN)
		{
			int maxHp = npc.getMaxHp();
			double nowHp = npc.getStatus().getCurrentHp();
			if (nowHp < maxHp * 0.5)
			{
				if (this.status == 0)
				{
					this.status = 1;
					attacker.teleToLocation(84570, -208327, -3337);
					L2Summon pet = attacker.getPet();
					if (pet != null)
						pet.teleToLocation(84570, -208327, -3337, true);
					npc.teleToLocation(84982, -208690, -3337);
					startQuestTimer("lematanMinions", 3000, npc, null);
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public final String onKill(L2Npc npc, L2Player player, boolean isPet)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
			return "";

		int npcId = npc.getNpcId();
		if (npcId == KAMS && st.getQuestItemsCount(KDROP) == 0)
			st.giveItems(KDROP, 1);
		else if (npcId == ALKASO && st.getQuestItemsCount(ADROP) == 0)
			st.giveItems(ADROP, 1);
		else if (npcId == LEMATAN)
		{
			st.set(CONDITION, 4);
			st.sendPacket(SND_MIDDLE);
			addSpawn(DADVENTURER2, 84990, -208376, -3342, 55000, false, 0, false, npc.getInstanceId());
			this.status = 0;
		}
		else if (npcId == MINION)
		{
			if (this.status == 1)
				startQuestTimer("lematanMinions1", 10000, npc, player);
		}
		else if (ArrayUtils.contains(MONSTERS, npcId))
		{
			if (Rnd.get(100) < 80)
				st.dropItem((L2MonsterInstance) npc, player, HERBS[Rnd.get(HERBS.length)], AMOUNTS1[Rnd.get(AMOUNTS1.length)]);
		}
		else if (npcId == CHEST)
		{
			if (Rnd.get(100) < 80)
				st.dropItem((L2MonsterInstance) npc, player, CHESTDROP[Rnd.get(CHESTDROP.length)], AMOUNTS2[Rnd.get(AMOUNTS2.length)]);
			else
				st.dropItem((L2MonsterInstance) npc, player, 13150, 1);
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onExitZone(L2Character character, L2Zone zone)
	{
		if (character instanceof L2Player && !character.isDead() && !character.isTeleporting() && ((L2Player) character).isOnline() > 0)
		{
			InstanceWorld world = InstanceManager.getInstance().getWorld(character.getInstanceId());
			if (world != null && world.templateId == INSTANCE_ID)
				ThreadPoolManager.getInstance().scheduleGeneral(new Teleport(character, world.instanceId), 1000);
		}
		return super.onExitZone(character, zone);
	}

	static final class Teleport implements Runnable
	{
		private final L2Character	_char;
		private final int			_instanceId;

		public Teleport(L2Character c, int id)
		{
			_char = c;
			_instanceId = id;
		}

		@Override
		public void run()
		{
			try
			{
				teleportPlayer((L2Player) _char, TELEPORT, _instanceId);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args)
	{
		new PailakaDevilsLegacy(129, QN, "Pailaka - Devils Legacy");
	}
}