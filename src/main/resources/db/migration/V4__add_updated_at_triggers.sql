CREATE OR REPLACE FUNCTION update_at_column()
       RETURNS TRIGGER AS $$
       BEGIN
            NEW.updated_at = now();
            RETURN NEW;
       END; $$
       language 'plpgsql';

CREATE TRIGGER handle_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_at_column();

CREATE TRIGGER handle_events_updated_at
    BEFORE UPDATE ON events
    FOR EACH ROW EXECUTE FUNCTION update_at_column();
