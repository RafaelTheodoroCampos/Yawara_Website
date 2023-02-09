package br.com.yawarasolution.DTO.product;

import br.com.yawarasolution.model.Product;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ProducRequestDTO {

  @NotBlank(message = "name must not be blank")
  private String name;

  @NotBlank(message = "description must not be blank")
  private String description;

  @NotNull(message = "The Price cannot be null.")
  @Min(value = 0, message = "Price must be greater than or equal to 0.00")
  @Max(value = 99999999, message = "Price must be less than or equal to 99999999")
  private Double price;

  @Min(value = 1, message = "The value of price must be at least 1")
  private Integer stock;

  @NotNull(message = "The Category cannot be null.")
  @Valid
  private CategoryProductRequestDTO category;

  public ProducRequestDTO(Product p) {
    this.name = p.getName();
    this.description = p.getDescription();
    this.price = p.getPrice();
    this.stock = p.getStock();
    this.category = new CategoryProductRequestDTO(p.getCategory());
  }

}
