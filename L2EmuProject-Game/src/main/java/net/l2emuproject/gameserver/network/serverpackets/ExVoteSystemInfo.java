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

import net.l2emuproject.gameserver.services.recommendation.RecoBonus;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 **	@author Gnacik
 ** TODO: Rework it...
 */
public final class ExVoteSystemInfo extends L2GameServerPacket
{
	private final int	_recomLeft;
	private final int	_recomHave;
	private final int	_bonusTime;
	private final int	_bonusVal;
	private final int	_bonusType;

	public ExVoteSystemInfo(final L2Player player)
	{
		_recomLeft = player.getEvaluations();
		_recomHave = player.getEvalPoints();
		_bonusTime = player.getEvalBonusTime();
		_bonusVal = RecoBonus.getRecoBonus(player);
		_bonusType = player.getEvalBonusType();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0xC9);
		writeD(_recomLeft);
		writeD(_recomHave);
		writeD(_bonusTime);
		writeD(_bonusVal);
		writeD(_bonusType);
	}

	@Override
	public final String getType()
	{
		return "[S] FE:C9 ExVoteSystemInfo";
	}
}
