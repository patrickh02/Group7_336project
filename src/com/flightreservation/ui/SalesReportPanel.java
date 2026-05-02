package com.flightreservation.ui;

import com.flightreservation.service.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.Year;
import java.util.List;

public class SalesReportPanel extends JPanel {

    private final DefaultTableModel tableModel;

    private static final String[] COLS = {
        "Ticket#", "Customer", "Type", "Total Fare", "Booking Fee", "Purchased", "Status"
    };

    public SalesReportPanel(AdminService service) {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {65, 140, 80, 85, 85, 160, 70};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        int currentYear  = Year.now().getValue();
        int currentMonth = java.time.LocalDate.now().getMonthValue();

        String[] months = {
            "January","February","March","April","May","June",
            "July","August","September","October","November","December"
        };
        JComboBox<String> monthBox = new JComboBox<>(months);
        monthBox.setSelectedIndex(currentMonth - 1);

        SpinnerNumberModel yearModel = new SpinnerNumberModel(currentYear, 2000, 2100, 1);
        JSpinner yearSpinner = new JSpinner(yearModel);
        ((JSpinner.DefaultEditor) yearSpinner.getEditor()).getTextField().setColumns(5);

        JButton runBtn = new JButton("Run Report");
        JLabel summaryLabel = new JLabel(" ");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Month:"));  top.add(monthBox);
        top.add(new JLabel("Year:"));   top.add(yearSpinner);
        top.add(runBtn);
        top.add(summaryLabel);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        runBtn.addActionListener(e -> {
            int month = monthBox.getSelectedIndex() + 1;
            int year  = (Integer) yearSpinner.getValue();
            tableModel.setRowCount(0);
            try {
                List<Object[]> rows = service.getSalesReport(year, month);
                double total = 0;
                int active = 0;
                for (Object[] row : rows) {
                    tableModel.addRow(new Object[]{
                        row[0],
                        row[1],
                        row[2],
                        String.format("$%.2f", row[3]),
                        String.format("$%.2f", row[4]),
                        row[5],
                        row[6]
                    });
                    if ("active".equals(row[6])) {
                        total += (Double) row[3];
                        active++;
                    }
                }
                summaryLabel.setText(String.format(
                    "  %d tickets (%d active) | Revenue: $%.2f", rows.size(), active, total));
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        runBtn.doClick();
    }
}
