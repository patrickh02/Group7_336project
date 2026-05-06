package com.flightreservation.ui;

import com.flightreservation.model.Flight;
import com.flightreservation.service.CustomerRepService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class WaitlistPanel extends JPanel {

    private final CustomerRepService service;
    private final DefaultTableModel  tableModel;
    private       List<Flight>       flights;

    private static final String[] COLS = {
        "Position", "Customer ID", "Name", "Email", "Phone", "Dep Date", "Class", "Request Time"
    };

    public WaitlistPanel(CustomerRepService service) {
        this.service = service;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Flight:"));
        JComboBox<String> flightBox = new JComboBox<>();
        topPanel.add(flightBox);
        JButton loadFlightsBtn = new JButton("Reload Flights");
        JButton searchBtn      = new JButton("Show Waitlist");
        topPanel.add(loadFlightsBtn);
        topPanel.add(searchBtn);
        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel countLabel = new JLabel(" ");
        countLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(countLabel, BorderLayout.SOUTH);

        Runnable loadFlights = () -> {
            try {
                flights = service.getAllFlights();
                flightBox.removeAllItems();
                for (Flight f : flights)
                    flightBox.addItem(f.getFlightId() + " | " + f.getAirlineId() + f.getFlightNum()
                        + " " + f.getDepAirportId() + "→" + f.getArrAirportId());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error loading flights: " + ex.getMessage());
            }
        };
        loadFlightsBtn.addActionListener(e -> loadFlights.run());

        searchBtn.addActionListener(e -> {
            if (flights == null || flights.isEmpty() || flightBox.getSelectedIndex() < 0) {
                JOptionPane.showMessageDialog(this, "Load flights first.");
                return;
            }
            Flight selected = flights.get(flightBox.getSelectedIndex());
            tableModel.setRowCount(0);
            try {
                List<Object[]> rows = service.getWaitlistForFlight(selected.getFlightId());
                for (Object[] row : rows) tableModel.addRow(row);
                countLabel.setText("Waitlist entries for flight "
                    + selected.getAirlineId() + selected.getFlightNum() + ": " + rows.size());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        loadFlights.run();
    }
}
