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
package teleports.NoblesseTeleport;

import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Ham Wong, Converted By K4N4BS
 * @since L0ngh0rn - Adapted for L2EmuProject.
 */
public final class NoblesseTeleport extends QuestJython
{
	public static final String			QN		= "NoblesseTeleport";

	// NPCs
	private static final int[]			NPCS	=
												{
			30006,
			30059,
			30080,
			30134,
			30146,
			30177,
			30233,
			30256,
			30320,
			30540,
			30576,
			30836,
			30848,
			30878,
			30899,
			31275,
			31320,
			31964,
			32163								};

	// Quest Item
	private static final int			TOKEN	= 13722;

	// Other
	private static final StringBuilder	HTML	= new StringBuilder();

	static
	{
		HTML.append("<html><body>");
		HTML.append("<br>Ah, you\'re a Noblesse! I can offer you a special service then.<br>");
		HTML.append("<br>You may use this Olympiad Token.<br>");
		HTML.append("<a action=\"bypass -h %bypass%\">Teleport to Hunting Grounds</a><br>");
		HTML.append("<br>Don\'t use Olympiad Token.<br>");
		HTML.append("<a action=\"bypass -h npc_%objectId%_Chat 2\">Teleport to Hunting Grounds</a><br>");
		HTML.append("<a action=\"bypass -h npc_%objectId%_Chat 0\">Back</a>");
		HTML.append("</body></html>");
	}

	public NoblesseTeleport(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		for (int NPC : NPCS)
		{
			addStartNpc(NPC);
			addTalkId(NPC);
		}
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			st = newQuestState(player);

		if (player.isNoble())
		{
			String bypass = "Quest NoblesseTeleport noble-nopass.htm";
			if (st.getQuestItemsCount(TOKEN) >= 1)
				bypass = "npc_%objectId%_Chat 3";
			htmltext = HTML.toString().replace("%bypass%", bypass).replace("%objectId%", String.valueOf(npc.getObjectId()));
		}
		else
			htmltext = "nobleteleporter-no.htm";
		return htmltext;
	}

	public static void main(String[] args)
	{
		new NoblesseTeleport(2000, QN, "Noblesse Teleport", "teleports");
	}
}