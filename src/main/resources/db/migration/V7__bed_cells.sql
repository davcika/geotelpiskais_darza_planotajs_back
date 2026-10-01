-- Dobes režģis: robežtaisnstūris sadalīts grid_cols × grid_rows šūnās (NULL = noklusējums ~30 cm šūnas, sk. BedGrid)
ALTER TABLE garden_object
    ADD COLUMN grid_cols INT CHECK (grid_cols BETWEEN 1 AND 100),
    ADD COLUMN grid_rows INT CHECK (grid_rows BETWEEN 1 AND 100);

-- Stādījuma aizņemtās šūnas kā "kol,rinda;kol,rinda" (NULL = vieta dobē nav norādīta)
ALTER TABLE planting ADD COLUMN cells TEXT;
