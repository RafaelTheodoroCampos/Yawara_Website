package br.com.yamarasolution.DTO.product;

import java.time.Instant;
import java.util.UUID;

import br.com.yamarasolution.DTO.category.CategoryResponseProductDTO;
import br.com.yamarasolution.model.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ProductResponseDTO {

  private UUID id;

  private String name;

  private String description;

  private Integer rating;

  private Boolean isActive;

  private Double price;

  private Integer stock;

  private Instant createdAt;

  private Instant updatedAt;

  private String imageUrl;

  private CategoryResponseProductDTO category;

  public ProductResponseDTO(Product p) {
    this.id = p.getId();
    this.name = p.getName();
    this.description = p.getDescription();
    this.rating = p.getRating();
    this.isActive = p.getIsActive();
    this.price = p.getPrice();
    this.stock = p.getStock();
    this.createdAt = p.getCreatedAt();
    this.updatedAt = p.getUpdatedAt();
    this.imageUrl = p.getImageUrl();
    this.category = new CategoryResponseProductDTO(p.getCategory());
  }

}
