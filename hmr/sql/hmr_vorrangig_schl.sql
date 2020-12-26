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

-- Exportiere Struktur von Tabelle tibustest.hmr_vorrangig_schl
CREATE TABLE IF NOT EXISTS `hmr_vorrangig_schl` (
  `vorrangig` decimal(1,0) NOT NULL,
  `Beschreibung` char(50) NOT NULL,
  `gueltig_von` date NOT NULL,
  `gueltig_bis` date DEFAULT NULL,
  `ccid` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='In dieser Tabelle werden die Schlüsselwerte für heilmittel bzgl. ihrer vorrangigen Verschreibungsmöglichkeit beschrieben und abgelegt';

-- Exportiere Daten aus Tabelle tibustest.hmr_vorrangig_schl: ~2 rows (ungefähr)
DELETE FROM `hmr_vorrangig_schl`;
/*!40000 ALTER TABLE `hmr_vorrangig_schl` DISABLE KEYS */;
INSERT INTO `hmr_vorrangig_schl` (`vorrangig`, `Beschreibung`, `gueltig_von`, `gueltig_bis`, `ccid`) VALUES
	(0, 'nicht vorrangig', '1970-01-01', NULL, 'Smutje'),
	(1, 'vorrangig', '1970-01-01', NULL, 'Smutje');
/*!40000 ALTER TABLE `hmr_vorrangig_schl` ENABLE KEYS */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
