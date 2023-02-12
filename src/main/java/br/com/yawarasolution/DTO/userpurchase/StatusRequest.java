package br.com.yawarasolution.DTO.userpurchase;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusRequest {

  @NotNull(message = "Not null value")
  private String purchaseStatus;

}
