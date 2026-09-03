CREATE TABLE app_user (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL
);

-- Katram dārzam ir īpašnieks
ALTER TABLE garden ADD COLUMN owner_id BIGINT REFERENCES app_user (id) ON DELETE CASCADE;

-- Izstrādes konts; esošie dārzi pāriet tam. Parole nav derīgs bcrypt hešs, tāpēc ar paroli
-- ieiet nevar, tikai caur /api/auth/dev-login profilā "dev". E-pastam jāsakrīt ar AuthService.DEV_EMAIL.
WITH dev AS (
    INSERT INTO app_user (email, password_hash) VALUES ('dev@darza-planotajs.lv', 'DEV_LOGIN_ONLY')
    RETURNING id
)
UPDATE garden SET owner_id = (SELECT id FROM dev);

ALTER TABLE garden ALTER COLUMN owner_id SET NOT NULL;

CREATE INDEX idx_garden_owner_id ON garden (owner_id);
