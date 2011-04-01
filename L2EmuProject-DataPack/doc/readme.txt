# ==================    #=================#
# L2Emu Project Pack    # DOCUMENT README #
# ==================    #=================#

Free Lineage II Server files, based on Java. Our mother fork is l2j-free and l2j/l2jdp.

L2Emu only supports L2Emu software obtained directly from L2Emu sources.

WARNING: L2EmuProject is NOT a plug and play software. You can find this part of README in all L2 packs. 
What does it mean? If you want a server just to play the game, go to the official server. 
If you want L2Emu or any L2 server you have to spend a lot of time on working with it. L2Emu is NOT exception from it.
If you are living in a dream about L2 packs, and you think everything is works and perfect. Wake up, stop it NOW.


# ====================
# I. LEGAL: GNU/GPL
# ====================

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free Software
Foundation, either version 3 of the License, or (at your option) any later
version.

This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
details.

You should have received a copy of the GNU General Public License along with
this program. If not, see <http://www.gnu.org/licenses/>.

Full GNU/GPL License is included in gpl.txt file.

Whereas L2Emu is distributed under the terms of GNU/GPL, we require you to:
A.) Preserve login notice. This gives us, L2Emu Developers, appropriate
credit for our hard work done during our free time without any
revenues.
 
B.) Do not distribute any extended data files with the server files in the same archive. 
NO world content should be incorporated in L2Emu distribution.
Server and DataPack may not be not be bundled or packaged.


# ====================
# II. REQUIREMENTS
# ====================

Hardware (Minimum):
- Pentium 4 CPU
- 1GB RAM
- 40GB Disk Space

Hardware (Optional):
- Dual or Quad Core CPU
- 2GB RAM or highter
- 80GB Disk Space or highter

Softwares:
- Java JDK 1.6
- MySQL
- Subversion
- Maven


# ======================
# III. STARTUP & INSTALL
# ======================

For startup and installation informations, please visit our wiki page.
- http://code.google.com/p/l2emu/wiki

# ====================
# VI. UPDATING
# ====================

It may arrive that you want to update your server to new version while
keeping old accounts. There are few steps you HAVE TO do in order to
keep the data accurate.

- You should ALWAYS look at ChangeLog before updating, sometimes a file
  format may change, so you will need to edit data manually to fit with
  new format.
- You should ALWAYS Backup all MySql data.
- You should ALWAYS Backup all Server and DataPack files.
- Back up all .properties files (don't forget to check if new server use same 
  format for those files)
- Download & UnZip new server code to the Server directory
- Download & UnZip new DataPack code to the Server directory
- Edit and run update in the tools folder
- Run newly installed server & enjoy ;)


# ====================
# V. CONTRIBUTING
# ====================

Anyone who wants to contribute to the project is encouraged to do so. Java
programming skills are not always required as L2Emu needs much more than java code.

If you created any source code that may be helpful please use the User Contributions
section on our forums. If you contributed good stuff that will be
accepted, you might be invited to join L2Emu Developer Team.

People willing to hang on chat and respond to user questions are also ALWAYS welcome ;)


# ====================
# VI. BUG REPORTING
# ====================

Bugs can be reported on our issue site.
- http://code.google.com/p/l2emu/issues/entry
Basic rules for reporting are:
- Please report only one bug per issue!!
- You must include the revision (changeset) number when reporting a bug!
- "The latest" does not mean anything when 5 more updates have been done since you set up the server.
If you are not sure if it should be reported here, make a post about it in the L2Emu forum.

Players should ALWAYS consult bugs with their Administrator/GM's and have them report it on our issue site. 
Some bugs may be caused by bad DataPack, server installation or modifications server owner has made. 
We can't help you in that case.

Please use the DataPacks issue tracker for reporting DataPack bugs.
Please do NOT report bugs related to unofficial add-ons to L2Emu. 
L2Emu issue tracker is NOT a place to fix that. Contact the person who made modification instead.


# ====================
# VII. CONTACT
# ====================

Our home/forum page is:
- http://www.l2emuproject.net
ChangeLog is:
- http://code.google.com/p/l2emu/source/list
Issue Tracker is:
- http://code.google.com/p/l2emu/issues/list
Wiki Page is:
- http://code.google.com/p/l2emu/wiki