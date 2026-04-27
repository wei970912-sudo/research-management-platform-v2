package com.research.management.foundation.repository;

import com.research.management.foundation.entity.StakeholderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StakeholderRepository extends JpaRepository<StakeholderEntity, String> {

    List<StakeholderEntity> findByProjectId(String projectId);
}
