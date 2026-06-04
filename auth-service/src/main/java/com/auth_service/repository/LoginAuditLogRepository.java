package com.auth_service.repository;

import com.auth_service.entity.LoginAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAuditLogRepository extends JpaRepository<LoginAuditLog, String> {

    Page<LoginAuditLog> findAllByUserId(String userId, Pageable pageable);

    Page<LoginAuditLog> findAllByEventType(String eventType, Pageable pageable);

    Page<LoginAuditLog> findAllByIpAddress(String ipAddress, Pageable pageable);
}
