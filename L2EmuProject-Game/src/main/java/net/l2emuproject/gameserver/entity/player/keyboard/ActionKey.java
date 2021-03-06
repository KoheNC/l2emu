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
package net.l2emuproject.gameserver.entity.player.keyboard;

/**
 * @author  mrTJO
 */
public final class ActionKey
{
	private final int	_cat;
	private final int	_cmd;
	private final int	_key;
	private final int	_tgKey1;
	private final int	_tgKey2;
	private final int	_show;

	/**
	 * L2ActionKey Initialization
	 * @param cat: Category ID
	 * @param cmd: Command ID
	 * @param key: User Defined Primary Key
	 * @param tgKey1: 1st Toogled Key (eg. Alt, Ctrl or Shift)
	 * @param tgKey2: 2nd Toogled Key (eg. Alt, Ctrl or Shift)
	 * @param show: Show Action in UI
	 */
	public ActionKey(final int cat, final int cmd, final int key, final int tgKey1, final int tgKey2, final int show)
	{
		_cat = cat;
		_cmd = cmd;
		_key = key;
		_tgKey1 = tgKey1;
		_tgKey2 = tgKey2;
		_show = show;
	}

	public final int getCategory()
	{
		return _cat;
	}

	public final int getCommandId()
	{
		return _cmd;
	}

	public final int getKeyId()
	{
		return _key;
	}

	public final int getToogleKey1()
	{
		return _tgKey1;
	}

	public final int getToogleKey2()
	{
		return _tgKey2;
	}

	public final int getShowStatus()
	{
		return _show;
	}

	public final String getSqlSaveString(final int playerId, final int order)
	{
		return "(" + playerId + ", " + _cat + ", " + order + ", " + _cmd + "," + _key + ", " + _tgKey1 + ", " + _tgKey2 + ", " + _show + ")";
	}
}
