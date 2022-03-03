DROP DATABASE IF EXISTS connectfour;
CREATE DATABASE connectfour;
USE connectfour;

DROP TABLE IF EXISTS c4players;
CREATE TABLE c4players(
	id INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    username VARCHAR(50),
    password VARCHAR(64),
    isRegistered BOOL
);