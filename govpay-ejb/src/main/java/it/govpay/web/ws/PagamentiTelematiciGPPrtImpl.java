/*
 * GovPay - Porta di Accesso al Nodo dei Pagamenti SPC 
 * http://www.gov4j.it/govpay
 * 
 * Copyright (c) 2014-2016 Link.it srl (http://www.link.it).
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govpay.web.ws;

import java.util.List;

import it.govpay.bd.BasicBD;
import it.govpay.bd.anagrafica.AnagraficaManager;
import it.govpay.core.business.model.SceltaWISP;
import it.govpay.core.exceptions.GovPayException;
import it.govpay.core.utils.Gp21Utils;
import it.govpay.core.utils.GpContext;
import it.govpay.core.utils.GpThreadLocal;
import it.govpay.core.utils.IuvUtils;
import it.govpay.model.Dominio;
import it.govpay.model.Iuv;
import it.govpay.model.Portale;
import it.govpay.model.Rpt;
import it.govpay.model.Rr;
import it.govpay.model.Versamento;
import it.govpay.servizi.PagamentiTelematiciGPPrt;
import it.govpay.servizi.commons.Canale;
import it.govpay.servizi.commons.EsitoOperazione;
import it.govpay.servizi.commons.IuvGenerato;
import it.govpay.servizi.commons.StatoVersamento;
import it.govpay.servizi.commons.TipoVersamento;
import it.govpay.servizi.commons.TipoSceltaWisp;
import it.govpay.servizi.gpprt.GpAvviaRichiestaStorno;
import it.govpay.servizi.gpprt.GpAvviaRichiestaStornoResponse;
import it.govpay.servizi.gpprt.GpAvviaTransazionePagamento;
import it.govpay.servizi.gpprt.GpAvviaTransazionePagamentoResponse;
import it.govpay.servizi.gpprt.GpChiediListaPsp;
import it.govpay.servizi.gpprt.GpChiediListaPspResponse;
import it.govpay.servizi.gpprt.GpChiediListaVersamenti;
import it.govpay.servizi.gpprt.GpChiediListaVersamentiResponse;
import it.govpay.servizi.gpprt.GpChiediSceltaWisp;
import it.govpay.servizi.gpprt.GpChiediSceltaWispResponse;
import it.govpay.servizi.gpprt.GpChiediStatoRichiestaStorno;
import it.govpay.servizi.gpprt.GpChiediStatoRichiestaStornoResponse;
import it.govpay.servizi.gpprt.GpChiediStatoTransazione;
import it.govpay.servizi.gpprt.GpChiediStatoTransazioneResponse;
import it.govpay.servizi.gpprt.GpChiediStatoVersamento;
import it.govpay.servizi.gpprt.GpChiediStatoVersamentoResponse;
import it.govpay.servizi.gpprt.GpChiediStatoVersamentoResponse.SpezzoneCausaleStrutturata;

import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.utils.logger.beans.Property;
import org.openspcoop2.utils.logger.beans.proxy.Actor;

@WebService(serviceName = "PagamentiTelematiciGPPrtService",
endpointInterface = "it.govpay.servizi.PagamentiTelematiciGPPrt",
targetNamespace = "http://www.govpay.it/servizi/",
portName = "GPPrtPort",
wsdlLocation = "classpath:wsdl/GpPrt.wsdl")

@HandlerChain(file="../../../../handler-chains/handler-chain-gpws.xml")

@org.apache.cxf.annotations.SchemaValidation

public class PagamentiTelematiciGPPrtImpl implements PagamentiTelematiciGPPrt {
	
	@Resource
	WebServiceContext wsCtxt;
	
	private static Logger log = LogManager.getLogger();
	
	@Override
	public GpChiediListaPspResponse gpChiediListaPsp(GpChiediListaPsp bodyrichiesta) {
		log.info("Richiesta operazione gpChiediListaPsp");
		GpChiediListaPspResponse response = new GpChiediListaPspResponse();
		GpContext ctx = GpThreadLocal.get();
		BasicBD bd = null;
		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			Portale portaleAutenticato = getPortaleAutenticato(bd);
			ctx.log("gpprt.ricevutaRichiesta");
			
			it.govpay.core.business.Psp pspBusiness = new it.govpay.core.business.Psp(bd);
			response = pspBusiness.chiediListaPsp(portaleAutenticato);
			response.setCodEsitoOperazione(EsitoOperazione.OK);
			ctx.log("gpprt.ricevutaRichiestaOk");
		} catch (GovPayException e) {
			response.setCodEsitoOperazione(e.getCodEsito());
			response.setDescrizioneEsitoOperazione(e.getMessage());
			e.log(log);
			ctx.log("gpprt.ricevutaRichiestaKo", response.getCodEsitoOperazione().toString(), response.getDescrizioneEsitoOperazione());
		} catch (Exception e) {
			response.setCodEsitoOperazione(EsitoOperazione.INTERNAL);
			response.setDescrizioneEsitoOperazione(e.getMessage());
			new GovPayException(e).log(log);
			ctx.log("gpprt.ricevutaRichiestaKo", response.getCodEsitoOperazione().toString(), response.getDescrizioneEsitoOperazione());
		} finally {
			if(ctx != null) {
				ctx.setResult(response);
				ctx.log();
			}
			if(bd != null) bd.closeConnection();
		}
		response.setCodOperazione(ThreadContext.get("op"));
		return response;
	}
	
	@Override
	public GpAvviaTransazionePagamentoResponse gpAvviaTransazionePagamento(GpAvviaTransazionePagamento bodyrichiesta) {
		GpAvviaTransazionePagamentoResponse response = new GpAvviaTransazionePagamentoResponse();
		GpContext ctx = GpThreadLocal.get();
		BasicBD bd = null;
		try {
			log.info("Richiesta operazione gpAvviaTransazionePagamento");
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			
			Portale portaleAutenticato = getPortaleAutenticato(bd);
			ctx.log("gpprt.ricevutaRichiesta");
			
			autorizzaPortale(bodyrichiesta.getCodPortale(), portaleAutenticato, bd);
			ctx.log("gpprt.autorizzazione");
			
			it.govpay.model.Canale canale = null;
			
			if(bodyrichiesta.getCanale() == null) {
				it.govpay.core.business.Wisp wisp = new it.govpay.core.business.Wisp(bd);
				
				Dominio dominio = null;
				try {
					dominio = AnagraficaManager.getDominio(bd, bodyrichiesta.getSceltaWisp().getCodDominio());
				} catch (NotFoundException e) {
					throw new GovPayException(EsitoOperazione.DOM_000, bodyrichiesta.getSceltaWisp().getCodDominio());
				}
				
				SceltaWISP scelta = wisp.chiediScelta(portaleAutenticato, dominio, bodyrichiesta.getSceltaWisp().getCodKeyPA(), bodyrichiesta.getSceltaWisp().getCodKeyWISP());
				if(scelta.isSceltaEffettuata() && !scelta.isPagaDopo()) {
					canale = scelta.getCanale();
				}
				if(!scelta.isSceltaEffettuata()) {
					throw new GovPayException(EsitoOperazione.WISP_003);
				}
				if(scelta.isPagaDopo()) { 
					throw new GovPayException(EsitoOperazione.WISP_004);
				}
			} else {
				try {
					it.govpay.model.Canale.TipoVersamento tipoVersamento = it.govpay.model.Canale.TipoVersamento.toEnum(bodyrichiesta.getCanale().getTipoVersamento().toString());
					canale = AnagraficaManager.getCanale(bd, bodyrichiesta.getCanale().getCodPsp(), bodyrichiesta.getCanale().getCodCanale(), tipoVersamento);
				} catch (NotFoundException e) {
					throw new GovPayException(EsitoOperazione.PSP_000, bodyrichiesta.getCanale().getCodPsp(), bodyrichiesta.getCanale().getCodCanale(), bodyrichiesta.getCanale().getTipoVersamento().toString());
				}
			}
			
			ctx.getContext().getRequest().addGenericProperty(new Property("codPsp", canale.getPsp(bd).getCodPsp()));
			ctx.getContext().getRequest().addGenericProperty(new Property("codCanale", canale.getCodCanale()));
			ctx.getContext().getRequest().addGenericProperty(new Property("tipoVersamento", canale.getTipoVersamento().getCodifica()));
			ctx.log("gpprt.identificazioneCanale");
			
			it.govpay.core.business.Pagamento pagamentoBusiness = new it.govpay.core.business.Pagamento(bd);
			response = pagamentoBusiness.avviaTransazione(portaleAutenticato, bodyrichiesta, canale);
			response.setCodEsitoOperazione(EsitoOperazione.OK);
			ctx.log("gpprt.ricevutaRichiestaOk");
		} catch (GovPayException e) {
			response.setCodEsitoOperazione(e.getCodEsito());
			response.setDescrizioneEsitoOperazione(e.getMessage());
			e.log(log);
			ctx.log("gpprt.ricevutaRichiestaKo", response.getCodEsitoOperazione().toString(), response.getDescrizioneEsitoOperazione());
		} catch (Exception e) {
			response.setCodEsitoOperazione(EsitoOperazione.INTERNAL);
			response.setDescrizioneEsitoOperazione(e.getMessage());
			new GovPayException(e).log(log);
			ctx.log("gpprt.ricevutaRichiestaKo", response.getCodEsitoOperazione().toString(), response.getDescrizioneEsitoOperazione());
		} finally {
			if(ctx != null) {
				ctx.setResult(response);
				ctx.log();
			}
			if(bd != null) bd.closeConnection();
		}
		response.setCodOperazione(ThreadContext.get("op"));
		return response;
	}

	@Override
	public GpChiediStatoTransazioneResponse gpChiediStatoTransazione(GpChiediStatoTransazione bodyrichiesta) {
		log.info("Richiesta operazione gpChiediStatoTransazione per la transazione con dominio (" + bodyrichiesta.getCodDominio() + ") iuv (" +  bodyrichiesta.getIuv()+") e ccp (" + bodyrichiesta.getCcp() + ")");
		GpContext ctx = GpThreadLocal.get();
		GpChiediStatoTransazioneResponse response = new GpChiediStatoTransazioneResponse();
		BasicBD bd = null;
		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			
			Portale portaleAutenticato = getPortaleAutenticato(bd);
			ctx.log("gpprt.ricevutaRichiesta");
			
			autorizzaPortale(bodyrichiesta.getCodPortale(), portaleAutenticato, bd);
			ctx.log("gpprt.autorizzazione");
			
			it.govpay.core.business.Pagamento pagamentoBusiness = new it.govpay.core.business.Pagamento(bd);
			Rpt rpt = pagamentoBusiness.chiediTransazione(portaleAutenticato, bodyrichiesta.getCodDominio(), bodyrichiesta.getIuv(), bodyrichiesta.getCcp());
			response.setCodEsitoOperazione(EsitoOperazione.OK);
			response.setTransazione(Gp21Utils.toTransazione(portaleAutenticato.getVersione(), rpt, bd));
			ctx.log("gpprt.ricevutaRichiestaOk");
		} catch (GovPayException e) {
			response.setCodEsitoOperazione(e.getCodEsito());
			response.setDescrizioneEsitoOperazione(e.getMessage());
			e.log(log);
			ctx.log("gpprt.ricevutaRichiestaKo", response.getCodEsitoOperazione().toString(), response.getDescrizioneEsitoOperazione());
		} catch (Exception e) {
			response.setCodEsitoOperazione(EsitoOperazione.INTERNAL);
			response.setDescrizioneEsitoOperazione(e.getMessage());
			new GovPayException(e).log(log);
			ctx.log("gpprt.ricevutaRichiestaKo", response.getCodEsitoOperazione().toString(), response.getDescrizioneEsitoOperazione());
		} finally {
			if(ctx != null) {
				ctx.setResult(response);
				ctx.log();
			}
			if(bd != null) bd.closeConnection();
		}
		response.setCodOperazione(ThreadContext.get("op"));
		return response;
	}

	@Override
	public GpChiediSceltaWispResponse gpChiediSceltaWisp(GpChiediSceltaWisp bodyrichiesta) {
		log.info("Richiesta operazione gpChiediSceltaWisp dal portale (" +  bodyrichiesta.getCodPortale() +")");
		GpContext ctx = GpThreadLocal.get();
		GpChiediSceltaWispResponse response = new GpChiediSceltaWispResponse();
		BasicBD bd = null;
		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			
			Portale portaleAutenticato = getPortaleAutenticato(bd);
			ctx.log("gpprt.ricevutaRichiesta");
			
			autorizzaPortale(bodyrichiesta.getCodPortale(), portaleAutenticato, bd);
			ctx.log("gpprt.autorizzazione");
			
			it.govpay.core.business.Wisp wisp = new it.govpay.core.business.Wisp(bd);
			
			Dominio dominio = null;
			try {
				dominio = AnagraficaManager.getDominio(bd, bodyrichiesta.getCodDominio());
			} catch (NotFoundException e) {
				throw new GovPayException(EsitoOperazione.DOM_000, bodyrichiesta.getCodDominio());
			}
			SceltaWISP scelta = wisp.chiediScelta(portaleAutenticato, dominio, bodyrichiesta.getCodKeyPA(), bodyrichiesta.getCodKeyWISP());
			if(scelta.isSceltaEffettuata()) {
				if(scelta.isPagaDopo()) {
					response.setScelta(TipoSceltaWisp.PAGA_DOPO);
				} else {
					response.setScelta(TipoSceltaWisp.SI);
					Canale canale = new Canale();
					canale.setCodCanale(scelta.getCanale().getCodCanale());
					canale.setCodPsp(scelta.getCanale().getPsp(bd).getCodPsp());
					canale.setTipoVersamento(TipoVersamento.fromValue(scelta.getCanale().getTipoVersamento().getCodifica()));
					response.setCanale(canale);
				}
			} else {
				response.setScelta(TipoSceltaWisp.NO);
			}
			response.setCodEsitoOperazione(EsitoOperazione.OK);
			ctx.log("gpprt.ricevutaRichiestaOk");
			log.info("SceltaWISP recuperata (" + response.getScelta() + ")");
		} catch (GovPayException e) {
			response.setCodEsitoOperazione(e.getCodEsito());
			response.setDescrizioneEsitoOperazione(e.getMessage());
			e.log(log);
			ctx.log("gpprt.ricevutaRichiestaKo", response.getCodEsitoOperazione().toString(), response.getDescrizioneEsitoOperazione());
		} catch (Exception e) {
			response.setCodEsitoOperazione(EsitoOperazione.INTERNAL);
			response.setDescrizioneEsitoOperazione(e.getMessage());
			new GovPayException(e).log(log);
			ctx.log("gpprt.ricevutaRichiestaKo", response.getCodEsitoOperazione().toString(), response.getDescrizioneEsitoOperazione());
		} finally {
			if(ctx != null) {
				ctx.setResult(response);
				ctx.log();
			}
			if(bd != null) bd.closeConnection();
		}
		response.setCodOperazione(ThreadContext.get("op"));
		return response;
	}


	@Override
	public GpChiediListaVersamentiResponse gpChiediListaVersamenti(GpChiediListaVersamenti bodyrichiesta) {
		log.info("Richiesta operazione gpChiediListaVersamenti dal portale (" +  bodyrichiesta.getCodPortale() +") per il debitore (" + bodyrichiesta.getCodUnivocoDebitore() + ")");
		GpContext ctx = GpThreadLocal.get();
		GpChiediListaVersamentiResponse response = new GpChiediListaVersamentiResponse();
		BasicBD bd = null;
		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			
			Portale portaleAutenticato = getPortaleAutenticato(bd);
			ctx.log("gpprt.ricevutaRichiesta");
			
			autorizzaPortale(bodyrichiesta.getCodPortale(), portaleAutenticato, bd);
			ctx.log("gpprt.autorizzazione");
			
			it.govpay.core.business.Versamento versamentoBusiness = new it.govpay.core.business.Versamento(bd);
			List<Versamento> versamenti = versamentoBusiness.chiediVersamenti(portaleAutenticato, bodyrichiesta.getCodPortale(), bodyrichiesta.getCodUnivocoDebitore());
			response.setCodEsitoOperazione(EsitoOperazione.OK);
			for(Versamento versamento : versamenti) {
				response.getVersamento().add(Gp21Utils.toVersamento(portaleAutenticato.getVersione(), versamento, bd));
			}
			ctx.log("gpprt.ricevutaRichiestaOk");
		} catch (GovPayException e) {
			response.setCodEsitoOperazione(e.getCodEsito());
			response.setDescrizioneEsitoOperazione(e.getMessage());
			e.log(log);
			ctx.log("gpprt.ricevutaRichiestaKo", response.getCodEsitoOperazione().toString(), response.getDescrizioneEsitoOperazione());
		} catch (Exception e) {
			response.setCodEsitoOperazione(EsitoOperazione.INTERNAL);
			response.setDescrizioneEsitoOperazione(e.getMessage());
			new GovPayException(e).log(log);
			ctx.log("gpprt.ricevutaRichiestaKo", response.getCodEsitoOperazione().toString(), response.getDescrizioneEsitoOperazione());
		} finally {
			if(ctx != null) {
				ctx.setResult(response);
				ctx.log();
			}
			if(bd != null) bd.closeConnection();
		}
		response.setCodOperazione(ThreadContext.get("op"));
		return response;
	}
	
	@Override
	public GpAvviaRichiestaStornoResponse gpAvviaRichiestaStorno(GpAvviaRichiestaStorno bodyrichiesta) {
		log.info("Richiesta operazione gpAvviaTransazionePagamento dal portale (" +  bodyrichiesta.getCodPortale() +")");
		GpContext ctx = GpThreadLocal.get();
		GpAvviaRichiestaStornoResponse response = new GpAvviaRichiestaStornoResponse();
		BasicBD bd = null;
		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			
			ctx.getContext().getRequest().addGenericProperty(new Property("codDominio", bodyrichiesta.getCodDominio()));
			ctx.getContext().getRequest().addGenericProperty(new Property("iuv", bodyrichiesta.getIuv()));
			ctx.getContext().getRequest().addGenericProperty(new Property("ccp", bodyrichiesta.getCcp()));
			
			Portale portaleAutenticato = getPortaleAutenticato(bd);
			ctx.log("gpprt.ricevutaRichiestaStorno");
			
			autorizzaPortale(bodyrichiesta.getCodPortale(), portaleAutenticato, bd);
			ctx.log("gpprt.autorizzazione");
			
			it.govpay.core.business.Pagamento pagamentoBusiness = new it.govpay.core.business.Pagamento(bd);
			response = pagamentoBusiness.avviaStorno(portaleAutenticato, bodyrichiesta);
			response.setCodEsitoOperazione(EsitoOperazione.OK);
			ctx.log("gpprt.ricevutaRichiestaOk");
		} catch (GovPayException e) {
			response.setCodEsitoOperazione(e.getCodEsito());
			response.setDescrizioneEsitoOperazione(e.getMessage());
			e.log(log);
			ctx.log("gpprt.ricevutaRichiestaKo", response.getCodEsitoOperazione().toString(), response.getDescrizioneEsitoOperazione());
		} catch (Exception e) {
			response.setCodEsitoOperazione(EsitoOperazione.INTERNAL);
			response.setDescrizioneEsitoOperazione(e.getMessage());
			new GovPayException(e).log(log);
			ctx.log("gpprt.ricevutaRichiestaKo", response.getCodEsitoOperazione().toString(), response.getDescrizioneEsitoOperazione());
		} finally {
			if(ctx != null) {
				ctx.setResult(response);
				ctx.log();
			}
			if(bd != null) bd.closeConnection();
		}
		response.setCodOperazione(ThreadContext.get("op"));
		return response;
	}

	@Override
	public GpChiediStatoRichiestaStornoResponse gpChiediStatoRichiestaStorno(GpChiediStatoRichiestaStorno bodyrichiesta) {
		log.info("Richiesta operazione gpChiediStatoRichiestaStorno per la richiesta (" + bodyrichiesta.getCodRichiestaStorno() + ")");
		GpContext ctx = GpThreadLocal.get();
		GpChiediStatoRichiestaStornoResponse response = new GpChiediStatoRichiestaStornoResponse();
		BasicBD bd = null;
		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			
			Portale portaleAutenticato = getPortaleAutenticato(bd);
			ctx.log("gpprt.ricevutaRichiesta");
			
			autorizzaPortale(bodyrichiesta.getCodPortale(), portaleAutenticato, bd);
			ctx.log("gpprt.autorizzazione");
			
			it.govpay.core.business.Pagamento pagamentoBusiness = new it.govpay.core.business.Pagamento(bd);
			Rr rr = pagamentoBusiness.chiediStorno(portaleAutenticato, bodyrichiesta.getCodRichiestaStorno());
			response.setCodEsitoOperazione(EsitoOperazione.OK);
			response.setStorno(Gp21Utils.toStorno(rr, portaleAutenticato.getVersione(), bd));
			ctx.log("gpprt.ricevutaRichiestaOk");
		} catch (GovPayException e) {
			response.setCodEsitoOperazione(e.getCodEsito());
			response.setDescrizioneEsitoOperazione(e.getMessage());
			e.log(log);
			ctx.log("gpprt.ricevutaRichiestaKo", response.getCodEsitoOperazione().toString(), response.getDescrizioneEsitoOperazione());
		} catch (Exception e) {
			response.setCodEsitoOperazione(EsitoOperazione.INTERNAL);
			response.setDescrizioneEsitoOperazione(e.getMessage());
			new GovPayException(e).log(log);
			ctx.log("gpprt.ricevutaRichiestaKo", response.getCodEsitoOperazione().toString(), response.getDescrizioneEsitoOperazione());
		} finally {
			if(ctx != null) {
				ctx.setResult(response);
				ctx.log();
			}
			if(bd != null) bd.closeConnection();
		}
		response.setCodOperazione(ThreadContext.get("op"));
		return response;
	}
	
	@Override
	public GpChiediStatoVersamentoResponse gpChiediStatoVersamento(GpChiediStatoVersamento bodyrichiesta) {
		log.info("Richiesta operazione gpChiediStatoVersamento");
		GpContext ctx = GpThreadLocal.get();
		GpChiediStatoVersamentoResponse response = new GpChiediStatoVersamentoResponse();
		BasicBD bd = null;
		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			
			Portale portaleAutenticato = getPortaleAutenticato(bd);
			ctx.log("gpprt.ricevutaRichiesta");
			
			autorizzaPortale(bodyrichiesta.getCodPortale(), portaleAutenticato, bd);
			ctx.log("gpprt.autorizzazione");
		
			it.govpay.core.business.Versamento versamentoBusiness = new it.govpay.core.business.Versamento(bd);
			
			Versamento versamento = null;
			
			if(bodyrichiesta.getIuv() != null) {
				log.info("Richiesta operazione gpChiediStatoVersamento per lo iuv (" + bodyrichiesta.getIuv() + ") del dominio (" +  bodyrichiesta.getCodDominio()+")");
				versamento = versamentoBusiness.chiediVersamentoByIuv(portaleAutenticato, bodyrichiesta.getCodDominio(), bodyrichiesta.getIuv());
			} else if(bodyrichiesta.getBundleKey() != null) {
				log.info("Richiesta operazione gpChiediStatoVersamento per la bundleKey (" + bodyrichiesta.getBundleKey() + ")");
				versamento = versamentoBusiness.chiediVersamento(portaleAutenticato, bodyrichiesta.getBundleKey());
			} else {
				log.info("Richiesta operazione gpChiediStatoVersamento per il versamento (" + bodyrichiesta.getCodVersamentoEnte() + ") dell'applicazione (" +  bodyrichiesta.getCodApplicazione()+")");
				versamento = versamentoBusiness.chiediVersamento(portaleAutenticato, bodyrichiesta.getCodApplicazione(), bodyrichiesta.getCodVersamentoEnte());
			}
			
			if(bodyrichiesta.getCodUnivocoDebitore() != null && !bodyrichiesta.getCodUnivocoDebitore().equalsIgnoreCase(versamento.getAnagraficaDebitore().getCodUnivoco())) {
				throw new GovPayException(EsitoOperazione.PRT_005);
			}
			
			response.setCodApplicazione(versamento.getApplicazione(bd).getCodApplicazione());
			response.setCodDominio(versamento.getUo(bd).getDominio(bd).getCodDominio());
			response.setCodEsitoOperazione(EsitoOperazione.OK);
			response.setCodVersamentoEnte(versamento.getCodVersamentoEnte());
			response.setDataScadenza(versamento.getDataScadenza());
			if(versamento.getSingoliVersamenti(bd).size() == 1) {
				response.setIbanAccredito(versamento.getSingoliVersamenti(bd).get(0).getIbanAccredito(bd).getCodIban());
			}
			response.setImportoTotale(versamento.getImportoTotale());
			response.setStato(StatoVersamento.valueOf(versamento.getStatoVersamento().toString()));
			
			if(versamento.getCausaleVersamento() instanceof Versamento.CausaleSemplice)
				response.setCausale(((Versamento.CausaleSemplice) versamento.getCausaleVersamento()).getCausale());
			if(versamento.getCausaleVersamento() instanceof Versamento.CausaleSpezzoni)
				response.getSpezzoneCausale().addAll(((Versamento.CausaleSpezzoni) versamento.getCausaleVersamento()).getSpezzoni());
			if(versamento.getCausaleVersamento() instanceof Versamento.CausaleSpezzoniStrutturati) {
				Versamento.CausaleSpezzoniStrutturati c = (Versamento.CausaleSpezzoniStrutturati) versamento.getCausaleVersamento();
				for(int i = 0; i<c.getImporti().size(); i++) {
					SpezzoneCausaleStrutturata s = new SpezzoneCausaleStrutturata();
					s.setCausale(c.getSpezzoni().get(i));
					s.setImporto(c.getImporti().get(i));
					response.getSpezzoneCausaleStrutturata().add(s);
				}
			}
			
			Iuv iuv = versamento.getIuv(bd);
			if(iuv != null) {
				IuvGenerato iuvGenerato = IuvUtils.toIuvGenerato(versamento.getApplicazione(bd), versamento.getUo(bd).getDominio(bd), iuv, versamento.getImportoTotale());
				response.setIuv(iuv.getIuv());
				response.setBarCode(iuvGenerato.getBarCode());
				response.setQrCode(iuvGenerato.getQrCode());
			}
			
			List<Rpt> rpts = versamento.getRpt(bd);
			for(Rpt rpt : rpts) {
				response.getTransazione().add(Gp21Utils.toTransazione(portaleAutenticato.getVersione(), rpt, bd));
			}
			ctx.log("gpapp.ricevutaRichiestaOk");
		} catch (GovPayException e) {
			response.setCodEsitoOperazione(e.getCodEsito());
			response.setDescrizioneEsitoOperazione(e.getMessage());
			e.log(log);
			ctx.log("gpapp.ricevutaRichiestaKo", response.getCodEsitoOperazione().toString(), response.getDescrizioneEsitoOperazione());
		} catch (Exception e) {
			response.setCodEsitoOperazione(EsitoOperazione.INTERNAL);
			response.setDescrizioneEsitoOperazione(e.getMessage());
			new GovPayException(e).log(log);
			ctx.log("gpapp.ricevutaRichiestaKo", response.getCodEsitoOperazione().toString(), response.getDescrizioneEsitoOperazione());
		} finally {
			if(ctx != null) {
				ctx.setResult(response);
				ctx.log();
			}
			if(bd != null) bd.closeConnection();
		}
		response.setCodOperazione(ThreadContext.get("op"));
		return response;
	}
	
	private Portale getPortaleAutenticato(BasicBD bd) throws GovPayException, ServiceException {
		if(wsCtxt.getUserPrincipal() == null) {
			throw new GovPayException(EsitoOperazione.AUT_000);
		}
		
		Portale prt = null;
		try {
			prt =  AnagraficaManager.getPortaleByPrincipal(bd, wsCtxt.getUserPrincipal().getName());
		} catch (NotFoundException e) {
			throw new GovPayException(EsitoOperazione.AUT_002, wsCtxt.getUserPrincipal().getName());
		}
		
		if(prt != null) {
			Actor from = new Actor();
			from.setName(prt.getCodPortale());
			from.setType(GpContext.TIPO_SOGGETTO_PRT);
			GpThreadLocal.get().getTransaction().setFrom(from);
			GpThreadLocal.get().getTransaction().getClient().setName(prt.getCodPortale());
		}
		
		return prt;
	}
	
	private void autorizzaPortale(String codPortale, Portale portaleAutenticato, BasicBD bd) throws GovPayException, ServiceException {
		Portale portale = null;
		try {
			portale = AnagraficaManager.getPortale(bd, codPortale);
		} catch (NotFoundException e) {
			throw new GovPayException(EsitoOperazione.PRT_000, codPortale);
		}

		if(!portale.isAbilitato())
			throw new GovPayException(EsitoOperazione.PRT_001, codPortale);

		if(!portale.getCodPortale().equals(portaleAutenticato.getCodPortale()))
			throw new GovPayException(EsitoOperazione.PRT_002, portaleAutenticato.getCodPortale(), codPortale);
	}
}
