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
package custom.RaidbossInfo;

import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.Location;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class RaidbossInfo extends QuestJython
{
	private static final String					QN	= "RaidbossInfo";

	// not all are managed by RaidBossSpawnManager
	private static final Map<Integer, Location>	RAIDS	= new FastMap<Integer, Location>().shared();

	static
	{
		// lvl20 list
		RAIDS.put(25001, new Location(-54464, 146572, -2400)); // Greyclaw Kutus (lv23)
		RAIDS.put(25019, new Location(7352, 169433, -3172)); // Pan Dryad (lv25)
		RAIDS.put(25038, new Location(-57366, 186276, -4804)); // Tirak (lv28)
		RAIDS.put(25060, new Location(-60427, 188266, -4352)); // Unrequited Kael (lv24)
		RAIDS.put(25076, new Location(-61041, 127347, -2512)); // Princess Molrang (lv25)
		RAIDS.put(25095, new Location(-37799, 198120, -2200)); // Elf Renoa (lv29)
		RAIDS.put(25127, new Location(-47634, 219274, -1936)); // Langk Matriarch Rashkos (lv24)
		RAIDS.put(25146, new Location(-13698, 213796, -3300)); // Evil spirit Bifrons (lv21)
		RAIDS.put(25149, new Location(-12652, 138200, -3120)); // Zombie Lord Crowl (lv25)
		RAIDS.put(25166, new Location(-21778, 152065, -2636)); // Ikuntai (lv25)
		RAIDS.put(25272, new Location(49194, 127999, -3161)); // Partisan Leader Talakin (lv28)
		RAIDS.put(25357, new Location(-3451, 112819, -3032)); // Sukar Wererat Chief (lv21)
		RAIDS.put(25360, new Location(29064, 179362, -3128)); // Tiger Hornet (lv26)
		RAIDS.put(25362, new Location(-55791, 186903, -2856)); // Tracker Leader Sharuk (lv23)
		RAIDS.put(25365, new Location(-62171, 190489, -3160)); // Patriarch Kuroboros (lv26)
		RAIDS.put(25366, new Location(-62342, 179572, -3088)); // Kuroboros' Priest (lv23)
		RAIDS.put(25369, new Location(-45713, 111186, -3280)); // Soul Scavenger (lv25)
		RAIDS.put(25372, new Location(-114915, 233080, -1504)); // Discarded Guardian (lv20)
		RAIDS.put(25373, new Location(9661, 76976, -3652)); // Malex,  Herald of Dagoniel (lv21)
		RAIDS.put(25375, new Location(22523, 80431, -2772)); // Zombie Lord Farakelsus (lv20)
		RAIDS.put(25378, new Location(-53970, 84334, -3048)); // Madness Beast (lv20)
		RAIDS.put(25380, new Location(-47412, 51647, -5659)); // Kaysha,  Herald of Icarus (lv21)
		RAIDS.put(25426, new Location(-18053, -101274, -1580)); // Freki,  Betrayer of Urutu (lv25)
		RAIDS.put(25429, new Location(172122, -214776, -3064)); // Mammon's Collector Talloth (lv25)
		// lvl30 list
		RAIDS.put(25004, new Location(-94101, 100238, -3012)); // Turek Mercenary Captain (lv30)
		RAIDS.put(25020, new Location(90365, 125716, -1632)); // Breka Warlock Pastu (lv34)
		RAIDS.put(25023, new Location(27181, 101830, -3192)); // Swamp Stakato Queen Zyrnna (lv34)
		RAIDS.put(25041, new Location(10525, 126890, -3132)); // Remmel (lv35)
		RAIDS.put(25063, new Location(-91009, 116339, -2908)); // Chertuba of Great Soul (lv35)
		RAIDS.put(25079, new Location(53794, 102660, -529)); // Cat's Eye (lv30)
		RAIDS.put(25082, new Location(88554, 140646, -2960)); // Leader of Cat Gang (lv39)
		RAIDS.put(25098, new Location(123570, 133506, -3156)); // Sejarr's Servitor (lv35)
		RAIDS.put(25112, new Location(116219, 139458, -3124)); // Meana,  Agent of Beres (lv30)
		RAIDS.put(25118, new Location(50883, 146764, -3077)); // Guilotine,  Warden of the Execution Grounds (lv35)
		RAIDS.put(25128, new Location(17671, 179134, -3016)); // Vuku Grand Seer Gharmash (lv33)
		RAIDS.put(25152, new Location(43787, 124067, -2512)); // Flame Lord Shadar (lv35)
		RAIDS.put(25169, new Location(-54517, 170321, -2700)); // Ragraman (lv30)
		RAIDS.put(25170, new Location(26108, 122256, -3488)); // Lizardmen Leader Hellion (lv38)
		RAIDS.put(25185, new Location(88143, 166365, -3388)); // Tasaba Patriarch Hellena (lv35)
		RAIDS.put(25188, new Location(88102, 176262, -3012)); // Apepi (lv30)
		RAIDS.put(25189, new Location(68677, 203149, -3192)); // Cronos's Servitor Mumu (lv34)
		RAIDS.put(25211, new Location(76461, 193228, -3208)); // Sebek (lv36)
		RAIDS.put(25223, new Location(43062, 152492, -2294)); // Soul Collector Acheron (lv35)
		RAIDS.put(25352, new Location(-16843, 174890, -2984)); // Giant Wastelands Basilisk (lv30)
		RAIDS.put(25354, new Location(-16089, 184295, -3364)); // Gargoyle Lord Sirocco (lv35)
		RAIDS.put(25383, new Location(51405, 153984, -3008)); // Ghost of Sir Calibus (lv34)
		RAIDS.put(25385, new Location(53418, 143534, -3332)); // Evil Spirit Tempest (lv36)
		RAIDS.put(25388, new Location(40074, 102019, -790)); // Red Eye Captain Trakia (lv35)
		RAIDS.put(25391, new Location(45620, 120710, -2158)); // Nurka's Messenger (lv33)
		RAIDS.put(25392, new Location(29891, 107201, -3572)); // Captain of Queen's Royal Guards (lv32)
		RAIDS.put(25394, new Location(101806, 200394, -3180)); // Premo Prime (lv38)
		RAIDS.put(25398, new Location(5000, 189000, -3728)); // Eye of Beleth (lv35)
		RAIDS.put(25401, new Location(117812, 102948, -3140)); // Skyla (lv32)
		RAIDS.put(25404, new Location(36048, 191352, -2524)); // Corsair Captain Kylon (lv33)
		RAIDS.put(25501, new Location(48693, -106508, -1247)); // Grave Robber Boss Akata (30)
		RAIDS.put(25504, new Location(122771, -141022, -1016)); // Nellis' Vengeful Spirit (39)
		RAIDS.put(25506, new Location(127856, -160639, -1080)); // Rayito The Looter (37)
		// lvl40 list
		RAIDS.put(25007, new Location(124240, 75376, -2800)); // Retreat Spider Cletu (lv42)
		RAIDS.put(25026, new Location(92976, 7920, -3914)); // Katu Van Leader Atui (lv49)
		RAIDS.put(25044, new Location(107792, 27728, -3488)); // Barion (lv47)
		RAIDS.put(25047, new Location(116352, 27648, -3319)); // Karte (lv49)
		RAIDS.put(25057, new Location(107056, 168176, -3456)); // Biconne of Blue Sky (lv45)
		RAIDS.put(25064, new Location(92528, 84752, -3703)); // Mystic of Storm Teruk (lv40)
		RAIDS.put(25085, new Location(66944, 67504, -3704)); // Timak Orc Chief Ranger (lv44)
		RAIDS.put(25088, new Location(90848, 16368, -5296)); // Crazy Mechanic Golem (lv43)
		RAIDS.put(25099, new Location(64048, 16048, -3536)); // Rotten Tree Repiro (lv44)
		RAIDS.put(25102, new Location(113840, 84256, -2480)); // Shacram (lv45)
		RAIDS.put(25115, new Location(94000, 197500, -3300)); // Icarus Sample 1 (lv40)
		RAIDS.put(25134, new Location(87536, 75872, -3591)); // Leto Chief Talkin (lv40)
		RAIDS.put(25155, new Location(73520, 66912, -3728)); // Shaman King Selu (lv40)
		RAIDS.put(25158, new Location(77104, 5408, -3088)); // King Tarlk (lv48)
		RAIDS.put(25173, new Location(75968, 110784, -2512)); // Tiger King Karuta (lv45)
		RAIDS.put(25192, new Location(125920, 190208, -3291)); // Earth Protector Panathen (lv43)
		RAIDS.put(25208, new Location(73776, 201552, -3760)); // Water Couatle Ateka (lv40)
		RAIDS.put(25214, new Location(112112, 209936, -3616)); // Fafurion's Page Sika (lv40)
		RAIDS.put(25260, new Location(93120, 19440, -3607)); // Iron Giant Totem (lv45)
		RAIDS.put(25395, new Location(15000, 119000, -11900)); // Archon Suscepter (lv45)
		RAIDS.put(25410, new Location(72192, 125424, -3657)); // Road Scavenger Leader (lv40)
		RAIDS.put(25412, new Location(81920, 113136, -3056)); // Necrosentinel Royal Guard (lv47)
		RAIDS.put(25415, new Location(128352, 138464, -3467)); // Nakondas (lvl40)
		RAIDS.put(25418, new Location(62416, 8096, -3376)); // Dread Avenger Kraven (lv44)
		RAIDS.put(25420, new Location(42032, 24128, -4704)); // Orfen's Handmaiden (lv48)
		RAIDS.put(25431, new Location(79648, 18320, -5232)); // Flame Stone Golem (lv44)
		RAIDS.put(25437, new Location(67296, 64128, -3723)); // Timak Orc Gosmos (lv45)
		RAIDS.put(25438, new Location(107000, 92000, -2272)); // Thief Kelbar (lv44)
		RAIDS.put(25441, new Location(111440, 82912, -2912)); // Evil Spirit Cyrion (lv45)
		RAIDS.put(25456, new Location(133632, 87072, -3623)); // Mirror of Oblivion (lv49)
		RAIDS.put(25487, new Location(83056, 183232, -3616)); // Water Spirit Lian (lv40)
		RAIDS.put(25490, new Location(86528, 216864, -3584)); // Gwindorr (lv40)
		RAIDS.put(25498, new Location(126624, 174448, -3056)); // Fafurion's Henchman Istary (lv45)
		// lvl50 list
		RAIDS.put(25010, new Location(113920, 52960, -3735)); // Furious Thieles (lv55)
		RAIDS.put(25013, new Location(169744, 11920, -2732)); // Spiteful Soul of Peasant Leader (lv50)
		RAIDS.put(25029, new Location(54941, 206705, -3728)); // Atraiban (lv53)
		RAIDS.put(25032, new Location(88532, 245798, -10376)); // Eva's Guardian Millenu (58)
		RAIDS.put(25050, new Location(125520, 27216, -3632)); // Verfa (lv51)
		RAIDS.put(25067, new Location(94992, -23168, -2176)); // Shaka,  Captain of the Red Flag (lv52)
		RAIDS.put(25070, new Location(125600, 50100, -3600)); // Enchanted Forest Watcher Ruell (lv55)
		RAIDS.put(25089, new Location(165424, 93776, -2992)); // Soulless Wild Boar (lv59)
		RAIDS.put(25103, new Location(135872, 94592, -3735)); // Sorcerer Isirr (lv55)
		RAIDS.put(25119, new Location(121872, 64032, -3536)); // Berun,  Messenger of the Fairy Queen (lv50)
		RAIDS.put(25122, new Location(86300, -8200, -3000)); // Hopeful Refugee Leo (lv56)
		RAIDS.put(25131, new Location(75488, -9360, -2720)); // Carnage Lord Gato (lv50)
		RAIDS.put(25137, new Location(125280, 102576, -3305)); // Sephia,  Seer of Bereth (lv55)
		RAIDS.put(25159, new Location(124984, 43200, -3625)); // Unicorn Paniel (lv54)
		RAIDS.put(25176, new Location(92544, 115232, -3200)); // Black Lily (55)
		RAIDS.put(25182, new Location(41966, 215417, -3728)); // Demon Kurikups (59)
		RAIDS.put(25217, new Location(89904, 105712, -3292)); // Cursed Clara (lv50)
		RAIDS.put(25230, new Location(66672, 46704, -3920)); // Ragoth,  Seer of Timak (lv57)
		RAIDS.put(25238, new Location(155000, 85400, -3200)); // Abyss Brukunt (59)
		RAIDS.put(25241, new Location(165984, 88048, -2384)); // Harit Hero Tamash (lv55)
		RAIDS.put(25259, new Location(42050, 208107, -3752)); // Zaken's Butcher Krantz (lv55)
		RAIDS.put(25273, new Location(23800, 119500, -8976)); // Carnamakos (50)
		RAIDS.put(25277, new Location(54651, 180269, -4976)); // Lilith's Witch Marilion (lv50)
		RAIDS.put(25280, new Location(85622, 88766, -5120)); // Pagan Watcher Cerberon (lv55)
		RAIDS.put(25434, new Location(104096, -16896, -1803)); // Bandit Leader Barda (lv55)
		RAIDS.put(25460, new Location(150304, 67776, -3688)); // Deadman Ereve (lv51)
		RAIDS.put(25463, new Location(166288, 68096, -3264)); // Harit Guardian Garangky (lv56)
		RAIDS.put(25473, new Location(175712, 29856, -3776)); // Grave Robber Kim (lv52)
		RAIDS.put(25475, new Location(183568, 24560, -3184)); // Ghost Knight Kabed (lv55)
		RAIDS.put(25481, new Location(53517, 205413, -3728)); // Magus Kenishee (lv53)
		RAIDS.put(25484, new Location(43160, 220463, -3680)); // Zaken's Mate Tillion (lv50)
		RAIDS.put(25493, new Location(83174, 254428, -10873)); // Eva's Spirit Niniel (lv55)
		RAIDS.put(25496, new Location(88300, 258000, -10200)); // Fafurion's Envoy Pingolpin (lv52)
		RAIDS.put(25509, new Location(74000, -102000, 900)); // Dark Shaman Varangka (lv53)
		RAIDS.put(25512, new Location(96524, -111070, -3335)); // Gigantic Chaos Golem (lv52)
		RAIDS.put(29060, new Location(106000, -128000, -3000)); // Captain Of The Ice Queen's Royal Guard (lv59)
		// lvl60 list
		RAIDS.put(25016, new Location(76787, 245775, -10376)); // The 3rd Underwater Guardian (lv60)
		RAIDS.put(25051, new Location(117760, -9072, -3264)); // Rahha (lv65)
		RAIDS.put(25073, new Location(143265, 110044, -3944)); // Bloody Priest Rudelto (lv69)
		RAIDS.put(25106, new Location(173880, -11412, -2880)); // Lidia, Ghost of the Well (lv60)
		RAIDS.put(25125, new Location(170656, 85184, -2000)); // Fierce Tiger King Angel (lv65)
		RAIDS.put(25140, new Location(191975, 56959, -7616)); // Hekaton Prime (lv65)
		RAIDS.put(25162, new Location(194107, 53884, -4368)); // Giant Marpanak (lv60)
		RAIDS.put(25179, new Location(181814, 52379, -4344)); // Karum, Guardian Of The Statue Of the Giant (lv60)
		RAIDS.put(25226, new Location(104240, -3664, -3392)); // Roaring Lord Kastor (lv62)
		RAIDS.put(25233, new Location(185800, -26500, -2000)); // Spiteful Soul of Andras the Betrayer (lv69)
		RAIDS.put(25234, new Location(120080, 111248, -3047)); // Ancient Weird Drake (lv65)
		RAIDS.put(25255, new Location(170048, -24896, -3440)); // Gargoyle Lord Tiphon (lv65)
		RAIDS.put(25256, new Location(170320, 42640, -4832)); // Taik High Prefect Arak (lv60)
		RAIDS.put(25263, new Location(144400, -28192, -1920)); // Kernon's Faithful Servant Kelone (lv67)
		RAIDS.put(25322, new Location(93296, -75104, -1824)); // Demon's Agent Falston (lv66)
		RAIDS.put(25407, new Location(115072, 112272, -3018)); // Lord Ishka (lv60)
		RAIDS.put(25423, new Location(113600, 47120, -4640)); // Fairy Queen Timiniel (lv61)
		RAIDS.put(25444, new Location(113232, 17456, -4384)); // Enmity Ghost Ramdal (lv65)
		RAIDS.put(25467, new Location(186192, 61472, -4160)); // Gorgolos (lv64)
		RAIDS.put(25470, new Location(186896, 56276, -4576)); // Utenus, the Last Titan (lv66)
		RAIDS.put(25478, new Location(168288, 28368, -3632)); // Hisilrome, Priest of Shilen (lv65)
		RAIDS.put(29056, new Location(102800, -126000, -2500)); // Ice Fairy Sirra (lv60)
		// lvl70 list
		RAIDS.put(25035, new Location(180968, 12035, -2720)); // Shilen's Messenger Cabrio (lv70)
		RAIDS.put(25054, new Location(113432, 16403, 3960)); // Kernon (lv75)
		RAIDS.put(25092, new Location(116151, 16227, 1944)); // Korim (lv70)
		RAIDS.put(25109, new Location(152660, 110387, -5520)); // Cloe, Priest of Antharas (lv74)
		RAIDS.put(25126, new Location(116263, 15916, 6992)); // Longhorn Golkonda (lv79)
		RAIDS.put(25143, new Location(113102, 16002, 6992)); // Shuriel, Fire of Wrath (lv78)
		RAIDS.put(25163, new Location(130500, 59098, 3584)); // Roaring Skylancer (lv70)
		RAIDS.put(25198, new Location(102656, 157424, -3735)); // Fafurion's Messenger Loch Ness (lv70)
		RAIDS.put(25199, new Location(108096, 157408, -3688)); // Fafurion's Seer Sheshark (lv72)
		RAIDS.put(25202, new Location(119760, 157392, -3744)); // Crokian Padisha Sobekk (lv74)
		RAIDS.put(25205, new Location(123808, 153408, -3671)); // Ocean's Flame Ashakiel (lv76)
		RAIDS.put(25220, new Location(113551, 17083, -2120)); // Death Lord Hallate (lv73)
		RAIDS.put(25229, new Location(137568, -19488, -3552)); // Storm Winged Naga (lv75)
		RAIDS.put(25235, new Location(116400, -62528, -3264)); // Vanor Chief Kandra (lv72)
		RAIDS.put(25244, new Location(172000, 55000, -5400)); // Last Lesser Giant Olkuth (lv75)
		RAIDS.put(25245, new Location(187360, 45840, -5856)); // Last Lesser Giant Glaki (lv78)
		RAIDS.put(25248, new Location(127903, -13399, -3720)); // Doom Blade Tanatos (lv72)
		RAIDS.put(25249, new Location(147104, -20560, -3377)); // Palatanos of the Fearsome Power (lv75)
		RAIDS.put(25252, new Location(192376, 22087, -3608)); // Palibati Queen Themis (lv70)
		RAIDS.put(25266, new Location(188983, 13647, -2672)); // Bloody Empress Decarbia (lv75)
		RAIDS.put(25269, new Location(123504, -23696, -3481)); // Beast Lord Behemoth (lv70)
		RAIDS.put(25276, new Location(154088, -14116, -3736)); // Death Lord Ipos (lv75)
		RAIDS.put(25281, new Location(151053, 88124, -5424)); // Anakim's Nemesis Zakaron (lv70)
		RAIDS.put(25282, new Location(179311, -7632, -4896)); // Death Lord Shax (lv75)
		RAIDS.put(25290, new Location(186304, -43744, -3193)); // Daimon The White-Eyed (lvl78)
		RAIDS.put(25293, new Location(134672, -115600, -1216)); // Hestia, Guardian Deity of the Hot Springs (lv78)
		RAIDS.put(25296, new Location(158352, -121088, -2240)); // Icicle Emperor Bumpalump (lvl74)
		RAIDS.put(25325, new Location(91008, -85904, -2736)); // Barakiel, the Flame of Splendor (lv70)
		RAIDS.put(25328, new Location(59331, -42403, -3003)); // Eilhalder Von Hellman (lv71)
		RAIDS.put(25447, new Location(113200, 17552, -1424)); // Immortal Savior Mardil (lv71)
		RAIDS.put(25450, new Location(113600, 15104, 9559)); // Cherub Galaxia (lv79)
		RAIDS.put(25453, new Location(156704, -6096, -4185)); // Minas Anor (lv70)
		RAIDS.put(25523, new Location(168641, -60417, -3888)); // Plague Golem (lv73)
		RAIDS.put(25524, new Location(144143, -5731, -4722)); // Flamestone Giant (lv76)
		// lvl80 list
		RAIDS.put(25283, new Location(185060, -9622, -5104)); // Lilith (lv80)
		RAIDS.put(25286, new Location(185065, -12612, -5104)); // Anakim (lv80)
		RAIDS.put(25299, new Location(148154, -73782, -4364)); // Ketra's Hero Hekaton (lv80)
		RAIDS.put(25302, new Location(145553, -81651, -5464)); // Ketra's Commander Tayr (lv84)
		RAIDS.put(25305, new Location(144997, -84948, -5712)); // Ketra's Chief Brakki (lv87)
		RAIDS.put(25306, new Location(142368, -82512, -6487)); // Soul Of Fire Nastron (lv87)
		RAIDS.put(25309, new Location(115537, -39046, -1940)); // Varka's Hero Shadith (lv80)
		RAIDS.put(25312, new Location(109296, -36103, -648)); // Varka's Commander Mos (lv84)
		RAIDS.put(25315, new Location(105654, -42995, -1240)); // Varka's Chief Horus (lv87)
		RAIDS.put(25316, new Location(105452, -36775, -1050)); // Soul Of Water Ashutar (lv87)
		RAIDS.put(25319, new Location(185700, -106066, -6184)); // Ember (lv80)
		RAIDS.put(25527, new Location(3776, -6768, -3276)); // Uruka (lv80)
		RAIDS.put(25539, new Location(-17475, 253163, -3432)); // Typhoon (lv84)
		RAIDS.put(25623, new Location(-192361, 254528, 1598)); // Valdstone (lv80)
		RAIDS.put(25624, new Location(-174600, 219711, 4424)); // Rok (lv83)
		RAIDS.put(25625, new Location(-181989, 208968, 4424)); // Enira (lv85)
		RAIDS.put(25626, new Location(-252898, 235845, 5343)); // Dius (lv85)
		RAIDS.put(29062, new Location(-16373, -53562, -10197)); // Andreas Van Halter (lv80)
		RAIDS.put(29065, new Location(26528, -8244, -2007)); // Sailren (lv80)
	}

	private FastList<Integer>					_npcIds	= new FastList<Integer>();

	public RaidbossInfo(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);
		
		int[] NPC_IDS =
		{ 32337, 32338, 32339, 32340 };
		for (int i : NPC_IDS)
		{
			addStartNpc(i);
			addTalkId(i);
			_npcIds.add(i);
		}
		for (int i = 31729; i <= 31842; i++)
		{
			addStartNpc(i);
			addTalkId(i);
			_npcIds.add(i);
		}
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.startsWith("level"))
			return event;

		int bossId = Integer.parseInt(event);
		if (RAIDS.containsKey(bossId))
		{
			Location loc = RAIDS.get(bossId);
			st.addRadar(loc.getX(), loc.getY(), loc.getZ());
			st.exitQuest(true);
		}

		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player talker)
	{
		String htmltext = "";

		if (_npcIds.contains(npc.getNpcId()))
			htmltext = "info.htm";

		return htmltext;
	}

	public static void main(String[] args)
	{
		new RaidbossInfo(8000, QN, "Raid Boss Information", "custom");
	}
}
