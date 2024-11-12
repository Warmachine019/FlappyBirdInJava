Flappy bird but in java with mySQL integration, created by Spandan.

1. You will need to have the JDK installed first, download that from the oracle website
2. You will also need the mySQL connector, to add it to VSCode, go to Java Projects on the bottom right > <Project_Name> > Referenced Libraries and add the "connector.jar" file.
3. go to the MySQL command line and paste this:

    CREATE DATABASE IF NOT EXISTS FlappyBirdDB;

    USE FlappyBirdDB;

    -- Table for storing login information
    CREATE TABLE IF NOT EXISTS LoginInfo (
        Username VARCHAR(50) PRIMARY KEY,
        Password VARCHAR(50) NOT NULL
    );

    -- Table for storing high scores
    CREATE TABLE IF NOT EXISTS HighScores (
        Username VARCHAR(50),
        Score INT,
        FOREIGN KEY (Username) REFERENCES LoginInfo(Username) ON DELETE CASCADE
    );

4. Finally, run "FlappyBirdApp.java" file and enjoy!
