ALTER TABLE auth.users ADD COLUMN email_verification_token VARCHAR(255);
ALTER TABLE auth.users ADD COLUMN email_verification_token_expiry TIMESTAMPTZ;
ALTER TABLE auth.users ADD COLUMN last_verification_email_sent_at TIMESTAMPTZ;