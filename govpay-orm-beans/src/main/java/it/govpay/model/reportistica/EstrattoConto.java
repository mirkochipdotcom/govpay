package it.govpay.model.reportistica;

import it.govpay.model.BasicModel;

public class EstrattoConto  extends BasicModel {
	
	public enum TipoEstrattoConto {
		CSV , PDF 
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String codDominio;
	private String nomeFile;
	private String meseAnno;
	private Integer mese;
	private Integer anno;
	private String ibanAccredito;
	private String formato ;
	private long id;
	private TipoEstrattoConto tipoEstrattoConto;
	
	public String getCodDominio() {
		return codDominio;
	}
	public void setCodDominio(String codDominio) {
		this.codDominio = codDominio;
	}
	public String getNomeFile() {
		return nomeFile;
	}
	public void setNomeFile(String nomeFile) {
		this.nomeFile = nomeFile;
	}
	public String getMeseAnno() {
		return meseAnno;
	}
	public void setMeseAnno(String meseAnno) {
		this.meseAnno = meseAnno;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public Integer getMese() {
		return mese;
	}
	public void setMese(Integer mese) {
		this.mese = mese;
	}
	public Integer getAnno() {
		return anno;
	}
	public void setAnno(Integer anno) {
		this.anno = anno;
	}
	public String getIbanAccredito() {
		return ibanAccredito;
	}
	public void setIbanAccredito(String ibanAccredito) {
		this.ibanAccredito = ibanAccredito;
	}
	public String getFormato() {
		return formato;
	}
	public void setFormato(String formato) {
		this.formato = formato;
	}
	public TipoEstrattoConto getTipoEstrattoConto() {
		return tipoEstrattoConto;
	}
	public void setTipoEstrattoConto(TipoEstrattoConto tipoEstrattoConto) {
		this.tipoEstrattoConto = tipoEstrattoConto;
	}
}
