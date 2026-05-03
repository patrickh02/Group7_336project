package com.flightreservation.ui.customer;

import com.flightreservation.service.CustomerService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class BrowseQAPanel extends JPanel {

    private final CustomerService  service;
    private DefaultTableModel      tableModel;
    private List<Object[]>         loadedQA;

    private static final String[] COLS = {"ID", "Subject", "Answered On"};

    public BrowseQAPanel(CustomerService service) {
        this.service = service;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchBar.setBorder(BorderFactory.createTitledBorder("Browse Answered Questions & Answers"));
        JTextField keywordField = new JTextField(22);
        JButton searchBtn = new JButton("Search");
        JButton showAllBtn = new JButton("Show All");
        searchBar.add(new JLabel("Keyword:"));
        searchBar.add(keywordField);
        searchBar.add(searchBtn);
        searchBar.add(showAllBtn);
        add(searchBar, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(22);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(280);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);

        // Detail area
        JTextArea detailArea = new JTextArea(9, 50);
        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setBackground(new Color(248, 252, 255));
        detailArea.setFont(detailArea.getFont().deriveFont(12f));
        JPanel detailWrap = new JPanel(new BorderLayout());
        detailWrap.setBorder(BorderFactory.createTitledBorder("Question & Answer"));
        detailWrap.add(new JScrollPane(detailArea));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(table), detailWrap);
        split.setResizeWeight(0.45);
        add(split, BorderLayout.CENTER);

        // Wire up
        searchBtn.addActionListener(e -> loadQA(keywordField.getText().trim()));
        showAllBtn.addActionListener(e -> { keywordField.setText(""); loadQA(""); });
        keywordField.addActionListener(e -> loadQA(keywordField.getText().trim()));

        table.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting()) {
                int sel = table.getSelectedRow();
                if (sel < 0 || loadedQA == null || sel >= loadedQA.size()) {
                    detailArea.setText("");
                    return;
                }
                Object[] r = loadedQA.get(sel);
                String q = (String) r[2];
                String a = (String) r[3];
                detailArea.setText("Q: " + (q != null ? q : "") +
                    "\n\nA: " + (a != null ? a : "No answer yet."));
                detailArea.setCaretPosition(0);
            }
        });

        loadQA("");
    }

    private void loadQA(String keyword) {
        tableModel.setRowCount(0);
        try {
            loadedQA = service.getAnsweredQuestions(keyword);
            for (Object[] r : loadedQA) {
                tableModel.addRow(new Object[]{
                    r[0],
                    r[1],
                    r[5] != null ? r[5].toString() : ""
                });
            }
            if (loadedQA.isEmpty()) {
                tableModel.addRow(new Object[]{"", "No answered questions found.", ""});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
