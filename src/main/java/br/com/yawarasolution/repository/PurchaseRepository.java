package br.com.yawarasolution.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.yawarasolution.model.Purchase;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {
  
}
