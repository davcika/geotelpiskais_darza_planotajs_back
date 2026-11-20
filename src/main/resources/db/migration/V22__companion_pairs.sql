-- Widely-cited companion planting pairs (folk / extension-service guidance).
-- Pairs are looked up by name_en and stored once with plant_a_id < plant_b_id.
CREATE TEMP TABLE tmp_companion_pair (a_name TEXT, b_name TEXT, relation VARCHAR(10)) ON COMMIT DROP;
INSERT INTO tmp_companion_pair (a_name, b_name, relation) VALUES
    -- Tomato
    ('Tomato', 'Carrot', 'GOOD'), ('Tomato', 'Parsley', 'GOOD'), ('Tomato', 'Lettuce', 'GOOD'),
    ('Tomato', 'Onion', 'GOOD'), ('Tomato', 'Garlic', 'GOOD'), ('Tomato', 'Celery', 'GOOD'),
    ('Tomato', 'Spinach', 'GOOD'), ('Tomato', 'Pepper', 'GOOD'),
    ('Tomato', 'Dill', 'BAD'), ('Tomato', 'Sweet corn', 'BAD'), ('Tomato', 'Broccoli', 'BAD'),
    ('Tomato', 'Cauliflower', 'BAD'), ('Tomato', 'Kale', 'BAD'),
    -- Pepper
    ('Pepper', 'Basil', 'GOOD'), ('Pepper', 'Onion', 'GOOD'), ('Pepper', 'Carrot', 'GOOD'),
    ('Pepper', 'Marigold', 'GOOD'), ('Pepper', 'Bean', 'BAD'),
    -- Cucumber
    ('Cucumber', 'Bean', 'GOOD'), ('Cucumber', 'Pea', 'GOOD'), ('Cucumber', 'Lettuce', 'GOOD'),
    ('Cucumber', 'Radish', 'GOOD'), ('Cucumber', 'Sweet corn', 'GOOD'), ('Cucumber', 'Marigold', 'GOOD'),
    -- Squash family
    ('Zucchini', 'Bean', 'GOOD'), ('Zucchini', 'Sweet corn', 'GOOD'), ('Zucchini', 'Marigold', 'GOOD'),
    ('Zucchini', 'Radish', 'GOOD'), ('Zucchini', 'Potato', 'BAD'),
    ('Pumpkin', 'Bean', 'GOOD'), ('Pumpkin', 'Marigold', 'GOOD'),
    -- Beans / peas vs alliums
    ('Bean', 'Leek', 'BAD'), ('Pea', 'Leek', 'BAD'),
    ('Broad bean', 'Onion', 'BAD'), ('Broad bean', 'Garlic', 'BAD'), ('Broad bean', 'Leek', 'BAD'),
    ('Bean', 'Carrot', 'GOOD'), ('Bean', 'Cabbage', 'GOOD'), ('Bean', 'Beetroot', 'GOOD'),
    ('Bean', 'Celery', 'GOOD'), ('Bean', 'Swiss chard', 'GOOD'), ('Bean', 'Radish', 'GOOD'),
    ('Pea', 'Radish', 'GOOD'), ('Pea', 'Turnip', 'GOOD'), ('Pea', 'Sweet corn', 'GOOD'),
    ('Pea', 'Lettuce', 'GOOD'), ('Pea', 'Spinach', 'GOOD'), ('Pea', 'Potato', 'GOOD'),
    ('Broad bean', 'Potato', 'GOOD'), ('Broad bean', 'Sweet corn', 'GOOD'), ('Broad bean', 'Lettuce', 'GOOD'),
    -- Alliums
    ('Garlic', 'Lettuce', 'GOOD'), ('Garlic', 'Rose', 'GOOD'),
    ('Garlic', 'Beetroot', 'GOOD'), ('Garlic', 'Carrot', 'GOOD'), ('Garlic', 'Raspberry', 'GOOD'),
    ('Garlic', 'Cabbage', 'GOOD'), ('Garlic', 'Apple tree', 'GOOD'),
    ('Onion', 'Beetroot', 'GOOD'), ('Onion', 'Lettuce', 'GOOD'), ('Onion', 'Cabbage', 'GOOD'),
    ('Onion', 'Broccoli', 'GOOD'), ('Onion', 'Swiss chard', 'GOOD'), ('Onion', 'Parsnip', 'GOOD'),
    ('Leek', 'Celery', 'GOOD'), ('Leek', 'Parsnip', 'GOOD'), ('Leek', 'Cabbage', 'GOOD'),
    ('Leek', 'Beetroot', 'BAD'),
    -- Carrot / roots
    ('Carrot', 'Lettuce', 'GOOD'), ('Carrot', 'Radish', 'GOOD'), ('Carrot', 'Parsnip', 'BAD'),
    ('Carrot', 'Celery', 'BAD'), ('Carrot', 'Parsley', 'BAD'),
    ('Beetroot', 'Lettuce', 'GOOD'), ('Beetroot', 'Cabbage', 'GOOD'), ('Beetroot', 'Broccoli', 'GOOD'),
    ('Beetroot', 'Kale', 'GOOD'), ('Beetroot', 'Swiss chard', 'BAD'), ('Beetroot', 'Sweet corn', 'BAD'),
    ('Radish', 'Spinach', 'GOOD'), ('Radish', 'Pumpkin', 'GOOD'), ('Radish', 'Turnip', 'BAD'),
    ('Parsnip', 'Radish', 'GOOD'),
    ('Turnip', 'Pea', 'GOOD'), ('Turnip', 'Potato', 'BAD'),
    -- Potato
    ('Potato', 'Cabbage', 'GOOD'), ('Potato', 'Sweet corn', 'GOOD'), ('Potato', 'Marigold', 'GOOD'),
    ('Potato', 'Broad bean', 'GOOD'), ('Potato', 'Zucchini', 'BAD'), ('Potato', 'Raspberry', 'BAD'),
    ('Potato', 'Pepper', 'BAD'),
    -- Brassicas
    ('Cabbage', 'Onion', 'GOOD'), ('Cabbage', 'Marigold', 'GOOD'), ('Cabbage', 'Lettuce', 'GOOD'),
    ('Broccoli', 'Dill', 'GOOD'), ('Broccoli', 'Celery', 'GOOD'), ('Broccoli', 'Lettuce', 'GOOD'),
    ('Cauliflower', 'Dill', 'GOOD'), ('Cauliflower', 'Celery', 'GOOD'), ('Cauliflower', 'Bean', 'GOOD'),
    ('Kale', 'Dill', 'GOOD'), ('Kale', 'Onion', 'GOOD'), ('Kale', 'Bean', 'GOOD'),
    -- Leafy
    ('Lettuce', 'Dill', 'GOOD'), ('Lettuce', 'Parsley', 'BAD'),
    ('Lettuce', 'Celery', 'BAD'),
    ('Spinach', 'Cabbage', 'GOOD'), ('Spinach', 'Bean', 'GOOD'),
    ('Swiss chard', 'Cabbage', 'GOOD'), ('Swiss chard', 'Lettuce', 'GOOD'),
    ('Celery', 'Tomato', 'GOOD'), ('Celery', 'Parsley', 'BAD'),
    -- Herbs
    ('Basil', 'Lettuce', 'GOOD'), ('Basil', 'Marigold', 'GOOD'),
    ('Dill', 'Onion', 'GOOD'), ('Dill', 'Sweet corn', 'GOOD'),
    ('Parsley', 'Rose', 'GOOD'),
    -- Sweet corn
    ('Sweet corn', 'Zucchini', 'GOOD'), ('Sweet corn', 'Marigold', 'GOOD'),
    -- Berries / fruit
    ('Raspberry', 'Marigold', 'GOOD'), ('Raspberry', 'Tomato', 'BAD'),
    ('Blackcurrant', 'Raspberry', 'BAD'), ('Gooseberry', 'Tomato', 'GOOD'),
    ('Rose', 'Marigold', 'GOOD'), ('Apple tree', 'Marigold', 'GOOD');

-- Fail the migration if any name does not resolve to a built-in catalog plant.
DO $$
DECLARE missing TEXT;
BEGIN
    SELECT string_agg(DISTINCT n, ', ') INTO missing
    FROM (SELECT a_name AS n FROM tmp_companion_pair UNION SELECT b_name FROM tmp_companion_pair) names
    WHERE NOT EXISTS (SELECT 1 FROM plant p WHERE p.name_en = names.n AND p.owner_id IS NULL);
    IF missing IS NOT NULL THEN
        RAISE EXCEPTION 'V22 companion pairs: unknown plant name(s): %', missing;
    END IF;
END $$;

INSERT INTO plant_companion (plant_a_id, plant_b_id, relation)
SELECT DISTINCT ON (LEAST(a.id, b.id), GREATEST(a.id, b.id))
       LEAST(a.id, b.id), GREATEST(a.id, b.id), t.relation
FROM tmp_companion_pair t
JOIN plant a ON a.name_en = t.a_name AND a.owner_id IS NULL
JOIN plant b ON b.name_en = t.b_name AND b.owner_id IS NULL
WHERE a.id <> b.id
ORDER BY LEAST(a.id, b.id), GREATEST(a.id, b.id)
ON CONFLICT (plant_a_id, plant_b_id) DO NOTHING;
