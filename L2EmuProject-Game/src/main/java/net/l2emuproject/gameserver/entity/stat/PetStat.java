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
package net.l2emuproject.gameserver.entity.stat;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.PetDataTable;
import net.l2emuproject.gameserver.entity.base.Experience;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SocialAction;
import net.l2emuproject.gameserver.network.serverpackets.StatusUpdate;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.Stats;
import net.l2emuproject.gameserver.world.npc.L2PetData;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.instance.L2PetInstance;

public class PetStat extends SummonStat
{
	// =========================================================
	// Data Field
	
	// =========================================================
	// Constructor
	public PetStat(L2PetInstance activeChar)
	{
		super(activeChar);
	}

	// =========================================================
	// Method - Public
	@Override
	public boolean addExp(long value)
	{
		if (!super.addExp(value))
			return false;

		// PetInfo packet is only for the pet owner
		getActiveChar().broadcastFullInfo();

		return true;
	}

	@Override
	public boolean addExpAndSp(long addToExp, int addToSp)
	{
		if (!super.addExpAndSp(addToExp, addToSp))
			return false;

		SystemMessage sm = new SystemMessage(SystemMessageId.PET_EARNED_S1_EXP);
		sm.addNumber((int)addToExp);
		getActiveChar().getOwner().sendPacket(sm);
		getActiveChar().broadcastFullInfo();

		return true;
	}

	@Override
	public final boolean addLevel(byte value)
	{
		if (getLevel() + value > (Experience.MAX_LEVEL - 1))
			return false;

		boolean levelIncreased = super.addLevel(value);

		// Sync up exp with current level
		if (getExp() > getExpForLevel(getLevel() + 1) || getExp() < getExpForLevel(getLevel()))
			setExp(Experience.LEVEL[getLevel()]);

		if (levelIncreased)
		{
			getActiveChar().getOwner().sendMessage("Your pet has increased it's level.");
			getActiveChar().broadcastPacket(new SocialAction(getActiveChar(), SocialAction.LEVEL_UP));
		}

		StatusUpdate su = new StatusUpdate(getActiveChar().getObjectId());
		su.addAttribute(StatusUpdate.LEVEL, getLevel());
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		getActiveChar().broadcastPacket(su);

		// Send a Server->Client packet PetInfo to the L2Player
		getActiveChar().broadcastFullInfo();
		
		if (getActiveChar().getControlItem() != null)
			getActiveChar().getControlItem().setEnchantLevel(getLevel());

		return levelIncreased;
	}

	@Override
	public final long getExpForLevel(int level)
	{
		L2PetData data = PetDataTable.getInstance().getPetData(getActiveChar().getNpcId(), level);
		if (data != null)
			return data.getPetMaxExp();

		_log.warn("Pet NPC ID "+getActiveChar().getNpcId()+", level "+level+" is missing data from pets_stats table!");
		return 5000000L * level; // temp value calculated from lvl 81 wyvern, 395734658
	}
	
	// =========================================================
	// Method - Private

	// =========================================================
	// Property - Public
	@Override
	public L2PetInstance getActiveChar()
	{
		return (L2PetInstance) _activeChar;
	}

	public final int getFeedBattle()
	{
		return getActiveChar().getPetData().getPetFeedBattle();
	}

	public final int getFeedNormal()
	{
		return getActiveChar().getPetData().getPetFeedNormal();
	}

	@Override
	public void setLevel(byte value)
	{
		getActiveChar().stopFeed();
		super.setLevel(value);

		getActiveChar().setPetData(PetDataTable.getInstance().getPetData(getActiveChar().getTemplate().getNpcId(), getLevel()));
		getActiveChar().startFeed();

		if (getActiveChar().getControlItem() != null)
			getActiveChar().getControlItem().setEnchantLevel(getLevel());
	}

	public final int getMaxFeed()
	{
		return getActiveChar().getPetData().getPetMaxFeed();
	}

	@Override
	public int getMaxHp()
	{
		return (int)calcStat(Stats.MAX_HP, getActiveChar().getPetData().getPetMaxHP(), null, null);
	}
	
	@Override
	public int getMaxMp()
	{
		return (int)calcStat(Stats.MAX_MP, getActiveChar().getPetData().getPetMaxMP(), null, null);
	}
	
	@Override
	public final int getMAtk(final L2Character target, final L2Skill skill)
	{
		double attack = getActiveChar().getPetData().getPetMAtk();

		if (skill != null)
			attack += skill.getPower();
		
		final int val = (int) calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
		
		return (int) (val * Config.ALT_PETS_MAGICAL_DAMAGE_MULTI);
	}

	@Override
	public final int getMDef(final L2Character target, final L2Skill skill)
	{
		double defence = getActiveChar().getPetData().getPetMDef();
		final int val = (int) calcStat(Stats.MAGIC_DEFENCE, defence, target, skill);
		return (int) (val * Config.ALT_PETS_MAGICAL_DEFENSE_MULTI);
	}

	@Override
	public final int getPAtk(final L2Character target)
	{
		final int val = (int) calcStat(Stats.POWER_ATTACK, getActiveChar().getPetData().getPetPAtk(), target, null);
		return (int) (val * Config.ALT_PETS_PHYSICAL_DAMAGE_MULTI);
	}

	@Override
	public final int getPDef(final L2Character target)
	{
		final int val = (int) calcStat(Stats.POWER_DEFENCE, getActiveChar().getPetData().getPetPDef(), target, null);
		return (int) (val * Config.ALT_PETS_PHYSICAL_DEFENSE_MULTI);
	}

	@Override
	public int getAccuracy()
	{
		return (int)calcStat(Stats.ACCURACY_COMBAT, getActiveChar().getPetData().getPetAccuracy(), null, null);
	}

	@Override
	public int getCriticalHit(L2Character target)
	{
		return (int)calcStat(Stats.CRITICAL_RATE, getActiveChar().getPetData().getPetCritical(), target, null);
	}

	@Override
	public int getEvasionRate(L2Character target)
	{
		return (int)calcStat(Stats.EVASION_RATE, getActiveChar().getPetData().getPetEvasion(), target, null);
	}

	public int getRegenHp()
	{
		return (int)calcStat(Stats.REGENERATE_HP_RATE, getActiveChar().getPetData().getPetRegenHP(), null, null);
	}

	public int getRegenMp()
	{
		return (int)calcStat(Stats.REGENERATE_MP_RATE, getActiveChar().getPetData().getPetRegenMP(), null, null);
	}

	@Override
	protected int getBaseRunSpd()
	{
		return getActiveChar().getPetData().getPetSpeed();
	}

	@Override
	public int getWalkSpeed()
	{
		return getRunSpeed() / 2;
	}

	@Override
	public float getMovementSpeedMultiplier()
	{
		float val = getRunSpeed() * 1f / getActiveChar().getPetData().getPetSpeed();
		if (!getActiveChar().isRunning())
			val = val/2;
		return val;
	}

	@Override
	public int getPAtkSpd()
	{
		int val = (int)calcStat(Stats.POWER_ATTACK_SPEED, getActiveChar().getPetData().getPetAtkSpeed(), null, null);
		if (!getActiveChar().isRunning())
			val = val/2;
		return val;
	}

	@Override
	public int getMAtkSpd()
	{
		int val = (int)calcStat(Stats.MAGIC_ATTACK_SPEED, getActiveChar().getPetData().getPetCastSpeed(), null, null);
		if (!getActiveChar().isRunning())
			val = val/2;
		return val;
	}
}
