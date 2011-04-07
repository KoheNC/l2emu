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

/*
 * @author lewzer
 * work in progress
 */
package instances.SanctumOftheLordsOfDawn;

import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.manager.instances.InstanceManager;
import net.l2emuproject.gameserver.manager.instances.InstanceManager.InstanceWorld;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.NpcSay;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2DoorInstance;
import net.l2emuproject.gameserver.world.object.position.L2CharPosition;
import ai.L2NpcAIScript;

public final class SanctumOftheLordsOfDawn extends L2NpcAIScript
{
	private final class HSWorld extends InstanceWorld
	{
		private long[]	storeTime	=
									{ 0, 0 };
		private L2Npc	NPC_1, NPC_2, NPC_3, NPC_4, NPC_5, NPC_6, NPC_7, NPC_8, NPC_9, NPC_10, NPC_11, NPC_12, NPC_13, NPC_14, NPC_15, NPC_16, NPC_17, NPC_18,
				NPC_19, NPC_20, NPC_21, NPC_22, NPC_23, NPC_24, NPC_25, NPC_26, NPC_27, NPC_28, NPC_29, NPC_30, NPC_31, NPC_32, NPC_33, S_C_NPC_1, S_C_NPC_2,
				S_C_NPC_3, S_C_NPC_4, S_C_NPC_5, S_C_NPC_6;

		public HSWorld()
		{
			InstanceManager.getInstance().super();
		}
	}

	private static final String			QN				= "SanctumOftheLordsOfDawn";
	private static final int			INSTANCEID		= 111;

	// NPCs
	private static final int			LIGHTOFDAWN		= 32575;
	private static final int			DEVICE			= 32578;
	private static final int			DEVICE2			= 36600;
	private static final int			PWDEVICE		= 32577;
	private static final int			BOOKCASE		= 32580;
	private static final int			ONE				= 17240001;
	private static final int			TWO				= 17240003;
	private static final int			THREE			= 17240005;
	private static final int			BLACK			= 32579;
	private static final int			MGUARD			= 27348;
	private static final int			SPRIEST_F		= 27351;
	private static final int			SPRIEST_F_2		= 27352;
	private static final int			SPRIEST_F_3		= 36601;
	private static final int			WPRIEST			= 27349;
	private static final int			WPRIEST_2		= 27350;
	private static final int			PRIESTS			= 18828;
	private static final int			GUARD_FIRST		= 27347;
	private static final int			GUARD_LAST		= 36602;

	//TRANSFORM SKILL
	private static final int			GUARD_AMBUSH	= 963;

	// NPC TASK VARS
	private static final int			RADIUS			= 150;
	private static final int			TIME			= 1000;

	// WALK TIMERS
	private static final int			SHORT			= 3500;
	private static final int			MID				= 7000;
	private static final int			MID2			= 7500;
	private static final int			LONG			= 14000;
	private static final int			HUGE			= 25000;

	// MOVE PATHS
	private static final L2CharPosition	MOVE_TO_1_A		= new L2CharPosition(-75022, 212090, -7317, 0);
	private static final L2CharPosition	MOVE_TO_1_B		= new L2CharPosition(-74876, 212091, -7317, 0);
	private static final L2CharPosition	MOVE_TO_2_A		= new L2CharPosition(-75334, 212109, -7317, 0);
	private static final L2CharPosition	MOVE_TO_2_B		= new L2CharPosition(-75661, 212109, -7319, 0);
	private static final L2CharPosition	MOVE_TO_3_A		= new L2CharPosition(-74205, 212102, -7319, 0);
	private static final L2CharPosition	MOVE_TO_3_B		= new L2CharPosition(-74576, 212102, -7319, 0);
	private static final L2CharPosition	MOVE_TO_4_A		= new L2CharPosition(-75228, 211458, -7317, 0);
	private static final L2CharPosition	MOVE_TO_4_B		= new L2CharPosition(-75233, 211125, -7319, 0);
	private static final L2CharPosition	MOVE_TO_5_A		= new L2CharPosition(-74673, 211129, -7321, 0);
	private static final L2CharPosition	MOVE_TO_5_B		= new L2CharPosition(-74686, 211494, -7321, 0);
	private static final L2CharPosition	MOVE_TO_6_A		= new L2CharPosition(-75230, 210171, -7415, 0);
	private static final L2CharPosition	MOVE_TO_6_B		= new L2CharPosition(-74689, 210157, -7418, 0);
	private static final L2CharPosition	MOVE_TO_7_A		= new L2CharPosition(-74685, 209824, -7415, 0);
	private static final L2CharPosition	MOVE_TO_7_B		= new L2CharPosition(-75215, 209817, -7415, 0);
	private static final L2CharPosition	MOVE_TO_8_A		= new L2CharPosition(-75545, 207553, -7511, 0);
	private static final L2CharPosition	MOVE_TO_8_B		= new L2CharPosition(-75558, 208834, -7514, 0);
	private static final L2CharPosition	MOVE_TO_9_A		= new L2CharPosition(-75412, 207137, -7511, 0);
	private static final L2CharPosition	MOVE_TO_9_B		= new L2CharPosition(-75691, 207140, -7511, 0);
	private static final L2CharPosition	MOVE_TO_10_A	= new L2CharPosition(-74512, 208266, -7511, 0);
	private static final L2CharPosition	MOVE_TO_10_B	= new L2CharPosition(-74197, 208271, -7511, 0);
	private static final L2CharPosition	MOVE_TO_11_A	= new L2CharPosition(-74515, 207060, -7509, 0);
	private static final L2CharPosition	MOVE_TO_11_B	= new L2CharPosition(-74196, 207061, -7509, 0);
	private static final L2CharPosition	MOVE_TO_12_A	= new L2CharPosition(-74263, 206487, -7511, 0);
	private static final L2CharPosition	MOVE_TO_12_B	= new L2CharPosition(-75703, 206491, -7511, 0);
	private static final L2CharPosition	MOVE_TO_13_A	= new L2CharPosition(-76402, 207958, -7607, 0);
	private static final L2CharPosition	MOVE_TO_13_B	= new L2CharPosition(-76612, 207962, -7607, 0);
	private static final L2CharPosition	MOVE_TO_14_A	= new L2CharPosition(-76374, 208206, -7606, 0);
	private static final L2CharPosition	MOVE_TO_14_B	= new L2CharPosition(-76632, 208205, -7606, 0);
	private static final L2CharPosition	MOVE_TO_15_A	= new L2CharPosition(-76371, 208853, -7606, 0);
	private static final L2CharPosition	MOVE_TO_15_B	= new L2CharPosition(-76638, 208854, -7606, 0);
	private static final L2CharPosition	MOVE_TO_16_A	= new L2CharPosition(-76893, 209445, -7606, 0);
	private static final L2CharPosition	MOVE_TO_16_B	= new L2CharPosition(-76894, 209199, -7606, 0);
	private static final L2CharPosition	MOVE_TO_17_A	= new L2CharPosition(-77276, 209436, -7607, 0);
	private static final L2CharPosition	MOVE_TO_17_B	= new L2CharPosition(-77280, 209197, -7607, 0);
	private static final L2CharPosition	MOVE_TO_18_A	= new L2CharPosition(-78033, 208406, -7706, 0);
	private static final L2CharPosition	MOVE_TO_18_B	= new L2CharPosition(-77380, 208406, -7704, 0);
	private static final L2CharPosition	MOVE_TO_19_A	= new L2CharPosition(-77691, 208131, -7704, 0);
	private static final L2CharPosition	MOVE_TO_19_B	= new L2CharPosition(-77702, 207454, -7678, 0);
	private static final L2CharPosition	MOVE_TO_20_A	= new L2CharPosition(-78102, 208037, -7701, 0);
	private static final L2CharPosition	MOVE_TO_20_B	= new L2CharPosition(-78453, 208037, -7703, 0);
	private static final L2CharPosition	MOVE_TO_21_A	= new L2CharPosition(-77287, 208041, -7701, 0);
	private static final L2CharPosition	MOVE_TO_21_B	= new L2CharPosition(-76955, 208030, -7703, 0);
	private static final L2CharPosition	MOVE_TO_22_A	= new L2CharPosition(-78925, 206091, -7893, 0);
	private static final L2CharPosition	MOVE_TO_22_B	= new L2CharPosition(-78713, 206295, -7893, 0);
	private static final L2CharPosition	MOVE_TO_23_A	= new L2CharPosition(-79361, 206329, -7893, 0);
	private static final L2CharPosition	MOVE_TO_23_B	= new L2CharPosition(-79355, 206670, -7893, 0);
	private static final L2CharPosition	MOVE_TO_24_A	= new L2CharPosition(-79078, 206234, -7893, 0);
	private static final L2CharPosition	MOVE_TO_24_B	= new L2CharPosition(-78866, 206446, -7893, 0);
	private static final L2CharPosition	MOVE_TO_25_A	= new L2CharPosition(-79646, 206245, -7893, 0);
	private static final L2CharPosition	MOVE_TO_25_B	= new L2CharPosition(-79839, 206452, -7893, 0);
	private static final L2CharPosition	MOVE_TO_26_A	= new L2CharPosition(-79789, 206100, -7893, 0);
	private static final L2CharPosition	MOVE_TO_26_B	= new L2CharPosition(-79993, 206309, -7893, 0);
	private static final L2CharPosition	MOVE_TO_27_A	= new L2CharPosition(-79782, 205610, -7893, 0);
	private static final L2CharPosition	MOVE_TO_27_B	= new L2CharPosition(-79993, 205402, -7893, 0);
	private static final L2CharPosition	MOVE_TO_28_A	= new L2CharPosition(-79657, 205469, -7893, 0);
	private static final L2CharPosition	MOVE_TO_28_B	= new L2CharPosition(-79862, 205266, -7893, 0);
	private static final L2CharPosition	MOVE_TO_29_A	= new L2CharPosition(-79362, 205383, -7893, 0);
	private static final L2CharPosition	MOVE_TO_29_B	= new L2CharPosition(-79361, 204984, -7893, 0);
	private static final L2CharPosition	MOVE_TO_30_A	= new L2CharPosition(-78984, 205568, -7893, 0);
	private static final L2CharPosition	MOVE_TO_30_B	= new L2CharPosition(-78769, 205351, -7893, 0);
	private static final L2CharPosition	MOVE_TO_31_A	= new L2CharPosition(-79118, 205436, -7893, 0);
	private static final L2CharPosition	MOVE_TO_31_B	= new L2CharPosition(-78905, 205223, -7893, 0);
	private static final L2CharPosition	MOVE_TO_32_A	= new L2CharPosition(-81948, 205857, -7989, 0);
	private static final L2CharPosition	MOVE_TO_32_B	= new L2CharPosition(-81350, 205857, -7989, 0);
	private static final L2CharPosition	MOVE_TO_33_A	= new L2CharPosition(-74948, 206370, -7514, 0);
	private static final L2CharPosition	MOVE_TO_33_B	= new L2CharPosition(-74950, 206681, -7514, 0);

	public SanctumOftheLordsOfDawn(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(LIGHTOFDAWN);
		addTalkId(LIGHTOFDAWN);
		addTalkId(DEVICE);
		addTalkId(DEVICE2);
		addTalkId(PWDEVICE);
		addTalkId(BLACK);
	}

	private final void teleportPlayer(L2Player player, int[] coords, int instanceId)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2]);
	}

	private final void exitInstance(L2Player player)
	{
		player.setInstanceId(0);
		player.teleToLocation(-12585, 122305, -2989);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		if (event.equalsIgnoreCase("Group_SHORT_B"))
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof HSWorld)
			{
				HSWorld world = (HSWorld) tmpworld;
				world.NPC_1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_1_B);
				startQuestTimer("Group_SHORT_A", SHORT, world.NPC_1, null);
			}
		}
		else if (event.equalsIgnoreCase("Group_SHORT_A"))
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof HSWorld)
			{
				HSWorld world = (HSWorld) tmpworld;
				world.NPC_1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_1_A);
				startQuestTimer("Group_SHORT_B", SHORT, world.NPC_1, null);
			}
		}
		if (event.equalsIgnoreCase("Group_MID_B"))
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof HSWorld)
			{
				HSWorld world = (HSWorld) tmpworld;
				world.NPC_2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_2_B);
				world.NPC_3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_3_B);
				world.NPC_4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_4_B);
				world.NPC_5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_5_B);
				world.NPC_9.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_9_B);
				world.NPC_10.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_10_B);
				world.NPC_11.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_11_B);
				world.NPC_13.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_13_B);
				world.NPC_15.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_15_B);
				world.NPC_16.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_16_B);
				world.NPC_20.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_20_B);
				world.NPC_21.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_21_B);
				world.NPC_22.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_22_B);
				world.NPC_23.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_23_B);
				world.NPC_26.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_26_B);
				world.NPC_27.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_27_B);
				world.NPC_29.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_29_B);
				world.NPC_30.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_30_B);
				world.NPC_32.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_32_B);
				world.NPC_33.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_33_B);
				startQuestTimer("Group_MID_A", MID, world.NPC_2, null);
			}
		}
		else if (event.equalsIgnoreCase("Group_MID_A"))
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof HSWorld)
			{
				HSWorld world = (HSWorld) tmpworld;
				world.NPC_2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_2_A);
				world.NPC_3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_3_A);
				world.NPC_4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_4_A);
				world.NPC_5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_5_A);
				world.NPC_9.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_9_A);
				world.NPC_10.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_10_A);
				world.NPC_11.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_11_A);
				world.NPC_13.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_13_A);
				world.NPC_15.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_15_A);
				world.NPC_16.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_16_A);
				world.NPC_20.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_20_A);
				world.NPC_21.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_21_A);
				world.NPC_22.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_22_A);
				world.NPC_23.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_23_A);
				world.NPC_26.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_26_A);
				world.NPC_27.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_27_A);
				world.NPC_29.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_29_A);
				world.NPC_30.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_30_A);
				world.NPC_32.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_32_A);
				world.NPC_33.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_33_A);
				startQuestTimer("Group_MID_B", MID, world.NPC_2, null);
			}
		}
		if (event.equalsIgnoreCase("Group_MID2_B"))
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof HSWorld)
			{
				HSWorld world = (HSWorld) tmpworld;
				world.NPC_14.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_14_B);
				world.NPC_17.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_17_B);
				world.NPC_24.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_24_B);
				world.NPC_25.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_25_B);
				world.NPC_28.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_28_B);
				world.NPC_31.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_31_B);
				startQuestTimer("Group_MID2_A", MID2, world.NPC_14, null);
			}
		}
		else if (event.equalsIgnoreCase("Group_MID2_A"))
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof HSWorld)
			{
				HSWorld world = (HSWorld) tmpworld;
				world.NPC_14.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_14_A);
				world.NPC_17.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_17_A);
				world.NPC_24.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_24_A);
				world.NPC_25.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_25_A);
				world.NPC_28.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_28_A);
				world.NPC_31.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_31_A);
				startQuestTimer("Group_MID2_B", MID2, world.NPC_14, null);
			}
		}
		if (event.equalsIgnoreCase("Group_LONG_B"))
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof HSWorld)
			{
				HSWorld world = (HSWorld) tmpworld;
				world.NPC_6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_6_B);
				world.NPC_7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_7_B);
				world.NPC_18.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_18_B);
				world.NPC_19.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_19_B);
				startQuestTimer("Group_LONG_A", LONG, world.NPC_6, null);
			}
		}
		else if (event.equalsIgnoreCase("Group_LONG_A"))
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof HSWorld)
			{
				HSWorld world = (HSWorld) tmpworld;
				world.NPC_6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_6_A);
				world.NPC_7.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_7_A);
				world.NPC_18.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_18_A);
				world.NPC_19.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_19_A);
				startQuestTimer("Group_LONG_B", LONG, world.NPC_6, null);
			}
		}
		if (event.equalsIgnoreCase("Group_HUGE_A"))
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof HSWorld)
			{
				HSWorld world = (HSWorld) tmpworld;
				world.NPC_12.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_12_A);
				world.NPC_8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_8_A);
				startQuestTimer("Group_HUGE_B", HUGE, world.NPC_12, null);
			}
		}
		else if (event.equalsIgnoreCase("Group_HUGE_B"))
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof HSWorld)
			{
				HSWorld world = (HSWorld) tmpworld;
				world.NPC_12.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_12_B);
				world.NPC_8.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_8_B);
				startQuestTimer("Group_HUGE_A", HUGE, world.NPC_12, null);
			}
		}
		return "";
	}

	private final int enterInstance(L2Player player)
	{
		int instanceId = 0;
		final String template = "SanctumoftheLordsofDawn.xml";
		final int[] coords =
		{ -75988, 213414, -7119 };
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (!(world instanceof HSWorld))
			{
				player.sendPacket(new SystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			teleportPlayer(player, coords, instanceId);
			return instanceId;
		}
		else
		{
			instanceId = InstanceManager.getInstance().createDynamicInstance(template);
			world = new HSWorld();
			world.instanceId = instanceId;
			world.templateId = INSTANCEID;
			world.status = 0;
			((HSWorld) world).storeTime[0] = System.currentTimeMillis();
			InstanceManager.getInstance().addWorld(world);
			spawnState((HSWorld) world);
			_log.info("SevenSigns 4th quest started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
			teleportPlayer(player, coords, instanceId);
			world.allowed.add(player.getObjectId());
			return instanceId;
		}
	}

	private final void spawnState(HSWorld world)
	{
		// STATIC NPC's
		L2Npc S_NPC_0 = addSpawn(DEVICE, -75710, 213535, -7126, 0, false, 0, false, world.instanceId);
		S_NPC_0.setIsNoRndWalk(true);
		L2Npc S_NPC_1 = addSpawn(DEVICE2, -78355, 205740, -7892, 0, false, 0, false, world.instanceId);
		S_NPC_1.setIsNoRndWalk(true);
		L2Npc S_NPC_2 = addSpawn(PWDEVICE, -80133, 205743, -7888, 0, false, 0, false, world.instanceId);
		S_NPC_2.setIsNoRndWalk(true);
		L2Npc S_NPC_3 = addSpawn(BOOKCASE, -81386, 205562, -7992, 0, false, 0, false, world.instanceId);
		S_NPC_3.setIsNoRndWalk(true);
		L2Npc S_NPC_4 = addSpawn(BLACK, -76003, 213413, -7124, 0, false, 0, false, world.instanceId);
		S_NPC_4.setIsNoRndWalk(true);
		L2Npc S_NPC_5 = addSpawn(GUARD_FIRST, -74948, 213468, -7218, 0, false, 0, false, world.instanceId);
		S_NPC_5.setIsNoRndWalk(true);
		L2Npc S_NPC_7 = addSpawn(WPRIEST, -74951, 211621, -7317, 0, false, 0, false, world.instanceId);
		S_NPC_7.setIsNoRndWalk(true);
		L2Npc S_NPC_8 = addSpawn(SPRIEST_F, -75329, 209990, -7392, 0, false, 0, false, world.instanceId);
		S_NPC_8.setIsNoRndWalk(true);
		L2Npc S_NPC_9 = addSpawn(SPRIEST_F, -74568, 209981, -7390, 0, false, 0, false, world.instanceId);
		S_NPC_9.setIsNoRndWalk(true);
		L2Npc S_NPC_10 = addSpawn(SPRIEST_F_2, -75638, 208763, -7486, 0, false, 0, false, world.instanceId);
		S_NPC_10.setIsNoRndWalk(true);
		L2Npc S_NPC_11 = addSpawn(SPRIEST_F_2, -74276, 208794, -7486, 0, false, 0, false, world.instanceId);
		S_NPC_11.setIsNoRndWalk(true);
		L2Npc S_NPC_12 = addSpawn(MGUARD, -74959, 207618, -7486, 0, false, 0, false, world.instanceId);
		S_NPC_12.setIsNoRndWalk(true);
		L2Npc S_NPC_13 = addSpawn(SPRIEST_F_3, -77701, 208305, -7701, 0, false, 0, false, world.instanceId);
		S_NPC_13.setIsNoRndWalk(true);
		L2Npc S_NPC_14 = addSpawn(SPRIEST_F_3, -77702, 207286, -7704, 0, false, 0, false, world.instanceId);
		S_NPC_14.setIsNoRndWalk(true);
		L2Npc S_NPC_15 = addSpawn(WPRIEST_2, -78354, 207117, -7703, 0, false, 0, false, world.instanceId);
		S_NPC_15.setIsNoRndWalk(true);
		L2Npc S_NPC_16 = addSpawn(WPRIEST_2, -78108, 207388, -7701, 0, false, 0, false, world.instanceId);
		S_NPC_16.setIsNoRndWalk(true);
		L2Npc S_NPC_17 = addSpawn(WPRIEST_2, -77290, 207381, -7701, 0, false, 0, false, world.instanceId);
		S_NPC_17.setIsNoRndWalk(true);
		L2Npc S_NPC_18 = addSpawn(WPRIEST_2, -77053, 207113, -7703, 0, false, 0, false, world.instanceId);
		S_NPC_18.setIsNoRndWalk(true);
		L2Npc S_NPC_19 = addSpawn(SPRIEST_F_3, -78878, 206292, -7894, 0, false, 0, false, world.instanceId);
		S_NPC_19.setIsNoRndWalk(true);
		L2Npc S_NPC_20 = addSpawn(SPRIEST_F_3, -79800, 206274, -7894, 0, false, 0, false, world.instanceId);
		S_NPC_20.setIsNoRndWalk(true);
		L2Npc S_NPC_21 = addSpawn(SPRIEST_F_3, -79809, 205446, -7894, 0, false, 0, false, world.instanceId);
		S_NPC_21.setIsNoRndWalk(true);
		L2Npc S_NPC_22 = addSpawn(SPRIEST_F_3, -78917, 205414, -7894, 0, false, 0, false, world.instanceId);
		S_NPC_22.setIsNoRndWalk(true);
		L2Npc S_NPC_23 = addSpawn(SPRIEST_F_2, -74575, 206628, -7511, 0, false, 0, false, world.instanceId);
		S_NPC_23.setIsNoRndWalk(true);
		L2Npc S_NPC_24 = addSpawn(SPRIEST_F_2, -75434, 206743, -7511, 0, false, 0, false, world.instanceId);
		S_NPC_24.setIsNoRndWalk(true);
		L2Npc S_NPC_25 = addSpawn(MGUARD, -75448, 208164, -7510, 0, false, 0, false, world.instanceId);
		S_NPC_25.setIsNoRndWalk(true);
		L2Npc S_NPC_26 = addSpawn(MGUARD, -75655, 208175, -7512, 0, false, 0, false, world.instanceId);
		S_NPC_26.setIsNoRndWalk(true);
		L2Npc S_NPC_27 = addSpawn(SPRIEST_F_3, -81531, 205455, -7989, 0, false, 0, false, world.instanceId);
		S_NPC_27.setIsNoRndWalk(true);
		L2Npc S_NPC_28 = addSpawn(SPRIEST_F_3, -81531, 206237, -7992, 0, false, 0, false, world.instanceId);
		S_NPC_28.setIsNoRndWalk(true);

		// STATIC NPCS IN CIRCLE
		world.S_C_NPC_1 = addSpawn(PRIESTS, -79361, 206020, -7903, 0, false, 0, false, world.instanceId);
		world.S_C_NPC_1.setIsNoRndWalk(true);
		world.S_C_NPC_2 = addSpawn(PRIESTS, -79501, 205936, -7905, 0, false, 0, false, world.instanceId);
		world.S_C_NPC_2.setIsNoRndWalk(true);
		world.S_C_NPC_3 = addSpawn(PRIESTS, -79500, 205774, -7909, 0, false, 0, false, world.instanceId);
		world.S_C_NPC_3.setIsNoRndWalk(true);
		world.S_C_NPC_4 = addSpawn(PRIESTS, -79359, 205696, -7905, 0, false, 0, false, world.instanceId);
		world.S_C_NPC_4.setIsNoRndWalk(true);
		world.S_C_NPC_5 = addSpawn(PRIESTS, -79213, 205770, -7903, 0, false, 0, false, world.instanceId);
		world.S_C_NPC_5.setIsNoRndWalk(true);
		world.S_C_NPC_6 = addSpawn(PRIESTS, -79214, 205940, -7903, 0, false, 0, false, world.instanceId);
		world.S_C_NPC_6.setIsNoRndWalk(true);

		// WALKING NPC's
		world.NPC_1 = addSpawn(WPRIEST, -75022, 212090, -7317, 0, false, 0, false, world.instanceId);
		world.NPC_2 = addSpawn(WPRIEST, -75334, 212109, -7317, 0, false, 0, false, world.instanceId);
		world.NPC_3 = addSpawn(WPRIEST, -74205, 212102, -7319, 0, false, 0, false, world.instanceId);
		world.NPC_4 = addSpawn(WPRIEST, -75228, 211458, -7319, 0, false, 0, false, world.instanceId);
		world.NPC_5 = addSpawn(WPRIEST, -74673, 211129, -7321, 0, false, 0, false, world.instanceId);
		world.NPC_6 = addSpawn(GUARD_FIRST, -75215, 210171, -7415, 0, false, 0, false, world.instanceId);
		world.NPC_7 = addSpawn(GUARD_FIRST, -74685, 209824, -7415, 0, false, 0, false, world.instanceId);
		world.NPC_8 = addSpawn(MGUARD, -75545, 207553, -7511, 0, false, 0, false, world.instanceId);
		world.NPC_9 = addSpawn(MGUARD, -75412, 207137, -7511, 0, false, 0, false, world.instanceId);
		world.NPC_10 = addSpawn(MGUARD, -74512, 208266, -7511, 0, false, 0, false, world.instanceId);
		world.NPC_11 = addSpawn(MGUARD, -74515, 207060, -7509, 0, false, 0, false, world.instanceId);
		world.NPC_12 = addSpawn(MGUARD, -74263, 206487, -7511, 0, false, 0, false, world.instanceId);
		world.NPC_13 = addSpawn(MGUARD, -76402, 207958, -7607, 0, false, 0, false, world.instanceId);
		world.NPC_14 = addSpawn(MGUARD, -76374, 208206, -7606, 0, false, 0, false, world.instanceId);
		world.NPC_15 = addSpawn(MGUARD, -76371, 208853, -7606, 0, false, 0, false, world.instanceId);
		world.NPC_16 = addSpawn(MGUARD, -76893, 209445, -7606, 0, false, 0, false, world.instanceId);
		world.NPC_17 = addSpawn(MGUARD, -77276, 209436, -7607, 0, false, 0, false, world.instanceId);
		world.NPC_18 = addSpawn(WPRIEST_2, -78033, 208406, -7706, 0, false, 0, false, world.instanceId);
		world.NPC_19 = addSpawn(WPRIEST_2, -77691, 208131, -7704, 0, false, 0, false, world.instanceId);
		world.NPC_20 = addSpawn(WPRIEST_2, -78102, 208037, -7701, 0, false, 0, false, world.instanceId);
		world.NPC_21 = addSpawn(WPRIEST_2, -77287, 208041, -7701, 0, false, 0, false, world.instanceId);
		world.NPC_22 = addSpawn(GUARD_LAST, -78925, 206091, -7893, 0, false, 0, false, world.instanceId);
		world.NPC_23 = addSpawn(GUARD_LAST, -79361, 206329, -7893, 0, false, 0, false, world.instanceId);
		world.NPC_24 = addSpawn(GUARD_LAST, -79078, 206234, -7893, 0, false, 0, false, world.instanceId);
		world.NPC_25 = addSpawn(GUARD_LAST, -79646, 206245, -7893, 0, false, 0, false, world.instanceId);
		world.NPC_26 = addSpawn(GUARD_LAST, -79789, 206100, -7893, 0, false, 0, false, world.instanceId);
		world.NPC_27 = addSpawn(GUARD_LAST, -79782, 205610, -7893, 0, false, 0, false, world.instanceId);
		world.NPC_28 = addSpawn(GUARD_LAST, -79657, 205469, -7893, 0, false, 0, false, world.instanceId);
		world.NPC_29 = addSpawn(GUARD_LAST, -79362, 205383, -7893, 0, false, 0, false, world.instanceId);
		world.NPC_30 = addSpawn(GUARD_LAST, -78984, 205568, -7893, 0, false, 0, false, world.instanceId);
		world.NPC_31 = addSpawn(GUARD_LAST, -79118, 205436, -7893, 0, false, 0, false, world.instanceId);
		world.NPC_32 = addSpawn(GUARD_LAST, -81948, 205857, -7989, 0, false, 0, false, world.instanceId);
		world.NPC_33 = addSpawn(MGUARD, -74948, 206370, -7514, 0, false, 0, false, world.instanceId);

		//NPC RANGE TASKS
		startNpcRangeTask(S_NPC_5, RADIUS, TIME);
		startNpcRangeTask(S_NPC_7, RADIUS, TIME);
		startNpcRangeTask(S_NPC_8, RADIUS, TIME);
		startNpcRangeTask(S_NPC_9, RADIUS, TIME);
		startNpcRangeTask(S_NPC_10, RADIUS, TIME);
		startNpcRangeTask(S_NPC_11, RADIUS, TIME);
		startNpcRangeTask(S_NPC_12, RADIUS, TIME);
		startNpcRangeTask(S_NPC_13, RADIUS, TIME);
		startNpcRangeTask(S_NPC_14, RADIUS, TIME);
		startNpcRangeTask(S_NPC_15, RADIUS, TIME);
		startNpcRangeTask(S_NPC_16, RADIUS, TIME);
		startNpcRangeTask(S_NPC_17, RADIUS, TIME);
		startNpcRangeTask(S_NPC_18, RADIUS, TIME);
		startNpcRangeTask(S_NPC_19, RADIUS, TIME);
		startNpcRangeTask(S_NPC_20, RADIUS, TIME);
		startNpcRangeTask(S_NPC_21, RADIUS, TIME);
		startNpcRangeTask(S_NPC_22, RADIUS, TIME);
		startNpcRangeTask(S_NPC_23, RADIUS, TIME);
		startNpcRangeTask(S_NPC_24, RADIUS, TIME);
		startNpcRangeTask(S_NPC_25, RADIUS, TIME);
		startNpcRangeTask(S_NPC_26, RADIUS, TIME);
		startNpcRangeTask(S_NPC_27, RADIUS, TIME);
		startNpcRangeTask(S_NPC_28, RADIUS, TIME);
		startNpcRangeTask(world.NPC_1, RADIUS, TIME);
		startNpcRangeTask(world.NPC_2, RADIUS, TIME);
		startNpcRangeTask(world.NPC_3, RADIUS, TIME);
		startNpcRangeTask(world.NPC_4, RADIUS, TIME);
		startNpcRangeTask(world.NPC_5, RADIUS, TIME);
		startNpcRangeTask(world.NPC_6, RADIUS, TIME);
		startNpcRangeTask(world.NPC_7, RADIUS, TIME);
		startNpcRangeTask(world.NPC_8, RADIUS, TIME);
		startNpcRangeTask(world.NPC_9, RADIUS, TIME);
		startNpcRangeTask(world.NPC_10, RADIUS, TIME);
		startNpcRangeTask(world.NPC_11, RADIUS, TIME);
		startNpcRangeTask(world.NPC_12, RADIUS, TIME);
		startNpcRangeTask(world.NPC_13, RADIUS, TIME);
		startNpcRangeTask(world.NPC_14, RADIUS, TIME);
		startNpcRangeTask(world.NPC_15, RADIUS, TIME);
		startNpcRangeTask(world.NPC_16, RADIUS, TIME);
		startNpcRangeTask(world.NPC_17, RADIUS, TIME);
		startNpcRangeTask(world.NPC_18, RADIUS, TIME);
		startNpcRangeTask(world.NPC_19, RADIUS, TIME);
		startNpcRangeTask(world.NPC_20, RADIUS, TIME);
		startNpcRangeTask(world.NPC_21, RADIUS, TIME);
		startNpcRangeTask(world.NPC_22, RADIUS, TIME);
		startNpcRangeTask(world.NPC_23, RADIUS, TIME);
		startNpcRangeTask(world.NPC_24, RADIUS, TIME);
		startNpcRangeTask(world.NPC_25, RADIUS, TIME);
		startNpcRangeTask(world.NPC_26, RADIUS, TIME);
		startNpcRangeTask(world.NPC_27, RADIUS, TIME);
		startNpcRangeTask(world.NPC_28, RADIUS, TIME);
		startNpcRangeTask(world.NPC_29, RADIUS, TIME);
		startNpcRangeTask(world.NPC_30, RADIUS, TIME);
		startNpcRangeTask(world.NPC_31, RADIUS, TIME);
		startNpcRangeTask(world.NPC_32, RADIUS, TIME);
		startNpcRangeTask(world.NPC_33, RADIUS, TIME);

		// START TIMERS
		startQuestTimer("Group_SHORT_B", SHORT, world.NPC_1, null);
		startQuestTimer("Group_MID_B", MID, world.NPC_2, null);
		startQuestTimer("Group_MID2_B", MID2, world.NPC_2, null);
		startQuestTimer("Group_LONG_B", LONG, world.NPC_6, null);
		startQuestTimer("Group_HUGE_B", HUGE, world.NPC_12, null);
	}

	private final void openDoor(int doorId, int instanceId)
	{
		for (L2DoorInstance door : InstanceManager.getInstance().getInstance(instanceId).getDoors())
			if (door.getDoorId() == doorId)
				door.openMe();
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
			st = newQuestState(player);

		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());

		switch (npc.getNpcId())
		{
			case LIGHTOFDAWN:
				enterInstance(player);
				break;
			case DEVICE:
				if (tmpworld instanceof HSWorld)
				{
					HSWorld world = (HSWorld) tmpworld;
					openDoor(ONE, world.instanceId);
				}
				break;
			case DEVICE2:
				if (tmpworld instanceof HSWorld)
				{
					HSWorld world = (HSWorld) tmpworld;
					openDoor(TWO, world.instanceId);
					player.showQuestMovie(11);
				}
				break;
			case PWDEVICE:
				if (tmpworld instanceof HSWorld)
				{
					HSWorld world = (HSWorld) tmpworld;
					openDoor(THREE, world.instanceId);
				}
				break;
			case BLACK:
				InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
				world.allowed.remove(player.getObjectId());
				exitInstance(player);
				break;
		}

		return "";
	}

	@Override
	public final String onNpcRangeEnter(L2Character character, L2Npc npc)
	{
		final L2Player player = character.getActingPlayer();
		if (character instanceof L2Player)
		{
			switch (npc.getNpcId())
			{
				case SPRIEST_F:
					if (player.getFirstEffect(GUARD_AMBUSH) == null)
					{
						npc.broadcastPacket(new NpcSay(npc, "This is restricted area! Go away!"));
						player.teleToLocation(-75711, 213421, -7125);
					}
					break;
				case WPRIEST:
					npc.broadcastPacket(new NpcSay(npc, "Intruder spotted! Call guards!"));
					player.teleToLocation(-75711, 213421, -7125);
					break;
				case GUARD_FIRST:
					npc.broadcastPacket(new NpcSay(npc, "Intruder alert! Get out!"));
					player.teleToLocation(-75711, 213421, -7125);
					break;
				case MGUARD:
					npc.broadcastPacket(new NpcSay(npc, "Intruder alert! Get out!"));
					player.teleToLocation(-74960, 209017, -7511);
					break;
				case SPRIEST_F_2:
					if (player.getFirstEffect(GUARD_AMBUSH) == null)
					{
						npc.broadcastPacket(new NpcSay(npc, "This is restricted area! Go away!"));
						player.teleToLocation(-74960, 209017, -7511);
					}
					break;
				case WPRIEST_2:
					npc.broadcastPacket(new NpcSay(npc, "Intruder spotted! Call guards!"));
					player.teleToLocation(-77694, 208726, -7705);
					break;
				case SPRIEST_F_3:
					if (player.getFirstEffect(GUARD_AMBUSH) == null)
					{
						npc.broadcastPacket(new NpcSay(npc, "This is restricted area! Go away!"));
						player.teleToLocation(-77694, 208726, -7705);
					}
					break;
				case GUARD_LAST:
					npc.broadcastPacket(new NpcSay(npc, "Intruder alert! Get out!"));
					player.teleToLocation(-77694, 208726, -7705);
					break;
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new SanctumOftheLordsOfDawn(-1, QN, "instances");
	}
}
