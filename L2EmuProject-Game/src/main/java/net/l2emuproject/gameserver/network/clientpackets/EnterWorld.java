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
import net.l2emuproject.gameserver.Announcements;
import net.l2emuproject.gameserver.LoginServerThread;
import net.l2emuproject.gameserver.SevenSigns;
import net.l2emuproject.gameserver.cache.HtmCache;
import net.l2emuproject.gameserver.communitybbs.Manager.RegionBBSManager;
import net.l2emuproject.gameserver.communitybbs.Manager.RegionBBSManager.PlayerStateOnCommunity;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.instancemanager.CoupleManager;
import net.l2emuproject.gameserver.instancemanager.CrownManager;
import net.l2emuproject.gameserver.instancemanager.DimensionalRiftManager;
import net.l2emuproject.gameserver.instancemanager.FortManager;
import net.l2emuproject.gameserver.instancemanager.FortSiegeManager;
import net.l2emuproject.gameserver.instancemanager.InstanceManager;
import net.l2emuproject.gameserver.instancemanager.MailManager;
import net.l2emuproject.gameserver.instancemanager.PetitionManager;
import net.l2emuproject.gameserver.instancemanager.QuestManager;
import net.l2emuproject.gameserver.instancemanager.SiegeManager;
import net.l2emuproject.gameserver.instancemanager.TerritoryWarManager;
import net.l2emuproject.gameserver.model.L2Clan;
import net.l2emuproject.gameserver.model.L2ClanMember;
import net.l2emuproject.gameserver.model.L2ItemInstance;
import net.l2emuproject.gameserver.model.L2ShortCut;
import net.l2emuproject.gameserver.model.L2World;
import net.l2emuproject.gameserver.model.actor.instance.L2ClassMasterInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.entity.Couple;
import net.l2emuproject.gameserver.model.entity.Fort;
import net.l2emuproject.gameserver.model.entity.FortSiege;
import net.l2emuproject.gameserver.model.entity.Hero;
import net.l2emuproject.gameserver.model.entity.Instance;
import net.l2emuproject.gameserver.model.entity.Siege;
import net.l2emuproject.gameserver.model.itemcontainer.Inventory;
import net.l2emuproject.gameserver.model.mapregion.TeleportWhereType;
import net.l2emuproject.gameserver.model.olympiad.Olympiad;
import net.l2emuproject.gameserver.model.quest.Quest;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.restriction.ObjectRestrictions;
import net.l2emuproject.gameserver.model.restriction.global.GlobalRestrictions;
import net.l2emuproject.gameserver.model.zone.L2Zone;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.Die;
import net.l2emuproject.gameserver.network.serverpackets.ExBasicActionList;
import net.l2emuproject.gameserver.network.serverpackets.ExGetBookMarkInfoPacket;
import net.l2emuproject.gameserver.network.serverpackets.ExNoticePostArrived;
import net.l2emuproject.gameserver.network.serverpackets.ExNotifyBirthDay;
import net.l2emuproject.gameserver.network.serverpackets.ExStorageMaxCount;
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

public class EnterWorld extends L2GameClientPacket
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
		L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			_log.warn("EnterWorld failed! activeChar is null...");
			getClient().closeNow();
			return;
		}
		
		String[] adress = new String[5];
		for (int i = 0; i < 5; i++)
			adress[i] = _tracert[i][0] + "." + _tracert[i][1] + "." + _tracert[i][2] + "." + _tracert[i][3];
		
		LoginServerThread.getInstance().sendClientTracert(activeChar.getAccountName(), adress);
		
		getClient().setClientTracert(_tracert);

		getClient().initServerPacketQueue();
		
		if (Config.GM_EVERYBODY_HAS_ADMIN_RIGHTS && !(activeChar.isGM()))
			activeChar.setAccessLevel(200);

		// restore instance
		Instance ins = InstanceManager.getInstance().getDynamicInstance(activeChar);
		if (ins != null)
		{
			if (Config.RESTORE_PLAYER_INSTANCE)
				activeChar.setInstanceId(ins.getId());
			else
				ins.removePlayer(activeChar.getObjectId());
		}

		// Restore Vitality
		if (Config.ENABLE_VITALITY && Config.RECOVER_VITALITY_ON_RECONNECT)
		{
			float points = Config.RATE_RECOVERY_ON_RECONNECT * (System.currentTimeMillis() - activeChar.getLastAccess()) / 60000;
			if (points > 0)
				activeChar.getPlayerVitality().updateVitalityPoints(points, false, true);
		}

		if (Config.PLAYER_SPAWN_PROTECTION > 0)
			activeChar.setProtection(true);
		activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());

		activeChar.getKnownList().updateKnownObjects();

		sendPacket(new SSQInfo());
		activeChar.broadcastUserInfo();
		sendPacket(new ItemList(activeChar, false));
		activeChar.getPlayerSettings().getMacroses().sendUpdate();
		sendPacket(new ShortCutInit(activeChar));
		activeChar.sendSkillList();
		sendPacket(SystemMessageId.WELCOME_TO_LINEAGE);
		if (Config.SERVER_AGE_LIM >= 18 || Config.SERVER_PVP)
			sendPacket(SystemMessageId.ENTERED_ADULTS_ONLY_SERVER);
		else if (Config.SERVER_AGE_LIM >= 15)
			sendPacket(SystemMessageId.ENTERED_COMMON_SERVER);
		else
			sendPacket(SystemMessageId.ENTERED_JUVENILES_SERVER);
		sendPacket(new HennaInfo(activeChar));

		Announcements.getInstance().showAnnouncements(activeChar);
		
		// L2EMU_EDIT
		if (Config.ANNOUNCE_7S_AT_START_UP)
			SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);
		// L2EMU_EDIT
		
		Siege quickfix = SiegeManager.getInstance().getSiege(activeChar);
		if (quickfix != null && quickfix.getIsInProgress()
				&& !quickfix.checkIsDefender(activeChar.getClan()))
		{
			if (activeChar.isInsideZone(L2Zone.FLAG_NO_HQ) // no such zones yet, so
					|| activeChar.isInsideZone(L2Zone.FLAG_CASTLE))
				activeChar.teleToLocation(TeleportWhereType.Town);
		}

		// send user info again .. just like the real client
		sendPacket(new UserInfo(activeChar));

		if (activeChar.getClanId() != 0 && activeChar.getClan() != null)
		{
			sendPacket(new PledgeShowMemberListAll(activeChar.getClan()));
			sendPacket(new PledgeStatusChanged(activeChar.getClan()));

			// Residential skills support
			activeChar.enableResidentialSkills(true);
		}

		if (activeChar.getStatus().getCurrentHp() < 0.5) // is dead
			activeChar.setIsDead(true);
		if (activeChar.isAlikeDead()) // dead or fake dead
			// no broadcast needed since the player will already spawn dead to others
			sendPacket(new Die(activeChar));

		// engage and notify Partner
		if (Config.ALLOW_WEDDING)
		{
			engage(activeChar);
			notifyPartner(activeChar);

			// Check if player is married and remove if necessary Cupid's Bow
			if (!activeChar.isMaried())
			{
				L2ItemInstance item = activeChar.getInventory().getItemByItemId(9140);
				// Remove Cupid's Bow
				if (item != null)
				{
					activeChar.destroyItem("Removing Cupid's Bow", item, activeChar, true);

					// No need to update every item in the inventory
					//activeChar.getInventory().updateDatabase();

					// Log it
					if (_log.isDebugEnabled())
						_log.debug("Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " got Cupid's Bow removed.");
				}
			}
		}

		L2ItemInstance weapon = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
		if (weapon == null)
			weapon = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (weapon != null)
		{
			if ((weapon.isHeroItem() && !activeChar.isHero() && !activeChar.isGM())
					|| (activeChar.getPkKills() > 0 && weapon.getItemId() > 7815 && weapon.getItemId() < 7832))
				activeChar.getInventory().unEquipItemInBodySlotAndRecord(weapon.getItem().getBodyPart());
		}

		activeChar.updateEffectIcons();
		activeChar.sendSkillCoolTime();

		Quest.playerEnter(activeChar);
		loadTutorial(activeChar);
		for (Quest quest : QuestManager.getInstance().getAllManagedScripts())
		{
			if (quest != null && quest.getOnEnterWorld())
				quest.notifyEnterWorld(activeChar);
		}

		activeChar.notifyFriends();
		notifyClanMembers(activeChar);
		notifySponsorOrApprentice(activeChar);

		L2Clan clan = activeChar.getClan();
		if (clan != null)
			sendPacket(new PledgeSkillList(clan));

		sendPacket(new ExStorageMaxCount(activeChar));
		sendPacket(new QuestList(activeChar));

		activeChar.broadcastUserInfo();
		
		for (L2ItemInstance i : activeChar.getWarehouse().getItems())
			if (i.isTimeLimitedItem())
				i.scheduleLifeTimeTask();

		if (Olympiad.getInstance().playerInStadia(activeChar))
		{
			activeChar.doRevive();
			activeChar.teleToLocation(TeleportWhereType.Town);
			activeChar.sendMessage("You have been teleported to the nearest town due to you being in an Olympiad Stadium.");
		}

		activeChar.revalidateZone(true);
		activeChar.sendEtcStatusUpdate();

		if (DimensionalRiftManager.getInstance().checkIfInRiftZone(activeChar.getX(), activeChar.getY(), activeChar.getZ(), true)) // Exclude waiting room
			DimensionalRiftManager.getInstance().teleportToWaitingRoom(activeChar);

		// Wherever these should be?
		sendPacket(new ShortCutInit(activeChar));

		if (Hero.getInstance().getHeroes() != null && Hero.getInstance().getHeroes().containsKey(activeChar.getObjectId()))
			activeChar.setHero(true);

		// Restore character's siege state
		if (activeChar.getClan() != null)
		{
			for (Siege siege : SiegeManager.getInstance().getSieges())
			{
				if (!siege.getIsInProgress())
					continue;
				if (siege.checkIsAttacker(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 1);
					activeChar.setSiegeSide(siege.getCastle().getCastleId());
				}
				else if (siege.checkIsDefender(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 2);
					activeChar.setSiegeSide(siege.getCastle().getCastleId());
				}
			}

			for (FortSiege fsiege : FortSiegeManager.getInstance().getSieges())
			{
				if (!fsiege.getIsInProgress())
					continue;
				if (fsiege.checkIsAttacker(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 1);
					activeChar.setSiegeSide(fsiege.getFort().getFortId());
					
				}
				else if (fsiege.checkIsDefender(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 2);
					activeChar.setSiegeSide(fsiege.getFort().getFortId());
				}
			}
		}
		
		if (TerritoryWarManager.getInstance().getRegisteredTerritoryId(activeChar) > 0)
		{
			if (TerritoryWarManager.getInstance().isTWInProgress())
				activeChar.setSiegeState((byte) 1);
			
			activeChar.setSiegeSide(TerritoryWarManager.getInstance().getRegisteredTerritoryId(activeChar));
		}

		//Updating Seal of Strife Buff/Debuff
		if (SevenSigns.getInstance().isSealValidationPeriod())
		{
			int owner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE);
			if (owner != SevenSigns.CABAL_NULL)
			{
				int cabal = SevenSigns.getInstance().getPlayerCabal(activeChar);
				if (cabal == owner)
					activeChar.addSkill(SkillTable.getInstance().getInfo(5074, 1), false);
				else if (cabal != SevenSigns.CABAL_NULL)
					activeChar.addSkill(SkillTable.getInstance().getInfo(5075, 1), false);
			}
		}

		for (L2ItemInstance i : activeChar.getInventory().getItems())
			if (i.isTimeLimitedItem())
				i.scheduleLifeTimeTask();

		activeChar.queryGameGuard();

		sendPacket(new FriendList(activeChar));
		
		// Send "Friend has logged in" message to all player friends
		SystemMessage sm0 = new SystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN).addString(activeChar.getName());
		for (int objectId : activeChar.getFriendList().getFriendIds())
		{
			L2PcInstance player = L2World.getInstance().findPlayer(objectId);
			if (player != null)
				player.sendPacket(sm0);
		}

		if (Config.SHOW_LICENSE)
		{
			activeChar.sendMessage("Welcome to L2Emu Project.");
			activeChar.sendMessage("L2Server Version: " + VersionService.getGameRevision());
			activeChar.sendMessage("L2DataPack Version: " + Config.DATAPACK_REVISION);
			activeChar.sendMessage("Enjoy our project: L2EmuProject " + VersionService.getGameVersion() + "!");
		}

		if (Config.SHOW_HTML_NEWBIE && activeChar.getLevel() < Config.LEVEL_HTML_NEWBIE)
		{
			String Newbie_Path = "data/html/newbie.htm";
			if (HtmCache.getInstance().pathExists(Newbie_Path))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(Newbie_Path);
				html.replace("%name%", activeChar.getName()); // replaces %name%, so you can say like "welcome to the server %name%"
				sendPacket(html);
			}
		}
		else if (Config.SHOW_HTML_GM && activeChar.isGM())
		{
			String Gm_Path = "data/html/gm.htm";
			if (HtmCache.getInstance().pathExists(Gm_Path))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(Gm_Path);
				html.replace("%name%", activeChar.getName()); // replaces %name%, so you can say like "welcome to the server %name%"
				sendPacket(html);
			}
		}
		else if (Config.SHOW_HTML_WELCOME)
		{
			String Welcome_Path = "data/html/welcome.htm";
			if (HtmCache.getInstance().pathExists(Welcome_Path))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(Welcome_Path);
				html.replace("%name%", activeChar.getName()); // replaces %name%, so you can say like "welcome to the server %name%"
				sendPacket(html);
			}
		}

		// Resume paused restrictions
		ObjectRestrictions.getInstance().resumeTasks(activeChar.getObjectId());

		// check player skills
		activeChar.checkAllowedSkills();

		// check for academy
		activeChar.academyCheck(activeChar.getClassId().getId());

		// check for crowns
		CrownManager.checkCrowns(activeChar);

		if (Config.ONLINE_PLAYERS_AT_STARTUP)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1);
			if (L2World.getInstance().getAllPlayers().size() == 1)
				sm.addString("Player online: " + L2World.getInstance().getAllPlayers().size());
			else
				sm.addString("Players online: " + L2World.getInstance().getAllPlayers().size());
			sendPacket(sm);
		}

		PetitionManager.getInstance().checkPetitionMessages(activeChar);

		activeChar.onPlayerEnter();

		if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
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
		activeChar.setPledgeClass(L2ClanMember.getCurrentPledgeClass(activeChar));

		L2ShortCut[] allShortCuts = activeChar.getPlayerSettings().getAllShortCuts();
		for (L2ShortCut sc : allShortCuts)
			sendPacket(new ShortCutRegister(sc));

		// remove combat flag before teleporting
		L2ItemInstance flag = activeChar.getInventory().getItemByItemId(9819);
		if (flag != null)
		{
			Fort fort = FortManager.getInstance().getFort(activeChar);
			if (fort != null)
			{
				FortSiegeManager.getInstance().dropCombatFlag(activeChar);
			}
			else
			{
				int slot = flag.getItem().getBodyPart();
				activeChar.getInventory().unEquipItemInBodySlotAndRecord(slot);
				activeChar.destroyItem("CombatFlag", flag, null, true);
			}
		}
		if (!activeChar.isGM()
		// inside siege zone
				&& activeChar.isInsideZone(L2Zone.FLAG_SIEGE)
				// but non-participant or attacker
				&& (!activeChar.isInSiege() || activeChar.getSiegeState() < 2))
		{
			// Attacker or spectator logging in to a siege zone. Actually should be checked for inside castle only?
			activeChar.teleToLocation(TeleportWhereType.Town);
			//activeChar.sendMessage("You have been teleported to the nearest town due to you being in siege zone"); - custom
		}

		if (MailManager.getInstance().hasUnreadPost(activeChar))
			sendPacket(new ExNoticePostArrived(false));
		
		RegionBBSManager.changeCommunityBoard(activeChar, PlayerStateOnCommunity.NONE);

		if (!activeChar.getPlayerTransformation().isTransformed())
			activeChar.regiveTemporarySkills();

		// Send Teleport Bookmark List
		sendPacket(new ExGetBookMarkInfoPacket(activeChar));

		ExBasicActionList.sendTo(activeChar);

		int daysLeft = activeChar.getPlayerBirthday().canReceiveAnnualPresent();
		if (daysLeft < 8 && daysLeft != -1)
		{
			if (daysLeft == 0)			
				sendPacket(ExNotifyBirthDay.PACKET);				
			else
				sendPacket(new SystemMessage(SystemMessageId.THERE_ARE_S1_DAYS_UNTIL_YOUR_CHARACTERS_BIRTHDAY).addNumber(daysLeft));
		}

		L2ClassMasterInstance.showQuestionMark(activeChar);

		if (activeChar.getLevel() == 28)
			sendPacket(new TutorialShowQuestionMark(1002));
		
		//if (!activeChar.getPremiumItemList().isEmpty())
			//activeChar.sendPacket(ExNotifyPremiumItem.PACKET);

		GlobalRestrictions.playerLoggedIn(activeChar);
	}

	/**
	 * @param activeChar
	 */
	private void engage(L2PcInstance cha)
	{
		int _chaid = cha.getObjectId();

		for (Couple cl : CoupleManager.getInstance().getCouples())
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
	private void notifyPartner(L2PcInstance cha)
	{
		if (cha.getPartnerId() != 0)
		{
			L2PcInstance partner = L2World.getInstance().getPlayer(cha.getPartnerId());
			if (partner != null)
				partner.sendMessage("Your Partner " + cha.getName() + " has logged in.");
		}
	}

	/**
	 * @param activeChar
	 */
	private void notifyClanMembers(L2PcInstance activeChar)
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
					notice.setFile("data/html/clanNotice.htm");
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
	private void notifySponsorOrApprentice(L2PcInstance activeChar)
	{
		if (activeChar.getSponsor() != 0)
		{
			L2PcInstance sponsor = L2World.getInstance().getPlayer(activeChar.getSponsor());

			if (sponsor != null)
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				sponsor.sendPacket(msg);
			}
		}
		else if (activeChar.getApprentice() != 0)
		{
			L2PcInstance apprentice = L2World.getInstance().getPlayer(activeChar.getApprentice());

			if (apprentice != null)
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_SPONSOR_C1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				apprentice.sendPacket(msg);
			}
		}
	}

	private void loadTutorial(L2PcInstance player)
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