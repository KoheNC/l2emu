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
package ai.raidboss;

import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author L0ngh0rn
 * @since 2011-02-15
 */
public final class Darnel extends QuestJython
{
	public static final String	QN		= "Darnel";

	// NPCs
	private static final int	DARNEL	= 25531;
	private static final int	GATE	= 32279;

	public Darnel(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);
		addKillId(DARNEL);
	}

	@Override
	public final String onKill(L2Npc npc, L2Player player, boolean isPet)
	{
		if (npc.getNpcId() == DARNEL)
			addSpawn(GATE, 152761, 145950, -12588, 0, false, 0, false, player.getInstanceId());
		return null;
	}

	public static void main(String[] args)
	{
		new Darnel(25531, QN, "Darnel", "ai");
	}
}
