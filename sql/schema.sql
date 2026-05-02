CREATE DATABASE IF NOT EXISTS flight_reservation;
USE flight_reservation;

CREATE TABLE Airline (
    airline_id VARCHAR(2) PRIMARY KEY,
    name       VARCHAR(50) NOT NULL
);

CREATE TABLE Aircraft (
    aircraft_id INT PRIMARY KEY AUTO_INCREMENT,
    model       VARCHAR(50) NOT NULL,
    capacity    INT NOT NULL,
    airline_id  VARCHAR(2) NOT NULL,
    FOREIGN KEY (airline_id) REFERENCES Airline(airline_id)
);

CREATE TABLE Airport (
    airport_id VARCHAR(3) PRIMARY KEY,
    name       VARCHAR(50) NOT NULL,
    city       VARCHAR(50) NOT NULL,
    country    VARCHAR(50) NOT NULL
);

CREATE TABLE Flight (
    flight_id      INT PRIMARY KEY AUTO_INCREMENT,
    flight_num     VARCHAR(10) NOT NULL,
    airline_id     VARCHAR(2) NOT NULL,
    aircraft_id    INT NOT NULL,
    dep_airport_id VARCHAR(3) NOT NULL,
    arr_airport_id VARCHAR(3) NOT NULL,
    dep_time       TIME NOT NULL,
    arr_time       TIME NOT NULL,
    type           VARCHAR(15) NOT NULL,
    days_of_week   VARCHAR(30) NOT NULL,
    economy_price  DECIMAL(10,2) NOT NULL DEFAULT 200.00,
    business_price DECIMAL(10,2) NOT NULL DEFAULT 500.00,
    first_price    DECIMAL(10,2) NOT NULL DEFAULT 1000.00,
    UNIQUE KEY uq_flight (airline_id, flight_num),
    FOREIGN KEY (airline_id)     REFERENCES Airline(airline_id),
    FOREIGN KEY (aircraft_id)    REFERENCES Aircraft(aircraft_id),
    FOREIGN KEY (dep_airport_id) REFERENCES Airport(airport_id),
    FOREIGN KEY (arr_airport_id) REFERENCES Airport(airport_id)
);

CREATE TABLE Customer (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(50) NOT NULL,
    address     VARCHAR(100),
    email       VARCHAR(50) UNIQUE NOT NULL,
    phone       VARCHAR(15),
    password    VARCHAR(255) NOT NULL
);

CREATE TABLE Employee (
    employee_id INT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(50) NOT NULL,
    email       VARCHAR(50) UNIQUE NOT NULL,
    type        VARCHAR(15) NOT NULL,
    password    VARCHAR(255) NOT NULL
);

CREATE TABLE Ticket (
    ticket_num        INT PRIMARY KEY AUTO_INCREMENT,
    customer_id       INT NOT NULL,
    total_fare        FLOAT NOT NULL,
    purchase_datetime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    booking_fee       FLOAT NOT NULL DEFAULT 25.00,
    type              VARCHAR(15) NOT NULL,
    flexible          BOOLEAN NOT NULL DEFAULT FALSE,
    status            VARCHAR(10) NOT NULL DEFAULT 'active',
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id)
);

CREATE TABLE TicketFlight (
    ticket_num     INT NOT NULL,
    flight_id      INT NOT NULL,
    sequence_order INT NOT NULL,
    dep_date       DATE NOT NULL,
    seat_num       VARCHAR(5),
    meal_pref      VARCHAR(20) DEFAULT 'standard',
    class          VARCHAR(10) NOT NULL,
    PRIMARY KEY (ticket_num, flight_id),
    FOREIGN KEY (ticket_num) REFERENCES Ticket(ticket_num) ON DELETE CASCADE,
    FOREIGN KEY (flight_id)  REFERENCES Flight(flight_id)
);

CREATE TABLE Waitlist (
    customer_id  INT NOT NULL,
    flight_id    INT NOT NULL,
    position     INT NOT NULL,
    request_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dep_date     DATE NOT NULL,
    class        VARCHAR(10) NOT NULL,
    PRIMARY KEY (customer_id, flight_id),
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id),
    FOREIGN KEY (flight_id)   REFERENCES Flight(flight_id)
);

CREATE TABLE Question (
    question_id       INT PRIMARY KEY AUTO_INCREMENT,
    customer_id       INT NOT NULL,
    rep_id            INT,
    subject           VARCHAR(200),
    question_text     TEXT NOT NULL,
    answer_text       TEXT,
    asked_datetime    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    answered_datetime DATETIME,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id),
    FOREIGN KEY (rep_id)      REFERENCES Employee(employee_id)
);

INSERT INTO Employee (name, email, type, password)
VALUES ('Admin User', 'admin@airline.com', 'admin', 'admin123'),
       ('Jane Smith', 'rep1@airline.com',  'rep',   'rep123');

INSERT INTO Customer (name, address, email, phone, password) VALUES
('John Doe',  '123 Main St, New York, NY',    'john@example.com', '555-1234', 'customer123'),
('Jane Roe',  '456 Oak Ave, Los Angeles, CA', 'jane@example.com', '555-5678', 'customer456'),
('Bob Smith', '789 Pine Rd, Chicago, IL',     'bob@example.com',  '555-9012', 'customer789');

INSERT INTO Airline VALUES ('AA', 'American Airlines'),
                           ('UA', 'United Airlines'),
                           ('DL', 'Delta Air Lines');

INSERT INTO Airport VALUES
('JFK', 'John F Kennedy Intl',      'New York',    'USA'),
('LGA', 'LaGuardia Airport',         'New York',    'USA'),
('EWR', 'Newark Liberty Intl',       'Newark',      'USA'),
('LAX', 'Los Angeles Intl',          'Los Angeles', 'USA'),
('ORD', 'O''Hare International',     'Chicago',     'USA'),
('MIA', 'Miami International',       'Miami',       'USA'),
('LHR', 'Heathrow Airport',          'London',      'UK'),
('CDG', 'Charles de Gaulle Airport', 'Paris',       'France');

INSERT INTO Aircraft (model, capacity, airline_id) VALUES
('Boeing 737',  190, 'AA'),
('Boeing 777',  330, 'AA'),
('Airbus A320', 172, 'UA'),
('Boeing 757',  228, 'DL');

INSERT INTO Flight (flight_num, airline_id, aircraft_id, dep_airport_id, arr_airport_id,
                    dep_time, arr_time, type, days_of_week,
                    economy_price, business_price, first_price)
VALUES
('100', 'AA', 1, 'JFK', 'LAX', '08:00:00', '11:30:00', 'domestic',      'MON,WED,FRI,SUN',              299,  699,  1299),
('101', 'AA', 2, 'LAX', 'JFK', '13:00:00', '21:30:00', 'domestic',      'MON,WED,FRI,SUN',              299,  699,  1299),
('200', 'UA', 3, 'EWR', 'ORD', '09:30:00', '11:15:00', 'domestic',      'MON,TUE,WED,THU,FRI',          199,  499,   899),
('201', 'UA', 3, 'ORD', 'LAX', '12:00:00', '14:30:00', 'domestic',      'MON,TUE,WED,THU,FRI',          249,  549,   999),
('300', 'DL', 4, 'JFK', 'LHR', '22:00:00', '10:00:00', 'international', 'MON,WED,FRI',                  599, 1499,  2999),
('102', 'AA', 1, 'JFK', 'MIA', '07:00:00', '10:30:00', 'domestic',      'MON,TUE,WED,THU,FRI,SAT,SUN',  149,  399,   799),
('301', 'DL', 4, 'LAX', 'JFK', '06:00:00', '14:30:00', 'domestic',      'TUE,THU,SAT',                  279,  679,  1279);

INSERT INTO Question (customer_id, subject, question_text, asked_datetime) VALUES
(1, 'Baggage policy',   'What is the baggage allowance for domestic flights?',        NOW()),
(2, 'Seat upgrade',     'How can I upgrade my seat to business class after booking?', NOW()),
(3, 'Cancellation fee', 'Is there a fee if I cancel my ticket 48 hours before?',     NOW());
