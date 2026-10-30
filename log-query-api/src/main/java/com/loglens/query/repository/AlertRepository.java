package com.loglens.query.repository;

import com.loglens.query.entity.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<AlertEntity, UUID> {
    List<AlertEntity> findByAcknowledgedFalseAndSeverityOrderByCreatedAtDesc(String severity);
    List<AlertEntity> findByAcknowledgedFalseOrderByCreatedAtDesc();
    List<AlertEntity> findBySeverityOrderByCreatedAtDesc(String severity);
}
