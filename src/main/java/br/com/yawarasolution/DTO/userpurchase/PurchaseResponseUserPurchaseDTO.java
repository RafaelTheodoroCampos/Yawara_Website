package br.com.yawarasolution.DTO.userpurchase;

import java.math.BigDecimal;
import java.util.UUID;

import br.com.yawarasolution.model.Purchase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseResponseUserPurchaseDTO {

  private UUID id;

  private BigDecimal quantity;

  private BigDecimal unitPrice;

  private ProductResponseUserPurchaseDTO product;

  public PurchaseResponseUserPurchaseDTO(Purchase p) {
    this.id = p.getId();
    this.quantity = p.getQuantity();
    this.unitPrice = p.getUnitPrice();
    this.product = new ProductResponseUserPurchaseDTO(p.getProduct());
  }
}
