package br.com.yawarasolution.enums;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PurchaseStatus {
  PENDING("1"),
  APPROVED("2"),
  REJECTED("3");

  private String codigo;

  PurchaseStatus(String codigo) {
    this.codigo = codigo;
  }

  public String getCodigo() {
    return codigo;
  }

  public static PurchaseStatus fromCodigo(String codigo) {
    for (PurchaseStatus status : PurchaseStatus.values()) {
      if (status.getCodigo().equals(codigo)) {
        return status;
      }
    }
    throw null;
  }

  public static List<String> getCodigos() {
    List<String> codigos = new ArrayList<>();
    for (PurchaseStatus status : PurchaseStatus.values()) {
      codigos.add(status.getCodigo());
    }
    return codigos;
  }

  @JsonCreator
  public static PurchaseStatus fromString(String codigo) {
    return PurchaseStatus.fromCodigo(codigo);
  }
}
