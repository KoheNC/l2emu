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

import java.util.Collection;
import java.util.List;

import net.l2emuproject.loginserver.beans.Gameservers;
import net.l2emuproject.loginserver.dao.GameserversDAO;

import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * DAO object for domain model class Gameservers.
 * @see net.l2emuproject.loginserver.beans.Gameservers
 */
public class GameserversDAOHib extends BaseRootDAOHib implements GameserversDAO
{
	/**
	 * Search by id
	 * @param id
	 * @return
	 */
	@Override
	public Gameservers getGameserverByServerId(int id)
	{
		final Gameservers gameserver = (Gameservers) get(Gameservers.class, id);
		if (gameserver == null)
			throw new ObjectRetrievalFailureException("Gameserver", id);
		return gameserver;
	}

	/**
	 * @see net.l2emuproject.loginserver.dao.GameserversDAO#createGameserver(Gameservers)
	 */
	@Override
	public int createGameserver(Gameservers obj)
	{
		return (Integer) save(obj);
	}

	/**
	 * @see net.l2emuproject.loginserver.dao.GameserversDAO#createOrUpdate(Gameservers)
	 */
	@Override
	public void createOrUpdate(Gameservers obj)
	{
		saveOrUpdate(obj);

	}

	/**
	 * @see net.l2emuproject.loginserver.dao.GameserversDAO#createOrUpdateAll(java.util.Collection)
	 */
	@Override
	public void createOrUpdateAll(Collection<?> entities)
	{
		saveOrUpdateAll(entities);

	}

	/**
	 * @see net.l2emuproject.loginserver.dao.GameserversDAO#getAllGameservers()
	 */
	@Override
	public List<Gameservers> getAllGameservers()
	{
		return findAllOrderById(Gameservers.class);
	}

	/**
	 * Return all objects related to the implementation of this DAO with no filter.
	 */
	@SuppressWarnings("unchecked")
	public List<Gameservers> findAllOrderById(Class<Gameservers> refClass)
	{
		return getCurrentSession().createQuery("from " + refClass.getName() + " order by serverId").list();
	}

	/**
	 * @see net.l2emuproject.loginserver.dao.GameserversDAO#removeGameservers(Gameservers)
	 */
	@Override
	public void removeGameserver(Gameservers obj)
	{
		delete(obj);

	}

	/**
	 * @see net.l2emuproject.loginserver.dao.GameserversDAO#removeAccountById(java.io.Serializable)
	 */
	@Override
	public void removeGameserverByServerId(int id)
	{
		removeObject(Gameservers.class, id);
	}

	/* (non-Javadoc)
	 * @see net.l2emuproject.loginserver.dao.GameserversDAO#removeAll()
	 */
	@Override
	public void removeAll()
	{
		removeAll(getAllGameservers());
	}
}
