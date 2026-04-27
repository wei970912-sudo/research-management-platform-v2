package com.research.management.foundation.repository;

import com.research.management.foundation.entity.CalendarEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalendarEventRepository extends JpaRepository<CalendarEventEntity, String> {

    List<CalendarEventEntity> findByProjectId(String projectId);
}

