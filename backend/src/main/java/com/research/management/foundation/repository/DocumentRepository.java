package com.research.management.foundation.repository;

import com.research.management.foundation.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<DocumentEntity, String> {

    List<DocumentEntity> findByProjectId(String projectId);
}

