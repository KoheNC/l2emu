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
package net.l2emuproject.gameserver.world.object;

import net.l2emuproject.gameserver.entity.view.CharLikeView;
import net.l2emuproject.gameserver.entity.view.DecoyView;
import net.l2emuproject.gameserver.network.serverpackets.CharInfo;
import net.l2emuproject.gameserver.system.taskmanager.DecayTaskManager;
import net.l2emuproject.gameserver.templates.chars.L2CharTemplate;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;

public abstract class L2Decoy extends L2Character
{
	private final L2Player _owner;
	
	public L2Decoy(int objectId, L2CharTemplate template, L2Player owner)
	{
		super(objectId, template);
		getKnownList();
		getStat();
		getStatus();
		_owner = owner;
		getPosition().setXYZInvisible(owner.getX(), owner.getY(), owner.getZ());
		setIsInvul(false);
	}
	
	@Override
	protected CharLikeView initView()
	{
		return new DecoyView(this);
	}
	
	@Override
	public DecoyView getView()
	{
		return (DecoyView)_view;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		broadcastFullInfo();
	}
	
	@Override
	public void onAction(L2Player player)
	{
		player.setTarget(this);
	}
	
	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancelDecayTask(this);
	}
	
	@Override
	public void onDecay()
	{
		deleteMe(_owner);
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return _owner.isAutoAttackable(attacker);
	}
	
	public final int getNpcId()
	{
		return getTemplate().getNpcId();
	}
	
	@Override
	public int getLevel()
	{
		return getTemplate().getLevel();
	}
	
	public void deleteMe(L2Player owner)
	{
		decayMe();
		getKnownList().removeAllKnownObjects();
		owner.setDecoy(null);
	}
	
	public synchronized void unSummon(L2Player owner)
	{
		if (isVisible() && !isDead())
		{
			if (getWorldRegion() != null)
				getWorldRegion().removeFromZones(this);
			owner.setDecoy(null);
			decayMe();
			getKnownList().removeAllKnownObjects();
		}
	}
	
	public final L2Player getOwner()
	{
		return _owner;
	}
	
	@Override
	public L2Player getActingPlayer()
	{
		return _owner;
	}
	
	@Override
	public L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate)super.getTemplate();
	}
	
	@Override
	public void sendInfo(L2Player activeChar)
	{
		activeChar.sendPacket(new CharInfo(this));
	}
	
	@Override
	public void broadcastFullInfoImpl()
	{
		broadcastPacket(new CharInfo(this));
	}
}
