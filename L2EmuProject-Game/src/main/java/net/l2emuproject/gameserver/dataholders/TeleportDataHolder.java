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

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastMap;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.world.mapregion.L2TeleportLocation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * 
 * @author Intrepid
 *
 */
public class TeleportDataHolder
{
	private final static Log						_log	= LogFactory.getLog(TeleportDataHolder.class);

	private FastMap<Integer, L2TeleportLocation>	_teleports;

	public static TeleportDataHolder getInstance()
	{
		return SingletonHolder._instance;
	}

	private TeleportDataHolder()
	{
		_teleports = new FastMap<Integer, L2TeleportLocation>();
		load();
	}

	public void load()
	{	
		Document doc = null;
		
		for (File f : Util.getDatapackFiles("dataholders/teleport", ".xml"))
		{
			int count = 0;
			try
			{
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating(true);
				factory.setIgnoringComments(true);
				doc = factory.newDocumentBuilder().parse(f);
			}
			catch (Exception e)
			{
				_log.fatal("TeleportDataHolder: Error loading file " + f.getAbsolutePath(), e);
				continue;
			}
			try
			{
				count = parseDocument(doc);
			}
			catch (Exception e)
			{
				_log.fatal("TeleportDataHolder: Error in file " + f.getAbsolutePath(), e);
				continue;
			}
			_log.info(getClass().getSimpleName() + " : " + f.getName() + " loaded with " + count + " teleport(s).");
		}
	}

	private int parseDocument(Document doc)
	{
		int teleportCount = 0;
		
		L2TeleportLocation teleport = new L2TeleportLocation();
		
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("teleport".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						Node att;
						
						att = attrs.getNamedItem("id");
						if (att == null)
						{
							_log.warn("TeleportDataHolder: Missing id.");
							continue;
						}
						teleport.setTeleId(Integer.parseInt(att.getNodeValue()));
						
						att = attrs.getNamedItem("locX");
						if (att == null)
						{
							_log.warn("TeleportDataHolder: Missing locX.");
							continue;
						}
						teleport.setLocX(Integer.parseInt(att.getNodeValue()));
						
						att = attrs.getNamedItem("locY");
						if (att == null)
						{
							_log.warn("TeleportDataHolder: Missing locY.");
							continue;
						}
						teleport.setLocY(Integer.parseInt(att.getNodeValue()));
						
						att = attrs.getNamedItem("locZ");
						if (att == null)
						{
							_log.warn("TeleportDataHolder: Missing locZ.");
							continue;
						}
						teleport.setLocZ(Integer.parseInt(att.getNodeValue()));
						
						att = attrs.getNamedItem("price");
						if (att == null)
						{
							_log.warn("TeleportDataHolder: Missing price.");
							continue;
						}
						if (Config.ALT_GAME_FREE_TELEPORT)
							teleport.setPrice(0);
						else
							teleport.setPrice(Integer.parseInt(att.getNodeValue()));
						
						att = attrs.getNamedItem("isNoble");
						if (att == null)
						{
							_log.warn("TeleportDataHolder: Missing isNoble.");
							continue;
						}
						teleport.setIsForNoble(Boolean.parseBoolean(att.getNodeValue()));
						
						_teleports.put(teleport.getTeleId(), teleport);
						
						teleportCount++;
					}
				}
			}
		}
		
		return teleportCount;
	}

	/**
	 * @param template id
	 * @return
	 */
	public L2TeleportLocation getTemplate(int id)
	{
		return _teleports.get(id);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final TeleportDataHolder _instance = new TeleportDataHolder();
	}
}