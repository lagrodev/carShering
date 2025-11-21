CREATE SEQUENCE IF NOT EXISTS car_rental.refresh_token_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE car_rental.refresh_tokens
(
    id          BIGINT                      NOT NULL,
    token_hash  VARCHAR(60)                 NOT NULL,
    client_id   BIGINT                      NOT NULL,
    expiry_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    revoked     BOOLEAN                     NOT NULL,
    revoked_at  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id)
);

ALTER TABLE car_rental.refresh_tokens
    ADD CONSTRAINT uc_refresh_tokens_token_hash UNIQUE (token_hash);

ALTER TABLE car_rental.refresh_tokens
    ADD CONSTRAINT FK_REFRESH_TOKENS_ON_CLIENT FOREIGN KEY (client_id) REFERENCES car_rental.client (id);

CREATE INDEX idx_4fc22ad287dd814fd10209148 ON car_rental.refresh_tokens (client_id);