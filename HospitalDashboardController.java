package com.bloodbank.controller;

import com.bloodbank.dto.DashboardStats;
import com.bloodbank.entity.BloodRequest;
import com.bloodbank.service.BloodRequestService;
import com.bloodbank.service.DashboardService;
import com.bloodbank.service.HospitalService;
import com.bloodbank.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Hospital-facing dashboard and request endpoints.
 * Accessible only with HOSPITAL role JWT.
 */
@RestController
@RequestMapping("/api/hospital-dashboard")
public class HospitalDashboardController {

    @Autowired private DashboardService dashboardService;
    @Autowired private BloodRequestService requestService;
    @Autowired private HospitalService hospitalService;
    @Autowired private InventoryService inventoryService;

    /** Hospital dashboard — stock availability + own request stats */
    @GetMapping
    public ResponseEntity<DashboardStats> getHospitalDashboard(Authentication auth) {
        var hospital = hospitalService.getByUsername(auth.getName());
        return ResponseEntity.ok(dashboardService.getHospitalStats(hospital.getId()));
    }

    /** Current blood stock (read-only for hospitals) */
    @GetMapping("/stock")
    public ResponseEntity<Map<String, Integer>> getBloodStock() {
        return ResponseEntity.ok(inventoryService.getStockByBloodGroup());
    }

    /** Submit a blood request */
    @PostMapping("/requests")
    public ResponseEntity<BloodRequest> submitRequest(@RequestBody BloodRequest request,
                                                       Authentication auth) {
        var hospital = hospitalService.getByUsername(auth.getName());
        request.setHospitalId(hospital.getId());
        request.setHospital(hospital.getHospitalName());
        return ResponseEntity.ok(requestService.createRequest(request, auth.getName()));
    }

    /** View own request history */
    @GetMapping("/requests")
    public ResponseEntity<List<BloodRequest>> getMyRequests(Authentication auth) {
        var hospital = hospitalService.getByUsername(auth.getName());
        return ResponseEntity.ok(requestService.getRequestsByHospital(hospital.getId()));
    }

    /** Track a specific request */
    @GetMapping("/requests/{id}")
    public ResponseEntity<BloodRequest> getRequest(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.getById(id));
    }
}
