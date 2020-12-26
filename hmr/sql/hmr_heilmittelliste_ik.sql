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

-- Exportiere Struktur von Tabelle tibustest.hmr_heilmittelliste_ik
CREATE TABLE IF NOT EXISTS `hmr_heilmittelliste_ik` (
  `ik` char(9) NOT NULL COMMENT 'zu welcher IK-Nr gehört die Liste',
  `heilmittelposition` char(5) NOT NULL COMMENT '5-stellige Heilmittelposition',
  `kurzbezeichnung` char(100) NOT NULL COMMENT 'beschreibender Text',
  `gueltig_von` date NOT NULL COMMENT 'Gültig-von-Datum',
  `gueltig_bis` date DEFAULT NULL COMMENT 'Gültig-bis-Datum',
  `ccid` varchar(50) NOT NULL COMMENT 'Change-Control-Id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='In dieser Tabelle stehen die "individuellen" Preise, Kurzbezeichnungen etc. die in dieser Praxis genutzt werden. Die hier enthaltenen Daten kommen ursprünglich aus hmr_heilmittelliste';

-- Exportiere Daten aus Tabelle tibustest.hmr_heilmittelliste_ik: ~0 rows (ungefähr)
DELETE FROM `hmr_heilmittelliste_ik`;
/*!40000 ALTER TABLE `hmr_heilmittelliste_ik` DISABLE KEYS */;
INSERT INTO `hmr_heilmittelliste_ik` (`ik`, `heilmittelposition`, `kurzbezeichnung`, `gueltig_von`, `gueltig_bis`, `ccid`) VALUES
	('123456789', '10102', 'UWM', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '10106', 'KMT', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '10107', 'BGM', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '10108', 'SM-PM-CM', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '10201', 'MLD-45', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '10202', 'MLD-60', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '10204', 'Komp.Band', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '10205', 'MLD-30', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '10301', 'Übungsbehandlung (EB)', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '10305', 'Übungsbhdlg. Bewegungsbad (EB)', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '10306', 'Chirogymnastik', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '10307', 'Schlingentisch', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '10401', 'Übungsbehandlung – Gruppenbehandlung mit 2 bis 5 Patienten', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '10402', 'Bewegungsbad bis 3 Pers.', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '10403', 'Bewegungsbad bis 5 Pers.', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '10404', 'Bewegungsbad bis 7 Pers.', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '10405', 'Bewegungsbad 4 -5 Pers.', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11301', 'ElektrotherapieBefunderhebung', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11302', 'Elektrotherapie einzelner Körperteile', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11303', 'Elektrostimulation bei Lähmung', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11310', 'Hydroelektrische Teillbad (z.B. Zwei-/Vierzellenbad)', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11312', 'Hydroelektrisches Vollbad (z.B. Stangerbad)', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11501', 'Warmpackungen', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11517', 'Wärmetherapie (Heißluft)', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11530', 'Heiße Rolle', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11531', 'Ultraschall-Wärmetherapie', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11532', 'Vollbad mit Peloiden z. B. Fango, Schlick oder Moor', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11533', 'Teilbad mit Peloiden z. B. Fango, Schlick oder Moor', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11534', 'Kältetherapie bei einem oder mehreren Körperteil(en)', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11703', 'Medizinische Bäder Sitzbad', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11705', 'Medizinische Bäder Vollbad', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11708', 'Medizinische Bäder Medizinisches Vollbad mit Zusatz und Ruhe', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11710', 'Medizinische Bäder Gashaltiges Bad ohne Zusatz', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11712', 'Medizinische Bäder Gashaltiges Bad mit Zusatz und Ruhe', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11714', 'Medizinische Bäder Kohlensäurebad', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11720', 'Medizinische Bäder Zusätze', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11732', 'CO2-Trockenbad', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11733', 'CO2-Trockenbad', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11801', 'Inhalationstherapie (EB)', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11901', 'Geburtsvorbereitung', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '11904', 'Wochenbettgymnastik Gruppe', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '19701', 'Therapeut - Arzt - Bericht', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '19901', 'Hausbesuch/Wegegeld Hausbesuch', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '19902', 'Hausbesuch/Wegegeld Hausbesuch mehrerer Patienten (z.B. in einer Einrichtung/Gemeinschaft)', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '19906', 'Hausbesuch/Wegegeld Wegegeldpauschale', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '19907', 'Hausbesuch/Wegegeld Wegegeld je Kilometer', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '19909', 'Hausbesuch/Wegegeld Wegegeld pausch. in geschl. Ortschaften', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '19910', 'Hausbesuch/Wegegeld Wegegeld je Kilometer bei Überschreiten der Ortsgrenze', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '19911', 'Hausbesuch/Wegegeld Wegegeld pausch. bis 15 km', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '19933', 'Hausbesuch (Einzelpatient)', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '19934', 'Hausbesuch (Einrichtung)', '2019-07-01', '9999-12-31', 'Smutje\r'),
	('123456789', '19935', 'Hausbesuch (Einrichtung/Einzelbes.)', '2019-07-01', '9999-12-31', 'Smutje\r');
/*!40000 ALTER TABLE `hmr_heilmittelliste_ik` ENABLE KEYS */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
