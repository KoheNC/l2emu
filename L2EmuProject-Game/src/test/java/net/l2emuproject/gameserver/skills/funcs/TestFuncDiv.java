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

import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.skills.Stats;
import net.l2emuproject.gameserver.skills.funcs.FuncDiv;
import junit.framework.TestCase;


public class TestFuncDiv extends TestCase
{

    public void testFuncDivCalc()
    {
        FuncDiv fa = new FuncDiv(Stats.MAX_HP,1,null,2, null);
        
        Env env = new Env();
        env.setValue(1);
        fa.calc(env);
        assertEquals(0.5,env.getValue());
    }
    
    public void testFuncDivCalcDivByZero()
    {
        FuncDiv fa = new FuncDiv(Stats.MAX_HP,1,null,0, null);
        
        Env env = new Env();
        env.setValue(1);
        fa.calc(env);
        assertEquals(Double.POSITIVE_INFINITY,env.getValue());
    }

}
