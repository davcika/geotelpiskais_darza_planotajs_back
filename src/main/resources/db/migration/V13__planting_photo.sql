-- Sēklu paciņu bildes stādījumam; faili uz diska (app.photos.dir), šeit tikai metadati
CREATE TABLE planting_photo (
    id            BIGSERIAL PRIMARY KEY,
    planting_id   BIGINT       NOT NULL REFERENCES planting (id) ON DELETE CASCADE,
    content_type  VARCHAR(32)  NOT NULL,
    size_bytes    BIGINT       NOT NULL,
    storage_key   VARCHAR(64)  NOT NULL UNIQUE,
    original_name VARCHAR(255),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX planting_photo_planting_idx ON planting_photo (planting_id);
