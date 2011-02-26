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
package net.l2emuproject.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Calendar;
import java.util.regex.Pattern;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.CharNameTable;
import net.l2emuproject.gameserver.datatables.CharTemplateTable;
import net.l2emuproject.gameserver.datatables.ItemTable;
import net.l2emuproject.gameserver.datatables.NpcTable;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.datatables.SkillTreeTable;
import net.l2emuproject.gameserver.entity.stat.PcStat;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.model.itemcontainer.PcInventory;
import net.l2emuproject.gameserver.network.Disconnection;
import net.l2emuproject.gameserver.network.serverpackets.CharSelectionInfo;
import net.l2emuproject.gameserver.network.serverpackets.CharacterCreateFail;
import net.l2emuproject.gameserver.network.serverpackets.CharacterCreateSuccess;
import net.l2emuproject.gameserver.services.quest.Quest;
import net.l2emuproject.gameserver.services.quest.QuestService;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.recommendation.RecommendationService;
import net.l2emuproject.gameserver.services.shortcuts.L2ShortCut;
import net.l2emuproject.gameserver.skills.L2SkillLearn;
import net.l2emuproject.gameserver.system.L2DatabaseFactory;
import net.l2emuproject.gameserver.system.idfactory.IdFactory;
import net.l2emuproject.gameserver.system.taskmanager.SQLQueue;
import net.l2emuproject.gameserver.templates.chars.L2PcTemplate;
import net.l2emuproject.gameserver.templates.chars.L2PcTemplate.PcTemplateItem;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;


/**
 * This class represents a packet sent by the client when a new character is being created.
 * 
 * @version $Revision: 1.9.2.3.2.8 $ $Date: 2005/03/27 15:29:30 $
 */
public class NewCharacter extends L2GameClientPacket
{
	private static final String _C__NEWCHARACTER = "[C] 0C NewCharacter c[sdddddddddddd]";
	private static final Object CREATION_LOCK = new Object();
	
	private String _name;
	//private int				_race;
	private byte _sex;
	private int _classId;
	/*
	private int _int;
	private int _str;
	private int _con;
	private int _men;
	private int _dex;
	private int _wit;
	*/
	private byte _hairStyle;
	private byte _hairColor;
	private byte _face;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
		/*_race  = */readD();
		_sex = (byte)readD();
		_classId = readD();
		/*_int   = */readD();
		/*_str   = */readD();
		/*_con   = */readD();
		/*_men   = */readD();
		/*_dex   = */readD();
		/*_wit   = */readD();
		_hairStyle = (byte)readD();
		_hairColor = (byte)readD();
		_face = (byte)readD();
	}
	
	@Override
	protected void runImpl()
	{
		// Only 1 packet may be executed at a time (prevent multiple names)
		synchronized (CREATION_LOCK)
		{
			int reason = -1;
			if (CharNameTable.getInstance().doesCharNameExist(_name))
			{
				if (_log.isDebugEnabled())
					_log.debug("charname: " + _name + " already exists. creation failed.");
				reason = CharacterCreateFail.REASON_NAME_ALREADY_EXISTS;
			}
			else if (CharNameTable.getInstance().accountCharNumber(getClient().getAccountName()) >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT
					&& Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0)
			{
				if (_log.isDebugEnabled())
					_log.debug("Max number of characters reached. Creation failed.");
				reason = CharacterCreateFail.REASON_TOO_MANY_CHARACTERS;
			}
			else if (!Config.CNAME_PATTERN.matcher(_name).matches())
			{
				if (_log.isDebugEnabled())
					_log.debug("charname: " + _name + " is invalid. creation failed.");
				reason = CharacterCreateFail.REASON_16_ENG_CHARS;
			}
			else if (NpcTable.getInstance().getTemplateByName(_name) != null || obsceneCheck(_name))
			{
				if (_log.isDebugEnabled())
					_log.debug("charname: " + _name + " overlaps with a NPC. creation failed.");
				reason = CharacterCreateFail.REASON_INCORRECT_NAME;
			}
			
			if (_log.isDebugEnabled())
				_log.debug("charname: " + _name + " classId: " + _classId);
			
			L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(_classId);
			if (template == null || template.getClassBaseLevel() > 1)
			{
				sendPacket(new CharacterCreateFail(CharacterCreateFail.REASON_CREATION_FAILED));
				return;
			}
			else if (reason != -1)
			{
				sendPacket(new CharacterCreateFail(reason));
				return;
			}
			
			int objectId = IdFactory.getInstance().getNextId();
			L2Player newChar = L2Player.create(objectId, template, getClient().getAccountName(), _name,
					_hairStyle, _hairColor, _face, _sex != 0);
			newChar.getStatus().setCurrentHp(template.getBaseHpMax());
			newChar.getStatus().setCurrentCp(template.getBaseCpMax());
			newChar.getStatus().setCurrentMp(template.getBaseMpMax());
			//newChar.setMaxLoad(template.baseLoad);
			
			// send acknowledgement
			sendPacket(CharacterCreateSuccess.PACKET);
			
			initNewChar(newChar);
			sendAF();
		}
	}
	
	private void initNewChar(L2Player newChar)
	{
		if (_log.isDebugEnabled())
			_log.debug("Character init start");
		
		storeCreationDate(newChar);
		
		L2World.getInstance().storeObject(newChar);
		
		L2PcTemplate template = newChar.getTemplate();
		
		newChar.addAdena("Init", Config.STARTING_ADENA, null, false);
		
		// L2EMU_ADD - START
		for (int[] startingItems : Config.CUSTOM_STARTER_ITEMS)
		{
			if (newChar == null)			
				continue;		
			
			PcInventory inventory = newChar.getInventory();
			if (ItemTable.getInstance().createDummyItem(startingItems[0]).isStackable())		
				inventory.addItem("Starter Items", startingItems[0], startingItems[1], newChar, null);		
			else
			{
				for (int i = 0; i < startingItems[1]; i++)				
					inventory.addItem("Starter Items", startingItems[0], 1, newChar, null);			
			}
		}
		// L2EMU_ADD- END
		
		newChar.getPosition().setXYZInvisible(template.getSpawnX(), template.getSpawnY(), template.getSpawnZ());
		// L2EMU_ADD
		if (Config.ALLOW_NEW_CHARACTER_TITLE)
			newChar.setTitle(Config.NEW_CHARACTER_TITLE);
		else
			newChar.setTitle("");
		// L2EMU_ADD
		
		newChar.getPlayerVitality().setVitalityPoints(PcStat.MAX_VITALITY_POINTS, true);
		
		if (Config.STARTING_LEVEL > 1)
			newChar.getStat().addLevel((byte)(Config.STARTING_LEVEL - 1));
		
		if (Config.STARTING_SP > 0)
			newChar.getStat().addSp(Config.STARTING_SP);
		
		//add attack shortcut
		newChar.getPlayerSettings().registerShortCut(new L2ShortCut(0, 0, L2ShortCut.TYPE_ACTION, 2, 0, 1));
		//add take shortcut
		newChar.getPlayerSettings().registerShortCut(new L2ShortCut(3, 0, L2ShortCut.TYPE_ACTION, 5, 0, 1));
		//add sit shortcut
		newChar.getPlayerSettings().registerShortCut(new L2ShortCut(10, 0, L2ShortCut.TYPE_ACTION, 0, 0, 1));
		
		for (PcTemplateItem ia : template.getItems())
		{
			L2ItemInstance item = newChar.getInventory().addItem("Init", ia.getItemId(), ia.getAmount(), newChar, null);
			
			// add tutorial guide shortcut
			if (item.getItemId() == 5588)
				newChar.getPlayerSettings().registerShortCut(new L2ShortCut(11, 0, L2ShortCut.TYPE_ITEM, item.getObjectId(), 0, 1));
			
			if (item.isEquipable() && ia.isEquipped())
				newChar.getInventory().equipItemAndRecord(item);
		}
		
		SQLQueue.getInstance().run();
		
		for (L2SkillLearn skill : SkillTreeTable.getInstance().getAvailableSkills(newChar, newChar.getClassId()))
		{
			newChar.addSkill(SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel()), true);
			if (skill.getId() == 1001 || skill.getId() == 1177)
				newChar.getPlayerSettings().registerShortCut(new L2ShortCut(1, 0, L2ShortCut.TYPE_SKILL, skill.getId(), skill.getLevel(), 1));
			if (skill.getId() == 1216)
				newChar.getPlayerSettings().registerShortCut(new L2ShortCut(10, 0, L2ShortCut.TYPE_SKILL, skill.getId(), skill.getLevel(), 1));
			if (_log.isDebugEnabled())
				_log.debug("adding starter skill:" + skill.getId() + " / " + skill.getLevel());
		}
		startTutorialQuest(newChar);
		RecommendationService.getInstance().onCreate(newChar);
		new Disconnection(getClient(), newChar).store().deleteMe();
		
		// send char list
		sendPacket(new CharSelectionInfo(getClient()));
		if (_log.isDebugEnabled())
			_log.debug("Character init end");
	}
	
	public void startTutorialQuest(L2Player player)
	{
		QuestState qs = player.getQuestState("_255_Tutorial");
		Quest q = null;
		if (qs == null)
			q = QuestService.getInstance().getQuest("_255_Tutorial");
		if (q != null)
			q.newQuestState(player);
	}
	
	private final void storeCreationDate(L2Player player)
	{
		Connection con = null;
		try
		{
			Calendar now = Calendar.getInstance();
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO character_birthdays VALUES (?,?,?)");
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, now.get(Calendar.YEAR));
			ps.setDate(3, new Date(now.getTimeInMillis()));
			ps.executeUpdate();
			ps.close();
		}
		catch (Exception e)
		{
			_log.error("Could not store " + player + "'s creation day!", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	private final boolean obsceneCheck(String name)
	{
		for (Pattern pattern : Config.FILTER_LIST)
			if (pattern.matcher(name).find())
				return true;
		return false;
	}
	
	@Override
	public String getType()
	{
		return _C__NEWCHARACTER;
	}
}
