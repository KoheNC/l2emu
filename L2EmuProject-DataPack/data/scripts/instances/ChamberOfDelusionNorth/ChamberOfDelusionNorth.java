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
package instances.ChamberOfDelusionNorth;

import instances.ChamberOfDelusion.ChamberOfDelusion;

/**
 * @author Stake
 */
public class ChamberOfDelusionNorth extends ChamberOfDelusion
{
	private static final String DC_NORTH = "ChamberOfDelusionNorth";
	
	public ChamberOfDelusionNorth()
	{
		super(Q_ID, DC_NORTH, Q_TYPE);
		QN = DC_NORTH;
		TEMPLATE_ID = 130;
		GKSTART = 32661;
		GKFINISH = 32667;
		AENKINEL = 25693;
		initScript();
	}
}
