package com.bloodbank.dto;

import java.util.List;
import java.util.Map;

/**
 * Enhanced dashboard stats DTO — includes camp, hospital, and audit data.
 */
public class DashboardStats {
    private long totalDonors;
    private long totalAvailableUnits;
    private long pendingRequests;
    private long totalDonations;
    private long totalHospitals;
    private long totalCamps;
    private long criticalRequests;
    private Map<String, Integer> stockByBloodGroup;
    private List<String> lowStockAlerts;
    private List<String> expiryAlerts;
    private List<String> emergencyAlerts;
    private List<Map<String, Object>> recentActivity;

    // ── Getters & Setters ──────────────────────────────────────────────────────
    public long getTotalDonors() { return totalDonors; }
    public void setTotalDonors(long totalDonors) { this.totalDonors = totalDonors; }
    public long getTotalAvailableUnits() { return totalAvailableUnits; }
    public void setTotalAvailableUnits(long totalAvailableUnits) { this.totalAvailableUnits = totalAvailableUnits; }
    public long getPendingRequests() { return pendingRequests; }
    public void setPendingRequests(long pendingRequests) { this.pendingRequests = pendingRequests; }
    public long getTotalDonations() { return totalDonations; }
    public void setTotalDonations(long totalDonations) { this.totalDonations = totalDonations; }
    public long getTotalHospitals() { return totalHospitals; }
    public void setTotalHospitals(long totalHospitals) { this.totalHospitals = totalHospitals; }
    public long getTotalCamps() { return totalCamps; }
    public void setTotalCamps(long totalCamps) { this.totalCamps = totalCamps; }
    public long getCriticalRequests() { return criticalRequests; }
    public void setCriticalRequests(long criticalRequests) { this.criticalRequests = criticalRequests; }
    public Map<String, Integer> getStockByBloodGroup() { return stockByBloodGroup; }
    public void setStockByBloodGroup(Map<String, Integer> stockByBloodGroup) { this.stockByBloodGroup = stockByBloodGroup; }
    public List<String> getLowStockAlerts() { return lowStockAlerts; }
    public void setLowStockAlerts(List<String> lowStockAlerts) { this.lowStockAlerts = lowStockAlerts; }
    public List<String> getExpiryAlerts() { return expiryAlerts; }
    public void setExpiryAlerts(List<String> expiryAlerts) { this.expiryAlerts = expiryAlerts; }
    public List<String> getEmergencyAlerts() { return emergencyAlerts; }
    public void setEmergencyAlerts(List<String> emergencyAlerts) { this.emergencyAlerts = emergencyAlerts; }
    public List<Map<String, Object>> getRecentActivity() { return recentActivity; }
    public void setRecentActivity(List<Map<String, Object>> recentActivity) { this.recentActivity = recentActivity; }
}
