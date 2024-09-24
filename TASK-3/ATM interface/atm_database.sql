CREATE DATABASE atm_db;

USE atm_db;

CREATE TABLE users (
    userId VARCHAR(50) PRIMARY KEY,
    pin VARCHAR(10),
    balance DOUBLE DEFAULT 0
);

CREATE TABLE transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50),
    transaction TEXT,
    FOREIGN KEY (userId) REFERENCES users(userId)
);
