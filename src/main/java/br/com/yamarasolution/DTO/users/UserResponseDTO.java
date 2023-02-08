package br.com.yamarasolution.DTO.users;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import br.com.yamarasolution.model.Role;
import br.com.yamarasolution.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserResponseDTO {


  private UUID id;

  private String username;

  private String email;

  private Boolean isActive;

  private String imageUrl;

  private Set<Role> roles = new HashSet<>();

  public UserResponseDTO(User u) {
    this.id = u.getId();
    this.username = u.getUsername();
    this.email = u.getEmail();
    this.isActive = u.getIsActive();
    this.imageUrl = u.getImageUrl();
    this.roles = u.getRoles();
  }
  
}
