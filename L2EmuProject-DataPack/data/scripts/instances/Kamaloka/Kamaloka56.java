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
public final class Kamaloka56 extends BasicKamaloka
{
	public static final String	QN	= "Kamaloka56";

	public Kamaloka56(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		_clientInstanceId = 67;
		_xmlTemplate = "Kamaloka-56.xml";

		_enterLocation = new int[]
		{ -49337, -220890, -8071 };
		_bossId = 30196;
		_requiredLevel = 56;

		addKillId(_bossId);
	}
}