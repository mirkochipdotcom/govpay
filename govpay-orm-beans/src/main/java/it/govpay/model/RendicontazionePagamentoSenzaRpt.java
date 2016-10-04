package it.govpay.model;

import it.govpay.bd.model.BasicModel;
import it.govpay.bd.model.Fr;
import it.govpay.bd.model.FrApplicazione;
import it.govpay.bd.model.RendicontazioneSenzaRpt;
import it.govpay.bd.model.SingoloVersamento;
import it.govpay.bd.model.Versamento;

public class RendicontazionePagamentoSenzaRpt extends BasicModel {

	private static final long serialVersionUID = 1L;
	
	private Fr fr;
	private FrApplicazione frApplicazione;
	private RendicontazioneSenzaRpt rendicontazioneSenzaRpt;
	private Versamento versamento;
	private SingoloVersamento singoloVersamento;
	
	public Fr getFr() {
		return fr;
	}
	public void setFr(Fr fr) {
		this.fr = fr;
	}
	public FrApplicazione getFrApplicazione() {
		return frApplicazione;
	}
	public void setFrApplicazione(FrApplicazione frApplicazione) {
		this.frApplicazione = frApplicazione;
	}
	public Versamento getVersamento() {
		return versamento;
	}
	public void setVersamento(Versamento versamento) {
		this.versamento = versamento;
	}
	public SingoloVersamento getSingoloVersamento() {
		return singoloVersamento;
	}
	public void setSingoloVersamento(SingoloVersamento singoloVersamento) {
		this.singoloVersamento = singoloVersamento;
	}
	public RendicontazioneSenzaRpt getRendicontazioneSenzaRpt() {
		return rendicontazioneSenzaRpt;
	}
	public void setRendicontazioneSenzaRpt(RendicontazioneSenzaRpt rendicontazioneSenzaRpt) {
		this.rendicontazioneSenzaRpt = rendicontazioneSenzaRpt;
	}
	

}
