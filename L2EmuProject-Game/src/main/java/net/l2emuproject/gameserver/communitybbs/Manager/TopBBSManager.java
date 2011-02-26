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
package net.l2emuproject.gameserver.communitybbs.Manager;

import java.util.StringTokenizer;

import net.l2emuproject.gameserver.system.cache.HtmCache;
import net.l2emuproject.gameserver.world.object.L2Player;



public class TopBBSManager extends BaseBBSManager
{
	private TopBBSManager()
	{
	}

	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.communitybbs.Manager.BaseBBSManager#parsecmd(java.lang.String, net.l2emuproject.gameserver.world.object.instance.L2PcInstance)
	 */
	@Override
	public void parsecmd(String command, L2Player activeChar)
	{
		if (command.equals("_bbstop"))
		{
			String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/index.htm");
			if (content == null)
			{
				content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/index.htm' </center></body></html>";
			}
			separateAndSend(content, activeChar);
		}
		else if (command.equals("_bbshome"))
		{
			String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/index.htm");
			if (content == null)
			{
				content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/index.htm' </center></body></html>";
			}
			separateAndSend(content, activeChar);
		}
		else if (command.startsWith("_bbstop;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			int idp = Integer.parseInt(st.nextToken());
			String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + idp + ".htm");
			if (content == null)
			{
				content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/" + idp + ".htm' </center></body></html>";
			}
			separateAndSend(content, activeChar);
		}
		else
		{
			notImplementedYet(activeChar, command);
		}
	}

	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.communitybbs.Manager.BaseBBSManager#parsewrite(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, net.l2emuproject.gameserver.world.object.instance.L2PcInstance)
	 */
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2Player activeChar)
	{

	}

	/**
	 * @return
	 */
	public static TopBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final TopBBSManager _instance = new TopBBSManager();
	}
}
