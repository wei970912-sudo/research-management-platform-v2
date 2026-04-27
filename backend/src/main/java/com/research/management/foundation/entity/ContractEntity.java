package com.research.management.foundation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "rm_contract")
public class ContractEntity {

    @Id
    @Column(length = 32)
    private String id;

    @Column(name = "project_id", nullable = false, length = 32)
    private String projectId;

    @Column(name = "task_book_item_id", length = 32)
    private String taskBookItemId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "task_name", nullable = false, length = 200)
    private String taskName;

    @Column(name = "task_budget", nullable = false, precision = 18, scale = 2)
    private BigDecimal taskBudget;

    @Column(name = "signing_unit", nullable = false, length = 200)
    private String signingUnit;

    @Column(name = "signing_date", nullable = false, length = 20)
    private String signingDate;

    @Column(name = "delivery_date", nullable = false, length = 20)
    private String deliveryDate;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(name = "delivery_terms", nullable = false, length = 2000)
    private String deliveryTerms;

    @Column(name = "payment_terms", nullable = false, length = 2000)
    private String paymentTerms;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getTaskBookItemId() {
        return taskBookItemId;
    }

    public void setTaskBookItemId(String taskBookItemId) {
        this.taskBookItemId = taskBookItemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public BigDecimal getTaskBudget() {
        return taskBudget;
    }

    public void setTaskBudget(BigDecimal taskBudget) {
        this.taskBudget = taskBudget;
    }

    public String getSigningUnit() {
        return signingUnit;
    }

    public void setSigningUnit(String signingUnit) {
        this.signingUnit = signingUnit;
    }

    public String getSigningDate() {
        return signingDate;
    }

    public void setSigningDate(String signingDate) {
        this.signingDate = signingDate;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDeliveryTerms() {
        return deliveryTerms;
    }

    public void setDeliveryTerms(String deliveryTerms) {
        this.deliveryTerms = deliveryTerms;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }
}
