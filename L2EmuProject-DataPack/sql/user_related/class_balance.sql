CREATE TABLE IF NOT EXISTS `class_balance` (
	`class_id` tinyint NOT NULL ,
	`FxH` decimal(5,2) NOT NULL DEFAULT 1.00,
	`FxL` decimal(5,2) NOT NULL DEFAULT 1.00,
	`FxR` decimal(5,2) NOT NULL DEFAULT 1.00,
	`MxH` decimal(5,2) NOT NULL DEFAULT 1.00,
	`MxL` decimal(5,2) NOT NULL DEFAULT 1.00,
	`MxR` decimal(5,2) NOT NULL DEFAULT 1.00,
	PRIMARY KEY (`class_id`)
) DEFAULT CHARSET=utf8;

INSERT IGNORE INTO class_balance VALUES
(0, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(1, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(2, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(3, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(4, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(5, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(6, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(7, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(8, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(9, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(10, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(11, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(12, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(13, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(14, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(15, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(16, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(17, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(18, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(19, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(20, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(21, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(22, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(23, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(24, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(25, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(26, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(27, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(28, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(29, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(30, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(31, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(32, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(33, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(34, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(35, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(36, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(37, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(38, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(39, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(40, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(41, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(42, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(43, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(44, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(45, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(46, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(47, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(48, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(49, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(50, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(51, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(52, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(53, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(54, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(55, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(56, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(57, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(88, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(89, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(90, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(91, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(92, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(93, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(94, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(95, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(96, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(97, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(98, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(99, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00);

INSERT IGNORE INTO class_balance VALUES
(100, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(101, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(102, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(103, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(104, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(105, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(106, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(107, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(108, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(109, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(110, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(111, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(112, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(113, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(114, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(115, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(116, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(117, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(118, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(123, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(124, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(125, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(126, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(127, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(128, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(129, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(130, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(131, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(132, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(133, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(134, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(135, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00),
(136, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00);
