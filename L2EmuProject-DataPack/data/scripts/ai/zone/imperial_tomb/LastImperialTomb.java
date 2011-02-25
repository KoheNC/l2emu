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
package ai.zone.imperial_tomb;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.instancemanager.lastimperialtomb.LastImperialTombManager;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author L0ngh0rn
 * @since 2011-02-15
 */
public final class LastImperialTomb extends QuestJython
{
	public static final String	QN				= "LastImperialTomb";

	// NPCs
	private static final int	GUIDE			= 32011;
	private static final int	ALARM_DEVICE	= 18328;
	private static final int	CHOIR_PRAYER	= 18339;
	private static final int	CHOIR_CAPTAIN	= 18334;
	private static final int	FRINTEZZA		= 29045;

	public LastImperialTomb(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		addStartNpc(GUIDE);
		addTalkId(GUIDE);

		addKillId(ALARM_DEVICE);
		addKillId(CHOIR_PRAYER);
		addKillId(CHOIR_CAPTAIN);

		addFirstTalkId(FRINTEZZA);
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (npc.getNpcId() == GUIDE)
		{
			if (player.isFlying())
				htmltext = "<html><body>Imperial Tomb Guide:<br>To enter, get off the wyvern.</body></html>";
			else
			{
				switch (Config.LIT_REGISTRATION_MODE)
				{
					case 0:
						if (LastImperialTombManager.getInstance().tryRegistrationCc(player))
							LastImperialTombManager.getInstance().registration(player, npc);
						break;
					case 1:
						if (LastImperialTombManager.getInstance().tryRegistrationPt(player))
							LastImperialTombManager.getInstance().registration(player, npc);
						break;
					case 2:
						if (LastImperialTombManager.getInstance().tryRegistrationPc(player))
							LastImperialTombManager.getInstance().registration(player, npc);
						break;
				}
			}
		}

		return htmltext;
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2Player player)
	{
		return null;
	}

	@Override
	public final String onKill(L2Npc npc, L2Player killer, boolean isPet)
	{
		switch (npc.getNpcId())
		{
			case ALARM_DEVICE:
				LastImperialTombManager.getInstance().onKillHallAlarmDevice();
				break;
			case CHOIR_PRAYER:
				LastImperialTombManager.getInstance().onKillDarkChoirPlayer();
				break;
			case CHOIR_CAPTAIN:
				LastImperialTombManager.getInstance().onKillDarkChoirCaptain();
				break;
		}
		return null;
	}

	public static void main(String[] args)
	{
		new LastImperialTomb(-1, QN, "Last Imperial Tomb", "ai");
	}
}