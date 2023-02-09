package br.com.yawarasolution.model;

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
@Table(name = "survey_question")
public class SurveyQuestion {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "question", nullable = false)
  private String question;

  @Column(name = "response", nullable = false)
  private String response;

  @Column(name = "survey_date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime surveyDate;

  @ManyToOne
  @JoinColumn(name = "donation_id", referencedColumnName = "id")
  private Donation donation;

  @ManyToOne
  @JoinColumn(name = "donator_id", referencedColumnName = "id")
  private Donator donator;
  
}
