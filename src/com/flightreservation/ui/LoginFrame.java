package com.flightreservation.ui;

import com.flightreservation.model.Customer;
import com.flightreservation.model.Employee;
import com.flightreservation.service.CustomerRepService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class LoginFrame extends JFrame {

    private final CustomerRepService service = new CustomerRepService();

    public LoginFrame() {
        setTitle("Flight Reservation System - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 230);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill   = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Flight Reservation System", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        panel.add(title, g);

        g.gridwidth = 1;
        g.gridy = 1; g.gridx = 0; panel.add(new JLabel("Email:"), g);
        JTextField emailField = new JTextField(18);
        g.gridx = 1; panel.add(emailField, g);

        g.gridy = 2; g.gridx = 0; panel.add(new JLabel("Password:"), g);
        JPasswordField passField = new JPasswordField(18);
        g.gridx = 1; panel.add(passField, g);

        JButton loginBtn = new JButton("Login");
        g.gridy = 3; g.gridx = 0; g.gridwidth = 2;
        panel.add(loginBtn, g);

        add(panel);

        Runnable doLogin = () -> {
            String email = emailField.getText().trim();
            String pass  = new String(passField.getPassword());
            if (email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Please enter email and password.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Employee emp = service.loginEmployee(email, pass);
                if (emp != null) {
                    dispose();
                    if ("rep".equalsIgnoreCase(emp.getType())) {
                        new CustomerRepDashboard(emp).setVisible(true);
                    } else {
                        // Admin placeholder — Person 1 will add admin UI here
                        JOptionPane.showMessageDialog(null,
                            "Welcome, " + emp.getName() + " (Admin).\nAdmin UI is handled by Person 1.",
                            "Admin Login", JOptionPane.INFORMATION_MESSAGE);
                        new LoginFrame().setVisible(true);
                    }
                    return;
                }
                Customer cu = service.loginCustomer(email, pass);
                if (cu != null) {
                    // Customer placeholder — Person 2 will add customer UI here
                    JOptionPane.showMessageDialog(null,
                        "Welcome, " + cu.getName() + " (Customer).\nCustomer UI is handled by Person 2.",
                        "Customer Login", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                JOptionPane.showMessageDialog(this,
                    "Invalid email or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        loginBtn.addActionListener(e -> doLogin.run());
        passField.addActionListener(e -> doLogin.run());
    }
}
