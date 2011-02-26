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
package net.l2emuproject.gameserver.entity.appearance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.system.L2DatabaseFactory;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.tools.util.HexUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class PcAppearance
{
	private static final Log	_log				= LogFactory.getLog(PcAppearance.class);

	// Name / Title Colors
	private static final String	RESTORE_COLORS		= "SELECT name_color, title_color FROM character_name_title_colors WHERE char_id=?";
	private static final String	UPDATE_COLORS		= "REPLACE INTO character_name_title_colors VALUES(?,?,?)";

	/** The default hexadecimal color of players' name (white is 0xFFFFFF) */
	public static final int		DEFAULT_NAME_COLOR	= 0xFFFFFF;
	/** The default hexadecimal color of players' title (light blue is 0xFFFF77) */
	public static final int		DEFAULT_TITLE_COLOR	= 0xFFFF77;

	// =========================================================
	// Data Field
	private L2Player		_owner;
	private byte				_face;
	private byte				_hairColor;
	private byte				_hairStyle;
	private boolean				_sex;																										// Female true(1)

	/** true if the player is invisible */
	private boolean				_invisible			= false;

	/** The current visisble name of this palyer, not necessarily the real one */
	private String				_visibleName;
	/** The current visisble title of this palyer, not necessarily the real one */
	private String				_visibleTitle;

	/** The hexadecimal Color of players name (white is 0xFFFFFF) */
	private int					_nameColor			= DEFAULT_NAME_COLOR;
	/** The hexadecimal Color of players title (light blue is 0xFFFF77) */
	private int					_titleColor			= DEFAULT_TITLE_COLOR;

	// =========================================================
	// Constructor
	public PcAppearance(byte face, byte hColor, byte hStyle, boolean sex)
	{
		_face = face;
		_hairColor = hColor;
		_hairStyle = hStyle;
		_sex = sex;
	}

	public void setVisibleName(String visibleName)
	{
		_visibleName = visibleName;
	}

	public String getVisibleName()
	{
		if (_visibleName != null)
			return _visibleName;

		return _owner.getName();
	}

	public void setVisibleTitle(String visibleTitle)
	{
		_visibleTitle = visibleTitle;
	}

	public String getVisibleTitle()
	{
		if (_visibleTitle != null)
			return _visibleTitle;

		return _owner.getTitle();
	}

	public byte getFace()
	{
		return _face;
	}

	public void setFace(int value)
	{
		_face = (byte) value;
	}

	public byte getHairColor()
	{
		return _hairColor;
	}

	public void setHairColor(int value)
	{
		_hairColor = (byte) value;
	}

	public byte getHairStyle()
	{
		return _hairStyle;
	}

	public void setHairStyle(int value)
	{
		_hairStyle = (byte) value;
	}

	public boolean getSex()
	{
		return _sex;
	}

	public void setSex(boolean isFemale)
	{
		_sex = isFemale;
	}

	public void setInvisible()
	{
		_invisible = true;
	}

	public void setVisible()
	{
		_invisible = false;
	}

	public boolean isInvisible()
	{
		return _invisible;
	}

	public int getNameColor()
	{
		return _nameColor;
	}

	public void setNameColor(int nameColor)
	{
		_nameColor = nameColor;
	}

	public void setNameColor(int red, int green, int blue)
	{
		_nameColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
	}

	public int getTitleColor()
	{
		return _titleColor;
	}

	public void setTitleColor(int titleColor)
	{
		_titleColor = titleColor;
	}

	public void setTitleColor(int red, int green, int blue)
	{
		setTitleColor((red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16));
	}

	public void setOwner(L2Player owner)
	{
		_owner = owner;
	}

	public final void updateNameTitleColor()
	{
		// L2EMU_EDIT: START
		if (_owner.isClanLeader() && Config.CLAN_LEADER_COLOR_ENABLED && _owner.getClan().getLevel() >= Config.CLAN_LEADER_COLOR_CLAN_LEVEL)
		{
			if (Config.CLAN_LEADER_COLORED == Config.ClanLeaderColored.name)
				_nameColor = Config.CLAN_LEADER_COLOR;
			else
				_titleColor = Config.CLAN_LEADER_COLOR;
		}

		else if (Config.CHAR_VIP_COLOR_ENABLED && _owner.isCharViP())
		{
			_nameColor = Config.CHAR_VIP_COLOR;
		}

		else if (_owner.isGM())
		{
			if (Config.GM_NAME_COLOR_ENABLED)
			{
				if (_owner.getAccessLevel() >= 100)
					_nameColor = Config.ADMIN_NAME_COLOR;
				else if (_owner.getAccessLevel() >= 75)
					_nameColor = Config.GM_NAME_COLOR;
			}

			if (Config.GM_TITLE_COLOR_ENABLED)
			{
				if (_owner.getAccessLevel() >= 100)
					_titleColor = Config.ADMIN_TITLE_COLOR;
				else if (_owner.getAccessLevel() >= 75)
					_titleColor = Config.GM_TITLE_COLOR;
			}
		}

		// L2EMU_ADD
		else if (_owner.isDonator())
		{
			_nameColor = Config.DONATOR_NAME_COLOR;
			_titleColor = Config.DONATOR_TITLE_COLOR;
		}
		// L2EMU_ADD

		else if (Config.ALLOW_OFFLINE_TRADE_COLOR_NAME && _owner.isInOfflineMode())
		{
			_nameColor = Config.OFFLINE_TRADE_COLOR_NAME;
		}

		else
		{
			updatePvPColor(_owner.getPvpKills());
			updatePkColor(_owner.getPkKills());

			_nameColor = DEFAULT_NAME_COLOR;
			_titleColor = DEFAULT_TITLE_COLOR;
		}
		// L2EMU_EDIT: END

		_owner.broadcastUserInfo();
	}

	private final void updatePvPColor(int pvpKillAmount)
	{
		if (Config.PVP_COLOR_SYSTEM_ENABLED)
		{
			if ((pvpKillAmount >= (Config.PVP_AMOUNT1)) && (pvpKillAmount <= (Config.PVP_AMOUNT2)))
				_nameColor = (Config.NAME_COLOR_FOR_PVP_AMOUNT1);
			else if ((pvpKillAmount >= (Config.PVP_AMOUNT2)) && (pvpKillAmount <= (Config.PVP_AMOUNT3)))
				_nameColor = (Config.NAME_COLOR_FOR_PVP_AMOUNT2);
			else if ((pvpKillAmount >= (Config.PVP_AMOUNT3)) && (pvpKillAmount <= (Config.PVP_AMOUNT4)))
				_nameColor = (Config.NAME_COLOR_FOR_PVP_AMOUNT3);
			else if ((pvpKillAmount >= (Config.PVP_AMOUNT4)) && (pvpKillAmount <= (Config.PVP_AMOUNT5)))
				_nameColor = (Config.NAME_COLOR_FOR_PVP_AMOUNT4);
			else if (pvpKillAmount >= (Config.PVP_AMOUNT5))
				_nameColor = (Config.NAME_COLOR_FOR_PVP_AMOUNT5);
		}
	}

	private final void updatePkColor(int pkKillAmount)
	{
		if (Config.PK_COLOR_SYSTEM_ENABLED)
		{
			if ((pkKillAmount >= (Config.PK_AMOUNT1)) && (pkKillAmount <= (Config.PVP_AMOUNT2)))
				_titleColor = (Config.TITLE_COLOR_FOR_PK_AMOUNT1);
			else if ((pkKillAmount >= (Config.PK_AMOUNT2)) && (pkKillAmount <= (Config.PVP_AMOUNT3)))
				_titleColor = (Config.TITLE_COLOR_FOR_PK_AMOUNT2);
			else if ((pkKillAmount >= (Config.PK_AMOUNT3)) && (pkKillAmount <= (Config.PVP_AMOUNT4)))
				_titleColor = (Config.TITLE_COLOR_FOR_PK_AMOUNT3);
			else if ((pkKillAmount >= (Config.PK_AMOUNT4)) && (pkKillAmount <= (Config.PVP_AMOUNT5)))
				_titleColor = (Config.TITLE_COLOR_FOR_PK_AMOUNT4);
			else if (pkKillAmount >= (Config.PK_AMOUNT5))
				_titleColor = (Config.TITLE_COLOR_FOR_PK_AMOUNT5);
		}
	}

	public final void restoreNameTitleColors()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(RESTORE_COLORS);
			statement.setInt(1, _owner.getObjectId());
			ResultSet result = statement.executeQuery();
			if (result.next())
			{
				setNameColor(Util.reverseRGBChanels(Integer.decode("0x" + result.getString(1))));
				setTitleColor(Util.reverseRGBChanels(Integer.decode("0x" + result.getString(2))));
			}
			else
			{
				setNameColor(PcAppearance.DEFAULT_NAME_COLOR);
				setTitleColor(PcAppearance.DEFAULT_TITLE_COLOR);
			}
			result.close();
			statement.close();
		}
		catch (Exception e)
		{
			setNameColor(PcAppearance.DEFAULT_NAME_COLOR);
			setTitleColor(PcAppearance.DEFAULT_TITLE_COLOR);

			_log.error("Could not load character name/title colors!", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		updateNameTitleColor();
	}

	public final void storeNameTitleColors()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_COLORS);
			statement.setInt(1, _owner.getObjectId());
			statement.setString(2, HexUtil.fillHex(Util.reverseRGBChanels(_nameColor), 6));
			statement.setString(3, HexUtil.fillHex(Util.reverseRGBChanels(_titleColor), 6));
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not store character name/title colors!", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
}
