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
package net.l2emuproject.gameserver.model.restriction.global;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.Announcements;
import net.l2emuproject.gameserver.manager.AntiFeedManager;
import net.l2emuproject.gameserver.manager.CastleManager;
import net.l2emuproject.gameserver.manager.games.KrateiCube;
import net.l2emuproject.gameserver.manager.leaderboards.ArenaManager;
import net.l2emuproject.gameserver.model.clan.L2Clan;
import net.l2emuproject.gameserver.model.entity.Castle;
import net.l2emuproject.gameserver.model.entity.events.TvT;
import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.zone.L2Zone;
import net.l2emuproject.tools.random.Rnd;

/**
 * @author lord_rex
 */
public final class CustomRestriction extends AbstractRestriction
{
	@Override
	public final void playerLoggedIn(L2Player activeChar)
	{
		activeChar.getPlayerCustom().restoreCustomStatus();

		if (activeChar.isDonator() && !activeChar.isGM())
		{
			activeChar.sendMessage("Welcome Back: " + activeChar.getName() + " to our " + Config.SERVER_NAME + " Server!");
			activeChar.sendMessage("Please, Enjoy your Stay !");
		}

		if (Config.ANNOUNCE_CASTLE_LORDS_LOGIN)
			notifyCastleOwner(activeChar);

		activeChar.getAppearance().updateNameTitleColor();
	}

	@Override
	public final void playerKilled(L2Character activeChar, L2Player target, L2Player killer)
	{
		if (Config.ALLOW_QUAKE_SYSTEM && !TvT.isPlaying(killer) && !KrateiCube.isPlaying(killer))
		{
			if (killer != null)
				killer.getPlayerEventData().givePoints(1);

			for (L2Player player : L2World.getInstance().getAllPlayers())
				if (player != null && killer != null)
					player.sendCreatureMessage(SystemChatChannelId.Chat_Critical_Announce, "", getRandomMessage() + " " + killer.getName() + " "
							+ getMessage(killer.getPlayerEventData().getPoints()));
		}

		if (killer != null && target != null && AntiFeedManager.getInstance().check(killer, target) && Config.ALLOW_PVP_REWARD)
			killer.dropItem(Config.PVP_REWARD_ITEM_ID, Config.PVP_REWARD_ITEM_AMOUNT);

		if (target.isInsideZone(L2Zone.FLAG_PVP) && !target.isInSiege() && Config.ARENA_ENABLED)
		{
			ArenaManager.getInstance().onKill(killer.getObjectId(), killer.getName());
			ArenaManager.getInstance().onDeath(target.getObjectId(), target.getName());
		}
	}

	private final String getRandomMessage()
	{
		String message = "No random message."; // Never happens.

		switch (Rnd.get(1, 3))
		{
			case 1:
				message = "Holy Shit!";
				break;
			case 2:
				message = "Holy Mother Goos!!";
				break;
			case 3:
				message = "GODLIKE!!!";
				break;
		}

		return message;
	}

	private final String getMessage(int kills)
	{
		String message = "is fantastic!"; // If kills are more than 10.

		switch (kills)
		{
			case 1:
				message = "is Dominating!";
				break;
			case 2:
				message = "is on a Rampage!";
				break;
			case 3:
				message = "is on a Killing Spree!";
				break;
			case 4:
				message = "is on a Monster Kill!";
				break;
			case 5:
				message = "is Unstoppable!";
				break;
			case 6:
				message = "is on an Ultra Kill!";
				break;
			case 7:
				message = "is Godlike";
				break;
			case 8:
				message = "is Wicked Sick!";
				break;
			case 9:
				message = "is on a Ludricrous Kill!";
				break;
			case 10:
				message = "is on a Holy Shit!";
				break;
		}

		return message;
	}

	private final void notifyCastleOwner(L2Player activeChar)
	{
		L2Clan clan = activeChar.getClan();

		if (clan != null)
		{
			if (clan.getHasCastle() > 0)
			{
				Castle castle = CastleManager.getInstance().getCastleById(clan.getHasCastle());

				if ((castle != null) && (activeChar.getObjectId() == clan.getLeaderId()))
					Announcements.getInstance().announceToAll(
							"Castle Lord " + activeChar.getName() + " Of " + castle.getName() + " Castle Is Currently Online.");
			}
		}
	}
}
