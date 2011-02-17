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
package teleports.HellboundWarpGate;

import net.l2emuproject.gameserver.instancemanager.hellbound.HellboundManager;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;

/**
 * @author K4N4BS
 * @since Adapted for L2EmuProject by L0ngh0rn 2011-02-08
 */
public final class HellboundWarpGate extends QuestJython
{
	public static final String		QN							= "HellboundWarpGate";

	// Quests
	private static final String		THATS_BLOODY_HOT			= "133_ThatsBloodyHot";
	private static final String		PATH_TO_HELLBOUND			= "130_PathToHellbound";

	// NPCs
	private static final int[]		WARPGATES					=
																{ 32314, 32315, 32316, 32317, 32318, 32319 };

	// Other
	private static final boolean	ENERGY_FROM_MINOR_BOSSES	= true;
	private static int				BLOOD_HOT					= 0;

	// Boss
	private static final int		KECHI						= 25532;
	private static final int		DARNEL						= 25531;
	private static final int		TEARS						= 25534;
	private static final int		BAYLOR						= 29099;

	public HellboundWarpGate(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		for (int id : WARPGATES)
		{
			addStartNpc(id);
			addFirstTalkId(id);
			addTalkId(id);
		}

		addKillId(BAYLOR);
		addKillId(TEARS);
		addKillId(KECHI);
		addKillId(DARNEL);

		try
		{
			String longQuest = loadGlobalQuestVar("BloodyHotQuest");
			if (longQuest.equalsIgnoreCase(""))
			{
				saveGlobalQuestVar("BloodyHotQuest", "0");
				longQuest = "0";
			}
			BLOOD_HOT = Integer.parseInt(longQuest);
		}
		catch (Exception e)
		{
			BLOOD_HOT = 0;
		}
	}

	public final void openHbGates()
	{
		String longQuest = loadGlobalQuestVar("BloodyHotQuest");
		if (!longQuest.equalsIgnoreCase("1"))
		{
			saveGlobalQuestVar("BloodyHotQuest", "1");
			longQuest = "1";
			BLOOD_HOT = Integer.parseInt(longQuest);
		}
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (!canEnter(player) && !HellboundManager.getInstance().isWarpgateActive())
			return "warpgate-locked.htm";

		return npc.getNpcId() + ".htm";
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (!canEnter(player))
			return "warpgate-no.htm";

		player.teleToLocation(-11095, 236440, -3232, true);
		return null;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		switch (npc.getNpcId())
		{
			case BAYLOR:
				if (BLOOD_HOT == 1)
					HellboundManager.getInstance().addWarpgateEnergy(80000);
				break;
			case TEARS:
			case KECHI:
			case DARNEL:
				if (ENERGY_FROM_MINOR_BOSSES)
				{
					HellboundManager.getInstance().addWarpgateEnergy(10000);
				}
				break;
		}
		return null;
	}

	private final boolean canEnter(L2PcInstance player)
	{
		if (player.isFlying())
			return false;

		QuestState st = player.getQuestState(PATH_TO_HELLBOUND);
		QuestState st2 = player.getQuestState(THATS_BLOODY_HOT);
		if (st != null && st.getState() == State.COMPLETED && st2 != null && st2.getState() == State.COMPLETED)
			return true;

		return false;
	}

	private static final class SingletonHolder
	{
		private static final HellboundWarpGate	INSTANCE	= new HellboundWarpGate(1108, QN, "Hellbound WarpGate", "teleports");
	}

	public static final HellboundWarpGate getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	public static void main(String[] args)
	{
		HellboundWarpGate.getInstance();
	}
}