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
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.entity.player.keyboard.ActionKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author  mrTJO
 */
public final class UIDataHolder
{
	private static final Log					_log				= LogFactory.getLog(UIDataHolder.class);

	private final Map<Integer, List<ActionKey>>	_storedKeys			= new FastMap<Integer, List<ActionKey>>();
	private final Map<Integer, List<Integer>>	_storedCategories	= new FastMap<Integer, List<Integer>>();

	private static final class SingletonHolder
	{
		private static final UIDataHolder	INSTANCE	= new UIDataHolder();
	}

	public static UIDataHolder getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private UIDataHolder()
	{
		reload();
	}

	public void reload()
	{
		_storedKeys.clear();
		_storedCategories.clear();
		parseCatData();
		parseKeyData();
		_log.info(getClass().getSimpleName() + " : Loaded " + _storedCategories.size() + " Categorie(s).");
		_log.info(getClass().getSimpleName() + " : Loaded " + _storedKeys.size() + " Key(s).");
	}

	private void parseCatData()
	{
		Document doc = null;

		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			final File file = new File(Config.DATAPACK_ROOT, "data/ui/uicats_en.xml");
			doc = factory.newDocumentBuilder().parse(file);

			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						int category = 0, command = 0;
						if ("uicat".equalsIgnoreCase(d.getNodeName()))
						{
							category = Integer.parseInt(d.getAttributes().getNamedItem("category").getNodeValue());
							command = Integer.parseInt(d.getAttributes().getNamedItem("command").getNodeValue());
							insertCategory(category, command);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}
	}

	private void parseKeyData()
	{
		Document doc = null;

		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			final File file = new File(Config.DATAPACK_ROOT, "data/ui/uikeys_en.xml");
			doc = factory.newDocumentBuilder().parse(file);

			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						int category = 0, command = 0, key = 0, toogleKey1 = 0, toogleKey2 = 0, showType = 0;
						if ("uikey".equalsIgnoreCase(d.getNodeName()))
						{
							category = Integer.parseInt(d.getAttributes().getNamedItem("category").getNodeValue());
							command = Integer.parseInt(d.getAttributes().getNamedItem("command").getNodeValue());
							key = Integer.parseInt(d.getAttributes().getNamedItem("key").getNodeValue());
							toogleKey1 = Integer.parseInt(d.getAttributes().getNamedItem("toogleKey1").getNodeValue());
							toogleKey2 = Integer.parseInt(d.getAttributes().getNamedItem("toogleKey2").getNodeValue());
							showType = Integer.parseInt(d.getAttributes().getNamedItem("showType").getNodeValue());
							insertKey(category, command, key, toogleKey1, toogleKey2, showType);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}
	}

	private void insertCategory(int category, int command)
	{
		if (_storedCategories.containsKey(category))
			_storedCategories.get(category).add(command);
		else
		{
			final List<Integer> tmp = new FastList<Integer>();
			tmp.add(command);
			_storedCategories.put(category, tmp);
		}
	}

	private void insertKey(int category, int commandId, int key, int toogleKey1, int toogleKey2, int showType)
	{
		final ActionKey tmk = new ActionKey(category, commandId, key, toogleKey1, toogleKey2, showType);
		if (_storedKeys.containsKey(category))
			_storedKeys.get(category).add(tmk);
		else
		{
			final List<ActionKey> tmp = new FastList<ActionKey>();
			tmp.add(tmk);
			_storedKeys.put(category, tmp);
		}
	}

	public Map<Integer, List<Integer>> getCategories()
	{
		return _storedCategories;
	}

	public Map<Integer, List<ActionKey>> getKeys()
	{
		return _storedKeys;
	}
}
