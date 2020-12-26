CREATE TABLE `hmv` (
  `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `disziplin` INT(10) NOT NULL,
  `nummer` INT(10) UNSIGNED NOT NULL,
  `pat_id` INT(10) UNSIGNED NOT NULL,
  `kk_id` INT(10) UNSIGNED NOT NULL,
  `betriebs_id` INT(10) UNSIGNED NOT NULL,
  `arzt_id` INT(10) UNSIGNED NOT NULL,
  `pat_vers_nummer` VARCHAR(50) NOT NULL,
  `pat_vers_status` INT(10) UNSIGNED NOT NULL,
  `datum` DATE NOT NULL,
  `icd10_1` VARCHAR(50) NOT NULL,
  `icd10_2` VARCHAR(50) NOT NULL,
  `icd10_text` VARCHAR(50) NOT NULL,
  `diagnosegruppe` VARCHAR(50) NOT NULL,
  `leitsymptomatik` VARCHAR(50) NOT NULL,
  `leitsymptomatik_text` VARCHAR(50) NOT NULL,
  `therapiebericht` TINYINT(1) NOT NULL,
  `hausbesuch` TINYINT(1) NOT NULL,
  `frequenz_von` INT(11) NOT NULL COMMENT 'anzahl Behandlungen pro Woche',
  `frequent_bis` INT(11) NOT NULL COMMENT 'anzahl Behandlungen pro Woche',
  `dauer` INT(11) NOT NULL COMMENT 'Dauer der Behandlung in Minuten',
  `therapieziele` TEXT NOT NULL,
  `farbcode_tk` TEXT NOT NULL,
  `dringlich` TINYINT(1) NOT NULL,
  `angelegt_von` INT(11) NOT NULL,
  `angelegt_am` DATETIME NOT NULL,
  PRIMARY KEY (`id`)
)
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;
