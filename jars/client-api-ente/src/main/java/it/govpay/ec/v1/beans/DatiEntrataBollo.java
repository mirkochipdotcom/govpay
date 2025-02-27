package it.govpay.ec.v1.beans;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

//import io.swagger.v3.oas.annotations.media.Schema;

/**
  * Definisce i dati di un bollo telematico
 **/
// @Schema(description="Definisce i dati di un bollo telematico")
public class DatiEntrataBollo  {
  public enum TipoBolloEnum {
	  IMPOSTA_BOLLO("01");

    private String value;

    TipoBolloEnum(String value) {
      this.value = value;
    }
    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    @JsonCreator
    public static TipoBolloEnum fromValue(String text) {
      for (TipoBolloEnum b : TipoBolloEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }  
  // @Schema(requiredMode = RequiredMode.REQUIRED, description = "Tipologia di Bollo digitale")
 /**
   * Tipologia di Bollo digitale  
  **/
  private TipoBolloEnum tipoBollo = null;
  
  // @Schema(requiredMode = RequiredMode.REQUIRED, description = "Digest in base64 del documento informatico associato alla marca da bollo")
 /**
   * Digest in base64 del documento informatico associato alla marca da bollo  
  **/
  private String hashDocumento = null;
  
  // @Schema(requiredMode = RequiredMode.REQUIRED, description = "Sigla automobilistica della provincia di residenza del soggetto pagatore")
 /**
   * Sigla automobilistica della provincia di residenza del soggetto pagatore  
  **/
  private String provinciaResidenza = null;
 /**
   * Tipologia di Bollo digitale
   * @return tipoBollo
  **/
  @JsonProperty("tipoBollo")
  @NotNull
  @Valid
  public String getTipoBollo() {
    return tipoBollo != null ? tipoBollo.getValue() : "";
  }

  public void setTipoBollo(TipoBolloEnum tipoBollo) {
    this.tipoBollo = tipoBollo;
  }

  public DatiEntrataBollo tipoBollo(TipoBolloEnum tipoBollo) {
    this.tipoBollo = tipoBollo;
    return this;
  }

 /**
   * Digest in base64 del documento informatico associato alla marca da bollo
   * @return hashDocumento
  **/
  @JsonProperty("hashDocumento")
  @NotNull
  @Valid
  public String getHashDocumento() {
    return hashDocumento;
  }

  public void setHashDocumento(String hashDocumento) {
    this.hashDocumento = hashDocumento;
  }

  public DatiEntrataBollo hashDocumento(String hashDocumento) {
    this.hashDocumento = hashDocumento;
    return this;
  }

 /**
   * Sigla automobilistica della provincia di residenza del soggetto pagatore
   * @return provinciaResidenza
  **/
  @JsonProperty("provinciaResidenza")
  @NotNull
  @Valid
  public String getProvinciaResidenza() {
    return provinciaResidenza;
  }

  public void setProvinciaResidenza(String provinciaResidenza) {
    this.provinciaResidenza = provinciaResidenza;
  }

  public DatiEntrataBollo provinciaResidenza(String provinciaResidenza) {
    this.provinciaResidenza = provinciaResidenza;
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DatiEntrataBollo {\n");
    
    sb.append("    tipoBollo: ").append(toIndentedString(tipoBollo)).append("\n");
    sb.append("    hashDocumento: ").append(toIndentedString(hashDocumento)).append("\n");
    sb.append("    provinciaResidenza: ").append(toIndentedString(provinciaResidenza)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private static String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
