CREATE TABLE tags (
                      id BIGSERIAL PRIMARY KEY,
                      name VARCHAR(50) UNIQUE NOT NULL,
                      slug VARCHAR(560) UNIQUE NOT NULL,
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE INDEX idx_tags_slug ON tags(slug);

CREATE INDEX idx_tags_name ON tags(name);