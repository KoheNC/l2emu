/*
 * This program is free software, you can redistribute it and/or modify it under
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
 * this program. If not, see <http,//www.gnu.org/licenses/>.
 */
package ai.zone.kratei_cube;

import ai.L2AttackableAIScript;
import net.l2emuproject.gameserver.instancemanager.games.KrateiCube;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.world.object.L2Npc;

/**
 * @author lord_rex
 */
public final class KrateiCubeAI extends L2AttackableAIScript
{
	private static final String	QN			= "KrateiCubeAI";
	private static final int[]	MONSTERS	=
											{
			18587,
			18580,
			18581,
			18584,
			18591,
			18589,
			18583,
			18590,
			18591,
			18589,
			18585,
			18586,
			18583,
			18592,
			18582,
			18595,
			18597,
			18596,
			18598,
			18593,
			18600,
			18594,
			18599							};

	public KrateiCubeAI(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int i : MONSTERS)
			addKillId(i);
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (KrateiCube.isPlaying(player))
			player.getPlayerEventData().givePoints(3);

		return "";
	}

	public static void main(String[] args)
	{
		new KrateiCubeAI(-1, QN, "ai");
	}
}
