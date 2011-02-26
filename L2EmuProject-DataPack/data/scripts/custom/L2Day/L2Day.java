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
package custom.L2Day;

import javolution.util.FastMap;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author L0ngh0rn
 */
public final class L2Day extends QuestJython
{
	private static final String						QN				= "L2Day";

	private static final int						NPC				= 31774;

	private static final int						GUIDANCE		= 3926;
	private static final int						DEATH_WHISPER	= 3927;
	private static final int						FOCUS			= 3928;
	private static final int						GREATER_ACUMEN	= 3929;
	private static final int						EMPOWER			= 3932;
	private static final int						WWALK			= 3934;
	private static final int						BSOE			= 3958;
	private static final int						BRES			= 3959;

	private static final FastMap<String, Integer>	LETTER			= new FastMap<String, Integer>();

	private static final FastMap<String, ItemsMap>	ITEMS			= new FastMap<String, ItemsMap>();

	static
	{
		LETTER.put("A", 3875);
		LETTER.put("C", 3876);
		LETTER.put("E", 3877);
		LETTER.put("F", 3878);
		LETTER.put("G", 3879);
		LETTER.put("H", 3880);
		LETTER.put("I", 3881);
		LETTER.put("L", 3882);
		LETTER.put("N", 3883);
		LETTER.put("O", 3884);
		LETTER.put("R", 3885);
		LETTER.put("S", 3886);
		LETTER.put("T", 3887);
		LETTER.put("II", 3888);

		FastMap<Integer, Integer> reward;
		FastMap<Integer, Integer> consumed;

		// LINEAGEII
		reward = new FastMap<Integer, Integer>();
		consumed = new FastMap<Integer, Integer>();

		reward.put(BRES, 3);
		reward.put(BSOE, 3);
		reward.put(GREATER_ACUMEN, 3);
		reward.put(EMPOWER, 3);

		consumed.put(LETTER.get("L"), 1);
		consumed.put(LETTER.get("I"), 1);
		consumed.put(LETTER.get("N"), 1);
		consumed.put(LETTER.get("E"), 2);
		consumed.put(LETTER.get("A"), 1);
		consumed.put(LETTER.get("G"), 1);
		consumed.put(LETTER.get("II"), 1);

		ITEMS.put("LINEAGEII", new ItemsMap("LINEAGEII", reward, consumed));

		// NCSOFT
		reward = new FastMap<Integer, Integer>();
		consumed = new FastMap<Integer, Integer>();

		reward.put(BSOE, 1);
		reward.put(BRES, 1);
		reward.put(GUIDANCE, 1);
		reward.put(DEATH_WHISPER, 1);

		consumed.put(LETTER.get("N"), 1);
		consumed.put(LETTER.get("C"), 1);
		consumed.put(LETTER.get("S"), 1);
		consumed.put(LETTER.get("O"), 1);
		consumed.put(LETTER.get("F"), 1);
		consumed.put(LETTER.get("T"), 1);

		ITEMS.put("NCSOFT", new ItemsMap("NCSOFT", reward, consumed));

		// CHRONICLE
		reward = new FastMap<Integer, Integer>();
		consumed = new FastMap<Integer, Integer>();

		reward.put(WWALK, 2);
		reward.put(BRES, 2);
		reward.put(BSOE, 2);
		reward.put(FOCUS, 2);

		consumed.put(LETTER.get("C"), 2);
		consumed.put(LETTER.get("H"), 1);
		consumed.put(LETTER.get("R"), 1);
		consumed.put(LETTER.get("O"), 1);
		consumed.put(LETTER.get("N"), 1);
		consumed.put(LETTER.get("I"), 1);
		consumed.put(LETTER.get("L"), 1);
		consumed.put(LETTER.get("E"), 1);

		ITEMS.put("CHRONICLE", new ItemsMap("CHRONICLE", reward, consumed));
	}

	public L2Day(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(NPC);
		addTalkId(NPC);
	}

	private final boolean validConsumedItem(QuestState st, FastMap<Integer, Integer> list)
	{
		for (Integer itemId : list.keySet())
		{
			if (st.getQuestItemsCount(itemId) < list.get(itemId))
				return false;
		}
		return true;
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("0"))
			return "1.htm";
		else if (ITEMS.containsKey(event))
		{
			if (validConsumedItem(st, ITEMS.get(event).getConsumed()))
			{
				for (Integer itemId : ITEMS.get(event).getConsumed().keySet())
					st.takeItems(itemId, ITEMS.get(event).getConsumed().get(itemId));

				for (Integer itemId : ITEMS.get(event).getReward().keySet())
					st.giveItems(itemId, ITEMS.get(event).getReward().get(itemId));

				return "2.htm";
			}
			else
				return "<html><body>You do not have enough materials.</body></html>";
		}
		else if (htmltext != event)
			st.exitQuest(false);

		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		QuestState st = player.getQuestState(QN);

		if (st == null)
			st = newQuestState(player);

		st.set(CONDITION, 0);
		st.setState(State.STARTED);

		return "1.htm";
	}

	public static void main(String[] args)
	{
		new L2Day(5011, QN, "custom");
	}

	static class ItemsMap
	{
		private String						_name;
		private FastMap<Integer, Integer>	_reward;
		private FastMap<Integer, Integer>	_consumed;

		public ItemsMap(String name, FastMap<Integer, Integer> reward, FastMap<Integer, Integer> consumed)
		{
			_name = name;
			_reward = reward;
			_consumed = consumed;
		}

		public String getName()
		{
			return _name;
		}

		public FastMap<Integer, Integer> getReward()
		{
			return _reward;
		}

		public FastMap<Integer, Integer> getConsumed()
		{
			return _consumed;
		}
	}
}
