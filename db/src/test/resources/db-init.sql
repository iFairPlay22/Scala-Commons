CREATE TABLE IF NOT EXISTS PERSONS(
    id                  SERIAL PRIMARY KEY,
    first_name          VARCHAR(25),
    last_name           VARCHAR(25),
    age                 INT
);