package br.com.yamarasolution.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.yamarasolution.model.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {

  Optional<Product> findProductByNameIgnoreCaseAndIsActive(String name, boolean isActive);

  Page<Product> findByNameContainingIgnoreCaseAndIsActive(String nome, Boolean isActive, Pageable pageable);

  Page<Product> findByIsActive(Boolean isActive, Pageable pageable);

  Optional<Product> findProductByNameIgnoreCase(String name);

  Boolean existsByNameIgnoreCase(String name);

}
