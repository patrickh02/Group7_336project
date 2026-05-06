package com.flightreservation.ui.customer;

import com.flightreservation.model.Customer;
import com.flightreservation.service.CustomerService;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ReservationsPanel extends JPanel {

    private final CustomerService service;
    private final Customer        customer;

    private DefaultTableModel upcomingModel;
    private JTable            upcomingTable;
    private List<Object[]>    upcomingData;

    private DefaultTableModel pastModel;
    private JTable            pastTable;

    private static final String[] COLS = {
        "Ticket#", "Flight#", "Airline", "From", "To",
        "Date", "Class", "Seat", "Meal", "Total Fare", "Status"
    };

    public ReservationsPanel(CustomerService service, Customer customer) {
        this.service  = service;
        this.customer = customer;
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("✈  Upcoming Trips", buildUpcomingTab());
        tabs.addTab("🕓  Past Trips",     buildPastTab());
        add(tabs, BorderLayout.CENTER);

        loadUpcoming();
        loadPast();
    }

    private JPanel buildUpcomingTab() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        upcomingModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        upcomingTable = new JTable(upcomingModel);
        upcomingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleTable(upcomingTable);

        p.add(new JScrollPane(upcomingTable), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        JButton refreshBtn = new JButton("Refresh");
        JButton cancelBtn  = new JButton("Cancel Selected Ticket");
        cancelBtn.setForeground(Color.RED);
        JLabel note = new JLabel("  (Economy: $50 cancellation fee | Business/First: no fee)");
        note.setFont(note.getFont().deriveFont(Font.ITALIC, 11f));
        note.setForeground(Color.GRAY);

        btns.add(refreshBtn);
        btns.add(cancelBtn);
        btns.add(note);
        p.add(btns, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadUpcoming());
        cancelBtn.addActionListener(e -> cancelSelected());
        return p;
    }

    private void loadUpcoming() {
        upcomingModel.setRowCount(0);
        try {
            upcomingData = service.getUpcomingReservations(customer.getCustomerId());
            for (Object[] r : upcomingData) {
                upcomingModel.addRow(buildRow(r));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelSelected() {
        int row = upcomingTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a reservation to cancel.");
            return;
        }
        Object[] r = upcomingData.get(row);
        int    ticketNum = (Integer) r[0];
        String cls       = (String)  r[9];
        int    flightId  = (Integer) r[15];
        java.sql.Date depDate = (java.sql.Date) r[8];

        String confirmMsg;
        if ("economy".equalsIgnoreCase(cls)) {
            confirmMsg = "Economy tickets require a $50 cancellation fee.\n" +
                         "Cancel Ticket #" + ticketNum + " for a $50 fee?";
        } else {
            confirmMsg = "Cancel Ticket #" + ticketNum + " (" + cls + " class) with no fee?\n" +
                         "This action cannot be undone.";
        }

        int confirm = JOptionPane.showConfirmDialog(this, confirmMsg,
            "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.cancelTicket(ticketNum);
                String cancelMsg = "Ticket #" + ticketNum + " has been cancelled.";
                if ("economy".equalsIgnoreCase(cls)) {
                    cancelMsg += "\nA $50 cancellation fee has been applied.";
                }

                // Notify first waitlisted customer if a seat opened up
                Object[] waitlisted = service.getFirstWaitlisted(flightId, depDate);
                if (waitlisted != null) {
                    String custName  = (String) waitlisted[1];
                    String custEmail = (String) waitlisted[2];
                    int    custId    = (Integer) waitlisted[0];
                    service.removeFromWaitlist(custId, flightId, depDate);
                    cancelMsg += "\n\nWaitlist Alert: " + custName + " (" + custEmail + ")\n" +
                                 "has been notified that a seat is now available.";
                }

                JOptionPane.showMessageDialog(this, cancelMsg, "Cancelled",
                    JOptionPane.INFORMATION_MESSAGE);
                loadUpcoming();
                loadPast();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Past ──────────────────────────────────────────────────────────────────

    private JPanel buildPastTab() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        pastModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        pastTable = new JTable(pastModel);
        pastTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleTable(pastTable);

        p.add(new JScrollPane(pastTable), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh");
        btns.add(refreshBtn);
        p.add(btns, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadPast());
        return p;
    }

    private void loadPast() {
        pastModel.setRowCount(0);
        try {
            List<Object[]> data = service.getPastReservations(customer.getCustomerId());
            for (Object[] r : data) {
                pastModel.addRow(buildRow(r));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Object[] buildRow(Object[] r) {
        // r: ticketNum, flightNum, airlineId, airlineName, depAirportId, arrAirportId,
        //    depCity, arrCity, depDate, class, seatNum, mealPref,
        //    totalFare, status, ticketType, flightId
        return new Object[]{
            r[0],  // ticket#
            r[1],  // flight#
            r[3],  // airlineName
            r[4] + " (" + r[6] + ")",  // from
            r[5] + " (" + r[7] + ")",  // to
            r[8],  // date
            r[9],  // class
            r[10] != null ? r[10] : "—",  // seat
            r[11] != null ? r[11] : "standard",  // meal
            String.format("$%.2f", r[12]),  // fare
            r[13]  // status
        };
    }

    private void styleTable(JTable t) {
        t.setRowHeight(22);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {65, 65, 130, 120, 120, 95, 75, 55, 80, 80, 75};
        for (int i = 0; i < widths.length && i < t.getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Colour cancelled rows
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component comp = super.getTableCellRendererComponent(tbl, val, sel, focus, row, col);
                if (!sel) {
                    Object statusVal = tbl.getModel().getValueAt(row, 10); // Status column
                    if ("cancelled".equals(statusVal)) {
                        comp.setForeground(Color.GRAY);
                        comp.setBackground(new Color(245, 245, 245));
                    } else {
                        comp.setForeground(Color.BLACK);
                        comp.setBackground(Color.WHITE);
                    }
                }
                return comp;
            }
        });
    }
}
