CREATE TABLE IF NOT EXISTS `echo_message` (
	`id`	IDENTITY NOT NULL PRIMARY KEY,
	`version`	INT NOT NULL,
	`created`	TIMESTAMP NOT NULL,
	`modified`	TIMESTAMP NOT NULL,
	`message`	CLOB NOT NULL
);

CREATE TABLE IF NOT EXISTS `echo_data` (
	`id`	IDENTITY NOT NULL PRIMARY KEY,
	`version`	INT NOT NULL,
	`created`	TIMESTAMP NOT NULL,
	`modified`	TIMESTAMP NOT NULL,
	`key`	VARCHAR(255) NOT NULL UNIQUE,
	`value`	VARCHAR(255)
);