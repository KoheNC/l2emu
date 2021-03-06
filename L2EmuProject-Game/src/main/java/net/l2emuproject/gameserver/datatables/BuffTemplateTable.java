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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.l2emuproject.gameserver.entity.base.Experience;
import net.l2emuproject.gameserver.system.database.L2DatabaseFactory;
import net.l2emuproject.gameserver.templates.StatsSet;
import net.l2emuproject.gameserver.templates.skills.L2BuffTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class represents the buff templates list
 * @author G1ta0
 */
public final class BuffTemplateTable
{
	private final static Log _log = LogFactory.getLog(BuffTemplateTable.class);
	
	/** The table containing all buff templates */
	private final Map<Integer, TemplateList> _templates;
	
	/**
	 * Create and Load the buff templates from SQL Table buff_templates
	 */
	private BuffTemplateTable()
	{
		_templates = new HashMap<Integer, TemplateList>();
		reloadBuffTemplates();
	}
	
	/**
	 * Read and Load the buff templates from SQL Table buff_templates
	 */
	public final void reloadBuffTemplates()
	{
		_templates.clear();
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			final PreparedStatement statement = con.prepareStatement("SELECT * FROM buff_templates ORDER BY id, skill_order");
			final ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				StatsSet buff = new StatsSet();
				
				final int templateId = rset.getInt("id");
				buff.set("id", templateId);
				buff.set("name", rset.getString("name"));
				buff.set("skillId", rset.getInt("skill_id"));
				buff.set("skillLevel", rset.getInt("skill_level"));
				buff.set("skillOrder", rset.getInt("skill_order"));
				buff.set("forceCast", rset.getInt("skill_force"));
				buff.set("minLevel", rset.getInt("char_min_level"));
				buff.set("maxLevel", rset.getInt("char_max_level"));
				buff.set("race", rset.getInt("char_race"));
				buff.set("class", rset.getInt("char_class"));
				
				// Add this buff template to the buff template list
				final L2BuffTemplate template = new L2BuffTemplate(buff);
				if (template.getSkill() != null)
				{
					TemplateList list = getBuffTemplate(templateId);
					if (list == TemplateList.EMPTY_LIST)
					{
						list = new TemplateList(templateId, template.getName());
						_templates.put(templateId, list);
					}
					list.add(template);
				}
				else
					_log.warn("Skill doesn't exist: " + template.getSkillId() + " Lv" + template.getSkillLevel(),
							new IllegalArgumentException());
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Could not completely load buff templates!", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
			_log.info(getClass().getSimpleName() + " : Loaded " + _templates.size() + " buff template(s).");
		}
	}
	
	/**
	 * @return Returns the buffs of template by template Id
	 */
	public final TemplateList getBuffTemplate(int id)
	{
		final TemplateList list = _templates.get(id);
		if (list == null)
			return TemplateList.EMPTY_LIST;
		else
			return list;
	}
	
	/**
	 * @return Returns the template Id by template Name
	 */
	public final int getTemplateIdByName(String name)
	{
		for (TemplateList list : _templates.values())		
			if (list.getName().equals(name))
				return list.getId();
		
		return -1;
	}
	
	/**
	 * @return Returns the lowest char level for Buff template
	 */
	public final int getLowestLevel(int templateId)
	{
		return getBuffTemplate(templateId).getLvlMin();
	}
	
	/**
	 * @return Returns the highest char level for Buff template
	 */
	public final int getHighestLevel(int templateId)
	{
		return getBuffTemplate(templateId).getLvlMax();
	}
	
	/**
	 * @return Returns the buff templates
	 */
	public final Map<Integer, TemplateList> getBuffTemplateTable()
	{
		return _templates;
	}

	public static final class TemplateList
	{
		private static final TemplateList EMPTY_LIST = new TemplateList(0, "<<<Empty>>>");
		
		private final int _id;
		private final String _name;
		private int _lvlMin;
		private int _lvlMax;
		private final List<L2BuffTemplate> _buffs;
		
		private TemplateList(int id, String name)
		{
			_id = id;
			_name = name;
			_lvlMin = Experience.MAX_LEVEL;
			_lvlMax = 0;
			_buffs = new ArrayList<L2BuffTemplate>();
		}
		
		private final void add(L2BuffTemplate bt)
		{
			if (bt == null || bt.getId() != getId() || !bt.getName().equals(getName()))
			{
				_log.warn("", new IllegalArgumentException());
				return;
			}
			getBuffs().add(bt);
			if (bt.getMinLevel() < getLvlMin())
				_lvlMin = bt.getMinLevel();
			if (bt.getMaxLevel() > getLvlMax())
				_lvlMax = bt.getMaxLevel();
		}
		
		public final int getId()
		{
			return _id;
		}
		
		public final String getName()
		{
			return _name;
		}
		
		public final int getLvlMin()
		{
			return _lvlMin;
		}
		
		public final int getLvlMax()
		{
			return _lvlMax;
		}
		
		public final List<L2BuffTemplate> getBuffs()
		{
			return _buffs;
		}
	}
	
	public static BuffTemplateTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final BuffTemplateTable _instance = new BuffTemplateTable();
	}
}
