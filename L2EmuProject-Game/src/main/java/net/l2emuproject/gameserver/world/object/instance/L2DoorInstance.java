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
package net.l2emuproject.gameserver.world.object.instance;

import java.util.concurrent.ScheduledFuture;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.ClanTable;
import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.entity.ai.L2CharacterAI;
import net.l2emuproject.gameserver.entity.ai.L2DoorAI;
import net.l2emuproject.gameserver.entity.stat.CharStat;
import net.l2emuproject.gameserver.entity.stat.DoorStat;
import net.l2emuproject.gameserver.events.global.clanhallsiege.ClanHall;
import net.l2emuproject.gameserver.events.global.fortsiege.Fort;
import net.l2emuproject.gameserver.events.global.fortsiege.FortManager;
import net.l2emuproject.gameserver.events.global.siege.Castle;
import net.l2emuproject.gameserver.events.global.siege.CastleManager;
import net.l2emuproject.gameserver.events.global.siege.L2SiegeClan;
import net.l2emuproject.gameserver.events.global.siege.Siege;
import net.l2emuproject.gameserver.events.global.siege.SiegeManager;
import net.l2emuproject.gameserver.events.global.territorywar.TerritoryWarManager;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.clientpackets.ConfirmDlgAnswer.AnswerHandler;
import net.l2emuproject.gameserver.network.serverpackets.ConfirmDlg;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.network.serverpackets.StaticObject;
import net.l2emuproject.gameserver.services.clan.L2Clan;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.system.threadmanager.ThreadPoolManager;
import net.l2emuproject.gameserver.templates.chars.L2CharTemplate;
import net.l2emuproject.gameserver.world.geodata.GeoData;
import net.l2emuproject.gameserver.world.knownlist.CharKnownList;
import net.l2emuproject.gameserver.world.knownlist.DoorKnownList;
import net.l2emuproject.gameserver.world.mapregion.L2MapRegion;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Playable;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.position.L2CharPosition;
import net.l2emuproject.lang.L2Math;
import net.l2emuproject.lang.L2TextBuilder;


public class L2DoorInstance extends L2Character
{
	/** The castle index in the array of L2Castle this L2DoorInstance belongs to */
	private int					_castleIndex		= -2;
	private Castle				_castle;
	/** The fort index in the array of L2Fort this L2DoorInstance belongs to */
	private int					_fortId				= -2;
	private Fort				_fort;

	private L2MapRegion			_mapRegion			= null;

	protected final int			_doorId;
	protected final String		_name;
	private boolean				_open;
	private boolean				_commanderDoor;
	private final boolean		_unlockable;

	// when door is closed, the dimensions are
	private int					_rangeXMin			= 0;
	private int					_rangeYMin			= 0;
	private int					_rangeZMin			= 0;
	private int					_rangeXMax			= 0;
	private int					_rangeYMax			= 0;
	private int					_rangeZMax			= 0;

	// these variables assist in see-through calculation only
	private int					_A					= 0;
	private int					_B					= 0;
	private int					_C					= 0;
	private int					_D					= 0;

	private ClanHall			_clanHall;

	protected int				_autoActionDelay	= -1;
	private ScheduledFuture<?>	_autoActionTask;

	private boolean				_seedOfDestructionAttackableDoor = false;

	/** This class may be created only by L2Character and only for AI */
	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor()
		{
		}

		@Override
		public L2DoorInstance getActor()
		{
			return L2DoorInstance.this;
		}

		@Override
		public void moveTo(int x, int y, int z, int offset)
		{
		}

		@Override
		public void moveTo(int x, int y, int z)
		{
		}

		@Override
		public void stopMove(L2CharPosition pos)
		{
		}

		@Override
		public void doAttack(L2Character target)
		{
		}

		@Override
		public void doCast(L2Skill skill)
		{
		}
	}

	@Override
	protected L2CharacterAI initAI()
	{
		return new L2DoorAI(new AIAccessor());
	}

	class CloseTask implements Runnable
	{
		@Override
		public void run()
		{
			onClose();
		}
	}

	/**
	 * Manages the auto open and closing of a door.
	 */
	class AutoOpenClose implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				String doorAction;

				if (!isOpen())
				{
					doorAction = "opened";
					openMe();
				}
				else
				{
					doorAction = "closed";
					closeMe();
				}

				if (_log.isDebugEnabled())
					_log.info("Auto " + doorAction + " door ID " + _doorId + " (" + _name + ") for " + (_autoActionDelay / 60000) + " minute(s).");
			}
			catch (Exception e)
			{
				_log.warn("Could not auto open/close door ID " + _doorId + " (" + _name + ")", e);
			}
		}
	}

	public L2DoorInstance(int objectId, L2CharTemplate template, int doorId, String name, boolean unlockable)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		getStat(); // init stats
		getStatus(); // init status
		setIsInvul(false);
		_doorId = doorId;
		_name = name;
		_unlockable = unlockable;
	}

	@Override
	protected CharKnownList initKnownList()
	{
		return new DoorKnownList(this);
	}

	@Override
	public final DoorKnownList getKnownList()
	{
		return (DoorKnownList) _knownList;
	}

	@Override
	protected CharStat initStat()
	{
		return new DoorStat(this);
	}

	@Override
	public DoorStat getStat()
	{
		return (DoorStat) _stat;
	}

	public final boolean isUnlockable()
	{
		return _unlockable;
	}

	@Override
	public final int getLevel()
	{
		return 1;
	}

	public int getDoorId()
	{
		return _doorId;
	}

	public boolean isOpen()
	{
		return _open;
	}

	public void setOpen(boolean open)
	{
		_open = open;
		GeoData.getInstance().setDoorGeodataOpen(this, open);
		getKnownList().updateKnownObjects();
		broadcastFullInfo();
	}

	/**
	 * @param val Used for Fortresses to determine if doors can be attacked during siege or not
	 */
	public void setIsCommanderDoor(boolean val)
	{
		_commanderDoor = val;
	}

	/**
	 * @return Doors that cannot be attacked during siege
	 * these doors will be auto opened if u take control of all commanders buildings
	 */
	public boolean isCommanderDoor()
	{
		return _commanderDoor;
	}

	/**
	 * Sets the delay in milliseconds for automatic opening/closing of this door
	 * instance. <BR>
	 * <B>Note:</B> A value of -1 cancels the auto open/close task.
	 * @param actionDelay open/close delay
	 */
	public void setAutoActionDelay(int actionDelay)
	{
		if (_autoActionDelay == actionDelay)
			return;

		if (actionDelay > -1)
		{
			AutoOpenClose ao = new AutoOpenClose();
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(ao, actionDelay, actionDelay);
		}
		else
		{
			if (_autoActionTask != null)
				_autoActionTask.cancel(false);
		}

		_autoActionDelay = actionDelay;
	}

	public int getDamageGrade()
	{
		return L2Math.limit(0, 6 - Math.ceil(getStatus().getCurrentHp() / getMaxHp() * 6), 6);
	}

	public final Castle getCastle()
	{
		if (_castle == null)
		{
			Castle castle = null;

			if (_castleIndex < 0)
			{
				castle = CastleManager.getInstance().getCastle(this);
				if (castle != null)
					_castleIndex = castle.getCastleId();
			}
			if (_castleIndex > 0)
				castle = CastleManager.getInstance().getCastleById(_castleIndex);
			_castle = castle;
		}
		return _castle;
	}

	public final Fort getFort()
	{
		if (_fort == null)
		{
			Fort fort = null;

			if (_fortId < 0)
			{
				fort = FortManager.getInstance().getFort(this);
				if (fort != null)
					_fortId = fort.getFortId();
			}
			if (_fortId > 0)
				fort = FortManager.getInstance().getFortById(_fortId);
			_fort = fort;
		}
		return _fort;
	}

	public void setClanHall(ClanHall clanHall)
	{
		_clanHall = clanHall;
	}

	public ClanHall getClanHall()
	{
		return _clanHall;
	}

	public boolean isEnemy()
	{
		if (getCastle() != null && getCastle().getSiege().getIsInProgress())
			return true;
		else if (getFort() != null && getFort().getSiege().getIsInProgress() && !isCommanderDoor())
			return true;
		else if (getClanHall() != null && getClanHall().getSiege() != null)
			return getClanHall().getSiege().getIsInProgress();
		return false;
    }

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (isUnlockable() && getFort() == null)
			return true;

		// Doors can't be attacked by NPCs
		if (!(attacker instanceof L2Playable))
			return false;

		if (_seedOfDestructionAttackableDoor)
			return true;

		// Attackable only during siege by everyone (not owner)
		boolean isCastle = (getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress());
		boolean isFort = (getFort() != null && getFort().getFortId() > 0 && getFort().getSiege().getIsInProgress() && !isCommanderDoor());
		boolean isHideout = (getClanHall() != null && getClanHall().getSiege() != null && getClanHall().getSiege().getIsInProgress());
		int activeSiegeId = (getFort() != null ? getFort().getFortId() : (getCastle() != null ? getCastle().getCastleId() : 0));
		L2Player actingPlayer = attacker.getActingPlayer();
		L2Clan clan = actingPlayer.getClan();

		if (TerritoryWarManager.getInstance().isTWInProgress())
		{
			if (TerritoryWarManager.getInstance().isAllyField(actingPlayer, activeSiegeId))
				return false;
			else
				return true;
		}
		else if (isFort)
		{
			if (attacker instanceof L2SummonInstance)
			{
				if (clan != null && clan == getFort().getOwnerClan())
					return false;
			}
			else if (attacker instanceof L2Player)
			{
				if (clan != null && clan == getFort().getOwnerClan())
					return false;
			}
		}
		else if (isCastle)
		{
			if (attacker instanceof L2SummonInstance)
			{
				if (clan != null && clan.getClanId() == getCastle().getOwnerId())
					return false;
			}
			else if (attacker instanceof L2Player)
			{
				if (clan != null && clan.getClanId() == getCastle().getOwnerId())
					return false;
			}
		}

		return (isCastle || isFort || isHideout);
	}

	public boolean isAttackable(L2Character attacker)
	{
		return isAutoAttackable(attacker);
	}

	@Override
	public void onAction(L2Player player)
	{
		if (player == null)
			return;

		if (Config.SIEGE_ONLY_REGISTERED)
		{
			boolean opp = false;
			Siege siege = SiegeManager.getInstance().getSiege(player);
			L2Clan oppClan = player.getClan();
			if (siege != null && siege.getIsInProgress())
			{
				if (oppClan != null)
				{
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
				}
			}
			else
				opp = true;

			if (!opp)
				return;
		}

		// Check if the L2Player already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2Player player
			player.setTarget(this);

			sendInfo(player);
		}
		else
		{
			if (isAutoAttackable(player))
			{
				if (Math.abs(player.getZ() - getZ()) < 400) // this max heigth
				// difference might
				// need some
				// tweaking
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				}
			}
			else if (player.getClan() != null && getClanHall() != null && player.getClanId() == getClanHall().getOwnerId())
			{
				if (!isInsideRadius(player, L2Npc.INTERACTION_DISTANCE, false, false))
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
				else
				{
					askGateOpenClose(player);
				}
			}
			else if (player.getClan() != null && getFort() != null && player.getClanId() == getFort().getOwnerId() && isUnlockable() && !getFort().getSiege().getIsInProgress())
			{
				if (!isInsideRadius(player, L2Npc.INTERACTION_DISTANCE, false, false))
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
				else
				{
					askGateOpenClose(player);
				}
			}
		}
	}
	
	private void askGateOpenClose(L2Player player)
	{
		if (!isOpen())
		{
			player.sendPacket(new ConfirmDlg(SystemMessageId.WOULD_YOU_LIKE_TO_OPEN_THE_GATE).addAnswerHandler(new AnswerHandler() {
				@Override
				public void handle(boolean answer)
				{
					if (answer)
						openMe();
				}
			}));
		}
		else
		{
			player.sendPacket(new ConfirmDlg(SystemMessageId.WOULD_YOU_LIKE_TO_CLOSE_THE_GATE).addAnswerHandler(new AnswerHandler() {
				@Override
				public void handle(boolean answer)
				{
					if (answer)
						closeMe();
				}
			}));
		}
	}

	@Override
	public void onActionShift(L2Player player)
	{
		if (player.getAccessLevel() >= Config.GM_ACCESSLEVEL)
		{
			player.setTarget(this);

			sendInfo(player);

			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			L2TextBuilder html1 = L2TextBuilder.newInstance("<html><body><table border=0>");
			html1.append("<tr><td>S.Y.L. Says:</td></tr>");
			html1.append("<tr><td>Current HP  " + getStatus().getCurrentHp() + "</td></tr>");
			html1.append("<tr><td>Max HP      " + getMaxHp() + "</td></tr>");
			html1.append("<tr><td>Max X       " + getXMax() + "</td></tr>");
			html1.append("<tr><td>Max Y       " + getYMax() + "</td></tr>");
			html1.append("<tr><td>Max Z       " + getZMax() + "</td></tr>");
			html1.append("<tr><td>Min X       " + getXMin() + "</td></tr>");
			html1.append("<tr><td>Min Y       " + getYMin() + "</td></tr>");
			html1.append("<tr><td>Min Z       " + getZMin() + "</td></tr>");
			html1.append("<tr><td>Object ID:  " + getObjectId() + "</td></tr>");
			html1.append("<tr><td>Door ID: <br>" + getDoorId() + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");
			html1.append("<tr><td><br></td></tr>");

			html1.append("<tr><td>Class: " + getClass().getName() + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");
			html1.append("</table>");

			html1.append("<table><tr>");
			html1.append("<td><button value=\"Open\" action=\"bypass -h admin_open " + getDoorId()
					+ "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			html1.append("<td><button value=\"Close\" action=\"bypass -h admin_close " + getDoorId()
					+ "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			html1
					.append("<td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			html1
					.append("<td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			html1.append("</tr></table></body></html>");

			html.setHtml(html1.moveToString());
			player.sendPacket(html);
		}
		else
		{
			// ATTACK the mob without moving?
		}
	}

	@Override
	public final void broadcastStatusUpdateImpl()
	{
		broadcastFullInfoImpl();
	}

	public void onOpen()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new CloseTask(), 60000);
	}

	public void onClose()
	{
		closeMe();
	}

	public final void closeMe()
	{
		setOpen(false);
	}

	public final void openMe()
	{
		setOpen(true);
	}

	@Override
	public String toString()
	{
		return "door " + _doorId;
	}

	public int getXMin()
	{
		return _rangeXMin;
	}

	public int getYMin()
	{
		return _rangeYMin;
	}

	public int getZMin()
	{
		return _rangeZMin;
	}

	public int getXMax()
	{
		return _rangeXMax;
	}

	public int getYMax()
	{
		return _rangeYMax;
	}

	public int getZMax()
	{
		return _rangeZMax;
	}

	public void setRange(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax)
	{
		_rangeXMin = xMin;
		_rangeYMin = yMin;
		_rangeZMin = zMin;

		_rangeXMax = xMax;
		_rangeYMax = yMax;
		_rangeZMax = zMax;

		_A = _rangeYMax * (_rangeZMax - _rangeZMin) + _rangeYMin * (_rangeZMin - _rangeZMax);
		_B = _rangeZMin * (_rangeXMax - _rangeXMin) + _rangeZMax * (_rangeXMin - _rangeXMax);
		_C = _rangeXMin * (_rangeYMax - _rangeYMin) + _rangeXMin * (_rangeYMin - _rangeYMax);
		_D = -1
				* (_rangeXMin * (_rangeYMax * _rangeZMax - _rangeYMin * _rangeZMax) + _rangeXMax * (_rangeYMin * _rangeZMin - _rangeYMin * _rangeZMax) + _rangeXMin
						* (_rangeYMin * _rangeZMax - _rangeYMax * _rangeZMin));
	}

	public String getDoorName()
	{
		return _name;
	}

	public L2MapRegion getMapRegion()
	{
		return _mapRegion;
	}

	public void setMapRegion(L2MapRegion region)
	{
		_mapRegion = region;
	}

	public int getA()
	{
		return _A;
	}

	public int getB()
	{
		return _B;
	}

	public int getC()
	{
		return _C;
	}

	public int getD()
	{
		return _D;
	}

	// FIXME: BADLY IMPLEMENTED METHODS
	// Most doors (Castle, CH, Fort, automatic) show hp and are targetable
	// Some automatic doors (e.g. krateis cube) are NOT targetable (and don't show hp)

	@Override
	public void sendInfo(L2Player activeChar)
	{
		activeChar.sendPacket(new StaticObject(this));
	}

	@Override
	public void broadcastFullInfoImpl()
	{
		broadcastPacket(new StaticObject(this));
	}

	// BADLY IMPLEMENTED METHODS END

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;

		if (isEnemy())
			broadcastPacket(SystemMessageId.CASTLE_GATE_BROKEN_DOWN.getSystemMessage());
		
		GeoData.getInstance().setDoorGeodataOpen(this, true);

		return true;
	}

	public void setIsSeedOfDestructionAttackableDoor(boolean val)
	{
		_seedOfDestructionAttackableDoor = val;
	}

	public boolean isSeedOfDestructionAttackableDoor()
	{
		return _seedOfDestructionAttackableDoor;
	}
}
