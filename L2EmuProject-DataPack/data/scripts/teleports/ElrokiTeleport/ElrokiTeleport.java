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
package teleports.ElrokiTeleport;

import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author kerberos_20
 * 
 * @translated by Intrepid
 */
public final class ElrokiTeleport extends QuestJython
{
	private static final String	QN		= "ElrokiTeleport";

	private static final int	NPC1	= 32111;
	private static final int	NPC2	= 32112;

	public ElrokiTeleport(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(NPC1);
		addTalkId(NPC1);
		addStartNpc(NPC2);
		addTalkId(NPC2);
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player talker)
	{
		switch (npc.getNpcId())
		{
			case NPC1:
			{
				if (talker.isInCombat())
					return "32111-no.htm";
				else
					talker.teleToLocation(4990, -1879, -3178);
				break;
			}
			case NPC2:
			{
				talker.teleToLocation(7557, -5513, -3221);
				break;
			}
		}
		return "";
	}

	public static void main(String args[])
	{
		new ElrokiTeleport(-1, QN, "teleports");
	}
}