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

import net.l2emuproject.gameserver.manager.grandbosses.SailrenManager;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author L0ngh0rn
 * @since 2011-02-17
 */
public final class Sailren extends QuestJython
{
	public static final String	QN				= "Sailren";

	// NPCs
	private static final int	STATUE			= 32109;
	private static final int	VELOCIRAPTOR	= 22218;
	private static final int	PTEROSAUR		= 22199;
	private static final int	TYRANNOSAURUS	= 22217;
	private static final int	SAILREN			= 29065;

	// ITEM
	private static final int	GAZKH			= 8784;

	public Sailren(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		addStartNpc(STATUE);

		addTalkId(STATUE);

		addKillId(VELOCIRAPTOR);
		addKillId(PTEROSAUR);
		addKillId(TYRANNOSAURUS);
		addKillId(SAILREN);
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		final int npcId = npc.getNpcId();
		htmltext = null;

		if (npcId == STATUE)
		{
			if (st.getQuestItemsCount(GAZKH) > 0)
			{
				switch (SailrenManager.getInstance().canIntoSailrenLair(player))
				{
					case 0:
						st.takeItems(GAZKH, 1);
						SailrenManager.getInstance().setSailrenSpawnTask(VELOCIRAPTOR);
						SailrenManager.getInstance().entryToSailrenLair(player);
						htmltext = "<html><body>Shilen's Stone Statue:<br>Please seal the sailren by your ability.</body></html>";
						break;
					case 1:
					case 2:
						st.exitQuest(true);
						htmltext = "<html><body>Shilen's Stone Statue:<br>Another adventurers have already fought against the sailren. Do not obstruct them.</body></html>";
						break;
					case 3:
						st.exitQuest(true);
						htmltext = "<html><body>Shilen's Stone Statue:<br>The sailren is very powerful now. It is not possible to enter the inside.</body></html>";
						break;
					case 4:
						st.exitQuest(true);
						htmltext = "<html><body>Shilen's Stone Statue:<br>You seal the sailren alone? You should not do so! Bring the companion.</body></html>";
						break;
				}
			}
			else
			{
				st.exitQuest(true);
				htmltext = "<html><body>Shilen's Stone Statue:<br><font color=\"LEVEL\">Gazkh</font> is necessary for seal the sailren.</body></html>";
			}
		}

		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2Player player, boolean isPet)
	{
		switch (npc.getNpcId())
		{
			case VELOCIRAPTOR:
				SailrenManager.getInstance().setSailrenSpawnTask(PTEROSAUR);
				break;
			case PTEROSAUR:
				SailrenManager.getInstance().setSailrenSpawnTask(TYRANNOSAURUS);
				break;
			case TYRANNOSAURUS:
				SailrenManager.getInstance().setSailrenSpawnTask(SAILREN);
				break;
			case SAILREN:
				SailrenManager.getInstance().setCubeSpawn();
				QuestState st = player.getQuestState(QN);
				if (player.getQuestState(QN) == null)
					return null;
				st.exitQuest(true);
				break;
		}
		return null;
	}

	public static void main(String[] args)
	{
		new Sailren(29065, QN, "Sailren", "ai");
	}
}
