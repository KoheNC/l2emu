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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.l2emuproject.L2DatabaseFactory;
import net.l2emuproject.gameserver.cache.CrestCache;
import net.l2emuproject.gameserver.datatables.ClanTable;
import net.l2emuproject.gameserver.idfactory.IdFactory;
import net.l2emuproject.gameserver.model.clan.L2Clan;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.world.object.L2Player;


/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestSetAllyCrest extends L2GameClientPacket
{
	private static final String	_C__87_REQUESTSETALLYCREST	= "[C] 87 RequestSetAllyCrest";

	private int					_length;

	private byte[]				_data;

	@Override
	protected void readImpl()
	{
		_length = readD();
		if (_length < 0 || _length > 192)
			return;

		_data = new byte[_length];
		readB(_data);
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		L2Clan clan = ClanTable.getInstance().getClan(activeChar.getAllyId());

		if (clan == null || !activeChar.isClanLeader() || activeChar.getClanId() != clan.getClanId())
		{
			requestFailed(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
			return;
		}

		if (_length < 0 || _length > 192)
		{
			requestFailed(SystemMessageId.INVALID_INSIGNIA_FORMAT);
			return;
		}

		CrestCache crestCache = CrestCache.getInstance();

		int newId = IdFactory.getInstance().getNextId();

		if (!crestCache.saveAllyCrest(newId, _data))
		{
			//all lies, the problem is server-side :D
			requestFailed(SystemMessageId.INVALID_INSIGNIA_COLOR);
			_log.info("Error saving alliance crest: " + clan.getAllyName());
			return;
		}

		if (clan.getAllyCrestId() != 0)
			crestCache.removeAllyCrest(clan.getAllyCrestId());

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET ally_crest_id = ? WHERE ally_id = ?");
			statement.setInt(1, newId);
			statement.setInt(2, clan.getAllyId());
			statement.executeUpdate();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warn("could not update the ally crest id:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		for (L2Clan c : ClanTable.getInstance().getClans())
		{
			if (c.getAllyId() == activeChar.getAllyId())
			{
				c.setAllyCrestId(newId);
				for (L2Player member : c.getOnlineMembers(0))
					member.broadcastUserInfo();
			}
		}

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__87_REQUESTSETALLYCREST;
	}
}
