package com.flightreservation.ui;

import com.flightreservation.model.Customer;
import com.flightreservation.model.Employee;
import com.flightreservation.model.Flight;
import com.flightreservation.service.CustomerRepService;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class CustomerRepReservationPanel extends JPanel {

    private final CustomerRepService service;
    private final Employee           rep;

    private JComboBox<String> customerBox;
    private JComboBox<String> flightBox;
    private JTextField        dateField;
    private JComboBox<String> classBox;
    private JTextField        seatField;
    private JComboBox<String> mealBox;
    private JLabel            capacityLabel;

    private List<Customer> customers;
    private List<Flight>   flights;

    public CustomerRepReservationPanel(CustomerRepService service, Employee rep) {
        this.service = service;
        this.rep     = rep;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill   = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        int row = 0;

        JLabel title = new JLabel("Make Reservation for Customer");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        g.gridx = 0; g.gridy = row++; g.gridwidth = 3;
        form.add(title, g);
        g.gridwidth = 1;

        g.gridy = row; g.gridx = 0; form.add(new JLabel("Customer:"), g);
        customerBox = new JComboBox<>();
        g.gridx = 1; g.gridwidth = 2; form.add(customerBox, g);
        g.gridwidth = 1;
        JButton refreshCust = new JButton("Reload Customers");
        g.gridx = 3; form.add(refreshCust, g);
        row++;

        g.gridy = row; g.gridx = 0; form.add(new JLabel("Flight:"), g);
        flightBox = new JComboBox<>();
        g.gridx = 1; g.gridwidth = 2; form.add(flightBox, g);
        g.gridwidth = 1;
        JButton refreshFlight = new JButton("Reload Flights");
        g.gridx = 3; form.add(refreshFlight, g);
        row++;

        capacityLabel = new JLabel(" ");
        g.gridy = row++; g.gridx = 1; g.gridwidth = 2;
        form.add(capacityLabel, g);
        g.gridwidth = 1;

        g.gridy = row; g.gridx = 0; form.add(new JLabel("Departure Date (YYYY-MM-DD):"), g);
        dateField = new JTextField("2026-06-15", 12);
        g.gridx = 1; form.add(dateField, g);
        row++;

        g.gridy = row; g.gridx = 0; form.add(new JLabel("Class:"), g);
        classBox = new JComboBox<>(new String[]{"economy", "business", "first"});
        g.gridx = 1; form.add(classBox, g);
        row++;

        g.gridy = row; g.gridx = 0; form.add(new JLabel("Seat Number (optional):"), g);
        seatField = new JTextField("", 6);
        g.gridx = 1; form.add(seatField, g);
        row++;

        g.gridy = row; g.gridx = 0; form.add(new JLabel("Meal Preference:"), g);
        mealBox = new JComboBox<>(new String[]{"standard", "vegetarian", "vegan", "halal", "kosher"});
        g.gridx = 1; form.add(mealBox, g);
        row++;

        JButton bookBtn = new JButton("Book Reservation");
        bookBtn.setFont(bookBtn.getFont().deriveFont(Font.BOLD));
        g.gridy = row; g.gridx = 0; g.gridwidth = 4; g.fill = GridBagConstraints.NONE;
        form.add(bookBtn, g);

        add(form, BorderLayout.NORTH);

        refreshCust.addActionListener(e -> loadCustomers());
        refreshFlight.addActionListener(e -> loadFlights());
        flightBox.addActionListener(e -> updateCapacityLabel());
        bookBtn.addActionListener(e -> bookReservation());

        loadCustomers();
        loadFlights();
    }

    private void loadCustomers() {
        try {
            customers = service.getAllCustomers();
            customerBox.removeAllItems();
            for (Customer c : customers)
                customerBox.addItem(c.getCustomerId() + " - " + c.getName() + " (" + c.getEmail() + ")");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading customers: " + ex.getMessage());
        }
    }

    private void loadFlights() {
        try {
            flights = service.getAllFlights();
            flightBox.removeAllItems();
            for (Flight f : flights)
                flightBox.addItem(f.getFlightId() + " | " + f.getAirlineId() + f.getFlightNum()
                    + " " + f.getDepAirportId() + "→" + f.getArrAirportId()
                    + " dep:" + f.getDepTime());
            updateCapacityLabel();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading flights: " + ex.getMessage());
        }
    }

    private void updateCapacityLabel() {
        if (flights == null || flights.isEmpty() || flightBox.getSelectedIndex() < 0) return;
        Flight f = flights.get(flightBox.getSelectedIndex());
        try {
            Date d = parseDate();
            if (d == null) { capacityLabel.setText("Enter a valid date to check capacity."); return; }
            int booked   = service.getBookedSeats(f.getFlightId(), d);
            int capacity = service.getFlightCapacity(f.getFlightId());
            boolean full = booked >= capacity;
            capacityLabel.setText("Booked: " + booked + " / " + capacity + (full ? "  *** FLIGHT FULL — will add to waitlist ***" : "  seats available"));
            capacityLabel.setForeground(full ? Color.RED : new Color(0, 128, 0));
        } catch (SQLException ex) {
            capacityLabel.setText("Could not check capacity.");
        }
    }

    private Date parseDate() {
        try { return Date.valueOf(dateField.getText().trim()); }
        catch (IllegalArgumentException e) { return null; }
    }

    private void bookReservation() {
        if (customers == null || customers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No customers loaded."); return;
        }
        if (flights == null || flights.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No flights loaded."); return;
        }
        Date depDate = parseDate();
        if (depDate == null) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.");
            return;
        }

        Customer selected = customers.get(customerBox.getSelectedIndex());
        Flight   flight   = flights.get(flightBox.getSelectedIndex());
        String   cls      = (String) classBox.getSelectedItem();
        String   seat     = seatField.getText().trim();
        String   meal     = (String) mealBox.getSelectedItem();

        try {
            if (service.isFlightFull(flight.getFlightId(), depDate)) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Flight is full. Add " + selected.getName() + " to the waitlist?",
                    "Flight Full", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    service.addToWaitlist(selected.getCustomerId(), flight.getFlightId(), depDate, cls);
                    JOptionPane.showMessageDialog(this, selected.getName() + " added to waitlist.");
                }
            } else {
                int ticketNum = service.createReservation(
                    selected.getCustomerId(), flight.getFlightId(), depDate, cls, seat, meal);
                JOptionPane.showMessageDialog(this,
                    "Reservation booked!\nTicket #: " + ticketNum +
                    "\nCustomer: " + selected.getName() +
                    "\nFlight: " + flight.getAirlineId() + flight.getFlightNum() +
                    "\nClass: " + cls + "  Date: " + depDate);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
