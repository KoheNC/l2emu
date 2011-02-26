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
package net.l2emuproject.gameserver.world.object;

import java.util.concurrent.ScheduledFuture;

import net.l2emuproject.gameserver.ThreadPoolManager;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.events.global.krateicube.KrateiCube;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.MagicSkillUse;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.instance.L2BlueWatcherInstance;
import net.l2emuproject.gameserver.world.object.instance.L2RedWatcherInstance;

/**
 * @author lord_rex
 */
public abstract class L2Watcher extends L2Attackable
{
	private ScheduledFuture<?>	_aiTask;

	public L2Watcher(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);

		if (_aiTask != null)
			_aiTask.cancel(true);

		_aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new WatcherAI(), 3000, 3000);
	}

	private final class WatcherAI implements Runnable
	{
		private WatcherAI()
		{
		}

		@Override
		public final void run()
		{
			handleWatcherAI();
		}
	}

	protected abstract void handleWatcherAI();

	protected final boolean handleCast(L2Watcher caster, L2Player player, int skillId)
	{
		final int skillLevel = 12;

		if (player.isDead() || !player.isVisible() || !isInsideRadius(player, getKnownList().getDistanceToWatchObject(player), false, false))
			return false;

		final L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);

		if (player.getFirstEffect(4104) == null && player.getFirstEffect(4034) == null && player.getFirstEffect(4035) == null)
		{
			skill.getEffects(caster, player);

			broadcastPacket(new MagicSkillUse(caster, player, skill.getId(), skillLevel, skill.getHitTime(), 0));

			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
			sm.addSkillName(skill);
			player.sendPacket(sm);
			return true;
		}
		return false;
	}

	@Override
	public final boolean isAutoAttackable(L2Character attacker)
	{
		if (attacker instanceof L2Watcher)
			return false;

		return true;
	}

	@Override
	public final void deleteMe()
	{
		if (_aiTask != null)
		{
			_aiTask.cancel(true);
			_aiTask = null;
		}

		super.deleteMe();
	}

	@Override
	public final boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;

		if (this instanceof L2RedWatcherInstance)
			KrateiCube.getInstance().startWatcherTask(KrateiCube.RED_WATCHER, KrateiCube.BLUE_WATCHER);
		else if (this instanceof L2BlueWatcherInstance)
			KrateiCube.getInstance().startWatcherTask(KrateiCube.BLUE_WATCHER, KrateiCube.RED_WATCHER);

		return true;
	}
}
