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
public final class KillThePriests extends TerritoryWarSuperClass
{
	public static String	qn1	= "737_DenyBlessings";

	public KillThePriests()
	{
		super(737, qn1, "Deny Blessings");

		CLASS_IDS = new int[]
		{ 16, 17, 30, 43, 52, 97, 98, 105, 112, 116 };
		QN = qn1;
		RANDOM_MIN = 3;
		RANDOM_MAX = 8;
		_text = new String[]
		{ "Out of MAX Priests you have defeated KILL.", "You weakened the enemy's attack!" };
	}
}
