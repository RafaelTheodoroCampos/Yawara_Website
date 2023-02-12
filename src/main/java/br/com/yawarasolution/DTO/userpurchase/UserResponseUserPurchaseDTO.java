package br.com.yawarasolution.DTO.userpurchase;

import java.util.UUID;

import br.com.yawarasolution.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserResponseUserPurchaseDTO {

  private UUID id;
  
  private String username;

  private String email;

  private String imageUrl;

  private String telefone;

  public UserResponseUserPurchaseDTO(User u) {
    this.id = u.getId();
    this.username = u.getUsername();
    this.email = u.getEmail();
    this.imageUrl = u.getImageUrl();
    this.telefone = u.getTelefone();
  }

}
