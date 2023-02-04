package br.com.yamarasolution.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "product")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "name", nullable = false)
  private String name;
  
  @Column(name = "description", nullable = false)
  private String description;
  
  @Column(name = "rating", nullable = false)
  private Double rating;

  @Column(name = "isactive", nullable = false)
  private Boolean isActive;
  
  @Column(name = "price", nullable = false)
  private BigDecimal price;
  
  @Column(name = "stock", nullable = false)
  private BigDecimal stock;
  
  @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private Instant createdAt;
  
  @Column(name = "updated_at")
  private Instant updatedAt;
  
  @Column(name = "image_url", nullable = false)
  private String imageUrl;
  
  @ManyToOne
  @JoinColumn(name = "created_by", referencedColumnName = "id")
  private User createdBy;
  
  @ManyToOne
  @JoinColumn(name = "category_id", referencedColumnName = "id")
  private Category category;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
  private List<Purchase> purchases;
  
}
