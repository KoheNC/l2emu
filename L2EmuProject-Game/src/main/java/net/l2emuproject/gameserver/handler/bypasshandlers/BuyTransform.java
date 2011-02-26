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
package net.l2emuproject.gameserver.handler.bypasshandlers;

import net.l2emuproject.gameserver.handler.IBypassHandler;
import net.l2emuproject.gameserver.manager.QuestManager;
import net.l2emuproject.gameserver.model.quest.Quest;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.services.transactions.L2Multisell;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2TransformManagerInstance;

/**
 * @author lord_rex
 */
public final class BuyTransform implements IBypassHandler
{
	private static final String[]	COMMANDS	=
												{ "BuyTransform" };

	private static final String		QN			= "136_MoreThanMeetsTheEye";

	@Override
	public boolean useBypass(String command, L2Player activeChar, L2Character target)
	{
		if (!(target instanceof L2TransformManagerInstance))
			return false;

		QuestState st = activeChar.getQuestState(QN);

		if (st == null)
		{
			Quest quest = QuestManager.getInstance().getQuest(QN);
			st = quest.newQuestState(activeChar);
		}

		if (st.getState() == State.COMPLETED)
			L2Multisell.getInstance().separateAndSend(9006, activeChar, ((L2Npc) target).getNpcId(), false, ((L2Npc) target).getCastle().getTaxRate());
		else
			((L2TransformManagerInstance) target).showChatWindow(activeChar, "data/html/default/32323-6.htm");

		return true;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
