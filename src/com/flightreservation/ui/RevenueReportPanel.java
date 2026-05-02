package com.flightreservation.ui;

import com.flightreservation.service.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class RevenueReportPanel extends JPanel {

    public RevenueReportPanel(AdminService service) {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("By Flight",   buildFlightTab(service));
        tabs.addTab("By Airline",  buildAirlineTab(service));
        tabs.addTab("By Customer", buildCustomerTab(service));
        tabs.addTab("Top Customer", buildTopCustomerTab(service));
        tabs.addTab("Most Active Flights", buildActiveFlightsTab(service));
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildFlightTab(AdminService service) {
        String[] cols = {"Flight#", "Airline", "From", "To", "Tickets Sold", "Revenue"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] w = {65, 140, 50, 50, 90, 100};
        for (int i = 0; i < w.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        JButton refreshBtn = new JButton("Refresh");
        JLabel totalLabel  = new JLabel(" ");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refreshBtn); top.add(totalLabel);

        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> {
            model.setRowCount(0);
            try {
                List<Object[]> rows = service.getRevenueByFlight();
                double grand = 0;
                for (Object[] row : rows) {
                    model.addRow(new Object[]{
                        row[0], row[1], row[2], row[3], row[4],
                        String.format("$%.2f", row[5])
                    });
                    grand += (Double) row[5];
                }
                totalLabel.setText(String.format("  Grand total: $%.2f", grand));
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(p, "DB Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        refreshBtn.doClick();
        return p;
    }

    private JPanel buildAirlineTab(AdminService service) {
        String[] cols = {"ID", "Airline", "Tickets Sold", "Revenue"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        JButton refreshBtn = new JButton("Refresh");
        JLabel totalLabel  = new JLabel(" ");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refreshBtn); top.add(totalLabel);

        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> {
            model.setRowCount(0);
            try {
                List<Object[]> rows = service.getRevenueByAirline();
                double grand = 0;
                for (Object[] row : rows) {
                    model.addRow(new Object[]{
                        row[0], row[1], row[2], String.format("$%.2f", row[3])
                    });
                    grand += (Double) row[3];
                }
                totalLabel.setText(String.format("  Grand total: $%.2f", grand));
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(p, "DB Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        refreshBtn.doClick();
        return p;
    }

    private JPanel buildCustomerTab(AdminService service) {
        String[] cols = {"ID", "Name", "Email", "Tickets", "Total Spent"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] w = {50, 140, 180, 60, 100};
        for (int i = 0; i < w.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        JButton refreshBtn = new JButton("Refresh");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refreshBtn);

        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> {
            model.setRowCount(0);
            try {
                for (Object[] row : service.getRevenueByCustomer()) {
                    model.addRow(new Object[]{
                        row[0], row[1], row[2], row[3], String.format("$%.2f", row[4])
                    });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(p, "DB Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        refreshBtn.doClick();
        return p;
    }

    private JPanel buildTopCustomerTab(AdminService service) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(area.getFont().deriveFont(14f));
        JButton refreshBtn = new JButton("Refresh");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refreshBtn);

        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(area), BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> {
            try {
                Object[] row = service.getTopRevenueCustomer();
                if (row == null) {
                    area.setText("No data available.");
                } else {
                    area.setText(String.format(
                        "Top Revenue Customer\n\nName:  %s\nEmail: %s\nTotal Spent: $%.2f",
                        row[0], row[1], row[2]));
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(p, "DB Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        refreshBtn.doClick();
        return p;
    }

    private JPanel buildActiveFlightsTab(AdminService service) {
        String[] cols = {"Flight#", "Airline", "From", "To", "Tickets Sold"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        JButton refreshBtn = new JButton("Refresh");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refreshBtn);

        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> {
            model.setRowCount(0);
            try {
                for (Object[] row : service.getMostActiveFlights()) {
                    model.addRow(new Object[]{ row[0], row[1], row[2], row[3], row[4] });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(p, "DB Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        refreshBtn.doClick();
        return p;
    }
}
