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
package net.l2emuproject.gameserver.handler.itemhandlers;

import net.l2emuproject.gameserver.datatables.SkillTable.SkillInfo;
import net.l2emuproject.gameserver.handler.IItemHandler;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.services.manor.CastleManorService;
import net.l2emuproject.gameserver.services.manor.L2Manor;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.world.mapregion.MapRegionManager;
import net.l2emuproject.gameserver.world.object.L2Boss;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Playable;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2ChestInstance;
import net.l2emuproject.gameserver.world.object.instance.L2MonsterInstance;

/**
 * @author  l3x
 */
public class Seed implements IItemHandler
{
	// All the item IDs that this handler knows.
	private static int[]	ITEM_IDS	=
										{
			5016,
			5017,
			5018,
			5019,
			5020,
			5021,
			5022,
			5023,
			5024,
			5025,
			5026,
			5027,
			5028,
			5029,
			5030,
			5031,
			5032,
			5033,
			5034,
			5035,
			5036,
			5037,
			5038,
			5039,
			5040,
			5041,
			5042,
			5043,
			5044,
			5045,
			5046,
			5047,
			5048,
			5049,
			5050,
			5051,
			5052,
			5053,
			5054,
			5055,
			5056,
			5057,
			5058,
			5059,
			5060,
			5061,
			5221,
			5222,
			5223,
			5224,
			5225,
			5226,
			5227,
			5650,
			5651,
			5652,
			5653,
			5654,
			5655,
			5656,
			5657,
			5658,
			5659,
			5660,
			5661,
			5662,
			5663,
			5664,
			5665,
			5666,
			5667,
			5668,
			5669,
			5670,
			5671,
			5672,
			5673,
			5674,
			5675,
			5676,
			5677,
			5678,
			5679,
			5680,
			5681,
			5682,
			5683,
			5684,
			5685,
			5686,
			5687,
			5688,
			5689,
			5690,
			5691,
			5692,
			5693,
			5694,
			5695,
			5696,
			5697,
			5698,
			5699,
			5700,
			5701,
			5702,
			6727,
			6728,
			6729,
			6730,
			6731,
			6732,
			6733,
			6734,
			6735,
			6736,
			6737,
			6738,
			6739,
			6740,
			6741,
			6742,
			6743,
			6744,
			6745,
			6746,
			6747,
			6748,
			6749,
			6750,
			6751,
			6752,
			6753,
			6754,
			6755,
			6756,
			6757,
			6758,
			6759,
			6760,
			6761,
			6762,
			6763,
			6764,
			6765,
			6766,
			6767,
			6768,
			6769,
			6770,
			6771,
			6772,
			6773,
			6774,
			6775,
			6776,
			6777,
			6778,
			7016,
			7017,
			7018,
			7019,
			7020,
			7021,
			7022,
			7023,
			7024,
			7025,
			7026,
			7027,
			7028,
			7029,
			7030,
			7031,
			7032,
			7033,
			7034,
			7035,
			7036,
			7037,
			7038,
			7039,
			7040,
			7041,
			7042,
			7043,
			7044,
			7045,
			7046,
			7047,
			7048,
			7049,
			7050,
			7051,
			7052,
			7053,
			7054,
			7055,
			7056,
			7057,
			8223,
			8224,
			8225,
			8226,
			8227,
			8228,
			8229,
			8230,
			8231,
			8232,
			8233,
			8234,
			8235,
			8236,
			8237,
			8238,
			8239,
			8240,
			8241,
			8242,
			8243,
			8244,
			8245,
			8246,
			8247,
			8248,
			8249,
			8250,
			8251,
			8252,
			8253,
			8254,
			8255,
			8256,
			8257,
			8258,
			8259,
			8260,
			8261,
			8262,
			8263,
			8264,
			8265,
			8266,
			8267,
			8268,
			8269,
			8270,
			8271,
			8272,
			8521,
			8522,
			8523,
			8524,
			8525,
			8526						};

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2Player))
			return;

		if (CastleManorService.getInstance().isDisabled())
			return;

		L2Player activeChar = (L2Player) playable;

		L2Object obj = activeChar.getTarget();

		if (!(obj instanceof L2MonsterInstance) || (obj instanceof L2ChestInstance) || (obj instanceof L2Boss))
		{
			activeChar.sendPacket(SystemMessageId.THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2MonsterInstance target = (L2MonsterInstance) obj;

		if (target.isDead())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (target.isSeeded())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		int seedId = item.getItemId();

		if (areaValid(MapRegionManager.getInstance().getAreaCastle(activeChar), seedId))
		{
			//FIXME: get right skill level
			target.setSeeded(seedId, activeChar);
			
			for (SkillInfo skillInfo : item.getEtcItem().getSkillInfos())
			{
				L2Skill itemSkill = skillInfo.getSkill();
				if (itemSkill == null)
					continue;
				
				activeChar.useMagic(itemSkill, false, false); // Sowing skill
			}
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.THIS_SEED_MAY_NOT_BE_SOWN_HERE);
		}
	}

	private boolean areaValid(int castleId, int seedId)
	{
		return (L2Manor.getInstance().getCastleIdForSeed(seedId) == castleId);
	}

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
