CREATE TABLE IF NOT EXISTS persons(
    age                 int,
    firstName           text,
    lastName            text,
    PRIMARY KEY((age), firstName, lastName)
);