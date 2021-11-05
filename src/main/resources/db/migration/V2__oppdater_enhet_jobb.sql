CREATE TYPE oppdater_enhet_jobb_type AS ENUM (
    'MODERENHET',
    'UNDERENHET'
    );

CREATE TYPE oppdater_enhet_jobb_status AS ENUM (
    'IN_PROGRESS',
    'COMPLETED',
    'PAUSED'
    );

CREATE TABLE oppdater_enhet_jobb
(
    id           SERIAL PRIMARY KEY,
    current_page INT                        NOT NULL DEFAULT 0,
    page_size    INT                        NOT NULL DEFAULT 0,
    total_pages  INT                        NOT NULL DEFAULT 0,
    type         oppdater_enhet_jobb_type   NOT NULL,
    status       oppdater_enhet_jobb_status NOT NULL,
    started_at   TIMESTAMP WITH TIME ZONE   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paused_at    TIMESTAMP WITH TIME ZONE,
    finished_at  TIMESTAMP WITH TIME ZONE,
    updated_at   TIMESTAMP WITH TIME ZONE   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER set_timestamp
    BEFORE UPDATE
    ON oppdater_enhet_jobb
    FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();
