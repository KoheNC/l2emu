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
package net.l2emuproject.gameserver.model.entity.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.l2emuproject.L2DatabaseFactory;
import net.l2emuproject.gameserver.datatables.HennaTable;
import net.l2emuproject.gameserver.model.L2ItemInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.HennaInfo;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.network.serverpackets.UserInfo;
import net.l2emuproject.gameserver.templates.item.L2Henna;

public class PlayerHenna extends PlayerExtension
{
	private static final String	RESTORE_CHAR_HENNAS	= "SELECT slot,symbol_id FROM character_hennas WHERE charId=? AND class_index=?";
	private static final String	ADD_CHAR_HENNA		= "INSERT INTO character_hennas (charId,symbol_id,slot,class_index) VALUES (?,?,?,?)";
	private static final String	DELETE_CHAR_HENNA	= "DELETE FROM character_hennas WHERE charId=? AND slot=? AND class_index=?";
	private static final String	DELETE_CHAR_HENNAS	= "DELETE FROM character_hennas WHERE charId=? AND class_index=?";

	private final L2Henna[]		_henna				= new L2Henna[3];
	private int					_hennaSTR;
	private int					_hennaINT;
	private int					_hennaDEX;
	private int					_hennaMEN;
	private int					_hennaWIT;
	private int					_hennaCON;

	public PlayerHenna(L2PcInstance activeChar)
	{
		super(activeChar);
	}

	/**
	 * Retrieve from the database all Henna of this L2PcInstance, add them to _henna and calculate stats of the L2PcInstance.<BR><BR>
	 */
	public void restoreHenna()
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_HENNAS);
			statement.setInt(1, getPlayer().getObjectId());
			statement.setInt(2, getPlayer().getClassIndex());
			ResultSet rset = statement.executeQuery();

			for (int i = 0; i < 3; i++)
				_henna[i] = null;

			while (rset.next())
			{
				int slot = rset.getInt("slot");

				if (slot < 1 || slot > 3)
					continue;

				_henna[slot - 1] = HennaTable.getInstance().getTemplate(rset.getInt("symbol_id"));
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Failed restoing character hennas.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		// Calculate Henna modifiers of this L2PcInstance
		recalcHennaStats();
	}

	/**
	 * Return the number of Henna empty slot of the L2PcInstance.<BR><BR>
	 */
	public int getHennaEmptySlots()
	{
		int totalSlots;
		if (getPlayer().getClassId().level() == 1)
			totalSlots = 2;
		else
			totalSlots = 3;

		for (int i = 0; i < 3; i++)
			if (_henna[i] != null)
				totalSlots--;

		if (totalSlots <= 0)
			return 0;

		return totalSlots;
	}

	/**
	 * Remove a Henna of the L2PcInstance, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2PcInstance.<BR><BR>
	 */
	public boolean removeHenna(int slot)
	{
		if (slot < 1 || slot > 3)
			return false;

		slot--;

		if (_henna[slot] == null)
			return false;

		L2Henna henna = _henna[slot];
		_henna[slot] = null;

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(DELETE_CHAR_HENNA);
			statement.setInt(1, getPlayer().getObjectId());
			statement.setInt(2, slot + 1);
			statement.setInt(3, getPlayer().getClassIndex());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Failed removing character henna.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		// Calculate Henna modifiers of this L2PcInstance
		recalcHennaStats();

		// Send Server->Client HennaInfo packet to this L2PcInstance
		getPlayer().sendPacket(new HennaInfo(getPlayer()));

		// Send Server->Client UserInfo packet to this L2PcInstance
		getPlayer().sendPacket(new UserInfo(getPlayer()));

		// Add the recovered dyes to the player's inventory and notify them.
		L2ItemInstance dye = getPlayer().getInventory().addItem("Henna", henna.getItemId(), henna.getAmount() / 2, getPlayer(), null);
		getPlayer().getInventory().updateInventory(dye);

		getPlayer().reduceAdena("Henna", henna.getPrice() / 5, getPlayer(), false);

		SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
		sm.addItemName(henna.getItemId());
		sm.addItemNumber(henna.getAmount() / 2);
		getPlayer().sendPacket(sm);

		getPlayer().sendPacket(SystemMessageId.SYMBOL_DELETED);

		return true;
	}

	/**
	 * Add a Henna to the L2PcInstance, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2PcInstance.<BR><BR>
	 * <B>Does not do <U>any</U> validation!</B>
	 */
	public void addHenna(L2Henna henna)
	{
		for (int i = 0; i < 3; i++)
		{
			if (_henna[i] == null)
			{
				_henna[i] = henna;

				// Calculate Henna modifiers of this L2PcInstance
				recalcHennaStats();

				Connection con = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection(con);
					PreparedStatement statement = con.prepareStatement(ADD_CHAR_HENNA);
					statement.setInt(1, getPlayer().getObjectId());
					statement.setInt(2, henna.getSymbolId());
					statement.setInt(3, i + 1);
					statement.setInt(4, getPlayer().getClassIndex());
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					_log.error("Failed saving character henna.", e);
				}
				finally
				{
					L2DatabaseFactory.close(con);
				}

				// Send Server->Client HennaInfo packet to this L2PcInstance
				getPlayer().sendPacket(new HennaInfo(getPlayer()));
				// Send Server->Client UserInfo packet to this L2PcInstance
				getPlayer().sendPacket(new UserInfo(getPlayer()));
				return;
			}
		}
	}

	public void removeAllHenna(int classIndex, int newClassId)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			// Remove all henna info stored for this sub-class.
			PreparedStatement statement = con.prepareStatement(DELETE_CHAR_HENNAS);
			statement.setInt(1, getPlayer().getObjectId());
			statement.setInt(2, classIndex);
			statement.execute();
			statement.close();

		}
		catch (Exception e)
		{

		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Calculate Henna modifiers of this L2PcInstance.<BR><BR>
	 */
	private void recalcHennaStats()
	{
		_hennaINT = 0;
		_hennaSTR = 0;
		_hennaCON = 0;
		_hennaMEN = 0;
		_hennaWIT = 0;
		_hennaDEX = 0;

		for (int i = 0; i < 3; i++)
		{
			if (_henna[i] == null)
				continue;
			_hennaINT += _henna[i].getStatINT();
			_hennaSTR += _henna[i].getStatSTR();
			_hennaMEN += _henna[i].getStatMEM();
			_hennaCON += _henna[i].getStatCON();
			_hennaWIT += _henna[i].getStatWIT();
			_hennaDEX += _henna[i].getStatDEX();
		}

		if (_hennaINT > 5)
			_hennaINT = 5;
		if (_hennaSTR > 5)
			_hennaSTR = 5;
		if (_hennaMEN > 5)
			_hennaMEN = 5;
		if (_hennaCON > 5)
			_hennaCON = 5;
		if (_hennaWIT > 5)
			_hennaWIT = 5;
		if (_hennaDEX > 5)
			_hennaDEX = 5;
	}

	/**
	 * Return the Henna of this L2PcInstance corresponding to the selected slot.<BR><BR>
	 */
	public L2Henna getHenna(int slot)
	{
		if (slot < 1 || slot > 3)
			return null;

		return _henna[slot - 1];
	}

	public void setHennasEmptyOnSubclassChange()
	{
		for (int i = 0; i < 3; i++)
			_henna[i] = null;
	}

	/**
	 * Return the INT Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatINT()
	{
		return _hennaINT;
	}

	/**
	 * Return the STR Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatSTR()
	{
		return _hennaSTR;
	}

	/**
	 * Return the CON Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatCON()
	{
		return _hennaCON;
	}

	/**
	 * Return the MEN Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatMEN()
	{
		return _hennaMEN;
	}

	/**
	 * Return the WIT Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatWIT()
	{
		return _hennaWIT;
	}

	/**
	 * Return the DEX Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatDEX()
	{
		return _hennaDEX;
	}
}
