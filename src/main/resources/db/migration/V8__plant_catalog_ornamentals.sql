-- Katalogs paplašināts ar puķēm, krūmiem un kokiem (pamats vainaga skatam, gadalaiku skatam un augsnes ieteikumiem).
-- category: kam augs domāts; PLANT = stāda sīpolu, stādu vai stādiņu (nevis sēj).
-- Izskats: evergreen = lapas visu gadu; citādi foliage_* ir aptuvenais lapu (virszemes daļas) laiks 'MM-DD'.
-- Dekoratīviem augiem raža nav, tāpēc days_to_harvest/harvest_window_days drīkst būt NULL (abi vai neviens).
-- Izmēri ir pieauguša auga (metri) pēc years_to_mature gadiem; spread_m = vainaga/krūma diametrs.

ALTER TABLE plant DROP CONSTRAINT IF EXISTS plant_sow_method_check;
ALTER TABLE plant ADD CONSTRAINT plant_sow_method_check CHECK (sow_method IN ('DIRECT', 'TRANSPLANT', 'PLANT'));

ALTER TABLE plant ALTER COLUMN days_to_harvest DROP NOT NULL;
ALTER TABLE plant ALTER COLUMN harvest_window_days DROP NOT NULL;
ALTER TABLE plant ADD CONSTRAINT plant_harvest_both CHECK ((days_to_harvest IS NULL) = (harvest_window_days IS NULL));

ALTER TABLE plant
    ADD COLUMN category        VARCHAR(20) NOT NULL DEFAULT 'VEGETABLE'
        CHECK (category IN ('VEGETABLE', 'HERB', 'FLOWER', 'BULB', 'SHRUB', 'TREE', 'HEDGE')),
    ADD COLUMN perennial       BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN evergreen       BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN foliage_from    VARCHAR(5) CHECK (foliage_from ~ '^\d{2}-\d{2}$'),
    ADD COLUMN foliage_to      VARCHAR(5) CHECK (foliage_to ~ '^\d{2}-\d{2}$'),
    ADD COLUMN bloom_from      VARCHAR(5) CHECK (bloom_from ~ '^\d{2}-\d{2}$'),
    ADD COLUMN bloom_to        VARCHAR(5) CHECK (bloom_to ~ '^\d{2}-\d{2}$'),
    ADD COLUMN bloom_color     VARCHAR(7) CHECK (bloom_color ~ '^#[0-9a-f]{6}$'),
    ADD COLUMN height_m        DOUBLE PRECISION CHECK (height_m > 0),
    ADD COLUMN spread_m        DOUBLE PRECISION CHECK (spread_m > 0),
    ADD COLUMN years_to_mature INT CHECK (years_to_mature > 0),
    ADD COLUMN ph_min          DOUBLE PRECISION CHECK (ph_min BETWEEN 3 AND 9),
    ADD COLUMN ph_max          DOUBLE PRECISION CHECK (ph_max BETWEEN 3 AND 9),
    ADD COLUMN light           VARCHAR(10) CHECK (light IN ('SUN', 'PART', 'SHADE')),
    ADD CONSTRAINT plant_ph_range CHECK (ph_min IS NULL OR ph_max IS NULL OR ph_min <= ph_max),
    ADD CONSTRAINT plant_bloom_both CHECK ((bloom_from IS NULL) = (bloom_to IS NULL)),
    ADD CONSTRAINT plant_foliage CHECK (evergreen OR (foliage_from IS NULL) = (foliage_to IS NULL));

-- Esošie: garšaugi un samtene (puķe)
UPDATE plant SET category = 'HERB' WHERE name_en IN ('Parsley', 'Dill', 'Basil');
UPDATE plant SET category = 'FLOWER', bloom_from = '07-01', bloom_to = '09-30', bloom_color = '#f29d1f'
WHERE name_en = 'Marigold';

-- Jaunie augi. outdoor_* = stādīšanas logs (sīpoli rudenī, kokaugi pavasarī). Vērtības aptuvenas, Latvijas klimatam,
-- no stādaudzētavu aprakstiem; izmēri tipiskām šķirnēm (tūja 'Smaragd', skarainā hortenzija u. tml.).
INSERT INTO plant (name_en, name_lv, family, category, sow_method, outdoor_from, outdoor_to, frost_sensitive, perennial,
                   evergreen, foliage_from, foliage_to, bloom_from, bloom_to, bloom_color,
                   height_m, spread_m, years_to_mature, ph_min, ph_max, light) VALUES
('Tulip',               'Tulpe',               'LILIACEAE',       'BULB',  'PLANT', '09-15', '10-31', FALSE, TRUE, FALSE, '04-01', '06-30', '04-25', '05-25', '#e0457b', 0.4, 0.15, 1, 6.0, 7.5, 'SUN'),
('Daffodil',            'Narcise',             'AMARYLLIDACEAE',  'BULB',  'PLANT', '09-01', '10-15', FALSE, TRUE, FALSE, '03-25', '06-20', '04-15', '05-15', '#f2c94c', 0.4, 0.15, 1, 6.0, 7.5, 'SUN'),
('Crocus',              'Krokuss',             'IRIDACEAE',       'BULB',  'PLANT', '09-01', '10-15', FALSE, TRUE, FALSE, '03-15', '05-31', '03-25', '04-25', '#8e6bd1', 0.1, 0.08, 1, 6.0, 7.5, 'SUN'),
('Peony',               'Peonija',             'PAEONIACEAE',     'FLOWER','PLANT', '08-20', '09-30', FALSE, TRUE, FALSE, '04-20', '10-15', '06-01', '06-30', '#e8a0b4', 0.9, 0.9,  3, 6.5, 7.5, 'SUN'),
('Hosta',               'Hosta',               'ASPARAGACEAE',    'FLOWER','PLANT', '04-20', '09-15', FALSE, TRUE, FALSE, '05-01', '10-10', '07-01', '08-15', '#c9b8e8', 0.5, 0.8,  3, 6.0, 7.5, 'SHADE'),
('Rhododendron',        'Rododendrs',          'ERICACEAE',       'SHRUB', 'PLANT', '04-20', '05-31', FALSE, TRUE, TRUE,  NULL,    NULL,    '05-15', '06-15', '#c2185b', 1.5, 1.5, 10, 4.5, 5.5, 'PART'),
('Highbush blueberry',  'Krūmmellene',         'ERICACEAE',       'SHRUB', 'PLANT', '04-20', '05-31', FALSE, TRUE, FALSE, '05-05', '10-25', '05-15', '06-05', '#fff4f4', 1.8, 1.5,  6, 4.0, 5.2, 'SUN'),
('Lilac',               'Ceriņi',              'OLEACEAE',        'SHRUB', 'PLANT', '04-15', '05-31', FALSE, TRUE, FALSE, '05-01', '10-20', '05-20', '06-10', '#a77bca', 3.5, 3.0,  8, 6.5, 7.5, 'SUN'),
('Panicle hydrangea',   'Skarainā hortenzija', 'HYDRANGEACEAE',   'SHRUB', 'PLANT', '04-20', '05-31', FALSE, TRUE, FALSE, '05-05', '10-20', '07-15', '09-30', '#f0ede0', 2.0, 2.0,  5, 5.5, 7.0, 'PART'),
('Rose',                'Roze',                'ROSACEAE',        'SHRUB', 'PLANT', '04-25', '05-20', FALSE, TRUE, FALSE, '05-01', '11-01', '06-15', '09-15', '#d23c4b', 1.0, 0.8,  3, 6.0, 7.0, 'SUN'),
('Blackcurrant',        'Upene',               'GROSSULARIACEAE', 'SHRUB', 'PLANT', '04-15', '05-15', FALSE, TRUE, FALSE, '05-01', '10-15', '05-10', '05-25', '#d7e3a5', 1.5, 1.5,  4, 6.0, 7.0, 'SUN'),
('Gooseberry',          'Ērkšķoga',            'GROSSULARIACEAE', 'SHRUB', 'PLANT', '04-15', '05-15', FALSE, TRUE, FALSE, '04-25', '10-15', '05-05', '05-20', '#d7e3a5', 1.2, 1.2,  4, 6.0, 7.0, 'SUN'),
('Raspberry',           'Avene',               'ROSACEAE',        'SHRUB', 'PLANT', '04-20', '05-20', FALSE, TRUE, FALSE, '05-01', '10-15', '06-05', '06-30', '#ffffff', 1.8, 0.6,  2, 5.5, 6.5, 'SUN'),
('Thuja',               'Tūja',                'CUPRESSACEAE',    'HEDGE', 'PLANT', '04-20', '05-31', FALSE, TRUE, TRUE,  NULL,    NULL,    NULL,    NULL,    NULL,      4.0, 1.2, 10, 6.0, 7.5, 'SUN'),
('Hornbeam',            'Parastais skābardis', 'BETULACEAE',      'HEDGE', 'PLANT', '04-15', '05-15', FALSE, TRUE, FALSE, '05-05', '10-25', NULL,    NULL,    NULL,      3.0, 1.0,  6, 6.0, 7.5, 'PART'),
('Apple tree',          'Ābele',               'ROSACEAE',        'TREE',  'PLANT', '04-20', '05-15', FALSE, TRUE, FALSE, '05-05', '10-25', '05-15', '05-31', '#f7d6dc', 5.0, 5.0, 15, 6.0, 7.0, 'SUN'),
('Pear tree',           'Bumbiere',            'ROSACEAE',        'TREE',  'PLANT', '04-20', '05-15', FALSE, TRUE, FALSE, '05-01', '10-25', '05-05', '05-20', '#ffffff', 6.0, 4.5, 15, 6.0, 7.0, 'SUN'),
('Plum tree',           'Plūme',               'ROSACEAE',        'TREE',  'PLANT', '04-20', '05-15', FALSE, TRUE, FALSE, '05-01', '10-20', '05-05', '05-20', '#ffffff', 4.5, 4.0, 12, 6.0, 7.5, 'SUN'),
('Sour cherry',         'Skābais ķirsis',      'ROSACEAE',        'TREE',  'PLANT', '04-20', '05-15', FALSE, TRUE, FALSE, '05-01', '10-20', '05-10', '05-25', '#ffffff', 4.0, 4.0, 10, 6.5, 7.5, 'SUN'),
('Norway maple',        'Parastā kļava',       'SAPINDACEAE',     'TREE',  'PLANT', '04-15', '05-15', FALSE, TRUE, FALSE, '05-01', '10-20', '04-25', '05-10', '#d6e07a', 20.0, 15.0, 40, 5.5, 7.5, 'SUN'),
('Silver birch',        'Āra bērzs',           'BETULACEAE',      'TREE',  'PLANT', '04-15', '05-15', FALSE, TRUE, FALSE, '05-01', '10-15', NULL,    NULL,    NULL,      20.0, 8.0, 30, 5.0, 7.0, 'SUN'),
('Norway spruce',       'Parastā egle',        'PINACEAE',        'TREE',  'PLANT', '04-15', '05-15', FALSE, TRUE, TRUE,  NULL,    NULL,    NULL,    NULL,    NULL,      25.0, 6.0, 40, 4.5, 6.5, 'PART');
