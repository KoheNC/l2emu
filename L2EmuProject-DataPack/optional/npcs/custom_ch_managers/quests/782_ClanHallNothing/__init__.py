print "importing quests: 782: ClanHallNothing"

import sys
from net.l2emuproject.gameserver.model.actor.instance import L2PcInstance
from java.util import Iterator
from net.l2emuproject.gameserver             import SkillTable
from net.l2emuproject			       import L2DatabaseFactory
from net.l2emuproject.gameserver.services.quest import State
from net.l2emuproject.gameserver.services.quest import QuestState
from net.l2emuproject.gameserver.services.quest.jython import QuestJython as JQuest

NPC=[30784,30788,30790,30786,30778,30780,30782,30774,30776,30800,30802,30798,35457,35459,35451,35455,35453,31158,31160,31156,31152,31150,31154,35467,35465,35463,35461]

ADENA_ID=57

MIN_LEVEL=40

class Quest(JQuest):

	def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)


	def onEvent(self,event,st):
		htmltext = event
		# No se usa
		if event == "2":
			st.takeItems(57,500)
			st.player.teleToLocation(-80826,149775,-3043)		
                        htmltext = "Teletransportando a Gludin."
                        st.setState(COMPLETED)
    			
		if htmltext != event:
    			st.setState(COMPLETED)
			st.exitQuest(1)
		return htmltext


        def onTalk (self,npc,st):
                npcId = npc.getNpcId()
                count=st.getQuestItemsCount(ADENA_ID)
                if count > 0 :
                    st.takeItems(ADENA_ID,0)
                    clan = st.getPlayer().getClan().getClanId()
                    con=L2DatabaseFactory.getInstance().getConnection()
                    id_buffer = 0
                    offline=con.prepareStatement("SELECT * FROM clanhall WHERE ownerId=? AND id_buffer=?")
                    offline.setInt(1, clan)
                    offline.setInt(2, npcId)
                    rs=offline.executeQuery()
                    while (rs.next()) :
                            id_buffer=rs.getInt("id_buffer")
                    try :
                         offline.close()
                         con.close()
                    except : pass
                    if   npcId == 30784 and id_buffer == 30784:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 30788 and id_buffer == 30788:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 30790 and id_buffer == 30790:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 30786 and id_buffer == 30786:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 30778 and id_buffer == 30778:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 30780 and id_buffer == 30780:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 30782 and id_buffer == 30782:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 30774 and id_buffer == 30774:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 30776 and id_buffer == 30776:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 30800 and id_buffer == 30800:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 30802 and id_buffer == 30802:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 30798 and id_buffer == 30798:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 35457 and id_buffer == 35457:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 35459 and id_buffer == 35459:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 35451 and id_buffer == 35451:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 35455 and id_buffer == 35455:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 35453 and id_buffer == 35453:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 31158 and id_buffer == 31158:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 31160 and id_buffer == 31160:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 31156 and id_buffer == 31156:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 31152 and id_buffer == 31152:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 31150 and id_buffer == 31150:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 31154 and id_buffer == 31154:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 35467 and id_buffer == 35467:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 35465 and id_buffer == 35465:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 35463 and id_buffer == 35463:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    elif npcId == 35461 and id_buffer == 35461:
                            htmltext = "ClanHall-Nothing.htm"
                            st.setState(STARTED)
                            return htmltext
                    else:
                            #st.player.teleToLocation(-84318, 244617, -3725)
                            st.getPlayer().setKarma(1000)
			    htmltext = "ClanHall-No.htm"
			    st.setState(COMPLETED)
                            return htmltext
                else:
                     htmltext = "<html><body>TEXTO SIN TRADUCIR.</body></html>"
                     return htmltext
                      


QUEST=Quest(782,"782_ClanHallNothing","ClanHallNothing")
CREATED=State('Start',QUEST)
STARTED=State('Started',QUEST)
COMPLETED=State('Completed',QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(30784)
STARTED.addTalkId(30784)
QUEST.addStartNpc(30788)
STARTED.addTalkId(30788)
QUEST.addStartNpc(30790)
STARTED.addTalkId(30790)
QUEST.addStartNpc(30786)
STARTED.addTalkId(30786)
QUEST.addStartNpc(30778)
STARTED.addTalkId(30778)
QUEST.addStartNpc(30780)
STARTED.addTalkId(30780)
QUEST.addStartNpc(30782)
STARTED.addTalkId(30782)
QUEST.addStartNpc(30774)
STARTED.addTalkId(30774)
QUEST.addStartNpc(30776)
STARTED.addTalkId(30776)
QUEST.addStartNpc(30800)
STARTED.addTalkId(30800)
QUEST.addStartNpc(30802)
STARTED.addTalkId(30802)
QUEST.addStartNpc(30798)
STARTED.addTalkId(30798)
QUEST.addStartNpc(35457)
STARTED.addTalkId(35457)
QUEST.addStartNpc(35459)
STARTED.addTalkId(35459)
QUEST.addStartNpc(35451)
STARTED.addTalkId(35451)
QUEST.addStartNpc(35455)
STARTED.addTalkId(35455)
QUEST.addStartNpc(35453)
STARTED.addTalkId(35453)
QUEST.addStartNpc(31158)
STARTED.addTalkId(31158)
QUEST.addStartNpc(31160)
STARTED.addTalkId(31160)
QUEST.addStartNpc(31156)
STARTED.addTalkId(31156)
QUEST.addStartNpc(31152)
STARTED.addTalkId(31152)
QUEST.addStartNpc(31150)
STARTED.addTalkId(31150)
QUEST.addStartNpc(31154)
STARTED.addTalkId(31154)
QUEST.addStartNpc(35467)
STARTED.addTalkId(35467)
QUEST.addStartNpc(35465)
STARTED.addTalkId(35465)
QUEST.addStartNpc(35463)
STARTED.addTalkId(35463)
QUEST.addStartNpc(35461)
STARTED.addTalkId(35461)

for npcId in NPC:
 STARTED.addTalkId(npcId)





              
