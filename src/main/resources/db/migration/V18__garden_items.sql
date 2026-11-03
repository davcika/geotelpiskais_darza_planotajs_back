-- Dārza elementi (siltumnīca, lecekts, žogs, mucas...). Katalogs ir tikai lasāms.
-- shape: AREA = taisnstūris (Polygon), POINT = punkts, LINE = līnija (LineString, mēra garumu).
-- Sezonas ietekme tikai aizsardzības elementiem: *_shift_days ir dienas, par cik agrāk var sākt
-- sēt/stādīt ārā (start), par cik vēlāk beigt (end) un par cik ilgāk novākt ražu (harvest).
CREATE TABLE item_type (
    key                  VARCHAR(30) PRIMARY KEY,
    name_en              VARCHAR(60) NOT NULL,
    name_lv              VARCHAR(60) NOT NULL,
    icon                 VARCHAR(16) NOT NULL,
    shape                VARCHAR(10) NOT NULL CHECK (shape IN ('AREA', 'POINT', 'LINE')),
    category             VARCHAR(20) NOT NULL CHECK (category IN ('STRUCTURE', 'PROTECTION', 'WATERING', 'SUPPORT', 'WILDLIFE', 'OTHER')),
    default_w_m          DOUBLE PRECISION CHECK (default_w_m > 0),
    default_h_m          DOUBLE PRECISION CHECK (default_h_m > 0),
    start_shift_days     INT NOT NULL DEFAULT 0 CHECK (start_shift_days BETWEEN 0 AND 120),
    end_shift_days       INT NOT NULL DEFAULT 0 CHECK (end_shift_days BETWEEN 0 AND 120),
    harvest_shift_days   INT NOT NULL DEFAULT 0 CHECK (harvest_shift_days BETWEEN 0 AND 120),
    frost_protection_c   DOUBLE PRECISION NOT NULL DEFAULT 0 CHECK (frost_protection_c >= 0),
    climbing_support     BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order           INT NOT NULL
);

-- Vērtības Latvijai (neapsildāmas konstrukcijas), aptuvenas dārzkopības prakses vērtības
INSERT INTO item_type (key, name_en, name_lv, icon, shape, category, default_w_m, default_h_m,
                       start_shift_days, end_shift_days, harvest_shift_days, frost_protection_c, climbing_support, sort_order) VALUES
('COMPOST_BIN',   'Compost bin',    'Komposta kaste',      '♻️', 'AREA',  'STRUCTURE',  1.0, 1.0,  0,  0,  0, 0,   FALSE, 10),
('GREENHOUSE',    'Greenhouse',     'Siltumnīca',          '🏠', 'AREA',  'PROTECTION', 3.0, 4.0, 35, 30, 30, 3,   FALSE, 20),
('POLYTUNNEL',    'Polytunnel',     'Plēves tunelis',      '⛺', 'AREA',  'PROTECTION', 3.0, 6.0, 30, 30, 30, 2.5, FALSE, 30),
('COLD_FRAME',    'Cold frame',     'Lecekts',             '🪟', 'AREA',  'PROTECTION', 1.2, 0.8, 21, 21, 21, 2,   FALSE, 40),
('GARDEN_FLEECE', 'Garden fleece',  'Agroplēve',           '🧣', 'AREA',  'PROTECTION', 1.2, 3.0, 10, 14, 14, 2,   FALSE, 50),
('CLOCHE',        'Cloche',         'Stikla zvans',        '🔔', 'POINT', 'PROTECTION', 0.4, 0.4, 14,  0,  0, 2,   FALSE, 60),
('FORCING_POT',   'Forcing pot',    'Dzīšanas pods',       '🏺', 'POINT', 'PROTECTION', 0.5, 0.5, 14,  0,  0, 1,   FALSE, 70),
('FRUIT_CAGE',    'Fruit cage',     'Ogu tīkla būris',     '🕸️', 'AREA',  'PROTECTION', 2.0, 3.0,  0,  0,  0, 0,   FALSE, 80),
('CONTAINER',     'Container',      'Konteiners',          '🪴', 'AREA',  'OTHER',      0.6, 0.6,  0,  0,  0, 0,   FALSE, 90),
('BEE_HIVE',      'Bee hive',       'Bišu strops',         '🐝', 'AREA',  'WILDLIFE',   0.5, 0.5,  0,  0,  0, 0,   FALSE, 100),
('WATER_BARREL',  'Water barrel',   'Ūdens muca',          '🛢️', 'POINT', 'WATERING',   0.6, 0.6,  0,  0,  0, 0,   FALSE, 110),
('SPIGOT',        'Spigot',         'Ūdens krāns',         '🚰', 'POINT', 'WATERING',   NULL, NULL, 0,  0,  0, 0,   FALSE, 120),
('TIMER',         'Timer',          'Laistīšanas taimeris','⏲️', 'POINT', 'WATERING',   NULL, NULL, 0,  0,  0, 0,   FALSE, 130),
('PSI_REGULATOR', 'PSI regulator',  'Spiediena regulators','🎚️', 'POINT', 'WATERING',   NULL, NULL, 0,  0,  0, 0,   FALSE, 140),
('END',           'End',            'Gala aizbāznis',      '⏹️', 'POINT', 'WATERING',   NULL, NULL, 0,  0,  0, 0,   FALSE, 150),
('DRIPLINE',      'Dripline',       'Pilienlaistīšanas caurule', '💧', 'LINE', 'WATERING', NULL, NULL, 0, 0, 0, 0, FALSE, 160),
('SUPPLY_TUBING', 'Supply tubing',  'Pievadcaurule',       '➰', 'LINE',  'WATERING',   NULL, NULL, 0,  0,  0, 0,   FALSE, 170),
('BEAN_TRELLIS',  'Bean trellis',   'Pupu režģis',         '🪜', 'LINE',  'SUPPORT',    NULL, NULL, 0,  0,  0, 0,   TRUE,  180),
('TEEPEE',        'Teepee',         'Balstu vigvams',      '🔺', 'POINT', 'SUPPORT',    0.8, 0.8,  0,  0,  0, 0,   TRUE,  190),
('ARCH',          'Arch',           'Arka',                '🌈', 'POINT', 'SUPPORT',    1.5, 0.5,  0,  0,  0, 0,   TRUE,  200),
('FENCE',         'Fence',          'Žogs',                '🚧', 'LINE',  'STRUCTURE',  NULL, NULL, 0,  0,  0, 0,   FALSE, 210);

-- Dārzā novietots elements. Ģeometrija kā garden_object: GeoJSON teksts EPSG:3059.
CREATE TABLE garden_item (
    id            BIGSERIAL PRIMARY KEY,
    garden_id     BIGINT       NOT NULL REFERENCES garden (id) ON DELETE CASCADE,
    item_type_key VARCHAR(30)  NOT NULL REFERENCES item_type (key),
    geometry      TEXT         NOT NULL,
    rotation_deg  DOUBLE PRECISION NOT NULL DEFAULT 0 CHECK (rotation_deg >= 0 AND rotation_deg < 360),
    label         VARCHAR(100),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_garden_item_garden_id ON garden_item (garden_id);
