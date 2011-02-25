/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.l2emuproject.gameserver.model.actor.instance;

import java.util.Calendar;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.Announcements;
import net.l2emuproject.gameserver.ai.CtrlIntention;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * An Special Custom Instance to Make an Announcer Npc for Players :)
 *
 *
 * @author Rayan
 * @project L2Emu Project
 * @since 2313
 */
public class L2AnnouncerInstance extends L2NpcInstance
{
	public L2AnnouncerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";

		if (val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;

		return "data/html/mods/announcer/" + pom + ".htm";
	}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		// Get the distance between the L2Player and the L2NpcAnnouncerInstance
		if (!isInsideRadius(player, INTERACTION_DISTANCE, false, false))
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
		}
		if (command.equalsIgnoreCase("request_announce"))
		{
			int playerlevel = player.getLevel();
			//prevents player with lower level than config to announce
			if (playerlevel < Config.MIN_LVL_TO_ANNOUNCE)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());//will add proper code soon was too lazy for atm :P
				String filename = "data/html/mods/announcer/min_lvl.htm";
				html.setFile(filename);
				if (filename != null)
				{
					html.replace("%min_lvl_player%", String.valueOf(Config.MIN_LVL_TO_ANNOUNCE));
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%npcname%", String.valueOf(getName()));
					player.sendPacket(html);
					return;
				}
			}

			//prevents player with higher level than config to announce
			if (playerlevel > Config.MAX_LVL_TO_ANNOUNCE)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());//will add proper code soon was too lazy for atm :P
				String filename = "data/html/mods/announcer/max_lvl.htm";
				html.setFile(filename);
				if (filename != null)
				{
					html.replace("%max_lvl_player%", String.valueOf(Config.MAX_LVL_TO_ANNOUNCE));
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%npcname%", String.valueOf(getName()));
					player.sendPacket(html);
					return;
				}
			}

			//checks if donator mode is active
			if (Config.ANNOUNCER_DONATOR_ONLY)
			{
				if (!player.isDonator() && !player.isGM())
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());//will add proper code soon was too lazy for atm :P
					String filename = "data/html/mods/announcer/donator_only.htm";
					html.setFile(filename);
					if (filename != null)
					{
						html.replace("%objectId%", String.valueOf(getObjectId()));
						html.replace("%npcname%", String.valueOf(getName()));
						player.sendPacket(html);
						return;
					}
				}
			}
			//if player has already announce the count of max announces per day
			if (player.getPlayerCustom().getAnnounceCount() == Config.MAX_ANNOUNCES_PER_DAY)
			{
				if (player.getPlayerCustom().getLastAnnounceDate() == Calendar.DAY_OF_WEEK)//player is in the same day
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());//will add proper code soon was too lazy for atm :P
					String filename = "data/html/mods/announcer/max_announce.htm";
					html.setFile(filename);
					if (filename != null)
					{
						html.replace("%max_announces_per_day%", String.valueOf(Config.MAX_ANNOUNCES_PER_DAY));
						html.replace("%objectId%", String.valueOf(getObjectId()));
						html.replace("%npcname%", String.valueOf(getName()));
						player.sendPacket(html);
						return;
					}
				}
			}
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());//will add proper code soon was too lazy for atm :P
			String filename = "data/html/mods/announcer/announce.htm";
			html.setFile(filename);
			if (filename != null)
			{
				html.replace("%price_per_announce%", String.valueOf(Config.PRICE_PER_ANNOUNCE));
				html.replace("%max_announce_per_day%", String.valueOf(Config.MAX_ANNOUNCES_PER_DAY));
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", String.valueOf(getName()));
				player.sendPacket(html);
			}
		}
		else if (command.equalsIgnoreCase("main_window"))
		{
			player.showHTMLFile("data/html/mods/announcer/" + getNpcId() + ".htm");
		}
		else if (command.startsWith("make_announce") && Config.ALLOW_ANNOUNCER)
		{

			String playerName = player.getName();
			String msg = command.substring(14).toLowerCase();

			//checks adena
			if (!player.reduceAdena("NpcAnnouncer: announce", Config.PRICE_PER_ANNOUNCE, player, true) && !player.isGM())
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());//will add proper code soon was too lazy for atm :P
				String filename = "data/html/mods/announcer/missing_adena.htm";
				html.setFile(filename);
				if (filename != null)
				{
					html.replace("%price_per_announce%", String.valueOf(Config.PRICE_PER_ANNOUNCE));
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%npcname%", String.valueOf(getName()));
					player.sendPacket(html);
					return;
				}
			}
			Announcements.getInstance().announceToAll(playerName + ": " + msg);
			player.getPlayerCustom().increaseAnnounces();
			player.getPlayerCustom().setLastAnnounceDate();
			//TODO: player.setDelayForNextAnnounce();
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId()); //will add proper code soon was too lazy for atm :P
			String filename = "data/html/mods/announcer/announce_complete.htm";
			html.setFile(filename);
			if (filename != null)
			{
				html.replace("%remaining_announces%", String.valueOf(player.getPlayerCustom().getRemainingAnnounces()));
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", String.valueOf(getName()));
				player.sendPacket(html);
			}
		}
		super.onBypassFeedback(player, command);
	}
}