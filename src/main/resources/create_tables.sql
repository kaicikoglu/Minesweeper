-- create_tables.sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE grid (
                    gridId INT PRIMARY KEY ,
                    sizeRow INT,
                    sizeCol INT
);

CREATE TABLE field (
                    filedID INT PRIMARY KEY,
                    gridID INT,
                    row INT,
                    column INT
);