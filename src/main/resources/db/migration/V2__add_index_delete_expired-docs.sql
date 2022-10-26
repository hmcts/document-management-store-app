
CREATE INDEX CONCURRENTLY IF NOT EXISTS storeddocument_delete_expired_docs
ON storeddocument (ttl, harddeleted) WHERE TTL is not null;
