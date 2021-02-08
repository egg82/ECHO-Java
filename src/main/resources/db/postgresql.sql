CREATE TABLE IF NOT EXISTS "echo_show" (
    "id"        SERIAL PRIMARY KEY,
    "version"   INTEGER NOT NULL,
    "created"   TIMESTAMP NOT NULL,
    "modified"  TIMESTAMP NOT NULL,
    "tvdb"      BIGINT NOT NULL,
    "season"    INTEGER NOT NULL DEFAULT 1,
    "episode"   INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS "echo_upload" (
    "id"        SERIAL PRIMARY KEY,
    "version"   INTEGER NOT NULL,
    "created"   TIMESTAMP NOT NULL,
    "modified"  TIMESTAMP NOT NULL,
    "hash"      CHAR(128) NOT NULL,
    "service"   VARCHAR(255) NOT NULL,
    "data"      BLOB NOT NULL,
    UNIQUE ("hash", "service")
);

CREATE TABLE IF NOT EXISTS "echo_message" (
    "id"        SERIAL PRIMARY KEY,
    "version"   INTEGER NOT NULL,
    "created"   TIMESTAMP NOT NULL,
    "modified"  TIMESTAMP NOT NULL,
    "message"   TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS "echo_learn" (
    "id"        SERIAL PRIMARY KEY,
    "version"   INTEGER NOT NULL,
    "created"   TIMESTAMP NOT NULL,
    "modified"  TIMESTAMP NOT NULL,
    "user"      BIGINT NOT NULL,
    "learning"  BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS "echo_data" (
    "id"        SERIAL PRIMARY KEY,
    "version"   INTEGER NOT NULL,
    "created"   TIMESTAMP NOT NULL,
    "modified"  TIMESTAMP NOT NULL,
    "key"       VARCHAR(255) NOT NULL UNIQUE,
    "value"     VARCHAR(255)
);