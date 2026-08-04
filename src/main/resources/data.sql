INSERT INTO categories (name) VALUES
    ('Health'),
    ('Career');

INSERT INTO goals (name, description, completed, category, priority, finish_by_date) VALUES
    ('Run a 5k', 'Train and complete a 5k run', false, 1, 'MEDIUM', '2026-09-15'),
    ('Drink more water', 'Aim for 8 glasses a day', false, 1, 'LOW', NULL),
    ('Learn Spring Boot', 'Build a full CRUD app with Spring Boot', false, 2, 'HIGH', '2026-10-01'),
    ('Get promoted', 'Work towards a promotion this year', false, 2, 'HIGH', '2026-12-31'),
    ('Read 12 books', 'Read one book per month', false, 1, 'MEDIUM', NULL);

INSERT INTO schedules (date) VALUES
    ('2026-07-28'),
    ('2026-07-29'),
    ('2026-07-30');

INSERT INTO events (goal, schedule, name, start_time, end_time) VALUES
    (1, 1, 'Morning run', '07:00:00', '07:30:00'),
    (NULL, 1, 'Team standup', '09:00:00', '09:15:00'),
    (3, 2, 'Spring Boot study session', '18:00:00', '19:30:00'),
    (NULL, 2, 'Dentist appointment', '11:00:00', '11:45:00'),
    (5, 3, 'Reading time', '20:00:00', '21:00:00');

INSERT INTO schedule_templates (name) VALUES
    ('Weekday Routine'),
    ('Weekend Routine');

INSERT INTO event_templates (goal, schedule_template, name, start_time, end_time) VALUES
    (1, 1, 'Morning run', '07:00:00', '07:30:00'),
    (NULL, 1, 'Team standup', '09:00:00', '09:15:00'),
    (3, 2, 'Weekend study block', '10:00:00', '12:00:00');
