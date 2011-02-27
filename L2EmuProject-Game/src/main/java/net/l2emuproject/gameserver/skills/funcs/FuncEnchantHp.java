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

import net.l2emuproject.gameserver.datatables.EnchantHPBonusData;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.skills.Stats;
import net.l2emuproject.gameserver.skills.conditions.Condition;

/**
 * @author Yamaneko
 */
public final class FuncEnchantHp extends Func
{
	public FuncEnchantHp(Stats pStat, int pOrder, FuncOwner pFuncOwner, double pLambda, Condition pCondition)
	{
		super(pStat, pOrder, pFuncOwner, pCondition);
	}
	
	@Override
	public void calc(Env env)
	{
		final L2ItemInstance item = (L2ItemInstance)funcOwner;
		
		if (item.getEnchantLevel() > 0)
			env.setValue(env.getValue() + EnchantHPBonusData.getInstance().getHPBonus(item));
	}
}
