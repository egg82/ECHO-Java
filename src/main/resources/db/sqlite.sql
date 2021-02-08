CREATE TABLE IF NOT EXISTS "echo_show" (
    "id"        INTEGER NOT NULL,
    "version"   INTEGER NOT NULL,
    "created"   INTEGER NOT NULL,
    "modified"  INTEGER NOT NULL,
    "tvdb"      INTEGER NOT NULL,
    "season"    INTEGER NOT NULL DEFAULT 1,
    "episode"   INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY ("id" AUTOINCREMENT)
);

CREATE TABLE IF NOT EXISTS "echo_web" (
    "id"        INTEGER NOT NULL,
    "version"   INTEGER NOT NULL,
    "created"   INTEGER NOT NULL,
    "modified"  INTEGER NOT NULL,
    "hash"      TEXT NOT NULL,
    "service"   TEXT NOT NULL,
    "path"      VARCHAR(4096) NOT NULL,
    PRIMARY KEY ("id" AUTOINCREMENT),
    UNIQUE ("hash", "service")
);

CREATE TABLE IF NOT EXISTS "echo_message" (
    "id"        INTEGER NOT NULL,
    "version"   INTEGER NOT NULL,
    "created"   INTEGER NOT NULL,
    "modified"  INTEGER NOT NULL,
    "message"   TEXT NOT NULL,
    PRIMARY KEY ("id" AUTOINCREMENT)
);

CREATE TABLE IF NOT EXISTS "echo_learn" (
    "id"        INTEGER NOT NULL,
    "version"   INTEGER NOT NULL,
    "created"   INTEGER NOT NULL,
    "modified"  INTEGER NOT NULL,
    "user"      INTEGER NOT NULL,
    "learning"  INTEGER NOT NULL,
    PRIMARY KEY ("id" AUTOINCREMENT)
);

CREATE TABLE IF NOT EXISTS "echo_data" (
    "id"        INTEGER NOT NULL,
    "version"   INTEGER NOT NULL,
    "created"   INTEGER NOT NULL,
    "modified"  INTEGER NOT NULL,
    "key"       TEXT NOT NULL UNIQUE,
    "value"     TEXT,
    PRIMARY KEY ("id" AUTOINCREMENT)
);