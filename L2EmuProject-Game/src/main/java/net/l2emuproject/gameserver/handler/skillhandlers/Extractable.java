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
package net.l2emuproject.gameserver.handler.skillhandlers;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.ExtractableSkillsData;
import net.l2emuproject.gameserver.datatables.ItemTable;
import net.l2emuproject.gameserver.handler.ISkillHandler;
import net.l2emuproject.gameserver.items.model.L2ExtractableProductItem;
import net.l2emuproject.gameserver.items.model.L2ExtractableSkill;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.itemcontainer.PcInventory;
import net.l2emuproject.gameserver.model.skill.L2Skill;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.tools.random.Rnd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class Extractable implements ISkillHandler
{
	protected static Log	_log						= LogFactory.getLog(Extractable.class);

	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.EXTRACTABLE
	};

	/**
	 * 
	 * @see net.l2emuproject.gameserver.handler.ISkillHandler#useSkill(net.l2emuproject.gameserver.model.actor.L2Character, net.l2emuproject.gameserver.model.skill.L2Skill, net.l2emuproject.gameserver.model.actor.L2Character...)
	 */
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;

		L2PcInstance player = (L2PcInstance)activeChar;
		int itemID = skill.getReferenceItemId();
		if (itemID == 0)
			return;

		L2ExtractableSkill exitem = ExtractableSkillsData.getInstance().getExtractableItem(skill);

		if (exitem == null)
			return;

		int rndNum = Rnd.get(100), chanceFrom = 0;
		int[] createItemID = new int[20];
		int[] createAmount = new int[20];

		// calculate extraction
		for (L2ExtractableProductItem expi : exitem.getProductItemsArray())
		{
			int chance = expi.getChance();

			if (rndNum >= chanceFrom && rndNum <= chance + chanceFrom)
			{
				for (int i = 0; i < expi.getId().length; i++)
				{
					createItemID[i] = expi.getId()[i];

					if ((itemID >= 6411 && itemID <= 6518) || (itemID >= 7726 && itemID <= 7860) || (itemID >= 8403 && itemID <= 8483))
						createAmount[i] = (expi.getAmmount()[i] * Config.RATE_EXTR_FISH);
					else
						createAmount[i] = expi.getAmmount()[i];
				}
				break;
			}

			chanceFrom += chance;
		}
		if (player.isSubClassActive() && skill.getReuseDelay() > 0)
		{
			// TODO: remove this once skill reuse will be global for main/subclass
			player.sendPacket(SystemMessageId.MAIN_CLASS_SKILL_ONLY);
			player.sendPacket(new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
			return;
		}
		if (createItemID[0] <= 0 || createItemID.length == 0 )
		{
			player.sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);
			return;
		}
		else
		{
			for (int i = 0; i < createItemID.length; i++)
			{
				if (createItemID[i] <= 0)
					return;

				if (ItemTable.getInstance().getTemplate(createItemID[i]) == null)
				{
					_log.warn("createItemID " + createItemID[i] + " doesn't have template!");
					player.sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);
					return;
				}

				if (ItemTable.getInstance().getTemplate(createItemID[i]).isStackable())
					player.addItem("Extract", createItemID[i], createAmount[i], targets[0], false);
				else
				{
					for (int j = 0; j < createAmount[i]; j++)
						player.addItem("Extract", createItemID[i], 1, targets[0], false);
				}

				if (createItemID[i] == PcInventory.ADENA_ID)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S1_ADENA);
					sm.addNumber(createAmount[i]);
					player.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
					sm.addItemName(createItemID[i]);
					sm.addNumber(createAmount[i]);
					player.sendPacket(sm);
				}
			}
		}
	}

	/**
	 * 
	 * @see net.l2emuproject.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
