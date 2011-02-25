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
package net.l2emuproject.gameserver.world.knownlist;

import java.util.Map;

import net.l2emuproject.gameserver.util.Util;
import net.l2emuproject.gameserver.world.L2WorldRegion;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.util.L2Collections;
import net.l2emuproject.util.SingletonMap;


public class CharKnownList extends ObjectKnownList
{
	protected final L2Character _activeChar;
	
	private final Map<Integer, L2Object> _knownObjects = new SingletonMap<Integer, L2Object>().shared();
	private final Map<Integer, L2Player> _knownPlayers = new SingletonMap<Integer, L2Player>().shared();
	
	public CharKnownList(L2Character activeChar)
	{
		_activeChar = activeChar;
	}
	
	@Override
	public boolean addKnownObject(L2Object object)
	{
		if (object == null || object == getActiveChar())
			return false;
		
		// Check if object is not inside distance to watch object
		if (!Util.checkIfInShortRadius(getDistanceToWatchObject(object), getActiveChar(), object, true))
			return false;
		
		// instance -1 for gms can see everything on all instances
		if (!getActiveChar().isSameInstance(object))
			return false;
		
		if (getKnownObjects().put(object.getObjectId(), object) != null)
			return false;
		
		if (object instanceof L2Player)
			getKnownPlayers().put(object.getObjectId(), (L2Player)object);
		
		return true;
	}
	
	public final boolean knowsObject(L2Object object)
	{
		return object != null && (getActiveChar() == object || _knownObjects != null && _knownObjects.containsKey(object.getObjectId()));
	}
	
	public final boolean knowsThePlayer(L2Player player)
	{
		return player != null && (getActiveChar() == player || _knownPlayers != null && _knownPlayers.containsKey(player.getObjectId()));
	}
	
	@Override
	public final L2Object getKnownObject(int objectId)
	{
		return getKnownObjects().get(objectId);
	}
	
	public final L2Player getKnownPlayer(int objectId)
	{
		return getKnownPlayers().get(objectId);
	}
	
	@Override
	public void removeAllKnownObjects()
	{
		for (L2Object object : getKnownObjects().values())
		{
			removeKnownObject(object);
			object.getKnownList().removeKnownObject(getActiveChar());
		}
		
		getKnownObjects().clear();
		
		getKnownPlayers().clear();
		
		// Set _target of the L2Character to null
		// Cancel Attack or Cast
		getActiveChar().setTarget(null);
		
		// Cancel AI Task
		if (getActiveChar().hasAI())
			getActiveChar().setAI(null);
	}
	
	@Override
	public boolean removeKnownObject(L2Object object)
	{
		if (object == null)
			return false;
		
		if (getKnownObjects().remove(object.getObjectId()) == null)
			return false;
		
		if (object instanceof L2Player)
			getKnownPlayers().remove(object.getObjectId());
		
		// If object is targeted by the L2Character, cancel Attack or Cast
		if (object == getActiveChar().getTarget())
			getActiveChar().setTarget(null);
		
		return true;
	}
	
	// =========================================================
	// Method - Private
	
	// =========================================================
	// Property - Public
	
	public L2Character getActiveChar()
	{
		return _activeChar;
	}
	
	public int getDistanceToForgetObject(L2Object object)
	{
		return 0;
	}
	
	public int getDistanceToWatchObject(L2Object object)
	{
		return 0;
	}
	
	public Iterable<L2Character> getKnownCharacters()
	{
		return L2Collections.filteredIterable(L2Character.class, getKnownObjects().values());
	}
	
	public Iterable<L2Character> getKnownCharactersInRadius(final int radius)
	{
		return L2Collections.filteredIterable(L2Character.class, getKnownObjects().values(),
			new L2Collections.Filter<L2Character>()
			{
				@Override
				public boolean accept(L2Character obj)
				{
					return Util.checkIfInRange(radius, getActiveChar(), obj, true);
				}
			});
	}
	
	public final Map<Integer, L2Object> getKnownObjects()
	{
		return _knownObjects;
	}
	
	public final Map<Integer, L2Player> getKnownPlayers()
	{
		return _knownPlayers;
	}
	
	public final Iterable<L2Player> getKnownPlayersInRadius(final int radius)
	{
		return L2Collections.filteredIterable(L2Player.class, getKnownPlayers().values(),
			new L2Collections.Filter<L2Player>()
			{
				@Override
				public boolean accept(L2Player player)
				{
					return Util.checkIfInRange(radius, getActiveChar(), player, true);
				}
			});
	}
	
	@Override
	public final void tryAddObjects(L2Object[][] surroundingObjects)
	{
		if (surroundingObjects == null)
		{
			final L2WorldRegion reg = getActiveChar().getWorldRegion();
			
			if (reg == null)
				return;
			
			surroundingObjects = reg.getAllSurroundingObjects2DArray();
		}
		
		for (L2Object[] regionObjects : surroundingObjects)
		{
			for (L2Object object : regionObjects)
			{
				addKnownObject(object);
				object.getKnownList().addKnownObject(getActiveChar());
			}
		}
	}
	
	@Override
	public final void tryRemoveObjects()
	{
		for (L2Object object : getKnownObjects().values())
		{
			tryRemoveObject(object);
			object.getKnownList().tryRemoveObject(getActiveChar());
		}
	}
	
	@Override
	public boolean tryRemoveObject(L2Object obj)
	{
		if (obj.isVisible() && Util.checkIfInShortRadius(getDistanceToForgetObject(obj), getActiveChar(), obj, true))
			return false;
		
		return removeKnownObject(obj);
	}
	
	private long _lastUpdate;
	
	public synchronized final void updateKnownObjects()
	{
		if (System.currentTimeMillis() - _lastUpdate < 100)
			return;
		
		tryRemoveObjects();
		tryAddObjects(null);
		
		_lastUpdate = System.currentTimeMillis();
	}
}
