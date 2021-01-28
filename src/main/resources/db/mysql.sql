CREATE TABLE IF NOT EXISTS `echo_message` (
	`id` INT NOT NULL AUTO_INCREMENT,
	`version` INT NOT NULL,
	`created` DATETIME NOT NULL,
	`modified` DATETIME NOT NULL,
	`message` LONGTEXT NOT NULL,
	PRIMARY KEY( `id` )
);

CREATE TABLE IF NOT EXISTS `echo_data` (
	`id` INT NOT NULL AUTO_INCREMENT,
	`version` INT NOT NULL,
	`created` DATETIME NOT NULL,
	`modified` DATETIME NOT NULL,
	`key` VARCHAR(255) NOT NULL UNIQUE,
	`value` VARCHAR(255),
	PRIMARY KEY( `id` )
);