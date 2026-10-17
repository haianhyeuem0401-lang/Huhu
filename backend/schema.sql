CREATE TABLE IF NOT EXISTS activation_keys (
    id BIGSERIAL PRIMARY KEY,
    key_sha256 CHAR(64) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Store SHA-256 hashes, not raw activation keys.
-- Generate a hash in a trusted admin tool and insert it into key_sha256.
