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
package net.l2emuproject.gameserver.entity.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import net.l2emuproject.gameserver.services.crafting.L2RecipeList;
import net.l2emuproject.gameserver.services.crafting.RecipeService;
import net.l2emuproject.gameserver.services.shortcuts.L2ShortCut;
import net.l2emuproject.gameserver.system.L2DatabaseFactory;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.util.SingletonMap;

public final class PlayerRecipe extends PlayerExtension
{
	private static final String					INSERT_RECIPE		= "INSERT INTO character_recipebook (charId, id, classIndex, type) values(?,?,?,?)";
	private static final String					DELETE_RECIPE		= "DELETE FROM character_recipebook WHERE charId=? AND id=? AND classIndex=?";
	private static final String					SELECT_RECIPE1		= "SELECT id, type, classIndex FROM character_recipebook WHERE charId=?";
	private static final String					SELECT_RECIPE2		= "SELECT id FROM character_recipebook WHERE charId=? AND classIndex=? AND type = 1";

	private final Map<Integer, L2RecipeList>	_dwarvenRecipeBook	= new SingletonMap<Integer, L2RecipeList>();
	private final Map<Integer, L2RecipeList>	_commonRecipeBook	= new SingletonMap<Integer, L2RecipeList>();

	public PlayerRecipe(L2Player activeChar)
	{
		super(activeChar);
	}

	private final void insertNewRecipeData(int recipeId, boolean isDwarf)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_RECIPE);
			statement.setInt(1, getPlayer().getObjectId());
			statement.setInt(2, recipeId);
			statement.setInt(3, isDwarf ? getPlayer().getClassIndex() : 0);
			statement.setInt(4, isDwarf ? 1 : 0);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.error("SQL exception while inserting recipe: " + recipeId + " from character " + getPlayer().getObjectId(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private final void deleteRecipeData(int recipeId, boolean isDwarf)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_RECIPE);
			statement.setInt(1, getPlayer().getObjectId());
			statement.setInt(2, recipeId);
			statement.setInt(3, isDwarf ? getPlayer().getClassIndex() : 0);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.error("SQL exception while deleting recipe: " + recipeId + " from character " + getPlayer().getObjectId(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Restore recipe book data for this L2Player.
	 */
	public final void restoreRecipeBook(boolean loadCommon)
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			String sql = loadCommon ? SELECT_RECIPE1 : SELECT_RECIPE2;
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setInt(1, getPlayer().getObjectId());
			if (!loadCommon)
				statement.setInt(2, getPlayer().getClassIndex());
			ResultSet rset = statement.executeQuery();

			_dwarvenRecipeBook.clear();

			L2RecipeList recipe;
			while (rset.next())
			{
				recipe = RecipeService.getInstance().getRecipeList(rset.getInt("id"));

				if (loadCommon)
				{
					if (rset.getInt(2) == 1)
					{
						if (rset.getInt(3) == getPlayer().getClassIndex())
							registerDwarvenRecipeList(recipe, false);
					}
					else
						registerCommonRecipeList(recipe, false);
				}
				else
					registerDwarvenRecipeList(recipe, false);
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not restore recipe book data:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Return a table containing all Common L2Recipe of the L2Player.<BR><BR>
	 */
	public final L2RecipeList[] getCommonRecipeBook()
	{
		return _commonRecipeBook.values().toArray(new L2RecipeList[_commonRecipeBook.values().size()]);
	}

	/**
	 * Return a table containing all Dwarf L2Recipe of the L2Player.<BR><BR>
	 */
	public final L2RecipeList[] getDwarvenRecipeBook()
	{
		return _dwarvenRecipeBook.values().toArray(new L2RecipeList[_dwarvenRecipeBook.values().size()]);
	}

	/**
	 * Add a new L2Recipe to the table _commonrecipebook containing all L2Recipe of the L2Player <BR><BR>
	 *
	 * @param recipe The L2RecipeList to add to the _recipebook
	 * @param saveToDb true to save infos into the DB
	 */
	public final void registerCommonRecipeList(L2RecipeList recipe, boolean saveToDb)
	{
		_commonRecipeBook.put(recipe.getId(), recipe);

		if (saveToDb)
			insertNewRecipeData(recipe.getId(), false);
	}

	/**
	 * Add a new L2Recipe to the table _recipebook containing all L2Recipe of the L2Player <BR><BR>
	 *
	 * @param recipe The L2Recipe to add to the _recipebook
	 * @param saveToDb true to save infos into the DB
	 */
	public final void registerDwarvenRecipeList(L2RecipeList recipe, boolean saveToDb)
	{
		_dwarvenRecipeBook.put(recipe.getId(), recipe);

		if (saveToDb)
			insertNewRecipeData(recipe.getId(), true);
	}

	/**
	 * @param recipeId The Identifier of the L2Recipe to check in the player's recipe books
	 *
	 * @return
	 * <b>TRUE</b> if player has the recipe on Common or Dwarven Recipe book else returns <b>FALSE</b>
	 */
	public final boolean hasRecipeList(int recipeId)
	{
		if (_dwarvenRecipeBook.containsKey(recipeId))
			return true;
		else
			return _commonRecipeBook.containsKey(recipeId);
	}

	/**
	 * Tries to remove a L2Recipe from the table _DwarvenRecipeBook or from table _CommonRecipeBook, those table contain all L2Recipe of the L2Player <BR><BR>
	 *
	 * @param recipeId The Identifier of the L2Recipe to remove from the _recipebook
	 *
	 */
	public final void unregisterRecipeList(int recipeId)
	{
		if (_dwarvenRecipeBook.remove(recipeId) != null)
			deleteRecipeData(recipeId, true);
		else if (_commonRecipeBook.remove(recipeId) != null)
			deleteRecipeData(recipeId, false);
		else
			_log.warn("Attempted to remove unknown RecipeList: " + recipeId);

		getPlayer().getPlayerSettings().getShortCuts().deleteShortCutByTypeAndId(L2ShortCut.TYPE_RECIPE, recipeId);
	}
}
