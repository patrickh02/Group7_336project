package com.flightreservation.service;

import com.flightreservation.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * All database operations needed by the Customer UI.
 */
public class CustomerService {

    // ── Flight Search ────────────────────────────────────────────────────────

    /**
     * Search for direct flights matching departure airport, arrival airport, and date.
     * If flexible=true, also matches flights within ±3 days of the given date.
     *
     * Returns rows: flightId, flightNum, airlineId, airlineName, depAirportId,
     *               arrAirportId, depTime, arrTime, type, economyPrice, businessPrice,
     *               firstPrice, capacity, bookedSeats
     */
    public List<Object[]> searchFlights(String depAirport, String arrAirport,
                                        java.sql.Date date, boolean flexible)
            throws SQLException {
        List<Object[]> list = new ArrayList<>();

        String dayOfWeek = getDayAbbrev(date);

        // For flexible searches, all flights on the route are returned (user confirms
        // the exact date at booking time). For specific dates, filter by days_of_week.
        String sql =
            "SELECT f.flight_id, f.flight_num, f.airline_id, al.name AS airline_name, " +
            "f.dep_airport_id, f.arr_airport_id, " +
            "da.city AS dep_city, aa.city AS arr_city, " +
            "f.dep_time, f.arr_time, f.type, f.days_of_week, " +
            "f.economy_price, f.business_price, f.first_price, " +
            "ac.capacity, f.stops, " +
            "COALESCE((SELECT COUNT(*) FROM TicketFlight tf2 " +
            "          JOIN Ticket t2 ON tf2.ticket_num = t2.ticket_num " +
            "          WHERE tf2.flight_id = f.flight_id AND tf2.dep_date = ? AND t2.status = 'active'), 0) AS booked " +
            "FROM Flight f " +
            "JOIN Airline  al ON f.airline_id     = al.airline_id " +
            "JOIN Airport  da ON f.dep_airport_id = da.airport_id " +
            "JOIN Airport  aa ON f.arr_airport_id = aa.airport_id " +
            "JOIN Aircraft ac ON f.aircraft_id    = ac.aircraft_id " +
            "WHERE UPPER(f.dep_airport_id) = UPPER(?) " +
            "AND   UPPER(f.arr_airport_id) = UPPER(?) " +
            (flexible ? "" : "AND f.days_of_week LIKE ? ") +
            "ORDER BY f.dep_time";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int idx = 1;
            ps.setDate(idx++, date);          // for booked subquery
            ps.setString(idx++, depAirport);
            ps.setString(idx++, arrAirport);
            if (!flexible) {
                ps.setString(idx++, "%" + dayOfWeek + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("flight_id"),       // [0]
                        rs.getString("flight_num"),    // [1]
                        rs.getString("airline_id"),    // [2]
                        rs.getString("airline_name"),  // [3]
                        rs.getString("dep_airport_id"),// [4]
                        rs.getString("arr_airport_id"),// [5]
                        rs.getString("dep_city"),      // [6]
                        rs.getString("arr_city"),      // [7]
                        rs.getTime("dep_time"),        // [8]
                        rs.getTime("arr_time"),        // [9]
                        rs.getString("type"),          // [10]
                        rs.getString("days_of_week"),  // [11]
                        rs.getDouble("economy_price"), // [12]
                        rs.getDouble("business_price"),// [13]
                        rs.getDouble("first_price"),   // [14]
                        rs.getInt("capacity"),         // [15]
                        rs.getInt("booked"),           // [16]
                        rs.getInt("stops")             // [17]
                    });
                }
            }
        }
        return list;
    }

    private String getDayAbbrev(java.sql.Date date) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        String[] days = {"SUN","MON","TUE","WED","THU","FRI","SAT"};
        return days[cal.get(java.util.Calendar.DAY_OF_WEEK) - 1];
    }

    // ── Booking ──────────────────────────────────────────────────────────────

    /**
     * Returns number of booked seats for a flight on a specific date.
     */
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

    /**
     * Returns capacity of the aircraft assigned to the given flight.
     */
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

    /**
     * Books a one-way ticket for the customer.
     * Returns the new ticket_num on success.
     */
    public int bookTicket(int customerId, int flightId, java.sql.Date depDate,
                          String flightClass, String seatNum, String mealPref,
                          boolean flexible, double fare)
            throws SQLException {
        final double total = fare + 25.00; // fare + booking fee

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int ticketNum;
                String ins1 =
                    "INSERT INTO Ticket (customer_id, total_fare, booking_fee, type, flexible, status) " +
                    "VALUES (?, ?, 25.00, 'oneWay', ?, 'active')";
                try (PreparedStatement ps = c.prepareStatement(ins1, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, customerId);
                    ps.setDouble(2, total);
                    ps.setBoolean(3, flexible);
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

    /**
     * Adds a customer to the waitlist for a given flight/date.
     */
    public void addToWaitlist(int customerId, int flightId, java.sql.Date depDate,
                              String flightClass) throws SQLException {
        String posSql =
            "SELECT COALESCE(MAX(position), 0) + 1 FROM Waitlist WHERE flight_id = ? AND dep_date = ?";
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

    // ── Reservations ─────────────────────────────────────────────────────────

    /**
     * Returns upcoming reservations (dep_date >= today) for the customer.
     * Rows: ticketNum, flightNum, airlineId, airlineName, depAirportId, arrAirportId,
     *       depDate, class, seatNum, mealPref, totalFare, status, ticketType, flightId, purchaseDatetime
     */
    public List<Object[]> getUpcomingReservations(int customerId) throws SQLException {
        return getReservations(customerId, true);
    }

    /**
     * Returns past reservations (dep_date < today) for the customer.
     */
    public List<Object[]> getPastReservations(int customerId) throws SQLException {
        return getReservations(customerId, false);
    }

    private List<Object[]> getReservations(int customerId, boolean upcoming) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String dateFilter = upcoming
            ? "tf.dep_date >= CURDATE() AND t.status = 'active'"
            : "tf.dep_date < CURDATE()";
        String sql =
            "SELECT t.ticket_num, f.flight_num, f.airline_id, al.name AS aname, " +
            "f.dep_airport_id, f.arr_airport_id, " +
            "da.city AS dep_city, aa.city AS arr_city, " +
            "tf.dep_date, tf.class, tf.seat_num, tf.meal_pref, " +
            "t.total_fare, t.status, t.type AS ticket_type, f.flight_id, t.purchase_datetime " +
            "FROM Ticket t " +
            "JOIN TicketFlight tf ON t.ticket_num   = tf.ticket_num " +
            "JOIN Flight        f  ON tf.flight_id   = f.flight_id " +
            "JOIN Airline       al ON f.airline_id   = al.airline_id " +
            "JOIN Airport       da ON f.dep_airport_id = da.airport_id " +
            "JOIN Airport       aa ON f.arr_airport_id = aa.airport_id " +
            "WHERE t.customer_id = ? AND " + dateFilter + " " +
            "ORDER BY tf.dep_date " + (upcoming ? "ASC" : "DESC") + ", t.ticket_num";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("ticket_num"),
                        rs.getString("flight_num"),
                        rs.getString("airline_id"),
                        rs.getString("aname"),
                        rs.getString("dep_airport_id"),
                        rs.getString("arr_airport_id"),
                        rs.getString("dep_city"),
                        rs.getString("arr_city"),
                        rs.getDate("dep_date"),
                        rs.getString("class"),
                        rs.getString("seat_num"),
                        rs.getString("meal_pref"),
                        rs.getDouble("total_fare"),
                        rs.getString("status"),
                        rs.getString("ticket_type"),
                        rs.getInt("flight_id"),
                        rs.getTimestamp("purchase_datetime")
                    });
                }
            }
        }
        return list;
    }

    public void cancelTicket(int ticketNum) throws SQLException {
        String sql = "UPDATE Ticket SET status = 'cancelled' WHERE ticket_num = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ticketNum);
            ps.executeUpdate();
        }
    }

    // ── Round-Trip Booking ───────────────────────────────────────────────────

    public int bookRoundTrip(int customerId,
                             int outFlightId, java.sql.Date outDate,
                             int retFlightId, java.sql.Date retDate,
                             String cls, String seat, String meal,
                             boolean flexible, double outFare, double retFare)
            throws SQLException {
        final double total = outFare + retFare + 25.00;
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int ticketNum;
                String ins1 =
                    "INSERT INTO Ticket (customer_id, total_fare, booking_fee, type, flexible, status) " +
                    "VALUES (?, ?, 25.00, 'roundTrip', ?, 'active')";
                try (PreparedStatement ps = c.prepareStatement(ins1, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, customerId);
                    ps.setDouble(2, total);
                    ps.setBoolean(3, flexible);
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) {
                        k.next();
                        ticketNum = k.getInt(1);
                    }
                }
                String ins2 =
                    "INSERT INTO TicketFlight (ticket_num, flight_id, sequence_order, dep_date, seat_num, meal_pref, class) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = c.prepareStatement(ins2)) {
                    ps.setInt(1, ticketNum);
                    ps.setInt(2, outFlightId);
                    ps.setInt(3, 1);
                    ps.setDate(4, outDate);
                    ps.setString(5, (seat == null || seat.isEmpty()) ? null : seat);
                    ps.setString(6, (meal == null || meal.isEmpty()) ? "standard" : meal);
                    ps.setString(7, cls);
                    ps.executeUpdate();

                    ps.setInt(1, ticketNum);
                    ps.setInt(2, retFlightId);
                    ps.setInt(3, 2);
                    ps.setDate(4, retDate);
                    ps.setString(5, null);
                    ps.setString(6, "standard");
                    ps.setString(7, cls);
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

    // ── Waitlist Notification Helpers ────────────────────────────────────────

    public Object[] getTicketFlightInfo(int ticketNum) throws SQLException {
        String sql =
            "SELECT tf.flight_id, tf.dep_date FROM TicketFlight tf " +
            "WHERE tf.ticket_num = ? AND tf.sequence_order = 1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ticketNum);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return new Object[]{rs.getInt("flight_id"), rs.getDate("dep_date")};
            }
        }
        return null;
    }

    public Object[] getFirstWaitlisted(int flightId, java.sql.Date depDate) throws SQLException {
        String sql =
            "SELECT w.customer_id, c.name, c.email, w.class " +
            "FROM Waitlist w JOIN Customer c ON w.customer_id = c.customer_id " +
            "WHERE w.flight_id = ? AND w.dep_date = ? ORDER BY w.position LIMIT 1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, flightId);
            ps.setDate(2, depDate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return new Object[]{
                        rs.getInt("customer_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("class")
                    };
            }
        }
        return null;
    }

    public void removeFromWaitlist(int customerId, int flightId, java.sql.Date depDate)
            throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int pos = 0;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT position FROM Waitlist WHERE customer_id=? AND flight_id=? AND dep_date=?")) {
                    ps.setInt(1, customerId); ps.setInt(2, flightId); ps.setDate(3, depDate);
                    try (ResultSet rs = ps.executeQuery()) { if (rs.next()) pos = rs.getInt(1); }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "DELETE FROM Waitlist WHERE customer_id=? AND flight_id=? AND dep_date=?")) {
                    ps.setInt(1, customerId); ps.setInt(2, flightId); ps.setDate(3, depDate);
                    ps.executeUpdate();
                }
                if (pos > 0) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE Waitlist SET position = position - 1 WHERE flight_id=? AND dep_date=? AND position > ?")) {
                        ps.setInt(1, flightId); ps.setDate(2, depDate); ps.setInt(3, pos);
                        ps.executeUpdate();
                    }
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    // ── Ask a Question ───────────────────────────────────────────────────────

    public void submitQuestion(int customerId, String subject, String questionText)
            throws SQLException {
        String sql =
            "INSERT INTO Question (customer_id, subject, question_text, asked_datetime) " +
            "VALUES (?, ?, ?, NOW())";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setString(2, subject);
            ps.setString(3, questionText);
            ps.executeUpdate();
        }
    }

    public List<Object[]> getMyQuestions(int customerId) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT question_id, subject, question_text, answer_text, " +
            "asked_datetime, answered_datetime " +
            "FROM Question WHERE customer_id = ? ORDER BY asked_datetime DESC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("question_id"),
                        rs.getString("subject"),
                        rs.getString("question_text"),
                        rs.getString("answer_text"),
                        rs.getTimestamp("asked_datetime"),
                        rs.getTimestamp("answered_datetime")
                    });
                }
            }
        }
        return list;
    }

    // rows: questionId, subject, questionText, answerText, askedDatetime, answeredDatetime
    public List<Object[]> getAnsweredQuestions(String keyword) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String kw = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        String sql =
            "SELECT question_id, subject, question_text, answer_text, " +
            "asked_datetime, answered_datetime " +
            "FROM Question WHERE answer_text IS NOT NULL " +
            "AND (subject LIKE ? OR question_text LIKE ? OR answer_text LIKE ?) " +
            "ORDER BY answered_datetime DESC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("question_id"),
                        rs.getString("subject"),
                        rs.getString("question_text"),
                        rs.getString("answer_text"),
                        rs.getTimestamp("asked_datetime"),
                        rs.getTimestamp("answered_datetime")
                    });
                }
            }
        }
        return list;
    }
}
