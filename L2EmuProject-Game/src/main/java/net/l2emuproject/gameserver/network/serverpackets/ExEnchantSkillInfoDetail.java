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

import java.util.List;

import net.l2emuproject.gameserver.datatables.SkillTreeTable;
import net.l2emuproject.gameserver.entity.itemcontainer.PcInventory;
import net.l2emuproject.gameserver.skills.L2EnchantSkillLearn;
import net.l2emuproject.gameserver.skills.L2EnchantSkillLearn.EnchantSkillDetail;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author KenM
 */
public class ExEnchantSkillInfoDetail extends L2GameServerPacket
{
	private static final int	TYPE_NORMAL_ENCHANT		= 0;
	private static final int	TYPE_SAFE_ENCHANT		= 1;
	private static final int	TYPE_UNTRAIN_ENCHANT	= 2;
	private static final int	TYPE_CHANGE_ENCHANT		= 3;

	private int					_bookId					= 0;
	private int					_reqCount				= 0;
	private int					_multi					= 1;
	private final int			_type;
	private final int			_skillId;
	private final int			_skillLevel;
	private final int			_chance;
	private int					_sp;
	private final int			_adenaCount;

	public ExEnchantSkillInfoDetail(int type, int skillid, int skilllvl, L2Player ply)
	{
		L2EnchantSkillLearn enchantLearn = SkillTreeTable.getInstance().getSkillEnchantmentBySkillId(skillid);
		EnchantSkillDetail esd = null;
		// do we have this skill?
		if (enchantLearn != null)
		{
			if (skilllvl > 100)
			{
				int route = (skilllvl / 100) - 1;
				if (skilllvl % 100 == 0)
					esd = enchantLearn.getEnchantRoutes()[route].get(0);
				else if (skilllvl % 100 >= enchantLearn.getEnchantRoutes()[route].size())
					esd = enchantLearn.getEnchantRoutes()[route].get(enchantLearn.getEnchantRoutes()[route].size() - 1);
				else
					esd = enchantLearn.getEnchantSkillDetail(skilllvl);
			}
			else
				// find first enchant route
				for (List<EnchantSkillDetail> list : enchantLearn.getEnchantRoutes())
					if (list != null)
					{
						esd = list.get(0);
						break;
					}
		}

		if (esd == null)
			throw new IllegalArgumentException("Skill " + skillid + " dont have enchant data for level " + skilllvl);

		if (type == 0)
			_multi = SkillTreeTable.NORMAL_ENCHANT_COST_MULTIPLIER;
		else if (type == 1)
			_multi = SkillTreeTable.SAFE_ENCHANT_COST_MULTIPLIER;
		_chance = esd.getRate(ply);
		_sp = esd.getSpCost();
		if (type == TYPE_UNTRAIN_ENCHANT)
			_sp = (int) (0.8 * _sp);
		_adenaCount = esd.getAdenaCost() * _multi;
		_type = type;
		_skillId = skillid;
		_skillLevel = skilllvl;

		switch (type)
		{
			case TYPE_NORMAL_ENCHANT:
				_bookId = SkillTreeTable.NORMAL_ENCHANT_BOOK;
				_reqCount = ((_skillLevel % 100 > 1) ? 0 : 1);
				break;
			case TYPE_SAFE_ENCHANT:
				_bookId = SkillTreeTable.SAFE_ENCHANT_BOOK;
				_reqCount = 1;
				break;
			case TYPE_UNTRAIN_ENCHANT:
				_bookId = SkillTreeTable.UNTRAIN_ENCHANT_BOOK;
				_reqCount = 1;
				break;
			case TYPE_CHANGE_ENCHANT:
				_bookId = SkillTreeTable.CHANGE_ENCHANT_BOOK;
				_reqCount = 1;
				break;
			default:
				return;
		}

	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x5e);

		writeD(_type);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_sp * _multi); // sp
		writeD(_chance); // exp
		writeD(2); // items count?
		writeD(PcInventory.ADENA_ID); // adena
		writeD(_adenaCount); // adena count
		writeD(_bookId); // ItemId Required
		writeD(_reqCount);
	}

	@Override
	public String getType()
	{
		return "[S] FE:5E ExEnchantSkillInfoDetail";
	}
}
