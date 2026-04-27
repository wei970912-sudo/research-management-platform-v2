package com.research.management.foundation.repository;

import com.research.management.foundation.entity.ControlPointItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ControlPointItemRepository extends JpaRepository<ControlPointItemEntity, String> {

    List<ControlPointItemEntity> findByControlPointId(String controlPointId);
}
