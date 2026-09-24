-- Lietotāju augi un moderācija. owner_id NULL = iebūvēts kataloga augs.
-- PRIVATE redz tikai īpašnieks; PENDING gaida admina apstiprinājumu; APPROVED redz visi; REJECTED ar review_note.
ALTER TABLE plant
    ADD COLUMN owner_id     BIGINT REFERENCES app_user (id),
    ADD COLUMN status       VARCHAR(10) NOT NULL DEFAULT 'APPROVED'
        CHECK (status IN ('APPROVED', 'PRIVATE', 'PENDING', 'REJECTED')),
    ADD COLUMN submitted_at TIMESTAMPTZ,
    ADD COLUMN review_note  VARCHAR(500),
    -- Iebūvētie augi vienmēr apstiprināti
    ADD CONSTRAINT plant_builtin_approved CHECK (owner_id IS NOT NULL OR status = 'APPROVED');

-- Unikāls nosaukums tikai kopīgajā katalogā; privātie augi drīkst atkārtoties
ALTER TABLE plant DROP CONSTRAINT plant_name_en_key;
CREATE UNIQUE INDEX uq_plant_approved_name_en ON plant (lower(name_en)) WHERE status = 'APPROVED';
CREATE INDEX idx_plant_owner_id ON plant (owner_id);

-- Lomas: USER vai ADMIN (moderē augus). Admins tiek iecelts ar SQL (sk. DOKUMENTACIJAS_PIEZIMES.md §17)
ALTER TABLE app_user ADD COLUMN role VARCHAR(10) NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN'));

-- Izstrādes konts ir admins, lai moderāciju var izmēģināt lokāli
UPDATE app_user SET role = 'ADMIN' WHERE email = 'dev@darza-planotajs.lv';
