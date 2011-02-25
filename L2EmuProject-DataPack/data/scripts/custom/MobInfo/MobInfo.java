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
package custom.MobInfo;

import java.util.ArrayList;
import java.util.List;

import javolution.text.TextBuilder;
import net.l2emuproject.gameserver.cache.HtmCache;
import net.l2emuproject.gameserver.datatables.ItemTable;
import net.l2emuproject.gameserver.datatables.NpcTable;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.templates.item.L2Item;
import net.l2emuproject.gameserver.world.npc.L2DropCategory;
import net.l2emuproject.gameserver.world.npc.L2DropData;

import org.apache.commons.lang.ArrayUtils;

/**
 * @author L0ngh0rn
 * @since Need for rework. Maybe I'll do this later. (2011-01-04)
 */
public final class MobInfo extends QuestJython
{
	public static final String	QN				= "MobInfo";

	private static final int	MAX_PER_PAGE	= 15;

	private static final int[]	NPCS			=
												{
			31729,
			31732,
			31733,
			31734,
			31738,
			31775,
			31776,
			31777,
			31778,
			31779,
			31780,
			31781,
			31782,
			31783,
			31784,
			31785,
			31786,
			31787,
			31788,
			31789,
			31790,
			31791,
			31792,
			31793,
			31794,
			31795,
			31796,
			31797,
			31798,
			31799,
			31800,
			31801,
			31802,
			31803,
			31804,
			31805,
			31806,
			31807,
			31808,
			31809,
			31810,
			31811,
			31812,
			31813,
			31814,
			31815,
			31816,
			31817,
			31818,
			31819,
			31820,
			31821,
			31822,
			31823,
			31824,
			31825,
			31826,
			31827,
			31828,
			31829,
			31830,
			31831,
			31832,
			31833,
			31834,
			31835,
			31836,
			31837,
			31838,
			31839,
			31840,
			31841,
			32337,
			32338,
			32339,
			32340								};

	public MobInfo(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);
		for (int i : NPCS)
		{
			addStartNpc(i);
			addTalkId(i);
		}
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "mobinfo.htm";
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		String paramEvent[] = event.split(" ");
		String action = "page", param1 = "", param2 = "";
		int size = paramEvent.length;

		if (size >= 1)
			action = paramEvent[0];
		if (size >= 2)
			param1 = paramEvent[1];
		if (size >= 3)
		{
			for (int i = 2; i < size; i++)
				param2 += paramEvent[i] + " ";
			param2 = param2.trim().replaceAll(" ", "_");
		}

		if (st.getQuestItemsCount(57) < 100)
		{
			player.sendMessage("You not have enought adena for payment this service.");
			return htmltext;
		}
		st.takeAdena(100);

		// TODO: Should have some better way to treat it ...
		// Load Template
		String path = "data/scripts/" + getFolder() + "/" + QN + "/template.htm";
		String template = HtmCache.getInstance().getHtm(path);
		if (template == null)
		{
			_log.warn("Missing html page " + path);
			template = "<html><body>File " + path + " not found or file is empty.</body></html>";
			return template;
		}

		TextBuilder t = new TextBuilder();

		if (action.equalsIgnoreCase("npc_level") || action.equalsIgnoreCase("npc_name"))
		{
			int page = Integer.parseInt(param1);
			String title = "", subTitle = "";
			int level = 1;
			List<L2NpcTemplate> npcData = new ArrayList<L2NpcTemplate>();

			if (action.equalsIgnoreCase("npc_level"))
			{
				try
				{
					level = Integer.parseInt(param2);
					title = "Monster by Level";
					subTitle = "Search Result by Level";

					if (!(level > 0 && level < 100))
					{
						player.sendMessage("Please enter level between 1 and 99.");
						return htmltext;
					}
				}
				catch (Exception e)
				{
					player.sendMessage("Please enter level format number.");
					return htmltext;
				}
			}
			else if (action.equalsIgnoreCase("npc_name"))
			{
				title = "Monster by Name";
				subTitle = "Search Result by Name";
			}

			for (L2NpcTemplate n : NpcTable.getInstance().getAllTemplates())
			{
				if ((action.equalsIgnoreCase("npc_level") && n.getLevel() == level)
						|| (action.equalsIgnoreCase("npc_name") && n.getName().toLowerCase().contains(param2.replaceAll("_", " ").toLowerCase())))
					npcData.add(n);
			}

			if (npcData.isEmpty())
			{
				if (action.equalsIgnoreCase("npc_level"))
					player.sendMessage("I dont know any Monster in level " + level + ".");
				else if (action.equalsIgnoreCase("npc_name"))
					player.sendMessage("Not found NPC name " + param2.replaceAll("_", " ") + ".");
				return htmltext;
			}

			int length = npcData.size();
			int maxPages = length / MAX_PER_PAGE;
			if (length > MAX_PER_PAGE * maxPages)
				maxPages = maxPages + 1;
			if (page > maxPages)
				page = maxPages;
			int start = MAX_PER_PAGE * page;
			int end = length;
			if ((end - start) > MAX_PER_PAGE)
				end = start + MAX_PER_PAGE;

			t.clear();

			template = template.replace("%title%", title);

			if (page == 0)
				t.append("<button value=\"Back\" action=\"bypass -h Quest MobInfo\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			else
			{
				t.append("<button value=\"Back\" action=\"bypass -h Quest MobInfo ");
				t.append(action);
				t.append(" ");
				t.append(page - 1);
				t.append(" ");
				t.append(param2);
				t.append("\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			}
			template = template.replace("%option01%", t.toString());
			t.clear();

			template = template.replace("%option02%", subTitle);

			if (page + 1 < maxPages)
			{
				t.append("<button value=\"Next\" action=\"bypass -h Quest MobInfo ");
				t.append(action);
				t.append(" ");
				t.append(page + 1);
				t.append(" ");
				t.append(param2);
				t.append("\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			}
			else
				t.append(" ");
			template = template.replace("%option03%", t.toString());
			t.clear();

			template = template.replace("%search%", param2.replace("_", " "));
			template = template.replace("%found%", String.valueOf(npcData.size()));
			template = template.replace("%page%", String.valueOf(page + 1));
			template = template.replace("%total%", String.valueOf(maxPages));

			t.append("<table width=\"100%\"><tr>");
			t.append("<td width=\"100%\"><font color=\"LEVEL\">Name</font></td><td width=\"40\"><font color=\"LEVEL\">Drop</font></td>");
			t.append("<td width=\"40\"><font color=\"LEVEL\">Find</font></td></tr>");
			for (int i = start; i < end; i++)
			{
				String name = npcData.get(i).getName();
				if (name.isEmpty() || name.trim().equals(""))
					name = "-No Name-";
				t.append("<tr><td>");
				t.append(name);
				t.append(npcData.get(i).getAggroRange() > 0 ? " <font color=\"LEVEL\">*</font>" : " ");
				t.append("</td><td><button value=\"Show\" action=\"bypass -h Quest MobInfo show_drop 0 ");
				t.append(npcData.get(i).getNpcId());
				t.append("\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				t.append("<td><button value=\"Find\" action=\"bypass -h npc_%objectId%_npcfind_byid ");
				t.append(npcData.get(i).getNpcId());
				t.append("\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
			}
			t.append("</table>");

			template = template.replace("%table_rows%", t.toString());
			t.clear();

			template = template.replace("%objectId%", String.valueOf(npc.getObjectId()));
			return template;
		}
		else if (action.equalsIgnoreCase("item_name"))
		{
			String nameItem = param2.replace("_", " ");
			List<L2Item> itemData = ItemTable.getInstance().findItemsByName(nameItem);
			if (itemData == null)
			{
				player.sendMessage("I dont know any item by this name: " + nameItem + ".");
				return htmltext;
			}

			int page = Integer.parseInt(param1);

			int length = itemData.size();
			int maxPages = length / MAX_PER_PAGE;
			if (length > MAX_PER_PAGE * maxPages)
				maxPages = maxPages + 1;
			if (page > maxPages)
				page = maxPages;
			int start = MAX_PER_PAGE * page;
			int end = length;
			if ((end - start) > MAX_PER_PAGE)
				end = start + MAX_PER_PAGE;

			t.clear();

			template = template.replace("%title%", "Items");

			if (page == 0)
				t.append("<button value=\"Back\" action=\"bypass -h Quest MobInfo\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			else
			{
				t.append("<button value=\"Back\" action=\"bypass -h Quest MobInfo ");
				t.append(action);
				t.append(" ");
				t.append(page - 1);
				t.append(" ");
				t.append(param2);
				t.append("\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			}
			template = template.replace("%option01%", t.toString());
			t.clear();

			template = template.replace("%option02%", "Search Result for Items:");

			if (page + 1 < maxPages)
			{
				t.append("<button value=\"Next\" action=\"bypass -h Quest MobInfo ");
				t.append(action);
				t.append(" ");
				t.append(page + 1);
				t.append(" ");
				t.append(param2);
				t.append("\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			}
			else
				t.append(" ");
			template = template.replace("%option03%", t.toString());
			t.clear();

			template = template.replace("%search%", param2.replace("_", " "));
			template = template.replace("%found%", String.valueOf(itemData.size()));
			template = template.replace("%page%", String.valueOf(page + 1));
			template = template.replace("%total%", String.valueOf(maxPages));

			t.append("<table width=\"100%\"><tr>");
			t.append("<td width=\"100%\"><font color=\"LEVEL\">Name</font></td>");
			t.append("<td width=\"60\"><font color=\"LEVEL\">View Drops</font></td></tr>");
			for (int i = start; i < end; i++)
			{
				String name = itemData.get(i).getName();
				if (name.isEmpty() || name.trim().equals(""))
					name = "-No Name-";
				t.append("<tr><td>");
				t.append(name);
				t.append("</td><td><button value=\"View\" action=\"bypass -h Quest MobInfo drop_npc 0 ");
				t.append(itemData.get(i).getItemId());
				t.append("\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				t.append("</tr>");
			}
			t.append("</table>");

			template = template.replace("%table_rows%", t.toString());
			t.clear();

			template = template.replace("%objectId%", String.valueOf(npc.getObjectId()));
			return template;
		}
		else if (action.equalsIgnoreCase("show_drop"))
		{
			L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(Integer.parseInt(param2));
			if (npcData.getDropData() == null)
			{
				player.sendMessage("No data found for Level " + npcData.getLevel() + " - " + npcData.getName());
				return htmltext;
			}

			int page = Integer.parseInt(param1);
			List<L2DropData> dropData = new ArrayList<L2DropData>();
			List<L2DropCategory> sweepData = new ArrayList<L2DropCategory>();

			for (L2DropCategory cat : npcData.getDropData())
				for (L2DropData drop : cat.getAllDrops())
				{
					dropData.add(drop);
					sweepData.add(cat);
				}

			if (dropData.isEmpty())
			{
				player.sendMessage("No data found for Level " + npcData.getLevel() + " - " + npcData.getName());
				return htmltext;
			}

			int length = dropData.size();
			int maxPages = length / MAX_PER_PAGE;
			if (length > MAX_PER_PAGE * maxPages)
				maxPages = maxPages + 1;
			if (page > maxPages)
				page = maxPages;
			int start = MAX_PER_PAGE * page;
			int end = length;
			if ((end - start) > MAX_PER_PAGE)
				end = start + MAX_PER_PAGE;

			t.clear();

			template = template.replace("%title%", "Show DropList");

			if (page == 0)
				t.append("<button value=\"Back\" action=\"bypass -h Quest MobInfo\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			else
			{
				t.append("<button value=\"Back\" action=\"bypass -h Quest MobInfo ");
				t.append(action);
				t.append(" ");
				t.append(page - 1);
				t.append(" ");
				t.append(param2);
				t.append("\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			}
			template = template.replace("%option01%", t.toString());
			t.clear();

			template = template.replace("%option02%", "Drop and Spoil Result:");

			if (page + 1 < maxPages)
			{
				t.append("<button value=\"Next\" action=\"bypass -h Quest MobInfo ");
				t.append(action);
				t.append(" ");
				t.append(page + 1);
				t.append(" ");
				t.append(param2);
				t.append("\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			}
			else
				t.append(" ");
			template = template.replace("%option03%", t.toString());
			t.clear();

			template = template.replace("%search%", npcData.getName());
			template = template.replace("%found%", String.valueOf(dropData.size()));
			template = template.replace("%page%", String.valueOf(page + 1));
			template = template.replace("%total%", String.valueOf(maxPages));

			t.append("<table width=\"100%\"><tr>");
			t.append("<td width=\"100%\"><font color=\"LEVEL\">Name</font></td><td width=\"40\"><font color=\"LEVEL\">Type</font></td>");
			t.append("<td width=\"40\"><font color=\"LEVEL\">Chance</font></td></tr>");
			for (int i = start; i < end; i++)
			{
				String name = ItemTable.getInstance().getTemplate(dropData.get(i).getItemId()).getName();
				if (name.isEmpty() || name.trim().equals(""))
					name = "-No Name-";
				String ty = "";
				if (dropData.get(i).isQuestDrop())
					ty = "Qu";
				if (sweepData.get(i).isSweep())
					ty = "<font color=\"LEVEL\">Sw</font>";

				t.append("<tr><td>");
				t.append(name);
				t.append("</td><td>");
				t.append(ty);
				t.append("</td><td>");
				t.append(dropData.get(i).getChance() / 10000);
				t.append("</td></tr>");
			}
			t.append("</table>");

			template = template.replace("%table_rows%", t.toString());
			t.clear();

			template = template.replace("%objectId%", String.valueOf(npc.getObjectId()));
			return template;
		}
		else if (action.equalsIgnoreCase("drop_npc"))
		{
			List<L2NpcTemplate> npcData = NpcTable.getInstance().getMobsByDrop(Integer.parseInt(param2));

			if (npcData.isEmpty())
			{
				player.sendMessage("No monster obtains this item, you must search it somewhere besides the ancient battlegrounds!");
				return htmltext;
			}

			int page = Integer.parseInt(param1);
			int length = npcData.size();
			int maxPages = length / MAX_PER_PAGE;
			if (length > MAX_PER_PAGE * maxPages)
				maxPages = maxPages + 1;
			if (page > maxPages)
				page = maxPages;
			int start = MAX_PER_PAGE * page;
			int end = length;
			if ((end - start) > MAX_PER_PAGE)
				end = start + MAX_PER_PAGE;

			t.clear();

			template = template.replace("%title%", "Drop Monster");

			if (page == 0)
				t.append("<button value=\"Back\" action=\"bypass -h Quest MobInfo\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			else
			{
				t.append("<button value=\"Back\" action=\"bypass -h Quest MobInfo ");
				t.append(action);
				t.append(" ");
				t.append(page - 1);
				t.append(" ");
				t.append(param2);
				t.append("\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			}
			template = template.replace("%option01%", t.toString());
			t.clear();

			template = template.replace("%option02%", "Drop: " + ItemTable.getInstance().getTemplate(Integer.parseInt(param2)).getName());

			if (page + 1 < maxPages)
			{
				t.append("<button value=\"Next\" action=\"bypass -h Quest MobInfo ");
				t.append(action);
				t.append(" ");
				t.append(page + 1);
				t.append(" ");
				t.append(param2);
				t.append("\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			}
			else
				t.append(" ");
			template = template.replace("%option03%", t.toString());
			t.clear();

			template = template.replace("%search%", param2.replace("_", " "));
			template = template.replace("%found%", String.valueOf(npcData.size()));
			template = template.replace("%page%", String.valueOf(page + 1));
			template = template.replace("%total%", String.valueOf(maxPages));

			t.append("<table width=\"100%\"><tr>");
			t.append("<td width=\"100%\"><font color=\"LEVEL\">Name</font></td><td width=\"40\"><font color=\"LEVEL\">Drop</font></td>");
			t.append("<td width=\"40\"><font color=\"LEVEL\">Find</font></td></tr>");
			for (int i = start; i < end; i++)
			{
				String name = npcData.get(i).getName();
				if (name.isEmpty() || name.trim().equals(""))
					name = "-No Name-";
				t.append("<tr><td>");
				t.append(name);
				t.append(npcData.get(i).getAggroRange() > 0 ? " <font color=\"LEVEL\">*</font>" : " ");
				t.append("</td><td><button value=\"Show\" action=\"bypass -h Quest MobInfo show_drop 0 ");
				t.append(npcData.get(i).getNpcId());
				t.append("\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				t.append("<td><button value=\"Find\" action=\"bypass -h npc_%objectId%_npcfind_byid ");
				t.append(npcData.get(i).getNpcId());
				t.append("\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
			}
			t.append("</table>");

			template = template.replace("%table_rows%", t.toString());
			t.clear();

			template = template.replace("%objectId%", String.valueOf(npc.getObjectId()));
			return template;
		}
		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			st = newQuestState(player);

		if (ArrayUtils.contains(NPCS, npc.getNpcId()))
			htmltext = "mobinfo.htm";
		return htmltext;
	}

	public static void main(String[] args)
	{
		new MobInfo(8002, QN, "Mob Information", "custom");
	}
}
