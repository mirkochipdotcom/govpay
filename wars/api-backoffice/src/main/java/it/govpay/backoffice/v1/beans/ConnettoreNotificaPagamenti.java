package it.govpay.backoffice.v1.beans;


import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.govpay.backoffice.v1.controllers.ApplicazioniController;
import it.govpay.core.beans.JSONSerializable;
import it.govpay.core.exceptions.IOException;
import it.govpay.core.exceptions.ValidationException;
import it.govpay.core.utils.validator.CostantiValidazione;
import it.govpay.core.utils.validator.IValidable;
import it.govpay.core.utils.validator.ValidatorFactory;
import it.govpay.core.utils.validator.ValidatoreIdentificativi;
@com.fasterxml.jackson.annotation.JsonPropertyOrder({
"abilitato",
"codiceIPA",
"tipoConnettore",
"versioneCsv",
"webServiceUrl",
"webServiceAuth",
"emailIndirizzi",
"emailSubject",
"emailAllegato",
"downloadBaseUrl",
"fileSystemPath",
"tipiPendenza",
"intervalloCreazioneTracciato",
})
public class ConnettoreNotificaPagamenti extends JSONSerializable implements IValidable {

  @JsonProperty("abilitato")
  private Boolean abilitato = null;

  @JsonProperty("codiceIPA")
  private String codiceIPA = null;


  /**
   * Gets or Sets tipoConnettore
   */
  public enum TipoConnettoreEnum {




    WEBSERVICE("WEBSERVICE"),


    EMAIL("EMAIL"),


    FILESYSTEM("FILESYSTEM");




    private String value;

    TipoConnettoreEnum(String value) {
      this.value = value;
    }

    @Override
    @com.fasterxml.jackson.annotation.JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    public static TipoConnettoreEnum fromValue(String text) {
      for (TipoConnettoreEnum b : TipoConnettoreEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }



  @JsonProperty("tipoConnettore")
  private TipoConnettoreEnum tipoConnettore = null;


  /**
   * Versione del CSV prodotto.
   */
  public enum VersioneCsvEnum {




    _0("1.0");




    private String value;

    VersioneCsvEnum(String value) {
      this.value = value;
    }

    @Override
    @com.fasterxml.jackson.annotation.JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    public static VersioneCsvEnum fromValue(String text) {
      for (VersioneCsvEnum b : VersioneCsvEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }



  @JsonProperty("versioneCsv")
  private VersioneCsvEnum versioneCsv = null;

  @JsonProperty("webServiceUrl")
  private String webServiceUrl = null;

  @JsonProperty("webServiceAuth")
  private TipoAutenticazione webServiceAuth = null;

  @JsonProperty("emailIndirizzi")
  private List<String> emailIndirizzi = null;

  @JsonProperty("emailSubject")
  private String emailSubject = null;

  @JsonProperty("emailAllegato")
  private Boolean emailAllegato = null;

  @JsonProperty("downloadBaseUrl")
  private String downloadBaseUrl = null;

  @JsonProperty("fileSystemPath")
  private String fileSystemPath = null;

  @JsonProperty("tipiPendenza")
  private List<Object> tipiPendenza = null;

  @JsonProperty("intervalloCreazioneTracciato")
  private BigDecimal intervalloCreazioneTracciato = null;

  /**
   * Indica se il connettore e' abilitato
   **/
  public ConnettoreNotificaPagamenti abilitato(Boolean abilitato) {
    this.abilitato = abilitato;
    return this;
  }

  @JsonProperty("abilitato")
  public Boolean getAbilitato() {
    return abilitato;
  }
  public void setAbilitato(Boolean abilitato) {
    this.abilitato = abilitato;
  }

  /**
   * Codice IPA
   **/
  public ConnettoreNotificaPagamenti codiceIPA(String codiceIPA) {
    this.codiceIPA = codiceIPA;
    return this;
  }

  @JsonProperty("codiceIPA")
  public String getCodiceIPA() {
    return codiceIPA;
  }
  public void setCodiceIPA(String codiceIPA) {
    this.codiceIPA = codiceIPA;
  }

  /**
   **/
  public ConnettoreNotificaPagamenti tipoConnettore(TipoConnettoreEnum tipoConnettore) {
    this.tipoConnettore = tipoConnettore;
    return this;
  }

  @JsonProperty("tipoConnettore")
  public TipoConnettoreEnum getTipoConnettore() {
    return tipoConnettore;
  }
  public void setTipoConnettore(TipoConnettoreEnum tipoConnettore) {
    this.tipoConnettore = tipoConnettore;
  }

  /**
   * Versione del CSV prodotto.
   **/
  public ConnettoreNotificaPagamenti versioneCsv(VersioneCsvEnum versioneCsv) {
    this.versioneCsv = versioneCsv;
    return this;
  }

  @JsonProperty("versioneCsv")
  public VersioneCsvEnum getVersioneCsv() {
    return versioneCsv;
  }
  public void setVersioneCsv(VersioneCsvEnum versioneCsv) {
    this.versioneCsv = versioneCsv;
  }

  /**
   * Dati di integrazione al servizio web tracciati
   **/
  public ConnettoreNotificaPagamenti webServiceUrl(String webServiceUrl) {
    this.webServiceUrl = webServiceUrl;
    return this;
  }

  @JsonProperty("webServiceUrl")
  public String getWebServiceUrl() {
    return webServiceUrl;
  }
  public void setWebServiceUrl(String webServiceUrl) {
    this.webServiceUrl = webServiceUrl;
  }

  /**
   **/
  public ConnettoreNotificaPagamenti webServiceAuth(TipoAutenticazione webServiceAuth) {
    this.webServiceAuth = webServiceAuth;
    return this;
  }

  @JsonProperty("webServiceAuth")
  public TipoAutenticazione getWebServiceAuth() {
    return webServiceAuth;
  }
  public void setWebServiceAuth(TipoAutenticazione webServiceAuth) {
    this.webServiceAuth = webServiceAuth;
  }

  /**
   * Indirizzi Email al quale verra' spedito il tracciato
   **/
  public ConnettoreNotificaPagamenti emailIndirizzi(List<String> emailIndirizzi) {
    this.emailIndirizzi = emailIndirizzi;
    return this;
  }

  @JsonProperty("emailIndirizzi")
  public List<String> getEmailIndirizzi() {
    return emailIndirizzi;
  }
  public void setEmailIndirizzi(List<String> emailIndirizzi) {
    this.emailIndirizzi = emailIndirizzi;
  }

  /**
   * Subject da inserire nella mail
   **/
  public ConnettoreNotificaPagamenti emailSubject(String emailSubject) {
    this.emailSubject = emailSubject;
    return this;
  }

  @JsonProperty("emailSubject")
  public String getEmailSubject() {
    return emailSubject;
  }
  public void setEmailSubject(String emailSubject) {
    this.emailSubject = emailSubject;
  }

  /**
   * Indica se inviare il tracciato come allegato all'email oppure se inserire nel messaggio il link al download
   **/
  public ConnettoreNotificaPagamenti emailAllegato(Boolean emailAllegato) {
    this.emailAllegato = emailAllegato;
    return this;
  }

  @JsonProperty("emailAllegato")
  public Boolean getEmailAllegato() {
    return emailAllegato;
  }
  public void setEmailAllegato(Boolean emailAllegato) {
    this.emailAllegato = emailAllegato;
  }

  /**
   * URL base del link dove scaricare il tracciato
   **/
  public ConnettoreNotificaPagamenti downloadBaseUrl(String downloadBaseUrl) {
    this.downloadBaseUrl = downloadBaseUrl;
    return this;
  }

  @JsonProperty("downloadBaseUrl")
  public String getDownloadBaseUrl() {
    return downloadBaseUrl;
  }
  public void setDownloadBaseUrl(String downloadBaseUrl) {
    this.downloadBaseUrl = downloadBaseUrl;
  }

  /**
   * Path nel quale verra' salvato il tracciato
   **/
  public ConnettoreNotificaPagamenti fileSystemPath(String fileSystemPath) {
    this.fileSystemPath = fileSystemPath;
    return this;
  }

  @JsonProperty("fileSystemPath")
  public String getFileSystemPath() {
    return fileSystemPath;
  }
  public void setFileSystemPath(String fileSystemPath) {
    this.fileSystemPath = fileSystemPath;
  }

  /**
   * tipi pendenza da includere nel tracciato
   **/
  public ConnettoreNotificaPagamenti tipiPendenza(List<Object> tipiPendenza) {
    this.tipiPendenza = tipiPendenza;
    return this;
  }

  @JsonProperty("tipiPendenza")
  public List<Object> getTipiPendenza() {
    return tipiPendenza;
  }
  public void setTipiPendenza(List<Object> tipiPendenza) {
    this.tipiPendenza = tipiPendenza;
  }

  /**
   * intervallo di creazione del tracciato in ore
   **/
  public ConnettoreNotificaPagamenti intervalloCreazioneTracciato(BigDecimal intervalloCreazioneTracciato) {
    this.intervalloCreazioneTracciato = intervalloCreazioneTracciato;
    return this;
  }

  @JsonProperty("intervalloCreazioneTracciato")
  public BigDecimal getIntervalloCreazioneTracciato() {
    return intervalloCreazioneTracciato;
  }
  public void setIntervalloCreazioneTracciato(BigDecimal intervalloCreazioneTracciato) {
    this.intervalloCreazioneTracciato = intervalloCreazioneTracciato;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConnettoreNotificaPagamenti connettoreNotificaPagamenti = (ConnettoreNotificaPagamenti) o;
    return Objects.equals(abilitato, connettoreNotificaPagamenti.abilitato) &&
        Objects.equals(codiceIPA, connettoreNotificaPagamenti.codiceIPA) &&
        Objects.equals(tipoConnettore, connettoreNotificaPagamenti.tipoConnettore) &&
        Objects.equals(versioneCsv, connettoreNotificaPagamenti.versioneCsv) &&
        Objects.equals(webServiceUrl, connettoreNotificaPagamenti.webServiceUrl) &&
        Objects.equals(webServiceAuth, connettoreNotificaPagamenti.webServiceAuth) &&
        Objects.equals(emailIndirizzi, connettoreNotificaPagamenti.emailIndirizzi) &&
        Objects.equals(emailSubject, connettoreNotificaPagamenti.emailSubject) &&
        Objects.equals(emailAllegato, connettoreNotificaPagamenti.emailAllegato) &&
        Objects.equals(downloadBaseUrl, connettoreNotificaPagamenti.downloadBaseUrl) &&
        Objects.equals(fileSystemPath, connettoreNotificaPagamenti.fileSystemPath) &&
        Objects.equals(tipiPendenza, connettoreNotificaPagamenti.tipiPendenza) &&
        Objects.equals(intervalloCreazioneTracciato, connettoreNotificaPagamenti.intervalloCreazioneTracciato);
  }

  @Override
  public int hashCode() {
    return Objects.hash(abilitato, codiceIPA, tipoConnettore, versioneCsv, webServiceUrl, webServiceAuth, emailIndirizzi, emailSubject, emailAllegato, downloadBaseUrl, fileSystemPath, tipiPendenza, intervalloCreazioneTracciato);
  }

  public static ConnettoreNotificaPagamenti parse(String json) throws IOException {
    return parse(json, ConnettoreNotificaPagamenti.class);
  }

  @Override
  public String getJsonIdFilter() {
    return "connettoreNotificaPagamenti";
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConnettoreNotificaPagamenti {\n");

    sb.append("    abilitato: ").append(toIndentedString(abilitato)).append("\n");
    sb.append("    codiceIPA: ").append(toIndentedString(codiceIPA)).append("\n");
    sb.append("    tipoConnettore: ").append(toIndentedString(tipoConnettore)).append("\n");
    sb.append("    versioneCsv: ").append(toIndentedString(versioneCsv)).append("\n");
    sb.append("    webServiceUrl: ").append(toIndentedString(webServiceUrl)).append("\n");
    sb.append("    webServiceAuth: ").append(toIndentedString(webServiceAuth)).append("\n");
    sb.append("    emailIndirizzi: ").append(toIndentedString(emailIndirizzi)).append("\n");
    sb.append("    emailSubject: ").append(toIndentedString(emailSubject)).append("\n");
    sb.append("    emailAllegato: ").append(toIndentedString(emailAllegato)).append("\n");
    sb.append("    downloadBaseUrl: ").append(toIndentedString(downloadBaseUrl)).append("\n");
    sb.append("    fileSystemPath: ").append(toIndentedString(fileSystemPath)).append("\n");
    sb.append("    tipiPendenza: ").append(toIndentedString(tipiPendenza)).append("\n");
    sb.append("    intervalloCreazioneTracciato: ").append(toIndentedString(intervalloCreazioneTracciato)).append("\n");
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

  @Override
	public void validate() throws ValidationException {
		ValidatorFactory vf = ValidatorFactory.newInstance();
		vf.getValidator("abilitato", this.abilitato).notNull();

		if(this.abilitato) {
			vf.getValidator("tipoConnettore", this.tipoConnettore).notNull();
			vf.getValidator("versioneCsv", this.versioneCsv).notNull();
			vf.getValidator("codiceIPA", this.codiceIPA).notNull().minLength(1).maxLength(4000);
			vf.getValidator("intervalloCreazioneTracciato", this.intervalloCreazioneTracciato).notNull().min(BigDecimal.ONE);

			switch (this.tipoConnettore) {
			case EMAIL:
				if(this.emailIndirizzi != null && !this.emailIndirizzi.isEmpty()) {
					for (String indirizzo : emailIndirizzi) {
						vf.getValidator("emailIndirizzi", indirizzo).minLength(1).pattern(CostantiValidazione.PATTERN_EMAIL);
					}
					String v = StringUtils.join(this.emailIndirizzi, ",");
					vf.getValidator("emailIndirizzi", v).maxLength(4000);
				} else {
					throw new ValidationException("Il campo emailIndirizzi non deve essere vuoto.");
				}
				vf.getValidator("emailSubject", this.emailSubject).minLength(1).maxLength(4000);
				vf.getValidator("emailAllegato", this.emailAllegato).notNull();
				if(!this.emailAllegato) {
					vf.getValidator("downloadBaseUrl", this.downloadBaseUrl).notNull().pattern("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)");
				}
				break;
			case FILESYSTEM:
				vf.getValidator("fileSystemPath", this.fileSystemPath).notNull().minLength(1).maxLength(4000);
				break;
			case WEBSERVICE:
				vf.getValidator("webServiceUrl", this.webServiceUrl).notNull().minLength(1).maxLength(4000).isUrl();
				vf.getValidator("webServiceAuth", this.webServiceAuth).validateFields();
				break;
			}

			if(this.tipiPendenza != null && !this.tipiPendenza.isEmpty()) {
				ValidatoreIdentificativi validatoreId = ValidatoreIdentificativi.newInstance();
				for (Object object : this.tipiPendenza) {
					if(object instanceof String) {
						String idTipoPendenza = (String) object;
						if(!idTipoPendenza.equals(ApplicazioniController.AUTORIZZA_TIPI_PENDENZA_STAR))
							validatoreId.validaIdTipoVersamento("tipiPendenza", idTipoPendenza);
					} else if(object instanceof TipoPendenzaProfiloIndex) {
						TipoPendenzaProfiloIndex tipoPendenzaProfiloPost = (TipoPendenzaProfiloIndex) object;
						if(!tipoPendenzaProfiloPost.getIdTipoPendenza().equals(ApplicazioniController.AUTORIZZA_TIPI_PENDENZA_STAR))
							tipoPendenzaProfiloPost.validate();
					} else if(object instanceof java.util.LinkedHashMap) {
						java.util.LinkedHashMap<?,?> map = (LinkedHashMap<?,?>) object;

						TipoPendenzaProfiloIndex tipoPendenzaProfiloPost = new TipoPendenzaProfiloIndex();
						if(map.containsKey("idTipoPendenza"))
							tipoPendenzaProfiloPost.setIdTipoPendenza((String) map.get("idTipoPendenza"));
						if(map.containsKey("descrizione")) {
							tipoPendenzaProfiloPost.setDescrizione((String) map.get("descrizione"));
						}

						if(tipoPendenzaProfiloPost.getIdTipoPendenza() == null)
							validatoreId.validaIdDominio("idTipoPendenza", tipoPendenzaProfiloPost.getIdTipoPendenza());
						if(!tipoPendenzaProfiloPost.getIdTipoPendenza().equals(ApplicazioniController.AUTORIZZA_TIPI_PENDENZA_STAR))
							tipoPendenzaProfiloPost.validate();
					} else {
						throw new ValidationException("Tipo non valido per il campo domini");
					}
				}
			} else {
				throw new ValidationException("Indicare almeno un valore nel campo tipiPendenza");
			}
		}
	}
}



