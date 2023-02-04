package br.com.yamarasolution.DTO.category;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import br.com.yamarasolution.model.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ProductCategoryResponseDTO {

  private UUID id;

  private String name;
  
  private String description;
  
  private Double rating;

  private Boolean isActive;
  
  private BigDecimal price;
  
  private BigDecimal stock;
  
  private Instant createdAt;
  
  private Instant updatedAt;
  
  private String imageUrl;
  
  private UserCategoryResponseDTO createdBy;
  

  public ProductCategoryResponseDTO(Product p) {
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
    this.createdBy = new UserCategoryResponseDTO(p.getCreatedBy());
  }
  
}
