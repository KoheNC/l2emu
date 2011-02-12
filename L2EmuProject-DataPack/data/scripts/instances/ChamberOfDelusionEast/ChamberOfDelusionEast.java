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
package instances.ChamberOfDelusionEast;

import instances.ChamberOfDelusion.ChamberOfDelusion;

/**
 * @author Stake
 */
public class ChamberOfDelusionEast extends ChamberOfDelusion
{
	private static final String DC_EAST = "ChamberOfDelusionEast";
	
	public ChamberOfDelusionEast()
	{
		super(Q_ID, DC_EAST, Q_TYPE);
		QN = DC_EAST;
		TEMPLATE_ID = 127;
		GKSTART = 32658;
		GKFINISH = 32664;
		AENKINEL = 25690;
		initScript();
	}
}