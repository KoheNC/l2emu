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
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.entity.player.keyboard.ActionKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author  mrTJO
 */
public final class UIDataHolder
{
	private static final Log							_log				= LogFactory.getLog(UIDataHolder.class);

	private final Map<Integer, List<ActionKey>>	_storedKeys			= new FastMap<Integer, List<ActionKey>>();
	private final Map<Integer, List<Integer>>	_storedCategories	= new FastMap<Integer, List<Integer>>();

	@SuppressWarnings("synthetic-access")
	private static final class SingletonHolder
	{
		private static final UIDataHolder	_instance	= new UIDataHolder();
	}

	public static UIDataHolder getInstance()
	{
		return SingletonHolder._instance;
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
		DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
		dbfactory.setValidating(false);
		dbfactory.setIgnoringComments(true);
		File file = new File(Config.DATAPACK_ROOT, "data/dataholders/ui/uicats_en.xml");
		Document doc = null;
		if (file.exists())
		{
			try
			{
				doc = dbfactory.newDocumentBuilder().parse(file);
			}
			catch (SAXException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (ParserConfigurationException e)
			{
				e.printStackTrace();
			}
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("uicat".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							Node att;
							int cat,cmd;
							att = attrs.getNamedItem("category");
							if (att == null)
							{
								_log.warn("UIDataHolder: Missing category.");
								continue;
							}
							cat = Integer.parseInt(att.getNodeValue());
							att = attrs.getNamedItem("command");
							if (att == null)
							{
								_log.warn("UIDataHolder: Missing command.");
								continue;
							}
							cmd = Integer.parseInt(att.getNodeValue());
							
							insertCategory(cat, cmd);
						}
					}
				}
			}
		}
	}

	private void parseKeyData()
	{
		DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
		dbfactory.setValidating(false);
		dbfactory.setIgnoringComments(true);
		File file = new File(Config.DATAPACK_ROOT, "data/dataholders/ui/uikeys_en.xml");
		Document doc = null;
		if (file.exists())
		{
			try
			{
				doc = dbfactory.newDocumentBuilder().parse(file);
			}
			catch (SAXException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (ParserConfigurationException e)
			{
				e.printStackTrace();
			}
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("uikey".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							Node att;
							int cat,cmd,key,tk1,tk2,shw;
							att = attrs.getNamedItem("category");
							if (att == null)
							{
								_log.warn("UIDataHolder: Missing category.");
								continue;
							}
							cat = Integer.parseInt(att.getNodeValue());
							att = attrs.getNamedItem("command");
							if (att == null)
							{
								_log.warn("UIDataHolder: Missing command.");
								continue;
							}
							cmd = Integer.parseInt(att.getNodeValue());
							att = attrs.getNamedItem("key");
							if (att == null)
							{
								_log.warn("UIDataHolder: Missing key.");
								continue;
							}
							key = Integer.parseInt(att.getNodeValue());
							att = attrs.getNamedItem("toogleKey1");
							if (att == null)
							{
								_log.warn("UIDataHolder: Missing toggleKey1.");
								continue;
							}
							tk1 = Integer.parseInt(att.getNodeValue());
							att = attrs.getNamedItem("toogleKey2");
							if (att == null)
							{
								_log.warn("UIDataHolder: Missing toogleKey2.");
								continue;
							}
							tk2 = Integer.parseInt(att.getNodeValue());
							att = attrs.getNamedItem("showType");
							if (att == null)
							{
								_log.warn("UIDataHolder: Missing showType.");
								continue;
							}
							shw = Integer.parseInt(att.getNodeValue());
							
							insertKey(cat, cmd, key, tk1, tk2, shw);
						}
					}
				}
			}
		}
	}

	private void insertCategory(int cat, int cmd)
	{
		if (_storedCategories.containsKey(cat))
			_storedCategories.get(cat).add(cmd);
		else
		{
			List<Integer> tmp = new FastList<Integer>();
			tmp.add(cmd);
			_storedCategories.put(cat, tmp);
		}
	}

	private void insertKey(int cat, int cmdId, int key, int tgKey1, int tgKey2, int show)
	{
		ActionKey tmk = new ActionKey(cat, cmdId, key, tgKey1, tgKey2, show);
		if (_storedKeys.containsKey(cat))
			_storedKeys.get(cat).add(tmk);
		else
		{
			List<ActionKey> tmp = new FastList<ActionKey>();
			tmp.add(tmk);
			_storedKeys.put(cat, tmp);
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
