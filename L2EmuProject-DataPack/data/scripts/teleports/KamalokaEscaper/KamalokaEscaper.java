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
package teleports.KamalokaEscaper;

import net.l2emuproject.gameserver.manager.instances.Instance;
import net.l2emuproject.gameserver.manager.instances.InstanceManager;
import net.l2emuproject.gameserver.services.party.L2Party;
import net.l2emuproject.gameserver.services.quest.Quest;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author lord_rex
 *	<br> A simple script to handle escape from Kamaloka Instances.
 */
public final class KamalokaEscaper extends Quest
{
	public static final int	GATEKEEPER	= 32496;

	public KamalokaEscaper(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		addFirstTalkId(GATEKEEPER);
		addTalkId(GATEKEEPER);
	}

	@Override
	public final String onTalk(final L2Npc npc, final L2Player player)
	{
		final int npcId = npc.getNpcId();
		final L2Party party = player.getParty();

		if (npcId == GATEKEEPER && party != null && party.isLeader(player))
		{
			final int instanceId = player.getInstanceId();
			final Instance instance = InstanceManager.getInstance().getInstance(instanceId);

			instance.removePlayers();
		}

		return null;
	}

	@Override
	public final String onFirstTalk(final L2Npc npc, final L2Player player)
	{
		String htmltext = "";
		final int npcId = npc.getNpcId();
		final L2Party party = player.getParty();

		if (npcId == GATEKEEPER)
		{
			if (party != null && party.isLeader(player))
				htmltext = "32496.htm";
			else
				htmltext = "32496-no.htm";
		}

		return htmltext;
	}

	public static void main(String[] args)
	{
		new KamalokaEscaper(-1, KamalokaEscaper.class.getSimpleName(), "Kamaloka Escaper", "teleports");
	}
}
