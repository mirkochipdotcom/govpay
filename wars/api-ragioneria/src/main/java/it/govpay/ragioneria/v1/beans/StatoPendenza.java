package it.govpay.ragioneria.v1.beans;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * Stato della pendenza:   * ESEGUITA: Pagata  * NON_ESEGUITA: Da pagare  * ESEGUITA_PARZIALE: Pagata parzialmente  * ANNULLATA: Annullata  * SCADUTA: Scaduta
 */
public enum StatoPendenza {




  ESEGUITA("ESEGUITA"),


  NON_ESEGUITA("NON_ESEGUITA"),


  ESEGUITA_PARZIALE("ESEGUITA_PARZIALE"),


  ANNULLATA("ANNULLATA"),


  SCADUTA("SCADUTA");




  private String value;

  StatoPendenza(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(this.value);
  }

  @JsonCreator
  public static StatoPendenza fromValue(String text) {
    for (StatoPendenza b : StatoPendenza.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}



