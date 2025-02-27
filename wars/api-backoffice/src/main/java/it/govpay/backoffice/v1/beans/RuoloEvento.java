package it.govpay.backoffice.v1.beans;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * Gets or Sets ruoloEvento
 */
public enum RuoloEvento {




  CLIENT("CLIENT"),


  SERVER("SERVER");




  private String value;

  RuoloEvento(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static RuoloEvento fromValue(String text) {
    for (RuoloEvento b : RuoloEvento.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}



