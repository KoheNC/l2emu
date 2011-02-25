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
package quests._512_AwlUnderFoot;

import java.util.Calendar;
import java.util.List;

import javolution.util.FastList;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.instancemanager.CastleManager;
import net.l2emuproject.gameserver.instancemanager.FortManager;
import net.l2emuproject.gameserver.instancemanager.InstanceManager;
import net.l2emuproject.gameserver.instancemanager.InstanceManager.InstanceWorld;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.clan.L2Clan;
import net.l2emuproject.gameserver.model.entity.Castle;
import net.l2emuproject.gameserver.model.entity.Fort;
import net.l2emuproject.gameserver.model.entity.Instance;
import net.l2emuproject.gameserver.model.party.L2Party;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.util.Util;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.tools.random.Rnd;

import org.apache.commons.lang.ArrayUtils;

/**
 * @author CAMMARO, converted by Angy
 *   Remade for L2EmuProject: lord_rex
 */
public final class AwlUnderFoot2 extends QuestJython
{
	private static final String	QN							= "_512_AwlUnderFoot";

	// NPC
	private static int[]		WARDEN						=
															{ 36403, 36404, 36405, 36406, 36407, 36408, 36409, 36410, 36411 };

	// MONSTER TO KILL -- Only last 3 Raids (lvl ordered) from 10, drop DL_MARK
	private static int[]		RAIDS1						=
															{ 25546, 25549, 25552 };
	private static int[]		RAIDS2						=
															{ 25553, 25554, 25557, 25560 };
	private static int[]		RAIDS3						=
															{ 25563, 25566, 25569 };

	// QUEST ITEMS
	private static final int	DL_MARK						= 9798;

	// REWARDS
	private static final int	KNIGHT_EPALUETTE			= 9912;

	// MESSAGES 
	private static final String	TOO_LOW_LEVEL				= "<html><body><br>"
																	+ "Detention Camp Warden:<br>Thank you for volunteering to help, kind adventurer, but we can't allow inexperienced clan members to put themselves in "
																	+ "jeopardy -- bad for our reputation, you know.<br>Believe it or not, I was once a prominent adventurer before taking this job.<br> "
																	+ "If I can give you some advice, you're not quite ready for such a task.<br>Why don't you come back to me after you've had an "
																	+ "opportunity to further develop your skills.<br>(This quest is only available for characters Level 70 or higher.)"
																	+ "</body></html>";

	private static final String	WRONG_CASTLE				= "<html><body><br>Detention Camp Warden:<br>This castle is not under your clan " + "control."
																	+ "</body></html>";

	private static final String	NO_CLAN						= "<html><body><br>Detention Camp Warden:<br>Who are you? I don't see your name "
																	+ "on the list of clan members...<br>  (This quest is reserved for members of the clan that currently owns this castle.)"
																	+ "</body></html>";

	private static final String	ALREADY_DONE				= "<html><body>Detention Camp Warden:<br>Ah, I understand -- no doubt another "
																	+ "adventure calls you...<br>Thank you for all you've done here.<br>If you'd like to lend a hand again, we'd be very grateful.<br>"
																	+ "Good luck on your journeys!" + "</body></html>";

	private static final String	NO_FORT						= "<html><body>Detention Camp Warden:<br>"
																	+ "Castle must be contracted with fort to enter the prison";

	private static final long	RIM_PAILAKA_REENTER_TIME	= 14400000;

	private List<Integer>		world_ids					= new FastList<Integer>();
	private InstanceWorld		world;

	private class CCWorld extends InstanceWorld
	{
		public CCWorld(Long time)
		{
			InstanceManager.getInstance().super();
		}
	}

	private class teleCoord
	{
		int	instanceId;
		int	x;
		int	y;
		int	z;
	}

	private void reward(QuestState st)
	{
		if (st.getState() == State.STARTED)
		{
			if (st.getRandom(9) > 5)
			{
				// No retail info about drop quantity. Guesed 1-2. 60% for 1 and 40% for 2 
				st.giveItems(DL_MARK, (int) (2 * Config.RATE_DROP_QUEST));
				st.sendPacket(SND_ITEM_GET);
			}
			else if (st.getRandom(9) < 6)
			{
				st.giveItems(DL_MARK, (int) (1 * Config.RATE_DROP_QUEST));
				st.sendPacket(SND_ITEM_GET);
			}
		}
	}

	public boolean checkConditions(L2PcInstance player, boolean isNew)
	{
		if (player.getClan() == null)
		{
			player.sendMessage("You are not in clan that holds this castle, so you cannot enter.");
			return false;
		}

		Castle castle = CastleManager.getInstance().getCastleByOwner(player.getClan());
		if (castle == null)
		{
			player.sendMessage("You are not in clan that holds this castle, so you cannot enter.");
			return false;
		}

		Calendar cal = castle.getSiege().getSiegeDate();
		long diff = cal.getTimeInMillis() - System.currentTimeMillis();
		if (diff > 0 && diff < 60 * 60 * 1000)
		{
			player.sendMessage("Siege is soon, so you cannot enter.");
			return false;
		}

		L2Party party = player.getParty();
		if (party == null)
		{
			player.sendMessage("You are not currently in a party, so you cannot enter.");
			return false;
		}

		for (L2PcInstance partyMember : party.getPartyMembers())
		{
			if (partyMember.getLevel() < 70)
			{
				player.sendMessage(partyMember.getName() + "s level requirement is not sufficient and cannot be entered.");
				return false;
			}

			if (!Util.checkIfInRange(1000, player, partyMember, true) && isNew)
			{
				player.sendMessage(partyMember.getName() + " is in a location which cannot be entered, therefore it cannot be processed.");
				return false;
			}

			if (partyMember.getClan() == null)
			{
				player.sendMessage(partyMember.getName() + " is not a clan member of clan that owns this castle.");
				return false;
			}

			if (player.getClanId() != partyMember.getClanId())
			{
				player.sendMessage(partyMember.getName() + " is not a clan member of clan that owns this castle.");
				return false;
			}
		}

		return true;
	}

	private void teleportPlayer(L2PcInstance player, teleCoord teleto)
	{
		player.setInstanceId(teleto.instanceId);
		player.teleToLocation(teleto.x, teleto.y, teleto.z);
	}

	private int enterInstance(L2PcInstance player, String template, teleCoord teleto)
	{
		int instanceId = 0;
		if (!checkEnter(player))
			return 0;

		L2Party party = player.getParty();
		QuestState st;

		// check for other instances of party members
		if (party != null)
		{
			for (L2PcInstance partyMember : party.getPartyMembers())
			{
				st = partyMember.getQuestState(QN);
				if (st != null)
				{
					if (st.getState() != State.STARTED)
					{
						player.sendPacket(SystemMessage.sendString(partyMember.getName() + " not take Awl under foot quest."));
						return 0;
					}
				}
				else
				{
					player.sendPacket(SystemMessage.sendString(partyMember.getName() + " not take Awl under foot quest."));
					return 0;
				}

				if (partyMember.getInstanceId() != 0)
					instanceId = partyMember.getInstanceId();
			}
		}
		else
		{
			if (player.getInstanceId() != 0)
				instanceId = player.getInstanceId();
		}

		// exising instance
		if (instanceId != 0)
		{
			if (!checkConditions(player, false))
				return 0;

			boolean foundworld = false;
			for (int worldid : world_ids)
				if (worldid == instanceId)
				{
					foundworld = true;
					break;
				}

			if (!foundworld)
			{
				player.sendMessage("You have entered another instance zone, therefore you cannot enter corresponding dungeon.");
				return 0;
			}

			teleto.instanceId = instanceId;
			teleportPlayer(player, teleto);
			return instanceId;
		}
		// new instance
		else
		{
			if (!checkConditions(player, true))
				return 0;

			instanceId = InstanceManager.getInstance().createDynamicInstance(template);
			if (!world_ids.contains(instanceId))
			{
				world = new CCWorld(System.currentTimeMillis() + 5400000);
				InstanceManager.getInstance().addWorld(world);

				//world.rewarded = {};
				world.instanceId = instanceId;
				world_ids.add(instanceId);

				_log.info("Rim Pailaka: started. " + template + " Instance: " + instanceId + " created by player: " + player.getName());

				long reenterTime = System.currentTimeMillis() + RIM_PAILAKA_REENTER_TIME;
				String castleName = CastleManager.getInstance().getCastleByOwner(player.getClan()).getName();

				saveGlobalQuestVar(castleName, String.valueOf(reenterTime));

				spawnRaid(world.instanceId, 1);
			}

			//teleports player
			teleto.instanceId = instanceId;

			for (L2PcInstance partyMember : party.getPartyMembers())
			{
				st = partyMember.getQuestState(QN);
				st.set(CONDITION, 1);
				teleportPlayer(partyMember, teleto);
			}

			return instanceId;
		}
	}

	private void exitInstance(L2Npc npc)
	{
		int instanceId = npc.getInstanceId();
		if (world_ids.contains(instanceId))
		{
			Instance instanceObj = InstanceManager.getInstance().getInstance(instanceId);
			instanceObj.setDuration(60000);
			instanceObj.removeNpcs();
			world_ids.remove((Object) instanceId);
		}
	}

	private void spawnRaid(int instanceId, int raid)
	{
		int spawnId = 0;
		switch (raid)
		{
			case 1:
				spawnId = RAIDS1[Rnd.get(0, 2)];
				break;
			case 2:
				spawnId = RAIDS2[Rnd.get(0, 3)];
				break;
			case 3:
				spawnId = RAIDS3[Rnd.get(0, 2)];
				break;
			default:
				_log.error("512_AwlUnderFoot: raid = " + raid + " in spawnRaid(..) func");
				break;
		}

		if (spawnId > 0)
			addSpawn(spawnId, 12161, -49144, -3000, 0, false, 0, false, instanceId);
	}

	private boolean checkEnter(L2PcInstance player)
	{
		long enterTime = 0;

		String enter = loadGlobalQuestVar(CastleManager.getInstance().getCastleByOwner(player.getClan()).getName());
		try
		{
			enterTime = Long.valueOf(enter);
		}
		catch (Exception e)
		{
			enterTime = 0;
		}

		if (enterTime > 0)
		{
			long remain = enterTime - System.currentTimeMillis();
			if (remain <= 0)
			{
				return true;
			}
			else
			{
				player.sendMessage("Cant enter now. Check back in: " + remain / 60000 + " min.");
				return false;
			}
		}

		return true;
	}

	private boolean checkLeader(L2PcInstance player)
	{
		if (player.getParty() == null)
		{
			player.sendMessage("You are not currently in a party, so you cannot enter.");
			return false;
		}
		else if (!player.getParty().isLeader(player))
		{
			player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}

		return true;
	}

	public AwlUnderFoot2(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int npcId : WARDEN)
		{
			addStartNpc(npcId);
			addTalkId(npcId);
			addFirstTalkId(npcId);
		}

		for (int mobId : RAIDS1)
			addKillId(mobId);

		for (int mobId : RAIDS2)
			addKillId(mobId);

		for (int mobId : RAIDS3)
			addKillId(mobId);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmlText = event;
		QuestState st = player.getQuestState(QN);
		int cond = st.getInt(CONDITION);

		if (event.equalsIgnoreCase("warden_yes.htm"))
		{
			if (cond == 0)
			{
				st.set(CONDITION, 1);
				st.setState(State.STARTED);
				st.sendPacket(SND_ACCEPT);
			}
		}
		else if (event.equalsIgnoreCase("quit"))
		{
			htmlText = ALREADY_DONE;
			st.sendPacket(SND_FINISH);
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("continue"))
		{
			st.set(CONDITION, 1);
			htmlText = "warden_yes.htm";
		}
		else if (event.equalsIgnoreCase("warden_no.htm"))
		{
			st.set(CONDITION, 0);
			htmlText = "warden_no.htm";
		}
		else if (event.equalsIgnoreCase("default"))
		{
			htmlText = NO_QUEST;
		}
		else if (event.equalsIgnoreCase("rumor"))
		{
			htmlText = "rumor.htm";
		}
		else if (event.equalsIgnoreCase("leave"))
		{
			htmlText = "";
			if (checkLeader(player))
			{
				exitInstance(npc);
				htmlText = "Instance deleted!";
			}
		}
		else if (event.equalsIgnoreCase("status"))
		{
			long entertime;
			try
			{
				entertime = Long.valueOf(loadGlobalQuestVar("512_NextEnter"));
			}
			catch (Exception e)
			{
				entertime = 0;
			}

			String text = "The dungeon is empty and you can enter now.";
			if (entertime > 0)
			{
				long remain = entertime - System.currentTimeMillis();
				if (remain > 0)
				{
					long timeleft = remain / 60000;
					if (timeleft > 180)
						text = "The dungeon is under other party control. Wait for them to get out and try again.";
					else
						text = "You cant enter now. Check again in: " + timeleft + " min.";
				}
			}

			htmlText = st.showHtmlFile("status.htm").replace("%text%", text);
		}
		else if (event.equalsIgnoreCase("enter"))
		{
			if (checkLeader(player))
			{
				teleCoord tele = new teleCoord();
				tele.x = 11740;
				tele.y = -49148;
				tele.z = -3000;
				enterInstance(player, "RimPailakaCastle.xml", tele);
			}
			else
				htmlText = "";
		}
		else
			htmlText = "";

		return htmlText;

	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmlText = NO_QUEST;
		QuestState st = player.getQuestState(QN);
		int CastleOwnerId = npc.getCastle().getOwnerId();
		L2Clan Clan = player.getClan();

		if (Clan == null)
			return NO_CLAN;

		if (CastleOwnerId != Clan.getClanId())
			return WRONG_CASTLE;

		boolean ok = false;
		for (Fort fort : FortManager.getInstance().getForts())
		{
			if (fort.getFortState() == 2 && fort.getCastleId() == npc.getCastle().getCastleId())
			{
				ok = true;
				break;
			}
		}

		if (!ok)
			return NO_FORT;

		if (st != null)
		{
			int npcId = npc.getNpcId();

			if (st.getState() == State.CREATED)
				st.set(CONDITION, 0);

			int cond = st.getInt(CONDITION);
			if (ArrayUtils.contains(WARDEN, npcId) && cond == 0)
			{
				if (player.getLevel() >= 70)
					htmlText = "warden_quest.htm";
				else
				{
					htmlText = TOO_LOW_LEVEL;
					st.exitQuest(true);
				}
			}
			else if (ArrayUtils.contains(WARDEN, npcId) && st.getState() == State.STARTED && cond > 0)
			{
				long count = st.getQuestItemsCount(DL_MARK);
				if (cond == 1 && count > 0)
				{
					htmlText = "warden_exchange.htm";
					st.takeItems(DL_MARK, count);
					st.giveItems(KNIGHT_EPALUETTE, count * 10);
				}
				else if (cond == 1 && count == 0)
					htmlText = "warden_yes.htm";
			}
		}
		return htmlText;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getNpcId();
		int instanceId = npc.getInstanceId();

		if (ArrayUtils.contains(RAIDS3, npcId))
		{
			QuestState st;
			L2Party party = player.getParty();
			// If is party, give item to all party member who have quest
			if (party != null)
			{
				for (L2PcInstance plr : party.getPartyMembers())
				{
					if (plr != null)
					{
						st = plr.getQuestState(QN);
						if (st != null)
							reward(st);
					}
				}
			}
			else
			{
				st = player.getQuestState(QN);
				if (st != null)
					reward(st);
			}

			exitInstance(npc);
		}
		else if (ArrayUtils.contains(RAIDS1, npcId))
			spawnRaid(instanceId, 2);
		else if (ArrayUtils.contains(RAIDS2, npcId))
			spawnRaid(instanceId, 3);

		return "";
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (ArrayUtils.contains(WARDEN, npc.getNpcId()))
			return "CastleWarden.htm";
		return "";
	}

	public static void main(String[] args)
	{
		new AwlUnderFoot2(512, QN, "Awl Under Foot - Access Castle Dungeon");
	}
}