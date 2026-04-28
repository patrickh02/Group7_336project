package com.flightreservation.model;

import java.sql.Timestamp;

public class Question {
    private int questionId;
    private int customerId;
    private Integer repId;
    private String subject;
    private String questionText;
    private String answerText;
    private Timestamp askedDatetime;
    private Timestamp answeredDatetime;

    private String customerName;
    private String repName;

    public Question() {}

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Integer getRepId() {
        return repId;
    }

    public void setRepId(Integer repId) {
        this.repId = repId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public Timestamp getAskedDatetime() {
        return askedDatetime;
    }

    public void setAskedDatetime(Timestamp askedDatetime) {
        this.askedDatetime = askedDatetime;
    }

    public Timestamp getAnsweredDatetime() {
        return answeredDatetime;
    }

    public void setAnsweredDatetime(Timestamp answeredDatetime) {
        this.answeredDatetime = answeredDatetime;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getRepName() {
        return repName;
    }

    public void setRepName(String repName) {
        this.repName = repName;
    }
}
