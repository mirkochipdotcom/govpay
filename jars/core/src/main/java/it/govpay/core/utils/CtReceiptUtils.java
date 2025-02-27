package it.govpay.core.utils;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.openspcoop2.generic_project.exception.MultipleResultException;
import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.utils.LoggerWrapperFactory;
import org.openspcoop2.utils.UtilsException;
import org.openspcoop2.utils.logger.beans.Property;
import org.openspcoop2.utils.service.context.ContextThreadLocal;
import org.openspcoop2.utils.service.context.IContext;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

import it.gov.pagopa.pagopa_api.pa.pafornode.CtPaymentPA;
import it.gov.pagopa.pagopa_api.pa.pafornode.CtPaymentPAV2;
import it.gov.pagopa.pagopa_api.pa.pafornode.CtReceipt;
import it.gov.pagopa.pagopa_api.pa.pafornode.CtReceiptV2;
import it.gov.pagopa.pagopa_api.pa.pafornode.CtSubject;
import it.gov.pagopa.pagopa_api.pa.pafornode.CtTransferPA;
import it.gov.pagopa.pagopa_api.pa.pafornode.CtTransferPAReceiptV2;
import it.gov.pagopa.pagopa_api.pa.pafornode.CtTransferPAV2;
import it.gov.pagopa.pagopa_api.pa.pafornode.PaGetPaymentRes;
import it.gov.pagopa.pagopa_api.pa.pafornode.PaGetPaymentV2Response;
import it.gov.pagopa.pagopa_api.pa.pafornode.PaSendRTReq;
import it.gov.pagopa.pagopa_api.pa.pafornode.PaSendRTV2Request;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.govpay.bd.BDConfigWrapper;
import it.govpay.bd.anagrafica.AnagraficaManager;
import it.govpay.bd.model.Dominio;
import it.govpay.bd.model.Notifica;
import it.govpay.bd.model.Pagamento;
import it.govpay.bd.model.Rpt;
import it.govpay.bd.model.SingoloVersamento;
import it.govpay.bd.model.Versamento;
import it.govpay.bd.pagamento.PagamentiBD;
import it.govpay.bd.pagamento.RptBD;
import it.govpay.bd.pagamento.VersamentiBD;
import it.govpay.bd.pagamento.filters.RptFilter;
import it.govpay.core.exceptions.GovPayException;
import it.govpay.core.exceptions.IOException;
import it.govpay.core.exceptions.NdpException;
import it.govpay.core.exceptions.NdpException.FaultPa;
import it.govpay.core.exceptions.NotificaException;
import it.govpay.core.utils.RtUtils.EsitoValidazione;
import it.govpay.core.utils.thread.InviaNotificaThread;
import it.govpay.core.utils.thread.ThreadExecutorManager;
import it.govpay.model.Canale.ModelloPagamento;
import it.govpay.model.Notifica.TipoNotifica;
import it.govpay.model.Pagamento.Stato;
import it.govpay.model.Rpt.StatoRpt;
import it.govpay.model.Rpt.TipoIdentificativoAttestante;
import it.govpay.model.SingoloVersamento.StatoSingoloVersamento;
import it.govpay.pagopa.beans.utils.JaxbUtils;

public class CtReceiptUtils  extends NdpValidationUtils {

	private static Logger log = LoggerWrapperFactory.getLogger(RtUtils.class);

	public static EsitoValidazione validaSemantica(PaGetPaymentRes ctRpt, PaSendRTReq ctRt) {
		CtPaymentPA ctPaymentPA = ctRpt.getData();
		CtReceipt ctReceipt = ctRt.getReceipt();

		EsitoValidazione esito = new RtUtils().new EsitoValidazione();
		valida(ctPaymentPA.getCreditorReferenceId(), ctReceipt.getCreditorReferenceId(), esito, "CreditorReferenceId non corrisponde", true); // Identificativo di correlazione dei due messaggi lo IUV???

		validaSemantica(ctPaymentPA.getDebtor(), ctReceipt.getDebtor(), esito);

		StOutcome ctRecepitOutcome = ctReceipt.getOutcome(); // esito pagamento ha solo due valori OK/KO
		String name = ctRecepitOutcome.name();
		switch (ctRecepitOutcome) {
		case OK:
			if(ctReceipt.getTransferList().getTransfer().size() != ctPaymentPA.getTransferList().getTransfer().size()) {
				esito.addErrore(MessageFormat.format("Numero di pagamenti diverso dal numero di versamenti per una ricevuta di tipo {0}", name), true);
				return esito;
			}
			break;
		case KO:
			if(ctReceipt.getTransferList().getTransfer().size() != 0 && ctReceipt.getTransferList().getTransfer().size() != ctPaymentPA.getTransferList().getTransfer().size()) {
				esito.addErrore(MessageFormat.format("Numero di pagamenti diverso dal numero di versamenti per una ricevuta di tipo {0}", name), true);
				return esito;
			}
			break;
		}

		BigDecimal importoTotaleCalcolato = BigDecimal.ZERO;

		for (int i = 0; i < ctPaymentPA.getTransferList().getTransfer().size(); i++) {

			CtTransferPA singoloVersamento = ctPaymentPA.getTransferList().getTransfer().get(i);
			CtTransferPA singoloPagamento = null; 
			if(ctReceipt.getTransferList().getTransfer().size() != 0) {
				singoloPagamento = ctReceipt.getTransferList().getTransfer().get(i);
				validaSemanticaSingoloVersamento(singoloVersamento, singoloPagamento, (i+1), esito);
				importoTotaleCalcolato = importoTotaleCalcolato.add(singoloPagamento.getTransferAmount());
			}
		}

		BigDecimal paymentAmount = ctReceipt.getPaymentAmount();
		if (importoTotaleCalcolato.compareTo(paymentAmount) != 0)
			esito.addErrore(MessageFormat.format("ImportoTotalePagato [{0}] non corrisponde alla somma dei SingoliImportiPagati [{1}]", paymentAmount.doubleValue(), importoTotaleCalcolato.doubleValue()), true);
		if (ctRecepitOutcome.equals(StOutcome.KO) && paymentAmount.compareTo(BigDecimal.ZERO) != 0)
			esito.addErrore(MessageFormat.format("ImportoTotalePagato [{0}] diverso da 0 per un pagamento con esito ''KO''.", paymentAmount.doubleValue()), true);
		BigDecimal ctPaymentPAPaymentAmount = ctPaymentPA.getPaymentAmount();
		if (ctRecepitOutcome.equals(StOutcome.OK) && paymentAmount.compareTo(ctPaymentPAPaymentAmount) != 0)
			esito.addErrore(MessageFormat.format("Importo totale del pagamento [{0}] diverso da quanto richiesto [{1}]", paymentAmount.doubleValue(), ctPaymentPAPaymentAmount.doubleValue()), false);

		return esito;
	}

	private static void validaSemanticaSingoloVersamento(CtTransferPA singoloVersamento, CtTransferPA singoloPagamento, int pos, EsitoValidazione esito) {

		if(singoloPagamento.getIdTransfer() != singoloVersamento.getIdTransfer()) {
			esito.addErrore(MessageFormat.format("IdTransfer non corrispondente per il pagamento in posizione [{0}]", pos), false);
		}
		valida(singoloVersamento.getTransferCategory(), singoloPagamento.getTransferCategory(), esito, "TransferCategory non corrisponde", false);

		if(singoloPagamento.getTransferAmount().compareTo(BigDecimal.ZERO) == 0) {

		} else if(singoloPagamento.getTransferAmount().compareTo(singoloVersamento.getTransferAmount()) != 0) {
			esito.addErrore(MessageFormat.format("Importo del pagamento in posizione {0} [{1}] diverso da quanto richiesto [{2}]", pos, singoloPagamento.getTransferAmount().doubleValue(), singoloVersamento.getTransferAmount().doubleValue()), false);
		}
	}

	public static void validaSemantica(CtSubject rpt, CtSubject rt, EsitoValidazione esito) {
		valida(rpt.getFullName(),rt.getFullName(), esito, "FullNameDebtor non corrisponde", false);
		valida(rpt.getPostalCode(),rt.getPostalCode(), esito, "PostalCodeDebtor non corrisponde", false);
		valida(rpt.getCivicNumber(),rt.getCivicNumber(), esito, "CivicNumberDebtor non corrisponde", false);
		valida(rpt.getEMail(),rt.getEMail(), esito, "EMailDebtor non corrisponde", false);
		valida(rpt.getUniqueIdentifier().getEntityUniqueIdentifierValue(),rt.getUniqueIdentifier().getEntityUniqueIdentifierValue(), esito, "UniqueIdentifierDebtor non corrisponde", true);
		valida(rpt.getStreetName(),rt.getStreetName(), esito, "StreetNameDebtor non corrisponde", false);
		valida(rpt.getCity(),rt.getCity(), esito, "CityDebtor non corrisponde", false);
		valida(rpt.getCountry(),rt.getCountry(), esito, "CountryDebtor non corrisponde", false);
		valida(rpt.getStateProvinceRegion(),rt.getStateProvinceRegion(), esito, "StateProvinceDebtor non corrisponde", false);
	}

	public static Rpt acquisisciRT(String codDominio, String iuv, PaSendRTReq ctRt, boolean recupero) throws ServiceException, NdpException, UtilsException, GovPayException {
		return acquisisciRT(codDominio, iuv, ctRt, recupero, false);
	}

	public static Rpt acquisisciRT(String codDominio, String iuv, PaSendRTReq ctRt, boolean recupero, boolean acquisizioneDaCruscotto) throws ServiceException, NdpException, UtilsException, GovPayException {

		if(ctRt == null || ctRt.getReceipt() == null) throw new NdpException(FaultPa.PAA_SYSTEM_ERROR, "Ricevuta vuota", codDominio);
		
		CtReceipt ctReceipt = ctRt.getReceipt();
		String receiptId = ctReceipt.getReceiptId();
		
		log.info(MessageFormat.format("Acquisizione RT Dominio[{0}], IUV[{1}], ReceiptID [{2}] in corso", codDominio, iuv, receiptId));
		RptBD rptBD = null; 
		try {
			IContext ctx = ContextThreadLocal.get();
			BDConfigWrapper configWrapper = new BDConfigWrapper(ctx.getTransactionId(), true);
			GpContext appContext = (GpContext) ctx.getApplicationContext();
			
			// lettura dati significativi dalla ricevuta
			BigDecimal paymentAmount = ctReceipt.getPaymentAmount();
			Date dataPagamento = ctReceipt.getPaymentDateTime() != null ? ctReceipt.getPaymentDateTime() : new Date();
			StOutcome ctReceiptOutcome = ctReceipt.getOutcome();
			it.govpay.model.Rpt.EsitoPagamento rptEsito = ctReceiptOutcome.equals(StOutcome.OK) ? it.govpay.model.Rpt.EsitoPagamento.PAGAMENTO_ESEGUITO : it.govpay.model.Rpt.EsitoPagamento.PAGAMENTO_NON_ESEGUITO; 
			String pspFiscalCode = ctReceipt.getPspFiscalCode();
			String pspCompanyName = ctReceipt.getPSPCompanyName();
			String idPSP = ctReceipt.getIdPSP();
			String idChannel = ctReceipt.getIdChannel();

			rptBD = new RptBD(configWrapper);

			rptBD.setupConnection(configWrapper.getTransactionID());

			rptBD.setAtomica(false);

			rptBD.setAutoCommit(false);

			VersamentiBD versamentiBD = new VersamentiBD(rptBD);

			versamentiBD.setAtomica(false);

			Rpt rpt = null;
			try { 
				rpt = rptBD.getRpt(codDominio, iuv, ModelloPagamento.ATTIVATO_PRESSO_PSP, it.govpay.model.Rpt.VersioneRPT.SANP_240, false); // ricerca della RPT senza caricare il dettaglio versamenti, sv, pagamenti e pagamenti_portale
			} catch (NotFoundException e) {
				throw new NdpException(FaultPa.PAA_RPT_SCONOSCIUTA, e.getMessage(), codDominio);
			}
			
			boolean isCarrello = RtUtils.isCarrelloRpt(rpt);

			// Faccio adesso la select for update, altrimenti in caso di 
			// ricezione di due RT afferenti allo stesso carrello di pagamento
			// vado in deadlock tra la getRpt precedente e la findAll seguente

			rptBD.enableSelectForUpdate();

			Long idPagamentoPortale = rpt.getIdPagamentoPortale();

			if(isCarrello) {
				@SuppressWarnings("unused")
				List<Rpt> rptsCarrello = null; 
				if(idPagamentoPortale != null) {
					RptFilter filter = rptBD.newFilter();
					filter.setIdPagamentoPortale(idPagamentoPortale);
					rptsCarrello = rptBD.findAll(filter);
				}
			}

			// Rifaccio la getRpt adesso che ho il lock per avere lo stato aggiornato
			// infatti in caso di RT concorrente, non viene gestito bene l'errore.

			try {
				rpt = rptBD.getRpt(codDominio, iuv, ModelloPagamento.ATTIVATO_PRESSO_PSP, it.govpay.model.Rpt.VersioneRPT.SANP_240, false); // ricerca della RPT senza caricare il dettaglio versamenti, sv, pagamenti e pagamenti_portale
			} catch (NotFoundException e) {
				throw new NdpException(FaultPa.PAA_RPT_SCONOSCIUTA, e.getMessage(), codDominio);
			}

			if(!acquisizioneDaCruscotto) {
				if(rpt.getStato().equals(StatoRpt.RT_ACCETTATA_PA)) {
					throw new NdpException(FaultPa.PAA_RECEIPT_DUPLICATA, MessageFormat.format("CtReceipt già acquisita in data {0}", rpt.getDataMsgRicevuta()), rpt.getCodDominio());
				}
			}

			PaGetPaymentRes ctRpt = null; 

			// Validazione Semantica
			RtUtils.EsitoValidazione esito = null;
			try {
				ctRpt = JaxbUtils.toPaGetPaymentRes_RPT(rpt.getXmlRpt(), false);
				esito = CtReceiptUtils.validaSemantica(ctRpt, ctRt);
			} catch (JAXBException e) {
				throw e;
			} catch (SAXException e) {
				throw e;
			}

			if(acquisizioneDaCruscotto) {
				// controllo esito validazione semantica
				// controllo stato pagamento attuale se e' gia' stato eseguito allora non devo acquisire l'rt
				//EsitoPagamento nuovoEsitoPagamento = it.govpay.model.Rpt.EsitoPagamento.toEnum(ctRt.getDatiPagamento().getCodiceEsitoPagamento());

				switch (rpt.getEsitoPagamento()) {
				case IN_CORSO:
				case PAGAMENTO_NON_ESEGUITO:
				case DECORRENZA_TERMINI:
				case RIFIUTATO:
					break;
				case DECORRENZA_TERMINI_PARZIALE:
				case PAGAMENTO_ESEGUITO:
				case PAGAMENTO_PARZIALMENTE_ESEGUITO:
					throw new NdpException(FaultPa.PAA_RECEIPT_DUPLICATA, MessageFormat.format("Aggiornamento di CtReceipt in pagamenti con esito {0} non supportata.",	rpt.getEsitoPagamento()), rpt.getCodDominio());
				}
			}

			if(esito.validato && esito.errori.size() > 0) {
				if(recupero)
					ctx.getApplicationLogger().log("pagamento.recuperoRtValidazioneRtWarn", esito.getDiagnostico());
				else 
					ctx.getApplicationLogger().log("pagamento.validazioneRtWarn", esito.getDiagnostico());
			} 

			if (!esito.validato) {
				if(recupero)
					ctx.getApplicationLogger().log("pagamento.recuperoRtValidazioneRtFail", esito.getDiagnostico());
				else 
					ctx.getApplicationLogger().log("pagamento.validazioneRtFail", esito.getDiagnostico());

				rpt.setStato(StatoRpt.RT_RIFIUTATA_PA);
				rpt.setDescrizioneStato(esito.getFatal());
				rpt.setXmlRt(JaxbUtils.toByte(ctRt));

				try {
					rptBD.updateRpt(rpt.getId(), rpt);
					rptBD.commit();
				}catch (ServiceException e1) {
					rptBD.rollback();
				} finally {
					rptBD.disableSelectForUpdate();
				}
				throw new NdpException(FaultPa.PAA_SEMANTICA, esito.getFatal(), codDominio);
			}
			
			log.info(MessageFormat.format("Acquisizione RT per un importo di {0}", paymentAmount));

			if(recupero) {
				appContext.getTransaction().getLastServer().addGenericProperty(new Property("codMessaggioRicevuta", receiptId));
				appContext.getTransaction().getLastServer().addGenericProperty(new Property("importo", paymentAmount.toString()));
				appContext.getTransaction().getLastServer().addGenericProperty(new Property("codEsitoPagamento", rptEsito.toString()));
				ctx.getApplicationLogger().log("rt.rtRecuperoAcquisizione");
			} else {
				appContext.getRequest().addGenericProperty(new Property("codMessaggioRicevuta", receiptId));
				appContext.getRequest().addGenericProperty(new Property("importo", paymentAmount.toString()));
				appContext.getRequest().addGenericProperty(new Property("codEsitoPagamento", rptEsito.toString()));
				ctx.getApplicationLogger().log("rt.acquisizione");
			}

			// aggiornamento del campo CCP con il valore RecepitID
			rpt.setCcp(receiptId);
			rpt.setCodMsgRicevuta(receiptId);
			rpt.setDataMsgRicevuta(new Date());
			rpt.setEsitoPagamento(rptEsito);
			rpt.setImportoTotalePagato(paymentAmount);
			rpt.setStato(StatoRpt.RT_ACCETTATA_PA);
			rpt.setDescrizioneStato(null);
			rpt.setXmlRt(JaxbUtils.toByte(ctRt));
			rpt.setIdTransazioneRt(ContextThreadLocal.get().getTransactionId());
			rpt.setTipoIdentificativoAttestante(TipoIdentificativoAttestante.G);
			rpt.setIdentificativoAttestante(pspFiscalCode);
			rpt.setDenominazioneAttestante(pspCompanyName);
			rpt.setCodPsp(idPSP);
			rpt.setCodCanale(idChannel);
			
			// Aggiorno l'RPT con i dati dell'RT
			rptBD.updateRpt(rpt.getId(), rpt);

			Versamento versamento = rpt.getVersamento(rptBD);

			List<CtTransferPA> datiSingoliPagamenti = ctReceipt.getTransferList().getTransfer();
			List<SingoloVersamento> singoliVersamenti = versamento.getSingoliVersamenti(rptBD);

			PagamentiBD pagamentiBD = new PagamentiBD(rptBD);
			pagamentiBD.setAtomica(false); // condivisione della connessione

			boolean irregolare = false;
			String irregolarita = null; 

			String iuvPagamento = rpt.getIuv();
			BigDecimal totalePagato = BigDecimal.ZERO;
			

			List<Pagamento> pagamenti = new ArrayList<Pagamento>();

			for(int indice = 0; indice < datiSingoliPagamenti.size(); indice++) {
				CtTransferPA ctDatiSingoloPagamentoRT = datiSingoliPagamenti.get(indice);
				BigDecimal transferAmount = ctDatiSingoloPagamentoRT.getTransferAmount();
				String codDominioSingoloPagamento = ctDatiSingoloPagamentoRT.getFiscalCodePA();
				int idTransfer = ctDatiSingoloPagamentoRT.getIdTransfer();
				BigDecimal commissioniApplicatePSP = ctReceipt.getFee();
				
				// Se non e' stato completato un pagamento, non faccio niente.
				if(transferAmount.compareTo(BigDecimal.ZERO) == 0)
					continue;

				SingoloVersamento singoloVersamento = singoliVersamenti.get(indice);
				Dominio dominioSingoloVersamento = null;
				try {
					dominioSingoloVersamento = AnagraficaManager.getDominio(configWrapper, codDominioSingoloPagamento);
				} catch (NotFoundException e1) {
					dominioSingoloVersamento = versamento.getDominio(configWrapper);
				}

				Pagamento pagamento = null;
				boolean insert = true;
				try {
					pagamento = pagamentiBD.getPagamento(dominioSingoloVersamento.getCodDominio(), iuv, receiptId, idTransfer);

					// Pagamento rendicontato precedentemente senza RPT
					// Probabilmente sono stati scambiati i tracciati per sanare la situazione
					// Aggiorno il pagamento associando la RT appena arrivata

					if(pagamento.getIdRpt() != null) {
						//!! Pagamento gia' notificato da un'altra RPT !!
						throw new ServiceException(MessageFormat.format("ERRORE: RT con pagamento gia'' presente in sistema [{0}/{1}/{2}]", dominioSingoloVersamento.getCodDominio(), iuv, receiptId));
					}
			 		
					pagamento.setDataPagamento(dataPagamento); // <!--data esecuzione pagamento da parte dell'utente-->
					pagamento.setRpt(rpt);
					// Se non e' gia' stato incassato, aggiorno lo stato in pagato
					if(!pagamento.getStato().equals(Stato.INCASSATO)) {
						pagamento.setStato(Stato.PAGATO);
						pagamento.setImportoPagato(transferAmount);
					} else {
						// Era stato gia incassato.
						// non faccio niente.
						continue;
					}
					insert = false;
				} catch (NotFoundException nfe){
					pagamento = RtUtils.creaNuovoPagamento(iuv, receiptId, ctx, configWrapper, dataPagamento, rpt, transferAmount, idTransfer, singoloVersamento, commissioniApplicatePSP, dominioSingoloVersamento);
				} catch (MultipleResultException e) {
					throw new ServiceException(MessageFormat.format("Identificativo pagamento non univoco: [Dominio:{0} Iuv:{1} Iur:{2} Indice:{3}]", dominioSingoloVersamento.getCodDominio(),	iuv, receiptId, idTransfer));
				}

				// Se ho solo aggiornato un pagamento che gia' c'era, non devo fare altro.
				// Se gli importi corrispondono e lo stato era da pagare, il singoloVersamento e' eseguito. Altrimenti irregolare.

				dataPagamento = pagamento.getDataPagamento();
				totalePagato = totalePagato.add(pagamento.getImportoPagato());

				if(insert) {
					if(singoloVersamento.getStatoSingoloVersamento().equals(StatoSingoloVersamento.NON_ESEGUITO) && singoloVersamento.getImportoSingoloVersamento().compareTo(pagamento.getImportoPagato()) == 0)
						singoloVersamento.setStatoSingoloVersamento(StatoSingoloVersamento.ESEGUITO);
					else {
						List<String> anomalie = new ArrayList<>();

						if(singoloVersamento.getStatoSingoloVersamento().equals(StatoSingoloVersamento.ESEGUITO)) {
							irregolarita = "Acquisito pagamento duplicato";
							anomalie.add(irregolarita);
							log.warn(irregolarita);
						}

						if(singoloVersamento.getImportoSingoloVersamento().compareTo(pagamento.getImportoPagato()) != 0) {
							irregolarita = "L'importo pagato non corrisponde all'importo dovuto.";
							anomalie.add(irregolarita);
							log.warn(irregolarita);
						}
						if(recupero)
							ctx.getApplicationLogger().log("pagamento.recuperoRtAcquisizionePagamentoAnomalo", receiptId, StringUtils.join(anomalie,"\n"));
						else 
							ctx.getApplicationLogger().log("pagamento.acquisizionePagamentoAnomalo", receiptId, StringUtils.join(anomalie,"\n"));

						irregolare = true;

					}
					ctx.getApplicationLogger().log("rt.acquisizionePagamento", pagamento.getIur(), pagamento.getImportoPagato().toString(), singoloVersamento.getCodSingoloVersamentoEnte(), singoloVersamento.getStatoSingoloVersamento().toString());
					versamentiBD.updateStatoSingoloVersamento(singoloVersamento.getId(), singoloVersamento.getStatoSingoloVersamento());
					pagamentiBD.insertPagamento(pagamento);
					
					if(!irregolare) {
						RtUtils.checkEsistenzaRendicontazioneAnomalaPerIlPagamento(pagamentiBD, pagamento);
					}
				}
				else {
					ctx.getApplicationLogger().log("rt.aggiornamentoPagamento", pagamento.getIur(), pagamento.getImportoPagato().toString(), singoloVersamento.getCodSingoloVersamentoEnte());
					pagamentiBD.updatePagamento(pagamento);
				}

				pagamenti.add(pagamento);
			}

			rpt.setPagamenti(pagamenti);

			boolean updateAnomalo = RtUtils.impostaNuovoStatoVersamento(rpt, versamento, irregolare, irregolarita);	
			
			RtUtils.schedulazionePromemoriaENotificaAppIO(rptBD, configWrapper, rpt, idPagamentoPortale, versamento, versamentiBD, iuvPagamento,
					totalePagato, dataPagamento, updateAnomalo);

			Notifica notifica = new Notifica(rpt, TipoNotifica.RICEVUTA, configWrapper);
			it.govpay.core.business.Notifica notificaBD = new it.govpay.core.business.Notifica();
			boolean schedulaThreadInvio = notificaBD.inserisciNotifica(notifica,rptBD);

			rptBD.commit();
			rptBD.disableSelectForUpdate();

			if(schedulaThreadInvio) {
				ThreadExecutorManager.getClientPoolExecutorNotifica().execute(new InviaNotificaThread(notifica, ctx));
			}

			ctx.getApplicationLogger().log("rt.acquisizioneOk", versamento.getCodVersamentoEnte(), versamento.getStatoVersamento().toString());
			log.info(MessageFormat.format("RT Dominio[{0}], IUV[{1}], ReceiptID [{2}] acquisita con successo.", codDominio, iuv, receiptId));

			return rpt;
		}  catch (JAXBException e) {
			throw new ServiceException(e);
		} catch (SAXException e) {
			throw new ServiceException(e);
		} catch (NotificaException | IOException e) {
			log.error(MessageFormat.format("Errore acquisizione RT: {0}", e.getMessage()),e);
			
			if(rptBD != null)
				rptBD.rollback();
			
			throw new ServiceException(e);
		} catch (ServiceException e) {
			log.error(MessageFormat.format("Errore acquisizione RT: {0}", e.getMessage()),e);

			if(rptBD != null)
				rptBD.rollback();

			throw e;
		} finally {
			if(rptBD != null)
				rptBD.closeConnection();
		}
	}
	
	public static Rpt acquisisciRT(String codDominio, String iuv, PaSendRTV2Request ctRt, boolean recupero) throws ServiceException, NdpException, UtilsException, GovPayException {
		return acquisisciRT(codDominio, iuv, ctRt, recupero, false);
	}

	public static Rpt acquisisciRT(String codDominio, String iuv, PaSendRTV2Request ctRt, boolean recupero, boolean acquisizioneDaCruscotto) throws ServiceException, NdpException, UtilsException, GovPayException {

		if(ctRt == null || ctRt.getReceipt() == null) throw new NdpException(FaultPa.PAA_SYSTEM_ERROR, "Ricevuta vuota", codDominio);
		
		CtReceiptV2 ctReceipt = ctRt.getReceipt();
		String receiptId = ctReceipt.getReceiptId();
		
		log.info(MessageFormat.format("Acquisizione RT Dominio[{0}], IUV[{1}], ReceiptID [{2}] in corso", codDominio, iuv, receiptId));
		RptBD rptBD = null; 
		try {
			IContext ctx = ContextThreadLocal.get();
			BDConfigWrapper configWrapper = new BDConfigWrapper(ctx.getTransactionId(), true);
			GpContext appContext = (GpContext) ctx.getApplicationContext();
			
			// lettura dati significativi dalla ricevuta
			BigDecimal paymentAmount = ctReceipt.getPaymentAmount();
			Date dataPagamento = ctReceipt.getPaymentDateTime() != null ? ctReceipt.getPaymentDateTime() : new Date();
			StOutcome ctReceiptOutcome = ctReceipt.getOutcome();
			it.govpay.model.Rpt.EsitoPagamento rptEsito = ctReceiptOutcome.equals(StOutcome.OK) ? it.govpay.model.Rpt.EsitoPagamento.PAGAMENTO_ESEGUITO : it.govpay.model.Rpt.EsitoPagamento.PAGAMENTO_NON_ESEGUITO; 
			String pspFiscalCode = ctReceipt.getPspFiscalCode();
			String pspCompanyName = ctReceipt.getPSPCompanyName();
			String idPSP = ctReceipt.getIdPSP();
			String idChannel = ctReceipt.getIdChannel();

			rptBD = new RptBD(configWrapper);

			rptBD.setupConnection(configWrapper.getTransactionID());

			rptBD.setAtomica(false);

			rptBD.setAutoCommit(false);

			VersamentiBD versamentiBD = new VersamentiBD(rptBD);

			versamentiBD.setAtomica(false);

			Rpt rpt = null;
			try { 
				rpt = rptBD.getRpt(codDominio, iuv, ModelloPagamento.ATTIVATO_PRESSO_PSP, it.govpay.model.Rpt.VersioneRPT.SANP_321_V2, false); // ricerca della RPT senza caricare il dettaglio versamenti, sv, pagamenti e pagamenti_portale
			} catch (NotFoundException e) {
				throw new NdpException(FaultPa.PAA_RPT_SCONOSCIUTA, e.getMessage(), codDominio);
			}
			
			boolean isCarrello = RtUtils.isCarrelloRpt(rpt);

			// Faccio adesso la select for update, altrimenti in caso di 
			// ricezione di due RT afferenti allo stesso carrello di pagamento
			// vado in deadlock tra la getRpt precedente e la findAll seguente

			rptBD.enableSelectForUpdate();

			Long idPagamentoPortale = rpt.getIdPagamentoPortale();

			if(isCarrello) {
				@SuppressWarnings("unused")
				List<Rpt> rptsCarrello = null; 
				if(idPagamentoPortale != null) {
					RptFilter filter = rptBD.newFilter();
					filter.setIdPagamentoPortale(idPagamentoPortale);
					rptsCarrello = rptBD.findAll(filter);
				}
			}

			// Rifaccio la getRpt adesso che ho il lock per avere lo stato aggiornato
			// infatti in caso di RT concorrente, non viene gestito bene l'errore.

			try {
				rpt = rptBD.getRpt(codDominio, iuv, ModelloPagamento.ATTIVATO_PRESSO_PSP, it.govpay.model.Rpt.VersioneRPT.SANP_321_V2, false); // ricerca della RPT senza caricare il dettaglio versamenti, sv, pagamenti e pagamenti_portale
			} catch (NotFoundException e) {
				throw new NdpException(FaultPa.PAA_RPT_SCONOSCIUTA, e.getMessage(), codDominio);
			}

			if(!acquisizioneDaCruscotto) {
				if(rpt.getStato().equals(StatoRpt.RT_ACCETTATA_PA)) {
					throw new NdpException(FaultPa.PAA_RECEIPT_DUPLICATA, MessageFormat.format("CtReceipt già acquisita in data {0}", rpt.getDataMsgRicevuta()), rpt.getCodDominio());
				}
			}

			PaGetPaymentV2Response ctRpt = null; 

			// Validazione Semantica
			RtUtils.EsitoValidazione esito = null;
			try {
				ctRpt = JaxbUtils.toPaGetPaymentV2Response_RPT(rpt.getXmlRpt(), false);
				esito = CtReceiptUtils.validaSemantica(ctRpt, ctRt);
			} catch (JAXBException e) {
				throw e;
			} catch (SAXException e) {
				throw e;
			}

			if(acquisizioneDaCruscotto) {
				// controllo esito validazione semantica
				// controllo stato pagamento attuale se e' gia' stato eseguito allora non devo acquisire l'rt
				//EsitoPagamento nuovoEsitoPagamento = it.govpay.model.Rpt.EsitoPagamento.toEnum(ctRt.getDatiPagamento().getCodiceEsitoPagamento());

				switch (rpt.getEsitoPagamento()) {
				case IN_CORSO:
				case PAGAMENTO_NON_ESEGUITO:
				case DECORRENZA_TERMINI:
				case RIFIUTATO:
					break;
				case DECORRENZA_TERMINI_PARZIALE:
				case PAGAMENTO_ESEGUITO:
				case PAGAMENTO_PARZIALMENTE_ESEGUITO:
					throw new NdpException(FaultPa.PAA_RECEIPT_DUPLICATA, MessageFormat.format("Aggiornamento di CtReceipt in pagamenti con esito {0} non supportata.",	rpt.getEsitoPagamento()), rpt.getCodDominio());
				}
			}

			if(esito.validato && esito.errori.size() > 0) {
				if(recupero)
					ctx.getApplicationLogger().log("pagamento.recuperoRtValidazioneRtWarn", esito.getDiagnostico());
				else 
					ctx.getApplicationLogger().log("pagamento.validazioneRtWarn", esito.getDiagnostico());
			} 

			if (!esito.validato) {
				if(recupero)
					ctx.getApplicationLogger().log("pagamento.recuperoRtValidazioneRtFail", esito.getDiagnostico());
				else 
					ctx.getApplicationLogger().log("pagamento.validazioneRtFail", esito.getDiagnostico());

				rpt.setStato(StatoRpt.RT_RIFIUTATA_PA);
				rpt.setDescrizioneStato(esito.getFatal());
				rpt.setXmlRt(JaxbUtils.toByte(ctRt));

				try {
					rptBD.updateRpt(rpt.getId(), rpt);
					rptBD.commit();
				}catch (ServiceException e1) {
					rptBD.rollback();
				} finally {
					rptBD.disableSelectForUpdate();
				}
				throw new NdpException(FaultPa.PAA_SEMANTICA, esito.getFatal(), codDominio);
			}
			
			log.info(MessageFormat.format("Acquisizione RT per un importo di {0}", paymentAmount));

			if(recupero) {
				appContext.getTransaction().getLastServer().addGenericProperty(new Property("codMessaggioRicevuta", receiptId));
				appContext.getTransaction().getLastServer().addGenericProperty(new Property("importo", paymentAmount.toString()));
				appContext.getTransaction().getLastServer().addGenericProperty(new Property("codEsitoPagamento", rptEsito.toString()));
				ctx.getApplicationLogger().log("rt.rtRecuperoAcquisizione");
			} else {
				appContext.getRequest().addGenericProperty(new Property("codMessaggioRicevuta", receiptId));
				appContext.getRequest().addGenericProperty(new Property("importo", paymentAmount.toString()));
				appContext.getRequest().addGenericProperty(new Property("codEsitoPagamento", rptEsito.toString()));
				ctx.getApplicationLogger().log("rt.acquisizione");
			}

			// aggiornamento del campo CCP con il valore RecepitID
			rpt.setCcp(receiptId);
			rpt.setCodMsgRicevuta(receiptId);
			rpt.setDataMsgRicevuta(new Date());
			rpt.setEsitoPagamento(rptEsito);
			rpt.setImportoTotalePagato(paymentAmount);
			rpt.setStato(StatoRpt.RT_ACCETTATA_PA);
			rpt.setDescrizioneStato(null);
			rpt.setXmlRt(JaxbUtils.toByte(ctRt));
			rpt.setIdTransazioneRt(ContextThreadLocal.get().getTransactionId());
			rpt.setTipoIdentificativoAttestante(TipoIdentificativoAttestante.G);
			rpt.setIdentificativoAttestante(pspFiscalCode);
			rpt.setDenominazioneAttestante(pspCompanyName);
			rpt.setCodPsp(idPSP);
			rpt.setCodCanale(idChannel);
			
			// Aggiorno l'RPT con i dati dell'RT
			rptBD.updateRpt(rpt.getId(), rpt);

			Versamento versamento = rpt.getVersamento(rptBD);

			List<CtTransferPAReceiptV2> datiSingoliPagamenti = ctReceipt.getTransferList().getTransfer();
			List<SingoloVersamento> singoliVersamenti = versamento.getSingoliVersamenti(rptBD);

			PagamentiBD pagamentiBD = new PagamentiBD(rptBD);
			pagamentiBD.setAtomica(false); // condivisione della connessione

			boolean irregolare = false;
			String irregolarita = null; 

			String iuvPagamento = rpt.getIuv();
			BigDecimal totalePagato = BigDecimal.ZERO;
			

			List<Pagamento> pagamenti = new ArrayList<Pagamento>();

			for(int indice = 0; indice < datiSingoliPagamenti.size(); indice++) {
				CtTransferPAReceiptV2 ctDatiSingoloPagamentoRT = datiSingoliPagamenti.get(indice);
				BigDecimal transferAmount = ctDatiSingoloPagamentoRT.getTransferAmount();
				String codDominioSingoloPagamento = ctDatiSingoloPagamentoRT.getFiscalCodePA();
				int idTransfer = ctDatiSingoloPagamentoRT.getIdTransfer();
				BigDecimal commissioniApplicatePSP = ctReceipt.getFee();
				
				// Se non e' stato completato un pagamento, non faccio niente.
				if(transferAmount.compareTo(BigDecimal.ZERO) == 0)
					continue;

				SingoloVersamento singoloVersamento = singoliVersamenti.get(indice);
				Dominio dominioSingoloVersamento = null;
				try {
					dominioSingoloVersamento = AnagraficaManager.getDominio(configWrapper, codDominioSingoloPagamento);
				} catch (NotFoundException e1) {
					dominioSingoloVersamento = versamento.getDominio(configWrapper);
				}

				Pagamento pagamento = null;
				boolean insert = true;
				try {
					pagamento = pagamentiBD.getPagamento(dominioSingoloVersamento.getCodDominio(), iuv, receiptId, idTransfer);

					// Pagamento rendicontato precedentemente senza RPT
					// Probabilmente sono stati scambiati i tracciati per sanare la situazione
					// Aggiorno il pagamento associando la RT appena arrivata

					if(pagamento.getIdRpt() != null) {
						//!! Pagamento gia' notificato da un'altra RPT !!
						throw new ServiceException(MessageFormat.format("ERRORE: RT con pagamento gia'' presente in sistema [{0}/{1}/{2}]",	dominioSingoloVersamento.getCodDominio(), iuv, receiptId));
					}
			 		
					pagamento.setDataPagamento(dataPagamento); // <!--data esecuzione pagamento da parte dell'utente-->
					pagamento.setRpt(rpt);
					// Se non e' gia' stato incassato, aggiorno lo stato in pagato
					if(!pagamento.getStato().equals(Stato.INCASSATO)) {
						pagamento.setStato(Stato.PAGATO);
						pagamento.setImportoPagato(transferAmount);
					} else {
						// Era stato gia incassato.
						// non faccio niente.
						continue;
					}
					insert = false;
				} catch (NotFoundException nfe){
					pagamento = RtUtils.creaNuovoPagamento(iuv, receiptId, ctx, configWrapper, dataPagamento, rpt, transferAmount, idTransfer, singoloVersamento, commissioniApplicatePSP, dominioSingoloVersamento);
				} catch (MultipleResultException e) {
					throw new ServiceException(MessageFormat.format("Identificativo pagamento non univoco: [Dominio:{0} Iuv:{1} Iur:{2} Indice:{3}]", dominioSingoloVersamento.getCodDominio(),	iuv, receiptId, idTransfer));
				}

				// Se ho solo aggiornato un pagamento che gia' c'era, non devo fare altro.
				// Se gli importi corrispondono e lo stato era da pagare, il singoloVersamento e' eseguito. Altrimenti irregolare.

				dataPagamento = pagamento.getDataPagamento();
				totalePagato = totalePagato.add(pagamento.getImportoPagato());

				if(insert) {
					if(singoloVersamento.getStatoSingoloVersamento().equals(StatoSingoloVersamento.NON_ESEGUITO) && singoloVersamento.getImportoSingoloVersamento().compareTo(pagamento.getImportoPagato()) == 0)
						singoloVersamento.setStatoSingoloVersamento(StatoSingoloVersamento.ESEGUITO);
					else {
						List<String> anomalie = new ArrayList<>();

						if(singoloVersamento.getStatoSingoloVersamento().equals(StatoSingoloVersamento.ESEGUITO)) {
							irregolarita = "Acquisito pagamento duplicato";
							anomalie.add(irregolarita);
							log.warn(irregolarita);
						}

						if(singoloVersamento.getImportoSingoloVersamento().compareTo(pagamento.getImportoPagato()) != 0) {
							irregolarita = "L'importo pagato non corrisponde all'importo dovuto.";
							anomalie.add(irregolarita);
							log.warn(irregolarita);
						}
						if(recupero)
							ctx.getApplicationLogger().log("pagamento.recuperoRtAcquisizionePagamentoAnomalo", receiptId, StringUtils.join(anomalie,"\n"));
						else 
							ctx.getApplicationLogger().log("pagamento.acquisizionePagamentoAnomalo", receiptId, StringUtils.join(anomalie,"\n"));

						irregolare = true;

					}
					ctx.getApplicationLogger().log("rt.acquisizionePagamento", pagamento.getIur(), pagamento.getImportoPagato().toString(), singoloVersamento.getCodSingoloVersamentoEnte(), singoloVersamento.getStatoSingoloVersamento().toString());
					versamentiBD.updateStatoSingoloVersamento(singoloVersamento.getId(), singoloVersamento.getStatoSingoloVersamento());
					pagamentiBD.insertPagamento(pagamento);
					
					if(!irregolare) {
						RtUtils.checkEsistenzaRendicontazioneAnomalaPerIlPagamento(pagamentiBD, pagamento);
					}
				}
				else {
					ctx.getApplicationLogger().log("rt.aggiornamentoPagamento", pagamento.getIur(), pagamento.getImportoPagato().toString(), singoloVersamento.getCodSingoloVersamentoEnte());
					pagamentiBD.updatePagamento(pagamento);
				}

				pagamenti.add(pagamento);
			}

			rpt.setPagamenti(pagamenti);

			boolean updateAnomalo = RtUtils.impostaNuovoStatoVersamento(rpt, versamento, irregolare, irregolarita);	
			
			RtUtils.schedulazionePromemoriaENotificaAppIO(rptBD, configWrapper, rpt, idPagamentoPortale, versamento, versamentiBD, iuvPagamento,
					totalePagato, dataPagamento, updateAnomalo);

			Notifica notifica = new Notifica(rpt, TipoNotifica.RICEVUTA, configWrapper);
			it.govpay.core.business.Notifica notificaBD = new it.govpay.core.business.Notifica();
			boolean schedulaThreadInvio = notificaBD.inserisciNotifica(notifica,rptBD);

			rptBD.commit();
			rptBD.disableSelectForUpdate();

			if(schedulaThreadInvio) {
				ThreadExecutorManager.getClientPoolExecutorNotifica().execute(new InviaNotificaThread(notifica, ctx));
			}

			ctx.getApplicationLogger().log("rt.acquisizioneOk", versamento.getCodVersamentoEnte(), versamento.getStatoVersamento().toString());
			log.info(MessageFormat.format("RT Dominio[{0}], IUV[{1}], ReceiptID [{2}] acquisita con successo.", codDominio, iuv, receiptId));

			return rpt;
		}  catch (JAXBException e) {
			throw new ServiceException(e);
		} catch (SAXException e) {
			throw new ServiceException(e);
		} catch (NotificaException | IOException e) {
			log.error(MessageFormat.format("Errore acquisizione RT: {0}", e.getMessage()),e);
			
			if(rptBD != null) 
				rptBD.rollback();
			
			throw new ServiceException(e);
		} catch (ServiceException e) {
			log.error(MessageFormat.format("Errore acquisizione RT: {0}", e.getMessage()),e);

			if(rptBD != null)
				rptBD.rollback();

			throw e;
		} finally {
			if(rptBD != null)
				rptBD.closeConnection();
		}
	}
	
	public static EsitoValidazione validaSemantica(PaGetPaymentV2Response ctRpt, PaSendRTV2Request ctRt) {
		CtPaymentPAV2 ctPaymentPA = ctRpt.getData();
		CtReceiptV2 ctReceipt = ctRt.getReceipt();

		EsitoValidazione esito = new RtUtils().new EsitoValidazione();
		valida(ctPaymentPA.getCreditorReferenceId(), ctReceipt.getCreditorReferenceId(), esito, "CreditorReferenceId non corrisponde", true); // Identificativo di correlazione dei due messaggi lo IUV???
		validaSemantica(ctPaymentPA.getDebtor(), ctReceipt.getDebtor(), esito);

		StOutcome ctRecepitOutcome = ctReceipt.getOutcome(); // esito pagamento ha solo due valori OK/KO
		String name = ctRecepitOutcome.name();
		switch (ctRecepitOutcome) {
		case OK:
			if(ctReceipt.getTransferList().getTransfer().size() != ctPaymentPA.getTransferList().getTransfer().size()) {
				esito.addErrore(MessageFormat.format("Numero di pagamenti diverso dal numero di versamenti per una ricevuta di tipo {0}", name), true);
				return esito;
			}
			break;
		case KO:
			if(ctReceipt.getTransferList().getTransfer().size() != 0 && ctReceipt.getTransferList().getTransfer().size() != ctPaymentPA.getTransferList().getTransfer().size()) {
				esito.addErrore(MessageFormat.format("Numero di pagamenti diverso dal numero di versamenti per una ricevuta di tipo {0}", name), true);
				return esito;
			}
			break;
		}

		BigDecimal importoTotaleCalcolato = BigDecimal.ZERO;

		for (int i = 0; i < ctPaymentPA.getTransferList().getTransfer().size(); i++) {

			CtTransferPAV2 singoloVersamento = ctPaymentPA.getTransferList().getTransfer().get(i);
			CtTransferPAReceiptV2 singoloPagamento = null; 
			if(ctReceipt.getTransferList().getTransfer().size() != 0) {
				singoloPagamento = ctReceipt.getTransferList().getTransfer().get(i);
				validaSemanticaSingoloVersamento(singoloVersamento, singoloPagamento, (i+1), esito);
				importoTotaleCalcolato = importoTotaleCalcolato.add(singoloPagamento.getTransferAmount());
			}
		}

		BigDecimal paymentAmount = ctReceipt.getPaymentAmount();
		if (importoTotaleCalcolato.compareTo(paymentAmount) != 0)
			esito.addErrore(MessageFormat.format("ImportoTotalePagato [{0}] non corrisponde alla somma dei SingoliImportiPagati [{1}]",	paymentAmount.doubleValue(), importoTotaleCalcolato.doubleValue()), true);
		if (ctRecepitOutcome.equals(StOutcome.KO) && paymentAmount.compareTo(BigDecimal.ZERO) != 0)
			esito.addErrore(MessageFormat.format("ImportoTotalePagato [{0}] diverso da 0 per un pagamento con esito ''KO''.", paymentAmount.doubleValue()), true);
		BigDecimal ctPaymentPAPaymentAmount = ctPaymentPA.getPaymentAmount();
		if (ctRecepitOutcome.equals(StOutcome.OK) && paymentAmount.compareTo(ctPaymentPAPaymentAmount) != 0)
			esito.addErrore(MessageFormat.format("Importo totale del pagamento [{0}] diverso da quanto richiesto [{1}]", paymentAmount.doubleValue(), ctPaymentPAPaymentAmount.doubleValue()), false);

		return esito;
	}

	private static void validaSemanticaSingoloVersamento(CtTransferPAV2 singoloVersamento, CtTransferPAReceiptV2 singoloPagamento, int pos, EsitoValidazione esito) {

		if(singoloPagamento.getIdTransfer() != singoloVersamento.getIdTransfer()) {
			esito.addErrore(MessageFormat.format("IdTransfer non corrispondente per il pagamento in posizione [{0}]", pos), false);
		}
		valida(singoloVersamento.getTransferCategory(), singoloPagamento.getTransferCategory(), esito, "TransferCategory non corrisponde", false);

		if(singoloPagamento.getTransferAmount().compareTo(BigDecimal.ZERO) == 0) {
		} else if(singoloPagamento.getTransferAmount().compareTo(singoloVersamento.getTransferAmount()) != 0) {
			esito.addErrore(MessageFormat.format("Importo del pagamento in posizione {0} [{1}] diverso da quanto richiesto [{2}]", pos, singoloPagamento.getTransferAmount().doubleValue(), singoloVersamento.getTransferAmount().doubleValue()), false);
		}
	}
}
