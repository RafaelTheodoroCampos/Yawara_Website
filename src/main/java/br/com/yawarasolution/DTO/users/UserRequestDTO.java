package br.com.yawarasolution.DTO.users;

import br.com.yawarasolution.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRequestDTO {
  
  @NotBlank
  @Size(max = 100)
  private String username;

  @NotBlank
  @Size(max = 120)
  private String password;

  @NotBlank
  private String telefone;

  public UserRequestDTO(User u) {
    this.username = u.getUsername();
    this.password = u.getPassword();
    this.telefone = u.getTelefone();
  }

}
