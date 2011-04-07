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
public final class Kamaloka36 extends BasicKamaloka
{
	public static final String	QN	= "Kamaloka36";

	public Kamaloka36(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		_clientInstanceId = 61;
		_xmlTemplate = "Kamaloka-36.xml";

		_enterLocation = new int[]
		{ -81937, -214200, -8103 };
		_bossId = 18559;
		_requiredLevel = 36;

		addKillId(_bossId);
	}
}
