package br.com.yawarasolution.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "user_purchase")
public class UserPurchase {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private User user;

  @OneToMany(mappedBy="userPurchase" ,fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<Purchase> purchases;

  @Column(name = "total_price", nullable = false)
  private BigDecimal totalPrice;

  @Column(name = "purchase_status", nullable = false)
  private String purchaseStatus;

  @Column(name = "purchase_date", nullable = false)
  private LocalDateTime purchaseDate;

}
