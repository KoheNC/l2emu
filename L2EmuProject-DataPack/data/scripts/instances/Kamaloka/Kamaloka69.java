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
package instances.Kamaloka;

/**
 * @author lord_rex
 */
public final class Kamaloka69 extends BasicKamaloka
{
	public static final String	QN	= "Kamaloka69";

	public Kamaloka69(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		_clientInstanceId = 77;
		_xmlTemplate = "Kamaloka-69.xml";

		_enterLocation = new int[]
		{ -10973, -174906, -10944 };
		_bossId = 31981;
		_requiredLevel = 69;
		_maximumPartyMembers = 9;

		addKillId(_bossId);
	}
}
