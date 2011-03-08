/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General public final License for more
 * details.
 * 
 * You should have received a copy of the GNU General public final License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.l2emuproject.gameserver.entity.player;

import net.l2emuproject.gameserver.entity.player.keyboard.L2UIKeysSettings;
import net.l2emuproject.gameserver.services.macro.L2Macro;
import net.l2emuproject.gameserver.services.macro.MacroList;
import net.l2emuproject.gameserver.services.shortcuts.L2ShortCut;
import net.l2emuproject.gameserver.services.shortcuts.ShortCuts;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class PlayerSettings extends PlayerExtension
{
	private L2UIKeysSettings	_uiKeySettings;
	private ShortCuts			_shortCuts;
	private MacroList			_macroses;

	public PlayerSettings(L2Player activeChar)
	{
		super(activeChar);
	}

	public final void restoreUISettings()
	{
		_uiKeySettings = new L2UIKeysSettings(getPlayer());
	}

	public final void storeUISettings()
	{
		if (_uiKeySettings == null)
			return;

		if (!_uiKeySettings.isSaved())
			_uiKeySettings.saveInDB();
	}

	public final L2UIKeysSettings getUISettings()
	{
		return _uiKeySettings;
	}

	public final ShortCuts getShortCuts()
	{
		if (_shortCuts == null)
			_shortCuts = new ShortCuts(getPlayer());

		return _shortCuts;
	}

	/**
	 * Return a table containing all L2ShortCut of the L2Player.<BR><BR>
	 */
	public final L2ShortCut[] getAllShortCuts()
	{
		return getShortCuts().getAllShortCuts();
	}

	/**
	 * Add a L2shortCut to the L2Player shortCuts<BR><BR>
	 */
	public final void registerShortCut(final L2ShortCut shortcut)
	{
		getShortCuts().registerShortCut(shortcut);
	}

	/**
	 * Delete the L2ShortCut corresponding to the position (page-slot) from the L2Player shortCuts.<BR><BR>
	 */
	public final void deleteShortCut(final int slot, final int page)
	{
		getShortCuts().deleteShortCut(slot, page);
	}

	/**
	 * Delete a ShortCut of the L2Player shortCuts.<BR><BR>
	 */
	public final void removeItemFromShortCut(final int objectId)
	{
		getShortCuts().deleteShortCutByObjectId(objectId);
	}

	/**
	 * Add a L2Macro to the L2Player macroses<BR><BR>
	 */
	public final void registerMacro(final L2Macro macro)
	{
		getMacroses().registerMacro(macro);
	}

	/**
	 * Delete the L2Macro corresponding to the Identifier from the L2Player macroses.<BR><BR>
	 */
	public final void deleteMacro(final int id)
	{
		getMacroses().deleteMacro(id);
	}

	/**
	 * Return all L2Macro of the L2Player.<BR><BR>
	 */
	public final MacroList getMacroses()
	{
		if (_macroses == null)
			_macroses = new MacroList(getPlayer());

		return _macroses;
	}
}
