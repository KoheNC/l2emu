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
package official_events.FreyaCelebration;

import org.apache.commons.lang.ArrayUtils;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.instancemanager.QuestManager;
import net.l2emuproject.gameserver.model.quest.Quest;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.CreatureSay;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.tools.random.Rnd;

/**
 ** @author Gnacik
 **
 ** Retail Event : 'Freya Celebration'
 */
public final class FreyaCelebration extends QuestJython
{
	private static final int		FREYA			= 13296;
	private static final int		FREYA_POTION	= 15440;
	private static final int		FREYA_GIFT		= 17138;
	private static final int		HOURS			= 20;

	private static final int[]		SKILLS			=
													{ 9150, 9151, 9152, 9153, 9154, 9155, 9156 };

	private static final String[]	FREYA_TEXTS		=
													{
			"It has been so long since I have felt this... I almost miss it.",
			"I have no idea what I'm feeling right now.  Are all human emotions like this?",
			"You humans bring me such nonsense... a gift.  I have no need of such things.",
			"I would 'appreciate' this, however it has been far too long since I have felt appreciation for anything.",
			" I am Freya the Ice Queen!  Feelings and emotions of Felicia are nothing but memories to me." };

	private static final int[][]	SPAWNS			=
													{
													{ -119494, 44882, 360, 24576 },
													{ -117239, 46842, 360, 49151 },
													{ -84023, 243051, -3728, 4096 },
													{ -84411, 244813, -3728, 57343 },
													{ 46908, 50856, -2992, 8192 },
													{ 45538, 48357, -3056, 18000 },
													{ -45372, -114104, -240, 16384 },
													{ -45278, -112766, -240, 0 },
													{ 9929, 16324, -4568, 62999 },
													{ 11546, 17599, -4584, 46900 },
													{ 115096, -178370, -880, 0 },
													{ -13727, 122117, -2984, 16384 },
													{ -14129, 123869, -3112, 40959 },
													{ -83156, 150994, -3120, 0 },
													{ -81031, 150038, -3040, 0 },
													{ 16111, 142850, -2696, 16000 },
													{ 17275, 145000, -3032, 25000 },
													{ 111004, 218928, -3536, 16384 },
													{ 81755, 146487, -3528, 32768 },
													{ 82145, 148609, -3464, 0 },
													{ 83037, 149324, -3464, 44000 },
													{ 81987, 53723, -1488, 0 },
													{ 147200, 25614, -2008, 16384 },
													{ 148557, 26806, -2200, 32768 },
													{ 147421, -55435, -2728, 49151 },
													{ 148206, -55786, -2776, 61439 },
													{ 85584, -142490, -1336, 0 },
													{ 86865, -142915, -1336, 26000 },
													{ 43966, -47709, -792, 49999 },
													{ 43165, -48461, -792, 17000 } };

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		QuestState st = player.getQuestState(getName());
		Quest q = QuestManager.getInstance().getQuest(getName());
		if (st == null || q == null)
			return null;

		if (event.equalsIgnoreCase("give_potion"))
		{
			if (st.getQuestItemsCount(57) > 1)
			{
				long _curr_time = System.currentTimeMillis();
				String value = q.loadGlobalQuestVar(player.getAccountName());
				long _reuse_time = value == "" ? 0 : Long.parseLong(value);

				if (_curr_time > _reuse_time)
				{
					st.setState(State.STARTED);
					st.takeItems(57, 1);
					st.giveItems(FREYA_POTION, 1);
					q.saveGlobalQuestVar(player.getAccountName(), Long.toString(System.currentTimeMillis() + (HOURS * 3600000)));
				}
				else
				{
					long remainingTime = (_reuse_time - System.currentTimeMillis()) / 1000;
					int hours = (int) (remainingTime / 3600);
					int minutes = (int) ((remainingTime % 3600) / 60);
					SystemMessage sm = new SystemMessage(SystemMessageId.AVAILABLE_AFTER_S1_S2_HOURS_S3_MINUTES);
					sm.addItemName(FREYA_POTION);
					sm.addNumber(hours);
					sm.addNumber(minutes);
					player.sendPacket(sm);
				}
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S2_UNITS_OF_S1_REQUIRED);
				sm.addItemName(57);
				sm.addNumber(1);
				player.sendPacket(sm);
			}
		}
		return null;
	}

	@Override
	public final String onSkillSee(L2Npc npc, L2Player caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if ((caster == null) || (npc == null))
			return null;

		if ((npc.getNpcId() == FREYA) && ArrayUtils.contains(targets, npc) && ArrayUtils.contains(SKILLS, skill.getId()))
		{
			if (Rnd.get(100) < 5)
			{
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Normal, npc.getName(), "Dear " + caster.getName()
						+ "... I want to express my appreciation for the gift. Take this with you. Why are you shocked? I'm a very generous person."));
				caster.addItem("FreyaCelebration", FREYA_GIFT, 1, npc, true);
			}
			else
			{
				if (Rnd.get(10) < 2)
				{
					npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Normal, npc.getName(), FREYA_TEXTS[Rnd
							.get(FREYA_TEXTS.length - 1)]));
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2Player player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		return "13296.htm";
	}

	public FreyaCelebration(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(FREYA);
		addFirstTalkId(FREYA);
		addTalkId(FREYA);
		addSkillSeeId(FREYA);
		for (int[] _spawn : SPAWNS)
		{
			addSpawn(FREYA, _spawn[0], _spawn[1], _spawn[2], _spawn[3], false, 0);
		}
	}

	public static void main(String[] args)
	{
		if (Config.ALLOW_FREYA_CELEBRATION)
		{
			new FreyaCelebration(-1, "FreyaCelebration", "official_events");
			_log.info("Official Events: Freya Celebration is loaded.");
		}
		else
			_log.info("Official Events: Freya Celebration is disabled.");
	}
}
