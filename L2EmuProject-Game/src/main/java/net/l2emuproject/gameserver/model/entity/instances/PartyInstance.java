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
package net.l2emuproject.gameserver.model.entity.instances;

import net.l2emuproject.gameserver.model.L2Party;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author lord_rex
 *<br> PartyInstance sketch to make instances easy.
 */
public abstract class PartyInstance extends L2Instance
{
	public PartyInstance(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);
	}

	protected final void teleportParty(L2PcInstance player, int[] coords, int instanceId)
	{
		L2Party party = player.getParty();

		if (party != null)
			for (L2PcInstance members : party.getPartyMembers())
				if (members != null)
					teleportPlayer(members, coords, instanceId);
	}

	// TODO: Add more functions...
}
