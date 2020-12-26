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

-- Exportiere Struktur von Tabelle tibustest.hmr_diagnosegruppe
CREATE TABLE IF NOT EXISTS `hmr_diagnosegruppe` (
  `diagnosegruppe` char(3) NOT NULL COMMENT 'Diagnosegruppen Kurztext',
  `diagnosegruppe_beschreibung` varchar(255) NOT NULL COMMENT 'Diagnosengruppe Langtext',
  `anzahl_vo` decimal(3,0) DEFAULT NULL COMMENT 'Anzahl der möglichen VO',
  `max_behandlungen` decimal(3,0) DEFAULT NULL COMMENT 'maximale Anzahl Behandlungen',
  `gueltig_von` date NOT NULL COMMENT 'Gültig von Datum',
  `gueltig_bis` date NOT NULL COMMENT 'Gültig bis Datum',
  `ccid` varchar(50) NOT NULL COMMENT 'Change-Control-Id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

-- Exportiere Daten aus Tabelle tibustest.hmr_diagnosegruppe: ~0 rows (ungefähr)
DELETE FROM `hmr_diagnosegruppe`;
/*!40000 ALTER TABLE `hmr_diagnosegruppe` DISABLE KEYS */;
INSERT INTO `hmr_diagnosegruppe` (`diagnosegruppe`, `diagnosegruppe_beschreibung`, `anzahl_vo`, `max_behandlungen`, `gueltig_von`, `gueltig_bis`, `ccid`) VALUES
	('AT', 'Störung der Atmung', 6, 18, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('CF', 'Mukoviszidose (Cystische Fibrose)', NULL, NULL, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('CS', 'Chronifiziertes Schmerzsyndrom', 6, 18, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('DF', 'diabetische Neuropathie mit oder ohne Angiopathie', 6, NULL, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('EN1', 'ZNS-Erkrankungen (Gehirn) Entwicklungsstörungen', 10, 40, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('EN2', 'ZNS-Erkrankungen (Rückenmark)/ Neuromuskuläre Erkrankungen', 10, 40, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('EN3', 'Periphere Nervenläsionen/ Muskelerkrankungen', 10, 20, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('EX', 'Erkrankung der Extremitäten und des Beckens', 6, 18, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('GE', 'Arterielle Gefäßerkrankungen (bei konventioneller Behandlung, nach interventioneller/operativer Behandlung)', 6, 18, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('LY', 'Lymphabflusstörungen', 6, 30, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('NF', 'Krankhafte Schädigung am Fuß als Folge einer sensiblen oder sensomotorischen Neuropathie (primär oder sekundär)', 6, NULL, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('PN', 'Perophere Nervenläsionen Muskelerkrankungen', 10, 30, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('PS1', 'Entwicklungs-, Verhaltens- und emotionale Störungen mit Beginn in Kindheit und Jugend', 10, 40, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('PS2', 'Neurotische, Belastungs-, somatoforme und Persönlichkeitsstörungen', 10, 40, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('PS3', 'Wahnhafte und affektive Störungen/ Abhängigkeitserkrankungen Schizophrenie / schizotype und wahnhafte Störungen / Affektive Störungen / Psychische und Verhaltensstörungen durch psychotrope Substanzen', 10, 40, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('PS4', 'Dementielle Syndrome', 10, 40, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('QF', 'Krankhafte Schädigung am Fuß als Folge einer sensiblen oder sensomotorischen Neuropathie (primär oder sekundär)', 6, NULL, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('RE1', 'Störungen des Redeflusses Stottern', 10, 50, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('RE2', 'Störungen des Redeflusses Poltern', 10, 20, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('SAS', 'Seltene angeborene Stoffwechselerkrankungen wenn Ernährungstherapie als medizinische Maßnahme (gegebenenfalls in Kombination mit anderen Maßnahmen) alternativlos ist, da ansonsten Tod oder Behinderung drohen', NULL, NULL, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('SB1', 'Erkrankungen der Wirbelsäule, Gelenke und Extremitäten (mit motorisch-funktionellen Schädigungen', 10, 20, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('SB2', 'Erkrankungen der Wirbelsäule, Gelenke und Extremitäten (mit motorisch-funktionellen und sensomotorisch-perzeptiven Schädigungen)', 10, 30, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('SB3', 'System- und Autoimmunerkrankungen mit Bindegewebe-, Muskel- und Gefäßbeteiligung (mit motorisch-funktionellen/ sensomotorisch-perzeptiven Schädigungen)', 10, 30, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('SC', 'Krankhafte Störungen des Schluckaktes Dysphagie (Schluckstörung)', 10, 60, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('SF', 'Störungen der Stimm- und Sprechfunktion Rhinophonie', 10, 20, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('SO1', 'Störung der Dickdarmfunktion', 6, 18, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('SO2', 'Störung der Ausscheidung (Stuhlinkontinez, Harninkontinenz)', 6, 18, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('SO3', 'Schwindel unterschiedlicher Gene und Ätiologie', 6, 18, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('SO4', 'Sekundäre periphere trophische Störungen bei erkrankungen', 6, 18, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('SO5', 'chronische Adnexitis, Prostatitis', 6, 18, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('SP1', 'Störungen der Sprache vor Abschluss der Sprachentwicklung', 10, 60, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('SP2', 'Störungen der auditiven Wahrnehmung', 10, 20, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('SP3', 'Störungen der Artikulation, Dyslalie', 10, 30, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('SP4', 'Störungen des Sprechens/der Sprache bei hochgradiger Schwerhörigkeit oder Taubheit', 20, 50, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('SP5', 'Störungen der Sprache nach Abschluss der Sprachentwicklung Aphasien und Dysphasien', 20, 60, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('SP6', 'Störungen der Sprechmotorik Dysarthrie/Dysarthrophonie/ Sprechapraxie', 10, 60, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('ST1', 'Organisch bedingte Erkrankungen der Stimme', 10, 20, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('ST2', 'Funktionell bedingte Erkrankungen der Stimme', 10, 20, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('ST3', 'Psychogene Aphonie', 10, 10, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('ST4', 'Psychogene Dysphonie', 10, 20, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('WS', 'Wirbelsäulenerkrankung', 6, 18, '2021-01-01', '9999-12-31', 'Smutje\r'),
	('ZN', 'ZNS-Erkrankungen einschließlich des Rückenmarks/Neuromuskuläre Erkrankungen', 10, 30, '2021-01-01', '9999-12-31', 'Smutje\r');
/*!40000 ALTER TABLE `hmr_diagnosegruppe` ENABLE KEYS */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
