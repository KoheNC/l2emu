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
package teleports.HuntingGroundsTeleport;

import org.apache.commons.lang.ArrayUtils;

import net.l2emuproject.gameserver.events.global.sevensigns.SevenSigns;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class HuntingGroundsTeleport extends QuestJython
{
	private static final String	QN			= "HuntingGroundsTeleport";

	private final static int[]	PRIESTS		=
											{
			31078,
			31079,
			31080,
			31081,
			31082,
			31083,
			31084,
			31085,
			31086,
			31087,
			31088,
			31089,
			31090,
			31091,
			31168,
			31169,
			31692,
			31693,
			31694,
			31695,
			31997,
			31998							};

	private static final int[]	DAWN_NPCS	=
											{ 31078, 31079, 31080, 31081, 31082, 31083, 31084, 31168, 31692, 31694, 31997 };

	public HuntingGroundsTeleport(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int id : PRIESTS)
		{
			addStartNpc(id);
			addTalkId(id);
		}
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		final int npcId = npc.getNpcId();
		final int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
		final int playerSeal = SevenSigns.getInstance().getPlayerSeal(player);
		final int sealOwnerGnosis = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_GNOSIS);
		boolean periodValidate = SevenSigns.getInstance().isSealValidationPeriod();

		if (playerCabal == SevenSigns.CABAL_NULL)
		{
			if (ArrayUtils.contains(DAWN_NPCS, npcId))
				htmltext = "dawn_tele-no.htm";
			else
				htmltext = "dusk_tele-no.htm";
		}
		else
		{
			switch (npcId)
			{
				case 31078:
				case 31085:
					if (periodValidate && playerCabal == sealOwnerGnosis && playerSeal == SevenSigns.SEAL_GNOSIS)
						htmltext = "low_gludin.htm";
					else
						htmltext = "hg_gludin.htm";
					break;
				case 31079:
				case 31086:
					if (periodValidate && playerCabal == sealOwnerGnosis && playerSeal == SevenSigns.SEAL_GNOSIS)
						htmltext = "low_gludio.htm";
					else
						htmltext = "hg_gludio.htm";
					break;
				case 31080:
				case 31087:
					if (periodValidate && playerCabal == sealOwnerGnosis && playerSeal == SevenSigns.SEAL_GNOSIS)
						htmltext = "low_dion.htm";
					else
						htmltext = "hg_dion.htm";
					break;
				case 31081:
				case 31088:
					if (periodValidate && playerCabal == sealOwnerGnosis && playerSeal == SevenSigns.SEAL_GNOSIS)
						htmltext = "low_giran.htm";
					else
						htmltext = "hg_giran.htm";
					break;
				case 31082:
				case 31089:
					if (periodValidate && playerCabal == sealOwnerGnosis && playerSeal == SevenSigns.SEAL_GNOSIS)
						htmltext = "low_heine.htm";
					else
						htmltext = "hg_heine.htm";
					break;
				case 31083:
				case 31090:
					if (periodValidate && playerCabal == sealOwnerGnosis && playerSeal == SevenSigns.SEAL_GNOSIS)
						htmltext = "low_oren.htm";
					else
						htmltext = "hg_oren.htm";
					break;
				case 31084:
				case 31091:
					if (periodValidate && playerCabal == sealOwnerGnosis && playerSeal == SevenSigns.SEAL_GNOSIS)
						htmltext = "low_aden.htm";
					else
						htmltext = "hg_aden.htm";
					break;
				case 31168:
				case 31169:
					if (periodValidate && playerCabal == sealOwnerGnosis && playerSeal == SevenSigns.SEAL_GNOSIS)
						htmltext = "low_hw.htm";
					else
						htmltext = "hg_hw.htm";
					break;
				case 31692:
				case 31693:
					if (periodValidate && playerCabal == sealOwnerGnosis && playerSeal == SevenSigns.SEAL_GNOSIS)
						htmltext = "low_goddard.htm";
					else
						htmltext = "hg_goddard.htm";
					break;
				case 31694:
				case 31695:
					if (periodValidate && playerCabal == sealOwnerGnosis && playerSeal == SevenSigns.SEAL_GNOSIS)
						htmltext = "low_rune.htm";
					else
						htmltext = "hg_rune.htm";
					break;
				case 31997:
				case 31998:
					if (periodValidate && playerCabal == sealOwnerGnosis && playerSeal == SevenSigns.SEAL_GNOSIS)
						htmltext = "low_schuttgart.htm";
					else
						htmltext = "hg_schuttgart.htm";
					break;
			}
		}

		st.exitQuest(true);

		return htmltext;
	}

	public static void main(String[] args)
	{
		new HuntingGroundsTeleport(-1, QN, "teleports");
	}
}
