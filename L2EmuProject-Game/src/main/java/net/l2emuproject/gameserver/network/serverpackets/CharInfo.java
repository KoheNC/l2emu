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

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.entity.appearance.PcAppearance;
import net.l2emuproject.gameserver.entity.itemcontainer.PcInventory;
import net.l2emuproject.gameserver.entity.view.DecoyView;
import net.l2emuproject.gameserver.entity.view.PcLikeView;
import net.l2emuproject.gameserver.network.L2GameClient;
import net.l2emuproject.gameserver.skills.AbnormalEffect;
import net.l2emuproject.gameserver.world.object.L2Decoy;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class CharInfo extends L2GameServerPacket
{
	private static final String	_S__31_CHARINFO	= "[S] 31 CharInfo [dddddsddd dddddddddddd dddddddd hhhh d hhhhhhhhhhhh d hhhh hhhhhhhhhhhhhhhh dddddd dddddddd ffff ddd s ddddd ccccccc h c d c h ddd cc d ccc ddddddddddd]";
	
	private final L2Player	_activeChar;
	private final PcLikeView	_view;
	
	public CharInfo(L2Player cha)
	{
		_view = cha.getView();
		_view.refresh();
		
		_activeChar = cha;
	}
	
	public CharInfo(L2Decoy decoy)
	{
		final int idTemplate = decoy.getTemplate().getIdTemplate();
		
		if (idTemplate <= 13070 || 13077 <= idTemplate)
			throw new IllegalArgumentException("Using DecoyInfo packet with an unsupported decoy template");
		
		_view = decoy.getView();
		_view.refresh();
		
		_activeChar = decoy.getOwner();
	}
	
	@Override
	public void packetSent(L2GameClient client, L2Player attacker)
	{
		RelationChanged.sendRelationChanged(_activeChar, attacker);
	}
	
	@Override
	protected void writeImpl()
	{
		final PcAppearance _appearance = _activeChar.getAppearance();
		final PcInventory _inv = _activeChar.getInventory();
		
		writeC(0x31);
		writeD(_view.getX());
		writeD(_view.getY());
		writeD(_view.getZ());
		writeD(_view.getVehicleId()); // airship ID
		writeD(_view.getObjectId());
		writeS(_appearance.getVisibleName());
		writeD(_activeChar.getRace().ordinal());
		writeD(_appearance.getSex() ? 1 : 0);
		
		if (_activeChar.getClassIndex() == 0)
			writeD(_activeChar.getClassId().getId());
		else
			writeD(_activeChar.getBaseClass());
		
		writePaperdollItemDisplayIds(_inv, false);
		writePaperdollAugmentationIds(_inv, false);
		
		writeD(0x00);
		writeD(0x01);
		
		writeD(_activeChar.getPvpFlag());
		writeD(_activeChar.getKarma());
		
		writeD(_view.getMAtkSpd());
		writeD(_view.getPAtkSpd());
		
		writeD(0x00);
		
		writeD(_view.getRunSpd()); // TODO: the order of the speeds should be confirmed
		writeD(_view.getWalkSpd());
		writeD(_view.getSwimRunSpd());
		writeD(_view.getSwimWalkSpd());
		writeD(_view.getFlRunSpd());
		writeD(_view.getFlWalkSpd());
		writeD(_view.getFlyRunSpd()); // fly run speed
		writeD(_view.getFlyWalkSpd()); // fly walk speed
		writeF(_view.getMovementSpeedMultiplier()); // _cha.getProperMultiplier()
		writeF(_view.getAttackSpeedMultiplier()); // _cha.getAttackSpeedMultiplier()
		
		writeF(_view.getCollisionRadius());
		writeF(_view.getCollisionHeight());
		
		writeD(_appearance.getHairStyle());
		writeD(_appearance.getHairColor());
		writeD(_appearance.getFace());
		
		if (_appearance.isInvisible())
		{
			writeS("(Invisible) ", _appearance.getVisibleTitle());
		}
		else
		{
			writeS(_appearance.getVisibleTitle());
		}
		
		if (!_activeChar.isCursedWeaponEquipped())
		{
			writeD(_activeChar.getClanId());
			writeD(_activeChar.getClanCrestId());
			writeD(_activeChar.getAllyId());
			writeD(_activeChar.getAllyCrestId());
		}
		else
		{
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
		}
		
		writeC(_activeChar.isSitting() ? 0 : 1); // standing = 1 sitting = 0
		writeC(_activeChar.isRunning() ? 1 : 0); // running = 1 walking = 0
		writeC(_activeChar.isInCombat() ? 1 : 0);
		writeC(_activeChar.isAlikeDead() ? 1 : 0);
		
		// If the character is invisible, the packet won't be sent
		writeC(0);
		
		writeC(_activeChar.getMountType()); // 1-on Strider, 2-on Wyvern, 3-on Great Wolf, 0-no mount
		writeC(_activeChar.getPrivateStoreType()); // 1 - sellshop
		
		writeH(_activeChar.getCubics().size());
		for (int id : _activeChar.getCubics().keySet())
			writeH(id);
		
		writeC(_activeChar.isLookingForParty());
		
		if (_appearance.isInvisible())
		{
			writeD(_activeChar.getAbnormalEffect() | AbnormalEffect.STEALTH.getMask());
		}
		else
		{
			writeD(_activeChar.getAbnormalEffect());
		}
		
		writeC(_activeChar.isFlying() ? 2 : 0);
		writeH(_activeChar.getEvalPoints()); // Blue value for name (0 = white, 255 = pure blue)
		writeD(_activeChar.getMountNpcId() + 1000000);
		
		writeD(_activeChar.getClassId().getId());
		writeD(0x00); // ?
		writeC(_activeChar.isMounted() || _view.getAirShipHelm() != 0 ? 0 : _activeChar.getEnchantEffect());
		
		if (_activeChar.getTeam() == 1)
			writeC(0x01); // team circle around feet 1= Blue, 2 = red
		else if (_activeChar.getTeam() == 2)
			writeC(0x02); // team circle around feet 1= Blue, 2 = red
		else
			writeC(0x00); // team circle around feet 1= Blue, 2 = red
			
		writeD(_activeChar.getClanCrestLargeId());
		writeC(_activeChar.isNoble() ? 1 : 0); // Symbol on char menu ctrl+I
		writeC((_activeChar.isHero() || (_activeChar.isGM() && Config.GM_HERO_AURA)) ? 1 : 0); // Hero Aura
		
		writeC(_activeChar.getPlayerFish().isFishing() ? 1 : 0); // 0x01: Fishing Mode (Cant be undone by setting back to 0)
		writeD(_activeChar.getPlayerFish().getFishx());
		writeD(_activeChar.getPlayerFish().getFishy());
		writeD(_activeChar.getPlayerFish().getFishz());
		
		writeD(_view.getNameColor());
		
		writeD(_view.getHeading());
		
		writeD(_activeChar.getPledgeClass());
		writeD(_activeChar.getSubPledgeType());
		
		writeD(_view.getTitleColor());
		
		// Doesn't work with Zariche
		writeD(_view.getCursedWeaponLevel());
		
		if (_activeChar.getClan() != null)
			writeD(_activeChar.getClan().getReputationScore());
		else
			writeD(0x00);
		
		// T1
		writeD(_view.getTransformationGraphicalId());
		writeD(_activeChar.getAgathionId());
		
		// T2
		writeD(0x00);
		
		// T2.3
		writeD(_activeChar.getSpecialEffect());
	}
	
	@Override
	public String getType()
	{
		return _S__31_CHARINFO;
	}
	
	@Override
	public boolean canBeSentTo(L2GameClient client, L2Player activeChar)
	{
		if (activeChar == null)
			return false;
		
		if (_view instanceof DecoyView)
		{
			// owner
			// if (_activeChar == activeChar)
			// return true;
		}
		else
		// if (_view instanceof PcView)
		{
			// self
			if (_activeChar == activeChar)
				return false;
		}
		
		if (!activeChar.canSee(_activeChar))
			return false;
		
		return true;
	}
}
