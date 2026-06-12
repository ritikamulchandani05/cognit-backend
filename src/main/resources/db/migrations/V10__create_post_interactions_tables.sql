-- Tracks which user liked which post (logged-in users only)
CREATE TABLE post_likes (
                            id         BIGSERIAL PRIMARY KEY,
                            post_id    BIGINT    NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
                            user_id    BIGINT    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                            UNIQUE (post_id, user_id)
);

CREATE INDEX idx_post_likes_post_id ON post_likes(post_id);
CREATE INDEX idx_post_likes_user_id ON post_likes(user_id);

-- Tracks view de-duplication: 1 per user per day, 1 per anon fingerprint per day
CREATE TABLE post_views (
                            id          BIGSERIAL    PRIMARY KEY,
                            post_id     BIGINT       NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
                            user_id     BIGINT       REFERENCES users(id) ON DELETE SET NULL,  -- NULL = anonymous
                            fingerprint VARCHAR(64),                                            -- hashed IP+UA for anon
                            viewed_on   DATE         NOT NULL,
                            created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
                            UNIQUE (post_id, user_id, viewed_on),            -- logged-in de-dup
                            UNIQUE (post_id, fingerprint, viewed_on)         -- anon de-dup
);

CREATE INDEX idx_post_views_post_id ON post_views(post_id);