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

import org.apache.commons.lang.ArrayUtils;

import net.l2emuproject.gameserver.instancemanager.grandbosses.VanHalterManager;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author L0ngh0rn
 * @since 2011-02-14
 */
public final class VanHalter extends QuestJython
{
	public static final String	QN					= "VanHalter";

	// NPCs
	private static final int	ANDREAS_VAN_HALTER	= 29062;
	private static final int	ANDREAS_CAPTAIN		= 22188;
	private static final int[]	TRIOLS				= new int[]
													{ 32058, 32059, 32060, 32061, 32062, 32063, 32064, 32065, 32066 };

	public VanHalter(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		addAttackId(ANDREAS_VAN_HALTER);

		addKillId(ANDREAS_VAN_HALTER);
		addKillId(ANDREAS_CAPTAIN);

		for (int i : TRIOLS)
			addKillId(i);
	}

	@Override
	public final String onAttack(L2Npc npc, L2Player attacker, int damage, boolean isPet, L2Skill skill)
	{
		if (npc.getNpcId() == ANDREAS_VAN_HALTER && ((npc.getStatus().getCurrentHp() / npc.getMaxHp()) * 100 <= 20))
			VanHalterManager.getInstance().callRoyalGuardHelper();
		return null;
	}

	@Override
	public final String onKill(L2Npc npc, L2Player killer, boolean isPet)
	{
		final int npcId = npc.getNpcId();
		if (ArrayUtils.contains(TRIOLS, npcId))
		{
			VanHalterManager.getInstance().removeBleeding(npcId);
			VanHalterManager.getInstance().checkTriolRevelationDestroy();
		}
		else
		{
			switch (npcId)
			{
				case ANDREAS_CAPTAIN:
					VanHalterManager.getInstance().checkRoyalGuardCaptainDestroy();
					break;
				case ANDREAS_VAN_HALTER:
					VanHalterManager.getInstance().enterInterval();
					break;
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new VanHalter(29062, QN, "Andreas Van Halter", "ai");
	}
}
