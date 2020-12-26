-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server Version:               5.5.5-10.0.3-MariaDB - mariadb.org binary distribution
-- Server Betriebssystem:        Win32
-- HeidiSQL Version:             8.0.0.4396
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Exportiere Struktur von Tabelle tibustest.hmr_menge
CREATE TABLE IF NOT EXISTS `hmr_menge` (
  `hmr_menge_verord_kombination` int(10) NOT NULL COMMENT 'Schlüsselwert für die Nutzung',
  `hmr_menge_VO` int(10) NOT NULL COMMENT 'Höchstmenge pro Verordnung',
  `hmr_behandlungs_menge_bis_18_Jahre` int(10) NOT NULL COMMENT 'Höchstmenge unter 18 Jahren',
  `hmr_behandlungs_menge_ab_18_Jahre` int(10) NOT NULL COMMENT 'Höchstmenge ab 18 Jahren',
  `frequenz_von` int(11) NOT NULL COMMENT 'Mindestmenge Behandlungen pro Woche oder alle n-Wochen',
  `frequenz_bis` int(11) NOT NULL COMMENT 'Höchstmenge Behandlungen pro Woche oder alle n-Wochen',
  `frequenz_zeitraum` char(5) NOT NULL COMMENT 'Woche, NWoche',
  `gueltig_von` date NOT NULL COMMENT 'Gültig von Datum',
  `gueltig_bis` date DEFAULT NULL COMMENT 'Gültig bis Datum',
  `ccid` varchar(50) NOT NULL COMMENT 'Wer hats geändert'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='In dieser Tabelle werden die möglichen Mengenkombinationen abgebildet die in den Heilmittelrichtlinien vorgegeben sind';

-- Exportiere Daten aus Tabelle tibustest.hmr_menge: ~17 rows (ungefähr)
DELETE FROM `hmr_menge`;
/*!40000 ALTER TABLE `hmr_menge` DISABLE KEYS */;
INSERT INTO `hmr_menge` (`hmr_menge_verord_kombination`, `hmr_menge_VO`, `hmr_behandlungs_menge_bis_18_Jahre`, `hmr_behandlungs_menge_ab_18_Jahre`, `frequenz_von`, `frequenz_bis`, `frequenz_zeitraum`, `gueltig_von`, `gueltig_bis`, `ccid`) VALUES
	(1, 6, 18, 18, 1, 3, 'W', '2021-01-01', NULL, 'Smutje'),
	(2, 6, 18, 50, 1, 3, 'W', '2021-01-01', NULL, 'Smutje'),
	(3, 10, 30, 50, 1, 3, 'W', '2021-12-01', NULL, 'Smutje'),
	(4, 10, 30, 30, 1, 3, 'W', '2021-12-01', NULL, 'Smutje'),
	(5, 6, 18, 50, 1, 3, 'W', '2021-12-01', NULL, 'Smutje'),
	(6, 6, 18, 18, 1, 3, 'W', '2021-12-01', NULL, 'Smutje'),
	(7, 6, 30, 30, 1, 3, 'W', '2021-12-01', NULL, 'Smutje'),
	(8, 6, 0, 0, 4, 6, 'NW', '2021-12-01', NULL, 'Smutje'),
	(9, 10, 20, 20, 1, 3, 'W', '2021-12-01', NULL, 'Smutje'),
	(10, 10, 10, 10, 1, 7, 'T', '2021-12-01', NULL, 'Smutje'),
	(12, 20, 50, 50, 1, 3, 'W', '2021-12-01', NULL, 'Smutje'),
	(11, 10, 60, 60, 1, 3, 'W', '2021-12-01', NULL, 'Smutje'),
	(13, 20, 60, 60, 1, 3, 'W', '2021-12-01', NULL, 'Smutje'),
	(14, 10, 50, 50, 1, 3, 'W', '2021-12-01', NULL, 'Smutje'),
	(15, 10, 40, 60, 1, 3, 'W', '2021-12-01', NULL, 'Smutje'),
	(16, 10, 40, 40, 1, 3, 'W', '2021-12-01', NULL, 'Smutje'),
	(17, 1, 9999, 9999, 0, 12, 'NW', '2021-12-01', NULL, 'Smutje');
/*!40000 ALTER TABLE `hmr_menge` ENABLE KEYS */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
