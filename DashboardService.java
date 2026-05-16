package com.bloodbank.service;

import com.bloodbank.dto.DashboardStats;
import com.bloodbank.entity.AuditLog;
import com.bloodbank.repository.HospitalRepository;
import com.bloodbank.repository.DonationCampRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Enhanced dashboard service — aggregates all platform stats.
 */
@Service
public class DashboardService {

    @Autowired private DonorService donorService;
    @Autowired private InventoryService inventoryService;
    @Autowired private BloodRequestService requestService;
    @Autowired private DonationService donationService;
    @Autowired private HospitalRepository hospitalRepository;
    @Autowired private DonationCampRepository campRepository;
    @Autowired private AuditLogService auditLogService;

    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();

        stats.setTotalDonors(donorService.getAllDonors().size());
        stats.setTotalDonations(donationService.getTotalDonations());
        stats.setPendingRequests(requestService.getPendingCount());
        stats.setTotalHospitals(hospitalRepository.findByActiveTrue().size());
        stats.setTotalCamps(campRepository.findByActiveTrue().size());
        stats.setCriticalRequests(requestService.getCriticalPendingRequests().size());

        Map<String, Integer> stockMap = inventoryService.getStockByBloodGroup();
        stats.setStockByBloodGroup(stockMap);
        stats.setTotalAvailableUnits(stockMap.values().stream().mapToLong(Integer::longValue).sum());

        stats.setLowStockAlerts(inventoryService.getLowStockAlerts());
        stats.setExpiryAlerts(inventoryService.getExpiryAlerts());

        // Emergency alerts = critical pending requests
        List<String> emergencyAlerts = new ArrayList<>();
        requestService.getCriticalPendingRequests().forEach(r ->
            emergencyAlerts.add("CRITICAL: " + r.getBloodGroup() + " x" + r.getUnits() +
                    " for " + r.getPatientName() + " at " + (r.getHospital() != null ? r.getHospital() : "N/A"))
        );
        stats.setEmergencyAlerts(emergencyAlerts);

        // Recent activity (last 10 audit log entries)
        List<AuditLog> recentLogs = auditLogService.getRecentLogs(10);
        List<Map<String, Object>> activity = new ArrayList<>();
        for (AuditLog log : recentLogs) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("action", log.getAction());
            entry.put("user", log.getUsername());
            entry.put("details", log.getDetails());
            entry.put("timestamp", log.getTimestamp() != null ? log.getTimestamp().toString() : "");
            activity.add(entry);
        }
        stats.setRecentActivity(activity);

        return stats;
    }

    /** Hospital-specific dashboard — only stock and their own requests */
    public DashboardStats getHospitalStats(Long hospitalId) {
        DashboardStats stats = new DashboardStats();
        Map<String, Integer> stockMap = inventoryService.getStockByBloodGroup();
        stats.setStockByBloodGroup(stockMap);
        stats.setTotalAvailableUnits(stockMap.values().stream().mapToLong(Integer::longValue).sum());
        stats.setPendingRequests(
            requestService.getRequestsByHospital(hospitalId).stream()
                .filter(r -> "PENDING".equals(r.getStatus())).count()
        );
        return stats;
    }
}
