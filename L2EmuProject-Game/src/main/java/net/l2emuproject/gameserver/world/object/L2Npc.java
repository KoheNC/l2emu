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

import static net.l2emuproject.gameserver.entity.ai.CtrlIntention.AI_INTENTION_ACTIVE;

import java.text.DateFormat;
import java.util.List;

import javolution.util.FastList;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.Shutdown;
import net.l2emuproject.gameserver.Shutdown.DisableType;
import net.l2emuproject.gameserver.datatables.BuffTemplateTable;
import net.l2emuproject.gameserver.datatables.ItemTable;
import net.l2emuproject.gameserver.datatables.NpcTable;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.datatables.SpawnTable;
import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.entity.itemcontainer.NpcInventory;
import net.l2emuproject.gameserver.entity.itemcontainer.PcInventory;
import net.l2emuproject.gameserver.entity.shot.CharShots;
import net.l2emuproject.gameserver.entity.shot.NpcShots;
import net.l2emuproject.gameserver.entity.stat.CharStat;
import net.l2emuproject.gameserver.entity.stat.NpcStat;
import net.l2emuproject.gameserver.entity.status.CharStatus;
import net.l2emuproject.gameserver.entity.status.NpcStatus;
import net.l2emuproject.gameserver.entity.view.CharLikeView;
import net.l2emuproject.gameserver.entity.view.NpcView;
import net.l2emuproject.gameserver.events.global.fortsiege.Fort;
import net.l2emuproject.gameserver.events.global.fortsiege.FortManager;
import net.l2emuproject.gameserver.events.global.lottery.Lottery;
import net.l2emuproject.gameserver.events.global.olympiad.Olympiad;
import net.l2emuproject.gameserver.events.global.sevensigns.SevenSigns;
import net.l2emuproject.gameserver.events.global.sevensigns.SevenSignsFestival;
import net.l2emuproject.gameserver.events.global.siege.Castle;
import net.l2emuproject.gameserver.events.global.siege.CastleManager;
import net.l2emuproject.gameserver.handler.BypassHandler;
import net.l2emuproject.gameserver.handler.IBypassHandler;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.AbstractNpcInfo;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.ExChangeNpcState;
import net.l2emuproject.gameserver.network.serverpackets.InventoryUpdate;
import net.l2emuproject.gameserver.network.serverpackets.MagicSkillUse;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.network.serverpackets.ServerObjectInfo;
import net.l2emuproject.gameserver.network.serverpackets.SocialAction;
import net.l2emuproject.gameserver.network.serverpackets.StatusUpdate;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.quest.Quest;
import net.l2emuproject.gameserver.services.quest.QuestService;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.Stats;
import net.l2emuproject.gameserver.system.cache.HtmCache;
import net.l2emuproject.gameserver.system.idfactory.IdFactory;
import net.l2emuproject.gameserver.system.restriction.global.GlobalRestrictions;
import net.l2emuproject.gameserver.system.taskmanager.AbstractIterativePeriodicTaskManager;
import net.l2emuproject.gameserver.system.taskmanager.DecayTaskManager;
import net.l2emuproject.gameserver.system.threadmanager.ThreadPoolManager;
import net.l2emuproject.gameserver.system.util.StringUtil;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate.AIType;
import net.l2emuproject.gameserver.templates.item.L2Item;
import net.l2emuproject.gameserver.templates.item.L2Weapon;
import net.l2emuproject.gameserver.templates.skills.L2BuffTemplate;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.L2WorldRegion;
import net.l2emuproject.gameserver.world.knownlist.CharKnownList;
import net.l2emuproject.gameserver.world.knownlist.NpcKnownList;
import net.l2emuproject.gameserver.world.npc.L2NpcCharData;
import net.l2emuproject.gameserver.world.npc.MobGroupTable;
import net.l2emuproject.gameserver.world.npc.drop.L2DropCategory;
import net.l2emuproject.gameserver.world.npc.drop.L2DropData;
import net.l2emuproject.gameserver.world.object.instance.L2AuctioneerInstance;
import net.l2emuproject.gameserver.world.object.instance.L2CCHBossInstance;
import net.l2emuproject.gameserver.world.object.instance.L2ChestInstance;
import net.l2emuproject.gameserver.world.object.instance.L2ClanHallManagerInstance;
import net.l2emuproject.gameserver.world.object.instance.L2ControllableMobInstance;
import net.l2emuproject.gameserver.world.object.instance.L2DoormenInstance;
import net.l2emuproject.gameserver.world.object.instance.L2FestivalGuideInstance;
import net.l2emuproject.gameserver.world.object.instance.L2FishermanInstance;
import net.l2emuproject.gameserver.world.object.instance.L2MerchantInstance;
import net.l2emuproject.gameserver.world.object.instance.L2MonsterInstance;
import net.l2emuproject.gameserver.world.object.instance.L2PetInstance;
import net.l2emuproject.gameserver.world.object.instance.L2TeleporterInstance;
import net.l2emuproject.gameserver.world.object.instance.L2TrainerInstance;
import net.l2emuproject.gameserver.world.object.instance.L2WarehouseInstance;
import net.l2emuproject.gameserver.world.spawn.L2Spawn;
import net.l2emuproject.gameserver.world.town.Town;
import net.l2emuproject.gameserver.world.town.TownManager;
import net.l2emuproject.lang.L2Math;
import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.tools.random.Rnd;

/**
 * This class represents a Non-Player-Character in the world. It can be a monster or a friendly character.
 * It also uses a template to fetch some static values. The templates are hardcoded in the client, so we can rely on them.<BR><BR>
 *
 * L2Character :<BR><BR>
 * <li>L2Attackable</li>
 * <li>L2NpcInstance</li>
 *
 * @version $Revision: 1.32.2.7.2.24 $ $Date: 2005/04/11 10:06:09 $
 */
public class L2Npc extends L2Character
{
	private static final class RandomAnimationTaskManager extends AbstractIterativePeriodicTaskManager<L2Npc>
	{
		private static final int MIN_SOCIAL_INTERVAL = 6000;
		private static final RandomAnimationTaskManager _instance = new RandomAnimationTaskManager();

		private static RandomAnimationTaskManager getInstance()
		{
			return _instance;
		}

		private RandomAnimationTaskManager()
		{
			super(1000);
			//super(MIN_SOCIAL_INTERVAL);
		}

		@Override
		protected void callTask(L2Npc npc)
		{
			if (!npc.tryBroadcastRandomAnimation(false, false))
				stopTask(npc);
		}

		@Override
		protected String getCalledMethodName()
		{
			return "broadcastRandomAnimation()";
		}
	}

	/** The interaction distance of the L2Npc(is used as offset in MovetoLocation method) */
	public static final int			INTERACTION_DISTANCE	= 150;

	/** The L2Spawn object that manage this L2Npc */
	private L2Spawn					_spawn;

	private NpcInventory			_inventory				= null;

	/** The flag to specify if this L2Npc is busy */
	private boolean					_isBusy					= false;

	/** The busy message for this L2Npc */
	private String					_busyMessage			= "";

	/** True if endDecayTask has already been called */
	volatile boolean				_isDecayed				= false;

	/** True if a Dwarf has used Spoil on this L2Npc */
	private boolean					_isSpoil				= false;

	/** The castle index in the array of L2Castle this L2Npc belongs to */
	private int						_castleIndex			= -2;

	/** The fortress index in the array of L2Fort this L2Npc belongs to */
	private int						_fortIndex				= -2;

	private boolean					_isInTown				= false;
	private int						_isSpoiledBy			= 0;

	private long					_lastRandomAnimation;
	private int						_randomAnimationDelay;
	private int						_currentLHandId;						// normally this shouldn't change from the template, but there exist exceptions
	private int						_currentRHandId;						// normally this shouldn't change from the template, but there exist exceptions

	private double					_currentCollisionHeight;				// used for npc grow effect skills
	private double					_currentCollisionRadius;				// used for npc grow effect skills

	private boolean					_isKillable				= true;
	private boolean					_questDropable			= true;

	// In case quests are going to use non-L2Attackables in the future
	private int						_questAttackStatus;
	private L2Player			_questFirstAttacker;

	// doesn't affect damage at all (retail)
	private int						_weaponEnchant			= 0;
	
	private int 					_displayEffect 			= 0;
	
	private boolean					_isHideName				= false;
	
	private boolean					_isAutoAttackable		= false;

	public final void broadcastRandomAnimation(boolean force)
	{
		tryBroadcastRandomAnimation(force, true);
	}

	public final boolean tryBroadcastRandomAnimation(boolean force, boolean init)
	{
		if (!isInActiveRegion() || !hasRandomAnimation())
			return false;

		if (isMob() && getAI().getIntention() != AI_INTENTION_ACTIVE)
			return false;

		if (_lastRandomAnimation + RandomAnimationTaskManager.MIN_SOCIAL_INTERVAL < System.currentTimeMillis()
				&& !getKnownList().getKnownPlayers().isEmpty())
		{
			if (force || _lastRandomAnimation + _randomAnimationDelay < System.currentTimeMillis())
			{
				if (!isDead() && !isStunned() && !isSleeping() && !isParalyzed())
				{
					SocialAction sa;
					if (force) // on talk/interact
						sa = new SocialAction(this, Rnd.get(8));
					else // periodic
						sa = new SocialAction(this, Rnd.get(2, 3));
					broadcastPacket(sa);

					int minWait = isMob() ? Config.MIN_MONSTER_ANIMATION : Config.MIN_NPC_ANIMATION;
					int maxWait = isMob() ? Config.MAX_MONSTER_ANIMATION : Config.MAX_NPC_ANIMATION;

					_lastRandomAnimation = System.currentTimeMillis();
					_randomAnimationDelay = Rnd.get(minWait, maxWait) * 1000;
				}
			}
		}

		if (init)
			RandomAnimationTaskManager.getInstance().startTask(this);
		return true;
	}

	public final void stopRandomAnimation()
	{
		RandomAnimationTaskManager.getInstance().stopTask(this);
	}

	/**
	 * Check if the server allows Random Animation.<BR><BR>
	 */
	public boolean hasRandomAnimation()
	{
		return (Config.MAX_NPC_ANIMATION > 0 && getTemplate().getAI() != AIType.CORPSE);
	}

	public static class DestroyTemporalNPC implements Runnable
	{
		private final L2Spawn	_oldSpawn;

		public DestroyTemporalNPC(L2Spawn spawn)
		{
			_oldSpawn = spawn;
		}

		public void run()
		{
			try
			{
				_oldSpawn.getLastSpawn().deleteMe();
				_oldSpawn.stopRespawn();
				SpawnTable.getInstance().deleteSpawn(_oldSpawn, false);
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	public static class DestroyTemporalSummon implements Runnable
	{
		L2Summon		_summon;
		L2Player	_player;

		public DestroyTemporalSummon(L2Summon summon, L2Player player)
		{
			_summon = summon;
			_player = player;
		}

		public void run()
		{
			_summon.unSummon(_player);
		}
	}

	/**
	 * Constructor of L2Npc (use L2Character constructor).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to set the _template of the L2Character (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)  </li>
	 * <li>Set the name of the L2Character</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it </li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2NpcTemplate to apply to the NPC
	 *
	 */
	public L2Npc(int objectId, L2NpcTemplate template)
	{
		// Call the L2Character constructor to set the _template of the L2Character, copy skills from template to object
		// and link _calculators to NPC_STD_CALCULATOR
		super(objectId, template);
		getKnownList(); // init knownlist
		getStat(); // init stats
		getStatus(); // init status
		super.initCharStatusUpdateValues(); // init status upadte values

		// Initialize the "current" equipment
		_currentLHandId = getTemplate().getLhand();
		_currentRHandId = getTemplate().getRhand();
		// initialize the "current" collisions
		_currentCollisionHeight = getTemplate().getCollisionHeight();
		_currentCollisionRadius = getTemplate().getCollisionRadius();

		if (template == null)
		{
			_log.fatal("No template for Npc. Please check your datapack is setup correctly.");
			return;
		}

		// Set the name and the title of the L2Character
		setName(template.getName());
		setTitle(template.getTitle());

		if ((template.getSS() > 0 || template.getBSS() > 0) && template.getSSRate() > 0)
			_inventory = new NpcInventory(this);
	}

	@Override
	protected CharKnownList initKnownList()
	{
		return new NpcKnownList(this);
	}

	@Override
	public NpcKnownList getKnownList()
	{
		return (NpcKnownList)_knownList;
	}

	@Override
	protected CharLikeView initView()
	{
		return new NpcView(this);
	}

	@Override
	public NpcView getView()
	{
		return (NpcView)_view;
	}

	@Override
	protected CharStat initStat()
	{
		return new NpcStat(this);
	}

	@Override
	public NpcStat getStat()
	{
		return (NpcStat)_stat;
	}

	@Override
	protected CharStatus initStatus()
	{
		return new NpcStatus(this);
	}

	@Override
	public NpcStatus getStatus()
	{
		return (NpcStatus)_status;
	}

	/** Return the L2NpcTemplate of the L2Npc. */
	@Override
	public final L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate) super.getTemplate();
	}

	/**
	 * Return the generic Identifier of this L2Npc contained in the L2NpcTemplate.<BR><BR>
	 */
	public int getNpcId()
	{
		return getTemplate().getNpcId();
	}

	@Override
	public boolean isAttackable()
	{
		return Config.ALT_ATTACKABLE_NPCS;
	}

	/**
	 * Return the faction Identifier of this L2Npc contained in the L2NpcTemplate.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * If a NPC belows to a Faction, other NPC of the faction inside the Faction range will help it if it's attacked<BR><BR>
	 *
	 */
	public final String getFactionId()
	{
		return getTemplate().getFactionId();
	}

	/**
	 * Return the Level of this L2Npc contained in the L2NpcTemplate.<BR><BR>
	 */
	@Override
	public final int getLevel()
	{
		return getTemplate().getLevel();
	}

	/**
	 * Return True if the L2Npc is agressive (ex : L2MonsterInstance in function of aggroRange).<BR><BR>
	 */
	public boolean isAggressive()
	{
		return false;
	}

	/**
	 * Return the Aggro Range of this L2Npc contained in the L2NpcTemplate.<BR><BR>
	 */
	public int getAggroRange()
	{
		return getTemplate().getAggroRange();
	}

	/**
	 * Return the Faction Range of this L2Npc contained in the L2NpcTemplate.<BR><BR>
	 */
	public int getFactionRange()
	{
		return getTemplate().getFactionRange();
	}

	/**
	 * Return True if this L2Npc is undead in function of the L2NpcTemplate.<BR><BR>
	 */
	@Override
	public boolean isUndead()
	{
		return getTemplate().isUndead();
	}

	/**
	 * Return the Identifier of the item in the left hand of this L2Npc contained in the L2NpcTemplate.<BR><BR>
	 */
	public int getLeftHandItem()
	{
		return _currentLHandId;
	}

	/**
	 * Return the Identifier of the item in the right hand of this L2Npc contained in the L2NpcTemplate.<BR><BR>
	 */
	public int getRightHandItem()
	{
		return _currentRHandId;
	}

	/**
	 * Return True if this L2Npc has drops that can be sweeped.<BR><BR>
	 */
	public boolean isSpoil()
	{
		return _isSpoil;
	}

	/**
	 * Set the spoil state of this L2Npc.<BR><BR>
	 */
	public void setSpoil(boolean isSpoil)
	{
		_isSpoil = isSpoil;
	}

	public final int getIsSpoiledBy()
	{
		return _isSpoiledBy;
	}

	public final void setIsSpoiledBy(int value)
	{
		_isSpoiledBy = value;
	}

	/**
	 * Return the busy status of this L2Npc.<BR><BR>
	 */
	public final boolean isBusy()
	{
		return _isBusy;
	}

	/**
	 * Set the busy status of this L2Npc.<BR><BR>
	 */
	public void setBusy(boolean isBusy)
	{
		_isBusy = isBusy;
	}

	/**
	 * Return the busy message of this L2Npc.<BR><BR>
	 */
	public final String getBusyMessage()
	{
		return _busyMessage;
	}

	/**
	 * Set the busy message of this L2Npc.<BR><BR>
	 */
	public void setBusyMessage(String message)
	{
		_busyMessage = message;
	}

	/**
	 * Return true if this L2Npc instance can be warehouse manager.<BR><BR>
	 */
	public boolean isWarehouse()
	{
		return false;
	}

	protected boolean canTarget(L2Player player)
	{
		if (player.isOutOfControl())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		if (!player.canChangeLockedTarget(this))
			return false;

		// Restrict interactions during restart/shutdown
		if (Shutdown.isActionDisabled(DisableType.NPC_ITERACTION))
		{
			player.sendMessage("NPC interaction disabled during restart/shutdown.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		return true;
	}

	public boolean canInteract(L2Player player)
	{
		// TODO: NPC busy check etc...

		if (player.isCastingNow() || player.isCastingSimultaneouslyNow())
			return false;
		if (player.isDead() || player.isFakeDeath())
			return false;
		if (player.isSitting())
			return false;
		if (!isSameInstance(player))
			return false;

		if (player.getPrivateStoreType() != 0)
			return false;

		return isInsideRadius(player, INTERACTION_DISTANCE, true, false);
	}

	/**
	 * Manage actions when a player click on the L2Npc.<BR><BR>
	 *
	 * <B><U> Actions on first click on the L2Npc (Select it)</U> :</B><BR><BR>
	 * <li>Set the L2Npc as target of the L2Player player (if necessary)</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the L2Player player (display the select window)</li>
	 * <li>If L2Npc is autoAttackable, send a Server->Client packet StatusUpdate to the L2Player in order to update L2Npc HP bar </li>
	 * <li>Send a Server->Client packet ValidateLocation to correct the L2Npc position and heading on the client </li><BR><BR>
	 *
	 * <B><U> Actions on second click on the L2Npc (Attack it/Intercat with it)</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet MyTargetSelected to the L2Player player (display the select window)</li>
	 * <li>If L2Npc is autoAttackable, notify the L2Player AI with AI_INTENTION_ATTACK (after a height verification)</li>
	 * <li>If L2Npc is NOT autoAttackable, notify the L2Player AI with AI_INTENTION_INTERACT (after a distance verification) and show message</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Each group of Server->Client packet must be terminated by a ActionFailed packet in order to avoid
	 * that client wait an other packet</B></FONT><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Client packet : Action, AttackRequest</li><BR><BR>
	 *
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2ArtefactInstance : Manage only fisrt click to select Artefact</li><BR><BR>
	 * <li> L2GuardInstance : </li><BR><BR>
	 *
	 * @param player The L2Player that start an action on the L2Npc
	 *
	 */
	@Override
	public void onAction(L2Player player, boolean interact)
	{
		if (!canTarget(player))
			return;

		player.setLastFolkNPC(this);

		try
		{
			// Check if the L2Player already target the L2Npc
			if (this != player.getTarget())
			{
				if (_log.isDebugEnabled())
					_log.debug("new target selected:" + getObjectId());

				// Set the target of the L2Player player
				player.setTarget(this);

				// Check if the player is attackable (without a forced attack)
				if (isAutoAttackable(player))
				{
					// Send a Server->Client packet StatusUpdate of the L2Npc to the L2Player to update its HP bar
					StatusUpdate su = new StatusUpdate(getObjectId());
					su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
					su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
					player.sendPacket(su);
				}
			}
			else if (interact)
			{
				// Check if the player is attackable (without a forced attack) and isn't dead
				if (isAutoAttackable(player) && !isAlikeDead())
				{
					// Check the height difference
					if (Math.abs(player.getZ() - getZ()) < 400) // this max heigth difference might need some tweaking
					{
						// Set the L2Player Intention to AI_INTENTION_ATTACK
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
						// player.startAttack(this);
					}
					else
					{
						// Send a Server->Client ActionFailed to the L2Player in order to avoid that the client wait another packet
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
				}
				else if (!isAutoAttackable(player))
				{
					// Calculate the distance between the L2Player and the L2Npc
					if (!canInteract(player))
					{
						// Notify the L2Player AI with AI_INTENTION_INTERACT
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
					}
					else
					{
						// Send a Server->Client packet SocialAction to the all L2Player on the _knownPlayer of the L2Npc
						// to display a social action of the L2Npc on their client
						broadcastRandomAnimation(true);

						// Open a chat window on client with the text of the L2Npc
						if (GlobalRestrictions.onAction(this, player))
						{
						}
						else
						{
							Quest[] qlsa = getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);
							if (qlsa != null && qlsa.length > 0)
								player.setLastQuestNpcObject(getObjectId());
							Quest[] qlst = getTemplate().getEventQuests(Quest.QuestEventType.ON_FIRST_TALK);
							if ((qlst != null) && qlst.length == 1)
								qlst[0].notifyFirstTalk(this, player);
							else
								showChatWindow(player);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.error("", e);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public int getMyTargetSelectedColor(L2Player player)
	{
		if (isAutoAttackable(player))
			return player.getLevel() - getLevel();
		else
			return 0;
	}

	/**
	 * Manage and Display the GM console to modify the L2Npc (GM only).<BR><BR>
	 *
	 * <B><U> Actions (If the L2Player is a GM only)</U> :</B><BR><BR>
	 * <li>Set the L2Npc as target of the L2Player player (if necessary)</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the L2Player player (display the select window)</li>
	 * <li>If L2Npc is autoAttackable, send a Server->Client packet StatusUpdate to the L2Player in order to update L2Npc HP bar </li>
	 * <li>Send a Server->Client NpcHtmlMessage() containing the GM console about this L2Npc </li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Each group of Server->Client packet must be terminated by a ActionFailed packet in order to avoid
	 * that client wait an other packet</B></FONT><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Client packet : Action</li><BR><BR>
	 *
	 * @param player The thread that manage the player that pessed Shift and click on the L2Npc
	 *
	 */
	@Override
	public void onActionShift(L2Player player)
	{
		// Check if the L2Player is a GM
		if (player.getAccessLevel() >= Config.GM_ACCESSLEVEL)
		{
			// Set the target of the L2Player player
			player.setTarget(this);

			// Check if the player is attackable (without a forced attack)
			if (isAutoAttackable(player))
			{
				// Send a Server->Client packet StatusUpdate of the L2Npc to the L2Player to update its HP bar
				StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				player.sendPacket(su);
			}

			// Send a Server->Client NpcHtmlMessage() containing the GM console about this L2Npc
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			String className = getClass().getSimpleName();

			final StringBuilder html1 = StringUtil.startAppend(500,
					"<html><body><center><font color=\"LEVEL\">NPC Information</font></center>" +
					"<br>" +
					"Instance Type: ",
					className,
					"<br1>Faction: ",
					getFactionId() != null ? getFactionId() : "null",
							"<br1>"
			);
			StringUtil.append(html1,
					"Coords ",
					String.valueOf(getX()),
					",",
					String.valueOf(getY()),
					",",
					String.valueOf(getZ()),
					"<br1>"
			);
			if (getSpawn() != null)
				StringUtil.append(html1,
						"Spawn ",
						String.valueOf(getSpawn().getLocx()),
						",",
						String.valueOf(getSpawn().getLocy()),
						",",
						String.valueOf(getSpawn().getLocz()),
						" Loc ID: ",
						String.valueOf(getSpawn().getLocation()),
						"<br1>",
						"Distance from spawn 2D ",
						String.valueOf((int)Math.sqrt(getPlanDistanceSq(getSpawn().getLocx(), getSpawn().getLocy()))),
						" 3D ",
						String.valueOf((int)Math.sqrt(getDistanceSq(getSpawn().getLocx(), getSpawn().getLocy(), getSpawn().getLocz()))),
						"<br1>"
				);

			if (this instanceof L2ControllableMobInstance)
				html1.append("Mob Group: " + MobGroupTable.getInstance().getGroupForMob((L2ControllableMobInstance) this).getGroupId() + "<br>");
			else
				html1.append("Respawn Time: " + (getSpawn() != null ? (getSpawn().getRespawnDelay() / 1000) + "  Seconds<br>" : "?  Seconds<br>"));

			html1.append("<table border=\"0\" width=\"100%\">");
			html1.append("<tr><td>Object ID</td><td>" + getObjectId() + "</td><td>NPC ID</td><td>" + getTemplate().getNpcId() + "</td></tr>");
			html1.append("<tr><td>Castle</td><td>" + getCastle().getCastleId() + "</td><td>AI </td><td>"
					+ (hasAI() ? getAI().getIntention() : "NULL") + "</td></tr>");
			html1.append("<tr><td>Level</td><td>" + getLevel() + "</td><td>Aggro</td><td>"
					+ ((this instanceof L2Attackable) ? getAggroRange() : 0) + "</td></tr>");
			html1.append("</table><br>");

			html1.append("<font color=\"LEVEL\">Combat</font>");
			html1.append("<table border=\"0\" width=\"100%\">");
			html1.append("<tr><td>Current HP</td><td>" + getStatus().getCurrentHp() + "</td><td>Current MP</td><td>" + getStatus().getCurrentMp()
					+ "</td></tr>");
			html1.append("<tr><td>Max.HP</td><td>" + (int) (getMaxHp() / getStat().calcStat(Stats.MAX_HP, 1, this, null)) + "*"
					+ getStat().calcStat(Stats.MAX_HP, 1, this, null) + "</td><td>Max.MP</td><td>" + getMaxMp() + "</td></tr>");
			html1.append("<tr><td>P.Atk.</td><td>" + getPAtk(null) + "</td><td>M.Atk.</td><td>" + getMAtk(null, null) + "</td></tr>");
			html1.append("<tr><td>P.Def.</td><td>" + getPDef(null) + "</td><td>M.Def.</td><td>" + getMDef(null, null) + "</td></tr>");
			html1.append("<tr><td>Accuracy</td><td>" + getAccuracy() + "</td><td>Evasion</td><td>" + getEvasionRate() + "</td></tr>");
			html1.append("<tr><td>Critical</td><td>" + getCriticalHit() + "</td><td>Speed</td><td>" + getRunSpeed() + "</td></tr>");
			html1.append("<tr><td>Atk.Speed</td><td>" + getPAtkSpd() + "</td><td>Cast.Speed</td><td>" + getMAtkSpd() + "</td></tr>");
			html1.append("<tr><td>Race</td><td>" + getTemplate().getRace() + "</td><td></td><td></td></tr>");
			html1.append("</table><br>");

			html1.append("<font color=\"LEVEL\">Basic Stats</font>");
			html1.append("<table border=\"0\" width=\"100%\">");
			html1.append("<tr><td>STR</td><td>" + getStat().getSTR() + "</td><td>DEX</td><td>" + getStat().getDEX() + "</td><td>CON</td><td>"
					+ getStat().getCON() + "</td></tr>");
			html1.append("<tr><td>INT</td><td>" + getINT() + "</td><td>WIT</td><td>" + getStat().getWIT() + "</td><td>MEN</td><td>" + getStat().getMEN()
					+ "</td></tr>");
			html1.append("</table>");

			html1.append("<font color=\"LEVEL\">Quest Info</font>");
			html1.append("<table border=\"0\" width=\"100%\">");
			html1.append("<tr><td>Quest attack status:</td><td>" + getQuestAttackStatus() + "</td></tr>");
			html1.append("<tr><td>Quest attacker:</td><td>" + getQuestFirstAttacker() + "</td></tr>");
			html1.append("</table>");

			html1.append("<br><center><table><tr><td><button value=\"Edit NPC\" action=\"bypass -h admin_edit_npc " + getTemplate().getNpcId()
					+ "\" width=100 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br1></td>");
			html1
			.append("<td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><br1></tr>");
			html1.append("<tr><td><button value=\"Show DropList\" action=\"bypass -h admin_show_droplist " + getTemplate().getNpcId()
					+ "\" width=100 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
			html1
			.append("<td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
			// [L2J_JP ADD START]
			html1.append("<tr><td><button value=\"Show Skillist\" action=\"bypass -h admin_show_skilllist_npc " + getTemplate().getNpcId()
					+ "\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td></td></tr>");
			// [L2J_JP ADD END]
			html1.append("</table></center><br>");
			html1.append("</body></html>");

			html.setHtml(html1.toString());
			player.sendPacket(html);
		}
		// Allow to see the stats of npc if option is activated and if not a box
		else if (Config.ALT_GAME_VIEWNPC && !(this instanceof L2ChestInstance))
		{
			// Set the target of the L2Player player
			player.setTarget(this);

			// Check if the player is attackable (without a forced attack)
			if (isAutoAttackable(player))
			{
				// Send a Server->Client packet StatusUpdate of the L2Npc to the L2Player to update its HP bar
				StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				player.sendPacket(su);
			}

			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			L2TextBuilder html1 = L2TextBuilder.newInstance("<html><body>");

			html1.append("<br><center><font color=\"LEVEL\">[Combat Stats]</font></center>");
			html1.append("<table border=0 width=\"100%\">");
			html1.append("<tr><td>Max.HP</td><td>" + (int) (getMaxHp() / getStat().calcStat(Stats.MAX_HP, 1, this, null)) + "*"
					+ (int) getStat().calcStat(Stats.MAX_HP, 1, this, null) + "</td><td>Max.MP</td><td>" + getMaxMp() + "</td></tr>");
			html1.append("<tr><td>P.Atk.</td><td>" + getPAtk(null) + "</td><td>M.Atk.</td><td>" + getMAtk(null, null) + "</td></tr>");
			html1.append("<tr><td>P.Def.</td><td>" + getPDef(null) + "</td><td>M.Def.</td><td>" + getMDef(null, null) + "</td></tr>");
			html1.append("<tr><td>Accuracy</td><td>" + getAccuracy() + "</td><td>Evasion</td><td>" + getEvasionRate() + "</td></tr>");
			html1.append("<tr><td>Critical</td><td>" + getCriticalHit() + "</td><td>Speed</td><td>" + getRunSpeed() + "</td></tr>");
			html1.append("<tr><td>Atk.Speed</td><td>" + getPAtkSpd() + "</td><td>Cast.Speed</td><td>" + getMAtkSpd() + "</td></tr>");
			html1.append("<tr><td>Race</td><td>" + getTemplate().getRace() + "</td><td></td><td></td></tr>");
			html1.append("</table>");

			html1.append("<br><center><font color=\"LEVEL\">[Basic Stats]</font></center>");
			html1.append("<table border=0 width=\"100%\">");
			html1.append("<tr><td>STR</td><td>" + getStat().getSTR() + "</td><td>DEX</td><td>" + getStat().getDEX() + "</td><td>CON</td><td>"
					+ getStat().getCON() + "</td></tr>");
			html1.append("<tr><td>INT</td><td>" + getINT() + "</td><td>WIT</td><td>" + getStat().getWIT() + "</td><td>MEN</td><td>" + getStat().getMEN()
					+ "</td></tr>");
			html1.append("</table>");

			html1.append("<br><center><font color=\"LEVEL\">[Drop Info]</font></center>");
			html1.append("<table border=0 width=\"100%\">");

			if (getTemplate().getDropData() != null)
			{
				for (L2DropCategory cat : getTemplate().getDropData())
				{
					for (L2DropData drop : cat.getAllDrops())
					{
						final L2Item item = ItemTable.getInstance().getTemplate(drop.getItemId());
						if (item == null)
							continue;

						String name = item.getName();
						float chance = drop.getChance() * Config.RATE_DROP_ITEMS / 10000;
						html1.append("<tr><td>" + name + "</td><td>" + chance + "%</td><td>"
								+ (drop.isQuestDrop() ? "Quest" : (cat.isSweep() ? "Sweep" : "Drop")) + "</td></tr>");
					}
				}
			}

			html1.append("</table>");
			html1.append("</body></html>");

			html.setHtml(html1.moveToString());
			player.sendPacket(html);
		}

		// Send a Server->Client ActionFailed to the L2Player in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	/** Return the L2Castle this L2Npc belongs to. */
	public final Castle getCastle()
	{
		// Get castle this NPC belongs to (excluding L2Attackable)
		if (_castleIndex < 0)
		{
			Town town = TownManager.getInstance().getTown(this);
			// Npc was spawned in town
			_isInTown = (town != null);

			if (!_isInTown)
				_castleIndex = CastleManager.getInstance().getClosestCastle(this).getCastleId();
			else if (town != null && town.getCastle() != null)
				_castleIndex = town.getCastle().getCastleId();
			else
				_castleIndex = CastleManager.getInstance().getClosestCastle(this).getCastleId();
		}

		return CastleManager.getInstance().getCastleById(_castleIndex);
	}

	/** Return the L2Fort this L2Npc belongs to. */
	public final Fort getFort()
	{
		// Get Fort this NPC belongs to (excluding L2Attackable)
		if (_fortIndex < 0)
		{
			Fort fort = FortManager.getInstance().getFort(getX(), getY(), getZ());
			if (fort != null)
			{
				_fortIndex = FortManager.getInstance().getFortIndex(fort.getFortId());
			}
			if (_fortIndex < 0)
			{
				_fortIndex = FortManager.getInstance().findNearestFortIndex(this);
			}
		}
		if (_fortIndex < 0)
		{
			return null;
		}
		return FortManager.getInstance().getForts().get(_fortIndex);
	}

	public final boolean getIsInTown()
	{
		if (_castleIndex < 0)
			getCastle();
		return _isInTown;
	}

	/**
	 * Open a quest or chat window on client with the text of the L2Npc in function of the command.<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Client packet : RequestBypassToServer</li><BR><BR>
	 *
	 * @param command The command string received from client
	 *
	 */
	public void onBypassFeedback(L2Player player, String command)
	{
		//if (canInteract(player))
		{
			if (isBusy() && getBusyMessage().length() > 0)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/npc_data/html/npcbusy.htm");
				html.replace("%busymessage%", getBusyMessage());
				html.replace("%npcname%", getName());
				html.replace("%playername%", player.getName());
				player.sendPacket(html);
			}
			else 
			{
				IBypassHandler handler = BypassHandler.getInstance().getBypassHandler(command);
				if (handler != null)
					handler.useBypass(command, player, this);
				else
					_log.warn(getClass().getSimpleName() + ": Unknown NPC bypass: \"" + command + "\" NpcId: " + getNpcId());
			}
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	/**
	 * Cast buffs on player, this function ignore target type
	 * only buff effects are aplied to player
	 *
	 * @param player Target player
	 * @param buffTemplate Name of buff template
	 */
	public void makeBuffs(L2Player player, String buffTemplate)
	{
		int _templateId = 0;

		try
		{
			_templateId = Integer.parseInt(buffTemplate);
		}
		catch (NumberFormatException e)
		{
			_templateId = BuffTemplateTable.getInstance().getTemplateIdByName(buffTemplate);
		}

		if (_templateId > 0)
			makeBuffs(player, _templateId, false);
	}

	/**
	 * Cast buffs on player/servitor, this function ignore target type
	 * only buff effects are aplied to player/servitor
	 *
	 * @param player Target player/servitor owner
	 * @param _templateId Id of buff template
	 */
	public void makeBuffs(L2Player player, int _templateId, boolean servitor)
	{
		if (player == null)
			return;

		List<L2BuffTemplate> _templateBuffs = BuffTemplateTable.getInstance().getBuffTemplate(_templateId).getBuffs();

		if (_templateBuffs.size() == 0)
			return;

		L2Playable receiver = (servitor ? player.getPet() : player);
		setTarget(receiver);

		for (L2BuffTemplate buff : _templateBuffs)
		{
			if (buff.checkPlayer(player))
			{
				if (buff.forceCast() || receiver.getFirstEffect(buff.getSkill()) == null)
				{
					// regeneration ^^
					getStatus().setCurrentHpMp(getMaxHp(), getMaxMp());

					if (_templateId != 1 && !servitor)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(buff.getSkill().getId());
						player.sendPacket(sm);
					}

					// hack for newbie summons
					if (buff.getSkill().getSkillType() == L2SkillType.SUMMON)
						player.doSimultaneousCast(buff.getSkill());
					else // Ignore skill cast time, using 100ms for NPC buffer's animation
					{
						MagicSkillUse msu = new MagicSkillUse(this, receiver, buff.getSkill().getId(), buff.getSkill().getLevel(), 100, 0);
						broadcastPacket(msu);
						buff.getSkill().getEffects(this, receiver);
					}
				}
			}
		}
	}

	/**
	 * Return null (regular NPCs don't have weapons instancies).<BR><BR>
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		// Regular NPCs dont have weapons instancies
		return null;
	}

	/**
	 * Return the weapon item equiped in the right hand of the L2Npc or null.<BR><BR>
	 */
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		// Get the weapon identifier equiped in the right hand of the L2Npc
		int weaponId = getTemplate().getRhand();

		if (weaponId < 1)
			return null;

		// Get the weapon item equiped in the right hand of the L2Npc
		L2Item item = ItemTable.getInstance().getTemplate(getTemplate().getRhand());

		if (!(item instanceof L2Weapon))
			return null;

		return (L2Weapon) item;
	}

	/**
	 * Return null (regular NPCs don't have weapons instancies).<BR><BR>
	 */
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		// Regular NPCs dont have weapons instancies
		return null;
	}

	/**
	 * Return the weapon item equiped in the left hand of the L2Npc or null.<BR><BR>
	 */
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		// Get the weapon identifier equiped in the right hand of the L2Npc
		int weaponId = getTemplate().getLhand();

		if (weaponId < 1)
			return null;

		// Get the weapon item equiped in the right hand of the L2Npc
		L2Item item = ItemTable.getInstance().getTemplate(getTemplate().getLhand());

		if (!(item instanceof L2Weapon))
			return null;

		return (L2Weapon) item;
	}

	/**
	 * Send a Server->Client packet NpcHtmlMessage to the L2Player in order to display the message of the L2Npc.<BR><BR>
	 *
	 * @param player The L2Player who talks with the L2Npc
	 * @param content The text of the L2NpcMessage
	 *
	 */
	public void insertObjectIdAndShowChatWindow(L2Player player, String content)
	{
		// Send a Server->Client packet NpcHtmlMessage to the L2Player in order to display the message of the L2Npc
		content = content.replaceAll("%objectId%", String.valueOf(getObjectId()));
		NpcHtmlMessage npcReply = new NpcHtmlMessage(getObjectId());
		npcReply.setHtml(content);
		player.sendPacket(npcReply);
	}

	/**
	 * Return the pathfile of the selected HTML file in function of the npcId and of the page number.<BR><BR>
	 *
	 * <B><U> Format of the pathfile </U> :</B><BR><BR>
	 * <li> if the file exists on the server (page number = 0) : <B>data/npc_data/html/default/12006.htm</B> (npcId-page number)</li>
	 * <li> if the file exists on the server (page number > 0) : <B>data/npc_data/html/default/12006-1.htm</B> (npcId-page number)</li>
	 * <li> if the file doesn't exist on the server : <B>data/npc_data/html/npcdefault.htm</B> (message : "I have nothing to say to you")</li><BR><BR>
	 *
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2GuardInstance : Set the pathfile to data/npc_data/html/guard/12006-1.htm (npcId-page number)</li><BR><BR>
	 *
	 * @param npcId The Identifier of the L2Npc whose text must be display
	 * @param val The number of the page to display
	 *
	 */
	public String getHtmlPath(int npcId, int val)
	{
		String pom = String.valueOf(npcId);

		if (val != 0)
			pom += "-" + val;

		String temp = "data/npc_data/html/default/" + pom + ".htm";

		if (HtmCache.getInstance().pathExists(temp))
			return temp;

		// If the file is not found, the standard message "I have nothing to say to you" is returned
		return "data/npc_data/html/npcdefault.htm";
	}

	/**
	 * Open a choose quest window on client with all quests available of the L2Npc.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2Npc to the L2Player </li><BR><BR>
	 *
	 * @param player The L2Player that talk with the L2Npc
	 * @param quests The table containing quests of the L2Npc
	 *
	 */
	public void showQuestChooseWindow(L2Player player, Quest[] quests)
	{
		L2TextBuilder sb = L2TextBuilder.newInstance();
		sb.append("<html><body>");
		for (Quest q : quests)
		{
			sb.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Quest ").append(q.getName()).append("\"> [").append(q.getDescr());

			QuestState qs = player.getQuestState(q.getScriptName());
			if (qs != null)
			{
				if (qs.getState() == State.STARTED && qs.getInt(Quest.CONDITION) > 0)
					sb.append(" (In Progress)");
				else if (qs.getState() == State.COMPLETED)
					sb.append(" (Done)");
			}
			sb.append("]</a><br>");
		}

		sb.append("</body></html>");

		// Send a Server->Client packet NpcHtmlMessage to the L2Player in order to display the message of the L2Npc
		insertObjectIdAndShowChatWindow(player, sb.moveToString());
	}

	/**
	 * Open a quest window on client with the text of the L2Npc.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the text of the quest state in the folder data/scripts/quests/questId/stateId.htm </li>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2Npc to the L2Player </li>
	 * <li>Send a Server->Client ActionFailed to the L2Player in order to avoid that the client wait another packet </li><BR><BR>
	 *
	 * @param player The L2Player that talk with the L2Npc
	 * @param questId The Identifier of the quest to display the message
	 *
	 */
	public void showQuestWindow(L2Player player, String questId)
	{
		String content = null;

		Quest q = QuestService.getInstance().getQuest(questId);

		// Get the state of the selected quest
		QuestState qs = player.getQuestState(questId);

		if (q == null)
		{
			// No quests found
			content = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
		}
		else
		{
			if ((q.getQuestIntId() >= 1 && q.getQuestIntId() < 20000)
					&& (player.getWeightPenalty() >= 3 || player.getInventoryLimit() * 0.8 <= player.getInventory().getSize()))
			{
				player.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
				return;
			}

			if (qs == null)
			{
				if (q.getQuestIntId() >= 1 && q.getQuestIntId() < 20000)
				{
					Quest[] questList = player.getAllActiveQuests();
					if (questList.length >= 25) // if too many ongoing quests, don't show window and send message
					{
						player.sendPacket(SystemMessageId.TOO_MANY_QUESTS);
						return;
					}
				}
				// Check for start point
				Quest[] qlst = getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);

				if (qlst != null && qlst.length > 0)
				{
					for (Quest temp: qlst)
					{
						if (temp == q)
						{
							qs = q.newQuestState(player);
							break;
						}
					}
				}
			}
		}

		if (qs != null)
		{
			// If the quest is alreday started, no need to show a window
			if (!qs.getQuest().notifyTalk(this, qs))
				return;

			questId = qs.getQuest().getName();
			String stateId = State.getStateName(qs.getState());
			String path = "data/scripts/quests/" + questId + "/" + stateId + ".htm";
			content = HtmCache.getInstance().getHtm(path);

			if (_log.isDebugEnabled())
			{
				if (content != null)
				{
					_log.debug("Showing quest window for quest " + questId + " html path: " + path);
				}
				else
				{
					_log.debug("File not exists for quest " + questId + " html path: " + path);
				}
			}
		}

		// Send a Server->Client packet NpcHtmlMessage to the L2Player in order to display the message of the L2Npc
		if (content != null)
			insertObjectIdAndShowChatWindow(player, content);

		// Send a Server->Client ActionFailed to the L2Player in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	/**
	 * Collect awaiting quests/start points and display a QuestChooseWindow (if several available) or QuestWindow.<BR><BR>
	 *
	 * @param player The L2Player that talk with the L2Npc
	 *
	 */
	public void showQuestWindow(L2Player player)
	{
		// Collect awaiting quests and start points
		FastList<Quest> options = new FastList<Quest>();

		QuestState[] awaits = player.getQuestsForTalk(getTemplate().getNpcId());
		Quest[] starts = getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);

		// Quests are limited between 1 and 999 because those are the quests that are supported by the client.
		// By limitting them there, we are allowed to create custom quests at higher IDs without interfering
		if (awaits != null)
		{
			for (QuestState x : awaits)
			{
				if (!options.contains(x.getQuest()))
					if ((x.getQuest().getQuestIntId() > 0) && (x.getQuest().getQuestIntId() < 20000))
						options.add(x.getQuest());
			}
		}

		if (starts != null)
		{
			for (Quest x : starts)
			{
				if (!options.contains(x))
					if ((x.getQuestIntId() > 0) && (x.getQuestIntId() < 20000))
						options.add(x);
			}
		}

		// Display a QuestChooseWindow (if several quests are available) or QuestWindow
		if (options.size() > 1)
		{
			showQuestChooseWindow(player, options.toArray(new Quest[options.size()]));
		}
		else if (options.size() == 1)
		{
			showQuestWindow(player, options.get(0).getName());
		}
		else
		{
			showQuestWindow(player, "");
		}
	}

	/**
	 * Open a Loto window on client with the text of the L2Npc.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the text of the selected HTML file in function of the npcId and of the page number </li>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2Npc to the L2Player </li>
	 * <li>Send a Server->Client ActionFailed to the L2Player in order to avoid that the client wait another packet </li><BR>
	 *
	 * @param player The L2Player that talk with the L2Npc
	 * @param val The number of the page of the L2Npc to display
	 *
	 */
	/**
	 * Open a Loto window on client with the text of the L2Npc.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the text of the selected HTML file in function of the npcId and of the page number </li>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2Npc to the L2Player </li>
	 * <li>Send a Server->Client ActionFailed to the L2Player in order to avoid that the client wait another packet </li><BR>
	 *
	 * @param player The L2Player that talk with the L2Npc
	 * @param val The number of the page of the L2Npc to display
	 *
	 */
	// -1 - lottery instructions
	// 0 - first buy lottery ticket window
	// 1-20 - buttons
	// 21 - second buy lottery ticket window
	// 22 - selected ticket with 5 numbers
	// 23 - current lottery jackpot
	// 24 - Previous winning numbers/Prize claim
	// >24 - check lottery ticket by item object id
	public void showLotoWindow(L2Player player, int val)
	{
		int npcId = getTemplate().getNpcId();
		String filename;
		SystemMessage sm;
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());

		if (val == 0) // 0 - first buy lottery ticket window
		{
			filename = (getHtmlPath(npcId, 1));
			html.setFile(filename);
		}
		else if (val >= 1 && val <= 21) // 1-20 - buttons, 21 - second buy lottery ticket window
		{
			if (!Lottery.getInstance().isStarted())
			{
				//tickets can't be sold
				player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_CURRENT_SOLD);
				return;
			}
			if (!Lottery.getInstance().isSellableTickets())
			{
				//tickets can't be sold
				player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_AVAILABLE);
				return;
			}

			filename = (getHtmlPath(npcId, 5));
			html.setFile(filename);

			int count = 0;
			int found = 0;
			// Counting buttons and unsetting button if found
			for (int i = 0; i < 5; i++)
			{
				if (player.getLoto(i) == val)
				{
					//unsetting button
					player.setLoto(i, 0);
					found = 1;
				}
				else if (player.getLoto(i) > 0)
				{
					count++;
				}
			}

			// If not rearched limit 5 and not unseted value
			if (count < 5 && found == 0 && val <= 20)
				for (int i = 0; i < 5; i++)
					if (player.getLoto(i) == 0)
					{
						player.setLoto(i, val);
						break;
					}

			// Setting pusshed buttons
			count = 0;
			for (int i = 0; i < 5; i++)
				if (player.getLoto(i) > 0)
				{
					count++;
					String button = String.valueOf(player.getLoto(i));
					if (player.getLoto(i) < 10)
						button = "0" + button;
					String search = "fore=\"L2UI.lottoNum" + button + "\" back=\"L2UI.lottoNum" + button + "a_check\"";
					String replace = "fore=\"L2UI.lottoNum" + button + "a_check\" back=\"L2UI.lottoNum" + button + "\"";
					html.replace(search, replace);
				}

			if (count == 5)
			{
				String search = "0\">Return";
				String replace = "22\">The winner selected the numbers above.";
				html.replace(search, replace);
			}
		}
		else if (val == 22) //22 - Selected ticket with 5 numbers
		{
			if (!Lottery.getInstance().isStarted())
			{
				// Tickets can't be sold
				player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_CURRENT_SOLD);
				return;
			}
			if (!Lottery.getInstance().isSellableTickets())
			{
				// Tickets can't be sold
				player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_AVAILABLE);
				return;
			}

			final long price = Config.ALT_LOTTERY_TICKET_PRICE;
			final int lotonumber = Lottery.getInstance().getId();
			int enchant = 0;
			int type2 = 0;

			for (int i = 0; i < 5; i++)
			{
				if (player.getLoto(i) == 0)
					return;

				if (player.getLoto(i) < 17)
					enchant += L2Math.pow(2, player.getLoto(i) - 1);
				else
					type2 += L2Math.pow(2, player.getLoto(i) - 17);
			}
			if (player.getAdena() < price)
			{
				player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
				return;
			}
			if (!player.reduceAdena("Loto", price, this, true))
				return;
			Lottery.getInstance().increasePrize(price);

			sm = new SystemMessage(SystemMessageId.ACQUIRED_S1_S2);
			sm.addItemNumber(lotonumber);
			sm.addItemName(4442);
			player.sendPacket(sm);

			L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), 4442);
			item.setCount(1);
			item.setCustomType1(lotonumber);
			item.setEnchantLevel(enchant);
			item.setCustomType2(type2);
			player.getInventory().addItem("Loto", item, player, this);

			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(item);
			L2ItemInstance adenaupdate = player.getInventory().getItemByItemId(PcInventory.ADENA_ID);
			iu.addModifiedItem(adenaupdate);
			player.sendPacket(iu);

			filename = (getHtmlPath(npcId, 3));
			html.setFile(filename);
		}
		else if (val == 23) //23 - Current lottery jackpot
		{
			filename = (getHtmlPath(npcId, 3));
			html.setFile(filename);
		}
		else if (val == 24) // 24 - Previous winning numbers/Prize claim
		{
			filename = (getHtmlPath(npcId, 4));
			html.setFile(filename);

			int lotonumber = Lottery.getInstance().getId();
			String message = "";
			for (L2ItemInstance item : player.getInventory().getItems())
			{
				if (item == null)
					continue;
				if (item.getItemId() == 4442 && item.getCustomType1() < lotonumber)
				{
					message = message + "<a action=\"bypass -h npc_%objectId%_Loto " + item.getObjectId() + "\">" + item.getCustomType1() + " Event Number ";
					int[] numbers = Lottery.getInstance().decodeNumbers(item.getEnchantLevel(), item.getCustomType2());
					for (int i = 0; i < 5; i++)
					{
						message += numbers[i] + " ";
					}
					long[] check = Lottery.getInstance().checkTicket(item);
					if (check[0] > 0)
					{
						switch ((int) check[0])
						{
						case 1:
							message += "- 1st Prize";
							break;
						case 2:
							message += "- 2nd Prize";
							break;
						case 3:
							message += "- 3th Prize";
							break;
						case 4:
							message += "- 4th Prize";
							break;
						}
						message += " " + check[1] + "a.";
					}
					message += "</a><br>";
				}
			}
			if (message.isEmpty())
			{
				message += "There is no winning lottery ticket...<br>";
			}
			html.replace("%result%", message);
		}
		else if (val > 24) // >24 - Check lottery ticket by item object id
		{
			final int lotonumber = Lottery.getInstance().getId();
			L2ItemInstance item = player.getInventory().getItemByObjectId(val);
			if (item == null || item.getItemId() != 4442 || item.getCustomType1() >= lotonumber)
				return;
			final long[] check = Lottery.getInstance().checkTicket(item);

			sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
			sm.addItemName(4442);
			player.sendPacket(sm);

			final long adena = check[1];
			if (adena > 0)
				player.addAdena("Loto", adena, this, true);
			player.destroyItem("Loto", item, this, false);
			return;
		}
		else if (val == -1) // -1 - Lottery Instrucions
		{
			filename = (getHtmlPath(npcId, 2));
			html.setFile(filename);
		}
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%race%", "" + Lottery.getInstance().getId());
		html.replace("%adena%", "" + Lottery.getInstance().getPrize());
		html.replace("%ticket_price%", "" + Config.ALT_LOTTERY_TICKET_PRICE);
		html.replace("%prize5%", "" + (Config.ALT_LOTTERY_5_NUMBER_RATE * 100));
		html.replace("%prize4%", "" + (Config.ALT_LOTTERY_4_NUMBER_RATE * 100));
		html.replace("%prize3%", "" + (Config.ALT_LOTTERY_3_NUMBER_RATE * 100));
		html.replace("%prize2%", "" + Config.ALT_LOTTERY_2_AND_1_NUMBER_PRIZE);
		html.replace("%enddate%", "" + DateFormat.getDateInstance().format(Lottery.getInstance().getEndDate()));
		player.sendPacket(html);

		// Send a Server->Client ActionFailed to the L2Player in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void makeCPRecovery(L2Player player)
	{
		if (getNpcId() != 31225 && getNpcId() != 31226)
			return;

		if (!cwCheck(player))
		{
			player.sendMessage("Go away, you're not welcome here.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (!player.reduceAdena("RestoreCP", 100, player.getLastFolkNPC(), true))
			return;

		L2Skill skill = SkillTable.getInstance().getInfo(4380, 1);
		if (skill != null)
		{
			setTarget(player);
			doCast(skill);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	/**
	 * Add Newbie helper buffs to L2Player according to its level.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the range level in wich player must be to obtain buff </li>
	 * <li>If player level is out of range, display a message and return </li>
	 * <li>According to player level cast buff </li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> Newbie Helper Buff list is define in buff templates sql table as "SupportMagic"</B></FONT><BR><BR>
	 *
	 * @param player The L2Player that talk with the L2Npc
	 *
	 */
	public final void makeSupportMagic(L2Player player, String cmd, boolean servitor)
	{
		// Prevent a cursed weapon weilder of being buffed
		if (!cwCheck(player))
			return;

		final int newbieBuffsId = BuffTemplateTable.getInstance().getTemplateIdByName(cmd);

		if (newbieBuffsId == 0)
			return;

		final int lowestLevel = BuffTemplateTable.getInstance().getLowestLevel(newbieBuffsId);
		final int highestLevel = BuffTemplateTable.getInstance().getHighestLevel(newbieBuffsId);

		// If the player is too high level, display a message and return
		if (player.getLevel() > highestLevel)
		{
			insertObjectIdAndShowChatWindow(player, "<html><body>Newbie Guide:<br>Only a <font color=\"LEVEL\">novice character of level "
					+ highestLevel
					+ " or less</font> can receive my support magic.<br>Your novice character is the first one that you created and raised in this world.</body></html>");
			return;
		}

		// If the player is too low level, display a message and return
		if (player.getLevel() < lowestLevel)
		{
			insertObjectIdAndShowChatWindow(player, "<html><body>Come back here when you have reached level " + lowestLevel + ". I will give you support magic then.</body></html>");
			return;
		}

		if (servitor)
		{
			final L2Summon pet = player.getPet();
			if (pet == null || pet instanceof L2PetInstance)
			{
				insertObjectIdAndShowChatWindow(player, "<html><body>Only servitors can receive this Support Magic. If you do not have a servitor, you cannot access these spells.</body></html>");
				return;
			}
		}

		makeBuffs(player, newbieBuffsId, servitor);
	}

	public final void giveBlessingSupport(L2Player player)
	{
		if (player == null)
			return;

		// Blessing of protection - author kerberos_20. Used codes from Rayan - L2Emu project.
		// Prevent a cursed weapon weilder of being buffed - I think no need of that becouse karma check > 0
		// if (player.isCursedWeaponEquiped())
		//   return;

		// Select the player
		setTarget(player);
		// If the player is too high level, display a message and return
		if (player.getLevel() > 39 || player.getClassId().level() >= 2)
		{
			insertObjectIdAndShowChatWindow(player, "<html><body>Newbie Guide:<br>I'm sorry, but you are not eligible to receive the protection blessing.<br1>It can only be bestowed on <font color=\"LEVEL\">characters below level 39 who have not made a seccond transfer.</font></body></html>");
			return;
		}
		L2Skill skill = SkillTable.getInstance().getInfo(5182,1);
		if (skill != null)
			doCast(skill);
	}

	public void showChatWindow(L2Player player)
	{
		showChatWindow(player, 0);
	}

	/**
	 * Returns true if html exists
	 * @param player
	 * @param type
	 * @return boolean
	 */
	private boolean showPkDenyChatWindow(L2Player player, String type)
	{
		String html = HtmCache.getInstance().getHtm("data/npc_data/html/" + type + "/" + getNpcId() + "-pk.htm");

		if (html != null)
		{
			NpcHtmlMessage pkDenyMsg = new NpcHtmlMessage(getObjectId());
			pkDenyMsg.setHtml(html);
			player.sendPacket(pkDenyMsg);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return true;
		}

		return false;
	}

	/**
	 * Open a chat window on client with the text of the L2Npc.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the text of the selected HTML file in function of the npcId and of the page number </li>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2Npc to the L2Player </li>
	 * <li>Send a Server->Client ActionFailed to the L2Player in order to avoid that the client wait another packet </li><BR>
	 *
	 * @param player The L2Player that talk with the L2Npc
	 * @param val The number of the page of the L2Npc to display
	 *
	 */
	public void showChatWindow(L2Player player, int val)
	{
		if (!cwCheck(player) && !(player.getTarget() instanceof L2ClanHallManagerInstance || player.getTarget() instanceof L2DoormenInstance))
		{
			player.setTarget(player);
			return;
		}
		if (player.getKarma() > 0)
		{
			if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && this instanceof L2MerchantInstance)
			{
				if (showPkDenyChatWindow(player, "merchant"))
					return;
			}
			else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && this instanceof L2TeleporterInstance)
			{
				if (showPkDenyChatWindow(player, "teleporter"))
					return;
			}
			else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && this instanceof L2WarehouseInstance)
			{
				if (showPkDenyChatWindow(player, "warehouse"))
					return;
			}
			else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && this instanceof L2FishermanInstance)
			{
				if (showPkDenyChatWindow(player, "fisherman"))
					return;
			}
		}

		if (this instanceof L2AuctioneerInstance && val == 0)
			return;

		int npcId = getTemplate().getNpcId();

		/* For use with Seven Signs implementation */
		String filename = SevenSigns.SEVEN_SIGNS_HTML_PATH;
		int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_AVARICE);
		int sealGnosisOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_GNOSIS);
		int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
		boolean isSealValidationPeriod = SevenSigns.getInstance().isSealValidationPeriod();
		int compWinner = SevenSigns.getInstance().getCabalHighestScore();

		switch (npcId)
		{
		case 31078:
		case 31079:
		case 31080:
		case 31081:
		case 31082: // Dawn Priests
		case 31083:
		case 31084:
		case 31168:
		case 31692:
		case 31694:
		case 31997:
			switch (playerCabal)
			{
			case SevenSigns.CABAL_DAWN:
				if (isSealValidationPeriod)
					if (compWinner == SevenSigns.CABAL_DAWN)
						if (compWinner != sealGnosisOwner)
							filename += "dawn_priest_2c.htm";
						else
							filename += "dawn_priest_2a.htm";
					else
						filename += "dawn_priest_2b.htm";
				else
					filename += "dawn_priest_1b.htm";
				break;
			case SevenSigns.CABAL_DUSK:
				if (isSealValidationPeriod)
					filename += "dawn_priest_3b.htm";
				else
					filename += "dawn_priest_3a.htm";
				break;
			default:
				if (isSealValidationPeriod)
					if (compWinner == SevenSigns.CABAL_DAWN)
						filename += "dawn_priest_4.htm";
					else
						filename += "dawn_priest_2b.htm";
				else
					filename += "dawn_priest_1a.htm";
				break;
			}
			break;
		case 31085:
		case 31086:
		case 31087:
		case 31088: // Dusk Priest
		case 31089:
		case 31090:
		case 31091:
		case 31169:
		case 31693:
		case 31695:
		case 31998:
			switch (playerCabal)
			{
			case SevenSigns.CABAL_DUSK:
				if (isSealValidationPeriod)
					if (compWinner == SevenSigns.CABAL_DUSK)
						if (compWinner != sealGnosisOwner)
							filename += "dusk_priest_2c.htm";
						else
							filename += "dusk_priest_2a.htm";
					else
						filename += "dusk_priest_2b.htm";
				else
					filename += "dusk_priest_1b.htm";
				break;
			case SevenSigns.CABAL_DAWN:
				if (isSealValidationPeriod)
					filename += "dusk_priest_3b.htm";
				else
					filename += "dusk_priest_3a.htm";
				break;
			default:
				if (isSealValidationPeriod)
					if (compWinner == SevenSigns.CABAL_DUSK)
						filename += "dusk_priest_4.htm";
					else
						filename += "dusk_priest_2b.htm";
				else
					filename += "dusk_priest_1a.htm";
				break;
			}
			break;
		case 31127: //
		case 31128: //
		case 31129: // Dawn Festival Guides
		case 31130: //
		case 31131: //
			filename += "festival/dawn_guide.htm";
			break;
		case 31137: //
		case 31138: //
		case 31139: // Dusk Festival Guides
		case 31140: //
		case 31141: //
			filename += "festival/dusk_guide.htm";
			break;
		case 31092: // Black Marketeer of Mammon
			filename += "blkmrkt_1.htm";
			break;
		case 31113: // Merchant of Mammon
			if (Config.ALT_STRICT_SEVENSIGNS)
			{
				switch (compWinner)
				{
				case SevenSigns.CABAL_DAWN:
					if (playerCabal != compWinner || playerCabal != sealAvariceOwner)
					{
						player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
						player.sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					break;
				case SevenSigns.CABAL_DUSK:
					if (playerCabal != compWinner || playerCabal != sealAvariceOwner)
					{
						player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
						player.sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					break;
				}
			}
			filename += "mammmerch_1.htm";
			break;
		case 31126: // Blacksmith of Mammon
			if (Config.ALT_STRICT_SEVENSIGNS)
			{
				switch (compWinner)
				{
				case SevenSigns.CABAL_DAWN:
					if (playerCabal != compWinner || playerCabal != sealGnosisOwner)
					{
						player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
						player.sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					break;
				case SevenSigns.CABAL_DUSK:
					if (playerCabal != compWinner || playerCabal != sealGnosisOwner)
					{
						player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
						player.sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					break;
				}
			}
			filename += "mammblack_1.htm";
			break;
		case 31132:
		case 31133:
		case 31134:
		case 31135:
		case 31136: // Festival Witches
		case 31142:
		case 31143:
		case 31144:
		case 31145:
		case 31146:
			filename += "festival/festival_witch.htm";
			break;
		case 31688:
			if (player.isNoble())
				filename = Olympiad.OLYMPIAD_HTML_PATH + "noble_main.htm";
			else
				filename = (getHtmlPath(npcId, val));
			break;
		case 31690:
		case 31769:
		case 31770:
		case 31771:
		case 31772:
			if (player.isHero())
				filename = Olympiad.OLYMPIAD_HTML_PATH + "hero_main.htm";
			else
				filename = (getHtmlPath(npcId, val));
			break;
		case 36402:
			if (player.getPlayerOlympiad().olyBuff > 0)
				filename = Olympiad.OLYMPIAD_HTML_PATH + (player.getPlayerOlympiad().olyBuff == 5 ? "olympiad_buffs.htm" : "olympiad_5buffs.htm");
			else
				filename = Olympiad.OLYMPIAD_HTML_PATH + "olympiad_nobuffs.htm";
			break;
		default:
			if (npcId >= 31865 && npcId <= 31918)
			{
				if (val == 0)
					filename += "rift/GuardianOfBorder.htm";
				else
					filename += "rift/GuardianOfBorder-" + val + ".htm";
				break;
			}
			if ((npcId >= 31093 && npcId <= 31094) || (npcId >= 31172 && npcId <= 31201) || (npcId >= 31239 && npcId <= 31254))
				return;
			// Get the text of the selected HTML file in function of the npcId and of the page number
			if (this instanceof L2TeleporterInstance && val == 1 && player.getLevel() < Config.ALT_GAME_FREE_TELEPORT_LEVEL) // Players below level 40 have free teleport
			{
				filename = "data/npc_data/html/teleporter/free/" + npcId + ".htm";
				if (!HtmCache.getInstance().pathExists(filename))
					filename = getHtmlPath(npcId, val);
			}
			else
				filename = (getHtmlPath(npcId, val));
			break;
		}

		// Send a Server->Client NpcHtmlMessage containing the text of the L2Npc to the L2Player
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);

		//String word = "npc-"+npcId+(val>0 ? "-"+val : "" )+"-dialog-append";

		if (this instanceof L2MerchantInstance)
			if (Config.LIST_PET_RENT_NPC.contains(npcId))
				html.replace("_Quest", "_RentPet\">Rent Pet</a><br><a action=\"bypass -h npc_%objectId%_Quest");

		html.replace("%objectId%", String.valueOf(getObjectId()));
		if (this instanceof L2FestivalGuideInstance)
		{
			html.replace("%festivalMins%", SevenSignsFestival.getInstance().getTimeToNextFestivalStr());
		}
		player.sendPacket(html);

		// Send a Server->Client ActionFailed to the L2Player in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	/**
	 * Open a chat window on client with the text specified by the given file name and path,<BR>
	 * relative to the datapack root.
	 * <BR><BR>
	 * Added by Tempy
	 * @param player The L2Player that talk with the L2Npc
	 * @param filename The filename that contains the text to send
	 *
	 */
	public void showChatWindow(L2Player player, String filename)
	{
		// Send a Server->Client NpcHtmlMessage containing the text of the L2Npc to the L2Player
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);

		// Send a Server->Client ActionFailed to the L2Player in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	/**
	 * Return the Exp Reward of this L2Npc contained in the L2NpcTemplate (modified by RATE_XP).<BR><BR>
	 */
	public int getExpReward()
	{
		double rateXp = getStat().calcStat(Stats.MAX_HP, 1, this, null);
		return (int) (getTemplate().getRewardExp() * rateXp * Config.RATE_XP);
	}

	/**
	 * Return the SP Reward of this L2Npc contained in the L2NpcTemplate (modified by RATE_SP).<BR><BR>
	 */
	public int getSpReward()
	{
		double rateSp = getStat().calcStat(Stats.MAX_HP, 1, this, null);
		return (int) (getTemplate().getRewardSp() * rateSp * Config.RATE_SP);
	}

	/**
	 * Kill the L2Npc (the corpse disappeared after 7 seconds).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Create a DecayTask to remove the corpse of the L2Npc after 7 seconds </li>
	 * <li>Set target to null and cancel Attack or Cast </li>
	 * <li>Stop movement </li>
	 * <li>Stop HP/MP/CP Regeneration task </li>
	 * <li>Stop all active skills effects in progress on the L2Character </li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all other L2Player to inform </li>
	 * <li>Notify L2Character AI </li><BR><BR>
	 *
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2Attackable </li><BR><BR>
	 *
	 * @param killer The L2Character who killed it
	 *
	 */
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;

		// Normally this wouldn't really be needed, but for those few exceptions,
		// We do need to reset the weapons back to the initial templated weapon.
		_currentLHandId = getTemplate().getLhand();
		_currentRHandId = getTemplate().getRhand();
		_currentCollisionHeight = getTemplate().getCollisionHeight();
		_currentCollisionRadius = getTemplate().getCollisionRadius();
		DecayTaskManager.getInstance().addDecayTask(this);
		return true;
	}

	/**
	 * Set the spawn of the L2Npc.<BR><BR>
	 *
	 * @param spawn The L2Spawn that manage the L2Npc
	 *
	 */
	public void setSpawn(L2Spawn spawn)
	{
		_spawn = spawn;
	}

	@Override
	public void onSpawn()
	{
		if (_inventory != null)
			_inventory.reset();

		super.onSpawn();

		setQuestFirstAttacker(null);
		setQuestAttackStatus(Quest.ATTACK_NOONE);
		if (getTemplate().getEventQuests(Quest.QuestEventType.ON_SPAWN) != null)
			for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_SPAWN))
				quest.notifySpawn(this);
	}

	/**
	 * Remove the L2Npc from the world and update its spawn object (for a complete removal use the deleteMe method).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the L2Npc from the world when the decay task is launched </li>
	 * <li>Decrease its spawn counter </li>
	 * <li>Manage Siege task (killFlag, killCT) </li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World </B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR><BR>
	 *
	 * @see net.l2emuproject.gameserver.world.object.L2Object#decayMe()
	 */
	@Override
	public void onDecay()
	{
		if (isDecayed())
			return;
		setDecayed(true);

		// Reset champion status if the thing is a mob
		setChampion(false);

		// Remove the L2Npc from the world when the decay task is launched
		super.onDecay();

		// Decrease its spawn counter
		if (_spawn != null)
			_spawn.decreaseCount(this);
	}

	private boolean _champion;

	public final void setChampion(boolean champion)
	{
		_champion = champion;
	}

	@Override
	public final boolean isChampion()
	{
		return _champion;
	}

	/**
	 * Remove PROPERLY the L2Npc from the world.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the L2Npc from the world and update its spawn object </li>
	 * <li>Remove all L2Object from _knownObjects and _knownPlayer of the L2Npc then cancel Attack or Cast and notify AI </li>
	 * <li>Remove L2Object object from _allObjects of L2World </li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR><BR>
	 *
	 */
	@Override
	public void deleteMe()
	{
		abortCast();
		abortAttack();
		getStatus().stopHpMpRegeneration();
		getEffects().stopAllEffects(true);

		L2WorldRegion region = getWorldRegion();

		try
		{
			decayMe();
		}
		catch (Exception e)
		{
			_log.fatal("Failed decayMe().", e);
		}

		try
		{
			if (_fusionSkill != null)
				abortCast();

			for (L2Character character : getKnownList().getKnownCharacters())
				if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
					character.abortCast();
		}
		catch (Exception e)
		{
			_log.fatal("Failed deleteMe().", e);
		}

		if (region != null)
			region.removeFromZones(this);

		// Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attack or Cast and notify AI
		try
		{
			getKnownList().removeAllKnownObjects();
		}
		catch (Exception e)
		{
			_log.fatal("Failed removing cleaning knownlist.", e);
		}
		
		super.deleteMe();
	}

	/**
	 * Return the L2Spawn object that manage this L2Npc.<BR><BR>
	 */
	public L2Spawn getSpawn()
	{
		return _spawn;
	}

	@Override
	public String toString()
	{
		return getTemplate().getName();
	}

	public boolean isDecayed()
	{
		return _isDecayed;
	}

	public void setDecayed(boolean decayed)
	{
		_isDecayed = decayed;
	}

	public void endDecayTask()
	{
		if (!isDecayed())
		{
			DecayTaskManager.getInstance().cancelDecayTask(this);
			onDecay();
		}
	}

	public boolean isMob() // Rather delete this check
	{
		return false; // This means we use MAX_NPC_ANIMATION instead of MAX_MONSTER_ANIMATION
	}

	// Two functions to change the appearance of the equipped weapons on the NPC
	// This is only useful for a few NPCs and is most likely going to be called from AI
	public void setLHandId(int newWeaponId)
	{
		_currentLHandId = newWeaponId;
		updateAbnormalEffect();
	}

	public void setRHandId(int newWeaponId)
	{
		_currentRHandId = newWeaponId;
		updateAbnormalEffect();
	}

	public void setLRHandId(int newLWeaponId, int newRWeaponId)
	{
		_currentRHandId = newRWeaponId;
		_currentLHandId = newLWeaponId;
		updateAbnormalEffect();
	}

	public void setCollisionHeight(double height)
	{
		_currentCollisionHeight = height;
	}

	public void setCollisionRadius(double radius)
	{
		_currentCollisionRadius = radius;
	}

	public double getCollisionHeight()
	{
		return _currentCollisionHeight;
	}

	public double getCollisionRadius()
	{
		return _currentCollisionRadius;
	}

	@Override
	protected final CharShots initShots()
	{
		return new NpcShots(this);
	}

	@Override
	public final NpcShots getShots()
	{
		return (NpcShots)_shots;
	}

	@Override
	public NpcInventory getInventory()
	{
		return _inventory;
	}

	private boolean cwCheck(L2Player player)
	{
		return Config.CURSED_WEAPON_NPC_INTERACT || !player.isCursedWeaponEquipped();
	}

	@Override
	public void sendInfo(L2Player activeChar)
	{
		if (Config.TEST_KNOWNLIST && activeChar.isGM())
			activeChar.sendMessage("Knownlist, added NPC: " + getName());

		activeChar.sendPacket(getRunSpeed() == 0 ? new ServerObjectInfo(this) : new AbstractNpcInfo.NpcInfo(this));
	}

	@Override
	public void broadcastFullInfoImpl()
	{
		broadcastPacket(getRunSpeed() == 0 ? new ServerObjectInfo(this) : new AbstractNpcInfo.NpcInfo(this));
	}
	
	public void showNoTeachHtml(L2Player player)
	{
		final int npcId = getNpcId();
		String path = null;
		
		if (this instanceof L2WarehouseInstance)
			path = "data/npc_data/html/warehouse/" + npcId + "-noteach.htm";
		else if (this instanceof L2TrainerInstance)
			path = "data/npc_data/html/trainer/" + npcId + "-noteach.htm";
		
		NpcHtmlMessage noTeachMsg = new NpcHtmlMessage(getObjectId());
		noTeachMsg.setFile(path);
		noTeachMsg.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(noTeachMsg);
	}

	public void setKillable(boolean b)
	{
		_isKillable = b;
	}

	public boolean isKillable()
	{
		return _isKillable;
	}

	public void setQuestDropable(boolean b)
	{
		_questDropable = b;
	}

	public boolean getQuestDropable()
	{
		return _questDropable;
	}

	public boolean canBeChampion()
	{
		switch (getNpcId())
		{
		// Devastated Castle
		case 35411: case 35412: case 35413: case 35414: case 35415: case 35416:
			// Fortress of the Dead
		case 35629: case 35630: case 35631: case 35632: case 35633: case 35634: case 35635:
		case 35636: case 35637:
			return false;
		}
		if (this instanceof L2CCHBossInstance)
			return false;
		return ((this instanceof L2MonsterInstance && !(this instanceof L2Boss)) || (this instanceof L2Boss && Config.CHAMPION_BOSS))
		&& Config.CHAMPION_FREQUENCY > 0 && !getTemplate().isQuestMonster() && getLevel() >= Config.CHAMPION_MIN_LEVEL
		&& getLevel() <= Config.CHAMPION_MAX_LEVEL;
	}

	public int getQuestAttackStatus()
	{
		return _questAttackStatus;
	}

	public void setQuestAttackStatus(int status)
	{
		_questAttackStatus = status;
	}

	public L2Player getQuestFirstAttacker()
	{
		return _questFirstAttacker;
	}

	public void setQuestFirstAttacker(L2Player attacker)
	{
		_questFirstAttacker = attacker;
	}

	public int getWeaponEnchantLevel()
	{
		return _weaponEnchant;
	}

	public void setWeaponEnchantLevel(int level)
	{
		_weaponEnchant = level;
	}
	
	public final void setHideName(boolean val)
	{
		_isHideName = val;
	}
	
	public final boolean isHideName()
	{
		return _isHideName;
	}
	
	public int getColorEffect()
	{
		return 0;
	}
	
	public final void setDisplayEffect(int val)
	{
		if (val != _displayEffect)
		{
			_displayEffect = val;
			broadcastPacket(new ExChangeNpcState(getObjectId(), val));
		}
	}
	
	public final int getDisplayEffect()
	{
		return _displayEffect;
	}
	
	public final void setAutoAttackable(boolean status)
	{
		_isAutoAttackable = status;
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return _isAutoAttackable;
	}
	
	public L2Npc scheduleDespawn(long delay)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new DespawnTask(), delay);
		return this;
	}
	
	private final class DespawnTask implements Runnable
	{
		@Override
		public void run()
		{
			if (!L2Npc.this.isDecayed())
				L2Npc.this.deleteMe();
		}
	}
	
	/**
	 * This Data are use by Specialize NPC only, still underconstruction...
	 * therefore don't expect it to work perfectly or more...
	 * @return
	 */
	public final L2NpcCharData getCharDataStatic()
	{
		final L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(getTemplate().getNpcId());
		
		return npcData.getCharDataStatic();
	}
	
	public final int getIsChar()
	{
		final L2NpcCharData npcChar = getCharDataStatic();
		if (npcChar == null)
			return 0;
		else
			return npcChar.getIsChar();
	}

	public final int getCharClass()
	{
		final L2NpcCharData npcChar = getCharDataStatic();
		if (npcChar == null)
			return 0;
		else
			return npcChar.getCharClass();
	}

	public final int getCharRace()
	{
		final L2NpcCharData npcChar = getCharDataStatic();
		if (npcChar == null)
			return 0;
		else
			return npcChar.getCharRace();
	}

	public final int getLrhand()
	{
		final L2NpcCharData npcChar = getCharDataStatic();
		if (npcChar == null)
			return 0;
		else
			return npcChar.getLrhand();
	}

	public final int getArmors()
	{
		final L2NpcCharData npcChar = getCharDataStatic();
		if (npcChar == null)
			return 0;
		else
			return npcChar.getArmor();
	}

	public final int getPant()
	{
		final L2NpcCharData npcChar = getCharDataStatic();
		if (npcChar == null)
			return 0;
		else
			return npcChar.getPant();
	}

	public final int getHead()
	{
		final L2NpcCharData npcChar = getCharDataStatic();
		if (npcChar == null)
			return 0;
		else
			return npcChar.getHead();
	}

	public final int getGlove()
	{
		final L2NpcCharData npcChar = getCharDataStatic();
		if (npcChar == null)
			return 0;
		else
			return npcChar.getGlove();
	}

	public final int getBoot()
	{
		final L2NpcCharData npcChar = getCharDataStatic();
		if (npcChar == null)
			return 0;
		else
			return npcChar.getBoot();
	}

	public final int getDHair()
	{
		final L2NpcCharData npcChar = getCharDataStatic();
		if (npcChar == null)
			return 0;
		else
			return npcChar.getDHair();
	}

	public final int getFace()
	{
		final L2NpcCharData npcChar = getCharDataStatic();
		if (npcChar == null)
			return 0;
		else
			return npcChar.getFace();
	}

	public final int getHair()
	{
		final L2NpcCharData npcChar = getCharDataStatic();
		if (npcChar == null)
			return 0;
		else
			return npcChar.getHair();
	}

	public final int getAugmentation()
	{
		final L2NpcCharData npcChar = getCharDataStatic();
		if (npcChar == null)
			return 0;
		else
			return npcChar.getAugmentation();
	}

	public final int getEnchLvl()
	{
		final L2NpcCharData npcChar = getCharDataStatic();
		if (npcChar == null)
			return 0;
		else
			return npcChar.getEnchLvl();
	}

	public final int getCharFace()
	{
		final L2NpcCharData npcChar = getCharDataStatic();
		if (npcChar == null)
			return 0;
		else
			return npcChar.getCharFace();
	}

	public final int getCharHair()
	{
		final L2NpcCharData npcChar = getCharDataStatic();
		if (npcChar == null)
			return 0;
		else
			return npcChar.getCharHair();
	}

	public final int getCharHairColor()
	{
		final L2NpcCharData npcChar = getCharDataStatic();
		if (npcChar == null)
			return 0;
		else
			return npcChar.getCharHairColor();
	}

	public final String getCharColor()
	{
		final L2NpcCharData npcChar = getCharDataStatic();
		if (npcChar == null)
			return "0xFFFFFF";
		else
			return npcChar.getCharColor();
	}

	public final int getCharSex()
	{
		final L2NpcCharData npcChar = getCharDataStatic();
		if (npcChar == null)
			return 0;
		else
			return npcChar.getCharSex();
	}

	public final int getCharHero()
	{
		final L2NpcCharData npcChar = getCharDataStatic();
		if (npcChar == null)
			return 0;
		else
			return npcChar.getCharHero();
	}
}
