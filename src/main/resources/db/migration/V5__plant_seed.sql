-- Sākotnējais katalogs: izplatītas kultūras Latvijas klimatā (Rīgas apkārtne, atklāts grunts).
-- Laiki ir aptuveni, apkopoti no Latvijas sēklu tirgotāju sējas kalendāriem un LLKC ieteikumiem;
-- dienas līdz ražai skaitītas no sējas (DIRECT) vai no sējas telpās (TRANSPLANT).
-- Marigold ir samtene (Tagetes), nevis kliņģerīte. Ķiploks stādāms rudenī, raža nākamajā gadā (kalendāram jāpārnes gads).
INSERT INTO plant (name_en, name_lv, family, sow_method, indoor_sow_from, indoor_sow_to, outdoor_from, outdoor_to,
                   days_to_harvest, harvest_window_days, frost_sensitive) VALUES
('Tomato',      'Tomāts',          'SOLANACEAE',     'TRANSPLANT', '03-01', '04-01', '05-25', '06-10', 140, 60, TRUE),
('Pepper',      'Paprika',         'SOLANACEAE',     'TRANSPLANT', '02-15', '03-15', '06-01', '06-15', 160, 50, TRUE),
('Potato',      'Kartupelis',      'SOLANACEAE',     'DIRECT',     NULL,    NULL,    '04-25', '05-25', 100, 30, TRUE),
('Cucumber',    'Gurķis',          'CUCURBITACEAE',  'TRANSPLANT', '04-20', '05-10', '06-01', '06-15',  60, 45, TRUE),
('Zucchini',    'Kabacis',         'CUCURBITACEAE',  'TRANSPLANT', '04-20', '05-10', '05-30', '06-15',  55, 60, TRUE),
('Pumpkin',     'Ķirbis',          'CUCURBITACEAE',  'TRANSPLANT', '04-20', '05-10', '06-01', '06-15', 110, 30, TRUE),
('Cabbage',     'Galviņkāposti',   'BRASSICACEAE',   'TRANSPLANT', '03-20', '04-20', '05-10', '06-01', 100, 30, FALSE),
('Cauliflower', 'Ziedkāposti',     'BRASSICACEAE',   'TRANSPLANT', '03-20', '04-20', '05-10', '06-01',  85, 21, FALSE),
('Broccoli',    'Brokoļi',         'BRASSICACEAE',   'TRANSPLANT', '03-20', '04-20', '05-10', '06-01',  75, 21, FALSE),
('Kale',        'Lapu kāposti',    'BRASSICACEAE',   'TRANSPLANT', '03-20', '04-30', '05-10', '06-10',  70, 90, FALSE),
('Radish',      'Redīss',          'BRASSICACEAE',   'DIRECT',     NULL,    NULL,    '04-20', '08-20',  28, 14, FALSE),
('Turnip',      'Rācenis',         'BRASSICACEAE',   'DIRECT',     NULL,    NULL,    '05-01', '07-15',  50, 21, FALSE),
('Carrot',      'Burkāns',         'APIACEAE',       'DIRECT',     NULL,    NULL,    '04-25', '05-25', 100, 40, FALSE),
('Parsley',     'Pētersīļi',       'APIACEAE',       'DIRECT',     NULL,    NULL,    '04-20', '05-20',  75, 90, FALSE),
('Dill',        'Dilles',          'APIACEAE',       'DIRECT',     NULL,    NULL,    '04-25', '07-15',  40, 30, FALSE),
('Celery',      'Selerija',        'APIACEAE',       'TRANSPLANT', '02-20', '03-20', '05-20', '06-05', 170, 30, FALSE),
('Parsnip',     'Pastinaks',       'APIACEAE',       'DIRECT',     NULL,    NULL,    '04-25', '05-20', 130, 40, FALSE),
('Onion',       'Sīpols',          'AMARYLLIDACEAE', 'DIRECT',     NULL,    NULL,    '04-25', '05-15', 100, 21, FALSE),
('Garlic',      'Ķiploks',         'AMARYLLIDACEAE', 'DIRECT',     NULL,    NULL,    '09-25', '10-25', 280, 21, FALSE),
('Leek',        'Puravs',          'AMARYLLIDACEAE', 'TRANSPLANT', '03-01', '03-31', '05-15', '06-01', 130, 60, FALSE),
('Pea',         'Zirņi',           'FABACEAE',       'DIRECT',     NULL,    NULL,    '04-20', '05-20',  65, 30, FALSE),
('Bean',        'Pupiņas',         'FABACEAE',       'DIRECT',     NULL,    NULL,    '05-25', '06-10',  60, 40, TRUE),
('Broad bean',  'Cūku pupas',      'FABACEAE',       'DIRECT',     NULL,    NULL,    '04-20', '05-10',  90, 21, FALSE),
('Beetroot',    'Galda biete',     'AMARANTHACEAE',  'DIRECT',     NULL,    NULL,    '05-10', '06-01',  90, 40, FALSE),
('Spinach',     'Spināti',         'AMARANTHACEAE',  'DIRECT',     NULL,    NULL,    '04-20', '05-20',  40, 21, FALSE),
('Swiss chard', 'Lapu biete',      'AMARANTHACEAE',  'DIRECT',     NULL,    NULL,    '05-01', '06-01',  60, 90, FALSE),
('Lettuce',     'Salāti',          'ASTERACEAE',     'DIRECT',     NULL,    NULL,    '04-20', '07-31',  50, 21, FALSE),
('Marigold',    'Samtene',         'ASTERACEAE',     'DIRECT',     NULL,    NULL,    '05-01', '05-31',  60, 90, FALSE),
('Basil',       'Baziliks',        'LAMIACEAE',      'TRANSPLANT', '04-01', '04-30', '06-01', '06-15',  60, 60, TRUE),
('Sweet corn',  'Cukurkukurūza',   'POACEAE',        'TRANSPLANT', '04-25', '05-15', '06-01', '06-10', 105, 21, TRUE);

-- Saderība pēc nosaukumiem; LEAST/GREATEST nodrošina a < b
INSERT INTO plant_companion (plant_a_id, plant_b_id, relation)
SELECT LEAST(a.id, b.id), GREATEST(a.id, b.id), pair.relation
FROM (VALUES
    ('Tomato', 'Basil', 'GOOD'), ('Tomato', 'Marigold', 'GOOD'), ('Carrot', 'Onion', 'GOOD'),
    ('Carrot', 'Leek', 'GOOD'), ('Cucumber', 'Dill', 'GOOD'), ('Cabbage', 'Dill', 'GOOD'),
    ('Cabbage', 'Celery', 'GOOD'), ('Bean', 'Sweet corn', 'GOOD'), ('Pumpkin', 'Sweet corn', 'GOOD'),
    ('Lettuce', 'Radish', 'GOOD'), ('Potato', 'Bean', 'GOOD'), ('Pea', 'Carrot', 'GOOD'),
    ('Tomato', 'Potato', 'BAD'), ('Bean', 'Onion', 'BAD'), ('Pea', 'Onion', 'BAD'),
    ('Bean', 'Garlic', 'BAD'), ('Pea', 'Garlic', 'BAD'), ('Cabbage', 'Tomato', 'BAD'),
    ('Potato', 'Cucumber', 'BAD'), ('Potato', 'Pumpkin', 'BAD'), ('Carrot', 'Dill', 'BAD')
) AS pair (a_name, b_name, relation)
JOIN plant a ON a.name_en = pair.a_name
JOIN plant b ON b.name_en = pair.b_name;
