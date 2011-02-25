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
import net.l2emuproject.gameserver.network.serverpackets.TradeOtherAdd;
import net.l2emuproject.gameserver.network.serverpackets.TradeOwnAdd;
import net.l2emuproject.gameserver.services.transactions.TradeList;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * This class represents a packet that is sent by the client when you are adding an item
 * to the trade window.
 */
public class AddTradeItem extends L2GameClientPacket
{
    private static final String _C__ADDTRADEITEM = "[C] 1B AddTradeItem c[ddq]";

    private int					_tradeId;
    private int					_objectId;
    private long				_count;

    @Override
    protected void readImpl()
    {
        _tradeId = readD();
        _objectId = readD();
        _count = readQ();
    }

    @Override
    protected void runImpl()
    {
        L2Player player = getActiveChar();
        if (player == null)
        	return;

        if (Shutdown.isActionDisabled(DisableType.TRANSACTION))
        {
        	requestFailed(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
        	player.cancelActiveTrade();
            return;
        }

        TradeList trade = player.getActiveTradeList();
        if (trade == null)
        {
            _log.warn("Character: " + player.getName() + " requested item:" + _objectId + " add without active tradelist:" + _tradeId);
            requestFailed(SystemMessageId.TRADE_ATTEMPT_FAILED);
            return;
        }

        L2Player partner = trade.getPartner();
        if (partner == null
            || L2World.getInstance().getPlayer(partner.getObjectId()) == null
            || partner.getActiveTradeList() == null)
        {
            // Trade partner not found, cancel trade
            if (trade.getPartner() != null)
                _log.warn("Character:" + player.getName() + " requested invalid trade object: " + _objectId);
            requestFailed(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
        	player.cancelActiveTrade();
            return;
        }

        if (Config.GM_DISABLE_TRANSACTION && player.getAccessLevel() >= Config.GM_TRANSACTION_MIN
            && player.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
        {
        	trade.getPartner().sendPacket(SystemMessageId.CANT_TRADE_WITH_TARGET);
        	requestFailed(SystemMessageId.ACCOUNT_CANT_TRADE_ITEMS);
        	player.cancelActiveTrade();
            return;
        }

        if (trade.isConfirmed() || trade.getPartner().getActiveTradeList().isConfirmed())
        {
        	requestFailed(SystemMessageId.CANNOT_ADJUST_ITEMS_AFTER_TRADE_CONFIRMED);
        	return;
        }

        if (!player.validateItemManipulation(_objectId, "trade") && !player.isGM())
        {
        	requestFailed(SystemMessageId.TRADE_ATTEMPT_FAILED);
            return;
        }

        final TradeList.TradeItem item = trade.addItem(_objectId, _count);
        if (item != null)
        {
            sendPacket(new TradeOwnAdd(item));
            trade.getPartner().sendPacket(new TradeOtherAdd(item));
        }

        sendAF();
    }

    @Override
    public String getType()
    {
        return _C__ADDTRADEITEM;
    }
}
