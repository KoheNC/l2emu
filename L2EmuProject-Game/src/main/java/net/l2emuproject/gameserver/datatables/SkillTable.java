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
package net.l2emuproject.gameserver.datatables;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javolution.util.FastSet;
import net.l2emuproject.gameserver.model.L2EnchantSkillLearn;
import net.l2emuproject.gameserver.model.L2EnchantSkillLearn.EnchantSkillDetail;
import net.l2emuproject.gameserver.model.L2Skill;
import net.l2emuproject.gameserver.skills.SkillsEngine;
import net.l2emuproject.gameserver.skills.l2skills.L2SkillLearnSkill;
import net.l2emuproject.lang.L2Integer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public final class SkillTable
{
	private static final Log _log = LogFactory.getLog(SkillTable.class);
	
	private static final class SingletonHolder
	{
		private static SkillTable INSTANCE = new SkillTable();
	}
	
	public static SkillTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	public synchronized static void reload()
	{
		SingletonHolder.INSTANCE = new SkillTable();
	}
	
	public static final class SkillInfo
	{
		private final Integer _skillId;
		private final int _level;
		private L2Skill _skill;
		
		private SkillInfo(int skillId, int level)
		{
			_skillId = L2Integer.valueOf(skillId);
			_level = level;
		}
		
		public Integer getId()
		{
			return _skillId;
		}
		
		public int getLevel()
		{
			return _level;
		}
		
		public L2Skill getSkill()
		{
			return _skill;
		}
	}
	
	private static SkillInfo[][] SKILL_INFOS = new SkillInfo[0][];
	
	private final L2Skill[][] _skillTable;
	private final int[] _maxLevels;
	private final Set<L2Skill> _learnedSkills = new HashSet<L2Skill>();
	
	private SkillTable()
	{
		final List<L2Skill> skills = SkillsEngine.loadSkills();
		_log.info("SkillTable: Loaded " + skills.size() + " skill templates from XML files.");
		
		int highestId = 0;
		for (L2Skill skill : skills)
			if (highestId < skill.getId())
				highestId = skill.getId();
		
		_maxLevels = new int[highestId + 1];
		
		int[] highestLevels = new int[highestId + 1];
		for (L2Skill skill : skills)
		{
			if (highestLevels[skill.getId()] < skill.getLevel())
				highestLevels[skill.getId()] = skill.getLevel();
			
			if (_maxLevels[skill.getId()] < skill.getLevel() && skill.getLevel() < 100)
				_maxLevels[skill.getId()] = skill.getLevel();
		}
		
		// clear previously stored skills
		for (SkillInfo[] infos : SKILL_INFOS)
			if (infos != null)
				for (SkillInfo info : infos)
					if (info != null)
						info._skill = null;
		
		_skillTable = new L2Skill[highestId + 1][];
		
		SKILL_INFOS = Arrays.copyOf(SKILL_INFOS, Math.max(SKILL_INFOS.length, highestId + 1));
		
		for (int i = 0; i < highestLevels.length; i++)
		{
			final int highestLevel = highestLevels[i];
			
			if (highestLevel < 1)
				continue;
			
			_skillTable[i] = new L2Skill[highestLevel + 1];
			
			if (SKILL_INFOS[i] == null)
				SKILL_INFOS[i] = new SkillInfo[highestLevel + 1];
			else
				SKILL_INFOS[i] = Arrays.copyOf(SKILL_INFOS[i], Math.max(SKILL_INFOS[i].length, highestLevel + 1));
		}
		
		for (L2Skill skill : skills)
		{
			_skillTable[skill.getId()][skill.getLevel()] = skill;
			
			if (SKILL_INFOS[skill.getId()][skill.getLevel()] == null)
				SKILL_INFOS[skill.getId()][skill.getLevel()] = new SkillInfo(skill.getId(), skill.getLevel());
			
			SKILL_INFOS[skill.getId()][skill.getLevel()]._skill = skill;
		}
		
		int length = _skillTable.length;
		for (L2Skill[] array : _skillTable)
			if (array != null)
				length += array.length;
		
		_log.info("SkillTable: Occupying arrays for " + length + ".");
		
		SingletonHolder.INSTANCE = this;
		
		Map<Integer, L2Skill> skillsByUID = new HashMap<Integer, L2Skill>();
		
		for (L2Skill skill : skills)
		{
			try
			{
				L2Skill old = skillsByUID.put(SkillTable.getSkillUID(skill), skill);
				
				if (old != null)
					_log.warn("Overlapping UIDs for: " + old + ", " + skill, new IllegalStateException());
				
				skill.validate();
			}
			catch (Exception e)
			{
				_log.warn(skill, e);
			}
		}
		
		for (L2Skill skill0 : skills)
		{
			if (!(skill0 instanceof L2SkillLearnSkill))
				continue;
			
			L2SkillLearnSkill skill = (L2SkillLearnSkill)skill0;
			
			for (int i = 0; i < skill.getNewSkillId().length; i++)
			{
				final L2Skill learnedSkill = getInfo(skill.getNewSkillId()[i], skill.getNewSkillLvl()[i]);
				
				if (learnedSkill != null)
					_learnedSkills.add(learnedSkill);
			}
		}
		
		// checking for skill enchantment mismatch
		
		// in XMLs
		final TreeSet<String> skillEnchantsInXMLs = new TreeSet<String>();
		
		// reusing
		final Map<Integer, Set<Integer>> enchantLevelsByEnchantType = new HashMap<Integer, Set<Integer>>();
		
		for (int skillId = 0; skillId < _skillTable.length; skillId++)
		{
			final L2Skill[] skillsById = _skillTable[skillId];
			
			if (skillsById == null)
				continue;
			
			for (final L2Skill skill : skillsById)
			{
				if (skill == null || skill.getLevel() < 100)
					continue;
				
				final int enchantType = skill.getLevel() / 100;
				final int enchantLevel = skill.getLevel() % 100;
				
				Set<Integer> enchantLevels = enchantLevelsByEnchantType.get(enchantType);
				
				if (enchantLevels == null)
					enchantLevelsByEnchantType.put(enchantType, enchantLevels = new FastSet<Integer>(30));
				
				enchantLevels.add(enchantLevel);
			}
			
			for (Map.Entry<Integer, Set<Integer>> entry : enchantLevelsByEnchantType.entrySet())
			{
				final int enchantType = entry.getKey();
				final Set<Integer> enchantLevels = entry.getValue();
				
				if (enchantLevels.isEmpty())
					continue;
				
				final String s = "Skill ID: " + skillId + " - EnchantType: enchant" + enchantType + " - Levels: " + enchantLevels.size();
				
				boolean valid = true;
				
				for (int skillLvl = 1; skillLvl <= 30; skillLvl++)
				{
					if (!enchantLevels.remove(skillLvl))
					{
						if (skillLvl == 16 && enchantLevels.isEmpty())
							break;
						_log.warn("Missing skill enchant level in XMLs for " + s + " - Level: " + skillLvl);
						valid = false;
					}
				}
				
				if (!enchantLevels.isEmpty())
					_log.warn("Extra skill enchant levels in XMLs for " + s + " - Levels: " + enchantLevels);
				else if (valid)
					skillEnchantsInXMLs.add(s);
				
				// reusing
				enchantLevels.clear();
			}
		}
		
		// in database
		final TreeSet<String> skillEnchantsInDatabase = new TreeSet<String>();
		
		for (L2EnchantSkillLearn skillLearn : SkillTreeTable.getInstance().getSkillEnchantments())
		{
			final int skillId = skillLearn.getId();
			final List<EnchantSkillDetail>[] details = skillLearn.getEnchantRoutes();
			
			if (details.length == 0)
				_log.warn("Invalid skill enchant data in database for Skill ID: " + skillId);
			
			for (int indexingEnchantType = 0; indexingEnchantType < details.length; indexingEnchantType++)
			{
				final List<EnchantSkillDetail> route = details[indexingEnchantType];
				
				if (route == null)
					continue;
				
				final String s = "Skill ID: " + skillId + " - EnchantType: enchant" + (indexingEnchantType + 1) + " - Levels: " + route.size();
				
				if (route.size() != 30 && route.size() != 15)
					_log.warn("Invalid skill enchant data in database for " + s);
				else
					skillEnchantsInDatabase.add(s);
			}
		}
		
		// comparing the results
		for (String skillEnchant : skillEnchantsInXMLs)
			if (!skillEnchantsInDatabase.remove(skillEnchant))
				_log.warn("Missing skill enchant data in database for " + skillEnchant);
		
		for (String skillEnchant : skillEnchantsInDatabase)
			_log.warn("Missing skill enchant data in XMLs for " + skillEnchant);
		
		// just validation
		for (L2EnchantSkillLearn skillLearn : SkillTreeTable.getInstance().getSkillEnchantments())
		{
			final int skillId = skillLearn.getId();
			final List<EnchantSkillDetail>[] details = skillLearn.getEnchantRoutes();
			final int maxLevel = getMaxLevel(skillId);
			
			if (skillLearn.getBaseLevel() != maxLevel)
				_log.warn("Invalid `base_lvl` skill enchant data in database for Skill ID: " + skillId);
			
			for (int indexingEnchantType = 0; indexingEnchantType < details.length; indexingEnchantType++)
			{
				final List<EnchantSkillDetail> route = details[indexingEnchantType];
				
				if (route == null)
					continue;
				
				final String s = "Skill ID: " + skillId + " - EnchantType: enchant" + (indexingEnchantType + 1) + " - Levels: " + route.size();
				
				int index = 1;
				int expectedMinSkillLevel = maxLevel;
				
				for (EnchantSkillDetail detail : route)
				{
					if (detail.getLevel() % 100 != index)
						_log.warn("Invalid `level` skill enchant data in database for " + s);
					
					if (detail.getMinSkillLevel() != expectedMinSkillLevel)
						_log.warn("Invalid `min_skill_lvl` skill enchant data in database for " + s);
					
					index++;
					expectedMinSkillLevel = detail.getLevel();
				}
			}
		}
	}
	
	public static int getSkillUID(L2Skill skill)
	{
		return skill == null ? 0 : getSkillUID(skill.getId(), skill.getLevel());
	}
	
	public static int getSkillUID(int skillId, int skillLevel)
	{
		return skillId * 1023 + skillLevel;
	}
	
	public L2Skill getInfo(final int skillId, final int level)
	{
		// there is no skill with non-positive level
		if (level < 1)
			return null; // TODO: warn
			
		// there is no skill at all with that id
		if (skillId < 1 || _skillTable.length <= skillId)
			return null; // TODO: warn
			
		final L2Skill[] array = _skillTable[skillId];
		
		// there is no skill at all with that id
		if (array == null)
			return null; // TODO: warn
			
		// skill maybe exists with the given level
		if (level < array.length)
		{
			final L2Skill skill = array[level];
			
			// and yes it do exists
			if (skill != null)
				return skill;
		}
		
		// if not, then skill with max level
		return array[_maxLevels[skillId]]; // TODO: warn
	}
	
	public SkillInfo getSkillInfo(int skillId, int level)
	{
		if (skillId < 1 || SKILL_INFOS.length <= skillId)
			return null;
		
		SkillInfo[] array = SKILL_INFOS[skillId];
		
		if (array == null)
			return null;
		
		if (level < 1 || array.length <= level)
			return null;
		
		return array[level];
	}
	
	public int getMaxLevel(int skillId)
	{
		if (skillId < 1 || _maxLevels.length <= skillId)
			return 0;
		
		return _maxLevels[skillId];
	}
	
	public int getNormalLevel(L2Skill skill)
	{
		if (skill.getLevel() < 100)
			return skill.getLevel();
		
		return getMaxLevel(skill.getId());
	}
	
	public L2Skill[] getSiegeSkills(boolean addNoble, boolean hasCastle)
	{
		final L2Skill[] skills = new L2Skill[2 + (addNoble ? 1 : 0) + (hasCastle ? 2 : 0)];
		
		int i = 0;
		
		skills[i++] = getInfo(246, 1);
		skills[i++] = getInfo(247, 1);
		
		if (addNoble)
			skills[i++] = getInfo(326, 1);
		if (hasCastle)
		{
			skills[i++] = getInfo(844, 1);
			skills[i++] = getInfo(845, 1);
		}
		
		return skills;
	}
	
	public boolean isLearnedSkill(L2Skill skill)
	{
		return _learnedSkills.contains(skill);
	}
	
	public static boolean isProphetBuff(int skillId)
	{
		switch (skillId)
		{
			case 1204:
			case 1040:
			case 1068:
			case 1036:
			case 1035:
			case 1045:
			case 1048:
			case 1062:
			case 1086:
			case 1240:
			case 1242:
			case 1077:
			case 1268:
			case 1087:
			case 1085:
			case 1059:
			case 1078:
			case 1243:
			case 1304:
				return true;
			default:
				return false;
		}
	}

	public static boolean isDance(int skillId)
	{
		switch (skillId)
		{
			case 271:
			case 274:
			case 275:
			case 272:
			case 310:
			case 273:
			case 276:
			case 277:
			case 365:
			case 311:
			case 307:
			case 309:
				return true;
			default:
				return false;
		}
	}

	public static boolean isSong(int skillId)
	{
		switch (skillId)
		{
			case 264:
			case 304:
			case 268:
			case 267:
			case 266:
			case 269:
			case 265:
			case 270:
			case 349:
			case 305:
			case 306:
			case 308:
			case 363:
			case 364:
				return true;
			default:
				return false;
		}
	}

	public static boolean isCubic(int skillId)
	{
		switch (skillId)
		{
			case 33:
			case 22:
			case 278:
			case 10:
			case 67:
			case 1279:
			case 1281:
			case 1280:
				return true;
			default:
				return false;
		}
	}

	public static boolean isHeroBuff(int skillId)
	{
		switch (skillId)
		{
			case 395:
			case 396:
			case 1374:
				return true;
			default:
				return false;
		}
	}

	public static boolean isNobleBuff(int skillId)
	{
		switch (skillId)
		{
			case 1323:
			case 1325:
				return true;
			default:
				return false;
		}
	}

	public static boolean isSummonBuff(int skillId)
	{
		switch (skillId)
		{
			case 4699:
			case 4700:
			case 4702:
			case 4703:
				return true;
			default:
				return false;
		}
	}

	public static boolean isOrcBuff(int skillId)
	{
		switch (skillId)
		{
			case 1006:
			case 1261:
			case 1250:
			case 1391:
			case 1390:
			case 1362:
			case 1363:
			case 1310:
			case 1309:
			case 1284:
			case 1249:
			case 1253:
			case 1008:
			case 1252:
			case 1004:
			case 1251:
			case 1010:
			case 1365:
			case 1009:
			case 1364:
			case 1007:
			case 1305:
			case 1005:
			case 1002:
			case 1308:
			case 1003:
			case 1260:
				return true;
			default:
				return false;
		}
	}

	public static boolean isOtherBuff(int skillId)
	{
		switch (skillId)
		{
			case 1355:
			case 1356:
			case 1357:
			case 1388:
			case 1413:
			case 1389:
			case 1392:
			case 1393:
			case 1303:
			case 1353:
			case 1259:
			case 1352:
				return true;
			default:
				return false;
		}
	}
	
	public static boolean isVitalityBuff(int skillId)
	{
		switch (skillId)
		{
			case 23179:
			case 23016:
			case 23024:
			case 23032:
			case 23035:
			case 23063:
			case 23064:
			case 22004:
			case 22030:
			case 22035:
			case 22054:
			case 21011:
			case 21012:
			case 21013:
			case 21066:
			case 21084:
			case 6123:
			case 5950:
				return true;
			default:
				return false;
		}
	}
}
