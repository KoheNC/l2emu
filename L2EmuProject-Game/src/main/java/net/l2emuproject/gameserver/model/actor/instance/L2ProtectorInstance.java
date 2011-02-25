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

import java.util.concurrent.ScheduledFuture;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.ThreadPoolManager;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.network.serverpackets.MagicSkillUse;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.knownlist.CharKnownList;
import net.l2emuproject.gameserver.world.knownlist.NpcKnownList;
import net.l2emuproject.gameserver.world.knownlist.ProtectorKnownList;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Ederik
 */
public class L2ProtectorInstance extends L2NpcInstance
{
	private ScheduledFuture<?>	_aiTask;

	private class ProtectorAI implements Runnable
	{
		private L2ProtectorInstance	_caster;

		protected ProtectorAI(L2ProtectorInstance caster)
		{
			_caster = caster;
		}

		@Override
		public void run()
		{
			/**
			 * For each known player in range, cast sleep if pvpFlag != 0 or Karma >0
			 * Skill use is just for buff animation
			 */
			for (L2Player player : getKnownList().getKnownPlayers().values())

			{
				if ((player.getKarma() > 0 && Config.PROTECTOR_PLAYER_PK) || (player.getPvpFlag() != 0 && Config.PROTECTOR_PLAYER_PVP))
				{
					handleCast(player, Config.PROTECTOR_SKILLID, Config.PROTECTOR_SKILLLEVEL);
				}
			}
		}

		private boolean handleCast(L2Player player, int skillId, int skillLevel)
		{
			if (player.isGM() || player.isDead() || !player.isVisible()
					|| !isInsideRadius(player, getKnownList().getDistanceToWatchObject(player), false, false))
				return false;

			L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);

			if (player.getFirstEffect(skill) == null)
			{
				skill.getEffects(_caster, player);
				broadcastPacket(new MagicSkillUse(_caster, player, skillId, skillLevel, Config.PROTECTOR_SKILLTIME, 0));
				player.sendCreatureMessage(SystemChatChannelId.Chat_Normal, getName(), Config.PROTECTOR_MESSAGE);

				return true;
			}

			return false;
		}
	}

	public L2ProtectorInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList();

		if (_aiTask != null)
			_aiTask.cancel(true);

		_aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new ProtectorAI(this), 3000, 3000);
	}

	@Override
	protected CharKnownList initKnownList()
	{
		return new ProtectorKnownList(this);
	}

	@Override
	public NpcKnownList getKnownList()
	{
		return (ProtectorKnownList) _knownList;
	}

	@Override
	public void deleteMe()
	{
		if (_aiTask != null)
		{
			_aiTask.cancel(true);
			_aiTask = null;
		}

		super.deleteMe();
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}
}
