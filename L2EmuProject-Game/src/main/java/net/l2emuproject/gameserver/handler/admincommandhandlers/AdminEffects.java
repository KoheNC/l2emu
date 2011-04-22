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
package net.l2emuproject.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import net.l2emuproject.gameserver.handler.IAdminCommandHandler;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.Earthquake;
import net.l2emuproject.gameserver.network.serverpackets.ExRedSky;
import net.l2emuproject.gameserver.network.serverpackets.L2GameServerPacket;
import net.l2emuproject.gameserver.network.serverpackets.MagicSkillUse;
import net.l2emuproject.gameserver.network.serverpackets.PlaySound;
import net.l2emuproject.gameserver.network.serverpackets.SSQInfo;
import net.l2emuproject.gameserver.network.serverpackets.SocialAction;
import net.l2emuproject.gameserver.network.serverpackets.StopMove;
import net.l2emuproject.gameserver.network.serverpackets.SunRise;
import net.l2emuproject.gameserver.network.serverpackets.SunSet;
import net.l2emuproject.gameserver.skills.AbnormalEffect;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2ChestInstance;

/**
 * This class handles following admin commands:
 *   <li> invis/invisible/vis/visible = makes yourself invisible or visible
 *   <li> earthquake = causes an earthquake of a given intensity and duration around you
 *   <li> bighead/shrinkhead = changes head size
 *   <li> gmspeed = temporary Super Haste effect.
 *   <li> para/unpara = paralyze/remove paralysis from target
 *   <li> para_all/unpara_all = same as para/unpara, affects the whole world.
 *   <li> polyself/unpolyself = makes you look as a specified mob.
 *   <li> clearteams/setteam_close/setteam = team related commands
 *   <li> social = forces an L2Character instance to broadcast social action packets.
 *   <li> effect = forces an L2Character instance to broadcast MSU packets.
 *   <li> abnormal = force changes over an L2Character instance's abnormal state.
 *   <li> play_sound/play_sounds = Music broadcasting related commands
 *   <li> atmosphere = sky change related commands.
 */

public class AdminEffects implements IAdminCommandHandler
{
	private static final String[]	ADMIN_COMMANDS	=
													{
			"admin_invis",
			"admin_invisible",
			"admin_vis",
			"admin_visible",
			"admin_invis_menu",
			"admin_earthquake",
			"admin_earthquake_menu",
			"admin_bighead",
			"admin_shrinkhead",
			"admin_frintezza",
			"admin_gmspeed_menu",
			"admin_unpara_all",
			"admin_para_all",
			"admin_unpara",
			"admin_para",
			"admin_unpara_all_menu",
			"admin_para_all_menu",
			"admin_unpara_menu",
			"admin_para_menu",
			"admin_polyself",
			"admin_unpolyself",
			"admin_polyself_menu",
			"admin_unpolyself_menu",
			"admin_clearteams",
			"admin_setteam_close",
			"admin_setteam",
			"admin_social",
			"admin_effect",
			"admin_social_menu",
			"admin_special",
			"admin_special_menu",
			"admin_effect_menu",
			"admin_abnormal",
			"admin_abnormal_menu",
			"admin_play_sounds",
			"admin_play_sound",
			"admin_atmosphere",
			"admin_atmosphere_menu",
			"admin_give_souls",
			"admin_set_displayeffect"
			};

	public boolean useAdminCommand(String command, L2Player activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();

		if (command.equals("admin_invis_menu"))
		{
			if (!activeChar.getAppearance().isInvisible())
			{
				activeChar.getAppearance().setInvisible();
				activeChar.updateInvisibilityStatus();
			}
			else
			{
				activeChar.getAppearance().setVisible();
				activeChar.broadcastUserInfo();
			}
		}
		else if (command.startsWith("admin_invis"))
		{
			activeChar.getAppearance().setInvisible();
			activeChar.updateInvisibilityStatus();
		}
		else if (command.startsWith("admin_vis"))
		{
			activeChar.getAppearance().setVisible();
			activeChar.broadcastUserInfo();
		}
		else if (command.startsWith("admin_earthquake"))
		{
			try
			{
				String val1 = st.nextToken();
				int intensity = Integer.parseInt(val1);
				String val2 = st.nextToken();
				int duration = Integer.parseInt(val2);
				Earthquake eq = new Earthquake(activeChar.getX(), activeChar.getY(), activeChar.getZ(), intensity, duration);
				activeChar.broadcastPacket(eq);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //earthquake <intensity> <duration>");
			}
		}
		else if (command.startsWith("admin_atmosphere"))
		{
			try
			{
				String type = st.nextToken();
				String state = st.nextToken();
				adminAtmosphere(type, state, activeChar);
			}
			catch (Exception ex)
			{
				activeChar.sendMessage("Usage: //atmosphere <signsky dawn|dusk>|<sky day|night|red>");
			}
		}
		else if (command.equals("admin_play_sounds"))
		{
			activeChar.showHTMLFile(AdminHelpPage.ADMIN_HELP_PAGE + "songs/songs.htm");
		}
		else if (command.startsWith("admin_play_sounds"))
		{
			try
			{
				activeChar.showHTMLFile(AdminHelpPage.ADMIN_HELP_PAGE + "songs/songs" + command.substring(18) + ".htm");
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //play_sounds <pagenumber>");
			}
		}
		else if (command.startsWith("admin_play_sound"))
		{
			try
			{
				playAdminSound(activeChar, command.substring(17));
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //play_sound <soundname>");
			}
		}
		else if (command.startsWith("admin_para_all"))
		{
			try
			{
				for (L2Player player : activeChar.getKnownList().getKnownPlayers().values())
				{
					if (!player.isGM())
					{
						player.startAbnormalEffect(AbnormalEffect.HOLD_1);
						player.startParalyze();
						StopMove sm = new StopMove(player);
						player.sendPacket(sm);
						player.broadcastPacket(sm);
					}
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_unpara_all"))
		{
			try
			{
				for (L2Player player : activeChar.getKnownList().getKnownPlayers().values())
				{
					player.stopAbnormalEffect(AbnormalEffect.HOLD_1);
					player.stopParalyze(false);
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_para")) // || command.startsWith("admin_para_menu"))
		{
			String type = "1";
			if (st.hasMoreTokens())
				type = st.nextToken();
			try
			{
				L2Object target = activeChar.getTarget();
				L2Character player = null;
				if (target instanceof L2Character)
				{
					player = (L2Character) target;
					if (type.equals("1"))
						player.startAbnormalEffect(AbnormalEffect.HOLD_1);
					else
						player.startAbnormalEffect(AbnormalEffect.HOLD_2);
					player.startParalyze();
					StopMove sm = new StopMove(player);
					player.broadcastPacket(sm);
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.equals("admin_frintezza"))
		{
		}
		else if (command.equals("admin_unpara") || command.equals("admin_unpara_menu"))
		{
			try
			{
				L2Object target = activeChar.getTarget();
				L2Character player = null;
				if (target instanceof L2Character)
				{
					player = (L2Character)target;
					player.stopAbnormalEffect(AbnormalEffect.HOLD_1);
					player.stopAbnormalEffect(AbnormalEffect.HOLD_2);
					player.stopParalyze(false);
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_bighead"))
		{
			try
			{
				L2Object target = activeChar.getTarget();
				L2Character player = null;
				if (target instanceof L2Character)
				{
					player = (L2Character) target;
					player.startAbnormalEffect(AbnormalEffect.BIG_HEAD);
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_shrinkhead"))
		{
			try
			{
				L2Object target = activeChar.getTarget();
				L2Character player = null;
				if (target instanceof L2Character)
				{
					player = (L2Character) target;
					player.stopAbnormalEffect(AbnormalEffect.BIG_HEAD);
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_polyself"))
		{
			try
			{
				String id = st.nextToken();
				if (activeChar.getPoly().setPolyInfo("npc", id))
				{
					activeChar.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false);
					activeChar.broadcastUserInfo(); // Should be done automatically?
				}
				else
				{
					activeChar.sendMessage("Invalid ID specified.");
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //polyself <npcId>");
			}
		}
		else if (command.startsWith("admin_unpolyself"))
		{
			if (activeChar.getPoly().isMorphed())
			{
				activeChar.getPoly().setPolyInfo(null, "1");
				activeChar.decayMe();
				activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
				activeChar.broadcastUserInfo(); // Should be done automatically?
			}
		}
		else if (command.equals("admin_clearteams"))
		{
			try
			{
				for (L2Player player : activeChar.getKnownList().getKnownPlayers().values())
				{
					player.setTeam(0);
					player.broadcastUserInfo();
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_setteam_close"))
		{
			String val = st.nextToken();
			int teamVal = Integer.parseInt(val);
			try
			{
				for (L2Player player : activeChar.getKnownList().getKnownPlayers().values())
				{
					if (activeChar.isInsideRadius(player, 400, false, true))
					{
						player.setTeam(teamVal);
						if (teamVal != 0)
						{
							player.sendMessage("You have joined team " + teamVal);
						}
						player.broadcastUserInfo();
					}
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //setteam_close <teamId>");
			}
		}
		else if (command.startsWith("admin_setteam"))
		{
			try
			{
				String val = st.nextToken();
				int teamVal = Integer.parseInt(val);
				L2Object target = activeChar.getTarget();
				L2Player player = null;
				if (target instanceof L2Player)
					player = (L2Player) target;
				else
					return false;
				player.setTeam(teamVal);
				if (teamVal != 0)
				{
					player.sendMessage("You have joined team " + teamVal);
				}
				player.broadcastUserInfo();
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //setteam <teamId>");
			}
		}
		else if (command.startsWith("admin_give_souls"))
		{
			try
			{
				if (st.hasMoreTokens())
				{
					int count = Integer.parseInt(st.nextToken());
					L2Object target = activeChar.getTarget();
					L2Player player = null;
					if (target instanceof L2Player)
					{
						player = (L2Player) target;
						player.setSouls(count);
					}
					else
						activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
				else
				{
					activeChar.sendMessage("Usage: //give_souls <count>");
				}
			}
			catch (NumberFormatException nbe)
			{
				activeChar.sendMessage("Usage: //give_souls <count>");
			}
		}
		else if (command.startsWith("admin_social"))
		{
			try
			{
				String target = null;
				L2Object obj = activeChar.getTarget();
				if (st.countTokens() == 2)
				{
					int social = Integer.parseInt(st.nextToken());
					target = st.nextToken();
					if (target != null)
					{
						L2Player player = L2World.getInstance().getPlayer(target);
						if (player != null)
						{
							if (performSocial(social, player, activeChar))
								activeChar.sendMessage(player.getName() + " was affected by your request.");
						}
						else
						{
							try
							{
								int radius = Integer.parseInt(target);
								for (L2Object object : activeChar.getKnownList().getKnownObjects().values())
									if (activeChar.isInsideRadius(object, radius, false, false))
										performSocial(social, object, activeChar);
								activeChar.sendMessage(radius + " units radius affected by your request.");
							}
							catch (NumberFormatException nbe)
							{
								activeChar.sendMessage("Incorrect parameter");
							}
						}
					}
				}
				else if (st.countTokens() == 1)
				{
					int social = Integer.parseInt(st.nextToken());
					if (obj == null)
						obj = activeChar;
					if (performSocial(social, obj, activeChar))
						activeChar.sendMessage(obj.getName() + " was affected by your request.");
					else
						activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
				}
				else if (!command.contains("menu"))
					activeChar.sendMessage("Usage: //social <social_id> [player_name|radius]");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else if (command.startsWith("admin_special"))
		{
			try
			{
				String target = null;
				L2Object obj = activeChar.getTarget();
				if (st.countTokens() == 2)
				{
					String parm = st.nextToken();
					int special = Integer.decode("0x" + parm);
					target = st.nextToken();
					if (target != null)
					{
						L2Player player = L2World.getInstance().getPlayer(target);
						if (player != null)
						{
							if (performSpecial(special, player))
								activeChar.sendMessage(player.getName() + "'s special status was affected by your request.");
							else
								activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
						}
						else
						{
							try
							{
								int radius = Integer.parseInt(target);
								for (L2Object object : activeChar.getKnownList().getKnownObjects().values())
									if (activeChar.isInsideRadius(object, radius, false, false))
										performSpecial(special, object);
								activeChar.sendMessage(radius + " units radius affected by your request.");
							}
							catch (NumberFormatException nbe)
							{
								activeChar.sendMessage("Usage: //special <hex_special_mask> [player|radius]");
							}
						}
					}
				}
				else if (st.countTokens() == 1)
				{
					int special = Integer.decode("0x" + st.nextToken());
					if (obj == null)
						obj = activeChar;
					//if (obj != null)
					//{
						if (performSpecial(special, obj))
							activeChar.sendMessage(obj.getName() + "'s special status was affected by your request.");
						else
							activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					//}
					//else
					//	activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
				else if (!command.contains("menu"))
					activeChar.sendMessage("Usage: //special <special_mask> [player_name|radius]");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else if (command.startsWith("admin_effect"))
		{
			try
			{
				L2Object obj = activeChar.getTarget();
				int level = 1, skilltime = 1;
				int skill = Integer.parseInt(st.nextToken());
				if (st.hasMoreTokens())
					level = Integer.parseInt(st.nextToken());
				if (st.hasMoreTokens())
					skilltime = Integer.parseInt(st.nextToken());
				if (obj != null)
				{
					if (!(obj instanceof L2Character))
						activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					else
					{
						L2Character target = (L2Character) obj;
						target.broadcastPacket(new MagicSkillUse(target, activeChar, skill, level, skilltime, 0));
						activeChar.sendMessage(obj.getName() + " performs MSU " + skill + "/" + level + " by your request.");
					}
				}
				else
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //effect skill [level | level skilltime]");
			}
		}
		else if (command.startsWith("admin_abnormal"))
		{
			try
			{
				String target = null;
				L2Object obj = activeChar.getTarget();
				if (st.countTokens() == 2)
				{
					String parm = st.nextToken();
					int abnormal = Integer.decode("0x" + parm);
					target = st.nextToken();
					if (target != null)
					{
						L2Player player = L2World.getInstance().getPlayer(target);
						if (player != null)
						{
							if (performAbnormal(abnormal, player))
								activeChar.sendMessage(player.getName() + "'s abnormal status was affected by your request.");
							else
								activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
						}
						else
						{
							try
							{
								int radius = Integer.parseInt(target);
								for (L2Object object : activeChar.getKnownList().getKnownObjects().values())
									if (activeChar.isInsideRadius(object, radius, false, false))
										performAbnormal(abnormal, object);
								activeChar.sendMessage(radius + " units radius affected by your request.");
							}
							catch (NumberFormatException nbe)
							{
								activeChar.sendMessage("Usage: //abnormal <hex_abnormal_mask> [player|radius]");
							}
						}
					}
				}
				else if (st.countTokens() == 1)
				{
					int abnormal = Integer.decode("0x" + st.nextToken());
					if (obj != null)
					{
						if (performAbnormal(abnormal, obj))
							activeChar.sendMessage(obj.getName() + "'s abnormal status was affected by your request.");
						else
							activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					}
					else
						activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
				else if (!command.contains("menu"))
					activeChar.sendMessage("Usage: //abnormal <abnormal_mask> [player_name|radius]");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else if (command.startsWith("admin_set_displayeffect"))
		{
			L2Object target = activeChar.getTarget();
			if (!(target instanceof L2Npc))
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			L2Npc npc = (L2Npc) target;
			try
			{
				String type = st.nextToken();
				int diplayeffect = Integer.parseInt(type);
				npc.setDisplayEffect(diplayeffect);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //set_displayeffect <id>");
			}
		}
		if (command.contains("menu"))
			showMainPage(activeChar, command);
		return true;
	}

	/**
	 * @param action bitmask that should be applied over target's abnormal
	 * @param target
	 * @return <i>true</i> if target's abnormal state was affected , <i>false</i> otherwise.
	 */
	private boolean performAbnormal(int action, L2Object target)
	{
		if (target instanceof L2Character)
		{
			L2Character character = (L2Character) target;
			if ((character.getAbnormalEffect() & action) == action)
				character.stopAbnormalEffect(action);
			else
				character.startAbnormalEffect(action);
			return true;
		}
		return false;
	}

	private boolean performSpecial(int action, L2Object target)
	{
		if (target instanceof L2Player)
		{
			L2Character character = (L2Character) target;
			if ((character.getSpecialEffect() & action) == action)
				character.stopSpecialEffect(action);
			else
				character.startSpecialEffect(action);
			return true;
		}
		else
			return false;
	}

	private boolean performSocial(int action, L2Object target, L2Player activeChar)
	{
		try
		{
			if (target instanceof L2Character)
			{
				if (target instanceof L2ChestInstance)
				{
					activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					return false;
				}
				if (target instanceof L2Npc && (action < 1 || action > 6))
				{
					activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					return false;
				}
				if (target instanceof L2Player && (action < 2 || action > 16))
				{
					activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					return false;
				}
				final L2Character character = (L2Character) target;
				character.broadcastPacket(new SocialAction(character, action));
			}
			else
				return false;
		}
		catch (Exception e)
		{
		}
		return true;
	}

	/**
	 * 
	 * @param type - atmosphere type (signssky,sky)
	 * @param state - atmosphere state(night,day)
	 */
	private void adminAtmosphere(String type, String state, L2Player activeChar)
	{
		L2GameServerPacket packet = null;

		if (type.equals("signsky"))
		{
			if (state.equals("dawn"))
				packet = new SSQInfo(2);
			else if (state.equals("dusk"))
				packet = new SSQInfo(1);
		}
		else if (type.equals("sky"))
		{
			if (state.equals("night"))
				packet = SunSet.STATIC_PACKET;
			else if (state.equals("day"))
				packet = SunRise.STATIC_PACKET;
			else if (state.equals("red"))
				packet = new ExRedSky(10);
		}
		else
			activeChar.sendMessage("Usage: //atmosphere <signsky dawn|dusk>|<sky day|night|red>");
		if (packet != null)
			for (L2Player player : L2World.getInstance().getAllPlayers())
				player.sendPacket(packet);
	}

	private void playAdminSound(L2Player activeChar, String sound)
	{
		PlaySound _snd = new PlaySound(1, sound);
		activeChar.sendPacket(_snd);
		activeChar.broadcastPacket(_snd);
		activeChar.sendMessage("Playing " + sound + ".");
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void showMainPage(L2Player activeChar, String command)
	{
		String filename = "effects_menu";
		if (command.contains("abnormal"))
			filename = "abnormal";
		else if (command.contains("special"))
			filename = "special";
		else if (command.contains("social"))
			filename = "social";
		activeChar.showHTMLFile(AdminHelpPage.ADMIN_HELP_PAGE + filename + ".htm");
	}
}
