-- Augu katalogs (tikai lasāms, aizpilda V5). Datumi ir 'MM-DD' bez gada, jo atkārtojas katru sezonu.
-- indoor_sow_* ir sēšana telpās dēstiem (tikai TRANSPLANT); outdoor_* ir sēšana vai dēstu izstādīšana ārā.
-- frost_sensitive: auga virszemes daļa necieš salnu (kartupelis stādāms agri, bet dzinumus jāsargā).
CREATE TABLE plant (
    id                  BIGSERIAL PRIMARY KEY,
    name_en             VARCHAR(60) NOT NULL UNIQUE,
    name_lv             VARCHAR(60) NOT NULL,
    family              VARCHAR(30) NOT NULL,
    sow_method          VARCHAR(20) NOT NULL CHECK (sow_method IN ('DIRECT', 'TRANSPLANT')),
    indoor_sow_from     VARCHAR(5) CHECK (indoor_sow_from ~ '^\d{2}-\d{2}$'),
    indoor_sow_to       VARCHAR(5) CHECK (indoor_sow_to ~ '^\d{2}-\d{2}$'),
    outdoor_from        VARCHAR(5) NOT NULL CHECK (outdoor_from ~ '^\d{2}-\d{2}$'),
    outdoor_to          VARCHAR(5) NOT NULL CHECK (outdoor_to ~ '^\d{2}-\d{2}$'),
    days_to_harvest     INT     NOT NULL CHECK (days_to_harvest > 0),
    harvest_window_days INT     NOT NULL CHECK (harvest_window_days > 0),
    frost_sensitive     BOOLEAN NOT NULL,
    CHECK ((sow_method = 'TRANSPLANT') = (indoor_sow_from IS NOT NULL AND indoor_sow_to IS NOT NULL))
);

-- Augu pāru saderība; katrs pāris glabājas vienreiz (plant_a_id < plant_b_id)
CREATE TABLE plant_companion (
    plant_a_id BIGINT      NOT NULL REFERENCES plant (id),
    plant_b_id BIGINT      NOT NULL REFERENCES plant (id),
    relation   VARCHAR(10) NOT NULL CHECK (relation IN ('GOOD', 'BAD')),
    PRIMARY KEY (plant_a_id, plant_b_id),
    CHECK (plant_a_id < plant_b_id)
);
