CREATE TABLE IF NOT EXISTS `echo_show` (
    `id`        BIGINT NOT NULL AUTO_INCREMENT,
    `version`   INT NOT NULL,
    `created`   DATETIME NOT NULL,
    `modified`  DATETIME NOT NULL,
    `tvdb`      BIGINT NOT NULL,
    `season`    INT NOT NULL DEFAULT 1,
    `episode`   INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `echo_upload` (
    `id`        BIGINT NOT NULL AUTO_INCREMENT,
    `version`   INT NOT NULL,
    `created`   DATETIME NOT NULL,
    `modified`  DATETIME NOT NULL,
    `hash`      CHAR(128) NOT NULL,
    `service`   VARCHAR(255) NOT NULL,
    `data`      LONGBLOB NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE (`hash`, `service`)
);

CREATE TABLE IF NOT EXISTS `echo_message` (
    `id`        BIGINT NOT NULL AUTO_INCREMENT,
    `version`   INT NOT NULL,
    `created`   DATETIME NOT NULL,
    `modified`  DATETIME NOT NULL,
    `message`   LONGTEXT NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `echo_learn` (
    `id`        BIGINT NOT NULL AUTO_INCREMENT,
    `version`   INT NOT NULL,
    `created`   DATETIME NOT NULL,
    `modified`  DATETIME NOT NULL,
    `user`      BIGINT NOT NULL,
    `learning`  BOOLEAN NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `echo_data` (
    `id`        BIGINT NOT NULL AUTO_INCREMENT,
    `version`   INT NOT NULL,
    `created`   DATETIME NOT NULL,
    `modified`  DATETIME NOT NULL,
    `key`       VARCHAR(255) NOT NULL UNIQUE,
    `value`     VARCHAR(255),
    PRIMARY KEY (`id`)
);