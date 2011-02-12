===================================
= CUSTOM CLAN HALLS MANAGERS v2.0 =
===================================

=======
Install
=======

> Remember always make a backup.
> Copy 'quests' directory content on your server folder (data/jscript/quests).
> Edit '__init__' file on 'quests' folder and add ...
	'778_ClanHallBuffer',
	'779_ClanHallGK',
	'780_ClanHallItems',
	'781_ClanHallWare',
	'782_ClanHallNothing' (Its important that the last one on that file have no ',')
> Copy 'warehouse' directory content on your server folder (data/html/warehouse).
	Now are only 4 ClanHall Managers working (12895, 12835, 8158, 7784).
	Make the other ones using the copy and paste method and taking the IDs from 'SQL Script' file.
> Copy 'skills' directory content on your server folder (stats/skills).
	Or edit your original one adding the Clan Hall Buffs Skills.
> Execute 'SQL Script' file fom 'sql' directory.
> Restart your server and ... Custom Clan Hall Managers installed! :)

===================
Clan Hall Functions
===================

> Only can be used by owners (1k karma points for others).
> Warehouse function ... Private and Clan.
> Support Magic function ... three types buffs (warriors, mistics, commons) for 15k adenas. Can be make on other ways ... edit the work :).
> Gatekeeper function ... main cities for 500 adenas. Edit other ways too.
> Create Object function ... very custom ... high prizes ... need more info.
> Others functions not available (yet I hope) ... Open inside doors, foreigns, decorations, regenerations, etc.

=====
Notes
=====

> This is a custom version of Clan Hall Managers, dont forget.
> The NPC are like "L2Warehouse" type because no other way to use warehouses links on NPCs. Well, there is one, yeah, making a quest code for that ... but I don find it yet!
> If a not Clan Hall owner use the NPCs, gains 1k karma points (that can be cancel, deleting 'st.getPlayer().setKarma(1000)' line on quest file ... But its funny the surprise! :)
> And well ... there is so many things that can be make on other and best way (like the '__init__' files) .. that ... you know ... edit and share yourself! :)

=======
Credits
=======

By Pivi_CT (Akatsuki Server - http://www.akatsuki.asturservers.com/).
Based on Work by X-Net (http://laii.ru/) on RageZone Forums for Clan Hall Buffs.
Based on Retail Screens info from Nemesiss and Ramon1 (http://forum.l2jdp.com/ Forums).

==============================
= Completed & Fixed By TOFIZ =
==============================
=    L2Emu-Project Pack      =
==============================