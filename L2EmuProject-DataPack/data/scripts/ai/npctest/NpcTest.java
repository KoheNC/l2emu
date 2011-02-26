package ai.npctest;

import ai.L2NpcAIScript;
import net.l2emuproject.gameserver.system.threadmanager.ThreadPoolManager;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Stake
 */
public final class NpcTest extends L2NpcAIScript
{
	private static final int	GRIZZLY_BEAR	= 13131;
	private Integer				coords[][]		=
												{
												{ 82855, 148198, -3472 },
												{ 82838, 148751, -3472 },
												{ 82574, 148698, -3469 },
												{ 82556, 148233, -3472 } }; // test location in Giran Town Center

	public NpcTest(int questId, String name, String descr)
	{
		super(questId, name, descr);
		final L2Npc npc = addSpawn(GRIZZLY_BEAR, 82981, 147855, -3469, 0, false, 0);
		if (npc != null)
		{
			registerCoords(GRIZZLY_BEAR, coords); // first always register the move coordinates
			startMoving(npc, true); // then you can make the npc move along the coords
			startNpcRangeTask(npc, 500, 1000); // starts the npc observe task
		}
		ThreadPoolManager.getInstance().schedule(new Runnable()
		{
			@Override
			public void run()
			{
				stopMoving(npc); // FIRST stop the npc when moving not needed
				removeCoords(GRIZZLY_BEAR); // after stopping, remove from _coords, if we don't want to use them later
				deleteNpcInfo(npc); // automatically calls stopNpcRangeTask, then deletes the npc from _npcInfo
			}
		}, 10000); // npc stop scheduled to 10 seconds after initializing NpcTest
		_log.info("NpcTest loaded.");
	}

	@Override
	public String onNpcRangeEnter(L2Character character, L2Npc npc)
	{
		L2Player player = character.getActingPlayer();
		if (player != null)
		{
			player.sendMessage("You are in " + npc.getName() + "'s hunting area.");
		}
		return null;
	}

	public static void main(String[] args)
	{
		new NpcTest(-1, "NpcTest", "NpcTest");
	}
}