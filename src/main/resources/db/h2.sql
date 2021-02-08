CREATE TABLE IF NOT EXISTS `echo_show` (
    `id`        IDENTITY NOT NULL PRIMARY KEY,
    `version`   INT NOT NULL,
    `created`   TIMESTAMP NOT NULL,
    `modified`  TIMESTAMP NOT NULL,
    `tvdb`      BIGINT NOT NULL,
    `season`    INT NOT NULL DEFAULT 1,
    `episode`   INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS `echo_upload` (
    `id`        IDENTITY NOT NULL PRIMARY KEY,
    `version`   INT NOT NULL,
    `created`   TIMESTAMP NOT NULL,
    `modified`  TIMESTAMP NOT NULL,
    `hash`      CHAR(128) NOT NULL,
    `service`   VARCHAR(255) NOT NULL,
    `data`      BLOB NOT NULL,
    UNIQUE (`hash`, `service`)
);

CREATE TABLE IF NOT EXISTS `echo_message` (
    `id`        IDENTITY NOT NULL PRIMARY KEY,
    `version`   INT NOT NULL,
    `created`   TIMESTAMP NOT NULL,
    `modified`  TIMESTAMP NOT NULL,
    `message`   CLOB NOT NULL
);

CREATE TABLE IF NOT EXISTS `echo_learn` (
    `id`        IDENTITY NOT NULL PRIMARY KEY,
    `version`   INT NOT NULL,
    `created`   TIMESTAMP NOT NULL,
    `modified`  TIMESTAMP NOT NULL,
    `user`      BIGINT NOT NULL UNIQUE,
    `learning`  BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS `echo_data` (
    `id`        IDENTITY NOT NULL PRIMARY KEY,
    `version`   INT NOT NULL,
    `created`   TIMESTAMP NOT NULL,
    `modified`  TIMESTAMP NOT NULL,
    `key`       VARCHAR(255) NOT NULL UNIQUE,
    `value`     VARCHAR(255)
);