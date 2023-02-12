package br.com.yawarasolution.DTO.userpurchase;

import java.util.UUID;

import br.com.yawarasolution.DTO.category.CategoryResponseProductDTO;
import br.com.yawarasolution.model.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ProductResponseUserPurchaseDTO {
  
  private UUID id;
  private String name;
  private String description;
  private Integer rating;
  private Double price;
  private String imageUrl; 
  private CategoryResponseProductDTO category;

  public ProductResponseUserPurchaseDTO(Product p) {
    this.id = p.getId();
    this.name = p.getName();
    this.description = p.getDescription();
    this.rating = p.getRating();
    this.price = p.getPrice();
    this.imageUrl = p.getImageUrl();
    this.category = new CategoryResponseProductDTO(p.getCategory());
  }

}
