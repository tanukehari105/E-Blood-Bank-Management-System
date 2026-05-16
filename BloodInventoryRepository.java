package com.bloodbank.repository;

import com.bloodbank.entity.BloodInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface BloodInventoryRepository extends JpaRepository<BloodInventory, Long> {
    List<BloodInventory> findByBloodGroup(String bloodGroup);

    @Query("SELECT b FROM BloodInventory b WHERE b.expiryDate < :today")
    List<BloodInventory> findExpiredBlood(@Param("today") LocalDate today);

    @Query("SELECT b FROM BloodInventory b WHERE b.expiryDate BETWEEN :today AND :warningDate")
    List<BloodInventory> findExpiringBlood(@Param("today") LocalDate today, @Param("warningDate") LocalDate warningDate);

    @Query("SELECT b.bloodGroup, SUM(b.quantity) FROM BloodInventory b WHERE b.expiryDate >= :today GROUP BY b.bloodGroup")
    List<Object[]> getAvailableStockByBloodGroup(@Param("today") LocalDate today);

    @Query("SELECT SUM(b.quantity) FROM BloodInventory b WHERE b.bloodGroup = :bloodGroup AND b.expiryDate >= :today")
    Integer getTotalAvailableByBloodGroup(@Param("bloodGroup") String bloodGroup, @Param("today") LocalDate today);
}
