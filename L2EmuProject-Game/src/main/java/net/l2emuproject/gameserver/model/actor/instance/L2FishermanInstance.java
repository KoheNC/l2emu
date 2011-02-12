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

import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.datatables.SkillTreeTable;
import net.l2emuproject.gameserver.model.L2Skill;
import net.l2emuproject.gameserver.model.L2SkillLearn;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.AcquireSkillDone;
import net.l2emuproject.gameserver.network.serverpackets.AcquireSkillList;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;

public class L2FishermanInstance extends L2MerchantInstance
{
    /**
     * @param objectId
     * @param template
     */
    public L2FishermanInstance(int objectId, L2NpcTemplate template)
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
        
        return "data/html/fisherman/" + pom + ".htm";
    }

    public static void showSkillList(L2PcInstance player, boolean closable)
    {
        L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableFishingSkills(player);
        AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.SkillType.Fishing);
        
        int counts = 0;

        for (L2SkillLearn s : skills)
        {
            L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
            
            if (sk == null)
                continue;
            
            counts++;
            asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getSpCost(), 1);
        }
        
        if (counts == 0)
        {
            SystemMessage sm;
            int minlevel = SkillTreeTable.getInstance().getMinLevelForNewFishingSkill(player);
            if (minlevel > 0)
            {
                sm = new SystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_COME_BACK_WHEN_REACHED_S1);
                sm.addNumber(minlevel);
            }
            else
            {
                sm = SystemMessageId.NO_MORE_SKILLS_TO_LEARN.getSystemMessage();
            }
            player.sendPacket(sm);
            if (closable)
            	player.sendPacket(AcquireSkillDone.PACKET);
        }
        else
        {
            player.sendPacket(asl);
        }
        
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }
}
