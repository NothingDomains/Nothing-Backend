CREATE TABLE IF NOT EXISTS verifications (
  token      VARCHAR(36) NOT NULL PRIMARY KEY,
  internalId VARCHAR(36) NOT NULL,
  ts         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);