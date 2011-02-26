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
import net.l2emuproject.gameserver.datatables.ClanTable;
import net.l2emuproject.gameserver.manager.TownManager;
import net.l2emuproject.gameserver.model.clan.L2Clan;
import net.l2emuproject.gameserver.model.itemcontainer.Inventory;
import net.l2emuproject.gameserver.network.L2GameClient;
import net.l2emuproject.gameserver.skills.AbnormalEffect;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.L2Summon;
import net.l2emuproject.gameserver.world.object.L2Trap;
import net.l2emuproject.gameserver.world.object.instance.L2MonsterInstance;
import net.l2emuproject.gameserver.world.object.instance.L2NpcInstance;
import net.l2emuproject.gameserver.world.zone.L2Zone;

public abstract class AbstractNpcInfo extends L2GameServerPacket
{
	private static final String	_S__22_NPCINFO	= "[S] 0c NpcInfo";
	
	protected int				_x, _y, _z, _heading;
	protected int				_idTemplate;
	protected boolean			_isSummoned;
	protected int				_mAtkSpd, _pAtkSpd;
	
	/**
	 * Run speed, swimming run speed and flying run speed
	 */
	protected int				_runSpd;
	
	/**
	 * Walking speed, swimming walking speed and flying walking speed
	 */
	protected int				_walkSpd;
	
	protected int				_rhand, _lhand, _chest, _enchantEffect;
	protected double			_collisionHeight, _collisionRadius;
	protected String			_name			= "";
	protected String			_title			= "";
	
	protected int lrhand, enchlvl, armor, head, boot, pant, glove, charrace, charhair, charface,
	charhaircolor, charcolor, charclass, charhero, charsex, dhair, hair, face, augmentation;
	protected boolean isChar;
	
	public AbstractNpcInfo(L2Character cha)
	{
		_isSummoned = cha.isShowSummonAnimation();
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_heading = cha.getHeading();
		_mAtkSpd = cha.getMAtkSpd();
		_pAtkSpd = cha.getPAtkSpd();
		_runSpd = cha.getTemplate().getBaseRunSpd();
		_walkSpd = cha.getTemplate().getBaseWalkSpd();
	}
	
	@Override
	public String getType()
	{
		return _S__22_NPCINFO;
	}
	
	public static class NpcInfo extends AbstractNpcInfo
	{
		private final L2Npc	_npc;
		private int			_clanCrest	= 0;
		private int			_allyCrest	= 0;
		private int			_allyId		= 0;
		private int			_clanId		= 0;
		private int         _displayEffect = 0;
		
		public NpcInfo(L2Npc cha)
		{
			super(cha);
			_npc = cha;
			_idTemplate = cha.getTemplate().getIdTemplate(); // On every subclass
			_rhand = cha.getRightHandItem(); // On every subclass
			_lhand = cha.getLeftHandItem(); // On every subclass
			_collisionHeight = cha.getCollisionHeight(); // On every subclass
			_collisionRadius = cha.getCollisionRadius(); // On every subclass
			
			if (cha.getTemplate().isServerSideName())
				_name = cha.getTemplate().getName(); // On every subclass
				
			if (cha.isChampion())
			{
				_title = Config.CHAMPION_TITLE; // On every subclass
			}
			else if (cha.getTemplate().isServerSideTitle())
			{
				_title = cha.getTemplate().getTitle(); // On every subclass
			}
			else
			{
				_title = cha.getTitle(); // On every subclass
			}

			if (Config.SHOW_MONSTER_LVL && _npc instanceof L2MonsterInstance)
			{
				String t = "Lvl " + cha.getLevel();
				if (cha.getAggroRange() > 0)
				{
					if (!cha.isChampion() || (cha.isChampion() && !Config.CHAMPION_PASSIVE))
						t += "*";
				}
				
				if (_title != null && !_title.isEmpty())
					t += " " + _title;
				
				_title = t;
			}
			
			// npc crest of owning clan/ally of castle
			if (cha instanceof L2NpcInstance && cha.isInsideZone(L2Zone.FLAG_TOWN) 
					/*&& (Config.ALT_SHOW_CREST_WITHOUT_QUEST || cha.getCastle().isCrestVisible())*/
					&& cha.getCastle().getOwnerId() != 0)
			{
				int townId = TownManager.getInstance().getTown(_x, _y, _z).getTownId();
				if (townId != 33 && townId != 22)
				{
					L2Clan clan = ClanTable.getInstance().getClan(cha.getCastle().getOwnerId());
					_clanCrest = clan.getCrestId();
					_clanId = clan.getClanId();
					_allyCrest = clan.getAllyCrestId();
					_allyId = clan.getAllyId();
				}
			}
			
			if (cha.getIsChar() > 0)
			{
				isChar = true;
				armor = cha.getArmors();
				pant = cha.getPant();
				head = cha.getHead();
				boot = cha.getBoot();
				glove = cha.getGlove();
				face = cha.getFace();
				dhair = cha.getDHair();
				hair = cha.getHair();
				lrhand = cha.getLrhand();
				charrace = cha.getCharRace();
				charclass = cha.getCharClass();
				charface = cha.getCharFace();
				charhaircolor = cha.getCharHairColor();
				charcolor = Integer.decode("0x" + cha.getCharColor());
				charhair = cha.getCharHair();
				charhero = cha.getCharHero();
				charsex = cha.getCharSex();
				augmentation = cha.getAugmentation();

				if (cha.getEnchLvl() > 0)				
					enchlvl = Math.min(127, cha.getEnchLvl());			
				else
					enchlvl = 0;
			}
			
			_displayEffect = cha.getDisplayEffect();
		}
		
		@Override
		protected void writeImpl(L2GameClient client, L2Player activeChar)
		{
			if (isChar)
			{
				writeC(0x31);
				writeD(_x);
				writeD(_y);
				writeD(_z);
				writeD(0x00);
				writeD(_npc.getObjectId());
				writeS(_npc.getTemplate().getName());
				writeD(charrace);
				writeD(charsex);
				writeD(charclass);
				writeD(dhair);
				writeD(head);
				writeD(_rhand);
				writeD(_lhand);
				writeD(glove);
				writeD(armor);
				writeD(pant);
				writeD(boot);
				writeD(0);
				writeD(lrhand);
				writeD(hair);
				writeD(face);
				// T1 new d's 
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				// end of t1 new d's 
				writeD(0x00);
				// c6 new h's
				writeD(0x00);
				writeD(0x00);
				writeD(augmentation);
				writeD(augmentation);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				if (lrhand > 0)
					writeD(augmentation);
				else
					writeD(0);
				writeD(0x00);
				writeD(0x00);

				// T1 new h's
				writeD(0);
				writeD(0);
				writeD(0x00); //Talisman 1
				writeD(0x00); //Talisman 2
				writeD(0x00); //Talisman 3
				writeD(0x00); //Talisman 4
				writeD(0x00); //Talisman 5
				writeD(0x00); //Talisman 6
				// end of t1 new h's 
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0);
				writeD(0);
				writeD(_mAtkSpd);
				writeD(_pAtkSpd);
				writeD(0);
				writeD(0);
				writeD(_runSpd);
				writeD(_walkSpd);
				writeD(50); // swimspeed
				writeD(50); // swimspeed
				writeD(_runSpd);
				writeD(_walkSpd);
				writeD(_runSpd);
				writeD(_walkSpd);
				writeF(_runSpd * 1f / 120); // _activeChar.getProperMultiplier()
				writeF(((1.1) * _pAtkSpd / 300)); // _activeChar.getAttackSpeedMultiplier()
				writeF(_collisionRadius);
				writeF(_collisionHeight);
				writeD(charhair);
				writeD(charhaircolor);
				writeD(charface);
				writeS(_title);
				writeD(0);
				writeD(0);
				writeD(0);
				writeD(0);
				// In UserInfo leader rights and siege flags, but here found nothing??
				// Therefore RelationChanged packet with that info is required
				writeD(0);
				writeC(1); // standing = 1  sitting = 0
				writeC(_npc.isRunning() ? 1 : 0); // running = 1   walking = 0
				writeC(_npc.isInCombat() ? 1 : 0);
				writeC(_npc.isAlikeDead() ? 1 : 0);
				writeC(0);
				writeC(0); // 1 on strider   2 on wyvern   0 no mount
				writeC(0); //  1 - sellshop
				writeH(0);
				//writeH(id);
				writeC(0x00); // find party members
				writeD(_npc.getAbnormalEffect());
				writeC(0); //Changed by Thorgrim
				writeH(0); //Blue value for name (0 = white, 255 = pure blue)
				writeD(0);
				writeD(charclass);
				writeD(0);
				writeC(enchlvl);
				writeC(0x00); //team circle around feet 1= Blue, 2 = red
				writeD(0);
				writeC(0); // Symbol on char menu ctrl+I
				writeC(charhero); // Hero Aura
				writeC(0); //0x01: Fishing Mode (Cant be undone by setting back to 0)
				writeD(0);
				writeD(0);
				writeD(0);
				writeD(charcolor);
				writeD(_heading); // isRunning() as in UserInfo?
				writeD(0);
				writeD(0); // ??
				writeD(0xFFFF77);
				writeD(0);
				writeD(0x00);
				writeD(0);
				writeD(0);
				writeD(0x01);
			}
			else
			{
				writeC(0x0c);
				writeD(_npc.getObjectId());
				writeD(_idTemplate + 1000000); // npctype id
				writeD(_npc.isAutoAttackable(activeChar) ? 1 : 0);
				writeD(_x);
				writeD(_y);
				writeD(_z);
				writeD(_heading);
				writeD(0x00);
				writeD(_mAtkSpd);
				writeD(_pAtkSpd);
				writeD(_runSpd);
				writeD(_walkSpd);
				writeD(_runSpd); // swim run speed
				writeD(_walkSpd); // swim walk speed
				writeD(_runSpd);
				writeD(_walkSpd);
				writeD(_runSpd); // fly run speed
				writeD(_walkSpd); // fly walk speed
				writeF(_npc.getStat().getMovementSpeedMultiplier());
				writeF(_npc.getStat().getAttackSpeedMultiplier());
				writeF(_collisionRadius);
				writeF(_collisionHeight);
				writeD(_rhand); // right hand weapon
				writeD(_chest);
				writeD(_lhand); // left hand weapon
				writeC(1); // name above char 1=true ... ??
				writeC(_npc.isRunning() ? 1 : 0);
				writeC(_npc.isInCombat() ? 1 : 0);
				writeC(_npc.isAlikeDead() ? 1 : 0);
				writeC(_isSummoned ? 2 : 0); // 0=teleported 1=default 2=summoned
				writeS(_name);
				writeS(_title);
				writeD(0x00); // Title color 0=client default
				writeD(0x00); // pvp flag
				writeD(0x00); // karma

				writeD(_npc.getAbnormalEffect()); // C2
				writeD(_clanId); // clan id
				writeD(_clanCrest); // crest id
				writeD(_allyId); // ally id
				writeD(_allyCrest); // ally crest
				writeC(_npc.isFlying() ? 2 : 0); // C2
				writeC(0x00); // title color 0=client

				writeF(_collisionRadius);
				writeF(_collisionHeight);
				writeD(_npc.getWeaponEnchantLevel()); // C4
				writeD(_npc.isFlying() ? 1 : 0); // C6
				writeD(0x00);
				writeD(_npc.getColorEffect());// CT1.5 Pet form and skills, Color effect
				writeC(_npc.isHideName() ? 0x00 : 0x01);
				writeC(_npc.isHideName() ? 0x00 : 0x01);
				writeD(_npc.getSpecialEffect());
				writeD(_displayEffect);
			}
		}
		
		@Override
		public boolean canBeSentTo(L2GameClient client, L2Player activeChar)
		{
			if (!activeChar.canSee(_npc))
				return false;
			
			return true;
		}
	}
	
	public static class TrapInfo extends AbstractNpcInfo
	{
		private final L2Trap	_trap;
		
		public TrapInfo(L2Trap cha)
		{
			super(cha);
			_trap = cha;
			_idTemplate = cha.getTemplate().getIdTemplate();
			_rhand = 0;
			_lhand = 0;
			_collisionHeight = _trap.getTemplate().getCollisionHeight();
			_collisionRadius = _trap.getTemplate().getCollisionRadius();
			_title = cha.getOwner() !=  null  ? cha.getOwner().getName() :  "";
			_runSpd = _trap.getStat().getRunSpeed();
			_walkSpd = _trap.getStat().getWalkSpeed();
		}
		
		@Override
		protected void writeImpl(L2GameClient client, L2Player activeChar)
		{
			writeC(0x0c);
			writeD(_trap.getObjectId());
			writeD(_idTemplate + 1000000); // npctype id
			writeD(_trap.isAutoAttackable(activeChar) ? 1 : 0);
			writeD(_x);
			writeD(_y);
			writeD(_z);
			writeD(_heading);
			writeD(0x00);
			writeD(_mAtkSpd);
			writeD(_pAtkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd); // swim run speed
			writeD(_walkSpd); // swim walk speed
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd); // fly run speed
			writeD(_walkSpd); // fly walk speed
			// FIXME?
			writeF(1.1/* _trap.getStat().getMovementSpeedMultiplier() */);
			writeF(_trap.getStat().getAttackSpeedMultiplier());
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			writeD(_rhand); // right hand weapon
			writeD(_chest);
			writeD(_lhand); // left hand weapon
			writeC(1); // name above char 1=true ... ??
			writeC(1);
			writeC(_trap.isInCombat() ? 1 : 0);
			writeC(_trap.isAlikeDead() ? 1 : 0);
			writeC(_isSummoned ? 2 : 0); // 0=teleported 1=default 2=summoned
			writeS(_name);
			writeS(_title);
			writeD(0x00); // title color 0 = client default
			
			writeD(0x00); // pvp flag
			writeD(0x00); // karma
			
			writeD(_trap.getAbnormalEffect()); // C2
			writeD(0x00); // clan id
			writeD(0x00); // crest id
			writeD(0000); // C2
			writeD(0000); // C2
			writeC(0000); // C2
			
			writeC(0x00); // Title color 0=client default
			
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			writeD(0x00); // C4
			writeD(0x00); // C6
			writeD(0x00);
			writeD(0);// CT1.5 Pet form and skills
			writeC(0x01);
			writeC(0x01);
			writeD(0x00);
		}
		
		@Override
		public boolean canBeSentTo(L2GameClient client, L2Player activeChar)
		{
			if (!activeChar.canSee(_trap))
				return false;
			
			return true;
		}
	}
	
	public static class SummonInfo extends AbstractNpcInfo
	{
		private final L2Summon	_summon;
		private int				_form	= 0;
		private int				_val	= 0;
		
		public SummonInfo(L2Summon cha, int val)
		{
			super(cha);
			_summon = cha;
			_val = val;
			
			int npcId = cha.getTemplate().getNpcId();
			
			if (npcId == 16041 || npcId == 16042)
			{
				if (cha.getLevel() > 84)
					_form = 3;
				else if (cha.getLevel() > 79)
					_form = 2;
				else if (cha.getLevel() > 74)
					_form = 1;
			}
			else if (npcId == 16025 || npcId == 16037)
			{
				if (cha.getLevel() > 69)
					_form = 3;
				else if (cha.getLevel() > 64)
					_form = 2;
				else if (cha.getLevel() > 59)
					_form = 1;
			}
			
			// fields not set on AbstractNpcInfo
			_rhand = cha.getWeapon();
			_lhand = 0;
			_chest = cha.getArmor();
			_collisionHeight = _summon.getTemplate().getCollisionHeight();
			_collisionRadius = _summon.getTemplate().getCollisionRadius();
			_name = cha.getName();
			_title = cha.getOwner() != null ? (cha.getOwner().isOnline() == 0 ? "" : cha.getOwner().getName()) : ""; // when owner online, summon will show in
																														// title owner name
			_idTemplate = cha.getTemplate().getIdTemplate();
			
			_collisionHeight = cha.getTemplate().getCollisionHeight();
			_collisionRadius = cha.getTemplate().getCollisionRadius();
			
			// few fields needing fix from AbstractNpcInfo
			_runSpd = cha.getPetSpeed();
			_walkSpd = cha.isMountable() ? 45 : 30;
		}
		
		@Override
		protected void writeImpl(L2GameClient client, L2Player activeChar)
		{
			writeC(0x0c);
			writeD(_summon.getObjectId());
			writeD(_idTemplate + 1000000); // npctype id
			writeD(_summon.isAutoAttackable(activeChar) ? 1 : 0);
			writeD(_x);
			writeD(_y);
			writeD(_z);
			writeD(_heading);
			writeD(0x00);
			writeD(_mAtkSpd);
			writeD(_pAtkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd); // swim run speed
			writeD(_walkSpd); // swim walk speed
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd); // fly run speed
			writeD(_walkSpd); // fly walk speed
			writeF(_summon.getStat().getMovementSpeedMultiplier());
			writeF(_summon.getStat().getAttackSpeedMultiplier());
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			writeD(_rhand); // right hand weapon
			writeD(_chest);
			writeD(_lhand); // left hand weapon
			writeC(1); // name above char 1=true ... ??
			writeC(1);
			writeC(_summon.isInCombat() ? 1 : 0);
			writeC(_summon.isAlikeDead() ? 1 : 0);
			writeC(_val); // 0=teleported 1=default 2=summoned
			writeS(_name);
			writeS(_title);
			writeD(0x01);// Title color 0=client default
			
			writeD(_summon.getPvpFlag());
			writeD(_summon.getKarma());
			
			if (_summon.getOwner().getAppearance().isInvisible())
			{
				writeD((_summon.getAbnormalEffect() | AbnormalEffect.STEALTH.getMask()));
			}
			else
			{
				writeD(_summon.getAbnormalEffect());
			}
			writeD(_summon.getOwner().getClanId());
			writeD(0x00); // crest id
			writeD(0000); // C2
			writeD(0000); // C2
			writeC(0000); // C2
			
			writeC(_summon.getTeam());
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			// FIXME
			// writeD(_summon.getWeaponEnchantLevel()); // C4
			writeD(0x00); // C4
			writeD(0x00); // C6
			writeD(0x00);
			writeD(_form); // CT1.5 Pet form and skills
			writeC(0x01);
			writeC(0x01);
			writeD(0x00);
		}
		
		@Override
		public boolean canBeSentTo(L2GameClient client, L2Player activeChar)
		{
			// Owner gets PetInfo
			if (_summon.getOwner() == activeChar)
				return false;
			
			if (!activeChar.canSee(_summon))
				return false;
			
			return true;
		}
	}
	
	public static class PcMorphInfo extends AbstractNpcInfo
	{
		private final L2Player	_pc;
		private final L2NpcTemplate	_template;
		
		public PcMorphInfo(L2Player cha, L2NpcTemplate template)
		{
			super(cha);
			_pc = cha;
			_template = template;
		}
		
		@Override
		protected void writeImpl(L2GameClient client, L2Player activeChar)
		{
			writeC(0x0c);
			writeD(_pc.getObjectId());
			writeD(_pc.getPoly().getPolyId() + 1000000); // npctype id
			writeD(_pc.isAutoAttackable(activeChar) ? 1 : 0);
			writeD(_x);
			writeD(_y);
			writeD(_z);
			writeD(_heading);
			writeD(0x00);
			writeD(_mAtkSpd);
			writeD(_pAtkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd); // swim run speed
			writeD(_walkSpd); // swim walk speed
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_runSpd); // fly run speed
			writeD(_walkSpd); // fly walk speed
			writeF(_pc.getStat().getMovementSpeedMultiplier());
			writeF(_pc.getStat().getAttackSpeedMultiplier());
			writeF(_template.getCollisionRadius());
			writeF(_template.getCollisionHeight());
			writeD(_pc.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_RHAND)); // right hand weapon
			writeD(0); // Chest
			writeD(_pc.getInventory().getPaperdollItemDisplayId(Inventory.PAPERDOLL_LHAND)); // left hand weapon
			writeC(1); // name above char 1=true ... ??
			writeC(_pc.isRunning() ? 1 : 0);
			writeC(_pc.isInCombat() ? 1 : 0);
			writeC(_pc.isAlikeDead() ? 1 : 0);
			writeC(0);
			writeS(_pc.getAppearance().getVisibleName());
			writeS(_pc.getAppearance().getVisibleTitle());
			
			writeD(0x00); // Title Color
			writeD(_pc.getKarma());
			writeD(_pc.getPvpFlag());
			
			if (_pc.getAppearance().isInvisible())
			{
				writeD((_pc.getAbnormalEffect() | AbnormalEffect.STEALTH.getMask()));
			}
			else
			{
				writeD(_pc.getAbnormalEffect()); // C2
			}
			
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeC(0x00);
			writeC(0x00);
			writeF(_template.getCollisionRadius());
			writeF(_template.getCollisionHeight());
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeC(0x01);
			writeC(0x01);
			writeD(0x00);
		}
		
		@Override
		public boolean canBeSentTo(L2GameClient client, L2Player activeChar)
		{
			// Won't work
			if (_pc == activeChar)
				return false;
			
			if (!activeChar.canSee(_pc))
				return false;
			
			return true;
		}
	}
}
