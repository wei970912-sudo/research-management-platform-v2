package com.research.management.foundation.repository;

import com.research.management.foundation.entity.TaskBookItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskBookItemRepository extends JpaRepository<TaskBookItemEntity, String> {

    List<TaskBookItemEntity> findByProjectId(String projectId);

    Optional<TaskBookItemEntity> findByProjectIdAndNameAndBudget(String projectId, String name, java.math.BigDecimal budget);
}
