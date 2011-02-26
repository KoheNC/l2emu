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
package net.l2emuproject.gameserver.model.actor.instance;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.ThreadPoolManager;
import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.position.L2CharPosition;
import net.l2emuproject.tools.random.Rnd;


/**
 * @author Kerberos
 */
public class L2TownPetInstance extends L2Npc
{
    private int _spawnX, _spawnY, _spawnZ;

    public L2TownPetInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    @Override
    public void onAction(L2Player player)
    {
        if (!canTarget(player)) return;
        
        if (this != player.getTarget())
        {
            // Set the target of the L2Player player
            player.setTarget(this);
        }
        else
        {
            // Calculate the distance between the L2Player and the L2NpcInstance
            if (!canInteract(player))
            {
                // Notify the L2Player AI with AI_INTENTION_INTERACT
                player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
            }
        }
        // Send a Server->Client ActionFailed to the L2Player in order to avoid that the client wait another packet
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    @Override
    public void firstSpawn()
    {
        super.firstSpawn();
        _spawnX = getX();
        _spawnY = getY();
        _spawnZ = getZ();
        if (Config.ALLOW_PET_WALKERS)
            ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new RandomWalkTask(), 2000, 4000);
    }

    public class RandomWalkTask implements Runnable
    {
        @Override
		public void run()
        {
            if (!isInActiveRegion())
                return; // but rather the AI should be turned off completely

            int randomX = _spawnX + Rnd.get(-1, 1) * 50;
            int randomY = _spawnY + Rnd.get(-1, 1) * 50;

            if (randomX != getX() || randomY != getY())
                getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(randomX, randomY, _spawnZ, 0));
        }
    }
}
