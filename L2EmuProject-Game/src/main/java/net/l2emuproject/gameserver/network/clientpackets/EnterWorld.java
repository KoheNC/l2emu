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
package net.l2emuproject.gameserver.network.clientpackets;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.LoginServerThread;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.entity.itemcontainer.Inventory;
import net.l2emuproject.gameserver.events.global.dimensionalrift.DimensionalRiftManager;
import net.l2emuproject.gameserver.events.global.fortsiege.Fort;
import net.l2emuproject.gameserver.events.global.fortsiege.FortManager;
import net.l2emuproject.gameserver.events.global.fortsiege.FortSiege;
import net.l2emuproject.gameserver.events.global.fortsiege.FortSiegeManager;
import net.l2emuproject.gameserver.events.global.olympiad.Hero;
import net.l2emuproject.gameserver.events.global.olympiad.Olympiad;
import net.l2emuproject.gameserver.events.global.sevensigns.SevenSigns;
import net.l2emuproject.gameserver.events.global.siege.Siege;
import net.l2emuproject.gameserver.events.global.siege.SiegeManager;
import net.l2emuproject.gameserver.events.global.territorywar.TerritoryWarManager;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.manager.CrownManager;
import net.l2emuproject.gameserver.manager.instances.Instance;
import net.l2emuproject.gameserver.manager.instances.InstanceManager;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.Die;
import net.l2emuproject.gameserver.network.serverpackets.ExBasicActionList;
import net.l2emuproject.gameserver.network.serverpackets.ExGetBookMarkInfoPacket;
import net.l2emuproject.gameserver.network.serverpackets.ExNavitAdventPointInfoPacket;
import net.l2emuproject.gameserver.network.serverpackets.ExNavitAdventTimeChange;
import net.l2emuproject.gameserver.network.serverpackets.ExNoticePostArrived;
import net.l2emuproject.gameserver.network.serverpackets.ExNotifyBirthDay;
import net.l2emuproject.gameserver.network.serverpackets.ExStorageMaxCount;
import net.l2emuproject.gameserver.network.serverpackets.ExVoteSystemInfo;
import net.l2emuproject.gameserver.network.serverpackets.FriendList;
import net.l2emuproject.gameserver.network.serverpackets.HennaInfo;
import net.l2emuproject.gameserver.network.serverpackets.ItemList;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.network.serverpackets.PledgeShowMemberListAll;
import net.l2emuproject.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.l2emuproject.gameserver.network.serverpackets.PledgeSkillList;
import net.l2emuproject.gameserver.network.serverpackets.PledgeStatusChanged;
import net.l2emuproject.gameserver.network.serverpackets.QuestList;
import net.l2emuproject.gameserver.network.serverpackets.SSQInfo;
import net.l2emuproject.gameserver.network.serverpackets.ShortCutInit;
import net.l2emuproject.gameserver.network.serverpackets.ShortCutRegister;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.network.serverpackets.TutorialShowQuestionMark;
import net.l2emuproject.gameserver.network.serverpackets.UserInfo;
import net.l2emuproject.gameserver.services.VersionService;
import net.l2emuproject.gameserver.services.clan.L2Clan;
import net.l2emuproject.gameserver.services.clan.L2ClanMember;
import net.l2emuproject.gameserver.services.couple.Couple;
import net.l2emuproject.gameserver.services.couple.CoupleService;
import net.l2emuproject.gameserver.services.mail.MailService;
import net.l2emuproject.gameserver.services.petition.PetitionService;
import net.l2emuproject.gameserver.services.quest.Quest;
import net.l2emuproject.gameserver.services.quest.QuestService;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.shortcuts.L2ShortCut;
import net.l2emuproject.gameserver.system.announcements.Announcements;
import net.l2emuproject.gameserver.system.cache.HtmCache;
import net.l2emuproject.gameserver.system.restriction.ObjectRestrictions;
import net.l2emuproject.gameserver.system.restriction.global.GlobalRestrictions;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.mapregion.TeleportWhereType;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2ClassMasterInstance;
import net.l2emuproject.gameserver.world.zone.L2Zone;

public final class EnterWorld extends L2GameClientPacket
{
	private static final String	_C__ENTERWORLD	= "[C] 11 EnterWorld c[bddddbdcccccccccccccccccccc] (unk)";

	private int[][] _tracert = new int[5][4];
	
	@Override
	protected void readImpl()
	{
		readB(new byte[32]);	// Unknown Byte Array
		readD();				// Unknown Value
		readD();				// Unknown Value
		readD();				// Unknown Value
		readD();				// Unknown Value
		readB(new byte[32]);	// Unknown Byte Array
		readD();				// Unknown Value
		for (int i = 0; i < 5; i++)
			for (int o = 0; o < 4; o++)
				_tracert[i][o] = readC();
	}

	@Override
	protected void runImpl()
	{
		final L2Player player = getActiveChar();
		if (player == null)
		{
			_log.warn("EnterWorld failed! activeChar is null...");
			getClient().closeNow();
			return;
		}
		
		String[] adress = new String[5];
		for (int i = 0; i < 5; i++)
			adress[i] = _tracert[i][0] + "." + _tracert[i][1] + "." + _tracert[i][2] + "." + _tracert[i][3];
		
		LoginServerThread.getInstance().sendClientTracert(player.getAccountName(), adress);
		
		getClient().setClientTracert(_tracert);

		getClient().initServerPacketQueue();
		
		if (Config.GM_EVERYBODY_HAS_ADMIN_RIGHTS && !(player.isGM()))
			player.setAccessLevel(200);

		// restore instance
		Instance ins = InstanceManager.getInstance().getDynamicInstance(player);
		if (ins != null)
		{
			if (Config.RESTORE_PLAYER_INSTANCE)
				player.setInstanceId(ins.getId());
			else
				ins.removePlayer(player.getObjectId());
		}

		// Restore Vitality
		if (Config.ENABLE_VITALITY && Config.RECOVER_VITALITY_ON_RECONNECT)
		{
			float points = Config.RATE_RECOVERY_ON_RECONNECT * (System.currentTimeMillis() - player.getLastAccess()) / 60000;
			if (points > 0)
				player.getPlayerVitality().updateVitalityPoints(points, false, true);
		}

		if (Config.PLAYER_SPAWN_PROTECTION > 0)
			player.setProtection(true);
		player.spawnMe(player.getX(), player.getY(), player.getZ());

		player.getKnownList().updateKnownObjects();

		sendPacket(new SSQInfo());
		player.broadcastUserInfo();
		sendPacket(new ItemList(player, false));
		player.getPlayerSettings().getMacroses().sendUpdate();
		sendPacket(new ShortCutInit(player));
		player.sendSkillList();
		sendPacket(SystemMessageId.WELCOME_TO_LINEAGE);
		if (Config.SERVER_AGE_LIM >= 18 || Config.SERVER_PVP)
			sendPacket(SystemMessageId.ENTERED_ADULTS_ONLY_SERVER);
		else if (Config.SERVER_AGE_LIM >= 15)
			sendPacket(SystemMessageId.ENTERED_COMMON_SERVER);
		else
			sendPacket(SystemMessageId.ENTERED_JUVENILES_SERVER);
		sendPacket(new HennaInfo(player));

		Announcements.getInstance().showAnnouncements(player);
		
		// L2EMU_EDIT
		if (Config.ANNOUNCE_7S_AT_START_UP)
			SevenSigns.getInstance().sendCurrentPeriodMsg(player);
		// L2EMU_EDIT
		
		Siege quickfix = SiegeManager.getInstance().getSiege(player);
		if (quickfix != null && quickfix.getIsInProgress()
				&& !quickfix.checkIsDefender(player.getClan()))
		{
			if (player.isInsideZone(L2Zone.FLAG_NO_HQ) // no such zones yet, so
					|| player.isInsideZone(L2Zone.FLAG_CASTLE))
				player.teleToLocation(TeleportWhereType.Town);
		}

		// send user info again .. just like the real client
		sendPacket(new UserInfo(player));

		if (player.getClanId() != 0 && player.getClan() != null)
		{
			sendPacket(new PledgeShowMemberListAll(player.getClan()));
			sendPacket(new PledgeStatusChanged(player.getClan()));

			// Residential skills support
			player.enableResidentialSkills(true);
		}

		if (player.getStatus().getCurrentHp() < 0.5) // is dead
			player.setIsDead(true);
		if (player.isAlikeDead()) // dead or fake dead
			// no broadcast needed since the player will already spawn dead to others
			sendPacket(new Die(player));

		// engage and notify Partner
		if (Config.ALLOW_WEDDING)
		{
			engage(player);
			notifyPartner(player);

			// Check if player is married and remove if necessary Cupid's Bow
			if (!player.isMaried())
			{
				L2ItemInstance item = player.getInventory().getItemByItemId(9140);
				// Remove Cupid's Bow
				if (item != null)
				{
					player.destroyItem("Removing Cupid's Bow", item, player, true);

					// No need to update every item in the inventory
					//activeChar.getInventory().updateDatabase();

					// Log it
					if (_log.isDebugEnabled())
						_log.debug("Character " + player.getName() + " of account " + player.getAccountName() + " got Cupid's Bow removed.");
				}
			}
		}

		L2ItemInstance weapon = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
		if (weapon == null)
			weapon = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (weapon != null)
		{
			if ((weapon.isHeroItem() && !player.isHero() && !player.isGM())
					|| (player.getPkKills() > 0 && weapon.getItemId() > 7815 && weapon.getItemId() < 7832))
				player.getInventory().unEquipItemInBodySlotAndRecord(weapon.getItem().getBodyPart());
		}

		player.updateEffectIcons();
		player.sendSkillCoolTime();

		Quest.playerEnter(player);
		loadTutorial(player);
		for (Quest quest : QuestService.getInstance().getAllManagedScripts())
		{
			if (quest != null && quest.getOnEnterWorld())
				quest.notifyEnterWorld(player);
		}

		player.notifyFriends();
		notifyClanMembers(player);
		notifySponsorOrApprentice(player);

		final L2Clan clan = player.getClan();
		if (clan != null)
			sendPacket(new PledgeSkillList(clan));

		sendPacket(new ExStorageMaxCount(player));
		sendPacket(new QuestList(player));

		player.broadcastUserInfo();
		
		for (L2ItemInstance i : player.getWarehouse().getItems())
			if (i.isTimeLimitedItem())
				i.scheduleLifeTimeTask();

		if (Olympiad.getInstance().playerInStadia(player))
		{
			player.doRevive();
			player.teleToLocation(TeleportWhereType.Town);
			player.sendMessage("You have been teleported to the nearest town due to you being in an Olympiad Stadium.");
		}

		player.revalidateZone(true);
		player.sendEtcStatusUpdate();
		
		sendPacket(new ExVoteSystemInfo(player)); // TODO: Rework...
		
		sendPacket(new ExNavitAdventPointInfoPacket(0));
		sendPacket(new ExNavitAdventTimeChange(-1)); // only set pause state...

		if (DimensionalRiftManager.getInstance().checkIfInRiftZone(player.getX(), player.getY(), player.getZ(), true)) // Exclude waiting room
			DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);

		// Wherever these should be?
		sendPacket(new ShortCutInit(player));

		if (Hero.getInstance().getHeroes() != null && Hero.getInstance().getHeroes().containsKey(player.getObjectId()))
			player.setHero(true);

		// Restore character's siege state
		if (player.getClan() != null)
		{
			for (Siege siege : SiegeManager.getInstance().getSieges())
			{
				if (!siege.getIsInProgress())
					continue;
				if (siege.checkIsAttacker(player.getClan()))
				{
					player.setSiegeState((byte) 1);
					player.setSiegeSide(siege.getCastle().getCastleId());
				}
				else if (siege.checkIsDefender(player.getClan()))
				{
					player.setSiegeState((byte) 2);
					player.setSiegeSide(siege.getCastle().getCastleId());
				}
			}

			for (FortSiege fsiege : FortSiegeManager.getInstance().getSieges())
			{
				if (!fsiege.getIsInProgress())
					continue;
				if (fsiege.checkIsAttacker(player.getClan()))
				{
					player.setSiegeState((byte) 1);
					player.setSiegeSide(fsiege.getFort().getFortId());
					
				}
				else if (fsiege.checkIsDefender(player.getClan()))
				{
					player.setSiegeState((byte) 2);
					player.setSiegeSide(fsiege.getFort().getFortId());
				}
			}
		}
		
		if (TerritoryWarManager.getInstance().getRegisteredTerritoryId(player) > 0)
		{
			if (TerritoryWarManager.getInstance().isTWInProgress())
				player.setSiegeState((byte) 1);
			
			player.setSiegeSide(TerritoryWarManager.getInstance().getRegisteredTerritoryId(player));
		}

		//Updating Seal of Strife Buff/Debuff
		if (SevenSigns.getInstance().isSealValidationPeriod())
		{
			int owner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE);
			if (owner != SevenSigns.CABAL_NULL)
			{
				int cabal = SevenSigns.getInstance().getPlayerCabal(player);
				if (cabal == owner)
					player.addSkill(SkillTable.getInstance().getInfo(5074, 1), false);
				else if (cabal != SevenSigns.CABAL_NULL)
					player.addSkill(SkillTable.getInstance().getInfo(5075, 1), false);
			}
		}

		for (L2ItemInstance i : player.getInventory().getItems())
			if (i.isTimeLimitedItem())
				i.scheduleLifeTimeTask();

		player.queryGameGuard();

		sendPacket(new FriendList(player));
		
		// Send "Friend has logged in" message to all player friends
		SystemMessage sm0 = new SystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN).addString(player.getName());
		for (int objectId : player.getFriendList().getFriendIds())
		{
			final L2Player friend = L2World.getInstance().findPlayer(objectId);
			if (friend != null)
				friend.sendPacket(sm0);
		}

		if (Config.SHOW_LICENSE)
		{
			player.sendMessage("Welcome to L2Emu Project.");
			player.sendMessage("L2Server Version: " + VersionService.getGameRevision());
			player.sendMessage("L2DataPack Version: " + Config.DATAPACK_REVISION);
			player.sendMessage("Enjoy our project: L2EmuProject " + VersionService.getGameVersion() + "!");
		}

		if (Config.SHOW_HTML_NEWBIE && player.getLevel() < Config.LEVEL_HTML_NEWBIE)
		{
			String Newbie_Path = "data/npc_data/html/newbie.htm";
			if (HtmCache.getInstance().pathExists(Newbie_Path))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(Newbie_Path);
				html.replace("%name%", player.getName()); // replaces %name%, so you can say like "welcome to the server %name%"
				sendPacket(html);
			}
		}
		else if (Config.SHOW_HTML_GM && player.isGM())
		{
			String Gm_Path = "data/npc_data/html/gm.htm";
			if (HtmCache.getInstance().pathExists(Gm_Path))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(Gm_Path);
				html.replace("%name%", player.getName()); // replaces %name%, so you can say like "welcome to the server %name%"
				sendPacket(html);
			}
		}
		else if (Config.SHOW_HTML_WELCOME)
		{
			String Welcome_Path = "data/npc_data/html/welcome.htm";
			if (HtmCache.getInstance().pathExists(Welcome_Path))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(Welcome_Path);
				html.replace("%name%", player.getName()); // replaces %name%, so you can say like "welcome to the server %name%"
				sendPacket(html);
			}
		}

		// Resume paused restrictions
		ObjectRestrictions.getInstance().resumeTasks(player.getObjectId());

		// check player skills
		player.checkAllowedSkills();

		// check for academy
		player.academyCheck(player.getClassId().getId());

		// check for crowns
		CrownManager.checkCrowns(player);

		if (Config.ONLINE_PLAYERS_AT_STARTUP)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1);
			if (L2World.getInstance().getAllPlayers().size() == 1)
				sm.addString("Player online: " + L2World.getInstance().getAllPlayers().size());
			else
				sm.addString("Players online: " + L2World.getInstance().getAllPlayers().size());
			sendPacket(sm);
		}

		PetitionService.getInstance().checkPetitionMessages(player);

		player.onPlayerEnter();

		if (player.getClanJoinExpiryTime() > System.currentTimeMillis())
			sendPacket(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED);
/*
		if (activeChar.getClan() != null)
		{
			// Add message if clanHall not paid. Possibly this is custom...
			ClanHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan());
			if (clanHall != null && !clanHall.getPaid())
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.MAKE_CLAN_HALL_PAYMENT_BY_S1_TOMORROW);
				sm.addNumber(???);
				sendPacket(sm);
			}
		}
*/
		//Sets the appropriate Pledge Class for the clannie (e.g. Viscount, Count, Baron, Marquiz)
		player.setPledgeClass(L2ClanMember.getCurrentPledgeClass(player));

		L2ShortCut[] allShortCuts = player.getPlayerSettings().getAllShortCuts();
		for (L2ShortCut sc : allShortCuts)
			sendPacket(new ShortCutRegister(sc));

		// remove combat flag before teleporting
		L2ItemInstance flag = player.getInventory().getItemByItemId(9819);
		if (flag != null)
		{
			Fort fort = FortManager.getInstance().getFort(player);
			if (fort != null)
			{
				FortSiegeManager.getInstance().dropCombatFlag(player);
			}
			else
			{
				int slot = flag.getItem().getBodyPart();
				player.getInventory().unEquipItemInBodySlotAndRecord(slot);
				player.destroyItem("CombatFlag", flag, null, true);
			}
		}
		if (!player.isGM()
		// inside siege zone
				&& player.isInsideZone(L2Zone.FLAG_SIEGE)
				// but non-participant or attacker
				&& (!player.isInSiege() || player.getSiegeState() < 2))
		{
			// Attacker or spectator logging in to a siege zone. Actually should be checked for inside castle only?
			player.teleToLocation(TeleportWhereType.Town);
			//activeChar.sendMessage("You have been teleported to the nearest town due to you being in siege zone"); - custom
		}

		if (MailService.getInstance().hasUnreadPost(player))
			sendPacket(new ExNoticePostArrived(false));
		
		if (!player.getPlayerTransformation().isTransformed())
			player.regiveTemporarySkills();

		// Send Teleport Bookmark List
		sendPacket(new ExGetBookMarkInfoPacket());

		ExBasicActionList.sendTo(player);

		int daysLeft = player.getPlayerBirthday().canReceiveAnnualPresent();
		if (daysLeft < 8 && daysLeft != -1)
		{
			if (daysLeft == 0)			
				sendPacket(ExNotifyBirthDay.PACKET);				
			else
				sendPacket(new SystemMessage(SystemMessageId.THERE_ARE_S1_DAYS_UNTIL_YOUR_CHARACTERS_BIRTHDAY).addNumber(daysLeft));
		}

		L2ClassMasterInstance.showQuestionMark(player);

		if (player.getLevel() == 28)
			sendPacket(new TutorialShowQuestionMark(1002));

		GlobalRestrictions.playerLoggedIn(player);
	}

	/**
	 * @param activeChar
	 */
	private void engage(L2Player cha)
	{
		int _chaid = cha.getObjectId();

		for (Couple cl : CoupleService.getInstance().getCouples())
		{
			if (cl.getPlayer1Id() == _chaid || cl.getPlayer2Id() == _chaid)
			{
				if (cl.getMaried())
					cha.setMaried(true);

				cha.setCoupleId(cl.getId());

				if (cl.getPlayer1Id() == _chaid)
					cha.setPartnerId(cl.getPlayer2Id());
				else
					cha.setPartnerId(cl.getPlayer1Id());
			}
		}
	}

	/**
	 * @param activeChar partnerid
	 */
	private void notifyPartner(L2Player cha)
	{
		if (cha.getPartnerId() != 0)
		{
			L2Player partner = L2World.getInstance().getPlayer(cha.getPartnerId());
			if (partner != null)
				partner.sendMessage("Your Partner " + cha.getName() + " has logged in.");
		}
	}

	/**
	 * @param activeChar
	 */
	private void notifyClanMembers(L2Player activeChar)
	{
		L2Clan clan = activeChar.getClan();
		if (clan != null)
		{
			L2ClanMember clanmember = clan.getClanMember(activeChar.getObjectId());
			if (clanmember != null)
			{
				clanmember.setPlayerInstance(activeChar);
				SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN);
				msg.addString(activeChar.getName());
				clan.broadcastToOtherOnlineMembers(msg, activeChar);
				clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(activeChar), activeChar);
				if (clan.isNoticeEnabled() && !clan.getNotice().isEmpty())
				{
					NpcHtmlMessage notice = new NpcHtmlMessage(clan.getClanId());
					notice.setFile("data/npc_data/html/clanNotice.htm");
					notice.replace("%clan_name%", clan.getName());
					// perhaps <br> is in retail?
					notice.replace("%notice_text%", clan.getNotice().replaceAll("\r\n", "<br1>"));
					activeChar.sendPacket(notice);
				}
			}
		}
	}

	/**
	 * @param activeChar
	 */
	private void notifySponsorOrApprentice(L2Player activeChar)
	{
		if (activeChar.getSponsor() != 0)
		{
			L2Player sponsor = L2World.getInstance().getPlayer(activeChar.getSponsor());

			if (sponsor != null)
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				sponsor.sendPacket(msg);
			}
		}
		else if (activeChar.getApprentice() != 0)
		{
			L2Player apprentice = L2World.getInstance().getPlayer(activeChar.getApprentice());

			if (apprentice != null)
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_SPONSOR_C1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				apprentice.sendPacket(msg);
			}
		}
	}

	private void loadTutorial(L2Player player)
	{
		QuestState qs = player.getQuestState("_255_Tutorial");
		if (qs != null)
			qs.getQuest().notifyEvent("UC", null, player);
	}

	@Override
	public String getType()
	{
		return _C__ENTERWORLD;
	}
}
