# Flight Reservation System — Group 7

A standalone Java Swing desktop application for an online travel reservation system.  
Roles: Admin, Customer Representative, Customer.

---

## Requirements

| Tool | Version |
|------|---------|
| Java (JDK) | 11 or later |
| MySQL Server | 8.x |
| MySQL Connector/J | 8.x (JAR file) |

---

## 1. MySQL Setup

1. Start your MySQL server.
2. Open a MySQL client (Workbench, DBeaver, or `mysql` CLI).
3. Run the schema file:

```sql
SOURCE /path/to/Group7_336project/sql/schema.sql;
```

Or via CLI:

```bash
mysql -u root -p < sql/schema.sql
```

This creates the `flight_reservation` database, all tables, and loads sample data.

---

## 2. Configure Database Credentials

Open:

```
src/com/flightreservation/db/DBConnection.java
```

Change these four lines to match your MySQL setup:

```java
private static final String HOST = "localhost";         // MySQL host
private static final String PORT = "3306";              // MySQL port
private static final String DB   = "flight_reservation";// database name
private static final String USER = "root";              // MySQL username
private static final String PASS = "password";          // MySQL password
```

---

## 3. Add MySQL Connector/J

Download `mysql-connector-j-<version>.jar` from https://dev.mysql.com/downloads/connector/j/

Place it in a `lib/` folder at the project root:

```
Group7_336project/
├── lib/
│   └── mysql-connector-j-8.x.xx.jar
├── src/
└── sql/
```

---

## 4. Compile

From the project root:

```bash
mkdir -p out
javac -cp "lib/*" -d out $(find src -name "*.java")
```

On Windows (PowerShell):

```powershell
mkdir out
javac -cp "lib\*" -d out (Get-ChildItem -Recurse src -Filter "*.java" | % { $_.FullName })
```

---

## 5. Run

```bash
java -cp "out:lib/*" com.flightreservation.Main
```

On Windows:

```cmd
java -cp "out;lib\*" com.flightreservation.Main
```

---

## 6. Sample Login Credentials

| Role | Email | Password |
|------|-------|----------|
| Customer Rep | rep1@airline.com | rep123 |
| Admin | admin@airline.com | admin123 |
| Customer | john@example.com | customer123 |
| Customer | jane@example.com | customer456 |
| Customer | bob@example.com | customer789 |

---

## 7. Customer Representative — Demo Steps

After logging in as `rep1@airline.com / rep123`, the Customer Rep Dashboard opens with the following tabs:

### Manage Flights
- Click **Refresh** to see all flights.
- Click **Add** to create a new flight (fill in flight number, airline, aircraft, airports, times, prices).
- Select a row and click **Edit Selected** to modify it.
- Select a row and click **Delete Selected** to remove it.

### Manage Aircrafts
- Click **Refresh** to see all aircraft.
- Click **Add** to add a new aircraft (model, capacity, airline).
- Select a row and click **Edit Selected** or **Delete Selected**.

### Manage Airports
- Click **Refresh** to see all airports.
- Click **Add** to add a new airport (3-letter code, name, city, country).
- Airport code (primary key) cannot be edited after creation.

### Make Reservation
- Load customers and flights with **Reload** buttons.
- Choose a customer, flight, departure date (YYYY-MM-DD), class, seat (optional), and meal.
- Click **Book Reservation**.
- If the flight is full, you will be offered the option to add the customer to the waitlist.

### Edit Reservation
- Type a customer name, email, or ticket number in the search box and click **Search**.
- Select a row in the results table.
- Edit departure date, class, seat, or meal in the form below.
- Click **Save Changes**.

### Flight Waitlist
- Click **Reload Flights** to populate the dropdown.
- Select a flight and click **Show Waitlist**.
- The table shows all customers on the waitlist for that flight.

### Flights by Airport
- Click **Reload Airports** to populate the dropdown.
- Select an airport and click **Show Flights**.
- Two tables show departing and arriving flights for that airport.

### Answer Questions
- The table loads all customer questions (unanswered first).
- Select a question to see its text.
- Type an answer in the text area and click **Save Answer**.
- Already-answered questions can be updated.

---

## 8. Package as project.jar

```bash
# From project root, after compiling to out/
jar cfe project.jar com.flightreservation.Main -C out . 
```

Run the JAR (Connector/J must still be on the classpath):

```bash
java -cp "project.jar:lib/*" com.flightreservation.Main
```

On Windows:

```cmd
java -cp "project.jar;lib\*" com.flightreservation.Main
```

---

## 9. Project Structure

```
Group7_336project/
├── lib/                          ← place mysql-connector-j jar here
├── out/                          ← compiled classes (created by javac)
├── sql/
│   └── schema.sql                ← database schema + seed data
├── src/com/flightreservation/
│   ├── Main.java                 ← entry point
│   ├── db/
│   │   └── DBConnection.java     ← JDBC connection (edit credentials here)
│   ├── model/                    ← POJOs: Airline, Aircraft, Airport, Flight,
│   │                                Customer, Employee, Ticket, TicketFlight,
│   │                                Waitlist, Question
│   ├── service/
│   │   └── CustomerRepService.java ← all DB queries for customer rep features
│   └── ui/
│       ├── LoginFrame.java
│       ├── CustomerRepDashboard.java
│       ├── ManageFlightsPanel.java
│       ├── ManageAircraftsPanel.java
│       ├── ManageAirportsPanel.java
│       ├── CustomerRepReservationPanel.java
│       ├── EditReservationPanel.java
│       ├── WaitlistPanel.java
│       ├── FlightsByAirportPanel.java
│       └── QuestionsPanel.java
└── README.md
```

---

## 10. Known Limitations

- Passwords are stored as plain text in the database (acceptable for a class project demo).
- Customer UI (flight search, booking, cancellation) is handled by **Person 2** — placeholders are shown on login.
- Admin UI (reports, user management) is handled by **Person 1** — a placeholder message is shown on admin login.
- The JAR does not bundle the MySQL Connector/J due to licensing; it must always be on the classpath.
