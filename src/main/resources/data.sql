
-- All demo users have password DemoPass123! (BCrypt hash below).
-- Training types are created by V2__seed_training_types.sql.
INSERT INTO users (id, first_name, last_name, username, password, active) VALUES
(1, 'James', 'Bond', 'james.bond', '$2y$10$ENmf.X5v6.hFd4jiFoVSbOuVSfJhkVhFcSJX6C2QOSPoZjNMPjpUC', true),
(2, 'Sarah', 'Connor', 'sarah.connor', '$2y$10$ENmf.X5v6.hFd4jiFoVSbOuVSfJhkVhFcSJX6C2QOSPoZjNMPjpUC', true),
(3, 'James', 'Wick', 'James.wick', '$2y$10$ENmf.X5v6.hFd4jiFoVSbOuVSfJhkVhFcSJX6C2QOSPoZjNMPjpUC', true),
(4, 'Lara', 'Croft', 'lara.croft', '$2y$10$ENmf.X5v6.hFd4jiFoVSbOuVSfJhkVhFcSJX6C2QOSPoZjNMPjpUC', true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO trainees (id, date_of_birth, city, street, building_number, zipcode) VALUES
(1, DATE '1990-04-13', 'Wroclaw', 'Main Street', 10, '50-001'),
(3, DATE '1985-09-02', 'Wroclaw', 'Park Street', 5, '50-002')
ON CONFLICT (id) DO NOTHING;

INSERT INTO trainers (id, specialization_id) VALUES
(2, (SELECT id FROM training_types WHERE name = 'Fitness')),
(4, (SELECT id FROM training_types WHERE name = 'Yoga'))
ON CONFLICT (id) DO NOTHING;

INSERT INTO trainee_trainers (trainee_id, trainer_id) VALUES (1, 2), (3, 4)
ON CONFLICT DO NOTHING;

INSERT INTO trainings (id, trainee_id, trainer_id, name, training_type_id, training_date, duration_minutes) VALUES
(1, 1, 2, 'Strength fundamentals', (SELECT id FROM training_types WHERE name = 'Fitness'), DATE '2026-11-10', 60),
(2, 3, 4, 'Yoga fundamentals', (SELECT id FROM training_types WHERE name = 'Yoga'), DATE '2026-11-11', 45)
ON CONFLICT (id) DO NOTHING;

-- Explicit IDs require advancing PostgreSQL identity sequences.
SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users), true);
SELECT setval(pg_get_serial_sequence('trainings', 'id'), (SELECT MAX(id) FROM trainings), true);
