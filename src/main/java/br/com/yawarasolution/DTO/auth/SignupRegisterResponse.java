package br.com.yawarasolution.DTO.auth;

import java.util.List;
import java.util.UUID;

import br.com.yawarasolution.enums.ERole;
import br.com.yawarasolution.model.User;
import lombok.Data;

@Data
public class SignupRegisterResponse {

  private UUID id;

  private String username;

  private String name;

  private String email;

  private String imageUrl;

  private List<ERole> roles;

  public SignupRegisterResponse(User u, List<ERole> roles2) {
    this.id = u.getId();
    this.username = u.getUsername();
    this.name = u.getName();
    this.email = u.getEmail();
    this.imageUrl = u.getImageUrl();
    this.roles = roles2;
  }

}
