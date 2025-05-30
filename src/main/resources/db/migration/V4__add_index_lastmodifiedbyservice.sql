
CREATE INDEX CONCURRENTLY IF NOT EXISTS storeddocument_lastmodifiedbyservice
ON storeddocument (lastmodifiedbyservice) WHERE TTL is not null;
