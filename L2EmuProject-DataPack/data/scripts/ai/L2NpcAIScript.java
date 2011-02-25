package ai;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastMap;
import net.l2emuproject.gameserver.ThreadPoolManager;
import net.l2emuproject.gameserver.ai.CtrlIntention;
import net.l2emuproject.gameserver.model.L2CharPosition;
import net.l2emuproject.gameserver.model.quest.Quest;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.util.Util;
import net.l2emuproject.gameserver.world.Location;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Stake
 */
public class L2NpcAIScript extends Quest
{
	private final Map<Integer, Integer[][]>	_coords		= new FastMap<Integer, Integer[][]>().shared();
	private final Map<L2Npc, NpcInfo>		_npcInfo	= new FastMap<L2Npc, NpcInfo>().shared();

	public L2NpcAIScript(int questId, String name, String descr)
	{
		super(questId, name, descr);
	}

	public static final L2NpcAIScript getInstance()
	{
		return SingletonHolder._instance;
	}

	public final void registerCoords(final int npcId, final Integer[][] coords)
	{
		if (_coords.get(npcId) == null)
		{
			_coords.put(npcId, coords);
		}
	}

	public final void removeCoords(final int npcId)
	{
		if (_coords.containsKey(npcId))
		{
			_coords.remove(npcId);
		}
	}

	@Override
	public void onArrived(final L2Npc npc)
	{
		moveNextPoint(npc);
	}

	public final void startMoving(final L2Npc npc, final boolean repeat)
	{
		addEventId(npc.getNpcId(), Quest.QuestEventType.ON_ARRIVED);
		getNpcInfo(npc)._repeatMove = repeat;
		moveNextPoint(npc);
	}

	public final void stopMoving(final L2Npc npc)
	{
		npc.stopMove(null);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		if (_npcInfo.containsKey(npc))
		{
			final NpcInfo ni = _npcInfo.get(npc);
			ni._movePointIndex = 0;
			ni._repeatMove = false;
			ni._canMove = false;
		}
	}

	private void moveNextPoint(final L2Npc npc)
	{
		final Integer[][] _c = _coords.get(npc.getNpcId());
		final NpcInfo ni = getNpcInfo(npc);
		if (_c != null && ni._canMove)
		{
			if (ni._movePointIndex < _c.length)
			{
				ThreadPoolManager.getInstance().execute(new Runnable()
				{
					@Override
					public void run()
					{
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
								new L2CharPosition(new Location(_c[ni._movePointIndex][0], _c[ni._movePointIndex][1], _c[ni._movePointIndex][2])));
						++ni._movePointIndex;
					}
				});
			}
			else if (ni._repeatMove)
			{
				ni._movePointIndex = 0;
				moveNextPoint(npc);
			}
		}
	}

	public String onNpcRangeEnter(final L2Character character, final L2Npc npc)
	{
		return null;
	}

	private boolean notifyNpcRangeEnter(final L2Character character, final L2Npc npc)
	{
		final L2Player player = character.getActingPlayer();
		String res = null;
		try
		{
			res = onNpcRangeEnter(character, npc);
		}
		catch (Exception e)
		{
			if (player != null)
			{
				return showError(player, e);
			}
		}
		if (player != null)
		{
			return showResult(player, res);
		}
		return true;
	}

	private class NpcRangeTask implements Runnable
	{
		private final L2Npc	_npc;
		private final int	_radius;

		public NpcRangeTask(final L2Npc npc, final int radius)
		{
			_npc = npc;
			_radius = radius;
		}

		@Override
		public void run()
		{
			Iterable<L2Character> characterKnownlist = _npc.getKnownList().getKnownCharacters();
			if (characterKnownlist != null)
			{
				for (L2Character charObj : characterKnownlist)
				{
					if (Util.checkIfInRange(_radius, charObj, _npc, true))
					{
						notifyNpcRangeEnter(charObj, _npc);
					}
				}
			}
		}
	}

	private class NpcInfo
	{
		private ScheduledFuture<?>	_npcRangeTask;
		private int					_movePointIndex	= 0;
		private boolean				_repeatMove		= false;
		private boolean				_canMove		= true;

		NpcInfo()
		{
		}
	}

	private NpcInfo getNpcInfo(final L2Npc npc)
	{
		if (!_npcInfo.containsKey(npc))
		{
			_npcInfo.put(npc, new NpcInfo());
		}
		return _npcInfo.get(npc);
	}

	public final void deleteNpcInfo(final L2Npc npc)
	{
		if (stopNpcRangeTask(npc))
		{
			_npcInfo.remove(npc);
		}
	}

	/**
	 * Starts the knownlist observe task of an L2Npc.
	 * <br/><b><font color="#ff0000">Use only when needed in derived classes,
	 * <br/>and always stop the task when not needed by stopNpcRangeTask!</font></b>
	 */
	public final void startNpcRangeTask(final L2Npc npc, final int radius, final int period)
	{
		final NpcInfo ni = getNpcInfo(npc);
		if (ni._npcRangeTask == null)
		{
			ni._npcRangeTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new NpcRangeTask(npc, radius), 0, period);
		}
	}

	/**
	 * Stops the knownlist observe task of an L2Npc.
	 */
	public final boolean stopNpcRangeTask(final L2Npc npc)
	{
		if (_npcInfo.containsKey(npc))
		{
			final NpcInfo ni = _npcInfo.get(npc);
			if (ni._npcRangeTask != null)
			{
				ni._npcRangeTask.cancel(true);
				ni._npcRangeTask = null;
			}
			return true;
		}
		return false;
	}

	/**
	 * The same method as in Quest, but those are private methods, and better to keep them private
	 * <br/>to avoid possible unnecessary vtables
	 * @param player : L2Player
	 * @param t : Throwable
	 * @return boolean
	 */
	private boolean showError(final L2Player player, final Throwable t)
	{
		_log.warn(getScriptFile().getAbsolutePath(), t);
		if (player != null && player.getAccessLevel() > 0)
		{
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.close();
			String res = "<html><body><title>Script error</title>" + sw.toString() + "</body></html>";
			return showResult(player, res);
		}
		return false;
	}

	/**
	 * The same method as in Quest, but those are private methods, and better to keep them private
	 * <br/>to avoid possible unnecessary vtables
	 * @param qs : QuestState
	 * @param res : String pointing out the message to show at the player
	 * @return boolean
	 */
	private boolean showResult(final L2Player player, final String res)
	{
		if (res == null || player == null)
		{
			return true;
		}
		if (res.endsWith(".htm"))
		{
			showHtmlFile(player, res);
		}
		else if (res.startsWith("<html>"))
		{
			NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
			npcReply.setHtml(res);
			npcReply.replace("%playername%", player.getName());
			player.sendPacket(npcReply);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			player.sendMessage(res);
		}
		return false;
	}

	private static class SingletonHolder
	{
		protected static final L2NpcAIScript	_instance	= new L2NpcAIScript(-1, "L2NpcAIScript", "L2NpcAIScript");
	}

	public static void main(String[] args)
	{
		L2NpcAIScript.getInstance();
	}
}