INSERT INTO training_types (name)
VALUES ('Fitness'),
       ('Yoga'),
       ('Zumba'),
       ('Stretching'),
       ('Resistance'),
       ('Cardio')
ON CONFLICT (name) DO NOTHING;