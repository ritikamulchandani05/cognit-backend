CREATE TABLE otps (
                      id            BIGSERIAL PRIMARY KEY,
                      user_id       BIGINT      NOT NULL REFERENCES users(id),
                      otp_value     VARCHAR(6)  NOT NULL,
                      email_req_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
                      valid_till    TIMESTAMP   NOT NULL,
                      used          BOOLEAN     NOT NULL DEFAULT FALSE,
                      created_at    TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_otps_user_id   ON otps(user_id);
CREATE INDEX idx_otps_valid_till ON otps(valid_till);