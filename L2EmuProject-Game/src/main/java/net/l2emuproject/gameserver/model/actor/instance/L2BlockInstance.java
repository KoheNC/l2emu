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
package net.l2emuproject.gameserver.model.actor.instance;

import net.l2emuproject.gameserver.datatables.ItemTable;
import net.l2emuproject.gameserver.instancemanager.games.HandysBlockCheckerManager.ArenaParticipantsHolder;
import net.l2emuproject.gameserver.model.L2ItemInstance;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.entity.BlockCheckerEngine;
import net.l2emuproject.gameserver.network.serverpackets.AbstractNpcInfo;
import net.l2emuproject.gameserver.network.serverpackets.ExCubeGameChangePoints;
import net.l2emuproject.gameserver.network.serverpackets.ExCubeGameExtendedChangePoints;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.tools.random.Rnd;

/**
 * @author BiggBoss
 */
public class L2BlockInstance extends L2MonsterInstance
{
	private int	_colorEffect;

	/**
	 * @param objectId
	 * @param template
	 */
	public L2BlockInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		
		setHideName(true);
	}

	/**
	 * Will change the color of the block and update
	 * the appearance in the known players clients
	 */
	public final void changeColor(L2PcInstance attacker, ArenaParticipantsHolder holder, int team)
	{
		// Do not update color while sending old info
		synchronized (this)
		{
			final BlockCheckerEngine event = holder.getEvent();
			if (_colorEffect == 0x53)
			{
				// Change color
				_colorEffect = 0x00;
				// BroadCast to all known players
				this.broadcastPacket(new AbstractNpcInfo.NpcInfo(this));
				increaseTeamPointsAndSend(attacker, team, event);
			}
			else
			{
				// Change color
				_colorEffect = 0x53;
				// BroadCast to all known players
				this.broadcastPacket(new AbstractNpcInfo.NpcInfo(this));
				increaseTeamPointsAndSend(attacker, team, event);
			}
			// 30% chance to drop the event items
			int random = Rnd.get(100);
			// Bond
			if (random > 69 && random <= 84)
				dropItem(13787, event, attacker);
			// Land Mine
			else if (random > 84)
				dropItem(13788, event, attacker);
		}
	}

	/**
	 * Sets if the block is red or blue. Mainly used in
	 * block spawn
	 * @param isRed
	 */
	public final void setRed(boolean isRed)
	{
		_colorEffect = isRed ? 0x53 : 0x00;
	}

	/**
	 * Return if the block is red at this momment
	 * @return
	 */
	@Override
	public final int getColorEffect()
	{
		return _colorEffect;
	}

	@Override
	public final boolean isAutoAttackable(L2Character attacker)
	{
		if (attacker instanceof L2PcInstance)
			return attacker.getActingPlayer() != null && attacker.getActingPlayer().getBlockCheckerArena() > -1;
		return true;
	}

	@Override
	public final boolean isAttackable()
	{
		return true;
	}

	@Override
	public final boolean doDie(L2Character killer)
	{
		return false;
	}

	private final void increaseTeamPointsAndSend(L2PcInstance player, int team, BlockCheckerEngine eng)
	{
		eng.increasePlayerPoints(player, team);

		int timeLeft = (int) ((eng.getStarterTime() - System.currentTimeMillis()) / 1000);
		boolean isRed = eng.getHolder().getRedPlayers().contains(player);

		ExCubeGameChangePoints changePoints = new ExCubeGameChangePoints(timeLeft, eng.getBluePoints(), eng.getRedPoints());
		ExCubeGameExtendedChangePoints secretPoints = new ExCubeGameExtendedChangePoints(timeLeft, eng.getBluePoints(), eng.getRedPoints(), isRed, player,
				eng.getPlayerPoints(player, isRed));

		eng.getHolder().broadCastPacketToTeam(changePoints);
		eng.getHolder().broadCastPacketToTeam(secretPoints);
	}

	private final void dropItem(int id, BlockCheckerEngine eng, L2PcInstance player)
	{
		L2ItemInstance drop = ItemTable.getInstance().createItem("Loot", id, 1, player, this);
		int x = getX() + Rnd.get(50);
		int y = getY() + Rnd.get(50);
		int z = getZ();

		drop.dropMe(this, x, y, z);

		eng.addNewDrop(drop);
	}
}
