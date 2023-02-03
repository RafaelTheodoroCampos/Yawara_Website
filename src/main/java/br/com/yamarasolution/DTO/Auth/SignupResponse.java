package br.com.yamarasolution.DTO.Auth;

import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class SignupResponse {
  private String accessToken;
  private String type = "Bearer";
  private String refreshToken;
  private UUID id;
  private String username;
  private String email;
  private List<String> roles;

  public SignupResponse(String accessToken, String refreshToken, UUID id, String username, String email, List<String> roles) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.id = id;
    this.username = username;
    this.email = email;
    this.roles = roles;
  }
}
