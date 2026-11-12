CREATE DATABASE IF NOT EXISTS practice;
USE practice;

CREATE TABLE IF NOT EXISTS People (
    id              BIGINT       NOT NULL,
    user_id         VARCHAR(50)  NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    sex             VARCHAR(20),
    email           VARCHAR(255),
    phone           VARCHAR(50),
    date_of_birth   DATE,
    job_title       VARCHAR(255),
    PRIMARY KEY (id)
);
