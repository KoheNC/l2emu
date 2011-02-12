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
package net.l2emuproject.gameserver.skills.effects;

import net.l2emuproject.gameserver.model.L2Effect;
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.templates.effects.EffectTemplate;
import net.l2emuproject.gameserver.templates.skills.L2EffectType;

/**
 * <B>Once applied, this effect is active until:</B>
 * <LI>Time limit (<I>count * time</I>) expires</LI>
 * <LI>Manually removed</LI>
 * <BR>
 * <U>It will not be removed under any other circumstances!</U>
 * 
 * @author Savormix
 * @since 2009-04-25
 */
public class EffectEnvironment extends L2Effect
{
	public EffectEnvironment(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.model.L2Effect#getEffectType()
	 */
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.ENVIRONMENT;
	}
	
	@Override
	public final boolean canBeStoredInDb()
	{
		return false;
	}
}
