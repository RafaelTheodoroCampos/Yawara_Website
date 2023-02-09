package br.com.yawarasolution.DTO.users;

import br.com.yawarasolution.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRequestAdminDTO {

  @NotBlank
  @Size(max = 100)
  private String username;

  @NotBlank
  @Email
  private String email;

  @NotBlank
  @Size(max = 120)
  private String password;

  @NotBlank
  private String imageUrl;

  @NotBlank
  private String telefone;

  public UserRequestAdminDTO(User u) {
    this.username = u.getUsername();
    this.email = u.getEmail();
    this.password = u.getPassword();
    this.imageUrl = u.getImageUrl();
    this.telefone = u.getTelefone();
  }
  
}
