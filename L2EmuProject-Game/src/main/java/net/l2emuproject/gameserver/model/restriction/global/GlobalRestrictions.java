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
package net.l2emuproject.gameserver.model.restriction.global;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.handler.IItemHandler;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.L2Playable;
import net.l2emuproject.gameserver.world.object.instance.L2PetInstance;
import net.l2emuproject.gameserver.world.zone.L2Zone;

import org.apache.commons.lang.ArrayUtils;

/**
 * @author NB4L1
 */
public final class GlobalRestrictions
{
	private GlobalRestrictions()
	{
	}

	private static enum RestrictionMode implements Comparator<GlobalRestriction>
	{
		isRestricted,
		isProtected,
		canInviteToParty,
		canRequestRevive,
		canTeleport,
		canUseItemHandler,
		canPickUp,
		playerLoggedIn,
		playerDisconnected,
		playerKilled,
		onBypassFeedback,
		onAction,
		canUseVoicedCommand,
		canUseSkill,
		canBeSummoned,

		// TODO
		;

		private final Method	_method;

		private RestrictionMode()
		{
			for (Method method : GlobalRestriction.class.getMethods())
			{
				if (name().equals(method.getName()))
				{
					_method = method;
					return;
				}
			}

			throw new InternalError();
		}

		private boolean equalsMethod(Method method)
		{
			if (!_method.getName().equals(method.getName()))
				return false;

			if (!_method.getReturnType().equals(method.getReturnType()))
				return false;

			return Arrays.equals(_method.getParameterTypes(), method.getParameterTypes());
		}

		private static final RestrictionMode[]	VALUES	= RestrictionMode.values();

		private static RestrictionMode parse(Method method)
		{
			for (RestrictionMode mode : VALUES)
				if (mode.equalsMethod(method))
					return mode;

			return null;
		}

		@Override
		public int compare(GlobalRestriction o1, GlobalRestriction o2)
		{
			return Double.compare(getPriority(o2), getPriority(o1));
		}

		private double getPriority(GlobalRestriction restriction)
		{
			RestrictionPriority a1 = getMatchingMethod(restriction.getClass()).getAnnotation(RestrictionPriority.class);
			if (a1 != null)
				return a1.value();

			RestrictionPriority a2 = restriction.getClass().getAnnotation(RestrictionPriority.class);
			if (a2 != null)
				return a2.value();

			return RestrictionPriority.DEFAULT_PRIORITY;
		}

		private Method getMatchingMethod(Class<? extends GlobalRestriction> clazz)
		{
			for (Method method : clazz.getMethods())
				if (equalsMethod(method))
					return method;

			throw new InternalError();
		}
	}

	private static final GlobalRestriction[][]	_restrictions	= new GlobalRestriction[RestrictionMode.VALUES.length][0];

	public synchronized static void activate(GlobalRestriction restriction)
	{
		for (Method method : restriction.getClass().getMethods())
		{
			RestrictionMode mode = RestrictionMode.parse(method);

			if (mode == null)
				continue;

			if (method.getAnnotation(DisabledRestriction.class) != null)
				continue;

			GlobalRestriction[] restrictions = _restrictions[mode.ordinal()];

			if (!ArrayUtils.contains(restrictions, restriction))
				restrictions = (GlobalRestriction[]) ArrayUtils.add(restrictions, restriction);

			Arrays.sort(restrictions, mode);

			_restrictions[mode.ordinal()] = restrictions;
		}
	}

	public synchronized static void deactivate(GlobalRestriction restriction)
	{
		for (RestrictionMode mode : RestrictionMode.VALUES)
		{
			GlobalRestriction[] restrictions = _restrictions[mode.ordinal()];

			for (int index; (index = ArrayUtils.indexOf(restrictions, restriction)) != -1;)
				restrictions = (GlobalRestriction[]) ArrayUtils.remove(restrictions, index);

			_restrictions[mode.ordinal()] = restrictions;
		}
	}

	static
	{
		activate(new CursedWeaponRestriction());
		activate(new EradicationRestriction());
		activate(new DuelRestriction());
		activate(new JailRestriction());
		activate(new MercenaryTicketRestriction());
		activate(new OlympiadRestriction());
		activate(new ProtectionBlessingRestriction());

		// L2EMU_ADD
		activate(new GMRestriction());
		activate(new CustomRestriction());
		// L2EMU_ADD
	}

	/**
	 * @param activeChar
	 * @param callingRestriction
	 * @return <b>true</b> if the player shouldn't be affected by any other kind of event system,<br>
	 *         because it's already participating in one, or it's just simply in a forbidden state<br>
	 *         <b>false</b> otherwise
	 */
	public static boolean isRestricted(L2Player activeChar, Class<? extends GlobalRestriction> callingRestriction)
	{
		// Avoid NPE and wrong usage
		if (activeChar == null)
			return true;

		// Cannot mess with offline trade
		if (activeChar.isInOfflineMode()) // trading in offline mode
		{
			//no need any message
			return true;
		}

		// Cannot mess with observation
		if (activeChar.getPlayerObserver().inObserverMode()) // normal/olympiad observing
		{
			activeChar.sendMessage("You are in observer mode!");
			return true;
		}

		// Cannot mess with raids or sieges
		if (activeChar.isInsideZone(L2Zone.FLAG_NOESCAPE))
		{
			return true;
		}

		if (activeChar.getMountType() == 2 && activeChar.isInsideZone(L2Zone.FLAG_NOWYVERN))
		{
			return true;
		}

		for (GlobalRestriction restriction : _restrictions[RestrictionMode.isRestricted.ordinal()])
			if (restriction.isRestricted(activeChar, callingRestriction))
				return true;

		return false;
	}

	/**
	 * Called in RequestJoinParty to restrict whether or not player can create a party or get invited to a party.
	 */
	public static boolean canInviteToParty(L2Player activeChar, L2Player target)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.canInviteToParty.ordinal()])
			if (!restriction.canInviteToParty(activeChar, target))
				return false;

		return true;
	}

	/**
	 * Indicates if the character can't hit/cast a skill on another, but can target it.
	 */
	public static boolean isProtected(L2Character activeChar, L2Character target, L2Skill skill, boolean sendMessage)
	{
		final L2Player attacker_ = L2Object.getActingPlayer(activeChar);
		final L2Player target_ = L2Object.getActingPlayer(target);

		final boolean isOffensive = (skill == null || skill.isOffensive());

		return isProtected(activeChar, target, skill, sendMessage, attacker_, target_, isOffensive);
	}

	private static boolean isProtected(L2Character activeChar, L2Character target, L2Skill skill, boolean sendMessage, L2Player attacker_,
			L2Player target_, boolean isOffensive)
	{
		if (attacker_ != null && target_ != null && attacker_ != target_)
		{
			if (attacker_.isGM())
			{
				if (isOffensive && attacker_.getAccessLevel() < Config.GM_CAN_GIVE_DAMAGE)
					return true;
				else if (!target_.isGM())
					return false;
			}

			if (attacker_.getPlayerObserver().inObserverMode() || target_.getPlayerObserver().inObserverMode())
			{
				if (sendMessage)
					attacker_.sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
				return true;
			}
			else if (attacker_.getLevel() <= Config.ALT_PLAYER_PROTECTION_LEVEL)
			{
				if (sendMessage)
					attacker_.sendMessage("Your level is too low to participate in a PvP combat.");
				return true;
			}
			else if (target_.getLevel() <= Config.ALT_PLAYER_PROTECTION_LEVEL)
			{
				if (sendMessage)
					attacker_.sendMessage("Target is under newbie protection.");
				return true;
			}
		}

		// Checking if target has moved to peace zone
		if (isOffensive && L2Character.isInsidePeaceZone(activeChar, target))
		{
			if (sendMessage)
				activeChar.sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
			return true;
		}

		if (Config.ALLOW_OFFLINE_TRADE_PROTECTION && target_ != null && target_.isInOfflineMode())
			return true;

		for (GlobalRestriction restriction : _restrictions[RestrictionMode.isProtected.ordinal()])
			if (restriction.isProtected(activeChar, target, skill, sendMessage, attacker_, target_, isOffensive))
				return true;

		return false;
	}

	/**
	 * Called in Die packet, if return false, player can't request for revive.
	 */
	public static boolean canRequestRevive(L2Player activeChar)
	{
		if (activeChar.isPendingRevive())
			return false;

		for (GlobalRestriction restriction : _restrictions[RestrictionMode.canRequestRevive.ordinal()])
			if (!restriction.canRequestRevive(activeChar))
				return false;

		return true;
	}

	/**
	 * This is allows to you to disable the teleport in a special state.
	 */
	public static boolean canTeleport(L2Player activeChar)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.canTeleport.ordinal()])
			if (!restriction.canTeleport(activeChar))
				return false;

		return true;
	}

	/**
	 * This is allows to you to disable an item handler in a special state.
	 * 
	 * Usage: <br>
	 * public boolean canUseItemHandler(Class<? extends IItemHandler> clazz, int itemId, 
	 * 		L2Playable activeChar, L2ItemInstance item, L2Player player) {
	 * if (clazz == ExampleApple.class) {
	 *		if (player != null && player.isInExample()) {
	 *			player.sendMessage("You can't do that...");
	 *			return false;
	 *		}
	 *	} 
	 *	return true;
	 *}
	 */
	public static boolean canUseItemHandler(Class<? extends IItemHandler> clazz, int itemId, L2Playable activeChar, L2ItemInstance item)
	{
		final L2Player player = L2Object.getActingPlayer(activeChar);

		for (GlobalRestriction restriction : _restrictions[RestrictionMode.canUseItemHandler.ordinal()])
			if (!restriction.canUseItemHandler(clazz, itemId, activeChar, item, player))
				return false;

		return true;
	}

	/**
	 * This is allows to you to disable the item picking up in a special state.
	 */
	public static boolean canPickUp(L2Player activeChar, L2ItemInstance item, L2PetInstance pet)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.canPickUp.ordinal()])
			if (!restriction.canPickUp(activeChar, item, pet))
				return false;

		return true;
	}

	/**
	 * Called in EnterWorld to restore(if allowed) the players event state.
	 */
	public static void playerLoggedIn(L2Player activeChar)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.playerLoggedIn.ordinal()])
			restriction.playerLoggedIn(activeChar);
	}

	/**
	 * Called on disconnect to save(if allowed) the players event state.
	 */
	public static void playerDisconnected(L2Player activeChar)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.playerDisconnected.ordinal()])
			restriction.playerDisconnected(activeChar);
	}

	/**
	 * Called in doDie() to run a task when a player is killed.
	 */
	public static void playerKilled(L2Character activeChar, L2Player target)
	{
		final L2Player killer = L2Object.getActingPlayer(activeChar);

		for (GlobalRestriction restriction : _restrictions[RestrictionMode.playerKilled.ordinal()])
			restriction.playerKilled(activeChar, target, killer);
	}

	/**
	 * This is a bypass manager restriction.
	 */
	public static boolean onBypassFeedback(L2Npc npc, L2Player activeChar, String command)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.onBypassFeedback.ordinal()])
			if (restriction.onBypassFeedback(npc, activeChar, command))
				return true;

		return false;
	}

	/**
	 * Called when player interacts with a target <br>
	 * (Usage: L2Player or L2Npc).
	 */
	public static boolean onAction(L2Character target, L2Character activeChar)
	{
		final L2Player attacker_ = L2Object.getActingPlayer(activeChar);
		final L2Player target_ = L2Object.getActingPlayer(target);

		// if GM is invisible, exclude him from the normal gameplay
		if (target_ != null && target_.isGM() && target_.getAppearance().isInvisible())
		{
			// if there is an attacker, but it isn't playable, or not GM
			if (activeChar != null && (attacker_ == null || !attacker_.isGM()))
			{
				return false;
			}
		}

		if (attacker_ != null && target_ != null && attacker_ != target_)
		{
			if (Config.SIEGE_ONLY_REGISTERED)
			{
				if (!target_.canBeTargetedByAtSiege(attacker_))
				{
					attacker_.sendMessage("Player interaction disabled during sieges.");
					return false;
				}
			}
		}

		for (GlobalRestriction restriction : _restrictions[RestrictionMode.onAction.ordinal()])
			if (restriction.onAction(target, activeChar))
				return false;

		return true;
	}

	/**
	 * Called when a player use a voiced command.
	 */
	public static boolean canUseVoicedCommand(String command, L2Player activeChar, String target)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.canUseVoicedCommand.ordinal()])
			if (restriction.canUseVoicedCommand(command, activeChar, target))
				return false;

		return true;
	}

	/**
	 * Called in RequestMagicSkillUse to restrict skill usage under event by skillId or skillType.
	 */
	public static boolean canUseSkill(L2Player player, L2Skill skill)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.canUseSkill.ordinal()])
			if (restriction.canUseSkill(player, skill))
				return false;

		return true;
	}

	/**
	 * Called in SummonFriend to restrict whether or not the player can be summoned from outside the state.
	 */
	public static boolean canBeSummoned(L2Player target)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.canBeSummoned.ordinal()])
			if (restriction.canBeSummoned(target))
				return false;

		return true;
	}
}
