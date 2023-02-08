package br.com.yamarasolution.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.yamarasolution.model.EmailChangeRequest;

public interface EmailChangeRequestRepository extends JpaRepository<EmailChangeRequest, UUID> {
  
  Optional<EmailChangeRequest> findByUserIdAndConfirmedFalse(UUID userId);

  Optional<EmailChangeRequest> findByNewEmailIgnoreCaseAndConfirmedFalse(String newEmail);

  Optional<EmailChangeRequest> findByConfirmationCode(String code);
  
}
