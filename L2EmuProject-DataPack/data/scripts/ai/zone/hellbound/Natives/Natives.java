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
package ai.zone.hellbound.Natives;

import net.l2emuproject.gameserver.instancemanager.hellbound.HellboundManager;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author DS, based on theOne's work
 */
public final class Natives extends QuestJython
{
	private static final int	NATIVE		= 32362;
	private static final int	INSURGENT	= 32363;

	public Natives(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addFirstTalkId(NATIVE);
		addFirstTalkId(INSURGENT);
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2Player player)
	{
		if (HellboundManager.getInstance().getHellboundLevel() > 5)
		{
			if (npc.getNpcId() == NATIVE)
				return "32362-01.htm";
			else
				return "32363-01.htm";
		}
		else
		{
			if (npc.getNpcId() == NATIVE)
				return "32362.htm";
			else
				return "32363.htm";
		}
	}

	public static void main(String[] args)
	{
		new Natives(-1, Natives.class.getSimpleName(), "ai/zones/hellbound");
	}
}