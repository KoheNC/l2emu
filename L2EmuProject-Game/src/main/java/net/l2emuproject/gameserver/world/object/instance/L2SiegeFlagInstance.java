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
package net.l2emuproject.gameserver.world.object.instance;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.entity.status.CharStatus;
import net.l2emuproject.gameserver.entity.status.SiegeFlagStatus;
import net.l2emuproject.gameserver.events.global.clanhallsiege.CCHManager;
import net.l2emuproject.gameserver.events.global.clanhallsiege.CCHSiege;
import net.l2emuproject.gameserver.events.global.fortsiege.FortSiege;
import net.l2emuproject.gameserver.events.global.fortsiege.FortSiegeManager;
import net.l2emuproject.gameserver.events.global.siege.L2SiegeClan;
import net.l2emuproject.gameserver.events.global.siege.Siege;
import net.l2emuproject.gameserver.events.global.siege.SiegeManager;
import net.l2emuproject.gameserver.events.global.territorywar.TerritoryWarManager;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.StatusUpdate;
import net.l2emuproject.gameserver.services.clan.L2Clan;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class L2SiegeFlagInstance extends L2Npc
{
	private final L2Clan		_clan;
	private final L2Player	_player;
	private final Siege			_siege;
	private final FortSiege		_fortSiege;
	private final CCHSiege		_contSiege;
	private final boolean		_isAdvanced;
	private long				_talkProtectionTime;

	public L2SiegeFlagInstance(L2Player player, int objectId, L2NpcTemplate template, boolean advanced, boolean outPost)
	{
		super(objectId, template);

		_isAdvanced = advanced;
		_player = player;
		_clan = player == null ? null : player.getClan();
		_talkProtectionTime = 0;
		_siege = SiegeManager.getInstance().getSiege(_player);
		_fortSiege = FortSiegeManager.getInstance().getSiege(_player);
		_contSiege = CCHManager.getInstance().getSiege(_player);

		if (_clan == null)
		{
			deleteMe();
			return;
		}

		if (_siege == null && _fortSiege == null && _contSiege == null)
		{
			deleteMe();
			return;
		}

		L2SiegeClan sc = null;
		if (_siege != null && _fortSiege == null && _contSiege == null)
			sc = _siege.getAttackerClan(_player.getClan());
		else if (_siege == null && _fortSiege != null && _contSiege == null)
			sc = _fortSiege.getAttackerClan(_player.getClan());
		else if (_siege == null && _fortSiege == null && _contSiege != null)
			sc = _contSiege.getAttackerClan(_player.getClan());

		if (sc == null)
			deleteMe();
		else
			sc.addFlag(this);

		if (TerritoryWarManager.getInstance().isTWInProgress())
		{
			if (_clan == null)
				deleteMe();

			if (outPost)
				setIsInvul(true);
			else
				setIsInvul(false);

			getStatus();
			return;
		}

		setIsInvul(false);
	}

	@Override
	public boolean isAttackable()
	{
		return !isInvul();
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return !isInvul();
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;

		L2SiegeClan sc = null;
		if (_siege != null)
			sc = _siege.getAttackerClan(_player.getClan());
		else if (_fortSiege != null)
			sc = _fortSiege.getAttackerClan(_player.getClan());
		else if (_contSiege != null)
			sc = _contSiege.getAttackerClan(_player.getClan());

		if (sc != null)
			sc.removeFlag(this);
		
		else if (_clan != null)
			TerritoryWarManager.getInstance().removeClanFlag(_clan);

		return true;
	}

	@Override
	public void onForcedAttack(L2Player player)
	{
		onAction(player);
	}

	@Override
	public void onAction(L2Player player)
	{
		if (!_player.canBeTargetedByAtSiege(player) && Config.SIEGE_ONLY_REGISTERED)
			return;

		if (player == null || !canTarget(player))
			return;

		// Check if the L2Player already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2Player player
			player.setTarget(this);

			// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2Player to update its HP bar
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);
		}
		else
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

	public void flagAttacked()
	{
		// send warning to owners of headquarters that theirs base is under attack
		if (_clan != null && canTalk())
			_clan.broadcastToOnlineMembers(SystemMessageId.BASE_UNDER_ATTACK.getSystemMessage());

		_talkProtectionTime = System.currentTimeMillis() + 20000;
	}

	public boolean canTalk()
	{
		return System.currentTimeMillis() > _talkProtectionTime;
	}

	@Override
	protected CharStatus initStatus()
	{
		return new SiegeFlagStatus(this);
	}

	@Override
	public SiegeFlagStatus getStatus()
	{
		return (SiegeFlagStatus) _status;
	}

	public boolean isAdvanced()
	{
		return _isAdvanced;
	}
}
