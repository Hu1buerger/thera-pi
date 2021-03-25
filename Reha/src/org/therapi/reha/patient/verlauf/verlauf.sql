CREATE TABLE `verlauf` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`patient_id` INT(11) NOT NULL,
	`therapist` VARCHAR(50) NOT NULL,
	`documentator` VARCHAR(50) NOT NULL,
	`documentedday` DATE NOT NULL,
	`dayofdocumentation` DATE NOT NULL,
	`text` TEXT NOT NULL,
	PRIMARY KEY (`id`)
)
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
;
