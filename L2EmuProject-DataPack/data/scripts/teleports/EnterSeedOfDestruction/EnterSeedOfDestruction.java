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
package teleports.EnterSeedOfDestruction;

import net.l2emuproject.gameserver.manager.gracia.SeedOfDestructionManager;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Psycho(killer1888) / L2jfree
 */
public final class EnterSeedOfDestruction extends QuestJython
{
	private static final String	QN	= "EnterSeedOfDestruction";

	private static final int	NPC	= 32526;

	public EnterSeedOfDestruction(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		return event + ".htm";
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2Player player)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
			newQuestState(player);

		String htmltext = "";
		final byte state = SeedOfDestructionManager.getInstance().getState();

		switch (state)
		{
			case SeedOfDestructionManager.STATE_ATTACK:
			case SeedOfDestructionManager.STATE_DEFENSE: // TODO: Check...
				htmltext = "32526-AttackMode.htm";
				break;
			case SeedOfDestructionManager.STATE_HUNTING_GROUND:
				htmltext = "32526-HuntingGroundMode.htm";
				break;
		}

		return htmltext;
	}

	public static void main(String[] args)
	{
		new EnterSeedOfDestruction(-1, QN, "teleports");
	}
}
