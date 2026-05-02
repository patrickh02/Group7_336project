package com.flightreservation.ui;

import com.flightreservation.model.Customer;
import com.flightreservation.model.Employee;
import com.flightreservation.service.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ManageUsersPanel extends JPanel {

    private final AdminService service;

    private final DefaultTableModel customerModel;
    private final JTable customerTable;
    private List<Customer> customers;

    private final DefaultTableModel employeeModel;
    private final JTable employeeTable;
    private List<Employee> employees;

    private static final String[] CUST_COLS = {"ID", "Name", "Email", "Phone", "Address"};
    private static final String[] EMP_COLS  = {"ID", "Name", "Email", "Role"};

    public ManageUsersPanel(AdminService service) {
        this.service = service;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        customerModel = new DefaultTableModel(CUST_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        customerTable = new JTable(customerModel);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        employeeModel = new DefaultTableModel(EMP_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        employeeTable = new JTable(employeeModel);
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Customers",  buildCustomerPanel());
        tabs.addTab("Employees",  buildEmployeePanel());
        add(tabs, BorderLayout.CENTER);

        loadCustomers();
        loadEmployees();
    }

    private JPanel buildCustomerPanel() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.add(new JScrollPane(customerTable), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("Refresh");
        JButton add     = new JButton("Add Customer");
        JButton edit    = new JButton("Edit Selected");
        JButton delete  = new JButton("Delete Selected");
        btns.add(refresh); btns.add(add); btns.add(edit); btns.add(delete);
        p.add(btns, BorderLayout.SOUTH);

        refresh.addActionListener(e -> loadCustomers());
        add.addActionListener(e -> showCustomerDialog(null));
        edit.addActionListener(e -> {
            int row = customerTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a customer first."); return; }
            showCustomerDialog(customers.get(row));
        });
        delete.addActionListener(e -> {
            int row = customerTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a customer first."); return; }
            Customer cu = customers.get(row);
            int ok = JOptionPane.showConfirmDialog(this,
                "Delete customer \"" + cu.getName() + "\"?\nThis will also delete all their tickets.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                try { service.deleteCustomer(cu.getCustomerId()); loadCustomers(); }
                catch (SQLException ex) { showErr(ex); }
            }
        });
        return p;
    }

    private void loadCustomers() {
        customerModel.setRowCount(0);
        try {
            customers = service.getAllCustomers();
            for (Customer cu : customers) {
                customerModel.addRow(new Object[]{
                    cu.getCustomerId(), cu.getName(), cu.getEmail(), cu.getPhone(), cu.getAddress()
                });
            }
        } catch (SQLException ex) { showErr(ex); }
    }

    private void showCustomerDialog(Customer existing) {
        boolean isEdit = (existing != null);
        JTextField nameField  = new JTextField(isEdit ? existing.getName()    : "", 20);
        JTextField emailField = new JTextField(isEdit ? existing.getEmail()   : "", 20);
        JTextField phoneField = new JTextField(isEdit ? existing.getPhone()   : "", 15);
        JTextField addrField  = new JTextField(isEdit ? existing.getAddress() : "", 25);
        JPasswordField passField = new JPasswordField(20);

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Name:"));    form.add(nameField);
        form.add(new JLabel("Email:"));   form.add(emailField);
        form.add(new JLabel("Phone:"));   form.add(phoneField);
        form.add(new JLabel("Address:")); form.add(addrField);
        if (!isEdit) {
            form.add(new JLabel("Password:")); form.add(passField);
        }

        int result = JOptionPane.showConfirmDialog(this, form,
            isEdit ? "Edit Customer" : "Add Customer", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        if (nameField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and email are required.");
            return;
        }

        try {
            Customer cu = isEdit ? existing : new Customer();
            cu.setName(nameField.getText().trim());
            cu.setEmail(emailField.getText().trim());
            cu.setPhone(phoneField.getText().trim());
            cu.setAddress(addrField.getText().trim());
            if (!isEdit) cu.setPassword(new String(passField.getPassword()));

            if (isEdit) service.updateCustomer(cu);
            else        service.addCustomer(cu);
            loadCustomers();
        } catch (SQLException ex) { showErr(ex); }
    }

    private JPanel buildEmployeePanel() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.add(new JScrollPane(employeeTable), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("Refresh");
        JButton add     = new JButton("Add Employee");
        JButton edit    = new JButton("Edit Selected");
        JButton delete  = new JButton("Delete Selected");
        btns.add(refresh); btns.add(add); btns.add(edit); btns.add(delete);
        p.add(btns, BorderLayout.SOUTH);

        refresh.addActionListener(e -> loadEmployees());
        add.addActionListener(e -> showEmployeeDialog(null));
        edit.addActionListener(e -> {
            int row = employeeTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an employee first."); return; }
            showEmployeeDialog(employees.get(row));
        });
        delete.addActionListener(e -> {
            int row = employeeTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an employee first."); return; }
            Employee emp = employees.get(row);
            int ok = JOptionPane.showConfirmDialog(this,
                "Delete employee \"" + emp.getName() + "\" (" + emp.getType() + ")?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                try { service.deleteEmployee(emp.getEmployeeId()); loadEmployees(); }
                catch (SQLException ex) { showErr(ex); }
            }
        });
        return p;
    }

    private void loadEmployees() {
        employeeModel.setRowCount(0);
        try {
            employees = service.getAllEmployees();
            for (Employee e : employees) {
                employeeModel.addRow(new Object[]{
                    e.getEmployeeId(), e.getName(), e.getEmail(), e.getType()
                });
            }
        } catch (SQLException ex) { showErr(ex); }
    }

    private void showEmployeeDialog(Employee existing) {
        boolean isEdit = (existing != null);
        JTextField nameField  = new JTextField(isEdit ? existing.getName()  : "", 20);
        JTextField emailField = new JTextField(isEdit ? existing.getEmail() : "", 20);
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"rep", "admin"});
        if (isEdit) typeBox.setSelectedItem(existing.getType());
        JPasswordField passField = new JPasswordField(20);

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Name:"));  form.add(nameField);
        form.add(new JLabel("Email:")); form.add(emailField);
        form.add(new JLabel("Role:"));  form.add(typeBox);
        if (!isEdit) {
            form.add(new JLabel("Password:")); form.add(passField);
        }

        int result = JOptionPane.showConfirmDialog(this, form,
            isEdit ? "Edit Employee" : "Add Employee", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        if (nameField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and email are required.");
            return;
        }

        try {
            Employee emp = isEdit ? existing : new Employee();
            emp.setName(nameField.getText().trim());
            emp.setEmail(emailField.getText().trim());
            emp.setType((String) typeBox.getSelectedItem());
            if (!isEdit) emp.setPassword(new String(passField.getPassword()));

            if (isEdit) service.updateEmployee(emp);
            else        service.addEmployee(emp);
            loadEmployees();
        } catch (SQLException ex) { showErr(ex); }
    }

    private void showErr(SQLException ex) {
        JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}
