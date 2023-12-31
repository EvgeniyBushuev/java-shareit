DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS items CASCADE;
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS requests CASCADE;

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_name VARCHAR(255) NOT NULL,
    email VARCHAR(512) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS requests (
    request_id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    request_description VARCHAR(1024)                  NOT NULL,
    requester_id        BIGINT REFERENCES users (user_id) NOT NULL,
    create_date         TIMESTAMP WITHOUT TIME ZONE    NOT NULL
);

CREATE TABLE IF NOT EXISTS items (
    item_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    item_name VARCHAR(255) NOT NULL,
    description VARCHAR(1024) NOT NULL,
    available BOOLEAN NOT NULL,
    owner_id BIGINT REFERENCES users (user_id) NOT NULL,
    request_id BIGINT REFERENCES requests (request_id)
);

CREATE TABLE IF NOT EXISTS bookings (
    booking_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    start_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    item_id BIGINT REFERENCES items (item_id) NOT NULL,
    booker_id BIGINT REFERENCES users (user_id) NOT NULL,
    status VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS comments (
    comment_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    comment_text VARCHAR(1024) NOT NULL,
    item_id BIGINT REFERENCES items (item_id) NOT NULL,
    author_id BIGINT REFERENCES users (user_id) NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
