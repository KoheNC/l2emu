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

import net.l2emuproject.gameserver.ai.CtrlIntention;
import net.l2emuproject.gameserver.ai.L2CharacterAI;
import net.l2emuproject.gameserver.ai.L2FortSiegeGuardAI;
import net.l2emuproject.gameserver.ai.L2SiegeGuardAI;
import net.l2emuproject.gameserver.instancemanager.CastleManager;
import net.l2emuproject.gameserver.instancemanager.FortManager;
import net.l2emuproject.gameserver.instancemanager.TerritoryWarManager;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.L2Guard;
import net.l2emuproject.gameserver.model.actor.knownlist.CharKnownList;
import net.l2emuproject.gameserver.model.actor.knownlist.DefenderKnownList;
import net.l2emuproject.gameserver.model.entity.Castle;
import net.l2emuproject.gameserver.model.entity.Fort;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.StatusUpdate;
import net.l2emuproject.gameserver.network.serverpackets.ValidateLocation;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.L2Object;

public class L2DefenderInstance extends L2Guard
{
	private Castle	_castle;	// the castle which the instance should defend
	private Fort	_fort;		// the fortress which the instance should defend

	public L2DefenderInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public int getMyTargetSelectedColor(L2PcInstance player)
	{
		return player.getLevel() - getLevel();
	}

	@Override
	public DefenderKnownList getKnownList()
	{
		return (DefenderKnownList) _knownList;
	}

	@Override
	public CharKnownList initKnownList()
	{
		return new DefenderKnownList(this);
	}

	@Override
	protected L2CharacterAI initAI()
	{
		synchronized (this)
		{
			if (getCastle() == null)
				return new L2FortSiegeGuardAI(new AIAccessor());
			else
				return new L2SiegeGuardAI(new AIAccessor());
		}
	}

	/**
	 * Return True if a siege is in progress and the L2Character attacker isn't a Defender.<BR>
	 * <BR>
	 * 
	 * @param attacker The L2Character that the L2SiegeGuardInstance try to attack
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		// Summons and traps are attackable, too
		L2PcInstance player = L2Object.getActingPlayer(attacker);
		if (player == null)
			return false;

		if (shouldAttack(player))
			return true;

		return false;
	}

	private int getActiveSiegeId(L2PcInstance player)
	{
		if (player == null)
			return -1;

		// Check if siege is in progress
		if (_fort != null && _fort.getSiege().getIsInProgress())
			return _fort.getFortId();

		if (_castle != null && _castle.getSiege().getIsInProgress())
			return _castle.getCastleId();

		return -1;
	}

	public boolean shouldAttack(L2PcInstance player)
	{
		final int activeSiegeId = getActiveSiegeId(player);

		if (activeSiegeId == -1)
			return false;

		// Check if player is an enemy of this defender npc
		if (player != null
				&& ((player.getSiegeState() == L2PcInstance.SIEGE_STATE_DEFENDER && !player.isRegisteredOnThisSiegeField(activeSiegeId))
						|| (player.getSiegeState() == L2PcInstance.SIEGE_STATE_ATTACKER && !TerritoryWarManager.getInstance()
								.isAllyField(player, activeSiegeId)) || player.getSiegeState() == 0))
			return true;

		return false;
	}

	public boolean shouldDefend(L2PcInstance player)
	{
		final int activeSiegeId = getActiveSiegeId(player);

		if (activeSiegeId == -1)
			return false;

		// Check if player is an enemy of this defender npc
		if (player != null
				&& ((player.getSiegeState() == L2PcInstance.SIEGE_STATE_DEFENDER && player.isRegisteredOnThisSiegeField(activeSiegeId)) || (player
						.getSiegeState() == L2PcInstance.SIEGE_STATE_ATTACKER && TerritoryWarManager.getInstance().isAllyField(player, activeSiegeId))))
			return true;

		return false;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	protected int getMaxAllowedDistanceFromHome()
	{
		return 40;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();

		_fort = FortManager.getInstance().getFort(getX(), getY(), getZ());
		_castle = CastleManager.getInstance().getCastle(getX(), getY(), getZ());

		if (_fort == null && _castle == null)
			_log.warn("L2DefenderInstance spawned outside of Fortress or Castle Zone! NpcId: " + getNpcId() + " x=" + getX() + " y=" + getY() + " z=" + getZ());
	}

	/**
	* Custom onAction behaviour. Note that super() is not called because guards need
	* extra check to see if a player should interact or ATTACK them when clicked.
	* 
	*/
	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if (!canTarget(player))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			if (_log.isDebugEnabled())
				_log.info("New target selected:" + getObjectId());

			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);

			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else if (interact)
		{
			if (isAutoAttackable(player) && !isAlikeDead())
			{
				if (Math.abs(player.getZ() - getZ()) < 600) // this max heigth difference might need some tweaking
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				}
			}
			if (!isAutoAttackable(player))
			{
				if (!canInteract(player))
				{
					// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
			}
		}
		//Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if (attacker == null)
			return;

		if (!(attacker instanceof L2DefenderInstance))
		{
			if (damage == 0 && aggro <= 1)
				if (shouldDefend(attacker.getActingPlayer()))
					return;

			super.addDamageHate(attacker, damage, aggro);
		}
	}
}
