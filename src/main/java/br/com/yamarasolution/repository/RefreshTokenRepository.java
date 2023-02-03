package br.com.yamarasolution.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import br.com.yamarasolution.model.RefreshToken;
import br.com.yamarasolution.model.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
  Optional<RefreshToken> findByToken(String token);

  RefreshToken findByUserAndExpiryDateAfter(User user, Instant instante);

  @Modifying
  int deleteByUser(User user);
}
