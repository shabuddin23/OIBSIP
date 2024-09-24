CREATE DATABASE ReservationSystem;

USE ReservationSystem;

CREATE TABLE Users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    loginId VARCHAR(50) NOT NULL,
    password VARCHAR(50) NOT NULL
);

CREATE TABLE Reservations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    trainNumber VARCHAR(20),
    classType VARCHAR(20),
    dateOfJourney DATE,
    fromPlace VARCHAR(50),
    toPlace VARCHAR(50)
);

CREATE TABLE Cancellations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pnrNumber VARCHAR(20),
    reservationId INT,
    FOREIGN KEY (reservationId) REFERENCES Reservations(id)
);
INSERT INTO Users (loginId, password) VALUES ('admin', 'admin');