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
package net.l2emuproject.gameserver.handler.skillhandlers;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.handler.ISkillConditionChecker;
import net.l2emuproject.gameserver.instancemanager.InstanceManager;
import net.l2emuproject.gameserver.instancemanager.MapRegionManager;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.l2skills.L2SkillRecall;
import net.l2emuproject.gameserver.skills.l2skills.L2SkillTeleport;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.Location;
import net.l2emuproject.gameserver.world.mapregion.TeleportWhereType;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.L2Player.TeleportMode;

public class Recall extends ISkillConditionChecker
{
	private static final L2SkillType[] SKILL_IDS = { L2SkillType.RECALL, L2SkillType.TELEPORT };
	
	@Override
	public boolean checkConditions(L2Character activeChar, L2Skill skill)
	{
		if (activeChar instanceof L2Player)
		{
			L2Player player = (L2Player)activeChar;
			
			// If Alternate rule Karma punishment is set to true, forbid skill Return to player with Karma
			if (skill.getSkillType() == L2SkillType.RECALL && !Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && player.getKarma() > 0)
			{
				player.sendMessage("You can't teleport with karma!");
				return false;
			}
			
			if (!player.canTeleport(player.hasSkill(skill.getId()) ? TeleportMode.RECALL : TeleportMode.SCROLL_OF_ESCAPE, true))
				return false;
		}
		
		return super.checkConditions(activeChar, skill);
	}
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (activeChar instanceof L2Player)
		{
			L2Player player = (L2Player)activeChar;
			
			if (!player.canTeleport(player.hasSkill(skill.getId()) ? TeleportMode.RECALL : TeleportMode.SCROLL_OF_ESCAPE, true))
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		for (L2Character target : targets)
		{
			if (target == null)
				continue;
			
			if (target instanceof L2Player)
			{
				L2Player targetChar = (L2Player)target;
				
				if (!targetChar.canTeleport(TeleportMode.RECALL))
				{
					targetChar.sendPacket(ActionFailed.STATIC_PACKET);
					continue;
				}
			}
			
			Location loc = null;
			if (skill.getSkillType() == L2SkillType.TELEPORT)
			{
				loc = ((L2SkillTeleport)skill).getTeleportCoords();
				
				if (loc != null)
				{
					// target is not player OR player is not flying or flymounted
					// TODO: add check for gracia continent coords
					if (target instanceof L2Player
							&& (target.isFlying() || ((L2Player)target).isFlyingMounted()))
						loc = null;
					// verified on retail - nothing happens
				}
			}
			else if (skill.getSkillType() == L2SkillType.RECALL && target instanceof L2Player)
			{
				if (target.isInInstance())
				{
					loc = InstanceManager.getInstance().getInstance(target.getInstanceId()).getReturnTeleport();
				}
				
				if (loc == null)
				{
					TeleportWhereType type = ((L2SkillRecall)skill).getRecallType();
					
					loc = MapRegionManager.getInstance().getTeleToLocation((L2Player)target, type);
				}
			}
			
			if (loc != null)
			{
				if (skill.getId() != 5226)
					target.setInstanceId(0);
				
				if (target instanceof L2Player)
					target.getActingPlayer().setIsIn7sDungeon(false);
				target.teleToLocation(loc, true);
			}
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
