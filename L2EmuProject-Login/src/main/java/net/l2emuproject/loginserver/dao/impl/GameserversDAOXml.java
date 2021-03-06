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
package net.l2emuproject.loginserver.dao.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.l2emuproject.loginserver.beans.Gameservers;
import net.l2emuproject.loginserver.dao.GameserversDAO;
import net.l2emuproject.loginserver.system.util.Util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * DAO object for domain model class Gameservers.
 * Xml implementation.
 * 
 * @see net.l2emuproject.loginserver.beans.Gameservers
 */
public class GameserversDAOXml implements GameserversDAO
{
	private static final Log				_log		= LogFactory.getLog(GameserversDAOXml.class);

	private final Map<Integer, Gameservers>	serverNames	= new TreeMap<Integer, Gameservers>();

	/**
	 * Load server name from xml
	 */
	public GameserversDAOXml()
	{
		Util.printSection("ServerName(s)");
		InputStream in = null;
		try
		{
			try
			{
				in = new FileInputStream("servername.xml");
			}
			catch (final FileNotFoundException e)
			{
				// just for eclipse development, we have to search in dist folder
				in = new FileInputStream("dist/servername.xml");
			}

			final SAXReader reader = new SAXReader();
			final Document document = reader.read(in);

			final Element root = document.getRootElement();

			// Find all servers_list (should have only one)
			for (final Iterator<?> i = root.elementIterator("server"); i.hasNext();)
			{
				final Element server = (Element) i.next();
				Integer id = null;
				String name = null;
				// For each server, read the attributes
				for (final Iterator<?> iAttr = server.attributeIterator(); iAttr.hasNext();)
				{
					final Attribute attribute = (Attribute) iAttr.next();
					if (attribute.getName().equals("id"))
						id = new Integer(attribute.getValue());
					else if (attribute.getName().equals("name"))
						name = attribute.getValue();
				}
				if (id != null && name != null)
				{
					final Gameservers gs = new Gameservers();
					gs.setServerId(id);
					gs.setServerName(name);
					serverNames.put(id, gs);
				}
			}
			_log.info("Loaded " + serverNames.size() + " server names");
		}
		catch (final FileNotFoundException e)
		{
			_log.warn("servername.xml could not be loaded : " + e.getMessage(), e);
		}
		catch (final DocumentException e)
		{
			_log.warn("servername.xml could not be loaded : " + e.getMessage(), e);
		}
		finally
		{
			try
			{
				if (in != null)
					in.close();
			}
			catch (final Exception e)
			{
			}
		}
	}

	/**
	 * Search by id
	 * @param id
	 * @return
	 */
	@Override
	public Gameservers getGameserverByServerId(int id)
	{
		return serverNames.get(id);
	}

	/**
	 * @see net.l2emuproject.loginserver.dao.GameserversDAO#createGameserver(Gameservers)
	 */
	@Override
	public int createGameserver(Gameservers obj)
	{
		serverNames.put(obj.getServerId(), obj);
		return obj.getServerId();
	}

	/**
	 * @see net.l2emuproject.loginserver.dao.GameserversDAO#createOrUpdate(Gameservers)
	 */
	@Override
	public void createOrUpdate(Gameservers obj)
	{
		createGameserver(obj);

	}

	/**
	 * @see net.l2emuproject.loginserver.dao.GameserversDAO#createOrUpdateAll(java.util.Collection)
	 */
	@Override
	public void createOrUpdateAll(Collection<?> entities)
	{
		final Iterator<?> it = entities.iterator();
		while (it.hasNext())
			createGameserver((Gameservers) it.next());
	}

	/**
	 * @see net.l2emuproject.loginserver.dao.GameserversDAO#getAllGameservers()
	 */
	@Override
	public List<Gameservers> getAllGameservers()
	{
		if (serverNames == null)
			throw new ObjectRetrievalFailureException("Could not load gameservers", new NullPointerException("serverNames"));
		return new ArrayList<Gameservers>(serverNames.values());
	}

	/**
	 * @see net.l2emuproject.loginserver.dao.GameserversDAO#removeGameservers(Gameservers)
	 */
	@Override
	public void removeGameserver(Gameservers obj)
	{
		serverNames.remove(obj.getServerId());

	}

	/**
	 * @see net.l2emuproject.loginserver.dao.GameserversDAO#removeAccountById(java.io.Serializable)
	 */
	@Override
	public void removeGameserverByServerId(int id)
	{
		serverNames.remove(id);
	}

	/**
	 * @see net.l2emuproject.loginserver.dao.GameserversDAO#removeAll(java.util.Collection)
	 */
	@Override
	public void removeAll(Collection<?> entities)
	{
		final Iterator<?> it = entities.iterator();
		while (it.hasNext())
			removeGameserver((Gameservers) it.next());
	}

	/**
	 * @see net.l2emuproject.loginserver.dao.GameserversDAO#update(java.lang.Object)
	 */
	@Override
	public void update(Object obj)
	{
		final Gameservers gs = (Gameservers) obj;
		removeGameserverByServerId(gs.getServerId());
		createGameserver(gs);
	}

	/* (non-Javadoc)
	 * @see net.l2emuproject.loginserver.dao.GameserversDAO#removeAll()
	 */
	@Override
	public void removeAll()
	{
		serverNames.clear();
	}
}
