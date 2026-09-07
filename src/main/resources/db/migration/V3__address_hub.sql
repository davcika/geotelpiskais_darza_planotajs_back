-- Adrešu meklēšana bez atsevišķa servisa: trigrammas (drukas kļūdas, daļēja sakritība)
-- un unaccent ("brivibas" atrod "Brīvības")
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS unaccent;

-- VZD Valsts adrešu reģistra ēku un zemes vienību adreses (aw_eka.csv), CC BY 4.0.
-- Koordinātas VZD publicē LKS-2020; glabājam kā EPSG:3059 (atšķirība ~10 cm).
CREATE TABLE address (
    code        BIGINT PRIMARY KEY,
    label       VARCHAR(300)     NOT NULL,
    search_text TEXT             NOT NULL, -- lower(unaccent(label)), aprēķināts importējot
    east        DOUBLE PRECISION NOT NULL,
    north       DOUBLE PRECISION NOT NULL
);

CREATE INDEX idx_address_search_text ON address USING GIN (search_text gin_trgm_ops);

-- Importa starptabula: aizpilda pa daļām, tad vienā transakcijā pārliek uz address
CREATE TABLE address_staging (
    code  BIGINT,
    label VARCHAR(300),
    east  DOUBLE PRECISION,
    north DOUBLE PRECISION
);

-- Tikai veiksmīgi importi; pēc pēdējā datuma nosaka, vai dati ir novecojuši
CREATE TABLE address_import (
    imported_at TIMESTAMPTZ PRIMARY KEY,
    row_count   INT NOT NULL
);

-- Dārza HUB zona: GeoJSON Polygon EPSG:3059. Adreses kods bez ārējās atslēgas,
-- lai adrešu pārimports nekad nesabojātu dārzus
ALTER TABLE garden
    ADD COLUMN address_code  BIGINT,
    ADD COLUMN address_label VARCHAR(300),
    ADD COLUMN hub_geometry  TEXT;
