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
package quests._061_LawEnforcement;

import net.l2emuproject.gameserver.entity.base.ClassId;
import net.l2emuproject.gameserver.entity.base.Race;
import net.l2emuproject.gameserver.entity.itemcontainer.PcInventory;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * 3rd class transfer quest for Inspector.
 * @author hex1r0
 */
public final class LawEnforcement extends QuestJython
{
	private static final String	QN			= "_061_LawEnforcement";

	// Quest NPCs
	private static final int	LIANE		= 32222;
	private static final int	KEKROPUS	= 32138;
	private static final int	EINDBURGH	= 32469;

	private LawEnforcement(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(LIANE);
		addTalkId(LIANE);
		addTalkId(KEKROPUS);
		addTalkId(EINDBURGH);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		QuestState qs = player.getQuestState(QN);
		if (qs == null)
			return NO_QUEST;
		else if (qs.isCompleted())
			return QUEST_DONE;

		if (event.equals("32222-05.htm"))
		{
			qs.set(CONDITION, 1);
			qs.setState(State.STARTED);
			player.sendPacket(SND_ACCEPT);
		}
		else if (event.equals("32138-09.htm"))
		{
			qs.set(CONDITION, 2);
			player.sendPacket(SND_MIDDLE);
		}
		else if (event.equals("32469-08.htm") || event.equals("32469-09.htm"))
		{
			player.setClassId(ClassId.Judicator.getId());
			player.broadcastUserInfo();
			qs.rewardItems(PcInventory.ADENA_ID, 26000);
			qs.exitQuest(false);
			player.sendPacket(SND_FINISH);
		}
		return event;
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player talker)
	{
		QuestState qs = talker.getQuestState(QN);
		if (qs == null)
			return NO_QUEST;

		int npcId = npc.getNpcId();
		int cond = qs.getInt(CONDITION);

		if (npcId == LIANE)
		{
			if (cond == 0)
			{
				if (talker.getRace() == Race.Kamael)
				{
					if (talker.getClassId() == ClassId.Inspector && talker.getLevel() >= 76)
						return "32222-01.htm";
					else
						return "32222-02.htm";
				}
				else
					return "32222-03.htm";
			}
			else if (cond == 1)
				return "32222-06.htm";
		}
		else if (npcId == KEKROPUS)
		{
			if (cond == 1)
				return "32138-01.htm";
			else if (cond == 2)
				return "32138-10.htm";
		}
		else if (npcId == EINDBURGH && cond == 2)
			return "32469-01.htm";
		return NO_QUEST;
	}

	public static void main(String[] args)
	{
		new LawEnforcement(61, QN, "Law Enforcement");
	}
}
