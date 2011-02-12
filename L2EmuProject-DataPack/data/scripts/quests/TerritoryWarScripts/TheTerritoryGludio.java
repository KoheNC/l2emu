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
public final class TheTerritoryGludio extends TerritoryWarSuperClass
{
	public static String	qn1	= "717_FortheSakeoftheTerritoryGludio";

	public TheTerritoryGludio()
	{
		super(717, qn1, "For the Sake of the Territory - Gludio");

		CATAPULT_ID = 36499;
		TERRITORY_ID = 81;
		LEADER_IDS = new int[]
		{ 36508, 36510, 36513, 36591 };
		GUARD_IDS = new int[]
		{ 36509, 36511, 36512 };
		QN = qn1;
		_text = new String[]
		{ "The catapult of Gludio has been destroyed!" };
		registerKillIds();
	}
}
