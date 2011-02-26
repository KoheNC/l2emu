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
package custom.NewbieCoupons;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.services.transactions.L2Multisell;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author L0ngh0rn
 */
public final class NewbieCoupons extends QuestJython
{
	private static final String	QN						= "NewbieCoupons";

	// NPCs
	private static final int	NG_HUMAN				= 30598;
	private static final int	NG_ELF					= 30599;
	private static final int	NG_DARKELF				= 30600;
	private static final int	NG_DWARF				= 30601;
	private static final int	NG_ORC					= 30602;
	private static final int	NG_COMMON_HUMAN_A		= 31076;
	private static final int	NG_COMMON_HUMAN_B		= 31077;
	private static final int	NG_KAMAEL				= 32135;

	public static final int[]	GUIDES					=
														{ NG_HUMAN, NG_ELF, NG_DARKELF, NG_DWARF, NG_ORC, NG_COMMON_HUMAN_A, NG_COMMON_HUMAN_B, NG_KAMAEL };

	// Quest Item
	public static final int		COUPON_ONE				= 7832;
	public static final int		COUPON_TWO				= 7833;

	// Others
	public static final int		NEWBIE_WEAPON			= 16;
	public static final int		NEWBIE_ACCESORY			= 32;
	public static final int		WEAPON_MULTISELL		= 305986001;
	public static final int		ACCESORIES_MULTISELL	= 305986002;

	public NewbieCoupons(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int i : GUIDES)
		{
			addStartNpc(i);
			addTalkId(i);
		}
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return "na.htm";

		int newbie = player.getNewbie();
		int level = player.getLevel();
		int occupation_level = player.getClassId().level();
		int pkkills = player.getPkKills();

		if (event.equalsIgnoreCase("newbie_give_weapon_coupon"))
		{
			if ((level >= 6 && level <= 39) && pkkills == 0 && occupation_level == 0)
			{
				if ((newbie | NEWBIE_WEAPON) != newbie)
				{

					player.setNewbie(newbie | NEWBIE_WEAPON);
					st.giveItems(COUPON_ONE, 5);
					return "2.htm";
				}
				else
					return "1.htm";
			}
			else
				return "3.htm";
		}
		else if (event.equalsIgnoreCase("newbie_give_armor_coupon"))
		{
			if ((level >= 6 && level <= 39) && pkkills == 0 && occupation_level == 1)
			{
				if ((newbie | NEWBIE_ACCESORY) != newbie)
				{
					player.setNewbie(newbie | NEWBIE_ACCESORY);
					st.giveItems(COUPON_TWO, 1);
					return "5.htm";
				}
				else
					return "4.htm";
			}
			else
				return "6.htm";
		}
		else if (event.equalsIgnoreCase("newbie_show_weapon"))
		{
			if ((level >= 6 && level <= 39) && pkkills == 0 && occupation_level == 0)
				L2Multisell.getInstance().separateAndSend(WEAPON_MULTISELL, player, npc.getNpcId(), false, 0.0);
			else
				return "7.htm";
		}
		else if (event.equalsIgnoreCase("newbie_show_armor"))
		{
			if ((level >= 6 && level <= 39) && pkkills == 0 && occupation_level == 0)
				L2Multisell.getInstance().separateAndSend(ACCESORIES_MULTISELL, player, npc.getNpcId(), false, 0.0);
			else
				return "8.htm";
		}
		return null;
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		if (Config.ALT_ENABLE_NEWBIE_COUPONS)
		{
			QuestState st = player.getQuestState(QN);

			if (st == null)
				st = newQuestState(player);

			return "0.htm";
		}
		return "na.htm";
	}

	public static void main(String[] args)
	{
		new NewbieCoupons(5001, QN, "custom");
	}
}