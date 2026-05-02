package com.flightreservation.ui;

import com.flightreservation.model.Employee;
import com.flightreservation.service.AdminService;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {

    public AdminDashboard(Employee admin) {
        setTitle("Admin Dashboard — " + admin.getName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        AdminService service = new AdminService();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Manage Users",       new ManageUsersPanel(service));
        tabs.addTab("Sales Report",        new SalesReportPanel(service));
        tabs.addTab("Reservations",        new ReservationsReportPanel(service));
        tabs.addTab("Revenue Reports",     new RevenueReportPanel(service));

        JLabel statusBar = new JLabel("Logged in as: " + admin.getName() + " (" + admin.getEmail() + ")");
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(statusBar, BorderLayout.WEST);
        bottom.add(logoutBtn, BorderLayout.EAST);
        bottom.setBorder(BorderFactory.createEtchedBorder());

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }
}
