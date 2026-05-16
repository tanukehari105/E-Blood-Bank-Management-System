package com.bloodbank.repository;

import com.bloodbank.entity.DonationCamp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DonationCampRepository extends JpaRepository<DonationCamp, Long> {

    List<DonationCamp> findByActiveTrue();

    List<DonationCamp> findByCampDateAfterAndActiveTrue(LocalDate date);

    List<DonationCamp> findByCampDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT c FROM DonationCamp c WHERE c.campDate >= :today AND c.active = true ORDER BY c.campDate ASC")
    List<DonationCamp> findUpcomingCamps(@Param("today") LocalDate today);

    @Query("SELECT c FROM DonationCamp c WHERE c.campDate < :today ORDER BY c.campDate DESC")
    List<DonationCamp> findPastCamps(@Param("today") LocalDate today);

    @Query("SELECT SUM(c.totalUnitsCollected) FROM DonationCamp c")
    Long getTotalUnitsFromCamps();
}
