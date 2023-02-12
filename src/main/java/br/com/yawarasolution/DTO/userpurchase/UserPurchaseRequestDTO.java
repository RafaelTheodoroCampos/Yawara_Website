package br.com.yawarasolution.DTO.userpurchase;

import java.util.List;
import java.util.stream.Collectors;

import br.com.yawarasolution.model.UserPurchase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserPurchaseRequestDTO {

  @NotNull(message = "The Category cannot be null.")
  @Valid
  private List<PurchaseRequestUserPurchaseDTO> purchases;

  public UserPurchaseRequestDTO(UserPurchase us) {
    this.purchases = us.getPurchases().stream()
        .map(PurchaseRequestUserPurchaseDTO::new)
        .collect(Collectors.toList());
  }

}
