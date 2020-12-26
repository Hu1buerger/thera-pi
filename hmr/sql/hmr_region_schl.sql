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

-- Exportiere Struktur von Tabelle tibustest.hmr_region_schl
CREATE TABLE IF NOT EXISTS `hmr_region_schl` (
  `region` char(5) NOT NULL,
  `beschreibung` varchar(50) NOT NULL,
  `gueltig_von` date NOT NULL,
  `gueltig_bis` date DEFAULT NULL,
  `ccid` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Die Tarife beinhalten trotz bundeseinheitlichen Preisen weiterhin Regionen, wie z.B. Ost, West, Bundesländer(NW,BY,BW) und neuerdings auch Stadtgebiete wie Tübingen';

-- Exportiere Daten aus Tabelle tibustest.hmr_region_schl: ~18 rows (ungefähr)
DELETE FROM `hmr_region_schl`;
/*!40000 ALTER TABLE `hmr_region_schl` DISABLE KEYS */;
INSERT INTO `hmr_region_schl` (`region`, `beschreibung`, `gueltig_von`, `gueltig_bis`, `ccid`) VALUES
	('BW', 'Baden-Würtenberg', '1970-01-01', NULL, 'Smutje'),
	('BY', 'Bayern', '1970-01-01', NULL, 'Smutje'),
	('BE', 'Berlin', '1970-01-01', NULL, 'Smutje'),
	('BB', '?', '1970-01-01', NULL, 'Smutje'),
	('HB', 'Hansestadt Bremen', '1970-01-01', NULL, 'Smutje'),
	('HE', 'Hessen', '1970-01-01', NULL, 'Smutje'),
	('MV', 'Mecklenburg Vorpommern', '1970-01-01', NULL, 'Smutje'),
	('NW', 'Nordrhein', '1970-01-01', NULL, 'Smutje'),
	('NI', 'Niedersachsen', '1970-01-01', NULL, 'Smutje'),
	('RP', 'Rheinland Pfalz', '1970-01-01', NULL, 'Smutje'),
	('HH', 'Hansestadt Hamburg', '1970-01-01', NULL, 'Smutje'),
	('SL', 'Schleswig Holstein', '1970-01-01', NULL, 'Smutje'),
	('SN', '?', '1970-01-01', NULL, 'Smutje'),
	('ST', '?', '1970-01-01', NULL, 'Smutje'),
	('SH', '?', '1970-01-01', NULL, 'Smutje'),
	('West', 'Westdeutschland', '1970-01-01', NULL, 'Smutje'),
	('TH', 'Thüringen', '1970-01-01', NULL, 'Smutje'),
	('Ost', 'Ostdeutschland', '1970-01-01', NULL, 'Smutje');
/*!40000 ALTER TABLE `hmr_region_schl` ENABLE KEYS */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
