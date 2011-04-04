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
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Playable;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Visor123
 */
public class L2LevelChangerInstance extends L2NpcInstance
{
	public L2LevelChangerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
			pom = String.valueOf(npcId);
		else
			pom = npcId + "-" + val;

		return "data/html/mods/level_changer/" + pom + ".htm";
	}

	/**
	 * For Decrease Character level
	 */
	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		// For Decrease Character level
		if (command.startsWith("CustomDecreaseCharLevel"))
			changeCharLevel(player, 1, false);
		else if (command.startsWith("CustomIncreaseCharLevel"))
			changeCharLevel(player, 1, true);
	}

	/**
	 * Custom Change (decrease/increase) character level
	 */
	private boolean changeCharLevel(L2Player player, int deltaLevel, boolean increase)
	{
		if (!Config.ALLOW_NPC_CHANGELEVEL)
		{
			player.sendMessage("Level changer is disabled.");
			return false;
		}

		int curLevel = player.getLevel();
		int resLevel = 0;

		if (increase)
			resLevel = curLevel + deltaLevel;
		else
			resLevel = curLevel - deltaLevel;

		long xpcur = 0;
		long xpres = 0;

		L2Playable target;

		int price = 0;

		if (curLevel > 0 && curLevel < 20)
			price = Config.DECREASE_PRICE0 * deltaLevel;

		else if (curLevel >= 20 && curLevel < 40)
			price = Config.DECREASE_PRICE1 * deltaLevel;

		else if (curLevel >= 40 && curLevel < 76)
			price = Config.DECREASE_PRICE2 * deltaLevel;

		else
			price = Config.DECREASE_PRICE3 * deltaLevel;

		if (player.getRace().toString() == "dwarf")
			price = Math.round(price / Config.COEFF_DIVISIONPRICE_R4);

		if (player.getInventory().getAdena() < price)
		{
			player.sendMessage("You do not have enough Adena. You need " + price + " Adena.");
			return false;
		}

		if ((curLevel < 20 && resLevel > 0) || (curLevel < 40 && resLevel > 20) || (curLevel < 76 && resLevel > 40) || (curLevel > 76 && resLevel > 76))
		{
			target = player;
			try
			{
				xpcur = target.getStat().getExp();
				xpres = target.getStat().getExpForLevel(resLevel);

				if (xpcur > xpres)
					target.getStat().removeExp(xpcur - xpres);
				else
					target.getStat().addExp(xpres - xpcur);

				player.reduceAdena("BuyChangeLevel", price, player.getLastFolkNPC(), true);
				player.sendMessage("You change self level to " + resLevel);
				return true;
			}
			catch (Exception e)
			{
				_log.error("CharacterService: Error, ", e);
				player.sendMessage("Incorrect action.");
				return false;
			}
		}
		else
			player.sendMessage("Incorrect level amount. You do not change level for another grade");

		return false;
	}
}
