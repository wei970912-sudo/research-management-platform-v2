package com.research.management.foundation.repository;

import com.research.management.foundation.entity.MeetingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingRepository extends JpaRepository<MeetingEntity, String> {

    List<MeetingEntity> findByProjectId(String projectId);
}

