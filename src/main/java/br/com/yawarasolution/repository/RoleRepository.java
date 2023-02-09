package br.com.yawarasolution.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.yawarasolution.enums.ERole;
import br.com.yawarasolution.model.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {
  Optional<Role> findByName(ERole name);
}

