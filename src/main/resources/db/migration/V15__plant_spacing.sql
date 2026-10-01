-- Augu atstatumi (cm) un ikona tabulas attēlošanai. spacing_cm = starp augiem rindā, row_spacing_cm = starp rindām.
-- Augi uz m² netiek glabāti, tos atvasina klients (10000 / (spacing * row_spacing)).
-- Vērtības aptuvenas, pēc Latvijas dārzkopības prakses (LLKC, sēklu tirgotāju ieteikumi); kokiem/krūmiem pēc vainaga platuma.
ALTER TABLE plant ADD COLUMN spacing_cm INT CHECK (spacing_cm IS NULL OR spacing_cm > 0);
ALTER TABLE plant ADD COLUMN row_spacing_cm INT CHECK (row_spacing_cm IS NULL OR row_spacing_cm > 0);
ALTER TABLE plant ADD COLUMN icon VARCHAR(40);

UPDATE plant p SET spacing_cm = v.s, row_spacing_cm = v.r, icon = v.i
FROM (VALUES
  ('Tomato', 50, 70, '🍅'), ('Pepper', 40, 50, '🫑'), ('Potato', 30, 70, '🥔'), ('Cucumber', 40, 100, '🥒'),
  ('Zucchini', 80, 100, '🥒'), ('Pumpkin', 100, 150, '🎃'), ('Cabbage', 50, 60, '🥬'), ('Cauliflower', 50, 60, '🥦'),
  ('Broccoli', 45, 60, '🥦'), ('Kale', 45, 60, '🥬'), ('Radish', 5, 15, '🌶️'), ('Turnip', 15, 30, '🧅'),
  ('Carrot', 5, 25, '🥕'), ('Parsley', 10, 25, '🌿'), ('Dill', 10, 25, '🌿'), ('Celery', 30, 40, '🥬'),
  ('Parsnip', 10, 35, '🥕'), ('Onion', 10, 25, '🧅'), ('Garlic', 15, 25, '🧄'), ('Leek', 15, 35, '🧅'),
  ('Pea', 5, 50, '🫛'), ('Bean', 10, 45, '🫘'), ('Broad bean', 15, 50, '🫘'), ('Beetroot', 10, 30, '🟣'),
  ('Spinach', 10, 25, '🥬'), ('Swiss chard', 30, 40, '🥬'), ('Lettuce', 25, 30, '🥬'), ('Marigold', 25, 30, '🌼'),
  ('Basil', 25, 30, '🌿'), ('Sweet corn', 35, 70, '🌽'),
  ('Tulip', 12, 12, '🌷'), ('Daffodil', 15, 15, '🌼'), ('Crocus', 8, 8, '🌸'), ('Peony', 80, 100, '🌺'),
  ('Hosta', 50, 60, '🍃'), ('Rhododendron', 120, 150, '🌺'), ('Highbush blueberry', 120, 150, '🫐'),
  ('Lilac', 200, 250, '🌸'), ('Panicle hydrangea', 150, 200, '💮'), ('Rose', 60, 80, '🌹'),
  ('Blackcurrant', 120, 150, '🍇'), ('Gooseberry', 120, 150, '🍈'), ('Raspberry', 50, 150, '🍓'),
  ('Thuja', 50, 100, '🌲'), ('Hornbeam', 40, 60, '🌳'),
  ('Apple tree', 400, 500, '🍎'), ('Pear tree', 400, 500, '🍐'), ('Plum tree', 300, 400, '🍑'),
  ('Sour cherry', 300, 400, '🍒'), ('Norway maple', 800, 800, '🍁'), ('Silver birch', 600, 600, '🌳'),
  ('Norway spruce', 500, 500, '🌲')
) AS v(n, s, r, i)
WHERE p.owner_id IS NULL AND lower(p.name_en) = lower(v.n);

-- Lietotāju augiem bez vērtībām: noklusējums pēc kategorijas
UPDATE plant SET icon = CASE category WHEN 'VEGETABLE' THEN '🥬' WHEN 'HERB' THEN '🌿' WHEN 'FLOWER' THEN '🌼'
    WHEN 'BULB' THEN '🌷' WHEN 'SHRUB' THEN '🌳' WHEN 'TREE' THEN '🌳' WHEN 'HEDGE' THEN '🌲' ELSE '🌱' END
WHERE icon IS NULL;
