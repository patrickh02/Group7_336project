package com.flightreservation.service;

import com.flightreservation.db.DBConnection;
import com.flightreservation.model.Customer;
import com.flightreservation.model.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminService {

    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT customer_id, name, address, email, phone FROM Customer ORDER BY name";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Customer cu = new Customer();
                cu.setCustomerId(rs.getInt("customer_id"));
                cu.setName(rs.getString("name"));
                cu.setAddress(rs.getString("address"));
                cu.setEmail(rs.getString("email"));
                cu.setPhone(rs.getString("phone"));
                list.add(cu);
            }
        }
        return list;
    }

    public void addCustomer(Customer cu) throws SQLException {
        String sql = "INSERT INTO Customer (name, address, email, phone, password) VALUES (?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cu.getName());
            ps.setString(2, cu.getAddress());
            ps.setString(3, cu.getEmail());
            ps.setString(4, cu.getPhone());
            ps.setString(5, cu.getPassword());
            ps.executeUpdate();
        }
    }

    public void updateCustomer(Customer cu) throws SQLException {
        String sql = "UPDATE Customer SET name=?, address=?, email=?, phone=? WHERE customer_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cu.getName());
            ps.setString(2, cu.getAddress());
            ps.setString(3, cu.getEmail());
            ps.setString(4, cu.getPhone());
            ps.setInt(5, cu.getCustomerId());
            ps.executeUpdate();
        }
    }

    public void deleteCustomer(int customerId) throws SQLException {
        String sql = "DELETE FROM Customer WHERE customer_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
        }
    }


    public List<Employee> getAllEmployees() throws SQLException {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT employee_id, name, email, type FROM Employee ORDER BY type, name";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Employee e = new Employee();
                e.setEmployeeId(rs.getInt("employee_id"));
                e.setName(rs.getString("name"));
                e.setEmail(rs.getString("email"));
                e.setType(rs.getString("type"));
                list.add(e);
            }
        }
        return list;
    }

    public void addEmployee(Employee e) throws SQLException {
        String sql = "INSERT INTO Employee (name, email, type, password) VALUES (?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getName());
            ps.setString(2, e.getEmail());
            ps.setString(3, e.getType());
            ps.setString(4, e.getPassword());
            ps.executeUpdate();
        }
    }

    public void updateEmployee(Employee e) throws SQLException {
        String sql = "UPDATE Employee SET name=?, email=?, type=? WHERE employee_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getName());
            ps.setString(2, e.getEmail());
            ps.setString(3, e.getType());
            ps.setInt(4, e.getEmployeeId());
            ps.executeUpdate();
        }
    }

    public void deleteEmployee(int employeeId) throws SQLException {
        String sql = "DELETE FROM Employee WHERE employee_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.executeUpdate();
        }
    }


    public List<Object[]> getSalesReport(int year, int month) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT t.ticket_num, c.name AS cname, t.type, t.total_fare, t.booking_fee, " +
            "t.purchase_datetime, t.status " +
            "FROM Ticket t " +
            "JOIN Customer c ON t.customer_id = c.customer_id " +
            "WHERE YEAR(t.purchase_datetime) = ? AND MONTH(t.purchase_datetime) = ? " +
            "ORDER BY t.purchase_datetime";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("ticket_num"),
                        rs.getString("cname"),
                        rs.getString("type"),
                        rs.getDouble("total_fare"),
                        rs.getDouble("booking_fee"),
                        rs.getTimestamp("purchase_datetime"),
                        rs.getString("status")
                    });
                }
            }
        }
        return list;
    }

    public List<Object[]> getReservationsByFlight(String flightNum) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT t.ticket_num, cu.name AS cname, cu.email, " +
            "f.flight_num, al.name AS aname, " +
            "f.dep_airport_id, f.arr_airport_id, tf.dep_date, tf.class, tf.seat_num, " +
            "t.total_fare, t.status " +
            "FROM Ticket t " +
            "JOIN Customer     cu ON t.customer_id  = cu.customer_id " +
            "JOIN TicketFlight tf ON t.ticket_num   = tf.ticket_num " +
            "JOIN Flight       f  ON tf.flight_id   = f.flight_id " +
            "JOIN Airline      al ON f.airline_id   = al.airline_id " +
            "WHERE f.flight_num LIKE ? " +
            "ORDER BY tf.dep_date, t.ticket_num";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + flightNum + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("ticket_num"),
                        rs.getString("cname"),
                        rs.getString("email"),
                        rs.getString("flight_num"),
                        rs.getString("aname"),
                        rs.getString("dep_airport_id"),
                        rs.getString("arr_airport_id"),
                        rs.getDate("dep_date"),
                        rs.getString("class"),
                        rs.getString("seat_num"),
                        rs.getDouble("total_fare"),
                        rs.getString("status")
                    });
                }
            }
        }
        return list;
    }

    public List<Object[]> getReservationsByCustomer(String customerName) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT t.ticket_num, cu.name AS cname, cu.email, " +
            "f.flight_num, al.name AS aname, " +
            "f.dep_airport_id, f.arr_airport_id, tf.dep_date, tf.class, tf.seat_num, " +
            "t.total_fare, t.status " +
            "FROM Ticket t " +
            "JOIN Customer     cu ON t.customer_id  = cu.customer_id " +
            "JOIN TicketFlight tf ON t.ticket_num   = tf.ticket_num " +
            "JOIN Flight       f  ON tf.flight_id   = f.flight_id " +
            "JOIN Airline      al ON f.airline_id   = al.airline_id " +
            "WHERE cu.name LIKE ? OR cu.email LIKE ? " +
            "ORDER BY cu.name, tf.dep_date";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String like = "%" + customerName + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("ticket_num"),
                        rs.getString("cname"),
                        rs.getString("email"),
                        rs.getString("flight_num"),
                        rs.getString("aname"),
                        rs.getString("dep_airport_id"),
                        rs.getString("arr_airport_id"),
                        rs.getDate("dep_date"),
                        rs.getString("class"),
                        rs.getString("seat_num"),
                        rs.getDouble("total_fare"),
                        rs.getString("status")
                    });
                }
            }
        }
        return list;
    }

    public List<Object[]> getRevenueByFlight() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT f.flight_num, al.name AS aname, " +
            "f.dep_airport_id, f.arr_airport_id, " +
            "COUNT(DISTINCT t.ticket_num) AS tickets_sold, " +
            "COALESCE(SUM(t.total_fare), 0) AS total_revenue " +
            "FROM Flight f " +
            "JOIN Airline al ON f.airline_id = al.airline_id " +
            "LEFT JOIN TicketFlight tf ON f.flight_id = tf.flight_id " +
            "LEFT JOIN Ticket t ON tf.ticket_num = t.ticket_num AND t.status = 'active' " +
            "GROUP BY f.flight_id, f.flight_num, al.name, f.dep_airport_id, f.arr_airport_id " +
            "ORDER BY total_revenue DESC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("flight_num"),
                    rs.getString("aname"),
                    rs.getString("dep_airport_id"),
                    rs.getString("arr_airport_id"),
                    rs.getInt("tickets_sold"),
                    rs.getDouble("total_revenue")
                });
            }
        }
        return list;
    }


    public List<Object[]> getRevenueByAirline() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT al.airline_id, al.name AS aname, " +
            "COUNT(DISTINCT t.ticket_num) AS tickets_sold, " +
            "COALESCE(SUM(t.total_fare), 0) AS total_revenue " +
            "FROM Airline al " +
            "LEFT JOIN Flight f ON al.airline_id = f.airline_id " +
            "LEFT JOIN TicketFlight tf ON f.flight_id = tf.flight_id " +
            "LEFT JOIN Ticket t ON tf.ticket_num = t.ticket_num AND t.status = 'active' " +
            "GROUP BY al.airline_id, al.name " +
            "ORDER BY total_revenue DESC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("airline_id"),
                    rs.getString("aname"),
                    rs.getInt("tickets_sold"),
                    rs.getDouble("total_revenue")
                });
            }
        }
        return list;
    }
    public List<Object[]> getRevenueByCustomer() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT c.customer_id, c.name, c.email, " +
            "COUNT(t.ticket_num) AS tickets_bought, " +
            "COALESCE(SUM(t.total_fare), 0) AS total_spent " +
            "FROM Customer c " +
            "LEFT JOIN Ticket t ON c.customer_id = t.customer_id AND t.status = 'active' " +
            "GROUP BY c.customer_id, c.name, c.email " +
            "ORDER BY total_spent DESC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("customer_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getInt("tickets_bought"),
                    rs.getDouble("total_spent")
                });
            }
        }
        return list;
    }

    public Object[] getTopRevenueCustomer() throws SQLException {
        String sql =
            "SELECT c.name, c.email, COALESCE(SUM(t.total_fare), 0) AS total_spent " +
            "FROM Customer c " +
            "LEFT JOIN Ticket t ON c.customer_id = t.customer_id AND t.status = 'active' " +
            "GROUP BY c.customer_id, c.name, c.email " +
            "ORDER BY total_spent DESC LIMIT 1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new Object[]{
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getDouble("total_spent")
                };
            }
        }
        return null;
    }

    public List<Object[]> getMostActiveFlights() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT f.flight_num, al.name AS aname, " +
            "f.dep_airport_id, f.arr_airport_id, " +
            "COUNT(DISTINCT t.ticket_num) AS tickets_sold " +
            "FROM Flight f " +
            "JOIN Airline al ON f.airline_id = al.airline_id " +
            "LEFT JOIN TicketFlight tf ON f.flight_id = tf.flight_id " +
            "LEFT JOIN Ticket t ON tf.ticket_num = t.ticket_num AND t.status = 'active' " +
            "GROUP BY f.flight_id, f.flight_num, al.name, f.dep_airport_id, f.arr_airport_id " +
            "ORDER BY tickets_sold DESC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("flight_num"),
                    rs.getString("aname"),
                    rs.getString("dep_airport_id"),
                    rs.getString("arr_airport_id"),
                    rs.getInt("tickets_sold")
                });
            }
        }
        return list;
    }
}
