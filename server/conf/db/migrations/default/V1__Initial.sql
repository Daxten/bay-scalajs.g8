CREATE TABLE "User" (
  id                   SERIAL PRIMARY KEY,
  email                VARCHAR     NOT NULL,
  password             VARCHAR     NOT NULL,
  created              TIMESTAMPTZ NOT NULL,
  "lastLogin"          TIMESTAMPTZ,
  "lastAction"         TIMESTAMPTZ,
  "resetPasswordToken" VARCHAR,
  UNIQUE (email)
);

CREATE TABLE "UserGroup" (
  name VARCHAR NOT NULL PRIMARY KEY
);

CREATE TABLE "UserToUserGroup" (
  "userId"    INT     NOT NULL REFERENCES "User",
  "groupName" VARCHAR NOT NULL REFERENCES "UserGroup"
);

INSERT INTO "User" (email, created, password) VALUES ('test@test.de', NOW(), '$2a$10$yrRTolAEYuyD82z.TPhUseNHSAGN9fLJ2Jj1H4nZ8/31SyPGs3DEG'); -- pw == testpw
INSERT INTO "UserGroup" VALUES ('admin');
INSERT INTO "UserToUserGroup" VALUES (1, 'admin');