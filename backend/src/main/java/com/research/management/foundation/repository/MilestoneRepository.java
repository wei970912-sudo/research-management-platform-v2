package com.research.management.foundation.repository;

import com.research.management.foundation.entity.MilestoneEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MilestoneRepository extends JpaRepository<MilestoneEntity, String> {

    List<MilestoneEntity> findByProjectId(String projectId);
}

