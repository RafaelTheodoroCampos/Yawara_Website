package br.com.yamarasolution.DTO.category;

import br.com.yamarasolution.model.Category;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CategoryRequestDTO {

  @NotBlank(message = "Category name must not be blank")
  private String name;
  
  @NotBlank(message = "Category description must not be blank")
  private String description;

  public CategoryRequestDTO(Category c) {
    this.name = c.getName();
    this.description = c.getDescription();
  }
  
}
