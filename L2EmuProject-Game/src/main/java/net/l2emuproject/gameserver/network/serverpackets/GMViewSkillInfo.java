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

import net.l2emuproject.gameserver.model.L2Clan;
import net.l2emuproject.gameserver.model.L2Skill;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;

public final class GMViewSkillInfo extends L2GameServerPacket
{
	private static final String	_S__91_GMViewSkillInfo	= "[S] 91 GMViewSkillInfo";
	
	private final L2PcInstance	_activeChar;
	private final L2Skill[]		_skills;
	
	public GMViewSkillInfo(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
		_skills = activeChar.getSortedAllSkills(true);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x97);
		writeS(_activeChar.getName());
		writeD(_skills.length);
		
		@SuppressWarnings("unused")
		boolean isDisabled = false;
		L2Clan clan = _activeChar.getClan();
		if (clan != null)
			isDisabled = clan.getReputationScore() < 0;
		
		for (L2Skill skill : _skills)
		{
			writeD(skill.isPassive() ? 1 : 0);
			writeD(skill.getLevel());
			writeD(skill.getDisplayId());
			
			// FIXME
			writeC(/* isDisabled && skill.isClanSkill() ? 1 : */0);
			writeC(skill.isEnchantable());			
		}
	}
	
	@Override
	public String getType()
	{
		return _S__91_GMViewSkillInfo;
	}
}
