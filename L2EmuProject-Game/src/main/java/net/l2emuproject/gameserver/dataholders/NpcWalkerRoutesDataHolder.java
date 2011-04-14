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
import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastList;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.SpawnTable;
import net.l2emuproject.gameserver.world.npc.L2NpcWalkerNode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Main Table to Load Npc Walkers Routes and Chat SQL Table.<br>
 * 
 * @author Rayan RPG for L2Emu Project
 */
public final class NpcWalkerRoutesDataHolder
{
	private final static Log				_log	= LogFactory.getLog(SpawnTable.class);

	private final FastList<L2NpcWalkerNode>	_routes	= new FastList<L2NpcWalkerNode>();

	private static final class SingletonHolder
	{
		private static final NpcWalkerRoutesDataHolder	INSTANCE	= new NpcWalkerRoutesDataHolder();
	}

	public static NpcWalkerRoutesDataHolder getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private NpcWalkerRoutesDataHolder()
	{
		load();
		_log.info(getClass().getSimpleName() + " : Loaded " + _routes.size() + " Walker Route(s).");
	}

	public void load()
	{
		Document doc = null;
		File file = new File(Config.DATAPACK_ROOT, "data/npc_data/walker_routes.xml");

		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(file);

			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						int routeId = 0, npcId = 0, moveX = 0, moveY = 0, moveZ = 0, delay = 0;
						String movePoint = "", chatText = "";
						boolean running = false;
						if ("npc".equalsIgnoreCase(d.getNodeName()))
						{
							final L2NpcWalkerNode route = new L2NpcWalkerNode();
							routeId = Integer.parseInt(d.getAttributes().getNamedItem("route_id").getNodeValue());
							npcId = Integer.parseInt(d.getAttributes().getNamedItem("npc_id").getNodeValue());
							movePoint = d.getAttributes().getNamedItem("move_point").getNodeValue();
							chatText = d.getAttributes().getNamedItem("chatText").getNodeValue();
							moveX = Integer.parseInt(d.getAttributes().getNamedItem("move_x").getNodeValue());
							moveY = Integer.parseInt(d.getAttributes().getNamedItem("move_y").getNodeValue());
							moveZ = Integer.parseInt(d.getAttributes().getNamedItem("move_z").getNodeValue());
							delay = Integer.parseInt(d.getAttributes().getNamedItem("delay").getNodeValue());
							running = Boolean.parseBoolean(d.getAttributes().getNamedItem("running").getNodeValue());

							route.setRouteId(routeId);
							route.setNpcId(npcId);
							route.setMovePoint(movePoint);
							route.setChatText(chatText);
							route.setMoveX(moveX);
							route.setMoveY(moveY);
							route.setMoveZ(moveZ);
							route.setDelay(delay);
							route.setRunning(running);
							_routes.add(route);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.fatal("WalkerRoutesTable: Error while loading Npc Walker Routes: " + e.getMessage(), e);
		}
	}

	public ArrayList<L2NpcWalkerNode> getRouteForNpc(final int id)
	{
		final ArrayList<L2NpcWalkerNode> list = new ArrayList<L2NpcWalkerNode>();

		for (FastList.Node<L2NpcWalkerNode> n = _routes.head(), end = _routes.tail(); (n = n.getNext()) != end;)
			if (n.getValue().getNpcId() == id)
				list.add(n.getValue());

		return list;
	}
}
