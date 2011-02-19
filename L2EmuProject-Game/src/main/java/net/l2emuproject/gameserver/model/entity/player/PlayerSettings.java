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

import net.l2emuproject.gameserver.model.L2UIKeysSettings;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.services.macro.L2Macro;
import net.l2emuproject.gameserver.services.macro.MacroList;
import net.l2emuproject.gameserver.services.shortcuts.L2ShortCut;
import net.l2emuproject.gameserver.services.shortcuts.ShortCuts;

public class PlayerSettings extends PlayerExtension
{
	private L2UIKeysSettings	_uiKeySettings;
	private ShortCuts			_shortCuts;
	private MacroList			_macroses;

	public PlayerSettings(L2PcInstance activeChar)
	{
		super(activeChar);
	}

	public void restoreUISettings()
	{
		_uiKeySettings = new L2UIKeysSettings(getPlayer());
	}

	public void storeUISettings()
	{
		if (_uiKeySettings == null)
			return;

		if (!_uiKeySettings.isSaved())
			_uiKeySettings.saveInDB();
	}

	public L2UIKeysSettings getUISettings()
	{
		return _uiKeySettings;
	}

	public ShortCuts getShortCuts()
	{
		if (_shortCuts == null)
			_shortCuts = new ShortCuts(getPlayer());

		return _shortCuts;
	}

	/**
	 * Return a table containing all L2ShortCut of the L2PcInstance.<BR><BR>
	 */
	public L2ShortCut[] getAllShortCuts()
	{
		return getShortCuts().getAllShortCuts();
	}

	/**
	 * Add a L2shortCut to the L2PcInstance shortCuts<BR><BR>
	 */
	public void registerShortCut(L2ShortCut shortcut)
	{
		getShortCuts().registerShortCut(shortcut);
	}

	/**
	 * Delete the L2ShortCut corresponding to the position (page-slot) from the L2PcInstance shortCuts.<BR><BR>
	 */
	public void deleteShortCut(int slot, int page)
	{
		getShortCuts().deleteShortCut(slot, page);
	}

	/**
	 * Delete a ShortCut of the L2PcInstance shortCuts.<BR><BR>
	 */
	public void removeItemFromShortCut(int objectId)
	{
		getShortCuts().deleteShortCutByObjectId(objectId);
	}

	/**
	 * Add a L2Macro to the L2PcInstance macroses<BR><BR>
	 */
	public void registerMacro(L2Macro macro)
	{
		getMacroses().registerMacro(macro);
	}

	/**
	 * Delete the L2Macro corresponding to the Identifier from the L2PcInstance macroses.<BR><BR>
	 */
	public void deleteMacro(int id)
	{
		getMacroses().deleteMacro(id);
	}

	/**
	 * Return all L2Macro of the L2PcInstance.<BR><BR>
	 */
	public MacroList getMacroses()
	{
		if (_macroses == null)
			_macroses = new MacroList(getPlayer());

		return _macroses;
	}
}

