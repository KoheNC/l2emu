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
package instances;

import net.l2emuproject.gameserver.instancemanager.InstanceManager;
import net.l2emuproject.gameserver.instancemanager.InstanceManager.InstanceWorld;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.entity.Instance;
import net.l2emuproject.gameserver.model.entity.instances.PartyInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.world.object.L2Npc;

/**
 * @author lord_rex
 *<br> an Example instance script.
 */
public final class ExampleInstance extends PartyInstance
{
	// Quest name
	private static final String	QN				= "ExampleInstance";

	// XML File for instance
	private static final String	TEMPLATE_XML	= "ExampleInstance.xml";

	// This is the template Instance Id
	private static final int	INSTANCE_ID		= 111;

	// Enter Instance Teleport locations
	private static final int[]	ENTER_LOCATION	=
												{ 11111, -11111, -1111 };

	// Exit Instance Teleport locations
	private static final int[]	EXIT_LOCATION	=
												{ 22222, -22222, -2222 };

	// Target monster Id, your job is in instance to kill this mob.
	private static final int	TARGET_MONSTER	= 11111;

	// The dynamic instance Id, what's created when you enter instance.
	private int					_instanceId		= 0;

	public ExampleInstance(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		addKillId(TARGET_MONSTER);
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
			return null;

		switch (npc.getNpcId())
		{
			case TARGET_MONSTER:
				st.setState(State.COMPLETED);
				player.sendMessage("Congratulation your party succesful killed the instance monster!");
				break;
		}

		return null;
	}

	@Override
	protected final boolean canEnter(L2PcInstance player)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
			newQuestState(player);

		if (player.getLevel() < 10)
		{
			player.sendMessage("You can't enter to " + QN + " because your level is too low.");
			return false;
		}

		if (st.getState() == State.COMPLETED)
		{
			player.sendMessage("You already finished this instance!");
			return false;
		}

		return true;
	}

	@Override
	protected final void enterInstance(L2PcInstance player)
	{
		if (!canEnter(player))
			return;

		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (world.templateId != INSTANCE_ID)
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return;
			}
			Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);
			if (instance != null)
				teleportParty(player, ENTER_LOCATION, world.instanceId);
			return;
		}
		else
		{
			_instanceId = InstanceManager.getInstance().createDynamicInstance(TEMPLATE_XML);

			world = InstanceManager.getInstance().new InstanceWorld();
			// Set instanceId to world instanceId.
			world.instanceId = _instanceId;
			world.templateId = INSTANCE_ID;
			InstanceManager.getInstance().addWorld(world);

			world.allowed.add(player.getObjectId());
			teleportParty(player, ENTER_LOCATION, _instanceId);

			_log.info("ExampleInstance: " + _instanceId + " created by player: " + player.getName());
		}
	}

	@Override
	protected final void exitInstance(L2PcInstance player)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
			return;

		if (st.getState() != State.COMPLETED)
		{
			player.sendMessage("You can't exit instance, because you are not done with your job!");
			return;
		}

		// Set instanceId to 0.
		teleportPlayer(player, EXIT_LOCATION, 0);
	}

	public static void main(String[] args)
	{
		new ExampleInstance(-1, QN, "Example Instance script...", "instances");
	}
}
