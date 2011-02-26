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
package net.l2emuproject.gameserver.handler.admincommandhandlers;

import net.l2emuproject.gameserver.events.global.monsterrace.MonsterRace;
import net.l2emuproject.gameserver.handler.IAdminCommandHandler;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.DeleteObject;
import net.l2emuproject.gameserver.network.serverpackets.MonRaceInfo;
import net.l2emuproject.gameserver.network.serverpackets.PlaySound;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.system.threadmanager.ThreadPoolManager;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * This class handles following admin commands: - mons = handles monster race
 * on/off
 * 
 * @version $Revision: 1.1.6.4 $ $Date: 2007/07/31 10:06:00 $
 */
public class AdminMonsterRace implements IAdminCommandHandler
{
	private static final String[]	ADMIN_COMMANDS	=
													{ "admin_mons" };

	protected static int			state			= -1;

	@Override
	public boolean useAdminCommand(String command, L2Player activeChar)
	{
		if (command.equalsIgnoreCase("admin_mons"))
		{
			handleSendPacket(activeChar);
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void handleSendPacket(L2Player activeChar)
	{
		/*
		 * -1 0 to initialize the race
		 * 0 15322 to start race
		 * 13765 -1 in middle of race
		 * -1 0 to end the race
		 * 
		 * 8003 to 8027
		 */

		int[][] codes =
		{
		{ -1, 0 },
		{ 0, 15322 },
		{ 13765, -1 },
		{ -1, 0 } };
		MonsterRace race = MonsterRace.getInstance();

		if (state == -1)
		{
			state++;
			race.newRace();
			race.newSpeeds();
			MonRaceInfo spk = new MonRaceInfo(codes[state][0], codes[state][1], race.getMonsters(), race.getSpeeds());
			activeChar.sendPacket(spk);
			activeChar.broadcastPacket(spk);
		}
		else if (state == 0)
		{
			state++;
			SystemMessage sm = new SystemMessage(SystemMessageId.MONSRACE_RACE_START);
			sm.addNumber(0);
			activeChar.sendPacket(sm);
			PlaySound SRace = new PlaySound(1, "S_Race");
			activeChar.sendPacket(SRace);
			activeChar.broadcastPacket(SRace);
			PlaySound SRace2 = new PlaySound(0, activeChar, 0, "ItemSound2.race_start");
			activeChar.sendPacket(SRace2);
			activeChar.broadcastPacket(SRace2);
			MonRaceInfo spk = new MonRaceInfo(codes[state][0], codes[state][1], race.getMonsters(), race.getSpeeds());
			activeChar.sendPacket(spk);
			activeChar.broadcastPacket(spk);

			ThreadPoolManager.getInstance().scheduleGeneral(new RunRace(codes, activeChar), 5000);
		}

	}

	class RunRace implements Runnable
	{

		private final int[][]			codes;
		private final L2Player	activeChar;

		public RunRace(int[][] pCodes, L2Player pActiveChar)
		{
			codes = pCodes;
			activeChar = pActiveChar;
		}

		@Override
		public void run()
		{
			//int[][] speeds1 = MonsterRace.getInstance().getSpeeds();
			//MonsterRace.getInstance().newSpeeds();
			//int[][] speeds2 = MonsterRace.getInstance().getSpeeds();
			/*
			 int[] speed = new int[8];
			 for (int i=0; i<8; i++)
			 {
			 for (int j=0; j<20; j++)
			 {
			 //_log.debugr("Adding "+speeds1[i][j] +" and "+ speeds2[i][j]);
			 speed[i] += (speeds1[i][j]*1);// + (speeds2[i][j]*1);
			 }
			 _log.debugr("Total speed for "+(i+1)+" = "+speed[i]);
			 }*/

			MonRaceInfo spk = new MonRaceInfo(codes[2][0], codes[2][1], MonsterRace.getInstance().getMonsters(), MonsterRace.getInstance().getSpeeds());
			activeChar.sendPacket(spk);
			activeChar.broadcastPacket(spk);
			ThreadPoolManager.getInstance().scheduleGeneral(new RunEnd(activeChar), 30000);
		}
	}

	class RunEnd implements Runnable
	{
		private final L2Player	activeChar;

		public RunEnd(L2Player pActiveChar)
		{
			activeChar = pActiveChar;
		}

		@Override
		public void run()
		{
			DeleteObject obj = null;
			for (int i = 0; i < 8; i++)
			{
				obj = new DeleteObject(MonsterRace.getInstance().getMonsters()[i]);
				activeChar.sendPacket(obj);
				activeChar.broadcastPacket(obj);
			}
			state = -1;
		}
	}
}
