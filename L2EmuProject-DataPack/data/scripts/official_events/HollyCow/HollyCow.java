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
package official_events.HollyCow;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.ai.CtrlIntention;
import net.l2emuproject.gameserver.model.actor.L2Attackable;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.tools.random.Rnd;

/**
 * @author Gigiikun & janii
 */
public class HollyCow extends QuestJython
{
	private static final String		QN								= "HollyCow";

	private static final boolean	HC_STARTED						= false;

	private static final int		FARMER							= 13183;

	private static final int		MILK_COW						= 13187;
	private static final int		HEAD_MILK_COW					= 13188;
	private static final int		GLOOM_MILK_COW					= 13191;
	private static final int		GLOOM_HEAD_MILK_COW				= 13192;
	private static final int		HC_ADENA						= 57;
	private static final int		HC_MILK							= 14739;
	private static final int		HC_MILK_COW_SCROLL				= 14724;
	private static final int		HC_HEAD_MILK_COW_SCROLL			= 14725;
	private static final int		HC_GLOOM_MILK_COW_SCROLL		= 14725;
	private static final int		HC_GLOOM_HEAD_MILK_COW_SCROLL	= 14725;

	/*
	 * Disabled Scrolls ID till i find npc and correct skills
	 * private static final int HC_GLOOM_MILK_COW_SCROLL = 14726;
	 * private static final int HC_GLOOM_HEAD_MILK_COW_SCROLL = 14727;
	 */

	/*
	 * List of Monsters
	 */
	private static final int		BULK							= 13189;
	private static final int		HEAD_BULK						= 13190;

	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
		{
			st = newQuestState(player);
		}
		if (st.getState() != State.STARTED)
		{
			st.setState(State.STARTED);
		}
		String htmltext = "";
		if (npc.getNpcId() == FARMER)
		{
			if (HC_STARTED)
			{
				htmltext = "farmer.htm";
			}
			else
			{
				htmltext = "";
			}
		}
		else
		{
			htmltext = "cows.htm";
		}
		return htmltext;
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
		{
			st = newQuestState(player);
		}

		String htmltext = "";
		int chance = st.getInt("chance");

		if (npc.getNpcId() == FARMER)
		{
			if (event.equalsIgnoreCase("getscroll"))
			{
				if (st.getQuestItemsCount(HC_ADENA) > 45000)
				{
					st.takeItems(HC_ADENA, 45000);
					st.giveItems(HC_MILK_COW_SCROLL, 1);
					htmltext = "farmer.htm";
					player.sendMessage("Farmer: I give you a scroll for summon cow for 2 minutes.");
				}
				else
				{
					htmltext = "farmer.htm";
					player.sendMessage("Farmer: I don't give you anything if you don't have a money. Give me 45k Adena.");
				}
			}
		}
		else
		{
			if (!HC_STARTED)
			{
				return "<html>Myam ... fresh air myam.</html>";
			}
			else
			{
				//	Milk Cow - finished
				if (npc.getNpcId() == MILK_COW)
				{
					if (event.equalsIgnoreCase("feed"))
					{
						if (chance >= 0 && chance < 6)
						{
							switch (chance)
							{
								case 1:
									st.set("chance", "2");
									break;
								case 2:
									st.set("chance", "3");
									break;
								case 3:
									st.set("chance", "4");
									break;
								case 4:
									st.set("chance", "5");
									break;
								case 5:
									st.set("chance", "6");
									break;
								default:
									st.set("chance", "1");
									break;
							}
							if (Rnd.get(1, 10) > 8)
							{
								st.set("chance", "0");
								player.sendMessage("I eat too much .... prepare for fight");
								htmltext = "";
								npc.deleteMe();
								L2Attackable newNpc = (L2Attackable) st.addSpawn(BULK, 60000);
								L2Character originalAttacker = player;
								newNpc.setRunning();
								newNpc.addDamageHate(originalAttacker, 0, 500);
								newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalAttacker);
							}
							else
							{
								player.sendMessage("Myam ... myam ...");
								htmltext = "cows.htm";
							}
						}
						else
						{
							if (Rnd.get(1, 10) > 5)
							{
								st.set("chance", "0");
								player.sendMessage("I eat too much .... prepare for fight");
								htmltext = "";
								npc.deleteMe();
								L2Attackable newNpc = (L2Attackable) st.addSpawn(HEAD_BULK, 60000);
								L2Character originalAttacker = player;
								newNpc.setRunning();
								newNpc.addDamageHate(originalAttacker, 0, 500);
								newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalAttacker);
							}
							else
							{
								player.sendMessage("Myam ... myam ...");
								htmltext = "cows.htm";
							}
						}
					}
					if (event.equalsIgnoreCase("milking"))
					{
						if (chance < 6)
						{
							if (Rnd.get(1, 4) > 3)
							{
								player.sendMessage("Ohhh this is nice.");
								st.giveItems(HC_MILK, 1);
								htmltext = "cows.htm";
							}
							else
							{
								switch (chance)
								{
									case 1:
										st.set("chance", "2");
										break;
									case 2:
										st.set("chance", "3");
										break;
									case 3:
										st.set("chance", "4");
										break;
									case 4:
										st.set("chance", "5");
										break;
									case 5:
										st.set("chance", "6");
										break;
									default:
										st.set("chance", "1");
										break;
								}
								player.sendMessage("Do this more ...");
								htmltext = "cows.htm";
							}
						}
						else
						{
							st.set("chance", "0");
							player.sendMessage("This hurt me ... help");
							htmltext = "";
							npc.deleteMe();
							L2Attackable newNpc = (L2Attackable) st.addSpawn(HEAD_BULK, 60000);
							L2Character originalAttacker = player;
							newNpc.setRunning();
							newNpc.addDamageHate(originalAttacker, 0, 500);
							newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalAttacker);
						}
					}
				}
				//	Head Milk Cow
				if (npc.getNpcId() == HEAD_MILK_COW)
				{
					if (event.equalsIgnoreCase("feed"))
					{
						if (chance >= 0 && chance < 6)
						{
							switch (chance)
							{
								case 1:
									st.set("chance", "2");
									break;
								case 2:
									st.set("chance", "3");
									break;
								case 3:
									st.set("chance", "4");
									break;
								case 4:
									st.set("chance", "5");
									break;
								case 5:
									st.set("chance", "6");
									break;
								default:
									st.set("chance", "1");
									break;
							}
							if (Rnd.get(1, 10) > 8)
							{
								st.set("chance", "0");
								player.sendMessage("I eat too much .... prepare for fight");
								htmltext = "";
								npc.deleteMe();
								L2Attackable newNpc = (L2Attackable) st.addSpawn(HEAD_BULK, 60000);
								L2Character originalAttacker = player;
								newNpc.setRunning();
								newNpc.addDamageHate(originalAttacker, 0, 500);
								newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalAttacker);
							}
							else
							{
								htmltext = "cows.htm";
								player.sendMessage("Myam ... myam ...");
							}
						}
						else
						{
							if (Rnd.get(1, 10) > 5)
							{
								st.set("chance", "0");
								player.sendMessage("I eat too much .... prepare for fight");
								htmltext = "";
								npc.deleteMe();
								L2Attackable newNpc = (L2Attackable) st.addSpawn(HEAD_BULK, 60000);
								L2Character originalAttacker = player;
								newNpc.setRunning();
								newNpc.addDamageHate(originalAttacker, 0, 500);
								newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalAttacker);
							}
							else
							{
								player.sendMessage("Myam ... myam ...");
								htmltext = "cows.htm";
							}
						}
					}
					if (event.equalsIgnoreCase("milking"))
					{
						if (chance < 6)
						{
							if (Rnd.get(1, 4) > 3)
							{
								player.sendMessage("Ohhh this is nice.");
								st.giveItems(HC_MILK, 1);
								htmltext = "cows.htm";
							}
							else
							{
								switch (chance)
								{
									case 1:
										st.set("chance", "2");
										break;
									case 2:
										st.set("chance", "3");
										break;
									case 3:
										st.set("chance", "4");
										break;
									case 4:
										st.set("chance", "5");
										break;
									case 5:
										st.set("chance", "6");
										break;
									default:
										st.set("chance", "1");
										break;
								}
								player.sendMessage("Do this more ...");
								htmltext = "cows.htm";
							}
						}
						else
						{
							st.set("chance", "0");
							player.sendMessage("This hurt me ... help");
							htmltext = "";
							npc.deleteMe();
							L2Attackable newNpc = (L2Attackable) st.addSpawn(HEAD_BULK, 60000);
							L2Character originalAttacker = player;
							newNpc.setRunning();
							newNpc.addDamageHate(originalAttacker, 0, 500);
							newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalAttacker);
						}
					}
				}
				//	Gloom Milk Cow - finished
				if (npc.getNpcId() == GLOOM_MILK_COW)
				{
					if (event.equalsIgnoreCase("feed"))
					{
						if (chance >= 0 && chance < 6)
						{
							switch (chance)
							{
								case 1:
									st.set("chance", "2");
									break;
								case 2:
									st.set("chance", "3");
									break;
								case 3:
									st.set("chance", "4");
									break;
								case 4:
									st.set("chance", "5");
									break;
								case 5:
									st.set("chance", "6");
									break;
								default:
									st.set("chance", "1");
									break;
							}
							if (Rnd.get(1, 10) > 9)
							{
								st.set("chance", "0");
								player.sendMessage("I eat too much .... prepare for fight");
								htmltext = "";
								npc.deleteMe();
								L2Attackable newNpc = (L2Attackable) st.addSpawn(BULK, 60000);
								L2Character originalAttacker = player;
								newNpc.setRunning();
								newNpc.addDamageHate(originalAttacker, 0, 500);
								newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalAttacker);
							}
							else
							{
								player.sendMessage("Myam ... myam ...");
								htmltext = "cows.htm";
							}
						}
						else
						{
							if (Rnd.get(1, 10) > 6)
							{
								st.set("chance", "0");
								player.sendMessage("I eat too much .... prepare for fight");
								htmltext = "";
								npc.deleteMe();
								L2Attackable newNpc = (L2Attackable) st.addSpawn(HEAD_BULK, 60000);
								L2Character originalAttacker = player;
								newNpc.setRunning();
								newNpc.addDamageHate(originalAttacker, 0, 500);
								newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalAttacker);
							}
							else
							{
								player.sendMessage("Myam ... myam ...");
								htmltext = "cows.htm";
							}
						}
					}
					if (event.equalsIgnoreCase("milking"))
					{
						if (chance < 6)
						{
							if (Rnd.get(1, 4) > 3)
							{
								player.sendMessage("Ohhh this is nice.");
								st.giveItems(HC_MILK, 1);
								htmltext = "cows.htm";
							}
							else
							{
								switch (chance)
								{
									case 1:
										st.set("chance", "2");
										break;
									case 2:
										st.set("chance", "3");
										break;
									case 3:
										st.set("chance", "4");
										break;
									case 4:
										st.set("chance", "5");
										break;
									case 5:
										st.set("chance", "6");
										break;
									default:
										st.set("chance", "1");
										break;
								}
								player.sendMessage("Do this more ...");
								htmltext = "cows.htm";
							}
						}
						else
						{
							st.set("chance", "0");
							player.sendMessage("This hurt me ... help");
							htmltext = "";
							npc.deleteMe();
							L2Attackable newNpc = (L2Attackable) st.addSpawn(HEAD_BULK, 60000);
							L2Character originalAttacker = player;
							newNpc.setRunning();
							newNpc.addDamageHate(originalAttacker, 0, 500);
							newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalAttacker);
						}
					}
				}
				//	Gloom Head Milk Cow
				if (npc.getNpcId() == GLOOM_HEAD_MILK_COW)
				{
					if (event.equalsIgnoreCase("feed"))
					{
						if (chance >= 0 && chance < 6)
						{
							switch (chance)
							{
								case 1:
									st.set("chance", "2");
									break;
								case 2:
									st.set("chance", "3");
									break;
								case 3:
									st.set("chance", "4");
									break;
								case 4:
									st.set("chance", "5");
									break;
								case 5:
									st.set("chance", "6");
									break;
								default:
									st.set("chance", "1");
									break;
							}
							if (Rnd.get(1, 10) > 9)
							{
								st.set("chance", "0");
								player.sendMessage("I eat too much .... prepare for fight");
								htmltext = "";
								npc.deleteMe();
								L2Attackable newNpc = (L2Attackable) st.addSpawn(HEAD_BULK, 60000);
								L2Character originalAttacker = player;
								newNpc.setRunning();
								newNpc.addDamageHate(originalAttacker, 0, 500);
								newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalAttacker);
							}
							else
							{
								player.sendMessage("Myam ... myam ...");
								htmltext = "cows.htm";
							}
						}
						else
						{
							if (Rnd.get(1, 10) > 6)
							{
								st.set("chance", "0");
								player.sendMessage("I eat too much .... prepare for fight");
								htmltext = "";
								npc.deleteMe();
								L2Attackable newNpc = (L2Attackable) st.addSpawn(HEAD_BULK, 60000);
								L2Character originalAttacker = player;
								newNpc.setRunning();
								newNpc.addDamageHate(originalAttacker, 0, 500);
								newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalAttacker);
							}
							else
							{
								player.sendMessage("Myam ... myam ...");
								htmltext = "cows.htm";
							}
						}
					}
					if (event.equalsIgnoreCase("milking"))
					{
						if (chance < 6)
						{
							if (Rnd.get(1, 4) > 3)
							{
								player.sendMessage("Ohhh this is nice.");
								st.giveItems(HC_MILK, 1);
								htmltext = "cows.htm";
							}
							else
							{
								switch (chance)
								{
									case 1:
										st.set("chance", "2");
										break;
									case 2:
										st.set("chance", "3");
										break;
									case 3:
										st.set("chance", "4");
										break;
									case 4:
										st.set("chance", "5");
										break;
									case 5:
										st.set("chance", "6");
										break;
									default:
										st.set("chance", "1");
										break;
								}
								player.sendMessage("Do this more ...");
								htmltext = "cows.htm";
							}
						}
						else
						{
							st.set("chance", "0");
							player.sendMessage("This hurt me ... help");
							htmltext = "";
							npc.deleteMe();
							L2Attackable newNpc = (L2Attackable) st.addSpawn(HEAD_BULK, 60000);
							L2Character originalAttacker = player;
							newNpc.setRunning();
							newNpc.addDamageHate(originalAttacker, 0, 500);
							newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalAttacker);
						}
					}
				}
			}
		}
		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(QN);
		if (st.getState() != State.STARTED)
		{
			return super.onKill(npc, player, isPet);
		}
		if (!HC_STARTED)
		{
			// nothing to do ...
		}
		else
		{
			//BULK		
			if (npc.getNpcId() == BULK)
			{
				int random_prize = Rnd.get(1, 10);
				switch (random_prize)
				{
					case 1:
					case 2:
						st.giveItems(HC_ADENA, Rnd.get(1500, 5000));
						break;
					case 3:
					case 4:
					case 5:
						if (Rnd.get(10) > 5)
							st.giveItems(HC_MILK_COW_SCROLL, 1);
						break;
					case 6:
					case 7:
					case 8:
						if (Rnd.get(10) > 5)
							st.giveItems(HC_HEAD_MILK_COW_SCROLL, 1);
						break;
					case 9:
						if (Rnd.get(10) > 5)
							st.giveItems(HC_GLOOM_MILK_COW_SCROLL, 1);
						break;
					case 10:
						if (Rnd.get(10) > 5)
							st.giveItems(HC_GLOOM_HEAD_MILK_COW_SCROLL, 1);
						break;
					default:
						break;
				}
				npc.deleteMe();
			}
			//Head BULK
			if (npc.getNpcId() == HEAD_BULK)
			{
				int random_prize = Rnd.get(1, 10);
				switch (random_prize)
				{
					case 1:
					case 2:
						st.giveItems(HC_ADENA, Rnd.get(4500, 15000));
						break;
					case 3:
					case 4:
					case 5:
						if (Rnd.get(10) > 5)
							st.giveItems(HC_MILK_COW_SCROLL, 2);
						break;
					case 6:
					case 7:
					case 8:
						if (Rnd.get(10) > 5)
							st.giveItems(HC_HEAD_MILK_COW_SCROLL, 2);
						break;
					case 9:
						if (Rnd.get(10) > 5)
							st.giveItems(HC_GLOOM_MILK_COW_SCROLL, 1);
						break;
					case 10:
						if (Rnd.get(10) > 5)
							st.giveItems(HC_GLOOM_HEAD_MILK_COW_SCROLL, 1);
						break;
					default:
						break;
				}
				npc.deleteMe();
			}
		}
		return super.onKill(npc, player, isPet);
	}

	public HollyCow(int questId, String name, String descr)
	{
		super(questId, name, descr);

		//  Farmer Npc
		addStartNpc(FARMER);
		addFirstTalkId(FARMER);
		addTalkId(FARMER);

		//Npc's for eat
		addStartNpc(MILK_COW);
		addFirstTalkId(MILK_COW);
		addTalkId(MILK_COW);
		addStartNpc(HEAD_MILK_COW);
		addFirstTalkId(HEAD_MILK_COW);
		addTalkId(HEAD_MILK_COW);
		addStartNpc(GLOOM_MILK_COW);
		addFirstTalkId(GLOOM_MILK_COW);
		addTalkId(GLOOM_MILK_COW);
		addStartNpc(GLOOM_HEAD_MILK_COW);
		addFirstTalkId(GLOOM_HEAD_MILK_COW);
		addTalkId(GLOOM_HEAD_MILK_COW);

		// Mob kill
		addKillId(BULK);
		addKillId(HEAD_BULK);
	}

	public static void main(String[] args)
	{
		if (Config.ALLOW_HOLLY_COW)
		{
			new HollyCow(-1, QN, "official_events");
			_log.info("Official Events: Holly Cow is loaded.");
		}
		else
			_log.info("Official Events: Holly Cow is disabled.");
	}
}
