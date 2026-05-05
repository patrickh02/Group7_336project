package com.flightreservation.ui;

import com.flightreservation.model.Aircraft;
import com.flightreservation.model.Airline;
import com.flightreservation.model.Airport;
import com.flightreservation.model.Flight;
import com.flightreservation.service.CustomerRepService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;

public class ManageFlightsPanel extends JPanel {

    private final CustomerRepService service;
    private final DefaultTableModel  tableModel;
    private final JTable             table;
    private       List<Flight>       currentData;

    private static final String[] COLS = {
        "ID", "Flight#", "Airline", "Aircraft", "From", "To",
        "Dep", "Arr", "Type", "Days", "Eco$", "Bus$", "1st$"
    };

    public ManageFlightsPanel(CustomerRepService service) {
        this.service = service;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {40, 65, 75, 70, 50, 50, 65, 65, 90, 130, 60, 60, 60};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh");
        JButton addBtn     = new JButton("Add");
        JButton editBtn    = new JButton("Edit Selected");
        JButton deleteBtn  = new JButton("Delete Selected");
        btnPanel.add(refreshBtn);
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        add(btnPanel, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadData());
        addBtn.addActionListener(e -> showDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }
            showDialog(currentData.get(row));
        });
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row first."); return; }
            Flight f = currentData.get(row);
            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete flight " + f.getAirlineId() + f.getFlightNum() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    service.deleteFlight(f.getFlightId());
                    loadData();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "DB Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            currentData = service.getAllFlights();
            for (Flight f : currentData) {
                tableModel.addRow(new Object[]{
                    f.getFlightId(), f.getFlightNum(), f.getAirlineId(),
                    f.getAircraftId(), f.getDepAirportId(), f.getArrAirportId(),
                    f.getDepTime(), f.getArrTime(), f.getType(), f.getDaysOfWeek(),
                    String.format("%.0f", f.getEconomyPrice()),
                    String.format("%.0f", f.getBusinessPrice()),
                    String.format("%.0f", f.getFirstPrice())
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDialog(Flight existing) {
        boolean isEdit = (existing != null);

        List<Airline>  airlines;
        List<Aircraft> aircrafts;
        List<Airport>  airports;
        try {
            airlines  = service.getAllAirlines();
            aircrafts = service.getAllAircrafts();
            airports  = service.getAllAirports();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading lookups: " + ex.getMessage());
            return;
        }

        JTextField flightNumField  = new JTextField(isEdit ? existing.getFlightNum() : "", 10);
        JComboBox<String> airlineBox = new JComboBox<>();
        for (Airline al : airlines) airlineBox.addItem(al.getAirlineId() + " - " + al.getName());

        JComboBox<String> aircraftBox = new JComboBox<>();
        for (Aircraft ac : aircrafts) aircraftBox.addItem(ac.getAircraftId() + " - " + ac.getModel());

        JComboBox<String> depBox = new JComboBox<>();
        JComboBox<String> arrBox = new JComboBox<>();
        for (Airport ap : airports) {
            depBox.addItem(ap.getAirportId() + " - " + ap.getCity());
            arrBox.addItem(ap.getAirportId() + " - " + ap.getCity());
        }

        JTextField depTimeField   = new JTextField(isEdit && existing.getDepTime() != null ? existing.getDepTime().toString() : "08:00:00", 9);
        JTextField arrTimeField   = new JTextField(isEdit && existing.getArrTime() != null ? existing.getArrTime().toString() : "10:00:00", 9);
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"domestic", "international"});
        JTextField daysField      = new JTextField(isEdit ? existing.getDaysOfWeek() : "MON,WED,FRI", 20);
        JTextField ecoField       = new JTextField(isEdit ? String.format("%.2f", existing.getEconomyPrice()) : "199.00", 8);
        JTextField busField       = new JTextField(isEdit ? String.format("%.2f", existing.getBusinessPrice()) : "499.00", 8);
        JTextField firstField     = new JTextField(isEdit ? String.format("%.2f", existing.getFirstPrice()) : "999.00", 8);

        if (isEdit) {
            for (int i = 0; i < airlines.size(); i++) {
                if (airlines.get(i).getAirlineId().equals(existing.getAirlineId())) {
                    airlineBox.setSelectedIndex(i);
                    break;
                }
            }
            selectByIndex(aircraftBox, aircrafts, existing.getAircraftId());
            selectAirportCombo(depBox, airports, existing.getDepAirportId());
            selectAirportCombo(arrBox, airports, existing.getArrAirportId());
            for (int i = 0; i < typeBox.getItemCount(); i++)
                if (typeBox.getItemAt(i).equals(existing.getType())) { typeBox.setSelectedIndex(i); break; }
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Flight Number:"));  form.add(flightNumField);
        form.add(new JLabel("Airline:"));         form.add(airlineBox);
        form.add(new JLabel("Aircraft:"));        form.add(aircraftBox);
        form.add(new JLabel("Dep Airport:"));     form.add(depBox);
        form.add(new JLabel("Arr Airport:"));     form.add(arrBox);
        form.add(new JLabel("Dep Time (HH:MM:SS):")); form.add(depTimeField);
        form.add(new JLabel("Arr Time (HH:MM:SS):")); form.add(arrTimeField);
        form.add(new JLabel("Type:"));            form.add(typeBox);
        form.add(new JLabel("Days (e.g. MON,WED,FRI):")); form.add(daysField);
        form.add(new JLabel("Economy Price:"));   form.add(ecoField);
        form.add(new JLabel("Business Price:"));  form.add(busField);
        form.add(new JLabel("First Price:"));     form.add(firstField);

        int result = JOptionPane.showConfirmDialog(this, new JScrollPane(form),
            isEdit ? "Edit Flight" : "Add Flight", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            Flight f = isEdit ? existing : new Flight();
            f.setFlightNum(flightNumField.getText().trim());
            f.setAirlineId(airlines.get(airlineBox.getSelectedIndex()).getAirlineId());
            f.setAircraftId(aircrafts.get(aircraftBox.getSelectedIndex()).getAircraftId());
            f.setDepAirportId(airports.get(depBox.getSelectedIndex()).getAirportId());
            f.setArrAirportId(airports.get(arrBox.getSelectedIndex()).getAirportId());
            f.setDepTime(Time.valueOf(depTimeField.getText().trim()));
            f.setArrTime(Time.valueOf(arrTimeField.getText().trim()));
            f.setType((String) typeBox.getSelectedItem());
            f.setDaysOfWeek(daysField.getText().trim());
            f.setEconomyPrice(Double.parseDouble(ecoField.getText().trim()));
            f.setBusinessPrice(Double.parseDouble(busField.getText().trim()));
            f.setFirstPrice(Double.parseDouble(firstField.getText().trim()));

            if (isEdit) service.updateFlight(f);
            else        service.addFlight(f);
            loadData();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format in price or time field.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Invalid time format. Use HH:MM:SS.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saving: " + ex.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selectByIndex(JComboBox<String> box, List<Aircraft> list, int id) {
        for (int i = 0; i < list.size(); i++)
            if (list.get(i).getAircraftId() == id) { box.setSelectedIndex(i); return; }
    }

    private void selectAirportCombo(JComboBox<String> box, List<Airport> list, String id) {
        for (int i = 0; i < list.size(); i++)
            if (list.get(i).getAirportId().equals(id)) { box.setSelectedIndex(i); return; }
    }
}
