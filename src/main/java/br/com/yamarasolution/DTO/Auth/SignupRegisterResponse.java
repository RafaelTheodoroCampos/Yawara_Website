package br.com.yamarasolution.DTO.Auth;

import java.util.List;
import java.util.UUID;

import br.com.yamarasolution.enums.ERole;
import br.com.yamarasolution.model.User;
import lombok.Data;

@Data
public class SignupRegisterResponse {

  private UUID id;

  private String username;

  private String email;

  private String imageUrl;

  private List<ERole> roles;

  public SignupRegisterResponse(User u, List<ERole> roles2) {
    this.id = u.getId();
    this.username = u.getUsername();
    this.email = u.getEmail();
    this.imageUrl = u.getImageUrl();
    this.roles = roles2;
  }

}
