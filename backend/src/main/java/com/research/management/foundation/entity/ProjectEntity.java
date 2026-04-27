package com.research.management.foundation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rm_project")
public class ProjectEntity {

    @Id
    @Column(length = 32)
    private String id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 100)
    private String owner;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "current_stage", nullable = false, length = 100)
    private String currentStage;

    @Column(name = "milestone_completion_rate", nullable = false)
    private Integer milestoneCompletionRate;

    @Column(nullable = false, length = 2000)
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(String currentStage) {
        this.currentStage = currentStage;
    }

    public Integer getMilestoneCompletionRate() {
        return milestoneCompletionRate;
    }

    public void setMilestoneCompletionRate(Integer milestoneCompletionRate) {
        this.milestoneCompletionRate = milestoneCompletionRate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

