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
package net.l2emuproject.gameserver.services;

import java.util.Date;

import com.jolbox.bonecp.BoneCP;

import net.l2emuproject.Config;
import net.l2emuproject.L2Config;
import net.l2emuproject.gameserver.L2GameServer;
import net.l2emuproject.versionning.Version;

/**
 * @author lord_rex
 */
public final class VersionService
{
	private static final CoreVersion	_commons	= new CoreVersion(L2Config.class);
	private static final CoreVersion	_bonecp		= new CoreVersion(BoneCP.class);
	private static final CoreVersion	_game		= new CoreVersion(L2GameServer.class);

	private VersionService()
	{
	}

	// =====================================================================================
	// Version Info - Commons
	public static String getCommonsVersion()
	{
		return _commons._version;
	}

	public static String getCommonsRevision()
	{
		return _commons._revision;
	}

	public static Date getCommonsBuildDate()
	{
		return _commons._buildDate;
	}

	// =====================================================================================
	// Version Info - Game
	public static String getGameVersion()
	{
		return _game._version;
	}

	public static String getGameRevision()
	{
		return _game._revision;
	}

	public static Date getGameBuildDate()
	{
		return _game._buildDate;
	}

	public static String getGameBuildJdk()
	{
		return _game._buildJdk;
	}

	public static String getGameBuiltBy()
	{
		return _game._builtBy;
	}

	public static String getGameCreatedBy()
	{
		return _game._createdBy;
	}

	public static String getGameArchiverVersion()
	{
		return _game._archiverVersion;
	}
	
	// =====================================================================================
	// Version Info - DataBase
	public static String getDataBaseVersion()
	{
		return _bonecp.getVersionNumber();
	}

	// =====================================================================================
	// Version Info - DataPack
	public static String[] getDataPackVersionInfo()
	{
		return new String[] {
				"L2EmuProject DataPack: ",
				" - DataPack Version: " + Config.DATAPACK_VERSION,
				" - DataPack Revision: " + Config.DATAPACK_REVISION,
				" - DataPack Build Date: " + Config.DATAPACK_BUILD_DATE 
		};
	}

	// =====================================================================================
	// Version Info - All
	public static String[] getFullVersionInfo()
	{
		return new String[] {
				"L2EmuProject-Library: ",
				" - Commons Version: " + getCommonsVersion(),
				" - Commons Revision: " + getCommonsRevision(),
				" - Commons Build Date: " + getCommonsBuildDate(),
				"L2EmuProject-Game: ",
				" - GameServer Version: " + getGameVersion(),
				" - GameServer Revision: " + getGameRevision(),
				" - GameServer Build Date: " + getGameBuildDate(),
				" - GameServer Build JDK: " + getGameBuildJdk(),
				" - GameServer Built By: " + getGameBuiltBy(),
				" - GameServer Created By: " + getGameCreatedBy(),
				" - GameServer Archiver Version: " + getGameArchiverVersion(),
				"Home Page:",
				" - http://www.l2emuproject.net" 
		};
	}

	private static final class CoreVersion extends Version
	{
		public final String	_version;
		public final String	_revision;
		public final Date	_buildDate;
		public final String	_buildJdk;
		public final String	_builtBy;
		public final String	_createdBy;
		public final String	_archiverVersion;

		public CoreVersion(Class<?> c)
		{
			super(c);

			_version = String.format("%-6s", getVersionNumber());
			_revision = String.format("%-6s", getRevisionNumber());
			_buildDate = new Date(getBuildTime());
			_buildJdk = getBuildJdk();
			_builtBy = getBuiltBy();
			_createdBy = getCreatedBy();
			_archiverVersion = getArchiverVersion();
		}
	}
}
