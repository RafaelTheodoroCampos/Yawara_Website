package br.com.yawarasolution.DTO.userpurchase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.yawarasolution.enums.PurchaseStatus;
import br.com.yawarasolution.model.Purchase;
import br.com.yawarasolution.model.UserPurchase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserPurchaseResponseDTO {

  private UUID id;

  private UserResponseUserPurchaseDTO user;

  private List<PurchaseResponseUserPurchaseDTO> purchases = new ArrayList<>();

  private BigDecimal totalPrice;

  private PurchaseStatus purchaseStatus;

  private LocalDate purchaseDate;

  public UserPurchaseResponseDTO(UserPurchase us) {
    this.id = us.getId();
    this.user = new UserResponseUserPurchaseDTO(us.getUser());
    this.totalPrice = us.getTotalPrice();
    this.purchaseStatus = us.getPurchaseStatus();
    this.purchaseDate = us.getPurchaseDate();
    if (us.getPurchases() != null) {
      us.getPurchases().stream().forEach(p -> purchases.add(new PurchaseResponseUserPurchaseDTO(p)));
    }
  }

  public UserPurchaseResponseDTO (UserPurchase us, List<Purchase> purchases) {
    this(us);
    purchases.forEach(p -> this.purchases.add(new PurchaseResponseUserPurchaseDTO(p)));
  }
}
