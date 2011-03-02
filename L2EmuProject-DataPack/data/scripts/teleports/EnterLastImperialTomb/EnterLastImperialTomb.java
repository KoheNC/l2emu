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
package teleports.EnterLastImperialTomb;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.manager.lastimperialtomb.LastImperialTombManager;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Psycho(killer1888)
 * 
 * @translated by Intrepid
 */
public final class EnterLastImperialTomb extends QuestJython
{
	private static final String	QN	= "EnterLastImperialTomb";
	private static final int	NPC	= 32011;

	public EnterLastImperialTomb(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(NPC);
		addTalkId(NPC);
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player talker)
	{
		switch (Config.LIT_REGISTRATION_MODE)
		{
			case 0:
			{
				if (LastImperialTombManager.getInstance().tryRegistrationCc(talker))
					LastImperialTombManager.getInstance().registration(talker, npc);
				break;
			}
			case 1:
			{
				if (LastImperialTombManager.getInstance().tryRegistrationPt(talker))
					LastImperialTombManager.getInstance().registration(talker, npc);
				break;
			}
			default:
			{
				if (LastImperialTombManager.getInstance().tryRegistrationPc(talker))
					LastImperialTombManager.getInstance().registration(talker, npc);
				break;
			}
		}
		return "";
	}

	public static void main(String args[])
	{
		new EnterLastImperialTomb(-1, QN, "teleports");
	}
}