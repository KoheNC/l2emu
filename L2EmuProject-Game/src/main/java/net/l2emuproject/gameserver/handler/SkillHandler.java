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

import net.l2emuproject.gameserver.handler.skillhandlers.BalanceLife;
import net.l2emuproject.gameserver.handler.skillhandlers.BallistaBomb;
import net.l2emuproject.gameserver.handler.skillhandlers.BeastFeed;
import net.l2emuproject.gameserver.handler.skillhandlers.ChangeFace;
import net.l2emuproject.gameserver.handler.skillhandlers.CombatPointHeal;
import net.l2emuproject.gameserver.handler.skillhandlers.Continuous;
import net.l2emuproject.gameserver.handler.skillhandlers.CpDam;
import net.l2emuproject.gameserver.handler.skillhandlers.CpDamPercent;
import net.l2emuproject.gameserver.handler.skillhandlers.Craft;
import net.l2emuproject.gameserver.handler.skillhandlers.DeluxeKey;
import net.l2emuproject.gameserver.handler.skillhandlers.Detection;
import net.l2emuproject.gameserver.handler.skillhandlers.Disablers;
import net.l2emuproject.gameserver.handler.skillhandlers.DrainSoul;
import net.l2emuproject.gameserver.handler.skillhandlers.Dummy;
import net.l2emuproject.gameserver.handler.skillhandlers.Extractable;
import net.l2emuproject.gameserver.handler.skillhandlers.Fishing;
import net.l2emuproject.gameserver.handler.skillhandlers.FishingSkill;
import net.l2emuproject.gameserver.handler.skillhandlers.GetPlayer;
import net.l2emuproject.gameserver.handler.skillhandlers.GiveSp;
import net.l2emuproject.gameserver.handler.skillhandlers.GiveVitality;
import net.l2emuproject.gameserver.handler.skillhandlers.Harvest;
import net.l2emuproject.gameserver.handler.skillhandlers.Heal;
import net.l2emuproject.gameserver.handler.skillhandlers.InstantJump;
import net.l2emuproject.gameserver.handler.skillhandlers.LearnSkill;
import net.l2emuproject.gameserver.handler.skillhandlers.MakeKillable;
import net.l2emuproject.gameserver.handler.skillhandlers.MakeQuestDropable;
import net.l2emuproject.gameserver.handler.skillhandlers.ManaHeal;
import net.l2emuproject.gameserver.handler.skillhandlers.Manadam;
import net.l2emuproject.gameserver.handler.skillhandlers.Mdam;
import net.l2emuproject.gameserver.handler.skillhandlers.OpenDoor;
import net.l2emuproject.gameserver.handler.skillhandlers.Pdam;
import net.l2emuproject.gameserver.handler.skillhandlers.Recall;
import net.l2emuproject.gameserver.handler.skillhandlers.Resurrect;
import net.l2emuproject.gameserver.handler.skillhandlers.ShiftTarget;
import net.l2emuproject.gameserver.handler.skillhandlers.SiegeFlag;
import net.l2emuproject.gameserver.handler.skillhandlers.Soul;
import net.l2emuproject.gameserver.handler.skillhandlers.Sow;
import net.l2emuproject.gameserver.handler.skillhandlers.Spoil;
import net.l2emuproject.gameserver.handler.skillhandlers.StrSiegeAssault;
import net.l2emuproject.gameserver.handler.skillhandlers.SummonFriend;
import net.l2emuproject.gameserver.handler.skillhandlers.SummonTreasureKey;
import net.l2emuproject.gameserver.handler.skillhandlers.Sweep;
import net.l2emuproject.gameserver.handler.skillhandlers.TakeCastle;
import net.l2emuproject.gameserver.handler.skillhandlers.TakeFort;
import net.l2emuproject.gameserver.handler.skillhandlers.TransformDispel;
import net.l2emuproject.gameserver.handler.skillhandlers.Trap;
import net.l2emuproject.gameserver.handler.skillhandlers.Unlock;
import net.l2emuproject.gameserver.handler.skillhandlers.ZakenTeleport;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.formulas.Formulas;
import net.l2emuproject.gameserver.skills.l2skills.L2SkillDrain;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2CubicInstance;
import net.l2emuproject.util.EnumHandlerRegistry;
import net.l2emuproject.util.HandlerRegistry;

public final class SkillHandler extends EnumHandlerRegistry<L2SkillType, ISkillHandler>
{
	public static SkillHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private SkillHandler()
	{
		super(L2SkillType.class);
		
		registerSkillHandler(new BalanceLife());
		registerSkillHandler(new BallistaBomb());
		registerSkillHandler(new BeastFeed());
		registerSkillHandler(new ChangeFace());
		registerSkillHandler(new CombatPointHeal());
		registerSkillHandler(new Continuous());
		registerSkillHandler(new CpDam());
		registerSkillHandler(new CpDamPercent());
		registerSkillHandler(new Craft());
		registerSkillHandler(new DeluxeKey());
		registerSkillHandler(new Detection());
		registerSkillHandler(new Disablers());
		registerSkillHandler(new DrainSoul());
		registerSkillHandler(new Dummy());
		registerSkillHandler(new Extractable());
		registerSkillHandler(new Fishing());
		registerSkillHandler(new FishingSkill());
		registerSkillHandler(new GetPlayer());
		registerSkillHandler(new GiveSp());
		registerSkillHandler(new GiveVitality());
		registerSkillHandler(new Harvest());
		registerSkillHandler(new Heal());
		registerSkillHandler(new InstantJump());
		registerSkillHandler(new LearnSkill());
		registerSkillHandler(new MakeKillable());
		registerSkillHandler(new MakeQuestDropable());
		registerSkillHandler(new ManaHeal());
		registerSkillHandler(new Manadam());
		registerSkillHandler(new Mdam());
		registerSkillHandler(new OpenDoor());
		registerSkillHandler(new Pdam());
		registerSkillHandler(new Recall());
		registerSkillHandler(new Resurrect());
		registerSkillHandler(new ShiftTarget());
		registerSkillHandler(new SiegeFlag());
		registerSkillHandler(new Soul());
		registerSkillHandler(new Sow());
		registerSkillHandler(new Spoil());
		registerSkillHandler(new StrSiegeAssault());
		registerSkillHandler(new SummonFriend());
		registerSkillHandler(new SummonTreasureKey());
		registerSkillHandler(new Sweep());
		registerSkillHandler(new TakeCastle());
		registerSkillHandler(new TakeFort());
		registerSkillHandler(new TransformDispel());
		registerSkillHandler(new Trap());
		registerSkillHandler(new Unlock());
		registerSkillHandler(new ZakenTeleport());
		
		HandlerRegistry._log.info("SkillHandler: Loaded " + size() + " handlers.");
	}
	
	public void registerSkillHandler(ISkillHandler handler)
	{
		registerAll(handler, handler.getSkillIds());
	}
	
	public void useSkill(L2SkillType skillType, L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		final ISkillHandler handler = get(skillType);
		
		if (handler != null)
			handler.useSkill(activeChar, skill, targets);
		else
			skill.useSkill(activeChar, targets);
	}
	
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (activeChar.isAlikeDead())
			return;
		
		useSkill(skill.getSkillType(), activeChar, skill, targets);
		
		for (L2Character target : targets)
		{
			Formulas.calcLethalHit(activeChar, target, skill);
		}
		
		// Increase Charges, Souls, Etc
		if (activeChar instanceof L2Player)
		{
			((L2Player) activeChar).increaseChargesBySkill(skill);
			((L2Player) activeChar).increaseSoulsBySkill(skill);
		}
		
		skill.getEffectsSelf(activeChar);
		
		if (skill.isSuicideAttack())
			activeChar.doDie(activeChar);
	}
	
	public void useCubicSkill(L2CubicInstance cubic, L2Skill skill, L2Character... targets)
	{
		final ISkillHandler handler = get(skill.getSkillType());
		
		if (handler instanceof ICubicSkillHandler)
			((ICubicSkillHandler) handler).useCubicSkill(cubic, skill, targets);
		else if (skill instanceof L2SkillDrain)
			((L2SkillDrain) skill).useCubicSkill(cubic, targets);
		else if (handler != null)
			handler.useSkill(cubic.getOwner(), skill, targets);
		else
			skill.useSkill(cubic.getOwner(), targets);
	}
	
	public boolean checkConditions(L2Character activeChar, L2Skill skill)
	{
		final ISkillHandler handler = get(skill.getSkillType());
		
		if (handler instanceof ISkillConditionChecker)
			return ((ISkillConditionChecker) handler).checkConditions(activeChar, skill);
		
		return true;
	}
	
	public boolean checkConditions(L2Character activeChar, L2Skill skill, L2Character target)
	{
		final ISkillHandler handler = get(skill.getSkillType());
		
		if (handler instanceof ISkillConditionChecker)
			return ((ISkillConditionChecker) handler).checkConditions(activeChar, skill, target);
		
		return true;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final SkillHandler _instance = new SkillHandler();
	}
}
