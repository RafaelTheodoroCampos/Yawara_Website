package br.com.yamarasolution.DTO.category;

import java.util.UUID;

import br.com.yamarasolution.model.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CategoryResponseDTO {

  private UUID id;

  private String name;

  private String description;

  // private List<ProductCategoryResponseDTO> products = new ArrayList<>();

  public CategoryResponseDTO(Category c) {
    this.id = c.getId();
    this.name = c.getName();
    this.description = c.getDescription();
    // if (c.getProducts() != null) {
    //   c.getProducts().forEach(product -> products.add(new ProductCategoryResponseDTO(product)));
    // }
  }

}
