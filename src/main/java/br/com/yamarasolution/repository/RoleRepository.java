package br.com.yamarasolution.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.yamarasolution.enums.ERole;
import br.com.yamarasolution.model.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {
  Optional<Role> findByName(ERole name);
}

