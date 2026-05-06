package com.flightreservation.ui;

import com.flightreservation.model.Airport;
import com.flightreservation.service.CustomerRepService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FlightsByAirportPanel extends JPanel {

    private final CustomerRepService service;
    private final DefaultTableModel  depModel;
    private final DefaultTableModel  arrModel;
    private       List<Airport>      airports = new ArrayList<>();

    private static final String[] COLS = {
        "Flight ID", "Flight#", "Airline", "City", "Time", "Type", "Days", "Economy$"
    };

    public FlightsByAirportPanel(CustomerRepService service) {
        this.service = service;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Airport:"));
        JComboBox<String> airportBox = new JComboBox<>();
        topPanel.add(airportBox);
        JButton loadAirportsBtn = new JButton("Reload Airports");
        JButton searchBtn       = new JButton("Show Flights");
        topPanel.add(loadAirportsBtn);
        topPanel.add(searchBtn);
        add(topPanel, BorderLayout.NORTH);

        depModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        arrModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JPanel depPanel = new JPanel(new BorderLayout());
        depPanel.setBorder(BorderFactory.createTitledBorder("Departing Flights"));
        depPanel.add(new JScrollPane(new JTable(depModel)));

        JPanel arrPanel = new JPanel(new BorderLayout());
        arrPanel.setBorder(BorderFactory.createTitledBorder("Arriving Flights"));
        arrPanel.add(new JScrollPane(new JTable(arrModel)));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, depPanel, arrPanel);
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);

        loadAirportsBtn.addActionListener(e -> loadAirports(airportBox));
        searchBtn.addActionListener(e -> showFlights(airportBox));

        loadAirports(airportBox);
    }

    private void loadAirports(JComboBox<String> box) {
        try {
            airports = service.getAllAirports();
            box.removeAllItems();
            for (Airport a : airports)
                box.addItem(a.getAirportId() + " - " + a.getCity());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading airports: " + ex.getMessage());
        }
    }

    private void showFlights(JComboBox<String> box) {
        if (airports.isEmpty() || box.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Load airports first.");
            return;
        }
        String airportId = airports.get(box.getSelectedIndex()).getAirportId();
        depModel.setRowCount(0);
        arrModel.setRowCount(0);
        try {
            for (Object[] row : service.getDepartingFlights(airportId)) depModel.addRow(row);
            for (Object[] row : service.getArrivingFlights(airportId))  arrModel.addRow(row);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
