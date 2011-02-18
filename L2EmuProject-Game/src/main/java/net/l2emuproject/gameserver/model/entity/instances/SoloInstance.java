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
package net.l2emuproject.gameserver.model.entity.instances;

/**
 * @author lord_rex
 *<br> SoloInstance sketch to make instances easy.
 */
public abstract class SoloInstance extends L2Instance
{
	public SoloInstance(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);
	}
	
	// TODO: Add more functions...
}
