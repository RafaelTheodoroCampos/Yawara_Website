package br.com.yawarasolution.enums;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PurchaseStatus {
  PENDING("1", "Pendente de pagamento"),
  APPROVED("2", "Pagamento aprovado"),
  DECLINED("3", "Pagamento recusado"),
  REFUNDED("4", "Pagamento estornado"),
  PROCESSING("5", "Processando"),
  SHIPPED("6", "Enviado"),
  DELIVERED("7", "Entregue"),
  CANCELED("8", "Cancelado");

  private String codigo;
  private String mensagem;

  PurchaseStatus(String codigo, String mensagem) {
    this.codigo = codigo;
    this.mensagem = mensagem;
  }

  public String getCodigo() {
    return codigo;
  }

  public String getMensagem() {
    return mensagem;
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
