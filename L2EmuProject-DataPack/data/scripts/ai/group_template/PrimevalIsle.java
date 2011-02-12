/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General private License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General private License for more
 * details.
 * 
 * You should have received a copy of the GNU General private License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.group_template;

import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2MonsterInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.tools.random.Rnd;

/**
 * @author L0ngh0rn
 */
public final class PrimevalIsle extends L2AttackableAIScript
{
	private static String						QN		= "PrimevalIsle";
	private static final int[]					NPCS	=
														{
			22201,
			22202,
			22204,
			22205,
			22209,
			22210,
			22212,
			22213,
			22214,
			22219,
			22220,
			22221,
			22222,
			22224,
			22225,
			22742,
			22743,
			22744,
			22745										};

	private final List<DropHerb>				_herbs	= new FastList<DropHerb>();
	private final Map<Integer, List<DropHerb>>	_mobs	= new FastMap<Integer, List<DropHerb>>();

	private void init()
	{
		// TODO: check chances on retail
		_herbs.add(new DropHerb(13028, 20, 1, 1)); // Vitality Replenishing Herb
		_herbs.add(new DropHerb(8600, 200, 1, 1)); // Herb of Life
		_herbs.add(new DropHerb(8601, 80, 1, 1)); // Greater Herb of Life
		_herbs.add(new DropHerb(8602, 16, 1, 1)); // Superior Herb of Life
		_herbs.add(new DropHerb(8603, 150, 1, 1)); // Herb of Mana
		_herbs.add(new DropHerb(8604, 60, 1, 1)); // Greater Herb of Mana
		_herbs.add(new DropHerb(8605, 12, 1, 1)); // Superior Herb of Mana
		_herbs.add(new DropHerb(8613, 4, 1, 1)); // Herb of the Mystic
		_herbs.add(new DropHerb(8614, 4, 1, 1)); // Herb of Recovery
		_herbs.add(new DropHerb(8607, 150, 1, 1)); // Herb of Magic
		_herbs.add(new DropHerb(8609, 150, 1, 1)); // Herb of Casting Spd.
		_herbs.add(new DropHerb(8611, 150, 1, 1)); // Herb of Speed
		_herbs.add(new DropHerb(10657, 75, 1, 1)); // Herb of Doubt

		for (int i = 0; i < NPCS.length; i++)
			_mobs.put(NPCS[i], _herbs);
	}

	public PrimevalIsle(int questId, String name, String descr)
	{
		super(questId, name, descr);
		init();
		for (int i : NPCS)
		{
			addKillId(i);
		}
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (_mobs.containsKey(npcId) && npc instanceof L2MonsterInstance)
		{
			for (DropHerb drop : _mobs.get(npcId))
			{
				if (drop.getChance() >= Rnd.get(1000))
				{
					int countHerb = Rnd.get(drop.getMin(), drop.getMax());
					for (int i = 0; i < countHerb; i++)
						((L2MonsterInstance) npc).dropItem(killer, drop.getItemId(), 1);
				}
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new PrimevalIsle(-1, QN, "ai");
	}
}
