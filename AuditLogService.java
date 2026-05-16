package com.bloodbank.service;

import com.bloodbank.entity.AuditLog;
import com.bloodbank.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for recording and querying audit log entries.
 */
@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Records an audit event.
     *
     * @param username   who performed the action
     * @param role       their role
     * @param action     action name (e.g. LOGIN, APPROVE_REQUEST, DELETE_DONOR)
     * @param entityType entity type (e.g. BloodRequest, Donor)
     * @param entityId   entity primary key (nullable)
     * @param details    human-readable description
     */
    public void log(String username, String role, String action,
                    String entityType, Long entityId, String details) {
        AuditLog entry = new AuditLog();
        entry.setUsername(username != null ? username : "SYSTEM");
        entry.setRole(role != null ? role : "SYSTEM");
        entry.setAction(action);
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setDetails(details);
        auditLogRepository.save(entry);
    }

    /** Convenience overload without entityId */
    public void log(String username, String role, String action, String details) {
        log(username, role, action, null, null, details);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllOrderByTimestampDesc();
    }

    public List<AuditLog> getRecentLogs(int count) {
        return auditLogRepository.findRecent(PageRequest.of(0, count));
    }

    public List<AuditLog> getLogsByUser(String username) {
        return auditLogRepository.findByUsernameOrderByTimestampDesc(username);
    }

    public List<AuditLog> getLogsByAction(String action) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action);
    }

    public List<AuditLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByDateRange(start, end);
    }
}
