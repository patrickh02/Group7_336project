package com.flightreservation.ui;

import com.flightreservation.model.Employee;
import com.flightreservation.service.CustomerRepService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class QuestionsPanel extends JPanel {

    private final CustomerRepService service;
    private final Employee           rep;
    private final DefaultTableModel  tableModel;
    private final JTable             table;
    private       List<Object[]>     currentData;

    private JTextArea questionArea;
    private JTextArea answerArea;
    private JLabel    statusLabel;
    private JButton   saveBtn;

    private static final String[] COLS = {
        "ID", "Customer", "Subject", "Asked", "Status"
    };

    public QuestionsPanel(CustomerRepService service, Employee rep) {
        this.service = service;
        this.rep     = rep;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(0, 200));

        JPanel detailPanel = new JPanel(new BorderLayout(5, 5));
        detailPanel.setBorder(BorderFactory.createTitledBorder("Question Detail & Answer"));

        questionArea = new JTextArea(4, 50);
        questionArea.setEditable(false);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setBackground(new Color(245, 245, 245));
        detailPanel.add(new JScrollPane(questionArea), BorderLayout.NORTH);

        answerArea = new JTextArea(5, 50);
        answerArea.setLineWrap(true);
        answerArea.setWrapStyleWord(true);
        JPanel answerWrap = new JPanel(new BorderLayout());
        answerWrap.setBorder(BorderFactory.createTitledBorder("Your Answer"));
        answerWrap.add(new JScrollPane(answerArea));
        detailPanel.add(answerWrap, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh Questions");
        saveBtn = new JButton("Save Answer");
        saveBtn.setEnabled(false);
        statusLabel = new JLabel(" ");
        btnRow.add(refreshBtn);
        btnRow.add(saveBtn);
        btnRow.add(statusLabel);
        detailPanel.add(btnRow, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, detailPanel);
        split.setResizeWeight(0.4);
        add(split, BorderLayout.CENTER);

        // Wire up
        refreshBtn.addActionListener(e -> loadData());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillDetail();
        });

        saveBtn.addActionListener(e -> saveAnswer());

        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            currentData = service.getAllQuestions();
            for (Object[] row : currentData) {
                // row: questionId, customerName, subject, questionText, answerText,
                //      askedDatetime, answeredDatetime
                String status = (row[6] != null) ? "Answered" : "Unanswered";
                tableModel.addRow(new Object[]{
                    row[0], row[1], row[2], row[5], status
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading questions: " + ex.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
        questionArea.setText("");
        answerArea.setText("");
        saveBtn.setEnabled(false);
        statusLabel.setText(" ");
    }

    private void fillDetail() {
        int row = table.getSelectedRow();
        if (row < 0 || currentData == null) { saveBtn.setEnabled(false); return; }
        Object[] r = currentData.get(row);
        // Build question display
        questionArea.setText(
            "From: " + r[1] + "\nSubject: " + r[2] + "\n\n" + r[3]);
        // Pre-fill existing answer if any
        answerArea.setText(r[4] != null ? (String) r[4] : "");
        boolean alreadyAnswered = (r[6] != null);
        saveBtn.setEnabled(true);
        statusLabel.setText(alreadyAnswered ? "  (already answered — you can update)" : "  (unanswered)");
        statusLabel.setForeground(alreadyAnswered ? Color.GRAY : new Color(180, 0, 0));
    }

    private void saveAnswer() {
        int row = table.getSelectedRow();
        if (row < 0 || currentData == null) return;
        String answer = answerArea.getText().trim();
        if (answer.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please type an answer before saving.");
            return;
        }
        int questionId = (Integer) currentData.get(row)[0];
        try {
            service.answerQuestion(questionId, rep.getEmployeeId(), answer);
            JOptionPane.showMessageDialog(this, "Answer saved successfully.");
            loadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saving answer: " + ex.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
