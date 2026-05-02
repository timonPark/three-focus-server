-- Allow null for columns that OAuth users may not initially provide
ALTER TABLE users ALTER COLUMN password DROP NOT NULL;
ALTER TABLE users ALTER COLUMN phone DROP NOT NULL;
ALTER TABLE users ALTER COLUMN gender DROP NOT NULL;
ALTER TABLE users ALTER COLUMN birthday DROP NOT NULL;

-- Add OAuth provider support
ALTER TABLE users ADD COLUMN provider    VARCHAR(20)  NOT NULL DEFAULT 'LOCAL';
ALTER TABLE users ADD COLUMN provider_id VARCHAR(255);

-- is_profile_complete: false for new OAuth users until they fill in remaining fields
-- DEFAULT TRUE so existing LOCAL users are automatically marked complete
ALTER TABLE users ADD COLUMN is_profile_complete BOOLEAN NOT NULL DEFAULT TRUE;

-- Partial unique index: only enforce uniqueness when provider_id is not null
CREATE UNIQUE INDEX idx_users_provider_provider_id
    ON users (provider, provider_id)
    WHERE provider_id IS NOT NULL;
