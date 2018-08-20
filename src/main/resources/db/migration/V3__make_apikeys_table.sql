CREATE TABLE IF NOT EXISTS apikeys (
  apikey VARCHAR(36) NOT NULL PRIMARY KEY,
  userid VARCHAR(36) NOT NULL,
  usages BIGINT      NOT NULL DEFAULT 0,
  CONSTRAINT `apikey_uuid` FOREIGN KEY (`userid`) REFERENCES `users` (`uuid`)
);