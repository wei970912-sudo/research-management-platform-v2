package com.research.management.foundation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rm_control_point_item")
public class ControlPointItemEntity {

    @Id
    @Column(length = 32)
    private String id;

    @Column(name = "control_point_id", nullable = false, length = 32)
    private String controlPointId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private Boolean completed;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getControlPointId() {
        return controlPointId;
    }

    public void setControlPointId(String controlPointId) {
        this.controlPointId = controlPointId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
