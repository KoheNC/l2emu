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
package net.l2emuproject.gameserver.world.object;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javolution.util.FastMap;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.LoginServerThread;
import net.l2emuproject.gameserver.Shutdown;
import net.l2emuproject.gameserver.Shutdown.DisableType;
import net.l2emuproject.gameserver.datatables.CharNameTable;
import net.l2emuproject.gameserver.datatables.CharNameTable.ICharacterInfo;
import net.l2emuproject.gameserver.datatables.CharTemplateTable;
import net.l2emuproject.gameserver.datatables.ClanTable;
import net.l2emuproject.gameserver.datatables.GmListTable;
import net.l2emuproject.gameserver.datatables.HeroSkillTable;
import net.l2emuproject.gameserver.datatables.ItemTable;
import net.l2emuproject.gameserver.datatables.NobleSkillTable;
import net.l2emuproject.gameserver.datatables.NpcTable;
import net.l2emuproject.gameserver.datatables.PetDataTable;
import net.l2emuproject.gameserver.datatables.RecordTable;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.datatables.SkillTreeTable;
import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.entity.ai.L2CharacterAI;
import net.l2emuproject.gameserver.entity.ai.L2PlayerAI;
import net.l2emuproject.gameserver.entity.ai.L2SummonAI;
import net.l2emuproject.gameserver.entity.appearance.PcAppearance;
import net.l2emuproject.gameserver.entity.base.ClassId;
import net.l2emuproject.gameserver.entity.base.ClassLevel;
import net.l2emuproject.gameserver.entity.base.Experience;
import net.l2emuproject.gameserver.entity.base.Race;
import net.l2emuproject.gameserver.entity.base.SubClass;
import net.l2emuproject.gameserver.entity.effects.PcEffects;
import net.l2emuproject.gameserver.entity.itemcontainer.Inventory;
import net.l2emuproject.gameserver.entity.itemcontainer.ItemContainer;
import net.l2emuproject.gameserver.entity.itemcontainer.PcInventory;
import net.l2emuproject.gameserver.entity.itemcontainer.PcRefund;
import net.l2emuproject.gameserver.entity.itemcontainer.PcWarehouse;
import net.l2emuproject.gameserver.entity.itemcontainer.PetInventory;
import net.l2emuproject.gameserver.entity.player.PlayerBirthday;
import net.l2emuproject.gameserver.entity.player.PlayerCertification;
import net.l2emuproject.gameserver.entity.player.PlayerCustom;
import net.l2emuproject.gameserver.entity.player.PlayerDuel;
import net.l2emuproject.gameserver.entity.player.PlayerEventData;
import net.l2emuproject.gameserver.entity.player.PlayerFish;
import net.l2emuproject.gameserver.entity.player.PlayerHenna;
import net.l2emuproject.gameserver.entity.player.PlayerObserver;
import net.l2emuproject.gameserver.entity.player.PlayerOlympiad;
import net.l2emuproject.gameserver.entity.player.PlayerRecipe;
import net.l2emuproject.gameserver.entity.player.PlayerSettings;
import net.l2emuproject.gameserver.entity.player.PlayerTeleportBookmark;
import net.l2emuproject.gameserver.entity.player.PlayerTransformation;
import net.l2emuproject.gameserver.entity.player.PlayerVitality;
import net.l2emuproject.gameserver.entity.reference.ClearableReference;
import net.l2emuproject.gameserver.entity.reference.ImmutableReference;
import net.l2emuproject.gameserver.entity.shot.CharShots;
import net.l2emuproject.gameserver.entity.shot.PcShots;
import net.l2emuproject.gameserver.entity.skills.PcSkills;
import net.l2emuproject.gameserver.entity.stat.CharStat;
import net.l2emuproject.gameserver.entity.stat.PcStat;
import net.l2emuproject.gameserver.entity.status.CharStatus;
import net.l2emuproject.gameserver.entity.status.PcStatus;
import net.l2emuproject.gameserver.entity.view.CharLikeView;
import net.l2emuproject.gameserver.entity.view.PcView;
import net.l2emuproject.gameserver.events.global.blockchecker.HandysBlockCheckerManager;
import net.l2emuproject.gameserver.events.global.dimensionalrift.DimensionalRiftManager;
import net.l2emuproject.gameserver.events.global.fortsiege.Fort;
import net.l2emuproject.gameserver.events.global.fortsiege.FortManager;
import net.l2emuproject.gameserver.events.global.fortsiege.FortSiegeManager;
import net.l2emuproject.gameserver.events.global.olympiad.Olympiad;
import net.l2emuproject.gameserver.events.global.sevensigns.SevenSigns;
import net.l2emuproject.gameserver.events.global.sevensigns.SevenSignsFestival;
import net.l2emuproject.gameserver.events.global.siege.Castle;
import net.l2emuproject.gameserver.events.global.siege.CastleManager;
import net.l2emuproject.gameserver.events.global.siege.L2SiegeClan;
import net.l2emuproject.gameserver.events.global.siege.Siege;
import net.l2emuproject.gameserver.events.global.siege.SiegeManager;
import net.l2emuproject.gameserver.events.global.territorywar.TerritoryWarManager;
import net.l2emuproject.gameserver.events.global.territorywar.TerritoryWard;
import net.l2emuproject.gameserver.handler.ItemHandler;
import net.l2emuproject.gameserver.handler.SkillHandler;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminEditChar;
import net.l2emuproject.gameserver.handler.skillhandlers.TakeCastle;
import net.l2emuproject.gameserver.handler.skillhandlers.TakeFort;
import net.l2emuproject.gameserver.items.ItemsAutoDestroy;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.manager.AntiFeedManager;
import net.l2emuproject.gameserver.manager.instances.Instance;
import net.l2emuproject.gameserver.manager.instances.InstanceManager;
import net.l2emuproject.gameserver.network.Disconnection;
import net.l2emuproject.gameserver.network.L2GameClient;
import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.clientpackets.ConfirmDlgAnswer.AnswerHandler;
import net.l2emuproject.gameserver.network.serverpackets.AbstractNpcInfo;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.CameraMode;
import net.l2emuproject.gameserver.network.serverpackets.ChangeWaitType;
import net.l2emuproject.gameserver.network.serverpackets.CharInfo;
import net.l2emuproject.gameserver.network.serverpackets.ConfirmDlg;
import net.l2emuproject.gameserver.network.serverpackets.CreatureSay;
import net.l2emuproject.gameserver.network.serverpackets.EffectInfoPacket.EffectInfoPacketList;
import net.l2emuproject.gameserver.network.serverpackets.EtcStatusUpdate;
import net.l2emuproject.gameserver.network.serverpackets.ExDuelUpdateUserInfo;
import net.l2emuproject.gameserver.network.serverpackets.ExGetOnAirShip;
import net.l2emuproject.gameserver.network.serverpackets.ExManagePartyRoomMember;
import net.l2emuproject.gameserver.network.serverpackets.ExOlympiadUserInfo;
import net.l2emuproject.gameserver.network.serverpackets.ExPrivateStoreSetWholeMsg;
import net.l2emuproject.gameserver.network.serverpackets.ExSetCompassZoneCode;
import net.l2emuproject.gameserver.network.serverpackets.ExSpawnEmitter;
import net.l2emuproject.gameserver.network.serverpackets.ExStartScenePlayer;
import net.l2emuproject.gameserver.network.serverpackets.ExStorageMaxCount;
import net.l2emuproject.gameserver.network.serverpackets.FriendList;
import net.l2emuproject.gameserver.network.serverpackets.FriendStatusPacket;
import net.l2emuproject.gameserver.network.serverpackets.GameGuardQuery;
import net.l2emuproject.gameserver.network.serverpackets.GetOnVehicle;
import net.l2emuproject.gameserver.network.serverpackets.HennaInfo;
import net.l2emuproject.gameserver.network.serverpackets.InventoryUpdate;
import net.l2emuproject.gameserver.network.serverpackets.ItemList;
import net.l2emuproject.gameserver.network.serverpackets.L2GameServerPacket;
import net.l2emuproject.gameserver.network.serverpackets.MagicEffectIcons;
import net.l2emuproject.gameserver.network.serverpackets.MagicSkillUse;
import net.l2emuproject.gameserver.network.serverpackets.MyTargetSelected;
import net.l2emuproject.gameserver.network.serverpackets.NicknameChanged;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.network.serverpackets.PartySmallWindowUpdate;
import net.l2emuproject.gameserver.network.serverpackets.PartySpelled;
import net.l2emuproject.gameserver.network.serverpackets.PetInventoryUpdate;
import net.l2emuproject.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import net.l2emuproject.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.l2emuproject.gameserver.network.serverpackets.PledgeSkillList;
import net.l2emuproject.gameserver.network.serverpackets.PrivateStoreListBuy;
import net.l2emuproject.gameserver.network.serverpackets.PrivateStoreListSell;
import net.l2emuproject.gameserver.network.serverpackets.PrivateStoreManageListBuy;
import net.l2emuproject.gameserver.network.serverpackets.PrivateStoreManageListSell;
import net.l2emuproject.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import net.l2emuproject.gameserver.network.serverpackets.PrivateStoreMsgSell;
import net.l2emuproject.gameserver.network.serverpackets.QuestList;
import net.l2emuproject.gameserver.network.serverpackets.RecipeShopMsg;
import net.l2emuproject.gameserver.network.serverpackets.RecipeShopSellList;
import net.l2emuproject.gameserver.network.serverpackets.RelationChanged;
import net.l2emuproject.gameserver.network.serverpackets.Ride;
import net.l2emuproject.gameserver.network.serverpackets.SetupGauge;
import net.l2emuproject.gameserver.network.serverpackets.ShortBuffStatusUpdate;
import net.l2emuproject.gameserver.network.serverpackets.ShortCutInit;
import net.l2emuproject.gameserver.network.serverpackets.SkillCoolTime;
import net.l2emuproject.gameserver.network.serverpackets.SkillList;
import net.l2emuproject.gameserver.network.serverpackets.Snoop;
import net.l2emuproject.gameserver.network.serverpackets.SocialAction;
import net.l2emuproject.gameserver.network.serverpackets.SpecialCamera;
import net.l2emuproject.gameserver.network.serverpackets.StaticPacket;
import net.l2emuproject.gameserver.network.serverpackets.StatusUpdate;
import net.l2emuproject.gameserver.network.serverpackets.StopMove;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.network.serverpackets.TargetSelected;
import net.l2emuproject.gameserver.network.serverpackets.TargetUnselected;
import net.l2emuproject.gameserver.network.serverpackets.TradeDone;
import net.l2emuproject.gameserver.network.serverpackets.TradeOtherDone;
import net.l2emuproject.gameserver.network.serverpackets.TradeStart;
import net.l2emuproject.gameserver.network.serverpackets.TutorialCloseHtml;
import net.l2emuproject.gameserver.network.serverpackets.UserInfo;
import net.l2emuproject.gameserver.network.serverpackets.ValidateLocation;
import net.l2emuproject.gameserver.services.attribute.Attributes;
import net.l2emuproject.gameserver.services.blocklist.BlockList;
import net.l2emuproject.gameserver.services.clan.L2Clan;
import net.l2emuproject.gameserver.services.clan.L2ClanMember;
import net.l2emuproject.gameserver.services.crafting.L2ManufactureList;
import net.l2emuproject.gameserver.services.crafting.RecipeService;
import net.l2emuproject.gameserver.services.cursedweapons.CursedWeapon;
import net.l2emuproject.gameserver.services.cursedweapons.CursedWeaponsService;
import net.l2emuproject.gameserver.services.duel.Duel;
import net.l2emuproject.gameserver.services.duel.DuelService;
import net.l2emuproject.gameserver.services.friendlist.L2FriendList;
import net.l2emuproject.gameserver.services.party.L2Party;
import net.l2emuproject.gameserver.services.party.L2PartyRoom;
import net.l2emuproject.gameserver.services.party.PartyRoomManager;
import net.l2emuproject.gameserver.services.quest.L2Marker;
import net.l2emuproject.gameserver.services.quest.Quest;
import net.l2emuproject.gameserver.services.quest.QuestService;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.recommendation.RecommendationService;
import net.l2emuproject.gameserver.services.shortcuts.L2ShortCut;
import net.l2emuproject.gameserver.services.transactions.L2Request;
import net.l2emuproject.gameserver.services.transactions.TradeList;
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.skills.L2Effect;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.SkillTargetTypes;
import net.l2emuproject.gameserver.skills.SkillUsageRequest;
import net.l2emuproject.gameserver.skills.Stats;
import net.l2emuproject.gameserver.skills.conditions.ConditionGameTime;
import net.l2emuproject.gameserver.skills.conditions.ConditionPlayerHp;
import net.l2emuproject.gameserver.skills.formulas.Formulas;
import net.l2emuproject.gameserver.skills.funcs.Func;
import net.l2emuproject.gameserver.skills.l2skills.L2SkillSummon;
import net.l2emuproject.gameserver.skills.skilllearn.L2CertificationSkillsLearn;
import net.l2emuproject.gameserver.skills.skilllearn.L2SkillLearn;
import net.l2emuproject.gameserver.skills.skilllearn.L2TransformSkillLearn;
import net.l2emuproject.gameserver.system.announcements.Announcements;
import net.l2emuproject.gameserver.system.cache.HtmCache;
import net.l2emuproject.gameserver.system.database.L2DatabaseFactory;
import net.l2emuproject.gameserver.system.restriction.AvailableRestriction;
import net.l2emuproject.gameserver.system.restriction.ObjectRestrictions;
import net.l2emuproject.gameserver.system.restriction.global.GlobalRestrictions;
import net.l2emuproject.gameserver.system.taskmanager.AbstractIterativePeriodicTaskManager;
import net.l2emuproject.gameserver.system.taskmanager.AttackStanceTaskManager;
import net.l2emuproject.gameserver.system.taskmanager.LeakTaskManager;
import net.l2emuproject.gameserver.system.taskmanager.MovementController;
import net.l2emuproject.gameserver.system.taskmanager.PacketBroadcaster.BroadcastMode;
import net.l2emuproject.gameserver.system.taskmanager.SQLQueue;
import net.l2emuproject.gameserver.system.threadmanager.ThreadPoolManager;
import net.l2emuproject.gameserver.system.time.GameTimeController;
import net.l2emuproject.gameserver.system.util.Broadcast;
import net.l2emuproject.gameserver.system.util.FloodProtector;
import net.l2emuproject.gameserver.system.util.FloodProtector.Protected;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.templates.chars.L2PcTemplate;
import net.l2emuproject.gameserver.templates.item.L2Armor;
import net.l2emuproject.gameserver.templates.item.L2ArmorType;
import net.l2emuproject.gameserver.templates.item.L2EtcItemType;
import net.l2emuproject.gameserver.templates.item.L2Item;
import net.l2emuproject.gameserver.templates.item.L2Weapon;
import net.l2emuproject.gameserver.templates.item.L2WeaponType;
import net.l2emuproject.gameserver.templates.skills.L2EffectType;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.L2WorldRegion;
import net.l2emuproject.gameserver.world.Location;
import net.l2emuproject.gameserver.world.geodata.GeoData;
import net.l2emuproject.gameserver.world.knownlist.CharKnownList;
import net.l2emuproject.gameserver.world.knownlist.PcKnownList;
import net.l2emuproject.gameserver.world.mapregion.MapRegionManager;
import net.l2emuproject.gameserver.world.mapregion.TeleportWhereType;
import net.l2emuproject.gameserver.world.npc.L2PetData;
import net.l2emuproject.gameserver.world.object.instance.L2AirShipInstance;
import net.l2emuproject.gameserver.world.object.instance.L2BoatInstance;
import net.l2emuproject.gameserver.world.object.instance.L2ClassMasterInstance;
import net.l2emuproject.gameserver.world.object.instance.L2CubicInstance;
import net.l2emuproject.gameserver.world.object.instance.L2DefenderInstance;
import net.l2emuproject.gameserver.world.object.instance.L2DoorInstance;
import net.l2emuproject.gameserver.world.object.instance.L2FestivalMonsterInstance;
import net.l2emuproject.gameserver.world.object.instance.L2GuardInstance;
import net.l2emuproject.gameserver.world.object.instance.L2MonsterInstance;
import net.l2emuproject.gameserver.world.object.instance.L2NpcInstance;
import net.l2emuproject.gameserver.world.object.instance.L2PetInstance;
import net.l2emuproject.gameserver.world.object.instance.L2StaticObjectInstance;
import net.l2emuproject.gameserver.world.object.instance.L2SummonInstance;
import net.l2emuproject.gameserver.world.object.instance.L2TamedBeastInstance;
import net.l2emuproject.gameserver.world.zone.L2JailZone;
import net.l2emuproject.gameserver.world.zone.L2Zone;
import net.l2emuproject.gameserver.world.zone.ZoneManager;
import net.l2emuproject.lang.L2Math;
import net.l2emuproject.lang.L2System;
import net.l2emuproject.lang.Replaceable;
import net.l2emuproject.network.mmocore.InvalidPacketException;
import net.l2emuproject.sql.SQLQuery;
import net.l2emuproject.tools.geometry.Point3D;
import net.l2emuproject.tools.random.Rnd;
import net.l2emuproject.util.ArrayBunch;
import net.l2emuproject.util.L2Arrays;
import net.l2emuproject.util.L2Collections;
import net.l2emuproject.util.SingletonList;
import net.l2emuproject.util.SingletonMap;

import org.apache.commons.lang.ArrayUtils;

/**
 * This class represents all player characters in the world.
 * There is always a client-thread connected to this (except if a player-store is activated upon logout).<BR><BR>
 *
 * @version $Revision: 1.66.2.41.2.33 $ $Date: 2005/04/11 10:06:09 $
 */
public final class L2Player extends L2Playable implements ICharacterInfo
{
	public static final L2Player[] EMPTY_ARRAY = new L2Player[0];

	// Characters of an account
	private static final String RESTORE_CHARS_FOR_ACCOUNT		= "SELECT charId, char_name FROM characters WHERE account_name=? AND charId<>?";
	
	// Character Skill Reuse SQL String Definitions:
	private static final String RESTORE_SKILL_REUSES			= "SELECT skillId,reuseDelay,expiration FROM character_skill_reuses WHERE charId=?";
	private static final String ADD_SKILL_REUSE					= "INSERT INTO character_skill_reuses (charId,skillId,reuseDelay,expiration) VALUES (?,?,?,?)";
	private static final String DELETE_SKILL_REUSES				= "DELETE FROM character_skill_reuses WHERE charId=?";

	// Character Character SQL String Definitions:
	private static final String INSERT_CHARACTER				= "INSERT INTO characters (account_name,charId,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp,face,hairStyle,hairColor,sex,exp,sp,karma,fame,pvpkills,pkkills,clanid,race,classid,deletetime,cancraft,title,accesslevel,online,isin7sdungeon,clan_privs,wantspeace,base_class,newbie,nobless,pledge_rank) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String	UPDATE_CHARACTER				= "UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,face=?,hairStyle=?,hairColor=?,sex=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,fame=?,pvpkills=?,pkkills=?,clanid=?,race=?,classid=?,deletetime=?,title=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,in_jail=?,jail_timer=?,newbie=?,nobless=?,pledge_rank=?,subpledge=?,lvl_joined_academy=?,apprentice=?,sponsor=?,varka_ketra_ally=?,clan_join_expiry_time=?,clan_create_expiry_time=?,banchat_timer=?,char_name=?,death_penalty_level=?,vitality_points=?,bookmarkslot=? WHERE charId=?";
	private static final String	RESTORE_CHARACTER				= "SELECT account_name, charId, char_name, level, maxHp, curHp, maxCp, curCp, maxMp, curMp, face, hairStyle, hairColor, sex, heading, x, y, z, exp, expBeforeDeath, sp, karma, fame, pvpkills, pkkills, clanid, race, classid, deletetime, cancraft, title, accesslevel, online, char_slot, lastAccess, clan_privs, wantspeace, base_class, onlinetime, isin7sdungeon, in_jail, jail_timer, banchat_timer, newbie, nobless, pledge_rank, subpledge, lvl_joined_academy, apprentice, sponsor, varka_ketra_ally, clan_join_expiry_time,clan_create_expiry_time,charViP,death_penalty_level,vitality_points,bookmarkslot FROM characters WHERE charId=?";

	// Character Subclass SQL String Definitions:
	private static final String	RESTORE_CHAR_SUBCLASSES			= "SELECT class_id,exp,sp,level,class_index FROM character_subclasses WHERE charId=? ORDER BY class_index ASC";
	private static final String	ADD_CHAR_SUBCLASS				= "INSERT INTO character_subclasses (charId,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)";
	private static final String	UPDATE_CHAR_SUBCLASS			= "UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=? WHERE charId=? AND class_index =?";
	private static final String	DELETE_CHAR_SUBCLASS			= "DELETE FROM character_subclasses WHERE charId=? AND class_index=?";

	public static final byte		REQUEST_TIMEOUT					= 15;

	public static final byte		STORE_PRIVATE_NONE				= 0;
	public static final byte		STORE_PRIVATE_SELL				= 1;
	public static final byte		STORE_PRIVATE_BUY				= 3;
	public static final byte		STORE_PRIVATE_MANUFACTURE		= 5;
	public static final byte		STORE_PRIVATE_PACKAGE_SELL		= 8;

	/** The table containing all minimum level needed for each Expertise (None, D, C, B, A, S, S80, S84)*/
	private static final int[]	EXPERTISE_LEVELS				=
	{
		SkillTreeTable.getInstance().getExpertiseLevel(0), // NONE
		SkillTreeTable.getInstance().getExpertiseLevel(1), // D
		SkillTreeTable.getInstance().getExpertiseLevel(2), // C
		SkillTreeTable.getInstance().getExpertiseLevel(3), // B
		SkillTreeTable.getInstance().getExpertiseLevel(4), // A
		SkillTreeTable.getInstance().getExpertiseLevel(5), // S
		SkillTreeTable.getInstance().getExpertiseLevel(6), // S80
		SkillTreeTable.getInstance().getExpertiseLevel(7)  //S84
	};

	private static final int[] COMMON_CRAFT_LEVELS = { 5, 20, 28, 36, 43, 49, 55, 62 };

	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor()
		{
		}
		
		public L2Player getPlayer()
		{
			return L2Player.this;
		}
		
		public void doPickupItem(L2Object object)
		{
			L2Player.this.doPickupItem(object);
		}
		
		public void doInteract(L2Character target)
		{
			L2Player.this.doInteract(target);
		}
		
		@Override
		public void doAttack(L2Character target)
		{
			super.doAttack(target);
			
			if (target.getActingPlayer() != null 
					&& getSiegeState() > 0 && isInsideZone(L2Zone.FLAG_SIEGE)
					&& target.getActingPlayer().getSiegeState() == getSiegeState()
					&& target.getActingPlayer() != L2Player.this 
					&& target.getActingPlayer().getSiegeSide() == getSiegeSide())
			{
				// 
				if (TerritoryWarManager.getInstance().isTWInProgress())
					sendPacket(SystemMessageId.YOU_CANNOT_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY);
				else
					sendPacket(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Cancel the recent fake-death protection instantly if the player attacks or casts spells
			setRecentFakeDeath(false);
		}
		
		@Override
		public void doCast(L2Skill skill)
		{
			super.doCast(skill);
			
			// Cancel the recent fake-death protection instantly if the player attacks or casts spells
			setRecentFakeDeath(false);
			if (skill == null)
				return;
			if (!skill.isOffensive())
				return;
			
			switch (skill.getTargetType())
			{
				case TARGET_GROUND:
					return;
				default:
				{
					for (L2CubicInstance cubic : getCubics().values())
						if (cubic.getId() != L2CubicInstance.LIFE_CUBIC)
							cubic.doAction();
				}
					break;
			}
		}
	}

	private L2GameClient					_client;

	private final PcAppearance				_appearance;

	/** Sitting down and Standing up fix */
	private long							_lastSitStandRequest	= 0;

	/** The Identifier of the L2Player */
	private int								_charId					= 0x00030b7a;

	/** The Experience of the L2Player before the last Death Penalty */
	private long							_expBeforeDeath;

	/** The Karma of the L2Player (if higher than 0, the name of the L2Player appears in red) */
	private int								_karma;

	/** The number of player killed during a PvP (the player killed was PvP Flagged) */
	private int								_pvpKills;

	/** The PK counter of the L2Player (= Number of non PvP Flagged player killed) */
	private int								_pkKills;

	/** The Siege state of the L2Player */
	private byte							_siegeState				= SIEGE_STATE_NOT_INVOLVED;
	/** The id of castle/fort which the L2Player is registered for siege */
	private int								_siegeSide				= 0;
	private boolean							_isInSiege				= false;

	private int								_lastCompassZone;																	// The last compass zone update send to the client

	private boolean							_isIn7sDungeon			= false;

	private int								_subPledgeType			= 0;

	/** L2Player's pledge rank*/
	private int								_pledgeRank;

	/** Level at which the player joined the clan as an accedemy member*/
	private int								_lvlJoinedAcademy		= 0;

	/** The random number of the L2Player */
	//private static final Random _rnd = new Random();
	private int								_curWeightPenalty		= 0;

	private long							_deleteTimer;
	private PcInventory						_inventory;
	private PcWarehouse						_warehouse;
	private PcRefund 						_refund;
	private final PcSkills					_pcSkills = new PcSkills(this);

	/** True if the L2Player is sitting */
	private boolean							_waitTypeSitting;

	/** True if the L2Player is using the relax skill */
	private boolean							_relax;
	
	/** Boat and AirShip */
    private L2Vehicle 						_vehicle = null;
    private Point3D 						_inVehiclePosition;

	/** Last NPC Id talked on a quest */
	private int								_questNpcObject			= 0;

	/** Bitmask used to keep track of one-time/newbie quest rewards */
	private int								_newbie;

	/** The table containing all Quests began by the L2Player */
	private final Map<String, QuestState>	_quests					= new SingletonMap<String, QuestState>();

	private TradeList						_activeTradeList;
	private ItemContainer					_activeWarehouse;
	private L2ManufactureList				_createList;
	private TradeList						_sellList;
	private TradeList						_buyList;

	private L2Player[] _snoopers = L2Player.EMPTY_ARRAY; // List of GMs snooping this player
	private L2Player[] _snoopedPlayers = L2Player.EMPTY_ARRAY; // List of players being snooped by this GM

	/** The Private Store type of the L2Player (STORE_PRIVATE_NONE=0, STORE_PRIVATE_SELL=1, sellmanage=2, STORE_PRIVATE_BUY=3, buymanage=4, STORE_PRIVATE_MANUFACTURE=5) */
	private int								_privatestore;
	private ClassId							_skillLearningClassId;

	private boolean							_isRidingStrider		= false;
	private boolean							_isRidingRedStrider		= false;
	private boolean							_isRidingHorse			= false;
	private boolean 						_isFlyingMounted 		= false;

	/** The L2Summon of the L2Player */
	private L2Summon						_summon					= null;
	/** The L2Decoy of the L2Player */
	private L2Decoy							_decoy					= null;
	/** The L2Trap of the L2Player */
	private L2Trap							_trap					= null;
	/** The L2Agathion of the L2Player */
	private int								_agathionId				= 0;
	// Apparently, a L2Player CAN have both a summon AND a tamed beast at the same time!!
	private L2TamedBeastInstance			_tamedBeast				= null;

	// Client radar
	private L2Marker							_radar;

	// These values are only stored temporarily
	private boolean							_lookingForParty;
	private boolean							_partyMatchingAllLevels;
	private int								_partyMatchingRegion;
	private L2PartyRoom						_partyRoom;
	private L2Party							_party;
	// Clan related attributes

	/** The Clan Identifier of the L2Player */
	private int								_clanId;

	/** The Clan object of the L2Player */
	private L2Clan							_clan;

	/** Apprentice and Sponsor IDs */
	private int								_apprentice				= 0;
	private int								_sponsor				= 0;

	private long							_clanJoinExpiryTime;
	private long							_clanCreateExpiryTime;

	private long							_onlineTime;
	private long							_onlineBeginTime;

	// GM Stuff
	private boolean							_isGm;
	private int								_accessLevel;

	private boolean							_messageRefusal			= false;													// Message refusal mode
	private boolean							_dietMode				= false;													// Ignore weight penalty
	private boolean							_tradeRefusal			= false;													// Trade refusal
	private boolean							_exchangeRefusal		= false;													// Exchange refusal

	// This is needed to find the inviting player for Party response
	// There can only be one active party request at once
	private L2Player						_activeRequester;
	private long							_requestExpireTime		= 0;
	private L2Request						_request;
	private L2ItemInstance					_arrowItem;
	private L2ItemInstance					_boltItem;

	// Used for protection after teleport
	private long							_protectEndTime			= 0;

	// Protects a char from agro mobs when getting up from fake death
	private long							_recentFakeDeathEndTime	= 0;

	/** The fists L2Weapon of the L2Player (used when no weapon is equipped) */
	private L2Weapon						_fistsWeaponItem;

	private long							_uptime;
	private final String					_accountName;

	private Map<Integer, String>			_chars;

	private int								_mountType;
	private int								_mountNpcId;
	private int 							_mountLevel;

	/** The current higher Expertise of the L2Player (None=0, D=1, C=2, B=3, A=4, S=5, S80=6, S84=7)*/
	private int								_expertiseIndex;																	// Index in EXPERTISE_LEVELS
	private int								_expertiseWeaponPenalty;
	private int								_expertiseArmorPenalty;

	private boolean							_isEnchanting			= false;
	private L2ItemInstance					_activeEnchantItem		= null;
	private L2ItemInstance					_activeEnchantSupportItem = null;
	private L2ItemInstance					_activeEnchantAttrItem	= null;
	private long							_activeEnchantTimestamp = 0;

	public static final byte ONLINE_STATE_LOADED = 0;
	public static final byte ONLINE_STATE_ONLINE = 1;
	public static final byte ONLINE_STATE_DELETED = 2;

	private byte _isOnline = ONLINE_STATE_LOADED;

	protected boolean						_inventoryDisabled		= false;

	protected Map<Integer, L2CubicInstance>	_cubics					= new SingletonMap<Integer, L2CubicInstance>().shared();

	/** The L2NpcInstance corresponding to the last Folk wich one the player talked. */
	private L2Npc							_lastFolkNpc			= null;

	private int								_clanPrivileges			= 0;

	/** L2Player's pledge class (knight, Baron, etc.)*/
	private int								_pledgeClass			= 0;

	public int								_telemode				= 0;

	/** new loto ticket **/
	private final int						_loto[]					= new int[5];
	/** new race ticket **/
	private final int						_race[]					= new int[2];

	private BlockList						_blockList;
	private L2FriendList					_friendList;

	private int								_team					= 0;
	private int								_wantsPeace				= 0;

	// Death Penalty Buff Level
	private int								_deathPenaltyBuffLevel	= 0;

	// Self resurrect during siege
	private boolean							_charmOfCourage			= false;

	private boolean							_hero					= false;
	private boolean							_noble					= false;

	/** ally with ketra or varka related vars*/
	private int								_alliedVarkaKetra		= 0;

	/**
	 * IMO we don't need it, as we have FIFO packet execution.
	 */
	private final ReentrantLock 			_subclassLock = new ReentrantLock();
	/** The list of sub-classes this character has. */
	private Map<Integer, SubClass>			_subClasses;
	protected int							_baseClass;
	protected int							_activeClass;
	protected int							_classIndex				= 0;

	/** data for mounted pets */
	private int								_controlItemId;
	private L2PetData						_data;
	private int								_curFeed;
	protected Future<?>						_mountFeedTask;
	private ScheduledFuture<?>				_dismountTask;

	private long							_lastAccess;

	private ScheduledFuture<?>				_taskRentPet;
	private ScheduledFuture<?>				_taskWater;

	/** Bypass validations */
	private List<String>					 _validBypass;
	private List<String> 					_validBypass2;
	
	private List<String>					_validLink;

	/** The number of evaluation points obtained by this player */
	private int								_evalPoints;

	/** The number of evaluations this player can give */
	private int								_evaluations;

	/** List of players this player already evaluated */
	private final List<Integer>				_evaluated				= new SingletonList<Integer>();

	private boolean							_inCrystallize;

	private boolean							_inCraftMode;

	/** Store object used to summon the strider you are mounting **/
	private int								_mountObjectID			= 0;

	/** character VIP **/
	private boolean							_charViP				= false;

	private boolean							_inJail					= false;
	private long							_jailTimer				= 0;

	private boolean							_maried					= false;
	private int								_partnerId				= 0;
	private int								_coupleId				= 0;
	private boolean							_maryrequest			= false;
	private boolean							_maryaccepted			= false;

	private int								_clientRevision			= 0;

	/* Flag to disable equipment/skills while wearing formal wear **/
	private boolean							_IsWearingFormalWear	= false;

	private L2StaticObjectInstance			_throne;

	// Absorbed Souls
	private int								_souls					= 0;
	private ScheduledFuture<?>				_soulTask				= null;
	private int								_lastSoulConsume		= 0;

	// Force charges
	private int								_charges				= 0;
	private ScheduledFuture<?>				_chargeTask				= null;

	public int								_fame = 0;					// The Fame of this L2Player
	private ScheduledFuture<?>				_fameTask;

	private ScheduledFuture<?>				_teleportWatchdog;

	// Id of the afro hair cut
	private int								_afroId					= 0;

	private long							_lastTargetChange;
	private int								_lastTargetId;

	private boolean							_illegalWaiting;

	private long							_nextJumpTime;
	
	// extension management
	private PlayerTeleportBookmark 			_teleBookmarkExtension 		= null;
	private PlayerVitality 					_vitalityExtension 			= null;
	private PlayerCertification 			_certificationExtension 	= null;
	private PlayerBirthday 					_birthdayExtension 			= null;
	private PlayerTransformation 			_transformationExtension 	= null;
	private PlayerHenna 					_hennaExtension 			= null;
	private PlayerRecipe 					_recipeExtension			= null;
	private PlayerCustom 					_customExtension 			= null;
	private PlayerObserver 					_observerExtension 			= null;
	private PlayerOlympiad 					_olympiadExtension 			= null;
	private PlayerFish 						_fishExtension 				= null;
	private PlayerDuel						_duelExtension				= null;
	private PlayerSettings					_settingsExtension			= null;
	private PlayerEventData 				_playerEventData 			= null;

	@Override
	public final boolean isAllSkillsDisabled()
	{
		return super.isAllSkillsDisabled() || isTryingToSitOrStandup();
	}

	@Override
	public final boolean isAttackingDisabled()
	{
		return super.isAttackingDisabled() || _combatFlagEquipped || isTryingToSitOrStandup();
	}

	@Override
	public boolean isInProtectedAction()
	{
		return super.isInProtectedAction() || isTryingToSitOrStandup();
	}

	/**
	 * Create a new L2Player and add it in the characters table of the database.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Create a new L2Player with an account name </li>
	 * <li>Set the name, the Hair Style, the Hair Color and  the Face type of the L2Player</li>
	 * <li>Add the player in the characters table of the database</li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2PcTemplate to apply to the L2Player
	 * @param accountName The name of the L2Player
	 * @param name The name of the L2Player
	 * @param hairStyle The hair style Identifier of the L2Player
	 * @param hairColor The hair color Identifier of the L2Player
	 * @param face The face type Identifier of the L2Player
	 *
	 * @return The L2Player added to the database or null
	 *
	 */
	public static L2Player create(int objectId, L2PcTemplate template, String accountName, String name, byte hairStyle, byte hairColor, byte face,
			boolean sex)
	{
		// Create a new L2Player with an account name
		PcAppearance app = new PcAppearance(face, hairColor, hairStyle, sex);
		L2Player player = new L2Player(objectId, template, accountName, app);

		// Set the name of the L2Player
		player.setName(name);

		// Set the base class ID to that of the actual class ID.
		player.setBaseClass(player.getClassId());

		// Kept for backwards compabitility.
		player.setNewbie(1);

		// Add the player in the characters table of the database
		boolean ok = player.createDb();

		if (!ok)
			return null;

		return player;
	}

	@Override
	public String getAccountName()
	{
		if (getClient() == null)
			return _accountName;
		return getClient().getAccountName();
	}

	public int getRelation(L2Player target)
	{
		int result = 0;

		if (getClan() != null)
			result |= RelationChanged.RELATION_CLAN_MEMBER;

		if (isClanLeader())
			result |= RelationChanged.RELATION_LEADER;

		L2Party party = getParty();
		if (party != null && party == target.getParty())
		{
			result |= RelationChanged.RELATION_HAS_PARTY;

			switch (party.getPartyMembers().indexOf(this))
			{
			case 0:
				result |= RelationChanged.RELATION_PARTYLEADER; // 0x10
				break;
			case 1:
				result |= RelationChanged.RELATION_PARTY4; // 0x8
				break;
			case 2:
				result |= RelationChanged.RELATION_PARTY3+RelationChanged.RELATION_PARTY2+RelationChanged.RELATION_PARTY1; // 0x7
				break;
			case 3:
				result |= RelationChanged.RELATION_PARTY3+RelationChanged.RELATION_PARTY2; // 0x6
				break;
			case 4:
				result |= RelationChanged.RELATION_PARTY3+RelationChanged.RELATION_PARTY1; // 0x5
				break;
			case 5:
				result |= RelationChanged.RELATION_PARTY3; // 0x4
				break;
			case 6:
				result |= RelationChanged.RELATION_PARTY2+RelationChanged.RELATION_PARTY1; // 0x3
				break;
			case 7:
				result |= RelationChanged.RELATION_PARTY2; // 0x2
				break;
			case 8:
				result |= RelationChanged.RELATION_PARTY1; // 0x1
				break;
			}
		}

		if (getSiegeState() != SIEGE_STATE_NOT_INVOLVED)
		{
			if (TerritoryWarManager.getInstance().getRegisteredTerritoryId(this) != 0)
			{
				result |= RelationChanged.RELATION_TERRITORY_WAR;
			}
			else
			{
				result |= RelationChanged.RELATION_INSIEGE;
				if (getSiegeState() != target.getSiegeState())
					result |= RelationChanged.RELATION_ENEMY;
				else
					result |= RelationChanged.RELATION_ALLY;
				if (getSiegeState() == SIEGE_STATE_ATTACKER)
					result |= RelationChanged.RELATION_ATTACKER;	
			}
		}

		if (getClan() != null && target.getClan() != null)
		{
			if (target.getSubPledgeType() != L2Clan.SUBUNIT_ACADEMY && getSubPledgeType() != L2Clan.SUBUNIT_ACADEMY && target.getClan().isAtWarWith(getClan().getClanId()))
			{
				result |= RelationChanged.RELATION_1SIDED_WAR;
				if (getClan().isAtWarWith(target.getClan().getClanId()))
					result |= RelationChanged.RELATION_MUTUAL_WAR;
			}
		}
		if (getBlockCheckerArena() != -1)
		{
			result |= RelationChanged.RELATION_INSIEGE;
			HandysBlockCheckerManager.ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(getBlockCheckerArena());
			if (holder.getPlayerTeam(this) == 0)
				result |= RelationChanged.RELATION_ENEMY;
			else
				result |= RelationChanged.RELATION_ALLY;
			result |= RelationChanged.RELATION_ATTACKER;
		}
		return result;
	}

	public Map<Integer, String> getAccountChars()
	{
		if (_chars == null)
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();

				// Retrieve the name and ID of the other characters assigned to this account.
				PreparedStatement statement = con.prepareStatement(RESTORE_CHARS_FOR_ACCOUNT);
				statement.setString(1, getAccountName());
				statement.setInt(2, getObjectId());
				ResultSet rset = statement.executeQuery();

				while (rset.next())
				{
					if (_chars == null)
						_chars = new HashMap<Integer, String>();

					_chars.put(rset.getInt("charId"), rset.getString("char_name"));
				}

				rset.close();
				statement.close();
			}
			catch (SQLException e)
			{
				_log.warn("", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}

			if (_chars == null)
				_chars = L2Collections.emptyMap();
		}

		return _chars;
	}

	private void initPcStatusUpdateValues()
	{
		_cpUpdateInterval = getMaxCp() / 352.0;
		_cpUpdateIncCheck = getMaxCp();
		_cpUpdateDecCheck = getMaxCp() - _cpUpdateInterval;
		_mpUpdateInterval = getMaxMp() / 352.0;
		_mpUpdateIncCheck = getMaxMp();
		_mpUpdateDecCheck = getMaxMp() - _mpUpdateInterval;
	}

	/**
	 * Constructor of L2Player (use L2Character constructor).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to create an empty _skills slot and copy basic Calculator set to this L2Player </li>
	 * <li>Set the name of the L2Player</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method SET the level of the L2Player to 1</B></FONT><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2PcTemplate to apply to the L2Player
	 * @param accountName The name of the account including this L2Player
	 *
	 */
	private L2Player(int objectId, L2PcTemplate template, String accountName, PcAppearance app)
	{
		super(objectId, template);
		getKnownList(); // Init knownlist
		getStat(); // Init stats
		getStatus(); // Init status
		super.initCharStatusUpdateValues();
		initPcStatusUpdateValues();

		_accountName = accountName;
		app.setOwner(this);
		_appearance = app;

		// Create an AI
		getAI();

		// Retrieve from the database all items of this L2Player and add them to _inventory
		getInventory().restore();
		getWarehouse();
		getPlayerVitality().startVitalityTask();
	}

	@Override
	protected CharKnownList initKnownList()
	{
		return new PcKnownList(this);
	}

	@Override
	public final PcKnownList getKnownList()
	{
		return (PcKnownList)_knownList;
	}

	@Override
	protected CharLikeView initView()
	{
		return new PcView(this);
	}

	@Override
	public PcView getView()
	{
		return (PcView)_view;
	}

	@Override
	protected CharStat initStat()
	{
		return new PcStat(this);
	}

	@Override
	public final PcStat getStat()
	{
		return (PcStat)_stat;
	}

	@Override
	protected CharStatus initStatus()
	{
		return new PcStatus(this);
	}

	@Override
	public final PcStatus getStatus()
	{
		return (PcStatus)_status;
	}
	
	@Override
	protected PcEffects initEffects()
	{
		return new PcEffects(this);
	}
	
	@Override
	public PcEffects getEffects()
	{
		return (PcEffects)_effects;
	}
	
	public final PcAppearance getAppearance()
	{
		return _appearance;
	}

	@Override
	public void setTitle(String value)
	{
		if (value.length() > 16)
			value = value.substring(0, 15);

		super.setTitle(value);
	}

	/**
	 * Return the base L2PcTemplate link to the L2Player.<BR><BR>
	 */
	public final L2PcTemplate getBaseTemplate()
	{
		return CharTemplateTable.getInstance().getTemplate(_baseClass);
	}

	/** Return the L2PcTemplate link to the L2Player. */
	@Override
	public final L2PcTemplate getTemplate()
	{
		return (L2PcTemplate) super.getTemplate();
	}

	public void setTemplate(ClassId newclass)
	{
		super.setTemplate(CharTemplateTable.getInstance().getTemplate(newclass));
	}

	@Override
	protected L2CharacterAI initAI()
	{
		return new L2PlayerAI(new L2Player.AIAccessor());
	}

	/** Return the Level of the L2Player. */
	@Override
	public final int getLevel()
	{
		return getStat().getLevel();
	}

	/**
	 * Return the _newbie rewards state of the L2Player.<BR><BR>
	 */
	public int getNewbie()
	{
		return _newbie;
	}

	/**
	 * Set the _newbie rewards state of the L2Player.<BR><BR>
	 *
	 * @param newbieRewards The Identifier of the _newbie state<BR><BR>
	 *
	 */
	public void setNewbie(int newbieRewards)
	{
		_newbie = newbieRewards;
	}

	public void setBaseClass(int baseClass)
	{
		_baseClass = baseClass;
	}

	public void setBaseClass(ClassId classId)
	{
		_baseClass = classId.ordinal();
	}

	public boolean isInStoreMode()
	{
		return (getPrivateStoreType() > 0);
	}

	public boolean isInCraftMode()
	{
		return _inCraftMode;
	}

	public void isInCraftMode(boolean b)
	{
		_inCraftMode = b;
	}

	/**
	 * Returns the Id for the last talked quest NPC.<BR><BR>
	 */
	public int getLastQuestNpcObject()
	{
		return _questNpcObject;
	}

	public void setLastQuestNpcObject(int npcId)
	{
		_questNpcObject = npcId;
	}

	/**
	 * Return the QuestState object corresponding to the quest name.<BR><BR>
	 *
	 * @param quest The name of the quest
	 *
	 */
	public QuestState getQuestState(String quest)
	{
		return _quests.get(quest);
	}

	/**
	 * Add a QuestState to the table _quest containing all quests began by the L2Player.<BR><BR>
	 *
	 * @param qs The QuestState to add to _quest
	 *
	 */
	public void setQuestState(QuestState qs)
	{
		_quests.put(qs.getQuestName(), qs);
	}

	/**
	 * Remove a QuestState from the table _quest containing all quests began by the L2Player.<BR><BR>
	 *
	 * @param quest The name of the quest
	 *
	 */
	public void delQuestState(String quest)
	{
		_quests.remove(quest);
	}

	private QuestState[] addToQuestStateArray(QuestState[] questStateArray, QuestState state)
	{
		int len = questStateArray.length;
		QuestState[] tmp = new QuestState[len + 1];
		System.arraycopy(questStateArray, 0, tmp, 0, len);
		tmp[len] = state;
		return tmp;
	}

	/**
	 * Return a table containing all Quest in progress from the table _quests.<BR><BR>
	 */
	public Quest[] getAllActiveQuests()
	{
		ArrayBunch<Quest> quests = new ArrayBunch<Quest>();

		for (QuestState qs : _quests.values())
		{
			if (qs == null)
				continue;

			int questId = qs.getQuest().getQuestIntId();
			if ((questId > 19999) || (questId < 1))
				continue;

			if (!qs.isStarted())
				continue;

			quests.add(qs.getQuest());
		}

		return quests.moveToArray(new Quest[quests.size()]);
	}

	/**
	 * Return a table containing all QuestState to modify after a L2Attackable killing.<BR><BR>
	 *
	 * @param npc The Identifier of the L2Attackable attacked
	 *
	 */
	public QuestState[] getQuestsForAttacks(L2NpcInstance npc)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;

		// Go through the QuestState of the L2Player quests
		for (Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK))
		{
			// Check if the Identifier of the L2Attackable attck is needed for the current quest
			if (getQuestState(quest.getName()) != null)
			{
				// Copy the current L2Player QuestState in the QuestState table
				if (states == null)
					states = new QuestState[]
					                        { getQuestState(quest.getName()) };
				else
					states = addToQuestStateArray(states, getQuestState(quest.getName()));
			}
		}

		// Return a table containing all QuestState to modify
		return states;
	}

	/**
	 * Return a table containing all QuestState to modify after a L2Attackable killing.<BR><BR>
	 *
	 * @param npc The Identifier of the L2Attackable killed
	 *
	 */
	public QuestState[] getQuestsForKills(L2NpcInstance npc)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;

		// Go through the QuestState of the L2Player quests
		for (Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL))
		{
			// Check if the Identifier of the L2Attackable killed is needed for the current quest
			if (getQuestState(quest.getName()) != null)
			{
				// Copy the current L2Player QuestState in the QuestState table
				if (states == null)
					states = new QuestState[]
					                        { getQuestState(quest.getName()) };
				else
					states = addToQuestStateArray(states, getQuestState(quest.getName()));
			}
		}

		// Return a table containing all QuestState to modify
		return states;
	}

	/**
	 * Return a table containing all QuestState from the table _quests in which the L2Player must talk to the NPC.<BR><BR>
	 *
	 * @param npcId The Identifier of the NPC
	 *
	 */
	public QuestState[] getQuestsForTalk(int npcId)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;

		// Go through the QuestState of the L2Player quests
		Quest[] quests = NpcTable.getInstance().getTemplate(npcId).getEventQuests(Quest.QuestEventType.ON_TALK);
		if (quests != null)
		{
			for (Quest quest : quests)
			{
				// Copy the current L2Player QuestState in the QuestState table
				if (quest != null)
				{
					// Copy the current L2Player QuestState in the QuestState table
					if (getQuestState(quest.getName()) != null)
					{
						if (states == null)
							states = new QuestState[]
							                        { getQuestState(quest.getName()) };
						else
							states = addToQuestStateArray(states, getQuestState(quest.getName()));
					}
				}
			}
		}

		// Return a table containing all QuestState to modify
		return states;
	}

	public QuestState processQuestEvent(String quest, String event)
	{
		QuestState retval = null;
		if (event == null)
			event = "";
		QuestState qs = getQuestState(quest);
		if (qs == null && event.isEmpty())
			return retval;
		if (qs == null)
		{
			Quest q = QuestService.getInstance().getQuest(quest);
			if (q == null)
				return retval;
			qs = q.newQuestState(this);
		}
		if (qs != null)
		{
			if (getLastQuestNpcObject() > 0)
			{
				L2Object object = getKnownList().getKnownObject(getLastQuestNpcObject());
				if (object instanceof L2Npc && isInsideRadius(object, L2Npc.INTERACTION_DISTANCE, false, false))
				{
					L2Npc npc = (L2Npc) object;
					QuestState[] states = getQuestsForTalk(npc.getNpcId());

					if (states != null)
					{
						for (QuestState state : states)
						{
							if ((state.getQuest().getQuestIntId() == qs.getQuest().getQuestIntId()))// && !qs.isCompleted())
							{
								if (qs.getQuest().notifyEvent(event, npc, this))
									showQuestWindow(quest, State.getStateName(qs.getState()));

								retval = qs;
							}
						}
						sendPacket(new QuestList(this));
					}
				}
			}
		}

		return retval;
	}

	/**
	 * FIXME: move this from L2Player, there is no reason to have this here
	 * @param questId
	 * @param stateId
	 */
	private void showQuestWindow(String questId, String stateId)
	{
		String path = "data/scripts/quests/" + questId + "/" + stateId + ".htm";
		String content = HtmCache.getInstance().getHtm(path);

		if (content != null)
		{
			if (_log.isDebugEnabled())
				_log.debug("Showing quest window for quest " + questId + " state " + stateId + " html path: " + path);

			NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
			npcReply.setHtml(content);
			sendPacket(npcReply);
		}

		sendPacket(ActionFailed.STATIC_PACKET);
	}

	/** List of all QuestState instance that needs to be notified of this L2Player's or its pet's death */
	private final List<QuestState> _NotifyQuestOfDeathList = new SingletonList<QuestState>();

	/**
	 * Add QuestState instance that is to be notified of L2Player's death.<BR>
	 * <BR>
	 *
	 * @param qs The QuestState that subscribe to this event
	 */
	public void addNotifyQuestOfDeath(QuestState qs)
	{
		if (qs == null || _NotifyQuestOfDeathList.contains(qs))
			return;

		_NotifyQuestOfDeathList.add(qs);
	}

	/**
	 * Remove QuestState instance that is to be notified of L2Player's death.<BR>
	 * <BR>
	 *
	 * @param qs The QuestState that subscribe to this event
	 */
	public void removeNotifyQuestOfDeath(QuestState qs)
	{
		if (qs == null || !_NotifyQuestOfDeathList.contains(qs))
			return;

		_NotifyQuestOfDeathList.remove(qs);
	}

	/**
	 * Return a list of QuestStates which registered for notify of death of this L2Player.<BR>
	 * <BR>
	 */
	public final List<QuestState> getNotifyQuestOfDeath()
	{
		return _NotifyQuestOfDeathList;
	}

	/**
	 * Set the siege state of the L2Player.<BR><BR>
	 * 1 = attacker, 2 = defender, 0 = not involved
	 */
	public void setSiegeState(byte siegeState)
	{
		_siegeState = siegeState;
		broadcastRelationChanged();
	}

	/**
	 * Get the siege state of the L2Player.<BR><BR>
	 * 1 = attacker, 2 = defender, 0 = not involved
	 */
	public byte getSiegeState()
	{
		return _siegeState;
	}
	
	public void setSiegeSide(int val)
	{
		_siegeSide = val;
	}
	
	public boolean isRegisteredOnThisSiegeField(int val)
	{
		if (_siegeSide != val && (_siegeSide < 81 || _siegeSide > 89))
			return false;
		return true;
	}
	
	public int getSiegeSide()
	{
		return _siegeSide;
	}
	
	public static final byte SIEGE_STATE_NOT_INVOLVED = 0;
	public static final byte SIEGE_STATE_ATTACKER = 1;
	public static final byte SIEGE_STATE_DEFENDER = 2;

	@Override
	public boolean revalidateZone(boolean force)
	{
		if (!super.revalidateZone(force))
			return false;

		if (Config.ALLOW_WATER)
			checkWaterState();

		if (isInsideZone(L2Zone.FLAG_SIEGE))
		{
			setLastCompassZone(ExSetCompassZoneCode.SIEGE_WAR);
		}
		else if (isInsideZone(L2Zone.FLAG_PVP))
		{
			setLastCompassZone(ExSetCompassZoneCode.PVP);
		}
		else if (isIn7sDungeon())
		{
			setLastCompassZone(ExSetCompassZoneCode.SEVEN_SIGNS);
		}
		else if (isInsideZone(L2Zone.FLAG_PEACE))
		{
			setLastCompassZone(ExSetCompassZoneCode.PEACEFUL);
		}
		else
		{
			if (_lastCompassZone == ExSetCompassZoneCode.SIEGE_WAR.getZoneCode())
				updatePvPStatus();

			setLastCompassZone(ExSetCompassZoneCode.GENERAL);
		}

		return true;
	}

	private void setLastCompassZone(ExSetCompassZoneCode packet)
	{
		if (_lastCompassZone == packet.getZoneCode())
			return;

		_lastCompassZone = packet.getZoneCode();
		sendPacket(packet);
	}

	/**
	 * Return True if the L2Player can Craft Dwarven Recipes.<BR><BR>
	 */
	public boolean hasDwarvenCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN) >= 1;
	}

	public int getDwarvenCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN);
	}

	/**
	 * Return True if the L2Player can Craft Dwarven Recipes.<BR><BR>
	 */
	public boolean hasCommonCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_COMMON) >= 1;
	}

	public int getCommonCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_COMMON);
	}

	/**
	 * Return the PK counter of the L2Player.<BR><BR>
	 */
	public int getPkKills()
	{
		return _pkKills;
	}

	/**
	 * Set the PK counter of the L2Player.<BR><BR>
	 */
	public void setPkKills(int pkKills)
	{
		_pkKills = pkKills;
	}

	/**
	 * Return the _deleteTimer of the L2Player.<BR><BR>
	 */
	public long getDeleteTimer()
	{
		return _deleteTimer;
	}

	/**
	 * Set the _deleteTimer of the L2Player.<BR><BR>
	 */
	public void setDeleteTimer(long deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}

	/**
	 * Return the current weight of the L2Player.<BR><BR>
	 */
	@Override
	public int getCurrentLoad()
	{
		return getInventory().getTotalWeight();
	}

	/** @return the number of evaluation points obtained by player. */
	public int getEvalPoints()
	{
		return _evalPoints;
	}

	/**
	 * Set the number of evaluation points obtained by player.
	 * @param value Evaluation point count
	 */
	public void setEvalPoints(int value)
	{
		_evalPoints = value;
	}

	/** @return the number of evaluations this player can give away. */
	public int getEvaluations()
	{
		return _evaluations;
	}

	/**
	 * Set the number of available evaluations.
	 * @param value New available evaluation count
	 */
	public void setEvaluationCount(int value)
	{
		_evaluations = value;
	}

	/**
	 * Add a player that has been evaluated by this player.
	 * @param charId evaluated player's ID
	 */
	public void addEvalRestriction(int charId)
	{
		_evaluated.add(charId);
	}

	/**	Removes all session evaluation restrictions for this player. */
	public void cleanEvalRestrictions()
	{
		_evaluated.clear();
	}

	/**
	 * @param target Player being evaluated
	 * @return whether this player hasn't evaluated the given player
	 */
	public boolean canEvaluate(L2Player target)
	{
		return !_evaluated.contains(target.getObjectId());
	}
	
	public final int getEvalBonusType()
	{
		// Maintain = 1
		return 0;
	}
	
	public final int getEvalBonusTime()
	{
		// TODO: Implement me...
		return 0;
	}

	/**
	 * Set the exp of the L2Player before a death
	 * @param exp
	 */
	public void setExpBeforeDeath(long exp)
	{
		_expBeforeDeath = exp;
	}

	public long getExpBeforeDeath()
	{
		return _expBeforeDeath;
	}

	/**
	 * Return the Karma of the L2Player.<BR><BR>
	 */
	public int getKarma()
	{
		return _karma;
	}

	/**
	 * Set the Karma of the L2Player and send a Server->Client packet StatusUpdate (broadcast).<BR><BR>
	 */
	public void setKarma(int karma)
	{
		if (karma < 0)
			karma = 0;
		if (_karma == 0 && karma > 0)
		{
			for (L2Object object : getKnownList().getKnownObjects().values())
			{
				if (!(object instanceof L2GuardInstance))
					continue;

				if (((L2GuardInstance) object).getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
					((L2GuardInstance) object).getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			}
		}
		else if (_karma > 0 && karma == 0)
		{
			// Send a Server->Client StatusUpdate packet with Karma and PvP Flag to the L2Player and all L2Player to inform (broadcast)
			setKarmaFlag(0);
		}
		_karma = karma;
		broadcastKarma();
	}

	/**
	 * Return the max weight that the L2Player can load.<BR><BR>
	 */
	@Override
	public int getMaxLoad()
	{
		return (int)(calcStat(Stats.MAX_LOAD, 69000, this, null) * Config.ALT_WEIGHT_LIMIT);
	}

	public int getExpertiseWeaponPenalty()
	{
		return _expertiseWeaponPenalty;
	}

	private void setWeaponPenalty(int level)
	{
		_expertiseWeaponPenalty = level;
	}

	public int getExpertiseArmorPenalty()
	{
		return _expertiseArmorPenalty;
	}

	private void setArmorPenalty(int level)
	{
		_expertiseArmorPenalty = level;
	}

	@Deprecated
	public boolean getExpertisePenalty()
	{
		return getExpertiseWeaponPenalty() > 0 || getExpertiseArmorPenalty() > 0;
	}

	@Override
	public int getWeightPenalty()
	{
		return _curWeightPenalty;
	}

	@Override
	public void setWeightPenalty(int value)
	{
		_curWeightPenalty = value;
	}

	public void refreshExpertisePenalty()
	{
		if (!Config.ALT_GRADE_PENALTY)
			return;

		int weaponPenalty = 0;
		int armorPenalty = 0;
		boolean sendUpdate = false;

		for (L2ItemInstance item : getInventory().getItems())
		{
			if (!item.isEquipped() || item.getItem() == null
					|| item.getItem().getCrystalType() <= getExpertiseIndex())
				continue;

			if (item.getItem().getType2() == L2Item.TYPE2_WEAPON)
				weaponPenalty = (item.getItem().getCrystalType() - getExpertiseIndex());
			else
				armorPenalty++;
		}

		L2Skill skill = getKnownSkill(6209);
		int skillLevel = skill == null ? 0 : skill.getLevel();
		if (weaponPenalty > 4)
			weaponPenalty = 4;
		if (getExpertiseWeaponPenalty() != weaponPenalty || skillLevel != weaponPenalty)
		{
			setWeaponPenalty(weaponPenalty);
			if (weaponPenalty > 0)
				super.addSkill(6209, weaponPenalty);
			else
				super.removeSkill(skill);
			sendUpdate = true;
		}

		skill = getKnownSkill(6213);
		skillLevel = skill == null ? 0 : skill.getLevel();
		if (armorPenalty > 4)
			armorPenalty = 4;
		if (getExpertiseArmorPenalty() != armorPenalty || skillLevel != armorPenalty)
		{
			setArmorPenalty(armorPenalty);
			if (armorPenalty > 0)
				super.addSkill(6213, armorPenalty);
			else
				super.removeSkill(skill);
			sendUpdate = true;
		}

		if (sendUpdate)
			sendEtcStatusUpdate();
	}

	/**
	 * Return the the PvP Kills of the L2Player (Number of player killed during a PvP).<BR><BR>
	 */
	public int getPvpKills()
	{
		return _pvpKills;
	}

	/**
	 * Set the the PvP Kills of the L2Player (Number of player killed during a PvP).<BR><BR>
	 */
	public void setPvpKills(int pvpKills)
	{
		_pvpKills = pvpKills;
	}

	/**
	 * Return the ClassId object of the L2Player contained in L2PcTemplate.<BR><BR>
	 */
	public ClassId getClassId()
	{
		return getTemplate().getClassId();
	}

	public void academyCheck(int Id)
	{
		if ((getSubPledgeType() == -1 || getLvlJoinedAcademy() != 0) && _clan != null && ClassId.values()[Id].getLevel() == ClassLevel.Third)
		{
			if (getLvlJoinedAcademy() <= 16)
				_clan.setReputationScore(_clan.getReputationScore() + Config.JOIN_ACADEMY_MAX_REP_SCORE, true);
			else if (getLvlJoinedAcademy() >= 39)
				_clan.setReputationScore(_clan.getReputationScore() + Config.JOIN_ACADEMY_MIN_REP_SCORE, true);
			else
				_clan.setReputationScore(_clan.getReputationScore() + (Config.JOIN_ACADEMY_MAX_REP_SCORE - (getLvlJoinedAcademy() - 16) * 20), true);
			setLvlJoinedAcademy(0);

			// Oust pledge member from the academy, cuz he has finished his 2nd class transfer
			SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED);
			msg.addString(getName());
			_clan.broadcastToOnlineMembers(msg);
			_clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(getName()));

			_clan.removeClanMember(getObjectId(), 0);
			sendPacket(SystemMessageId.ACADEMY_MEMBERSHIP_TERMINATED);
			// Receive graduation gift
			getInventory().addItem("Gift", 8181, 1, this, null); // Give academy circlet
		}
	}

	/**
	 * Set the template of the L2Player.<BR><BR>
	 *
	 * @param Id The Identifier of the L2PcTemplate to set to the L2Player
	 *
	 */
	public void setClassId(int Id)
	{
		if (!_subclassLock.tryLock())
			return;

		try
		{
			academyCheck(Id);

			if (isSubClassActive())
			{
				getSubClasses().get(_classIndex).setClassId(Id);
			}
			setClassTemplate(Id);

			setTarget(this);
			// Animation: Production - Clan / Transfer
			MagicSkillUse msu = new MagicSkillUse(this, this, 5103, 1, 1196, 0);
			broadcastPacket(msu);

			// Update class icon in party and clan
			broadcastClassIcon();

			rewardSkills();
		}
		finally
		{
			_subclassLock.unlock();
		}
	}

	public void useEquippableItem(L2ItemInstance item, boolean abortAttack)
	{
		// Equip or unEquip
		L2ItemInstance[] items = null;
		final boolean isEquiped = item.isEquipped();
		final int oldInvLimit = getInventoryLimit();
		SystemMessage sm = null;
		L2ItemInstance old = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
		if (old == null)
			old = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);

		int bodyPart = item.getItem().getBodyPart();
		if (isEquiped)
		{
			if (item.getEnchantLevel() > 0)
			{
				sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
				sm.addNumber(item.getEnchantLevel());
				sm.addItemName(item);
			}
			else
			{
				sm = new SystemMessage(SystemMessageId.S1_DISARMED);
				sm.addItemName(item);
			}
			sendPacket(sm);

			switch (bodyPart)
			{
			case L2Item.SLOT_L_EAR:
			case L2Item.SLOT_LR_EAR:
			case L2Item.SLOT_L_FINGER:
			case L2Item.SLOT_LR_FINGER:
				getInventory().setPaperdollItem(item.getLocationSlot(), null);
				sendPacket(new ItemList(this, false));
			}

			// We can't unequip talisman by body slot
			if (bodyPart == L2Item.SLOT_DECO)
				items = getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
			else
				items = getInventory().unEquipItemInBodySlotAndRecord(bodyPart);
		}
		else
		{
			L2ItemInstance tempItem = getInventory().getPaperdollItemByL2ItemId(bodyPart);

			// Check if the item replaces a wear-item
			if (tempItem != null && tempItem.isWear())
			{
				// Don't allow an item to replace a wear-item
				return;
			}
			else if (bodyPart == L2Item.SLOT_LR_HAND)
			{
				// This may not remove left OR right hand equipment
				tempItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
				if (tempItem != null && tempItem.isWear()) return;

				tempItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
				if (tempItem != null && tempItem.isWear()) return;
			}
			else if (bodyPart == L2Item.SLOT_FULL_ARMOR)
			{
				// This may not remove chest or leggings
				tempItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
				if (tempItem != null && tempItem.isWear()) return;

				tempItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
				if (tempItem != null && tempItem.isWear()) return;
			}

			if (item.getEnchantLevel() > 0)
			{
				sm = new SystemMessage(SystemMessageId.S1_S2_EQUIPPED);
				sm.addNumber(item.getEnchantLevel());
				sm.addItemName(item);
			}
			else
			{
				sm = new SystemMessage(SystemMessageId.S1_EQUIPPED);
				sm.addItemName(item);
			}
			sendPacket(sm);

			if ((bodyPart & L2Item.SLOT_HEAD) > 0 || (bodyPart & L2Item.SLOT_NECK) > 0
					|| (bodyPart & L2Item.SLOT_L_EAR) > 0 || (bodyPart & L2Item.SLOT_R_EAR) > 0
					|| (bodyPart & L2Item.SLOT_L_FINGER) > 0 || (bodyPart & L2Item.SLOT_R_FINGER) > 0
					|| (bodyPart & L2Item.SLOT_R_BRACELET) > 0 || (bodyPart & L2Item.SLOT_L_BRACELET) > 0
					|| (bodyPart & L2Item.SLOT_DECO) > 0)
			{
				// must be sent explicitly before IU
				sendPacket(new UserInfo(this));
			}

			items = getInventory().equipItemAndRecord(item);

			// Consume mana - will start a task if required; returns if item is not a shadow item
			item.decreaseMana(false);
		}

		refreshExpertisePenalty();

		InventoryUpdate iu = new InventoryUpdate();
		iu.addEquipItems(items);
		sendPacket(iu);

		// must be sent explicitly after IU
		sendPacket(new UserInfo(this));
		// send 3rd time, just like retail
		broadcastUserInfo();

		if (abortAttack)
			abortAttack();

		if (getInventoryLimit() != oldInvLimit)
			sendPacket(new ExStorageMaxCount(this));
	}

	/** Return the Experience of the L2Player. */
	public long getExp()
	{
		return getStat().getExp();
	}

	public void setActiveEnchantItem(L2ItemInstance scroll)
	{
		// If we dont have a Enchant Item, we are not enchanting.
		if (scroll == null)
		{
			setActiveEnchantSupportItem(null);
			setActiveEnchantTimestamp(0);
			setIsEnchanting(false);
		}

		_activeEnchantItem = scroll;
	}

	public L2ItemInstance getActiveEnchantItem()
	{
		return _activeEnchantItem;
	}

	public void setActiveEnchantSupportItem(L2ItemInstance item)
	{
		_activeEnchantSupportItem = item;
	}

	public L2ItemInstance getActiveEnchantSupportItem()
	{
		return _activeEnchantSupportItem;
	}

	public long getActiveEnchantTimestamp()
	{
		return _activeEnchantTimestamp;
	}

	public void setActiveEnchantTimestamp(long val)
	{
		_activeEnchantTimestamp = val;
	}

	public void setActiveEnchantAttrItem(L2ItemInstance stone)
	{
		_activeEnchantAttrItem = stone;
	}

	public L2ItemInstance getActiveEnchantAttrItem()
	{
		return _activeEnchantAttrItem;
	}

	public void setIsEnchanting(boolean val)
	{
		_isEnchanting = val;
	}

	public boolean isEnchanting()
	{
		return _isEnchanting;
	}

	/**
	 * Set the fists weapon of the L2Player (used when no weapon is equipped).<BR><BR>
	 *
	 * @param weaponItem The fists L2Weapon to set to the L2Player
	 *
	 */
	public void setFistsWeaponItem(L2Weapon weaponItem)
	{
		_fistsWeaponItem = weaponItem;
	}

	/**
	 * Return the fists weapon of the L2Player (used when no weapon is equipped).<BR><BR>
	 */
	public L2Weapon getFistsWeaponItem()
	{
		return _fistsWeaponItem;
	}

	/**
	 * Return the fists weapon of the L2Player Class (used when no weapon is equipped).<BR><BR>
	 */
	public L2Weapon findFistsWeaponItem(int classId)
	{
		L2Weapon weaponItem = null;
		if ((classId >= 0x00) && (classId <= 0x09))
		{
			// Human fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(246);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x0a) && (classId <= 0x11))
		{
			// Human mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(251);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x12) && (classId <= 0x18))
		{
			// Elven fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(244);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x19) && (classId <= 0x1e))
		{
			// Elven mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(249);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x1f) && (classId <= 0x25))
		{
			// Dark elven fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(245);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x26) && (classId <= 0x2b))
		{
			// Dark elven mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(250);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x2c) && (classId <= 0x30))
		{
			// Orc fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(248);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x31) && (classId <= 0x34))
		{
			// Orc mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(252);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x35) && (classId <= 0x39))
		{
			// Dwarven fists
			L2Item temp = ItemTable.getInstance().getTemplate(247);
			weaponItem = (L2Weapon) temp;
		}

		return weaponItem;
	}

	/**
	 * Give Expertise skill of this level and remove beginner Lucky skill.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the Level of the L2Player </li>
	 * <li>If L2Player Level is 5, remove beginner Lucky skill </li>
	 * <li>Add the Expertise skill corresponding to its Expertise level</li>
	 * <li>Update the overloaded status of the L2Player</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T give other free skills (SP needed = 0)</B></FONT><BR><BR>
	 *
	 */
	public void rewardSkills()
	{
		// Get the Level of the L2Player
		int lvl = getLevel();

		// Remove beginner Lucky skill
		if (lvl > 9)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(194, 1);
			skill = removeSkill(skill);

			if (_log.isDebugEnabled() && skill != null)
				_log.debug("removed skill 'Lucky' from " + getName());
		}

		// Calculate the current higher Expertise of the L2Player
		for (int i = 0; i < EXPERTISE_LEVELS.length; i++)
		{
			if (lvl >= EXPERTISE_LEVELS[i])
				setExpertiseIndex(i);
		}

		// Add the Expertise skill corresponding to its Expertise level
		if (getExpertiseIndex() > 0)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(239, getExpertiseIndex());
			addSkill(skill);

			if (_log.isDebugEnabled())
				_log.debug("awarded " + getName() + " with new expertise.");

		}
		else
		{
			if (_log.isDebugEnabled())
				_log.debug("No skills awarded at lvl: " + lvl);
		}

		// Active skill dwarven craft
		if (getSkillLevel(1321) < 1 && getRace() == Race.Dwarf)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(1321, 1);
			addSkill(skill);
		}

		// Active skill common craft
		if (getSkillLevel(1322) < 1)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(1322, 1);
			addSkill(skill);
		}

		for (int i = 0; i < COMMON_CRAFT_LEVELS.length; i++)
		{
			if (lvl >= COMMON_CRAFT_LEVELS[i] && getSkillLevel(1320) < (i + 1))
			{
				L2Skill skill = SkillTable.getInstance().getInfo(1320, (i + 1));
				addSkill(skill);
			}
		}

		// Auto-Learn skills if activated
		if (Config.ALT_AUTO_LEARN_SKILLS && !isCursedWeaponEquipped())
			sendMessage("You have learned " + SkillTreeTable.getInstance().giveAvailableSkills(this) + ".");

		if (isGM() && !hasSkill(7029))
		{
			addSkill(SkillTable.getInstance().getInfo(7029, 4));
		}
	}

	/** Set the Experience value of the L2Player. */
	public void setExp(long exp)
	{
		getStat().setExp(exp);
	}

	/**
	 * Regive all skills which aren't saved to database, like Noble, Hero, Clan Skills<BR><BR>
	 *
	 */
	public void regiveTemporarySkills()
	{
		// Do not call this on enterworld or char load

		// Add noble skills if noble
		if (isNoble())
			setNoble(true);

		// Add Hero skills if hero
		if (isHero())
			setHero(true);

		if (getClan() != null)
		{
			setPledgeClass(L2ClanMember.getCurrentPledgeClass(this));
			getClan().addSkillEffects(this, false);
			PledgeSkillList psl = new PledgeSkillList(getClan());
			sendPacket(psl);
			if (getClan().getLevel() >= Config.SIEGE_CLAN_MIN_LEVEL && isClanLeader())
				SiegeManager.getInstance().addSiegeSkills(this);
			enableResidentialSkills(true);
		}

		// Reload passive skills from armors / jewels / weapons
		getInventory().reloadEquippedItems();

		// Add Death Penalty Buff Level
		restoreDeathPenaltyBuffLevel();
	}

	public void enableResidentialSkills(boolean enable)
	{
		if (getClan() == null)
			return;

		if (enable)
		{
			if (getClan().getHasCastle() > 0)
				CastleManager.getInstance().getCastleByOwner(getClan()).giveResidentialSkills(this);

			if (getClan().getHasFort() > 0)
				FortManager.getInstance().getFortByOwner(getClan()).giveResidentialSkills(this);
		}
		else
		{
			if (getClan().getHasCastle() > 0)
				CastleManager.getInstance().getCastleByOwner(getClan()).removeResidentialSkills(this);
			if (getClan().getHasFort() > 0)
				FortManager.getInstance().getFortByOwner(getClan()).removeResidentialSkills(this);
		}
	}
	
	/**
	 * Return the Race object of the L2Player.<BR><BR>
	 */
	public Race getRace()
	{
		if (!isSubClassActive())
			return getTemplate().getRace();

		L2PcTemplate charTemp = CharTemplateTable.getInstance().getTemplate(_baseClass);
		return charTemp.getRace();
	}

	public L2Marker getRadar()
	{
		if (_radar == null)
			_radar = new L2Marker(this);

		return _radar;
	}

	/** Return the SP amount of the L2Player. */
	public int getSp()
	{
		return getStat().getSp();
	}

	/** Set the SP amount of the L2Player. */
	public void setSp(int sp)
	{
		getStat().setSp(sp);
	}

	/**
	 * Return true if this L2Player is a clan leader in
	 * ownership of the passed castle
	 */
	public boolean isCastleLord(int castleId)
	{
		L2Clan clan = getClan();
		// Player has clan and is the clan leader, check the castle info
		if ((clan != null) && (clan.getLeader().getPlayerInstance() == this))
		{
			// If the clan has a castle and it is actually the queried castle, return true
			Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
			if ((castle != null) && (castle == CastleManager.getInstance().getCastleById(castleId)))
				return true;
		}
		return false;
	}

	/**
	 * Return the Clan Identifier of the L2Player.<BR><BR>
	 */
	public int getClanId()
	{
		return _clanId;
	}

	/**
	 * Return the Clan Crest Identifier of the L2Player or 0.<BR><BR>
	 */
	public int getClanCrestId()
	{
		if (_clan != null && _clan.hasCrest())
		{
			return _clan.getCrestId();
		}
		return 0;
	}

	/**
	 * @return The Clan CrestLarge Identifier or 0
	 */
	public int getClanCrestLargeId()
	{
		if (_clan != null && _clan.hasCrestLarge())
		{
			return _clan.getCrestLargeId();
		}
		return 0;
	}

	public long getClanJoinExpiryTime()
	{
		return _clanJoinExpiryTime;
	}

	public void setClanJoinExpiryTime(long time)
	{
		_clanJoinExpiryTime = time;
	}

	public long getClanCreateExpiryTime()
	{
		return _clanCreateExpiryTime;
	}

	public void setClanCreateExpiryTime(long time)
	{
		_clanCreateExpiryTime = time;
	}

	public void setOnlineTime(long time)
	{
		_onlineTime = time;
		_onlineBeginTime = System.currentTimeMillis();
	}

	/**
	 * Return the PcInventory Inventory of the L2Player contained in _inventory.<BR><BR>
	 */
	@Override
	public PcInventory getInventory()
	{
		if (_inventory == null)
			_inventory = new PcInventory(this);

		return _inventory;
	}

	/**
	 * Return True if the L2Player is sitting.<BR><BR>
	 */
	public boolean isSitting()
	{
		return _waitTypeSitting;
	}

	public void sitDown()
	{
		sitDown(true);
	}

	/**
	 * Sit down the L2Player, set the AI Intention to AI_INTENTION_REST and send a Server->Client ChangeWaitType packet (broadcast)<BR><BR>
	 */
	public void sitDown(boolean force)
	{
		if ((isCastingNow() || isCastingSimultaneouslyNow()) && !_relax)
		{
			sendMessage("Cannot sit while casting");
			return;
		}
		if (!(_waitTypeSitting || super.isAttackingDisabled() || isOutOfControl() || isImmobilized() || (!force && isTryingToSitOrStandup())))
		{
			breakAttack();
			_waitTypeSitting = true;
			getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_SITTING));
			_lastSitStandRequest = System.currentTimeMillis();
		}
	}

	public void standUp()
	{
		standUp(true);
	}

	/**
	 * Stand up the L2Player, set the AI Intention to AI_INTENTION_IDLE and send a Server->Client ChangeWaitType packet (broadcast)<BR><BR>
	 */
	public void standUp(boolean force)
	{
		if (_waitTypeSitting && !isInStoreMode() && !isAlikeDead() && (!isTryingToSitOrStandup() || force))
		{
			if (_relax)
			{
				setRelax(false);
				stopEffects(L2EffectType.RELAXING);
			}
			_waitTypeSitting = false;
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STANDING));
			_lastSitStandRequest = System.currentTimeMillis();
		}
	}

	/**
	 * Set the value of the _relax value. Must be True if using skill Relax and False if not.
	 */
	public void setRelax(boolean val)
	{
		_relax = val;
	}

	/**
	 * Return the PcWarehouse object of the L2Player.<BR><BR>
	 */
	public PcWarehouse getWarehouse()
	{
		if (_warehouse == null)
		{
			_warehouse = new PcWarehouse(this);
			_warehouse.restore();
		}
		return _warehouse;
	}

	/**
	 * Free memory used by Warehouse
	 */
	public void clearWarehouse()
	{
		if (_warehouse != null)
			_warehouse.deleteMe();
		_warehouse = null;
	}
	
	/**
	 * Returns true if refund list is not empty
	 */
	public boolean hasRefund()
	{
		return _refund != null && _refund.getSize() > 0 && Config.ALLOW_REFUND;
	}
	
	/**
	 * Returns refund object or create new if not exist
	 */
	public PcRefund getRefund()
	{
		if (_refund == null)
			_refund = new PcRefund(this);
		return _refund;
	}
	
	/**
	 * Clear refund
	 */
	public void clearRefund()
	{
		if (_refund != null)
			_refund.deleteMe();
		_refund = null;
	}

	/**
	 * Return the Identifier of the L2Player.<BR><BR>
	 */
	public int getCharId()
	{
		return _charId;
	}

	/**
	 * Set the Identifier of the L2Player.<BR><BR>
	 */
	public void setCharId(int charId)
	{
		_charId = charId;
	}

	/**
	 * Return the Adena amount of the L2Player.<BR><BR>
	 */
	public long getAdena()
	{
		return getInventory().getAdena();
	}

	/**
	 * Return the Ancient Adena amount of the L2Player.<BR><BR>
	 */
	public long getAncientAdena()
	{
		return getInventory().getAncientAdena();
	}

	/**
	 * Add adena to Inventory of the L2Player and send a Server->Client InventoryUpdate packet to the L2Player.
	 * @param process : String Identifier of process triggering this action
	 * @param count : long Quantity of adena to be added
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void addAdena(String process, long count, L2Object reference, boolean sendMessage)
	{
		if (count > 0)
		{
			getInventory().addAdena(process, count, this, reference);

			if (sendMessage)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S1_ADENA);
				sm.addItemNumber(count);
				sendPacket(sm);
			}

			// Send update packet
			getInventory().updateInventory(getInventory().getAdenaInstance());
		}
	}

	/**
	 * Reduce adena in Inventory of the L2Player and send a Server->Client InventoryUpdate packet to the L2Player.
	 * @param process : String Identifier of process triggering this action
	 * @param count : long Quantity of adena to be reduced
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean reduceAdena(String process, long count, L2Object reference, boolean sendMessage)
	{
		if (count > getAdena())
		{
			if (sendMessage)
				sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			return false;
		}

		if (count > 0)
		{
			L2ItemInstance adenaItem = getInventory().getAdenaInstance();
			getInventory().reduceAdena(process, count, this, reference);

			// Send update packet
			getInventory().updateInventory(adenaItem);

			if (sendMessage)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_ADENA_DISAPPEARED);
				sm.addItemNumber(count);
				sendPacket(sm);
			}
		}

		return true;
	}

	/**
	 * Add ancient adena to Inventory of the L2Player and send a Server->Client InventoryUpdate packet to the L2Player.
	 *
	 * @param process : String Identifier of process triggering this action
	 * @param count : long Quantity of ancient adena to be added
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void addAncientAdena(String process, long count, L2Object reference, boolean sendMessage)
	{
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
			sm.addItemName(PcInventory.ANCIENT_ADENA_ID);
			sm.addItemNumber(count);
			sendPacket(sm);
		}

		if (count > 0)
		{
			getInventory().addAncientAdena(process, count, this, reference);
			getInventory().updateInventory(getInventory().getAncientAdenaInstance());
		}
	}

	/**
	 * Reduce ancient adena in Inventory of the L2Player and send a Server->Client InventoryUpdate packet to the L2Player.
	 * @param process : String Identifier of process triggering this action
	 * @param count : long Quantity of ancient adena to be reduced
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean reduceAncientAdena(String process, long count, L2Object reference, boolean sendMessage)
	{
		if (count > getAncientAdena())
		{
			if (sendMessage)
				sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);

			return false;
		}

		if (count > 0)
		{
			getInventory().reduceAncientAdena(process, count, this, reference);
			getInventory().updateInventory(getInventory().getAncientAdenaInstance());
			if (sendMessage)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
				sm.addItemName(PcInventory.ANCIENT_ADENA_ID);
				sm.addItemNumber(count);
				sendPacket(sm);
			}
		}

		return true;
	}

	/**
	 * Adds item to inventory and send a Server->Client InventoryUpdate packet to the L2Player.
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be added
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public L2ItemInstance addItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage, boolean UpdateIL)
	{
		if (item.getCount() > 0)
		{
			// Sends message to client if requested
			if (sendMessage)
			{
				if (item.getCount() > 1)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
					sm.addItemName(item);
					sm.addItemNumber(item.getCount());
					sendPacket(sm);
				}
				else if (item.getEnchantLevel() > 0)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_A_S1_S2);
					sm.addNumber(item.getEnchantLevel());
					sm.addItemName(item);
					sendPacket(sm);
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
					sm.addItemName(item);
					sendPacket(sm);
				}
			}

			// Add the item to inventory
			L2ItemInstance newitem = getInventory().addItem(process, item, this, reference);

			// Do treatments after adding this item
			processAddItem(UpdateIL, newitem);
			return newitem;
		}
		return null;
	}

	/**
	 * Adds item to Inventory and send a Server->Client InventoryUpdate packet to the L2Player.
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item Identifier of the item to be added
	 * @param count : long Quantity of items to be added
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public L2ItemInstance addItem(String process, int itemId, long count, L2Object reference, boolean sendMessage, boolean UpdateIL)
	{
		if (count > 0)
		{
			// Add the item to inventory
			L2ItemInstance newItem = getInventory().addItem(process, itemId, count, this, reference);

			// Sends message to client if requested
			if (sendMessage)
			{
				sendMessageForNewItem(newItem, count, process);
			}

			processAddItem(UpdateIL, newItem);
			return newItem;
		}
		return null;
	}

	/**
	 * @param UpdateIL
	 * @param newitem
	 */
	private void processAddItem(boolean UpdateIL, L2ItemInstance newitem)
	{
		// If over capacity, drop the item
		if (!isGM() && !getInventory().validateCapacity(0) && newitem.isDropable())
		{
			dropItem("InvDrop", newitem, null, true);
		}
		// Cursed Weapon
		else if (CursedWeaponsService.getInstance().isCursed(newitem.getItemId()))
		{
			if (!CursedWeaponsService.getInstance().activate(this, newitem))
				dropItem("CwDrop", newitem, null, true);
		}
		// Combat Flag
		else if (FortSiegeManager.getInstance().isCombat(newitem.getItemId()))
		{
			if (FortSiegeManager.getInstance().activateCombatFlag(this, newitem))
			{
				Fort fort = FortManager.getInstance().getFort(this);
				if (fort != null)
					fort.getSiege().announceToPlayer(new SystemMessage(SystemMessageId.C1_ACQUIRED_THE_FLAG), getName());
			}
			//else // FIXME: i'm not sure about this
			//	dropItem("CombatFlagDrop", newitem, null, true);
		}
		// Territory Ward
		else if (newitem.getItemId() >= 13560 && newitem.getItemId() <= 13568)
		{
			TerritoryWard ward = TerritoryWarManager.getInstance().getTerritoryWard(newitem.getItemId() - 13479);
			if (ward != null)
				ward.activate(this, newitem);
		}

		// Auto use herbs - autoloot
		else if (newitem.getItemType() == L2EtcItemType.HERB)
		{
			ItemHandler.getInstance().useItem(newitem.getItemId(), this, newitem);
		}

		// Update current load as well
		if (UpdateIL)
		{
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
			sendPacket(su);
		}

		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(newitem);
			sendPacket(playerIU);
		}
		else
			sendPacket(new ItemList(this, false));
	}

	/**
	 * @param item : L2ItemInstance Item Identifier of the item to be added
	 * @param count : long Quantity of items to be added
	 * @param process : String Identifier of process triggering this action
	 */
	private void sendMessageForNewItem(L2ItemInstance item, long count, String process)
	{
		if (count > 1)
		{
			if (process.equalsIgnoreCase("sweep") || process.equalsIgnoreCase("Quest"))
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
				sm.addItemName(item);
				sm.addItemNumber(count);
				sendPacket(sm);
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
				sm.addItemName(item);
				sm.addItemNumber(count);
				sendPacket(sm);
			}
		}
		else
		{
			if (process.equalsIgnoreCase("sweep") || process.equalsIgnoreCase("Quest"))
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S1);
				sm.addItemName(item);
				sendPacket(sm);
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
				sm.addItemName(item);
				sendPacket(sm);
			}
		}
	}

	public void addItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
	{
		addItem(process, item, reference, sendMessage, true);
	}

	public void addItem(String process, int itemId, long count, L2Object reference, boolean sendMessage)
	{
		addItem(process, itemId, count, reference, sendMessage, true);
	}

	/**
	 * Destroy item from inventory and send a Server->Client InventoryUpdate packet to the L2Player.
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be destroyed
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean destroyItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
	{
		return this.destroyItem(process, item, item.getCount(), reference, sendMessage);
	}

	/**
	 * Destroy item from inventory and send a Server->Client InventoryUpdate packet to the L2Player.
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be destroyed
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean destroyItem(String process, L2ItemInstance item, long count, L2Object reference, boolean sendMessage)
	{
		item = getInventory().destroyItem(process, item, count, this, reference);

		if (item == null)
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			return false;
		}

		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}

		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);

		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
			sm.addItemName(item);
			sm.addItemNumber(count);
			sendPacket(sm);
		}

		return true;
	}

	/**
	 * Destroys item from inventory and send a Server->Client InventoryUpdate packet to the L2Player.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be destroyed
	 * @param count : long Quantity of items to be destroyed
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	@Override
	public boolean destroyItem(String process, int objectId, long count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = getInventory().getItemByObjectId(objectId);
		if (item == null)
		{
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			return false;
		}

		return destroyItem(process, item, count, reference, sendMessage);
	}

	/**
	 * Destroys shots from inventory without logging and only occasional saving to database.
	 * Sends a Server->Client InventoryUpdate packet to the L2Player.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be destroyed
	 * @param count : long Quantity of items to be destroyed
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean destroyItemWithoutTrace(String process, int objectId, long count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = getInventory().getItemByObjectId(objectId);

		if (item == null || item.getCount() < count)
		{
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			return false;
		}

		return destroyItem(null, item, count, reference, sendMessage);
	}

	/**
	 * Destroy item from inventory by using its <B>itemId</B> and send a Server->Client InventoryUpdate packet to the L2Player.
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item identifier of the item to be destroyed
	 * @param count : long Quantity of items to be destroyed
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	@Override
	public boolean destroyItemByItemId(String process, int itemId, long count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = getInventory().getItemByItemId(itemId);
		if (item == null || item.getCount() < count || getInventory().destroyItemByItemId(process, itemId, count, this, reference) == null)
		{
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);

			return false;
		}

		// Send inventory update packet
		getInventory().updateInventory(item);

		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
			sm.addItemName(item);
			sm.addItemNumber(count);
			sendPacket(sm);
		}
		return true;
	}

	/**
	 * Destroy all weared items from inventory and send a Server->Client InventoryUpdate packet to the L2Player.
	 * @param process : String Identifier of process triggering this action
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public void destroyWearedItems(String process, L2Object reference, boolean sendMessage)
	{

		// Go through all Items of the inventory
		for (L2ItemInstance item : getInventory().getItems())
		{
			// Check if the item is a Try On item in order to remove it
			if (item.isWear())
			{
				if (item.isEquipped())
					getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());

				if (getInventory().destroyItem(process, item, this, reference) == null)
				{
					_log.warn("Player " + getName() + " can't destroy weared item: " + item.getName() + "[ " + item.getObjectId() + " ]");
					continue;
				}

				// Send an Unequipped Message in system window of the player for each Item
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISARMED);
				sm.addItemName(item);
				sendPacket(sm);

			}
		}

		// Send the ItemList Server->Client Packet to the player in order to refresh its Inventory
		ItemList il = new ItemList(this, true);
		sendPacket(il);

		// Send a Server->Client packet UserInfo to this L2Player and CharInfo to all L2Player in its _knownPlayers
		broadcastUserInfo();

		// Sends message to client if requested
		sendMessage("Trying-on mode has ended.");

	}

	/**
	 * Transfers item to another ItemContainer and send a Server->Client InventoryUpdate packet to the L2Player.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Identifier of the item to be transfered
	 * @param count : long Quantity of items to be transfered
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the new item or the updated item in inventory
	 */
	public L2ItemInstance transferItem(String process, int objectId, long count, Inventory target, L2Object reference)
	{
		L2ItemInstance oldItem = checkItemManipulation(objectId, count, "transfer");
		if (oldItem == null)
			return null;
		L2ItemInstance newItem = getInventory().transferItem(process, objectId, count, target, this, reference);
		if (newItem == null)
			return null;

		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();

			if (oldItem.getCount() > 0 && oldItem != newItem)
				playerIU.addModifiedItem(oldItem);
			else
				playerIU.addRemovedItem(oldItem);

			sendPacket(playerIU);
		}
		else
			sendPacket(new ItemList(this, false));

		// Update current load as well
		StatusUpdate playerSU = new StatusUpdate(getObjectId());
		playerSU.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(playerSU);

		// Send target update packet
		if (target instanceof PcInventory)
		{
			L2Player targetPlayer = ((PcInventory)target).getOwner();

			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate playerIU = new InventoryUpdate();

				if (newItem.getCount() > count)
					playerIU.addModifiedItem(newItem);
				else
					playerIU.addNewItem(newItem);

				targetPlayer.sendPacket(playerIU);
			}
			else
				targetPlayer.sendPacket(new ItemList(targetPlayer, false));

			// Update current load as well
			playerSU = new StatusUpdate(targetPlayer.getObjectId());
			playerSU.addAttribute(StatusUpdate.CUR_LOAD, targetPlayer.getCurrentLoad());
			targetPlayer.sendPacket(playerSU);
		}
		else if (target instanceof PetInventory)
		{
			PetInventoryUpdate petIU = new PetInventoryUpdate();

			if (newItem.getCount() > count)
				petIU.addModifiedItem(newItem);
			else
				petIU.addNewItem(newItem);

			((PetInventory)target).getOwner().getOwner().sendPacket(petIU);
		}

		return newItem;
	}

	/**
	 * Drop item from inventory and send a Server->Client InventoryUpdate packet to the L2Player.
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be dropped
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean dropItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
	{
		item = getInventory().dropItem(process, item, this, reference);

		if (item == null)
		{
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			return false;
		}

		item.dropMe(this, getX() + Rnd.get(50) - 25, getY() + Rnd.get(50) - 25, getZ() + 20);

		if (Config.DESTROY_DROPPED_PLAYER_ITEM)
		{
			ItemsAutoDestroy.tryAddItem(item);

			if (!item.isEquipable() || (item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM))
				item.setProtected(false);
			else
				item.setProtected(true);
		}
		else
			item.setProtected(true);

		// Send inventory update packet
		getInventory().updateInventory(item);

		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DROPPED_S1);
			sm.addItemName(item);
			sendPacket(sm);
		}

		return true;
	}

	/**
	 * Drop item from inventory by using its <B>objectID</B> and send a Server->Client InventoryUpdate packet to the L2Player.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be dropped
	 * @param count : long Quantity of items to be dropped
	 * @param x : int coordinate for drop X
	 * @param y : int coordinate for drop Y
	 * @param z : int coordinate for drop Z
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return L2ItemInstance corresponding to the new item or the updated item in inventory
	 */
	public L2ItemInstance dropItem(String process, int objectId, long count, int x, int y, int z, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance olditem = getInventory().getItemByObjectId(objectId);
		L2ItemInstance item = getInventory().dropItem(process, objectId, count, this, reference);

		if (item == null)
		{
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);

			return null;
		}

		item.dropMe(this, x, y, z);
		// Destroy item droped from inventory by player when DESTROY_PLAYER_INVENTORY_DROP is set to true
		if (Config.DESTROY_PLAYER_INVENTORY_DROP)
		{
			ItemsAutoDestroy.tryAddItem(item);

			item.setProtected(false);
		}
		// Avoids it from beeing removed by the auto item destroyer
		else
			item.setDropTime(0);

		// Send inventory update packet
		getInventory().updateInventory(olditem);

		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DROPPED_S1);
			sm.addItemName(item);
			sendPacket(sm);
		}
		return item;
	}

	public L2ItemInstance checkItemManipulation(int objectId, long count, String action)
	{
		L2ItemInstance item = getInventory().getItemByObjectId(objectId);
		if (item == null)
		{
			_log.debug(getObjectId() + ": player tried to " + action + " item he is not owner of");
			return null;
		}

		if (count < 0 || (count > 1 && !item.isStackable()))
		{
			_log.debug(getObjectId() + ": player tried to " + action + " item with invalid count: " + count);
			return null;
		}

		if (count > item.getCount())
		{
			_log.debug(getObjectId() + ": player tried to " + action + " more items than he owns");
			return null;
		}

		// Pet is summoned and not the item that summoned the pet AND not the buggle from strider you're mounting
		if (getPet() != null && getPet().getControlItemId() == objectId || getMountObjectID() == objectId)
		{
			if (_log.isDebugEnabled())
				_log.debug(getObjectId() + ": player tried to " + action + " item controling pet");
			return null;
		}

		if (getActiveEnchantItem() != null && getActiveEnchantItem().getObjectId() == objectId)
		{
			if (_log.isDebugEnabled())
				_log.debug(getObjectId() + ":player tried to " + action + " an enchant scroll he was using");
			return null;
		}

		if (item.isWear())
		{
			// Cannot drop/trade wear-items
			return null;
		}

		// We cannot put a Weapon with Augmention in WH while casting (Possible Exploit)
		if (item.isAugmented() && (isCastingNow() || isCastingSimultaneouslyNow()))
			return null;

		return item;
	}

	/**
	 * Set _protectEndTime according settings.
	 */
	public void setProtection(boolean protect)
	{
		int proTime = Config.PLAYER_SPAWN_PROTECTION;
		if (protect && (proTime == 0 || getPlayerOlympiad().isInOlympiadMode()))
			return;

		if (_log.isDebugEnabled() && (protect || _protectEndTime > 0))
			_log.debug(getName() + ": Protection "
					+ (protect ? "ON " + (GameTimeController.getGameTicks() + proTime * GameTimeController.TICKS_PER_SECOND) : "OFF")
					+ " (currently " + GameTimeController.getGameTicks() + ")");

		_protectEndTime = protect ? GameTimeController.getGameTicks() + proTime * GameTimeController.TICKS_PER_SECOND : 0;
	}

	public long getProtection()
	{
		return _protectEndTime;
	}

	/**
	 * Set protection from agro mobs when getting up from fake death, according settings.
	 */
	public void setRecentFakeDeath(boolean protect)
	{
		_recentFakeDeathEndTime = protect ? GameTimeController.getGameTicks() + Config.PLAYER_FAKEDEATH_UP_PROTECTION * GameTimeController.TICKS_PER_SECOND : 0;
	}

	public boolean isRecentFakeDeath()
	{
		return _recentFakeDeathEndTime > GameTimeController.getGameTicks();
	}

	/**
	 * Get the client owner of this char.<BR><BR>
	 */
	public L2GameClient getClient()
	{
		return _client;
	}

	/**
	 * Set the active connection with the client.<BR><BR>
	 */
	public void setClient(L2GameClient client)
	{
		_client = client;
	}

	public Point3D getCurrentSkillWorldPosition()
	{
		SkillUsageRequest currentSkill = getCurrentSkill();

		return currentSkill == null ? null : currentSkill.getSkillWorldPosition();
	}

	public boolean canBeTargetedByAtSiege(L2Player player)
	{
		Siege siege = SiegeManager.getInstance().getSiege(this);
		if (siege != null && siege.getIsInProgress())
		{
			L2Clan selfClan = getClan();
			L2Clan oppClan = player.getClan();
			if (selfClan != null && oppClan != null)
			{
				boolean self = false;
				for (L2SiegeClan clan : siege.getAttackerClans())
				{
					L2Clan cl = ClanTable.getInstance().getClan(clan.getClanId());

					if (cl == selfClan || cl.getAllyId() == getAllyId())
					{
						self = true;
						break;
					}
				}

				for (L2SiegeClan clan : siege.getDefenderClans())
				{
					L2Clan cl = ClanTable.getInstance().getClan(clan.getClanId());

					if (cl == selfClan || cl.getAllyId() == getAllyId())
					{
						self = true;
						break;
					}
				}

				boolean opp = false;
				for (L2SiegeClan clan : siege.getAttackerClans())
				{
					L2Clan cl = ClanTable.getInstance().getClan(clan.getClanId());

					if (cl == oppClan || cl.getAllyId() == player.getAllyId())
					{
						opp = true;
						break;
					}
				}

				for (L2SiegeClan clan : siege.getDefenderClans())
				{
					L2Clan cl = ClanTable.getInstance().getClan(clan.getClanId());

					if (cl == oppClan || cl.getAllyId() == player.getAllyId())
					{
						opp = true;
						break;
					}
				}

				return self && opp;
			}

			return false;
		}

		return true;
	}

	/**
	 * Manage actions when a player click on this L2Player.<BR><BR>
	 *
	 * <B><U> Actions on first click on the L2Player (Select it)</U> :</B><BR><BR>
	 * <li>Set the target of the player</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the player (display the select window)</li><BR><BR>
	 *
	 * <B><U> Actions on second click on the L2Player (Follow it/Attack it/Intercat with it)</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet MyTargetSelected to the player (display the select window)</li>
	 * <li>If this L2Player has a Private Store, notify the player AI with AI_INTENTION_INTERACT</li>
	 * <li>If this L2Player is autoAttackable, notify the player AI with AI_INTENTION_ATTACK</li><BR><BR>
	 * <li>If this L2Player is NOT autoAttackable, notify the player AI with AI_INTENTION_FOLLOW</li><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Client packet : Action, AttackRequest</li><BR><BR>
	 *
	 * @param player The player that start an action on this L2Player
	 *
	 */
	@Override
	public void onAction(L2Player player)
	{
		if (player == null)
			return;
		// Restrict interactions during restart/shutdown
		if (Shutdown.isActionDisabled(DisableType.PC_ITERACTION))
		{
			sendMessage("Player interaction disabled during restart/shutdown.");
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (GlobalRestrictions.onAction(this, player))
		{
		}

		// Check if the L2Player is confused
		if (player.isOutOfControl())
		{
			// Send a Server->Client packet ActionFailed to the player
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		// Aggression target lock effect
		if (!player.canChangeLockedTarget(this))
			return;

		// Check if the player already target this L2Player
		if (player.getTarget() != this)
		{
			// Set the target of the player
			player.setTarget(this);
		}
		else
		{
			if (player != this)
				player.sendPacket(new ValidateLocation(this));
			// Check if this L2Player has a Private Store
			if (getPrivateStoreType() != 0)
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				// Check if this L2Player is autoAttackable
				if (isAutoAttackable(player))
				{
					// Player with lvl < 21 can't attack a cursed weapon holder
					// And a cursed weapon holder  can't attack players with lvl < 21
					if ((isCursedWeaponEquipped() && player.getLevel() < 21) || (player.isCursedWeaponEquipped() && getLevel() < 21))
					{
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else
					{
						if (GeoData.getInstance().canSeeTarget(player, this))
						{
							player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
							player.onActionRequest();
						}
					}
				}
				else
				{
					// This Action Failed packet avoids player getting stuck when clicking three or more times
					player.sendPacket(ActionFailed.STATIC_PACKET);

					if (GeoData.getInstance().canSeeTarget(player, this))
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
				}
			}
		}
	}

	@Override
	public void onActionShift(L2Player gm)
	{
		gm.sendPacket(ActionFailed.STATIC_PACKET);
		if (gm.isGM())
		{
			if (this != gm.getTarget())
			{
				gm.setTarget(this);
			}
			else
			{
				AdminEditChar.gatherCharacterInfo(gm, this, "charinfo.htm");
			}
		}
	}

	/**
	 * Returns true if cp update should be done, false if not
	 * @return boolean
	 */
	private boolean needCpUpdate(int barPixels)
	{
		double currentCp = getStatus().getCurrentCp();

		if (currentCp <= 1.0 || getMaxCp() < barPixels)
			return true;

		if (currentCp <= _cpUpdateDecCheck || currentCp >= _cpUpdateIncCheck)
		{
			if (currentCp == getMaxCp())
			{
				_cpUpdateIncCheck = currentCp + 1;
				_cpUpdateDecCheck = currentCp - _cpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentCp / _cpUpdateInterval;
				int intMulti = (int) doubleMulti;

				_cpUpdateDecCheck = _cpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_cpUpdateIncCheck = _cpUpdateDecCheck + _cpUpdateInterval;
			}

			return true;
		}

		return false;
	}

	/**
	 * Returns true if mp update should be done, false if not
	 * @return boolean
	 */
	private boolean needMpUpdate(int barPixels)
	{
		double currentMp = getStatus().getCurrentMp();

		if (currentMp <= 1.0 || getMaxMp() < barPixels)
			return true;

		if (currentMp <= _mpUpdateDecCheck || currentMp >= _mpUpdateIncCheck)
		{
			if (currentMp == getMaxMp())
			{
				_mpUpdateIncCheck = currentMp + 1;
				_mpUpdateDecCheck = currentMp - _mpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentMp / _mpUpdateInterval;
				int intMulti = (int) doubleMulti;

				_mpUpdateDecCheck = _mpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_mpUpdateIncCheck = _mpUpdateDecCheck + _mpUpdateInterval;
			}

			return true;
		}

		return false;
	}

	/**
	 * Send packet StatusUpdate with current HP,MP and CP to the L2Player and only current HP, MP and Level to all other L2Player of the Party.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send the Server->Client packet StatusUpdate with current HP, MP and CP to this L2Player </li><BR>
	 * <li>Send the Server->Client packet PartySmallWindowUpdate with current HP, MP and Level to all other L2Player of the Party </li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND current HP and MP to all L2Player of the _statusListener</B></FONT><BR><BR>
	 *
	 */
	@Override
	public final void broadcastStatusUpdateImpl()
	{
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, (int)getStatus().getCurrentHp());
		su.addAttribute(StatusUpdate.CUR_MP, (int)getStatus().getCurrentMp());
		su.addAttribute(StatusUpdate.CUR_CP, (int)getStatus().getCurrentCp());
		sendPacket(su);

		final boolean needCpUpdate = needCpUpdate(352);
		final boolean needHpUpdate = needHpUpdate(352);
		if (isInParty() && (needCpUpdate || needHpUpdate || needMpUpdate(352)))
			getParty().broadcastToPartyMembers(this, new PartySmallWindowUpdate(this));

		if (getPlayerOlympiad().isInOlympiadMode() && getPlayerOlympiad().isOlympiadStart() && (needCpUpdate || needHpUpdate))
		{
			Collection<L2Player> players = getKnownList().getKnownPlayers().values();
			if (!players.isEmpty())
			{
				ExOlympiadUserInfo olyInfo = new ExOlympiadUserInfo(this, 2);
				for (L2Player player : players)
					if (player != null && player.getPlayerOlympiad().isInOlympiadMode() &&
							player.getPlayerOlympiad().getOlympiadGameId() == getPlayerOlympiad().getOlympiadGameId())
						player.sendPacket(olyInfo);
			}

			players = Olympiad.getInstance().getSpectators(getPlayerOlympiad().getOlympiadGameId());
			if(players != null && !players.isEmpty())
			{
				ExOlympiadUserInfo olyInfo = new ExOlympiadUserInfo(this, getPlayerOlympiad().getOlympiadSide());
				for (L2Player spectator : players)
					if (spectator != null)
						spectator.sendPacket(olyInfo);
			}
		}

		if (getPlayerDuel().isInDuel() && (needCpUpdate || needHpUpdate))
			DuelService.getInstance().broadcastToOppositTeam(this, new ExDuelUpdateUserInfo(this));
	}

	@Override
	public void updateEffectIconsImpl()
	{
		final EffectInfoPacketList list = new EffectInfoPacketList(this);

		sendPacket(new MagicEffectIcons(list));

		if (isInParty())
			getParty().broadcastToPartyMembers(this, new PartySpelled(list));
	}

	/**
	 * Send a Server->Client packet UserInfo to this L2Player and CharInfo to all L2Player in its _knownPlayers.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Others L2Player in the detection area of the L2Player are identified in <B>_knownPlayers</B>.
	 * In order to inform other players of this L2Player state modifications, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet UserInfo to this L2Player (Public and Private Data)</li>
	 * <li>Send a Server->Client packet CharInfo to all L2Player in _knownPlayers of the L2Player (Public data only)</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : DON'T SEND UserInfo packet to other players instead of CharInfo packet.
	 * Indeed, UserInfo packet contains PRIVATE DATA as MaxHP, STR, DEX...</B></FONT><BR><BR>
	 *
	 */
	public final void broadcastUserInfo()
	{
		broadcastFullInfo();
	}

	public final void broadcastTitleInfo()
	{
		// Send a Server->Client packet UserInfo to this L2Player
		sendPacket(new UserInfo(this));

		// Send a Server->Client packet NicknameChanged to all L2Player in _KnownPlayers of the L2Player
		if (_log.isDebugEnabled())
			_log.debug("players to notify:" + getKnownList().getKnownPlayers().size() + " packet: [S] cc NicknameChanged");

		Broadcast.toKnownPlayers(this, new NicknameChanged(this));
	}

	/**
	 * Return the Alliance Identifier of the L2Player.<BR><BR>
	 */
	public int getAllyId()
	{
		return (_clan == null) ? 0 : _clan.getAllyId();
	}

	public int getAllyCrestId()
	{
		if (getClanId() == 0)
		{
			return 0;
		}
		if (getClan().getAllyId() == 0)
		{
			return 0;
		}
		return getClan().getAllyCrestId();
	}

	public void queryGameGuard()
	{
		getClient().setGameGuardOk(false);
		sendPacket(GameGuardQuery.STATIC_PACKET);
		if (Config.GAMEGUARD_ENFORCE)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new GameGuardCheck(), 30 * 1000);
		}
	}

	class GameGuardCheck implements Runnable
	{

		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
			L2GameClient client = getClient();
			if (client != null && !client.isAuthedGG() && isOnline() == 1)
			{
				//GmListTable.broadcastMessageToGMs("Client "+client+" failed to reply GameGuard query and is being kicked!");
				_log.info("Client " + client + " failed to reply GameGuard query and is being kicked!");
				new Disconnection(client, L2Player.this).defaultSequence(false);
			}
		}
	}

	/**
	 * Send a Server->Client packet to the L2Player.<BR>
	 * <BR>
	 */
	@Override
	public void sendPacket(L2GameServerPacket packet)
	{
		final L2GameClient client = _client;
		if (client != null)
			client.sendPacket(packet);
	}

	/**
	 * Sends a SystemMessage without any parameter added. No instancing at all!
	 */
	@Override
	public void sendPacket(SystemMessageId sm)
	{
		sendPacket(sm.getSystemMessage());
	}

	@Override
	public void sendPacket(StaticPacket packet)
	{
		sendPacket((L2GameServerPacket) packet);
	}

	@Override
	public void sendMessage(String message)
	{
		sendPacket(SystemMessage.sendString(message));
	}
	
	public final void sendCreatureMessage(SystemChatChannelId type, String sender, String message)
	{
		sendPacket(new CreatureSay(0, type, sender, message));
	}

	/**
	 * Manage Interact Task with another L2Player.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the private store is a STORE_PRIVATE_SELL, send a Server->Client PrivateBuyListSell packet to the L2Player</li>
	 * <li>If the private store is a STORE_PRIVATE_BUY, send a Server->Client PrivateBuyListBuy packet to the L2Player</li>
	 * <li>If the private store is a STORE_PRIVATE_MANUFACTURE, send a Server->Client RecipeShopSellList packet to the L2Player</li><BR><BR>
	 *
	 * @param target The L2Character targeted
	 *
	 */
	public void doInteract(L2Character target)
	{
		if (target instanceof L2Player)
		{
			L2Player temp = (L2Player) target;
			sendPacket(ActionFailed.STATIC_PACKET);

			if (temp.getPrivateStoreType() == STORE_PRIVATE_SELL || temp.getPrivateStoreType() == STORE_PRIVATE_PACKAGE_SELL)
				sendPacket(new PrivateStoreListSell(this, temp));
			else if (temp.getPrivateStoreType() == STORE_PRIVATE_BUY)
				sendPacket(new PrivateStoreListBuy(this, temp));
			else if (temp.getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE)
				sendPacket(new RecipeShopSellList(this, temp));
		}
		else
		{
			// _interactTarget=null should never happen but one never knows ^^;
			if (target != null)
				target.onAction(this);
		}
	}

	/**
	 * Manage AutoLoot Task.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a System Message to the L2Player : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2</li>
	 * <li>Add the Item to the L2Player inventory</li>
	 * <li>Send a Server->Client packet InventoryUpdate to this L2Player with NewItem (use a new slot) or ModifiedItem (increase amount)</li>
	 * <li>Send a Server->Client packet StatusUpdate to this L2Player with current weight</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If a Party is in progress, distribute Items between party members</B></FONT><BR><BR>
	 *
	 * @param target The L2ItemInstance dropped
	 *
	 */
	public void doAutoLoot(L2Attackable target, L2Attackable.RewardItem item)
	{
		if (!tryAutoLoot(target, item.getItemId()))
			target.dropItem(this, item);
		else if (isInParty())
			getParty().distributeItem(this, item, false, target);
		else if (item.getItemId() == PcInventory.ADENA_ID)
			addAdena("Loot", item.getCount(), target, true);
		else
			addItem("Loot", item.getItemId(), item.getCount(), target, true, false);
	}

	private boolean tryAutoLoot(L2Attackable target, int itemId)
	{
		if (target.isFlying())
			return true;

		if (ItemTable.isAdenaLikeItem(itemId))
			return Config.ALT_AUTO_LOOT_ADENA;

		if (ItemTable.getInstance().getTemplate(itemId).getItemType() == L2EtcItemType.HERB)
			return Config.ALT_AUTO_LOOT_HERBS;

		if (target.isRaid())
			return Config.ALT_AUTO_LOOT_RAID;

		return Config.ALT_AUTO_LOOT;
	}

	/**
	 * Manage Pickup Task.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet StopMove to this L2Player </li>
	 * <li>Remove the L2ItemInstance from the world and send server->client GetItem packets </li>
	 * <li>Send a System Message to the L2Player : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2</li>
	 * <li>Add the Item to the L2Player inventory</li>
	 * <li>Send a Server->Client packet InventoryUpdate to this L2Player with NewItem (use a new slot) or ModifiedItem (increase amount)</li>
	 * <li>Send a Server->Client packet StatusUpdate to this L2Player with current weight</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If a Party is in progress, distribute Items between party members</B></FONT><BR><BR>
	 *
	 * @param object The L2ItemInstance to pick up
	 *
	 */
	protected void doPickupItem(L2Object object)
	{
		if (isAlikeDead() || isFakeDeath())
			return;

		// Set the AI Intention to AI_INTENTION_IDLE
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

		// Check if the L2Object to pick up is a L2ItemInstance
		if (!(object instanceof L2ItemInstance))
		{
			// Dont try to pickup anything that is not an item :)
			_log.warn("trying to pickup wrong target." + getTarget());
			return;
		}

		L2ItemInstance target = (L2ItemInstance) object;

		// Send a Server->Client packet ActionFailed to this L2Player
		sendPacket(ActionFailed.STATIC_PACKET);

		// Send a Server->Client packet StopMove to this L2Player
		StopMove sm = new StopMove(getObjectId(), getX(), getY(), getZ(), getHeading());
		if (_log.isDebugEnabled())
			_log.debug("pickup pos: " + target.getX() + " " + target.getY() + " " + target.getZ());
		sendPacket(sm);

		synchronized (target)
		{
			// Check if the target to pick up is visible
			if (!target.isVisible())
			{
				// Send a Server->Client packet ActionFailed to this L2Player
				sendPacket(ActionFailed.STATIC_PACKET);
				return;

			}

			if (((isInParty() && getParty().getLootDistribution() == L2Party.ITEM_LOOTER) || !isInParty()) && !getInventory().validateCapacity(target))
			{
				sendPacket(SystemMessageId.SLOTS_FULL);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (isInvul() && !isGM())
			{
				SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
				smsg.addItemName(target);
				sendPacket(smsg);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (getActiveTradeList() != null)
			{
				sendPacket(SystemMessageId.CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (target.getOwnerId() != 0 && target.getOwnerId() != getObjectId() && !isInLooterParty(target.getOwnerId()))
			{
				sendPacket(ActionFailed.STATIC_PACKET);

				if (target.getItemId() == PcInventory.ADENA_ID)
				{
					SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA);
					smsg.addItemNumber(target.getCount());
					sendPacket(smsg);
				}
				else if (target.getCount() > 1)
				{
					SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S);
					smsg.addItemName(target);
					smsg.addItemNumber(target.getCount());
					sendPacket(smsg);
				}
				else
				{
					SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
					smsg.addItemName(target);
					sendPacket(smsg);
				}

				return;
			}

			// Cursed Weapons
			if (CursedWeaponsService.getInstance().isCursed(target.getItemId()) && isCursedWeaponEquipped())
			{
				ItemTable.getInstance().destroyItem("Pickup CW", target, this, null);
				CursedWeapon cw = CursedWeaponsService.getInstance().getCursedWeapon(getCursedWeaponEquippedId());
				cw.increaseKills(cw.getStageKills());
				return;
			}

			// You can pickup only 1 combat flag
			if (FortSiegeManager.getInstance().isCombat(target.getItemId()))
			{
				if (!FortSiegeManager.getInstance().checkIfCanPickup(this))
					return;
			}

			if (target.getItemLootShedule() != null && (target.getOwnerId() == getObjectId() || isInLooterParty(target.getOwnerId())))
				target.resetOwnerTimer();

			// Remove the L2ItemInstance from the world and send server->client GetItem packets
			target.pickupMe(this);
		}
		// Auto use herbs - pick up
		if (target.getItemType() == L2EtcItemType.HERB)
		{
			ItemHandler.getInstance().useItem(target.getItemId(), this, target);
		}
		// Cursed Weapons are not distributed
		else if (CursedWeaponsService.getInstance().isCursed(target.getItemId()))
		{
			addItem("Pickup", target, null, true);
		}
		else if (FortSiegeManager.getInstance().isCombat(target.getItemId()))
		{
			addItem("Pickup", target, null, true);
		}
		else
		{
			// If item is instance of L2ArmorType or L2WeaponType broadcast an "Attention" system message
			if (target.getItemType() instanceof L2ArmorType || target.getItemType() instanceof L2WeaponType)
			{
				if (target.getEnchantLevel() > 0)
				{
					SystemMessage msg = new SystemMessage(SystemMessageId.ANNOUNCEMENT_C1_PICKED_UP_S2_S3);
					msg.addPcName(this);
					msg.addNumber(target.getEnchantLevel());
					msg.addItemName(target);
					broadcastPacket(msg, 1400);
				}
				else
				{
					SystemMessage msg = new SystemMessage(SystemMessageId.ANNOUNCEMENT_C1_PICKED_UP_S2);
					msg.addPcName(this);
					msg.addItemName(target);
					broadcastPacket(msg, 1400);
				}
			}

			// Check if a Party is in progress
			if (isInParty())
				getParty().distributeItem(this, target);
			// Target is adena
			else if (target.getItemId() == PcInventory.ADENA_ID && getInventory().getAdenaInstance() != null)
			{
				addAdena("Pickup", target.getCount(), null, true);
				ItemTable.getInstance().destroyItem("Pickup", target, this, null);
			}
			// Target is regular item
			else
				addItem("Pickup", target, null, true);
		}
	}

	/**
	 * Set a target.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the L2Player from the _statusListener of the old target if it was a L2Character </li>
	 * <li>Add the L2Player to the _statusListener of the new target if it's a L2Character </li>
	 * <li>Target the new L2Object (add the target to the L2Player _target, _knownObject and L2Player to _KnownObject of the L2Object)</li><BR><BR>
	 *
	 * @param newTarget The L2Object to target
	 *
	 */
	@Override
	public void setTarget(L2Object newTarget)
	{
		if (newTarget != null)
		{
			if (this != newTarget && newTarget instanceof L2Character)
				sendPacket(new ValidateLocation((L2Character)newTarget));

			if (!isGM())
			{
				if (newTarget instanceof L2FestivalMonsterInstance && !isFestivalParticipant())
					return;

				if (isInParty() && getParty().isInDimensionalRift())
				{
					byte riftType = getParty().getDimensionalRift().getType();
					byte riftRoom = getParty().getDimensionalRift().getCurrentRoom();

					if (!DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(newTarget))
						return;
				}
			}

			if (!(newTarget instanceof L2Player) || !isInParty() || getParty() != ((L2Player)newTarget).getParty())
			{
				if (!newTarget.isVisible())
					return;

				if (Math.abs(newTarget.getZ() - getZ()) > 500)
					return;
			}
		}

		super.setTarget(newTarget);
	}

	@Override
	protected void refreshTarget(L2Object newTarget)
	{
		final L2Object oldTarget = getTarget();

		if (oldTarget instanceof L2Character)
			((L2Character)oldTarget).getStatus().removeStatusListener(this);

		if (newTarget instanceof L2Character)
			((L2Character)newTarget).getStatus().addStatusListener(this);

		if (newTarget != null)
		{
			broadcastPacket(new TargetSelected(this, newTarget));

			sendPacket(new MyTargetSelected(this, newTarget));
		}
		else if (oldTarget != null)
		{
			broadcastPacket(new TargetUnselected(this));
		}

		super.refreshTarget(newTarget);

		saveLastTarget(oldTarget != null ? oldTarget.getObjectId() : 0);
	}

	/**
	 * Return the active weapon instance (always equipped in the right hand).<BR><BR>
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
	}

	/**
	 * Return the active weapon item (always equipped in the right hand).<BR><BR>
	 */
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		L2ItemInstance weapon = getActiveWeaponInstance();

		if (weapon == null)
			return getFistsWeaponItem();

		return (L2Weapon)weapon.getItem();
	}

	public L2ItemInstance getChestArmorInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
	}

	public L2ItemInstance getLegsArmorInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
	}

	public L2Armor getActiveChestArmorItem()
	{
		L2ItemInstance armor = getChestArmorInstance();

		if (armor == null)
			return null;

		return (L2Armor) armor.getItem();
	}

	public L2Armor getActiveLegsArmorItem()
	{
		L2ItemInstance legs = getLegsArmorInstance();

		if (legs == null)
			return null;

		return (L2Armor) legs.getItem();
	}

	public boolean isWearingHeavyArmor()
	{
		if ((getChestArmorInstance() != null) && getLegsArmorInstance() != null)
		{
			L2ItemInstance legs = getLegsArmorInstance();
			L2ItemInstance armor = getChestArmorInstance();
			if (legs.getItemType() == L2ArmorType.HEAVY && (armor.getItemType() == L2ArmorType.HEAVY))
				return true;
		}
		if (getChestArmorInstance() != null)
		{
			L2ItemInstance armor = getChestArmorInstance();

			if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR
					&& armor.getItemType() == L2ArmorType.HEAVY)
				return true;
		}

		return false;
	}

	public boolean isWearingLightArmor()
	{
		if ((getChestArmorInstance() != null) && getLegsArmorInstance() != null)
		{
			L2ItemInstance legs = getLegsArmorInstance();
			L2ItemInstance armor = getChestArmorInstance();
			if (legs.getItemType() == L2ArmorType.LIGHT && (armor.getItemType() == L2ArmorType.LIGHT))
				return true;
		}
		if (getChestArmorInstance() != null)
		{
			L2ItemInstance armor = getChestArmorInstance();

			if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR
					&& armor.getItemType() == L2ArmorType.LIGHT)
				return true;
		}

		return false;
	}

	public boolean isWearingMagicArmor()
	{
		if ((getChestArmorInstance() != null) && getLegsArmorInstance() != null)
		{
			L2ItemInstance legs = getLegsArmorInstance();
			L2ItemInstance armor = getChestArmorInstance();
			if (legs.getItemType() == L2ArmorType.MAGIC && (armor.getItemType() == L2ArmorType.MAGIC))
				return true;
		}
		if (getChestArmorInstance() != null)
		{
			L2ItemInstance armor = getChestArmorInstance();

			if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR
					&& armor.getItemType() == L2ArmorType.MAGIC)
				return true;
		}

		return false;
	}

	public boolean isWearingFormalWear()
	{
		return _IsWearingFormalWear;
	}

	public void setIsWearingFormalWear(boolean value)
	{
		_IsWearingFormalWear = value;
	}

	/**
	 * Return the secondary weapon instance (always equipped in the left hand).<BR><BR>
	 */
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
	}

	/**
	 * Return the secondary weapon item (always equipped in the left hand) or the fists weapon.<BR><BR>
	 */
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		L2ItemInstance weapon = getSecondaryWeaponInstance();

		if (weapon == null)
			return getFistsWeaponItem();

		L2Item item = weapon.getItem();

		if (item instanceof L2Weapon)
			return (L2Weapon)item;

		return null;
	}

	/**
	 * Kill the L2Character, Apply Death Penalty, Manage gain/loss Karma and Item Drop.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Reduce the Experience of the L2Player in function of the calculated Death Penalty </li>
	 * <li>If necessary, unsummon the Pet of the killed L2Player </li>
	 * <li>Manage Karma gain for attacker and Karma loss for the killed L2Player </li>
	 * <li>If the killed L2Player has Karma, manage Drop Item</li>
	 * <li>Kill the L2Player </li><BR><BR>
	 *
	 *
	 * @param killer The L2Character who attacks
	 *
	 */
	@Override
	public boolean doDie(L2Character killer)
	{
		// is the dying in duel? if so, change his duel state to dead
		if (getPlayerDuel().isInDuel()) // pets can die as usual
		{
			disableAllSkills();
			getStatus().setCurrentHp(1);
			getStatus().stopHpMpRegeneration();
			killer.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			killer.sendPacket(ActionFailed.STATIC_PACKET);

			// let the DuelService know of his defeat
			DuelService.getInstance().onPlayerDefeat(this);
			return false;
		}

		if (getPlayerOlympiad().isInOlympiadMode())
		{
			getStatus().stopHpMpRegeneration();
			setIsDead(true);
			setIsPendingRevive(true);
			if (getPet() != null)
				getPet().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
			return false;
		}

		/* Since L2Character.doDie() calls stopAllEffects(), which includes
		 * setting charm of curage and other blessings as false, this stores value
		 * before calling superclass method
		 */
		boolean charmOfCourage = getCharmOfCourage();

		// Kill the L2Player
		if (!super.doDie(killer))
			return false;

		if (isMounted())
			stopFeed();

		synchronized (this)
		{
			if (isFakeDeath())
				stopFakeDeath(true);
		}

		// Clear resurrect xp calculation
		setExpBeforeDeath(0);

		// Issues drop of Cursed Weapon.
		if (isCursedWeaponEquipped())
		{
			CursedWeaponsService.getInstance().drop(_cursedWeaponEquippedId, killer);
		}
		else if (isCombatFlagEquipped())
		{
			if (TerritoryWarManager.getInstance().isTWInProgress())
				TerritoryWarManager.getInstance().dropCombatFlag(this, true);
			else
				FortSiegeManager.getInstance().dropCombatFlag(this);
		}

		Castle castle = null;
		if (getClan() != null)
		{
			castle = CastleManager.getInstance().getCastleByOwner(getClan());
			if (castle != null)
				castle.destroyClanGate();
		}

		if (killer != null)
		{
			L2Player pk = killer.getActingPlayer();

			boolean bothWayClanWarKill = false;
			boolean clanWarKill = false;
			boolean playerKill = false;

			GlobalRestrictions.playerKilled(killer, this);

			if (pk != null)
			{
				if (pk.getClan() != null && getClan() != null && !isAcademyMember() && !pk.isAcademyMember())
				{
					if ((_clan.isAtWarWith(pk.getClanId()) && pk.getClan().isAtWarWith(getClanId())) || (isInSiege() && pk.isInSiege()))
					{
						bothWayClanWarKill = true;
						clanWarKill = true;
					}
					else if (_clan.isAtWarWith(pk.getClanId()))
						clanWarKill = true;
				}
				playerKill = true;
			}

			boolean srcInPvP = isInsideZone(L2Zone.FLAG_PVP) && !isInSiege();

			if (bothWayClanWarKill && pk != null)
			{
				if (AntiFeedManager.getInstance().check(killer, this))
				{
					// When your reputation score is 0 or below, the other clan cannot acquire any reputation points
					if (getClan().getReputationScore() > 0)
						pk.getClan().setReputationScore(pk.getClan().getReputationScore() + Config.REPUTATION_SCORE_PER_KILL, true);
					// When the opposing sides reputation score is 0 or below, your clans reputation score does not decrease
					if (pk.getClan().getReputationScore() > 0)
						_clan.setReputationScore(_clan.getReputationScore() - Config.REPUTATION_SCORE_PER_KILL, true);	
				}
			}

			if (!srcInPvP)
			{
				if (pk == null || !pk.isCursedWeaponEquipped())
				{
					onDieDropItem(killer); // Check if any item should be dropped

					if (!srcInPvP)
					{
						if (Config.ALT_GAME_DELEVEL)
						{
							// Reduce the Experience of the L2Player in function of the calculated Death Penalty
							// NOTE: deathPenalty +- Exp will update karma
							// Penalty is lower if the player is at war with the pk (war has to be declared)
							if (getSkillLevel(L2Skill.SKILL_LUCKY) < 0 || getStat().getLevel() > 9)
								deathPenalty(clanWarKill, playerKill, charmOfCourage, killer instanceof L2DefenderInstance);
						}
						else
						{
							if (!(isInsideZone(L2Zone.FLAG_PVP) && !isInsideZone(L2Zone.FLAG_PVP)) || pk == null)
								onDieUpdateKarma(); // Update karma if delevel is not allowed
						}
					}
				}
				if (pk != null)
				{
					if (Config.ALT_ANNOUNCE_PK)
					{
						String announcetext = "";
						// Build announce text
						if (getPvpFlag() == 0)
							announcetext = pk.getName() + " has slaughtered " + getName();
						else
							announcetext = pk.getName() + " has defeated " + getName();

						// Announce to player
						if (Config.ALT_ANNOUNCE_PK_NORMAL_MESSAGE)
							Announcements.getInstance().announceToPlayers(announcetext);
						else
							Announcements.getInstance().announceToAll(announcetext);
					}
				}
			}
			else if (pk != null && Config.ALT_ANNOUNCE_PK)
			{
				if (Config.ALT_ANNOUNCE_PK_NORMAL_MESSAGE)
					Announcements.getInstance().announceToPlayers(pk.getName() + " has defeated " + getName());
				else
					Announcements.getInstance().announceToAll(pk.getName() + " has defeated " + getName());
			}
		}

		// Force Charges
		clearCharges(); // Empty charges

		//updatePvPFlag(0); // Clear the pvp flag
		// Pet shouldn't get unsummoned after masters death.
		// Unsummon the Pet
		//if (getPet() != null) getPet().unSummon(this);

		// Unsummon Cubics
		if (!_cubics.isEmpty())
		{
			for (L2CubicInstance cubic : _cubics.values())
			{
				cubic.stopAction();
				cubic.cancelDisappear();
			}
			_cubics.clear();
		}

		if (_fusionSkill != null)
			abortCast();

		for (L2Character character : getKnownList().getKnownCharacters())
			if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
				character.abortCast();

		if (isInParty() && getParty().isInDimensionalRift())
		{
			getParty().getDimensionalRift().memberDead(this);
		}

		// Calculate death penalty buff
		calculateDeathPenaltyBuffLevel(killer);

		QuestState qs = getQuestState("_255_Tutorial");
		if (qs != null)
			qs.getQuest().notifyEvent("CE30", null, this);
		
		AntiFeedManager.getInstance().setLastDeathTime(getObjectId());

		return true;
	}

	/** UnEnquip on skills with disarm effect **/
	public void onDisarm(L2Player target)
	{
		target.getInventory().unEquipItemInBodySlotAndRecord(14);
	}

	private void onDieDropItem(L2Character killer)
	{
		if (killer == null)
			return;

		L2Player pk = killer.getActingPlayer();
		if (pk != null && getKarma() <= 0 && pk.getClan() != null && getClan() != null
				&& (pk.getClan().isAtWarWith(getClanId())
						//|| this.getClan().isAtWarWith(((L2Player)killer).getClanId())
				))
			return;

		if ((!isInsideZone(L2Zone.FLAG_PVP) || pk == null) && (!isGM() || Config.KARMA_DROP_GM))
		{
			boolean isKarmaDrop = false;
			boolean isKillerNpc = (killer instanceof L2Npc);
			int pkLimit = Config.KARMA_PK_LIMIT;

			int dropEquip = 0;
			int dropEquipWeapon = 0;
			int dropItem = 0;
			int dropLimit = 0;
			int dropPercent = 0;

			if (getKarma() > 0 && getPkKills() >= pkLimit)
			{
				isKarmaDrop = true;
				dropPercent = Config.KARMA_RATE_DROP;
				dropEquip = Config.KARMA_RATE_DROP_EQUIP;
				dropEquipWeapon = Config.KARMA_RATE_DROP_EQUIP_WEAPON;
				dropItem = Config.KARMA_RATE_DROP_ITEM;
				dropLimit = Config.KARMA_DROP_LIMIT;
			}
			else if (isKillerNpc && getLevel() > 4 && !isFestivalParticipant())
			{
				dropPercent = Config.PLAYER_RATE_DROP;
				dropEquip = Config.PLAYER_RATE_DROP_EQUIP;
				dropEquipWeapon = Config.PLAYER_RATE_DROP_EQUIP_WEAPON;
				dropItem = Config.PLAYER_RATE_DROP_ITEM;
				dropLimit = Config.PLAYER_DROP_LIMIT;
			}

			if (dropPercent > 0 && Rnd.get(100) < dropPercent)
			{
				int dropCount = 0;
				int itemDropPercent = 0;
				for (L2ItemInstance itemDrop : getInventory().getItems())
				{
					// Don't drop
					if (!itemDrop.isDropable() || itemDrop.getItemId() == PcInventory.ADENA_ID
							// Dont drop Shadow Items
							|| itemDrop.isShadowItem()
							// Dont drop Time Limited Items
							|| itemDrop.isTimeLimitedItem()
							// Quest Items
							|| itemDrop.getItem().getType2() == L2Item.TYPE2_QUEST
							// Control Item of active pet
							|| getPet() != null && getPet().getControlItemId() == itemDrop.getItemId()
							// Item listed in the non droppable item list
							|| Arrays.binarySearch(Config.KARMA_LIST_NONDROPPABLE_ITEMS, itemDrop.getItemId()) >= 0
							// Item listed in the non droppable pet item list
							|| Arrays.binarySearch(Config.KARMA_LIST_NONDROPPABLE_PET_ITEMS, itemDrop.getItemId()) >= 0
					)
						continue;

					if (itemDrop.isEquipped())
						// Set proper chance according to Item type of equipped Item
						itemDropPercent = itemDrop.getItem().getType2() == L2Item.TYPE2_WEAPON ? dropEquipWeapon : dropEquip;
					else
						itemDropPercent = dropItem; // Item in inventory

					// NOTE: Each time an item is dropped, the chance of another item being dropped gets lesser (dropCount * 2)
					if (Rnd.get(100) < itemDropPercent)
					{
						if (itemDrop.isEquipped())
						{
							getInventory().unEquipItemInSlotAndRecord(itemDrop.getLocationSlot());
							// must be sent explicitly to avoid visible garbage
							sendPacket(new UserInfo(this));
						}
						dropItem("DieDrop", itemDrop, killer, true);

						if (isKarmaDrop)
							_log.info(getName() + " has karma and dropped " + itemDrop);
						else
							_log.info(getName() + " dropped " + itemDrop);

						if (++dropCount >= dropLimit)
							break;
					}
				}
			}
			// Player can drop adena against other player
			if (Config.ALT_PLAYER_CAN_DROP_ADENA && !isKillerNpc && Config.PLAYER_RATE_DROP_ADENA > 0 && 100 >= Config.PLAYER_RATE_DROP_ADENA
					&& !(killer instanceof L2Player && ((L2Player) killer).isGM()))
			{
				L2ItemInstance itemDrop = getInventory().getAdenaInstance();
				long iCount = getInventory().getAdena();
				// Adena count depends on config
				iCount = iCount * Config.PLAYER_RATE_DROP_ADENA / 100;
				// Drop only adena this time
				if (itemDrop != null && itemDrop.getItemId() == PcInventory.ADENA_ID) // Adena
				{
					dropItem("DieDrop", itemDrop.getObjectId(), iCount, getPosition().getX() + Rnd.get(50) - 25, getPosition().getY() + Rnd.get(50) - 25,
							getPosition().getZ() + 20, killer, true);
				}
			}
		}
	}

	private void onDieUpdateKarma()
	{
		// Karma lose for server that does not allow delevel
		if (getKarma() > 0)
		{
			// This formula seems to work relatively well:
			// baseKarma * thisLVL * (thisLVL/100)
			// Calculate the new Karma of the attacker : newKarma = baseKarma*pkCountMulti*lvlDiffMulti
			double karmaLost = Config.KARMA_LOST_BASE;
			karmaLost *= getLevel(); // Multiply by char lvl
			karmaLost *= (getLevel() / 100.0); // Divide by 0.charLVL
			karmaLost = Math.round(karmaLost);
			if (karmaLost < 0)
				karmaLost = 1;

			// Decrease Karma of the L2Player and Send it a Server->Client StatusUpdate packet with Karma and PvP Flag if necessary
			setKarma(getKarma() - (int) karmaLost);
		}
	}

	public void onKillUpdatePvPKarma(L2Character target)
	{
		if (target == null)
			return;
		if (!(target instanceof L2Playable))
			return;

		L2Player targetPlayer = target.getActingPlayer();

		if (targetPlayer == null)
			return; // Target player is null
		if (targetPlayer == this)
			return; // Target player is self

		if (isCursedWeaponEquipped())
		{
			CursedWeaponsService.getInstance().increaseKills(_cursedWeaponEquippedId);
			// Custom message for time left
			// CursedWeapon cw = CursedWeaponsService.getInstance().getCursedWeapon(_cursedWeaponEquippedId);
			// SystemMessage msg = new SystemMessage(SystemMessageId.THERE_IS_S1_HOUR_AND_S2_MINUTE_LEFT_OF_THE_FIXED_USAGE_TIME);
			// int timeLeftInHours = (int)(((cw.getTimeLeft()/60000)/60));
			// msg.addItemName(_cursedWeaponEquippedId);
			// msg.addNumber(timeLeftInHours);
			// sendPacket(msg);
			return;
		}

		// If in duel and you kill (only can kill l2summon), do nothing
		if (getPlayerDuel().isInDuel() && targetPlayer.getPlayerDuel().isInDuel())
			return;

		// If in Arena, do nothing
		if (isInsideZone(L2Zone.FLAG_PVP))
			return;

		// Check if it's pvp
		if ((checkIfPvP(target) && //  Can pvp and
				targetPlayer.getPvpFlag() != 0 // Target player has pvp flag set
		)
		|| // or
		(isInsideZone(L2Zone.FLAG_PVP) && // Player is inside pvp zone and
				targetPlayer.isInsideZone(L2Zone.FLAG_PVP) // Target player is inside pvp zone
		))
		{
			if (target instanceof L2Player)
				increasePvpKills(target);
		}
		else // Target player doesn't have pvp flag set
		{
			// Check about wars
			boolean clanWarKill = (targetPlayer.getClan() != null && getClan() != null && !isAcademyMember() && !(targetPlayer.isAcademyMember())
					&& _clan.isAtWarWith(targetPlayer.getClanId()) && targetPlayer.getClan().isAtWarWith(_clan.getClanId()));
			if (clanWarKill)
			{
				// 'Both way war' -> 'PvP Kill'
				if (target instanceof L2Player)
					increasePvpKills(target);
				return;
			}

			// 'No war' or 'One way war' -> 'Normal PK'
			if (targetPlayer.getKarma() > 0) // Target player has karma
			{
				if (Config.KARMA_AWARD_PK_KILL)
				{
					if (target instanceof L2Player)
						increasePvpKills(target);
				}
			}
			else if (targetPlayer.getPvpFlag() == 0) // Target player doesn't have karma
			{
				if (targetPlayer instanceof L2Player)
					increasePkKillsAndKarma(targetPlayer);
				
				// Unequip adventurer items
				checkItemRestriction();
			}
		}
	}

	/**
	 * Increase the pvp kills count and send the info to the player
	 *
	 */
	private void increasePvpKills(L2Character target)
	{
		if (target instanceof L2Player
				&& AntiFeedManager.getInstance().check(this, target))
		{
			// Add karma to attacker and increase its PK counter
			setPvpKills(getPvpKills() + 1);
			
			getAppearance().updateNameTitleColor();

			// Send a Server->Client UserInfo packet to attacker with its Karma and PK Counter
			sendPacket(new UserInfo(this));	
		}
	}

	/**
	 * Increase pk count, karma and send the info to the player
	 *
	 * @param targLVL : level of the killed player
	 * @param increasePk : true if PK counter should be increased too
	 */
	private void increasePkKillsAndKarma(L2Character target)
	{
		int baseKarma = (int)(Config.KARMA_MIN_KARMA * Config.KARMA_RATE);
		int newKarma = baseKarma;
		int karmaLimit = (int)(Config.KARMA_MAX_KARMA * Config.KARMA_RATE);
		
		int targLVL = target.getLevel();

		int pkLVL = getLevel();
		int pkPKCount = getPkKills();

		int lvlDiffMulti = 0;
		int pkCountMulti = 0;

		// Check if the attacker has a PK counter greater than 0
		if (pkPKCount > 0)
			pkCountMulti = pkPKCount / 2;
		else
			pkCountMulti = 1;
		if (pkCountMulti < 1)
			pkCountMulti = 1;

		// Calculate the level difference Multiplier between attacker and killed L2Player
		if (pkLVL > targLVL)
			lvlDiffMulti = pkLVL / targLVL;
		else
			lvlDiffMulti = 1;
		if (lvlDiffMulti < 1)
			lvlDiffMulti = 1;

		// Calculate the new Karma of the attacker : newKarma = baseKarma*pkCountMulti*lvlDiffMulti
		newKarma = (int)(newKarma * pkCountMulti * lvlDiffMulti * Config.KARMA_RATE);

		// Make sure newKarma is less than karmaLimit and higher than baseKarma
		if (newKarma < baseKarma)
			newKarma = baseKarma;
		if (newKarma > karmaLimit)
			newKarma = karmaLimit;

		// Fix to prevent overflow (=> karma has a  max value of 2 147 483 647)
		if (getKarma() > (Integer.MAX_VALUE - newKarma))
			newKarma = Integer.MAX_VALUE - getKarma();

		// Add karma to attacker and increase its PK counter
		if (target instanceof L2Player && AntiFeedManager.getInstance().check(this, target))
		{
			setPkKills(getPkKills() + 1);
			getAppearance().updateNameTitleColor();
		}
		setKarma(getKarma() + newKarma);

		// Send a Server->Client UserInfo packet to attacker with its Karma and PK Counter
		sendPacket(new UserInfo(this));
	}

	public int calculateKarmaLost(long exp)
	{
		// KARMA LOSS
		// When a Player Killer gets killed by another player or a L2MonsterInstance, it loses a certain amount of Karma based on their level.
		// this (with defaults) results in a level 1 losing about ~2 karma per death, and a lvl 70 loses about 11760 karma per death...
		// You lose karma as long as you were not in a pvp zone and you did not kill urself.
		// NOTE: exp for death (if delevel is allowed) is based on the players level

		long expGained = Math.abs(exp);
		expGained /= Config.KARMA_XP_DIVIDER;

		int karmaLost = 0;
		if (expGained > Integer.MAX_VALUE)
			karmaLost = Integer.MAX_VALUE;
		else
			karmaLost = (int) expGained;

		if (karmaLost < Config.KARMA_LOST_BASE)
			karmaLost = Config.KARMA_LOST_BASE;
		if (karmaLost > getKarma())
			karmaLost = getKarma();

		return karmaLost;
	}

	private static final class PvPFlagManager extends AbstractIterativePeriodicTaskManager<L2Player>
	{
		private static final PvPFlagManager _instance = new PvPFlagManager();

		private static PvPFlagManager getInstance()
		{
			return _instance;
		}

		private PvPFlagManager()
		{
			super(1000);
		}

		@Override
		protected void callTask(L2Player task)
		{
			if (System.currentTimeMillis() > task.getPvpFlagLasts())
			{
				task.stopPvPFlag();
			}
			else if (System.currentTimeMillis() > (task.getPvpFlagLasts() - 20000))
			{
				task.updatePvPFlag(2);
			}
			else
			{
				task.updatePvPFlag(1);
			}
		}

		@Override
		protected String getCalledMethodName()
		{
			return "updatePvPFlag()";
		}
	}

	/** The PvP Flag state of the L2Player (0=White, 1=Purple) */
	private byte _pvpFlag;
	private long _pvpFlagLasts;

	/**
	 * Set the PvP Flag of the L2Player.<BR>
	 * <BR>
	 */
	private void setPvpFlag(int pvpFlag)
	{
		_pvpFlag = (byte)pvpFlag;
	}

	public byte getPvpFlag()
	{
		return _pvpFlag;
	}

	public void updatePvPFlag(int value)
	{
		if (getPvpFlag() == value)
			return;

		setPvpFlag((byte)value);

		if (getPvpFlag() == 0)
			PvPFlagManager.getInstance().stopTask(this);
		else
			PvPFlagManager.getInstance().startTask(this);

		sendPacket(new UserInfo(this));
		broadcastRelationChanged();
	}

	private void setPvpFlagLasts(long time)
	{
		_pvpFlagLasts = time;
	}

	private long getPvpFlagLasts()
	{
		return _pvpFlagLasts;
	}

	private void startPvPFlag()
	{
		updatePvPFlag(1);
	}

	private void stopPvPFlag()
	{
		updatePvPFlag(0);
	}

	public void updatePvPStatus()
	{
		if (isInsideZone(L2Zone.FLAG_PVP))
			return;

		setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_NORMAL_TIME);

		if (getPvpFlag() == 0)
			startPvPFlag();
	}

	public void updatePvPStatus(L2Character target)
	{
		L2Player player_target = target.getActingPlayer();

		if (player_target == null)
			return;

		if ((getPlayerDuel().isInDuel() && player_target.getPlayerDuel().getDuelId() == getPlayerDuel().getDuelId()))
			return;
		if ((!isInsideZone(L2Zone.FLAG_PVP) || !player_target.isInsideZone(L2Zone.FLAG_PVP)) && player_target.getKarma() == 0)
		{
			if (checkIfPvP(player_target))
				setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_PVP_TIME);
			else
				setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_NORMAL_TIME);
			if (getPvpFlag() == 0)
				startPvPFlag();
		}
	}

	/**
	 * Restore the specified % of experience this L2Player has
	 * lost and sends a Server->Client StatusUpdate packet.<BR><BR>
	 */
	public void restoreExp(double restorePercent)
	{
		if (getExpBeforeDeath() > 0)
		{
			// Restore the specified % of lost experience.
			getStat().addExp((int) Math.round((getExpBeforeDeath() - getExp()) * restorePercent / 100));
			setExpBeforeDeath(0);
		}
	}

	/**
	 * Reduce the Experience (and level if necessary) of the L2Player in function of the calculated Death Penalty.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate the Experience loss </li>
	 * <li>Set the value of _expBeforeDeath </li>
	 * <li>Set the new Experience value of the L2Player and Decrease its level if necessary </li>
	 * <li>Send a Server->Client StatusUpdate packet with its new Experience </li><BR><BR>
	 *
	 */
	public void deathPenalty(boolean atwar, boolean killed_by_pc, boolean charmOfCourage, boolean killed_by_siege_npc)
	{
		if (charmOfCourage && isInSiege())
			return;

		if ((killed_by_pc || killed_by_siege_npc)
				&& ((isInsideZone(L2Zone.FLAG_PVP) && !isInSiege()) || isInSiege()))
			return;

		// FIXME: Need Correct Penalty

		// Get the level of the L2Player
		final int lvl = getLevel();

		byte level = (byte)getLevel();

		int clan_luck = getSkillLevel(L2Skill.SKILL_CLAN_LUCK);

		double clan_luck_modificator = 1.0;

		if (!killed_by_pc)
		{
			switch (clan_luck)
			{
			case 3:
				clan_luck_modificator = 0.8;
				break;
			case 2:
				clan_luck_modificator = 0.8;
				break;
			case 1:
				clan_luck_modificator = 0.88;
				break;
			default:
				clan_luck_modificator = 1.0;
				break;
			}
		}
		else
		{
			switch (clan_luck)
			{
			case 3:
				clan_luck_modificator = 0.5;
				break;
			case 2:
				clan_luck_modificator = 0.5;
				break;
			case 1:
				clan_luck_modificator = 0.5;
				break;
			default:
				clan_luck_modificator = 1.0;
				break;
			}
		}

		// The death steal you some Exp
		double percentLost = Config.PLAYER_XP_PERCENT_LOST[getLevel()]*clan_luck_modificator;

		switch (level)
		{
		case 78:
			percentLost = (1.5*clan_luck_modificator);
			break;
		case 77:
			percentLost = (2.0*clan_luck_modificator);
			break;
		case 76:
			percentLost = (2.5*clan_luck_modificator);
			break;
		default:
			if (level < 40)
				percentLost = (7.0*clan_luck_modificator);
			else if (level >= 40 && level <= 75)
				percentLost = (4.0*clan_luck_modificator);
			break;
		}

		if (getKarma() > 0)
			percentLost *= Config.RATE_KARMA_EXP_LOST;

		if (isFestivalParticipant() || atwar)
			percentLost /= 4.0;

		// Calculate the Experience loss
		final long lostExp;

		if (lvl < Experience.MAX_LEVEL)
			lostExp = Math.round((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost / 100);
		else
			lostExp = Math.round((getStat().getExpForLevel(Experience.MAX_LEVEL) - getStat().getExpForLevel(Experience.MAX_LEVEL - 1)) * percentLost / 100);


		if (_log.isDebugEnabled())
			_log.debug(getName() + " died and lost " + lostExp + " experience.");

		// Get the Experience before applying penalty
		setExpBeforeDeath(getExp());

		// Set the new Experience value of the L2Player
		getStat().addExp(-lostExp);
	}

	public void deathPenalty(boolean atwar, boolean killed_by_pc, boolean killed_by_siege_npc)
	{
		deathPenalty(atwar, killed_by_pc, getCharmOfCourage(), killed_by_siege_npc);
	}

	public boolean isLookingForParty()
	{
		return _lookingForParty;
	}

	public boolean getPartyMatchingLevelRestriction()
	{
		return !_partyMatchingAllLevels;
	}

	public int getPartyMatchingRegion()
	{
		return _partyMatchingRegion;
	}

	public void setLookingForParty(boolean matching)
	{
		_lookingForParty = matching;
	}

	public void setPartyMatchingLevelRestriction(boolean off)
	{
		_partyMatchingAllLevels = off;
	}

	public void setPartyMatchingRegion(int region)
	{
		_partyMatchingRegion = region;
	}

	public L2PartyRoom getPartyRoom()
	{
		return _partyRoom;
	}

	/**
	 * Set the _partyRoom object of the L2Player (without joining it).
	 * @param room new party room
	 */
	public void setPartyRoom(L2PartyRoom room)
	{
		_partyRoom = room;
	}

	/**
	 * Stop the HP/MP/CP Regeneration task.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the RegenActive flag to False </li>
	 * <li>Stop the HP/MP/CP Regeneration task </li><BR><BR>
	 *
	 */
	public void stopAllTimers()
	{
		getStatus().stopHpMpRegeneration();
		stopWarnUserTakeBreak();
		stopAutoSaveTask();
		stopWaterTask();

		stopFeed();
		clearPetData();
		storePetFood(_mountNpcId);

		stopSoulTask();
		stopChargeTask();
		stopFameTask();
		getPlayerVitality().stopVitalityTask();

		stopPvPFlag();
		stopJailTask(true);
	}

	/**
	 * Return the L2Summon of the L2Player or null.<BR><BR>
	 */
	@Override
	public L2Summon getPet()
	{
		return _summon;
	}

	/**
	 * Return the L2Decoy of the L2Player or null.<BR><BR>
	 */
	public L2Decoy getDecoy()
	{
		return _decoy;
	}

	/**
	 * Return the L2Trap of the L2Player or null.<BR><BR>
	 */
	public L2Trap getTrap()
	{
		return _trap;
	}

	/**
	 * Set the L2Summon of the L2Player.<BR><BR>
	 */
	public void setPet(L2Summon summon)
	{
		_summon = summon;
		// update attack element value display
		if ((_summon == null || _summon instanceof L2SummonInstance)
				&& getClassId().isSummoner() && getAttackElement() != Attributes.NONE)
			sendPacket(new UserInfo(this));
	}

	/**
	 * Set the L2Decoy of the L2Player.<BR><BR>
	 */
	public void setDecoy(L2Decoy decoy)
	{
		_decoy = decoy;
	}

	/**
	 * Set the L2Trap of this L2Player<BR><BR>
	 * @param trap
	 */
	public void setTrap(L2Trap trap)
	{
		_trap = trap;
	}

	/**
	 * Return the L2Summon of the L2Player or null.<BR><BR>
	 */
	public L2TamedBeastInstance getTrainedBeast()
	{
		return _tamedBeast;
	}

	/**
	 * Set the L2Summon of the L2Player.<BR><BR>
	 */
	public void setTrainedBeast(L2TamedBeastInstance tamedBeast)
	{
		_tamedBeast = tamedBeast;
	}

	/**
	 * Return the L2Player requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).<BR><BR>
	 */
	public L2Request getRequest()
	{
		if (_request == null)
			_request = new L2Request(this);

		return _request;
	}

	/**
	 * Set the L2Player requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).<BR><BR>
	 */
	public synchronized void setActiveRequester(L2Player requester)
	{
		_activeRequester = requester;
	}

	/**
	 * Return true if last request is expired.
	 * @return
	 */
	public boolean isRequestExpired()
	{
		return !(_requestExpireTime > GameTimeController.getGameTicks());
	}

	/**
	 * Return the L2Player requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).<BR><BR>
	 */
	public L2Player getActiveRequester()
	{
		return _activeRequester;
	}

	/**
	 * Return True if a transaction is in progress.<BR><BR>
	 */
	public boolean isProcessingRequest()
	{
		return _activeRequester != null || _requestExpireTime > GameTimeController.getGameTicks();
	}

	/**
	 * Return True if a transaction is in progress.<BR><BR>
	 */
	public boolean isProcessingTransaction()
	{
		return _activeRequester != null || _activeTradeList != null || _requestExpireTime > GameTimeController.getGameTicks();
	}

	/**
	 * Select the Warehouse to be used in next activity.<BR><BR>
	 */
	public void onTransactionRequest(L2Player partner)
	{
		_requestExpireTime = GameTimeController.getGameTicks() + REQUEST_TIMEOUT * GameTimeController.TICKS_PER_SECOND;
		partner.setActiveRequester(this);
	}

	/**
	 * Select the Warehouse to be used in next activity.<BR><BR>
	 */
	public void onTransactionResponse()
	{
		_requestExpireTime = 0;
	}

	/**
	 * Select the Warehouse to be used in next activity.<BR><BR>
	 */
	public void setActiveWarehouse(ItemContainer warehouse)
	{
		_activeWarehouse = warehouse;
	}

	/**
	 * Return active Warehouse.<BR><BR>
	 */
	public ItemContainer getActiveWarehouse()
	{
		return _activeWarehouse;
	}

	/**
	 * Select the TradeList to be used in next activity.<BR><BR>
	 */
	public void setActiveTradeList(TradeList tradeList)
	{
		_activeTradeList = tradeList;
	}

	/**
	 * Return active TradeList.<BR><BR>
	 */
	public TradeList getActiveTradeList()
	{
		return _activeTradeList;
	}

	public void onTradeStart(L2Player partner)
	{
		_activeTradeList = new TradeList(this);
		_activeTradeList.setPartner(partner);

		SystemMessage msg = new SystemMessage(SystemMessageId.BEGIN_TRADE_WITH_C1);
		msg.addPcName(partner);
		sendPacket(msg);
		sendPacket(new TradeStart(this));
	}

	public void onTradeConfirm(L2Player partner)
	{
		SystemMessage msg = new SystemMessage(SystemMessageId.C1_CONFIRMED_TRADE);
		msg.addPcName(partner);
		sendPacket(msg);
		sendPacket(TradeOtherDone.STATIC_PACKET);
	}

	public void onTradeCancel(L2Player partner)
	{
		if (_activeTradeList == null)
			return;

		_activeTradeList.lock();
		_activeTradeList = null;
		sendPacket(TradeDone.CANCELLED);
		SystemMessage msg = new SystemMessage(SystemMessageId.C1_CANCELED_TRADE);
		msg.addPcName(partner);
		sendPacket(msg);
	}

	public void onTradeFinish(boolean successfull)
	{
		_activeTradeList = null;
		sendPacket(TradeDone.COMPLETED);
		if (successfull)
			sendPacket(SystemMessageId.TRADE_SUCCESSFUL);
	}

	public void startTrade(L2Player partner)
	{
		onTradeStart(partner);
		partner.onTradeStart(this);
	}

	public void cancelActiveTrade()
	{
		if (_activeTradeList == null)
			return;

		L2Player partner = _activeTradeList.getPartner();
		if (partner != null)
			partner.onTradeCancel(this);
		onTradeCancel(this);
	}

	/**
	 * Return the _createList object of the L2Player.<BR><BR>
	 */
	public L2ManufactureList getCreateList()
	{
		return _createList;
	}

	/**
	 * Set the _createList object of the L2Player.<BR><BR>
	 */
	public void setCreateList(L2ManufactureList x)
	{
		_createList = x;
	}

	/**
	 * Return the _buyList object of the L2Player.<BR><BR>
	 */
	public TradeList getSellList()
	{
		if (_sellList == null)
			_sellList = new TradeList(this);
		return _sellList;
	}

	/**
	 * Return the _buyList object of the L2Player.<BR><BR>
	 */
	public TradeList getBuyList()
	{
		if (_buyList == null)
			_buyList = new TradeList(this);
		return _buyList;
	}

	/**
	 * Set the Private Store type of the L2Player.<BR><BR>
	 *
	 * <B><U> Values </U> :</B><BR><BR>
	 * <li>0 : STORE_PRIVATE_NONE</li>
	 * <li>1 : STORE_PRIVATE_SELL</li>
	 * <li>2 : sellmanage</li><BR>
	 * <li>3 : STORE_PRIVATE_BUY</li><BR>
	 * <li>4 : buymanage</li><BR>
	 * <li>5 : STORE_PRIVATE_MANUFACTURE</li><BR>
	 *
	 */
	public void setPrivateStoreType(int type)
	{
		_privatestore = type;
	}

	/**
	 * Return the Private Store type of the L2Player.<BR><BR>
	 *
	 * <B><U> Values </U> :</B><BR><BR>
	 * <li>0 : STORE_PRIVATE_NONE</li>
	 * <li>1 : STORE_PRIVATE_SELL</li>
	 * <li>2 : sellmanage</li><BR>
	 * <li>3 : STORE_PRIVATE_BUY</li><BR>
	 * <li>4 : buymanage</li><BR>
	 * <li>5 : STORE_PRIVATE_MANUFACTURE</li><BR>
	 *
	 */
	public int getPrivateStoreType()
	{
		return _privatestore;
	}

	/**
	 * Set the _skillLearningClassId object of the L2Player.<BR><BR>
	 */
	public void setSkillLearningClassId(ClassId classId)
	{
		_skillLearningClassId = classId;
	}

	/**
	 * Return the _skillLearningClassId object of the L2Player.<BR><BR>
	 */
	public ClassId getSkillLearningClassId()
	{
		return _skillLearningClassId;
	}

	/**
	 * Set the _clan object, _clanId, _clanLeader Flag and title of the L2Player.<BR><BR>
	 */
	public void setClan(L2Clan clan)
	{
		_clan = clan;
		setTitle("");

		if (clan == null)
		{
			_clanId = 0;
			_clanPrivileges = 0;
			_subPledgeType = 0;
			_pledgeRank = 0;
			_lvlJoinedAcademy = 0;
			_apprentice = 0;
			_sponsor = 0;
			return;
		}

		if (!clan.isMember(getObjectId()))
		{
			// Char has been kicked from clan
			setClan(null);
			return;
		}

		_clanId = clan.getClanId();
	}

	/**
	 * Return the _clan object of the L2Player.<BR><BR>
	 */
	public L2Clan getClan()
	{
		return _clan;
	}

	/**
	 * Return True if the L2Player is the leader of its clan.<BR><BR>
	 */
	public boolean isClanLeader()
	{
		return (getClan() != null) && getObjectId() == getClan().getLeaderId();
	}

	/**
	 * Disarm the player's weapon and shield.<BR><BR>
	 */
	public boolean disarmWeapons(boolean shield)
	{
		// Don't allow disarming a cursed weapon
		if (isCursedWeaponEquipped())
			return false;
		
		// Don't allow disarming a Combat Flag or Territory Ward
		if (isCombatFlagEquipped())
			return false;

		// Unequip the weapon
		L2ItemInstance wpn = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (wpn == null)
			wpn = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (wpn != null)
		{
			if (wpn.isWear())
				return false;

			L2ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance element : unequipped)
				iu.addModifiedItem(element);
			sendPacket(iu);

			abortAttack();
			refreshExpertisePenalty();

			// This can be 0 if the user pressed the right mousebutton twice very fast
			if (unequipped.length > 0)
			{
				SystemMessage sm = null;
				if (unequipped[0].getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
					sm.addNumber(unequipped[0].getEnchantLevel());
					sm.addItemName(unequipped[0]);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_DISARMED);
					sm.addItemName(unequipped[0]);
				}
				sendPacket(sm);
			}
			broadcastFullInfoImpl();
		}

		if (!shield)
			return true;

		// Unequip the shield
		L2ItemInstance sld = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (sld != null)
		{
			if (sld.isWear())
				return false;

			L2ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(sld.getItem().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance element : unequipped)
				iu.addModifiedItem(element);
			sendPacket(iu);

			abortAttack();
			refreshExpertisePenalty();

			// This can be 0 if the user pressed the right mousebutton twice very fast
			if (unequipped.length > 0)
			{
				SystemMessage sm = null;
				if (unequipped[0].getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
					sm.addNumber(unequipped[0].getEnchantLevel());
					sm.addItemName(unequipped[0]);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_DISARMED);
					sm.addItemName(unequipped[0]);
				}
				sendPacket(sm);
			}
			broadcastFullInfoImpl();
		}

		return true;
	}

	/**
	 * Reduce the number of arrows/bolts owned by the L2Player and send it Server->Client Packet InventoryUpdate or ItemList (to unequip if the last arrow was consummed).<BR><BR>
	 */
	@Override
	protected void reduceArrowCount(boolean bolts)
	{
		L2ItemInstance arrows = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);

		if (arrows == null)
		{
			getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_LHAND);
			if (bolts)
				_boltItem = null;
			else
				_arrowItem = null;
			sendPacket(new ItemList(this, false));
			return;
		}

		// Adjust item quantity
		if (arrows.getCount() > 1)
		{
			synchronized (arrows)
			{
				arrows.changeCountWithoutTrace(-1, this, null);
				arrows.setLastChange(L2ItemInstance.MODIFIED);

				// Could do also without saving, but let's save approx 1 of 10
				if (GameTimeController.getGameTicks() % 10 == 0)
					arrows.updateDatabase();
				getInventory().refreshWeight();
			}
		}
		else
		{
			// Destroy entire item and save to database
			getInventory().destroyItem("Consume", arrows, this, null);

			getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_LHAND);
			if (bolts)
				_boltItem = null;
			else
				_arrowItem = null;

			if (_log.isDebugEnabled())
				_log.debug("removed arrows count");
			sendPacket(new ItemList(this, false));
			return;
		}

		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(arrows);
			sendPacket(iu);
		}
		else
			sendPacket(new ItemList(this, false));
	}

	/**
	 * Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True.<BR><BR>
	 */
	@Override
	protected boolean checkAndEquipArrows()
	{
		// Check if nothing is equipped in left hand
		if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
		{
			// Get the L2ItemInstance of the arrows needed for this bow
			_arrowItem = getInventory().findArrowForBow(getActiveWeaponItem());

			if (_arrowItem != null)
			{
				// Equip arrows needed in left hand
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, _arrowItem);
				// Send inventory update packet
				getInventory().updateInventory(_arrowItem);
			}
		}
		else
		{
			// Get the L2ItemInstance of arrows equipped in left hand
			_arrowItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		}

		return _arrowItem != null;
	}

	/**
	 * Equip bolts needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True.<BR><BR>
	 */
	@Override
	protected boolean checkAndEquipBolts()
	{
		// Check if nothing is equipped in left hand
		if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
		{
			// Get the L2ItemInstance of the arrows needed for this bow
			_boltItem = getInventory().findBoltForCrossBow(getActiveWeaponItem());

			if (_boltItem != null)
			{
				// Equip arrows needed in left hand
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, _boltItem);

				// Send a Server->Client packet ItemList to this L2PcINstance to update left hand equipement
				sendPacket(new ItemList(this, false));
			}
		}
		else
		{
			// Get the L2ItemInstance of arrows equipped in left hand
			_boltItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		}

		return _boltItem != null;
	}

	public boolean mount(L2Summon pet)
	{
		// TODO: all checks from usercommandhandler mount and requestactionuse should be handled in ONE place, and this is not l2pcinstance
		// so this is temporary
		if (!isInsideRadius(pet, 80, true, false))
		{
			sendPacket(SystemMessageId.TOO_FAR_AWAY_FROM_FENRIR_TO_MOUNT);
			return false;
		}
		else if (!GeoData.getInstance().canSeeTarget(this, pet))
		{
			sendPacket(SystemMessageId.CANT_SEE_TARGET);
			return false;
		}

		if (!disarmWeapons(true))
			return false;
		if (getPlayerTransformation().isTransformed())
			return false;

		for (L2Effect e : getAllEffects())
		{
			if (e != null && e.getSkill().isToggle())
				e.exit();
		}

		Ride mount = new Ride(this, true, pet.getTemplate().getNpcId());
		setMount(pet.getNpcId(), pet.getLevel(), mount.getMountType());
		setMountObjectID(pet.getControlItemId());
		clearPetData();
		startFeed(pet.getNpcId());
		broadcastPacket(mount);

		// Notify self and others about speed change
		broadcastUserInfo();

		pet.unSummon(this);

		return true;
	}

	public boolean remount(L2Player player)
	{
		Ride dismount = new Ride(this, false, 0);
		Ride mount = new Ride(this, true, getMountNpcId());

		player.sendPacket(dismount);
		player.sendPacket(mount);
		return true;
	}

	public boolean mount(int npcId, int controlItemObjId, boolean useFood)
	{
		if (!disarmWeapons(true))
			return false;
		if (getPlayerTransformation().isTransformed())
			return false;

		for (L2Effect e : getAllEffects())
		{
			if (e != null && e.getSkill().isToggle())
				e.exit();
		}

		Ride mount = new Ride(this, true, npcId);
		if (setMount(npcId, getLevel(), mount.getMountType()))
		{
			clearPetData();
			setMountObjectID(controlItemObjId);
			broadcastPacket(mount);
			// Notify self and others about speed change
			broadcastFullInfoImpl();
			if (useFood)
				startFeed(npcId);
			return true;
		}
		return false;
	}

	public boolean mountPlayer(L2Summon pet)
	{
		if (pet != null && pet.isMountable() && !isMounted() && !isBetrayed() && !pet.isOutOfControl())
		{
			if (pet.getNpcId() == 16030 && pet.getLevel() < Config.GREAT_WOLF_MOUNT_LEVEL)
			{
				sendMessage("Your Wolf needs minimum level " + Config.GREAT_WOLF_MOUNT_LEVEL);
				return false;
			}
			else if (isParalyzed())
			{
				// You cannot mount a steed while petrified.
				sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_PETRIFIED);
				return false;
			}
			else if (isDead())
			{
				//A strider cannot be ridden when dead
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD);
				return false;
			}
			else if (pet.isDead())
			{
				//A dead strider cannot be ridden.
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.DEAD_STRIDER_CANT_BE_RIDDEN);
				return false;
			}
			else if (pet.isInCombat() || pet.isRooted() || pet.isParalyzed())
			{
				//A strider in battle cannot be ridden
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.STRIDER_IN_BATLLE_CANT_BE_RIDDEN);
				return false;

			}
			else if (isInCombat())
			{
				//A strider cannot be ridden while in battle
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
				return false;
			}
			else if (isSitting() || isInsideZone(L2Zone.FLAG_WATER))
			{
				//A strider can be ridden only when standing
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING);
				return false;
			}
			else if (getPlayerFish().isFishing())
			{
				//You can't mount, dismount, break and drop items while fishing
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_2);
				return false;
			}
			else if (getPlayerDuel().isInDuel())
			{
				// You cannot mount a steed while in a duel.
				sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_IN_A_DUEL);
				return false;
			}
			else if (getPlayerTransformation().isTransformed() || isCursedWeaponEquipped())
			{
				// no message needed, player while transformed doesn't have mount action
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			else if (getInventory().getItemByItemId(9819) != null)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_HOLDING_A_FLAG); // TODO: confirm this message
				return false;
			}
			else if (isCastingNow())
			{
				// You cannot mount a steed while skill casting.
				sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_SKILL_CASTING);
				return false;
			}
			else if (pet.isHungry())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT);
				return false;
			}
			else if (!Util.checkIfInRange(200, this, pet, true))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.TOO_FAR_AWAY_FROM_FENRIR_TO_MOUNT);
				return false;
			}
			else if (!pet.isDead() && !isMounted())
			{
				mount(pet);
			}
		}
		else if (isRentedPet())
		{
			stopRentPet();
		}
		else if (isMounted())
		{
			if (getMountType() == 2 && isInsideZone(L2Zone.FLAG_NOWYVERN))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.NO_DISMOUNT_HERE);
				return false;
			}
			else if (isHungry())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT);
				return false;
			}
			else if (ObjectRestrictions.getInstance().checkRestriction(this, AvailableRestriction.PlayerUnmount))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendMessage("You cannot dismount due to a restriction.");
				return false;
			}
			else
				dismount();
		}
		return true;
	}

	public boolean dismount()
	{
		boolean wasFlying = isFlying();

		sendPacket(new SetupGauge(3, 0, 0));
		int petId = _mountNpcId;
		if (setMount(0, 0, 0 ))
		{
			stopFeed();
			clearPetData();

			if (wasFlying)
				removeSkill(SkillTable.getInstance().getInfo(4289, 1));
			Ride dismount = new Ride(this, false, 0);
			broadcastPacket(dismount);
			setMountObjectID(0);
			storePetFood(petId);

			// Notify self and others about speed change
			broadcastUserInfo();
			return true;
		}
		return false;
	}

	/**
	 * Return True if the L2Player use a dual weapon.<BR><BR>
	 */
	@Override
	public boolean isUsingDualWeapon()
	{
		L2Weapon weaponItem = getActiveWeaponItem();

		if (weaponItem == null)
			return false;
		if (weaponItem.getItemType() == L2WeaponType.DUAL)
		{
			return true;
		}
		else if (weaponItem.getItemType() == L2WeaponType.DUAL_DAGGER)
		{
			return true;
		}
		else if (weaponItem.getItemType() == L2WeaponType.DUALFIST)
		{
			return true;
		}
		else if (weaponItem.getItemId() == 248) // Orc fighter fists
		{
			return true;
		}
		else return weaponItem.getItemId() == 252;
	}

	public void setUptime(long time)
	{
		_uptime = time;
	}

	public long getUptime()
	{
		return System.currentTimeMillis() - _uptime;
	}

	public long getOnlineTime()
	{
		long totalOnlineTime = _onlineTime;

		if (_onlineBeginTime > 0)
			totalOnlineTime += (System.currentTimeMillis() - _onlineBeginTime) / 1000;

		return totalOnlineTime;
	}

	/**
	 * Return True if the L2Player is invulnerable.<BR><BR>
	 */
	@Override
	public boolean isInvul()
	{
		return super.isInvul() || _protectEndTime > GameTimeController.getGameTicks();
	}

	/**
	 * Return True if the L2Player has a Party in progress.<BR><BR>
	 */
	@Override
	public boolean isInParty()
	{
		return _party != null;
	}

	/**
	 * Set the _party object of the L2Player (without joining it).<BR><BR>
	 */
	public void setParty(L2Party party)
	{
		_party = party;
	}

	/**
	 * Set the _party object of the L2Player AND join it.<BR><BR>
	 */
	public void joinParty(L2Party party)
	{
		if (party != null)
		{
			// First set the party otherwise this wouldn't be considered
			// as in a party into the L2Character.updateEffectIcons() call.
			setParty(party);
			if (!party.addPartyMember(this))
				setParty(null);
		}
	}

	/**
	 * Manage the Leave Party task of the L2Player.<BR><BR>
	 */
	public void leaveParty()
	{
		if (isInParty())
		{
			_party.removePartyMember(this, false);
			_party = null;
		}
	}

	/**
	 * Return the _party object of the L2Player.<BR><BR>
	 */
	@Override
	public L2Party getParty()
	{
		return _party;
	}

	/**
	 * Set the _isGm Flag of the L2Player.<BR><BR>
	 */
	public void setIsGM(boolean status)
	{
		_isGm = status;
	}

	/**
	 * Return True if the L2Player is a GM.<BR><BR>
	 */
	public boolean isGM()
	{
		return _isGm;
	}

	/**
	 * Set the _accessLevel of the L2Player.<BR><BR>
	 */
	public void setAccessLevel(int level)
	{
		_accessLevel = level;
		if (_accessLevel >= Config.GM_MIN)
			setIsGM(true);
	}

	public void setAccountAccesslevel(int level)
	{
		LoginServerThread.getInstance().sendAccessLevel(getAccountName(), level);
	}

	/**
	 * Return the _accessLevel of the L2Player.<BR><BR>
	 */
	public int getAccessLevel()
	{
		return _accessLevel;
	}

	@Override
	public double getLevelMod()
	{
		return (89 + getLevel()) / 100.0;
	}

	/**
	 * Send a Server->Client StatusUpdate packet with Karma and PvP Flag to the L2Player and all L2Player to inform (broadcast).<BR><BR>
	 * @param flag
	 */
	public void setKarmaFlag(int flag)
	{
		sendPacket(new UserInfo(this));
		broadcastRelationChanged();
	}

	/**
	 * Send a Server->Client StatusUpdate packet with Karma to the L2Player and all L2Player to inform (broadcast).<BR><BR>
	 */
	public void broadcastKarma()
	{
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.KARMA, getKarma());
		sendPacket(su);
		broadcastRelationChanged();
	}

	/**
	 * Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout).<BR><BR>
	 */
	public void setOnlineStatus(boolean isOnline)
	{
		final byte value = isOnline ? ONLINE_STATE_ONLINE : ONLINE_STATE_DELETED;

		if (_isOnline != value)
		{
			_isOnline = value;

			// Update the characters table of the database with online status and lastAccess (called when login and logout)
			updateOnlineStatusInDb();
		}
	}

	public void setIsIn7sDungeon(boolean isIn7sDungeon)
	{
		_isIn7sDungeon = isIn7sDungeon;
	}

	/**
	 * Update the characters table of the database with online status and lastAccess of this L2Player (called when
	 * login and logout).<BR>
	 * <BR>
	 */
	private void updateOnlineStatusInDb()
	{
		RecordTable.getInstance().update();

		SQLQueue.getInstance().add(new SQLQuery() {
			public void execute(Connection con)
			{
				try
				{
					PreparedStatement statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE charId=?");
					statement.setInt(1, isOnline());
					statement.setLong(2, System.currentTimeMillis());
					statement.setInt(3, getObjectId());
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					_log.error("Failed updating character online status.", e);
				}
			}
		});
	}

	/**
	 * Create a new player in the characters table of the database.<BR><BR>
	 */
	private boolean createDb()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(INSERT_CHARACTER);
			statement.setString(1, _accountName);
			statement.setInt(2, getObjectId());
			statement.setString(3, getName());
			statement.setInt(4, getLevel());
			statement.setInt(5, getMaxHp());
			statement.setDouble(6, getStatus().getCurrentHp());
			statement.setInt(7, getMaxCp());
			statement.setDouble(8, getStatus().getCurrentCp());
			statement.setInt(9, getMaxMp());
			statement.setDouble(10, getStatus().getCurrentMp());
			statement.setInt(11, getAppearance().getFace());
			statement.setInt(12, getAppearance().getHairStyle());
			statement.setInt(13, getAppearance().getHairColor());
			statement.setInt(14, getAppearance().getSex() ? 1 : 0);
			statement.setLong(15, getExp());
			statement.setInt(16, getSp());
			statement.setInt(17, getKarma());
			statement.setInt(18, getFame());
			statement.setInt(19, getPvpKills());
			statement.setInt(20, getPkKills());
			statement.setInt(21, getClanId());
			statement.setInt(22, getRace().ordinal());
			statement.setInt(23, getClassId().getId());
			statement.setLong(24, getDeleteTimer());
			statement.setInt(25, hasDwarvenCraft() ? 1 : 0);
			statement.setString(26, getTitle());
			statement.setInt(27, getAccessLevel());
			statement.setInt(28, isOnline());
			statement.setInt(29, isIn7sDungeon() ? 1 : 0);
			statement.setInt(30, getClanPrivileges());
			statement.setInt(31, getWantsPeace());
			statement.setInt(32, getBaseClass());
			statement.setInt(33, getNewbie());
			statement.setInt(34, isNoble() ? 1 : 0);
			statement.setLong(35, 0);

			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not insert char data: ", e);
			return false;
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		return true;
	}

	public static void disconnectIfOnline(int objectId)
	{
		L2Player onlinePlayer = L2World.getInstance().findPlayer(objectId);
		
		if (onlinePlayer == null)
			onlinePlayer = L2World.getInstance().getPlayer(CharNameTable.getInstance().getNameByObjectId(objectId));
		
		if (onlinePlayer == null)
			return;
		
		if (!onlinePlayer.isInOfflineMode())
			_log.warn("Avoiding duplicate character! Disconnecting online character (" + onlinePlayer.getName() + ")");
		
		// FIXME won't be sent because client.close() clears the packet queue
		onlinePlayer.sendPacket(SystemMessageId.ANOTHER_LOGIN_WITH_ACCOUNT);
		
		new Disconnection(onlinePlayer).defaultSequence(true);
	}

	public static void disconnectIfOnline(String accountName)
	{
		for (int objectId : CharNameTable.getInstance().getObjectIdsForAccount(accountName))
			disconnectIfOnline(objectId);
	}

	/**
	 * Retrieve a L2Player from the characters table of the database and add it in _allObjects of the L2world.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Retrieve the L2Player from the characters table of the database </li>
	 * <li>Add the L2Player object in _allObjects </li>
	 * <li>Set the x,y,z position of the L2Player and make it invisible</li>
	 * <li>Update the overloaded status of the L2Player</li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 *
	 * @return The L2Player loaded from the database
	 *
	 */
	public static L2Player load(int objectId)
	{
		disconnectIfOnline(objectId);

		L2Player player = null;
		Connection con = null;

		try
		{
			// Retrieve the L2Player from the characters table of the database
			con = L2DatabaseFactory.getInstance().getConnection(con);

			PreparedStatement statement = con.prepareStatement(RESTORE_CHARACTER);
			statement.setInt(1, objectId);
			ResultSet rset = statement.executeQuery();

			double currentHp = 1, currentMp = 1, currentCp = 1;
			if (rset.next())
			{
				final int activeClassId = rset.getInt("classid");
				final boolean female = rset.getInt("sex") != 0;
				final L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(activeClassId);
				PcAppearance app = new PcAppearance(rset.getByte("face"), rset.getByte("hairColor"), rset.getByte("hairStyle"), female);

				player = new L2Player(objectId, template, rset.getString("account_name"), app);
				player.setName(rset.getString("char_name"));
				player._lastAccess = rset.getLong("lastAccess");

				player.getStat().setExp(rset.getLong("exp"));
				player.setExpBeforeDeath(rset.getLong("expBeforeDeath"));
				player.getStat().setLevel(rset.getByte("level"));
				player.getStat().setSp(rset.getInt("sp"));

				player.setWantsPeace(rset.getInt("wantspeace"));

				player.setHeading(rset.getInt("heading"));

				player.setKarma(rset.getInt("karma"));
				player.setFame(rset.getInt("fame"));
				player.setPvpKills(rset.getInt("pvpkills"));
				player.setPkKills(rset.getInt("pkkills"));

				player.setClanJoinExpiryTime(rset.getLong("clan_join_expiry_time"));
				if (player.getClanJoinExpiryTime() < System.currentTimeMillis())
				{
					player.setClanJoinExpiryTime(0);
				}
				player.setClanCreateExpiryTime(rset.getLong("clan_create_expiry_time"));
				if (player.getClanCreateExpiryTime() < System.currentTimeMillis())
				{
					player.setClanCreateExpiryTime(0);
				}

				int clanId = rset.getInt("clanid");

				if (clanId > 0)
				{
					player.setClan(ClanTable.getInstance().getClan(clanId));
				}

				player.setDeleteTimer(rset.getLong("deletetime"));
				player.setOnlineTime(rset.getLong("onlinetime"));
				player.setNewbie(rset.getInt("newbie"));
				player.setNoble(rset.getInt("nobless") == 1);

				player.setTitle(rset.getString("title"));
				player.setAccessLevel(rset.getInt("accesslevel"));
				player.setFistsWeaponItem(player.findFistsWeaponItem(activeClassId));
				player.setUptime(System.currentTimeMillis());

				// Only 1 line needed for each and their values only have to be set once as long as you don't die before it's set.
				currentHp = rset.getDouble("curHp");
				currentMp = rset.getDouble("curMp");
				currentCp = rset.getDouble("curCp");

				player._classIndex = 0;
				try
				{
					player.setBaseClass(rset.getInt("base_class"));
				}
				catch (Exception e)
				{
					player.setBaseClass(activeClassId);
				}

				// Restore Subclass Data (cannot be done earlier in function)
				if (restoreSubClassData(player))
				{
					if (activeClassId != player.getBaseClass())
					{
						for (SubClass subClass : player.getSubClasses().values())
							if (subClass.getClassId() == activeClassId)
								player._classIndex = subClass.getClassIndex();
					}
				}
				if (player.getClassIndex() == 0 && activeClassId != player.getBaseClass())
				{
					// Subclass in use but doesn't exist in DB -
					// a possible restart-while-modifysubclass cheat has been attempted.
					// Switching to use base class
					player.setClassId(player.getBaseClass());
					_log.warn("Player " + player.getName() + " reverted to base class. Possibly has tried a relogin exploit while subclassing.");
				}
				else
					player._activeClass = activeClassId;

				player.setIsIn7sDungeon(rset.getInt("isin7sdungeon") == 1);
				player.setInJail(rset.getInt("in_jail") == 1);
				player.setJailTimer(rset.getLong("jail_timer"));
				player.setBanChatTimer(rset.getLong("banchat_timer"));
				if (player.isInJail())
					player.setJailTimer(rset.getLong("jail_timer"));
				else
					player.setJailTimer(0);

				CursedWeaponsService.getInstance().onEnter(player);

				player.setNoble(rset.getBoolean("nobless"));
				player.setCharViP((rset.getInt("charViP") == 1));
				player.setSubPledgeType(rset.getInt("subpledge"));
				player.setPledgeRank(rset.getInt("pledge_rank"));
				player.setApprentice(rset.getInt("apprentice"));
				player.setSponsor(rset.getInt("sponsor"));
				if (player.getClan() != null)
				{
					if (player.getClan().getLeaderId() != player.getObjectId())
					{
						if (player.getPledgeRank() == 0)
						{
							player.setPledgeRank(5);
						}
						player.setClanPrivileges(player.getClan().getRankPrivs(player.getPledgeRank()));
					}
					else
					{
						player.setClanPrivileges(L2Clan.CP_ALL);
						player.setPledgeRank(1);
					}
				}
				else
				{
					player.setClanPrivileges(L2Clan.CP_NOTHING);
				}
				player.setLvlJoinedAcademy(rset.getInt("lvl_joined_academy"));
				player.setAllianceWithVarkaKetra(rset.getInt("varka_ketra_ally"));
				player.setDeathPenaltyBuffLevel(rset.getInt("death_penalty_level"));
				player.getPlayerVitality().setVitalityPoints(rset.getInt("vitality_points"), true);

				// Add the L2Player object in _allObjects
				// L2World.getInstance().storeObject(player);

				// Set the x,y,z position of the L2Player and make it invisible
				player.getPosition().setXYZInvisible(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));

				// Set Teleport Bookmark Slot
				player.getPlayerBookmark().setBookMarkSlot(rset.getInt("BookmarkSlot"));
			}

			rset.close();
			statement.close();

			if (player == null)
				return null;

			// Retrieve from the database all secondary data of this L2Player
			// and reward expertise/lucky skills if necessary.
			player.restoreCharData();
			player.rewardSkills();

			// Buff and status icons
			player.getEffects().restoreEffects();
			player.restoreSkillReuses();

			player.stopEffects(L2EffectType.HEAL_OVER_TIME);
			player.stopEffects(L2EffectType.COMBAT_POINT_HEAL_OVER_TIME);

			// Restore current Cp, HP and MP values
			player.getStatus().setCurrentCp(currentCp);
			player.getStatus().setCurrentHp(currentHp);
			player.getStatus().setCurrentMp(currentMp);

			if (currentHp < 0.5)
			{
				player.setIsDead(true);
				player.getStatus().stopHpMpRegeneration();
			}

			// Restore pet if exists in the world
			player.setPet(L2World.getInstance().getPet(player.getObjectId()));
			if (player.getPet() != null)
				player.getPet().setOwner(player);

			// refresh overloaded already done when loading inventory
			// Update the expertise status of the L2Player
			player.refreshExpertisePenalty();
			
			if (Config.STORE_UI_SETTINGS)
				player.getPlayerSettings().restoreUISettings();
		}
		catch (Exception e)
		{
			_log.error("Failed loading character.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		return player;
	}

	/**
	 * Restores sub-class data for the L2Player, used to check the current
	 * class index for the character.
	 */
	private static boolean restoreSubClassData(L2Player player)
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_SUBCLASSES);
			statement.setInt(1, player.getObjectId());

			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				SubClass subClass = new SubClass();
				subClass.setClassId(rset.getInt("class_id"));
				subClass.setLevel(rset.getByte("level"));
				subClass.setExp(rset.getLong("exp"));
				subClass.setSp(rset.getInt("sp"));
				subClass.setClassIndex(rset.getInt("class_index"));

				// Enforce the correct indexing of _subClasses against their class indexes.
				player.getSubClasses().put(subClass.getClassIndex(), subClass);
			}

			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not restore classes for " + player.getName() + ": ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		return true;
	}

	/**
	 * Restores secondary data for the L2Player, based on the current class index.
	 */
	private void restoreCharData()
	{
		// Retrieve from the database all skills of this L2Player and add them to _skills.
		restoreSkills();

		// Retrieve from the database all macroses of this L2Player and add them to macroses.
		getPlayerSettings().getMacroses().restore();

		// Retrieve from the database all shortCuts of this L2Player and add them to shortCuts.
		getPlayerSettings().getShortCuts().restore();

		// Retrieve from the database all henna of this L2Player and add them to _henna.
		getPlayerHenna().restoreHenna();

		// Retrieve from the database all teleport bookmark of this L2Player and add them to _tpbookmark.
		getPlayerBookmark().restoreTeleportBookmark();

		// Retrieve from the database all recom data of this L2Player and add to _recomChars.
		RecommendationService.getInstance().onJoin(this);

		// Retrieve from the database the recipe book of this L2Player.
		getPlayerRecipe().restoreRecipeBook(true);

		getPlayerBirthday().restoreCreationDate();
		
		getAppearance().restoreNameTitleColors();
	}

	/** player coords from client */
	private int _clientX;
	private int _clientY;
	private int _clientZ;
	private int _clientHeading;

	@Override
	public int getClientX()
	{
		return _clientX;
	}

	@Override
	public int getClientY()
	{
		return _clientY;
	}

	@Override
	public int getClientZ()
	{
		return _clientZ;
	}

	@Override
	public int getClientHeading()
	{
		return _clientHeading;
	}

	public void setClientX(int val)
	{
		_clientX = val;
	}

	public void setClientY(int val)
	{
		_clientY = val;
	}

	public void setClientZ(int val)
	{
		_clientZ = val;
	}

	public void setClientHeading(int val)
	{
		_clientHeading = val;
	}

	/**
	 * Update L2Player stats in the characters table of the database.<BR><BR>
	 */
	private long _lastStore;

	public void store()
	{
		store(false, true);
	}

	public void store(boolean storeActiveEffects)
	{
		store(false, storeActiveEffects);
	}

	public synchronized void store(boolean items, boolean storeActiveEffects)
	{
		_lastStore = System.currentTimeMillis();

		if (getOnlineState() == ONLINE_STATE_DELETED)
			return;

		// Update client coords, if these look like true
		// if (isInsideRadius(getClientX(), getClientY(), 1000, true))
		//	getPosition().setXYZ(getClientX(), getClientY(), getClientZ());

		storeCharBase();
		storeCharSub();
		storePet();
		getEffects().storeEffects(storeActiveEffects);
		storeSkillReuses();
		getPlayerTransformation().transformInsertInfo();
		getAppearance().storeNameTitleColors();

		if (Config.UPDATE_ITEMS_ON_CHAR_STORE || items)
			getInventory().updateDatabase();
		
		if (Config.STORE_UI_SETTINGS)
			getPlayerSettings().storeUISettings();
	}

	private void storeCharBase()
	{
		Connection con = null;

		try
		{
			// Get the exp, level, and sp of base class to store in base table
			int currentClassIndex = getClassIndex();
			_classIndex = 0;
			long exp = getStat().getExp();
			int level = getStat().getLevel();
			int sp = getStat().getSp();
			_classIndex = currentClassIndex;

			con = L2DatabaseFactory.getInstance().getConnection(con);

			// Update base class
			PreparedStatement statement = con.prepareStatement(UPDATE_CHARACTER);
			statement.setInt(1, level);
			statement.setInt(2, getMaxHp());
			statement.setDouble(3, getStatus().getCurrentHp());
			statement.setInt(4, getMaxCp());
			statement.setDouble(5, getStatus().getCurrentCp());
			statement.setInt(6, getMaxMp());
			statement.setDouble(7, getStatus().getCurrentMp());
			statement.setInt(8, getAppearance().getFace());
			statement.setInt(9, getAppearance().getHairStyle());
			statement.setInt(10, getAppearance().getHairColor());
			statement.setInt(11, getAppearance().getSex() ? 1 : 0);
			statement.setInt(12, getHeading());
			statement.setInt(13, getPlayerObserver().inObserverMode() ? getPlayerObserver().getObsX() : getX());
			statement.setInt(14, getPlayerObserver().inObserverMode() ? getPlayerObserver().getObsY() : getY());
			statement.setInt(15, getPlayerObserver().inObserverMode() ? getPlayerObserver().getObsZ() : getZ());
			statement.setLong(16, exp);
			statement.setLong(17, getExpBeforeDeath());
			statement.setInt(18, sp);
			statement.setInt(19, getKarma());
			statement.setInt(20, getFame());
			statement.setInt(21, getPvpKills());
			statement.setInt(22, getPkKills());
			statement.setInt(23, getClanId());
			statement.setInt(24, getRace().ordinal());
			statement.setInt(25, getClassId().getId());
			statement.setLong(26, getDeleteTimer());
			statement.setString(27, getTitle());
			statement.setInt(28, getAccessLevel());
			statement.setInt(29, isOnline());
			statement.setInt(30, isIn7sDungeon() ? 1 : 0);
			statement.setInt(31, getClanPrivileges());
			statement.setInt(32, getWantsPeace());
			statement.setInt(33, getBaseClass());

			statement.setLong(34, getOnlineTime());
			statement.setInt(35, isInJail() ? 1 : 0);
			statement.setLong(36, getJailTimer());
			statement.setInt(37, getNewbie());
			statement.setInt(38, isNoble() ? 1 : 0);
			statement.setLong(39, getPledgeRank());
			statement.setInt(40, getSubPledgeType());
			statement.setInt(41, getLvlJoinedAcademy());
			statement.setLong(42, getApprentice());
			statement.setLong(43, getSponsor());
			statement.setInt(44, getAllianceWithVarkaKetra());
			statement.setLong(45, getClanJoinExpiryTime());
			statement.setLong(46, getClanCreateExpiryTime());
			statement.setLong(47, getBanChatTimer());
			statement.setString(48, getName());
			statement.setLong(49, getDeathPenaltyBuffLevel());
			statement.setInt(50, getPlayerVitality().getVitalityPoints());
			statement.setInt(51, getPlayerBookmark().getBookMarkSlot());
			statement.setInt(52, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not store char base data: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void storeCharSub()
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(UPDATE_CHAR_SUBCLASS);

			if (getTotalSubClasses() > 0)
			{
				for (SubClass subClass : getSubClasses().values())
				{
					statement.setLong(1, subClass.getExp());
					statement.setInt(2, subClass.getSp());
					statement.setInt(3, subClass.getLevel());

					statement.setInt(4, subClass.getClassId());
					statement.setInt(5, getObjectId());
					statement.setInt(6, subClass.getClassIndex());

					statement.execute();
				}
			}
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not store sub class data for " + getName() + ": ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}



	private void storeSkillReuses()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement(DELETE_SKILL_REUSES);
			statement.setInt(1, getObjectId());
			statement.execute();
			statement.close();

			statement = con.prepareStatement(ADD_SKILL_REUSE);

			for (TimeStamp t : getReuseTimeStamps().values())
			{
				if (t.getRemaining() > 10000) // store only over 10s
				{
					statement.setInt(1, getObjectId());
					statement.setInt(2, t.getSkillId());
					statement.setInt(3, t.getReuseDelay());
					statement.setLong(4, t.getExpiration());
					statement.execute();
				}
			}

			statement.close();
		}
		catch (Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void storePet()
	{
		L2Summon pet = getPet();
		if (pet != null)
			pet.store();
	}

	/**
	 * Return True if the L2Player is on line.<BR><BR>
	 */
	public int isOnline()
	{
		return (getOnlineState() == ONLINE_STATE_ONLINE) ? 1 : 0;
	}

	public byte getOnlineState()
	{
		return _isOnline;
	}

	public boolean isIn7sDungeon()
	{
		return _isIn7sDungeon;
	}

	/**
	 * Add a skill to the L2Player _skills and its Func objects to the calculator set of the L2Player and save update in the character_skills table of the database.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2Player are identified in <B>_skills</B><BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Replace oldSkill by newSkill or Add the newSkill </li>
	 * <li>If an old skill has been replaced, remove all its Func objects of L2Character calculator set</li>
	 * <li>Add Func objects of newSkill to the calculator set of the L2Character </li><BR><BR>
	 *
	 * @param newSkill The L2Skill to add to the L2Character
	 *
	 * @return The L2Skill replaced or null if just added a new L2Skill
	 *
	 */
	public L2Skill addSkill(L2Skill newSkill, boolean save)
	{
		// Add a skill to the L2Player _skills and its Func objects to the calculator set of the L2Player
		final L2Skill oldSkill = addSkill(newSkill);
		
		// Add or update a L2Player skill in the character_skills table of the database
		if (save)
			_pcSkills.storeSkill(newSkill, getClassIndex());
		
		return oldSkill;
	}

	@Override
	protected void skillChanged(L2Skill removed, L2Skill added)
	{
		super.skillChanged(removed, added);
		
		if (!L2System.equals(removed, added))
			sendSkillList();
	}

	public L2Skill removeSkill(L2Skill skill, boolean store)
	{
		return store ? removeSkill(skill) : super.removeSkill(skill);
	}

	/**
	 * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character and save update in the character_skills table of the database.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the skill from the L2Character _skills </li>
	 * <li>Remove all its Func objects from the L2Character calculator set</li><BR><BR>
	 *
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2Player : Save update in the character_skills table of the database</li><BR><BR>
	 *
	 * @param skill The L2Skill to remove from the L2Character
	 *
	 * @return The L2Skill removed
	 *
	 */
	@Override
	public L2Skill removeSkill(L2Skill skill)
	{
		// Remove a skill from the L2Character and its Func objects from calculator set of the L2Character
		skill = super.removeSkill(skill);
		
		if (skill == null)
			return null;
		
		_pcSkills.deleteSkill(skill);
		
		if (getPlayerTransformation().transformId() > 0 || isCursedWeaponEquipped())
			return skill;
		
		if (!skill.isItemSkill())
			getPlayerSettings().getShortCuts().deleteShortCutByTypeAndId(L2ShortCut.TYPE_SKILL, skill.getId());
		
		return skill;
	}
	
	/**
	 * check player skills and remove unlegit ones (excludes hero, noblesse and cursed weapon skills)
	 */
	public void checkAllowedSkills()
	{
		if (isGM() || !Config.CHECK_SKILLS_ON_ENTER || Config.ALT_GAME_SKILL_LEARN)
			return;
		
		Set<Integer> skillTreeUIDs = SkillTreeTable.getInstance().getAllowedSkillUIDs(getClassId());
		
		skill_loop: for (L2Skill skill : getAllSkills())
		{
			int skillid = skill.getId();
			
			if (isStoredSkill(skill, skillTreeUIDs))
				continue;
			
			if (isTemporarySkill(skill))
				continue;
			
			// Exclude Skills from AllowedSkills in options.properties
			if (Config.ALLOWED_SKILLS_LIST.contains(skillid))
				continue skill_loop;
			// Exclude VIP character
			if (isCharViP() && Config.CHAR_VIP_SKIP_SKILLS_CHECK)
				continue skill_loop;
			
			// Remove skill from ingame, but not from the database to avoid accidentally removal of skills
			// if something failed loading and do a lil log message
			removeSkill(skill, false);
			sendMessage("Skill " + skill.getName() + " removed and GM informed!");
			_log.fatal("Cheater?! " + skill + " removed from " + getName() + " (" + getAccountName() + ")");
		}
	}
	
	public boolean isStoredSkill(L2Skill skill)
	{
		return isStoredSkill(skill, SkillTreeTable.getInstance().getAllowedSkillUIDs(getClassId()));
	}
	
	private boolean isStoredSkill(L2Skill skill, Set<Integer> skillTreeUIDs)
	{
		int skillid = skill.getId();
		
		// Loop through all skills in players skilltree
		if (skillTreeUIDs.contains(SkillTable.getSkillUID(skillid, SkillTable.getInstance().getNormalLevel(skill))))
			return true;
		
		// skills learned by L2SkillType.LEARN_SKILL
		if (SkillTable.getInstance().isLearnedSkill(skill))
			return true;
		
		// Exclude fishing skills and common skills + dwarfen craft
		if (skillid >= 1312 && skillid <= 1322)
			return true;
		if (skillid >= 1368 && skillid <= 1373)
			return true;
		
		if (L2CertificationSkillsLearn.isCertificationSkill(skillid))
			return true;
		if (L2TransformSkillLearn.isTransformSkill(skillid))
			return true;
		if (L2SkillLearn.isSpecialSkill(skillid))
			return true;
		
		return false;
	}
	
	public boolean isTemporarySkill(L2Skill skill)
	{
		int skillid = skill.getId();
		
		if (getPlayerTransformation().getTransformation() != null && getPlayerTransformation().containsAllowedTransformSkill(skillid))
			return true;
		// Exclude noble skills
		if (isNoble() && NobleSkillTable.isNobleSkill(skillid))
			return true;
		// Exclude hero skills
		if (isHero() && HeroSkillTable.isHeroSkill(skillid))
			return true;
		// Exclude cursed weapon skills
		if (isCursedWeaponEquipped() && skillid == CursedWeaponsService.getInstance().getCursedWeapon(_cursedWeaponEquippedId).getSkillId())
			return true;
		// Exclude clan skills
		if (getClan() != null && (skillid >= 370 && skillid <= 391))
			return true;
		// Exclude residential skills
		if (getClan() != null && (getClan().getHasCastle() > 0 || getClan().getHasFort() > 0))
			if (590 <= skillid && skillid <= 610)
				return true;
		// Exclude seal of ruler / build siege hq
		if (getClan() != null && getClan().getLeaderId() == getObjectId() && (skillid == 246 || skillid == 247))
			return true;
		// Exclude sa / enchant bonus / penality etc. skills
		if (skillid >= 3000 && skillid < 7000)
			return true;
		// Exclude Armor Set skills
		if (skillid >= 8100 && skillid < 8400)
			return true;
		
		return false;
	}

	/**
	 * Retrieve from the database all skills of this L2Player and add them to _skills.<BR><BR>
	 */
	private void restoreSkills()
	{
		_pcSkills.restoreSkills();

		// Restore clan skills
		if (_clan != null)
			_clan.addSkillEffects(this, false);
	}

	private void restoreSkillReuses()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement(RESTORE_SKILL_REUSES);
			statement.setInt(1, getObjectId());
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				final int skillId = rset.getInt("skillId");
				final int reuseDelay = rset.getInt("reuseDelay");
				final long expiration = rset.getLong("expiration");

				final int remaining = L2Math.limit(0, expiration - System.currentTimeMillis(), Integer.MAX_VALUE);

				disableSkill(skillId, reuseDelay, remaining);
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Return True if the L2Player is autoAttackable.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Check if the attacker isn't the L2Player Pet </li>
	 * <li>Check if the attacker is L2MonsterInstance</li>
	 * <li>If the attacker is a L2Player, check if it is not in the same party </li>
	 * <li>Check if the L2Player has Karma </li>
	 * <li>If the attacker is a L2Player, check if it is not in the same siege clan (Attacker, Defender) </li><BR><BR>
	 *
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		// Check if the attacker isn't the L2Player Pet
		if (attacker == this || attacker == getPet())
			return false;

		// Check if the attacker is a L2MonsterInstance
		if (attacker instanceof L2MonsterInstance)
			return true;

		// Check if the attacker is not in the same party
		if (getParty() != null && getParty().getPartyMembers().contains(attacker))
			return false;

		if (isCursedWeaponEquipped())
			return true;

		// Check if the attacker is in olympia and olympia start
		if (attacker instanceof L2Player && ((L2Player) attacker).getPlayerOlympiad().isInOlympiadMode())
		{
			return getPlayerOlympiad().isInOlympiadMode() && getPlayerOlympiad().isOlympiadStart() && ((L2Player) attacker).getPlayerOlympiad().getOlympiadGameId() == getPlayerOlympiad().getOlympiadGameId();
		}

		// Check if the attacker is not in the same clan
		if (getClan() != null && attacker != null && getClan().isMember(attacker.getObjectId()))
			return false;

		if (attacker instanceof L2Playable && isInsideZone(L2Zone.FLAG_PEACE))
			return false;

		// Check if the L2Player has Karma
		if (getKarma() > 0 || getPvpFlag() > 0)
			return true;

		// Check if the attacker is a L2Playable
		if (attacker instanceof L2Playable)
		{
			// Is AutoAttackable if both playables are in the same duel and the duel is still going on
			if (getPlayerDuel().getDuelState() == Duel.DUELSTATE_DUELLING && getPlayerDuel().getDuelId() == attacker.getActingPlayer().getPlayerDuel().getDuelId())
				return true;
		}

		// Check if the attacker is a L2Player
		if (attacker instanceof L2Player)
		{
			// Check if the L2Player is in an arena or a siege area
			if (isInsideZone(L2Zone.FLAG_PVP) && attacker.isInsideZone(L2Zone.FLAG_PVP))
				return true;

			// Check if the L2Player holds a cursed weapon
			if (((L2Player) attacker).isCursedWeaponEquipped())
				return true;

			if (getClan() != null)
			{
				Siege siege = SiegeManager.getInstance().getSiege(getX(), getY(), getZ());
				if (siege != null)
				{
					// Check if a siege is in progress and if attacker and the L2Player aren't in the Defender clan
					if (siege.checkIsDefender(((L2Player) attacker).getClan()) && siege.checkIsDefender(getClan()))
						return false;

					// Check if a siege is in progress and if attacker and the L2Player aren't in the Attacker clan
					if (siege.checkIsAttacker(((L2Player) attacker).getClan()) && siege.checkIsAttacker(getClan()))
						return false;
				}

				// Check if clan is at war
				if (getClan() != null
						&& ((L2Player) attacker).getClan() != null
						&& (getClan().isAtWarWith(((L2Player) attacker).getClanId())
								&& ((L2Player)attacker).getClan().isAtWarWith(getClanId())
								&& getWantsPeace() == 0 && ((L2Player) attacker).getWantsPeace() == 0 && !isAcademyMember()))
					return true;
			}
		}
		else if (attacker instanceof L2DefenderInstance)
		{
			return ((L2DefenderInstance)attacker).shouldAttack(this);
		}

		return false;
	}

	/**
	 * Checks if the client was allowed to call that skill at all, or not.
	 */
	public boolean canUseMagic(L2Skill skill)
	{
		if (skill == null || skill.getSkillType() == L2SkillType.NOTDONE)
			return false;

		// players mounted on pets cannot use any toggle skills
		if (skill.isToggle() && isMounted())
			return false;

		// Check if the skill is active
		if (skill.isPassive())
			return false;

		if (getPlayerTransformation().isTransformationDisabledSkill(skill) && !skill.isPotion())
			return false;

		// Failfast as in retail
		if (isSkillDisabled(skill.getId()))
		{
			sendReuseMessage(skill);
			return false;
		}
		
		if (!GlobalRestrictions.canUseSkill(this, skill))
			return false;

		return true;
	}

	@Override
	public void doCast(L2Skill skill)
	{
		if (!canUseMagic(skill))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		super.doCast(skill);
	}

	@Override
	public void doSimultaneousCast(L2Skill skill)
	{
		if (!canUseMagic(skill))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		super.doSimultaneousCast(skill);
	}

	public void sendReuseMessage(L2Skill skill)
	{
		SystemMessage sm = null;

		TimeStamp timeStamp = getReuseTimeStamps().get(skill.getId());
		int remainingTime = (timeStamp == null ? 0 : timeStamp.getRemaining() / 1000);
		int hours = remainingTime / 3600;
		int minutes = (remainingTime % 3600) / 60;
		int seconds = (remainingTime % 60);
		if (hours > 0)
		{
			sm = new SystemMessage(SystemMessageId.S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_REUSE_S1);
			sm.addSkillName(skill);
			sm.addNumber(hours);
			sm.addNumber(minutes);
			sm.addNumber(seconds);
		}
		else if (minutes > 0)
		{
			sm = new SystemMessage(SystemMessageId.S2_MINUTES_S3_SECONDS_REMAINING_FOR_REUSE_S1);
			sm.addSkillName(skill);
			sm.addNumber(minutes);
			sm.addNumber(seconds);
		}
		else if (seconds > 0)
		{
			sm = new SystemMessage(SystemMessageId.S2_SECONDS_REMAINING_FOR_REUSE_S1);
			sm.addSkillName(skill);
			sm.addNumber(seconds);
		}
		else
		{
			sm = new SystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
			sm.addSkillName(skill);
		}
		sendPacket(sm);
	}

	@Override
	protected boolean checkUseMagicConditions(L2Skill skill, boolean forceUse)
	{
		L2SkillType sklType = skill.getSkillType();

		//************************************* Check Player State *******************************************

		// Abnormal effects(ex : Stun, Sleep...) are checked in L2Character useMagic()

		if (!SkillHandler.getInstance().checkConditions(this, skill))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		if (isOutOfControl())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// Check if the player is dead
		if (isDead())
		{
			abortCast();
			// Send a Server->Client packet ActionFailed to the L2Player
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		if (getPlayerFish().isFishing() && (sklType != L2SkillType.PUMPING &&
				sklType != L2SkillType.REELING && sklType != L2SkillType.FISHING))
		{
			// Only fishing skills are available
			sendPacket(SystemMessageId.ONLY_FISHING_SKILLS_NOW);
			return false;
		}

		if (getPlayerObserver().inObserverMode())
		{
			sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			abortCast();
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// Check if the caster is sitting
		if (isSitting() && !skill.isPotion())
		{
			// Send a System Message to the caster
			sendPacket(SystemMessageId.CANT_MOVE_SITTING);

			// Send a Server->Client packet ActionFailed to the L2Player
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// Check if the skill type is TOGGLE
		if (skill.isToggle())
		{
			// Get effects of the skill
			L2Effect effect = getFirstEffect(skill);

			if (effect != null)
			{
				effect.exit();

				// Send a Server->Client packet ActionFailed to the L2Player
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}

		// Check if the player uses "Fake Death" skill
		// Note: do not check this before TOGGLE reset
		if (isFakeDeath())
		{
			// Send a Server->Client packet ActionFailed to the L2Player
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// Check if it's ok to summon
		// Siege Golem (13), Wild Hog Cannon (299), Swoop Cannon (448)
		switch (skill.getId())
		{
		case 13:
		case 299:
		case 448:
			if ((!SiegeManager.getInstance().checkIfOkToSummon(this, false) && !FortSiegeManager.getInstance().checkIfOkToSummon(this, false))
					|| SevenSigns.getInstance().checkSummonConditions(this))
			{
				return false;
			}
		}

		//************************************* Check Target *******************************************

		// Create and set a L2Object containing the target of the skill

		SkillTargetTypes sklTargetType = skill.getTargetType();
		Point3D worldPosition = getCurrentSkillWorldPosition();

		if (sklTargetType == SkillTargetTypes.TARGET_GROUND && worldPosition == null)
		{
			_log.info("WorldPosition is null for skill: " + skill.getName() + ", player: " + getName() + ".");
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		final L2Character target = skill.getFirstOfTargetList(this);

		// Check the validity of the target
		if (target == null)
		{
			//sendPacket(SystemMessageId.TARGET_CANT_FOUND);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// Skills can be used on walls and doors only during siege
		if (target instanceof L2DoorInstance)
		{
			L2DoorInstance door = (L2DoorInstance) target;
			boolean isCastleDoor = (door.getCastle() != null && door.getCastle().getSiege().getIsInProgress());
			boolean isFortDoor = (door.getFort() != null && door.getFort().getSiege().getIsInProgress() && !door.isCommanderDoor());
			if (!isCastleDoor && !isFortDoor && (door.isUnlockable() && skill.getSkillType() != L2SkillType.UNLOCK))
				return false;
		}

		// Are the target and the player in the same duel?
		if (getPlayerDuel().isInDuel())
		{
			if (!(target instanceof L2Playable && target.getActingPlayer().getPlayerDuel().getDuelId() == getPlayerDuel().getDuelId()))
			{
				sendMessage("You cannot do this while duelling.");
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}

		//************************************* Check skill availability *******************************************

		// Check if this skill is enabled (ex : reuse time)
		if (isSkillDisabled(skill.getId()))
		{
			sendReuseMessage(skill);
			return false;
		}

		//************************************* Check Consumables *******************************************

		if (skill.getTransformId() > 0)
		{
			if (getPet() != null)
				getPet().unSummon(this); // Unsummon pets

			if (getEffects().hasEffect(L2EffectType.TRANSFORMATION) || getPet() != null || isMounted() || isFlying())
			{
				sendPacket(new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
				return false;
			}
		}

		//************************************* Check Consumables *******************************************

		// Check if spell consumes a Soul
		// Most kamael skills have only optional soul consumption to empower skills!
		if (getSouls() < skill.getSoulConsumeCount())
		{
			sendPacket(SystemMessageId.THERE_IS_NOT_ENOUGH_SOUL);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// Check if spell consumes charges
		if (_charges < skill.getNeededCharges())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(skill);
			sendPacket(sm);
			return false;
		}

		// Check if spell adds charges
		if (!skill.isOffensive() && skill.getGiveCharges() > 0 && _charges >= skill.getMaxCharges())
		{
			sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		//************************************* Check Casting Conditions *******************************************

		// Check if all casting conditions are completed
		if (!skill.checkCondition(this, target))
		{
			// Send a Server->Client packet ActionFailed to the L2Player
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		//************************************* Check Skill Type *******************************************

		// Check if this is offensive magic skill
		if (skill.isOffensive())
		{
			if (L2Character.isInsidePeaceZone(this, target))
			{
				// If L2Character or target is in a peace zone, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed
				sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}

			if (getPlayerOlympiad().isInOlympiadMode() && !getPlayerOlympiad().isOlympiadStart())
			{
				// If the L2Player is in Olympia and the match isn't already start, send a Server->Client packet ActionFailed
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			
			if (target.getActingPlayer() != null && getSiegeState() > 0 && isInsideZone(L2Zone.FLAG_SIEGE)
					&& target.getActingPlayer().getSiegeState() == getSiegeState()
					&& target.getActingPlayer() != this && target.getActingPlayer().getSiegeSide() == getSiegeSide())
			{
				if (TerritoryWarManager.getInstance().isTWInProgress())
					sendPacket(SystemMessageId.YOU_CANNOT_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY);
				else
					sendPacket(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}

			// Check if the target is attackable
			if (!target.isAttackable() && (getAccessLevel() < Config.GM_PEACEATTACK))
			{
				// If target is not attackable, send a Server->Client packet ActionFailed
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}

			// Check if a Forced ATTACK is in progress on non-attackable target
			if (!target.isAutoAttackable(this) && !forceUse)
			{
				switch (sklTargetType)
				{
				case TARGET_AURA:
				case TARGET_FRONT_AURA:
				case TARGET_BEHIND_AURA:
				case TARGET_SERVITOR_AURA:
				case TARGET_CLAN:
				case TARGET_PARTY_CLAN:
				case TARGET_ALLY:
				case TARGET_PARTY:
				case TARGET_SELF:
				case TARGET_GROUND:
					// Everything okay
					break;
				default:
					// Send a Server->Client packet ActionFailed to the L2Player
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
		}

		// Check if the skill is defensive
		if (skill.isPositive() && target instanceof L2MonsterInstance && !forceUse)
		{
			switch (sklTargetType)
			{
			case TARGET_PET:
			case TARGET_SUMMON:
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_SERVITOR_AURA:
			case TARGET_CLAN:
			case TARGET_PARTY_CLAN:
			case TARGET_SELF:
			case TARGET_PARTY:
			case TARGET_ALLY:
			case TARGET_CORPSE_MOB:
			case TARGET_AREA_CORPSE_MOB:
			case TARGET_GROUND:
				// Everything okay
				break;
			default:
				switch (sklType)
				{
				case BEAST_FEED:
				case DELUXE_KEY_UNLOCK:
				case UNLOCK:
				case MAKE_KILLABLE:
					// Everything okay
					break;
				default:
					// send the action failed so that the skill doens't go off.
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
		}

		if (!SkillHandler.getInstance().checkConditions(this, skill, L2Object.getActingCharacter(target)))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// Check if this is a Pvp skill and target isn't a non-flagged/non-karma player
		switch (sklTargetType)
		{
		case TARGET_PARTY:
		case TARGET_ALLY: // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
		case TARGET_CLAN: // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
		case TARGET_PARTY_CLAN:// For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
		case TARGET_AURA:
		case TARGET_FRONT_AURA:
		case TARGET_BEHIND_AURA:
		case TARGET_SERVITOR_AURA:
		case TARGET_GROUND:
		case TARGET_SELF:
			break;
		default:
			if (!checkPvpSkill(target, skill) && (getAccessLevel() < Config.GM_PEACEATTACK))
			{
				// Send a System Message to the L2Player
				sendPacket(SystemMessageId.TARGET_IS_INCORRECT);

				// Send a Server->Client packet ActionFailed to the L2Player
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}

		if ((sklTargetType == SkillTargetTypes.TARGET_HOLY && (!TakeCastle.checkIfOkToCastSealOfRule(this)))
				|| (sklTargetType == SkillTargetTypes.TARGET_FLAGPOLE && !TakeFort.checkIfOkToCastFlagDisplay(this, false, skill, getTarget())))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			abortCast();
			return false;
		}

		// GeoData Los Check here
		if (skill.getCastRange() > 0)
		{
			if (sklTargetType == SkillTargetTypes.TARGET_GROUND)
			{
				if (!GeoData.getInstance().canSeeTarget(this, worldPosition))
				{
					sendPacket(SystemMessageId.CANT_SEE_TARGET);
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
			else if (!GeoData.getInstance().canSeeTarget(this, target))
			{
				sendPacket(SystemMessageId.CANT_SEE_TARGET);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}

		// Finally, after passing all conditions
		return true;
	}

	public boolean isInLooterParty(int LooterId)
	{
		L2Player looter = (L2Player) L2World.getInstance().findObject(LooterId);

		// If L2Player is in a CommandChannel
		if (isInParty() && getParty().isInCommandChannel() && looter != null)
			return getParty().getCommandChannel().getMembers().contains(looter);

		if (isInParty() && looter != null)
			return getParty().getPartyMembers().contains(looter);

		return false;
	}

	/**
	 * Check if the requested casting is a Pc->Pc skill cast and if it's a valid pvp condition
	 * @param obj L2Object instance containing the target
	 * @param skill L2Skill instance with the skill being casted
	 * @return False if the skill is a pvpSkill and target is not a valid pvp target
	 */
	public boolean checkPvpSkill(L2Object obj, L2Skill skill)
	{
		return checkPvpSkill(obj, skill, false);
	}

	/**
	 * Check if the requested casting is a Pc->Pc skill cast and if it's a valid pvp condition
	 * @param obj L2Object instance containing the target
	 * @param skill L2Skill instance with the skill being casted
	 * @param srcIsSummon is L2Summon - caster?
	 * @return False if the skill is a pvpSkill and target is not a valid pvp target
	 */
	public boolean checkPvpSkill(L2Object obj, L2Skill skill, boolean srcIsSummon)
	{
		if (obj instanceof L2Character)
			if (GlobalRestrictions.isProtected(this, (L2Character) obj, skill, false))
				return false;

		// Check for PC->PC Pvp status
		if (obj != this && // Target is not self and
				obj instanceof L2Player && // Target is L2Player and
				!(getPlayerDuel().isInDuel() && ((L2Player) obj).getPlayerDuel().getDuelId() == getPlayerDuel().getDuelId()) && // Self is not in a duel and attacking opponent
				!isInsideZone(L2Zone.FLAG_PVP) && // Pc is not in PvP zone
				!((L2Player) obj).isInsideZone(L2Zone.FLAG_PVP) // Target is not in PvP zone
		)
		{
			L2Player target = (L2Player) obj;

			if (skill.isPvpSkill()) // Pvp skill
			{
				if (getClan() != null && target.getClan() != null)
				{
					if (getClan().isAtWarWith(target.getClan().getClanId())
							&& target.getClan().isAtWarWith(getClan().getClanId()))
						return true; // In clan war player can attack whites even with sleep etc.
				}
				if (target.getPvpFlag() == 0 && // Target's pvp flag is not set and
						target.getKarma() == 0 // Target has no karma
				)
					return false;
			}
			else if ((getCurrentSkill() != null && !getCurrentSkill().isCtrlPressed() && skill.isOffensive() && !srcIsSummon)
					|| (getCurrentPetSkill() != null && !getCurrentPetSkill().isCtrlPressed() && skill.isOffensive() && srcIsSummon))
			{
				if (getClan() != null && target.getClan() != null)
				{
					if(getClan().isAtWarWith(target.getClan().getClanId()) && target.getClan().isAtWarWith(getClan().getClanId()))
						return true; // In clan war player can attack whites even without ctrl
				}
				if (target.getPvpFlag() == 0 && // Target's pvp flag is not set and
						target.getKarma() == 0 // Target has no karma
				)
					return false;
			}
		}
		return true;
	}

	/**
	 * Return True if the L2Player is a Mage.<BR><BR>
	 */
	public boolean isMageClass()
	{
		return getClassId().isMage();
	}

	public boolean isMounted()
	{
		return _mountType > 0;
	}

	public boolean checkCanLand()
	{
		// Check if char is in a no landing zone
		if (isInsideZone(L2Zone.FLAG_NOWYVERN))
			return false;

		// If this is a castle that is currently being sieged, and the rider is NOT a castle owner
		// he cannot land.
		// Castle owner is the leader of the clan that owns the castle where the pc is
		return !(SiegeManager.getInstance().checkIfInZone(this)
				&& !(getClan() != null && CastleManager.getInstance().getCastle(this) == CastleManager.getInstance().getCastleByOwner(getClan()) && this == getClan()
						.getLeader().getPlayerInstance()));
	}

	/**
	 * Set the type of Pet mounted (0 : none, 1 : Stridder, 2 : Wyvern) and send a Server->Client packet InventoryUpdate to the L2Player.<BR><BR>
	 * @return false if the change of mount type false
	 */
	public boolean setMount(int npcId, int npcLevel, int mountType)
	{
		switch (mountType)
		{
		case 0:
			setIsRidingStrider(false);
			setIsRidingRedStrider(false);
			setIsRidingHorse(false);
			setIsFlying(false);
			isFalling(false, 0); // Initialize the fall just incase dismount was made while in-air
			break; // Dismounted
		case 1:
			if (npcId >= 12526 && npcId <= 12528)
			{
				setIsRidingStrider(true);
			}
			else if (npcId >= 16038 && npcId <= 16040)
			{
				setIsRidingRedStrider(true);
			}
			if (isNoble())
			{
				L2Skill striderAssaultSkill = SkillTable.getInstance().getInfo(325, 1);
				addSkill(striderAssaultSkill); // Not saved to DB
			}
			break;
		case 2:
			setIsFlying(true);
			break; // Flying Wyvern
		case 4:
			setIsRidingHorse(true);
			break;
		}

		_mountType = mountType;
		_mountNpcId = npcId;
		_mountLevel = npcLevel;

		return true;
	}

	/**
	 * @return the type of Pet mounted (0 : none, 1 : Strider, 2 : Wyvern, 3: Wolf).
	 */
	public int getMountType()
	{
		return _mountType;
	}

	/**
	 * Disable the Inventory and create a new task to enable it after 1.5s.<BR><BR>
	 */
	public void tempInventoryDisable()
	{
		_inventoryDisabled = true;

		ThreadPoolManager.getInstance().scheduleGeneral(new InventoryEnable(), 1500);
	}

	/**
	 * Return True if the Inventory is disabled.<BR><BR>
	 */
	public boolean isInventoryDisabled()
	{
		return _inventoryDisabled;
	}

	class InventoryEnable implements Runnable
	{
		public void run()
		{
			_inventoryDisabled = false;
		}
	}

	public Map<Integer, L2CubicInstance> getCubics()
	{
		return _cubics;
	}

	/**
	 * Add a L2CubicInstance to the L2Player _cubics.<BR><BR>
	 */
	public void addCubic(int id, int level, double matk, int activationtime, int activationchance, int totalLifeTime)
	{
		if (_log.isDebugEnabled())
			_log.info("L2Player(" + getName() + "): addCubic(" + id + "|" + level + "|" + matk + ")");
		L2CubicInstance cubic = new L2CubicInstance(this, id, level, (int) matk, activationtime, activationchance, totalLifeTime);
		_cubics.put(id, cubic);
	}

	public void addCubic(L2SkillSummon skill)
	{
		addCubic(skill.getNpcId(), skill.getLevel(), skill.getPower(), skill.getActivationTime(), skill
				.getActivationChance(), skill.getTotalLifeTime());
	}

	/**
	 * Remove a L2CubicInstance from the L2Player _cubics.<BR><BR>
	 */
	public void delCubic(int id)
	{
		_cubics.remove(id);
	}

	/**
	 * Return the L2CubicInstance corresponding to the Identifier of the L2Player _cubics.<BR><BR>
	 */
	public L2CubicInstance getCubic(int id)
	{
		return _cubics.get(id);
	}

	@Override
	public String toString()
	{
		return "player " + getName();
	}

	/**
	 * Return the modifier corresponding to the Enchant Effect of the Active Weapon (Min : 127).<BR><BR>
	 */
	public int getEnchantEffect()
	{
		L2ItemInstance wpn = getActiveWeaponInstance();

		if (wpn == null)
			return 0;

		return Math.min(127, wpn.getEnchantLevel());
	}

	/**
	 * Set the _lastFolkNpc of the L2Player corresponding to the last Folk wich one the player talked.<BR><BR>
	 */
	public void setLastFolkNPC(L2Npc folkNpc)
	{
		_lastFolkNpc = folkNpc;
	}

	/**
	 * Return the _lastFolkNpc of the L2Player corresponding to the last Folk wich one the player talked.<BR><BR>
	 */
	public L2Npc getLastFolkNPC()
	{
		return _lastFolkNpc;
	}

	/**
	 * Return True if L2Player is a participant in the Festival of Darkness.<BR><BR>
	 */
	public boolean isFestivalParticipant()
	{
		return SevenSignsFestival.getInstance().isParticipant(this);
	}

	private ScheduledFuture<?>	_taskWarnUserTakeBreak;

	class WarnUserTakeBreak implements Runnable
	{
		public void run()
		{
			sendPacket(SystemMessageId.PLAYING_FOR_LONG_TIME);
		}
	}

	class RentPetTask implements Runnable
	{
		public void run()
		{
			stopRentPet();
		}
	}

	class WaterTask implements Runnable
	{
		@Override
		public void run()
		{
			double reduceHp = getMaxHp() / 100.0;

			if (reduceHp < 1)
				reduceHp = 1;

			if(getStatus().reduceHp(reduceHp, L2Player.this, false, false, false))
			{
				// Reduced hp, because not rest
				SystemMessage sm = new SystemMessage(SystemMessageId.DROWN_DAMAGE_S1);
				sm.addNumber((int) reduceHp);
				sendPacket(sm);
			}
		}
	}

	public int getClanPrivileges()
	{
		return _clanPrivileges;
	}

	public void setClanPrivileges(int n)
	{
		_clanPrivileges = n;
	}

	// [L2J_JP ADD SANDMAN]
	public void enterMovieMode()
	{
		setTarget(null);
		stopMove(null);
		setIsInvul(true);
		setIsImmobilized(true);
		sendPacket(CameraMode.FIRST_PERSON);
	}

	public void leaveMovieMode()
	{
		if (!isGM())
			setIsInvul(false);
		setIsImmobilized(false);
		sendPacket(CameraMode.THIRD_PERSON);
	}

	/**
	 * yaw:North=90, south=270, east=0, west=180<BR>
	 * pitch > 0:looks up,pitch < 0:looks down<BR>
	 * time:faster that small value is.<BR>
	 */
	public void specialCamera(L2Object target, int dist, int yaw, int pitch, int time, int duration)
	{
		sendPacket(new SpecialCamera(target.getObjectId(), dist, yaw, pitch, time, duration));
	}

	// L2JJP END

	public int getTeleMode()
	{
		return _telemode;
	}

	public void setTeleMode(int mode)
	{
		_telemode = mode;
	}

	public void setLoto(int i, int val)
	{
		_loto[i] = val;
	}

	public int getLoto(int i)
	{
		return _loto[i];
	}

	public void setRace(int i, int val)
	{
		_race[i] = val;
	}

	public int getRace(int i)
	{
		return _race[i];
	}

	@Deprecated
	private void setBanChatTimer(long timer)
	{
	}

	@Deprecated
	private long getBanChatTimer()
	{
		return 0;
	}

	public boolean isChatBanned()
	{
		return ObjectRestrictions.getInstance().checkRestriction(this, AvailableRestriction.PlayerChat);
	}

	public boolean getMessageRefusal()
	{
		return _messageRefusal;
	}

	public void setMessageRefusal(boolean mode)
	{
		_messageRefusal = mode;
		sendEtcStatusUpdate();
	}

	public void setDietMode(boolean mode)
	{
		_dietMode = mode;
	}

	public boolean getDietMode()
	{
		return _dietMode;
	}

	public void setTradeRefusal(boolean mode)
	{
		_tradeRefusal = mode;
	}

	public boolean getTradeRefusal()
	{
		return _tradeRefusal;
	}

	public void setExchangeRefusal(boolean mode)
	{
		_exchangeRefusal = mode;
	}

	public boolean getExchangeRefusal()
	{
		return _exchangeRefusal;
	}

	public BlockList getBlockList()
	{
		if (_blockList == null)
			_blockList = new BlockList(this);

		return _blockList;
	}

	public L2FriendList getFriendList()
	{
		if (_friendList == null)
			_friendList = new L2FriendList(this);

		return _friendList;
	}

	public void setHero(boolean hero)
	{
		if (hero && _baseClass == _activeClass)
			for (L2Skill s : HeroSkillTable.getHeroSkills())
				addSkill(s, false); // Dont Save Hero skills to Sql
		else
			for (L2Skill s : HeroSkillTable.getHeroSkills())
				super.removeSkill(s); // Just Remove skills without deleting from Sql
		_hero = hero;
	}

	public boolean isHero()
	{
		return _hero;
	}

	public void setNoble(boolean val)
	{
		if (val)
			for (L2Skill s : NobleSkillTable.getNobleSkills())
				addSkill(s, false); // Dont Save Noble skills to Sql
		else
			for (L2Skill s : NobleSkillTable.getNobleSkills())
				super.removeSkill(s); // Just Remove skills without deleting from Sql
		_noble = val;
	}

	public boolean isNoble()
	{
		return _noble;
	}

	public int getSubLevel()
	{
		if (isSubClassActive())
		{
			int lvl = getLevel();
			return lvl;
		}
		return 0;
	}

	// Baron, Wise Man etc, calculated on EnterWorld and when rank is changing
	public void setPledgeClass(int classId)
	{
		_pledgeClass = classId;
	}

	public int getPledgeClass()
	{
		return _pledgeClass;
	}

	public void setSubPledgeType(int typeId)
	{
		_subPledgeType = typeId;
	}

	public int getSubPledgeType()
	{
		return _subPledgeType;
	}

	public int getPledgeRank()
	{
		return _pledgeRank;
	}

	public void setPledgeRank(int rank)
	{
		_pledgeRank = rank;
	}

	public int getApprentice()
	{
		return _apprentice;
	}

	public void setApprentice(int apprentice_id)
	{
		_apprentice = apprentice_id;
	}

	public int getSponsor()
	{
		return _sponsor;
	}

	public void setSponsor(int sponsor_id)
	{
		_sponsor = sponsor_id;
	}

	public void setLvlJoinedAcademy(int lvl)
	{
		_lvlJoinedAcademy = lvl;
	}

	public int getLvlJoinedAcademy()
	{
		return _lvlJoinedAcademy;
	}

	public boolean isAcademyMember()
	{
		return _lvlJoinedAcademy > 0;
	}

	public void setTeam(int team)
	{
		_team = team;
		if (getPet() != null)
			getPet().broadcastStatusUpdate();
	}

	public int getTeam()
	{
		return _team;
	}

	public void setWantsPeace(int wantsPeace)
	{
		_wantsPeace = wantsPeace;
	}

	public int getWantsPeace()
	{
		return _wantsPeace;
	}

	public void setAllianceWithVarkaKetra(int sideAndLvlOfAlliance)
	{
		// [-5,-1] varka, 0 neutral, [1,5] ketra
		_alliedVarkaKetra = sideAndLvlOfAlliance;
	}

	public int getAllianceWithVarkaKetra()
	{
		return _alliedVarkaKetra;
	}

	public boolean isAlliedWithVarka()
	{
		return (_alliedVarkaKetra < 0);
	}

	public boolean isAlliedWithKetra()
	{
		return (_alliedVarkaKetra > 0);
	}

	public final Comparator<L2Skill> SKILL_LIST_COMPARATOR = new Comparator<L2Skill>() {
		@Override
		public int compare(L2Skill s1, L2Skill s2)
		{
			int o1 = getOrder(s1);
			int o2 = getOrder(s2);
			
			if (o1 != o2)
				return (o1 < o2) ? -1 : 1;
			
			int so1 = getSubOrder(s1);
			int so2 = getSubOrder(s2);
			
			if (so1 != so2)
				return (so1 < so2) ? -1 : 1;
			
			return s1.getId().compareTo(s2.getId());
		}
		
		private int getOrder(L2Skill s)
		{
			if (s.getSkillType() == L2SkillType.NOTDONE)
				return 10;
			
			if (getPlayerTransformation().isTransformationDisabledSkill(s))
				return 9;
			
			// TODO: add other ordering conditions, if there is any other useful :)
			
			return 0;
		}
		
		private int getSubOrder(L2Skill s)
		{
			if (s.isPositive())
				return 1;
			else if (s.isNeutral())
				return 0;
			else // s.isOffensive()
				return -1;
		}
	};

	public L2Skill[] getSortedAllSkills(boolean isGM)
	{
		L2Skill[] array = getAllSkills();
		
		for (int i = 0; i < array.length; i++)
		{
			L2Skill s = array[i];
			
			if (s == null)
				continue;
			
			if (!isGM)
			{
				if (!s.canSendToClient())
				{
					array[i] = null;
					continue;
				}
				
				// Hide skills when transformed if they are not passive
				if (getPlayerTransformation().isTransformationDisabledSkill(s))
				{
					array[i] = null;
					continue;
				}
			}
			
			if (s.getSkillType() == L2SkillType.NOTDONE)
			{
				switch (Config.SEND_NOTDONE_SKILLS)
				{
					case 1:
					{
						array[i] = null;
						continue;
					}
					case 2:
					{
						if (!isGM)
						{
							array[i] = null;
							continue;
						}
					}
				}
			}
		}
		
		array = L2Arrays.compact(array);
		
		Arrays.sort(array, SKILL_LIST_COMPARATOR);
		
		return array;
	}

	public void sendSkillList()
	{
		addPacketBroadcastMask(BroadcastMode.SEND_SKILL_LIST);
	}

	public void sendSkillListImpl()
	{
		sendPacket(new SkillList(this));
	}

	/** Section for mounted pets */
	class FeedTask implements Runnable
	{
		public void run()
		{
			try
			{
				if (!isMounted())
				{
					stopFeed();
					return;
				}

				if (getCurrentFeed() > getFeedConsume())
				{
					// eat
					setCurrentFeed(getCurrentFeed()-getFeedConsume());
				}
				else
				{
					// go back to pet control item, or simply said, unsummon it
					setCurrentFeed(0);
					stopFeed();
					dismount();
					sendPacket(SystemMessageId.OUT_OF_FEED_MOUNT_CANCELED);
				}

				int[] foodIds = PetDataTable.getFoodItemId(getMountNpcId());
				if (foodIds[0] == 0)
					return;
				L2ItemInstance food = null;
				food = getInventory().getItemByItemId(foodIds[0]);

				// use better strider food if exists
				if (PetDataTable.isStrider(getMountNpcId()))
				{
					if (getInventory().getItemByItemId(foodIds[1]) != null)
						food = getInventory().getItemByItemId(foodIds[1]);
				}
				if (food != null && isHungry())
				{
					if (ItemHandler.getInstance().useItem(food.getItemId(), L2Player.this, food))
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY);
						sm.addItemName(food.getItemId());
						sendPacket(sm);
					}
				}
			}
			catch (Exception e)
			{
				_log.fatal("Mounted Pet [NpcId: "+getMountNpcId()+"] a feed task error has occurred", e);
			}
		}
	}

	protected synchronized void startFeed(int npcId)
	{
		_canFeed = npcId > 0;
		if (!isMounted())
			return;
		if (getPet() != null)
		{
			setCurrentFeed(((L2PetInstance) getPet()).getCurrentFed());
			_controlItemId = getPet().getControlItemId();
			sendPacket(new SetupGauge(3, getCurrentFeed()*10000/getFeedConsume(), getMaxFeed()*10000/getFeedConsume()));
			if (!isDead())
			{
				_mountFeedTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FeedTask(), 10000, 10000);
			}
		}
		else if (_canFeed)
		{
			setCurrentFeed(getMaxFeed());
			SetupGauge sg = new SetupGauge(3, getCurrentFeed()*10000/getFeedConsume(), getMaxFeed()*10000/getFeedConsume());
			sendPacket(sg);
			if (!isDead())
			{
				_mountFeedTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FeedTask(), 10000, 10000);
			}
		}
	}

	protected synchronized void stopFeed()
	{
		if (_mountFeedTask != null)
		{
			_mountFeedTask.cancel(false);
			_mountFeedTask = null;
			if (_log.isDebugEnabled())
				_log.info("Pet [#"+_mountNpcId+"] feed task stop");
		}
	}

	protected final void clearPetData()
	{
		_data = null;
	}

	protected final L2PetData getPetData(int npcId)
	{
		if (_data == null && getPet() != null)
		{
			_data = PetDataTable.getInstance().getPetData(getPet().getNpcId(), getPet().getLevel());
		}
		else if (_data == null && npcId > 0)
		{
			_data = PetDataTable.getInstance().getPetData(npcId, getLevel());
		}

		return _data;
	}

	public int getCurrentFeed()
	{
		return _curFeed;
	}

	protected int getFeedConsume()
	{
		// if pet is attacking
		if (isAttackingNow())
			return getPetData(_mountNpcId).getPetFeedBattle();
		else
			return getPetData(_mountNpcId).getPetFeedNormal();
	}

	public void setCurrentFeed(int num)
	{
		_curFeed = num > getMaxFeed() ? getMaxFeed() : num;
		SetupGauge sg = new SetupGauge(3, getCurrentFeed()*10000/getFeedConsume(), getMaxFeed()*10000/getFeedConsume());
		sendPacket(sg);
	}

	protected int getMaxFeed()
	{
		return getPetData(_mountNpcId).getPetMaxFeed();
	}

	protected boolean isHungry()
	{
		return _canFeed ? (getCurrentFeed() < (0.55 * getPetData(getMountNpcId()).getPetMaxFeed())) : false;
	}

	public class dismount implements Runnable
	{
		public void run()
		{
			dismount();
		}
	}

	public void enteredNoWyvernZone()
	{
		sendPacket(SystemMessageId.AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN);

		_dismountTask = ThreadPoolManager.getInstance().scheduleGeneral(new L2Player.dismount(), 5000);
	}

	public void exitedNoWyvernZone()
	{
		if (_dismountTask != null)
		{
			_dismountTask.cancel(false);
			_dismountTask = null;
		}
	}

	public void storePetFood(int petId)
	{
		if (_controlItemId != 0 && petId != 0)
		{
			String req = "UPDATE pets SET fed=? WHERE item_obj_id = ?";
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(req);
				statement.setInt(1, getCurrentFeed());
				statement.setInt(2, _controlItemId);
				statement.executeUpdate();
				statement.close();
				_controlItemId = 0;
			}
			catch (Exception e)
			{
				_log.fatal("Failed to store Pet [NpcId: "+petId+"] data", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}
	/** End of section for mounted pets */

	/**
	 * 1. Add the specified class ID as a subclass (up to the maximum number of <b>three</b>)
	 * for this character.<BR>
	 * 2. This method no longer changes the active _classIndex of the player. This is only
	 * done by the calling of setActiveClass() method as that should be the only way to do so.
	 *
	 * @param classId
	 * @param classIndex
	 * @return subclassAdded
	 */
	public boolean addSubClass(int classId, int classIndex)
	{
		if (!_subclassLock.tryLock())
			return false;
		
		try
		{
			if (getTotalSubClasses() == Config.ALT_MAX_SUBCLASS || classIndex == 0)
				return false;
			
			if (getSubClasses().containsKey(classIndex))
				return false;
			
			// Note: Never change _classIndex in any method other than setActiveClass().
			
			SubClass newClass = new SubClass();
			newClass.setClassId(classId);
			newClass.setClassIndex(classIndex);
			
			Connection con = null;
			try
			{
				// Store the basic info about this new sub-class.
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(ADD_CHAR_SUBCLASS);
				statement.setInt(1, getObjectId());
				statement.setInt(2, newClass.getClassId());
				statement.setLong(3, newClass.getExp());
				statement.setInt(4, newClass.getSp());
				statement.setInt(5, newClass.getLevel());
				statement.setInt(6, newClass.getClassIndex()); // <-- Added
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.warn("Could not add character sub class for " + getName() + ": ", e);
				return false;
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
			
			// Commit after database INSERT incase exception is thrown.
			getSubClasses().put(newClass.getClassIndex(), newClass);
			
			if (_log.isDebugEnabled())
				_log.info(getName() + " added class ID " + classId + " as a sub class at index " + classIndex + ".");
			
			ClassId subTemplate = ClassId.values()[classId];
			Iterable<L2SkillLearn> skillTree = SkillTreeTable.getInstance().getAllowedSkills(subTemplate);
			
			if (skillTree == null)
				return true;
			
			final Map<Integer, L2Skill> skills = new FastMap<Integer, L2Skill>();
			
			for (L2SkillLearn skillInfo : skillTree)
			{
				if (skillInfo.getMinLevel() <= 40)
				{
					final L2Skill prevSkill = skills.get(skillInfo.getId());
					final L2Skill newSkill = SkillTable.getInstance().getInfo(skillInfo.getId(), skillInfo.getLevel());
					
					if (prevSkill != null && prevSkill.getLevel() >= newSkill.getLevel())
						continue;
					
					skills.put(newSkill.getId(), newSkill);
				}
			}
			
			for (L2Skill skill : skills.values())
				_pcSkills.storeSkill(skill, classIndex);
			
			if (_log.isDebugEnabled())
				_log.info(getName() + " was given " + getAllSkills().length + " skills for their new sub class.");
			
			return true;
		}
		finally
		{
			_subclassLock.unlock();
		}
	}

	/**
	 * 1. Completely erase all existance of the subClass linked to the classIndex.<BR>
	 * 2. Send over the newClassId to addSubClass()to create a new instance on this classIndex.<BR>
	 * 3. Upon Exception, revert the player to their BaseClass to avoid further problems.<BR>
	 * 
	 * @param classIndex
	 * @param newClassId
	 * @return subclassAdded
	 */
	public boolean modifySubClass(int classIndex, int newClassId)
	{
		if (!_subclassLock.tryLock())
			return false;

		try
		{
			int oldClassId = getSubClasses().get(classIndex).getClassId();

			if (_log.isDebugEnabled())
				_log.info(getName() + " has requested to modify sub class index " + classIndex + " from class ID " + oldClassId + " to " + newClassId + ".");

			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();

				getPlayerHenna().removeAllHenna(classIndex, newClassId);

				// Remove all shortcuts info stored for this sub-class.
				getPlayerSettings().getShortCuts().deleteShortCuts(con, classIndex);

				// Remove all effects info stored for this sub-class.
				getEffects().deleteEffects(con, classIndex);

				// Remove all skill info stored for this sub-class.
				_pcSkills.deleteSkills(con, classIndex);

				// Remove all basic info stored about this sub-class.
				PreparedStatement statement = con.prepareStatement(DELETE_CHAR_SUBCLASS);
				statement.setInt(1, getObjectId());
				statement.setInt(2, classIndex);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.warn("Could not modify sub class for " + getName() + " to class index " + classIndex + ": ", e);
				// This must be done in order to maintain data consistency.
				getSubClasses().remove(classIndex);
				return false;
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}

			getSubClasses().remove(classIndex);
		}
		finally
		{
			_subclassLock.unlock();
		}

		return addSubClass(newClassId, classIndex);
	}

	public boolean isSubClassActive()
	{
		return _classIndex > 0;
	}

	public Map<Integer, SubClass> getSubClasses()
	{
		if (_subClasses == null)
			_subClasses = new SingletonMap<Integer, SubClass>();

		return _subClasses;
	}

	public int getTotalSubClasses()
	{
		return getSubClasses().size();
	}

	public int getBaseClass()
	{
		return _baseClass;
	}

	public int getActiveClass()
	{
		return _activeClass;
	}

	public int getClassIndex()
	{
		return _classIndex;
	}

	private void setClassTemplate(int classId)
	{
		_activeClass = classId;

		L2PcTemplate t = CharTemplateTable.getInstance().getTemplate(classId);

		if (t == null)
		{
			_log.fatal("Missing template for classId: " + classId);
			throw new Error();
		}

		// Set the template of the L2Player
		setTemplate(t);

		L2PartyRoom room = getPartyRoom();
		if (room != null)
			room.broadcastPacket(new ExManagePartyRoomMember(ExManagePartyRoomMember.MODIFIED, this));
	}

	/**
	 * Changes the character's class based on the given class index.
	 * <BR><BR>
	 * An index of zero specifies the character's original (base) class,
	 * while indexes 1-3 specifies the character's sub-classes respectively.
	 * <br><br>
	 * <font color="00FF00"/>WARNING: Use only on subclass change</font>
	 *
	 * @param classIndex
	 */
	public boolean setActiveClass(int classIndex)
	{
		if (!_subclassLock.tryLock())
			return false;

		try
		{
			//  Cannot switch or change subclasses while transformed
			if (getPlayerTransformation().isTransformed())
				return false;

			// Remove active item skills before saving char to database
			// because next time when choosing this class, weared items can be different

			for (L2ItemInstance temp : getInventory().getAugmentedItems())
				if (temp != null && temp.isEquipped())
					temp.getAugmentation().removeBonus(this);

			// Remove class circlets (can't equip circlets while being in subclass)
			L2ItemInstance circlet = getInventory().getPaperdollItem(Inventory.PAPERDOLL_HAIRALL);
			if (circlet != null)
			{
				if (((circlet.getItemId() >= 9397 && circlet.getItemId() <= 9408) || circlet.getItemId() == 10169) && circlet.isEquipped())
				{
					L2ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(circlet.getItem().getBodyPart());
					InventoryUpdate iu = new InventoryUpdate();
					for (L2ItemInstance element : unequipped)
						iu.addModifiedItem(element);
					sendPacket(iu);
				}
			}

			// Delete a force buff upon class change.
			if (_fusionSkill != null)
				abortCast();

			// Stop casting for any player that may be casting a force buff on this l2pcinstance.
			for (L2Character character : getKnownList().getKnownCharacters())
			{
				if(character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
					character.abortCast();
			}

			/*
			 * 1. Call store() before modifying _classIndex to avoid skill effects rollover.
			 * 2. Register the correct _classId against applied 'classIndex'.
			 */

			store(Config.STORE_EFFECTS_ON_SUBCLASS_CHANGE);

			// clear charges
			clearCharges();

			if (classIndex == 0)
			{
				setClassTemplate(getBaseClass());
			}
			else
			{
				try
				{
					setClassTemplate(getSubClasses().get(classIndex).getClassId());
				}
				catch (Exception e)
				{
					_log.info("Could not switch " + getName() + "'s sub class to class index " + classIndex + ": ", e);
					return false;
				}
			}
			_classIndex = classIndex;

			if (isInParty())
			{
				if (Config.MAX_PARTY_LEVEL_DIFFERENCE > 0)
				{
					for (L2Player p : getParty().getPartyMembers())
					{
						if (Math.abs(p.getLevel() - getLevel()) > Config.MAX_PARTY_LEVEL_DIFFERENCE)
						{
							getParty().removePartyMember(this);
							sendMessage("You have been removed from your party, because the level difference is too big.");
							break;
						}
					}
				}
				else
					getParty().recalculatePartyLevel();
			}

			/*
			 * Update the character's change in class status.
			 *
			 * 1. Remove any active cubics from the player.
			 * 2. Renovate the characters table in the database with the new class info, storing also buff/effect data.
			 * 3. Remove all existing skills.
			 * 4. Restore all the learned skills for the current class from the database.
			 * 5. Restore effect/buff data for the new class.
			 * 6. Restore henna data for the class, applying the new stat modifiers while removing existing ones.
			 * 7. Reset HP/MP/CP stats and send Server->Client character status packet to reflect changes.
			 * 8. Restore shortcut data related to this class.
			 * 9. Resend a class change animation effect to broadcast to all nearby players.
			 * 10.Unsummon any active servitor from the player.
			 */

			if (getPet() instanceof L2SummonInstance)
				getPet().unSummon(this);

			if (!getCubics().isEmpty())
			{
				for (L2CubicInstance cubic : getCubics().values())
				{
					cubic.stopAction();
					cubic.cancelDisappear();
				}

				getCubics().clear();
			}

			abortCast();

			for (L2Skill oldSkill : getAllSkills())
				super.removeSkill(oldSkill);

			stopAllEffectsExceptThoseThatLastThroughDeath();

			getPlayerRecipe().restoreRecipeBook(false);

			// Restore any Death Penalty Buff
			restoreDeathPenaltyBuffLevel();

			restoreSkills();
			regiveTemporarySkills();
			rewardSkills();

			getEffects().restoreEffects();
			updateEffectIcons();

			// If player has quest "422: Repent Your Sins", remove it
			QuestState st = getQuestState("422_RepentYourSins");
			if (st != null)
			{
				st.exitQuest(true);
			}

			getPlayerHenna().setHennasEmptyOnSubclassChange();

			getPlayerHenna().restoreHenna();
			sendPacket(new HennaInfo(this));

			checkItemRestriction();

			if (getCurrentHp() > getMaxHp())
				getStatus().setCurrentHp(getMaxHp());
			if (getCurrentMp() > getMaxMp())
				getStatus().setCurrentMp(getMaxMp());
			if (getCurrentCp() > getMaxCp())
				getStatus().setCurrentCp(getMaxCp());

			getInventory().restoreEquipedItemsPassiveSkill();
			getInventory().restoreArmorSetPassiveSkill();

			refreshOverloaded();
			refreshExpertisePenalty();
			broadcastUserInfo();

			// Clear resurrect xp calculation
			setExpBeforeDeath(0);

			//getMacroses().restore();
			//getMacroses().sendUpdate();

			getPlayerSettings().getShortCuts().restore();
			sendPacket(new ShortCutInit(this));

			broadcastPacket(new SocialAction(this, SocialAction.LEVEL_UP));
			sendSkillCoolTime();
			sendPacket(new ExStorageMaxCount(this));

			broadcastClassIcon();

			return true;
		}
		finally
		{
			_subclassLock.unlock();
		}
	}

	public boolean isLocked()
	{
		return _subclassLock.isLocked();
	}

	public void broadcastClassIcon()
	{
		// Update class icon in party and clan
		if (isInParty())
			getParty().broadcastToPartyMembers(new PartySmallWindowUpdate(this));

		if (getClan() != null)
			getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
	}

	public void stopWarnUserTakeBreak()
	{
		if (_taskWarnUserTakeBreak != null)
		{
			_taskWarnUserTakeBreak.cancel(false);
			_taskWarnUserTakeBreak = null;
		}
	}

	public void startWarnUserTakeBreak()
	{
		if (_taskWarnUserTakeBreak == null)
			_taskWarnUserTakeBreak = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new WarnUserTakeBreak(), 7200000, 7200000);
	}

	public void stopRentPet()
	{
		if (_taskRentPet != null)
		{
			// If the rent of a wyvern expires while over a flying zone, tp to down before unmounting
			if (getMountType() == 2 && !checkCanLand())
				teleToLocation(TeleportWhereType.Town);

			if (dismount()) // This should always be true now, since we teleported already
			{
				_taskRentPet.cancel(true);
				_taskRentPet = null;
			}
		}
	}

	public void startRentPet(int seconds)
	{
		if (_taskRentPet == null)
			_taskRentPet = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RentPetTask(), seconds * 1000L, seconds * 1000);
	}

	public boolean isRentedPet()
	{
		return _taskRentPet != null;
	}

	public void stopWaterTask()
	{
		if (_taskWater != null)
		{
			_taskWater.cancel(false);

			_taskWater = null;
			sendPacket(new SetupGauge(SetupGauge.CYAN, 0));
			// Added to sync fall when swimming stops:
			// (e.g. catacombs players swim down and then they fell when they got out of the water).
			isFalling(false, 0);
		}

		broadcastUserInfo();
	}

	public void startWaterTask()
	{
		// Temp fix here
		if (isMounted())
			dismount();

		if (getPlayerTransformation().isTransformed() && !isCursedWeaponEquipped())
			getPlayerTransformation().untransform();
		// TODO: update to only send speed status when that packet is known
		else
			broadcastUserInfo();

		if (!isDead() && _taskWater == null)
		{
			int timeinwater = (int) calcStat(Stats.BREATH, 60000, this, null);

			sendPacket(new SetupGauge(2, timeinwater));
			_taskWater = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new WaterTask(), timeinwater, 1000);
		}
	}

	public boolean isInWater()
	{
		return _taskWater != null;
	}

	public void onPlayerEnter()
	{
		startWarnUserTakeBreak();
		startAutoSaveTask();

		if (SevenSigns.getInstance().isSealValidationPeriod() || SevenSigns.getInstance().isCompResultsPeriod())
		{
			if (!isGM() && isIn7sDungeon() && Config.ALT_STRICT_SEVENSIGNS
					&& SevenSigns.getInstance().getPlayerCabal(this) != SevenSigns.getInstance().getCabalHighestScore())
			{
				teleToLocation(TeleportWhereType.Town);
				setIsIn7sDungeon(false);
				sendPacket(SystemMessage.sendString("You have been teleported to the nearest town due to the beginning of the Seal Validation period."));
			}
		}
		else
		{
			if (!isGM() && isIn7sDungeon() && Config.ALT_STRICT_SEVENSIGNS && SevenSigns.getInstance().getPlayerCabal(this) == SevenSigns.CABAL_NULL)
			{
				teleToLocation(TeleportWhereType.Town);
				setIsIn7sDungeon(false);
				sendPacket(SystemMessage.sendString("You have been teleported to the nearest town because you have not signed for any cabal."));
			}
		}

		// Jail task
		updateJailState();

		revalidateZone(true);
	}

	public void checkWaterState()
	{
		if (isInsideZone(L2Zone.FLAG_WATER))
		{
			startWaterTask();
		}
		else
		{
			stopWaterTask();
		}
	}

	public long getLastAccess()
	{
		return _lastAccess;
	}

	@Override
	public void doRevive()
	{
		super.doRevive();
		stopEffects(L2EffectType.CHARMOFCOURAGE);
		updateEffectIcons();
		_reviveRequested = false;
		
		if (isMounted())
			startFeed(_mountNpcId);
		
		if (isInParty() && getParty().isInDimensionalRift())
		{
			if (!DimensionalRiftManager.getInstance().checkIfInPeaceZone(getX(), getY(), getZ()))
				getParty().getDimensionalRift().memberRessurected(this);
		}
	}
	
	@Override
	public void doRevive(double revivePower)
	{
		// Restore the player's lost experience,
		// depending on the % return of the skill used (based on its power).
		restoreExp(revivePower);
		doRevive();
	}
	
	public void reviveRequest(L2Player reviver, L2Skill skill)
	{
		if (isResurrectionBlocked())
			return;
		
		if (_reviveRequested || _revivePetRequested)
		{
			reviver.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
			return;
		}
		if (isDead())
		{
			_reviveRequested = true;
			
			final double revivePower;
			if (isPhoenixBlessed())
				revivePower = 100;
			else if (skill != null)
				revivePower = Formulas.calculateSkillResurrectRestorePercent(skill, reviver);
			else
				revivePower = 0;
			
			int restoreExp = (int)Math.round((getExpBeforeDeath() - getExp()) * revivePower / 100);
			
			if (getCharmOfCourage())
			{
				ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.RESURRECT_USING_CHARM_OF_COURAGE);
				dlg.addTime(60000);
				dlg.addAnswerHandler(new AnswerHandler() {
					@Override
					public void handle(boolean answer)
					{
						reviveAnswer(answer, revivePower);
					}
				});
				sendPacket(dlg);
				return;
			}
			
			ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.RESSURECTION_REQUEST_BY_C1_FOR_S2_XP);
			dlg.addPcName(reviver);
			dlg.addString(String.valueOf(restoreExp));
			dlg.addAnswerHandler(new AnswerHandler() {
				@Override
				public void handle(boolean answer)
				{
					reviveAnswer(answer, revivePower);
				}
			});
			sendPacket(dlg);
		}
	}
	
	public void revivePetRequest(L2Player reviver, L2Skill skill)
	{
		if (_reviveRequested || _revivePetRequested)
		{
			reviver.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
			return;
		}
		
		if (getPet().isDead() && getPet() instanceof L2PetInstance)
		{
			_revivePetRequested = true;
			
			final double revivePower;
			if (skill != null)
				revivePower = Formulas.calculateSkillResurrectRestorePercent(skill, reviver);
			else
				revivePower = 0;
			
			int restoreExp = (int)Math.round((((L2PetInstance)getPet()).getExpBeforeDeath() - getPet().getStat().getExp()) * revivePower / 100);
			
			ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.RESSURECTION_REQUEST_BY_C1_FOR_S2_XP);
			dlg.addAnswerHandler(new AnswerHandler() {
				@Override
				public void handle(boolean answer)
				{
					reviveAnswer(answer, revivePower);
				}
			});
			sendPacket(dlg.addPcName(reviver).addString("" + restoreExp));
		}
	}
	
	public void reviveAnswer(boolean answer, double revivePower)
	{
		if (!(_reviveRequested && isDead() || _revivePetRequested && getPet() != null && getPet().isDead()))
			return;
		// If character refuses a PhoenixBless autoress, cancel all buffs he had
		if (!answer && isPhoenixBlessed() && isDead() && _reviveRequested)
		{
			stopPhoenixBlessing(true);
			stopAllEffectsExceptThoseThatLastThroughDeath();
		}
		
		if (answer)
		{
			if (_reviveRequested)
			{
				if (revivePower != 0)
					doRevive(revivePower);
				else
					doRevive();
			}
			else if (_revivePetRequested && getPet() != null)
			{
				if (revivePower != 0)
					getPet().doRevive(revivePower);
				else
					getPet().doRevive();
			}
		}
		_reviveRequested = false;
		_revivePetRequested = false;
	}
	
	public boolean isReviveRequested()
	{
		return _reviveRequested;
	}
	
	public boolean isPetReviveRequested()
	{
		return _revivePetRequested;
	}
	
	public void removeReviving()
	{
		_reviveRequested = false;
	}
	
	public void removePetReviving()
	{
		_revivePetRequested = false;
	}
	
	public void onActionRequest()
	{
		setProtection(false);
	}

	/**
	 * @param expertiseIndex The expertiseIndex to set.
	 */
	public void setExpertiseIndex(int expertiseIndex)
	{
		_expertiseIndex = expertiseIndex;
	}

	/**
	 * @return Returns the expertiseIndex.
	 */
	public int getExpertiseIndex()
	{
		return _expertiseIndex;
	}
	
	@Override
	public final void teleToLocation(int x, int y, int z, int heading, boolean allowRandomOffset)
	{
		if (getVehicle() != null && !getVehicle().isTeleporting())
			setVehicle(null);
		
		super.teleToLocation(x, y, z, heading, allowRandomOffset);
	}

	@Override
	public boolean onTeleported()
	{
		if (!super.onTeleported())
			return false;

		getKnownList().updateKnownObjects();

		setProtection(true);

		// Trained beast is after teleport lost
		if (getTrainedBeast() != null)
		{
			getTrainedBeast().decayMe();
			setTrainedBeast(null);
		}
		// Modify the position of the pet if necessary
		L2Summon pet = getPet();
		if (pet != null)
		{
			pet.setFollowStatus(false);
			getPet().teleToLocation(getPosition().getX(), getPosition().getY(), getPosition().getZ(), false);
			((L2SummonAI)getPet().getAI()).setStartFollowController(true);
			pet.setFollowStatus(true);
			getPet().broadcastFullInfoImpl(0);
		}

		sendPacket(new UserInfo(this));
		return true;
	}

	@Override
	public void setIsTeleporting(boolean teleport)
	{
		super.setIsTeleporting(teleport);
		if (teleport)
		{
			if (Config.TELEPORT_WATCHDOG_TIMEOUT > 0 && _teleportWatchdog == null)
				_teleportWatchdog = ThreadPoolManager.getInstance().scheduleGeneral(new TeleportWatchdog(), Config.TELEPORT_WATCHDOG_TIMEOUT * 1000);
		}
		else if (_teleportWatchdog != null)
		{
			_teleportWatchdog.cancel(false);
			_teleportWatchdog = null;
		}
	}

	private class TeleportWatchdog implements Runnable
	{
		@Override
		public void run()
		{
			if (!isTeleporting() || getOnlineState() == L2Player.ONLINE_STATE_DELETED)
				return;

			if (_log.isDebugEnabled())
				_log.debug("Player " + getName() + " teleport timeout expired");
			onTeleported();
		}
	}
	
	public void teleToLocation(TeleportWhereType teleportWhere)
	{
		teleToLocation(MapRegionManager.getInstance().getTeleToLocation(this, teleportWhere), true);
	}

	@Override
	public void addExpAndSp(long addToExp, int addToSp)
	{
		if (!canGainExpSp())
			return;
		
		getStat().addExpAndSp(addToExp, addToSp, false);
	}

	public void addExpAndSp(long addToExp, int addToSp, boolean useVitality)
	{
		if (!canGainExpSp())
			return;
		
		getStat().addExpAndSp(addToExp, addToSp, useVitality);
	}

	public void removeExpAndSp(long removeExp, int removeSp)
	{
		getStat().removeExpAndSp(removeExp, removeSp);
	}

	public void removeExpAndSp(long removeExp, int removeSp, boolean sendMessage)
	{
		getStat().removeExpAndSp(removeExp, removeSp, sendMessage);
	}

	/**
	 * Function is used in the PLAYER, calls snoop for all GMs listening to this player speak.
	 *
	 * @param channel - msg channel of the snooped player
	 * @param name - name of snooped player
	 * @param text - the msg the snooped player sent/received
	 */
	public void broadcastSnoop(SystemChatChannelId channel, String name, String text)
	{
		if (_snoopers.length == 0)
			return;

		final Snoop sn = new Snoop(this, channel, name, text);

		for (L2Player snooper : _snoopers)
			snooper.sendPacket(sn);
	}

	/**
	 * Adds a spy ^^ GM to the player listener.
	 *
	 * @param pci - GM char that listens to the conversation
	 */
	public void addSnooper(L2Player snooper)
	{
		if (!ArrayUtils.contains(_snoopers, snooper))
			_snoopers = (L2Player[])ArrayUtils.add(_snoopers, snooper);
	}

	public void removeSnooper(L2Player snooper)
	{
		_snoopers = (L2Player[])ArrayUtils.removeElement(_snoopers, snooper);
	}

	public void removeSnooped(L2Player snooped)
	{
		_snoopedPlayers = (L2Player[])ArrayUtils.removeElement(_snoopedPlayers, snooped);
	}

	/**
	 * Adds a player to the GM queue for being listened.
	 * @param pci - player we listen to
	 */
	public void addSnooped(L2Player snooped)
	{
		if (!ArrayUtils.contains(_snoopedPlayers, snooped))
		{
			_snoopedPlayers = (L2Player[])ArrayUtils.add(_snoopedPlayers, snooped);

			sendPacket(new Snoop(snooped, SystemChatChannelId.Chat_Normal, "", "*** Starting snooping of player " + snooped.getName() + " ***"));
		}
	}

	public synchronized void buildBypassCache(final Replaceable replaceable)
	{
		if (_validBypass != null)
			_validBypass.clear();
		
		if (_validBypass2 != null)
			_validBypass2.clear();
		
		for (int i = 0; i < replaceable.length(); i++)
		{
			int start = replaceable.indexOf("\"bypass ", i);
			int finish = replaceable.indexOf("\"", start + 1);
			
			if (start < 0 || finish < 0)
				break;
			
			if (replaceable.substring(start + 8, start + 10).equals("-h"))
				start += 11;
			else
				start += 8;

			i = finish;
			int finish2 = replaceable.indexOf("$", start);
			
			if (0 < finish2 && finish2 < finish)
			{
				if (_validBypass2 == null)
					_validBypass2 = new ArrayList<String>();
				
				_validBypass2.add(replaceable.substring(start, finish2).trim());
			}
			else
			{
				if (_validBypass == null)
					_validBypass = new ArrayList<String>();
				
				_validBypass.add(replaceable.substring(start, finish).trim());
			}
		}
	}

	public synchronized void validateBypass(String cmd) throws InvalidPacketException
	{
		if (_validBypass != null)
			for (String bp : _validBypass)
				if (bp != null && cmd.equals(bp))
					return;

		if (_validBypass2 != null)
			for (String bp : _validBypass2)
				if (bp != null && cmd.startsWith(bp))
					return;

		throw new InvalidPacketException("[" + this + "] sent invalid bypass '" + cmd + "'!");
	}

	public synchronized void buildLinkCache(final Replaceable replaceable)
	{
		if (_validLink != null)
			_validLink.clear();

		int length = replaceable.length();
		for (int i = 0; i < length; i++)
		{
			int start = replaceable.indexOf("\"link ", i);
			if (start != -1)
				start++;

			int finish = replaceable.indexOf("\"", start);

			if (start < 0 || finish < 0)
				break;

			i = finish;

			if (_validLink == null)
				_validLink = new ArrayList<String>();

			_validLink.add(replaceable.substring(start + 5, finish).trim());
		}
	}

	public synchronized void validateLink(String cmd) throws InvalidPacketException
	{
		if (_validLink != null)
			for (String bp : _validLink)
				if (bp != null && cmd.equals(bp))
					return;

		throw new InvalidPacketException("[" + this + "] sent invalid link '" + cmd + "'!");
	}

	/**
	 * Performs following tests:<br>
	 * <li> Inventory contains item
	 * <li> Item owner id == this.owner id
	 * <li> It isnt pet control item while mounting pet or pet summoned
	 * <li> It isnt active enchant item
	 * <li> It isnt cursed weapon/item
	 * <li> It isnt wear item
	 * <br>
	 * 
	 * @param objectId item object id
	 * @param action just for logging
	 * @return
	 */
	public boolean validateItemManipulation(int objectId, String action)
	{
		L2ItemInstance item = getInventory().getItemByObjectId(objectId);

		if (item == null || item.getOwnerId() != getObjectId())
		{
			_log.debug(getObjectId() + ": player tried to " + action + " item he is not owner of");
			return false;
		}

		// Pet is summoned and not the item that summoned the pet AND not the buggle from strider you're mounting
		if (getPet() != null && getPet().getControlItemId() == objectId || getMountObjectID() == objectId)
		{
			if (_log.isDebugEnabled())
				_log.debug(getObjectId() + ": player tried to " + action + " item controling pet");

			return false;
		}

		if (getActiveEnchantItem() != null && getActiveEnchantItem().getObjectId() == objectId)
		{
			if (_log.isDebugEnabled())
				_log.debug(getObjectId() + ":player tried to " + action + " an enchant scroll he was using");

			return false;
		}

		if (CursedWeaponsService.getInstance().isCursed(item.getItemId()))
		{
			// Cannot trade a cursed weapon
			return false;
		}

		return !item.isWear();
	}

	/**
	 * @return Returns the inBoat.
	 */
	public boolean isInBoat()
	{
		return _vehicle != null && _vehicle.isBoat();
	}

	/**
	 * @return
	 */
	public L2BoatInstance getBoat()
	{
		return (L2BoatInstance) _vehicle;
	}

	/**
	 * @return Returns the inAirShip.
	 */
	public boolean isInAirShip()
	{
		return _vehicle != null && _vehicle.isAirShip();
	}

	/**
	 * @return
	 */
	public L2AirShipInstance getAirShip()
	{
		return (L2AirShipInstance) _vehicle;
	}

	public L2Vehicle getVehicle()
	{
		return _vehicle;
	}

	public void setVehicle(L2Vehicle vehicle)
	{
		if (vehicle == null && _vehicle != null)
			_vehicle.removePassenger(this);

		_vehicle = vehicle;
	}

	public void setInCrystallize(boolean inCrystallize)
	{
		_inCrystallize = inCrystallize;
	}

	public boolean isInCrystallize()
	{
		return _inCrystallize;
	}

	/**
	 * @return
	 */
	public Point3D getInVehiclePosition()
	{
		return _inVehiclePosition;
	}

	public void setInVehiclePosition(Point3D pt)
	{
		_inVehiclePosition = pt;
	}

	/**
	 * Manage the delete task of a L2Player (Leave Party, Unsummon pet, Save its inventory in the database, Remove it from the world...).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the L2Player is in observer mode, set its position to its position before entering in observer mode </li>
	 * <li>Set the online Flag to True or False and update the characters table of the database with online status and lastAccess </li>
	 * <li>Stop the HP/MP/CP Regeneration task </li>
	 * <li>Cancel Crafting, Attak or Cast </li>
	 * <li>Remove the L2Player from the world </li>
	 * <li>Stop Party and Unsummon Pet </li>
	 * <li>Update database with items in its inventory and remove them from the world </li>
	 * <li>Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify AI </li>
	 * <li>Close the connection with the client </li><BR><BR>
	 *
	 */
	@Deprecated
	public void deleteMe()
	{
		storeAndDeleteMe();
	}
	
	public void storeAndDeleteMe()
	{
		final HashSet<L2Zone> before = getZonesPlayerIn();

		if (getOnlineState() == ONLINE_STATE_DELETED)
			return;

		// Pause restrictions
		ObjectRestrictions.getInstance().pauseTasks(getObjectId());

		abortCast();
		abortAttack();

		try
		{
			if (isFlying())
				removeSkill(SkillTable.getInstance().getInfo(4289, 1));
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		try
		{
			L2ItemInstance flag = getInventory().getItemByItemId(9819);
			if (flag != null)
			{
				Fort fort = FortManager.getInstance().getFort(this);
				if (fort != null)
					FortSiegeManager.getInstance().dropCombatFlag(this);
				else
				{
					int slot = flag.getItem().getBodyPart();
					getInventory().unEquipItemInBodySlotAndRecord(slot);
					destroyItem("CombatFlag", flag, null, true);
				}
			}
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		// If the L2Player has Pet, unsummon it
		if (getPet() != null)
		{
			try
			{
				getPet().unSummon(this);
				// dead pet wasnt unsummoned, broadcast npcinfo changes (pet will be without owner name - means owner offline)
				if (getPet() != null)
					getPet().broadcastFullInfoImpl(0);
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}// Return pet to the control item
		}

		// Cancel trade
		if (getActiveRequester() != null)
		{
			getActiveRequester().onTradeCancel(this);
			onTradeCancel(getActiveRequester());

			cancelActiveTrade();

			setActiveRequester(null);
		}

		// Check if the L2Player is in observer mode to set its position to its position before entering in observer mode
		if (getPlayerObserver().inObserverMode())
			getPosition().setXYZ(getPlayerObserver().getObsX(), getPlayerObserver().getObsY(), getPlayerObserver().getObsZ());
		
		if (getVehicle() != null)
			getVehicle().oustPlayer(this);

		Castle castle = null;
		if (getClan() != null) {
			castle = CastleManager.getInstance().getCastleByOwner(getClan());
			if (castle != null)
				castle.destroyClanGate();
		}

		// Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout)
		try
		{
			setOnlineStatus(false);
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		// Stop the HP/MP/CP Regeneration task (scheduled tasks)
		try
		{
			stopAllTimers();
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		GlobalRestrictions.playerDisconnected(this);

		try
		{
			setIsTeleporting(false);
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		// Stop crafting, if in progress
		try
		{
			RecipeService.getInstance().requestMakeItemAbort(this);
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		try
		{
			setTarget(null);
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		if (_throne != null)
			_throne.setOccupier(null);
		_throne = null;

		try
		{
			if (_fusionSkill != null)
			{
				abortCast();
			}
			for (L2Character character : getKnownList().getKnownCharacters())
				if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
					character.abortCast();
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		getEffects().stopAllEffects(true);

		// Remove from world regions zones
		L2WorldRegion oldRegion = getWorldRegion();

		// Remove the L2Player from the world
		if (isVisible())
		{
			try
			{
				decayMe();
			}
			catch (Exception e)
			{
				_log.fatal(e.getMessage(), e);
			}
		}

		if (oldRegion != null)
			oldRegion.removeFromZones(this);

		// If a Party is in progress, leave it (and festival party)
		if (isInParty())
		{
			try
			{
				// If player is festival participant and it is in progress
				// notify party members that the player is not longer a participant.
				if (isFestivalParticipant() && SevenSignsFestival.getInstance().isFestivalInitialized())
				{
					getParty().broadcastToPartyMembers(SystemMessage.sendString(getName() + " has been removed from the upcoming festival."));
				}

				leaveParty();
			}
			catch (Exception e)
			{
				_log.fatal(e.getMessage(), e);
			}
		}
		else // if in party, already taken care of
		{
			L2PartyRoom room = getPartyRoom();
			if (room != null)
				room.removeMember(this, false);
		}
		PartyRoomManager.getInstance().removeFromWaitingList(this);

		if (getClanId() != 0 && getClan() != null)
		{
			// Set the status for pledge member list to OFFLINE
			try
			{
				L2ClanMember clanMember = getClan().getClanMember(getName());
				if (clanMember != null)
					clanMember.setPlayerInstance(null);
			}
			catch (Exception e)
			{
				_log.fatal(e.getMessage(), e);
			}
		}

		if (getActiveRequester() != null)
		{
			// Deals with sudden exit in the middle of transaction
			setActiveRequester(null);
		}

		// If the L2Player is a GM, remove it from the GM List
		if (isGM())
		{
			try
			{
				GmListTable.deleteGm(this);
			}
			catch (Exception e)
			{
				_log.fatal(e.getMessage(), e);
			}
		}

		// remove player from instance and set spawn location if any
		try
		{
			if (isInInstance())
			{
				final Instance inst = InstanceManager.getInstance().getInstance(getInstanceId());
				if (inst != null)
				{
					inst.removePlayer(getObjectId());
					final Location spawn = inst.getSpawnLoc();
					if (spawn != null)
					{
						final int x = spawn.getX() + Rnd.get(-30, 30);
						final int y = spawn.getY() + Rnd.get(-30, 30);
						getPosition().setXYZ(x, y, spawn.getZ());
						if (getPet() != null) // dead pet
						{
							getPet().teleToLocation(x, y, spawn.getZ());
							// ??? unset pet's instance id, but not players
							getPet().decayMe();
							getPet().spawnMe();
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		// Update database with items in its inventory and remove them from the world
		try
		{
			getInventory().deleteMe();
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		// Update database with items in its warehouse and remove them from the world
		try
		{
			clearWarehouse();
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}
		
		try
		{
			clearRefund();
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		// Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify AI
		try
		{
			getKnownList().removeAllKnownObjects();
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		getPlayerTransformation().untransform();

		if (getClanId() > 0)
			getClan().broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(this), this);

		if (_snoopedPlayers.length > 0)
		{
			for (L2Player snooped : _snoopedPlayers)
				snooped.removeSnooper(this);
			_snoopedPlayers = L2Player.EMPTY_ARRAY;
		}

		if (_snoopers.length > 0)
		{
			broadcastSnoop(SystemChatChannelId.Chat_Normal, "", "*** Player " + getName() + " logged off ***");
			for (L2Player snooper : _snoopers)
				snooper.removeSnooped(this);
			_snoopers = L2Player.EMPTY_ARRAY;
		}

		for (Integer objId : getFriendList().getFriendIds())
		{
			L2Player friend = L2World.getInstance().findPlayer(objId);
			if (friend != null)
			{
				friend.sendPacket(new FriendList(friend));
				friend.sendMessage("Friend: " + getName() + " has logged off.");
			}
		}

		MovementController.getInstance().remove(this);

		// Remove L2Object object from _allObjects of L2World, if still in it
		L2World.getInstance().removeObject(this);

		try
		{
			// To delete the player from L2World on crit during teleport ;)
			setIsTeleporting(false);

			L2World.getInstance().removeOnlinePlayer(this);
		}
		catch (RuntimeException e)
		{
			_log.fatal( "deleteMe()", e);
		}
		
		notifyFriends();

		//getClearableReference().clear();
		LeakTaskManager.getInstance().add(this);

		SQLQueue.getInstance().run();

		final HashSet<L2Zone> after = getZonesPlayerIn();

		if (!after.isEmpty())
		{
			_log.warn("Leaking zones before L2Player.deleteMe(): " + before.toString());
			_log.warn("Leaking zones after L2Player.deleteMe(): " + after.toString());
		}
	}

	private HashSet<L2Zone> getZonesPlayerIn()
	{
		final HashSet<L2Zone> set = new HashSet<L2Zone>();

		for (L2Zone[] zones : ZoneManager.getInstance().getZones())
			if (zones != null)
				for (L2Zone zone : zones)
					if (zone.getCharactersInside().contains(this))
						set.add(zone);

		return set;
	}

	public boolean canLogout()
	{
		return canLogout(false);
	}

	public boolean canLogout(boolean restart)
	{
		if (!isGM() || !Config.GM_RESTART_FIGHTING)
		{
			if (AttackStanceTaskManager.getInstance().getAttackStanceTask(this))
			{
				if (restart)
					sendPacket(SystemMessageId.CANT_RESTART_WHILE_FIGHTING);
				else
					sendPacket(SystemMessageId.CANT_LOGOUT_WHILE_FIGHTING);
				return false;
			}
		}

		if (isFlying())
		{
			sendPacket(SystemMessageId.CANNOT_DO_WHILE_MOUNTED);
			return false;
		}

		L2Summon summon = getPet();

		if (summon != null && summon instanceof L2PetInstance && !summon.isBetrayed() && summon.isAttackingNow())
		{
			sendPacket(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE);
			return false;
		}

		// Prevent player from restarting if they are a festival participant
		if (isFestivalParticipant() /* && SevenSignsFestival.getInstance().isFestivalInitialized()*/)
		{
			sendMessage("You can't logout while you are a participant in a festival.");
			return false;
		}

		if (getPrivateStoreType() != 0)
		{
			sendMessage("You can't logout while trading.");
			return false;
		}

		if (getActiveEnchantItem() != null || getActiveEnchantAttrItem() != null)
			return false;

		if (isLocked())
		{
			sendMessage("You can't logout while changing class.");
			return false;
		}

		if (!isGM())
		{
			if (isInsideZone(L2Zone.FLAG_NOESCAPE))
			{
				sendPacket(SystemMessageId.NO_LOGOUT_HERE);
				teleToLocation(TeleportWhereType.Town);
			}
		}

		return true;
	}

	public int getInventoryLimit()
	{
		int ivlim;
		if (isGM())
		{
			ivlim = Config.INVENTORY_MAXIMUM_GM;
		}
		else if (getRace() == Race.Dwarf)
		{
			ivlim = Config.INVENTORY_MAXIMUM_DWARF;
		}
		else
		{
			ivlim = Config.INVENTORY_MAXIMUM_NO_DWARF;
		}
		ivlim += (int) getStat().calcStat(Stats.INV_LIM, 0, null, null);

		return ivlim;
	}

	public int getWareHouseLimit()
	{
		int whlim;
		if (getRace() == Race.Dwarf)
		{
			whlim = Config.WAREHOUSE_SLOTS_DWARF;
		}
		else
		{
			whlim = Config.WAREHOUSE_SLOTS_NO_DWARF;
		}
		whlim += (int) getStat().calcStat(Stats.WH_LIM, 0, null, null);

		return whlim;
	}

	public int getPrivateSellStoreLimit()
	{
		int pslim;
		if (getRace() == Race.Dwarf)
		{
			pslim = Config.MAX_PVTSTORESELL_SLOTS_DWARF;
		}
		else
		{
			pslim = Config.MAX_PVTSTORESELL_SLOTS_OTHER;
		}
		pslim += (int) getStat().calcStat(Stats.P_SELL_LIM, 0, null, null);

		return pslim;
	}

	public int getPrivateBuyStoreLimit()
	{
		int pblim;
		if (getRace() == Race.Dwarf)
		{
			pblim = Config.MAX_PVTSTOREBUY_SLOTS_DWARF;
		}
		else
		{
			pblim = Config.MAX_PVTSTOREBUY_SLOTS_OTHER;
		}
		pblim += (int) getStat().calcStat(Stats.P_BUY_LIM, 0, null, null);

		return pblim;
	}

	public int getDwarfRecipeLimit()
	{
		int recdlim = Config.ALT_DWARF_RECIPE_LIMIT;
		recdlim += (int) getStat().calcStat(Stats.REC_D_LIM, 0, null, null);
		return recdlim;
	}

	public int getCommonRecipeLimit()
	{
		int recclim = Config.ALT_COMMON_RECIPE_LIMIT;
		recclim += (int) getStat().calcStat(Stats.REC_C_LIM, 0, null, null);
		return recclim;
	}

	/**
	 * @return Returns the mountNpcId.
	 */
	public int getMountNpcId()
	{
		return _mountNpcId;
	}

	/**
	 * @return Returns the mountLevel.
	 */
	public int getMountLevel()
	{
		return _mountLevel;
	}

	public void setMountObjectID(int newID)
	{
		_mountObjectID = newID;
	}

	public int getMountObjectID()
	{
		return _mountObjectID;
	}

	public SkillUsageRequest getCurrentPetSkill()
	{
		final L2Summon pet = getPet();

		return pet == null ?  null : pet.getCurrentSkill();
	}

	public boolean isMaried()
	{
		return _maried;
	}

	public void setMaried(boolean state)
	{
		_maried = state;
	}

	public void setMaryRequest(boolean state)
	{
		_maryrequest = state;
	}

	public boolean isMary()
	{
		return _maryrequest;
	}

	public void setMaryAccepted(boolean state)
	{
		_maryaccepted = state;
	}

	public boolean isMaryAccepted()
	{
		return _maryaccepted;
	}

	public int getPartnerId()
	{
		return _partnerId;
	}

	public void setPartnerId(int partnerid)
	{
		_partnerId = partnerid;
	}

	public int getCoupleId()
	{
		return _coupleId;
	}

	public void setCoupleId(int coupleId)
	{
		_coupleId = coupleId;
	}

	public void setClientRevision(int clientrev)
	{
		_clientRevision = clientrev;
	}

	public int getClientRevision()
	{
		return _clientRevision;
	}

	public boolean isInJail()
	{
		return _inJail;
	}

	public void setInJail(boolean state)
	{
		//setInJail(state, 0);
		_inJail = state;
	}

	public void setInJail(boolean state, int delayInMinutes)
	{
		_inJail = state;
		// Remove the task if any
		stopJailTask(false);

		if (_inJail)
		{
			if (delayInMinutes > 0)
			{
				_jailTimer = delayInMinutes * 60000L; // In millisec

				// Start the countdown
				_jailTask = ThreadPoolManager.getInstance().scheduleGeneral(new JailTask(), _jailTimer);
				sendMessage("You are in jail for " + delayInMinutes + " minutes.");
			}

			// Open a Html message to inform the player
			NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
			String jailInfos = HtmCache.getInstance().getHtm("data/npc_data/html/jail_in.htm");
			if (jailInfos != null)
				htmlMsg.setHtml(jailInfos);
			else
				htmlMsg.setHtml("<html><body>You have been put in jail by an admin.</body></html>");
			sendPacket(htmlMsg);

			if (isFlyingMounted())
				getPlayerTransformation().untransform();
			setInstanceId(0);
			setIsIn7sDungeon(false);

			teleToLocation(L2JailZone.JAIL_LOCATION, false); // Jail
		}
		else
		{
			// Open a Html message to inform the player
			NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
			String jailInfos = HtmCache.getInstance().getHtm("data/npc_data/html/jail_out.htm");
			if (jailInfos != null)
				htmlMsg.setHtml(jailInfos);
			else
				htmlMsg.setHtml("<html><body>You are free for now, respect server rules!</body></html>");
			sendPacket(htmlMsg);

			teleToLocation(17836, 170178, -3507); // Floran
		}

		// Store in database
		storeCharBase();

	}

	public long getJailTimer()
	{
		if (_jailTask != null)
			return _jailTask.getDelay(TimeUnit.MILLISECONDS);

		return _jailTimer;
	}

	public void setJailTimer(long time)
	{
		_jailTimer = time;
	}

	private void updateJailState()
	{
		if (isInJail())
		{
			// If jail time is elapsed, free the player
			if (_jailTimer > 0)
			{
				// Restart the countdown
				_jailTask = ThreadPoolManager.getInstance().scheduleGeneral(new JailTask(), _jailTimer);
				sendMessage("You are still in jail for " + Math.round(_jailTimer / 60000) + " minutes.");
			}

			// If player escaped, put him back in jail
			if (!isInsideZone(L2Zone.FLAG_JAIL))
				teleToLocation(L2JailZone.JAIL_LOCATION, false);
		}
	}

	public void stopJailTask(boolean save)
	{
		if (_jailTask != null)
		{
			if (save)
			{
				long delay = _jailTask.getDelay(TimeUnit.MILLISECONDS);
				if (delay < 0)
					delay = 0;
				setJailTimer(delay);
			}
			_jailTask.cancel(false);
			_jailTask = null;
		}
	}

	/**
	 * Return True if the L2Player is a ViP.<BR><BR>
	 */
	public boolean isCharViP()
	{
		return _charViP;
	}

	/**
	 * Set the _charViP Flag of the L2Player.<BR><BR>
	 */
	public void setCharViP(boolean status)
	{
		_charViP = status;
	}

	private ScheduledFuture<?>	_jailTask;
	private int					_cursedWeaponEquippedId	= 0;
	private boolean				_combatFlagEquipped		= false;

	private boolean				_reviveRequested		= false;
	private boolean				_revivePetRequested		= false;

	private double				_cpUpdateIncCheck		= .0;
	private double				_cpUpdateDecCheck		= .0;
	private double				_cpUpdateInterval		= .0;
	private double				_mpUpdateIncCheck		= .0;
	private double				_mpUpdateDecCheck		= .0;
	private double				_mpUpdateInterval		= .0;

	private class JailTask implements Runnable
	{
		public void run()
		{
			setInJail(false, 0);
		}
	}

	public void restoreHPMP()
	{
		getStatus().setCurrentHpMp(getMaxHp(), getMaxMp());
	}

	public boolean isCursedWeaponEquipped()
	{
		return _cursedWeaponEquippedId != 0;
	}

	public void setCursedWeaponEquippedId(int value)
	{
		_cursedWeaponEquippedId = value;

	}

	public int getCursedWeaponEquippedId()
	{
		return _cursedWeaponEquippedId;
	}

	public boolean isCombatFlagEquipped()
	{
		return _combatFlagEquipped;
	}

	public void setCombatFlagEquipped(boolean value)
	{
		_combatFlagEquipped = value;
	}

	public boolean getCharmOfCourage()
	{
		return _charmOfCourage;
	}

	public void setCharmOfCourage(boolean val)
	{
		_charmOfCourage = val;
		sendEtcStatusUpdate();
	}

	/** Return True if the L2Character is riding. */
	public final boolean isRidingStrider()
	{
		return _isRidingStrider;
	}

	public final boolean isRidingRedStrider()
	{
		return _isRidingRedStrider;
	}

	public final boolean isRidingHorse()
	{
		return _isRidingHorse;
	}

	/** Set the L2Character riding mode to True. */
	public final void setIsRidingStrider(boolean mode)
	{
		_isRidingStrider = mode;
	}

	public final void setIsRidingRedStrider(boolean mode)
	{
		_isRidingRedStrider = mode;
	}

	public final void setIsRidingHorse(boolean mode)
	{
		_isRidingHorse = mode;
	}

	public int getCharges()
	{
		return _charges;
	}

	private static final int[] CHARGE_SKILLS = {570, 8, 50}; // Transformation skill is checked first

	public L2Skill getChargeSkill()
	{
		for (int id : L2Player.CHARGE_SKILLS)
		{
			L2Skill skill = getKnownSkill(id);
			if (skill != null && skill.getMaxCharges() > 0)
			{
				return skill;
			}
		}
		return null;
	}

	public void increaseCharges(int count, int max)
	{
		if (count <= 0) // Wrong usage
			return;

		// Checking charges maximum
		if (_charges >= max)
		{
			sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
			return;
		}

		// Increase charges
		setCharges(Math.min(_charges + count, max));

		SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
		sm.addNumber(_charges);
		sendPacket(sm);
	}

	public void increaseChargesBySkill(L2Skill skill)
	{
		if (skill.getGiveCharges() > 0)
			increaseCharges(skill.getGiveCharges(), skill.getMaxCharges());
	}

	private void setCharges(int charges)
	{
		_charges = Math.max(0, charges);

		sendEtcStatusUpdate();
		if (_charges == 0)
			stopChargeTask();
		else
			restartChargeTask();
	}

	public void decreaseCharges(int count)
	{
		if (count < 0) // Wrong usage
			return;
		setCharges(_charges - count);
	}

	public class ChargeTask implements Runnable
	{
		public void run()
		{
			clearCharges();
		}
	}

	/**
	 * Clear out all charges from this L2Player
	 */
	public void clearCharges()
	{
		setCharges(0);
	}

	/**
	 * Starts/Restarts the SoulTask to Clear Charges after 10 Mins.
	 */
	private void restartChargeTask()
	{
		if (_chargeTask != null)
		{
			_chargeTask.cancel(false);
			_chargeTask = null;
		}
		_chargeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChargeTask(), 600000);
	}

	/**
	 * Stops the Clearing Task.
	 */
	public void stopChargeTask()
	{
		if (_chargeTask != null)
		{
			_chargeTask.cancel(false);
			_chargeTask = null;
		}
	}

	/**
	 * Returns the Number of Souls this L2Player got.
	 *
	 * @return
	 */
	public int getSouls()
	{
		return _souls;
	}

	public int getLastSoulConsume()
	{
		return _lastSoulConsume;
	}

	public void resetLastSoulConsume()
	{
		_lastSoulConsume = 0;
	}

	/**
	 * Absorbs a Soul from a Npc.
	 *
	 * @param skill
	 * @param target
	 */
	public void absorbSoulFromNpc(L2Skill soulMastery, L2Character target)
	{
		if (_souls >= soulMastery.getNumSouls())
		{
			sendPacket(SystemMessageId.SOUL_CANNOT_BE_INCREASED_ANYMORE);
			return;
		}

		increaseSouls(1);

		// Npc -> Player absorb animation
		if (target != null)
			broadcastPacket(new ExSpawnEmitter(getObjectId(), target.getObjectId()), 500);
	}

	/**
	 * Increase Souls
	 *
	 * @param count
	 */
	private void increaseSouls(int count) // By skill or mob kill
	{
		setSouls(_souls + count);

		SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_SOUL_HAS_INCREASED_BY_S1_SO_IT_IS_NOW_AT_S2);
		sm.addNumber(count);
		sm.addNumber(_souls);
		sendPacket(sm);
	}

	public void increaseSoulsBySkill(L2Skill skill)
	{
		if (skill.getNumSouls() == 0)
			return;

		final L2Skill soulmastery = getKnownSkill(L2Skill.SKILL_SOUL_MASTERY);
		if (soulmastery == null)
			return;

		if (_souls >= soulmastery.getNumSouls())
		{
			sendPacket(SystemMessageId.SOUL_CANNOT_BE_INCREASED_ANYMORE);
			return;
		}

		increaseSouls(Math.min(skill.getNumSouls(), soulmastery.getNumSouls() - getSouls()));
	}

	/**
	 * Decreases existing Souls.
	 *
	 * @param skill
	 */
	public void decreaseSouls(L2Skill skill)
	{
		if (_souls == 0)
			return;

		// Calculation part
		int souls = _souls;
		if (skill.getSoulConsumeCount() > 0)
		{
			souls -= skill.getSoulConsumeCount();
		}
		else if (skill.getMaxSoulConsumeCount() > 0)
		{
			int consume = Math.min(_souls, skill.getMaxSoulConsumeCount());
			souls -= consume;
			_lastSoulConsume = consume; // Store for PDAM/MDAM boosting
		}

		setSouls(souls);
	}

	public void setSouls(int count)
	{
		_souls = L2Math.limit(0, count, 45); // Client can't display more

		if (_souls > 0)
			restartSoulTask();
		else
			stopSoulTask();

		sendEtcStatusUpdate();
	}

	private class SoulTask implements Runnable
	{
		public void run()
		{
			clearSouls();
		}
	}

	/**
	 * Clear out all Souls from this L2Player
	 */
	public void clearSouls()
	{
		setSouls(0);
	}

	/**
	 * Starts/Restarts the SoulTask to Clear Souls after 10 Mins.
	 */
	private void restartSoulTask()
	{
		if (_soulTask != null)
		{
			_soulTask.cancel(false);
			_soulTask = null;
		}
		_soulTask = ThreadPoolManager.getInstance().scheduleGeneral(new SoulTask(), 600000);
	}

	/**
	 * Stops the Clearing Task.
	 */
	public void stopSoulTask()
	{
		if (_soulTask != null)
		{
			_soulTask.cancel(false);
			_soulTask = null;
		}
	}

	public void startFameTask(long delay, int fameFixRate)
	{
		if (getLevel() < 40 || getClassId().level() < 2)
			return;
		if (_fameTask == null)
			_fameTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FameTask(fameFixRate), delay, delay);
	}

	public void stopFameTask()
	{
		if (_fameTask != null)
		{
			_fameTask.cancel(false);
			_fameTask = null;
		}
	}

	public class FameTask implements Runnable
	{
		protected int _value;

		protected FameTask(int value)
		{
			_value = value;
		}

		public void run()
		{
			if (isDead() && !Config.ALT_FAME_FOR_DEAD_PLAYERS)
				return;

			setFame(getFame() + _value);
			SystemMessage sm = new SystemMessage(SystemMessageId.ACQUIRED_S1_REPUTATION_SCORE);
			sm.addNumber(_value);
			sendPacket(sm);
			sendPacket(new UserInfo(L2Player.this));
		}
	}

	private L2Effect _shortBuff;

	public void startShortBuffStatusUpdate(L2Effect effect)
	{
		if (ShortBuffStatusUpdate.getPriority(_shortBuff) > ShortBuffStatusUpdate.getPriority(effect))
			return;

		_shortBuff = effect;

		sendPacket(new ShortBuffStatusUpdate(effect.getId(), effect.getLevel(), (int)effect.getRemainingTaskTime()));
	}

	public void stopShortBuffStatusUpdate(L2Effect effect)
	{
		if (_shortBuff != effect)
			return;

		_shortBuff = null;

		sendPacket(new ShortBuffStatusUpdate(0, 0, 0));
	}

	public int getDeathPenaltyBuffLevel()
	{
		return _deathPenaltyBuffLevel;
	}

	public void setDeathPenaltyBuffLevel(int level)
	{
		_deathPenaltyBuffLevel = level;
	}

	public void calculateDeathPenaltyBuffLevel(L2Character killer)
	{
		if (Config.DEATH_PENALTY_CHANCE < 1)
			return;

		if (!(killer instanceof L2Playable) && !isGM() && !(getCharmOfLuck() && killer.isRaid())
				&& !isPhoenixBlessed() && (getKarma() > 0 || Rnd.get(100) < Config.DEATH_PENALTY_CHANCE))
			increaseDeathPenaltyBuffLevel();
	}

	public void increaseDeathPenaltyBuffLevel()
	{
		if (getDeathPenaltyBuffLevel() >= 15) // Maximum level reached
			return;

		if (getDeathPenaltyBuffLevel() != 0)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());

			if (skill != null)
				removeSkill(skill, true);
		}

		_deathPenaltyBuffLevel++;

		addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
		sendEtcStatusUpdate();
		SystemMessage sm = new SystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED);
		sm.addNumber(getDeathPenaltyBuffLevel());
		sendPacket(sm);
	}

	public void reduceDeathPenaltyBuffLevel()
	{
		if (getDeathPenaltyBuffLevel() <= 0)
			return;

		L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());

		if (skill != null)
			removeSkill(skill, true);

		_deathPenaltyBuffLevel--;

		if (getDeathPenaltyBuffLevel() > 0)
		{
			addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
			SystemMessage sm = new SystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED);
			sm.addNumber(getDeathPenaltyBuffLevel());
			sendPacket(sm);
		}
		else
		{
			sendPacket(SystemMessageId.DEATH_PENALTY_LIFTED);
		}

		sendEtcStatusUpdate();
	}

	public void restoreDeathPenaltyBuffLevel()
	{
		L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());

		if (skill != null)
			removeSkill(skill, true);

		if (getDeathPenaltyBuffLevel() > 0)
		{
			addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
		}
	}

	private boolean _canFeed;

	private final Map<Integer, TimeStamp> _reuseTimeStamps = new FastMap<Integer, TimeStamp>().shared();

	public Map<Integer, TimeStamp> getReuseTimeStamps()
	{
		return _reuseTimeStamps;
	}

	public void sendSkillCoolTime()
	{
		addPacketBroadcastMask(BroadcastMode.SEND_SKILL_COOL_TIME);
	}

	public void sendSkillCoolTimeImpl()
	{
		sendPacket(SkillCoolTime.STATIC_PACKET);
	}

	/**
	 * Simple class containing all neccessary information to maintain
	 * valid timestamps and reuse for skills upon relog. Filter this
	 * carefully as it becomes redundant to store reuse for small delays.
	 * @author  Yesod
	 */
	public static final class TimeStamp
	{
		private final int _skillId;
		private final int _reuseDelay;
		private final long _expiration;

		public TimeStamp(int skillId, int reuseDelay, int remaining)
		{
			_skillId = skillId;
			_reuseDelay = reuseDelay;
			_expiration = System.currentTimeMillis() + remaining;
		}

		public long getExpiration()
		{
			return _expiration;
		}

		public int getSkillId()
		{
			return _skillId;
		}

		public int getReuseDelay()
		{
			return _reuseDelay;
		}

		public int getRemaining()
		{
			return L2Math.limit(0, _expiration - System.currentTimeMillis(), Integer.MAX_VALUE);
		}
	}

	public void disableSkill(TimeStamp ts)
	{
		disableSkill(ts.getSkillId(), ts.getReuseDelay(), ts.getRemaining());
	}

	public boolean disableSkill(int skillId, int delay, int remaining)
	{
		if (!super.disableSkill(skillId, remaining))
			return false;

		//if (remaining < 10000)
		//	return true;

		final TimeStamp ts = getReuseTimeStamps().put(skillId, new TimeStamp(skillId, delay, remaining));
		final SkillUsageRequest request = getCurrentSkill();

		if (ts == null || Math.abs(ts.getReuseDelay() - delay) > 500 || Math.abs(ts.getRemaining() - remaining) > 500)
			if (request == null || request.getSkillId() != skillId)
				sendSkillCoolTime();
		return true;
	}

	@Override
	public boolean disableSkill(int skillId, int delay)
	{
		return disableSkill(skillId, delay, delay);
	}

	@Override
	public void enableSkill(int skillId)
	{
		super.enableSkill(skillId);

		final TimeStamp ts = getReuseTimeStamps().remove(skillId);

		if (ts != null && ts.getRemaining() > 500)
			sendSkillCoolTime();
	}

	public boolean isKamaelic()
	{
		return getRace() == Race.Kamael;
	}

	public boolean canOpenPrivateStore()
	{
		return !isAlikeDead() && !getPlayerOlympiad().isInOlympiadMode() && !isMounted();
	}

	public void tryOpenPrivateBuyStore()
	{
		// Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
		if (canOpenPrivateStore())
		{
			if (getPrivateStoreType() == L2Player.STORE_PRIVATE_BUY || getPrivateStoreType() == L2Player.STORE_PRIVATE_BUY + 1)
			{
				setPrivateStoreType(L2Player.STORE_PRIVATE_NONE);
			}
			else if (isInsideZone(L2Zone.FLAG_NOSTORE))
			{
				sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if (getPrivateStoreType() == L2Player.STORE_PRIVATE_NONE)
			{
				if (isSitting())
				{
					this.standUp();
				}
				setPrivateStoreType(L2Player.STORE_PRIVATE_BUY + 1);
				this.sendPacket(new PrivateStoreManageListBuy(this));
			}
		}
		else
		{
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	public void tryOpenPrivateSellStore(boolean isPackageSale)
	{
		// Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
		if (canOpenPrivateStore())
		{
			if (getPrivateStoreType() == L2Player.STORE_PRIVATE_SELL || getPrivateStoreType() == L2Player.STORE_PRIVATE_SELL + 1
					|| getPrivateStoreType() == L2Player.STORE_PRIVATE_PACKAGE_SELL)
			{
				setPrivateStoreType(L2Player.STORE_PRIVATE_NONE);
			}
			else if (isInsideZone(L2Zone.FLAG_NOSTORE))
			{
				sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (getPrivateStoreType() == L2Player.STORE_PRIVATE_NONE)
			{
				if (isSitting())
				{
					this.standUp();
				}
				setPrivateStoreType(L2Player.STORE_PRIVATE_SELL + 1);
				this.sendPacket(new PrivateStoreManageListSell(this, isPackageSale));
			}
		}
		else
		{
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	/**
	 *
	 * @param npcId
	 */
	public void setAgathionId(int npcId)
	{
		_agathionId = npcId;
	}

	/**
	 *
	 * @return
	 */
	public int getAgathionId()
	{
		return _agathionId;
	}

	public L2StaticObjectInstance getObjectSittingOn()
	{
		return _throne;
	}

	public void setObjectSittingOn(L2StaticObjectInstance throne)
	{
		//prevent misuse
		if (throne == null)
			resetThrone();
		else
			_throne = throne;
	}

	public void resetThrone()
	{
		if (_throne == null) return;
		_throne.setOccupier(null);
		_throne = null;
	}

	private ImmutableReference<L2Player> _immutableReference;
	private ClearableReference<L2Player> _clearableReference;

	public ImmutableReference<L2Player> getImmutableReference()
	{
		if (_immutableReference == null)
			_immutableReference = new ImmutableReference<L2Player>(this);

		return _immutableReference;
	}

	public ClearableReference<L2Player> getClearableReference()
	{
		if (_clearableReference == null)
			_clearableReference = new ClearableReference<L2Player>(this);

		return _clearableReference;
	}

	@Override
	public final L2Player getActingPlayer()
	{
		return this;
	}

	@Override
	public final L2Summon getActingSummon()
	{
		return getPet();
	}

	/**
	 * Set the Fame of this L2PcInstane <BR><BR>
	 * @param fame
	 */
	public void setFame(int fame)
	{
		if (fame > Config.MAX_PERSONAL_FAME_POINTS)
			_fame = Config.MAX_PERSONAL_FAME_POINTS;
		else
			_fame = fame;
	}

	/**
	 * Return the Fame of this L2Player <BR><BR>
	 * @return
	 */
	public int getFame()
	{
		return _fame;
	}

	private ScheduledFuture<?> _autoSaveTask;

	private void startAutoSaveTask()
	{
		if (_autoSaveTask == null && Config.CHAR_STORE_INTERVAL > 0)
			_autoSaveTask = ThreadPoolManager.getInstance().schedule(new AutoSave(), 300000);
	}

	private void stopAutoSaveTask()
	{
		if (_autoSaveTask != null)
		{
			_autoSaveTask.cancel(false);
			_autoSaveTask = null;
		}
	}

	private final class AutoSave implements Runnable
	{
		public void run()
		{
			long period = Config.CHAR_STORE_INTERVAL * 60000L;
			long delay = _lastStore + period - System.currentTimeMillis();

			if (delay <= 0)
			{
				try
				{
					store();
				}
				catch (RuntimeException e)
				{
					_log.fatal("", e);
				}

				delay = period;
			}

			if (Config.CHAR_STORE_INTERVAL > 0)
				_autoSaveTask = ThreadPoolManager.getInstance().schedule(this, delay);
		}
	}

	public void checkItemRestriction()
	{
		for (int i = 0; i < Inventory.PAPERDOLL_TOTALSLOTS; i++)
		{
			L2ItemInstance equippedItem = getInventory().getPaperdollItem(i);
			if (equippedItem != null && !equippedItem.getItem().checkCondition(this, false))
			{
				getInventory().unEquipItemInSlotAndRecord(i);
				if (equippedItem.isWear())
					continue;

				SystemMessage sm = null;
				if (equippedItem.getItem().getBodyPart() == L2Item.SLOT_BACK)
					sm = SystemMessageId.CLOAK_REMOVED_BECAUSE_ARMOR_SET_REMOVED.getSystemMessage();
				else if (equippedItem.getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
					sm.addNumber(equippedItem.getEnchantLevel());
					sm.addItemName(equippedItem);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_DISARMED);
					sm.addItemName(equippedItem);
				}
				sendPacket(sm);
			}
		}
	}

	@Override
	public void setName(String name)
	{
		super.setName(name);

		CharNameTable.getInstance().update(this);
	}

	// =========================================================================================
	// Condition listeners
	// TODO: wrapper conditions - ConditionLogicAnd, ConditionLogicOr, etc
	// TODO: make it more similar to conventional java listeners
	// TODO: add every listener which makes sense - currently listens for hp percent and game time only

	public enum ConditionListenerDependency
	{
		PLAYER_HP,
		GAME_TIME,
		;
	}

	private abstract class ConditionListener
	{
		private final Map<Func, Boolean> _values = new SingletonMap<Func, Boolean>().shared();
		private final Env _env;

		protected ConditionListener()
		{
			_env = new Env();
			_env.setPlayer(L2Player.this);
		}

		protected void refresh(ConditionListenerDependency dependency)
		{
			for (Entry<Func, Boolean> entry : _values.entrySet())
			{
				boolean newValue = entry.getKey().isAllowed(_env);
				boolean oldValue = entry.setValue(newValue);

				if (newValue != oldValue)
					onChange(entry.getKey(), newValue);
			}
		}

		protected void onChange(Func f, boolean newValue)
		{
			sendMessage(f.funcOwner.getFuncOwnerName() + (newValue ? " activated." : " deactivated."));
		}

		protected void onFuncAddition(Func f)
		{
			final boolean newValue = f.isAllowed(_env);

			_values.put(f, newValue);

			if (newValue)
				onChange(f, true);
		}

		protected void onFuncRemoval(Func f)
		{
			_values.remove(f);
		}
	}

	private final class ConditionPlayerHpListener extends ConditionListener
	{
		@Override
		protected void onFuncAddition(Func f)
		{
			if (f.condition instanceof ConditionPlayerHp)
				super.onFuncAddition(f);
		}

		@Override
		protected void refresh(ConditionListenerDependency dependency)
		{
			if (dependency == ConditionListenerDependency.PLAYER_HP)
				super.refresh(dependency);
		}

		@Override
		protected void onChange(Func f, boolean newValue)
		{
			final SystemMessage sm;

			if (newValue)
				sm = new SystemMessage(SystemMessageId.S1_HP_DECREASED_EFFECT_APPLIES);
			else
				sm = new SystemMessage(SystemMessageId.S1_HP_DECREASED_EFFECT_DISAPPEARS);

			if (f.funcOwner.getFuncOwnerSkill() != null)
				sm.addSkillName(f.funcOwner.getFuncOwnerSkill());
			else
				sm.addString(f.funcOwner.getFuncOwnerName());

			sendPacket(sm);

			broadcastUserInfo();
		}
	}

	private final class ConditionGameTimeListener extends ConditionListener
	{
		@Override
		protected void onFuncAddition(Func f)
		{
			if (f.condition instanceof ConditionGameTime)
				super.onFuncAddition(f);
		}

		@Override
		protected void refresh(ConditionListenerDependency dependency)
		{
			if (dependency == ConditionListenerDependency.GAME_TIME)
				super.refresh(dependency);
		}

		@Override
		protected void onChange(Func f, boolean newValue)
		{
			final SystemMessage sm;

			if (newValue)
				sm = new SystemMessage(SystemMessageId.S1_NIGHT_EFFECT_APPLIES);
			else
				sm = new SystemMessage(SystemMessageId.S1_NIGHT_EFFECT_DISAPPEARS);

			if (f.funcOwner.getFuncOwnerSkill() != null)
				sm.addSkillName(f.funcOwner.getFuncOwnerSkill());
			else
				sm.addString(f.funcOwner.getFuncOwnerName());

			sendPacket(sm);

			broadcastUserInfo();
		}
	}

	private ConditionListener[] _conditionListeners;

	private ConditionListener[] getConditionListeners()
	{
		if (_conditionListeners == null)
			_conditionListeners = new ConditionListener[] { new ConditionPlayerHpListener(), new ConditionGameTimeListener() };

		return _conditionListeners;
	}

	public void onFuncAddition(Func f)
	{
		for (ConditionListener listener : getConditionListeners())
			listener.onFuncAddition(f);
	}

	public void onFuncRemoval(Func f)
	{
		for (ConditionListener listener : getConditionListeners())
			listener.onFuncRemoval(f);
	}

	public void refreshConditionListeners(ConditionListenerDependency dependency)
	{
		for (ConditionListener listener : getConditionListeners())
			listener.refresh(dependency);
	}

	// =========================================================================================

	public void sendEtcStatusUpdate()
	{
		addPacketBroadcastMask(BroadcastMode.SEND_ETC_STATUS_UPDATE);
	}

	public void sendEtcStatusUpdateImpl()
	{
		sendPacket(EtcStatusUpdate.STATIC_PACKET);
	}

	public void broadcastRelationChanged()
	{
		addPacketBroadcastMask(BroadcastMode.BROADCAST_RELATION_CHANGED);
	}

	public void broadcastRelationChangedImpl()
	{
		if (!getKnownList().getKnownPlayers().isEmpty())
			for (L2Player player : getKnownList().getKnownPlayers().values())
				RelationChanged.sendRelationChanged(this, player);
	}

	@Override
	public void sendInfo(L2Player activeChar)
	{
		if (isInBoat())
			getPosition().setWorldPosition(getBoat().getPosition());
		else if (isInAirShip())
			getPosition().setWorldPosition(getAirShip().getPosition());

		if (getPoly().isMorphed())
		{
			activeChar.sendPacket(new AbstractNpcInfo.PcMorphInfo(this, getPoly().getNpcTemplate()));
		}
		else
		{
			activeChar.sendPacket(new CharInfo(this));
			//activeChar.sendPacket(new ExBrExtraUserInfo(this));
		}

		if (isInBoat())
			activeChar.sendPacket(new GetOnVehicle(this, getBoat(), getInVehiclePosition().getX(), getInVehiclePosition().getY(), getInVehiclePosition().getZ()));
		else if (isInAirShip())
			activeChar.sendPacket(new ExGetOnAirShip(this, getAirShip()));

		if (getMountType() == 4)
		{
			// TODO: Remove when horse mounts fixed
			activeChar.sendPacket(new Ride(this, false, 0));
			activeChar.sendPacket(new Ride(this, true, getMountNpcId()));
		}
		switch (getPrivateStoreType())
		{
		case L2Player.STORE_PRIVATE_SELL:
		{
			activeChar.sendPacket(new PrivateStoreMsgSell(this));
			break;
		}
		case L2Player.STORE_PRIVATE_PACKAGE_SELL:
		{
			activeChar.sendPacket(new ExPrivateStoreSetWholeMsg(this));
			break;
		}
		case L2Player.STORE_PRIVATE_BUY:
		{
			activeChar.sendPacket(new PrivateStoreMsgBuy(this));
			break;
		}
		case L2Player.STORE_PRIVATE_MANUFACTURE:
		{
			activeChar.sendPacket(new RecipeShopMsg(this));
			break;
		}
		}
	}

	@Override
	public void broadcastFullInfoImpl()
	{
		sendPacket(new UserInfo(this));
		if (getPoly().isMorphed())
		{
			Broadcast.toKnownPlayers(this, new AbstractNpcInfo.PcMorphInfo(this, getPoly().getNpcTemplate()));
		}
		else
		{
			Broadcast.toKnownPlayers(this, new CharInfo(this));
		}
	}

	@Override
	protected boolean shouldAddPacketBroadcastMask()
	{
		return getOnlineState() != L2Player.ONLINE_STATE_LOADED;
	}

	/**
	 * @return afro haircut id
	 */
	public int getAfroHaircutId()
	{
		return _afroId ;
	}

	public void setAfroHaircutId(int id)
	{
		_afroId = id;
		broadcastUserInfo();
	}

	public void setIsInSiege(boolean b)
	{
		_isInSiege = b;
	}

	public boolean isInSiege()
	{
		return _isInSiege;
	}

	@Override
	protected CharShots initShots()
	{
		return new PcShots(this);
	}

	@Override
	public PcShots getShots()
	{
		return (PcShots)_shots;
	}

	public boolean isTryingToSitOrStandup()
	{
		return ((_lastSitStandRequest + 2333) > System.currentTimeMillis());
	}

	public static enum TeleportMode
	{
		SCROLL_OF_ESCAPE,
		RECALL,
		UNSTUCK,
		GOTOLOVE,
	}

	public boolean canTeleport(TeleportMode mode)
	{
		return canTeleport(mode, false);
	}

	public boolean canTeleport(TeleportMode mode, boolean bySkill)
	{
		if (mode != TeleportMode.RECALL && !bySkill)
		{
			if (isCastingNow() || isMuted() || isAlikeDead() || isMovementDisabled() || isAllSkillsDisabled())
				return false;
		}

		if (mode == TeleportMode.SCROLL_OF_ESCAPE)
		{
			if (isSitting())
			{
				sendPacket(SystemMessageId.CANT_MOVE_SITTING);
				return false;
			}
		}

		if (!GlobalRestrictions.canTeleport(this))
			return false;

		// [L2J_JP ADD]
		if (isInsideZone(L2Zone.FLAG_NOESCAPE))
		{
			sendMessage("You can't teleport from here.");
			return false;
		}

		if (getPlayerOlympiad().isInOlympiadMode())
		{
			if (mode == TeleportMode.SCROLL_OF_ESCAPE)
				sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			else if (mode == TeleportMode.RECALL)
				sendPacket(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			else
				sendMessage("You can't teleport during Olympiad.");
			return false;
		}

		// Check to see if the player is in a festival.
		if (isFestivalParticipant())
		{
			sendMessage("You can't teleport in a festival.");
			return false;
		}

		if (getPlayerObserver().inObserverMode())
		{
			sendMessage("You can't teleport during Observation Mode.");
			return false;
		}

		if (isAfraid())
			return false;
		
		if (isCombatFlagEquipped())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		return true;
	}

	public boolean canSee(L2Character cha)
	{
		if (isGM())
			return true;

		final L2Player player = cha.getActingPlayer();
		
		if (player == this)
			return true;
		
		if (player != null)
		{
			if (player.getAppearance().isInvisible())
				return false;

			if (player.getPlayerObserver().inObserverMode())
				return false;
		}
		
		// Are traps invisible for other chars than owner?
		if (cha instanceof L2Trap)
		{
			final L2Trap trap = (L2Trap)cha;
			
			if (!trap.isDetected())
				return false;
		}
		
		return true;
	}

	public boolean isFlyingMounted()
	{
		return _isFlyingMounted;
	}

	public void setIsFlyingMounted(boolean val)
	{
		_isFlyingMounted = val;
		setIsFlying(val);
	}

	public synchronized boolean enterOfflineMode()
	{
		if (isInOfflineMode())
			return false;

		leaveParty();

		if (getPet() != null)
			getPet().unSummon(this);

		// set name color if enabled
		getAppearance().updateNameTitleColor();

		new Disconnection(this).store().close(false);

		return true;
	}

	public boolean isInOfflineMode()
	{
		return getOnlineState() == ONLINE_STATE_ONLINE && getClient() == null;
	}

	public boolean isChaotic()
	{
		return getKarma() > 0 || isCursedWeaponEquipped();
	}

	public int getSubClassType()
	{
		/*
		 * 1: Warrior
		 * 2: Rogue
		 * 3: Knight
		 * 4: Summoner
		 * 5: Wizard
		 * 6: Healer
		 * 7: Enchanter
		 */

		int classId = getActiveClass();
		int subClassType = 0;

		if (classId == 3 || classId == 2 || classId == 46 || classId == 48 || classId == 55 || classId == 57 || classId == 127 || classId == 128 || classId == 129 || classId == 89 || classId == 88 || classId == 113 || classId == 114 || classId == 117 || classId == 118 || classId == 131 || classId == 132 || classId == 133)
			subClassType = 1;
		else if (classId == 9 || classId == 24 || classId == 37 || classId == 130 || classId == 8 || classId == 23 || classId == 36 || classId == 92 || classId == 102 || classId == 109 || classId == 134 || classId == 93 || classId == 101 || classId == 108)
			subClassType = 2;
		else if (classId == 5 || classId == 6 || classId == 20 || classId == 33 || classId == 90 || classId == 91 || classId == 99 || classId == 106)
			subClassType = 3;
		else if (classId == 14 || classId == 28 || classId == 41 || classId == 96 || classId == 104 || classId == 111)
			subClassType = 4;
		else if (classId == 12 || classId == 13 || classId == 27 || classId == 40 || classId == 94 || classId == 95 || classId == 103 || classId == 110)
			subClassType = 5;
		else if (classId == 16 || classId == 30 || classId == 43 || classId == 97 || classId == 105 || classId == 112)
			subClassType = 6;
		else if (classId == 17 || classId == 21 || classId == 34 || classId == 52 || classId == 51 || classId == 135 || classId == 98 || classId == 100 || classId == 107 || classId == 116 || classId == 115 || classId == 136)
			subClassType = 7;

		return subClassType;
	}
	
	public final FloodProtector getFloodProtector()
	{
		if (_client == null)
			return null;
		
		return _client.getFloodProtector();
	}

	public final void saveLastTarget(int objectId)
	{
		_lastTargetId = objectId;
		_lastTargetChange = System.currentTimeMillis();
	}

	public final int getLastTargetId()
	{
		return _lastTargetId;
	}

	public final long getLastTargetTime()
	{
		return _lastTargetChange;
	}

	public final boolean isIllegalWaiting()
	{
		return _illegalWaiting;
	}

	public final void setIllegalWaiting(boolean iw)
	{
		_illegalWaiting = iw;
	}

	@Override
	public void sendResistedMyEffectMessage(L2Character target, L2Skill skill)
	{
		SystemMessage sm = new SystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2_EFFECT);
		sm.addCharName(target);
		sm.addSkillName(skill);
		sendPacket(sm);
	}

	public boolean tryJump()
	{
		long time = System.currentTimeMillis();
		if (_nextJumpTime < time)
		{
			_nextJumpTime = time + 3000;
			return true;
		}
		else
			return false;
	}

	public boolean destroyItems(String process, int[] itemId, long[] count, L2Object ref, boolean sendMessage)
	{
		if (itemId == null || itemId.length == 0 || count == null || count.length == 0 ||
				itemId.length != count.length)
		{
			_log.error("Invalid mass item destruction call!", new IllegalArgumentException());
			return false;
		}

		int fail = -1;
		L2ItemInstance[] items = new L2ItemInstance[itemId.length];
		for (int i = 0; i < itemId.length; i++)
		{
			L2ItemInstance item = getInventory().getItemByItemId(itemId[i]);
			items[i] = item;
			if (item == null || item.getCount() < count[i] ||
					getInventory().destroyItem(process, item, count[i], this, ref) == null)
			{
				fail = i;
				break;
			}
		}

		if (fail > -1)
		{
			if (fail > 0)
				for (int i = 0; i < fail; i++)
					getInventory().addItem(process, itemId[i], count[i], this, ref);
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			return false;
		}
		else
		{
			for (int i = 0; i < itemId.length; i++)
			{
				getInventory().updateInventory(items[i]);
				if (sendMessage)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
					sm.addItemName(itemId[i]);
					sm.addItemNumber(count[i]);
					sendPacket(sm);
				}
			}
			return true;
		}
	}

	public void onTutorialQuestionMark(int number)
	{
		showTutorialHtml(number);
	}

	public void onTutorialLink(String request)
	{
		if (request.equals("close"))
			sendPacket(new TutorialCloseHtml());
	}

	private void showTutorialHtml(int number)
	{
		switch (number)
		{
		case 1001:
			L2ClassMasterInstance.onTutorialQuestionMark(this, number);
			break;
		}
	}
	
	private AnswerHandler _answerHandler;
	
	public AnswerHandler setAnswerHandler(AnswerHandler nextHandler)
	{
		final AnswerHandler previousHandler = _answerHandler;
		
		_answerHandler = nextHandler;
		
		return previousHandler;
	}
	
	public boolean hasActiveConfirmDlg()
	{
		return _answerHandler != null;
	}
	
	public boolean isAllowedToEnchantSkills()
	{
		if (isLocked())
			return false;
		if (getPlayerTransformation().isTransformed())
			return false;
		if (AttackStanceTaskManager.getInstance().getAttackStanceTask(this))
			return false;
		if (isCastingNow() || isCastingSimultaneouslyNow())
			return false;
		if (isInBoat() || isInAirShip())
			return false;
		
		return true;
	}
	
	public void notifyFriends()
	{
		FriendStatusPacket pkt = new FriendStatusPacket(getObjectId());
		for (Integer objId : getFriendList().getFriendIds())
		{
			L2Player friend = L2World.getInstance().findPlayer(objId);
			if (friend != null)
				friend.sendPacket(pkt);
		}
	}
	
	// Quest Movie
	private int _movieId = 0;
	
	public final void showQuestMovie(int id)
	{
		if (_movieId > 0) //already in movie
			return;
		
		abortAttack();
		abortCast();
		stopMove(null);
		_movieId = id;
		sendPacket(new ExStartScenePlayer(id));
	}
	
	public final int getMovieId()
	{
		return _movieId;
	}
	
	public final void setMovieId(int id)
	{
		_movieId = id;
	}
	
	// Item Auction
	private volatile long _lastItemAuctionInfoRequest = 0;
	
	/**
	 * Update last item auction request timestamp to current
	 */
	public final void updateLastItemAuctionRequest()
	{
		_lastItemAuctionInfoRequest = System.currentTimeMillis();
	}

	/**
	 * Returns true if receiving item auction requests
	 * (last request was in 2 seconds before)
	 */
	public final boolean isItemAuctionPolling()
	{
		return System.currentTimeMillis() - _lastItemAuctionInfoRequest < 2000;
	}
	
	@Override
	public final boolean isMovementDisabled()
	{
		return super.isMovementDisabled() || _movieId > 0;
	}
	
	public final boolean canMakeSocialAction()
	{
		if (getPrivateStoreType() == 0 
				&& getActiveRequester() == null && !isAlikeDead() 
				&& (!isAllSkillsDisabled() || getPlayerDuel().isInDuel()) && !isCastingNow()
				&& !isCastingSimultaneouslyNow() && getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE
				&& !AttackStanceTaskManager.getInstance().getAttackStanceTask(this) && !getPlayerOlympiad().isInOlympiadMode() 
				&& getFloodProtector().tryPerformAction(Protected.SOCIAL))
		{
			return true;
		}
		else
			return false;
	}
	
	private int	_multiSociaAction = 0;
	private int	_multiSocialTarget = 0;

	public final void setMultiSocialAction(int id, int targetId)
	{
		_multiSociaAction = id;
		_multiSocialTarget = targetId;
	}

	public final int getMultiSociaAction()
	{
		return _multiSociaAction;
	}

	public final int getMultiSocialTarget()
	{
		return _multiSocialTarget;
	}
	
	private byte _handysBlockCheckerEventArena = -1;
	
	public final void setBlockCheckerArena(byte arena)
	{
		_handysBlockCheckerEventArena = arena;
	}

	public final int getBlockCheckerArena()
	{
		return _handysBlockCheckerEventArena;
	}
	
	public final void showHTMLFile(String filename)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(filename);
		sendPacket(html);

		sendPacket(ActionFailed.STATIC_PACKET);
	}

	public final void showHTMLMessage(String message)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml("<html><body>" + message + "</body></html>");
		sendPacket(html);

		sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private boolean _canGainExpSp = true;
	
	public final void setCanGainExpSp(boolean status)
	{
		_canGainExpSp = status;
	}

	public final boolean canGainExpSp()
	{
		return _canGainExpSp;
	}
	
	private boolean _hiding = false;
	
	public final void setHiding(boolean status, boolean enterWorld)
	{
		_hiding = status;

		if (_hiding)
		{
			setIsInvul(true);

			getAppearance().setInvisible();
			updateInvisibilityStatus();

			setMessageRefusal(true);

			GmListTable.addGm(this, false);

			if (enterWorld)
				sendCreatureMessage(SystemChatChannelId.Chat_Normal, "SYS", "Hide is default for builder.");
			else
				sendCreatureMessage(SystemChatChannelId.Chat_Normal, "SYS", "Now, you cannot be seen.");
		}
		else
		{
			setIsInvul(false);

			getAppearance().setVisible();
			broadcastUserInfo();

			setMessageRefusal(false);

			GmListTable.addGm(this, true);

			sendCreatureMessage(SystemChatChannelId.Chat_Normal, "SYS", "Now, you can be seen.");
		}
	}

	public final boolean isHiding()
	{
		return _hiding;
	}
	
	// extensions
	public final PlayerTeleportBookmark getPlayerBookmark()
	{
		if (_teleBookmarkExtension == null)
			_teleBookmarkExtension = new PlayerTeleportBookmark(this);
		return _teleBookmarkExtension;
	}
	
	public final PlayerVitality getPlayerVitality()
	{
		if (_vitalityExtension == null)
			_vitalityExtension = new PlayerVitality(this);
		return _vitalityExtension;
	}
	
	public final PlayerCertification getPlayerCertification()
	{
		if (_certificationExtension == null)
			_certificationExtension = new PlayerCertification(this);
		return _certificationExtension;
	}
	
	public final PlayerBirthday getPlayerBirthday()
	{
		if (_birthdayExtension == null)
			_birthdayExtension = new PlayerBirthday(this);
		return _birthdayExtension;
	}
	
	public final PlayerTransformation getPlayerTransformation()
	{
		if (_transformationExtension == null)
			_transformationExtension = new PlayerTransformation(this);
		return _transformationExtension;
	}
	
	public final PlayerHenna getPlayerHenna()
	{
		if (_hennaExtension == null)
			_hennaExtension = new PlayerHenna(this);
		return _hennaExtension;
	}
	
	public final PlayerRecipe getPlayerRecipe()
	{
		if (_recipeExtension == null)
			_recipeExtension = new PlayerRecipe(this);
		return _recipeExtension;
	}
	
	public final PlayerCustom getPlayerCustom()
	{
		if (_customExtension == null)
			_customExtension = new PlayerCustom(this);
		return _customExtension;
	}
	
	public final PlayerObserver getPlayerObserver()
	{
		if (_observerExtension == null)
			_observerExtension = new PlayerObserver(this);
		return _observerExtension;
	}
	
	public final PlayerOlympiad getPlayerOlympiad()
	{
		if (_olympiadExtension == null)
			_olympiadExtension = new PlayerOlympiad(this);
		return _olympiadExtension;
	}
	
	public final PlayerFish getPlayerFish()
	{
		if (_fishExtension == null)
			_fishExtension = new PlayerFish(this);
		return _fishExtension;
	}
	
	public final PlayerDuel getPlayerDuel()
	{
		if (_duelExtension == null)
			_duelExtension = new PlayerDuel(this);
		return _duelExtension;
	}
	
	public final PlayerSettings getPlayerSettings()
	{
		if (_settingsExtension == null)
			_settingsExtension = new PlayerSettings(this);
		return _settingsExtension;
	}
	
	public final PlayerEventData getPlayerEventData()
	{
		if (_playerEventData == null)
			_playerEventData = new PlayerEventData(this);
		return _playerEventData;
	}
}
