/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE TABLE IF NOT EXISTS `hmr_disziplin` (
  `id` char(1) NOT NULL,
  `disziplin_beschreibung` varchar(255) NOT NULL,
  `gueltig_von` varchar(255) NOT NULL,
  `gueltig_bis` varchar(255) NOT NULL,
  `ccid` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Welcher Tarifgruppe der Heilmittel gehört dazu';

DELETE FROM `hmr_disziplin`;
/*!40000 ALTER TABLE `hmr_disziplin` DISABLE KEYS */;
INSERT INTO `hmr_disziplin` (`id`, `disziplin_beschreibung`, `gueltig_von`, `gueltig_bis`, `ccid`) VALUES
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
/*!40000 ALTER TABLE `hmr_disziplin` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
