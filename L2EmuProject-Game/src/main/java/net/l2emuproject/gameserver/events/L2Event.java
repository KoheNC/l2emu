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
package net.l2emuproject.gameserver.events;

import java.util.List;

import javolution.util.FastList;
import net.l2emuproject.gameserver.datatables.NpcTable;
import net.l2emuproject.gameserver.datatables.SpawnTable;
import net.l2emuproject.gameserver.network.serverpackets.ExShowScreenMessage;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.spawn.L2Spawn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author lord_rex, Intrepid
 */
public abstract class L2Event
{
	protected static final Log	_log					= LogFactory.getLog(L2Event.class);

	protected static final byte	STATUS_NOT_IN_PROGRESS	= 0;
	protected static final byte	STATUS_REGISTRATION		= 1;
	protected static final byte	STATUS_PREPARATION		= 2;
	protected static final byte	STATUS_COMBAT			= 3;
	protected static final byte	STATUS_REWARDS			= 4;

	private final List<L2Npc>	_npcs					= new FastList<L2Npc>();

	/**
	 * Easy way to spawn an NPC for an event.
	 * 
	 * @param npcId
	 * @param coordX
	 * @param coordY
	 * @param coordZ
	 */
	protected final void spawnNpc(final int npcId, final int coordX, final int coordY, final int coordZ, final int instanceId)
	{
		final L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);

		try
		{
			final L2Spawn spawnData = new L2Spawn(template);

			spawnData.setLocx(coordX);
			spawnData.setLocy(coordY);
			spawnData.setLocz(coordZ);
			spawnData.setAmount(9); // to be able to spawn an npc in every town/village if needed
			spawnData.setHeading(0);
			spawnData.setRespawnDelay(1);
			spawnData.setInstanceId(instanceId);
			SpawnTable.getInstance().addNewSpawn(spawnData, false);
			_npcs.add(spawnData.doSpawn());
		}
		catch (Exception e)
		{
			_log.warn("Couldn't spawn the event npc with the Id of: " + npcId, e);
		}
	}

	protected final void deleteNpc(final int npcId)
	{
		for (L2Npc npc : _npcs)
		{
			if (npc.getNpcId() == npcId)
			{
				npc.getSpawn().stopRespawn();
				npc.deleteMe();
				_npcs.remove(npc);
			}
		}
	}

	public abstract void startRegistration();

	public abstract void registrationAnnounce();

	public abstract void endRegistration();

	public abstract void startEvent();

	public abstract void endEvent();

	protected abstract void giveRewards(final L2Player player);

	protected abstract boolean canJoin(final L2Player player);

	public abstract void registerPlayer(final L2Player player);

	public abstract void cancelRegistration(final L2Player player);

	public abstract void removeDisconnected(final L2Player player);

	protected abstract void initInstance();

	protected final void sendMessage(final L2Player player, final String text, final int time)
	{
		player.sendPacket(new ExShowScreenMessage(text, time));
		player.sendMessage(text);
	}

	protected final void teleportPlayer(final L2Player player, final int coords[], final int instanceId)
	{
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], true);
	}

	protected final void setTeamColor(final L2Player player, final int color)
	{
		player.getAppearance().setNameColor(color);
		player.getAppearance().setTitleColor(color);
		player.broadcastUserInfo();
	}

	protected final class ReviveTask implements Runnable
	{
		private final L2Player	_player;
		private final int[]		_coords;
		private final int		_instanceId;

		public ReviveTask(final L2Player player, final int[] coords, final int instanceId)
		{
			_player = player;
			_coords = coords;
			_instanceId = instanceId;
		}

		@Override
		public final void run()
		{
			_player.doRevive();
			_player.getStatus().setCurrentHpMp(_player.getMaxHp(), _player.getMaxMp());
			_player.getStatus().setCurrentCp(_player.getMaxCp());
			_player.broadcastStatusUpdate();
			_player.broadcastFullInfo();

			teleportPlayer(_player, _coords, _instanceId);
		}
	}
}
