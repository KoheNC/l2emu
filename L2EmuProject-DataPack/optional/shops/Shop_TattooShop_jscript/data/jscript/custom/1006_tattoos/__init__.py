print "importing custom: 1006_tattoos"
import sys
from net.l2emuproject.gameserver.model.quest import State
from net.l2emuproject.gameserver.model.quest import QuestState
from net.l2emuproject.gameserver.model.quest.jython import QuestJython as JQuest
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
	else:
             htmltext = "You do not have enough crystals."

# TattooOfire
    if event == "2":
        if st.getQuestItemsCount(1458) >= 276:
            st.takeItems(1458,276)
            st.giveItems(486,1)
            htmltext = "Enjoy your new tattoo."
	else:
             htmltext = "You do not have enough crystals."

# TatooOstout
    if event == "3":
        if st.getQuestItemsCount(1458) >= 276:
            st.takeItems(1458,276)
            st.giveItems(487,1)
            htmltext = "Enjoy your new tattoo."
  	else:
             htmltext = "You do not have enough crystals."

# TattooOflame
    if event == "4":
        if st.getQuestItemsCount(1460) >= 462:
            st.takeItems(1460,462)
            st.giveItems(488,1)
            htmltext = "Enjoy your new tattoo."
    	else:
             htmltext = "You do not have enough crystals."

# TattooObraze
    if event == "5":
         if st.getQuestItemsCount(1459) >= 428:
            st.takeItems(1459,428)
            st.giveItems(489,1)
            htmltext = "Enjoy your new tattoo."
         else:
             htmltext = "You do not have enough crystals."

# TatooOblood
    if event == "6":
         if st.getQuestItemsCount(1461) >= 462:
            st.takeItems(1461,462)
            st.giveItems(490,1)
            htmltext = "Enjoy your new tattoo."
         else:
             htmltext = "You do not have enough crystals."

# TatooOabsolute
    if event == "7":
         if st.getQuestItemsCount(1461) >= 422:
            st.takeItems(1461,422)
            st.giveItems(491,1)
            htmltext = "Enjoy your new tattoo."
         else:
             htmltext = "You do not have enough crystals."

# TatooOsoul
    if event == "8":
         if st.getQuestItemsCount(1458) >= 181:
            st.takeItems(1458,181)
            st.giveItems(492,1)
            htmltext = "Enjoy your new tattoo."
         else:
             htmltext = "You do not have enough crystals."

# TattooOavadon
    if event == "9":
         if st.getQuestItemsCount(1460) >= 208:
            st.takeItems(1460,208)
            st.giveItems(493,1)
            htmltext = "Enjoy your new tattoo."
         else:
             htmltext = "You do not have enough crystals."

# TatooOdoom
    if event == "10":
         if st.getQuestItemsCount(1460) >= 321:
            st.takeItems(1460,321)
            st.giveItems(494,1)
            htmltext = "Enjoy your new tattoo."
         else:
             htmltext = "You do not have enough crystals."

# TattooOpledge
    if event == "11":
         if st.getQuestItemsCount(1460) >= 208:
            st.takeItems(1460,208)
            st.giveItems(495,1)
            htmltext = "Enjoy your new tattoo."
         else:
             htmltext = "You do not have enough crystals."

# TattooOdivine
    if event == "12":
         if st.getQuestItemsCount(1460) >= 321:
            st.takeItems(1460,321)
            st.giveItems(496,1)
            htmltext = "Enjoy your new tattoo."
         else:
             htmltext = "You do not have enough crystals."

# TattooOnightmare
    if event == "13":
         if st.getQuestItemsCount(1461) >= 422:
            st.takeItems(1461,422)
            st.giveItems(2410,1)
            htmltext = "Enjoy your new tattoo."
         else:
             htmltext = "You do not have enough crystals."

    if event == "0":
      htmltext = "Trade has been canceled."
    
    if htmltext != event:
      st.setState(COMPLETED)
      st.exitQuest(1)

    return htmltext

 def onTalk (self,npc,player):

   npcId = npc.getNpcId()
   st = player.getQuestState(qn)
   htmltext = "<html><body>I have nothing to say to you.</body></html>"
   st.set("cond","0")
   st.setState(STARTED)
   return "1.htm"

QUEST       = Quest(1006,qn,"custom")
CREATED     = State('Start',     QUEST)
STARTED     = State('Started',   QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)

QUEST.addStartNpc(7100)

QUEST.addTalkId(7100)

