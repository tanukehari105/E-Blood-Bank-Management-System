package com.bloodbank.repository;

import com.bloodbank.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    Optional<Hospital> findByUsername(String username);

    Optional<Hospital> findByEmail(String email);

    List<Hospital> findByActiveTrue();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT h FROM Hospital h WHERE LOWER(h.hospitalName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(h.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Hospital> searchHospitals(@Param("query") String query);
}
