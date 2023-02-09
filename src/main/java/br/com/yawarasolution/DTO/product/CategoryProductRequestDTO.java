package br.com.yawarasolution.DTO.product;

import java.util.UUID;

import br.com.yawarasolution.model.Category;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CategoryProductRequestDTO {
  
  @NotNull
  private UUID id;

  public CategoryProductRequestDTO(Category c) {
    this.id = c.getId();
  }
}
