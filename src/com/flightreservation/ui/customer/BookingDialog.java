package com.flightreservation.ui.customer;

import com.flightreservation.model.Customer;
import com.flightreservation.service.CustomerService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class BookingDialog extends JDialog {

    private final CustomerService service;
    private final Customer        customer;
    private final Object[]        flight;
    private final Date            depDate;
    private final String          tripType;
    private final Date            returnDate;

    private JComboBox<String> classBox;
    private JTextField        seatField;
    private JComboBox<String> mealBox;
    private JLabel            priceLabel;
    private JLabel            capacityLabel;

    private List<Object[]>    returnFlights;
    private JTable            returnTable;
    private DefaultTableModel returnModel;
    private JLabel            returnCapacityLabel;

    private static final String[] RET_COLS = {
        "Flight#", "Airline", "Dep Time", "Arr Time", "Seats Left"
    };

    public BookingDialog(JFrame owner, CustomerService service,
                         Customer customer, Object[] flight, Date depDate,
                         String tripType, Date returnDate) {
        super(owner, "Book Flight", true);
        this.service    = service;
        this.customer   = customer;
        this.flight     = flight;
        this.depDate    = depDate;
        this.tripType   = tripType;
        this.returnDate = returnDate;

        boolean isRoundTrip = "Round-Trip".equals(tripType);
        setSize(520, isRoundTrip ? 620 : 400);
        setLocationRelativeTo(owner);
        setResizable(false);

        buildUI();
        updatePriceAndCapacity();

        if (isRoundTrip) loadReturnFlights();
    }

    private void buildUI() {
        String flightNum   = (String)  flight[1];
        String airlineId   = (String)  flight[2];
        String airlineName = (String)  flight[3];
        String depCode     = (String)  flight[4];
        String arrCode     = (String)  flight[5];
        String depCity     = (String)  flight[6];
        String arrCity     = (String)  flight[7];
        Object depTime     = flight[8];
        Object arrTime     = flight[9];

        boolean isRoundTrip = "Round-Trip".equals(tripType);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel(
            "<html><b>" + airlineId + flightNum + " — " + airlineName + "</b><br>" +
            depCode + " (" + depCity + ") → " + arrCode + " (" + arrCity + ")<br>" +
            "Dep: " + depTime + "  Arr: " + arrTime + "  Date: " + depDate +
            (isRoundTrip ? "  |  Return: " + returnDate : "") + "</html>");
        title.setFont(title.getFont().deriveFont(13f));
        main.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        // Outbound booking form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder(isRoundTrip ? "Outbound Details" : "Booking Details"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill   = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        int row = 0;

        g.gridy = row; g.gridx = 0; form.add(new JLabel("Class:"), g);
        classBox = new JComboBox<>(new String[]{"economy", "business", "first"});
        g.gridx = 1; form.add(classBox, g); row++;

        g.gridy = row; g.gridx = 0; form.add(new JLabel("Price:"), g);
        priceLabel = new JLabel("$---");
        priceLabel.setFont(priceLabel.getFont().deriveFont(Font.BOLD));
        g.gridx = 1; form.add(priceLabel, g); row++;

        g.gridy = row; g.gridx = 0; form.add(new JLabel("Availability:"), g);
        capacityLabel = new JLabel("Checking...");
        g.gridx = 1; form.add(capacityLabel, g); row++;

        g.gridy = row; g.gridx = 0; form.add(new JLabel("Seat (optional):"), g);
        seatField = new JTextField(6);
        g.gridx = 1; form.add(seatField, g); row++;

        g.gridy = row; g.gridx = 0; form.add(new JLabel("Meal:"), g);
        mealBox = new JComboBox<>(new String[]{"standard", "vegetarian", "vegan", "halal", "kosher"});
        g.gridx = 1; form.add(mealBox, g);

        center.add(form);

        // Return flight section (round-trip only)
        if (isRoundTrip) {
            JPanel retPanel = new JPanel(new BorderLayout(5, 5));
            retPanel.setBorder(BorderFactory.createTitledBorder(
                "Select Return Flight  (" + flight[5] + " → " + flight[4] + "  on " + returnDate + ")"));

            returnModel = new DefaultTableModel(RET_COLS, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            returnTable = new JTable(returnModel);
            returnTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            returnTable.getColumnModel().getColumn(0).setPreferredWidth(65);
            returnTable.getColumnModel().getColumn(1).setPreferredWidth(130);
            returnTable.getColumnModel().getColumn(2).setPreferredWidth(70);
            returnTable.getColumnModel().getColumn(3).setPreferredWidth(70);
            returnTable.getColumnModel().getColumn(4).setPreferredWidth(80);

            returnCapacityLabel = new JLabel("  Select a return flight above");
            returnCapacityLabel.setFont(returnCapacityLabel.getFont().deriveFont(Font.ITALIC, 11f));

            returnTable.getSelectionModel().addListSelectionListener(ev -> {
                if (!ev.getValueIsAdjusting()) updateReturnCapacity();
            });

            retPanel.add(new JScrollPane(returnTable), BorderLayout.CENTER);
            retPanel.add(returnCapacityLabel, BorderLayout.SOUTH);
            retPanel.setPreferredSize(new Dimension(480, 190));
            center.add(Box.createVerticalStrut(8));
            center.add(retPanel);
        }

        main.add(center, BorderLayout.CENTER);

        JButton confirmBtn = new JButton(isRoundTrip ? "Confirm Round-Trip Booking" : "Confirm Booking");
        confirmBtn.setFont(confirmBtn.getFont().deriveFont(Font.BOLD));
        confirmBtn.setBackground(Color.WHITE);
        confirmBtn.setForeground(new Color(50, 160, 80));
        confirmBtn.setOpaque(true);

        JButton cancelBtn = new JButton("Cancel");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(cancelBtn);
        btnPanel.add(confirmBtn);
        main.add(btnPanel, BorderLayout.SOUTH);

        add(main);

        classBox.addActionListener(e -> updatePriceAndCapacity());
        confirmBtn.addActionListener(e -> confirmBooking());
        cancelBtn.addActionListener(e -> dispose());
    }

    private void loadReturnFlights() {
        String arrCode = (String) flight[5];
        String depCode = (String) flight[4];
        returnModel.setRowCount(0);
        try {
            returnFlights = service.searchFlights(arrCode, depCode, returnDate, false);
            if (returnFlights.isEmpty()) {
                returnFlights = service.searchFlights(arrCode, depCode, returnDate, true);
            }
            for (Object[] r : returnFlights) {
                int cap     = (Integer) r[15];
                int booked  = (Integer) r[16];
                int avail   = cap - booked;
                returnModel.addRow(new Object[]{
                    (String) r[2] + r[1],
                    r[3],
                    r[8],
                    r[9],
                    avail <= 0 ? "FULL" : avail + " seats"
                });
            }
            if (returnFlights.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "No return flights found from " + arrCode + " → " + depCode + " on " + returnDate + ".\n" +
                    "You can still book as one-way, or try a different date.",
                    "No Return Flights", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading return flights: " + ex.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateReturnCapacity() {
        if (returnFlights == null || returnTable == null) return;
        int sel = returnTable.getSelectedRow();
        if (sel < 0 || sel >= returnFlights.size()) {
            returnCapacityLabel.setText("  Select a return flight above");
            return;
        }
        Object[] r = returnFlights.get(sel);
        int cap    = (Integer) r[15];
        int booked = (Integer) r[16];
        int avail  = cap - booked;
        if (avail <= 0) {
            returnCapacityLabel.setText("  Return flight is FULL — you will be waitlisted");
            returnCapacityLabel.setForeground(Color.RED);
        } else {
            returnCapacityLabel.setText("  " + avail + " seats available on return flight");
            returnCapacityLabel.setForeground(new Color(0, 128, 0));
        }
    }

    private void updatePriceAndCapacity() {
        String cls = (String) classBox.getSelectedItem();
        double price = getPrice(flight, cls);
        priceLabel.setText(String.format("$%.2f  (+$25.00 booking fee)", price));

        int capacity  = (Integer) flight[15];
        int booked    = (Integer) flight[16];
        int available = capacity - booked;
        if (available <= 0) {
            capacityLabel.setText("FLIGHT FULL — you will be placed on the waitlist");
            capacityLabel.setForeground(Color.RED);
        } else {
            capacityLabel.setText(available + " seats available");
            capacityLabel.setForeground(new Color(0, 128, 0));
        }
    }

    private double getPrice(Object[] f, String cls) {
        if ("business".equalsIgnoreCase(cls)) return (Double) f[13];
        if ("first".equalsIgnoreCase(cls))    return (Double) f[14];
        return (Double) f[12];
    }

    private void confirmBooking() {
        boolean isRoundTrip = "Round-Trip".equals(tripType);
        int    flightId = (Integer) flight[0];
        String cls      = (String)  classBox.getSelectedItem();
        String seat     = seatField.getText().trim();
        String meal     = (String)  mealBox.getSelectedItem();
        double outPrice = getPrice(flight, cls);

        try {
            int outBooked   = service.getBookedSeats(flightId, depDate);
            int outCapacity = service.getFlightCapacity(flightId);

            if (outBooked >= outCapacity) {
                int choice = JOptionPane.showConfirmDialog(this,
                    "The outbound flight is full on " + depDate + ".\nJoin the waitlist?",
                    "Flight Full", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    service.addToWaitlist(customer.getCustomerId(), flightId, depDate, cls);
                    JOptionPane.showMessageDialog(this,
                        "Added to waitlist. We will notify you when a seat is available.",
                        "Waitlist Confirmed", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }
                return;
            }

            if (isRoundTrip) {
                int retSel = returnTable == null ? -1 : returnTable.getSelectedRow();
                if (retSel < 0 || returnFlights == null || retSel >= returnFlights.size()) {
                    JOptionPane.showMessageDialog(this, "Please select a return flight.");
                    return;
                }
                Object[] retFlight  = returnFlights.get(retSel);
                int retFlightId     = (Integer) retFlight[0];
                double retPrice     = getPrice(retFlight, cls);
                int retBooked       = service.getBookedSeats(retFlightId, returnDate);
                int retCapacity     = service.getFlightCapacity(retFlightId);

                if (retBooked >= retCapacity) {
                    JOptionPane.showMessageDialog(this,
                        "The selected return flight is full. Please choose another return flight.",
                        "Return Flight Full", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int ticketNum = service.bookRoundTrip(
                    customer.getCustomerId(),
                    flightId, depDate,
                    retFlightId, returnDate,
                    cls, seat, meal, false, outPrice, retPrice);

                JOptionPane.showMessageDialog(this,
                    "Round-trip booking confirmed!\n\n" +
                    "Ticket #: " + ticketNum + "\n" +
                    "Outbound: " + (String) flight[2] + flight[1] + "  (" + depDate + ")\n" +
                    "Return:   " + (String) retFlight[2] + retFlight[1] + "  (" + returnDate + ")\n" +
                    "Class:    " + cls + "\n" +
                    String.format("Total:    $%.2f", outPrice + retPrice + 25),
                    "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
                dispose();

            } else {
                int ticketNum = service.bookTicket(
                    customer.getCustomerId(), flightId, depDate,
                    cls, seat, meal, false, outPrice);
                JOptionPane.showMessageDialog(this,
                    "Booking confirmed!\n\n" +
                    "Ticket #: " + ticketNum + "\n" +
                    "Flight:   " + (String) flight[2] + flight[1] + "\n" +
                    "Date:     " + depDate + "\n" +
                    "Class:    " + cls + "\n" +
                    String.format("Total:    $%.2f", outPrice + 25),
                    "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
