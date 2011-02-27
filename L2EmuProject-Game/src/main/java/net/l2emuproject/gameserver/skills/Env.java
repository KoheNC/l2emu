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
package net.l2emuproject.gameserver.skills;

import net.l2emuproject.gameserver.world.object.L2Character;

/**
 * An Env object is just a class to pass parameters to a calculator such as L2Player, L2ItemInstance, Initial value.
 */
public final class Env
{
	private L2Character	_player;
	//	Disabled until it's really used...
	//	public L2CubicInstance	cubic;
	private L2Character	_target;
	//	Disabled until it's really used...
	//	public L2ItemInstance	item;
	private L2Skill		_skill;
	//	Disabled until it's really used...
	//	public L2Effect			effect;
	private double		_value;
	private double		_baseValue	= Double.NaN;
	private boolean		_skillMastery;

	public Env()
	{
	}

	public final void setPlayer(L2Character player)
	{
		_player = player;
	}

	public final L2Character getPlayer()
	{
		return _player;
	}

	public final void setTarget(L2Character target)
	{
		_target = target;
	}

	public final L2Character getTarget()
	{
		return _target;
	}

	public final void setSkill(L2Skill skill)
	{
		_skill = skill;
	}

	public final L2Skill getSkill()
	{
		return _skill;
	}

	public final void setValue(double value)
	{
		_value = value;
	}

	public final double getValue()
	{
		return _value;
	}

	public final void setBaseValue(double baseValue)
	{
		_baseValue = baseValue;
	}

	public final double getBaseValue()
	{
		return _baseValue;
	}

	public final void setSkillMastery(boolean skillMastery)
	{
		_skillMastery = skillMastery;
	}

	public final boolean isSkillMastery()
	{
		return _skillMastery;
	}
}
