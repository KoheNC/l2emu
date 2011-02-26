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

import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.instancemanager.TerritoryWarManager;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.StatusUpdate;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.network.serverpackets.ValidateLocation;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Attackable;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class L2TerritoryWardInstance extends L2Attackable
{
	public L2TerritoryWardInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);

		disableCoreAI(true);
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (isInvul())
			return false;
		if (getCastle() == null || !getCastle().getSiege().getIsInProgress())
			return false;

		final L2Player actingPlayer = attacker.getActingPlayer();
		if (actingPlayer == null)
			return false;
		if (actingPlayer.getSiegeSide() == 0)
			return false;
		if (TerritoryWarManager.getInstance().isAllyField(actingPlayer, getCastle().getCastleId()))
			return false;

		return true;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();

		if (_log.isDebugEnabled() && getCastle() == null)
			_log.warn("L2TerritoryWardInstance(" + getName() + ") spawned outside Castle Zone!");
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		// Kill the L2NpcInstance (the corpse disappeared after 7 seconds)
		if (!super.doDie(killer) || getCastle() == null || !TerritoryWarManager.getInstance().isTWInProgress())
			return false;

		if (killer instanceof L2Player)
		{
			if (((L2Player) killer).getSiegeSide() > 0 && !((L2Player) killer).isCombatFlagEquipped())
				((L2Player) killer).addItem("Pickup", getNpcId() - 23012, 1, null, false);
			else
				TerritoryWarManager.getInstance().getTerritoryWard(getNpcId() - 36491).spawnMe();
			SystemMessage sm = new SystemMessage(SystemMessageId.THE_S1_WARD_HAS_BEEN_DESTROYED);
			sm.addString(this.getName().replaceAll(" Ward", ""));
			sm.addPcName((L2Player) killer);
			TerritoryWarManager.getInstance().announceToParticipants(sm, 0, 0);
		}
		else
			TerritoryWarManager.getInstance().getTerritoryWard(getNpcId() - 36491).spawnMe();
		decayMe();
		return true;
	}

	@Override
	public void onForcedAttack(L2Player player)
	{
		onAction(player);
	}

	@Override
	public void onAction(L2Player player, boolean interact)
	{
		if (player == null || !canTarget(player))
			return;

		player.setLastFolkNPC(this);

		// Check if the L2Player already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2Player player
			player.setTarget(this);

			// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2Player to update its HP bar
			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);

			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else if (interact)
		{
			if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100)
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			else
			{
				// Send a Server->Client ActionFailed to the L2Player in order to avoid that the client wait another packet
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
}
