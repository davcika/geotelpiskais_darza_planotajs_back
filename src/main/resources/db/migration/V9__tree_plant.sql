-- Koks/krūms no kataloga: auga sugu un stādīšanas gadu izmanto pieauguša vainaga aprēķinam (frontendā)
ALTER TABLE garden_object
    ADD COLUMN plant_id     BIGINT REFERENCES plant (id),
    ADD COLUMN planted_year INT CHECK (planted_year BETWEEN 1900 AND 2200);
