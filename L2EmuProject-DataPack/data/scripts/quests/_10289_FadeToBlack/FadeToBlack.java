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
package quests._10289_FadeToBlack;

import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;

/**
 * @author Plim
 */
public final class FadeToBlack extends QuestJython
{
   private static final String qn = "_10289_FadeToBlack";
   
   // NPCs
   private static final int GREYMORE = 32757;
   
   // Items
   private static final int MARK_OF_DARKNESS = 15528;
   private static final int MARK_OF_SPLENDOR = 15527;

   //MOBs
   private static final int ANAYS = 25701;
   
   @Override
   public String onAdvEvent(String event, L2Npc npc, L2Player player)
   {
      String htmltext = event;
      QuestState st = player.getQuestState(qn);
      
      if (st == null)
         return htmltext;
      
      if (npc.getNpcId() == GREYMORE)
      {
         if (event.equalsIgnoreCase("32757-04.htm"))
         {
            st.setState(State.STARTED);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         }
         else if(isNumber(event) && st.getQuestItemsCount(MARK_OF_SPLENDOR) > 0)
         {
            int itemId = Integer.parseInt(event);
            st.takeItems(MARK_OF_SPLENDOR, 1);
            st.giveItems(itemId, 1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
            htmltext = "32757-08.htm";
         }
      }
      return htmltext;
   }
   
   private boolean isNumber(String str)
   {
      if (str == null || str.length() == 0)
         return false;
      
      for (int i = 0; i < str.length(); i++)
      {
         if (!Character.isDigit(str.charAt(i)))
            return false;
      }
      
      return true;
   }

   @Override
   public String onTalk(L2Npc npc, L2Player player)
   {
      String htmltext = NO_QUEST;
      QuestState st = player.getQuestState(qn);
      QuestState secretMission = player.getQuestState("10288_SecretMission");
      if (st == null)
         return htmltext;
      
      if (npc.getNpcId() == GREYMORE)
      {
         switch(st.getState())
         {
            case State.CREATED :
               if (player.getLevel() >= 82 && secretMission != null && secretMission.getState() == State.COMPLETED)
                  htmltext = "32757-02.htm";
               else if (player.getLevel() < 82)
                  htmltext = "32757-00.htm";
               else
                  htmltext = "32757-01.htm";
               break;
            case State.STARTED :
               if (st.getInt("cond") == 1)
                  htmltext = "32757-04b.htm";
               if (st.getInt("cond") == 2)
               {
                  htmltext = "32757-05.htm";
                  player.addExpAndSp(55983, 136500);
                  st.set("cond","1");
                  st.playSound("ItemSound.quest_middle");
               }
               else if (st.getInt("cond") == 3)
                  htmltext = "32757-06.htm";
               break;
         }
      }
      return htmltext;
   }
   
   @Override
   public String onKill(L2Npc npc, L2Player player, boolean isPet)
   {
      if(player.getParty() == null)
      {
         QuestState st = player.getQuestState(qn);
         st.giveItems(MARK_OF_SPLENDOR, 1);
         st.playSound("ItemSound.quest_itemget");
         st.set("cond","3");
      }
      else
      {
         L2Player partyMember = getRandomPartyMember(player,"1");
         QuestState st1 = partyMember.getQuestState(qn);
         st1.giveItems(MARK_OF_SPLENDOR, 1);
         st1.playSound("ItemSound.quest_itemget");
         st1.set("cond","3");
         
         
         QuestState st2;
         for(L2Player pmember : player.getParty().getPartyMembers())
         {
            if(pmember.getObjectId() != partyMember.getObjectId() && st1.getInt("cond") == 1)
            {
               st2 = pmember.getQuestState(qn);
               st2.giveItems(MARK_OF_DARKNESS, 1);
               st2.playSound("ItemSound.quest_itemget");
               st2.set("cond","2");
            }
         }
      }

      return super.onKill(npc, player, isPet);
   }
   
   public FadeToBlack(int questId, String name, String descr)
   {
      super(questId, name, descr);
      
      addStartNpc(GREYMORE);
      addTalkId(GREYMORE);
      addKillId(ANAYS);
   }
   
   public static void main(String[] args)
   {
      new FadeToBlack(10289, qn, "Fade to Black");
   }
}
