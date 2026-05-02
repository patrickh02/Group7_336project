package com.flightreservation.ui;

import com.flightreservation.service.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ReservationsReportPanel extends JPanel {

    private final DefaultTableModel tableModel;

    private static final String[] COLS = {
        "Ticket#", "Customer", "Email", "Flight#", "Airline",
        "From", "To", "Dep Date", "Class", "Seat", "Total Fare", "Status"
    };

    public ReservationsReportPanel(AdminService service) {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {60, 120, 150, 65, 120, 50, 50, 90, 70, 50, 80, 70};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JRadioButton byFlight   = new JRadioButton("By Flight Number", true);
        JRadioButton byCustomer = new JRadioButton("By Customer Name");
        ButtonGroup group = new ButtonGroup();
        group.add(byFlight); group.add(byCustomer);

        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        JLabel countLabel = new JLabel(" ");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(byFlight); top.add(byCustomer);
        top.add(new JLabel("Search:"));
        top.add(searchField);
        top.add(searchBtn);
        top.add(countLabel);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable doSearch = () -> {
            String term = searchField.getText().trim();
            if (term.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a search term.");
                return;
            }
            tableModel.setRowCount(0);
            try {
                List<Object[]> rows = byFlight.isSelected()
                    ? service.getReservationsByFlight(term)
                    : service.getReservationsByCustomer(term);
                for (Object[] row : rows) {
                    tableModel.addRow(new Object[]{
                        row[0], row[1], row[2], row[3], row[4],
                        row[5], row[6], row[7], row[8], row[9],
                        String.format("$%.2f", row[10]), row[11]
                    });
                }
                countLabel.setText("  " + rows.size() + " result(s)");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        searchBtn.addActionListener(e -> doSearch.run());
        searchField.addActionListener(e -> doSearch.run());
    }
}
