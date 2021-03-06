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

import java.util.ArrayList;
import java.util.List;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.datatables.SkillTreeTable;
import net.l2emuproject.gameserver.handler.ISkillTargetHandler;
import net.l2emuproject.gameserver.handler.SkillTargetHandler;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.FlyToLocation.FlyType;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.clan.L2Clan;
import net.l2emuproject.gameserver.services.party.L2Party;
import net.l2emuproject.gameserver.skills.conditions.Condition;
import net.l2emuproject.gameserver.skills.formulas.Formulas;
import net.l2emuproject.gameserver.skills.funcs.Func;
import net.l2emuproject.gameserver.skills.funcs.FuncOwner;
import net.l2emuproject.gameserver.skills.funcs.FuncTemplate;
import net.l2emuproject.gameserver.skills.skilllearn.L2EnchantSkillLearn;
import net.l2emuproject.gameserver.skills.skilllearn.L2EnchantSkillLearn.EnchantSkillDetail;
import net.l2emuproject.gameserver.system.restriction.global.GlobalRestrictions;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.templates.StatsSet;
import net.l2emuproject.gameserver.templates.effects.EffectTemplate;
import net.l2emuproject.gameserver.templates.item.L2Weapon;
import net.l2emuproject.gameserver.templates.item.L2WeaponType;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.object.L2Attackable;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Playable;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.L2Summon;
import net.l2emuproject.gameserver.world.object.instance.L2CubicInstance;
import net.l2emuproject.gameserver.world.object.instance.L2DoorInstance;
import net.l2emuproject.gameserver.world.object.instance.L2MonsterInstance;
import net.l2emuproject.gameserver.world.object.instance.L2SiegeFlagInstance;
import net.l2emuproject.gameserver.world.zone.L2Zone;
import net.l2emuproject.lang.L2Integer;
import net.l2emuproject.lang.L2System;
import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.util.ArrayBunch;
import net.l2emuproject.util.L2Arrays;
import net.l2emuproject.util.concurrent.ForEachExecutable;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class L2Skill implements FuncOwner, IChanceSkillTrigger
{
	public static final L2Skill[]	EMPTY_ARRAY					= new L2Skill[0];

	protected static final Log		_log						= LogFactory.getLog(L2Skill.class);

	public static final int			SKILL_CUBIC_MASTERY			= 143;
	public static final int			SKILL_LUCKY					= 194;
	public static final int			SKILL_CREATE_COMMON			= 1320;
	public static final int			SKILL_CREATE_DWARVEN		= 172;
	public static final int			SKILL_EXPERTISE				= 239;
	public static final int			SKILL_CRYSTALLIZE			= 248;
	public static final int			SKILL_DIVINE_INSPIRATION	= 1405;
	public static final int			SKILL_SOUL_MASTERY			= 467;
	public static final int			SKILL_CLAN_LUCK				= 390;
	

	public static enum SkillOpType
	{
		OP_PASSIVE, OP_ACTIVE, OP_TOGGLE
	}

	private static enum OffensiveState
	{
		OFFENSIVE, NEUTRAL, POSITIVE;
	}

	// conditional values
	public final static int			COND_BEHIND		= 0x0008;
	public final static int			COND_CRIT		= 0x0010;

	// these two build the primary key
	private final Integer			_id;
	private final short				_level;

	/** Identifier for a skill that client can't display */
	private final int				_displayId;

	// not needed, just for easier debug
	private final String			_name;

	// Reference ID for extractable items
	private final short				_refId;

	private final SkillOpType		_operateType;
	private final boolean			_magic;
	private final boolean			_itemSkill;
	private final boolean			_staticReuse;
	private final boolean			_staticHitTime;
	private final short				_mpConsume;
	private final short				_mpInitialConsume;
	private final short				_hpConsume;
	private final short				_cpConsume;

	private final short				_itemConsume;
	private final short				_itemConsumeId;

	private final short				_targetConsume;
	private final short				_targetConsumeId;

	private final short				_feed;

	private final short				_castRange;
	private final short				_effectRange;

	// Abnormal levels for skills and their canceling, e.g. poison vs negate
	private final byte				_abnormalLvl;				// e.g. poison or bleed lvl 2
	// Note: see also _effectAbnormalLvl
	private final byte				_negateLvl;				// abnormalLvl is negated with negateLvl
	private final short[]				_negateId;					// cancels the effect of skill ID
	private final boolean			_negatePhysicalOnly;	// cancel physical effects only
	private final L2SkillType[]		_negateStats;			// lists the effect types that are canceled
	private final byte				_maxNegatedEffects;		// maximum number of effects to negate

	// all times in milliseconds
	private final int				_hitTime;
	private final int				_skillInterruptTime;
	private final int				_coolTime;
	private final int				_reuseDelay;
	// for item skills delay on equip
	private final int				_equipDelay;

	/** Target type of the skill : SELF, PARTY, CLAN, PET... */
	private final SkillTargetTypes	_targetType;
	// base success chance
	private final double			_power;
	private final double			_pvpPower;
	private final double 			_pvePower;
	private final byte				_levelDepend;
	private final boolean			_ignoreResists;

	// Kill by damage over time
	private final boolean			_killByDOT;

	// Effecting area of the skill, in radius.
	// The radius center varies according to the _targetType:
	// "caster" if targetType = AURA/PARTY/CLAN or "target" if targetType = AREA
	private final short				_skillRadius;

	private final L2SkillType		_skillType;
	private final L2SkillType		_effectType;				// additional effect has a type
	private final byte				_effectAbnormalLvl;		// abnormal level for the additional effect type, e.g. poison lvl 1
	private final byte				_effectPower;
	private final short				_effectId;
	private final float				_effectLvl;				// normal effect level
	private final byte				_skill_landing_percent;

	private final boolean			_isPotion;
	private final byte				_element;
	private final short				_elementPower;
	private final byte				_activateRate;
	private final byte				_magicLevel;

	private final short				_condition;
	private final boolean			_overhit;
	private final boolean			_ignoreShld;
	private final int				_weaponsAllowed;
	private final int				_armorsAllowed;

	private final OffensiveState	_offensiveState;

	private final byte				_needCharges;
	private final byte				_giveCharges;
	private final byte				_maxCharges;

	private final ChanceCondition	_chanceCondition;
	private final TriggeredSkill	_triggeredSkill;

	private final byte				_soulConsume;
	private final byte				_soulMaxConsume;
	private final byte				_numSouls;
	private final int				_expNeeded;
	private final byte				_critChance;

	//Stats for transformation skills
	private final short				_transformId;

	private final byte				_baseCritRate;				// percent of success for skill critical hit (especially for PDAM & BLOW -
	// they're not affected by rCrit values or buffs). Default loads -1 for all
	// other skills but 0 to PDAM & BLOW
	private final byte				_lethalEffect1;			// percent of success for lethal 1st effect (hit cp to 1 or if mob hp to 50%) (only
	// for PDAM skills)
	private final byte				_lethalEffect2;			// percent of success for lethal 2nd effect (hit cp,hp to 1 or if mob hp to 1) (only
	// for PDAM skills)
	private final boolean			_directHpDmg;				// If true then dmg is being make directly
	private final boolean			_isDance;					// If true then casting more dances will cost more MP
	private final boolean			_isSong;					// If true then casting more songs will cost more MP
	private final short				_nextDanceCost;
	private final float				_sSBoost;					// If true skill will have SoulShot boost (power*2)

	private final float				_timeMulti;

	private final String			_attribute;

	private final byte				_minPledgeClass;

	private final int				_aggroPoints;

	private Condition				_preCondition;
	private FuncTemplate[]			_funcTemplates;
	private EffectTemplate[]		_effectTemplates;
	private EffectTemplate[]		_effectTemplatesSelf;

	// Flying support
	private final FlyType			_flyType;
	private final int				_flyRadius;
	private final float				_flyCourse;

	private final boolean			_isDebuff;

	private final byte				_afroId;
	private final boolean			_isHerbEffect;

	private final boolean			_ignoreShield;
	private final boolean			_isSuicideAttack;
	private final boolean			_canBeReflected;
	private final boolean			_canBeDispeled;
	private final boolean 			_isClanSkill;
	private final boolean			_dispelOnAction;
	private final boolean			_dispelOnAttack;
	private final short				_afterEffectId;
	private final byte				_afterEffectLvl;

	private final boolean			_stayAfterDeath;					// skill should stay after death

	private final boolean			_sendToClient;
	private final float				_pvpPowerMulti;


	// Talisman Id this skill belongs to
	private final short				_talismanManaConsumeOnSkill;
	// Amount of mana consumed on talisman when using this skill. On official, it's ALWAYS equal to the skill reuse in minute
	private final short				_belongingTalismanId;

	public L2Skill(final StatsSet set)
	{
		_id = L2Integer.valueOf(set.getInteger("skill_id"));
		_level = set.getShort("level");
		_refId = set.getShort("referenceId", set.getShort("itemConsumeId", (short) 0));
		_afroId = set.getByte("afroId", (byte) 0);
		_displayId = set.getInteger("displayId", _id);
		_name = set.getString("name").intern();
		_skillType = set.getEnum("skillType", L2SkillType.class);
		_operateType = set.getEnum("operateType", SkillOpType.class);
		_targetType = set.getEnum("target", SkillTargetTypes.class);
		_magic = set.getBool("isMagic", isSkillTypeMagic());
		_itemSkill = set.getBool("isItem", 3080 <= getId() && getId() <= 3259);
		_isPotion = set.getBool("isPotion", false);
		_staticReuse = set.getBool("staticReuse", false);
		_staticHitTime = set.getBool("staticHitTime", false);
		_mpConsume = set.getShort("mpConsume", (short) 0);
		_mpInitialConsume = set.getShort("mpInitialConsume", (short) 0);
		_hpConsume = set.getShort("hpConsume", (short) 0);
		_cpConsume = set.getShort("cpConsume", (short) 0);
		_itemConsume = set.getShort("itemConsumeCount", (short) 0);
		_itemConsumeId = set.getShort("itemConsumeId", (short) 0);
		_targetConsume = set.getShort("targetConsumeCount", (short) 0);
		_targetConsumeId = set.getShort("targetConsumeId", (short) 0);
		_afterEffectId = set.getShort("afterEffectId", (short) 0);
		_afterEffectLvl = set.getByte("afterEffectLvl", (byte) 1);

		_isHerbEffect = _name.contains("Herb");

		_castRange = set.getShort("castRange", (short) 0);
		_effectRange = set.getShort("effectRange", (short) -1);

		_abnormalLvl = set.getByte("abnormalLvl", (byte) -1);
		_effectAbnormalLvl = set.getByte("effectAbnormalLvl", (byte) -1); // support for a separate effect abnormal lvl, e.g. poison inside a different skill
		_negateLvl = set.getByte("negateLvl", (byte) -1);
		final String str = set.getString("negateStats", "");
		
		if (str == "")
			_negateStats = new L2SkillType[0];
		else
		{
			final String[] stats = str.split(" ");
			final L2SkillType[] array = new L2SkillType[stats.length];
			
			for (int i = 0; i < stats.length; i++)
			{
				L2SkillType type = null;
				try
				{
					type = Enum.valueOf(L2SkillType.class, stats[i]);
				}
				catch (final Exception e)
				{
					throw new IllegalArgumentException("SkillId: " + _id + " Enum value of type "
							+ L2SkillType.class.getName() + " required, but found: " + stats[i]);
				}
				
				array[i] = type;
			}
			_negateStats = array;
		}
		
		final String negateId = set.getString("negateId", null);
		if (negateId != null)
		{
			final String[] valuesSplit = negateId.split(",");
			_negateId = new short[valuesSplit.length];
			for (int i = 0; i < valuesSplit.length; i++)
			{
				_negateId[i] = Short.parseShort(valuesSplit[i]);
			}
		}
		else
			_negateId = new short[0];
		
		_negatePhysicalOnly = set.getBool("negatePhysicalOnly", false);
		
		_maxNegatedEffects = set.getByte("maxNegated", (byte) 0);
		_stayAfterDeath = set.getBool("stayAfterDeath", false);
		_killByDOT = set.getBool("killByDOT", false);

		_hitTime = set.getInteger("hitTime", 0);
		_coolTime = set.getInteger("coolTime", 0);
		_skillInterruptTime = set.getInteger("interruptTime", Math.min(_hitTime, 500));
		_reuseDelay = set.getInteger("reuseDelay", 0);
		_equipDelay = set.getInteger("equipDelay", 0);

		_isDance = set.getBool("isDance", false);
		_isSong = set.getBool("isSong", false);
		
		// L2EMU_EDIT: START Buff time configs
		if (SkillTable.isProphetBuff(_id))
			_timeMulti = Config.ALT_PROPHET_BUFF_TIME;
		else if (SkillTable.isDance(_id))
			_timeMulti = Config.ALT_DANCE_TIME;
		else if (SkillTable.isSong(_id))
			_timeMulti = Config.ALT_SONG_TIME;
		else if (SkillTable.isCubic(_id))
			_timeMulti = Config.ALT_CUBIC_TIME;
		else if (SkillTable.isHeroBuff(_id))
			_timeMulti = Config.ALT_HERO_BUFF_TIME;
		else if (SkillTable.isNobleBuff(_id))
			_timeMulti = Config.ALT_NOBLE_BUFF_TIME;
		else if (SkillTable.isSummonBuff(_id))
			_timeMulti = Config.ALT_SUMMON_BUFF_TIME;
		else if (SkillTable.isOrcBuff(_id))
			_timeMulti = Config.ALT_ORC_BUFF_TIME;
		else if (SkillTable.isOtherBuff(_id))
			_timeMulti = Config.ALT_OTHER_BUFF_TIME;
		else if (SkillTable.isVitalityBuff(_id))
			_timeMulti = Config.ALT_VITALITY_BUFF_TIME;
		else
			_timeMulti = 1; //If the skills is not a DANCE type skill or BUFF type, the effect time is the normal, without any multiplier
		// L2EMU_EDIT: END

		_skillRadius = set.getShort("skillRadius", (short) 80);

		_power = set.getFloat("power", 0.f);
		_pvpPower = set.getFloat("pvpPower", (float)getPower());
		_pvePower = set.getFloat("pvePower", (float)getPower());

		_levelDepend = set.getByte("lvlDepend", (byte) 1);
		_ignoreResists = set.getBool("ignoreResists", false);

		_feed = set.getShort("feed", (short) 0); // Used for pet food

		_effectType = set.getEnum("effectType", L2SkillType.class, null);
		_effectPower = set.getByte("effectPower", (byte) 0);
		_effectId = set.getShort("effectId", (short) 0);
		_effectLvl = set.getFloat("effectLevel", 0.f);
		_skill_landing_percent = set.getByte("skill_landing_percent", (byte) 0);
		_element = set.getByte("element", (byte) -1);
		_elementPower = set.getShort("elementPower", (short) 0);
		_activateRate = set.getByte("activateRate", (byte) -1);
		_magicLevel = (byte) initMagicLevel(set);

		_ignoreShld = set.getBool("ignoreShld", false);
		_condition = set.getShort("condition", (short) 0);
		_overhit = set.getBool("overHit", false);
		_isSuicideAttack = set.getBool("isSuicideAttack", false);
		_weaponsAllowed = set.getInteger("weaponsAllowed", 0);
		_armorsAllowed = set.getInteger("armorsAllowed", 0);

		_needCharges = set.getByte("needCharges", (byte) 0);
		_giveCharges = set.getByte("giveCharges", (byte) 0);
		_maxCharges = set.getByte("maxCharges", (byte) 0);

		_minPledgeClass = set.getByte("minPledgeClass", (byte) 0);

		final ChanceCondition chanceCondition = ChanceCondition.parse(set);
		final TriggeredSkill triggeredSkill = TriggeredSkill.parse(set);
		
		if (isValid(chanceCondition, triggeredSkill))
		{
			_chanceCondition = chanceCondition;
			_triggeredSkill = triggeredSkill;
		}
		else
		{
			_chanceCondition = null;
			_triggeredSkill = null;
		}

		_offensiveState = getOffensiveState(set);
		_isDebuff = set.getBool("isDebuff", false/*isOffensive()*/);

		_numSouls = set.getByte("num_souls", (byte) 0);
		_soulConsume = set.getByte("soulConsumeCount", (byte) 0);
		_soulMaxConsume = set.getByte("soulMaxConsumeCount", (byte) 0);
		_expNeeded = set.getInteger("expNeeded", 0);
		_critChance = set.getByte("critChance", (byte) 0);

		// Stats for transformation Skill
		_transformId = set.getShort("transformId", (short) 0);

		_baseCritRate = set.getByte("baseCritRate", (byte) ((_skillType == L2SkillType.PDAM || _skillType == L2SkillType.BLOW) ? 0 : -1));
		_lethalEffect1 = set.getByte("lethal1", (byte) 0);
		_lethalEffect2 = set.getByte("lethal2", (byte) 0);
		_directHpDmg = set.getBool("dmgDirectlyToHp", false);
		_nextDanceCost = set.getShort("nextDanceCost", (short) 0);
		_sSBoost = set.getFloat("SSBoost", 2);

		_aggroPoints = set.getInteger("aggroPoints", 0);

		_flyType = set.getEnum("flyType", FlyType.class, null);
		_flyRadius = set.getInteger("flyRadius", 200);
		_flyCourse = set.getFloat("flyCourse", 0);
		_canBeReflected = set.getBool("canBeReflected", true);
		_canBeDispeled = set.getBool("canBeDispeled", true);
		_isClanSkill = set.getBool("isClanSkill", false);
		_dispelOnAction = set.getBool("dispelOnAction", false);
		_dispelOnAttack = set.getBool("dispelOnAttack", false);
		_attribute = set.getString("attribute", "");
		_ignoreShield = set.getBool("ignoreShld", false);
		_sendToClient = set.getBool("sendToClient", true);
		_pvpPowerMulti = set.getFloat("pvpPowerMulti", 1);

		_belongingTalismanId = set.getShort("talismanId", (short) 0);
		_talismanManaConsumeOnSkill = set.getShort("talismanManaConsume", (short) 0);
	}
	
	private int initMagicLevel(final StatsSet set)
	{
		final byte normalLevel = set.getByte("magicLvl", (byte) SkillTreeTable.getInstance().getMinSkillLevel(_id, _level));
		
		// normal skills
		if (getLevel() < 100)
			return normalLevel;
		
		// enchanted skills
		final L2EnchantSkillLearn esl = SkillTreeTable.getInstance().getSkillEnchantmentBySkillId(getId());
		
		if (esl == null)
			return -1;
		
		final List<EnchantSkillDetail> route = esl.getEnchantRoutes()[L2EnchantSkillLearn.getEnchantType(getLevel())];
		
		if (route == null)
			return -1;
		
		int minMagicLevel = SkillTreeTable.getInstance().getMinSkillLevel(getId(), 1);
		
		if (minMagicLevel == 0)
			minMagicLevel = normalLevel;
		
		if (minMagicLevel != 0)
		{
			if (route.size() == 15 && minMagicLevel > 75)
			{
				return 81 + ((getLevel() % 100) - 1) / 3;
			}
			else if (route.size() == 30 && minMagicLevel <= 75)
			{
				return 76 + ((getLevel() % 100) - 1) / 3;
			}
		}
		
		_log.warn("Invalid skill enchants (route.size(): " + route.size() + ") for " + this);
		return -1;
	}
	
	private static boolean isValid(final ChanceCondition chanceCondition, final TriggeredSkill triggeredSkill)
	{
		if (chanceCondition == null)
			return triggeredSkill == null;
		
		if (!chanceCondition.isValid())
			return false;
		
		return triggeredSkill == null || triggeredSkill.isValid();
	}
	
	private boolean isPurePassiveSkill()
	{
		return isPassive() && !isChance();
	}
	
	private boolean isPureChanceSkill()
	{
		return isChance() && getTriggeredSkill() != null;
	}
	
	public void validate() throws Exception
	{
		validateEffectsAndFuncs();
		validateMpConsume();
		validateToggle();
		validateOffensiveAndDebuffState();
		validateTriggeredSkill();
	}
	
	private void validateEffectsAndFuncs()
	{
		if (isPassive())
			if (_effectTemplates != null || _effectTemplatesSelf != null)
				if (_skillType != L2SkillType.NOTDONE)
					if (_chanceCondition == null || _triggeredSkill != null)
						throw new IllegalStateException(toString());
		
		if (!isPassive())
			if (_funcTemplates != null)
				throw new IllegalStateException(toString());
	}
	
	private void validateMpConsume() throws Exception
	{
		if (isToggle() && getMpConsume() != 0) // toggle skills consume the full mp on initial
			throw new IllegalStateException(toString());
	}
	
	private void validateToggle() throws Exception
	{
		if (!isToggle())
			return;
		
		if (getTargetType() != SkillTargetTypes.TARGET_SELF)
			throw new IllegalStateException(toString());
		
		if (getSkillType() != L2SkillType.CONT)
			throw new IllegalStateException(toString());
		
		if (getHitTime() != 0 || getSkillInterruptTime() != 0 || getCoolTime() != 0 || getReuseDelay() != 0)
			throw new IllegalStateException(toString());
		
		if (_effectTemplatesSelf != null)
			throw new IllegalStateException(toString());
		
		if (_effectTemplates == null || _effectTemplates.length != 1)
			throw new IllegalStateException(toString());
		
		if (_effectTemplates[0].count != Integer.MAX_VALUE)
			throw new IllegalStateException(toString());
	}
	
	private void validateOffensiveAndDebuffState() throws Exception
	{
		if (getSkillType() != L2SkillType.NOTDONE || getTargetType() != SkillTargetTypes.TARGET_NONE)
			if (!isOffensive() && isDebuff())
				throw new IllegalStateException(toString());
		
		if (isBuff() && isDebuff())
			throw new IllegalStateException(toString());
	}
	
	private void validateTriggeredSkill() throws Exception
	{
		// must have triggered skill
		if (isChance())
		{
			if (getTriggeredSkill() != null)
			{
				final L2Skill triggeredSkill = getTriggeredSkill().getTriggeredSkill();
				
				if (triggeredSkill == null)
					throw new IllegalStateException(toString());
				
				if (triggeredSkill == this)
					throw new IllegalStateException(toString());
			}
		}
		// can't have triggered skill
		else
		{
			if (getChanceCondition() != null)
				throw new IllegalStateException(toString());
			
			if (getTriggeredSkill() != null)
				throw new IllegalStateException(toString());
		}
	}

	private OffensiveState getOffensiveState(final StatsSet set)
	{
		final OffensiveState defaultState = getDefaultOffensiveState();
		
		final Boolean isOffensive = set.contains("offensive") ? set.getBool("offensive") : null;
		final Boolean isNeutral = set.contains("neutral") ? set.getBool("neutral") : null;
		
		if (isOffensive == null && isNeutral == null)
			return defaultState;
		
		if (isPurePassiveSkill() || isPureChanceSkill() || isToggle())
			throw new IllegalStateException(this + " shouldn't have 'offensive'/'neutral' property specified!");
		
		final List<OffensiveState> denied = new ArrayList<OffensiveState>(2);
		final List<OffensiveState> requested = new ArrayList<OffensiveState>(2);
		
		if (isOffensive != null)
		{
			if (isOffensive.booleanValue())
				requested.add(OffensiveState.OFFENSIVE);
			else
				denied.add(OffensiveState.OFFENSIVE);
		}
		
		if (isNeutral != null)
		{
			if (isNeutral.booleanValue())
				requested.add(OffensiveState.NEUTRAL);
			else
				denied.add(OffensiveState.NEUTRAL);
		}
		
		switch (requested.size())
		{
			case 2:
				throw new IllegalStateException("Both 'neutral' and 'offensive' property requested for " + this);
			case 1:
				return requested.get(0);
			case 0:
				if (!denied.contains(defaultState))
					return defaultState;
				//$FALL-THROUGH$
			default:
				throw new IllegalStateException("Requested 'neutral'/'offensive' value rules out default for " + this);
		}
	}

	public void useSkill(final L2Character caster, final L2Character... targets)
	{
		caster.sendPacket(ActionFailed.STATIC_PACKET);

		if (caster instanceof L2Player)
			((L2Player)caster).sendMessage("Skill not implemented. Skill ID: " + getId() + " " + getSkillType());
	}

	public final boolean isPotion()
	{
		return _isPotion;
	}

	public final int getArmorsAllowed()
	{
		return _armorsAllowed;
	}

	public final L2SkillType getSkillType()
	{
		return _skillType;
	}

	public final boolean hasEffectWhileCasting()
	{
		return getSkillType() == L2SkillType.FUSION || getSkillType() == L2SkillType.SIGNET_CASTTIME;
	}

	public final int getActivateRate()
	{
		return _activateRate;
	}

	public final int getMagicLevel()
	{
		return _magicLevel;
	}

	public final byte getElement()
	{
		return _element;
	}

	/**
	 * Return the target type of the skill : SELF, PARTY, CLAN, PET...<BR>
	 * <BR>
	 */
	public final SkillTargetTypes getTargetType()
	{
		return _targetType;
	}

	public final int getCondition()
	{
		return _condition;
	}

	public final boolean ignoreShld()
	{
		return _ignoreShld;
	}

	public final boolean isOverhit()
	{
		return _overhit;
	}

	public final boolean killByDOT()
	{
		return _killByDOT;
	}

	public final boolean isSuicideAttack()
	{
		return _isSuicideAttack;
	}

	/**
	 * Return the power of the skill.<BR><BR>
	 */
	public final double getPower(L2Character activeChar, L2Character target, boolean isPvP, boolean isPvE)
	{
		if (activeChar == null)
			return getPower(isPvP, isPvE);
		
		switch (_skillType)
		{
			case DEATHLINK:			
				return getPower(isPvP, isPvE) * Math.pow(1.7165 - activeChar.getCurrentHp() / activeChar.getMaxHp(), 2) * 0.577;
				/*
				 * DrHouse:
				 * Rolling back to old formula (look below) for DEATHLINK due to this one based on logarithm is not
				 * accurate enough. Commented here because probably is a matter of just adjusting a constant
    			if(activeChar.getCurrentHp() / activeChar.getMaxHp() > 0.005)
            		return _power*(-0.45*Math.log(activeChar.getCurrentHp()/activeChar.getMaxHp())+1.);
            	else
            		return _power*(-0.45*Math.log(0.005)+1.);
				 */		
			case FATALCOUNTER:			
				return getPower(isPvP, isPvE)*3.5*(1-target.getCurrentHp()/target.getMaxHp());			
			default:
				return getPower(isPvP, isPvE);
		}
	}
	
	public final double getPower()
	{
		return _power;
	}
	
	public final double getPower(boolean isPvP, boolean isPvE)
	{
		return isPvE ? _pvePower : isPvP ? _pvpPower : _power;
	}


	public final L2SkillType[] getNegateStats()
	{
		return _negateStats;
	}

	public final int getAbnormalLvl()
	{
		return _abnormalLvl;
	}

	public final int getNegateLvl()
	{
		return _negateLvl;
	}

	public final short[] getNegateId()
	{
		return _negateId;
	}
	
	public final boolean getNegatePhysicalOnly()
	{
		return _negatePhysicalOnly;
	}

	public final int getMaxNegatedEffects()
	{
		return _maxNegatedEffects;
	}

	public final int getEffectAbnormalLvl()
	{
		return _effectAbnormalLvl;
	}

	protected final TriggeredSkill getTriggeredSkill()
	{
		return _triggeredSkill;
	}

	public final int getLevelDepend()
	{
		return _levelDepend;
	}

	/**
	 * Return the skill landing percent probability.<BR>
	 * <BR>
	 */
	public final int getLandingPercent()
	{
		return _skill_landing_percent;
	}

	/**
	 * Return the additional effect power or base probability.<BR>
	 * <BR>
	 */
	public final double getEffectPower()
	{
		if (_effectTemplates != null)
			for (final EffectTemplate et : _effectTemplates)
				if (et.effectPower > 0)
					return et.effectPower;

		if (_effectPower > 0)
			return _effectPower;

		// to let damage dealing skills having proper resist even without specified effectPower
		switch (_skillType.getRoot())
		{
			case PDAM:
				return 20;
			case MDAM:
				return 20;
			default:
				// to let debuffs succeed even without specified power
				return (_power <= 0 || 100 < _power) ? 20 : _power;
		}
	}

	/**
	 * Return true if skill should ignore all resistances
	 */
	public final boolean ignoreResists()
	{
		return _ignoreResists;
	}

	/**
	 * Return the additional effect Id.<BR>
	 * <BR>
	 */
	public final int getEffectId()
	{
		return _effectId;
	}

	/**
	 * Return the additional effect level.<BR>
	 * <BR>
	 */
	public final float getEffectLvl()
	{
		return _effectLvl;
	}

	/**
	 * Return the additional effect skill type (ex : STUN, PARALYZE,...).<BR>
	 * <BR>
	 */
	public final L2SkillType getEffectType()
	{
		if (_effectTemplates != null)
			for (final EffectTemplate et : _effectTemplates)
				if (et.effectType != null)
					return et.effectType;

		if (_effectType != null)
			return _effectType;

		// to let damage dealing skills having proper resist even without specified effectType
		switch (_skillType.getRoot())
		{
			case PDAM:
				return L2SkillType.STUN;
			case MDAM:
				return L2SkillType.PARALYZE;
			default:
				return _skillType;
		}
	}

	/**
	 * @return Returns the timeMulti.
	 */
	public final float getTimeMulti()
	{
		return _timeMulti;
	}

	/**
	 * @return Returns the castRange.
	 */
	public final int getCastRange()
	{
		return _castRange;
	}

	/**
	 * @return Returns the effectRange.
	 */
	public final int getEffectRange()
	{
		return _effectRange;
	}

	/**
	 * @return Returns the hitTime.
	 */
	public final int getHitTime()
	{
		return _hitTime;
	}

	/**
	 * @return Returns the hpConsume.
	 */
	public final int getHpConsume()
	{
		return _hpConsume;
	}

	/**
	 * @return Returns the cpConsume.
	 */
	public final int getCpConsume()
	{
		return _cpConsume;
	}

	public final boolean allowOnTransform()
	{
		// FIXME: do something about item skills!!!
		return (isPassive() || (getId() > 1999 && getId() < 3000));
	}

	/**
	 * @return Returns the id.
	 */
	public final Integer getId()
	{
		return _id;
	}

	public final int getDisplayId()
	{
		return _displayId;
	}

	public final int getMinPledgeClass()
	{
		return _minPledgeClass;
	}

	/**
	 * @return Returns the _targetConsumeId.
	 */
	public final int getTargetConsumeId()
	{
		return _targetConsumeId;
	}

	/**
	 * @return Returns the targetConsume.
	 */
	public final int getTargetConsume()
	{
		return _targetConsume;
	}

	/**
	 * @return Returns the itemConsume.
	 */
	public final int getItemConsume()
	{
		return _itemConsume;
	}

	/**
	 * @return Returns the itemConsumeId.
	 */
	public final int getItemConsumeId()
	{
		return _itemConsumeId;
	}

	/**
	 * @return Returns the level.
	 */
	public final int getLevel()
	{
		return _level;
	}

	/**
	 * @return Returns the magic.
	 */
	public final boolean isMagic()
	{
		return _magic;
	}

	public final boolean isItemSkill()
	{
		return _itemSkill;
	}

	/**
	 * @return Returns true to set static reuse.
	 */
	public final boolean isStaticReuse()
	{
		return _staticReuse || isItemSkill() && Config.ALT_ITEM_SKILLS_NOT_INFLUENCED;
	}

	/**
	 * @return Returns true to set static hittime.
	 */
	public final boolean isStaticHitTime()
	{
		return _staticHitTime || isItemSkill() && Config.ALT_ITEM_SKILLS_NOT_INFLUENCED;
	}

	/**
	 * @return Returns true if skill can be enchanted
	 */
	public final boolean isEnchantable()
	{
		return SkillTreeTable.getInstance().getSkillEnchantmentBySkillId(getId()) != null;
	}
	
	/**
	 * @return Returns the mpConsume.
	 */
	public final int getMpConsume()
	{
		return _mpConsume;
	}

	/**
	 * @return Returns the mpInitialConsume.
	 */
	public final int getMpInitialConsume()
	{
		return _mpInitialConsume;
	}

	/**
	 * @return Returns the name.
	 */
	public final String getName()
	{
		return _name;
	}

	/**
	 * @return Returns the reuseDelay.
	 */
	public final int getReuseDelay()
	{
		return _reuseDelay;
	}

	public final int getEquipDelay()
	{
		return _equipDelay;
	}

	public final int getCoolTime()
	{
		return _coolTime;
	}

	public final int getSkillInterruptTime()
	{
		return _skillInterruptTime;
	}

	public final int getSkillRadius()
	{
		return _skillRadius;
	}

	public final boolean isActive()
	{
		return _operateType == SkillOpType.OP_ACTIVE;
	}

	public final boolean isPassive()
	{
		return _operateType == SkillOpType.OP_PASSIVE;
	}

	public final boolean isToggle()
	{
		return _operateType == SkillOpType.OP_TOGGLE;
	}

	public final boolean isChance()
	{
		return getChanceCondition() != null && isPassive();
	}

	public final boolean isDance()
	{
		return _isDance;
	}

	public final boolean isSong()
	{
		return _isSong;
	}

	public final boolean isDanceOrSong()
	{
		return isDance() || isSong();
	}

	public final int getNextDanceMpCost()
	{
		return _nextDanceCost;
	}

	/**
	 *@return Returns the boolean _isDebuff.
	 */
	public final boolean isDebuff()
	{
		return _isDebuff;
	}

	public final float getSSBoost()
	{
		return _sSBoost;
	}

	public final int getAggroPoints()
	{
		return _aggroPoints;
	}

	public final boolean useSpiritShot()
	{
		return isMagic();
	}

	public final boolean useFishShot()
	{
		return ((getSkillType() == L2SkillType.PUMPING) || (getSkillType() == L2SkillType.REELING));
	}

	public final int getWeaponsAllowed()
	{
		return _weaponsAllowed;
	}

	public final boolean isPvpSkill()
	{
		switch (_skillType.getRoot())
		{
			case DOT:
			case BLEED:
			case CONFUSION:
			case POISON:
			case DEBUFF:
			case AGGDEBUFF:
			case STUN:
			case ROOT:
			case FEAR:
			case SLEEP:
			case MDOT:
			case MANADAM:
			case MUTE:
			case WEAKNESS:
			case PARALYZE:
			case CANCEL:
			case MAGE_BANE:
			case WARRIOR_BANE:
			case BETRAY:
			case DISARM:
			case STEAL_BUFF:
			case AGGDAMAGE:
			case DELUXE_KEY_UNLOCK:
			case FATALCOUNTER:
			case MAKE_KILLABLE:
			case MAKE_QUEST_DROPABLE:
			case AGGREDUCE_CHAR:
				return true;
			default:
				return false;
		}
	}

	public final boolean isOffensive()
	{
		return _offensiveState == OffensiveState.OFFENSIVE;
	}

	public final boolean isNeutral()
	{
		return _offensiveState == OffensiveState.NEUTRAL;
	}

	public final boolean isPositive()
	{
		return _offensiveState == OffensiveState.POSITIVE;
	}

	public final int getNeededCharges()
	{
		return _needCharges;
	}

	public final int getGiveCharges()
	{
		return _giveCharges;
	}

	public final int getMaxCharges()
	{
		return _maxCharges;
	}

	public final int getNumSouls()
	{
		return _numSouls;
	}

	public final int getMaxSoulConsumeCount()
	{
		return _soulMaxConsume;
	}

	public final int getSoulConsumeCount()
	{
		return _soulConsume;
	}

	public final int getExpNeeded()
	{
		return _expNeeded;
	}

	public final int getCritChance()
	{
		return _critChance;
	}

	public final int getBaseCritRate()
	{
		return _baseCritRate;
	}

	public final int getLethalChance1()
	{
		return _lethalEffect1;
	}

	public final int getLethalChance2()
	{
		return _lethalEffect2;
	}

	public final boolean getDmgDirectlyToHP()
	{
		return _directHpDmg;
	}

	/**
	 * @return pet food
	 */
	public final int getFeed()
	{
		return _feed;
	}

	public final FlyType getFlyType()
	{
		return _flyType;
	}

	public final int getFlyRadius()
	{
		return _flyRadius;
	}

	public final float getFlyCourse()
	{
		return _flyCourse;
	}

	public final int getTransformId()
	{
		return _transformId;
	}

	public final static boolean skillLevelExists(final int skillId, final int level)
	{
		return SkillTable.getInstance().getInfo(skillId, level) != null;
	}

	public final boolean isSkillTypeMagic()
	{
		switch (getSkillType().getRoot())
		{
			// TODO: other skillTypes
			case MDAM:
			case HEAL:
			case SUMMON_FRIEND:
			case BALANCE_LIFE:
				return true;
			default:
				return false;
		}
	}

	private OffensiveState getDefaultOffensiveState()
	{
		if (isPurePassiveSkill() || isPureChanceSkill() || isToggle())
			return OffensiveState.POSITIVE;
		
		switch (_skillType)
		{
			case PDAM:
			case MDAM:
			case CPDAM:
			case DOT:
			case CPDAMPERCENT:
			case CPDRAIN:
			case BLEED:
			case POISON:
			case AGGDAMAGE:
			case DEBUFF:
			case AGGDEBUFF:
			case STUN:
			case ROOT:
			case CONFUSION:
			case ERASE:
			case BLOW:
			case FEAR:
			case DRAIN:
			case SLEEP:
			case CHARGEDAM:
			case STRSIEGEASSAULT:
			case CONFUSE_MOB_ONLY:
			case DEATHLINK:
			case FATALCOUNTER:
			case DETECT_WEAKNESS:
			case MDOT:
			case MANADAM:
			case MUTE:
			case SPOIL:
			case WEAKNESS:
			case SWEEP:
			case PARALYZE:
			case CANCEL:
			case MAGE_BANE:
			case WARRIOR_BANE:
			case AGGREDUCE_CHAR:
			case BETRAY:
			case GET_PLAYER:
			case DISARM:
			case STEAL_BUFF:
			case INSTANT_JUMP:
			case SIGNET_CASTTIME:
			case BALLISTA:
				return OffensiveState.OFFENSIVE;
			case BUFF:
			case CONT:
			case HEAL:
			case HEAL_STATIC:
			case HEAL_PERCENT:
			case BALANCE_LIFE:
			case HOT:
			case MPHOT:
			case CPHOT:
			case MANAHEAL:
			case MANAHEAL_PERCENT:
			case MANARECHARGE:
			case COMBATPOINTHEAL:
			case CPHEAL_PERCENT:
			case RECOVER:
			case REFLECT:
			case LUCK:
			case PASSIVE:
			case RESURRECT:
			case CANCEL_DEBUFF:
			case FUSION:
			case CHARGE_NEGATE:
			case CHARGESOUL:
			case CHAIN_HEAL:
				return OffensiveState.POSITIVE;
			case DRAIN_SOUL:
			case HEAL_MOB:
			case AGGREDUCE:
			case AGGREMOVE:
			case SHIFT_TARGET:
			case SOULSHOT:
			case SPIRITSHOT:
			case ENCHANT_ARMOR:
			case ENCHANT_WEAPON:
			case MOUNT:
			case DECOY:
			case SUMMON:
			case AGATHION:
			case SUMMON_TRAP:
			case SUMMON_TREASURE_KEY:
			case CREATE_ITEM:
			case EXTRACTABLE:
			case UNLOCK:
			case OPEN_DOOR:
			case DELUXE_KEY_UNLOCK:
			case DETECT_TRAP:
			case REMOVE_TRAP:
			case DETECTION:
			case COMMON_CRAFT:
			case DWARVEN_CRAFT:
			case SIEGEFLAG:
			case TAKECASTLE:
			case TAKEFORT:
			case TELEPORT:
			case ZAKEN_TELEPORT:
			case RECALL:
			case SUMMON_FRIEND:
			case GIVE_SP:
			case GIVE_VITALITY:
			case CHANGE_APPEARANCE:
			case LEARN_SKILL:
			case FEED_PET:
			case BEAST_FEED:
			case NEGATE: // should be divided, since can be positive, and negative skill too
			case CANCEL_STATS:
			case MAKE_KILLABLE:
			case MAKE_QUEST_DROPABLE:
			case SOW:
			case HARVEST:
			case FISHING:
			case PUMPING:
			case REELING:
			case TRANSFORMDISPEL:
			case CHANGEWEAPON:
			case SIGNET:
			case DUMMY:
			case COREDONE:
			case NOTDONE:
			case SPAWN:
				return OffensiveState.NEUTRAL;
			default:
				_log.warn(getSkillType() + " should be covered in L2Skill.getDefaultOffensiveState()!");
				return OffensiveState.NEUTRAL;
		}
	}

	public final boolean isNeedWeapon()
	{
		return (_skillType.getRoot() == L2SkillType.MDAM);
	}

	public final boolean isStayAfterDeath()
	{
		switch (getId())
		{
			case 5660:
			case 840:
			case 841:
			case 842:
				return true;
			default:
				return _stayAfterDeath;
		}
	}

	private String	_weaponDependancyMessage;

	public final boolean getWeaponDependancy(final L2Character activeChar, final boolean message)
	{
		final int weaponsAllowed = getWeaponsAllowed();
		if (weaponsAllowed == 0)
			return true;

		final L2Weapon weapon = activeChar.getActiveWeaponItem();
		if (weapon != null && (weapon.getItemType().mask() & weaponsAllowed) != 0)
			return true;

		final L2Weapon weapon2 = activeChar.getSecondaryWeaponItem();
		if (weapon2 != null && (weapon2.getItemType().mask() & weaponsAllowed) != 0)
			return true;

		if (message && activeChar instanceof L2Player)
		{
			if (_weaponDependancyMessage == null)
			{
				final L2TextBuilder sb = L2TextBuilder.newInstance();
				sb.append(getName());
				sb.append(" can only be used with weapons of type ");
				for (final L2WeaponType wt : L2WeaponType.VALUES)
				{
					if ((wt.mask() & weaponsAllowed) != 0)
					{
						if (sb.length() != 0)
							sb.append('/');

						sb.append(wt);
					}
				}
				sb.append(".");

				_weaponDependancyMessage = sb.moveToString().intern();
			}

			if (activeChar instanceof L2Player)
				((L2Player)activeChar).sendMessage(_weaponDependancyMessage);
		}

		return false;
	}

	public final boolean ownedFuncShouldBeDisabled(final L2Character activeChar)
	{
		if (isOffensive())
			return false;

		if (!isDanceOrSong() && !getWeaponDependancy(activeChar, false))
			return true;

		return false;
	}

	public boolean checkCondition(final L2Character activeChar, final L2Object target)
	{
		if (activeChar instanceof L2Player && ((L2Player)activeChar).isGM() && !Config.GM_SKILL_RESTRICTION)
			return true;

		final Condition preCondition = _preCondition;

		if (preCondition == null)
			return true;

		final Env env = new Env();
		env.setPlayer(activeChar);
		if (target instanceof L2Character)
			env.setTarget((L2Character) target);
		env.setSkill(this);

		if (preCondition.test(env))
			return true;

		if (activeChar instanceof L2Player)
			preCondition.sendMessage((L2Player) activeChar, this);
		return false;
	}

	public final L2Character[] getTargetList(final L2Character activeChar, final boolean onlyFirst)
	{
		return getTargetList(activeChar, onlyFirst, activeChar.getTarget(L2Character.class));
	}

	/**
	 * Return all targets of the skill in a table in function a the skill type.<BR>
	 * <BR>
	 * <B><U> Values of skill type</U> :</B><BR>
	 * <BR>
	 * <li>ONE : The skill can only be used on the L2Player targeted, or on
	 * the caster if it's a L2Player and no L2Player targeted</li> <li>
	 * SELF</li> <li>HOLY, UNDEAD</li> <li>PET</li> <li>AURA, AURA_CLOSE</li>
	 * <li>AREA</li> <li>MULTIFACE</li> <li>PARTY, CLAN</li> <li>CORPSE_PLAYER,
	 * CORPSE_MOB, CORPSE_CLAN</li> <li>UNLOCKABLE</li> <li>ITEM</li> <BR>
	 * <BR>
	 *
	 * @param activeChar The L2Character who use the skill
	 */
	public final L2Character[] getTargetList(final L2Character activeChar, final boolean onlyFirst, L2Character target)
	{
		final ISkillTargetHandler handler = SkillTargetHandler.getInstance().getSkillTargetHandler(getTargetType());
		
		if (handler != null)
			return handler.useSkillTargetHandler(activeChar, target, this, onlyFirst);
		else
			return null;
	}

	public final L2Character[] getMultiFaceTargetList(final L2Character activeChar)
	{
		final ArrayBunch<L2Character> targetList = new ArrayBunch<L2Character>();
		L2Object target;
		L2Object FirstTarget;
		L2Player tgOwner;
		L2Clan acClan;
		L2Clan tgClan;
		final L2Party acPt = activeChar.getParty();
		final int radius = getSkillRadius();

		if (getCastRange() <= 0)
			target = activeChar;
		else
			target = activeChar.getTarget();
		FirstTarget = target;

		if (target == null || !(target instanceof L2Character))
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return null;
		}

		final int newHeading = getNewHeadingToTarget(activeChar, (L2Character) target);

		if (target.getObjectId() != activeChar.getObjectId())
		{
			if (!((L2Character) target).isAlikeDead())
				targetList.add((L2Character) target);
			else
			{
				activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return null;
			}
		}

		if (!(activeChar instanceof L2Playable))
		{
			for (final L2Object obj : activeChar.getKnownList().getKnownObjects().values())
			{
				if (obj instanceof L2Playable)
				{
					if (!(Util.checkIfInRange(radius, target, obj, true)))
						continue;
					else if (isBehindFromCaster(newHeading, (L2Character) FirstTarget, (L2Character) target))
						continue;
					else if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);

				}
			}
			if (targetList.size() == 0)
				return null;
			return targetList.moveToArray(new L2Character[targetList.size()]);
		}

		if (activeChar.getActingPlayer() != null)
			acClan = activeChar.getActingPlayer().getClan();
		else
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return null;
		}

		if (activeChar.isInsideZone(L2Zone.FLAG_SIEGE))
		{
			for (final L2Object obj : activeChar.getKnownList().getKnownObjects().values())
			{
				if (!(obj instanceof L2Playable))
					continue;
				if (!(Util.checkIfInRange(radius, target, obj, true)))
					continue;
				else if (isBehindFromCaster(newHeading, (L2Character) FirstTarget, (L2Character) obj))
					continue;

				if (obj instanceof L2Player)
				{
					tgClan = ((L2Player) obj).getClan();

					if (acPt != null)
					{
						if (activeChar.getParty().getPartyMembers().contains(obj))
							continue;
						else if (!((L2Character) obj).isAlikeDead())
							targetList.add((L2Character) obj);
					}
					else if (tgClan != null)
					{
						if (tgClan.getClanId() == acClan.getClanId())
							continue;
						else if (tgClan.getAllyId() == acClan.getAllyId())
							continue;
						else if (!((L2Character) obj).isAlikeDead())
							targetList.add((L2Character) obj);
					}
					else if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);
				}
				else if (obj instanceof L2Summon)
				{
					tgOwner = ((L2Summon) obj).getOwner();
					tgClan = tgOwner.getClan();

					if (acPt != null)
					{
						if (activeChar.getParty().getPartyMembers().contains(tgOwner))
							continue;
						else if (!((L2Character) obj).isAlikeDead())
							targetList.add((L2Character) obj);
					}
					else if (tgClan != null)
					{
						if (tgClan.getClanId() == acClan.getClanId())
							continue;
						else if (tgClan.getAllyId() == acClan.getAllyId())
							continue;
						else if (!((L2Character) obj).isAlikeDead())
							targetList.add((L2Character) obj);
					}
					else if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);
				}
				else if (obj instanceof L2Attackable)
				{
					if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);
				}
				else
				{
					continue;
				}
			}
		}
		else if (activeChar.isInsideZone(L2Zone.FLAG_STADIUM) || activeChar.isInsideZone(L2Zone.FLAG_PVP))
		{
			for (final L2Object obj : activeChar.getKnownList().getKnownObjects().values())
			{
				if (!(obj instanceof L2Playable))
					continue;
				if (!(Util.checkIfInRange(radius, target, obj, true)))
					continue;
				else if (isBehindFromCaster(newHeading, (L2Character) FirstTarget, (L2Character) obj))
					continue;

				if (obj instanceof L2Player)
				{
					if (acPt != null)
					{
						if (activeChar.getParty().getPartyMembers().contains(obj))
							continue;
						else if (!((L2Character) obj).isAlikeDead())
							targetList.add((L2Character) obj);
					}
					else if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);
				}
				else if (obj instanceof L2Summon)
				{
					tgOwner = ((L2Summon) obj).getOwner();

					if (acPt != null)
					{
						if (activeChar.getParty().getPartyMembers().contains(tgOwner))
							continue;
						else if (!((L2Character) obj).isAlikeDead())
							targetList.add((L2Character) obj);
					}
					else if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);
				}
				else if (obj instanceof L2Attackable)
				{
					if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);
				}
				else
				{
					continue;
				}
			}
		}
		else
		{
			for (final L2Object obj : activeChar.getKnownList().getKnownObjects().values())
			{
				if (!(obj instanceof L2Playable))
					continue;
				if (!(Util.checkIfInRange(radius, target, obj, true)))
					continue;
				else if (isBehindFromCaster(newHeading, (L2Character) FirstTarget, (L2Character) obj))
					continue;

				if (obj instanceof L2MonsterInstance)
				{
					if (!((L2Character) obj).isAlikeDead())
						targetList.add((L2Character) obj);
				}
			}
		}

		if (targetList.size() == 0)
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return null;
		}

		return targetList.moveToArray(new L2Character[targetList.size()]);
	}

    public static final boolean addSummon(final L2Character caster, final L2Player owner, final int radius, final boolean isDead)
    {
        final L2Summon summon = owner.getPet();

        if (summon == null)
        	return false;

        return addCharacter(caster, summon, radius, isDead);
    }

    public static final boolean addCharacter(final L2Character caster, final L2Character target, final int radius, final boolean isDead)
    {
    	if (isDead != target.isDead())
    		return false;

    	if (radius > 0 && !Util.checkIfInRange(radius, caster, target, true))
			return false;

    	return true;

    }

	public boolean eventCheck(final L2Player player, final L2Player newTarget)
	{
		if (GlobalRestrictions.isProtected(player, newTarget, this, false))
			return false;

		return true;
	}

	private int getNewHeadingToTarget(final L2Character caster, final L2Character target)
	{
		if (caster == null || target == null)
			return 0;

		double befHeading = Util.convertHeadingToDegree(caster.getHeading());
		if (befHeading > 360)
			befHeading -= 360;

		final int dx = caster.getX() - target.getX();
		final int dy = caster.getY() - target.getY();

		double dist = Math.sqrt(dx * dx + dy * dy);

		if (dist == 0)
			dist = 0.01;

		final double sin = dy / dist;
		final double cos = dx / dist;
		final int heading = (int) (Math.atan2(-sin, -cos) * 10430.378350470452724949566316381);

		return heading;

	}

	public final boolean isBehindFromCaster(final int heading, final L2Character caster, final L2Character target)
	{
		if (caster == null || target == null)
			return true;

		double befHeading = Util.convertHeadingToDegree(heading);
		if (befHeading > 360)
			befHeading -= 360;
		else if (befHeading < 0)
			befHeading += 360;

		final int dx = caster.getX() - target.getX();
		final int dy = caster.getY() - target.getY();

		double dist = Math.sqrt(dx * dx + dy * dy);

		if (dist == 0)
			dist = 0.01;

		final double sin = dy / dist;
		final double cos = dx / dist;
		final int newheading = (int) (Math.atan2(-sin, -cos) * 10430.378350470452724949566316381);

		double aftHeading = Util.convertHeadingToDegree(newheading);
		if (aftHeading > 360)
			aftHeading -= 360;
		else if (aftHeading < 0)
			aftHeading += 360;

		double diffHeading = Math.abs(aftHeading - befHeading);
		if (diffHeading > 360)
			diffHeading -= 360;
		else if (diffHeading < 0)
			diffHeading += 360;

		return (diffHeading > 90) && (diffHeading < 270);
	}

	// [L2J_JP ADD SANDMAN END]
	public final L2Character[] getTargetList(final L2Character activeChar)
	{
		return getTargetList(activeChar, false);
	}
	
	public final L2Character getFirstOfTargetList(final L2Character activeChar)
	{
		return getFirstOfTargetList(activeChar, null);
	}
	
	public final L2Character getFirstOfTargetList(final L2Character activeChar, L2Character[] targets)
	{
		switch (getTargetType())
		{
			case TARGET_SELF:
			case TARGET_PARTY:
			case TARGET_PARTY_CLAN:
			case TARGET_CLAN:
			case TARGET_ALLY:
			case TARGET_ENEMY_ALLY:
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_GROUND:
				return activeChar;
			case TARGET_PET:
			case TARGET_SUMMON:
			case TARGET_SERVITOR_AURA:
				return activeChar.getActingSummon();
			case TARGET_OWNER_PET:
				return activeChar.getActingPlayer();
		}
		
		if (targets == null)
			targets = getTargetList(activeChar, true);
		
		return targets == null || targets.length == 0 ? null : targets[0];
	}

	private Func[] _statFuncs;

	public final Func[] getStatFuncs(final L2Character player)
	{
		if (!(player instanceof L2Playable) && !(player instanceof L2Attackable))
			return Func.EMPTY_ARRAY;

		if (_statFuncs == null)
		{
			if (_funcTemplates == null)
			{
				_statFuncs = Func.EMPTY_ARRAY;
			}
			else
			{
				final Func[] funcs = new Func[_funcTemplates.length];

				for (int i = 0; i < _funcTemplates.length; i++)
					funcs[i] = _funcTemplates[i].getFunc(this);

				_statFuncs = L2Arrays.compact(funcs);
			}
		}

		return _statFuncs;
	}

	public final boolean hasEffects()
	{
		return _effectTemplates != null && _effectTemplates.length > 0;
	}

	public final boolean hasSelfEffects()
	{
		return _effectTemplatesSelf != null && _effectTemplatesSelf.length > 0;
	}

	public final void dealDamage(final L2Character activeChar, final L2Character target, final L2Skill skill, final double damage, final byte reflect, final boolean mcrit, final boolean pcrit)
	{
		activeChar.sendDamageMessage(target, (int)damage, mcrit, pcrit, false);

		if (skill.getDmgDirectlyToHP())
		{
			final double actCp1 = target.getStatus().getCurrentCp();
			target.getStatus().setCurrentCp(0);
			target.reduceCurrentHp(damage, activeChar, skill);
			target.getStatus().setCurrentCp(actCp1);

			// vengeance reflected damage
			if ((reflect & Formulas.SKILL_REFLECT_VENGEANCE) != 0)
			{
				final double actCp2 = activeChar.getStatus().getCurrentCp();
				activeChar.getStatus().setCurrentCp(0);
				activeChar.reduceCurrentHp(damage, target, skill);
				activeChar.getStatus().setCurrentCp(actCp2);
			}
		}
		else
		{
			target.reduceCurrentHp(damage, activeChar, skill);

			// vengeance reflected damage
			if ((reflect & Formulas.SKILL_REFLECT_VENGEANCE) != 0)
				activeChar.reduceCurrentHp(damage, target, skill);
		}

		// Manage attack or cast break of the target (calculating rate, sending message...)
		if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
		{
			target.breakAttack();
			target.breakCast();
		}
	}

	public final void getEffects(final L2Character effector, L2Character effected, final byte reflect, final byte shld, final boolean ss, final boolean sps, final boolean bss)
	{
		if (_effectTemplates == null)
			return;

		if (!canCreateEffect(effector, effected, this))
			return;

		// Activate attacked effects, if any
		if (Formulas.calcSkillSuccess(effector, effected, this, shld, ss, sps, bss))
		{
			if ((reflect & Formulas.SKILL_REFLECT_SUCCEED) != 0)
				effected = effector;

			final Env env = new Env();
			env.setPlayer(effector);
			env.setTarget(effected);
			env.setSkill(this);
			env.setSkillMastery(Formulas.calcSkillMastery(effector, this));

			for (final EffectTemplate et : _effectTemplates)
				et.getEffect(env);

			if (effected instanceof L2Player)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
				sm.addSkillName(this);
				effected.getActingPlayer().sendPacket(sm);
			}
		}
		else
		{
			effector.sendResistedMyEffectMessage(effected, this);
		}
	}

	public final void getEffects(final L2Character effector, final L2Character effected)
	{
		getEffects(effector, effected, null);
	}
	
	public final void getEffects(final L2Character effector, final L2Character effected, final ForEachExecutable<L2Effect> executable)
	{
		if (_effectTemplates == null)
			return;
		
		if (!canCreateEffect(effector, effected, this))
			return;
		
		final Env env = new Env();
		env.setPlayer(effector);
		env.setTarget(effected);
		env.setSkill(this);
		env.setSkillMastery(Formulas.calcSkillMastery(effector, this));
		
		for (final EffectTemplate et : _effectTemplates)
		{
			final L2Effect e = et.getEffect(env);
			if (e != null)
				if (executable != null)
					executable.execute(e);
		}
	}
	
	private boolean canCreateEffect(final L2Character activeChar, final L2Character target, final L2Skill skill)
	{
		if (skill.isPassive())
			return false;
		
		if (activeChar == target)
			return true;
		
		// doors and siege flags cannot receive any effects
		if (target instanceof L2DoorInstance || target instanceof L2SiegeFlagInstance)
			return false;
		
		if (target.isInvul())
			return false;
		
		return true;
	}

	public final void getEffects(final L2CubicInstance effector, final L2Character effected)
	{
		if (_effectTemplates == null)
			return;

		if (!canCreateEffect(effector.getOwner(), effected, this))
			return;

		final Env env = new Env();
		env.setPlayer(effector.getOwner());
		//env.cubic = effector;
		env.setTarget(effected);
		env.setSkill(this);

		for (final EffectTemplate et : _effectTemplates)
			et.getEffect(env);
	}

	public final void getEffectsSelf(final L2Character effector)
	{
		if (_effectTemplatesSelf == null)
			return;

		if (!canCreateEffect(effector, effector, this))
			return;

		final Env env = new Env();
		env.setPlayer(effector);
		env.setTarget(effector);
		env.setSkill(this);

		for (final EffectTemplate et : _effectTemplatesSelf)
			et.getEffect(env);
	}

	public final EffectTemplate[] getEffectTemplates()
	{
		return _effectTemplates;
	}

	public final void attach(final FuncTemplate f)
	{
		if (_funcTemplates == null)
		{
			_funcTemplates = new FuncTemplate[] { f };
		}
		else
		{
			final int len = _funcTemplates.length;
			final FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(_funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			_funcTemplates = tmp;
		}
	}

	public final void attach(final EffectTemplate effect)
	{
		if (_effectTemplates == null)
		{
			_effectTemplates = new EffectTemplate[] { effect };
		}
		else
		{
			// support for improved buffs, in case it gets overwritten in DP
			for (final EffectTemplate template : _effectTemplates)
				if (template.merge(this, effect))
					return;
			
			final int len = _effectTemplates.length;
			final EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(_effectTemplates, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplates = tmp;
		}
	}

	public final void attachSelf(final EffectTemplate effect)
	{
		if (_effectTemplatesSelf == null)
		{
			_effectTemplatesSelf = new EffectTemplate[] { effect };
		}
		else
		{
			// support for improved buffs, in case it gets overwritten in DP
			for (final EffectTemplate template : _effectTemplatesSelf)
				if (template.merge(this, effect))
					return;
			
			final int len = _effectTemplatesSelf.length;
			final EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(_effectTemplatesSelf, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplatesSelf = tmp;
		}
	}

	public final void attach(final Condition c)
	{
		final Condition old = _preCondition;

		if (old != null)
			_log.fatal("Replaced " + old + " condition with " + c + " condition at skill: " + this);

		_preCondition = c;
	}

	@Override
	public final String toString()
	{
		return _name + "[id=" + _id + ",lvl=" + _level + "]";
	}

	public final String generateUniqueStackType()
	{
		int count = _effectTemplates == null ? 0 : _effectTemplates.length;
		count += _effectTemplatesSelf == null ? 0 : _effectTemplatesSelf.length;

		return _id + "-" + count;
	}

	public final float generateStackOrder()
	{
		return getLevel();
	}

	@Override
	public final String getFuncOwnerName()
	{
		return getName();
	}

	@Override
	public final L2Skill getFuncOwnerSkill()
	{
		return this;
	}

	/**
	 * used for tracking item id in case that item consume cannot be used
	 *
	 * @return reference item id
	 */
	public final int getReferenceItemId()
	{
		return _refId;
	}

	/**
	 * @return
	 */
	public final int getAfroColor()
	{
		return _afroId;
	}

	public final boolean is7Signs()
	{
		return (4360 < getId() && getId() < 4367);
	}
	
	public final boolean isBuff()
	{
		if (is7Signs()) // 7s buffs
			return false;

		// TODO: this is a so ugly hax
		switch (getSkillType().getRoot())
		{
			case BUFF:
			case REFLECT:
			case HEAL_PERCENT:
			case MANAHEAL_PERCENT:
			case COMBATPOINTHEAL:
				return true;
			default:
				return false;
		}
	}

	public final boolean isHerbEffect()
	{
		return _isHerbEffect;
	}

	@Override
	public final boolean equals(final Object obj)
	{
		if (!(obj instanceof L2Skill))
			return false;

		final L2Skill skill = (L2Skill) obj;

		return getId() == skill.getId() && getLevel() == skill.getLevel();
	}

	@Override
	public final int hashCode()
	{
		return L2System.hash(SkillTable.getSkillUID(this));
	}

	public final int getElementPower()
	{
		return _elementPower;
	}

	public final String getAttributeName()
	{
		return _attribute;
	}

	public final boolean ignoreShield()
	{
		return _ignoreShield;
	}

	public final boolean canBeReflected()
	{
		return _canBeReflected;
	}

	public boolean canBeDispeled()
	{
		return _canBeDispeled;
	}
	
	public boolean isClanSkill()
	{
		return _isClanSkill;
	}
	
	public boolean isDispeledOnAction()
	{
		return _dispelOnAction;
	}
	
	public boolean isDispeledOnAttack()
	{
		return _dispelOnAttack;
	}

	public final int getAfterEffectId()
	{
		return _afterEffectId;
	}

	public final int getAfterEffectLvl()
	{
		return _afterEffectLvl;
	}

	public final L2Skill getAfterEffectSkill()
	{
		return SkillTable.getInstance().getInfo(getAfterEffectId(), getAfterEffectLvl());
	}

	public final boolean canSendToClient()
	{
		return _sendToClient;
	}

	public final float getPvpPowerMultiplier()
	{
		return _pvpPowerMulti;
	}

	@Override
	public final L2Skill getChanceTriggeredSkill(final L2Character activeChar, final L2Character evtInitiator)
	{
		if (!getWeaponDependancy(activeChar, false))
			return null;
		
		if (!checkCondition(activeChar, evtInitiator))
			return null;
		
		if (getTriggeredSkill() == null)
			return this;
		
		return getTriggeredSkill().getTriggeredSkill();
	}

	/**
	 * @return Returns how much mana is consummed on the talisman when it's skill is used.
	 */
	public final int getTalismanManaConsumeOnSkill()
	{
		return _talismanManaConsumeOnSkill;
	}

	/**
	 * @return Returns the talisman id this skill uses.
	 */
	public final int getBelongingTalismanId()
	{
		return _belongingTalismanId;
	}
	
	@Override
	public final ChanceCondition getChanceCondition()
	{
		return _chanceCondition;
	}
}
