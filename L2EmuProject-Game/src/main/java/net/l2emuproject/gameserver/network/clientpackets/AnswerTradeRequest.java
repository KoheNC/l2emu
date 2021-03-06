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
import net.l2emuproject.gameserver.Shutdown;
import net.l2emuproject.gameserver.Shutdown.DisableType;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.network.serverpackets.TradeDone;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * This class represents a packet sent by the client when a player clicks either "Yes"
 * or "No" in the trade request dialog.
 */
public class AnswerTradeRequest extends L2GameClientPacket
{
    private static final String _C__ANSWERTRADEREQUEST = "[C] 55 AnswerTradeRequest c[d]";
    
    private boolean				_accepted;
    
    @Override
    protected void readImpl()
    {
        _accepted = (readD() == 1);
    }
    
    @Override
    protected void runImpl()
    {
        L2Player player = getActiveChar(), partner = null;
        if (player == null)
        	return;
        
        try
        {
	        if (Shutdown.isActionDisabled(DisableType.TRANSACTION))
	        {
	        	sendPacket(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
	            return;
	        }
	        
	        partner = player.getActiveRequester();
	        if (partner == null || L2World.getInstance().getPlayer(partner.getObjectId()) == null)
	        {
	            // Trade partner not found, cancel trade
	            sendPacket(TradeDone.CANCELLED);
	            sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
	            return;
	        }
	        
	        //possible exploit fix
	        if (player.getActiveTradeList() != null)
	        {
	        	partner.sendPacket(new SystemMessage(SystemMessageId.C1_ALREADY_TRADING).addString(player.getName()));
	        	sendPacket(SystemMessageId.ALREADY_TRADING);
	        	return;
	        }
	        else if (partner.getActiveTradeList() != null)
	        {
	        	partner.sendPacket(SystemMessageId.ALREADY_TRADING);
	        	sendPacket(new SystemMessage(SystemMessageId.C1_ALREADY_TRADING).addString(partner.getName()));
	        	return;
	        }
	        
	        if (Config.GM_DISABLE_TRANSACTION && player.getAccessLevel() >= Config.GM_TRANSACTION_MIN && player.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
	        {
	        	partner.sendPacket(SystemMessageId.CANT_TRADE_WITH_TARGET);
	            sendPacket(SystemMessageId.ACCOUNT_CANT_TRADE_ITEMS);
	            return;
	        }
	        
	        if (_accepted && !partner.isRequestExpired())
				player.startTrade(partner);
			else
				partner.sendPacket(new SystemMessage(SystemMessageId.C1_DENIED_TRADE_REQUEST).addString(player.getName()));
        }
        finally
        {
        	clearRequestStatus(player, partner);
    		sendAF();
        }
	}
    
    private final void clearRequestStatus(L2Player player, L2Player partner)
    {
    	if (player != null)
    		player.setActiveRequester(null);
    	if (partner != null)
    		partner.onTransactionResponse();
    }
    
	@Override
	public String getType()
	{
		return _C__ANSWERTRADEREQUEST;
	}
}
