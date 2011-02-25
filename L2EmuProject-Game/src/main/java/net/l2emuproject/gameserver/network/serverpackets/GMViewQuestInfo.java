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
package net.l2emuproject.gameserver.network.serverpackets;

import net.l2emuproject.gameserver.model.quest.Quest;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * Sh (dd) h (dddd)
 * @author Tempy
 */
public class GMViewQuestInfo extends L2GameServerPacket
{
	private static final String S_99_GMVIEWQUESTINFO = "[S] 99 GMViewQuestInfo";

    private final L2Player _activeChar;

	public GMViewQuestInfo(L2Player cha)
	{
		_activeChar = cha;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x99);
		writeS(_activeChar.getName());

        Quest[] questList = _activeChar.getAllActiveQuests();

        if (questList.length == 0)
        {
            writeC(0);
            writeH(0);
            writeH(0);
            return;
        }

        writeH(questList.length); // quest count

        for (Quest q : questList)
        {
            writeD(q.getQuestIntId());

            QuestState qs = _activeChar.getQuestState(q.getName());

            if (qs == null)
            {
                writeD(0);
                continue;
            }

            writeD(qs.getInt(Quest.CONDITION));   // stage of quest progress
        }
	}

	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return S_99_GMVIEWQUESTINFO;
	}
}
