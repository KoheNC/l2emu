# Made by disKret
import sys
from net.l2emuproject.gameserver.services.quest import State
from net.l2emuproject.gameserver.services.quest import QuestState
from net.l2emuproject.gameserver.services.quest.jython import QuestJython as JQuest

qn = "33_MakeAPairOfDressShoes"

#NPC
WOODLEY = 30838
IAN     = 30164
LEIKAR  = 31520

#ITEM
LEATHER         = 1882
THREAD          = 1868
ADENA           = 57
DRESS_SHOES_BOX = 7113

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
   htmltext = event
   if event == "30838-1.htm" :
     st.set("cond","1")
     st.setState(State.STARTED)
     st.playSound("ItemSound.quest_accept")
   if event == "31520-1.htm" :
     st.set("cond","2")
   if event == "30838-3.htm" :
     st.set("cond","3")
   if event == "30838-5.htm" :
     if st.getQuestItemsCount(LEATHER) >= 200 and st.getQuestItemsCount(THREAD) >= 600 and st.getQuestItemsCount(ADENA) >= 200000 :
       st.takeItems(LEATHER,200)
       st.takeItems(THREAD,600)
       st.takeItems(ADENA,200000)
       st.set("cond","4")
     else :
       htmltext = "You don't have enough materials"
   if event == "30164-1.htm" :
     if st.getQuestItemsCount(ADENA) >= 300000 :
       st.takeItems(ADENA,300000)
       st.set("cond","5")
     else :
       htmltext = "You don't have enough materials"
   if event == "30838-7.htm" :
     st.giveItems(DRESS_SHOES_BOX,1)
     st.playSound("ItemSound.quest_finish")
     st.unset("cond")
     st.exitQuest(False)
   return htmltext

 def onTalk (self,npc,player):
   htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
   st = player.getQuestState(qn)
   if not st : return htmltext

   npcId = npc.getNpcId()
   id = st.getState()
   cond = st.getInt("cond")
   if id == State.COMPLETED:
     htmltext = "<html><body>This quest has already been completed.</body></html>"
   elif npcId == WOODLEY and cond == 0 and st.getQuestItemsCount(DRESS_SHOES_BOX) == 0 :
     fwear=player.getQuestState("37_PleaseMakeMeFormalWear")
     if fwear :
       if fwear.get("cond") == "7" :
         htmltext = "30838-0.htm"
       else:
         st.exitQuest(1)
     else:
       st.exitQuest(1)
   elif id == State.STARTED :    
       if npcId == LEIKAR and cond == 1 :
         htmltext = "31520-0.htm"
       elif npcId == WOODLEY and cond == 2 :
         htmltext = "30838-2.htm"
       elif npcId == WOODLEY and cond == 3 :
         htmltext = "30838-4.htm"
       elif npcId == IAN and cond == 4 :
         htmltext = "30164-0.htm"
       elif npcId == WOODLEY and cond == 5 :
         htmltext = "30838-6.htm"
   return htmltext

QUEST       = Quest(33,qn,"Make A Pair Of Dress Shoes")

QUEST.addStartNpc(WOODLEY)
QUEST.addTalkId(WOODLEY)

QUEST.addTalkId(IAN)
QUEST.addTalkId(LEIKAR)
