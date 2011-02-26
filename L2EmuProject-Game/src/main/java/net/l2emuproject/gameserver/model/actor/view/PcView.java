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
package net.l2emuproject.gameserver.model.actor.view;

import net.l2emuproject.gameserver.datatables.NpcTable;
import net.l2emuproject.gameserver.entity.appearance.PcAppearance;
import net.l2emuproject.gameserver.model.actor.stat.PcStat;
import net.l2emuproject.gameserver.services.cursedweapons.CursedWeaponsService;
import net.l2emuproject.gameserver.services.transformation.L2Transformation;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.templates.chars.L2PcTemplate;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.position.ObjectPosition;

/**
 * @author NB4L1
 */
public final class PcView extends CharView<L2Player> implements UniversalCharView
{
	public PcView(L2Player activeChar)
	{
		super(activeChar);
	}
	
	@Override
	protected void refreshImpl()
	{
		super.refreshImpl();
		
		final L2Player cha = _activeChar;
		final ObjectPosition position = cha.getPosition();
		final PcAppearance appearance = cha.getAppearance();
		final PcStat stat = cha.getStat();
		final L2Transformation transformation = cha.getPlayerTransformation().getTransformation();
		
		_objectId = cha.getObjectId();
		
		if (_activeChar.getVehicle() != null && _activeChar.getInVehiclePosition() != null)
		{
			_x = _activeChar.getInVehiclePosition().getX();
			_y = _activeChar.getInVehiclePosition().getY();
			_z = _activeChar.getInVehiclePosition().getZ();
			_vehicleId = _activeChar.getVehicle().getObjectId();
			if (_activeChar.isInAirShip() && _activeChar.getAirShip().isCaptain(_activeChar))
				_airShipHelm = _activeChar.getAirShip().getHelmItemId();
			else
				_airShipHelm = 0;
		}
		else
		{
			_x = position.getX();
			_y = position.getY();
			_z = position.getZ();	
			_vehicleId = 0;
			_airShipHelm = 0;
		}
		
		_heading = position.getHeading();
		
		_movementSpeedMultiplier = stat.getMovementSpeedMultiplier();
		_attackSpeedMultiplier = stat.getAttackSpeedMultiplier();
		
		if (cha.getMountType() != 0)
		{
			final L2NpcTemplate template = NpcTable.getInstance().getTemplate(cha.getMountNpcId());
			
			_collisionRadius = template.getCollisionRadius();
			_collisionHeight = template.getCollisionHeight();
		}
		else if (transformation != null && !transformation.isStance())
		{
			_collisionRadius = transformation.getCollisionRadius(cha);
			_collisionHeight = transformation.getCollisionHeight(cha);
		}
		else
		{
			final L2PcTemplate template = cha.getBaseTemplate();
			
			if (appearance.getSex())
			{
				_collisionRadius = template.getFCollisionRadius();
				_collisionHeight = template.getFCollisionHeight();
			}
			else
			{
				_collisionRadius = template.getdCollisionRadius();
				_collisionHeight = template.getdCollisionHeight();
			}
		}
		
		_runSpd = (int)(stat.getRunSpeed() / _movementSpeedMultiplier);
		_walkSpd = (int)(stat.getWalkSpeed() / _movementSpeedMultiplier);
		
		_pAtk = stat.getPAtk(null);
		_pDef = stat.getPDef(null);
		_pAtkSpd = stat.getPAtkSpd();
		
		_mAtk = stat.getMAtk(null, null);
		_mDef = stat.getMDef(null, null);
		_mAtkSpd = stat.getMAtkSpd();
		
		_accuracy = stat.getAccuracy();
		_evasionRate = stat.getEvasionRate(null);
		_criticalHit = stat.getCriticalHit(null);
		
		if (cha.isCursedWeaponEquipped())
			_cursedWeaponLevel = CursedWeaponsService.getInstance().getLevel(cha.getCursedWeaponEquippedId());
		else
			_cursedWeaponLevel = 0;
		
		if (transformation != null)
			_transformationGraphicalId = transformation.getGraphicalId();
		else
			_transformationGraphicalId = 0;
		
		_nameColor = appearance.getNameColor();
		_titleColor = appearance.getTitleColor();
	}
	
	private int _objectId;
	private int _x;
	private int _y;
	private int _z;
	private int _heading;
	
	private int _vehicleId; 
	private int _airShipHelm;
	
	private float _movementSpeedMultiplier;
	private float _attackSpeedMultiplier;
	
	private double _collisionRadius;
	private double _collisionHeight;
	
	private int _runSpd;
	private int _walkSpd;
	
	private int _pAtk;
	private int _pDef;
	private int _pAtkSpd;
	
	private int _mAtk;
	private int _mDef;
	private int _mAtkSpd;
	
	private int _accuracy;
	private int _evasionRate;
	private int _criticalHit;
	
	private int _cursedWeaponLevel;
	private int _transformationGraphicalId;
	
	private int _nameColor;
	private int _titleColor;
	
	@Override
	public int getObjectId()
	{
		return _objectId;
	}
	
	@Override
	public int getX()
	{
		return _x;
	}
	
	@Override
	public int getY()
	{
		return _y;
	}
	
	@Override
	public int getZ()
	{
		return _z;
	}
	
	@Override
	public int getHeading()
	{
		return _heading;
	}
	
	@Override
	public int getVehicleId()
	{
		return _vehicleId;
	}
	
	@Override
	public int getAirShipHelm()
	{
		return _airShipHelm;
	}
	
	@Override
	public float getMovementSpeedMultiplier()
	{
		return _movementSpeedMultiplier;
	}
	
	@Override
	public float getAttackSpeedMultiplier()
	{
		return _attackSpeedMultiplier;
	}
	
	@Override
	public double getCollisionRadius()
	{
		return _collisionRadius;
	}
	
	@Override
	public double getCollisionHeight()
	{
		return _collisionHeight;
	}
	
	@Override
	public int getRunSpd()
	{
		return _runSpd;
	}
	
	@Override
	public int getWalkSpd()
	{
		return _walkSpd;
	}
	
	@Override
	public int getSwimRunSpd()
	{
		return getRunSpd();
	}
	
	@Override
	public int getSwimWalkSpd()
	{
		return getWalkSpd();
	}
	
	@Override
	public int getFlRunSpd()
	{
		return getRunSpd();
	}
	
	@Override
	public int getFlWalkSpd()
	{
		return getWalkSpd();
	}
	
	@Override
	public int getFlyRunSpd()
	{
		return getRunSpd();
	}
	
	@Override
	public int getFlyWalkSpd()
	{
		return getWalkSpd();
	}
	
	// ---
	
	public int getPAtk()
	{
		return _pAtk;
	}
	
	public int getPDef()
	{
		return _pDef;
	}
	
	@Override
	public int getPAtkSpd()
	{
		return _pAtkSpd;
	}
	
	public int getMAtk()
	{
		return _mAtk;
	}
	
	public int getMDef()
	{
		return _mDef;
	}
	
	@Override
	public int getMAtkSpd()
	{
		return _mAtkSpd;
	}
	
	public int getAccuracy()
	{
		return _accuracy;
	}
	
	public int getEvasionRate()
	{
		return _evasionRate;
	}
	
	public int getCriticalHit()
	{
		return _criticalHit;
	}
	
	// ---
	
	@Override
	public int getCursedWeaponLevel()
	{
		return _cursedWeaponLevel;
	}
	
	@Override
	public int getTransformationGraphicalId()
	{
		return _transformationGraphicalId;
	}
	
	@Override
	public int getNameColor()
	{
		return _nameColor;
	}
	
	@Override
	public int getTitleColor()
	{
		return _titleColor;
	}
}
