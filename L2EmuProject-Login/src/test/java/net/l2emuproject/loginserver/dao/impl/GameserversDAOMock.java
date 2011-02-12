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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.l2emuproject.loginserver.beans.Gameservers;
import net.l2emuproject.loginserver.dao.GameserversDAO;

import org.springframework.orm.ObjectRetrievalFailureException;


/**
 * DAO object for domain model class Gameservers.
 * @see net.l2emuproject.loginserver.beans.Gameservers
 */
public class GameserversDAOMock implements GameserversDAO
{
	private final Map<Integer, Gameservers>	referential	= new HashMap<Integer, Gameservers>();

	public GameserversDAOMock()
	{
		referential.put(1, new Gameservers(1, "548545", "*"));
	}

	/**
	 * Search by id
	 * @param id
	 * @return
	 */
	@Override
	public Gameservers getGameserverByServerId(int id)
	{
		if (!referential.containsKey(id))
		{
			throw new ObjectRetrievalFailureException("Gameservers", id);
		}
		return referential.get(id);
	}

	/**
	 * @see net.l2emuproject.loginserver.dao.GameserversDAO#createGameserver(java.lang.Object)
	 */
	@Override
	public int createGameserver(Gameservers obj)
	{
		referential.put(obj.getServerId(), obj);
		return obj.getServerId();
	}

	/**
	 * @see net.l2emuproject.loginserver.dao.GameserversDAO#createOrUpdate(java.lang.Object)
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
		Iterator<?> it = entities.iterator();
		while (it.hasNext())
		{
			createGameserver((Gameservers) it.next());
		}
	}

	/**
	 * @see net.l2emuproject.loginserver.dao.GameserversDAO#getAlGameservers()
	 */
	@Override
	public List<Gameservers> getAllGameservers()
	{
		return new ArrayList<Gameservers>(referential.values());
	}

	/**
	 * @see net.l2emuproject.loginserver.dao.GameserversDAO#removeGameserver(java.lang.Object)
	 */
	@Override
	public void removeGameserver(Gameservers obj)
	{
		referential.remove(obj.getServerId());

	}

	/**
	 * @see net.l2emuproject.loginserver.dao.GameserversDAO#removeGameserverById(java.io.Serializable)
	 */
	@Override
	public void removeGameserverByServerId(int id)
	{
		referential.remove(id);
	}

	/**
	 * @see net.l2emuproject.loginserver.dao.GameserversDAO#removeAll(java.util.Collection)
	 */
	@Override
	public void removeAll(Collection<?> entities)
	{
		Iterator<?> it = entities.iterator();
		while (it.hasNext())
		{
			removeGameserver((Gameservers) it.next());
		}
	}

	/**
	 * @see net.l2emuproject.loginserver.dao.GameserversDAO#update(java.lang.Object)
	 */
	@Override
	public void update(Object obj)
	{
		Gameservers acc = (Gameservers) obj;
		removeGameserverByServerId(acc.getServerId());
		createGameserver(acc);

	}

	@Override
	public void removeAll()
	{
		referential.clear();
	}

}
