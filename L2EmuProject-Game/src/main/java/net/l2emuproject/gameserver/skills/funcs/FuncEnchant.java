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
package net.l2emuproject.gameserver.skills.funcs;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.skills.Stats;
import net.l2emuproject.gameserver.skills.conditions.Condition;
import net.l2emuproject.gameserver.templates.item.L2Item;
import net.l2emuproject.gameserver.templates.item.L2WeaponType;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class FuncEnchant extends Func
{
	public FuncEnchant(Stats pStat, int pOrder, FuncOwner pFuncOwner, double pLambda, Condition pCondition)
	{
		super(pStat, pOrder, pFuncOwner, pCondition);
	}
	
	@Override
	protected void calc(Env env)
	{
		final L2ItemInstance item = (L2ItemInstance)funcOwner;
		
		int enchant = item.getEnchantLevel();
		
		if (Config.ALT_OLY_ENCHANT_LIMIT >= 0)
			if (env.getPlayer() instanceof L2Player && ((L2Player)env.getPlayer()).getPlayerOlympiad().isInOlympiadMode())
				enchant = Math.min(Config.ALT_OLY_ENCHANT_LIMIT, enchant);
		
		if (enchant > 0)
			env.setValue(env.getValue() + getEnchantAddition(Math.min(enchant, 3), Math.max(0, enchant - 3), item.getItem()));
	}
	
	private int getEnchantAddition(int enchant, int overEnchant, L2Item item)
	{
		switch (stat)
		{
			case MAGIC_DEFENCE:
			case POWER_DEFENCE:
			case SHIELD_DEFENCE:
			{
				return 1 * enchant + 3 * overEnchant;
			}
			case MAGIC_ATTACK:
			{
				switch (item.getCrystalGrade())
				{
					case L2Item.CRYSTAL_S:
					{
						return 4 * enchant + 8 * overEnchant;
					}
					case L2Item.CRYSTAL_A:
					case L2Item.CRYSTAL_B:
					case L2Item.CRYSTAL_C:
					{
						return 3 * enchant + 6 * overEnchant;
					}
					case L2Item.CRYSTAL_D:
					case L2Item.CRYSTAL_NONE:
					{
						return 2 * enchant + 4 * overEnchant;
					}
				}
				break;
			}
			case POWER_ATTACK:
			{
				final boolean isBow = ((L2WeaponType)item.getItemType()).isBowType();
				
				switch (item.getCrystalGrade())
				{
					case L2Item.CRYSTAL_S:
					{
						if (isBow)
							return 10 * enchant + 20 * overEnchant;
						else
							return 5 * enchant + 10 * overEnchant;
					}
					case L2Item.CRYSTAL_A:
					{
						if (isBow)
							return 8 * enchant + 16 * overEnchant;
						else
							return 4 * enchant + 8 * overEnchant;
					}
					case L2Item.CRYSTAL_B:
					case L2Item.CRYSTAL_C:
					{
						if (isBow)
							return 6 * enchant + 12 * overEnchant;
						else
							return 3 * enchant + 6 * overEnchant;
					}
					case L2Item.CRYSTAL_D:
					case L2Item.CRYSTAL_NONE:
					{
						if (isBow)
							return 4 * enchant + 8 * overEnchant;
						else
							return 2 * enchant + 4 * overEnchant;
					}
				}
				break;
			}
		}
		
		throw new IllegalStateException(stat + " " + order + " " + funcOwner);
	}
}
