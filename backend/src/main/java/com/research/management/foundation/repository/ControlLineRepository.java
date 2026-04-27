package com.research.management.foundation.repository;

import com.research.management.foundation.entity.ControlLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ControlLineRepository extends JpaRepository<ControlLineEntity, String> {

    List<ControlLineEntity> findByProjectId(String projectId);
}
