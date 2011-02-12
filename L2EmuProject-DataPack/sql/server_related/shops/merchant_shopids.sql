DROP TABLE IF EXISTS `merchant_shopids`;
CREATE TABLE `merchant_shopids` (
  `shop_id` DECIMAL(9,0) NOT NULL DEFAULT 0,
  `npc_id` VARCHAR(9) DEFAULT NULL,
  PRIMARY KEY (`shop_id`)
) DEFAULT CHARSET=utf8;

INSERT INTO `merchant_shopids` VALUES

-- Golem shop
(13128001,'13128'),

-- General Shops
(3000100,'30001'),
(3000101,'30001'),
(3000200,'30002'),
(3000201,'30002'),
(3000300,'30003'),
(3000301,'30003'),
(3000400,'30004'),
(3004700,'30047'),
(3006000,'30060'),
(3006001,'30060'),
(3006002,'30060'),
(3006003,'30060'),
(3006100,'30061'),
(3006101,'30061'),
(3006102,'30061'),
(3006103,'30061'),
(3006200,'30062'),
(3006201,'30062'),
(3006202,'30062'),
(3006300,'30063'),
(3006301,'30063'),
(3007800,'30078'),
(3008100,'30081'),
(3008101,'30081'),
(3008200,'30082'),
(3008201,'30082'),
(3008202,'30082'),
(3008400,'30084'),
(3008401,'30084'),
(3008500,'30085'),
(3008501,'30085'),
(3008700,'30087'),
(3008701,'30087'),
(3008800,'30088'),
(3008801,'30088'),
(3009000,'30090'),
(3009001,'30090'),
(3009100,'30091'),
(3009101,'30091'),
(3009300,'30093'),
(3009400,'30094'),
(3009401,'30094'),
(3009402,'30094'),
(3013500,'30135'),
(3013501,'30135'),
(3013600,'30136'),
(3013601,'30136'),
(3013700,'30137'),
(3013800,'30138'),
(3013801,'30138'),
(3014700,'30147'),
(3014701,'30147'),
(3014800,'30148'),
(3014801,'30148'),
(3014900,'30149'),
(3014901,'30149'),
(3015000,'30150'),
(3016300,'30163'),
(3016400,'30164'),
(3016500,'30165'),
(3016501,'30165'),
(3016600,'30166'),
(3016601,'30166'),
(3017800,'30178'),
(3017801,'30178'),
(3017802,'30178'),
(3017803,'30178'),
(3017900,'30179'),
(3017901,'30179'),
(3017902,'30179'),
(3017903,'30179'),
(3018000,'30180'),
(3018001,'30180'),
(3018100,'30181'),
(3018101,'30181'),
(3018102,'30181'),
(3020700,'30207'),
(3020701,'30207'),
(3020702,'30207'),
(3020703,'30207'),
(3020800,'30208'),
(3020801,'30208'),
(3020802,'30208'),
(3020803,'30208'),
(3020900,'30209'),
(3023000,'30230'),
(3023001,'30230'),
(3023002,'30230'),
(3023003,'30230'),
(3023100,'30231'),
(3023101,'30231'),
(3025300,'30253'),
(3025301,'30253'),
(3025302,'30253'),
(3025303,'30253'),
(3025400,'30254'),
(3025401,'30254'),
(3029400,'30294'),
(3029401,'30294'),
(3030100,'30301'),
(3030101,'30301'),
(3031300,'30313'),
(3031400,'30314'),
(3031401,'30314'),
(3031402,'30314'),
(3031500,'30315'),
(3032100,'30321'),
(3032101,'30321'),
(3032102,'30321'),
(3032103,'30321'),
(3038700,'30387'),
(3042000,'30420'),
(3043600,'30436'),
(3043700,'30437'),
(3051600,'30516'),
(3051601,'30516'),
(3051700,'30517'),
(3051701,'30517'),
(3051800,'30518'),
(3051900,'30519'),
(3051901,'30519'),
(3055800,'30558'),
(3055801,'30558'),
(3055900,'30559'),
(3055901,'30559'),
(3056000,'30560'),
(3056001,'30560'),
(3056100,'30561'),
(3068400,'30684'),
(3068401,'30684'),
(3068402,'30684'),
(3068403,'30684'),
(3073100,'30731'),
(3082700,'30827'),
(3082800,'30828'),
(3082900,'30829'),
(3083000,'30830'),
(3083100,'30831'),
(3083400,'30834'),
(3083401,'30834'),
(3083700,'30837'),
(3083701,'30837'),
(3083702,'30837'),
(3083703,'30837'),
(3083800,'30838'),
(3083801,'30838'),
(3083802,'30838'),
(3083803,'30838'),
(3083900,'30839'),
(3084000,'30840'),
(3084100,'30841'),
(3084101,'30841'),
(3084200,'30842'),
(3084201,'30842'),
(3086900,'30869'),
(3087900,'30879'),
(3089000,'30890'),
(3089001,'30890'),
(3089002,'30890'),
(3089003,'30890'),
(3089100,'30891'),
(3089101,'30891'),
-- (3089102,'30891'), Commented to cleanup loading error messages. Uncomment if you have the info to finish it
-- (3089103,'30891'), Commented to cleanup loading error messages. Uncomment if you have the info to finish it
(3089200,'30892'),
(3089201,'30892'),
(3089202,'30892'),
(3089300,'30893'),
(3089301,'30893'),
(3104400,'31044'),
(3104500,'31045'),
(3106700,'31067'),
(3125600,'31256'),
(3125601,'31256'),
(3125700,'31257'),
(3125701,'31257'),
(3125800,'31258'),
(3125801,'31258'),
(3125900,'31259'),
(3125901,'31259'),
(3126000,'31260'),
(3126100,'31261'),
(3126200,'31262'),
(3126300,'31263'),
(3126301,'31263'),
(3126500,'31265'),
(3127400,'31274'),
(3130000,'31300'),
(3130001,'31300'),
(3130002,'31300'),
(3130003,'31300'),
(3130100,'31301'),
(3130101,'31301'),
(3130102,'31301'),
(3130103,'31301'),
(3130200,'31302'),
(3130201,'31302'),
(3130202,'31302'),
(3130203,'31302'),
(3130300,'31303'),
(3130301,'31303'),
(3130302,'31303'),
(3130303,'31303'),
(3130400,'31304'),
(3130401,'31304'),
(3130500,'31305'),
(3130501,'31305'),
(3130600,'31306'),
(3130700,'31307'),
(3130701,'31307'),
(3130900,'31309'),
(3131800,'31318'),
(3131900,'31319'),
(3135100,'31351'),
(3136600,'31366'),
(3138600,'31386'),
(3141300,'31413'),
(3141400,'31414'),
(3141500,'31415'),
(3141600,'31416'),
(3141700,'31417'),
(3141800,'31418'),
(3141900,'31419'),
(3142000,'31420'),
(3142100,'31421'),
(3142200,'31422'),
(3142300,'31423'),
(3142400,'31424'),
(3142500,'31425'),
(3142600,'31426'),
(3142700,'31427'),
(3142800,'31428'),
(3142900,'31429'),
(3143000,'31430'),
(3143100,'31431'),
(3143200,'31432'),
(3143300,'31433'),
(3143400,'31434'),
(3143500,'31435'),
(3143600,'31436'),
(3143700,'31437'),
(3143800,'31438'),
(3143900,'31439'),
(3144000,'31440'),
(3144100,'31441'),
(3144200,'31442'),
(3144300,'31443'),
(3144400,'31444'),
(3144500,'31445'),
(3166600,'31666'),
(3166700,'31667'),
(3166800,'31668'),
(3166900,'31669'),
(3167000,'31670'),
(3194500,'31945'),
(3194501,'31945'),
(3194502,'31945'),
(3194503,'31945'),
(3194600,'31946'),
(3194601,'31946'),
(3194602,'31946'),
(3194603,'31946'),
(3194700,'31947'),
(3194701,'31947'),
(3194702,'31947'),
(3194703,'31947'),
(3194800,'31948'),
(3194801,'31948'),
(3194802,'31948'),
(3194803,'31948'),
(3194900,'31949'),
(3194901,'31949'),
(3195000,'31950'),
(3195001,'31950'),
(3195100,'31951'),
(3195200,'31952'),
(3195201,'31952'),
(3195400,'31954'),
(3196200,'31962'),
(3196300,'31963'),
(3197300,'31973'),
(3198000,'31980'),
(3210500,'32105'),
(3210600,'32106'),
(3216400,'32164'),
(3216401,'32164'),
(3216500,'32165'),
(3216501,'32165'),
(3216600,'32166'),
(3216700,'32167'),
(3216800,'32168'),
(3216900,'32169'),
(382,'31380'),
(383,'31373'),

-- Mercenary Managers
(351021,'35102'),
(351441,'35144'),
(351861,'35186'),
(352281,'35228'),
(352761,'35276'),
(353181,'35318'),
(353651,'35365'),

-- Fishermens
(3156200,'31562'),
(3156300,'31563'),
(3156400,'31564'),
(3156500,'31565'),
(3156600,'31566'),
(3156700,'31567'),
(3156800,'31568'),
(3156900,'31569'),
(3157000,'31570'),
(3157100,'31571'),
(3157200,'31572'),
(3157300,'31573'),
(3157400,'31574'),
(3157500,'31575'),
(3157600,'31576'),
(3157700,'31577'),
(3157800,'31578'),
(3157900,'31579'),
(3169600,'31696'),
(3169700,'31697'),
(3198900,'31989'),
(3234800,'32348'),

-- Old GM Shops (some still used temp)
(9001,'gm'),
(9002,'gm'),
(9003,'gm'),
(9004,'gm'),
(9005,'gm'),
(9006,'gm'),
(9007,'gm'),
(9008,'gm'),
(9009,'gm'),
(9010,'gm'),
(9011,'gm'),
(9012,'gm'),
(9013,'gm'),
(9014,'gm'),
(9015,'gm'),
(9016,'gm'),
(9017,'gm'),
(9018,'gm'),
(9019,'gm'),
(9020,'gm'),
(9021,'gm'),
(9022,'gm'),
(9023,'gm'),
(9024,'gm'),
(9025,'gm'),
(9026,'gm'),
(9027,'gm'),
(9028,'gm'),
(9029,'gm'),
(9030,'gm'),
(9031,'gm'),
(9032,'gm'),
(9033,'gm'),
(9034,'gm'),
(9035,'gm'),
(9036,'gm'),
(9037,'gm'),
(9038,'gm'),
(9039,'gm'),
(9040,'gm'),
(9041,'gm'),
(9042,'gm'),
(9043,'gm'),
(9044,'gm'),
(9045,'gm'),
(9046,'gm'),
(9047,'gm'),
(9048,'gm'),
(9049,'gm'),
(9050,'gm'),
(9051,'gm'),
(9052,'gm'),
(9053,'gm'),
(71022,'gm'),
(71028,'gm'),
(71029,'gm'),
(30040,'gm'),
(30041,'gm'),
(30042,'gm'),
(30043,'gm'),
(30044,'gm'),
(30045,'gm'),
(30046,'gm'),
(30047,'gm'),
(30048,'gm'),
(30049,'gm'),
(30050,'gm'),
(30051,'gm'),
(30052,'gm'),
(30053,'gm'),
(30054,'gm'),
(30055,'gm'),
(30056,'gm'),
(30057,'gm'),
(30058,'gm'),
(30059,'gm'),
(300530,'gm'),
(300536,'gm'),
(300539,'gm'),
(300540,'gm'),
(300541,'gm'),
(300542,'gm'),
(300410,'gm'),
(300510,'gm'),
-- (9157,'gm'),

-- Castle
(335103, '35103'),
(335145, '35145'),
(335187, '35187'),
(335229, '35229'),
(335230, '35230'),
(335231, '35231'),
(335277, '35277'),
(335319, '35319'),
(335366, '35366'),
(335512, '35512'),
(335558, '35558'),
(335644, '35644'),
(335645, '35645'),
(336456, '36456'),

-- Castles Lord's Certificates
-- (63881, 35100),
-- (63882, 35142),
-- (63883, 35184),
-- (63884, 35226),
-- (63885, 35274),
-- (63886, 35316),
-- (63887, 35363),
-- (63888, 35509),
-- (63889, 35555),

-- Castles Item creation
(351001,'35100'),
(351421,'35142'),
(351841,'35184'),
(352261,'35226'),
(352741,'35274'),
(353161,'35316'),
(353631,'35363'),
(355091,'35509'),
(355551,'35555'),

-- Clan Halls Item creation
(135445, '35445'),
(235445, '35445'),
(335445, '35445'),
(135453, '35453'),
(235453, '35453'),
(335453, '35453'),
(135455, '35455'),
(235455, '35455'),
(335455, '35455'),
(135451, '35451'),
(235451, '35451'),
(335451, '35451'),
(135457, '35457'),
(235457, '35457'),
(335457, '35457'),
(135459, '35459'),
(235459, '35459'),
(335459, '35459'),
(135383, '35383'),
(235383, '35383'),
(335383, '35383'),
(135398, '35398'),
(235398, '35398'),
(335398, '35398'),
(135400, '35400'),
(235400, '35400'),
(335400, '35400'),
(135392, '35392'),
(235392, '35392'),
(335392, '35392'),
(135394, '35394'),
(235394, '35394'),
(335394, '35394'),
(135396, '35396'),
(235396, '35396'),
(335396, '35396'),
(135384, '35384'),
(235384, '35384'),
(335384, '35384'),
(135390, '35390'),
(235390, '35390'),
(335390, '35390'),
(135386, '35386'),
(235386, '35386'),
(335386, '35386'),
(135388, '35388'),
(235388, '35388'),
(335388, '35388'),
(135407, '35407'),
(235407, '35407'),
(335407, '35407'),
(135403, '35403'),
(235403, '35403'),
(335403, '35403'),
(135405, '35405'),
(235405, '35405'),
(335405, '35405'),
(135421, '35421'),
(235421, '35421'),
(335421, '35421'),
(135439, '35439'),
(235439, '35439'),
(335439, '35439'),
(135441, '35441'),
(235441, '35441'),
(335441, '35441'),
(135443, '35443'),
(235443, '35443'),
(335443, '35443'),
(135447, '35447'),
(235447, '35447'),
(335447, '35447'),
(135449, '35449'),
(235449, '35449'),
(335449, '35449'),
(135467, '35467'),
(235467, '35467'),
(335467, '35467'),
(135465, '35465'),
(235465, '35465'),
(335465, '35465'),
(135463, '35463'),
(235463, '35463'),
(335463, '35463'),
(135461, '35461'),
(235461, '35461'),
(335461, '35461'),
(335566, '35566'),
(235566, '35566'),
(135566, '35566'),
(335568, '35568'),
(235568, '35568'),
(135568, '35568'),
(335570, '35570'),
(235570, '35570'),
(135570, '35570'),
(335572, '35572'),
(235572, '35572'),
(135572, '35572'),
(335574, '35574'),
(235574, '35574'),
(135574, '35574'),
(335576, '35576'),
(235576, '35576'),
(135576, '35576'),
(335578, '35578'),
(235578, '35578'),
(135578, '35578'),
(235580, '35580'),
(135580, '35580'),
(335580, '35580'),
(335582, '35582'),
(235582, '35582'),
(135582, '35582'),
(135584, '35584'),
(235584, '35584'),
(335584, '35584'),
(335586, '35586'),
(135586, '35586'),
(235586, '35586'),
(355111, '35511'),
(355571, '35557');

-- GM SHOP IDs START (99xx range)--
INSERT INTO `merchant_shopids` VALUES
('9901', 'gm'), -- Forgotten Scrolls
('9902', 'gm'), -- Life Stones
('9903', 'gm'), -- Elemental
('9904', 'gm'), -- Codex Books
('9905', 'gm'), -- Divine Inspiration Books
('9906', 'gm'), -- Transform Sealbooks
('9907', 'gm'), -- S84 Vesper Weapons
('9908', 'gm'), -- S84 Vesper Jewels
('9909', 'gm'), -- Cloaks
('9910', 'gm'), -- Belts
('9911', 'gm'), -- Magic Pins
('9912', 'gm'), -- Magic Pouches
('9913', 'gm'), -- Shirts
('9914', 'gm'), -- Bracelets
('9915', 'gm'), -- Talisman
('9916', 'gm'), -- Currency
('9917', 'gm'), -- Boss Jewels
('9918', 'gm'), -- Pets
('9919', 'gm'), -- Castle Circlets
('9920', 'gm'), -- Hair Accessory
('9921', 'gm'), -- Fly Transform Books
('9922', 'gm'), -- Fishing Potions
('9923', 'gm'), -- Dynasty Weapons
('9924', 'gm'), -- Icarus Weapons
('9925', 'gm'), -- Monster Weapons
('9926', 'gm'), -- Sigils
('9927', 'gm'), -- SA Crystals
('9928', 'gm'), -- Potions
('9929', 'gm'), -- Elixirs
('9930', 'gm'), -- Scrolls
('9931', 'gm'), -- Enchant Scrolls
('9932', 'gm'), -- Pet Gear
('9933', 'gm'), -- S80 Dynasty Recipes
('9934', 'gm'), -- S80 Dynasty Parts
('9935', 'gm'), -- S80 Icarus Recipes
('9936', 'gm'), -- S80 Icarus Parts
('9937', 'gm'), -- Craft Materials
('9938', 'gm'), -- Shots
('9939', 'gm'), -- Crystals/Gemstones
('9940', 'gm'), -- S grade Parts
('9941', 'gm'), -- S grade Recipes
('9942', 'gm'), -- A Grade Recipes
('9943', 'gm'), -- A Grade Parts 
('9944', 'gm'), -- Other Circlets
('9945', 'gm'), -- Buff Scrolls (event)
('9946', 'gm'), -- B Grade Recipes
('9947', 'gm'), -- B Grade Parts
('9948', 'gm'), -- Dyes
('9949', 'gm'), -- Fishing Rods
('9950', 'gm'), -- Fishing Gear
('9951', 'gm'), -- NG set
('9952', 'gm'), -- D set
('9953', 'gm'), -- C set
('9954', 'gm'), -- B set
('9955', 'gm'), -- A set
('9956', 'gm'), -- S set
('9957', 'gm'), -- S80 set
('9958', 'gm'), -- S84 set
('9959', 'gm'), -- Apella set
('9960', 'gm'), -- NG Jewel
('9961', 'gm'), -- D Jewel
('9962', 'gm'), -- C Jewel
('9963', 'gm'), -- B Jewel
('9964', 'gm'), -- A Jewel
('9965', 'gm'), -- S Jewel
('9966', 'gm'), -- S80 Jewel
('9967', 'gm'), -- Fishes
('9968', 'gm'), -- Designs (Gracia Items)
('9969', 'gm'), -- Materials for Designs (Gracia Items)
('9970', 'gm'), -- Mercenary Transformation Scrolls
('9971', 'gm'), -- Disguise Scroll
('9972', 'gm'), -- Santa Weapons
('9973', 'gm'), -- Baguette Weapons
('9974', 'gm'), -- Limited Time Weapons - 60 minutes
('9975', 'gm'), -- Limited Time Weapons - 4 hours
('9976', 'gm'), -- Transformation Sticks
('9977', 'gm'), -- Misc Event Items
('9978', 'gm'), -- Misc Consumables
('9979', 'gm'), -- Arrows
('9980', 'gm'), -- Infinity Weapons
('9981', 'gm'), -- Vesper PVP Weapons
('9982', 'gm'), -- Dynasty PVP Weapons
('9983', 'gm'), -- Icarus PVP Weapons
('9984', 'gm'), -- S PVP Weapons
('9985', 'gm'), -- A PVP Weapons
('9986', 'gm'), -- Hair Accessory Part 2
('9987', 'gm'), -- Territory Jewels
('9988', 'gm'), -- Territory Items
('9989', 'gm'), -- Fortress Misc
('9990', 'gm'), -- Territory Weapons
('9991', 'gm'), -- Territory Wards
('9992', 'gm'), -- Territory Flags
('9993', 'gm'), -- Seeds
('9994', 'gm'), -- Castle Guards
('9995', 'gm'), -- Magic Clip
('9996', 'gm'), -- Magic Ornament
('9997', 'gm'), -- A Weapons
('9998', 'gm'), -- S Weapons
('9999', 'gm'), -- Masterwork Jewels
('10000', 'gm'), -- Masterwork Armor B
('10001', 'gm'), -- Masterwork Armor A
('10002', 'gm'), -- Masterwork Armor S
('10003', 'gm'), -- Masterwork Armor S80
('10004', 'gm'), -- Agathions
('10005', 'gm'), -- Mounts
('10006', 'gm'); -- Event Masks

-- L2J-Free Add-ons
-- Edit: Left these here in case you wanna add them to the new GM Shop
-- GM Shop Addition by Skatershi

INSERT INTO `merchant_shopids` VALUES
(71040, 'gm'), -- Interlude Spellbooks
(71041, 'gm'), -- Interlude Recipes
(71042, 'gm'), -- CT1/CT1.5 Battle Manuals
(71043, 'gm'), -- CT1 Spellbooks
(71044, 'gm'), -- CT1/CT1.5 Recipes
(71045, 'gm'), -- CT1/CT1.5 Transformation Scrolls
(71046, 'gm'), -- CT1/CT1.5 Shadow Items
(71047, 'gm'), -- CT1/CT1.5 Bracelet
(71048, 'gm'), -- CT1/CT1.5 Special Jewels
(71049, 'gm'), -- CT1/CT1.5 Talisman
(71050, 'gm'), -- CT1/CT1.5 Hair Accessories
(71051, 'gm'), -- CT1.5 Weapons (Monster Only)
(71052, 'gm'), -- CT1.5 Shirt
(71053, 'gm'), -- CT1.5 Forgotten Scrolls
(71054, 'gm'); -- CT1.5 Others

-- L2EmuProject //gmshop - items
INSERT INTO `merchant_shopids` VALUES
(1015,'gm'),
(1020,'gm');

-- Rainbow Springs Fisherman
INSERT INTO `merchant_shopids` VALUES
(3200700,'32007');