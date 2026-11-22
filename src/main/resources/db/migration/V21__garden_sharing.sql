-- Dārza koplietošana: īpašnieks (garden.owner_id) uzaicina citus ar saiti; uzaicinātie ir redaktori.
-- Īpašnieks šajā tabulā nav; dalībnieki var mainīt visu dārzā, izņemot dārza dzēšanu un dalībnieku pārvaldību.
CREATE TABLE garden_member (
    garden_id BIGINT      NOT NULL REFERENCES garden (id) ON DELETE CASCADE,
    user_id   BIGINT      NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (garden_id, user_id)
);

CREATE INDEX idx_garden_member_user ON garden_member (user_id);

-- Viena aktīva uzaicinājuma saite dārzam; jauna saite aizstāj veco, atsaukšana dzēš rindu.
-- Saiti var izmantot vairāki cilvēki līdz derīguma beigām.
CREATE TABLE garden_invite (
    garden_id  BIGINT      PRIMARY KEY REFERENCES garden (id) ON DELETE CASCADE,
    token      VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL
);
