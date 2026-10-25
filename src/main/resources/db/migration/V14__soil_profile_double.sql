-- V12 izveidoja augsnes skaitļus kā NUMERIC, bet entītija (SoilProfile) lieto Double un Hibernate validate to noraida.
-- Pārveidojam uz DOUBLE PRECISION tāpat kā plant.ph_min/ph_max (V8); CHECK ierobežojumi saglabājas.
ALTER TABLE soil_profile
    ALTER COLUMN ph                 TYPE DOUBLE PRECISION,
    ALTER COLUMN organic_matter_pct TYPE DOUBLE PRECISION,
    ALTER COLUMN nitrogen           TYPE DOUBLE PRECISION,
    ALTER COLUMN phosphorus         TYPE DOUBLE PRECISION,
    ALTER COLUMN potassium          TYPE DOUBLE PRECISION;
