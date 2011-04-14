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

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.NpcTable;
import net.l2emuproject.gameserver.world.npc.L2MinionData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author lord_rex
 */
public final class MinionDataHolder
{
	private static final Log	_log	= LogFactory.getLog(MinionDataHolder.class);

	private static final class SingletonHolder
	{
		private static final MinionDataHolder	INSTANCE	= new MinionDataHolder();
	}

	public static MinionDataHolder getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private MinionDataHolder()
	{
		load();
	}

	private void load()
	{
		Document doc = null;
		int count = 0;
		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(new File(Config.DATAPACK_ROOT, "data/npc_data/spawns/minion/minions.xml"));

			count = 0;
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("minionList".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						int bossId = 0, minionId = 0;
						byte minimum = 0, maximum = 0;

						if ("minion".equalsIgnoreCase(d.getNodeName()))
						{
							final L2MinionData minionData = new L2MinionData();

							bossId = Integer.parseInt(d.getAttributes().getNamedItem("bossId").getNodeValue());
							minionId = Integer.parseInt(d.getAttributes().getNamedItem("minionId").getNodeValue());
							minimum = Byte.parseByte(d.getAttributes().getNamedItem("minimum").getNodeValue());
							maximum = Byte.parseByte(d.getAttributes().getNamedItem("maximum").getNodeValue());

							minionData.setMinionId(minionId);
							minionData.setAmountMin(minimum);
							minionData.setAmountMax(maximum);
							NpcTable.getInstance().getTemplate(bossId).addRaidData(minionData);
							count++;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}
		_log.info(getClass().getSimpleName() + " : Loaded " + count + " minion(s).");
	}
}
