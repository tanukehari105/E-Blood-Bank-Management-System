package com.bloodbank.repository;

import com.bloodbank.entity.BloodAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BloodAllocationRepository extends JpaRepository<BloodAllocation, Long> {

    List<BloodAllocation> findByRequestId(Long requestId);

    List<BloodAllocation> findByDonorId(Long donorId);

    List<BloodAllocation> findByBloodGroup(String bloodGroup);

    @Query("SELECT a FROM BloodAllocation a WHERE a.allocationDate BETWEEN :start AND :end ORDER BY a.allocationDate DESC")
    List<BloodAllocation> findByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT SUM(a.unitsAllocated) FROM BloodAllocation a WHERE a.bloodGroup = :bloodGroup")
    Long getTotalAllocatedByBloodGroup(@Param("bloodGroup") String bloodGroup);
}
