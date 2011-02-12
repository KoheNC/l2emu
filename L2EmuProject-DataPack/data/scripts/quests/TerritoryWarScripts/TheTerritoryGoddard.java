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
public final class TheTerritoryGoddard extends TerritoryWarSuperClass
{
	public static String	qn1	= "723_FortheSakeoftheTerritoryGoddard";

	public TheTerritoryGoddard()
	{
		super(723, qn1, "For the Sake of the Territory - Goddard");

		CATAPULT_ID = 36505;
		TERRITORY_ID = 87;
		LEADER_IDS = new int[]
		{ 36544, 36546, 36549, 36597 };
		GUARD_IDS = new int[]
		{ 36545, 36547, 36548 };
		QN = qn1;
		_text = new String[]
		{ "The catapult of Goddard has been destroyed!" };
		registerKillIds();
	}
}
