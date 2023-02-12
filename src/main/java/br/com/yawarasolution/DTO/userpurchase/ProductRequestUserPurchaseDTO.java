package br.com.yawarasolution.DTO.userpurchase;

import java.util.UUID;

import br.com.yawarasolution.model.Product;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductRequestUserPurchaseDTO {
  
  @NotNull(message = "Product ID must not be null")
  private UUID id;

  public ProductRequestUserPurchaseDTO(Product p){
    this.id = p.getId();
  }
 
}
