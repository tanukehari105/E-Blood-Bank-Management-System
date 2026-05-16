package com.bloodbank.controller;

import com.bloodbank.entity.BloodAllocation;
import com.bloodbank.entity.BloodInventory;
import com.bloodbank.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Inventory controller — enhanced with QR code and allocation endpoints.
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<BloodInventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BloodInventory> getById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getById(id));
    }

    @PostMapping
    public ResponseEntity<BloodInventory> addInventory(@RequestBody BloodInventory inventory) {
        return ResponseEntity.ok(inventoryService.addInventory(inventory));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BloodInventory> updateInventory(@PathVariable Long id,
                                                           @RequestBody BloodInventory inventory) {
        return ResponseEntity.ok(inventoryService.updateInventory(id, inventory));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.ok(Map.of("message", "Inventory record deleted"));
    }

    @GetMapping("/stock")
    public ResponseEntity<Map<String, Integer>> getStockByBloodGroup() {
        return ResponseEntity.ok(inventoryService.getStockByBloodGroup());
    }

    @GetMapping("/alerts/low-stock")
    public ResponseEntity<List<String>> getLowStockAlerts() {
        return ResponseEntity.ok(inventoryService.getLowStockAlerts());
    }

    @GetMapping("/alerts/expiry")
    public ResponseEntity<List<String>> getExpiryAlerts() {
        return ResponseEntity.ok(inventoryService.getExpiryAlerts());
    }

    @DeleteMapping("/remove-expired")
    public ResponseEntity<?> removeExpiredBlood() {
        inventoryService.removeExpiredBlood();
        return ResponseEntity.ok(Map.of("message", "Expired blood removed from inventory"));
    }

    /** Generate QR code for a specific inventory batch */
    @GetMapping("/{id}/qr")
    public ResponseEntity<Map<String, String>> getQrCode(@PathVariable Long id) {
        String qrBase64 = inventoryService.generateQrCode(id);
        return ResponseEntity.ok(Map.of("qrCode", qrBase64, "inventoryId", id.toString()));
    }

    /** All allocation records */
    @GetMapping("/allocations")
    public ResponseEntity<List<BloodAllocation>> getAllAllocations() {
        return ResponseEntity.ok(inventoryService.getAllAllocations());
    }

    /** Allocations for a specific request */
    @GetMapping("/allocations/request/{requestId}")
    public ResponseEntity<List<BloodAllocation>> getAllocationsByRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(inventoryService.getAllocationsByRequest(requestId));
    }
}
