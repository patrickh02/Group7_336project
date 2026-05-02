package com.flightreservation.ui;

import com.flightreservation.model.Airport;
import com.flightreservation.service.CustomerRepService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ManageAirportsPanel extends JPanel {

    private final CustomerRepService service;
    private final DefaultTableModel  tableModel;
    private final JTable             table;
    private       List<Airport>      currentData;

    private static final String[] COLS = {"Code", "Name", "City", "Country"};

    public ManageAirportsPanel(CustomerRepService service) {
        this.service = service;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);

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
            Airport a = currentData.get(row);
            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete airport " + a.getAirportId() + " (" + a.getName() + ")?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    service.deleteAirport(a.getAirportId());
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
            currentData = service.getAllAirports();
            for (Airport a : currentData) {
                tableModel.addRow(new Object[]{
                    a.getAirportId(), a.getName(), a.getCity(), a.getCountry()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDialog(Airport existing) {
        boolean isEdit = (existing != null);

        JTextField codeField    = new JTextField(isEdit ? existing.getAirportId() : "", 4);
        JTextField nameField    = new JTextField(isEdit ? existing.getName() : "", 20);
        JTextField cityField    = new JTextField(isEdit ? existing.getCity() : "", 15);
        JTextField countryField = new JTextField(isEdit ? existing.getCountry() : "", 15);

        if (isEdit) codeField.setEditable(false); // PK cannot change

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Airport Code (3 letters):")); form.add(codeField);
        form.add(new JLabel("Name:"));                     form.add(nameField);
        form.add(new JLabel("City:"));                     form.add(cityField);
        form.add(new JLabel("Country:"));                  form.add(countryField);

        int result = JOptionPane.showConfirmDialog(this, form,
            isEdit ? "Edit Airport" : "Add Airport", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String code    = codeField.getText().trim().toUpperCase();
        String name    = nameField.getText().trim();
        String city    = cityField.getText().trim();
        String country = countryField.getText().trim();

        if (!isEdit && (code.isEmpty() || code.length() != 3)) {
            JOptionPane.showMessageDialog(this, "Airport code must be exactly 3 letters.");
            return;
        }
        if (name.isEmpty() || city.isEmpty() || country.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        Airport a = isEdit ? existing : new Airport();
        if (!isEdit) a.setAirportId(code);
        a.setName(name);
        a.setCity(city);
        a.setCountry(country);

        try {
            if (isEdit) service.updateAirport(a);
            else        service.addAirport(a);
            loadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saving: " + ex.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
