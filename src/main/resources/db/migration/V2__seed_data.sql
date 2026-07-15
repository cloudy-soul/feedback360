-- src/main/resources/db/migration/V2__seed_data.sql

INSERT INTO users (first_name, last_name, email, password_hash, role, department, active)
VALUES ('System', 'Admin', 'admin@feedback360.local',
        '$2a$10$t0qZvcUaK2L9.omgUGP5S.n/Rwn/.HNlZBUymZM.ilhV8DJH8qdNC',
        'ADMIN', 'IT', true);

INSERT INTO system_setting (setting_key, setting_value) VALUES
    ('reminder.delay.days', '5'),
    ('email.template.reminder', 'Don''t forget to share your feedback on {moduleTitle}!');