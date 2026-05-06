package com.flightreservation.ui.customer;

import com.flightreservation.model.Customer;
import com.flightreservation.service.CustomerService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class AskQuestionPanel extends JPanel {

    private final CustomerService service;
    private final Customer        customer;
    private DefaultTableModel     tableModel;

    private static final String[] COLS = {
        "ID", "Subject", "Asked", "Status", "Answered"
    };

    public AskQuestionPanel(CustomerService service, Customer customer) {
        this.service  = service;
        this.customer = customer;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JPanel submitPanel = new JPanel(new BorderLayout(6, 6));
        submitPanel.setBorder(BorderFactory.createTitledBorder("Ask a New Question"));

        JTextField subjectField = new JTextField(30);
        JTextArea  questionArea = new JTextArea(4, 50);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);

        JPanel subjectRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        subjectRow.add(new JLabel("Subject:"));
        subjectRow.add(subjectField);
        submitPanel.add(subjectRow, BorderLayout.NORTH);
        submitPanel.add(new JScrollPane(questionArea), BorderLayout.CENTER);

        JButton submitBtn = new JButton("Submit Question");
        submitBtn.setFont(submitBtn.getFont().deriveFont(Font.BOLD));
        JPanel submitBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        submitBtnRow.add(submitBtn);
        submitPanel.add(submitBtnRow, BorderLayout.SOUTH);

        JPanel historyPanel = new JPanel(new BorderLayout(5, 5));
        historyPanel.setBorder(BorderFactory.createTitledBorder("My Questions & Answers"));

        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(140);

        JTextArea answerArea = new JTextArea(5, 50);
        answerArea.setEditable(false);
        answerArea.setLineWrap(true);
        answerArea.setWrapStyleWord(true);
        answerArea.setBackground(new Color(245, 250, 255));
        answerArea.setFont(answerArea.getFont().deriveFont(12f));
        JPanel answerWrap = new JPanel(new BorderLayout());
        answerWrap.setBorder(BorderFactory.createTitledBorder("Answer from Support"));
        answerWrap.add(new JScrollPane(answerArea));

        JButton refreshBtn = new JButton("Refresh");
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRow.add(refreshBtn);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(table), answerWrap);
        split.setResizeWeight(0.55);

        historyPanel.add(btnRow, BorderLayout.NORTH);
        historyPanel.add(split,  BorderLayout.CENTER);

        // ── Layout ────────────────────────────────────────────────────────
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            submitPanel, historyPanel);
        mainSplit.setResizeWeight(0.35);
        add(mainSplit, BorderLayout.CENTER);

        // ── Wire up ───────────────────────────────────────────────────────
        submitBtn.addActionListener(e -> {
            String subject  = subjectField.getText().trim();
            String question = questionArea.getText().trim();
            if (subject.isEmpty() || question.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Please fill in both the subject and your question.");
                return;
            }
            try {
                service.submitQuestion(customer.getCustomerId(), subject, question);
                JOptionPane.showMessageDialog(this,
                    "Question submitted! A representative will reply soon.",
                    "Submitted", JOptionPane.INFORMATION_MESSAGE);
                subjectField.setText("");
                questionArea.setText("");
                loadHistory(table, answerArea);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        refreshBtn.addActionListener(e -> loadHistory(table, answerArea));

        loadHistory(table, answerArea);

        table.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting()) {
                int sel = table.getSelectedRow();
                if (sel < 0 || loadedQuestions == null || sel >= loadedQuestions.size()) {
                    answerArea.setText("");
                    return;
                }
                Object[] r = loadedQuestions.get(sel);
                String qText = (String) r[2];
                String aText = (String) r[3];
                StringBuilder sb = new StringBuilder();
                sb.append("Your question:\n").append(qText != null ? qText : "").append("\n\n");
                if (aText != null && !aText.isEmpty()) {
                    sb.append("Answer from support:\n").append(aText);
                } else {
                    sb.append("No answer yet. Please check back later.");
                }
                answerArea.setText(sb.toString());
                answerArea.setCaretPosition(0);
            }
        });
    }

    private List<Object[]> loadedQuestions;

    private void loadHistory(JTable table, JTextArea answerArea) {
        tableModel.setRowCount(0);
        answerArea.setText("");
        try {
            loadedQuestions = service.getMyQuestions(customer.getCustomerId());
            for (Object[] r : loadedQuestions) {
                // r: questionId, subject, questionText, answerText,
                //    askedDatetime, answeredDatetime
                String status = (r[3] != null) ? "Answered" : "Awaiting reply";
                tableModel.addRow(new Object[]{
                    r[0],   // ID
                    r[1],   // Subject
                    r[4],   // askedDatetime
                    status,
                    r[5] != null ? r[5].toString() : "—"  // answeredDatetime
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
