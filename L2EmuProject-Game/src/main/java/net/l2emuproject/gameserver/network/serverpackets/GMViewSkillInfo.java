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

import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class GMViewSkillInfo extends L2GameServerPacket
{
	private static final String	_S__91_GMViewSkillInfo	= "[S] 91 GMViewSkillInfo";

	private final L2Player		_player;
	private final L2Skill[]		_skills;

	public GMViewSkillInfo(final L2Player player)
	{
		_player = player;
		_skills = player.getSortedAllSkills(true);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x97);
		writeS(_player.getName());
		writeD(_skills.length);

		boolean isDisabled = false;
		if (_player.getClan() != null)
			isDisabled = _player.getClan().getReputationScore() < 0;

		for (L2Skill skill : _skills)
		{
			writeD(skill.isPassive() ? 1 : 0);
			writeD(skill.getLevel());
			writeD(skill.getId());
			writeC(isDisabled && skill.isClanSkill() ? 1 : 0);
			writeC(skill.isEnchantable());
		}
	}

	@Override
	public final String getType()
	{
		return _S__91_GMViewSkillInfo;
	}
}
