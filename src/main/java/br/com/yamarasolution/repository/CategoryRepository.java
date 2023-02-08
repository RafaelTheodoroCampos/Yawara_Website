package br.com.yamarasolution.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.yamarasolution.model.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
  
  List<Category> findByNameEqualsIgnoreCase(String name);

  Boolean existsByNameIgnoreCase(String name);

}
