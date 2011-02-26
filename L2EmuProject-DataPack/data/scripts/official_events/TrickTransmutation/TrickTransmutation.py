# Trick or Transmutation created by Gnat

import math
import sys
from java.lang import System

from net.l2emuproject.gameserver.ai import CtrlIntention
from net.l2emuproject.gameserver.services.quest import State
from net.l2emuproject.gameserver import Announcements
from net.l2emuproject.gameserver.datatables import EventDroplist
from net.l2emuproject.gameserver.datatables import SkillTable
from net.l2emuproject.gameserver.services.quest import QuestState
from net.l2emuproject.gameserver.services.quest.jython import QuestJython as JQuest
from net.l2emuproject.gameserver.network.serverpackets import PlaySound
from net.l2emuproject.gameserver.model.actor.instance import L2PcInstance
from net.l2emuproject.gameserver.script import DateRange
from net.l2emuproject.tools.random import Rnd
from java.util import Date
from java.util import GregorianCalendar

#### Configurable variables ###
#put here your event dates
#IMPORTANT!!! In the Gregorian Calendar January is month 0
# which means that february is month 1, july is month 6, etc...
StartDate = GregorianCalendar(2009,05,19)
EndDate = GregorianCalendar(2009,06,10)
EndSpawn = GregorianCalendar(2009,06,17) #normally 7 days after the end of the event

AlchemistKey = [9205]
AlchemistKeyDropChance = 50000 #5%  (100% = 1000000)
AlchemistKeyDropCount = [1,1]    #min 1, max 1
AlchemistServitor = 32132
AlchemistChest = 13036
OnEnterAnnuonce = ['"Trick or Transmutation" Event is currently active.']
###############################

Date1 = StartDate.getTime()
Date2 = EndDate.getTime()
Date3 = EndSpawn.getTime()
EventDates = DateRange(Date1, Date2)
ManagerSpawnDates = DateRange(Date1, Date3)

lowDrop = [ 9162,9163,9164,9165,9166,9167,9169 ]
# X3 qty
midDrop = 9170 
# X6 qty
highDrop = 9168


ServX = [ 147698,147443,82282,82754,15064,111067,-12965,87362,-81037,117412,43983,-45907,12153,-84458,114750,-45656,-117195 ]
ServY = [ -56025,26942,148608,53573,143254,218933,122914,-143166,150092,76642,-47758,49387,16753,244761,-178692,-113119,46837 ]
ServZ = [ -2775,-2205,-3473,-1496,-2668,-3543,-3117,-1293,-3044,-2695,-797,-3060,-4584,-3730,-820,-240,367 ]

EventDroplist.getInstance().addGlobalDrop(AlchemistKey,AlchemistKeyDropCount,AlchemistKeyDropChance,EventDates)
Announcements.getInstance().addEventAnnouncement(EventDates,OnEnterAnnuonce) 

class TrickTransmutation (JQuest):

    def __init__(self,id,name,descr):
        JQuest.__init__(self,id,name,descr)
        self.chests = [ 13036 ]
        #will check every 30 minutes if the event should still be on or off.
        self.startQuestTimer("trickEventCheck",1800000,None,None)
        self.trickEvent = 0
        self.xx = []
        self.yy = []
        self.zz = []
        self.chestId = []
        self.livechests = []
        self.AlchemServ = []
        self.L2NpcId = []
        if EventDates.isWithinRange(Date()):
                    self.trickEvent = 1
        if self.trickEvent == 1:
            print "Trick or Transmutation Event - ON"
            for m in range(len(ServX)):
                mob = self.addSpawn(AlchemistServitor,ServX[m],ServY[m],ServZ[m],0,False,0)
                self.AlchemServ.append(mob)
            for i in self.chests :
                self.addSkillSeeId(i)
                self.addKillId(i)
            numChests = 0
            for i in range(len(ServX)):
              r1 = Rnd.get(8)
              if r1 <= 3:
                    r1 = 4
              for m in range(r1):
                    x1 = ServX[i] + (Rnd.get(1000) - 500)
                    y1 = ServY[i] + (Rnd.get(1000) - 500)
                    z1 = ServZ[i]
                    numChests += 1
                    mob = self.addSpawn(self.chests[0],x1,y1,z1,0,False,0)
                    self.livechests.append(mob)
            numChests = str(numChests)
            print "Event Chests spawned: " + numChests
        if self.trickEvent == 0:
            print "Trick or Transmutation Event - OFF"
            if ManagerSpawnDates.isWithinRange(Date()):
                    print "Alchemist Servitor Spawned"
                    for m in range(len(ServX)):
                        mob = self.addSpawn(AlchemistServitor,ServX[m],ServY[m],ServZ[m],0,False,0)
                        self.AlchemServ.append(mob)



    def onSkillSee (self,npc,player,skill,targets,isPet):
        if not npc in targets: return
        npcId = npc.getNpcId()
        skillId = skill.getId()
        st = player.getQuestState("TrickTransmutation")
        level = player.getLevel()
        if not st : return
        if npcId == AlchemistChest:
          if self.trickEvent == 1:
             if skillId == 2322:
                npc.reduceCurrentHp(99999999, player,None)
                if Rnd.get(100) <= 30:
                      rr = Rnd.get(3)
                      if rr == 0:
                          r1 = Rnd.get(7)
                          st.giveItems(lowDrop[r1],1)
                      if rr == 1:
                          st.giveItems(midDrop,3)
                      if rr == 2:
                          st.giveItems(highDrop,6)
                if Rnd.get(100) <= 70:
                      RewardBase = int(20000 + ((level / 20) * 25000))
                      rr = int(Rnd.get(RewardBase) / 3)
                      amount = (RewardBase + rr - int(RewardBase / 6))
                      st.giveItems(57,amount)
                KillId = []
             else:
                if level > 60:
                    npc.doCast(SkillTable.getInstance().getInfo(264,1))
                    npc.doCast(SkillTable.getInstance().getInfo(1068,2))
                    npc.doCast(SkillTable.getInstance().getInfo(1036,2))
                    npc.doCast(SkillTable.getInstance().getInfo(1311,3))
                if level > 70:
                    npc.doCast(SkillTable.getInstance().getInfo(1068,3))
                    npc.doCast(SkillTable.getInstance().getInfo(1311,6))
                    npc.doCast(SkillTable.getInstance().getInfo(1310,2))
                if level > 80:
                    npc.doCast(SkillTable.getInstance().getInfo(1310,4))
                    npc.doCast(SkillTable.getInstance().getInfo(1308,3))
                    npc.doCast(SkillTable.getInstance().getInfo(310,1))
                    npc.doCast(SkillTable.getInstance().getInfo(275,1))
                    npc.doCast(SkillTable.getInstance().getInfo(1261,1))
                


    def onAdvEvent (self,event,npc,player):
        #This part is to check if the event starts or ends while the server is online.  I.E.: It will start or stop the event even if there is no restart.
        if event == "trickEventCheck":
          if self.trickEvent == 1:
              if not EventDates.isWithinRange(Date()):
                     self.trickEvent = 0
                     for i in range(len(self.livechests)):
                         self.livechests[i].onDecay()
          if self.trickEvent == 0:
              if EventDates.isWithinRange(Date()):
                     Announcements.getInstance().announceToAll("The Trick or Transmutation Event is now starting and will last until " + str(EndDate) + ".  See the Alchemist Servitor to participate!")
                     self.trickEvent = 1
                     print "Trick or Transmutation Event - ON"
                     for m in range(len(ServX)):
                         mob = self.addSpawn(AlchemistServitor,ServX[m],ServY[m],ServZ[m],0,False,0)
                         self.AlchemServ.append(mob)
                     for i in self.chests :
                         self.addSkillSeeId(i)
                         self.addKillId(i)
                     for i in range(len(ServX)):
                       r1 = Rnd.get(8)
                       if r1 <= 3:
                             r1 = 4
                       for m in range(r1):
                             x1 = ServX[i] + (Rnd.get(500) - 250)
                             y1 = ServY[i] + (Rnd.get(500) - 250)
                             z1 = ServZ[i]
                             number = number + 1
                             mob = self.addSpawn(self.chests[0],x1,y1,z1,0,False,0)
                             self.livechests.append(mob)
        if event == "chestsRespawn":
          if self.trickEvent == 1:
             spawn = npc.getSpawn()
             spawn.spawnOne(False)



    def onTalk (self,npc,player) :
        npcId = npc.getNpcId()
        st = player.getQuestState("TrickTransmutation")
        if not st : return
        if npcId == 32132:
          if self.trickEvent == 1:
             htmltext = "<html><body><title>Trick or Transmutation Event</title>Alchemist's Servitor:<br><br>If you can find one of the Alchemist's Chest Keys, you can keep whatever you find inside the Alchemist's Chest you open. Wondrous treasures are to be had by he (or she) who creates a Philosopher's Stone!<br><br></html>/body>"
          if self.trickEvent == 0:
             htmltext = "<html><body>AlchemistServitor:<br><br>I am very sorry but all the chests have been found.<br><br><font color=LEVEL>(The event is over, better luck next time)</font></body></html>"
        return htmltext            
     


    def onKill(self,npc,player,isPet):
        npcId = npc.getNpcId() 
        npcObjId = npc.getObjectId() 
        if npcId != AlchemistChest : return
        if self.trickEvent == 1:
          self.startQuestTimer("chestsRespawn",20000,npc,None)



# Quest class and state definition
QUEST = TrickTransmutation(-1, "TrickTransmutation", "official_events")



QUEST.addStartNpc(32132)
QUEST.addTalkId(32132)
