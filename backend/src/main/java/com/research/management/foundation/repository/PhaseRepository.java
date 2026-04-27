package com.research.management.foundation.repository;

import com.research.management.foundation.entity.PhaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhaseRepository extends JpaRepository<PhaseEntity, String> {

    List<PhaseEntity> findByProjectId(String projectId);
}

