package com.loglens.query.repository;

import com.loglens.query.entity.LogEntryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface LogEntryRepository extends JpaRepository<LogEntryEntity, String> {

    Page<LogEntryEntity> findByServiceNameAndLevelAndTimestampBetween(
            String serviceName, String level, Instant from, Instant to, Pageable pageable);

    Page<LogEntryEntity> findByServiceName(String serviceName, Pageable pageable);

    Page<LogEntryEntity> findByLevel(String level, Pageable pageable);

    Page<LogEntryEntity> findByServiceNameAndLevel(String serviceName, String level, Pageable pageable);

    @Query("SELECT DISTINCT e.serviceName FROM LogEntryEntity e")
    List<String> findDistinctServiceNames();

    @Query("SELECT e.serviceName, e.level, COUNT(e) FROM LogEntryEntity e WHERE e.createdAt > :since GROUP BY e.serviceName, e.level")
    List<Object[]> countByServiceAndLevelSince(Instant since);
}
