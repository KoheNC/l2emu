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
package ai.individual;

import java.util.Collection;
import java.util.Map;

import javolution.util.FastMap;
import net.l2emuproject.gameserver.ThreadPoolManager;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.instancemanager.ZoneManager;
import net.l2emuproject.gameserver.model.actor.L2Attackable;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2MonsterInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.zone.L2Zone;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.tools.random.Rnd;
import ai.L2AttackableAIScript;

/**
 * Eye of Kasha AI
 * 
 * @author InsOmnia, Synerge
 *  from l2jepilogue: https://www.assembla.com/code/l2jepilogue/subversion/nodes
 *  
 *  Remade for L2EmuProject by lord_rex
 */
public class EyeOfKasha extends L2AttackableAIScript
{
	private static final String					QN					= "EyeOfKasha";

	private static final int					CURSE_MONSTERS		= 18812;							// Monsters which cast Kasha's Curse
	private static final int					YEARNING_MONSTERS	= 18813;							// Monsters which cast Kasha's Yearning
	private static final int					DESPAIR_MONSTERS	= 18814;							// Monsters which cast Kasha's Despair

	private static final int					DENOFEVILZONE		= 100056;
	private static final int[]					CAMP_ZONE			=

																	{
			100005,
			100006,
			100007,
			100008,
			100009,
			100010,
			100011,
			100012,
			100013,
			100014,
			100015,
			100016,
			100017,
			100018,
			100019,
			100020,
			100021,
			100022,
			100023,
			100024,
			100025,
			100026,
			100027,
			100028,
			100029,
			100030,
			100031,
			100032,
			100033,
			100034,
			100035,
			100036,
			100037,
			100038,
			100039,
			100040,
			100041,
			100042,
			100043,
			100044,
			100045,
			100046,
			100047,
			100048,
			100049,
			100050,
			100051,
			100052,
			100053,
			100054,
			100055													};

	private static final int[]					KASHA_ZONE			=

																	{
			17,
			18,
			19,
			20,
			21,
			22,
			23,
			24,
			25,
			26,
			27,
			28,
			29,
			30,
			31,
			32,
			33,
			34,
			35,
			36,
			37,
			38,
			39,
			40,
			41,
			42,
			43,
			44,
			45,
			46,
			47,
			48,
			49,
			50,
			51,
			52,
			53,
			54,
			55,
			56														};

	private static final Map<Integer, Integer>	KASHA_RESPAWN		= new FastMap<Integer, Integer>();

	static
	{
		KASHA_RESPAWN.put(18812, 18813);
		KASHA_RESPAWN.put(18813, 18814);
		KASHA_RESPAWN.put(18814, 18812);
	}

	private static final int[]					MONSTERS			=
																	{ 18812, 18813, 18814 };

	public EyeOfKasha(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addExitZoneId(DENOFEVILZONE);
		for (int i : KASHA_ZONE)
			addEnterZoneId(i);
		for (int i = 0; i < CAMP_ZONE.length; i++)
		{
			int random = Rnd.get(60 * 1000 * 1, 60 * 1000 * 7);
			int message;
			ThreadPoolManager.getInstance().scheduleGeneral(new CampDestroyTask(ZoneManager.getInstance().getZoneById(CAMP_ZONE[i])), random);
			if (random > 5 * 60000)
			{
				message = random - (5 * 60000);
				ThreadPoolManager.getInstance().scheduleGeneral(new CampMessageTask(0, ZoneManager.getInstance().getZoneById(CAMP_ZONE[i])), message);
			}
			if (random > 3 * 60000)
			{
				message = random - (3 * 60000);
				ThreadPoolManager.getInstance().scheduleGeneral(new CampMessageTask(0, ZoneManager.getInstance().getZoneById(CAMP_ZONE[i])), message);
			}
			if (random > 60000)
			{
				message = random - 60000;
				ThreadPoolManager.getInstance().scheduleGeneral(new CampMessageTask(0, ZoneManager.getInstance().getZoneById(CAMP_ZONE[i])), message);
			}
			if (random > 15000)
			{
				message = random - 15000;
				ThreadPoolManager.getInstance().scheduleGeneral(new CampMessageTask(1, ZoneManager.getInstance().getZoneById(CAMP_ZONE[i])), message);
			}
		}
	}

	@Override
	public String onEnterZone(L2Character character, L2Zone zone)
	{
		if (isKashaRange(zone))
		{
			if (isKashaInZone(zone))
			{
				if (character instanceof L2PcInstance)
					ThreadPoolManager.getInstance().scheduleGeneral(new CastTask(character, zone), Rnd.get(2000, 10000));
			}
		}
		return "";
	}

	@Override
	public String onExitZone(L2Character character, L2Zone zone)
	{
		if (character instanceof L2PcInstance)
		{
			if (character.getFirstEffect(6150) != null)
				character.stopSkillEffects(6150);
			if (character.getFirstEffect(6152) != null)
				character.stopSkillEffects(6152);
			if (character.getFirstEffect(6154) != null)
				character.stopSkillEffects(6154);
		}
		return "";
	}

	/**
	 * @param character - L2Character player inside Camp
	 * @return L2Zone campZone where character is
	 */
	private L2Zone getCamp(L2Character character)
	{
		for (int z : CAMP_ZONE)
		{
			L2Zone zone = ZoneManager.getInstance().getZoneById(z);
			if (zone != null && zone.isInsideZone(character))
				return zone;
		}
		return null;
	}

	/**
	 * @param zone - L2Zone circle arround the KashaEye
	 * @return true if this is Kasha Eye zone
	 */
	private boolean isKashaRange(L2Zone zone)
	{
		for (int z : KASHA_ZONE)
			if (zone != null && zone.getId() == z)
				return true;

		return false;
	}

	/**
	 * @param zone - L2Zone circle arround Kasha Eye
	 * @return L2Npc Kasha Eye inside the circle
	 */
	private L2Npc getKasha(L2Zone zone)
	{
		if (zone == null)
			return null;
		for (L2Character c : zone.getCharactersInside())
		{
			if (c instanceof L2MonsterInstance)
			{
				for (int k : MONSTERS)
				{
					if (k == ((L2MonsterInstance) c).getNpcId())
						return (L2MonsterInstance) c;
				}
			}
		}
		return null;
	}

	/**
	 * @param zone - L2Zone circle arround Kasha Eye
	 * @return true if Kasha Eye is inside the circle
	 */
	private boolean isKashaInZone(L2Zone zone)
	{
		Collection<L2Character> chars = zone.getCharactersInside();
		for (L2Character c : chars)
		{
			if (c instanceof L2MonsterInstance)
			{
				for (int k : MONSTERS)
				{
					if (k == ((L2MonsterInstance) c).getNpcId())
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param zone - L2Zone circle arround Kasha Eye
	 * @return true if character is still inside the circle
	 */
	private boolean isCharacterInZone(L2Character character, L2Zone zone)
	{
		if (zone == null)
			return false;
		Collection<L2Character> chars = zone.getCharactersInside();
		for (L2Character c : chars)
		{
			if (c == character)
				return true;
		}
		return false;
	}

	/**
	 * @param campZone - L2Zone camp zone
	 */
	private void destroyKashaInCamp(L2Zone campZone)
	{
		if (campZone == null)
			return;
		L2Skill skill = SkillTable.getInstance().getInfo(6149, 1);
		for (L2Character c : campZone.getCharactersInside())
		{
			if (c instanceof L2MonsterInstance)
			{
				for (int m : MONSTERS)
				{
					if (m == ((L2MonsterInstance) c).getNpcId())
					{
						if (!c.isDead())
						{
							c.doCast(skill);
							((L2Attackable) c).getSpawn().stopRespawn();
							c.doDie(c);
							ThreadPoolManager.getInstance().scheduleGeneral(new RespawnTask(c), 40000);
						}
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param message - 0 or 1
	 * 0 - I can feel that the energy being flown in the Kasha's eye is getting stronger rapidly.
	 * 1 - Kasha's eye pitches and tosses like it's about to explode.
	 */
	private void broadcastKashaMessage(int message, L2Zone campZone)
	{
		if (campZone == null)
			return;
		for (L2Character c : campZone.getCharactersInside())
		{
			if (c instanceof L2PcInstance)
			{
				switch (message)
				{
					case 0:
						c.sendPacket(SystemMessageId.KASHA_EYE_GETTING_STRONGER);
						break;
					case 1:
						c.sendPacket(SystemMessageId.KASHA_EYE_ABOUT_TO_EXPLODE);
						break;
				}
			}
		}
	}

	/**
	 * @param c - L2Character Kasha Eye Respawn Task
	 */
	private final class RespawnTask implements Runnable
	{
		private L2Character	_c;

		public RespawnTask(L2Character c)
		{
			_c = c;
		}

		@Override
		public void run()
		{
			L2MonsterInstance npc = ((L2MonsterInstance) _c);
			int npcId = npc.getNpcId();
			if (KASHA_RESPAWN.containsKey(npcId))
			{
				npc.deleteMe();
				addSpawn(KASHA_RESPAWN.get(npcId), npc);
			}
		}
	}

	/**
	 * @param character - L2PcInstance player to cast Kasha buffs
	 * @param kashaZone circle arround Kasha Eye
	 */
	private final class CastTask implements Runnable
	{
		private L2Character	_character;
		private L2Zone		_kashaZone;

		public CastTask(L2Character character, L2Zone kashaZone)
		{
			_character = character;
			_kashaZone = kashaZone;
		}

		@Override
		public void run()
		{
			if (getCamp(_character) != null && isCharacterInZone(_character, _kashaZone))
			{
				L2Zone campZone = getCamp(_character);
				int curseLvl = 0;
				int yearningLvl = 0;
				int despairLvl = 0;
				for (L2Character c : campZone.getCharactersInside())
				{
					if (c instanceof L2MonsterInstance)
					{
						if (((L2MonsterInstance) c).getNpcId() == CURSE_MONSTERS)
							curseLvl++;
						else if (((L2MonsterInstance) c).getNpcId() == YEARNING_MONSTERS)
							yearningLvl++;
						else if (((L2MonsterInstance) c).getNpcId() == DESPAIR_MONSTERS)
							despairLvl++;
					}
				}
				if (getKasha(_kashaZone) != null)
				{
					L2Skill curse = null;
					L2Skill yearning = null;
					L2Skill despair = null;
					L2Npc npc = getKasha(_kashaZone);
					boolean casted = false;
					if (curseLvl > 0)
					{
						if (_character.getFirstEffect(6150) != null)
							_character.stopSkillEffects(6150);
						curse = SkillTable.getInstance().getInfo(6150, curseLvl);
						curse.getEffects(npc, _character);
						casted = true;
					}
					if (yearningLvl > 0)
					{
						if (_character.getFirstEffect(6152) != null)
							_character.stopSkillEffects(6152);
						yearning = SkillTable.getInstance().getInfo(6152, yearningLvl);
						yearning.getEffects(npc, _character);
						casted = true;
					}

					if (despairLvl > 0)
					{
						if (_character.getFirstEffect(6154) != null)
							_character.stopSkillEffects(6154);
						despair = SkillTable.getInstance().getInfo(6154, despairLvl);
						despair.getEffects(npc, _character);
						casted = true;
					}
					if (casted && Rnd.get(100) <= 20)
						_character.sendPacket(SystemMessageId.KASHA_EYE_GIVES_STRANGE_FEELING);
				}
			}
		}
	}

	private final class CampDestroyTask implements Runnable
	{
		private L2Zone	_zone;

		public CampDestroyTask(L2Zone campZone)
		{
			_zone = campZone;
		}

		@Override
		public void run()
		{
			destroyKashaInCamp(_zone);
			ThreadPoolManager.getInstance().scheduleGeneral(new CampDestroyTask(_zone), 7 * 60000 + 40000);
			ThreadPoolManager.getInstance().scheduleGeneral(new CampMessageTask(0, _zone), 2 * 60000 + 40000);
			ThreadPoolManager.getInstance().scheduleGeneral(new CampMessageTask(0, _zone), 4 * 60000 + 40000);
			ThreadPoolManager.getInstance().scheduleGeneral(new CampMessageTask(0, _zone), 6 * 60000 + 40000);
			ThreadPoolManager.getInstance().scheduleGeneral(new CampMessageTask(1, _zone), (7 * 60000) + 25000);
		}
	}

	private final class CampMessageTask implements Runnable
	{
		private int		_message;
		private L2Zone	_campZone;

		public CampMessageTask(int message, L2Zone campZone)
		{
			_message = message;
			_campZone = campZone;
		}

		@Override
		public void run()
		{
			broadcastKashaMessage(_message, _campZone);
		}
	}

	public static void main(String[] args)
	{
		new EyeOfKasha(-1, QN, "ai");
	}
}