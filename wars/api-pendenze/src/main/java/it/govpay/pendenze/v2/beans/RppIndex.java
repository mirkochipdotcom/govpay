package it.govpay.pendenze.v2.beans;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.govpay.core.beans.JSONSerializable;
@com.fasterxml.jackson.annotation.JsonPropertyOrder({
"stato",
"dettaglioStato",
"segnalazioni",
"rpt",
"rt",
"pendenza",
})
public class RppIndex extends JSONSerializable {

  @JsonProperty("stato")
  private String stato = null;

  @JsonProperty("dettaglioStato")
  private String dettaglioStato = null;

  @JsonProperty("segnalazioni")
  private List<Segnalazione> segnalazioni = null;

  @JsonProperty("rpt")
  private Object rpt = null;

  @JsonProperty("rt")
  private Object rt = null;

  @JsonProperty("pendenza")
  private PendenzaIndex pendenza = null;

  /**
   * Stato della richiesta di pagamento sulla piattaforma PagoPA.
   **/
  public RppIndex stato(String stato) {
    this.stato = stato;
    return this;
  }

  @JsonProperty("stato")
  public String getStato() {
    return stato;
  }
  public void setStato(String stato) {
    this.stato = stato;
  }

  /**
   * Dettaglio fornito dal Nodo dei Pagamenti sullo stato della richiesta.
   **/
  public RppIndex dettaglioStato(String dettaglioStato) {
    this.dettaglioStato = dettaglioStato;
    return this;
  }

  @JsonProperty("dettaglioStato")
  public String getDettaglioStato() {
    return dettaglioStato;
  }
  public void setDettaglioStato(String dettaglioStato) {
    this.dettaglioStato = dettaglioStato;
  }

  /**
   **/
  public RppIndex segnalazioni(List<Segnalazione> segnalazioni) {
    this.segnalazioni = segnalazioni;
    return this;
  }

  @JsonProperty("segnalazioni")
  public List<Segnalazione> getSegnalazioni() {
    return segnalazioni;
  }
  public void setSegnalazioni(List<Segnalazione> segnalazioni) {
    this.segnalazioni = segnalazioni;
  }

  /**
   * Rpt inviata a PagoPa. {http://www.digitpa.gov.it/schemas/2011/Pagamenti/} ctRichiestaPagamentoTelematico
   **/
  public RppIndex rpt(Object rpt) {
    this.rpt = rpt;
    return this;
  }

  @JsonProperty("rpt")
  public Object getRpt() {
    return rpt;
  }
  public void setRpt(Object rpt) {
    this.rpt = rpt;
  }

  /**
   * Rt inviata da PagoPa. {http://www.digitpa.gov.it/schemas/2011/Pagamenti/} ctRicevutaTelematica
   **/
  public RppIndex rt(Object rt) {
    this.rt = rt;
    return this;
  }

  @JsonProperty("rt")
  public Object getRt() {
    return rt;
  }
  public void setRt(Object rt) {
    this.rt = rt;
  }

  /**
   **/
  public RppIndex pendenza(PendenzaIndex pendenza) {
    this.pendenza = pendenza;
    return this;
  }

  @JsonProperty("pendenza")
  public PendenzaIndex getPendenza() {
    return pendenza;
  }
  public void setPendenza(PendenzaIndex pendenza) {
    this.pendenza = pendenza;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RppIndex rppIndex = (RppIndex) o;
    return Objects.equals(stato, rppIndex.stato) &&
        Objects.equals(dettaglioStato, rppIndex.dettaglioStato) &&
        Objects.equals(segnalazioni, rppIndex.segnalazioni) &&
        Objects.equals(rpt, rppIndex.rpt) &&
        Objects.equals(rt, rppIndex.rt) &&
        Objects.equals(pendenza, rppIndex.pendenza);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stato, dettaglioStato, segnalazioni, rpt, rt, pendenza);
  }

  public static RppIndex parse(String json) throws it.govpay.core.exceptions.IOException {
    return parse(json, RppIndex.class);
  }

  @Override
  public String getJsonIdFilter() {
    return "rppIndex";
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RppIndex {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    stato: ").append(toIndentedString(stato)).append("\n");
    sb.append("    dettaglioStato: ").append(toIndentedString(dettaglioStato)).append("\n");
    sb.append("    segnalazioni: ").append(toIndentedString(segnalazioni)).append("\n");
    sb.append("    rpt: ").append(toIndentedString(rpt)).append("\n");
    sb.append("    rt: ").append(toIndentedString(rt)).append("\n");
    sb.append("    pendenza: ").append(toIndentedString(pendenza)).append("\n");
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



