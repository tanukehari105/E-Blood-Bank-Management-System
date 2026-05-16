package com.bloodbank.repository;

import com.bloodbank.entity.BloodRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {

    List<BloodRequest> findByStatus(String status);

    List<BloodRequest> findByBloodGroup(String bloodGroup);

    List<BloodRequest> findByHospitalId(Long hospitalId);

    List<BloodRequest> findByHospitalIdOrderByRequestDateDesc(Long hospitalId);

    @Query("SELECT COUNT(r) FROM BloodRequest r WHERE r.status = 'PENDING'")
    long countPendingRequests();

    @Query("SELECT r FROM BloodRequest r ORDER BY r.requestDate DESC")
    List<BloodRequest> findAllOrderByDateDesc();

    @Query("SELECT r FROM BloodRequest r WHERE r.urgencyLevel = 'CRITICAL' AND r.status = 'PENDING' ORDER BY r.requestDate DESC")
    List<BloodRequest> findCriticalPendingRequests();

    @Query("SELECT r FROM BloodRequest r WHERE r.urgencyLevel IN ('URGENT', 'CRITICAL') AND r.status = 'PENDING' ORDER BY r.requestDate DESC")
    List<BloodRequest> findUrgentPendingRequests();
}
