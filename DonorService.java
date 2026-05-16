package com.bloodbank.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bloodbank.entity.Donor;
import com.bloodbank.repository.DonorRepository;

@Service
public class DonorService {

    private static final int ELIGIBILITY_DAYS = 90;

    @Autowired
    private DonorRepository donorRepository;

    public List<Donor> getAllDonors() {
        return donorRepository.findByActiveTrue();
    }

    public Donor getDonorById(Long id) {
        return donorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Donor not found with id: " + id));
    }

    public Donor createDonor(Donor donor) {
        donor.setActive(true);
        return donorRepository.save(donor);
    }

    public Donor updateDonor(Long id, Donor updated) {
        Donor existing = getDonorById(id);
        existing.setName(updated.getName());
        existing.setAge(updated.getAge());
        existing.setGender(updated.getGender());
        existing.setBloodGroup(updated.getBloodGroup());
        existing.setContact(updated.getContact());
        existing.setAddress(updated.getAddress());
        existing.setLastDonationDate(updated.getLastDonationDate());
        return donorRepository.save(existing);
    }

    public void deleteDonor(Long id) {
        Donor donor = getDonorById(id);
        donor.setActive(false);
        donorRepository.save(donor);
    }

    public List<Donor> searchDonors(String query) {
        return donorRepository.searchDonors(query);
    }

    public List<Donor> getDonorsByBloodGroup(String bloodGroup) {
        return donorRepository.findByBloodGroupAndActiveTrue(bloodGroup);
    }

    public boolean isDonorEligible(Donor donor) {
        if (donor.getLastDonationDate() == null) return true;
        LocalDate eligibleFrom = donor.getLastDonationDate().plusDays(ELIGIBILITY_DAYS);
        return !eligibleFrom.isAfter(LocalDate.now());
    }

    public List<Donor> getEligibleDonorsByBloodGroup(String bloodGroup) {
        LocalDate eligibleDate = LocalDate.now().minusDays(ELIGIBILITY_DAYS);
        return donorRepository.findEligibleDonorsByBloodGroup(bloodGroup, eligibleDate);
    }

    public List<Donor> getAllEligibleDonors() {
        LocalDate eligibleDate = LocalDate.now().minusDays(ELIGIBILITY_DAYS);
        return donorRepository.findAllEligibleDonors(eligibleDate);
    }
}
