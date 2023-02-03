package br.com.yamarasolution.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.yamarasolution.model.Donator;

public interface DonatorRepository extends JpaRepository<Donator, UUID> {
  
}