package br.com.yawarasolution.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.yawarasolution.model.Donation;

public interface DonationRepository extends JpaRepository<Donation, UUID> {
  
}
