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

import net.l2emuproject.gameserver.model.L2Skill;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.L2GameClient;

public final class SkillList extends L2GameServerPacket
{
	private static final String	_S__6D_SKILLLIST	= "[S] 58 SkillList";
	
	private final L2Skill[]		_skills;
	//private final L2PcInstance	_activeChar;
	
	public SkillList(L2PcInstance activeChar)
	{
		//_activeChar = activeChar;
		_skills = activeChar.getSortedAllSkills(activeChar.isGM() && !activeChar.isSubClassActive());
	}
	
	@Override
	public void packetSent(L2GameClient client, L2PcInstance activeChar)
	{
		if (activeChar != null)
			activeChar.sendSkillCoolTime();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x5f);
		writeD(_skills.length);
		
		for (L2Skill s : _skills)
		{
			writeD(s.isPassive() ? 1 : 0);
			writeD(s.getLevel());
			writeD(s.getDisplayId());

			// FIXME
			writeC(/*s.isClanSkill() && _activeChar.getClan().getReputationScore() <*/ 0);
			writeC(s.isEnchantable());	
		}
	}
	
	@Override
	public String getType()
	{
		return _S__6D_SKILLLIST;
	}
}
