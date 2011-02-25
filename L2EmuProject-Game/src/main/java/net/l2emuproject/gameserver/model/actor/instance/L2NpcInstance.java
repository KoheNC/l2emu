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

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.datatables.SkillTreeTable;
import net.l2emuproject.gameserver.model.actor.status.CharStatus;
import net.l2emuproject.gameserver.model.actor.status.FolkStatus;
import net.l2emuproject.gameserver.model.base.ClassId;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.AcquireSkillDone;
import net.l2emuproject.gameserver.network.serverpackets.AcquireSkillList;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.L2SkillLearn;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.lang.L2TextBuilder;

public class L2NpcInstance extends L2Npc
{
	public L2NpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setIsInvul(false);
	}
	
	@Override
	protected CharStatus initStatus()
	{
		return new FolkStatus(this);
	}
	
	@Override
	public FolkStatus getStatus()
	{
		return (FolkStatus) _status;
	}

	/**
	 * this displays SkillList to the player.
	 * @param player
	 */
	public void showSkillList(L2PcInstance player, ClassId classId, boolean closable)
	{
		if (_log.isDebugEnabled())
			_log.debug("SkillList activated on: " + getObjectId());

		int npcId = getTemplate().getNpcId();

		if (getTemplate().getTeachInfo() == null)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			L2TextBuilder sb = L2TextBuilder.newInstance();
			sb.append("<html><body>");
			sb.append("I cannot teach you. My class list is empty.<br> Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:"
					+ npcId + ", Your classId:" + player.getClassId().getId() + "<br>");
			sb.append("</body></html>");
			html.setHtml(sb.moveToString());
			player.sendPacket(html);

			return;
		}

		if (!getTemplate().canTeach(classId))
		{
			showNoTeachHtml(player);
			return;
		}

		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player, classId);
		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.SkillType.Usual);
		int counts = 0;

		for (L2SkillLearn s : skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());

			if (sk == null)
				continue;

			int cost = SkillTreeTable.getInstance().getSkillCost(player, sk);
			counts++;

			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
		}

		if (counts == 0)
		{
			int minlevel = SkillTreeTable.getInstance().getMinLevelForNewSkill(player, classId);
			if (minlevel > 0)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_COME_BACK_WHEN_REACHED_S1);
				sm.addNumber(minlevel);
				player.sendPacket(sm);
			}
			else
				player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
            if (closable)
            	player.sendPacket(AcquireSkillDone.PACKET);
		}
		else
			player.sendPacket(asl);

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (player.getPlayerObserver().inObserverMode())
			return;

		if (command.startsWith("SkillList"))
		{
			if (Config.ALT_GAME_SKILL_LEARN)
			{
				String id = command.substring(9).trim();

				if (id.length() != 0)
				{
					player.setSkillLearningClassId(ClassId.values()[Integer.parseInt(id)]);
					showSkillList(player, ClassId.values()[Integer.parseInt(id)], false);
				}
				else
				{
					boolean own_class = false;

					if (getTemplate().getTeachInfo() != null)
					{
						for (ClassId cid : getTemplate().getTeachInfo())
						{
							if (cid.equalsOrChildOf(player.getClassId()))
							{
								own_class = true;
								break;
							}
						}
					}

					String text = "<html><body><center>Skill learning:</center><br>";

					if (!own_class)
					{
						String charType = player.getClassId().isMage() ? "fighter" : "mage";
						text += "Skills of your class are the easiest to learn.<br>\n" + "Skills of another class of your race are a little harder.<br>"
								+ "Skills for classes of another race are extremely difficult.<br>" + "But the hardest of all to learn are the " + charType
								+ " skills!<br>";
					}

					// Make a list of classes
					if (getTemplate().getTeachInfo() != null)
					{
						int count = 0;
						ClassId classCheck = player.getClassId();

						while ((count == 0) && (classCheck != null))
						{
							for (ClassId cid : getTemplate().getTeachInfo())
							{
								if (cid.level() > classCheck.level())
									continue;

								if (SkillTreeTable.getInstance().getAvailableSkills(player, cid).length == 0)
									continue;

								text += "<a action=\"bypass -h npc_%objectId%_SkillList " + cid.getId() + "\">Learn " + cid + "'s class Skills</a><br>\n";
								count++;
							}
							classCheck = classCheck.getParent();
						}
						classCheck = null;
					}
					else
						text += "No Skills.<br>";

					text += "</body></html>";

					insertObjectIdAndShowChatWindow(player, text);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
			else
			{
				player.setSkillLearningClassId(player.getClassId());
				showSkillList(player, player.getClassId(), false);
			}
		}
		else
		{
			// This class dont know any other commands, let forward the command to the parent class
			super.onBypassFeedback(player, command);
		}
	}
}
