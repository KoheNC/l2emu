package net.l2emuproject.gameserver.network.serverpackets;

import net.l2emuproject.gameserver.items.ItemInfo;
import net.l2emuproject.gameserver.services.itemauction.ItemAuction;
import net.l2emuproject.gameserver.services.itemauction.ItemAuctionBid;
import net.l2emuproject.gameserver.services.itemauction.ItemAuctionState;

/**
 * @author Forsaiken
 * Format: (cdqd)(dddqhhhdhhdddhhhhhhhhhhh)(ddd)(dddqhhhdhhdddhhhhhhhhhhh)
 */
public final class ExItemAuctionInfoPacket extends L2GameServerPacket
{
	private final boolean		_refresh;
	private final int			_timeRemaining;
	private final ItemAuction	_currentAuction;
	private final ItemAuction	_nextAuction;

	public ExItemAuctionInfoPacket(final boolean refresh, final ItemAuction currentAuction, final ItemAuction nextAuction)
	{
		if (currentAuction == null)
			throw new NullPointerException();

		if (currentAuction.getAuctionState() != ItemAuctionState.STARTED)
			_timeRemaining = 0;
		else
			_timeRemaining = (int) (currentAuction.getFinishingTimeRemaining() / 1000); // in seconds

		_refresh = refresh;
		_currentAuction = currentAuction;
		_nextAuction = nextAuction;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x68);
		writeC(_refresh ? 0x00 : 0x01);
		writeD(_currentAuction.getInstanceId());

		final ItemAuctionBid highestBid = _currentAuction.getHighestBid();
		writeQ(highestBid != null ? highestBid.getLastBid() : _currentAuction.getAuctionInitBid());

		writeD(_timeRemaining);
		writeItemInfo(_currentAuction.getItemInfo());

		if (_nextAuction != null)
		{
			writeQ(_nextAuction.getAuctionInitBid());
			writeD((int) (_nextAuction.getStartingTime() / 1000)); // unix time in seconds
			writeItemInfo(_nextAuction.getItemInfo());
		}
	}

	private final void writeItemInfo(final ItemInfo item)
	{
		writeD(item.getItem().getItemId());
		writeD(item.getItem().getItemId());
		writeD(item.getLocation());
		writeQ(item.getCount());
		writeH(item.getItem().getType2());
		writeH(item.getCustomType1());
		writeH(0x00); //Equipped ? ON AUCTION?
		writeD(item.getItem().getBodyPart());
		writeH(item.getEnchant());
		writeH(item.getCustomType2());
		writeD(item.getAugemtationBonus());
		writeD(item.getMana());
		writeD(item.getTime());

		writeH(item.getAttackElementType());
		writeH(item.getAttackElementPower());
		for (byte i = 0; i < 6; i++)
			writeH(item.getElementDefAttr(i));

		writeEnchantEffectInfo();
	}

	@Override
	public final String getType()
	{
		return "[S] fe:68:00 ExItemAuctionInfoPacket";
	}
}
