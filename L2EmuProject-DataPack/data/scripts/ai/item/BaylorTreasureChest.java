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
package ai.item;

import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.tools.random.Rnd;

/**
 * @author savormix
 *
 */
public final class BaylorTreasureChest extends QuestJython
{
	private static final String THIS = "BaylorTreasureChest";

	// Quest items
	private static final int DYNASTY_EARRING = 9470;
	private static final int BLESSED_ENCHANT_SCROLL = 6578;
	private static final int SEALED_BOOTS_DESIGN = 6704;

	// Quest monsters
	private static final int[] TREASURE_CHEST = {
		29116, 29117
	};

	public BaylorTreasureChest(int questId, String name, String descr)
	{
		super(questId, name, descr);
		for (int id : TREASURE_CHEST)
			addKillId(id);
	}

	@Override
	public String onKill(L2Npc npc, L2Player killer, boolean isPet)
	{
		int rnd = Rnd.get(100);
		if (rnd < 2)
			npc.dropItem(DYNASTY_EARRING, 1);
		else if (rnd < 33)
			npc.dropItem(BLESSED_ENCHANT_SCROLL, 2);
		else
			npc.dropItem(SEALED_BOOTS_DESIGN, 10);
		return null;
	}

	public static void main(String[] args)
	{
		new BaylorTreasureChest(-1, THIS, "ai");
	}
}
