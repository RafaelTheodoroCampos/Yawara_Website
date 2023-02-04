package br.com.yamarasolution.DTO.category;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import br.com.yamarasolution.model.Role;
import br.com.yamarasolution.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserCategoryResponseDTO {

  private UUID id;

  private String username;

  private String email;

  private String imageUrl;

  private Set<Role> roles = new HashSet<>();

  public UserCategoryResponseDTO(User u) {
    this.id = u.getId();
    this.username = u.getUsername();
    this.email = u.getEmail();
    this.imageUrl = u.getImageUrl();
    this.roles = u.getRoles();
  }
  
}
