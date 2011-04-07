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
public final class Kamaloka29 extends BasicKamaloka
{
	public static final String	QN	= "Kamaloka29";

	public Kamaloka29(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		_clientInstanceId = 73;
		_xmlTemplate = "Kamaloka-29.xml";

		_enterLocation = new int[]
		{ -76426, -174915, -11008 };
		_bossId = 29129;
		_requiredLevel = 29;

		addKillId(_bossId);
	}
}
