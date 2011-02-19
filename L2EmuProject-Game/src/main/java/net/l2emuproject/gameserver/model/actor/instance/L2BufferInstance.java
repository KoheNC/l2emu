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

import java.util.Map;

import javolution.util.FastMap;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.model.skill.L2Skill;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.MagicSkillUse;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author lord_rex & NB4L1
 */
public class L2BufferInstance extends L2NpcInstance
{
	private static final String			BUFFER_HTML_FOLDER	= "data/html/mods/buffer/";

	private final Map<Integer, Integer>	_bypassIndex		= new FastMap<Integer, Integer>().shared();

	private static final int			PROPHET				= 1;
	private static final int			DANCES				= 2;
	private static final int			SONGS				= 3;
	private static final int			HERO				= 4;
	private static final int			NOBLE				= 5;
	private static final int			ORC					= 6;
	private static final int			CUBICS				= 7;
	private static final int			SUMMONS				= 8;
	private static final int			OTHER				= 9;

	private static final int			MAIN				= 10;
	private static final int			DISABLED			= 11;

	public L2BufferInstance(int objectId, L2NpcTemplate template)
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

		return BUFFER_HTML_FOLDER + pom + ".htm";
	}

	@Override
	public final void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.equalsIgnoreCase("Donator"))
		{
			if (Config.ALLOW_DONATOR_BUFFER)
			{
				if (player.isDonator())
				{
					showBufferWindow(player, MAIN);
				}
				else
					showBufferWindow(player, DISABLED);
			}
			else
				showBufferWindow(player, DISABLED);
		}
		else if (command.equalsIgnoreCase("NonDonator"))
		{
			if (Config.ALLOW_NORMAL_BUFFER)
			{
				showBufferWindow(player, MAIN);
			}
			else
				showBufferWindow(player, DISABLED);
		}
		else if (command.equalsIgnoreCase("Prophet"))
		{
			if (Config.SELL_PROPHET_BUFFS)
			{
				showBufferWindow(player, PROPHET);
			}
			else
				showBufferWindow(player, DISABLED);
		}
		else if (command.equalsIgnoreCase("Dances"))
		{
			if (Config.SELL_DANCES)
			{
				showBufferWindow(player, DANCES);
			}
			else
				showBufferWindow(player, DISABLED);
		}
		else if (command.equalsIgnoreCase("Songs"))
		{
			if (Config.SELL_SONGS)
			{
				showBufferWindow(player, SONGS);
			}
			else
				showBufferWindow(player, DISABLED);
		}
		else if (command.equalsIgnoreCase("Hero"))
		{
			if (Config.SELL_HERO_BUFFS)
			{
				showBufferWindow(player, HERO);
			}
			else
				showBufferWindow(player, DISABLED);
		}
		else if (command.equalsIgnoreCase("Noble"))
		{
			if (Config.SELL_NOBLE_BUFFS)
			{
				showBufferWindow(player, NOBLE);
			}
			else
				showBufferWindow(player, DISABLED);
		}
		else if (command.equalsIgnoreCase("Orc"))
		{
			if (Config.SELL_ORC_BUFFS)
			{
				showBufferWindow(player, ORC);
			}
			else
				showBufferWindow(player, DISABLED);
		}
		else if (command.equalsIgnoreCase("Cubics"))
		{
			if (Config.SELL_CUBICS_BUFFS)
			{
				showBufferWindow(player, CUBICS);
			}
			else
				showBufferWindow(player, DISABLED);
		}
		else if (command.equalsIgnoreCase("Summons"))
		{
			if (Config.SELL_SUMMON_BUFFS)
			{
				showBufferWindow(player, SUMMONS);
			}
			else
				showBufferWindow(player, DISABLED);
		}
		else if (command.equalsIgnoreCase("Other"))
		{
			if (Config.SELL_OTHER_BUFFS)
			{
				showBufferWindow(player, OTHER);
			}
			else
				showBufferWindow(player, DISABLED);
		}
		else if (command.equalsIgnoreCase("Heal"))
		{
			player.getStatus().setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			showBufferWindow(player, _bypassIndex.get(player.getObjectId()));
		}
		else if (command.equalsIgnoreCase("Cancel"))
		{
			player.stopAllEffects();
			showBufferWindow(player, _bypassIndex.get(player.getObjectId()));
		}
		else if (command.equalsIgnoreCase("Back"))
		{
			showBufferWindow(player, MAIN);
		}
		else if (command.startsWith("Buff"))
		{
			String[] cmd = command.substring(5).split(" ");

			int skillId = Integer.parseInt(cmd[0]);
			int skillLevel = Integer.parseInt(cmd[1]);

			try
			{
				makeBuff(player, skillId, skillLevel);
			}
			catch (Exception e)
			{
				_log.error("L2BufferInstance: Unable to Make Magic", e);
			}

			showBufferWindow(player, _bypassIndex.get(player.getObjectId()));
		}
		else
		{
			super.onBypassFeedback(player, command);
			return;
		}
	}

	private void makeBuff(L2PcInstance player, int skillId, int skillLevel)
	{
		int priceIndex = 0;

		if (SkillTable.isProphetBuff(skillId))
			priceIndex = Config.PRICE_PROPHET;
		else if (SkillTable.isDance(skillId))
			priceIndex = Config.PRICE_DANCE;
		else if (SkillTable.isSong(skillId))
			priceIndex = Config.PRICE_SONG;
		else if (SkillTable.isCubic(skillId))
			priceIndex = Config.PRICE_CUBIC;
		else if (SkillTable.isHeroBuff(skillId))
			priceIndex = Config.PRICE_HERO;
		else if (SkillTable.isNobleBuff(skillId))
			priceIndex = Config.PRICE_NOBLE;
		else if (SkillTable.isSummonBuff(skillId))
			priceIndex = Config.PRICE_SUMMON;
		else if (SkillTable.isOrcBuff(skillId))
			priceIndex = Config.PRICE_ORC;
		else if (SkillTable.isOtherBuff(skillId))
			priceIndex = Config.PRICE_OTHER;

		checkPrice(player, priceIndex);
		checkPlayer(player);
		checkCw(player);
		checkLevel(player);
		// TODO: Later add this ... checkEvent(player);

		player.reduceAdena("Buffer", priceIndex, player.getLastFolkNPC(), true);

		L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
		if (skill != null)
			skill.getEffects(player, player);

		if (Config.ANIMATION)
			broadcastPacket(new MagicSkillUse(this, player, skillId, skillLevel, 100, 0));
	}

	private void showBufferWindow(L2PcInstance player, int bypassIndex)
	{
		String filename = "";

		switch (bypassIndex)
		{
			case PROPHET:
				filename = BUFFER_HTML_FOLDER + "prophet.htm";
				break;
			case DANCES:
				filename = BUFFER_HTML_FOLDER + "dances.htm";
				break;
			case SONGS:
				filename = BUFFER_HTML_FOLDER + "songs.htm";
				break;
			case HERO:
				filename = BUFFER_HTML_FOLDER + "hero.htm";
				break;
			case NOBLE:
				filename = BUFFER_HTML_FOLDER + "noble.htm";
				break;
			case ORC:
				filename = BUFFER_HTML_FOLDER + "orc.htm";
				break;
			case CUBICS:
				filename = BUFFER_HTML_FOLDER + "cubics.htm";
				break;
			case SUMMONS:
				filename = BUFFER_HTML_FOLDER + "summons.htm";
				break;
			case OTHER:
				filename = BUFFER_HTML_FOLDER + "other.htm";
				break;
			case MAIN:
				filename = BUFFER_HTML_FOLDER + "main.htm";
				break;
			case DISABLED:
				filename = BUFFER_HTML_FOLDER + "disabled.htm";
				break;
			default:
				filename = BUFFER_HTML_FOLDER + "main.htm";
				break;
		}

		_bypassIndex.put(player.getObjectId(), bypassIndex);
		showChatWindow(player, filename);
	}

	private void checkPrice(L2PcInstance player, int priceIndex)
	{
		if (player.getInventory().getAdena() < priceIndex)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			return;
		}
	}

	private void checkPlayer(L2PcInstance player)
	{
		if (player.isDead() || player.isAlikeDead())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
			return;
		}
	}

	private void checkCw(L2PcInstance player)
	{
		// Prevent a cursed weapon wielder of being buffed
		if (Config.ALLOW_KARMA_PLAYER && player.isCursedWeaponEquipped() || player.isChaotic())
		{
			showBufferWindow(player, DISABLED);
			return;
		}
	}

	private void checkLevel(L2PcInstance player)
	{
		if (player.getLevel() < Config.MIN_PC_LVL_FOR_BUFFS || (player.getLevel() > Config.MAX_PC_LVL_FOR_BUFFS))
		{
			showBufferWindow(player, DISABLED);
			return;
		}
	}
}
