-- Stādījuma bloks dobē: taisnstūris metros dobes lokālajās koordinātās
-- (sākumpunkts = dobes poligona robežtaisnstūra minX/minY EPSG:3059, asis kā kartē, y uz ziemeļiem).
-- Vai nu visi četri lauki, vai neviens (vecie stādījumi lieto šūnas, sk. V7).
ALTER TABLE planting
    ADD COLUMN block_x     DOUBLE PRECISION,
    ADD COLUMN block_y     DOUBLE PRECISION,
    ADD COLUMN block_w     DOUBLE PRECISION CHECK (block_w > 0),
    ADD COLUMN block_h     DOUBLE PRECISION CHECK (block_h > 0),
    ADD COLUMN plant_count INT CHECK (plant_count >= 1),
    ADD CONSTRAINT planting_block_all_or_none CHECK (
        (block_x IS NULL AND block_y IS NULL AND block_w IS NULL AND block_h IS NULL)
        OR (block_x IS NOT NULL AND block_y IS NOT NULL AND block_w IS NOT NULL AND block_h IS NOT NULL));
