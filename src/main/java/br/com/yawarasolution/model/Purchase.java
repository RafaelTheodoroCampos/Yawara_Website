package br.com.yawarasolution.model;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "purchase")
public class Purchase {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private BigDecimal quantity;

  private BigDecimal unitPrice;

  @ManyToOne
  @JoinColumn(name = "product_id")
  private Product product;

  @ManyToOne
  @JoinColumn(name = "user_purchase_id")
  private UserPurchase userPurchase;

}
