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
package quests.TerritoryWarScripts;

/**
 * @author Gigiikun
 */
public final class TheTerritoryRune extends TerritoryWarSuperClass
{
	public static String	qn1	= "724_FortheSakeoftheTerritoryRune";

	public TheTerritoryRune()
	{
		super(724, qn1, "For the Sake of the Territory - Rune");
		
		CATAPULT_ID = 36506;
		TERRITORY_ID = 88;
		LEADER_IDS = new int[]
		{ 36550, 36552, 36555, 36598 };
		GUARD_IDS = new int[]
		{ 36551, 36553, 36554 };
		QN = qn1;
		_text = new String[]
		{ "The catapult of Rune has been destroyed!" };
		registerKillIds();
	}
}
