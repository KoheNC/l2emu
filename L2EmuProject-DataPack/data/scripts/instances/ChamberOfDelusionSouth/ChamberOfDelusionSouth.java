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
package instances.ChamberOfDelusionSouth;

import instances.ChamberOfDelusion.ChamberOfDelusion;

/**
 * @author Stake
 */
public class ChamberOfDelusionSouth extends ChamberOfDelusion
{
	private static final String DC_SOUTH = "ChamberOfDelusionSouth";
	
	public ChamberOfDelusionSouth()
	{
		super(Q_ID, DC_SOUTH, Q_TYPE);
		QN = DC_SOUTH;
		TEMPLATE_ID = 129;
		GKSTART = 32660;
		GKFINISH = 32666;
		AENKINEL = 25692;
		initScript();
	}
}