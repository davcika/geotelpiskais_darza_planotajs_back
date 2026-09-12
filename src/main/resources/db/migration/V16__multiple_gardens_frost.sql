-- Vairāki dārzi: katram nosaukums, ikona (atslēga no fiksēta saraksta) un krāsa (#hex)
ALTER TABLE garden
    ADD COLUMN icon  VARCHAR(20) NOT NULL DEFAULT 'leaf'
        CHECK (icon IN ('leaf', 'carrot', 'flower', 'tree', 'apple', 'sun', 'house', 'drop')),
    ADD COLUMN color VARCHAR(7)  NOT NULL DEFAULT '#2d6a3d' CHECK (color ~ '^#[0-9a-fA-F]{6}$');

-- Salnu datumi 'MM-DD' (atkārtojas katru gadu); avots: Open-Meteo vēsture, lietotājs vai noklusējums
ALTER TABLE garden
    ADD COLUMN last_frost           VARCHAR(5),
    ADD COLUMN first_frost          VARCHAR(5),
    ADD COLUMN last_frost_earliest  VARCHAR(5),
    ADD COLUMN last_frost_latest    VARCHAR(5),
    ADD COLUMN first_frost_earliest VARCHAR(5),
    ADD COLUMN first_frost_latest   VARCHAR(5),
    ADD COLUMN frost_source         VARCHAR(20) CHECK (frost_source IN ('OPEN_METEO', 'MANUAL', 'DEFAULT')),
    ADD COLUMN frost_updated_at     TIMESTAMPTZ,
    -- Punkts (WGS84), kuram aprēķināts; kešs der, kamēr punkts nemainās
    ADD COLUMN frost_lat            DOUBLE PRECISION,
    ADD COLUMN frost_lon            DOUBLE PRECISION;

-- ON DELETE CASCADE ķēde jau ir: garden → garden_object (V1), garden_task (V10), soil_profile (V12);
-- garden_object → planting (V6) → planting_photo (V13). Bilžu failus pēc tam dzēš OrphanPhotoCleaner.
