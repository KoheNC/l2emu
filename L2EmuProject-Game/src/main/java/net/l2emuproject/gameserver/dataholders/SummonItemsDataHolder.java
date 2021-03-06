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
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.items.L2SummonItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author FBIagent
 * @reworked by Michiru
 */
public final class SummonItemsDataHolder
{
	private static final Log					_log			= LogFactory.getLog(SummonItemsDataHolder.class);

	private final Map<Integer, L2SummonItem>	_summonItems	= new HashMap<Integer, L2SummonItem>();

	private int[]								_summonItemIds;

	private static final class SingletonHolder
	{
		private static final SummonItemsDataHolder	INSTANCE	= new SummonItemsDataHolder();
	}

	public static SummonItemsDataHolder getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private SummonItemsDataHolder()
	{
		Document doc = null;
		File file = new File(Config.DATAPACK_ROOT, "data/item_data/summon_items/summon_items.xml");

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(file);

			int itemID = 0, npcID = 0;
			byte summonType = 0;
			Node a;
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("item".equalsIgnoreCase(d.getNodeName()))
						{
							a = d.getAttributes().getNamedItem("id");
							if (a == null)
								throw new Exception("Error in summon item defenition!");
							itemID = Integer.parseInt(a.getNodeValue());

							for (Node e = d.getFirstChild(); e != null; e = e.getNextSibling())
							{
								if ("npcId".equalsIgnoreCase(e.getNodeName()))
								{
									a = e.getAttributes().getNamedItem("val");
									if (a == null)
										throw new Exception("Not defined npc id for summon item id=" + itemID + "!");
									npcID = Integer.parseInt(a.getNodeValue());
								}
								else if ("summonType".equalsIgnoreCase(e.getNodeName()))
								{
									a = e.getAttributes().getNamedItem("val");
									if (a == null)
										throw new Exception("Not defined summon type for summon item id=" + itemID + "!");
									summonType = Byte.parseByte(a.getNodeValue());
								}
							}
							final L2SummonItem summonitem = new L2SummonItem(itemID, npcID, summonType);
							_summonItems.put(itemID, summonitem);
						}
					}
				}
			}
			_summonItemIds = new int[_summonItems.size()];
			int i = 0;
			for (int itemId : _summonItems.keySet())
				_summonItemIds[i++] = itemId;
		}
		catch (IOException e)
		{
			_log.warn("SummonItemsData: Can not find " + file.getAbsolutePath() + " !", e);
		}
		catch (Exception e)
		{
			_log.warn("SummonItemsData: Error while parsing " + file.getAbsolutePath() + " !", e);
		}
		_log.info(getClass().getSimpleName() + " : Loaded " + _summonItems.size() + " Summon Items from " + file.getName());
	}

	public L2SummonItem getSummonItem(final int itemId)
	{
		return _summonItems.get(itemId);
	}

	public int[] itemIDs()
	{
		return _summonItemIds;
	}
}
