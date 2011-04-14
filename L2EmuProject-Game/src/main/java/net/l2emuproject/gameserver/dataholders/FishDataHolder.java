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
package net.l2emuproject.gameserver.dataholders;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.SkillTreeTable;
import net.l2emuproject.gameserver.services.fishing.FishData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * @author -Nemesiss-
 *
 */
public class FishDataHolder
{
	private final static Log		_log		= LogFactory.getLog(SkillTreeTable.class);

	private static List<FishData>	_fishsNormal;
	private static List<FishData>	_fishsEasy;
	private static List<FishData>	_fishsHard;

	public static FishDataHolder getInstance()
	{
		return SingletonHolder._instance;
	}

	private FishDataHolder()
	{
		Document doc = null;
		File file = new File(Config.DATAPACK_ROOT, "data/npc_data/fish/fish.xml");

		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(true);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(file);
			
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						int id = 0, lvl = 0, hp = 0, hpreg = 0, type = 0, group = 0, fish_guts = 0, guts_check_time = 0, wait_time = 0, combat_time = 0;
						String name = "";
						if ("fish".equalsIgnoreCase(d.getNodeName()))
						{
							final FishData fish;
							id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
							lvl = Integer.parseInt(d.getAttributes().getNamedItem("level").getNodeValue());
							name = d.getAttributes().getNamedItem("name").getNodeValue();
							hp = Integer.parseInt(d.getAttributes().getNamedItem("hp").getNodeValue());
							hpreg = Integer.parseInt(d.getAttributes().getNamedItem("hpregen").getNodeValue());
							type = Integer.parseInt(d.getAttributes().getNamedItem("fish_type").getNodeValue());
							group = Integer.parseInt(d.getAttributes().getNamedItem("fish_group").getNodeValue());
							fish_guts = Integer.parseInt(d.getAttributes().getNamedItem("fish_guts").getNodeValue());
							guts_check_time = Integer.parseInt(d.getAttributes().getNamedItem("guts_check_time").getNodeValue());
							wait_time = Integer.parseInt(d.getAttributes().getNamedItem("wait_time").getNodeValue());
							combat_time = Integer.parseInt(d.getAttributes().getNamedItem("combat_time").getNodeValue());
							
							fish = new FishData(id, lvl, name, hp, hpreg, type, group, fish_guts, guts_check_time, wait_time, combat_time);
							switch (fish.getGroup())
							{
							case 0:
								_fishsEasy.add(fish);
								break;
							case 1:
								_fishsNormal.add(fish);
								break;
							case 2:
								_fishsHard.add(fish);
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.fatal("error while creating fishes table", e);
		}

		_log.info(getClass().getSimpleName() + " : Loaded " + _fishsEasy.size() + " Easy Fish.");
		_log.info(getClass().getSimpleName() + " : Loaded " + _fishsNormal.size() + " Normal Fish.");
		_log.info(getClass().getSimpleName() + " : Loaded " + _fishsHard.size() + " Hard Fish.");
	}

	/**
	 * @param Fish - lvl
	 * @param Fish - type
	 * @param Fish - group
	 * @return List of Fish that can be fished
	 */
	public List<FishData> getFish(int lvl, int type, int group)
	{
		final List<FishData> result = new ArrayList<FishData>();
		List<FishData> _fishs = null;
		switch (group)
		{
		case 0:
			_fishs = _fishsEasy;
			break;
		case 1:
			_fishs = _fishsNormal;
			break;
		case 2:
			_fishs = _fishsHard;
		}
		if (_fishs == null)
		{
			// the fish list is empty
			_log.warn("Fish are not defined !");
			return null;
		}
		for (FishData f : _fishs)
		{
			if (f.getLevel() != lvl)
				continue;
			if (f.getType() != type)
				continue;

			result.add(f);
		}
		if (result.size() == 0)
			_log.warn("FishTable: Fishes are not definied for Lvl " + lvl + " type " + type + " group " + group);
		return result;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final FishDataHolder _instance = new FishDataHolder();
	}
}
