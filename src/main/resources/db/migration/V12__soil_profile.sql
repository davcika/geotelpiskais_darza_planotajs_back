-- Augsnes profils: object_id NULL = visa dārza noklusējums, citādi dobes pārrakstījums.
-- Atsevišķa tabula (garden_object netiek mainīts). Izdzēšot dobi, izdzēšas arī tās augsne.
CREATE TABLE soil_profile (
    id                 BIGSERIAL PRIMARY KEY,
    garden_id          BIGINT        NOT NULL REFERENCES garden (id) ON DELETE CASCADE,
    object_id          BIGINT        REFERENCES garden_object (id) ON DELETE CASCADE,
    soil_type          VARCHAR(12)   CHECK (soil_type IN ('SAND', 'SANDY_LOAM', 'LOAM', 'CLAY_LOAM', 'CLAY', 'PEAT')),
    ph                 NUMERIC(3, 1) CHECK (ph BETWEEN 3.0 AND 10.0),
    organic_matter_pct NUMERIC(4, 1) CHECK (organic_matter_pct BETWEEN 0 AND 100),
    nitrogen           NUMERIC(7, 1) CHECK (nitrogen BETWEEN 0 AND 10000),
    phosphorus         NUMERIC(7, 1) CHECK (phosphorus BETWEEN 0 AND 10000),
    potassium          NUMERIC(7, 1) CHECK (potassium BETWEEN 0 AND 10000),
    moisture           VARCHAR(6)    CHECK (moisture IN ('DRY', 'MEDIUM', 'WET')),
    light              VARCHAR(5)    CHECK (light IN ('SUN', 'PART', 'SHADE')),
    tested_on          DATE,
    lab_note           VARCHAR(500)
);

-- UNIQUE (garden_id, object_id) neaizliedz vairākus NULL, tāpēc dārza noklusējumam atsevišķs indekss
CREATE UNIQUE INDEX uq_soil_profile_object ON soil_profile (garden_id, object_id) WHERE object_id IS NOT NULL;
CREATE UNIQUE INDEX uq_soil_profile_garden ON soil_profile (garden_id) WHERE object_id IS NULL;
