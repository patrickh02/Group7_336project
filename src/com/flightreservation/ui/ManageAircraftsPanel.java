package com.flightreservation.ui;

import com.flightreservation.model.Aircraft;
import com.flightreservation.model.Airline;
import com.flightreservation.service.CustomerRepService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ManageAircraftsPanel extends JPanel {

    private final CustomerRepService service;
    private final DefaultTableModel  tableModel;
    private final JTable             table;
    private       List<Aircraft>     currentData;

    private static final String[] COLS = {"ID", "Model", "Capacity", "Airline"};

    public ManageAircraftsPanel(CustomerRepService service) {
        this.service = service;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);

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
            Aircraft a = currentData.get(row);
            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete aircraft ID " + a.getAircraftId() + " (" + a.getModel() + ")?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    service.deleteAircraft(a.getAircraftId());
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
            currentData = service.getAllAircrafts();
            for (Aircraft a : currentData) {
                tableModel.addRow(new Object[]{
                    a.getAircraftId(), a.getModel(), a.getCapacity(), a.getAirlineId()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDialog(Aircraft existing) {
        boolean isEdit = (existing != null);

        List<Airline> airlines;
        try { airlines = service.getAllAirlines(); }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading airlines: " + ex.getMessage());
            return;
        }

        JTextField modelField    = new JTextField(isEdit ? existing.getModel() : "", 15);
        JTextField capacityField = new JTextField(isEdit ? String.valueOf(existing.getCapacity()) : "", 6);
        JComboBox<String> airlineBox = new JComboBox<>();
        for (Airline al : airlines) airlineBox.addItem(al.getAirlineId() + " - " + al.getName());
        if (isEdit) {
            for (int i = 0; i < airlines.size(); i++) {
                if (airlines.get(i).getAirlineId().equals(existing.getAirlineId())) {
                    airlineBox.setSelectedIndex(i);
                    break;
                }
            }
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Model:"));       form.add(modelField);
        form.add(new JLabel("Capacity:"));    form.add(capacityField);
        form.add(new JLabel("Airline:"));     form.add(airlineBox);

        int result = JOptionPane.showConfirmDialog(this, form,
            isEdit ? "Edit Aircraft" : "Add Aircraft", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String model = modelField.getText().trim();
        String capStr = capacityField.getText().trim();
        if (model.isEmpty() || capStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Model and Capacity are required.");
            return;
        }
        int capacity;
        try { capacity = Integer.parseInt(capStr); }
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Capacity must be a number.");
            return;
        }
        String airlineId = airlines.get(airlineBox.getSelectedIndex()).getAirlineId();

        Aircraft a = isEdit ? existing : new Aircraft();
        a.setModel(model);
        a.setCapacity(capacity);
        a.setAirlineId(airlineId);

        try {
            if (isEdit) service.updateAircraft(a);
            else        service.addAircraft(a);
            loadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saving: " + ex.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
