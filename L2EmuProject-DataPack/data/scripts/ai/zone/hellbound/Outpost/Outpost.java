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
package ai.zone.hellbound.Outpost;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;
import net.l2emuproject.gameserver.ThreadPoolManager;
import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.manager.hellbound.HellboundEngine;
import net.l2emuproject.gameserver.manager.hellbound.HellboundManager;
import net.l2emuproject.gameserver.services.quest.Quest;
import net.l2emuproject.gameserver.world.object.L2Attackable;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author DS, based on theOne's work
 */
public final class Outpost extends Quest
{
	private static final int	CAPTAIN		= 18466;
	private static final int	DEFENDER	= 22358;

	private static final int	TIMEOUT		= 60000;

	private volatile boolean	_isAttacked	= false;
	private long				_lastAttack	= 0;
	private L2Npc				_boss		= null;
	private List<L2Npc>			_defenders	= new FastList<L2Npc>();
	private ScheduledFuture<?>	_checkTask	= null;

	public Outpost(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addFirstTalkId(CAPTAIN);
		addAttackId(CAPTAIN);
		addAttackId(DEFENDER);
		addKillId(CAPTAIN);
		addSpawnId(CAPTAIN);
		addSpawnId(DEFENDER);
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2Player player)
	{
		if (!_isAttacked)
		{
			_isAttacked = true;
			ThreadPoolManager.getInstance().scheduleGeneral(new Attack(player), 5000);
			if (_checkTask == null)
				_checkTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Check(), TIMEOUT, TIMEOUT);

			return "18466.htm";
		}

		return null;
	}

	@Override
	public final String onAttack(L2Npc npc, L2Player attacker, int damage, boolean isPet)
	{
		_lastAttack = System.currentTimeMillis();

		if (!_isAttacked)
		{
			_isAttacked = true;
			ThreadPoolManager.getInstance().scheduleGeneral(new Attack(attacker), 100);
			if (_checkTask == null)
				_checkTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Check(), TIMEOUT, TIMEOUT);
		}

		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public final String onKill(L2Npc npc, L2Player killer, boolean isPet)
	{
		if (_checkTask != null)
		{
			_checkTask.cancel(false);
			_checkTask = null;
		}

		if (npc.getNpcId() == CAPTAIN)
			HellboundManager.getInstance().setHellboundLevel(HellboundEngine.LEVEL_9);

		return super.onKill(npc, killer, isPet);
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		npc.setIsNoRndWalk(true);

		if (npc.getNpcId() == CAPTAIN)
		{
			_boss = npc;
			npc.setAutoAttackable(false);
		}
		else if (!_isAttacked)
		{
			if (!_defenders.contains(npc))
				_defenders.add(npc);

			ThreadPoolManager.getInstance().scheduleGeneral(new Delete(npc), 100);
		}

		return super.onSpawn(npc);
	}

	private final class Attack implements Runnable
	{
		private final L2Player	_player;

		public Attack(L2Player player)
		{
			_player = player;
		}

		@Override
		public final void run()
		{
			for (L2Npc npc : _defenders)
			{
				try
				{
					npc.getSpawn().startRespawn();
					if (npc.isDecayed())
						npc.setDecayed(false);
					if (npc.isDead())
						npc.doRevive();

					npc.spawnMe(npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz());

					npc.setIsRunning(true);
					((L2Attackable) npc).addDamageHate(_player, 0, 1);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _player);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			try
			{
				_boss.setAutoAttackable(true);
				_boss.setIsRunning(true);
				((L2Attackable) _boss).addDamageHate(_player, 0, 1);
				_boss.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _player);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private final class Delete implements Runnable
	{
		private final L2Npc	_npc;

		public Delete(L2Npc npc)
		{
			_npc = npc;
		}

		@Override
		public final void run()
		{
			_npc.getSpawn().stopRespawn();
			_npc.deleteMe();
		}
	}

	private final class Check implements Runnable
	{
		@Override
		public final void run()
		{
			if (System.currentTimeMillis() - _lastAttack > TIMEOUT)
			{
				_isAttacked = false;
				for (L2Npc npc : _defenders)
				{
					try
					{
						if (npc.isVisible())
						{
							npc.getSpawn().stopRespawn();
							((L2Attackable) npc).clearAggroList();
							npc.onDecay();
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}

				try
				{
					_boss.setAutoAttackable(false);
					((L2Attackable) _boss).clearAggroList();
					_boss.onDecay();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				_checkTask.cancel(false);
				_checkTask = null;
			}
		}
	}

	public static void main(String[] args)
	{
		new Outpost(-1, Outpost.class.getSimpleName(), "ai/zones/hellbound");
	}
}