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
package net.l2emuproject.gameserver.events.global.clanhallsiege;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.l2emuproject.gameserver.datatables.ClanTable;
import net.l2emuproject.gameserver.datatables.DoorTable;
import net.l2emuproject.gameserver.entity.itemcontainer.PcInventory;
import net.l2emuproject.gameserver.events.global.siege.Siegeable;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.auction.AuctionService;
import net.l2emuproject.gameserver.services.clan.L2Clan;
import net.l2emuproject.gameserver.system.database.L2DatabaseFactory;
import net.l2emuproject.gameserver.system.threadmanager.ThreadPoolManager;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2DoorInstance;

public final class ClanHall extends Siegeable<CCHSiege>
{
	private int								_clanHallId;
	private List<L2DoorInstance>			_doors;
	private List<String>					_doorDefault;
	private L2Clan							_ownerClan;
	private int								_lease;
	private String							_desc;
	private String							_location;
	protected long							_paidUntil;
	private int								_grade;
	protected final int						_chRate						= 604800000;
	protected boolean						_isFree						= true;
	private Map<Integer, ClanHallFunction>	_functions;
	protected boolean						_paid;

	// Only for contestable clan halls
	private CCHSiege						_siege;
	private Calendar						_siegeDate;
	private boolean							_isTimeRegistrationOver		= true;
	private Calendar						_siegeTimeRegistrationEndDate;

	/** Clan Hall Functions */
	public static final byte				FUNC_TELEPORT				= 1;
	public static final byte				FUNC_ITEM_CREATE			= 2;
	public static final byte				FUNC_RESTORE_HP				= 3;
	public static final byte				FUNC_RESTORE_MP				= 4;
	public static final byte				FUNC_RESTORE_EXP			= 5;
	public static final byte				FUNC_SUPPORT				= 6;
	public static final byte				FUNC_DECO_FRONTPLATEFORM	= 7;
	public static final byte				FUNC_DECO_CURTAINS			= 8;

	public final class ClanHallFunction
	{
		private int			_type;
		private int			_lvl;
		protected int		_fee;
		protected int		_tempFee;
		private long		_rate;
		private long		_endDate;
		protected boolean	_inDebt;
		public boolean		_cwh;		// first activating clanhall function is payed from player inventory, any others from clan warehouse

		public ClanHallFunction(int type, int lvl, int lease, int tempLease, long rate, long time, boolean cwh)
		{
			_type = type;
			_lvl = lvl;
			_fee = lease;
			_tempFee = tempLease;
			_rate = rate;
			_endDate = time;
			initializeFunctionTask(cwh);
		}

		public final int getType()
		{
			return _type;
		}

		public final int getLvl()
		{
			return _lvl;
		}

		public final int getLease()
		{
			return _fee;
		}

		public final long getRate()
		{
			return _rate;
		}

		public final long getEndTime()
		{
			return _endDate;
		}

		public final void setLvl(int lvl)
		{
			_lvl = lvl;
		}

		public final void setLease(int lease)
		{
			_fee = lease;
		}

		public final void setEndTime(long time)
		{
			_endDate = time;
		}

		private final void initializeFunctionTask(boolean cwh)
		{
			if (_isFree)
				return;
			long currentTime = System.currentTimeMillis();
			if (_endDate > currentTime)
				ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(cwh), _endDate - currentTime);
			else
				ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(cwh), 0);
		}

		private final class FunctionTask implements Runnable
		{
			public FunctionTask(boolean cwh)
			{
				_cwh = cwh;
			}

			@Override
			public final void run()
			{
				try
				{
					if (_isFree)
						return;
					if (getOwnerClan().getWarehouse().getAdena() >= _fee || !_cwh)
					{
						int fee = _fee;
						boolean newfc = true;
						if (getEndTime() == 0 || getEndTime() == -1)
						{
							if (getEndTime() == -1)
							{
								newfc = false;
								fee = _tempFee;
							}
						}
						else
							newfc = false;
						setEndTime(System.currentTimeMillis() + getRate());
						dbSave(newfc);
						if (_cwh)
						{
							getOwnerClan().getWarehouse().destroyItemByItemId("CH_function_fee", PcInventory.ADENA_ID, fee, null, null);
							if (_log.isDebugEnabled())
								_log.warn("deducted " + fee + " adena from " + getName() + " owner's cwh for function id : " + getType());
						}
						ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(true), getRate());
					}
					else
						removeFunction(getType());
				}
				catch (Exception e)
				{
					_log.error(e.getMessage(), e);
				}
			}
		}

		public final void dbSave(boolean newFunction)
		{
			Connection con = null;
			try
			{
				PreparedStatement statement;

				con = L2DatabaseFactory.getInstance().getConnection(con);
				if (newFunction)
				{
					statement = con.prepareStatement("INSERT INTO clanhall_functions (hall_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)");
					statement.setInt(1, getId());
					statement.setInt(2, getType());
					statement.setInt(3, getLvl());
					statement.setInt(4, getLease());
					statement.setLong(5, getRate());
					statement.setLong(6, getEndTime());
				}
				else
				{
					statement = con.prepareStatement("UPDATE clanhall_functions SET lvl=?, lease=?, endTime=? WHERE hall_id=? AND type=?");
					statement.setInt(1, getLvl());
					statement.setInt(2, getLease());
					statement.setLong(3, getEndTime());
					statement.setInt(4, getId());
					statement.setInt(5, getType());
				}
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.fatal("Exception: ClanHall.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " + e.getMessage(), e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}

	public ClanHall(int clanHallId, String name, int ownerId, int lease, String desc, String location, long paidUntil, int Grade, boolean paid)
	{
		super(clanHallId);
		_clanHallId = clanHallId;
		_name = name;
		_ownerId = ownerId;
		if (_log.isDebugEnabled())
			_log.warn("Init Owner : " + _ownerId);
		_lease = lease;
		_desc = desc;
		_location = location;
		_paidUntil = paidUntil;
		_grade = Grade;
		_paid = paid;
		//_doorDefault = new FastList<String>();
		_functions = new FastMap<Integer, ClanHallFunction>();
		if (ownerId != 0)
		{
			_isFree = false;
			initializeTask(false);
			loadFunctions();
		}
		loadSiegeData();
	}

	/** Return if clanHall is paid or not */
	public final boolean getPaid()
	{
		return _paid;
	}

	/** Return Id Of Clan hall */
	public final int getId()
	{
		return _clanHallId;
	}

	/** Return lease*/
	public final int getLease()
	{
		return _lease;
	}

	/** Return Desc */
	public final String getDesc()
	{
		return _desc;
	}

	/** Return Location */
	public final String getLocation()
	{
		return _location;
	}

	/** Return PaidUntil */
	public final long getPaidUntil()
	{
		return _paidUntil;
	}

	/** Return Grade */
	public final int getGrade()
	{
		return _grade;
	}

	/** Return all DoorInstance */
	public final List<L2DoorInstance> getDoors()
	{
		if (_doors == null)
			_doors = new FastList<L2DoorInstance>();
		return _doors;
	}

	/** Return Door */
	public final L2DoorInstance getDoor(int doorId)
	{
		if (doorId <= 0)
			return null;
		for (L2DoorInstance door : getDoors())
		{
			if (door.getDoorId() == doorId)
				return door;
		}
		return null;
	}

	/** Return function with id */
	public final ClanHallFunction getFunction(int type)
	{
		if (_functions.get(type) != null)
			return _functions.get(type);
		return null;
	}

	/** Free this clan hall */
	public final void free()
	{
		_ownerId = 0;
		if (_ownerClan != null)
		{
			_ownerClan.setHasHideout(0);
			_ownerClan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(_ownerClan));
			_ownerClan = null;
		}
		_isFree = true;
		for (Map.Entry<Integer, ClanHallFunction> fc : _functions.entrySet())
			removeFunction(fc.getKey());
		_functions.clear();
		_paidUntil = 0;
		updateDb();
	}

	/** Set owner if clan hall is free */
	public final void setOwner(L2Clan clan)
	{
		// An owner exists - must not mess with auction, as GM commands call a different method
		if (!isSiegeable() && _ownerId > 0)
			return;

		// An owner might exist, but we wont mess anything
		if (isSiegeable())
			free();
		// New owner doesn't exist. Stop here.
		if (clan == null)
			return;

		_ownerId = clan.getClanId();
		_ownerClan = clan;
		_isFree = false;
		if (isSiegeable())
		{
			_paidUntil = Long.MAX_VALUE;
			_paid = true;
			clan.setHasHideout(getId());
		}
		else
			_paidUntil = System.currentTimeMillis();
		initializeTask(true);
		// Announce to Online member new ClanHall
		clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
		updateDb();
	}

	/** Respawn all doors */
	public final void spawnDoor()
	{
		spawnDoor(false);
	}

	/** Respawn all doors */
	public final void spawnDoor(boolean isDoorWeak)
	{
		for (int i = 0; i < getDoors().size(); i++)
		{
			L2DoorInstance door = getDoors().get(i);
			if (door.getStatus().getCurrentHp() <= 0)
			{
				door.decayMe(); // Kill current if not killed already
				// This doesn't even work
				door = DoorTable.parseLine(_doorDefault.get(i));
				DoorTable.getInstance().putDoor(door); //Read the new door to the DoorTable By Erb
				if (isDoorWeak)
					door.getStatus().setCurrentHp(door.getMaxHp() / 2);
				door.spawnMe(door.getX(), door.getY(), door.getZ());
				getDoors().set(i, door);
			}
			else if (door.isOpen())
				door.closeMe();
		}
	}

	/** Open or Close Door */
	public final void openCloseDoor(L2Player activeChar, int doorId, boolean open)
	{
		if (activeChar != null && activeChar.getClanId() == getOwnerId())
			openCloseDoor(doorId, open);
	}

	public final void openCloseDoor(int doorId, boolean open)
	{
		openCloseDoor(getDoor(doorId), open);
	}

	public final void openCloseDoor(L2DoorInstance door, boolean open)
	{
		if (door != null)
		{
			if (open)
				door.openMe();
			else
				door.closeMe();
		}
	}

	public final void openCloseDoors(L2Player activeChar, boolean open)
	{
		if (activeChar != null && activeChar.getClanId() == getOwnerId())
			openCloseDoors(open);
	}

	public final void openCloseDoors(boolean open)
	{
		for (L2DoorInstance door : getDoors())
		{
			if (door != null)
			{
				if (open)
					door.openMe();
				else
					door.closeMe();
			}
		}
	}

	@Override
	public final boolean checkBanish(L2Player cha)
	{
		return cha.getClanId() != getOwnerId();
	}

	/** Load All Functions */
	private final void loadFunctions()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			final PreparedStatement statement = con.prepareStatement("SELECT * FROM clanhall_functions WHERE hall_id = ?");
			statement.setInt(1, getId());
			final ResultSet rs = statement.executeQuery();
			while (rs.next())
			{
				_functions.put(rs.getInt("type"),
						new ClanHallFunction(rs.getInt("type"), rs.getInt("lvl"), rs.getInt("lease"), 0, rs.getLong("rate"), rs.getLong("endTime"), true));
			}
			statement.close();
		}
		catch (SQLException e)
		{
			_log.fatal("Exception: ClanHall.loadFunctions(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/** Remove function In List and in DB */
	public final void removeFunction(int functionType)
	{
		_functions.remove(functionType);
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			final PreparedStatement statement = con.prepareStatement("DELETE FROM clanhall_functions WHERE hall_id=? AND type=?");
			statement.setInt(1, getId());
			statement.setInt(2, functionType);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.fatal("Exception: ClanHall.removeFunctions(int functionType): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public final boolean updateFunctions(L2Player player, int type, int lvl, int lease, long rate, boolean addNew)
	{
		if (player == null)
			return false;

		if (_log.isDebugEnabled())
			_log.warn("Called ClanHall.updateFunctions(int type, int lvl, int lease, long rate, boolean addNew) Owner : " + getOwnerId());

		if (lease > 0)
		{
			if (!player.destroyItemByItemId("Consume", PcInventory.ADENA_ID, lease, null, true))
				return false;
		}

		if (addNew)
		{
			_functions.put(type, new ClanHallFunction(type, lvl, lease, 0, rate, 0, false));
		}
		else
		{
			if (lvl == 0 && lease == 0)
				removeFunction(type);
			else
			{
				int diffLease = lease - _functions.get(type).getLease();
				if (_log.isDebugEnabled())
					_log.warn("Called ClanHall.updateFunctions diffLease : " + diffLease);
				if (diffLease > 0)
				{
					_functions.remove(type);
					_functions.put(type, new ClanHallFunction(type, lvl, lease, 0, rate, -1, false));
				}
				else
				{
					_functions.get(type).setLease(lease);
					_functions.get(type).setLvl(lvl);
					_functions.get(type).dbSave(false);
				}
			}
		}
		return true;
	}

	/** Update DB */
	public final void updateDb()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			final PreparedStatement statement = con.prepareStatement("UPDATE clanhall SET ownerId=?, paidUntil=?, paid=? WHERE id=?");
			statement.setInt(1, _ownerId);
			statement.setLong(2, _paidUntil);
			statement.setInt(3, (_paid) ? 1 : 0);
			statement.setInt(4, _clanHallId);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.error("Exception: updateOwnerInDB(L2Clan clan): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/** Initialyze Fee Task */
	private final void initializeTask(boolean forced)
	{
		final long currentTime = System.currentTimeMillis();
		if (_paidUntil > currentTime)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _paidUntil - currentTime);
		}
		else if (!_paid && !forced)
		{
			if (System.currentTimeMillis() + (1000 * 60 * 60 * 24) <= _paidUntil + _chRate)
				ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), 1000 * 60 * 60 * 24);
			else
				ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), (_paidUntil + _chRate) - System.currentTimeMillis());
		}
		else
			ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), 0);
	}

	/** Fee Task */
	private final class FeeTask implements Runnable
	{
		public FeeTask()
		{
		}

		@Override
		public final void run()
		{
			try
			{
				if (_isFree)
					return;
				L2Clan Clan = getOwnerClan();
				if (getOwnerClan().getWarehouse().getAdena() >= getLease())
				{
					if (_paidUntil != 0)
					{
						while (_paidUntil < System.currentTimeMillis())
							_paidUntil += _chRate;
					}
					else
						_paidUntil = System.currentTimeMillis() + _chRate;

					getOwnerClan().getWarehouse().destroyItemByItemId("CH_rental_fee", PcInventory.ADENA_ID, getLease(), null, null);
					if (_log.isDebugEnabled())
						_log.warn("deducted " + getLease() + " adena from " + getName() + " owner's cwh for ClanHall _paidUntil" + _paidUntil);
					ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _paidUntil - System.currentTimeMillis());
					_paid = true;
					updateDb();
				}
				else
				{
					_paid = false;
					if (System.currentTimeMillis() > _paidUntil + _chRate)
					{
						if (ClanHallManager.loaded())
						{
							AuctionService.getInstance().initNPC(getId());
							ClanHallManager.getInstance().setFree(getId());
							Clan.broadcastToOnlineMembers(SystemMessageId.CLAN_HALL_PAYMENT_OVERDUE.getSystemMessage());
						}
						else
							ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), 3000);
					}
					else
					{
						updateDb();
						// TODO: fix this...
						Clan.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.MAKE_CLAN_HALL_PAYMENT_BY_S1_TOMORROW).addNumber(12));
						if (System.currentTimeMillis() + (1000 * 60 * 60 * 24) <= _paidUntil + _chRate)
							ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), 1000 * 60 * 60 * 24);
						else
							ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), (_paidUntil + _chRate) - System.currentTimeMillis());
					}
				}
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	public final L2Clan getOwnerClan()
	{
		if (_ownerId == 0)
			return null;

		if (_ownerClan == null)
			_ownerClan = ClanTable.getInstance().getClan(getOwnerId());

		return _ownerClan;
	}

	private final boolean isSiegeable()
	{
		switch (getId())
		{
			case 34:
			case 64:
				return true;
			default:
				return false;
		}
	}

	@Override
	public final CCHSiege getSiege()
	{
		if (isSiegeable() && _siege == null)
			_siege = new CCHSiege(this);
		return _siege;
	}

	public final Calendar getSiegeDate()
	{
		return _siegeDate;
	}

	public final boolean getIsTimeRegistrationOver()
	{
		return _isTimeRegistrationOver;
	}

	public final void setIsTimeRegistrationOver(boolean val)
	{
		_isTimeRegistrationOver = val;
	}

	public final Calendar getTimeRegistrationOverDate()
	{
		if (_siegeTimeRegistrationEndDate == null)
			_siegeTimeRegistrationEndDate = Calendar.getInstance();
		return _siegeTimeRegistrationEndDate;
	}

	private final void loadSiegeData()
	{
		if (!isSiegeable())
			return;

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT siegeDate,regTimeEnd,regTimeOver FROM clanhall_sieges WHERE hallId=?");
			ps.setInt(1, getId());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				_siegeDate = Calendar.getInstance();
				_siegeDate.setTimeInMillis(rs.getLong("siegeDate"));
				_siegeTimeRegistrationEndDate = Calendar.getInstance();
				_siegeTimeRegistrationEndDate.setTimeInMillis(rs.getLong("regTimeEnd"));
				_isTimeRegistrationOver = rs.getBoolean("regTimeOver");
			}
			else
				_log.warn("Missing contest schedule data for " + getName());
		}
		catch (Exception e)
		{
			_log.error("Failed loading contest data!", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
}
