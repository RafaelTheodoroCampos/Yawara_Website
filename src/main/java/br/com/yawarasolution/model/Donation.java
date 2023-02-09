package br.com.yawarasolution.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
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
@Table(name = "donation")
public class Donation {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "goal", nullable = false)
  private BigDecimal goal;

  @Column(name = "amount_received", nullable = false)
  private BigDecimal amountReceived;

  @Column(name = "donation_date", nullable = false)
  private LocalDateTime donationDate;

  @ManyToOne
  @JoinColumn(name = "donator_id", referencedColumnName = "id")
  private Donator donator;
  
}
