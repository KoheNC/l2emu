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
package net.l2emuproject.gameserver.model.entity.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import net.l2emuproject.L2DatabaseFactory;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExBasicActionList;
import net.l2emuproject.gameserver.services.transformation.L2Transformation;
import net.l2emuproject.gameserver.skills.L2Effect;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.templates.skills.L2EffectType;
import net.l2emuproject.util.SingletonList;

public final class PlayerTransformation extends PlayerExtension
{
	private static final String	SELECT_CHAR_TRANSFORM	= "SELECT transform_id FROM characters WHERE charId=?";
	private static final String	UPDATE_CHAR_TRANSFORM	= "UPDATE characters SET transform_id=? WHERE charId=?";

	private L2Transformation	_transformation;
	private int					_transformationId		= 0;

	private final List<Integer>	_transformAllowedSkills	= new SingletonList<Integer>();

	public PlayerTransformation(L2PcInstance activeChar)
	{
		super(activeChar);
	}

	public final boolean isTransformationDisabledSkill(L2Skill skill)
	{
		if (_transformation != null && !containsAllowedTransformSkill(skill.getId()) && !skill.allowOnTransform())
			return true;

		return false;
	}

	public final boolean isTransformed()
	{
		return _transformation != null && !_transformation.isStance();
	}

	public final boolean isInStance()
	{
		return _transformation != null && _transformation.isStance();
	}

	public final void transform(L2Transformation transformation)
	{
		if (_transformation != null)
		{
			// You already polymorphed and cannot polymorph again.
			getPlayer().sendPacket(SystemMessageId.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
			return;
		}
		if (getPlayer().isMounted())
		{
			// Get off the strider or something else if character is mounted
			getPlayer().dismount();
		}
		if (getPlayer().getPet() != null)
		{
			// Unsummon pets
			getPlayer().getPet().unSummon(getPlayer());
		}
		_transformation = transformation;
		for (L2Effect e : getPlayer().getAllEffects())
		{
			if (e != null && e.getSkill().isToggle())
				e.exit();
		}
		transformation.onTransform(getPlayer());
		getPlayer().sendSkillList();
		getPlayer().sendSkillCoolTime();
		ExBasicActionList.sendTo(getPlayer());
		getPlayer().broadcastUserInfo();
	}

	public final void untransform()
	{
		if (_transformation != null)
		{
			_transformAllowedSkills.clear();
			_transformation.onUntransform(getPlayer());
			_transformation = null;
			getPlayer().stopEffects(L2EffectType.TRANSFORMATION);
			getPlayer().broadcastUserInfo();
			ExBasicActionList.sendTo(getPlayer());
			getPlayer().sendSkillList();
			getPlayer().sendSkillCoolTime();
		}
	}

	public final L2Transformation getTransformation()
	{
		return _transformation;
	}

	/**
	 * This returns the transformation Id of the current transformation.
	 * For example, if a player is transformed as a Buffalo, and then picks up the Zariche,
	 * the transform Id returned will be that of the Zariche, and NOT the Buffalo.
	 * @return Transformation Id
	 */
	public final int getTransformationId()
	{
		L2Transformation transformation = getTransformation();
		if (transformation == null)
			return 0;
		return transformation.getId();
	}

	/**
	 * This returns the transformation Id stored inside the character table, selected by the method: transformSelectInfo()
	 * For example, if a player is transformed as a Buffalo, and then picks up the Zariche,
	 * the transform Id returned will be that of the Buffalo, and NOT the Zariche.
	 * @return Transformation Id
	 */
	public final int transformId()
	{
		return _transformationId;
	}

	/**
	 * This is a simple query that inserts the transform Id into the character table for future reference.
	 */
	public final void transformInsertInfo()
	{

		if (getTransformationId() == L2Transformation.TRANSFORM_AKAMANAH || getTransformationId() == L2Transformation.TRANSFORM_ZARICHE)
			return;

		_transformationId = getTransformationId();

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(UPDATE_CHAR_TRANSFORM);
			statement.setInt(1, _transformationId);
			statement.setInt(2, getPlayer().getObjectId());

			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.fatal("Transformation insert info: " + e, e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * This selects the current
	 * @return transformation Id
	 */
	public final int transformSelectInfo()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(SELECT_CHAR_TRANSFORM);
			statement.setInt(1, getPlayer().getObjectId());

			ResultSet rset = statement.executeQuery();
			if (rset.next())
				_transformationId = rset.getInt("transform_id");

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.fatal("Transformation select info error:" + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		return _transformationId;
	}

	public final void setTransformAllowedSkills(int[] ids)
	{
		_transformAllowedSkills.clear();
		for (int id : ids)
			addTransformAllowedSkill(id);
	}

	public final void addTransformAllowedSkill(int[] ids)
	{
		for (int id : ids)
			addTransformAllowedSkill(id);
	}

	public final void addTransformAllowedSkill(int id)
	{
		_transformAllowedSkills.add(id);
	}

	public final boolean containsAllowedTransformSkill(int id)
	{
		return _transformAllowedSkills.contains(id);
	}
}
