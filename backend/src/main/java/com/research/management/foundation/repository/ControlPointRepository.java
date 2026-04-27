package com.research.management.foundation.repository;

import com.research.management.foundation.entity.ControlPointEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ControlPointRepository extends JpaRepository<ControlPointEntity, String> {

    List<ControlPointEntity> findByProjectId(String projectId);

    List<ControlPointEntity> findByControlLineId(String controlLineId);
}
