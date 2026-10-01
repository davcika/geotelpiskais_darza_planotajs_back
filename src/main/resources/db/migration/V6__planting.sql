-- Stādījums dobē. Sezona ir sown_on gads (atsevišķa kolonna nav vajadzīga).
-- Izdzēšot dobi, izdzēšas arī tās stādījumi.
CREATE TABLE planting (
    id               BIGSERIAL PRIMARY KEY,
    garden_object_id BIGINT       NOT NULL REFERENCES garden_object (id) ON DELETE CASCADE,
    plant_id         BIGINT       NOT NULL REFERENCES plant (id),
    sown_on          DATE         NOT NULL,
    variety          VARCHAR(60),
    note             VARCHAR(200)
);

CREATE INDEX idx_planting_object ON planting (garden_object_id);
