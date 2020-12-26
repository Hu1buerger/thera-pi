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

-- Exportiere Struktur von Tabelle tibustest.hmr_diziplin
CREATE TABLE IF NOT EXISTS `hmr_diziplin` (
  `diziplin_id` char(1) NOT NULL,
  `diziplin_beschreibung` varchar(255) NOT NULL,
  `gueltig_von` varchar(255) NOT NULL,
  `gueltig_bis` varchar(255) NOT NULL,
  `ccid` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Welcher Tarifgruppe der Heilmittel gehört dazu';

-- Exportiere Daten aus Tabelle tibustest.hmr_diziplin: ~10 rows (ungefähr)
DELETE FROM `hmr_diziplin`;
/*!40000 ALTER TABLE `hmr_diziplin` DISABLE KEYS */;
INSERT INTO `hmr_diziplin` (`diziplin_id`, `diziplin_beschreibung`, `gueltig_von`, `gueltig_bis`, `ccid`) VALUES
	('5', 'Ergotherapie', '01.01.1900', '31.12.9999', 'Smutje'),
	('2', 'Physiotherapie', '01.01.1900', '31.12.9999', 'Smutje'),
	('7', 'Podologie', '01.01.1900', '31.12.9999', 'Smutje'),
	('1', 'Massagetherapie', '01.01.1900', '31.12.9999', 'Smutje'),
	('3', 'Logotherapie', '01.01.1900', '31.12.9999', 'Smutje'),
	('R', 'Reha', '01.01.1900', '31.12.9999', 'Smutje'),
	('A', 'Ernährungstherapie', '01.01.1900', '31.12.9999', 'Smutje'),
	('6', 'Krankenhaus', '01.01.1900', '31.12.9999', 'Smutje'),
	('8', 'Physiotherapie Kurort', '01.01.1900', '31.12.9999', 'Smutje'),
	('4', 'Sprachtherapie', '01.01.1900', '31.12.9999', 'Smutje');
/*!40000 ALTER TABLE `hmr_diziplin` ENABLE KEYS */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
