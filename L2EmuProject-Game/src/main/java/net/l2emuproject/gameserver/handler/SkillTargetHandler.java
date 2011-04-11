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
package net.l2emuproject.gameserver.handler;

import net.l2emuproject.gameserver.handler.skilltargethandlers.*;
import net.l2emuproject.gameserver.skills.SkillTargetTypes;
import net.l2emuproject.util.EnumHandlerRegistry;

/**
 * @author Intrepid
 *
 */
public final class SkillTargetHandler extends EnumHandlerRegistry<SkillTargetTypes, ISkillTargetHandler>
{
	public static SkillTargetHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private SkillTargetHandler()
	{
		super(SkillTargetTypes.class);
		
		registerSkillTargetHandler(new TargetAreaHandler());
		registerSkillTargetHandler(new TargetAuraHandler());
		registerSkillTargetHandler(new TargetClanHandler());
		registerSkillTargetHandler(new TargetCorpseHandler());
		registerSkillTargetHandler(new TargetObjectHandler());
		registerSkillTargetHandler(new TargetPartyHandler());
		registerSkillTargetHandler(new TargetPetHandler());
		registerSkillTargetHandler(new TargetPlayerHandler());
		registerSkillTargetHandler(new TargetSpecialHandler());
		
		_log.info(getClass().getSimpleName() + " : Loaded " + size() + " handler(s).");
	}
	
	public void registerSkillTargetHandler(ISkillTargetHandler handler)
	{
		registerAll(handler, handler.getSkillTargetTypes());
	}
	
	public ISkillTargetHandler getSkillTargetHandler(SkillTargetTypes targetType)
	{
		return get(targetType);
	}
	
	@SuppressWarnings("synthetic-access")
	private static final class SingletonHolder
	{
		private static final SkillTargetHandler INSTANCE = new SkillTargetHandler();
	}
}
