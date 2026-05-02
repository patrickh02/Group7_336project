package com.flightreservation.ui;

import com.flightreservation.model.Employee;
import com.flightreservation.service.CustomerRepService;

import javax.swing.*;
import java.awt.*;

public class CustomerRepDashboard extends JFrame {

    public CustomerRepDashboard(Employee rep) {
        setTitle("Customer Rep Dashboard — " + rep.getName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(960, 680);
        setLocationRelativeTo(null);

        CustomerRepService service = new CustomerRepService();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Manage Flights",     new ManageFlightsPanel(service));
        tabs.addTab("Manage Aircrafts",   new ManageAircraftsPanel(service));
        tabs.addTab("Manage Airports",    new ManageAirportsPanel(service));
        tabs.addTab("Make Reservation",   new CustomerRepReservationPanel(service, rep));
        tabs.addTab("Edit Reservation",   new EditReservationPanel(service));
        tabs.addTab("Flight Waitlist",    new WaitlistPanel(service));
        tabs.addTab("Flights by Airport", new FlightsByAirportPanel(service));
        tabs.addTab("Answer Questions",   new QuestionsPanel(service, rep));

        JLabel statusBar = new JLabel("Logged in as: " + rep.getName() + " (" + rep.getEmail() + ")");
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
