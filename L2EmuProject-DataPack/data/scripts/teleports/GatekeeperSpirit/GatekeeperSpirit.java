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
package teleports.GatekeeperSpirit;

import net.l2emuproject.gameserver.events.global.sevensigns.SevenSigns;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class GatekeeperSpirit extends QuestJython
{
	private static final String	QN	= "GatekeeperSpirit";
	private static final int	NPC	= 31111;

	public GatekeeperSpirit(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2Player player)
	{
		String htmltext = "";
		final int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
		final int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_AVARICE);
		final int compWinner = SevenSigns.getInstance().getCabalHighestScore();

		if (playerCabal == sealAvariceOwner && playerCabal == compWinner)
		{
			switch (sealAvariceOwner)
			{
				case SevenSigns.CABAL_DAWN:
					htmltext = "dawn.htm";
					break;
				case SevenSigns.CABAL_DUSK:
					htmltext = "dusk.htm";
					break;
				case SevenSigns.CABAL_NULL:
					npc.showChatWindow(player);
					break;
			}
		}
		else
			npc.showChatWindow(player);

		return htmltext;
	}

	public static void main(String[] args)
	{
		new GatekeeperSpirit(-1, QN, "teleports");
	}
}
