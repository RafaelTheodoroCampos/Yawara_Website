package br.com.yamarasolution.DTO.Auth;

import java.util.List;
import java.util.UUID;

import br.com.yamarasolution.enums.ERole;
import lombok.Data;

@Data
public class TokenRefreshResponse {

  private UUID id;
  private String username;
  private String email;
  private List<ERole> roles;
  private String accessToken;
  private String refreshToken;
  private String tokenType = "Bearer";

  public TokenRefreshResponse(String accessToken, String refreshToken, UUID id, String username, String email,
      List<ERole> roles) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.id = id;
    this.username = username;
    this.email = email;
    this.roles = roles;
  }

}
