package br.com.yamarasolution.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.yamarasolution.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {
  
  Optional<User> findByUsername(String username);

  User findByEmail(String email);

  Optional<User> findByActivationCode(String code);

  Boolean existsByUsername(String username);

  Boolean existsByEmailIgnoreCase(String email);
}
