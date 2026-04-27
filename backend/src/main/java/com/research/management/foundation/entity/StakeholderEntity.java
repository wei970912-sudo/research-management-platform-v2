package com.research.management.foundation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rm_stakeholder")
public class StakeholderEntity {

    @Id
    @Column(length = 32)
    private String id;

    @Column(name = "project_id", nullable = false, length = 32)
    private String projectId;

    @Column(name = "stakeholder_name", nullable = false, length = 100)
    private String stakeholderName;

    @Column(name = "related_work_type", nullable = false, length = 30)
    private String relatedWorkType;

    @Column(name = "related_work_value", nullable = false, length = 400)
    private String relatedWorkValue;

    @Column(name = "main_content", nullable = false, length = 2000)
    private String mainContent;

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

    public String getStakeholderName() {
        return stakeholderName;
    }

    public void setStakeholderName(String stakeholderName) {
        this.stakeholderName = stakeholderName;
    }

    public String getRelatedWorkType() {
        return relatedWorkType;
    }

    public void setRelatedWorkType(String relatedWorkType) {
        this.relatedWorkType = relatedWorkType;
    }

    public String getRelatedWorkValue() {
        return relatedWorkValue;
    }

    public void setRelatedWorkValue(String relatedWorkValue) {
        this.relatedWorkValue = relatedWorkValue;
    }

    public String getMainContent() {
        return mainContent;
    }

    public void setMainContent(String mainContent) {
        this.mainContent = mainContent;
    }
}
