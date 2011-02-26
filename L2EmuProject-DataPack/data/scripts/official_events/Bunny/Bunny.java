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
package official_events.Bunny;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

import javolution.util.FastList;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.ThreadPoolManager;
import net.l2emuproject.gameserver.datatables.NpcTable;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.datatables.SpawnTable;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ItemList;
import net.l2emuproject.gameserver.network.serverpackets.NpcSay;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.quest.Quest;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.system.script.DateRange;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Attackable;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2FlyMonsterInstance;
import net.l2emuproject.gameserver.world.object.instance.L2MonsterInstance;
import net.l2emuproject.gameserver.world.spawn.L2Spawn;
import net.l2emuproject.tools.random.Rnd;

/**
 * @author Gigiikun
 */
public class Bunny extends QuestJython
{
	private static final String			QN				= "Bunny";
	public static final String			SPAWNLIST_FILE	= "/data/scripts/official_events/Bunny/spawnlist.csv";

	private static final String[]		TEXT			=
														{
			"I am telling the truth.",
			"A relaxing feeling is moving through my stomach.",
			"I am nothing.",
			"Boo-hoo... I hate...",
			"You will regret this.",
			"see you later.",
			"You've made a great choice.",
			"Did you see that Firecracker explode?",
			"If you need to go to Fantasy Isle, come see me.",
			"All of Fantasy Isle is a Peace Zone.",
			"If you collect 50 individual Treasure Sack Pieces, you can exchange them for a Treasure Sack.",
			"Startled",
			"Bumps"									};

	private static FastList<L2Spawn>	_spawns			= new FastList<L2Spawn>();

	private boolean						_isEventTime	= false;

	private static final DateRange		EVENT_DATES		= DateRange.parse(Config.BUNNY_DATE, new SimpleDateFormat("dd MM yyyy", Locale.US));

	public Bunny(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addEventId(32365, Quest.QuestEventType.ON_TALK);
		addEventId(32365, Quest.QuestEventType.ON_FIRST_TALK);
		addEventId(32365, Quest.QuestEventType.QUEST_START);
		addEventId(13097, Quest.QuestEventType.ON_SKILL_SEE);
		addEventId(13097, Quest.QuestEventType.ON_SPAWN);
		addEventId(13097, Quest.QuestEventType.ON_FIRST_TALK);
		addEventId(13098, Quest.QuestEventType.ON_SKILL_SEE);
		addEventId(13098, Quest.QuestEventType.ON_SPAWN);
		addEventId(13098, Quest.QuestEventType.ON_FIRST_TALK);

		final Date currentDate = new Date();

		if (EVENT_DATES.isWithinRange(currentDate))
		{
			_isEventTime = true;
			registerDrops();
			load();
			doSpawns();
		}
	}

	private final void registerDrops()
	{
		for (int level = 1; level < 100; level++)
		{
			L2NpcTemplate[] templates = NpcTable.getInstance().getAllOfLevel(level);
			if ((templates != null) && (templates.length > 0))
			{
				for (L2NpcTemplate t : templates)
				{
					try
					{
						if (t.isAssignableTo(L2Attackable.class))
							addEventId(t.getNpcId(), Quest.QuestEventType.ON_KILL);
					}
					catch (RuntimeException e)
					{
						_log.warn("", e);
					}
				}
			}
		}
	}

	private final void doSpawns()
	{
		if (_spawns.isEmpty() || _spawns.size() == 0)
			return;
		for (L2Spawn spawn : _spawns)
		{
			if (spawn == null)
				continue;
			spawn.doSpawn();
			spawn.startRespawn();
		}
	}

	private final void deleteSpawns()
	{
		if (_spawns.isEmpty() || _spawns.size() == 0)
			return;
		for (L2Spawn spawn : _spawns)
		{
			if (spawn == null)
				continue;
			spawn.stopRespawn();
			spawn.getLastSpawn().doDie(spawn.getLastSpawn());
		}
	}

	private final void load()
	{
		Scanner s;
		try
		{
			s = new Scanner(new File(Config.DATAPACK_ROOT + SPAWNLIST_FILE));
		}
		catch (Exception e)
		{
			_log.warn("Bunny Event: Can not find '" + Config.DATAPACK_ROOT + SPAWNLIST_FILE);
			return;
		}
		int lineCount = 0;
		_spawns.clear();
		while (s.hasNextLine())
		{
			lineCount++;
			String line = s.nextLine();

			if (line.startsWith("#"))
				continue;
			else if (line.equals(""))
				continue;

			String[] lineSplit = line.split(";");

			boolean ok = true;
			int npcID = 0;

			try
			{
				npcID = Integer.parseInt(lineSplit[0]);
			}
			catch (Exception e)
			{
				_log.warn("Bunny Event: Error in line " + lineCount + " -> invalid npc id or wrong seperator after npc id!");
				_log.warn("		" + line);
				ok = false;
			}
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcID);
			if (template == null)
			{
				_log.warn("Bunny Event: NPC Id " + npcID + " not found!");
				continue;
			}
			if (!ok)
				continue;

			String[] lineSplit2 = lineSplit[1].split(",");

			int x = 0, y = 0, z = 0, heading = 0, respawn = 0;

			try
			{
				x = Integer.parseInt(lineSplit2[0]);
				y = Integer.parseInt(lineSplit2[1]);
				z = Integer.parseInt(lineSplit2[2]);
				heading = Integer.parseInt(lineSplit2[3]);
				respawn = Integer.parseInt(lineSplit2[4]);
			}

			catch (Exception e)
			{
				_log.warn("Bunny Event: Error in line " + lineCount + " -> incomplete/invalid data or wrong seperator!");
				_log.warn("		" + line);
				ok = false;
			}

			if (!ok)
				continue;
			try
			{
				L2Spawn spawnDat = new L2Spawn(template);
				spawnDat.setAmount(1);
				spawnDat.setLocx(x);
				spawnDat.setLocy(y);
				spawnDat.setLocz(z);
				spawnDat.setHeading(heading);
				spawnDat.setRespawnDelay(respawn);
				SpawnTable.getInstance().addNewSpawn(spawnDat, false);
				_spawns.add(spawnDat);
				//spawnDat.doSpawn();
				//spawnDat.startRespawn();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		s.close();
	}

	@Override
	public String onSkillSee(L2Npc npc, L2Player caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (npc.isInsideRadius(caster, 150, false, false) && npc.getNpcId() == 13097 && skill.getId() == 629)
			spawnChests(npc);
		if (caster.getTarget() == npc && npc.getNpcId() == 13098 && skill.getId() == 630)
			dropReward(npc, caster);

		return "";
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		if (isDigit(event) && npc != null)
		{
			switch (Integer.valueOf(event))
			{
				case 0:
					if (Rnd.get(5) == 0)
						shout(npc, Rnd.get(3));
					break;
				case 1:
					cancelQuestTimer("0", npc, null);
					shout(npc, Rnd.get(3, 5));
					npc.deleteMe();
					break;
				case 2:
					cancelQuestTimer("0", npc, null);
					shout(npc, 6);
					break;
				case 3:
					shout(npc, 7);
					break;
				case 4:
					if (Rnd.get(3) == 0)
						shout(npc, Rnd.get(8, 10));
					break;
				case 5:
					if (Rnd.get(100) == 0)
					{
						if (Rnd.get(2) == 0)
							shout(npc, 12);
						else
						{
							shout(npc, 11);
						}
					}
					break;
			}
			return "";
		}
		else if (player != null)
		{
			QuestState st = player.getQuestState("Bunny");
			if (st != null)
			{
				if (event.equalsIgnoreCase("sacks"))
				{
					if (st.getQuestItemsCount(10272) < 50)
						return "32365-04.htm";
					int chance = Rnd.get(1000);
					if (chance < 5)
						st.giveItems(10254, 1);
					else if (chance < 50)
						st.giveItems(10255, 1);
					else if (chance < 150)
						st.giveItems(10256, 1);
					else if (chance < 300)
						st.giveItems(10257, 1);
					else if (chance < 550)
						st.giveItems(10258, 1);
					else
						st.giveItems(10259, 1);
					st.takeItems(10272, 50);

					return "32365-01.htm";
				}
				else if (event.equalsIgnoreCase("scroll"))
				{
					if (!_isEventTime)
						return "32365-07.htm";
					else if (player.getLevel() < 61)
						return "32365-06.htm";
					String time = loadGlobalQuestVar(player.getAccountName());
					if (time == "")
					{
						if (st.getQuestItemsCount(57) < 500)
							return "32365-05.htm";
						st.takeAdena(500);
						st.giveItems(10274, 1);
						saveGlobalQuestVar(player.getAccountName(), String.valueOf(System.currentTimeMillis() + 86400000));
					}
					else
					{
						long remain = Long.valueOf(time) - System.currentTimeMillis();
						if (remain <= 60000)
						{
							if (st.getQuestItemsCount(57) < 500)
								return "32365-05.htm";
							st.takeAdena(500);
							st.giveItems(10274, 1);
							saveGlobalQuestVar(player.getAccountName(), String.valueOf(System.currentTimeMillis() + 86400000));
						}
						else
						{
							int hours = (int) remain / 1000 / 60 / 60;
							int minutes = (int) remain / 1000 / 60 % 60;
							SystemMessage sm;
							if (hours > 0)
							{
								sm = new SystemMessage(SystemMessageId.ITEM_PURCHASABLE_IN_S1_HOURS_S2_MINUTES);
								sm.addNumber(hours);
								sm.addNumber(minutes);
							}
							else
							{
								sm = new SystemMessage(SystemMessageId.ITEM_PURCHASABLE_IN_S1_MINUTES);
								sm.addNumber(minutes);
							}
							player.sendPacket(sm);
						}
					}
					return "32365-01.htm";
				}
			}
		}
		return event;
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		switch (npc.getNpcId())
		{
			case 32365:
				startQuestTimer("4", 60000, npc, null, true);
				break;
			case 13097:
				startQuestTimer("5", 5000, npc, null, true);
				npc.disableCoreAI(true);
				break;
			case 13098:
				if (Rnd.get(100) < 5)
				{
					npc.setTarget(npc);
					npc.doCast(SkillTable.getInstance().getInfo(3156, 1));
					startQuestTimer("3", 2000, npc, null);
					npc.setIsInvul(true);
				}
				else
					startQuestTimer("0", 2000, npc, null, true);
				startQuestTimer("1", 18000, npc, null, false);
				npc.disableCoreAI(true);
				break;

		}
		return "";
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2Player player)
	{
		switch (npc.getNpcId())
		{
			case 32365:
				QuestState st = player.getQuestState("Bunny");
				if (st == null)
					st = newQuestState(player);
				player.setLastQuestNpcObject(npc.getObjectId());
				return "32365-01.htm";
			case 13097:
				break;
			case 13098:
				break;

		}
		return "";
	}

	private void dropReward(L2Npc npc, L2Player player)
	{
		if (npc.isInvul())
		{
			int itemId = 10272;
			int count = 1;
			npc.setIsInvul(false);
			if (Rnd.get(100) < 50)
			{
				int chance = Rnd.get(100);
				if (chance < 1)
					itemId = 10254;
				else if (chance < 10)
					itemId = 10255;
				else if (chance < 30)
					itemId = 10256;
				else if (chance < 60)
					itemId = 10257;
				else
					itemId = 10258;
			}
			else
				count += Rnd.get(7, 12);
			L2ItemInstance item = player.getInventory().addItem("event", itemId, count, player, npc);
			if (item != null)
			{
				if (count > 1)
				{
					SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
					smsg.addItemName(item);
					smsg.addNumber(count);
					player.sendPacket(smsg);
				}
				else
				{
					SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_S1);
					smsg.addItemName(item);
					player.sendPacket(smsg);
				}
				player.sendPacket(new ItemList(player, false));
			}
		}
		L2ItemInstance item = ((L2MonsterInstance) npc).dropItem(player, 10272, Rnd.get(3, 7));
		if (item.getOwnerId() != player.getObjectId() || item.getItemLootShedule() == null)
		{
			item.setOwnerId(player.getObjectId());
			item.setItemLootShedule(ThreadPoolManager.getInstance().scheduleGeneral(new ResetOwner(item), 5000));
		}
		cancelQuestTimer("0", npc, null);
		cancelQuestTimer("1", npc, null);
		startQuestTimer("2", 10, npc, null, false);
		npc.reduceCurrentHp(9999, npc, null);
	}

	private final class ResetOwner implements Runnable
	{
		L2ItemInstance	_item;

		public ResetOwner(L2ItemInstance item)
		{
			_item = item;
		}

		@Override
		public void run()
		{
			_item.setOwnerId(0);
			_item.setItemLootShedule(null);
		}
	}

	private void spawnChests(L2Npc npc)
	{
		int x = 0, y = 0;
		int z = npc.getZ();
		for (int i = 0; i < 3; i++)
		{
			switch (i)
			{
				case 0:
					x = npc.getX() + 150;
					y = npc.getY() + 120;
					addSpawn(13098, x, y, z, 0, false, 0);
					break;
				case 1:
					x = npc.getX() - 150;
					y = npc.getY() + 120;
					addSpawn(13098, x, y, z, 0, false, 0);
					break;
				case 2:
					x = npc.getX();
					y = npc.getY() - 130;
					addSpawn(13098, x, y, z, 0, false, 0);
					break;
			}
		}
		cancelQuestTimer("5", npc, null);
		npc.getSpawn().decreaseCount(npc);
		npc.deleteMe();
	}

	private void shout(L2Npc npc, int id)
	{
		npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), TEXT[id]));
	}

	private boolean isDigit(String str)
	{
		for (int i = 0; i < str.length(); i++)
			if (!Character.isDigit(str.charAt(i)))
				return false;

		return true;
	}

	@Override
	public String onKill(L2Npc npc, L2Player killer, boolean isPet)
	{
		if ((killer.getLevel() - npc.getLevel()) >= 8)
			return super.onKill(npc, killer, isPet);
		if (_isEventTime && npc instanceof L2FlyMonsterInstance && Rnd.get(100) < Config.BUNNY_DROP_FLY_CHANCE)
			killer.addItem("", 10272, 1, null, true, true);
		else if (_isEventTime && npc instanceof L2MonsterInstance && Rnd.get(100) < Config.BUNNY_DROP_CHANCE)
		{
			L2ItemInstance item = ((L2MonsterInstance) npc).dropItem(killer, 10272, 1);
			if (item.getOwnerId() != killer.getObjectId() || item.getItemLootShedule() == null)
			{
				item.setOwnerId(killer.getObjectId());
				item.setItemLootShedule(ThreadPoolManager.getInstance().scheduleGeneral(new ResetOwner(item), 5000));
			}
		}
		return "";
	}

	@Override
	public boolean unload()
	{
		deleteSpawns();
		return super.unload();
	}

	public static void main(String[] args)
	{
		if (Config.ALLOW_BUNNY)
		{
			new Bunny(-1, QN, "official_events");
			_log.info("Official Events: Bunny is loaded.");
		}
		else
			_log.info("Official Events: Bunny is disabled.");
	}
}
