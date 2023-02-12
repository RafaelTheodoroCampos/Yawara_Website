package br.com.yawarasolution.DTO.userpurchase;

import java.math.BigDecimal;

import br.com.yawarasolution.model.Purchase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseRequestUserPurchaseDTO {


  @NotNull(message = "Quantity must not be null")
  @Min(value = 1, message = "Quantity must be at least 1")
  private BigDecimal quantity;

  @NotNull(message = "The Product cannot be null.")
  @Valid
  private ProductRequestUserPurchaseDTO product;

  public PurchaseRequestUserPurchaseDTO (Purchase p) {
    this.quantity = p.getQuantity();
    this.product = new ProductRequestUserPurchaseDTO(p.getProduct());
  }
  
}
