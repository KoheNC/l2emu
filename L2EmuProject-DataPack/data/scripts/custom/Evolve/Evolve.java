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
package custom.Evolve;

import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Unknown, transated by Intrepid
 */
public final class Evolve extends QuestJython
{
	private static final String	QN					= "5025_Evolve";

	// pet managers
	private static final int[]	MANAGERS			=
													{ 30731, 30869, 31067, 31265, 31309, 31954, 30827, 30828, 30829, 30830, 30831 };

	// npcId for wolf
	private static final int	WOLF				= 12077;

	// items
	private static final int[]	CONTROLITEMS		=
													{ 2375, 9882 };

	// minimum level to evolve
	private static final int	MINIMUM_LEVEL		= 55;

	// max dist. beetwen pet and owner
	private static final int	MAXIMUM_DISTANCE	= 100;

	// messages
	private static final String	ERROR1				= "<html><body>You're suppossed to own a wolf and have it summoned in order for it to evolve.</body></html>";
	private static final String	ERROR2				= "<html><body>Your pet needs to be level " + MINIMUM_LEVEL + " in order for it to evolve.</body></html>";
	private static final String	ERROR3				= "Your pet is not a wolf.";
	private static final String	ERROR4				= "Your pet should be nearby.";
	private static final String	END_MESSAGE			= "<html><body>Great job, your Wolf";
	private static final String	END_MESSAGE2		= "has become a GreatWolf, enjoy!</body></html>";

	public Evolve(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int i : MANAGERS)
		{
			addStartNpc(i);
			addTalkId(i);
		}
	}

	@Override
	public String onTalk(L2Npc npc, L2Player talker)
	{
		String htmlText = NO_QUEST;
		QuestState st = talker.getQuestState(QN);
		if (st == null)
			return htmlText;

		if (talker.getPet() == null)
		{
			htmlText = ERROR1;
			st.exitQuest(true);
		}
		else if (talker.getPet().getTemplate().getNpcId() != WOLF)
		{
			htmlText = ERROR3;
			st.exitQuest(true);
		}
		else if (talker.getPet().getLevel() < MINIMUM_LEVEL)
		{
			htmlText = ERROR2;
			st.exitQuest(true);
		}
		else if (!getDistance(talker))
		{
			htmlText = ERROR4;
			st.exitQuest(true);
		}
		else if (getControlItem(talker) == 0)
		{
			htmlText = ERROR3;
			st.exitQuest(true);
		}
		else
		{
			htmlText = END_MESSAGE + talker.getPet() + END_MESSAGE2;
			int _item = CONTROLITEMS[talker.getInventory().getItemByObjectId(talker.getPet().getControlItemId()).getItemId()];
			talker.getPet().deleteMe(talker);
			st.giveItems(_item, 1);
			st.exitQuest(true);
			st.sendPacket(SND_FINISH);
		}

		return htmlText;
	}

	private int getControlItem(L2Player player)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
			return 0;

		int item = st.getPlayer().getPet().getControlItemId();

		if (st.getState() == State.STARTED)
			st.set("item", item);
		else
		{
			if (st.getInt("item") != item)
				item = 0;
		}

		return item;
	}

	private boolean getDistance(L2Player player)
	{
		boolean isFar = false;

		if (player.getPet().getX() - player.getX() > MAXIMUM_DISTANCE)
			isFar = true;
		if (player.getPet().getY() - player.getY() > MAXIMUM_DISTANCE)
			isFar = true;
		if (player.getPet().getZ() - player.getZ() > MAXIMUM_DISTANCE)
			isFar = true;

		return isFar;
	}

	public static void main(String[] args)
	{
		new Evolve(5025, QN, "Evolve your Wolf");
	}
}
