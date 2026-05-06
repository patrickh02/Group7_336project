package com.flightreservation.ui;

import com.flightreservation.service.CustomerRepService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class EditReservationPanel extends JPanel {

    private final CustomerRepService service;
    private final DefaultTableModel  tableModel;
    private final JTable             table;
    private       List<Object[]>     currentData;

    // Edit fields
    private JTextField    dateField;
    private JComboBox<String> classBox;
    private JTextField    seatField;
    private JComboBox<String> mealBox;
    private JButton       saveBtn;

    private static final String[] COLS = {
        "Ticket#", "Customer", "Email", "Flight#", "Airline", "Dep Date", "Class", "Seat", "Meal"
    };

    public EditReservationPanel(CustomerRepService service) {
        this.service = service;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search by name / email / ticket#:"));
        JTextField searchField = new JTextField(20);
        JButton    searchBtn   = new JButton("Search");
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        add(searchPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel editForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        editForm.setBorder(BorderFactory.createTitledBorder("Edit Selected Reservation"));
        editForm.add(new JLabel("Dep Date (YYYY-MM-DD):"));
        dateField = new JTextField(10);
        editForm.add(dateField);
        editForm.add(new JLabel("Class:"));
        classBox = new JComboBox<>(new String[]{"economy", "business", "first"});
        editForm.add(classBox);
        editForm.add(new JLabel("Seat:"));
        seatField = new JTextField(5);
        editForm.add(seatField);
        editForm.add(new JLabel("Meal:"));
        mealBox = new JComboBox<>(new String[]{"standard", "vegetarian", "vegan", "halal", "kosher"});
        editForm.add(mealBox);
        saveBtn = new JButton("Save Changes");
        saveBtn.setEnabled(false);
        editForm.add(saveBtn);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(table), editForm);
        split.setResizeWeight(0.65);
        add(split, BorderLayout.CENTER);

        searchBtn.addActionListener(e -> doSearch(searchField.getText().trim()));
        searchField.addActionListener(e -> doSearch(searchField.getText().trim()));

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillEditForm();
        });

        saveBtn.addActionListener(e -> saveChanges());
    }

    private void doSearch(String term) {
        if (term.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter a search term."); return; }
        tableModel.setRowCount(0);
        try {
            currentData = service.searchReservations(term);
            for (Object[] row : currentData) {
                tableModel.addRow(new Object[]{
                    row[0], row[1], row[2], row[3], row[4], row[5], row[6], row[7], row[8]
                });
            }
            if (currentData.isEmpty())
                JOptionPane.showMessageDialog(this, "No active reservations found for: " + term);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
        saveBtn.setEnabled(false);
    }

    private void fillEditForm() {
        int row = table.getSelectedRow();
        if (row < 0 || currentData == null) { saveBtn.setEnabled(false); return; }
        Object[] r = currentData.get(row);
        // r: [ticket_num, cname, email, flight_num, aname, depDate, class, seatNum, mealPref, flightId]
        dateField.setText(r[5] != null ? r[5].toString() : "");
        String cls = (String) r[6];
        for (int i = 0; i < classBox.getItemCount(); i++)
            if (classBox.getItemAt(i).equals(cls)) { classBox.setSelectedIndex(i); break; }
        seatField.setText(r[7] != null ? (String) r[7] : "");
        String meal = (String) r[8];
        for (int i = 0; i < mealBox.getItemCount(); i++)
            if (mealBox.getItemAt(i).equals(meal)) { mealBox.setSelectedIndex(i); break; }
        saveBtn.setEnabled(true);
    }

    private void saveChanges() {
        int row = table.getSelectedRow();
        if (row < 0 || currentData == null) return;
        Object[] r = currentData.get(row);
        int ticketNum = (Integer) r[0];
        int flightId  = (Integer) r[9];

        Date depDate;
        try { depDate = Date.valueOf(dateField.getText().trim()); }
        catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.");
            return;
        }

        try {
            service.updateTicketFlight(ticketNum, flightId, depDate,
                (String) classBox.getSelectedItem(),
                seatField.getText().trim(),
                (String) mealBox.getSelectedItem());
            JOptionPane.showMessageDialog(this, "Reservation updated successfully.");
            // Refresh the row in the table
            currentData.get(row)[5] = depDate;
            currentData.get(row)[6] = classBox.getSelectedItem();
            currentData.get(row)[7] = seatField.getText().trim();
            currentData.get(row)[8] = mealBox.getSelectedItem();
            tableModel.setValueAt(depDate, row, 5);
            tableModel.setValueAt(classBox.getSelectedItem(), row, 6);
            tableModel.setValueAt(seatField.getText().trim(), row, 7);
            tableModel.setValueAt(mealBox.getSelectedItem(), row, 8);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saving: " + ex.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
