DROP TABLE IF EXISTS event_templates;
DROP TABLE IF EXISTS schedule_templates;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS schedules;
DROP TABLE IF EXISTS goals;
DROP TABLE IF EXISTS categories;

CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE goals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    category BIGINT,
    priority ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL DEFAULT 'MEDIUM',
    finish_by_date DATE,
    FOREIGN KEY (category) REFERENCES categories(id)
);

CREATE TABLE schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL UNIQUE
);

CREATE TABLE events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    goal BIGINT,
    schedule BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    FOREIGN KEY (goal) REFERENCES goals(id),
    FOREIGN KEY (schedule) REFERENCES schedules(id)
);

CREATE TABLE schedule_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE event_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    goal BIGINT,
    schedule_template BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    FOREIGN KEY (goal) REFERENCES goals(id),
    FOREIGN KEY (schedule_template) REFERENCES schedule_templates(id)
);
