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

import java.util.Arrays;

import net.l2emuproject.gameserver.templates.StatsSet;
import net.l2emuproject.tools.random.Rnd;


/**
 * @author kombat/crion
 */
public final class ChanceCondition
{
	public static final int EVT_HIT = 1;
	public static final int EVT_CRIT = 2;
	public static final int EVT_CAST = 4;
	public static final int EVT_PHYSICAL = 8;
	public static final int EVT_MAGIC = 16;
	public static final int EVT_MAGIC_GOOD = 32;
	public static final int EVT_MAGIC_OFFENSIVE = 64;
	public static final int EVT_ATTACKED = 128;
	public static final int EVT_ATTACKED_HIT = 256;
	public static final int EVT_ATTACKED_CRIT = 512;
	public static final int EVT_HIT_BY_SKILL = 1024;
	public static final int EVT_HIT_BY_OFFENSIVE_SKILL = 2048;
	public static final int EVT_HIT_BY_GOOD_MAGIC = 4096;
	public static final int EVT_EVADED_HIT = 8192;
	public static final int EVT_RANGE = 16384;
	public static final int EVT_MELEE = 32768;
	
	public static enum TriggerType
	{
		// You hit an enemy
		ON_HIT(1),
		// You hit an enemy - was crit
		ON_CRIT(2),
		// You cast a skill
		ON_CAST(4),
		// You cast a skill - it was a physical one
		ON_PHYSICAL(8),
		// You cast a skill - it was a magic one
		ON_MAGIC(16),
		// You cast a skill - it was a magic one - good magic
		ON_MAGIC_GOOD(32),
		// You cast a skill - it was a magic one - offensive magic
		ON_MAGIC_OFFENSIVE(64),
		// You are attacked by enemy
		ON_ATTACKED(128),
		// You are attacked by enemy - by hit
		ON_ATTACKED_HIT(256),
		// You are attacked by enemy - by hit - was crit
		ON_ATTACKED_CRIT(512),
		// A skill was casted on you
		ON_HIT_BY_SKILL(1024),
		// An evil skill was casted on you
		ON_HIT_BY_OFFENSIVE_SKILL(2048),
		// A good skill was casted on you
		ON_HIT_BY_GOOD_MAGIC(4096),
		// Evading melee attack
		ON_EVADED_HIT(8192),
		// You do a range attack
		ON_RANGE(16384),
		// You do a melee attack
		ON_MELEE(32768);
		
		private final int _mask;
		
		private TriggerType(int mask)
		{
			_mask = mask;
		}
		
		public final boolean check(int event)
		{
			return (_mask & event) != 0; // Trigger (sub-)type contains event (sub-)type
		}
	}
	
	private final TriggerType _triggerType;
	
	private final int _chance;
	private final int _mindmg;
	private final byte[] _elements;
	private final boolean _pvpOnly;
	
	private ChanceCondition(TriggerType trigger, int chance, int mindmg, byte[] elements, boolean pvpOnly)
	{
		_triggerType = trigger;
		_chance = chance;
		_mindmg = mindmg;
		_elements = elements;
		_pvpOnly = pvpOnly;
	}
	
	public boolean isValid()
	{
		return _chance > 0;
	}
	
	public static ChanceCondition parse(StatsSet set)
	{
		if (!set.contains("chanceType") && !set.contains("activationChance") && !set.contains("activationElements")
				&& !set.contains("pvpChanceOnly"))
			return null;
		
		final TriggerType trigger = set.getEnum("chanceType", TriggerType.class);
		final int chance = set.getInteger("activationChance");
		final int mindmg = set.getInteger("activationMinDamage", -1);
		final String elements = set.getString("activationElements", null);
		final boolean pvpOnly = set.getBool("pvpChanceOnly", false);
		
		if (trigger != null && chance >= 0)
			return new ChanceCondition(trigger, chance, mindmg, parseElements(elements), pvpOnly);
		else
			throw new IllegalStateException();
	}
	
	private static byte[] parseElements(String list)
	{
		if (list == null)
			return null;
		
		String[] valuesSplit = list.split(",");
		byte[] elements = new byte[valuesSplit.length];
		for (int i = 0; i < valuesSplit.length; i++)
			elements[i] = Byte.parseByte(valuesSplit[i]);
		
		Arrays.sort(elements);
		return elements;
	}
	
	public boolean trigger(int event, int damage, byte element, boolean playable)
	{
		if (_pvpOnly && !playable)
			return false;
		
		if (_elements != null && Arrays.binarySearch(_elements, element) < 0)
			return false;
		
		// if the skill has "activationMinDamage" setted to higher than -1(default)
		// and if "activationMinDamage" is still higher than the recieved damage, the skill wont trigger
		if (_mindmg > -1 && _mindmg > damage)
			return false;
		
		return _triggerType.check(event) && Rnd.get(100) < _chance;
	}
	
	@Override
	public String toString()
	{
		return "Trigger[" + _chance + ";" + _triggerType.toString() + "]";
	}
}
