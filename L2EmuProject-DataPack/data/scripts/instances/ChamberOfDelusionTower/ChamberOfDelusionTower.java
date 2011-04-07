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
package instances.ChamberOfDelusionTower;

import instances.ChamberOfDelusion.ChamberOfDelusion;

/**
 * @author Stake
 */
public class ChamberOfDelusionTower extends ChamberOfDelusion
{
	private static final String DC_TOWER = "ChamberOfDelusionTower";
	
	public ChamberOfDelusionTower()
	{
		super(Q_ID, DC_TOWER, Q_TYPE);
		QN = DC_TOWER;
		TEMPLATE_ID = 132;
		GKSTART = 32663;
		GKFINISH = 32669;
		AENKINEL = 25695;
		initScript();
	}
}
