CREATE TABLE IF NOT EXISTS events (
    id             UUID PRIMARY KEY,
    organizer_id   UUID NOT NULL,
    title          VARCHAR(200) NOT NULL,
    description    TEXT,
    category       VARCHAR(100) NOT NULL,
    location       VARCHAR(300) NOT NULL,
    start_date     TIMESTAMPTZ NOT NULL,
    end_date       TIMESTAMPTZ NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    image_url      VARCHAR(500),
    version        BIGINT NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_events_status ON events (status);
CREATE INDEX IF NOT EXISTS idx_events_organizer ON events (organizer_id);
CREATE INDEX IF NOT EXISTS idx_events_category ON events (category);
CREATE INDEX IF NOT EXISTS idx_events_start_date ON events (start_date);
