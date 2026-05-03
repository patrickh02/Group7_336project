package com.flightreservation.ui.customer;

import com.flightreservation.model.Customer;
import com.flightreservation.service.CustomerService;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;

/**
 * Modal dialog shown when a customer selects a flight and clicks "Book".
 * Handles class selection, seat, meal, capacity check, and waitlist offer.
 */
public class BookingDialog extends JDialog {

    private final CustomerService service;
    private final Customer        customer;
    private final Object[]        flight;   // row from searchFlights()
    private final Date            depDate;

    private JComboBox<String> classBox;
    private JTextField        seatField;
    private JComboBox<String> mealBox;
    private JLabel            priceLabel;
    private JLabel            capacityLabel;

    public BookingDialog(JFrame owner, CustomerService service,
                          Customer customer, Object[] flight, Date depDate) {
        super(owner, "Book Flight", true);
        this.service  = service;
        this.customer = customer;
        this.flight   = flight;
        this.depDate  = depDate;

        setSize(480, 400);
        setLocationRelativeTo(owner);
        setResizable(false);

        buildUI();
        updatePriceAndCapacity();
    }

    private void buildUI() {
        int    flightId    = (Integer) flight[0];
        String flightNum   = (String)  flight[1];
        String airlineId   = (String)  flight[2];
        String airlineName = (String)  flight[3];
        String depCode     = (String)  flight[4];
        String arrCode     = (String)  flight[5];
        String depCity     = (String)  flight[6];
        String arrCity     = (String)  flight[7];
        Object depTime     = flight[8];
        Object arrTime     = flight[9];

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Title
        JLabel title = new JLabel(
            "<html><b>" + airlineId + flightNum + " — " + airlineName + "</b><br>" +
            depCode + " (" + depCity + ") → " + arrCode + " (" + arrCity + ")<br>" +
            "Dep: " + depTime + "  Arr: " + arrTime + "  Date: " + depDate + "</html>");
        title.setFont(title.getFont().deriveFont(13f));
        main.add(title, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 6, 7, 6);
        g.fill   = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        int row = 0;

        g.gridy = row; g.gridx = 0; form.add(new JLabel("Class:"), g);
        classBox = new JComboBox<>(new String[]{"economy", "business", "first"});
        g.gridx = 1; form.add(classBox, g);
        row++;

        g.gridy = row; g.gridx = 0; form.add(new JLabel("Price:"), g);
        priceLabel = new JLabel("$---");
        priceLabel.setFont(priceLabel.getFont().deriveFont(Font.BOLD));
        g.gridx = 1; form.add(priceLabel, g);
        row++;

        g.gridy = row; g.gridx = 0; form.add(new JLabel("Availability:"), g);
        capacityLabel = new JLabel("Checking...");
        g.gridx = 1; form.add(capacityLabel, g);
        row++;

        g.gridy = row; g.gridx = 0; form.add(new JLabel("Seat Number (optional):"), g);
        seatField = new JTextField(6);
        g.gridx = 1; form.add(seatField, g);
        row++;

        g.gridy = row; g.gridx = 0; form.add(new JLabel("Meal Preference:"), g);
        mealBox = new JComboBox<>(new String[]{"standard", "vegetarian", "vegan", "halal", "kosher"});
        g.gridx = 1; form.add(mealBox, g);
        row++;

        main.add(form, BorderLayout.CENTER);

        // Buttons
        JButton confirmBtn = new JButton("Confirm Booking");
        confirmBtn.setFont(confirmBtn.getFont().deriveFont(Font.BOLD));
        confirmBtn.setBackground(new Color(50, 160, 80));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setOpaque(true);

        JButton cancelBtn = new JButton("Cancel");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(cancelBtn);
        btnPanel.add(confirmBtn);
        main.add(btnPanel, BorderLayout.SOUTH);

        add(main);

        // Wire up
        classBox.addActionListener(e -> updatePriceAndCapacity());
        confirmBtn.addActionListener(e -> confirmBooking());
        cancelBtn.addActionListener(e -> dispose());
    }

    private void updatePriceAndCapacity() {
        String cls = (String) classBox.getSelectedItem();
        double price;
        if ("business".equalsIgnoreCase(cls)) price = (Double) flight[13];
        else if ("first".equalsIgnoreCase(cls)) price = (Double) flight[14];
        else price = (Double) flight[12];

        priceLabel.setText(String.format("$%.2f  (+$25.00 booking fee = $%.2f total)", price, price + 25));

        int capacity = (Integer) flight[15];
        int booked   = (Integer) flight[16];
        int available = capacity - booked;
        if (available <= 0) {
            capacityLabel.setText("FLIGHT FULL — you will be placed on the waitlist");
            capacityLabel.setForeground(Color.RED);
        } else {
            capacityLabel.setText(available + " seats available");
            capacityLabel.setForeground(new Color(0, 128, 0));
        }
    }

    private void confirmBooking() {
        int flightId = (Integer) flight[0];
        String cls   = (String) classBox.getSelectedItem();
        String seat  = seatField.getText().trim();
        String meal  = (String) mealBox.getSelectedItem();

        double price;
        if ("business".equalsIgnoreCase(cls)) price = (Double) flight[13];
        else if ("first".equalsIgnoreCase(cls)) price = (Double) flight[14];
        else price = (Double) flight[12];

        try {
            int booked   = service.getBookedSeats(flightId, depDate);
            int capacity = service.getFlightCapacity(flightId);

            if (booked >= capacity) {
                // Flight full — offer waitlist
                int choice = JOptionPane.showConfirmDialog(this,
                    "This flight is full on " + depDate + ".\n" +
                    "Would you like to join the waitlist?",
                    "Flight Full", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    service.addToWaitlist(customer.getCustomerId(), flightId, depDate, cls);
                    JOptionPane.showMessageDialog(this,
                        "You have been added to the waitlist.\n" +
                        "We will notify you if a seat becomes available.",
                        "Waitlist Confirmed", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }
            } else {
                int ticketNum = service.bookTicket(
                    customer.getCustomerId(), flightId, depDate,
                    cls, seat, meal, false, price);
                JOptionPane.showMessageDialog(this,
                    "Booking confirmed!\n\n" +
                    "Ticket #: " + ticketNum + "\n" +
                    "Flight:   " + flight[2] + flight[1] + "\n" +
                    "Date:     " + depDate + "\n" +
                    "Class:    " + cls + "\n" +
                    "Total:    $" + String.format("%.2f", price + 25),
                    "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
