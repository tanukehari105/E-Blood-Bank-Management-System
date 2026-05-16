package com.bloodbank.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bloodbank.entity.Donor;

public interface DonorRepository extends JpaRepository<Donor, Long> {
    List<Donor> findByBloodGroupAndActiveTrue(String bloodGroup);
    List<Donor> findByActiveTrue();

    @Query("SELECT d FROM Donor d WHERE d.active = true AND (LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%')) OR d.contact LIKE CONCAT('%', :query, '%'))")
    List<Donor> searchDonors(@Param("query") String query);

    @Query("SELECT d FROM Donor d WHERE d.active = true AND d.bloodGroup = :bloodGroup AND (d.lastDonationDate IS NULL OR d.lastDonationDate <= :eligibleDate) ORDER BY d.lastDonationDate ASC")
    List<Donor> findEligibleDonorsByBloodGroup(@Param("bloodGroup") String bloodGroup, @Param("eligibleDate") LocalDate eligibleDate);

    @Query("SELECT d FROM Donor d WHERE d.active = true AND (d.lastDonationDate IS NULL OR d.lastDonationDate <= :eligibleDate)")
    List<Donor> findAllEligibleDonors(@Param("eligibleDate") LocalDate eligibleDate);
}
