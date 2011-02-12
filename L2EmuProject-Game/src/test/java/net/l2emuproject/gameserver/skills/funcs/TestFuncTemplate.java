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
import net.l2emuproject.gameserver.skills.conditions.Condition;
import net.l2emuproject.gameserver.skills.conditions.ConditionLogicOr;
import net.l2emuproject.gameserver.skills.funcs.Func;
import net.l2emuproject.gameserver.skills.funcs.FuncTemplate;
import junit.framework.TestCase;


public class TestFuncTemplate extends TestCase
{
    public void testInstantiateGoodFunc ()
    {
        Condition clo = new ConditionLogicOr();
        try
        {
            new FuncTemplate(clo,"Add",Stats.MAX_HP, 1, 2);
        }
        catch (RuntimeException e)
        {
            fail (e.getMessage());
        }
    }
    
    public void testInstantiateInexistantFunc ()
    {
        Condition clo = new ConditionLogicOr();
        try
        {
            new FuncTemplate(clo,"FuncNotExistant",Stats.MAX_HP, 1, 2);
            fail ("Function should be not found");
        }
        catch (RuntimeException e)
        {
            assertNotNull(e);
        }
    }
	
    
    public void testExecuteFuncWithNoConditions ()
    {
        Condition clo = null;
        try
        {
            FuncTemplate fa = new FuncTemplate(clo,"Add",Stats.MAX_HP, 1, 2);
            Env env = new Env();
            env.value=1;
            Func f = fa.getFunc(null);
            f.calc(env);
            assertEquals(3.0,env.value);
        }
        catch (RuntimeException e)
        {
            fail (e.getMessage());
        }
    }

}
