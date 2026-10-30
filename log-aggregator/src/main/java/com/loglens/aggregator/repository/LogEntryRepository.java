package com.loglens.aggregator.repository;

import com.loglens.aggregator.entity.LogEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogEntryRepository extends JpaRepository<LogEntryEntity, String> {
}
