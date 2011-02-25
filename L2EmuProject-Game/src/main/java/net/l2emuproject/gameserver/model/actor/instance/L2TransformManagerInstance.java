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
package net.l2emuproject.gameserver.model.actor.instance;

import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.datatables.SkillTreeTable;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.AcquireSkillDone;
import net.l2emuproject.gameserver.network.serverpackets.AcquireSkillList;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.transformation.L2TransformSkillLearn;
import net.l2emuproject.gameserver.skills.L2CertificationSkillsLearn;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Player;

public class L2TransformManagerInstance extends L2MerchantInstance
{
    /**
	 * @param objectId
	 * @param template
	 */
	public L2TransformManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";

		if (val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;

		return "data/html/default/" + pom + ".htm";
	}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		if (command.startsWith("TransformSkillList"))
		{
			player.setSkillLearningClassId(player.getClassId());
			showTransformSkillList(player, false);
		}
		else if (command.startsWith("CertificationSkills"))
		{
			if (!player.isSubClassActive())
			{
				QuestState qs = player.getQuestState("136_MoreThanMeetsTheEye");
				if (qs == null || !qs.isCompleted())
				{
					player.sendMessage("You must have completed the More than meets the eye quest for receiving these special skills.");
					return;
				}
				
				player.setSkillLearningClassId(player.getClassId());
				showCertificationSkillsList(player, false);
			}
			else
			{
				showChatWindow(player,"data/html/default/32323-10.htm");
				return;
			}
		}
		else if (command.startsWith("DeleteCertifications"))
		{
			if (!player.isSubClassActive())
			{
				int subclassItemIds[] = {10280,10281,10282,10283,10284,10285,10286,10287,10288,10289,10290,10291,10292,10293,10294,10612};
				
				if (player.reduceAdena("Subclass Certification Removal", 10000000, player, true))
				{
					for (L2Skill skill : player.getAllSkills())
					{
						if (L2CertificationSkillsLearn.isCertificationSkill(skill.getId()))
							player.removeSkill(skill);
					}
					
					for (int itemId : subclassItemIds)
					{
						L2ItemInstance item = player.getInventory().getItemByItemId(itemId);
						if (item != null)
							player.destroyItemByItemId("Subclass Certification Removal", itemId, item.getCount(), player, true);

						item = player.getWarehouse().getItemByItemId(itemId);
						if (item != null)
							player.getWarehouse().destroyItemByItemId("Subclass Certification Removal", itemId, item.getCount(), player, null);
					}
					player.getPlayerCertification().deleteSubclassCertifications();

					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setHtml("<html><body>Ok, your certifications have been removed!</body></html>");
					player.sendPacket(html);
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setHtml("<html><body>Subclass certification removal costs 10'000'000 Adena.</body></html>");
					player.sendPacket(html);
				}
			}
			else
			{
				showChatWindow(player,"data/html/default/32323-13.htm");
				return;
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	/**
	 * this displays TransformationSkillList to the player.
	 * @param player
	 */
	public void showTransformSkillList(L2Player player, boolean closable)
	{
		L2TransformSkillLearn[] skills = SkillTreeTable.getInstance().getAvailableTransformSkills(player);
		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.SkillType.Usual);
		int counts = 0;

		for (L2TransformSkillLearn s: skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if (sk == null)
				continue;

			counts++;

			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getSpCost(), 0);
		}

		if (counts == 0)
		{
			int minlevel = SkillTreeTable.getInstance().getMinLevelForNewTransformSkill(player);

			if (minlevel > 0)
			{
				// No more skills to learn, come back when you level.
				SystemMessage sm = new SystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_COME_BACK_WHEN_REACHED_S1);
				sm.addNumber(minlevel);
				player.sendPacket(sm);
			}
			else
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setHtml("<html><body>You've learned all skills.<br></body></html>");
				player.sendPacket(html);
			}
            if (closable)
            	player.sendPacket(AcquireSkillDone.PACKET);
		}
		else
		{
			player.sendPacket(asl);
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void showCertificationSkillsList(L2Player player, boolean closable)
	{
		if (player.getPlayerTransformation().isTransformed())
			return;

		L2CertificationSkillsLearn[] skills = SkillTreeTable.getInstance().getAvailableCertificationSkills(player);
		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.SkillType.Usual);
		int counts = 0;

		for (L2CertificationSkillsLearn s: skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if (sk == null)
				continue;

			counts++;

			asl.addSkill(s.getId(), s.getLevel(), 1, 0, 0);
		}

		if (counts == 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setHtml("<html><body>You've learned all skills.<br></body></html>");
			player.sendPacket(html);
            if (closable)
            	player.sendPacket(AcquireSkillDone.PACKET);
		}
		else
		{
			player.sendPacket(asl);
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void showHtmlFile(L2Player player, String file)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile("data/html/default/" + file);
		player.sendPacket(html);
	}

	public boolean testQuestTransformation(L2Player player)
	{
		if (player == null)
			return false;

		String _questName = "136_MoreThanMeetsTheEye";
		QuestState qs = player.getQuestState(_questName);
		if (qs == null)
			return false;
		return State.getStateName(qs.getState()) == "Completed";
	}
}
