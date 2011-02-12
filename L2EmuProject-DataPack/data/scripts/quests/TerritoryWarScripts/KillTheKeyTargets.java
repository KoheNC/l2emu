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
public final class KillTheKeyTargets extends TerritoryWarSuperClass
{
	public static String	qn1	= "738_DestroyKeyTargets";

	public KillTheKeyTargets()
	{
		super(738, qn1, "Destroy Key Targets");

		CLASS_IDS = new int[]
		{ 13, 21, 34, 51, 57, 95, 100, 107, 115, 118, 135, 136 };
		QN = qn1;
		RANDOM_MIN = 3;
		RANDOM_MAX = 8;
		_text = new String[]
		{ "Out of MAX Production and Curse you have defeated KILL.", "You weakened the enemy's attack!" };
	}
}
