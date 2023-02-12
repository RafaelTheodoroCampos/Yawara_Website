package br.com.yawarasolution.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.yawarasolution.enums.PurchaseStatus;
import br.com.yawarasolution.model.User;
import br.com.yawarasolution.model.UserPurchase;

public interface UserPurchaseRepository extends JpaRepository<UserPurchase, UUID> {

  Optional<UserPurchase> findByUser_id(UUID id);
  
  Page<UserPurchase> findByUser(User user, Pageable pageable);

  Page<UserPurchase> findBypurchaseStatusAndUser(PurchaseStatus purchaseStatus, User user, Pageable pageable);
  
}
