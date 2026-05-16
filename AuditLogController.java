package com.bloodbank.controller;

import com.bloodbank.entity.AuditLog;
import com.bloodbank.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Audit log controller — ADMIN only.
 */
@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }

    @GetMapping("/recent/{count}")
    public ResponseEntity<List<AuditLog>> getRecentLogs(@PathVariable int count) {
        return ResponseEntity.ok(auditLogService.getRecentLogs(count));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<AuditLog>> getLogsByUser(@PathVariable String username) {
        return ResponseEntity.ok(auditLogService.getLogsByUser(username));
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<List<AuditLog>> getLogsByAction(@PathVariable String action) {
        return ResponseEntity.ok(auditLogService.getLogsByAction(action));
    }

    @GetMapping("/range")
    public ResponseEntity<List<AuditLog>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(auditLogService.getLogsByDateRange(start, end));
    }
}
