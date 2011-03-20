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
package instances.Kamaloka;

import java.util.Arrays;

import net.l2emuproject.gameserver.manager.instances.Instance;
import net.l2emuproject.gameserver.manager.instances.InstanceManager;
import net.l2emuproject.gameserver.manager.instances.InstanceManager.InstanceWorld;
import net.l2emuproject.gameserver.manager.instances.PartyInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.services.party.L2Party;
import net.l2emuproject.gameserver.skills.L2Effect;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import teleports.KamalokaEscaper.KamalokaEscaper;

/**
 * @author lord_rex
 *	<br> A basic class to handle Kamaloka Instances.
 */
public abstract class BasicKamaloka extends PartyInstance
{
	private static final int[]	NPCS					=
														{ 30332, 30071, 30916, 30196, 31981, 31340 };

	private static final int[]	ALLOWED_BUFFS			=
														{ 5627, 5628, 5629, 5630, 5631, 5632, 5633, 5634, 5635, 5636, 5637, 5950 };

	private int					_instanceId;

	protected byte				_clientInstanceId;
	protected String			_xmlTemplate;

	protected int[]				_enterLocation;
	protected int				_bossId;
	protected byte				_requiredLevel;
	protected byte				_maximumPartyMembers	= 6;

	public BasicKamaloka(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		for (int i : NPCS)
		{
			addStartNpc(i);
			addTalkId(i);
		}
	}

	protected final class KamalokaWorld extends InstanceWorld
	{
		private KamalokaWorld()
		{
			InstanceManager.getInstance().super();
		}
	}

	@Override
	protected final boolean canEnter(final L2Player player)
	{
		final L2Party party = player.getParty();

		if (party == null)
			return false;
		else if (party.getMemberCount() > _maximumPartyMembers)
		{
			player.sendMessage("Your party size is bigger than allowed!");
			return false;
		}
		else if (!canEnterPlayer(player))
			return false;

		for (L2Player members : party.getPartyMembers())
			if (!canEnterPlayer(members))
				return false;

		return true;
	}

	private final boolean canEnterPlayer(final L2Player player)
	{
		if (player.getLevel() < _requiredLevel)
		{
			player.sendMessage("Your level is too low to enter Kamaloka!");
			return false;
		}
		else if (System.currentTimeMillis() < InstanceManager.getInstance().getInstanceTime(player.getObjectId(), _clientInstanceId))
		{
			player.sendPacket(SystemMessageId.NO_RE_ENTER_TIME_FOR_C1);
			return false;
		}

		return true;
	}

	@Override
	protected final void enterInstance(final L2Player player)
	{
		if (!canEnter(player))
			return;

		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (world.templateId != _clientInstanceId)
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return;
			}

			return;
		}
		else
		{
			_instanceId = InstanceManager.getInstance().createDynamicInstance(_xmlTemplate);

			world = new KamalokaWorld();
			world.instanceId = _instanceId;
			world.templateId = _clientInstanceId;
			InstanceManager.getInstance().addWorld(world);

			addSpawn(KamalokaEscaper.GATEKEEPER, -10865, -174905, -10944, 0, false, 0, _instanceId);
			addSpawn(KamalokaEscaper.GATEKEEPER, 21837, -174885, -10904, 0, false, 0, _instanceId);

			removePartyBuffs(player);
			addParty(world, player);
			teleportParty(player, _enterLocation, _instanceId);

			_log.info("KamalokaInstance: " + _instanceId + " - " + _xmlTemplate + " created by player: " + player.getName());
		}
	}

	@Override
	protected final void exitInstance(final L2Player player)
	{
		// For now this is not used, players exit by KamalokaEscaper.
	}

	private final void removePartyBuffs(final L2Player player)
	{
		final L2Party party = player.getParty();

		if (party != null)
			for (L2Player members : party.getPartyMembers())
				if (members != null)
					removeBuffs(members);
	}

	private final void removeBuffs(final L2Character cha)
	{
		for (L2Effect e : cha.getAllEffects())
		{
			if (e == null)
				continue;
			final L2Skill skill = e.getSkill();
			if (skill.isDebuff() || skill.isStayAfterDeath())
				continue;
			if (Arrays.binarySearch(ALLOWED_BUFFS, skill.getId()) >= 0)
				continue;
			e.exit();
		}
		if (cha.getPet() != null)
		{
			for (L2Effect e : cha.getPet().getAllEffects())
			{
				if (e == null)
					continue;
				final L2Skill skill = e.getSkill();
				if (skill.isDebuff() || skill.isStayAfterDeath())
					continue;
				if (Arrays.binarySearch(ALLOWED_BUFFS, skill.getId()) >= 0)
					continue;
				e.exit();
			}
		}
	}

	@Override
	public final String onTalk(final L2Npc npc, final L2Player player)
	{
		enterInstance(player);
		return null;
	}

	@Override
	public final String onKill(final L2Npc npc, final L2Player player, final boolean isPet)
	{
		final int npcId = npc.getNpcId();

		if (npcId == _bossId)
		{
			final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof KamalokaWorld)
			{
				KamalokaWorld world = (KamalokaWorld) tmpworld;

				for (int objectId : world.allowed)
					InstanceManager.getInstance().setInstanceTime(objectId, _clientInstanceId, ((System.currentTimeMillis() + 86400000)));

				final Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);
				instance.setDuration(300000);
			}
		}

		return null;
	}

	public static void main(String[] args)
	{
		new Kamaloka23(-1, Kamaloka23.class.getSimpleName(), Kamaloka23.QN, "Kamaloka");
		new Kamaloka26(-1, Kamaloka26.class.getSimpleName(), Kamaloka26.QN, "Kamaloka");
		new Kamaloka29(-1, Kamaloka29.class.getSimpleName(), Kamaloka29.QN, "Kamaloka");
		new Kamaloka33(-1, Kamaloka33.class.getSimpleName(), Kamaloka33.QN, "Kamaloka");
		new Kamaloka36(-1, Kamaloka36.class.getSimpleName(), Kamaloka36.QN, "Kamaloka");
		new Kamaloka39(-1, Kamaloka39.class.getSimpleName(), Kamaloka39.QN, "Kamaloka");
		new Kamaloka43(-1, Kamaloka43.class.getSimpleName(), Kamaloka43.QN, "Kamaloka");
		new Kamaloka46(-1, Kamaloka46.class.getSimpleName(), Kamaloka46.QN, "Kamaloka");
		new Kamaloka49(-1, Kamaloka49.class.getSimpleName(), Kamaloka49.QN, "Kamaloka");
	}
}
