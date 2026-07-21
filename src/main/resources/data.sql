INSERT INTO types (name) VALUES
    ('Health'),
    ('Career');

INSERT INTO goals (name, description, completed, type, priority) VALUES
    ('Run a 5k', 'Train and complete a 5k run', false, 1, 'MEDIUM'),
    ('Drink more water', 'Aim for 8 glasses a day', false, 1, 'LOW'),
    ('Learn Spring Boot', 'Build a full CRUD app with Spring Boot', false, 2, 'HIGH'),
    ('Get promoted', 'Work towards a promotion this year', false, 2, 'HIGH'),
    ('Read 12 books', 'Read one book per month', false, 1, 'MEDIUM');
