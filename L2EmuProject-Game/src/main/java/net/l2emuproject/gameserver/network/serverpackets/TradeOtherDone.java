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
package net.l2emuproject.gameserver.network.serverpackets;

public class TradeOtherDone extends StaticPacket
{
	private static final String _S__82_SENDTRADEOTHERDONE = "[S] 82 SendTradeOtherDone";
	
	public static final TradeOtherDone STATIC_PACKET = new TradeOtherDone();
	
	private TradeOtherDone()
	{
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x82);
	}
	
	@Override
	public String getType()
	{
		return _S__82_SENDTRADEOTHERDONE;
	}
}
