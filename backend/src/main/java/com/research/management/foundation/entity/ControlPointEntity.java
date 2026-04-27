package com.research.management.foundation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rm_control_point")
public class ControlPointEntity {

    @Id
    @Column(length = 32)
    private String id;

    @Column(name = "control_line_id", nullable = false, length = 32)
    private String controlLineId;

    @Column(name = "project_id", nullable = false, length = 32)
    private String projectId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "planned_date", nullable = false, length = 20)
    private String plannedDate;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getControlLineId() {
        return controlLineId;
    }

    public void setControlLineId(String controlLineId) {
        this.controlLineId = controlLineId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(String plannedDate) {
        this.plannedDate = plannedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
