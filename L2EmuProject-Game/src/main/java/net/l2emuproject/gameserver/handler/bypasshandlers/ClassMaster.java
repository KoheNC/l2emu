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
package net.l2emuproject.gameserver.handler.bypasshandlers;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.ThreadPoolManager;
import net.l2emuproject.gameserver.datatables.NpcTable;
import net.l2emuproject.gameserver.datatables.PetDataTable;
import net.l2emuproject.gameserver.datatables.SpawnTable;
import net.l2emuproject.gameserver.handler.IBypassHandler;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.serverpackets.ItemList;
import net.l2emuproject.gameserver.network.serverpackets.MagicSkillUse;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Summon;
import net.l2emuproject.gameserver.world.object.L2Npc.DestroyTemporalNPC;
import net.l2emuproject.gameserver.world.object.L2Npc.DestroyTemporalSummon;
import net.l2emuproject.gameserver.world.spawn.L2Spawn;
import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.tools.random.Rnd;

public class ClassMaster implements IBypassHandler
{
	private static final String[]	COMMANDS	=
												{ "upgrade" };

	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!(target instanceof L2Npc))
			return false;

		try
		{
			if (Config.ALLOW_WYVERN_UPGRADER && command.startsWith(COMMANDS[0]) && activeChar.getClan() != null && activeChar.getClan().getHasCastle() != 0)
			{
				String type = command.substring(8);

				if (type.equalsIgnoreCase("wyvern"))
				{
					L2NpcTemplate wind = NpcTable.getInstance().getTemplate(PetDataTable.STRIDER_WIND_ID);
					L2NpcTemplate star = NpcTable.getInstance().getTemplate(PetDataTable.STRIDER_STAR_ID);
					L2NpcTemplate twilight = NpcTable.getInstance().getTemplate(PetDataTable.STRIDER_TWILIGHT_ID);

					L2Summon summon = activeChar.getPet();
					L2NpcTemplate myPet = summon.getTemplate();

					if ((myPet.equals(wind) || myPet.equals(star) || myPet.equals(twilight)) && activeChar.getAdena() >= 20000000
							&& (activeChar.getInventory().getItemByObjectId(summon.getControlItemId()) != null))
					{
						int exchangeItem = PetDataTable.WYVERN_ID;
						if (!activeChar.reduceAdena("PetUpdate", 20000000, target, true))
							return false;
						activeChar.getInventory().destroyItem("PetUpdate", summon.getControlItemId(), 1, activeChar, target);

						L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(20629);
						try
						{
							L2Spawn spawn = new L2Spawn(template1);

							spawn.setLocx(target.getX() + 20);
							spawn.setLocy(target.getY() + 20);
							spawn.setLocz(target.getZ());
							spawn.setAmount(1);
							spawn.setHeading(activeChar.getHeading());
							spawn.setRespawnDelay(1);

							SpawnTable.getInstance().addNewSpawn(spawn, false);

							spawn.init();
							spawn.getLastSpawn().getStatus().setCurrentHp(target.getMaxHp());
							spawn.getLastSpawn().setName("baal");
							spawn.getLastSpawn().setTitle("hell's god");
							//spawn.getLastSpawn().isEventMob = true;
							spawn.getLastSpawn().isAggressive();
							spawn.getLastSpawn().decayMe();
							spawn.getLastSpawn().spawnMe(spawn.getLastSpawn().getX(), spawn.getLastSpawn().getY(), spawn.getLastSpawn().getZ());

							int level = summon.getLevel();
							int chance = (level - 54) * 10;
							spawn.getLastSpawn().broadcastPacket(new MagicSkillUse(spawn.getLastSpawn(), spawn.getLastSpawn(), 1034, 1, 1, 1));
							spawn.getLastSpawn().broadcastPacket(new MagicSkillUse(spawn.getLastSpawn(), summon, 1034, 1, 1, 1));

							if (Rnd.nextInt(100) < chance)
							{
								ThreadPoolManager.getInstance().scheduleGeneral(new DestroyTemporalSummon(summon, activeChar), 6000);
								activeChar.addItem("PetUpdate", exchangeItem, 1, activeChar, true, true);

								NpcHtmlMessage adminReply = new NpcHtmlMessage(target.getObjectId());
								L2TextBuilder replyMSG = L2TextBuilder.newInstance("<html><body>");
								replyMSG.append("Congratulations, the evolution suceeded.");
								replyMSG.append("</body></html>");
								adminReply.setHtml(replyMSG.moveToString());
								activeChar.sendPacket(adminReply);
							}
							else
							{
								summon.reduceCurrentHp(summon.getStatus().getCurrentHp(), activeChar);
							}
							ThreadPoolManager.getInstance().scheduleGeneral(new DestroyTemporalNPC(spawn), 15000);

							ItemList il = new ItemList(activeChar, true);
							activeChar.sendPacket(il);
						}
						catch (Exception e)
						{
							_log.error(e.getMessage(), e);
						}
					}
					else
					{
						NpcHtmlMessage adminReply = new NpcHtmlMessage(target.getObjectId());
						L2TextBuilder replyMSG = L2TextBuilder.newInstance("<html><body>");

						replyMSG.append("You will need 20.000.000 and have the pet summoned for the ceremony ...");
						replyMSG.append("</body></html>");

						adminReply.setHtml(replyMSG.moveToString());
						activeChar.sendPacket(adminReply);
					}
				}
				else if (Config.ALT_CLASS_MASTER_STRIDER_UPDATE && type.equalsIgnoreCase("strider"))
				{
					L2NpcTemplate wind = NpcTable.getInstance().getTemplate(PetDataTable.HATCHLING_WIND_ID);
					L2NpcTemplate star = NpcTable.getInstance().getTemplate(PetDataTable.HATCHLING_STAR_ID);
					L2NpcTemplate twilight = NpcTable.getInstance().getTemplate(PetDataTable.HATCHLING_TWILIGHT_ID);

					L2Summon summon = activeChar.getPet();
					L2NpcTemplate myPet = summon.getTemplate();

					if ((myPet.equals(wind) || myPet.equals(star) || myPet.equals(twilight)) && activeChar.getAdena() >= 6000000
							&& (activeChar.getInventory().getItemByObjectId(summon.getControlItemId()) != null))
					{
						int exchangeItem = PetDataTable.STRIDER_TWILIGHT_ID;
						if (myPet.equals(wind))
							exchangeItem = PetDataTable.STRIDER_WIND_ID;
						else if (myPet.equals(star))
							exchangeItem = PetDataTable.STRIDER_STAR_ID;

						if (!activeChar.reduceAdena("PetUpdate", 6000000, target, true))
							return false;
						activeChar.getInventory().destroyItem("PetUpdate", summon.getControlItemId(), 1, activeChar, target);

						L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(689);
						try
						{
							L2Spawn spawn = new L2Spawn(template1);

							spawn.setLocx(target.getX() + 20);
							spawn.setLocy(target.getY() + 20);
							spawn.setLocz(target.getZ());
							spawn.setAmount(1);
							spawn.setHeading(activeChar.getHeading());
							spawn.setRespawnDelay(1);

							SpawnTable.getInstance().addNewSpawn(spawn, false);

							spawn.init();
							spawn.getLastSpawn().getStatus().setCurrentHp(target.getMaxHp());
							spawn.getLastSpawn().setName("mercebu");
							spawn.getLastSpawn().setTitle("baal's son");

							spawn.getLastSpawn().isAggressive();
							spawn.getLastSpawn().decayMe();
							spawn.getLastSpawn().spawnMe(spawn.getLastSpawn().getX(), spawn.getLastSpawn().getY(), spawn.getLastSpawn().getZ());

							spawn.getLastSpawn().broadcastPacket(new MagicSkillUse(spawn.getLastSpawn(), summon, 297, 1, 1, 1));

							int level = summon.getLevel();
							int chance = (level - 34) * 10;
							spawn.getLastSpawn().broadcastPacket(new MagicSkillUse(spawn.getLastSpawn(), spawn.getLastSpawn(), 1034, 1, 1, 1));
							spawn.getLastSpawn().broadcastPacket(new MagicSkillUse(spawn.getLastSpawn(), summon, 1034, 1, 1, 1));

							if (Rnd.nextInt(100) < chance)
							{
								ThreadPoolManager.getInstance().scheduleGeneral(new DestroyTemporalSummon(summon, activeChar), 6000);
								activeChar.addItem("PetUpdate", exchangeItem, 1, activeChar, true, true);
								NpcHtmlMessage adminReply = new NpcHtmlMessage(target.getObjectId());
								L2TextBuilder replyMSG = L2TextBuilder.newInstance("<html><body>");

								replyMSG.append("Congratulations, the evolution suceeded.");
								replyMSG.append("</body></html>");

								adminReply.setHtml(replyMSG.moveToString());
								activeChar.sendPacket(adminReply);
							}
							else
							{
								summon.reduceCurrentHp(summon.getStatus().getCurrentHp(), activeChar);
							}

							ThreadPoolManager.getInstance().scheduleGeneral(new DestroyTemporalNPC(spawn), 15000);
							ItemList il = new ItemList(activeChar, true);
							activeChar.sendPacket(il);
						}
						catch (Exception e)
						{
							_log.error(e.getMessage(), e);
						}
					}
					else
					{
						NpcHtmlMessage adminReply = new NpcHtmlMessage(target.getObjectId());
						L2TextBuilder replyMSG = L2TextBuilder.newInstance("<html><body>");

						replyMSG.append("You will need 6.000.000 and have the pet summoned for the ceremony ...");
						replyMSG.append("</body></html>");

						adminReply.setHtml(replyMSG.moveToString());
						activeChar.sendPacket(adminReply);
					}
				}
			}
			return true;
		}
		catch (Exception e)
		{
			_log.warn("Exception in " + getClass().getSimpleName());
		}
		return false;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
