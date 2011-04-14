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
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.world.mapregion.L2TeleportLocation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author Intrepid
 */
public final class TeleportDataHolder
{
	private final static Log						_log		= LogFactory.getLog(TeleportDataHolder.class);

	private final Map<Integer, L2TeleportLocation>	_teleports	= new HashMap<Integer, L2TeleportLocation>();

	private static final class SingletonHolder
	{
		private static final TeleportDataHolder	INSTANCE	= new TeleportDataHolder();
	}

	public static TeleportDataHolder getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private TeleportDataHolder()
	{
		load();
	}

	public void reload()
	{
		_teleports.clear();
		load();
	}

	private void load()
	{
		Document doc = null;

		for (File f : Util.getDatapackFiles("npc_data/teleport", ".xml"))
		{
			try
			{
				final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating(true);
				factory.setIgnoringComments(true);
				doc = factory.newDocumentBuilder().parse(f);

				for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
				{
					if ("list".equalsIgnoreCase(n.getNodeName()))
					{
						for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							int teleportId = 0, locX = 0, locY = 0, locZ = 0, price = 0;
							boolean isNoble = false;
							if ("teleport".equalsIgnoreCase(d.getNodeName()))
							{
								final L2TeleportLocation teleport = new L2TeleportLocation();
								teleportId = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
								locX = Integer.parseInt(d.getAttributes().getNamedItem("locX").getNodeValue());
								locY = Integer.parseInt(d.getAttributes().getNamedItem("locY").getNodeValue());
								locZ = Integer.parseInt(d.getAttributes().getNamedItem("locZ").getNodeValue());
								price = Integer.parseInt(d.getAttributes().getNamedItem("price").getNodeValue());
								isNoble = Boolean.parseBoolean(d.getAttributes().getNamedItem("isNoble").getNodeValue());

								teleport.setTeleId(teleportId);
								teleport.setLocX(locX);
								teleport.setLocY(locY);
								teleport.setLocZ(locZ);
								teleport.setPrice(price);
								teleport.setIsForNoble(isNoble);

								_teleports.put(teleport.getTeleId(), teleport);
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				_log.fatal("TeleportDataHolder: Error in file " + f.getAbsolutePath(), e);
				continue;
			}
			_log.info(getClass().getSimpleName() + " : " + f.getName() + " loaded with " + _teleports.size() + " teleport(s).");
		}
	}

	public L2TeleportLocation getTemplate(int id)
	{
		return _teleports.get(id);
	}
}
