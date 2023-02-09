package br.com.yawarasolution.DTO.category;

import java.util.UUID;

import br.com.yawarasolution.model.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CategoryResponseProductDTO {

  private UUID id;

  private String name;

  private String description;

  public CategoryResponseProductDTO(Category c) {
    this.id = c.getId();
    this.name = c.getName();
    this.description = c.getDescription();
  }
}
