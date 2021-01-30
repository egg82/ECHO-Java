CREATE TABLE IF NOT EXISTS "echo_message" (
	"id" SERIAL PRIMARY KEY,
	"version" INTEGER NOT NULL,
	"created" TIMESTAMP NOT NULL,
	"modified" TIMESTAMP NOT NULL,
	"message" TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS "echo_learn" (
	"id" SERIAL PRIMARY KEY,
	"version" INTEGER NOT NULL,
	"created" TIMESTAMP NOT NULL,
	"modified" TIMESTAMP NOT NULL,
	"user" BIGINT NOT NULL,
	"learning" BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS "echo_data" (
	"id" SERIAL PRIMARY KEY,
	"version" INTEGER NOT NULL,
	"created" TIMESTAMP NOT NULL,
	"modified" TIMESTAMP NOT NULL,
	"key" VARCHAR(255) NOT NULL UNIQUE,
	"value" VARCHAR(255)
);