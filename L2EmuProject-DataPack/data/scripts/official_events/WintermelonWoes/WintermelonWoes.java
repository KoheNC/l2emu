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
package official_events.WintermelonWoes;

import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.network.serverpackets.CreatureSay;
import net.l2emuproject.gameserver.services.quest.Quest;
import net.l2emuproject.gameserver.services.quest.QuestService;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2MonsterInstance;
import net.l2emuproject.tools.random.Rnd;

public class WintermelonWoes extends Quest
{
    private static final int MANAGER = 32727;
   
    private static final int NECTAR_SKILL = 2005;
   
    private static final int[] CHRONO_LIST = {
        5133,5817,7058,8350,4202
    };
   
    private static final int[] WATERMELON_LIST = {
        13271,13272,13273,13274,
        13275,13276,13277,13278
    };
   
    private static final String[] _NOCHRONO_TEXT = {
        "So weak... If I had lings and a mouth I would cough pathecally, but I cat't even do that! I'M A MONSTER!",
        "Are you trying to eat without doing the legwork? Fine! Do what you want!",
        "I'm gonna bursttttt!!!!",
        "Haha! You have cotton candy for muscles! Ahahaha!",
        "Go go ! haha..."
    };
   
    private static final String[] _CHRONO_TEXT = {
        "Arghh... Chrono weapon...",
        "My end is coming...",
        "Please leave me !",
        "Heeellpppp...",
        "Somebody help me please..."
    };
    private static final String[] _NECTAR_TEXT = {
        "Yummy... Nectar...",
        "Plase give me more...",
        "Hmmm.. More.. I need more...",
        "I will like you more if you give me more...",
        "Hmmmmmmm...",
        "My favourite..."
    };
   
    private static final int[][] DROPLIST =
    {
        // npcId, itemId, chance
 
        // Young Watermelon
        { 13271,  6391, 100 },  // Nectar
        // Defective Watermelon
        { 13272,  6391, 100 },  // Nectar
 
        // Rain Watermelon
        { 13273,  6391, 100 },  // Nectar
        // Large Rain Watermelon
        { 13274,  6391, 100 },  // Nectar
 
        // Young Honey Watermelon
        { 13275,  6391, 100 },  // Nectar
        // Defective Honey Watermelon
        { 13276,  6391, 100 },  // Nectar
       
        // Rain Honey Watermelon
        { 13277,  6391, 100 },  // Nectar
        // Large Rain Honey Watermelon
        { 13278,  6391, 100 }   // Nectar
    };
   
    public String onAttack(L2Npc npc, L2Player attacker, int damage, boolean isPet)
    {      
        if (contains(WATERMELON_LIST,npc.getNpcId()))
        {
            if(isPet)
            {
                noChronoText(npc);
                npc.setIsInvul(true);              
                return null;
            }
            if(attacker.getActiveWeaponItem() != null && contains(CHRONO_LIST,attacker.getActiveWeaponItem().getItemId()))
            {
                ChronoText(npc);
                npc.setIsInvul(false);
                npc.getStatus().reduceHp(10, attacker);
                return null;
            }
            else
            {
                noChronoText(npc);
                npc.setIsInvul(true);
                return null;
            }
        }
        return super.onAttack(npc, attacker, damage, isPet);
    }
   
    public String onSkillSee(L2Npc npc, L2Player caster, L2Skill skill, L2Object[] targets, boolean isPet)
    {
        if (contains(targets,npc) && contains(WATERMELON_LIST,npc.getNpcId()) && (skill.getId() == NECTAR_SKILL))
        {
            switch(npc.getNpcId())
            {
                case 13271:
                    randomSpawn(13272, npc, true);
                    break;
 
                case 13273:
                    randomSpawn(13274, npc, true);
                    break;
 
                case 13275:
                    randomSpawn(13276, npc, true);
                    break;
 
                case 13277:
                    randomSpawn(13278, npc, true);
                    break;
            }           
        }
        return super.onSkillSee(npc,caster,skill,targets,isPet);
    }
 
    public String onKill (L2Npc npc, L2Player killer, boolean isPet)
    {
        dropItem(npc, killer);
       
        return super.onKill(npc, killer, isPet);
    }
 
    public String onSpawn(L2Npc npc)
    {
        npc.setIsImmobilized(true);
        npc.disableCoreAI(true);
        return null;
        // return super.onSpawn(npc);
    }
   
    private static final void dropItem(L2Npc mob, L2Player player)
    {
        final int npcId = mob.getNpcId();
        final int chance = Rnd.get(100);
        for (int i = 0; i < DROPLIST.length; i++)
        {
            int[] drop = DROPLIST[i];
            if (npcId == drop[0])
            {
                if (chance < drop[2])
                {
                    if(drop[1] > 20000)
                        ((L2MonsterInstance)mob).dropItem(player, drop[1], 2);
                    else
                        ((L2MonsterInstance)mob).dropItem(player, drop[1], Rnd.get(2, 6));
                    continue;
                }
            }
            if (npcId < drop[0])
                return; // not found
        }
    }
   
    private void randomSpawn(int npcId, L2Npc npc, boolean delete)
    {
        if(Rnd.get(100) < 10)
            spawnNext(npcId, npc);
        else
            nectarText(npc);
    }
   
    private void ChronoText(L2Npc npc)
    {
        if(Rnd.get(100) < 20)
            npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Normal, npc.getName(), _CHRONO_TEXT[Rnd.get(_CHRONO_TEXT.length)]));      
    }
    private void noChronoText(L2Npc npc)
    {
        if(Rnd.get(100) < 20)
            npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Normal, npc.getName(), _NOCHRONO_TEXT[Rnd.get(_NOCHRONO_TEXT.length)]));      
    }
    private void nectarText(L2Npc npc)
    {
        if(Rnd.get(100) < 30)
            npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Normal, npc.getName(), _NECTAR_TEXT[Rnd.get(_NECTAR_TEXT.length)]));
    }
   
    private void spawnNext(int npcId, L2Npc npc)
    {
        addSpawn(npcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 60000);       
        npc.deleteMe();
    }
 
    public static <T> boolean contains(T[] array, T obj)
    {
        for (int i = 0; i < array.length; i++)
        {
            if (array[i] == obj)
            {
                return true;
            }
        }
        return false;
    }
   
    public static boolean contains(int[] array, int obj)
    {
        for (int i = 0; i < array.length; i++)
        {
            if (array[i] == obj)
            {
                return true;
            }
        }
        return false;
    }
   
    public WintermelonWoes(int questId, String name, String descr)
    {
        super(questId, name, descr);
       
        for (int mob : WATERMELON_LIST)
        {
            addAttackId(mob);
            addKillId(mob);
            addSpawnId(mob);
            addSkillSeeId(mob);
        }
 
        addStartNpc(MANAGER);
        addFirstTalkId(MANAGER);
        addTalkId(MANAGER);
       
        addSpawn(MANAGER, -13946, 122414, -2984, 0, false, 0);
    }
   
    @Override
    public String onFirstTalk(L2Npc npc, L2Player player)
    {
        String htmltext = "";
        QuestState st = player.getQuestState(getName());
        if (st == null)
        {
            Quest q = QuestService.getInstance().getQuest(getName());
            st = q.newQuestState(player);
        }
        htmltext = npc.getNpcId() + ".htm";
        return htmltext;
    }
   
    public static void main(String[] args)
    {
        new WintermelonWoes(-1,"WintermelonWoes","events");
    }
}