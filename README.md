# Flight Reservation System — Group 7

A standalone Java Swing desktop application for an online travel reservation system backed by MySQL. Supports three user roles: **Customer**, **Customer Representative**, and **Admin**.

---

## Requirements

| Tool | Version |
|------|---------|
| Java (JDK) | 11 or later |
| MySQL Server | 8.x |
| MySQL Connector/J | included in `lib/` |

---

## Setup

### 1. Start MySQL and run the schema

Open MySQL Workbench, go to **File → Open SQL Script**, select `sql/schema.sql`, and click the ⚡ button to run it.

Or via terminal:
```bash
mysql -u root -p < sql/schema.sql
```

This creates the `flight_reservation` database with all tables and sample data.

### 2. Configure database credentials

Open `src/com/flightreservation/db/DBConnection.java` and update these lines to match your MySQL setup:

```java
private static final String DB   = "flight_reservation";
private static final String USER = "root";
private static final String PASS = "password";
```

### 3. Compile

```bash
mkdir -p out
javac -cp "lib/*" -d out $(find src -name "*.java")
```

### 4. Run

```bash
java -cp "out:lib/*" com.flightreservation.Main
```

On Windows:
```cmd
java -cp "out;lib\*" com.flightreservation.Main
```

---

## Login Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@airline.com | admin123 |
| Customer Rep | rep1@airline.com | rep123 |
| Customer | jane@example.com | customer456 |
| Customer | bob@example.com | customer789 |

---

## Available Airports

| Code | Airport | City | Country |
|------|---------|------|---------|
| JFK | John F Kennedy Intl | New York | USA |
| LGA | LaGuardia Airport | New York | USA |
| EWR | Newark Liberty Intl | Newark | USA |
| LAX | Los Angeles Intl | Los Angeles | USA |
| ORD | O'Hare International | Chicago | USA |
| MIA | Miami International | Miami | USA |
| LHR | Heathrow Airport | London | UK |
| CDG | Charles de Gaulle Airport | Paris | France |

## Available Airlines

| Code | Airline |
|------|---------|
| AA | American Airlines |
| UA | United Airlines |
| DL | Delta Air Lines |

## Sample Flight Routes

| Flight | Route | Days | Economy |
|--------|-------|------|---------|
| AA100 | JFK → LAX | Mon, Wed, Fri, Sun | $299 |
| AA101 | LAX → JFK | Mon, Wed, Fri, Sun | $299 |
| UA200 | EWR → ORD | Mon–Fri | $199 |
| UA201 | ORD → LAX | Mon–Fri | $249 |
| DL300 | JFK → LHR | Mon, Wed, Fri | $599 |
| AA102 | JFK → MIA | Daily | $149 |
| DL301 | LAX → JFK | Tue, Thu, Sat | $279 |

---

## Customer Guide

Log in with `jane@example.com / customer456` or `bob@example.com / customer789`.

### Search Flights
- Go to the **Search Flights** tab
- Enter a **From** airport code (e.g. `JFK`) and **To** airport code (e.g. `LAX`)
- Enter a date in `YYYY-MM-DD` format (e.g. `2026-06-15`)
- Click **Search** — matching flights appear in the table below

### One-Way vs Round-Trip
- Set the **Trip** dropdown to **One-Way** or **Round-Trip**
- For Round-Trip, a **Return Date** field appears — fill it in
- Click **Search**

### Flexible Dates
- Check the **Flexible ±3 days** checkbox before clicking Search
- Results will include flights within 3 days of your chosen date

### Sort Flights
Use the **Sort by** dropdown to sort results by:
- Departure Time
- Arrival Time
- Economy Price (low to high or high to low)
- Duration (shortest or longest)

### Filter Flights
- **Airline** — filter by a specific airline
- **Max Price ($)** — only show flights at or below this economy price
- **Max Stops** — e.g. enter `0` for nonstop only
- **Depart After (HH:MM)** — e.g. `09:00`
- **Arrive Before (HH:MM)** — e.g. `18:00`
- Click **Apply Filters** after setting any filter

### Book a Flight
1. Select a flight row in the table
2. Click **Book Selected Flight**
3. Choose your **Class** (economy / business / first)
4. Optionally enter a **Seat** number (e.g. `12A`)
5. Choose a **Meal** preference
6. Click **Confirm Booking**

If the flight is full, you will be offered the option to join the waitlist.

### Cancel a Reservation
- Go to **My Reservations → Upcoming Trips**
- Select a ticket and click **Cancel Selected Ticket**
- Economy class: a **$50 cancellation fee** applies
- Business/First class: cancelled with **no fee**
- When a seat opens up, the first customer on the waitlist is automatically notified

### View Past & Upcoming Trips
- Go to **My Reservations**
- **Upcoming Trips** tab — active future bookings
- **Past Trips** tab — completed or cancelled reservations
- Details shown: flight, route, date, class, seat, meal, fare, status, purchase date

### Ask a Question
- Go to **Ask a Question** tab
- Enter a subject and your question
- Click **Submit** — a customer representative will answer it

### Browse Q&A
- Go to **Browse Q&A** tab
- Click **Show All** to see all answered questions
- Type a keyword (e.g. `baggage`) and click **Search** to filter results

---

## Admin Guide

Log in with `admin@airline.com / admin123`.

### Manage Users
- **Customers** sub-tab — view, add, edit, or delete customer accounts
- **Employees** sub-tab — view, add, edit, or delete rep/admin accounts
- Select a row then click **Edit Selected** or **Delete Selected**

### Sales Report
- Choose a **Month** and **Year**
- Click **Run Report** to see all tickets for that period with fare, booking fee, and status
- Summary line shows total tickets and total revenue

### Reservations
- Search **By Flight Number** (e.g. `100`) or **By Customer Name** (e.g. `Jane`)
- Click **Search** to see matching reservations

### Revenue Reports
Five sub-tabs, each with a **Refresh** button:
- **By Flight** — revenue and ticket count per flight
- **By Airline** — revenue and ticket count per airline
- **By Customer** — total spent per customer
- **Top Customer** — the single highest-spending customer
- **Most Active Flights** — flights ranked by tickets sold

### Flights by Airport
- Select an airport from the dropdown
- Click **Show Flights** to see all departing and arriving flights for that airport

---

## Customer Representative Guide

Log in with `rep1@airline.com / rep123`.

### Make Reservation (on behalf of a customer)
- Go to **Make Reservation** tab
- Click **Reload** to load customers and flights
- Select a customer, flight, date, class, seat, and meal
- Click **Book Reservation**

### Edit Reservation
- Go to **Edit Reservation** tab
- Search by customer name, email, or ticket number
- Select a result row, update the fields, and click **Save Changes**

### Manage Flights
- View all flights, add new ones, or edit/delete existing flights
- Fields include: flight number, airline, aircraft, airports, times, days, stops, prices

### Manage Aircrafts
- Add, edit, or delete aircraft records (model, capacity, airline)

### Manage Airports
- Add new airports using a 3-letter IATA code (e.g. `SFO`)
- Existing airports can be viewed but the airport code cannot be changed after creation

### Flight Waitlist
- Select a flight from the dropdown and click **Show Waitlist**
- See all customers waiting for a seat on that flight

### Flights by Airport
- Select an airport and click **Show Flights**
- Separate tables show departing and arriving flights

### Answer Questions
- All customer questions load automatically (unanswered shown first)
- Select a question, type an answer, and click **Save Answer**

---

## Notes

- Passwords are stored as plain text (acceptable for a class project demo)
- The MySQL Connector/J JAR must always be on the classpath — it is not bundled into the application
