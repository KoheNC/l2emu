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
package hellbound.Quarry;

import net.l2emuproject.gameserver.ThreadPoolManager;
import net.l2emuproject.gameserver.ai.CtrlIntention;
import net.l2emuproject.gameserver.instancemanager.hellbound.HellboundManager;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2MonsterInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.model.skill.L2Skill;
import net.l2emuproject.gameserver.model.world.L2Object;
import net.l2emuproject.gameserver.model.zone.L2Zone;
import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.network.serverpackets.CreatureSay;
import net.l2emuproject.tools.random.Rnd;

public final class Quarry extends QuestJython
{
	private static final int	SLAVE		= 32299;
	private static final int	TRUST		= 10;
	private static final int	ZONE		= 40106;
	private static final int[]	DROPLIST	=
											{ 1876, 1885, 9628 };
	private static final String	MSG			= "Thank you for saving me! Here is a small gift.";

	public Quarry(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addSpawnId(SLAVE);
		addFirstTalkId(SLAVE);
		addStartNpc(SLAVE);
		addTalkId(SLAVE);
		addAttackId(SLAVE);
		addSkillSeeId(SLAVE);
		addKillId(SLAVE);
		addEnterZoneId(ZONE);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("FollowMe"))
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player);
			npc.setTarget(player);
			npc.setAutoAttackable(true);
			npc.setWalking();

			return null;
		}
		return event;
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		npc.setAutoAttackable(false);

		return super.onSpawn(npc);
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (HellboundManager.getInstance().getHellboundLevel() != 5)
			return "32299.htm";
		else
		{
			if (player.getQuestState(getName()) == null)
				newQuestState(player);

			return "32299-01.htm";
		}
	}

	@Override
	public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (!npc.isDead())
			npc.doDie(attacker);

		return null;
	}

	@Override
	public final String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (skill.isOffensive() && !npc.isDead() && targets.length > 0)
		{
			for (L2Object obj : targets)
			{
				if (obj == npc)
				{
					npc.doDie(caster);
					return null;
				}
			}
		}
		return null;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		HellboundManager.getInstance().updateTrustPoints(-TRUST, true);
		npc.setAutoAttackable(false);

		return super.onKill(npc, killer, isPet);
	}

	@Override
	public final String onEnterZone(L2Character character, L2Zone zone)
	{
		if (character instanceof L2Npc && ((L2Npc) character).getNpcId() == SLAVE)
		{
			if (!character.isDead() && !((L2Npc) character).isDecayed() && character.getAI().getIntention() == CtrlIntention.AI_INTENTION_FOLLOW)
			{
				if (HellboundManager.getInstance().getHellboundLevel() == 5)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new Decay((L2Npc) character), 1000);
					try
					{
						character.broadcastPacket(new CreatureSay(character.getObjectId(), SystemChatChannelId.Chat_Normal, character.getName(), MSG));
					}
					catch (Exception e)
					{
						_log.warn("", e);
					}
				}
			}
		}
		return null;
	}

	private final class Decay implements Runnable
	{
		private final L2Npc	_npc;

		public Decay(L2Npc npc)
		{
			_npc = npc;
		}

		@Override
		public final void run()
		{
			if (_npc != null && !_npc.isDead())
			{
				if (_npc.getTarget() instanceof L2PcInstance)
					((L2MonsterInstance) _npc).dropItem((L2PcInstance) (_npc.getTarget()), DROPLIST[Rnd.get(DROPLIST.length)], 1);

				_npc.setAutoAttackable(false);
				_npc.deleteMe();
				_npc.getSpawn().decreaseCount(_npc);
				HellboundManager.getInstance().updateTrustPoints(TRUST, true);
			}
		}
	}

	public static void main(String[] args)
	{
		new Quarry(-1, Quarry.class.getSimpleName(), "hellbound");
	}
}