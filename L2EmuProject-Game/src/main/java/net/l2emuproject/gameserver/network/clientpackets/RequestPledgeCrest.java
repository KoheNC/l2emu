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

import net.l2emuproject.gameserver.network.serverpackets.PledgeCrest;
import net.l2emuproject.gameserver.system.cache.CrestCache;

public class RequestPledgeCrest extends L2GameClientPacket
{
	private static final String _C__REQUESTPLEDGECREST = "[C] 67 RequestPledgeCrest c[d]";
	
	private int _crestId;
	
    @Override
    protected void readImpl()
	{
		_crestId = readD();
	}
    
    @Override
    protected void runImpl()
	{
		if (_crestId == 0)
		    return;
		
		if (_log.isDebugEnabled())
			_log.debug("Clan crest " + _crestId + " requested.");
        
        byte[] data = CrestCache.getInstance().getPledgeCrest(_crestId);
        
		if (data != null)
			sendPacket(new PledgeCrest(_crestId, data));
		else if (_log.isDebugEnabled())
			_log.debug("Clan crest is missing: " + _crestId);
	}
    
	@Override
	public String getType()
	{
		return _C__REQUESTPLEDGECREST;
	}
}
