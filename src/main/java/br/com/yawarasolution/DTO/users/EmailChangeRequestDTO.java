package br.com.yawarasolution.DTO.users;

import br.com.yawarasolution.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmailChangeRequestDTO {

  private String email;

  public EmailChangeRequestDTO(User e) {
    this.email = e.getEmail();
  }
  
}
