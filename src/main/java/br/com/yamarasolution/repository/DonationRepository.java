package br.com.yamarasolution.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.yamarasolution.model.Donation;

public interface DonationRepository extends JpaRepository<Donation, UUID> {
  
}
