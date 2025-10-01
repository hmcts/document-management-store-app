
ALTER TABLE documentcontentversion ADD COLUMN mime_type_updated BOOLEAN DEFAULT FALSE;

CREATE INDEX idx_mime_type_updated ON documentcontentversion(mime_type_updated);
