package br.com.yawarasolution.DTO.auth;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RoleRequest {

  @NotEmpty
  private Set<String> roles;
}
