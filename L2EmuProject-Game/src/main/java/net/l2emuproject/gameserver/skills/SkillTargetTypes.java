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
package net.l2emuproject.gameserver.skills;

/**
 * @author Intrepid
 *
 */
public enum SkillTargetTypes
{
	TARGET_NONE,
	TARGET_SELF,
	TARGET_ONE,
	TARGET_PET,
	TARGET_SUMMON,
	TARGET_PARTY,
	TARGET_PARTY_CLAN,
	TARGET_ALLY,
	TARGET_CLAN,
	TARGET_AREA,
	TARGET_FRONT_AREA,
	TARGET_BEHIND_AREA,
	TARGET_AURA,
	TARGET_FRONT_AURA,
	TARGET_BEHIND_AURA,
	TARGET_SERVITOR_AURA,
	TARGET_CORPSE,
	TARGET_CORPSE_ALLY,
	TARGET_CORPSE_CLAN,
	TARGET_CORPSE_PLAYER,
	TARGET_CORPSE_PET,
	TARGET_AREA_CORPSE_MOB,
	TARGET_CORPSE_MOB,
	TARGET_AREA_CORPSES,
	TARGET_MULTIFACE,
	TARGET_AREA_UNDEAD,
	TARGET_UNLOCKABLE,
	TARGET_HOLY,
	TARGET_FLAGPOLE,
	TARGET_PARTY_MEMBER,
	TARGET_PARTY_OTHER,
	TARGET_ENEMY_SUMMON,
	TARGET_OWNER_PET,
	TARGET_ENEMY_ALLY,
	TARGET_ENEMY_PET,
	TARGET_GATE,
	TARGET_COUPLE,
	TARGET_MOB,
	TARGET_AREA_MOB,
	TARGET_KNOWNLIST,
	TARGET_GROUND,
	TARGET_INITIATOR,
	TARGET_PARTY_NOTME,
	
	// TODO: Implement more target types
	;
	
	private static final SkillTargetTypes[] VALUES = SkillTargetTypes.values();
	
	public static SkillTargetTypes getChatType(int targetTypeId)
	{
		return VALUES[targetTypeId];
	}
}
