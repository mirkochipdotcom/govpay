package it.govpay.core.rs.v1.beans.base;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonValue;

@org.codehaus.jackson.annotate.JsonPropertyOrder({
"stato",
"importo",
"idDominio",
"numeroAvviso",
"dataValidita",
"dataScadenza",
"descrizione",
"tassonomiaAvviso",
"qrcode",
"barcode",
})
public class Avviso extends it.govpay.core.rs.v1.beans.JSONSerializable {
  
    
  /**
   * Stato dell'avviso
   */
  public enum StatoEnum {
    
    
        
            
    PAGATO("Pagato"),
    
            
    NON_PAGATO("Non pagato"),
    
            
    SCADUTO("Scaduto"),
    
            
    ANNULLATO("Annullato");
            
        
    

    private String value;

    StatoEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    public static StatoEnum fromValue(String text) {
      for (StatoEnum b : StatoEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

    
    
  @JsonProperty("stato")
  private StatoEnum stato = null;
  
  @JsonProperty("importo")
  private BigDecimal importo = null;
  
  @JsonProperty("idDominio")
  private String idDominio = null;
  
  @JsonProperty("numeroAvviso")
  private String numeroAvviso = null;
  
  @JsonProperty("dataValidita")
  private Date dataValidita = null;
  
  @JsonProperty("dataScadenza")
  private Date dataScadenza = null;
  
  @JsonProperty("descrizione")
  private String descrizione = null;
  
  @JsonProperty("tassonomiaAvviso")
  private String tassonomiaAvviso = null;
  
  @JsonProperty("qrcode")
  private String qrcode = null;
  
  @JsonProperty("barcode")
  private String barcode = null;
  
  /**
   * Stato dell'avviso
   **/
  public Avviso stato(StatoEnum stato) {
    this.stato = stato;
    return this;
  }

  @JsonIgnore
  public StatoEnum getStatoEnum() {
    return stato;
  }
  public void setStato(StatoEnum stato) {
    this.stato = stato;
  }
  
  @JsonProperty("stato")
  public String getStato() {
	  if(stato != null) {
		  return stato.value;
	  } else {
		  return null;
	  }
    
  }
  public void setStato(String stato) throws Exception{
	  if(stato != null) {
		  this.stato = StatoEnum.fromValue(stato);
		  if(this.stato == null)
			  throw new Exception("valore ["+stato+"] non ammesso per la property stato");
	  }
  }

  /**
   * Importo della pendenza. Deve corrispondere alla somma delle singole voci.
   **/
  public Avviso importo(BigDecimal importo) {
    this.importo = importo;
    return this;
  }

  @JsonProperty("importo")
  public BigDecimal getImporto() {
    return importo;
  }
  public void setImporto(BigDecimal importo) {
    this.importo = importo;
  }

  /**
   * Identificativo del creditore dell'avviso
   **/
  public Avviso idDominio(String idDominio) {
    this.idDominio = idDominio;
    return this;
  }

  @JsonProperty("idDominio")
  public String getIdDominio() {
    return idDominio;
  }
  public void setIdDominio(String idDominio) {
    this.idDominio = idDominio;
  }

  /**
   * Identificativo univoco versamento, assegnato se pagabile da psp
   **/
  public Avviso numeroAvviso(String numeroAvviso) {
    this.numeroAvviso = numeroAvviso;
    return this;
  }

  @JsonProperty("numeroAvviso")
  public String getNumeroAvviso() {
    return numeroAvviso;
  }
  public void setNumeroAvviso(String numeroAvviso) {
    this.numeroAvviso = numeroAvviso;
  }

  /**
   * Data di validita dei dati della pendenza, decorsa la quale la pendenza può subire variazioni.
   **/
  public Avviso dataValidita(Date dataValidita) {
    this.dataValidita = dataValidita;
    return this;
  }

  @JsonProperty("dataValidita")
  public Date getDataValidita() {
    return dataValidita;
  }
  public void setDataValidita(Date dataValidita) {
    this.dataValidita = dataValidita;
  }

  /**
   * Data di scadenza della pendenza, decorsa la quale non è più pagabile.
   **/
  public Avviso dataScadenza(Date dataScadenza) {
    this.dataScadenza = dataScadenza;
    return this;
  }

  @JsonProperty("dataScadenza")
  public Date getDataScadenza() {
    return dataScadenza;
  }
  public void setDataScadenza(Date dataScadenza) {
    this.dataScadenza = dataScadenza;
  }

  /**
   * Descrizione da inserire nell'avviso di pagamento
   **/
  public Avviso descrizione(String descrizione) {
    this.descrizione = descrizione;
    return this;
  }

  @JsonProperty("descrizione")
  public String getDescrizione() {
    return descrizione;
  }
  public void setDescrizione(String descrizione) {
    this.descrizione = descrizione;
  }

  /**
   * Macro categoria della pendenza secondo la classificazione AgID
   **/
  public Avviso tassonomiaAvviso(String tassonomiaAvviso) {
    this.tassonomiaAvviso = tassonomiaAvviso;
    return this;
  }

  @JsonProperty("tassonomiaAvviso")
  public String getTassonomiaAvviso() {
    return tassonomiaAvviso;
  }
  public void setTassonomiaAvviso(String tassonomiaAvviso) {
    this.tassonomiaAvviso = tassonomiaAvviso;
  }

  /**
   * Testo da codificare nel qr-code dell'avviso
   **/
  public Avviso qrcode(String qrcode) {
    this.qrcode = qrcode;
    return this;
  }

  @JsonProperty("qrcode")
  public String getQrcode() {
    return qrcode;
  }
  public void setQrcode(String qrcode) {
    this.qrcode = qrcode;
  }

  /**
   * Testo da codificare nel bar-code dell'avviso
   **/
  public Avviso barcode(String barcode) {
    this.barcode = barcode;
    return this;
  }

  @JsonProperty("barcode")
  public String getBarcode() {
    return barcode;
  }
  public void setBarcode(String barcode) {
    this.barcode = barcode;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Avviso avviso = (Avviso) o;
    return Objects.equals(stato, avviso.stato) &&
        Objects.equals(importo, avviso.importo) &&
        Objects.equals(idDominio, avviso.idDominio) &&
        Objects.equals(numeroAvviso, avviso.numeroAvviso) &&
        Objects.equals(dataValidita, avviso.dataValidita) &&
        Objects.equals(dataScadenza, avviso.dataScadenza) &&
        Objects.equals(descrizione, avviso.descrizione) &&
        Objects.equals(tassonomiaAvviso, avviso.tassonomiaAvviso) &&
        Objects.equals(qrcode, avviso.qrcode) &&
        Objects.equals(barcode, avviso.barcode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stato, importo, idDominio, numeroAvviso, dataValidita, dataScadenza, descrizione, tassonomiaAvviso, qrcode, barcode);
  }

  public static Avviso parse(String json) {
    return (Avviso) parse(json, Avviso.class);
  }

  @Override
  public String getJsonIdFilter() {
    return "avviso";
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Avviso {\n");
    
    sb.append("    stato: ").append(toIndentedString(stato)).append("\n");
    sb.append("    importo: ").append(toIndentedString(importo)).append("\n");
    sb.append("    idDominio: ").append(toIndentedString(idDominio)).append("\n");
    sb.append("    numeroAvviso: ").append(toIndentedString(numeroAvviso)).append("\n");
    sb.append("    dataValidita: ").append(toIndentedString(dataValidita)).append("\n");
    sb.append("    dataScadenza: ").append(toIndentedString(dataScadenza)).append("\n");
    sb.append("    descrizione: ").append(toIndentedString(descrizione)).append("\n");
    sb.append("    tassonomiaAvviso: ").append(toIndentedString(tassonomiaAvviso)).append("\n");
    sb.append("    qrcode: ").append(toIndentedString(qrcode)).append("\n");
    sb.append("    barcode: ").append(toIndentedString(barcode)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}



