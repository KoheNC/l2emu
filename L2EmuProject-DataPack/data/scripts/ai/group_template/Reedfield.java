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
 * @author L0ngh0rn, Q.I
 */
public final class Reedfield extends L2AttackableAIScript
{
	private static String						QN		= "Reedfield";
	private static final int[]					NPCS	=
														{
			22650,
			22651,
			22652,
			22653,
			22654,
			22655,
			22656,
			22657,
			22658,
			22659										};

	private final List<DropHerb>				_herbs	= new FastList<DropHerb>();
	private final Map<Integer, List<DropHerb>>	_mobs	= new FastMap<Integer, List<DropHerb>>();

	private void init()
	{
		// TODO Ancient herbs
		_herbs.add(new DropHerb(13028, 5, 1, 1)); // Vitality Replenishing Herb
		_herbs.add(new DropHerb(8600, 210, 1, 1)); // Herb of Life
		_herbs.add(new DropHerb(8601, 150, 1, 1)); // Greater Herb of Life
		_herbs.add(new DropHerb(8602, 35, 1, 1)); // Superior Herb of Life
		_herbs.add(new DropHerb(8603, 190, 1, 1)); // Herb of Mana
		_herbs.add(new DropHerb(8604, 180, 1, 1)); // Greater Herb of Mana
		_herbs.add(new DropHerb(8605, 50, 1, 1)); // Superior Herb of Mana
		_herbs.add(new DropHerb(8612, 2, 1, 1)); // Herb of the Warrior
		_herbs.add(new DropHerb(8613, 10, 1, 1)); // Herb of the Mystic
		_herbs.add(new DropHerb(8614, 2, 1, 1)); // Herb of Recovery
		_herbs.add(new DropHerb(8606, 60, 1, 1)); // Herb of Power
		_herbs.add(new DropHerb(8607, 60, 1, 1)); // Herb of Magic
		_herbs.add(new DropHerb(8608, 80, 1, 1)); // Herb of Atk. Spd
		_herbs.add(new DropHerb(8609, 40, 1, 1)); // Herb of Casting Spd
		_herbs.add(new DropHerb(8610, 20, 1, 1)); // Herb of Critical Attack - Probability
		_herbs.add(new DropHerb(8611, 90, 1, 1)); // Herb of Speed
		_herbs.add(new DropHerb(10655, 60, 1, 1)); // Herb of Vampiric Rage
		_herbs.add(new DropHerb(10656, 30, 1, 1)); // Herb of Critical Attack - Power
		_herbs.add(new DropHerb(10657, 5, 1, 1)); // Herb of Doubt
		// _herbs.add(new DropHerb(14824, 9, 1, 1)); // Ancient Herb - Slayer
		// _herbs.add(new DropHerb(14825, 9, 1, 1)); // Ancient Herb - Immortal
		_herbs.add(new DropHerb(14826, 25, 1, 1)); // Ancient Herb - Terminator
		_herbs.add(new DropHerb(14827, 30, 1, 1)); // Ancient Herb - Guide

		for (int i = 0; i < NPCS.length; i++)
			_mobs.put(NPCS[i], _herbs);
	}

	public Reedfield(int questId, String name, String descr)
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
		new Reedfield(-1, QN, "ai");
	}
}
