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
package custom.Nottingale;

import net.l2emuproject.gameserver.network.serverpackets.RadarControl;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author L0ngh0rn
 */
public final class Nottingale extends QuestJython
{
	private static final String	QN	= "Nottingale";

	private static final int	NPC	= 32627;

	public Nottingale(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;

		QuestState qs = player.getQuestState("10273_GoodDayToFly");

		if (qs == null || (qs != null && qs.getState() != State.COMPLETED))
		{
			player.sendPacket(new RadarControl(2, 2, 0, 0, 0));
			player.sendPacket(new RadarControl(0, 2, -184545, 243120, 1581));
			htmltext = "32627.htm";
		}
		else if (event.equalsIgnoreCase("32627-3.htm"))
		{
			player.sendPacket(new RadarControl(2, 2, 0, 0, 0));
			player.sendPacket(new RadarControl(0, 2, -192361, 254528, 3598));
		}
		else if (event.equalsIgnoreCase("32627-4.htm"))
		{
			player.sendPacket(new RadarControl(2, 2, 0, 0, 0));
			player.sendPacket(new RadarControl(0, 2, -174600, 219711, 4424));
		}
		else if (event.equalsIgnoreCase("32627-5.htm"))
		{
			player.sendPacket(new RadarControl(2, 2, 0, 0, 0));
			player.sendPacket(new RadarControl(0, 2, -181989, 208968, 4424));
		}
		else if (event.equalsIgnoreCase("32627-6.htm"))
		{
			player.sendPacket(new RadarControl(2, 2, 0, 0, 0));
			player.sendPacket(new RadarControl(0, 2, -252898, 235845, 5343));
		}
		else if (event.equalsIgnoreCase("32627-8.htm"))
		{
			player.sendPacket(new RadarControl(2, 2, 0, 0, 0));
			player.sendPacket(new RadarControl(0, 2, -212819, 209813, 4288));
		}
		else if (event.equalsIgnoreCase("32627-9.htm"))
		{
			player.sendPacket(new RadarControl(2, 2, 0, 0, 0));
			player.sendPacket(new RadarControl(0, 2, -246899, 251918, 4352));
		}
		return htmltext;
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2Player player)
	{
		QuestState st = player.getQuestState(QN);

		if (st == null)
			st = newQuestState(player);

		player.setLastQuestNpcObject(npc.getObjectId());
		npc.showChatWindow(player);
		return null;
	}

	public static void main(String[] args)
	{
		new Nottingale(1002, QN, "custom");
	}
}
