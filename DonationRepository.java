package com.bloodbank.repository;

import com.bloodbank.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findByDonorId(Long donorId);

    List<Donation> findByCampId(Long campId);

    @Query("SELECT d FROM Donation d WHERE d.donationDate BETWEEN :start AND :end ORDER BY d.donationDate DESC")
    List<Donation> findByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT SUM(d.quantity) FROM Donation d WHERE d.campId = :campId")
    Integer getTotalUnitsByCamp(@Param("campId") Long campId);

    @Query("SELECT COUNT(DISTINCT d.donor.id) FROM Donation d WHERE d.campId = :campId")
    Long getUniqueDonorsByCamp(@Param("campId") Long campId);

    @Query("SELECT d.bloodGroup, SUM(d.quantity) FROM Donation d WHERE d.campId = :campId GROUP BY d.bloodGroup ORDER BY SUM(d.quantity) DESC")
    List<Object[]> getBloodGroupStatsByCamp(@Param("campId") Long campId);
}
