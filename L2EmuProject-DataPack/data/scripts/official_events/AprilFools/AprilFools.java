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
package official_events.AprilFools;

import java.util.Calendar;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.NpcTable;
import net.l2emuproject.gameserver.model.actor.instance.L2MonsterInstance;
import net.l2emuproject.gameserver.model.quest.Quest;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.network.serverpackets.ExBrBroadcastEventState;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Attackable;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.tools.random.Rnd;

/**
 * @author Gigiikun
 */
public class AprilFools extends QuestJython
{
	private static final String	QN		= "AprilFools";
	private boolean				_isFool	= false;

	public AprilFools(int questId, String name, String descr)
	{
		super(questId, name, descr);

		Calendar cal = Calendar.getInstance();
		if (cal.get(Calendar.DAY_OF_MONTH) <= 3 && cal.get(Calendar.MONTH) == Calendar.APRIL)
		{
			_isFool = true;
			setOnEnterWorld(true);
			registerDrops();
		}
		else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
		{
			registerDrops();
		}
		else
			_log.info("April Fools Event not started!");
	}

	private final void registerDrops()
	{
		for (int level = 1; level < 100; level++)
		{
			L2NpcTemplate[] templates = NpcTable.getInstance().getAllOfLevel(level);
			if ((templates != null) && (templates.length > 0))
			{
				for (L2NpcTemplate t : templates)
				{
					try
					{
						if (t.isAssignableTo(L2Attackable.class))
							addEventId(t.getNpcId(), Quest.QuestEventType.ON_KILL);
					}
					catch (RuntimeException e)
					{
						_log.warn("", e);
					}
				}
			}
		}
	}

	@Override
	public String onEnterWorld(L2Player player)
	{
		player.sendPacket(new ExBrBroadcastEventState(Config.APRIL_FOOLS_DATE, 1));
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2Player killer, boolean isPet)
	{
		int r = Rnd.get(100);
		if (r <= Config.APRIL_FOOLS_DROP_CHANCE && npc instanceof L2MonsterInstance)
		{
			r = Rnd.get(100);
			if (_isFool)
			{
				if (r <= 16)
					((L2MonsterInstance) npc).dropItem(killer, 20272, 1);
				else if (r < 33)
					((L2MonsterInstance) npc).dropItem(killer, 20273, 1);
				else if (r < 50)
					((L2MonsterInstance) npc).dropItem(killer, 20274, 1);
				else if (r < 67)
					((L2MonsterInstance) npc).dropItem(killer, 20923, 1);
				else if (r < 83)
					((L2MonsterInstance) npc).dropItem(killer, 20924, 1);
				else
					((L2MonsterInstance) npc).dropItem(killer, 20925, 1);
			}
			else
			{
				if (r <= 33)
					((L2MonsterInstance) npc).dropItem(killer, 20272, 1);
				else if (r < 67)
					((L2MonsterInstance) npc).dropItem(killer, 20273, 1);
				else
					((L2MonsterInstance) npc).dropItem(killer, 20274, 1);
			}
		}
		return "";
	}

	public static void main(String[] args)
	{
		if (Config.ALLOW_APRIL_FOOLS)
		{
			new AprilFools(9555, QN, "official_events");
			_log.info("Official Events: April Fools is loaded.");
		}
		else
			_log.info("Official Events: April Fools is disabled.");
	}
}
