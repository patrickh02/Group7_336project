package com.flightreservation.service;

import com.flightreservation.db.DBConnection;
import com.flightreservation.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * All database operations needed by the Customer Representative UI.
 * Every method opens, uses, and closes its own Connection via try-with-resources.
 */
public class CustomerRepService {

    // ── Login ────────────────────────────────────────────────────────────────

    public Employee loginEmployee(String email, String password) throws SQLException {
        String sql = "SELECT employee_id, name, email, type FROM Employee WHERE email=? AND password=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Employee e = new Employee();
                    e.setEmployeeId(rs.getInt("employee_id"));
                    e.setName(rs.getString("name"));
                    e.setEmail(rs.getString("email"));
                    e.setType(rs.getString("type"));
                    return e;
                }
            }
        }
        return null;
    }

    public Customer loginCustomer(String email, String password) throws SQLException {
        String sql = "SELECT customer_id, name, email, phone, address FROM Customer WHERE email=? AND password=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Customer cu = new Customer();
                    cu.setCustomerId(rs.getInt("customer_id"));
                    cu.setName(rs.getString("name"));
                    cu.setEmail(rs.getString("email"));
                    cu.setPhone(rs.getString("phone"));
                    cu.setAddress(rs.getString("address"));
                    return cu;
                }
            }
        }
        return null;
    }

    // ── Airlines (for dropdowns) ─────────────────────────────────────────────

    public List<Airline> getAllAirlines() throws SQLException {
        List<Airline> list = new ArrayList<>();
        String sql = "SELECT airline_id, name FROM Airline ORDER BY airline_id";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Airline(rs.getString("airline_id"), rs.getString("name")));
            }
        }
        return list;
    }

    // ── Aircraft CRUD ────────────────────────────────────────────────────────

    public List<Aircraft> getAllAircrafts() throws SQLException {
        List<Aircraft> list = new ArrayList<>();
        String sql = "SELECT aircraft_id, model, capacity, airline_id FROM Aircraft ORDER BY aircraft_id";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Aircraft a = new Aircraft();
                a.setAircraftId(rs.getInt("aircraft_id"));
                a.setModel(rs.getString("model"));
                a.setCapacity(rs.getInt("capacity"));
                a.setAirlineId(rs.getString("airline_id"));
                list.add(a);
            }
        }
        return list;
    }

    public void addAircraft(Aircraft a) throws SQLException {
        String sql = "INSERT INTO Aircraft (model, capacity, airline_id) VALUES (?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getModel());
            ps.setInt(2, a.getCapacity());
            ps.setString(3, a.getAirlineId());
            ps.executeUpdate();
        }
    }

    public void updateAircraft(Aircraft a) throws SQLException {
        String sql = "UPDATE Aircraft SET model=?, capacity=?, airline_id=? WHERE aircraft_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getModel());
            ps.setInt(2, a.getCapacity());
            ps.setString(3, a.getAirlineId());
            ps.setInt(4, a.getAircraftId());
            ps.executeUpdate();
        }
    }

    public void deleteAircraft(int aircraftId) throws SQLException {
        String sql = "DELETE FROM Aircraft WHERE aircraft_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, aircraftId);
            ps.executeUpdate();
        }
    }

    // ── Airport CRUD ─────────────────────────────────────────────────────────

    public List<Airport> getAllAirports() throws SQLException {
        List<Airport> list = new ArrayList<>();
        String sql = "SELECT airport_id, name, city, country FROM Airport ORDER BY airport_id";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Airport a = new Airport();
                a.setAirportId(rs.getString("airport_id"));
                a.setName(rs.getString("name"));
                a.setCity(rs.getString("city"));
                a.setCountry(rs.getString("country"));
                list.add(a);
            }
        }
        return list;
    }

    public void addAirport(Airport a) throws SQLException {
        String sql = "INSERT INTO Airport (airport_id, name, city, country) VALUES (?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getAirportId().toUpperCase());
            ps.setString(2, a.getName());
            ps.setString(3, a.getCity());
            ps.setString(4, a.getCountry());
            ps.executeUpdate();
        }
    }

    public void updateAirport(Airport a) throws SQLException {
        String sql = "UPDATE Airport SET name=?, city=?, country=? WHERE airport_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getName());
            ps.setString(2, a.getCity());
            ps.setString(3, a.getCountry());
            ps.setString(4, a.getAirportId());
            ps.executeUpdate();
        }
    }

    public void deleteAirport(String airportId) throws SQLException {
        String sql = "DELETE FROM Airport WHERE airport_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, airportId);
            ps.executeUpdate();
        }
    }

    // ── Flight CRUD ──────────────────────────────────────────────────────────

    public List<Flight> getAllFlights() throws SQLException {
        List<Flight> list = new ArrayList<>();
        String sql =
            "SELECT f.flight_id, f.flight_num, f.airline_id, al.name AS airline_name, " +
            "f.aircraft_id, f.dep_airport_id, f.arr_airport_id, " +
            "da.city AS dep_city, aa.city AS arr_city, " +
            "f.dep_time, f.arr_time, f.type, f.days_of_week, f.stops, " +
            "f.economy_price, f.business_price, f.first_price " +
            "FROM Flight f " +
            "JOIN Airline  al ON f.airline_id     = al.airline_id " +
            "JOIN Airport  da ON f.dep_airport_id = da.airport_id " +
            "JOIN Airport  aa ON f.arr_airport_id = aa.airport_id " +
            "ORDER BY f.flight_id";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Flight f = new Flight();
                f.setFlightId(rs.getInt("flight_id"));
                f.setFlightNum(rs.getString("flight_num"));
                f.setAirlineId(rs.getString("airline_id"));
                f.setAirlineName(rs.getString("airline_name"));
                f.setAircraftId(rs.getInt("aircraft_id"));
                f.setDepAirportId(rs.getString("dep_airport_id"));
                f.setArrAirportId(rs.getString("arr_airport_id"));
                f.setDepCity(rs.getString("dep_city"));
                f.setArrCity(rs.getString("arr_city"));
                f.setDepTime(rs.getTime("dep_time"));
                f.setArrTime(rs.getTime("arr_time"));
                f.setType(rs.getString("type"));
                f.setDaysOfWeek(rs.getString("days_of_week"));
                f.setStops(rs.getInt("stops"));
                f.setEconomyPrice(rs.getDouble("economy_price"));
                f.setBusinessPrice(rs.getDouble("business_price"));
                f.setFirstPrice(rs.getDouble("first_price"));
                list.add(f);
            }
        }
        return list;
    }

    public void addFlight(Flight f) throws SQLException {
        String sql =
            "INSERT INTO Flight (flight_num, airline_id, aircraft_id, dep_airport_id, arr_airport_id, " +
            "dep_time, arr_time, type, days_of_week, stops, economy_price, business_price, first_price) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, f.getFlightNum());
            ps.setString(2, f.getAirlineId());
            ps.setInt(3, f.getAircraftId());
            ps.setString(4, f.getDepAirportId());
            ps.setString(5, f.getArrAirportId());
            ps.setTime(6, f.getDepTime());
            ps.setTime(7, f.getArrTime());
            ps.setString(8, f.getType());
            ps.setString(9, f.getDaysOfWeek());
            ps.setInt(10, f.getStops());
            ps.setDouble(11, f.getEconomyPrice());
            ps.setDouble(12, f.getBusinessPrice());
            ps.setDouble(13, f.getFirstPrice());
            ps.executeUpdate();
        }
    }

    public void updateFlight(Flight f) throws SQLException {
        String sql =
            "UPDATE Flight SET flight_num=?, airline_id=?, aircraft_id=?, dep_airport_id=?, " +
            "arr_airport_id=?, dep_time=?, arr_time=?, type=?, days_of_week=?, stops=?, " +
            "economy_price=?, business_price=?, first_price=? WHERE flight_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, f.getFlightNum());
            ps.setString(2, f.getAirlineId());
            ps.setInt(3, f.getAircraftId());
            ps.setString(4, f.getDepAirportId());
            ps.setString(5, f.getArrAirportId());
            ps.setTime(6, f.getDepTime());
            ps.setTime(7, f.getArrTime());
            ps.setString(8, f.getType());
            ps.setString(9, f.getDaysOfWeek());
            ps.setInt(10, f.getStops());
            ps.setDouble(11, f.getEconomyPrice());
            ps.setDouble(12, f.getBusinessPrice());
            ps.setDouble(13, f.getFirstPrice());
            ps.setInt(14, f.getFlightId());
            ps.executeUpdate();
        }
    }

    public void deleteFlight(int flightId) throws SQLException {
        String sql = "DELETE FROM Flight WHERE flight_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, flightId);
            ps.executeUpdate();
        }
    }

    // ── Customers ────────────────────────────────────────────────────────────

    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT customer_id, name, email, phone, address FROM Customer ORDER BY name";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Customer cu = new Customer();
                cu.setCustomerId(rs.getInt("customer_id"));
                cu.setName(rs.getString("name"));
                cu.setEmail(rs.getString("email"));
                cu.setPhone(rs.getString("phone"));
                cu.setAddress(rs.getString("address"));
                list.add(cu);
            }
        }
        return list;
    }

    public List<Customer> searchCustomers(String term) throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT customer_id, name, email, phone, address FROM Customer " +
                     "WHERE name LIKE ? OR email LIKE ? ORDER BY name";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String like = "%" + term + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Customer cu = new Customer();
                    cu.setCustomerId(rs.getInt("customer_id"));
                    cu.setName(rs.getString("name"));
                    cu.setEmail(rs.getString("email"));
                    cu.setPhone(rs.getString("phone"));
                    cu.setAddress(rs.getString("address"));
                    list.add(cu);
                }
            }
        }
        return list;
    }

    // ── Reservations ─────────────────────────────────────────────────────────

    public int getBookedSeats(int flightId, java.sql.Date depDate) throws SQLException {
        String sql =
            "SELECT COUNT(*) FROM TicketFlight tf " +
            "JOIN Ticket t ON tf.ticket_num = t.ticket_num " +
            "WHERE tf.flight_id = ? AND tf.dep_date = ? AND t.status = 'active'";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, flightId);
            ps.setDate(2, depDate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getFlightCapacity(int flightId) throws SQLException {
        String sql =
            "SELECT ac.capacity FROM Flight f " +
            "JOIN Aircraft ac ON f.aircraft_id = ac.aircraft_id WHERE f.flight_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, flightId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public boolean isFlightFull(int flightId, java.sql.Date depDate) throws SQLException {
        return getBookedSeats(flightId, depDate) >= getFlightCapacity(flightId);
    }

    /**
     * Books a one-way ticket for a customer (rep-assisted).
     * Returns the new ticket_num on success.
     */
    public int createReservation(int customerId, int flightId, java.sql.Date depDate,
                                  String flightClass, String seatNum, String mealPref)
            throws SQLException {
        String priceSql = "business".equalsIgnoreCase(flightClass)
                ? "SELECT business_price FROM Flight WHERE flight_id=?"
                : "first".equalsIgnoreCase(flightClass)
                        ? "SELECT first_price FROM Flight WHERE flight_id=?"
                        : "SELECT economy_price FROM Flight WHERE flight_id=?";
        double fare = 0;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(priceSql)) {
            ps.setInt(1, flightId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) fare = rs.getDouble(1);
            }
        }
        final double total = fare + 25.00;

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int ticketNum;
                String ins1 = "INSERT INTO Ticket (customer_id, total_fare, type, flexible, status) " +
                              "VALUES (?, ?, 'oneWay', FALSE, 'active')";
                try (PreparedStatement ps = c.prepareStatement(ins1, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, customerId);
                    ps.setDouble(2, total);
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) {
                        k.next();
                        ticketNum = k.getInt(1);
                    }
                }
                String ins2 =
                    "INSERT INTO TicketFlight (ticket_num, flight_id, sequence_order, dep_date, seat_num, meal_pref, class) " +
                    "VALUES (?, ?, 1, ?, ?, ?, ?)";
                try (PreparedStatement ps = c.prepareStatement(ins2)) {
                    ps.setInt(1, ticketNum);
                    ps.setInt(2, flightId);
                    ps.setDate(3, depDate);
                    ps.setString(4, (seatNum == null || seatNum.isEmpty()) ? null : seatNum);
                    ps.setString(5, (mealPref == null || mealPref.isEmpty()) ? "standard" : mealPref);
                    ps.setString(6, flightClass);
                    ps.executeUpdate();
                }
                c.commit();
                return ticketNum;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    public void addToWaitlist(int customerId, int flightId, java.sql.Date depDate,
                               String flightClass) throws SQLException {
        String posSql =
            "SELECT COALESCE(MAX(position), 0) + 1 FROM Waitlist WHERE flight_id=? AND dep_date=?";
        int pos = 1;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(posSql)) {
            ps.setInt(1, flightId);
            ps.setDate(2, depDate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) pos = rs.getInt(1);
            }
        }
        String sql =
            "INSERT IGNORE INTO Waitlist (customer_id, flight_id, position, dep_date, class) " +
            "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, flightId);
            ps.setInt(3, pos);
            ps.setDate(4, depDate);
            ps.setString(5, flightClass);
            ps.executeUpdate();
        }
    }

    // ── Edit Reservation ─────────────────────────────────────────────────────

    /**
     * Returns rows: ticket_num, customerName, email, flightNum, airlineName,
     *               depDate, class, seatNum, mealPref, flightId
     */
    public List<Object[]> searchReservations(String term) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT t.ticket_num, c.name AS cname, c.email, " +
            "f.flight_num, al.name AS aname, " +
            "tf.dep_date, tf.class, tf.seat_num, tf.meal_pref, tf.flight_id " +
            "FROM Ticket t " +
            "JOIN Customer    c  ON t.customer_id  = c.customer_id " +
            "JOIN TicketFlight tf ON t.ticket_num   = tf.ticket_num " +
            "JOIN Flight       f  ON tf.flight_id   = f.flight_id " +
            "JOIN Airline      al ON f.airline_id   = al.airline_id " +
            "WHERE (c.name LIKE ? OR c.email LIKE ? OR CAST(t.ticket_num AS CHAR) = ?) " +
            "AND t.status = 'active' " +
            "ORDER BY t.ticket_num, tf.sequence_order";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String like = "%" + term + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, term);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("ticket_num"),
                        rs.getString("cname"),
                        rs.getString("email"),
                        rs.getString("flight_num"),
                        rs.getString("aname"),
                        rs.getDate("dep_date"),
                        rs.getString("class"),
                        rs.getString("seat_num"),
                        rs.getString("meal_pref"),
                        rs.getInt("flight_id")
                    });
                }
            }
        }
        return list;
    }

    public void updateTicketFlight(int ticketNum, int flightId, java.sql.Date depDate,
                                    String flightClass, String seatNum, String mealPref)
            throws SQLException {
        String priceSql = "business".equalsIgnoreCase(flightClass)
                ? "SELECT business_price FROM Flight WHERE flight_id=?"
                : "first".equalsIgnoreCase(flightClass)
                        ? "SELECT first_price FROM Flight WHERE flight_id=?"
                        : "SELECT economy_price FROM Flight WHERE flight_id=?";
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                double fare = 0;
                try (PreparedStatement ps = c.prepareStatement(priceSql)) {
                    ps.setInt(1, flightId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) fare = rs.getDouble(1);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE TicketFlight SET dep_date=?, class=?, seat_num=?, meal_pref=? " +
                        "WHERE ticket_num=? AND flight_id=?")) {
                    ps.setDate(1, depDate);
                    ps.setString(2, flightClass);
                    ps.setString(3, (seatNum == null || seatNum.isEmpty()) ? null : seatNum);
                    ps.setString(4, (mealPref == null || mealPref.isEmpty()) ? "standard" : mealPref);
                    ps.setInt(5, ticketNum);
                    ps.setInt(6, flightId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE Ticket SET total_fare=? WHERE ticket_num=?")) {
                    ps.setDouble(1, fare + 25.00);
                    ps.setInt(2, ticketNum);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    // ── Waitlist ─────────────────────────────────────────────────────────────

    /**
     * Returns rows: position, customerId, name, email, phone, depDate, class, requestTime
     */
    public List<Object[]> getWaitlistForFlight(int flightId) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT w.position, c.customer_id, c.name, c.email, c.phone, " +
            "w.dep_date, w.class, w.request_time " +
            "FROM Waitlist w " +
            "JOIN Customer c ON w.customer_id = c.customer_id " +
            "WHERE w.flight_id = ? " +
            "ORDER BY w.dep_date, w.position";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, flightId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("position"),
                        rs.getInt("customer_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getDate("dep_date"),
                        rs.getString("class"),
                        rs.getTimestamp("request_time")
                    });
                }
            }
        }
        return list;
    }

    // ── Flights by Airport ───────────────────────────────────────────────────

    /**
     * Returns rows: flightId, flightNum, airlineName, otherCity, time, type, days, economyPrice
     */
    public List<Object[]> getDepartingFlights(String airportId) throws SQLException {
        return flightsByAirport(airportId, true);
    }

    public List<Object[]> getArrivingFlights(String airportId) throws SQLException {
        return flightsByAirport(airportId, false);
    }

    private List<Object[]> flightsByAirport(String airportId, boolean dep) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String filterCol = dep ? "f.dep_airport_id" : "f.arr_airport_id";
        String otherJoin  = dep ? "f.arr_airport_id" : "f.dep_airport_id";
        String timeCol    = dep ? "f.dep_time" : "f.arr_time";
        String sql =
            "SELECT f.flight_id, f.flight_num, al.name AS aname, " +
            "oa.city AS other_city, " + timeCol + " AS t, " +
            "f.type, f.days_of_week, f.economy_price " +
            "FROM Flight f " +
            "JOIN Airline al ON f.airline_id  = al.airline_id " +
            "JOIN Airport oa ON " + otherJoin + " = oa.airport_id " +
            "WHERE " + filterCol + " = ? " +
            "ORDER BY t";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, airportId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("flight_id"),
                        rs.getString("flight_num"),
                        rs.getString("aname"),
                        rs.getString("other_city"),
                        rs.getTime("t"),
                        rs.getString("type"),
                        rs.getString("days_of_week"),
                        rs.getDouble("economy_price")
                    });
                }
            }
        }
        return list;
    }

    // ── Questions ────────────────────────────────────────────────────────────

    /**
     * Returns rows: questionId, customerName, subject, questionText,
     *               answerText, askedDatetime, answeredDatetime
     * Unanswered questions first.
     */
    public List<Object[]> getAllQuestions() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT q.question_id, c.name AS cname, q.subject, " +
            "q.question_text, q.answer_text, q.asked_datetime, q.answered_datetime " +
            "FROM Question q " +
            "JOIN Customer c ON q.customer_id = c.customer_id " +
            "ORDER BY (q.answered_datetime IS NOT NULL), q.asked_datetime DESC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("question_id"),
                    rs.getString("cname"),
                    rs.getString("subject"),
                    rs.getString("question_text"),
                    rs.getString("answer_text"),
                    rs.getTimestamp("asked_datetime"),
                    rs.getTimestamp("answered_datetime")
                });
            }
        }
        return list;
    }

    public void answerQuestion(int questionId, int repId, String answerText) throws SQLException {
        String sql =
            "UPDATE Question SET rep_id=?, answer_text=?, answered_datetime=NOW() " +
            "WHERE question_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, repId);
            ps.setString(2, answerText);
            ps.setInt(3, questionId);
            ps.executeUpdate();
        }
    }
}
