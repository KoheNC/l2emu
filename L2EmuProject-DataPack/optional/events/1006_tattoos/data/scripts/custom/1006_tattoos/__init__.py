import sys
from net.l2emuproject.gameserver.services.quest import State
from net.l2emuproject.gameserver.services.quest import QuestState
from net.l2emuproject.gameserver.services.quest.jython import QuestJython as JQuest

qn = "1006_tattoos"

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
    htmltext = event

# TattooOpower
    if event == "1":
        if st.getQuestItemsCount(1458) >= 181:
            st.takeItems(1458,181)
            st.giveItems(485,1)
            htmltext = "Enjoy your new tattoo."
            st.exitQuest(1)
        else:
            htmltext = "You do not have enough crystals."
            st.exitQuest(1)

# TattooOfire
    if event == "2":
        if st.getQuestItemsCount(1458) >= 276:
            st.takeItems(1458,276)
            st.giveItems(486,1)
            htmltext = "Enjoy your new tattoo."
            st.exitQuest(1)
        else:
            htmltext = "You do not have enough crystals."
            st.exitQuest(1)

# TatooOstout
    if event == "3":
        if st.getQuestItemsCount(1458) >= 276:
            st.takeItems(1458,276)
            st.giveItems(487,1)
            htmltext = "Enjoy your new tattoo."
            st.exitQuest(1)
        else:
            htmltext = "You do not have enough crystals."
            st.exitQuest(1)

# TattooOflame
    if event == "4":
        if st.getQuestItemsCount(1460) >= 462:
            st.takeItems(1460,462)
            st.giveItems(488,1)
            htmltext = "Enjoy your new tattoo."
            st.exitQuest(1)
    	else:
             htmltext = "You do not have enough crystals."
             st.exitQuest(1)

# TattooObraze
    if event == "5":
         if st.getQuestItemsCount(1459) >= 428:
            st.takeItems(1459,428)
            st.giveItems(489,1)
            htmltext = "Enjoy your new tattoo."
            st.exitQuest(1)
         else:
             htmltext = "You do not have enough crystals."
             st.exitQuest(1)

# TatooOblood
    if event == "6":
         if st.getQuestItemsCount(1461) >= 462:
            st.takeItems(1461,462)
            st.giveItems(490,1)
            htmltext = "Enjoy your new tattoo."
            st.exitQuest(1)
         else:
             htmltext = "You do not have enough crystals."
             st.exitQuest(1)

# TatooOabsolute
    if event == "7":
         if st.getQuestItemsCount(1461) >= 422:
            st.takeItems(1461,422)
            st.giveItems(491,1)
            htmltext = "Enjoy your new tattoo."
            st.exitQuest(1)
         else:
             htmltext = "You do not have enough crystals."
             st.exitQuest(1)

# TatooOsoul
    if event == "8":
         if st.getQuestItemsCount(1458) >= 181:
            st.takeItems(1458,181)
            st.giveItems(492,1)
            htmltext = "Enjoy your new tattoo."
            st.exitQuest(1)
         else:
             htmltext = "You do not have enough crystals."
             st.exitQuest(1)

# TattooOavadon
    if event == "9":
         if st.getQuestItemsCount(1460) >= 208:
            st.takeItems(1460,208)
            st.giveItems(493,1)
            htmltext = "Enjoy your new tattoo."
            st.exitQuest(1)
         else:
             htmltext = "You do not have enough crystals."
             st.exitQuest(1)

# TatooOdoom
    if event == "10":
         if st.getQuestItemsCount(1460) >= 321:
            st.takeItems(1460,321)
            st.giveItems(494,1)
            htmltext = "Enjoy your new tattoo."
            st.exitQuest(1)
         else:
             htmltext = "You do not have enough crystals."
             st.exitQuest(1)

# TattooOpledge
    if event == "11":
         if st.getQuestItemsCount(1460) >= 208:
            st.takeItems(1460,208)
            st.giveItems(495,1)
            htmltext = "Enjoy your new tattoo."
            st.exitQuest(1)
         else:
             htmltext = "You do not have enough crystals."
             st.exitQuest(1)

# TattooOdivine
    if event == "12":
         if st.getQuestItemsCount(1460) >= 321:
            st.takeItems(1460,321)
            st.giveItems(496,1)
            htmltext = "Enjoy your new tattoo."
            st.exitQuest(1)
         else:
             htmltext = "You do not have enough crystals."
             st.exitQuest(1)

# TattooOnightmare
    if event == "13":
         if st.getQuestItemsCount(1461) >= 422:
            st.takeItems(1461,422)
            st.giveItems(2410,1)
            htmltext = "Enjoy your new tattoo."
            st.exitQuest(1)
         else:
             htmltext = "You do not have enough crystals."
             st.exitQuest(1)

    if event == "0":
      htmltext = "Trade has been canceled."
      st.exitQuest(1)
    
    if htmltext != event:
      st.exitQuest(1)

    return htmltext

 def onTalk (Self,npc,player):

   npcId = npc.getNpcId()
   st = player.getQuestState(qn)
   htmltext = "<html><body>I have no tasks for you right now.</body></html>"
   st.set("cond","0")
   st.setState(State.STARTED)
   return "1.htm"

QUEST       = Quest(1006,qn,"custom")

QUEST.addStartNpc(31227)

QUEST.addTalkId(31227)
