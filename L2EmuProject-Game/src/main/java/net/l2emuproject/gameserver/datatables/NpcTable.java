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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.entity.base.ClassId;
import net.l2emuproject.gameserver.services.attribute.Attributes;
import net.l2emuproject.gameserver.services.quest.QuestService;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.Stats;
import net.l2emuproject.gameserver.skills.formulas.Formulas;
import net.l2emuproject.gameserver.system.database.L2DatabaseFactory;
import net.l2emuproject.gameserver.templates.StatsSet;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.npc.L2MinionData;
import net.l2emuproject.gameserver.world.npc.L2NpcCharData;
import net.l2emuproject.gameserver.world.npc.drop.L2DropCategory;
import net.l2emuproject.gameserver.world.npc.drop.L2DropData;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.instance.L2MonsterInstance;
import net.l2emuproject.util.LookupTable;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public final class NpcTable
{
	private static final Log _log = LogFactory.getLog(NpcTable.class);
	
	public static NpcTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private final LookupTable<L2NpcTemplate> _npcs = new LookupTable<L2NpcTemplate>();
	
	private NpcTable()
	{
		restoreNpcData();
	}
	
	private void restoreNpcData()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			try
			{
				PreparedStatement statement = con.prepareStatement("SELECT * FROM npc");
				ResultSet npcdata = statement.executeQuery();
				fillNpcTable(npcdata);
				npcdata.close();
				statement.close();
				_log.info(getClass().getSimpleName() + " : Loaded " + _npcs.size() + " Npc Template(s).");
			}
			catch (Exception e)
			{
				_log.fatal("NPCTable: Error creating NPC table: ", e);
			}
			
			try
			{
				PreparedStatement statement = con.prepareStatement("SELECT * FROM custom_npc");
				ResultSet npcdata = statement.executeQuery();
				int npc_count = _npcs.size();
				fillNpcTable(npcdata);
				npcdata.close();
				statement.close();
				if (_npcs.size() > npc_count)
					_log.info(getClass().getSimpleName() + " : Loaded " + (_npcs.size() - npc_count) + " Custom Npc Template(s).");
			}
			catch (Exception e)
			{
				_log.fatal("NPCTable: Error creating custom NPC table: ", e);
			}
			
			try
			{
				PreparedStatement statement = con.prepareStatement("SELECT npcid, skillid, level FROM npcskills");
				ResultSet npcskills = statement.executeQuery();
				
				while (npcskills.next())
				{
					int mobId = npcskills.getInt("npcid");
					L2NpcTemplate npcDat = _npcs.get(mobId);
					
					if (npcDat == null)
						continue;
					
					int skillId = npcskills.getInt("skillid");
					int level = npcskills.getInt("level");
					
					if (skillId == 4416)
					{
						npcDat.setRace(level);
						continue;
					}
					
					L2Skill npcSkill = SkillTable.getInstance().getInfo(skillId, level);
					
					if (npcSkill == null)
						continue;
					
					npcDat.addSkill(npcSkill);
				}
				
				npcskills.close();
				statement.close();
			}
			catch (Exception e)
			{
				_log.fatal("NPCTable: Error reading NPC skills table: ", e);
			}
			
			try
			{
				PreparedStatement statement10 = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
				{ "npc_id", "elemAtkType", "elemAtkValue", "fireDefValue", "waterDefValue", "earthDefValue", "windDefValue", "holyDefValue", "darkDefValue" })
						+ " FROM npc_elementals ORDER BY npc_id");
				ResultSet npcElementals = statement10.executeQuery();
				L2NpcTemplate npcDat = null;
				int cont = 0;
				while (npcElementals.next())
				{
					int npc_id = npcElementals.getInt("npc_id");
					npcDat = _npcs.get(npc_id);
					if (npcDat == null)
					{
						_log.error("NPCElementals: Elementals Error with id : " + npc_id);
						continue;
					}
					switch (npcElementals.getByte("elemAtkType"))
					{
						case Attributes.FIRE:
							npcDat.setBaseFire(npcElementals.getInt("elemAtkValue"));
							break;
						case Attributes.WATER:
							npcDat.setBaseWater(npcElementals.getInt("elemAtkValue"));
							break;
						case Attributes.EARTH:
							npcDat.setBaseEarth(npcElementals.getInt("elemAtkValue"));
							break;
						case Attributes.WIND:
							npcDat.setBaseWind(npcElementals.getInt("elemAtkValue"));
							break;
						case Attributes.HOLY:
							npcDat.setBaseHoly(npcElementals.getInt("elemAtkValue"));
							break;
						case Attributes.DARK:
							npcDat.setBaseDark(npcElementals.getInt("elemAtkValue"));
							break;
						default:
							_log.error("NPCElementals: Elementals Error with id : " + npc_id + "; unknown elementType: " + npcElementals.getByte("elemAtkType"));
							continue;
					}
					npcDat.setBaseFireRes(npcElementals.getInt("fireDefValue"));
					npcDat.setBaseWaterRes(npcElementals.getInt("waterDefValue"));
					npcDat.setBaseEarthRes(npcElementals.getInt("earthDefValue"));
					npcDat.setBaseWindRes(npcElementals.getInt("windDefValue"));
					npcDat.setBaseHolyRes(npcElementals.getInt("holyDefValue"));
					npcDat.setBaseDarkRes(npcElementals.getInt("darkDefValue"));
					cont++;
				}
				npcElementals.close();
				statement10.close();
				_log.info("NPC Elementals Data Table : Loaded " + cont + " elementals Data.");
			}
			catch (Exception e)
			{
				_log.fatal("NPCTable: Error reading NPC Elementals Data: " + e.getMessage(), e);
			}
			
			try
			{
				PreparedStatement statement = con.prepareStatement("SELECT npcid, skillid, level FROM custom_npcskills");
				ResultSet npcskills = statement.executeQuery();
				
				while (npcskills.next())
				{
					int mobId = npcskills.getInt("npcid");
					L2NpcTemplate npcDat = _npcs.get(mobId);
					
					if (npcDat == null)
					{
						_log.warn("NpcTable: Missing template for custom_npcskills for npc id: " + mobId);
						continue;
					}
					
					final int skillId = npcskills.getInt("skillid");
					final int level = npcskills.getInt("level");
					
					if (skillId == 4416)
					{
						npcDat.setRace(level);
						continue;
					}
					
					L2Skill npcSkill = SkillTable.getInstance().getInfo(skillId, level);
					
					if (npcSkill == null)
					{
						_log.warn("NpcTable: Missing skill for custom_npcskills for skill id-lvl: " + skillId + " " + level);
						continue;
					}
					
					npcDat.addSkill(npcSkill);
				}
				
				npcskills.close();
				statement.close();
			}
			catch (Exception e)
			{
				_log.fatal("NPCTable: Error reading custom NPC skills table: ", e);
			}
			
			try
			{
				PreparedStatement statement2 = con.prepareStatement("SELECT * FROM droplist ORDER BY mobId, chance DESC");
				ResultSet dropData = statement2.executeQuery();
				
				while (dropData.next())
				{
					int mobId = dropData.getInt("mobId");
					L2NpcTemplate npcDat = _npcs.get(mobId);
					if (npcDat == null)
					{
						_log.fatal("NPCTable: Drop data for undefined NPC. npcId: " + mobId);
						continue;
					}
					L2DropData dropDat = new L2DropData();
					
					dropDat.setItemId(dropData.getInt("itemId"));
					dropDat.setMinDrop(dropData.getInt("min"));
					dropDat.setMaxDrop(dropData.getInt("max"));
					dropDat.setChance(dropData.getInt("chance"));
					
					int category = dropData.getInt("category");
					
					npcDat.addDropData(dropDat, category);
				}
				
				dropData.close();
				statement2.close();
			}
			catch (Exception e)
			{
				_log.fatal("NPCTable: Error reading NPC drop data: ", e);
			}
			
			try
			{
				PreparedStatement statement2 = con.prepareStatement("SELECT * FROM custom_droplist ORDER BY mobId, chance DESC");
				ResultSet dropData = statement2.executeQuery();
				
				while (dropData.next())
				{
					final int mobId = dropData.getInt("mobId");
					L2NpcTemplate npcDat = _npcs.get(mobId);
					if (npcDat == null)
					{
						_log.fatal("NPCTable: Custom drop data for undefined NPC. npcId: " + mobId);
						continue;
					}
					L2DropData dropDat = new L2DropData();
					
					dropDat.setItemId(dropData.getInt("itemId"));
					dropDat.setMinDrop(dropData.getInt("min"));
					dropDat.setMaxDrop(dropData.getInt("max"));
					dropDat.setChance(dropData.getInt("chance"));
					
					int category = dropData.getInt("category");
					
					npcDat.addDropData(dropDat, category);
				}
				
				dropData.close();
				statement2.close();
			}
			catch (Exception e)
			{
				_log.fatal("NPCTable: Error reading custom NPC drop data: ", e);
			}
			
			try
			{
				PreparedStatement statement3 = con.prepareStatement("SELECT * FROM skill_learn");
				ResultSet learndata = statement3.executeQuery();
				
				while (learndata.next())
				{
					final int npcId = learndata.getInt("npc_id");
					final int classId = learndata.getInt("class_id");
					L2NpcTemplate npc = getTemplate(npcId);

					if (npc == null)
					{
						_log.warn("NPCTable: Error getting NPC template ID " + npcId + " while trying to load skill trainer data.");
						continue;
					}

					npc.addTeachInfo(ClassId.values()[classId]);
				}
				
				learndata.close();
				statement3.close();
			}
			catch (Exception e)
			{
				_log.fatal("NPCTable: Error reading NPC trainer data: ", e);
			}
			
			try
			{
				PreparedStatement statement10 = con.prepareStatement("SELECT "
						+ L2DatabaseFactory.getInstance().safetyString(
								new String[]
								{
										"npc_id",
										"ischar",
										"charclass",
										"charrace",
										"charface",
										"charhair",
										"charhaircolor",
										"charcolor",
										"charsex",
										"charhero",
										"lrhand",
										"armor",
										"pant",
										"head",
										"boot",
										"glove",
										"dhair",
										"hair",
										"face",
										"enchlvl",
										"augmentation" }) + " FROM npc_char_data ORDER BY npc_id");
				ResultSet NpcCharDataTable = statement10.executeQuery();
				L2NpcCharData npcCharDat = null;
				L2NpcTemplate npcDat = null;
				int cont = 0;
				while (NpcCharDataTable.next())
				{
					final int npc_id = NpcCharDataTable.getInt("npc_id");
					npcDat = _npcs.get(npc_id);
					if (npcDat == null)
					{
						_log.error("NPCTable: Npc Character Data Error with id : " + npc_id);
						continue;
					}
					npcCharDat = new L2NpcCharData();

					npcCharDat.setIsChar(NpcCharDataTable.getInt("ischar"));
					npcCharDat.setCharClass(NpcCharDataTable.getInt("charclass"));
					npcCharDat.setCharRace(NpcCharDataTable.getInt("charrace"));
					npcCharDat.setCharFace(NpcCharDataTable.getInt("charface"));
					npcCharDat.setCharHair(NpcCharDataTable.getInt("charhair"));
					npcCharDat.setCharHairColor(NpcCharDataTable.getInt("charhaircolor"));
					npcCharDat.setCharColor(NpcCharDataTable.getString("charcolor"));
					npcCharDat.setCharSex(NpcCharDataTable.getInt("charsex"));
					npcCharDat.setCharHero(NpcCharDataTable.getInt("charhero"));
					npcCharDat.setLrhand(NpcCharDataTable.getInt("lrhand"));
					npcCharDat.setArmor(NpcCharDataTable.getInt("armor"));
					npcCharDat.setPant(NpcCharDataTable.getInt("pant"));
					npcCharDat.setHead(NpcCharDataTable.getInt("head"));
					npcCharDat.setBoot(NpcCharDataTable.getInt("boot"));
					npcCharDat.setGlove(NpcCharDataTable.getInt("glove"));
					npcCharDat.setHair(NpcCharDataTable.getInt("hair"));
					npcCharDat.setDHair(NpcCharDataTable.getInt("dhair"));
					npcCharDat.setFace(NpcCharDataTable.getInt("face"));
					npcCharDat.setEnchLvl(NpcCharDataTable.getInt("enchlvl"));
					npcCharDat.setAugmentation(NpcCharDataTable.getInt("augmentation"));

					npcDat.setCharData(npcCharDat);
					cont++;
				}

				NpcCharDataTable.close();
				statement10.close();
				_log.info("NPC Character Data : Loaded " + cont + " char data(s).");
			}
			catch (Exception e)
			{
				_log.error("NPCTable: Error reading NPC Character char data: ", e);
			}
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	private boolean fillNpcTable(ResultSet NpcData) throws Exception
	{
		boolean loaded = false;
		while (NpcData.next())
		{
			StatsSet npcDat = new StatsSet();
			final int id = NpcData.getInt("id");
			
			npcDat.set("npcId", id);
			npcDat.set("idTemplate", NpcData.getInt("idTemplate"));
			final int level = NpcData.getInt("level");
			npcDat.set("level", level);
			npcDat.set("jClass", NpcData.getString("class"));
			
			npcDat.set("baseShldDef", 0);
			npcDat.set("baseShldRate", 0);
			npcDat.set("baseCritRate", 38);
			
			npcDat.set("name", NpcData.getString("name"));
			npcDat.set("serverSideName", NpcData.getBoolean("serverSideName"));
			npcDat.set("title", NpcData.getString("title"));
			npcDat.set("serverSideTitle", NpcData.getBoolean("serverSideTitle"));
			npcDat.set("collision_radius", NpcData.getDouble("collision_radius"));
			npcDat.set("collision_height", NpcData.getDouble("collision_height"));
			npcDat.set("fcollision_radius", NpcData.getDouble("collision_radius"));
			npcDat.set("fcollision_height", NpcData.getDouble("collision_height"));
			npcDat.set("sex", NpcData.getString("sex"));
			if (!Config.ALLOW_NPC_WALKERS && NpcData.getString("type").equalsIgnoreCase("L2NpcWalker"))
				npcDat.set("type", "L2Npc");
			else
				npcDat.set("type", NpcData.getString("type"));
			npcDat.set("baseAtkRange", NpcData.getInt("attackrange"));
			npcDat.set("rewardExp", NpcData.getInt("exp"));
			npcDat.set("rewardSp", NpcData.getInt("sp"));
			npcDat.set("basePAtkSpd", NpcData.getInt("atkspd"));
			npcDat.set("baseMAtkSpd", NpcData.getInt("matkspd"));
			npcDat.set("aggroRange", NpcData.getInt("aggro"));
			npcDat.set("rhand", NpcData.getInt("rhand"));
			npcDat.set("lhand", NpcData.getInt("lhand"));
			npcDat.set("armor", NpcData.getInt("armor"));
			npcDat.set("baseWalkSpd", NpcData.getInt("walkspd"));
			npcDat.set("baseRunSpd", NpcData.getInt("runspd"));

			npcDat.safeSet("baseSTR", NpcData.getInt("str"), 0, Formulas.MAX_STAT_VALUE, "Loading NPC template; ID: " + npcDat.getString("idTemplate"));
			npcDat.safeSet("baseCON", NpcData.getInt("con"), 0, Formulas.MAX_STAT_VALUE, "Loading NPC template; ID: " + npcDat.getString("idTemplate"));
			npcDat.safeSet("baseDEX", NpcData.getInt("dex"), 0, Formulas.MAX_STAT_VALUE, "Loading NPC template; ID: " + npcDat.getString("idTemplate"));
			npcDat.safeSet("baseINT", NpcData.getInt("int"), 0, Formulas.MAX_STAT_VALUE, "Loading NPC template; ID: " + npcDat.getString("idTemplate"));
			npcDat.safeSet("baseWIT", NpcData.getInt("wit"), 0, Formulas.MAX_STAT_VALUE, "Loading NPC template; ID: " + npcDat.getString("idTemplate"));
			npcDat.safeSet("baseMEN", NpcData.getInt("men"), 0, Formulas.MAX_STAT_VALUE, "Loading NPC template; ID: " + npcDat.getString("idTemplate"));

			npcDat.set("baseHpMax", NpcData.getInt("hp"));
			npcDat.set("baseCpMax", 0);
			npcDat.set("baseMpMax", NpcData.getInt("mp"));
			npcDat.set("baseHpReg", NpcData.getFloat("hpreg") > 0 ? NpcData.getFloat("hpreg") : 1.5 + ((level - 1) / 10.0));
			npcDat.set("baseMpReg", NpcData.getFloat("mpreg") > 0 ? NpcData.getFloat("mpreg") : 0.9 + 0.3 * ((level - 1) / 10.0));
			npcDat.set("basePAtk", NpcData.getInt("patk"));
			npcDat.set("basePDef", NpcData.getInt("pdef"));
			npcDat.set("baseMAtk", NpcData.getInt("matk"));
			npcDat.set("baseMDef", NpcData.getInt("mdef"));
			
			npcDat.set("factionId", NpcData.getString("faction_id"));
			npcDat.set("factionRange", NpcData.getInt("faction_range"));
			
			npcDat.set("isUndead", NpcData.getString("isUndead"));
			
			npcDat.set("absorb_level", NpcData.getString("absorb_level"));
			npcDat.set("absorb_type", NpcData.getString("absorb_type"));
			
			npcDat.set("ss", NpcData.getInt("ss"));
			npcDat.set("bss", NpcData.getInt("bss"));
			npcDat.set("ssRate", NpcData.getInt("ss_rate"));
			
			npcDat.set("AI", NpcData.getString("AI"));
			npcDat.set("drop_herbs", Boolean.valueOf(NpcData.getString("drop_herbs")));
			
			L2NpcTemplate template = new L2NpcTemplate(npcDat);
			template.addVulnerability(Stats.BOW_WPN_VULN, 1);
			template.addVulnerability(Stats.CROSSBOW_WPN_VULN, 1);
			template.addVulnerability(Stats.BLUNT_WPN_VULN, 1);
			template.addVulnerability(Stats.DAGGER_WPN_VULN, 1);
			
			_npcs.set(id, template);
			
			loaded = true;
		}
		return loaded;
	}
	
	public boolean reloadNpc(int id)
	{
		Connection con = null;
		boolean loaded = false;
		try
		{
			// save a copy of the old data
			L2NpcTemplate old = getTemplate(id);
			FastMap<Integer, L2Skill> skills = null;

			// L2NpcTemplate.getSkillS() is unmodifiable, so the entrySet() of it can't be used
			if (old != null && old.getSkills() != null)
			{
				skills = new FastMap<Integer, L2Skill>(old.getSkills().size());

				for (Integer key : old.getSkills().keySet())
					skills.put(key, old.getSkills().get(key));
			}

			L2DropCategory[] categories = new L2DropCategory[0];

			if (old != null && old.getDropData() != null)
				categories = (L2DropCategory[])ArrayUtils.addAll(categories, old.getDropData());

			FastList<ClassId> classIds = new FastList<ClassId>();

			if (old != null && old.getTeachInfo() != null)
				classIds.addAll(old.getTeachInfo());

			L2MinionData[] minions = new L2MinionData[0];

			if (old != null && old.getMinionData() != null)
				minions = (L2MinionData[])ArrayUtils.addAll(minions, old.getMinionData());

			// reload the NPC base data
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement("SELECT * FROM npc WHERE id=?");
			st.setInt(1, id);
			ResultSet rs = st.executeQuery();
			loaded = fillNpcTable(rs);
			rs.close();
			st.close();

			if (!loaded)
			{
				st = con.prepareStatement("SELECT * FROM custom_npc WHERE id=?");
				st.setInt(1, id);
				rs = st.executeQuery();
				loaded = fillNpcTable(rs);
				rs.close();
				st.close();
			}

			// restore additional data from saved copy
			L2NpcTemplate created = getTemplate(id);

			if (skills != null)
				for (L2Skill skill : skills.values())
					created.addSkill(skill);

			for (ClassId classId : classIds)
				created.addTeachInfo(classId);

			for (L2MinionData minion : minions)
				created.addRaidData(minion);
		}
		catch (Exception e)
		{
			_log.warn("NPCTable: Could not reload data for NPC " + id + ": " + e, e);
			loaded = false;
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		return loaded;
	}

	// just wrapper
	public void reloadAll()
	{
		reloadAll(true);
	}
	
	public void reloadAll(boolean reloadQuests)
	{
		restoreNpcData();
		if (reloadQuests)
			QuestService.getInstance().reloadAllQuests();
	}

	public void cleanUp()
	{
		_npcs.clear(false);
	}

	public void saveNpc(StatsSet npc)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			Map<String, Object> set = npc.getSet();

			String name = "";
			String values = "";

			for (Object obj : set.keySet())
			{
				name = (String) obj;

				if (name.equalsIgnoreCase("npcId"))
					continue;

				if (values != "")
					values += ", ";

				values += name + " = '" + set.get(name) + "'";
			}

			String query = "UPDATE npc SET " + values + " WHERE id = ?";
			String query_custom = "UPDATE custom_npc SET " + values + " WHERE id = ?";

			try
			{
				PreparedStatement statement = con.prepareStatement(query);
				statement.setInt(1, npc.getInteger("npcId"));
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.warn("", e);
			}

			try
			{
				PreparedStatement statement = con.prepareStatement(query_custom);
				statement.setInt(1, npc.getInteger("npcId"));
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.warn("", e);
			}
		}
		catch (Exception e)
		{
			_log.warn("NPCTable: Could not store new NPC data in database: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public L2NpcTemplate getTemplate(int id)
	{
		return _npcs.get(id);
	}

	public Iterable<L2NpcTemplate> getAllTemplates()
	{
		return _npcs;
	}

	public L2NpcTemplate getTemplateByName(String name)
	{
		for (L2NpcTemplate npcTemplate : _npcs)
			if (npcTemplate.getName().equalsIgnoreCase(name))
				return npcTemplate;
		return null;
	}

	public L2NpcTemplate[] getAllOfLevel(int lvl)
	{
		FastList<L2NpcTemplate> list = new FastList<L2NpcTemplate>();
		for (L2NpcTemplate t : _npcs)
			if (t.getLevel() == lvl)
				list.add(t);
		return list.toArray(new L2NpcTemplate[list.size()]);
	}

	public L2NpcTemplate[] getAllMonstersOfLevel(int lvl)
	{
		FastList<L2NpcTemplate> list = new FastList<L2NpcTemplate>();
		for (L2NpcTemplate t : _npcs)
			if (t.getLevel() == lvl && t.isAssignableTo(L2MonsterInstance.class))
				list.add(t);
		return list.toArray(new L2NpcTemplate[list.size()]);
	}

	public L2NpcTemplate[] getAllNpcStartingWith(String letter)
	{
		FastList<L2NpcTemplate> list = new FastList<L2NpcTemplate>();
		for (L2NpcTemplate t : _npcs)
			if (t.getName().startsWith(letter) && t.isAssignableTo(L2Npc.class))
				list.add(t);
		return list.toArray(new L2NpcTemplate[list.size()]);
	}

	/**
	 * @param classType
	 * @return
	 */
	public Set<Integer> getAllNpcOfClassType(String classType)
	{
		return null;
	}

	/**
	 * @param clazz
	 * @return
	 */
	public Set<Integer> getAllNpcOfL2jClass(Class<?> clazz)
	{
		return null;
	}

	/**
	 * @param aiType
	 * @return
	 */
	public Set<Integer> getAllNpcOfAiType(String aiType)
	{
		return null;
	}

	public List<L2NpcTemplate> getMobsByDrop(int itemid)
	{
		List<L2NpcTemplate> returnVal = new FastList<L2NpcTemplate>();
		for(L2NpcTemplate tempNpc:_npcs)
		{
			List<L2DropData> dropdata = tempNpc.getAllDropData();
			if(dropdata!=null)
			{
				for(L2DropData tempDrop:dropdata)
				{
					if(tempDrop.getItemId()==itemid)
					{
						returnVal.add(tempNpc);
						break;
					}
				}
			}
		}
		return returnVal;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final NpcTable _instance = new NpcTable();
	}
}
