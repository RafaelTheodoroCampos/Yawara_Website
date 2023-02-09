package br.com.yawarasolution.DTO.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {

  @NotBlank
  private String refreshToken;

}
