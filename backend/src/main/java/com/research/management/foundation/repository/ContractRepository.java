package com.research.management.foundation.repository;

import com.research.management.foundation.entity.ContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractRepository extends JpaRepository<ContractEntity, String> {

    List<ContractEntity> findByProjectId(String projectId);
}

