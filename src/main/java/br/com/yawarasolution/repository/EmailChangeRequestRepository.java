package br.com.yawarasolution.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.yawarasolution.model.EmailChangeRequest;

public interface EmailChangeRequestRepository extends JpaRepository<EmailChangeRequest, UUID> {
  
  Optional<EmailChangeRequest> findByUserIdAndConfirmedFalse(UUID userId);

  Optional<EmailChangeRequest> findByNewEmailIgnoreCaseAndConfirmedFalse(String newEmail);

  Optional<EmailChangeRequest> findByConfirmationCode(String code);
  
}
