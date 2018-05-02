/**
 * 
 */
package it.govpay.core.dao.pagamenti.dto;

import it.govpay.bd.model.Rpt;
import it.govpay.bd.model.SingoloVersamento;
import it.govpay.bd.model.UnitaOperativa;
import it.govpay.bd.model.Versamento;

import java.util.List;

import it.govpay.bd.model.Applicazione;
import it.govpay.bd.model.Dominio;

/**
 * @author Bussu Giovanni (bussu@link.it)
 * @author  $Author: bussu $
 * @version $ Rev: 12563 $, $Date: 01 feb 2018 $
 * 
 */
public class LeggiRptDTOResponse {

	private Rpt rpt;
	private Versamento versamento;
	private Applicazione applicazione;
	private List<SingoloVersamento> lstSingoliVersamenti;
	private UnitaOperativa unitaOperativa;
	private Dominio dominio;

	public Versamento getVersamento() {
		return versamento;
	}

	public void setVersamento(Versamento versamento) {
		this.versamento = versamento;
	}

	public Applicazione getApplicazione() {
		return applicazione;
	}

	public void setApplicazione(Applicazione applicazione) {
		this.applicazione = applicazione;
	}

	public Rpt getRpt() {
		return rpt;
	}

	public void setRpt(Rpt rpt) {
		this.rpt = rpt;
	}

	public List<SingoloVersamento> getLstSingoliVersamenti() {
		return lstSingoliVersamenti;
	}

	public void setLstSingoliVersamenti(List<SingoloVersamento> lstSingoliVersamenti) {
		this.lstSingoliVersamenti = lstSingoliVersamenti;
	}

	public UnitaOperativa getUnitaOperativa() {
		return unitaOperativa;
	}

	public void setUnitaOperativa(UnitaOperativa unitaOperativa) {
		this.unitaOperativa = unitaOperativa;
	}

	public Dominio getDominio() {
		return dominio;
	}

	public void setDominio(Dominio dominio) {
		this.dominio = dominio;
	}

}
