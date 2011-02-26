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
package custom.NonTalkingNPCs;

import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class NonTalkingNPCs extends QuestJython
{
	private static final String	QN		= "NonTalkingNPCs";

	private static final int[]	NPCS	=
										{
			18684,
			18685,
			18686,
			18687,
			18688,
			18689,
			18690,
			19691,
			18692,
			31557,
			31606,
			31671,
			31672,
			31673,
			31674,
			32026,
			32030,
			32031,
			32032,
			32306,
			32619,
			32620,
			32621,						};

	public NonTalkingNPCs(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		for (int i : NPCS)
			addFirstTalkId(i);
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2Player player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		return "";
	}

	public static void main(String[] args)
	{
		new NonTalkingNPCs(-1, QN, "custom");
	}
}