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
package net.l2emuproject.gameserver.model.entity.player;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import net.l2emuproject.gameserver.GameTimeController;
import net.l2emuproject.gameserver.ThreadPoolManager;
import net.l2emuproject.gameserver.datatables.FishTable;
import net.l2emuproject.gameserver.model.L2Effect;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExFishingEnd;
import net.l2emuproject.gameserver.network.serverpackets.ExFishingStart;
import net.l2emuproject.gameserver.services.fishing.FishData;
import net.l2emuproject.gameserver.services.fishing.L2Fishing;
import net.l2emuproject.tools.random.Rnd;

public final class PlayerFish extends PlayerExtension
{
	private ScheduledFuture<?>	_taskforfish;
	private boolean				_fishing	= false;
	private int					_fishx		= 0;
	private int					_fishy		= 0;
	private int					_fishz		= 0;
	private L2Fishing			_fishCombat;
	private FishData			_fish;
	private L2ItemInstance		_lure		= null;

	public PlayerFish(L2PcInstance activeChar)
	{
		super(activeChar);
	}

	private final class LookingForFishTask implements Runnable
	{
		boolean	_isNoob, _isUpperGrade;
		@SuppressWarnings("unused")
		int		_fishType, _fishGutsCheck, _gutsCheckTime;
		long	_endTaskTime;

		protected LookingForFishTask(int fishWaitTime, int fishGutsCheck, int fishType, boolean isNoob, boolean isUpperGrade)
		{
			_fishGutsCheck = fishGutsCheck;
			_endTaskTime = System.currentTimeMillis() + fishWaitTime + 10000;
			_fishType = fishType;
			_isNoob = isNoob;
			_isUpperGrade = isUpperGrade;
		}

		@Override
		public final void run()
		{
			if (System.currentTimeMillis() >= _endTaskTime)
			{
				endFishing(false);
				return;
			}

			if (!GameTimeController.getInstance().isNowNight() && _lure.isNightLure())
				return;

			int check = Rnd.get(1000);
			if (_fishGutsCheck > check)
			{
				stopLookingForFishTask();
				startFishCombat(_isNoob, _isUpperGrade);
			}
		}
	}

	public final void startFishing(int x, int y, int z)
	{
		_fishx = x;
		_fishy = y;
		_fishz = z;

		getPlayer().stopMove(null);
		getPlayer().setIsImmobilized(true);
		_fishing = true;
		getPlayer().broadcastUserInfo();
		// Start Fishing
		int lvl = getRandomFishLvl();
		int group = getRandomGroup();
		int type = getRandomFishType(group);
		List<FishData> fishs = FishTable.getInstance().getFish(lvl, type, group);
		if (fishs == null || fishs.size() == 0)
		{
			endFishing(false);
			return;
		}
		int check = Rnd.get(fishs.size());
		_fish = fishs.get(check);
		fishs.clear();
		fishs = null;
		getPlayer().sendPacket(SystemMessageId.CAST_LINE_AND_START_FISHING);
		ExFishingStart efs = null;
		efs = new ExFishingStart(getPlayer(), _fish.getType(), x, y, z, _lure.isNightLure());
		getPlayer().broadcastPacket(efs);
		startLookingForFishTask();
	}

	public final void stopLookingForFishTask()
	{
		if (_taskforfish != null)
		{
			_taskforfish.cancel(false);
			_taskforfish = null;
		}
	}

	public final void startLookingForFishTask()
	{
		if (!getPlayer().isDead() && _taskforfish == null)
		{
			int checkDelay = 0;
			boolean isNoob = false;
			boolean isUpperGrade = false;

			if (_lure != null)
			{
				int lureid = _lure.getItemId();
				isNoob = _fish.getGroup() == 0;
				isUpperGrade = _fish.getGroup() == 2;
				if (lureid == 6519 || lureid == 6522 || lureid == 6525 || lureid == 8505 || lureid == 8508 || lureid == 8511) // Low Grade
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * (1.33)));
				else if (lureid == 6520 || lureid == 6523 || lureid == 6526 || (lureid >= 8505 && lureid <= 8513) || (lureid >= 7610 && lureid <= 7613)
						|| (lureid >= 7807 && lureid <= 7809) || (lureid >= 8484 && lureid <= 8486)) // Medium Grade, Beginner, Prize-Winning & Quest Special Bait
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * (1.00)));
				else if (lureid == 6521 || lureid == 6524 || lureid == 6527 || lureid == 8507 || lureid == 8510 || lureid == 8513) // High grade
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * (0.66)));
			}
			_taskforfish = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(
					new LookingForFishTask(_fish.getWaitTime(), _fish.getFishGuts(), _fish.getType(), isNoob, isUpperGrade), 10000, checkDelay);
		}
	}

	private final int getRandomGroup()
	{
		switch (_lure.getItemId())
		{
			case 7807: // Green for Beginners
			case 7808: // Purple for Beginners
			case 7809: // Yellow for Beginners
			case 8486: // Prize-Winning for Beginners
				return 0;
			case 8485: // Prize-Winning Luminous
			case 8506: // Green Luminous
			case 8509: // Purple Luminous
			case 8512: // Yellow Luminous
				return 2;
			default:
				return 1;
		}
	}

	private final int getRandomFishType(int group)
	{
		int check = Rnd.get(100);
		int type = 1;
		switch (group)
		{
			case 0: // Fish for Novices
				switch (_lure.getItemId())
				{
					case 7807: // Green Lure, preferred by fast-moving (nimble) fish (type 5)
						if (check <= 54)
							type = 5;
						else if (check <= 77)
							type = 4;
						else
							type = 6;
						break;
					case 7808: // Purple Lure, preferred by fat fish (type 4)
						if (check <= 54)
							type = 4;
						else if (check <= 77)
							type = 6;
						else
							type = 5;
						break;
					case 7809: // Yellow Lure, preferred by ugly fish (type 6)
						if (check <= 54)
							type = 6;
						else if (check <= 77)
							type = 5;
						else
							type = 4;
						break;
					case 8486: // Prize-Winning Fishing Lure for Beginners
						if (check <= 33)
							type = 4;
						else if (check <= 66)
							type = 5;
						else
							type = 6;
						break;
				}
				break;
			case 1: // Normal Fish
				switch (_lure.getItemId())
				{
					case 7610:
					case 7611:
					case 7612:
					case 7613:
						type = 3;
						break;
					case 6519: // All theese lures (green) are prefered by fast-moving (nimble) fish (type 1)
					case 8505:
					case 6520:
					case 6521:
					case 8507:
						if (check <= 54)
							type = 1;
						else if (check <= 74)
							type = 0;
						else if (check <= 94)
							type = 2;
						else
							type = 3;
						break;
					case 6522: // All theese lures (purple) are prefered by fat fish (type 0)
					case 8508:
					case 6523:
					case 6524:
					case 8510:
						if (check <= 54)
							type = 0;
						else if (check <= 74)
							type = 1;
						else if (check <= 94)
							type = 2;
						else
							type = 3;
						break;
					case 6525: // All theese lures (yellow) are prefered by ugly fish (type 2)
					case 8511:
					case 6526:
					case 6527:
					case 8513:
						if (check <= 55)
							type = 2;
						else if (check <= 74)
							type = 1;
						else if (check <= 94)
							type = 0;
						else
							type = 3;
						break;
					case 8484: // Prize-Winning Fishing Lure
						if (check <= 33)
							type = 0;
						else if (check <= 66)
							type = 1;
						else
							type = 2;
						break;
				}
				break;
			case 2: // Upper Grade Fish, Luminous Lure
				switch (_lure.getItemId())
				{
					case 8506: // Green Lure, preferred by fast-moving (nimble) fish (type 8)
						if (check <= 54)
							type = 8;
						else if (check <= 77)
							type = 7;
						else
							type = 9;
						break;
					case 8509: // Purple Lure, preferred by fat fish (type 7)
						if (check <= 54)
							type = 7;
						else if (check <= 77)
							type = 9;
						else
							type = 8;
						break;
					case 8512: // Yellow Lure, preferred by ugly fish (type 9)
						if (check <= 54)
							type = 9;
						else if (check <= 77)
							type = 8;
						else
							type = 7;
						break;
					case 8485: // Prize-Winning Fishing Lure
						if (check <= 33)
							type = 7;
						else if (check <= 66)
							type = 8;
						else
							type = 9;
						break;
				}
		}
		return type;
	}

	private final int getRandomFishLvl()
	{
		L2Effect[] effects = getPlayer().getAllEffects();
		int skilllvl = getPlayer().getSkillLevel(1315);
		for (L2Effect e : effects)
		{
			if (e.getSkill().getId() == 2274)
				skilllvl = (int) e.getSkill().getPower();
		}
		if (skilllvl <= 0)
			return 1;
		int randomlvl;
		int check = Rnd.get(100);

		if (check <= 50)
		{
			randomlvl = skilllvl;
		}
		else if (check <= 85)
		{
			randomlvl = skilllvl - 1;
			if (randomlvl <= 0)
			{
				randomlvl = 1;
			}
		}
		else
		{
			randomlvl = skilllvl + 1;
			if (randomlvl > 27)
				randomlvl = 27;
		}

		return randomlvl;
	}

	public final void startFishCombat(boolean isNoob, boolean isUpperGrade)
	{
		_fishCombat = new L2Fishing(getPlayer(), _fish, isNoob, isUpperGrade);
	}

	public final void endFishing(boolean win)
	{
		ExFishingEnd efe = new ExFishingEnd(win, getPlayer());
		getPlayer().broadcastPacket(efe);
		_fishing = false;
		_fishx = 0;
		_fishy = 0;
		_fishz = 0;
		getPlayer().broadcastUserInfo();
		if (_fishCombat == null)
			getPlayer().sendPacket(SystemMessageId.BAIT_LOST_FISH_GOT_AWAY);
		_fishCombat = null;

		_lure = null;
		// End Fishing
		getPlayer().sendPacket(SystemMessageId.REEL_LINE_AND_STOP_FISHING);
		getPlayer().setIsImmobilized(false);
		stopLookingForFishTask();
	}

	public final L2Fishing getFishCombat()
	{
		return _fishCombat;
	}

	public final int getFishx()
	{
		return _fishx;
	}

	public final int getFishy()
	{
		return _fishy;
	}

	public final int getFishz()
	{
		return _fishz;
	}

	public final void setLure(L2ItemInstance lure)
	{
		_lure = lure;
	}

	public final L2ItemInstance getLure()
	{
		return _lure;
	}

	public final boolean isFishing()
	{
		return _fishing;
	}

	public final void setFishing(boolean fishing)
	{
		_fishing = fishing;
	}
}
