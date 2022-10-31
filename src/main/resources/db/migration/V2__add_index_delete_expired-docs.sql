
CREATE INDEX CONCURRENTLY IF NOT EXISTS storeddocument_ttl_harddeleted
ON storeddocument (ttl, harddeleted) WHERE TTL is not null;

ANALYZE;
