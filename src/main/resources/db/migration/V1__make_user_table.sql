CREATE TABLE IF NOT EXISTS `users` (
  `username`       VARCHAR(40) NOT NULL UNIQUE,
  `email`          VARCHAR(40) NOT NULL UNIQUE,
  `hash`           BLOB        NOT NULL,
  `uuid`           VARCHAR(36) NOT NULL UNIQUE,
  `email_verified` BOOL        NOT NULL,
  `ts`             TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);