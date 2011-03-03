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
package net.l2emuproject.gameserver.world.object.instance;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.network.serverpackets.MagicSkillUse;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.services.couple.Couple;
import net.l2emuproject.gameserver.services.couple.CoupleService;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.system.announcements.Announcements;
import net.l2emuproject.gameserver.system.threadmanager.ThreadPoolManager;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author evill33t & squeezed
 */
public final class L2WeddingManagerInstance extends L2Npc
{
	public L2WeddingManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
    public final void showChatWindow(L2Player player)
    {
        String filename = "data/html/wedding/start.htm";
        String replace = "";
        
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%replace%", replace);
        html.replace("%npcname%", getName());
        player.sendPacket(html);
    }
    
    @Override
    public final synchronized void onBypassFeedback(final L2Player player, String command)
    {
        // Standard msg
        String filename = "data/html/wedding/start.htm";
        String replace = "";
        
        // If player has no partner
        if(player.getPartnerId() == 0)
        {
            filename = "data/html/wedding/nopartner.htm";
            sendHtmlMessage(player, filename, replace);
            return;
        }

        L2Object obj = L2World.getInstance().findObject(player.getPartnerId());
        final L2Player ptarget = obj instanceof L2Player ? (L2Player) obj : null;
        // Partner online ?
        if(ptarget == null || ptarget.isOnline() == 0)
        {
            filename = "data/html/wedding/notfound.htm";
            sendHtmlMessage(player, filename, replace);
            return;
        }

        // Already married ?
        if(player.isMaried())
        {
            filename = "data/html/wedding/already.htm";
            sendHtmlMessage(player, filename, replace);
            return;
        }
        else if (player.isMaryAccepted())
        {
            filename = "data/html/wedding/waitforpartner.htm";
            sendHtmlMessage(player, filename, replace);
            return;
        }
        else if (command.startsWith("AcceptWedding"))
        {
        	if (Config.WEDDING_FORMALWEAR && (!player.isWearingFormalWear() || !ptarget.isWearingFormalWear()))
        	{
                player.setMaryRequest(false);
                player.setMaryAccepted(false);
                ptarget.setMaryRequest(false);
                ptarget.setMaryAccepted(false);

        		player.sendMessage("A shame, you tried to foul us by exchanging your wedding dress!");
        		ptarget.sendMessage("A shame, you tried to foul us by exchanging your wedding dress!");

				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName()
						+ " tried to marry without wedding dress.", Config.DEFAULT_PUNISH);
				Util.handleIllegalPlayerAction(ptarget, "Warning!! Character " + ptarget.getName() + " of account " + ptarget.getAccountName()
						+ " tried to marry without wedding dress.", Config.DEFAULT_PUNISH);
        		return;
        	}

            // Accept the wedding request
            player.setMaryAccepted(true);
            Couple couple = CoupleService.getInstance().getCouple(player.getCoupleId());
            couple.marry();

            // Messages to the couple
            player.sendMessage("Congratulations, you are married!");
            player.setMaried(true);
            player.setMaryRequest(false);
            ptarget.sendMessage("Congratulations, you are married!");
            ptarget.setMaried(true);
            ptarget.setMaryRequest(false);
            
            if(Config.WEDDING_GIVE_CUPID_BOW)
            {
            	// give cupid's bows to couple's
            	player.addItem("Cupids Bow", 9140, 1, player, true, true); // give cupids bow
            	
            	// No need to update every item in the inventory
            	//player.getInventory().updateDatabase(); // update database
            	
            	ptarget.addItem("Cupids Bow", 9140, 1, ptarget, true, true); // give cupids bow
            	
            	// No need to update every item in the inventory
            	//ptarget.getInventory().updateDatabase(); // update database
            	
                // Refresh client side skill lists
                //player.sendSkillList();
                //ptarget.sendSkillList();
            }

            // Wedding march
            MagicSkillUse MSU = new MagicSkillUse(player, player, 2230, 1, 1, 0);
            player.broadcastPacket(MSU);
            MSU = new MagicSkillUse(ptarget, ptarget, 2230, 1, 1, 0);
            ptarget.broadcastPacket(MSU);
            
            // Fireworks
            L2Skill skill = SkillTable.getInstance().getInfo(5966,1);
            if (skill != null)
            {
                MSU = new MagicSkillUse(player, player, 5966, 1, 1, 0);
                player.sendPacket(MSU);
                player.broadcastPacket(MSU);
                player.useMagic(skill, false, false);

                MSU = new MagicSkillUse(ptarget, ptarget, 5966, 1, 1, 0);
                ptarget.sendPacket(MSU);
                ptarget.broadcastPacket(MSU);
                ptarget.useMagic(skill, false, false);
            }
            
            Announcements.getInstance().announceToAll("Congratulations, "+player.getName()+" and "+ptarget.getName()+" have married!");
            
            MSU = null;
            
            filename = "data/html/wedding/accepted.htm";
            replace = ptarget.getName();
            sendHtmlMessage(ptarget, filename, replace);
			
			if (Config.WEDDING_HONEYMOON_PORT)
			{
				// Wait a little for all effects, and then go on honeymoon
				ThreadPoolManager.getInstance().schedule(new Runnable() {
					@Override
					public void run()
					{
						// Port both players to Fantasy Isle for happy time
						player.teleToLocation(-56641, -56345, -2005);
						ptarget.teleToLocation(-56641, -56345, -2005);
					}
				}, 10000);
			}
			return;
		}
        else if (command.startsWith("DeclineWedding"))
        {
            player.setMaryRequest(false);
            ptarget.setMaryRequest(false);
            player.setMaryAccepted(false);
            ptarget.setMaryAccepted(false);
            player.sendMessage("You declined");
            ptarget.sendMessage("Your partner declined");
            replace = ptarget.getName();
            filename = "data/html/wedding/declined.htm";
            sendHtmlMessage(ptarget, filename, replace);
            return;
        }
        else if (player.isMary())
        {
            // Check for formalwear
            if(Config.WEDDING_FORMALWEAR && !player.isWearingFormalWear())
            {
                filename = "data/html/wedding/noformal.htm";
                sendHtmlMessage(player, filename, replace);
                return;
            }
            filename = "data/html/wedding/ask.htm";
            player.setMaryRequest(false);
            ptarget.setMaryRequest(false);
            replace = ptarget.getName();
            sendHtmlMessage(player, filename, replace);
            return;
        }
        else if (command.startsWith("AskWedding"))
        {
            // Check for formalwear
            if(Config.WEDDING_FORMALWEAR && !player.isWearingFormalWear())
            {
                filename = "data/html/wedding/noformal.htm";
                sendHtmlMessage(player, filename, replace);
                return;
            }
            else if(player.getAdena()<Config.WEDDING_PRICE)
            {
                filename = "data/html/wedding/adena.htm";
                replace = String.valueOf(Config.WEDDING_PRICE);
                sendHtmlMessage(player, filename, replace);
                return;
            }
            else
            {
                player.setMaryAccepted(true);
                ptarget.setMaryRequest(true);
                replace = ptarget.getName();
                filename = "data/html/wedding/requested.htm";
                player.getInventory().reduceAdena("Wedding", Config.WEDDING_PRICE, player, player.getLastFolkNPC());
                sendHtmlMessage(player, filename, replace);
                return;
            }
        }

        sendHtmlMessage(player, filename, replace);
    }

    private final void sendHtmlMessage(L2Player player, String filename, String replace)
    {
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%replace%", replace);
        html.replace("%npcname%", getName());
        player.sendPacket(html);
    }
}
