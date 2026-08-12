CREATE TABLE garden (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- Ģeometrija glabājas kā GeoJSON teksts koordinātās EPSG:3059 (LKS-92, metri)
CREATE TABLE garden_object (
    id             BIGSERIAL PRIMARY KEY,
    garden_id      BIGINT       NOT NULL REFERENCES garden (id) ON DELETE CASCADE,
    type           VARCHAR(20)  NOT NULL CHECK (type IN ('BED', 'TREE')),
    name           VARCHAR(100) NOT NULL,
    geometry       TEXT         NOT NULL,
    crown_radius_m DOUBLE PRECISION CHECK (crown_radius_m > 0)
);

CREATE INDEX idx_garden_object_garden_id ON garden_object (garden_id);

-- Kamēr nav lietotāju, visi objekti pieder vienam noklusējuma dārzam
INSERT INTO garden (name) VALUES ('Mans dārzs');
