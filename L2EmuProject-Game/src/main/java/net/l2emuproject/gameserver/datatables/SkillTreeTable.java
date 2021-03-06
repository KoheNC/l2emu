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

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.entity.base.ClassId;
import net.l2emuproject.gameserver.skills.L2Effect;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.skilllearn.L2CertificationSkillsLearn;
import net.l2emuproject.gameserver.skills.skilllearn.L2EnchantSkillLearn;
import net.l2emuproject.gameserver.skills.skilllearn.L2EnchantSkillLearn.EnchantSkillDetail;
import net.l2emuproject.gameserver.skills.skilllearn.L2PledgeSkillLearn;
import net.l2emuproject.gameserver.skills.skilllearn.L2SkillLearn;
import net.l2emuproject.gameserver.skills.skilllearn.L2TransformSkillLearn;
import net.l2emuproject.gameserver.system.database.L2DatabaseFactory;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.util.ArrayBunch;
import net.l2emuproject.util.L2Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

@SuppressWarnings("unchecked")
public class SkillTreeTable
{
	public static final byte						NORMAL_ENCHANT_COST_MULTIPLIER	= 1;
	public static final byte						SAFE_ENCHANT_COST_MULTIPLIER	= 3;

	public static final int							NORMAL_ENCHANT_BOOK				= 6622;
	public static final int							SAFE_ENCHANT_BOOK				= 9627;
	public static final int							CHANGE_ENCHANT_BOOK				= 9626;
	public static final int							UNTRAIN_ENCHANT_BOOK			= 9625;

	private final static Log						_log							= LogFactory.getLog(SkillTreeTable.class);

	private final Map<Integer, L2SkillLearn>[]		_skillTrees						= new Map[ClassId.values().length];
	private ArrayList<L2SkillLearn>					_fishingSkillTrees;														// all common skills (teached by Fisherman)
	private ArrayList<L2SkillLearn>					_expandDwarfCraftSkillTrees;												// list of special skill for dwarf (expand dwarf craft) learned by class teacher
	private ArrayList<L2PledgeSkillLearn>			_pledgeSkillTrees;															// pledge skill list
	private HashMap<Integer, L2EnchantSkillLearn>	_enchantSkillTrees;														// enchant skill list
	private ArrayList<L2TransformSkillLearn>		_transformSkillTrees;														// Transform Skills (Test)
	private ArrayList<L2SkillLearn>					_specialSkillTrees;
	private ArrayList<L2CertificationSkillsLearn>	_certificationSkillsTrees;													// Special Ability Skills (Hellbound)

	private Map<Integer, Integer>					_minSkillLevel					= new HashMap<Integer, Integer>();

	public static SkillTreeTable getInstance()
	{
		return SingletonHolder._instance;
	}

	/**
	 * Return the minimum level needed to have this Expertise.<BR>
	 * <BR>
	 * 
	 * @param grade The grade level searched
	 */
	public int getExpertiseLevel(int grade)
	{
		if (grade <= 0)
			return 0;

		// since expertise comes at same level for all classes we use paladin for now
		Map<Integer, L2SkillLearn> learnMap = getSkillTrees()[ClassId.Paladin.ordinal()];

		int skillHashCode = SkillTable.getSkillUID(239, grade);
		if (learnMap.containsKey(skillHashCode))
		{
			return learnMap.get(skillHashCode).getMinLevel();
		}

		_log.fatal("Expertise not found for grade " + grade);
		return 0;
	}

	public int getMinSkillLevel(int skillId, int skillLvl)
	{
		final int skillHashCode = SkillTable.getSkillUID(skillId, skillLvl);
		final Integer minLevel = _minSkillLevel.get(skillHashCode);
		return minLevel == null ? 0 : minLevel.intValue();
	}

	private SkillTreeTable()
	{
		int classId = 0;
		int count = 0;

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT * FROM class_list ORDER BY id");
			ResultSet classlist = statement.executeQuery();

			Map<Integer, L2SkillLearn> map;
			int parentClassId;
			L2SkillLearn skillLearn;

			while (classlist.next())
			{
				map = new HashMap<Integer, L2SkillLearn>();
				parentClassId = classlist.getInt("parent_id");
				classId = classlist.getInt("id");
				PreparedStatement statement2 = con
						.prepareStatement("SELECT class_id, skill_id, level, name, sp, min_level FROM skill_trees where class_id=? ORDER BY skill_id, level");
				statement2.setInt(1, classId);
				ResultSet skilltree = statement2.executeQuery();

				if (parentClassId != -1)
				{
					Map<Integer, L2SkillLearn> parentMap = getSkillTrees()[parentClassId];
					map.putAll(parentMap);
				}

				int prevSkillId = -1;

				while (skilltree.next())
				{
					final int id = skilltree.getInt("skill_id");
					final int lvl = skilltree.getInt("level");
					final int minLvl = skilltree.getInt("min_level");
					final int cost = skilltree.getInt("sp");

					if (prevSkillId != id)
						prevSkillId = id;

					skillLearn = new L2SkillLearn(id, lvl, minLvl, cost, 0, 0);
					int hash = SkillTable.getSkillUID(id, lvl);
					map.put(hash, skillLearn);
					if (_minSkillLevel.get(hash) == null)
						_minSkillLevel.put(hash, skillLearn.getMinLevel());
				}

				getSkillTrees()[classId] = map;
				skilltree.close();
				statement2.close();

				count += map.size();
				if (_log.isDebugEnabled())
					_log.info(getClass().getSimpleName() + " : skill tree for class " + classId + " has " + map.size() + " skills");
			}

			classlist.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.fatal("Error while creating skill tree (Class ID " + classId + "):", e);
		}

		_log.info(getClass().getSimpleName() + " : Loaded " + count + " skill(s).");

		Document doc = null;
		try
		{
			_fishingSkillTrees = new ArrayList<L2SkillLearn>();
			_expandDwarfCraftSkillTrees = new ArrayList<L2SkillLearn>();

			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(new File(Config.DATAPACK_ROOT, "data/char_data/skill_tree/fishing_skill_tree.xml"));

			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						int skillId = 0, skillLevel = 0;
						int minimumLevel = 0, cost = 0, costId = 0, costCount = 0, isDwarven = 0;

						if ("fish".equalsIgnoreCase(d.getNodeName()))
						{
							skillId = Integer.parseInt(d.getAttributes().getNamedItem("skill_id").getNodeValue());
							skillLevel = Integer.parseInt(d.getAttributes().getNamedItem("level").getNodeValue());
							minimumLevel = Integer.parseInt(d.getAttributes().getNamedItem("min_level").getNodeValue());
							cost = Integer.parseInt(d.getAttributes().getNamedItem("sp").getNodeValue());
							costId = Integer.parseInt(d.getAttributes().getNamedItem("costid").getNodeValue());
							costCount = Integer.parseInt(d.getAttributes().getNamedItem("cost").getNodeValue());
							isDwarven = Integer.parseInt(d.getAttributes().getNamedItem("isfordwarf").getNodeValue());

							final L2SkillLearn skill = new L2SkillLearn(skillId, skillLevel, minimumLevel, cost, costId, costCount);

							if (isDwarven == 0)
								_fishingSkillTrees.add(skill);
							else
								_expandDwarfCraftSkillTrees.add(skill);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.fatal("Error while creating fishing skill table: ", e);
		}

		try
		{
			_enchantSkillTrees = new HashMap<Integer, L2EnchantSkillLearn>();

			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT * FROM enchant_skill_trees ORDER BY skill_id, level");
			ResultSet skilltree3 = statement.executeQuery();

			while (skilltree3.next())
			{
				final int id = skilltree3.getInt("skill_id");
				final int baseLvl = skilltree3.getInt("base_lvl");

				L2EnchantSkillLearn skill = _enchantSkillTrees.get(id);
				if (skill == null)
				{
					skill = new L2EnchantSkillLearn(id, baseLvl);
					_enchantSkillTrees.put(id, skill);
				}
				EnchantSkillDetail esd = new EnchantSkillDetail(skilltree3);
				skill.addEnchantDetail(esd);
			}

			skilltree3.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.fatal("Error while creating enchant skill table: ", e);
		}

		try
		{
			_pledgeSkillTrees = new ArrayList<L2PledgeSkillLearn>();

			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con
					.prepareStatement("SELECT skill_id, level, name, clan_lvl, repCost, itemId, itemCount FROM pledge_skill_trees ORDER BY skill_id, level");
			ResultSet skilltree4 = statement.executeQuery();

			int prevSkillId = -1;

			while (skilltree4.next())
			{
				final int id = skilltree4.getInt("skill_id");
				final int lvl = skilltree4.getInt("level");
				final String name = skilltree4.getString("name");
				final int baseLvl = skilltree4.getInt("clan_lvl");
				final int sp = skilltree4.getInt("repCost");
				final int itemId = skilltree4.getInt("itemId");
				final long itemCount = skilltree4.getLong("itemCount");

				if (prevSkillId != id)
					prevSkillId = id;

				L2PledgeSkillLearn skill = new L2PledgeSkillLearn(id, lvl, baseLvl, name, sp, itemId, itemCount);

				_pledgeSkillTrees.add(skill);
			}

			skilltree4.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.fatal("Error while creating pledge skill table: ", e);
		}
		
		try
		{
			_transformSkillTrees = new ArrayList<L2TransformSkillLearn>();
			
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(new File(Config.DATAPACK_ROOT, "data/char_data/skill_tree/transform_skill_tree.xml"));

			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						int raceId = 0, skillId = 0, itemId = 0, level = 0, sp = 0, minLevel = 0;

						if ("transform".equalsIgnoreCase(d.getNodeName()))
						{
							raceId = Integer.parseInt(d.getAttributes().getNamedItem("race_id").getNodeValue());
							skillId = Integer.parseInt(d.getAttributes().getNamedItem("skill_id").getNodeValue());
							itemId = Integer.parseInt(d.getAttributes().getNamedItem("item_id").getNodeValue());
							level = Integer.parseInt(d.getAttributes().getNamedItem("level").getNodeValue());
							sp = Integer.parseInt(d.getAttributes().getNamedItem("sp").getNodeValue());
							minLevel = Integer.parseInt(d.getAttributes().getNamedItem("min_level").getNodeValue());

							final L2TransformSkillLearn skill = new L2TransformSkillLearn(raceId, skillId, itemId, level, sp, minLevel);

							_transformSkillTrees.add(skill);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.fatal("Error while creating Transformation skill table ", e);
		}
		
		try
		{
			_specialSkillTrees = new ArrayList<L2SkillLearn>();
			
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(new File(Config.DATAPACK_ROOT, "data/char_data/skill_tree/special_skill_tree.xml"));

			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						int skillId = 0, level = 0, itemId, itemCount = 0;

						if ("special".equalsIgnoreCase(d.getNodeName()))
						{
							skillId = Integer.parseInt(d.getAttributes().getNamedItem("skill_id").getNodeValue());
							level = Integer.parseInt(d.getAttributes().getNamedItem("level").getNodeValue());
							itemId = Integer.parseInt(d.getAttributes().getNamedItem("costid").getNodeValue());
							itemCount = Integer.parseInt(d.getAttributes().getNamedItem("cost").getNodeValue());

							final L2SkillLearn skill = new L2SkillLearn(skillId, level, 0, 0, itemId, itemCount);

							_specialSkillTrees.add(skill);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.fatal("Error while creating SpecialSkillTree skill table ", e);
		}
		
		try
		{
			_certificationSkillsTrees = new ArrayList<L2CertificationSkillsLearn>();
			
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(new File(Config.DATAPACK_ROOT, "data/char_data/skill_tree/certification_skill_tree.xml"));

			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						int skillId = 0, itemId = 0, level = 0;

						if ("certification".equalsIgnoreCase(d.getNodeName()))
						{
							skillId = Integer.parseInt(d.getAttributes().getNamedItem("skill_id").getNodeValue());
							itemId = Integer.parseInt(d.getAttributes().getNamedItem("item_id").getNodeValue());
							level = Integer.parseInt(d.getAttributes().getNamedItem("level").getNodeValue());

							final L2CertificationSkillsLearn skill = new L2CertificationSkillsLearn(skillId, itemId, level);

							_certificationSkillsTrees.add(skill);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.fatal("Error while creating Certification skill table ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		_log.info("FishingSkillTreeTable : Loaded " + _fishingSkillTrees.size() + " general skill(s).");
		_log.info("DwarvenSkillTreeTable : Loaded " + _expandDwarfCraftSkillTrees.size() + " dwarven skill(s).");
		_log.info("EnchantSkillTreeTable : Loaded " + _enchantSkillTrees.size() + " enchant skill(s).");
		_log.info("PledgeSkillTreeTable : Loaded " + _pledgeSkillTrees.size() + " pledge skill(s).");
		_log.info("TransformSkillTreeTable : Loaded " + _transformSkillTrees.size() + " transform skill(s).");
		_log.info("SpecialSkillTreeTable : Loaded " + _specialSkillTrees.size() + " special skill(s).");
		_log.info("CertificationSkillsTreeTable : Loaded " + _certificationSkillsTrees.size() + " certification skill(s).");
	}

	private Map<Integer, L2SkillLearn>[] getSkillTrees()
	{
		return _skillTrees;
	}

	public L2SkillLearn[] getAvailableSkills(L2Player cha, ClassId classId)
	{
		ArrayBunch<L2SkillLearn> result = new ArrayBunch<L2SkillLearn>();
		Collection<L2SkillLearn> skills = getAllowedSkills(classId);

		if (skills == null)
		{
			// the skilltree for this class is undefined, so we give an empty list
			_log.warn("Skilltree for class " + classId + " is not defined !");
			return new L2SkillLearn[0];
		}

		L2Skill[] oldSkills = cha.getAllSkills();

		for (L2SkillLearn temp : skills)
		{
			if (temp.getMinLevel() <= cha.getLevel())
			{
				boolean knownSkill = false;

				for (int j = 0; j < oldSkills.length && !knownSkill; j++)
				{
					if (oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;

						if (oldSkills[j].getLevel() == temp.getLevel() - 1)
						{
							// this is the next level of a skill that we know
							result.add(temp);
						}
					}
				}

				if (!knownSkill && temp.getLevel() == 1)
				{
					// this is a new skill
					result.add(temp);
				}
			}
		}

		return result.moveToArray(new L2SkillLearn[result.size()]);
	}

	public String giveAvailableSkills(L2Player activeChar)
	{
		Map<Integer, L2Skill> skillsToAdd = new HashMap<Integer, L2Skill>();

		for (L2SkillLearn temp : getAllowedSkills(activeChar.getClassId()))
		{
			if (temp.getMinLevel() > activeChar.getLevel())
				continue;

			if (temp.getId() == L2Skill.SKILL_EXPERTISE)
				continue;

			if (temp.getId() == L2Skill.SKILL_LUCKY)
				continue;

			if (temp.getId() == L2Skill.SKILL_DIVINE_INSPIRATION && !Config.ALT_AUTO_LEARN_DIVINE_INSPIRATION)
				continue;

			L2Skill knownSkill = activeChar.getKnownSkill(temp.getId());

			if (knownSkill != null && knownSkill.getLevel() >= temp.getLevel())
				continue;

			L2Skill mappedSkill = skillsToAdd.get(temp.getId());

			if (mappedSkill != null && mappedSkill.getLevel() >= temp.getLevel())
				continue;

			L2Skill skill = SkillTable.getInstance().getInfo(temp.getId(), temp.getLevel());

			if (skill == null)
				continue;

			skillsToAdd.put(temp.getId(), skill);
		}

		long skillsAdded = 0;
		long newSkillsAdded = 0;

		for (L2Skill skill : skillsToAdd.values())
		{
			skillsAdded++;

			if (!activeChar.hasSkill(skill.getId()))
				newSkillsAdded++;

			// fix when learning toggle skills
			if (skill.isToggle())
			{
				L2Effect toggleEffect = activeChar.getFirstEffect(skill.getId());
				if (toggleEffect != null)
				{
					// stop old toggle skill effect, and give new toggle skill effect back
					toggleEffect.exit();
					skill.getEffects(activeChar, activeChar);
				}
			}

			activeChar.addSkill(skill, true);
		}

		return skillsAdded + " (" + newSkillsAdded + " new) skill(s)";
	}

	public L2SkillLearn[] getAvailableSpecialSkills(L2Player cha)
	{
		ArrayBunch<L2SkillLearn> result = new ArrayBunch<L2SkillLearn>();

		L2Skill[] oldSkills = cha.getAllSkills();

		for (L2SkillLearn temp : _specialSkillTrees)
		{
			boolean knownSkill = false;

			for (int j = 0; j < oldSkills.length && !knownSkill; j++)
			{
				if (oldSkills[j].getId() == temp.getId())
				{
					knownSkill = true;

					if (oldSkills[j].getLevel() == temp.getLevel() - 1)
					{
						// this is the next level of a skill that we know
						result.add(temp);
					}
				}
			}

			if (!knownSkill && temp.getLevel() == 1)
			{
				// this is a new skill
				result.add(temp);
			}
		}

		return result.moveToArray(new L2SkillLearn[result.size()]);
	}

	public L2SkillLearn[] getAvailableFishingSkills(L2Player cha)
	{
		ArrayBunch<L2SkillLearn> result = new ArrayBunch<L2SkillLearn>();

		//if (skills == null)
		//{
		//    // the skilltree for this class is undefined, so we give an empty list
		//    _log.warn("Skilltree for fishing is not defined !");
		//    return new L2SkillLearn[0];
		//}

		Iterable<L2SkillLearn> iterable = cha.hasDwarvenCraft() ? L2Collections.concatenatedIterable(_fishingSkillTrees, _expandDwarfCraftSkillTrees) : _fishingSkillTrees;

		L2Skill[] oldSkills = cha.getAllSkills();

		for (L2SkillLearn temp : iterable)
		{
			if (temp.getMinLevel() <= cha.getLevel())
			{
				boolean knownSkill = false;

				for (int j = 0; j < oldSkills.length && !knownSkill; j++)
				{
					if (oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;

						if (oldSkills[j].getLevel() == temp.getLevel() - 1)
						{
							// this is the next level of a skill that we know
							result.add(temp);
						}
					}
				}

				if (!knownSkill && temp.getLevel() == 1)
				{
					// this is a new skill
					result.add(temp);
				}
			}
		}

		return result.moveToArray(new L2SkillLearn[result.size()]);
	}

	public L2EnchantSkillLearn getSkillEnchantmentForSkill(L2Skill skill)
	{
		L2EnchantSkillLearn esl = getSkillEnchantmentBySkillId(skill.getId());
		// there is enchantment for this skill and we have the required level of it
		if (esl != null && skill.getLevel() >= esl.getBaseLevel())
		{
			return esl;
		}
		return null;
	}

	public L2EnchantSkillLearn getSkillEnchantmentBySkillId(int skillId)
	{
		return _enchantSkillTrees.get(skillId);
	}

	public Collection<L2EnchantSkillLearn> getSkillEnchantments()
	{
		return _enchantSkillTrees.values();
	}

	public L2PledgeSkillLearn[] getAvailablePledgeSkills(L2Player cha)
	{
		ArrayBunch<L2PledgeSkillLearn> result = new ArrayBunch<L2PledgeSkillLearn>();
		List<L2PledgeSkillLearn> skills = _pledgeSkillTrees;

		if (skills == null)
		{
			// the skilltree for this class is undefined, so we give an empty list
			_log.warn("No clan skills defined!");
			return new L2PledgeSkillLearn[0];
		}

		L2Skill[] oldSkills = cha.getClan().getAllSkills();

		for (L2PledgeSkillLearn temp : skills)
		{
			if (temp.getBaseLevel() <= cha.getClan().getLevel())
			{
				boolean knownSkill = false;

				for (int j = 0; j < oldSkills.length && !knownSkill; j++)
				{
					if (oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;
						if (oldSkills[j].getLevel() == temp.getLevel() - 1)
						{
							// this is the next level of a skill that we know
							result.add(temp);
						}
					}
				}

				if (!knownSkill && temp.getLevel() == 1)
				{
					// this is a new skill
					result.add(temp);
				}
			}
		}
		return result.moveToArray(new L2PledgeSkillLearn[result.size()]);
	}

	public L2TransformSkillLearn[] getAvailableTransformSkills(L2Player cha)
	{
		ArrayBunch<L2TransformSkillLearn> result = new ArrayBunch<L2TransformSkillLearn>();
		List<L2TransformSkillLearn> skills = _transformSkillTrees;

		if (skills == null)
		{
			// the skilltree for this class is undefined, so we give an empty list

			_log.warn("No Transform skills defined!");
			return new L2TransformSkillLearn[0];
		}

		L2Skill[] oldSkills = cha.getAllSkills();

		for (L2TransformSkillLearn temp : skills)
		{
			if (temp.getMinLevel() <= cha.getLevel() && (temp.getRace() == cha.getRace().ordinal() || temp.getRace() == -1))
			{
				boolean knownSkill = false;

				for (int j = 0; j < oldSkills.length && !knownSkill; j++)
				{
					if (oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;

						if (oldSkills[j].getLevel() == temp.getLevel() - 1)
						{
							// this is the next level of a skill that we know
							result.add(temp);
						}
					}
				}

				if (!knownSkill && temp.getLevel() == 1)
				{
					// this is a new skill
					result.add(temp);
				}
			}
		}

		return result.moveToArray(new L2TransformSkillLearn[result.size()]);
	}

	public L2CertificationSkillsLearn[] getAvailableCertificationSkills(L2Player cha)
	{
		ArrayBunch<L2CertificationSkillsLearn> result = new ArrayBunch<L2CertificationSkillsLearn>();
		List<L2CertificationSkillsLearn> skills = _certificationSkillsTrees;

		if (skills == null)
		{
			// the skilltree for this class is undefined, so we give an empty list

			_log.warn("No certification skills defined!");
			return new L2CertificationSkillsLearn[0];
		}

		if (cha.isSubClassActive())
			return new L2CertificationSkillsLearn[0];

		L2Skill[] oldSkills = cha.getAllSkills();

		for (L2CertificationSkillsLearn temp : skills)
		{
			boolean knownSkill = false;

			for (int j = 0; j < oldSkills.length && !knownSkill; j++)
			{
				if (oldSkills[j].getId() == temp.getId() && cha.getInventory().getItemByItemId(temp.getItemId()) != null)
				{
					knownSkill = true;

					if (oldSkills[j].getLevel() == temp.getLevel() - 1)
					{
						// this is the next level of a skill that we know
						result.add(temp);
					}
				}
			}

			if (!knownSkill && temp.getLevel() == 1 && cha.getInventory().getItemByItemId(temp.getItemId()) != null)
			{
				// this is a new skill
				result.add(temp);
			}
		}

		return result.moveToArray(new L2CertificationSkillsLearn[result.size()]);
	}

	/**
	 * Returns all allowed skills for a given class.
	 * 
	 * @param classId
	 * @return all allowed skills for a given class.
	 */
	public Collection<L2SkillLearn> getAllowedSkills(ClassId classId)
	{
		return getSkillTrees()[classId.ordinal()].values();
	}

	public Set<Integer> getAllowedSkillUIDs(ClassId classId)
	{
		return getSkillTrees()[classId.ordinal()].keySet();
	}

	public int getMinLevelForNewSkill(L2Player cha, ClassId classId)
	{
		int minLevel = 0;
		Collection<L2SkillLearn> skills = getAllowedSkills(classId);

		if (skills == null)
		{
			// the skilltree for this class is undefined, so we give an empty list
			_log.warn("Skilltree for class " + classId + " is not defined !");
			return minLevel;
		}

		for (L2SkillLearn temp : skills)
		{
			if (temp.getMinLevel() > cha.getLevel() && temp.getSpCost() != 0)
				if (minLevel == 0 || temp.getMinLevel() < minLevel)
					minLevel = temp.getMinLevel();
		}

		return minLevel;
	}

	public int getMinLevelForNewFishingSkill(L2Player cha)
	{
		int minLevel = 0;
		List<L2SkillLearn> skills = new ArrayList<L2SkillLearn>();

		skills.addAll(_fishingSkillTrees);

		//if (skills == null)
		//{
		//    // the skilltree for this class is undefined, so we give an empty list
		//    _log.warn("SkillTree for fishing is not defined !");
		//    return minLevel;
		//}

		if (cha.hasDwarvenCraft() && _expandDwarfCraftSkillTrees != null)
		{
			skills.addAll(_expandDwarfCraftSkillTrees);
		}

		for (L2SkillLearn s : skills)
		{
			if (s.getMinLevel() > cha.getLevel())
				if (minLevel == 0 || s.getMinLevel() < minLevel)
					minLevel = s.getMinLevel();
		}

		return minLevel;
	}

	public int getMinLevelForNewTransformSkill(L2Player cha)
	{
		int minLevel = 0;
		List<L2TransformSkillLearn> skills = new ArrayList<L2TransformSkillLearn>();

		skills.addAll(_transformSkillTrees);

		if (skills.size() == 0)
		{
			// the skilltree for this class is undefined, so we give an empty list
			_log.warn("SkillTree for transformation is not defined !");
			return minLevel;
		}

		for (L2TransformSkillLearn s : skills)
		{
			if ((s.getMinLevel() > cha.getLevel()) && (s.getRace() == cha.getRace().ordinal()))
				if (minLevel == 0 || s.getMinLevel() < minLevel)
					minLevel = s.getMinLevel();
		}

		return minLevel;
	}

	public int getSkillCost(L2Player player, L2Skill skill)
	{
		int skillCost = 100000000;
		final ClassId classId = player.getSkillLearningClassId();
		final int skillHashCode = SkillTable.getSkillUID(skill);

		if (getSkillTrees()[classId.ordinal()].containsKey(skillHashCode))
		{
			L2SkillLearn skillLearn = getSkillTrees()[classId.ordinal()].get(skillHashCode);
			if (skillLearn.getMinLevel() <= player.getLevel())
			{
				skillCost = skillLearn.getSpCost();
				if (!player.getClassId().equalsOrChildOf(classId))
					return skillCost;
			}
		}

		return skillCost;
	}

	public int getEnchantSkillSpCost(L2Skill skill)
	{
		L2EnchantSkillLearn enchantSkillLearn = _enchantSkillTrees.get(skill.getId());
		if (enchantSkillLearn != null)
		{
			EnchantSkillDetail esd = enchantSkillLearn.getEnchantSkillDetail(skill.getLevel());
			if (esd != null)
			{
				return esd.getSpCost();
			}
		}

		return Integer.MAX_VALUE;
	}

	public int getEnchantSkillAdenaCost(L2Skill skill)
	{
		L2EnchantSkillLearn enchantSkillLearn = _enchantSkillTrees.get(skill.getId());
		if (enchantSkillLearn != null)
		{
			EnchantSkillDetail esd = enchantSkillLearn.getEnchantSkillDetail(skill.getLevel());
			if (esd != null)
			{
				return esd.getAdenaCost();
			}
		}

		return Integer.MAX_VALUE;
	}

	public byte getEnchantSkillRate(L2Player player, L2Skill skill)
	{
		L2EnchantSkillLearn enchantSkillLearn = _enchantSkillTrees.get(skill.getId());
		if (enchantSkillLearn != null)
		{
			EnchantSkillDetail esd = enchantSkillLearn.getEnchantSkillDetail(skill.getLevel());
			if (esd != null)
			{
				return esd.getRate(player);
			}
		}

		return 0;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final SkillTreeTable	_instance	= new SkillTreeTable();
	}
}
