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
package net.l2emuproject.gameserver.skills.effects;

import net.l2emuproject.gameserver.instancemanager.CastleManager;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.clan.L2Clan;
import net.l2emuproject.gameserver.model.entity.Castle;
import net.l2emuproject.gameserver.model.restriction.global.GlobalRestrictions;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.skills.AbnormalEffect;
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.skills.L2Effect;
import net.l2emuproject.gameserver.templates.effects.EffectTemplate;
import net.l2emuproject.gameserver.templates.skills.L2EffectType;
import net.l2emuproject.gameserver.world.zone.L2Zone;

/**
 * Moved here from skill handler for easier handling.
 * @author savormix
 */
public final class EffectClanGate extends L2Effect
{
	public EffectClanGate(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CLAN_GATE;
	}

	@Override
	protected boolean onStart()
	{
		L2Character effected = getEffected();
		L2PcInstance lord;
		if (effected instanceof L2PcInstance)
			lord = (L2PcInstance) effected;
		else
			return false;
		if (!GlobalRestrictions.canTeleport(lord) || lord.isInsideZone(L2Zone.FLAG_NOSUMMON))
			return false;
		L2Clan clan = lord.getClan();
		if (clan == null || !lord.isClanLeader())
			return false;
		Castle c = CastleManager.getInstance().getCastleByOwner(clan);
		if (c == null)
			return false;
		effected.setIsParalyzed(true);
		c.createClanGate(effected.getX(), effected.getY(), effected.getZ());
		clan.broadcastToOnlineMembers(SystemMessageId.COURT_MAGICIAN_CREATED_PORTAL.getSystemMessage());
		return true;
	}

	@Override
	protected void onExit()
	{
		L2PcInstance lord = (L2PcInstance) getEffected();
		Castle c = CastleManager.getInstance().getCastleByOwner(lord.getClan());
		if (c != null)
			c.destroyClanGate();
		lord.setIsParalyzed(false);
	}
	
	@Override
	protected int getTypeBasedAbnormalEffect()
	{
		return AbnormalEffect.MAGIC_CIRCLE.getMask();
	}
}
