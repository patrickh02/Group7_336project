package com.flightreservation.ui.customer;

import com.flightreservation.model.Customer;
import com.flightreservation.service.CustomerService;
import com.flightreservation.ui.LoginFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Main window displayed after a customer logs in.
 * Hosts tabs for flight search, reservations, and support.
 */
public class CustomerDashboard extends JFrame {

    public CustomerDashboard(Customer customer) {
        setTitle("Flight Reservation — " + customer.getName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1050, 720);
        setLocationRelativeTo(null);

        CustomerService service = new CustomerService();

        // ── Tab pane ──────────────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();

        FlightSearchPanel searchPanel = new FlightSearchPanel(service, customer);
        tabs.addTab("Search Flights",     searchPanel);
        tabs.addTab("My Reservations",    new ReservationsPanel(service, customer));
        tabs.addTab("Ask a Question",     new AskQuestionPanel(service, customer));
        tabs.addTab("Browse Q&A",         new BrowseQAPanel(service));

        // ── Status bar ────────────────────────────────────────────────────
        JLabel statusBar = new JLabel(
            "  Logged in as: " + customer.getName() + " (" + customer.getEmail() + ")");
        statusBar.setFont(statusBar.getFont().deriveFont(12f));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEtchedBorder());
        bottom.add(statusBar, BorderLayout.WEST);
        bottom.add(logoutBtn, BorderLayout.EAST);

        // ── Welcome banner ────────────────────────────────────────────────
        JLabel welcome = new JLabel(
            "  Welcome back, " + customer.getName() + "!   Search flights, view reservations, ask a question, or browse Q&A.",
            SwingConstants.LEFT);
        welcome.setFont(welcome.getFont().deriveFont(Font.ITALIC, 13f));
        welcome.setForeground(new Color(60, 100, 170));
        welcome.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        setLayout(new BorderLayout());
        add(welcome, BorderLayout.NORTH);
        add(tabs,    BorderLayout.CENTER);
        add(bottom,  BorderLayout.SOUTH);
    }
}
