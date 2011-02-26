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
package net.l2emuproject.gameserver.datatables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.entity.player.keyboard.ActionKey;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author  mrTJO
 */
public final class UITable
{
	private static final Log							_log				= LogFactory.getLog(UITable.class);

	private final Map<Integer, List<ActionKey>>	_storedKeys			= new FastMap<Integer, List<ActionKey>>();
	private final Map<Integer, List<Integer>>	_storedCategories	= new FastMap<Integer, List<Integer>>();

	@SuppressWarnings("synthetic-access")
	private static final class SingletonHolder
	{
		private static final UITable	_instance	= new UITable();
	}

	public static UITable getInstance()
	{
		return SingletonHolder._instance;
	}

	private UITable()
	{
		reload();
	}

	public void reload()
	{
		_storedKeys.clear();
		_storedCategories.clear();
		parseCatData();
		parseKeyData();
		_log.info(getClass().getSimpleName() + " : Loaded " + _storedCategories.size() + " Categorie(s).");
		_log.info(getClass().getSimpleName() + " : Loaded " + _storedKeys.size() + " Key(s).");
	}

	private void parseCatData()
	{
		LineNumberReader lnr = null;
		try
		{
			File uiData = new File(Config.DATAPACK_ROOT, "data/uicats_en.csv");
			lnr = new LineNumberReader(new BufferedReader(new FileReader(uiData)));

			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().length() == 0 || line.startsWith("#"))
					continue;

				StringTokenizer st = new StringTokenizer(line, ";");

				final int cat = Integer.parseInt(st.nextToken());
				final int cmd = Integer.parseInt(st.nextToken());

				insertCategory(cat, cmd);
			}
		}
		catch (FileNotFoundException e)
		{
			_log.warn("uicats_en.csv is missing in data folder");
		}
		catch (Exception e)
		{
			_log.warn("error while creating UI Default Categories table ", e);
		}
		finally
		{
			IOUtils.closeQuietly(lnr);
		}
	}

	private void parseKeyData()
	{
		LineNumberReader lnr = null;
		try
		{
			File uiData = new File(Config.DATAPACK_ROOT, "data/uikeys_en.csv");
			lnr = new LineNumberReader(new BufferedReader(new FileReader(uiData)));

			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().length() == 0 || line.startsWith("#"))
					continue;

				StringTokenizer st = new StringTokenizer(line, ";");

				final int cat = Integer.parseInt(st.nextToken());
				final int cmd = Integer.parseInt(st.nextToken());
				final int key = Integer.parseInt(st.nextToken());
				final int tk1 = Integer.parseInt(st.nextToken());
				final int tk2 = Integer.parseInt(st.nextToken());
				final int shw = Integer.parseInt(st.nextToken());

				insertKey(cat, cmd, key, tk1, tk2, shw);
			}
		}
		catch (FileNotFoundException e)
		{
			_log.warn("uikeys_en.csv is missing in data folder");
		}
		catch (Exception e)
		{
			_log.warn("error while creating UI Default Keys table ", e);
		}
		finally
		{
			IOUtils.closeQuietly(lnr);
		}
	}

	private void insertCategory(int cat, int cmd)
	{
		if (_storedCategories.containsKey(cat))
			_storedCategories.get(cat).add(cmd);
		else
		{
			List<Integer> tmp = new FastList<Integer>();
			tmp.add(cmd);
			_storedCategories.put(cat, tmp);
		}
	}

	private void insertKey(int cat, int cmdId, int key, int tgKey1, int tgKey2, int show)
	{
		ActionKey tmk = new ActionKey(cat, cmdId, key, tgKey1, tgKey2, show);
		if (_storedKeys.containsKey(cat))
			_storedKeys.get(cat).add(tmk);
		else
		{
			List<ActionKey> tmp = new FastList<ActionKey>();
			tmp.add(tmk);
			_storedKeys.put(cat, tmp);
		}
	}

	public Map<Integer, List<Integer>> getCategories()
	{
		return _storedCategories;
	}

	public Map<Integer, List<ActionKey>> getKeys()
	{
		return _storedKeys;
	}
}
