/*
 * GovPay - Porta di Accesso al Nodo dei Pagamenti SPC 
 * http://www.gov4j.it/govpay
 * 
 * Copyright (c) 2014-2018 Link.it srl (http://www.link.it).
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
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
package it.govpay.core.business;

import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.utils.LoggerWrapperFactory;
import org.openspcoop2.utils.service.context.IContext;
import org.openspcoop2.utils.sonde.Sonda;
import org.openspcoop2.utils.sonde.SondaException;
import org.openspcoop2.utils.sonde.SondaFactory;
import org.openspcoop2.utils.sonde.impl.SondaBatch;
import org.slf4j.Logger;

import it.govpay.bd.BDConfigWrapper;
import it.govpay.bd.BasicBD;
import it.govpay.bd.anagrafica.AnagraficaManager;
import it.govpay.bd.anagrafica.BatchBD;
import it.govpay.bd.model.Incasso;
import it.govpay.bd.model.Notifica;
import it.govpay.bd.model.NotificaAppIo;
import it.govpay.bd.model.Tracciato;
import it.govpay.bd.model.TracciatoNotificaPagamenti;
import it.govpay.bd.model.Versamento;
import it.govpay.bd.pagamento.IncassiBD;
import it.govpay.bd.pagamento.TracciatiBD;
import it.govpay.bd.pagamento.VersamentiBD;
import it.govpay.bd.pagamento.filters.TracciatoFilter;
import it.govpay.core.business.Rendicontazioni.DownloadRendicontazioniResponse;
import it.govpay.core.dao.pagamenti.dto.ElaboraTracciatoDTO;
import it.govpay.core.exceptions.IOException;
import it.govpay.core.utils.GovpayConfig;
import it.govpay.core.utils.client.BasicClientCORE;
import it.govpay.core.utils.logger.Log4JUtils;
import it.govpay.core.utils.thread.InviaNotificaAppIoThread;
import it.govpay.core.utils.thread.InviaNotificaThread;
import it.govpay.core.utils.thread.SpedizioneTracciatoNotificaPagamentiThread;
import it.govpay.core.utils.thread.ThreadExecutorManager;
import it.govpay.model.Batch;
import it.govpay.model.Tracciato.STATO_ELABORAZIONE;
import it.govpay.model.Tracciato.TIPO_TRACCIATO;
import it.govpay.model.configurazione.AppIOBatch;
import it.govpay.model.configurazione.MailBatch;

public class Operazioni{

	private static final String ERROR_MSG_AGGIORNAMENTO_SONDA_FALLITO_0 = "Aggiornamento sonda fallito: {0}";
	private static final String ERROR_MSG_SONDA_0_NON_TROVATA = "Sonda [{0}] non trovata";
	private static final String DEBUG_MSG_COMPLETATA_ESECUZIONE_DEI_0_THREADS_OK_1_ERRORE_2 = "Completata Esecuzione dei [{0}] Threads, OK [{1}], Errore [{2}]";
	private static final String ERROR_MSG_INTERRUPTED_0 = "Interrupted: {0}";
	private static Logger log = LoggerWrapperFactory.getLogger(Operazioni.class);
	public static final String CHECK_DB = "check-db";
	public static final String RND = "update-rnd";
	public static final String PND = "update-pnd";
	public static final String NTFY = "update-ntfy";
	public static final String NTFY_APP_IO = "update-ntfy-appio";
	public static final String CHECK_NTFY = "check-ntfy";
	public static final String CHECK_NTFY_APP_IO = "check-ntfy-appio";
	public static final String BATCH_TRACCIATI = "caricamento-tracciati";
	public static final String CHECK_TRACCIATI = "check-tracciati";
	public static final String CHECK_PROMEMORIA = "check-promemoria";
	public static final String BATCH_SPEDIZIONE_PROMEMORIA = "spedizione-promemoria";
	public static final String CACHE_ANAGRAFICA_GOVPAY = "cache-anagrafica";
	public static final String BATCH_GESTIONE_PROMEMORIA = "gestione-promemoria";
	public static final String CHECK_GESTIONE_PROMEMORIA = "check-gestione-promemoria";
	
	public static final String BATCH_ELABORAZIONE_TRACCIATI_NOTIFICA_PAGAMENTI = "elaborazione-trac-notif-pag";
	public static final String CHECK_ELABORAZIONE_TRACCIATI_NOTIFICA_PAGAMENTI = "check-elab-trac-notif-pag";
	
	public static final String BATCH_SPEDIZIONE_TRACCIATI_NOTIFICA_PAGAMENTI = "spedizione-trac-notif-pag";
	public static final String CHECK_SPEDIZIONE_TRACCIATI_NOTIFICA_PAGAMENTI = "check-spedizione-trac-notif-pag";
	
	public static final String BATCH_RICONCILIAZIONI = "riconciliazioni";
	public static final String CHECK_RICONCILIAZIONI = "check-riconciliazioni";
	
	public static final String BATCH_CHIUSURA_RPT_SCADUTE = "rpt-scadute";
	public static final String CHECK_CHIUSURA_RPT_SCADUTE = "check-rpt-scadute";

	private static boolean eseguiGestionePromemoria;
	private static boolean eseguiInvioPromemoria;
	private static boolean eseguiInvioNotifiche;
	private static boolean eseguiInvioNotificheAppIO;
	private static boolean eseguiGenerazioneAvvisi;
	private static boolean eseguiElaborazioneTracciati = true;
	
	private static boolean eseguiElaborazioneTracciatiNotificaPagamenti;
	private static boolean eseguiInvioTracciatiNotificaPagamenti;
	
	private static boolean eseguiElaborazioneRiconciliazioni;
	private static boolean eseguiElaborazioneChiusuraRptScadute;

	public static synchronized void setEseguiGestionePromemoria() {
		eseguiGestionePromemoria = true;
	}

	public static synchronized void resetEseguiGestionePromemoria() {
		eseguiGestionePromemoria = false;
	}

	public static synchronized boolean getEseguiGestionePromemoria() {
		return eseguiGestionePromemoria;
	}

	public static synchronized void setEseguiInvioPromemoria() {
		eseguiInvioPromemoria = true;
	}

	public static synchronized void resetEseguiInvioPromemoria() {
		eseguiInvioPromemoria = false;
	}

	public static synchronized boolean getEseguiInvioPromemoria() {
		return eseguiInvioPromemoria;
	}

	public static synchronized void setEseguiInvioNotifiche() {
		eseguiInvioNotifiche = true;
	}

	public static synchronized void resetEseguiInvioNotifiche() {
		eseguiInvioNotifiche = false;
	}

	public static synchronized boolean getEseguiInvioNotifiche() {
		return eseguiInvioNotifiche;
	}

	public static synchronized void setEseguiInvioNotificheAppIO() {
		eseguiInvioNotificheAppIO = true;
	}

	public static synchronized void resetEseguiInvioNotificheAppIO() {
		eseguiInvioNotificheAppIO = false;
	}

	public static synchronized boolean getEseguiInvioNotificheAppIO() {
		return eseguiInvioNotificheAppIO;
	}

	public static synchronized void setEseguiGenerazioneAvvisi() {
		eseguiGenerazioneAvvisi = true;
	}

	public static synchronized void resetEseguiGenerazioneAvvisi() {
		eseguiGenerazioneAvvisi = false;
	}

	public static synchronized boolean getEseguiGenerazioneAvvisi() {
		return eseguiGenerazioneAvvisi;
	}

	public static synchronized void setEseguiElaborazioneTracciati() {
		eseguiElaborazioneTracciati = true;
	}

	public static synchronized void resetEseguiElaborazioneTracciati() {
		eseguiElaborazioneTracciati = false;
	}

	public static synchronized boolean getEseguiElaborazioneTracciati() {
		return eseguiElaborazioneTracciati;
	}
	
	public static synchronized void setEseguiElaborazioneTracciatiNotificaPagamenti() {
		eseguiElaborazioneTracciatiNotificaPagamenti = true;
	}

	public static synchronized void resetEseguiElaborazioneTracciatiNotificaPagamenti() {
		eseguiElaborazioneTracciatiNotificaPagamenti = false;
	}

	public static synchronized boolean getEseguiElaborazioneTracciatiNotificaPagamenti() {
		return eseguiElaborazioneTracciatiNotificaPagamenti;
	}
	
	public static synchronized void setEseguiInvioTracciatiNotificaPagamenti() {
		eseguiInvioTracciatiNotificaPagamenti = true;
	}

	public static synchronized void resetEseguiInvioTracciatiNotificaPagamenti() {
		eseguiInvioTracciatiNotificaPagamenti = false;
	}

	public static synchronized boolean getEseguiInvioTracciatiNotificaPagamenti() {
		return eseguiInvioTracciatiNotificaPagamenti;
	}
	
	public static synchronized void setEseguiElaborazioneRiconciliazioni() {
		eseguiElaborazioneRiconciliazioni = true;
	}

	public static synchronized void resetEseguiElaborazioneRiconciliazioni() {
		eseguiElaborazioneRiconciliazioni = false;
	}

	public static synchronized boolean getEseguiElaborazioneRiconciliazioni() {
		return eseguiElaborazioneRiconciliazioni;
	}
	
	public static synchronized void setEseguiElaborazioneChiusuraRptScadute() {
		eseguiElaborazioneChiusuraRptScadute = true;
	}

	public static synchronized void resetEseguiElaborazioneChiusuraRptScadute() {
		eseguiElaborazioneChiusuraRptScadute = false;
	}

	public static synchronized boolean getEseguiElaborazioneChiusuraRptScadute() {
		return eseguiElaborazioneChiusuraRptScadute;
	}

	public static String acquisizioneRendicontazioni(IContext ctx){
		BDConfigWrapper configWrapper = new BDConfigWrapper(ctx.getTransactionId(), true);
		try {
			if(BatchManager.startEsecuzione(configWrapper, RND)) {
				DownloadRendicontazioniResponse downloadRendicontazioni = new Rendicontazioni().downloadRendicontazioni(ctx);
				aggiornaSondaOK(configWrapper, RND);
				return downloadRendicontazioni.getDescrizioneEsito();
			} else {
				return "Operazione in corso su altro nodo. Richiesta interrotta.";
			}
		} catch (Exception e) {
			log.error("Acquisizione rendicontazioni fallita", e);
			aggiornaSondaKO(configWrapper, RND, e);
			return "Acquisizione fallita#" + e;
		} finally {
			BatchManager.stopEsecuzione(configWrapper, RND);
		}
	}

	public static String recuperoRptPendenti(IContext ctx){
		BDConfigWrapper configWrapper = new BDConfigWrapper(ctx.getTransactionId(), true);
		try {
			if(BatchManager.startEsecuzione(configWrapper, PND)) {
				String verificaTransazioniPendenti = new Pagamento().verificaTransazioniPendenti();
				aggiornaSondaOK(configWrapper, PND);
				return verificaTransazioniPendenti;
			} else {
				return "Operazione in corso su altro nodo. Richiesta interrotta.";
			}
		} catch (Exception e) {
			log.error("Acquisizione Rpt pendenti fallita", e);
			aggiornaSondaKO(configWrapper, PND, e);
			return "Acquisizione fallita#" + e;
		} finally {
			BatchManager.stopEsecuzione(configWrapper, PND);
		}
	}
	
	public static String chiusuraRptScadute(IContext ctx){
		BDConfigWrapper configWrapper = new BDConfigWrapper(ctx.getTransactionId(), true);
		try {
			if(BatchManager.startEsecuzione(configWrapper, BATCH_CHIUSURA_RPT_SCADUTE)) {
				
				Sonda sonda = leggiSonda(configWrapper, BATCH_CHIUSURA_RPT_SCADUTE);
				Date dataUltimoCheck = (sonda != null && sonda.getParam() != null) ? sonda.getParam().getDataUltimoCheck() : null;
				String chiusuraRPTScadute = new Pagamento().chiusuraRPTScadute(ctx, dataUltimoCheck);
				aggiornaSondaOK(configWrapper, BATCH_CHIUSURA_RPT_SCADUTE);
				return chiusuraRPTScadute;
			} else {
				return "Operazione in corso su altro nodo. Richiesta interrotta.";
			}
		} catch (Exception e) {
			log.error("Chiusura RPT scadute fallita", e);
			aggiornaSondaKO(configWrapper, PND, e);
			return "Chiusura RPT scadute fallita#" + e;
		} finally {
			BatchManager.stopEsecuzione(configWrapper, BATCH_CHIUSURA_RPT_SCADUTE);
		}
	}

	public static String spedizioneNotifiche(IContext ctx){
		BDConfigWrapper configWrapper = new BDConfigWrapper(ctx.getTransactionId(), true);
		try {
			if(BatchManager.startEsecuzione(configWrapper, NTFY)) {
				log.debug("Spedizione notifiche non consegnate");

				List<String> applicazioni = AnagraficaManager.getListaCodApplicazioni(configWrapper);
				
				it.govpay.core.business.Notifica notificheBD = new it.govpay.core.business.Notifica();

				int threadNotificaPoolSize = GovpayConfig.getInstance().getDimensionePoolNotifica();

				for (String codApplicazione : applicazioni) {
					it.govpay.bd.model.Applicazione applicazione = null;
					try {
						applicazione = AnagraficaManager.getApplicazione(configWrapper, codApplicazione);
					}catch(NotFoundException e) {
						log.debug(MessageFormat.format("Applicazione [{0}] non trovata, passo alla prossima applicazione.", codApplicazione));
						continue;
					}

					// effettuo la spedizione solo per le applicazioni che hanno il connettore configurato.
					if(applicazione.getConnettoreIntegrazione() != null) {
						int offset = 0;
						int limit = (2 * threadNotificaPoolSize);
						List<InviaNotificaThread> threads = new ArrayList<>();
						List<Notifica> notifiche  = notificheBD.findNotificheDaSpedire(offset,limit,codApplicazione);

						log.debug(MessageFormat.format("Trovate [{0}] notifiche da spedire per l''applicazione [{1}]", notifiche.size(), codApplicazione));

						if(notifiche.size() > 0) {
							for(Notifica notifica: notifiche) {
								InviaNotificaThread sender = new InviaNotificaThread(notifica, ctx);
								ThreadExecutorManager.getClientPoolExecutorNotifica().execute(sender);
								threads.add(sender);
							}

							log.info(MessageFormat.format("Processi di spedizione notifiche per l''applicazione [{0}] avviati.", codApplicazione));
							aggiornaSondaOK(configWrapper, NTFY);

							// Aspetto che abbiano finito tutti
							int numeroErrori = 0;
							while(true){
								try {
									Thread.sleep(2000);
								} catch (InterruptedException e) {
									log.warn(MessageFormat.format(ERROR_MSG_INTERRUPTED_0, e.getMessage()), e);
								    // Restore interrupted state...
								    Thread.currentThread().interrupt();
								}
								boolean completed = true;
								for(InviaNotificaThread sender : threads) {
									if(!sender.isCompleted()) 
										completed = false;
								}

								if(completed) { 
									for(InviaNotificaThread sender : threads) {
										if(sender.isErrore()) 
											numeroErrori ++;
									}
									int numOk = threads.size() - numeroErrori;
									log.debug(MessageFormat.format(DEBUG_MSG_COMPLETATA_ESECUZIONE_DEI_0_THREADS_OK_1_ERRORE_2, threads.size(), numOk, numeroErrori));
									break; // esco
								}
							}

							// Hanno finito tutti, aggiorno stato esecuzione
							BatchManager.aggiornaEsecuzione(configWrapper, NTFY);
							log.info(MessageFormat.format("Processi di spedizione notifiche per l''applicazione [{0}] terminati.", codApplicazione));
						}
					}   else {
						log.debug(MessageFormat.format("Connettore non configurato per l''applicazione [{0}], non ricerco notifiche da spedire.", codApplicazione));
					}
				}
				aggiornaSondaOK(configWrapper, NTFY);
				log.debug("Spedizione notifiche completata.");
				return "Spedizione notifiche completata.";
			} else {
				log.debug("Operazione in corso su altro nodo. Richiesta interrotta.");
				return "Operazione in corso su altro nodo. Richiesta interrotta.";
			}
		} catch (ServiceException | IOException e) {
			log.error("Non è stato possibile avviare la spedizione delle notifiche", e);
			aggiornaSondaKO(configWrapper, NTFY, e); 
			return "Non è stato possibile avviare la spedizione delle notifiche: " + e;
		} finally {
			BatchManager.stopEsecuzione(configWrapper, NTFY);
		}
	}

	public static String spedizioneNotificheAppIO(IContext ctx){
		BDConfigWrapper configWrapper = new BDConfigWrapper(ctx.getTransactionId(), true);
		
		try {
			it.govpay.bd.model.Configurazione configurazione = new it.govpay.core.business.Configurazione().getConfigurazione();
			if(!configurazione.getBatchSpedizioneAppIo().isAbilitato()) {
				return "Spedizione notifiche AppIO disabilitata.";
			}
		} catch (Exception e) {
			log.error("Non è stato possibile avviare la spedizione delle notifiche AppIO", e);
			return "Non è stato possibile avviare la spedizione delle notifiche AppIO: " + e;
		}
		
		try {
			if(BatchManager.startEsecuzione(configWrapper, NTFY_APP_IO)) {
				log.debug("Spedizione notifiche AppIO non consegnate");

				it.govpay.core.business.NotificaAppIo notificheBD = new it.govpay.core.business.NotificaAppIo();

				int threadNotificaPoolSize = GovpayConfig.getInstance().getDimensionePoolNotificaAppIO();
				int offset = 0;
				int limit = (2 * threadNotificaPoolSize);
				List<InviaNotificaAppIoThread> threads = new ArrayList<>();
				List<NotificaAppIo> notifiche  = notificheBD.findNotificheDaSpedire(offset,limit);

				log.debug(MessageFormat.format("Trovate [{0}] notifiche AppIO da spedire", notifiche.size()));

				if(notifiche.size() > 0) {
					for(NotificaAppIo notifica: notifiche) {
						InviaNotificaAppIoThread sender = new InviaNotificaAppIoThread(notifica, ctx);
						ThreadExecutorManager.getClientPoolExecutorNotificaAppIo().execute(sender);
						threads.add(sender);
					}

					log.debug("Processi di spedizione notifiche AppIO avviati.");
					aggiornaSondaOK(configWrapper, NTFY_APP_IO);

					// Aspetto che abbiano finito tutti
					int numeroErrori = 0;
					while(true){
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							log.warn(MessageFormat.format(ERROR_MSG_INTERRUPTED_0, e.getMessage()), e);
						    // Restore interrupted state...
						    Thread.currentThread().interrupt();
						}
						boolean completed = true;
						for(InviaNotificaAppIoThread sender : threads) {
							if(!sender.isCompleted()) 
								completed = false;
						}

						if(completed) { 
							for(InviaNotificaAppIoThread sender : threads) {
								if(sender.isErrore()) 
									numeroErrori ++;
							}
							int numOk = threads.size() - numeroErrori;
							log.debug(MessageFormat.format(DEBUG_MSG_COMPLETATA_ESECUZIONE_DEI_0_THREADS_OK_1_ERRORE_2, threads.size(), numOk, numeroErrori));
							break; // esco
						}
					}
					log.info("Processi di spedizione notifiche AppIO terminati.");
					//Hanno finito tutti, aggiorno stato esecuzione
					BatchManager.aggiornaEsecuzione(configWrapper, NTFY_APP_IO);
				}
				aggiornaSondaOK(configWrapper, NTFY_APP_IO);
				log.debug("Spedizione notifiche AppIO completata.");
				return "Spedizione notifiche AppIO completata.";
			} else {
				log.debug("Operazione in corso su altro nodo. Richiesta interrotta.");
				return "Operazione in corso su altro nodo. Richiesta interrotta.";
			}
		} catch (ServiceException | IOException e) {
			log.error("Non è stato possibile avviare la spedizione delle notifiche AppIO", e);
			aggiornaSondaKO(configWrapper, NTFY_APP_IO, e); 
			return "Non è stato possibile avviare la spedizione delle notifiche AppIO: " + e;
		} finally {
			BatchManager.stopEsecuzione(configWrapper, NTFY_APP_IO);
		}
	}

	public static String resetCacheAnagrafica(IContext ctx){
		BDConfigWrapper configWrapper = new BDConfigWrapper(ctx.getTransactionId(), true);
		return resetCacheAnagrafica(configWrapper);
	}

	public static String resetCacheAnagrafica(BDConfigWrapper configWrapper){
		
		BatchBD batchBD = null;
		try {
			batchBD = new BatchBD(configWrapper);
			Batch batch = batchBD.get(CACHE_ANAGRAFICA_GOVPAY);
			batch.setAggiornamento(new Date());
			batchBD.update(batch);
			AnagraficaManager.cleanCache();
			BasicClientCORE.cleanCache();
			log.info("Aggiornamento della data di reset della cache anagrafica del sistema completato con successo.");	
			
			Log4JUtils.reloadLog4j();
			log.info("Reload Log4J completato.");
			
			return "Aggiornamento della data di reset della cache anagrafica del sistema completato con successo.";
		} catch (Exception e) {
			log.error("Aggiornamento della data di reset cache anagrafica del sistema fallita", e);
			return "Aggiornamento della data di reset cache del sistema fallita: " + e;
		} finally {
			if(batchBD != null) batchBD.closeConnection();
		}
	}

	public static String resetCacheAnagraficaCheck(IContext ctx){
		BDConfigWrapper configWrapper = new BDConfigWrapper(ctx.getTransactionId(), true);
		BatchBD batchBD = null;
		try {
			log.debug("Check reset della cache anagrafica locale in corso ...");	

			batchBD = new BatchBD(configWrapper);
			Batch batch = batchBD.get(CACHE_ANAGRAFICA_GOVPAY);
			Date aggiornamento = batch.getAggiornamento();

			Date dataResetAttuale = AnagraficaManager.getDataReset();
			if(aggiornamento != null && dataResetAttuale.getTime() < aggiornamento.getTime()) {
				String clusterId = GovpayConfig.getInstance().getClusterId();
				if(StringUtils.isEmpty(clusterId))
					clusterId = "1";

				log.info(MessageFormat.format("Nodo [{0}]: Reset della cache anagrafica locale in corso...", clusterId));	
				AnagraficaManager.cleanCache();
				BasicClientCORE.cleanCache();
				log.info(MessageFormat.format("Nodo [{0}]: Reset della cache anagrafica locale completato.", clusterId));
				
				log.info(MessageFormat.format("Nodo [{0}]: Reload Log4J in corso...", clusterId));	
				Log4JUtils.reloadLog4j();
				log.info(MessageFormat.format("Nodo [{0}]: Reload Log4J completato.", clusterId));
			}

			log.debug("Check reset della cache anagrafica locale completato con successo.");	
			return "Check reset della cache anagrafica locale completato con successo.";
		} catch (Exception e) {
			log.error("Check reset della cache anagrafica locale fallito", e);
			return "Check reset della cache anagrafica locale fallito: " + e;
		} finally {
			if(batchBD != null) batchBD.closeConnection();
		}
	}

	private static void aggiornaSondaOK(BDConfigWrapper configWrapper, String nome) {
		BasicBD bd = null;

		try {
			// costruttore
			bd = BasicBD.newInstance(configWrapper.getTransactionID());

			// apro la connessione
			bd.setupConnection(configWrapper.getTransactionID());

			// setto enableselectforupdate
			bd.enableSelectForUpdate();

			// prendo la connessione
			Connection con = bd.getConnection();

			Sonda sonda = SondaFactory.get(nome, con, bd.getJdbcProperties().getDatabase());
			if(sonda == null) throw new SondaException(MessageFormat.format(ERROR_MSG_SONDA_0_NON_TROVATA, nome));
			//			Properties properties = new Properties();
			//			((SondaBatch)sonda).aggiornaStatoSonda(true, properties, new Date(), "Ok", con, bd.getJdbcProperties().getDatabase());
			((SondaBatch)sonda).aggiornaStatoSonda(true,  new Date(), "Ok", con, bd.getJdbcProperties().getDatabase());
		} catch (Throwable t) {
			log.warn(MessageFormat.format("Errore nell''aggiornamento della sonda OK: {0}", t.getMessage()));
		}
		finally {
			if(bd != null) {
				try {
					bd.disableSelectForUpdate();
				} catch (ServiceException e) {
					log.error("Errore " +e.getMessage() , e);
				}

				bd.closeConnection();
			}
		}
	}

	private static void aggiornaSondaKO(BDConfigWrapper configWrapper, String nome, Exception e) {
		BasicBD bd = null;

		try {
			// costruttore
			bd = BasicBD.newInstance(configWrapper.getTransactionID());

			// apro la connessione
			bd.setupConnection(configWrapper.getTransactionID());

			// setto enableselectforupdate
			bd.enableSelectForUpdate();

			// prendo la connessione
			Connection con = bd.getConnection();
			
			Sonda sonda = SondaFactory.get(nome, con, bd.getJdbcProperties().getDatabase());
			if(sonda == null) throw new SondaException(MessageFormat.format(ERROR_MSG_SONDA_0_NON_TROVATA, nome));
			((SondaBatch)sonda).aggiornaStatoSonda(false, new Date(), MessageFormat.format("Il batch e'' stato interrotto con errore: {0}", e.getMessage()), con, bd.getJdbcProperties().getDatabase());
		} catch (Throwable t) {
			log.warn("Errore nell'aggiornamento della sonda KO: "+ t.getMessage());
		} finally {
			if(bd != null) {
				try {
					bd.disableSelectForUpdate();
				} catch (ServiceException e1) {
					log.error("Errore " +e1.getMessage() , e1);
				}

				bd.closeConnection();
			}
		}
	}
	
	private static Sonda leggiSonda(BDConfigWrapper configWrapper, String nome) {
		BasicBD bd = null;

		try {
			// costruttore
			bd = BasicBD.newInstance(configWrapper.getTransactionID());

			// apro la connessione
			bd.setupConnection(configWrapper.getTransactionID());

			// setto enableselectforupdate
			bd.enableSelectForUpdate();

			// prendo la connessione
			Connection con = bd.getConnection();

			Sonda sonda = SondaFactory.get(nome, con, bd.getJdbcProperties().getDatabase());
			if(sonda == null) throw new SondaException(MessageFormat.format(ERROR_MSG_SONDA_0_NON_TROVATA, nome));
			return sonda;
		} catch (Throwable t) {
			log.warn(MessageFormat.format("Errore nella lettura della sonda [{0}]: {1}", nome, t.getMessage()));
			return null;
		}
		finally {
			if(bd != null) {
				try {
					bd.disableSelectForUpdate();
				} catch (ServiceException e) {
					log.error(MessageFormat.format("Errore {0}", e.getMessage()) , e);
				}

				bd.closeConnection();
			}
		}
	}

	public static String elaborazioneTracciatiPendenze(IContext ctx){
		BDConfigWrapper configWrapper = new BDConfigWrapper(ctx.getTransactionId(), true);
		try {

			if(BatchManager.startEsecuzione(configWrapper, BATCH_TRACCIATI)) {
				log.debug("Elaborazione tracciati");

				TracciatiBD tracciatiBD = new TracciatiBD(configWrapper);
				TracciatoFilter filter = tracciatiBD.newFilter();
				filter.setTipo(Arrays.asList(TIPO_TRACCIATO.PENDENZA));
				filter.setStati(Arrays.asList(STATO_ELABORAZIONE.ELABORAZIONE, STATO_ELABORAZIONE.IN_STAMPA));
				filter.setLimit(25);
				filter.setIncludiRawRichiesta(true);
				List<Tracciato> tracciati = tracciatiBD.findAll(filter);
				Tracciati tracciatiBusiness = new Tracciati();

				while(!tracciati.isEmpty()) {
					log.info(MessageFormat.format("Trovati [{0}] tracciati da elaborare...", tracciati.size()));

					for(Tracciato tracciato: tracciati) {
						log.info("Avvio elaborazione tracciato "  + tracciato.getId());
						ElaboraTracciatoDTO elaboraTracciatoDTO = new ElaboraTracciatoDTO();
						elaboraTracciatoDTO.setTracciato(tracciato);
						tracciatiBusiness.elaboraTracciatoPendenze(elaboraTracciatoDTO, ctx);
						log.info(MessageFormat.format("Elaborazione tracciato {0} completata", tracciato.getId()));
					}
					tracciati = tracciatiBD.findAll(filter);
				}

				aggiornaSondaOK(configWrapper, BATCH_TRACCIATI);
				BatchManager.stopEsecuzione(configWrapper, BATCH_TRACCIATI);
				log.debug("Elaborazione tracciati terminata.");
				return "Elaborazione tracciati terminata.";
			} else {
				log.info("Operazione in corso su altro nodo. Richiesta interrotta.");
				return "Operazione in corso su altro nodo. Richiesta interrotta.";
			}
		} catch (Exception e) {
			try {
				aggiornaSondaKO(configWrapper, BATCH_TRACCIATI, e);
			} catch (Throwable e1) {
				log.error(MessageFormat.format(ERROR_MSG_AGGIORNAMENTO_SONDA_FALLITO_0, e1.getMessage()),e1);
			}
			log.error("Non è stato possibile eseguire l'elaborazione dei tracciati", e);
			return "Non è stato possibile eseguire l'elaborazione dei tracciati: " + e;
		} finally {
		}
	}

	public static String spedizionePromemoria(IContext ctx){
		BDConfigWrapper configWrapper = new BDConfigWrapper(ctx.getTransactionId(), true);
		try {
			it.govpay.bd.model.Configurazione configurazione = new it.govpay.core.business.Configurazione().getConfigurazione();

			MailBatch batchSpedizioneEmail = configurazione.getBatchSpedizioneEmail();
			if(!batchSpedizioneEmail.isAbilitato()) {
				return "Spedizione promemoria disabilitata.";
			}

			if(BatchManager.startEsecuzione(configWrapper, BATCH_SPEDIZIONE_PROMEMORIA)) {
				int limit = 100;
				log.debug(MessageFormat.format("Spedizione primi [{0}] promemoria non consegnati", limit));
				Promemoria promemoriaBD = new Promemoria(); 
				List<it.govpay.bd.model.Promemoria> promemorias = promemoriaBD.findPromemoriaDaSpedire(0, limit);

				if(promemorias.size() == 0) {
					aggiornaSondaOK(configWrapper, BATCH_SPEDIZIONE_PROMEMORIA);
					BatchManager.stopEsecuzione(configWrapper, BATCH_SPEDIZIONE_PROMEMORIA);
					log.debug("Nessun promemoria da inviare.");
					return "Nessun promemoria da inviare.";
				}

				log.info(MessageFormat.format("Trovati [{0}] promemoria da spedire", promemorias.size()));

				for(it.govpay.bd.model.Promemoria promemoria: promemorias) {
					promemoriaBD.invioPromemoria(promemoria);
				}
				aggiornaSondaOK(configWrapper, BATCH_SPEDIZIONE_PROMEMORIA);
				log.debug("Spedizione promemoria completata.");
				return "Spedizione promemoria completata.";
			} else {
				log.info("Operazione in corso su altro nodo. Richiesta interrotta.");
				return "Operazione in corso su altro nodo. Richiesta interrotta.";
			}
		} catch (Exception e) {
			log.error("Non è stato possibile avviare la spedizione dei promemoria", e);
			try {
				aggiornaSondaKO(configWrapper, BATCH_SPEDIZIONE_PROMEMORIA, e); 
			} catch (Throwable e1) {
				log.error(MessageFormat.format(ERROR_MSG_AGGIORNAMENTO_SONDA_FALLITO_0, e1.getMessage()),e1);
			}
			return "Non è stato possibile avviare la spedizione dei promemoria: " + e;
		} finally {
		}
	}

	public static String gestionePromemoria(IContext ctx){
		BDConfigWrapper configWrapper = new BDConfigWrapper(ctx.getTransactionId(), true);
		try {
			it.govpay.bd.model.Configurazione configurazione = new it.govpay.core.business.Configurazione().getConfigurazione();

			MailBatch batchSpedizioneEmail = configurazione.getBatchSpedizioneEmail();
			AppIOBatch batchSpedizioneAppIO = configurazione.getBatchSpedizioneAppIo();

			if(!batchSpedizioneEmail.isAbilitato() && !batchSpedizioneAppIO.isAbilitato()) {
				return "Spedizione promemoria Email e AppIO disabilitata.";
			}

			if(BatchManager.startEsecuzione(configWrapper, BATCH_GESTIONE_PROMEMORIA)) {
				int limit = 100;

				log.debug(MessageFormat.format("Elaborazione primi [{0}] versamenti con promemoria avviso non consegnati", limit));
				VersamentiBD versamentiBD = new VersamentiBD(configWrapper);
				it.govpay.core.business.Versamento versamentoBusiness = new it.govpay.core.business.Versamento();
				List<Versamento> listaPromemoriaAvviso = versamentiBD.findVersamentiConAvvisoDiPagamentoDaSpedire(0, limit);

				if(listaPromemoriaAvviso.size() == 0) {
					log.debug("Nessun promemoria avviso da inviare, controllo presenza promemoria scadenza.");
				} else {
					// elaborazione avvisi...
					log.info(MessageFormat.format("Trovati [{0}] promemoria avviso da spedire", listaPromemoriaAvviso.size()));
					for (Versamento versamento : listaPromemoriaAvviso) {
						versamentoBusiness.inserisciPromemoriaAvviso(versamento);				
					}
				}

				aggiornaSondaOK(configWrapper, BATCH_GESTIONE_PROMEMORIA);

				log.debug(MessageFormat.format("Elaborazione primi [{0}] versamenti con promemoria scadenza via mail non consegnati", limit));
				List<Versamento> listaPromemoriaScadenzaMail = versamentiBD.findVersamentiConAvvisoDiScadenzaDaSpedireViaMail(0, limit);

				if(listaPromemoriaScadenzaMail.size() == 0) {
					log.debug("Nessun promemoria scadenza da inviare via mail.");
				} else { 
					log.info(MessageFormat.format("Trovati [{0}] promemoria scadenza da spedire via mail", listaPromemoriaScadenzaMail.size()));
					for (Versamento versamento : listaPromemoriaScadenzaMail) {
						versamentoBusiness.inserisciPromemoriaScadenzaMail(versamento);
					}
				}

				aggiornaSondaOK(configWrapper, BATCH_GESTIONE_PROMEMORIA);

				log.debug(MessageFormat.format("Elaborazione primi [{0}] versamenti con promemoria scadenza via appIO non consegnati", limit));
				List<Versamento> listaPromemoriaScadenzaAppIO = versamentiBD.findVersamentiConAvvisoDiScadenzaDaSpedireViaAppIO(0, limit);

				if(listaPromemoriaScadenzaAppIO.size() == 0) {
					log.debug("Nessun promemoria scadenza da inviare via appIO.");
				} else {
					log.info(MessageFormat.format("Trovati [{0}] promemoria scadenza da spedire", listaPromemoriaScadenzaAppIO.size()));
					for (Versamento versamento : listaPromemoriaScadenzaAppIO) {
						versamentoBusiness.inserisciPromemoriaScadenzaAppIO(versamento);
					}
				}

				aggiornaSondaOK(configWrapper, BATCH_GESTIONE_PROMEMORIA);
				BatchManager.stopEsecuzione(configWrapper, BATCH_GESTIONE_PROMEMORIA);

				Operazioni.setEseguiInvioPromemoria();
				Operazioni.setEseguiInvioNotificheAppIO();

				log.debug("Gestione promemoria completata.");
				return "Gestione promemoria completata.";
			} else {
				log.info("Operazione in corso su altro nodo. Richiesta interrotta.");
				return "Operazione in corso su altro nodo. Richiesta interrotta.";
			}
		} catch (Exception e) {
			log.error("Non è stato possibile avviare la gestione dei promemoria", e);
			try {
				aggiornaSondaKO(configWrapper, BATCH_GESTIONE_PROMEMORIA, e); 
			} catch (Throwable e1) {
				log.error(MessageFormat.format(ERROR_MSG_AGGIORNAMENTO_SONDA_FALLITO_0, e1.getMessage()),e1);
			}
			return "Non è stato possibile avviare la gestione dei promemoria: " + e;
		} finally {
		}
	}
	
	public static String elaborazioneTracciatiNotificaPagamenti(IContext ctx){
		BDConfigWrapper configWrapper = new BDConfigWrapper(ctx.getTransactionId(), true);
		try {
			if(BatchManager.startEsecuzione(configWrapper, BATCH_ELABORAZIONE_TRACCIATI_NOTIFICA_PAGAMENTI)) {
				
				log.debug("Avvio elaborazione tracciati notifica pagamenti.");
				// ricerca domini con connettore mypivot abilitato
				List<String> domini = AnagraficaManager.getListaCodDomini(configWrapper);
				
				for (String codDominio : domini) {
					it.govpay.bd.model.Dominio dominio = null;
					log.debug(MessageFormat.format("Elaborazione tracciati notifica pagamenti per il Dominio [{0}].", codDominio));
					try {
						dominio = AnagraficaManager.getDominio(configWrapper, codDominio);
					}catch(NotFoundException e) {
						log.debug(MessageFormat.format("Dominio [{0}] non trovato, passo alla prossimo.", dominio));
						continue;
					}

					if(dominio.getConnettoreMyPivot() != null && dominio.getConnettoreMyPivot().isAbilitato()) {
						log.debug(MessageFormat.format("Elaborazione Tracciato MyPivot per il Dominio [{0}]...", codDominio));
						TracciatiNotificaPagamenti tracciatiMyPivot = new TracciatiNotificaPagamenti(it.govpay.model.TracciatoNotificaPagamenti.TIPO_TRACCIATO.MYPIVOT);
						tracciatiMyPivot.elaboraTracciatoNotificaPagamenti(dominio, dominio.getConnettoreMyPivot(), ctx);
						log.debug(MessageFormat.format("Elaborazione Tracciato MyPivot per il Dominio [{0}] completata.", codDominio));
					} else {
						log.debug(MessageFormat.format("Connettore MyPivot non configurato per il Dominio [{0}], non ricerco tracciati da elaborare.", codDominio));
					}
					
					if(dominio.getConnettoreSecim() != null && dominio.getConnettoreSecim().isAbilitato()) {
						log.debug(MessageFormat.format("Elaborazione Tracciato Secim per il Dominio [{0}]...", codDominio));
						TracciatiNotificaPagamenti tracciatiSecim = new TracciatiNotificaPagamenti(it.govpay.model.TracciatoNotificaPagamenti.TIPO_TRACCIATO.SECIM);
						tracciatiSecim.elaboraTracciatoNotificaPagamenti(dominio, dominio.getConnettoreSecim(), ctx);
						log.debug(MessageFormat.format("Elaborazione Tracciato Secim per il Dominio [{0}] completata.", codDominio));
					} else {
						log.debug(MessageFormat.format("Connettore Secim non configurato per il Dominio [{0}], non ricerco tracciati da elaborare.", codDominio));
					}
					
					if(dominio.getConnettoreGovPay() != null && dominio.getConnettoreGovPay().isAbilitato()) {
						log.debug(MessageFormat.format("Elaborazione Tracciato GovPay per il Dominio [{0}]...", codDominio));
						TracciatiNotificaPagamenti tracciatiGovpay = new TracciatiNotificaPagamenti(it.govpay.model.TracciatoNotificaPagamenti.TIPO_TRACCIATO.GOVPAY);
						tracciatiGovpay.elaboraTracciatoNotificaPagamenti(dominio, dominio.getConnettoreGovPay(), ctx);
						log.debug(MessageFormat.format("Elaborazione Tracciato GovPay per il Dominio [{0}] completata.", codDominio));
					} else {
						log.debug(MessageFormat.format("Connettore GovPay non configurato per il Dominio [{0}], non ricerco tracciati da elaborare.", codDominio));
					}
					
					if(dominio.getConnettoreHyperSicAPKappa() != null && dominio.getConnettoreHyperSicAPKappa().isAbilitato()) {
						log.debug(MessageFormat.format("Elaborazione Tracciato HyperSicAPKappa per il Dominio [{0}]...", codDominio));
						TracciatiNotificaPagamenti tracciatiGovpay = new TracciatiNotificaPagamenti(it.govpay.model.TracciatoNotificaPagamenti.TIPO_TRACCIATO.HYPERSIC_APK);
						tracciatiGovpay.elaboraTracciatoNotificaPagamenti(dominio, dominio.getConnettoreHyperSicAPKappa(), ctx);
						log.debug(MessageFormat.format("Elaborazione Tracciato HyperSicAPKappa per il Dominio [{0}] completata.", codDominio));
					} else {
						log.debug(MessageFormat.format("Connettore HyperSicAPKappa non configurato per il Dominio [{0}], non ricerco tracciati da elaborare.", codDominio));
					}
					
					if(dominio.getConnettoreMaggioliJPPA() != null && dominio.getConnettoreMaggioliJPPA().isAbilitato()) {
						log.debug(MessageFormat.format("Elaborazione Tracciato Maggioli JPPA per il Dominio [{0}]...", codDominio));
						TracciatiNotificaPagamenti tracciatiGovpay = new TracciatiNotificaPagamenti(it.govpay.model.TracciatoNotificaPagamenti.TIPO_TRACCIATO.MAGGIOLI_JPPA);
						tracciatiGovpay.elaboraTracciatoNotificaPagamenti(dominio, dominio.getConnettoreMaggioliJPPA(), ctx);
						log.debug(MessageFormat.format("Elaborazione Tracciato Maggioli JPPA per il Dominio [{0}] completata.", codDominio));
					} else {
						log.debug(MessageFormat.format("Connettore Maggioli JPPA non configurato per il Dominio [{0}], non ricerco tracciati da elaborare.", codDominio));
					}
				}
				
				aggiornaSondaOK(configWrapper, BATCH_ELABORAZIONE_TRACCIATI_NOTIFICA_PAGAMENTI);
				BatchManager.stopEsecuzione(configWrapper, BATCH_ELABORAZIONE_TRACCIATI_NOTIFICA_PAGAMENTI);
			
				log.debug("Elaborazione tracciati notifica pagamenti terminata.");
				return "Elaborazione tracciati notifica pagamenti terminata.";
			} else {
				log.info("Operazione in corso su altro nodo. Richiesta interrotta.");
				return "Operazione in corso su altro nodo. Richiesta interrotta.";
			}
		} catch (Exception e) {
			log.error("Non è stato possibile avviare l'elaborazione dei tracciati notifica pagamenti", e);
			try {
				aggiornaSondaKO(configWrapper, BATCH_ELABORAZIONE_TRACCIATI_NOTIFICA_PAGAMENTI, e); 
			} catch (Throwable e1) {
				log.error(MessageFormat.format(ERROR_MSG_AGGIORNAMENTO_SONDA_FALLITO_0, e1.getMessage()),e1);
			}
			return "Non è stato possibile avviare l'elaborazione dei tracciati notifica pagamenti: " + e;
		} finally {
		}
	}
	
	public static String spedizioneTracciatiNotificaPagamenti(IContext ctx){
		BDConfigWrapper configWrapper = new BDConfigWrapper(ctx.getTransactionId(), true);
		try {
			if(BatchManager.startEsecuzione(configWrapper, BATCH_SPEDIZIONE_TRACCIATI_NOTIFICA_PAGAMENTI)) {
				// ricerca domini con connettore mypivot abilitato
				List<String> domini = AnagraficaManager.getListaCodDomini(configWrapper);
				
				int threadNotificaPoolSize = GovpayConfig.getInstance().getDimensionePoolThreadSpedizioneTracciatiNotificaPagamenti();
				for (String codDominio : domini) {
					it.govpay.bd.model.Dominio dominio = null;
					try {
						dominio = AnagraficaManager.getDominio(configWrapper, codDominio);
					}catch(NotFoundException e) {
						log.debug(MessageFormat.format("Dominio [{0}] non trovato, passo alla prossimo.", dominio));
						continue;
					}

					if(dominio.getConnettoreMyPivot() != null && dominio.getConnettoreMyPivot().isAbilitato()) {
						log.debug(MessageFormat.format("Scheduling spedizione Tracciati MyPivot per il Dominio [{0}]...", codDominio));
						TracciatiNotificaPagamenti tracciatiMyPivot = new TracciatiNotificaPagamenti(it.govpay.model.TracciatoNotificaPagamenti.TIPO_TRACCIATO.MYPIVOT);
						
						
						int offset = 0;
						int limit = (2 * threadNotificaPoolSize);
						List<SpedizioneTracciatoNotificaPagamentiThread> threads = new ArrayList<>();
						List<TracciatoNotificaPagamenti> tracciatiInStatoNonTerminalePerDominio = tracciatiMyPivot.findTracciatiInStatoNonTerminalePerDominio(codDominio, offset, limit, dominio.getConnettoreMyPivot(), ctx);
						
						log.debug(MessageFormat.format("Trovati [{0}] Tracciati MyPivot da spedire per il Dominio [{1}]...", tracciatiInStatoNonTerminalePerDominio.size(), codDominio));

						if(tracciatiInStatoNonTerminalePerDominio.size() > 0) {
							for(TracciatoNotificaPagamenti tracciatoMyPivot: tracciatiInStatoNonTerminalePerDominio) {
								SpedizioneTracciatoNotificaPagamentiThread sender = new SpedizioneTracciatoNotificaPagamentiThread(tracciatoMyPivot, dominio.getConnettoreMyPivot(), ctx);
								ThreadExecutorManager.getClientPoolExecutorSpedizioneTracciatiNotificaPagamenti().execute(sender);
								threads.add(sender);
							}

							log.debug("Processi di spedizione Tracciati MyPivot avviati.");
							aggiornaSondaOK(configWrapper, BATCH_SPEDIZIONE_TRACCIATI_NOTIFICA_PAGAMENTI);

							// Aspetto che abbiano finito tutti
							int numeroErrori = 0;
							while(true){
								try {
									Thread.sleep(2000);
								} catch (InterruptedException e) {
									log.warn(MessageFormat.format(ERROR_MSG_INTERRUPTED_0, e.getMessage()), e);
								    // Restore interrupted state...
								    Thread.currentThread().interrupt();
								}
								boolean completed = true;
								for(SpedizioneTracciatoNotificaPagamentiThread sender : threads) {
									if(!sender.isCompleted()) 
										completed = false;
								}

								if(completed) { 
									for(SpedizioneTracciatoNotificaPagamentiThread sender : threads) {
										if(sender.isErrore()) 
											numeroErrori ++;
									}
									int numOk = threads.size() - numeroErrori;
									log.debug(MessageFormat.format(DEBUG_MSG_COMPLETATA_ESECUZIONE_DEI_0_THREADS_OK_1_ERRORE_2, threads.size(), numOk, numeroErrori));
									break; // esco
								}
							}
							
							log.info(MessageFormat.format("Spedizione Tracciati MyPivot per il Dominio [{0}] completata.", codDominio));
							//Hanno finito tutti, aggiorno stato esecuzione
							BatchManager.aggiornaEsecuzione(configWrapper, BATCH_SPEDIZIONE_TRACCIATI_NOTIFICA_PAGAMENTI);
						}
					} else {
						log.debug(MessageFormat.format("Connettore MyPivot non configurato per il Dominio [{0}], non ricerco tracciati da spedire.", codDominio));
					}
					
					if(dominio.getConnettoreSecim() != null && dominio.getConnettoreSecim().isAbilitato()) {
						log.debug(MessageFormat.format("Scheduling spedizione Tracciati Secim per il Dominio [{0}]...", codDominio));
						TracciatiNotificaPagamenti tracciatiSecim = new TracciatiNotificaPagamenti(it.govpay.model.TracciatoNotificaPagamenti.TIPO_TRACCIATO.SECIM);
						
						int offset = 0;
						int limit = (2 * threadNotificaPoolSize);
						List<SpedizioneTracciatoNotificaPagamentiThread> threads = new ArrayList<>();
						List<TracciatoNotificaPagamenti> tracciatiInStatoNonTerminalePerDominio = tracciatiSecim.findTracciatiInStatoNonTerminalePerDominio(codDominio, offset, limit, dominio.getConnettoreSecim(), ctx);
						
						log.debug(MessageFormat.format("Trovati [{0}] Tracciati Secim da spedire per il Dominio [{1}]...", tracciatiInStatoNonTerminalePerDominio.size(), codDominio));

						if(tracciatiInStatoNonTerminalePerDominio.size() > 0) {
							for(TracciatoNotificaPagamenti tracciatoMyPivot: tracciatiInStatoNonTerminalePerDominio) {
								SpedizioneTracciatoNotificaPagamentiThread sender = new SpedizioneTracciatoNotificaPagamentiThread(tracciatoMyPivot, dominio.getConnettoreSecim(), ctx);
								ThreadExecutorManager.getClientPoolExecutorSpedizioneTracciatiNotificaPagamenti().execute(sender);
								threads.add(sender);
							}

							log.debug("Processi di spedizione Tracciati Secim avviati.");
							aggiornaSondaOK(configWrapper, BATCH_SPEDIZIONE_TRACCIATI_NOTIFICA_PAGAMENTI);

							// Aspetto che abbiano finito tutti
							int numeroErrori = 0;
							while(true){
								try {
									Thread.sleep(2000);
								} catch (InterruptedException e) {
									log.warn(MessageFormat.format(ERROR_MSG_INTERRUPTED_0, e.getMessage()), e);
								    // Restore interrupted state...
								    Thread.currentThread().interrupt();
								}
								boolean completed = true;
								for(SpedizioneTracciatoNotificaPagamentiThread sender : threads) {
									if(!sender.isCompleted()) 
										completed = false;
								}

								if(completed) { 
									for(SpedizioneTracciatoNotificaPagamentiThread sender : threads) {
										if(sender.isErrore()) 
											numeroErrori ++;
									}
									int numOk = threads.size() - numeroErrori;
									log.debug(MessageFormat.format(DEBUG_MSG_COMPLETATA_ESECUZIONE_DEI_0_THREADS_OK_1_ERRORE_2, threads.size(), numOk, numeroErrori));
									break; // esco
								}
							}
							
							log.info(MessageFormat.format("Spedizione Tracciati Secim per il Dominio [{0}] completata.", codDominio));
							//Hanno finito tutti, aggiorno stato esecuzione
							BatchManager.aggiornaEsecuzione(configWrapper, BATCH_SPEDIZIONE_TRACCIATI_NOTIFICA_PAGAMENTI);
						}
					} else {
						log.debug(MessageFormat.format("Connettore Secim non configurato per il Dominio [{0}], non ricerco tracciati da spedire.", codDominio));
					}
					
					if(dominio.getConnettoreGovPay() != null && dominio.getConnettoreGovPay().isAbilitato()) {
						log.debug(MessageFormat.format("Scheduling spedizione Tracciati GovPay per il Dominio [{0}]...", codDominio));
						TracciatiNotificaPagamenti tracciatiGovPay = new TracciatiNotificaPagamenti(it.govpay.model.TracciatoNotificaPagamenti.TIPO_TRACCIATO.GOVPAY);
						
						int offset = 0;
						int limit = (2 * threadNotificaPoolSize);
						List<SpedizioneTracciatoNotificaPagamentiThread> threads = new ArrayList<>();
						List<TracciatoNotificaPagamenti> tracciatiInStatoNonTerminalePerDominio = tracciatiGovPay.findTracciatiInStatoNonTerminalePerDominio(codDominio, offset, limit, dominio.getConnettoreGovPay(), ctx);
						
						log.debug(MessageFormat.format("Trovati [{0}] Tracciati GovPay da spedire per il Dominio [{1}]...",	tracciatiInStatoNonTerminalePerDominio.size(), codDominio));

						if(tracciatiInStatoNonTerminalePerDominio.size() > 0) {
							for(TracciatoNotificaPagamenti tracciatGovPay: tracciatiInStatoNonTerminalePerDominio) {
								SpedizioneTracciatoNotificaPagamentiThread sender = new SpedizioneTracciatoNotificaPagamentiThread(tracciatGovPay, dominio.getConnettoreGovPay(), ctx);
								ThreadExecutorManager.getClientPoolExecutorSpedizioneTracciatiNotificaPagamenti().execute(sender);
								threads.add(sender);
							}

							log.debug("Processi di spedizione Tracciati GovPay avviati.");
							aggiornaSondaOK(configWrapper, BATCH_SPEDIZIONE_TRACCIATI_NOTIFICA_PAGAMENTI);

							// Aspetto che abbiano finito tutti
							int numeroErrori = 0;
							while(true){
								try {
									Thread.sleep(2000);
								} catch (InterruptedException e) {
									log.warn(MessageFormat.format(ERROR_MSG_INTERRUPTED_0, e.getMessage()), e);
								    // Restore interrupted state...
								    Thread.currentThread().interrupt();
								}
								boolean completed = true;
								for(SpedizioneTracciatoNotificaPagamentiThread sender : threads) {
									if(!sender.isCompleted()) 
										completed = false;
								}

								if(completed) { 
									for(SpedizioneTracciatoNotificaPagamentiThread sender : threads) {
										if(sender.isErrore()) 
											numeroErrori ++;
									}
									int numOk = threads.size() - numeroErrori;
									log.debug(MessageFormat.format(DEBUG_MSG_COMPLETATA_ESECUZIONE_DEI_0_THREADS_OK_1_ERRORE_2, threads.size(), numOk, numeroErrori));
									break; // esco
								}
							}
							
							log.info(MessageFormat.format("Spedizione Tracciati GovPay per il Dominio [{0}] completata.", codDominio));
							//Hanno finito tutti, aggiorno stato esecuzione
							BatchManager.aggiornaEsecuzione(configWrapper, BATCH_SPEDIZIONE_TRACCIATI_NOTIFICA_PAGAMENTI);
						}
					} else {
						log.debug(MessageFormat.format("Connettore GovPay non configurato per il Dominio [{0}], non ricerco tracciati da spedire.", codDominio));
					}
					
					if(dominio.getConnettoreHyperSicAPKappa() != null && dominio.getConnettoreHyperSicAPKappa().isAbilitato()) {
						log.debug(MessageFormat.format("Scheduling spedizione Tracciati HyperSicAPKappa per il Dominio [{0}]...", codDominio));
						TracciatiNotificaPagamenti tracciatiGovPay = new TracciatiNotificaPagamenti(it.govpay.model.TracciatoNotificaPagamenti.TIPO_TRACCIATO.HYPERSIC_APK);
						
						int offset = 0;
						int limit = (2 * threadNotificaPoolSize);
						List<SpedizioneTracciatoNotificaPagamentiThread> threads = new ArrayList<>();
						List<TracciatoNotificaPagamenti> tracciatiInStatoNonTerminalePerDominio = tracciatiGovPay.findTracciatiInStatoNonTerminalePerDominio(codDominio, offset, limit, dominio.getConnettoreHyperSicAPKappa(), ctx);
						
						log.debug(MessageFormat.format("Trovati [{0}] Tracciati HyperSicAPKappa da spedire per il Dominio [{1}]...", tracciatiInStatoNonTerminalePerDominio.size(),	codDominio));

						if(tracciatiInStatoNonTerminalePerDominio.size() > 0) {
							for(TracciatoNotificaPagamenti tracciatoHyperSicAPKappa: tracciatiInStatoNonTerminalePerDominio) {
								SpedizioneTracciatoNotificaPagamentiThread sender = new SpedizioneTracciatoNotificaPagamentiThread(tracciatoHyperSicAPKappa, dominio.getConnettoreHyperSicAPKappa(), ctx);
								ThreadExecutorManager.getClientPoolExecutorSpedizioneTracciatiNotificaPagamenti().execute(sender);
								threads.add(sender);
							}

							log.debug("Processi di spedizione Tracciati HyperSicAPKappa avviati.");
							aggiornaSondaOK(configWrapper, BATCH_SPEDIZIONE_TRACCIATI_NOTIFICA_PAGAMENTI);

							// Aspetto che abbiano finito tutti
							int numeroErrori = 0;
							while(true){
								try {
									Thread.sleep(2000);
								} catch (InterruptedException e) {
									log.warn(MessageFormat.format(ERROR_MSG_INTERRUPTED_0, e.getMessage()), e);
								    // Restore interrupted state...
								    Thread.currentThread().interrupt();
								}
								boolean completed = true;
								for(SpedizioneTracciatoNotificaPagamentiThread sender : threads) {
									if(!sender.isCompleted()) 
										completed = false;
								}

								if(completed) { 
									for(SpedizioneTracciatoNotificaPagamentiThread sender : threads) {
										if(sender.isErrore()) 
											numeroErrori ++;
									}
									int numOk = threads.size() - numeroErrori;
									log.debug(MessageFormat.format(DEBUG_MSG_COMPLETATA_ESECUZIONE_DEI_0_THREADS_OK_1_ERRORE_2, threads.size(), numOk, numeroErrori));
									break; // esco
								}
							}
							
							log.info(MessageFormat.format("Spedizione Tracciati HyperSicAPKappa per il Dominio [{0}] completata.", codDominio));
							//Hanno finito tutti, aggiorno stato esecuzione
							BatchManager.aggiornaEsecuzione(configWrapper, BATCH_SPEDIZIONE_TRACCIATI_NOTIFICA_PAGAMENTI);
						}
					} else {
						log.debug(MessageFormat.format("Connettore HyperSicAPKappa non configurato per il Dominio [{0}], non ricerco tracciati da spedire.", codDominio));
					}
					
					if(dominio.getConnettoreMaggioliJPPA() != null && dominio.getConnettoreMaggioliJPPA().isAbilitato()) {
						log.debug(MessageFormat.format("Scheduling spedizione Tracciati Maggioli JPPA per il Dominio [{0}]...",	codDominio));
						TracciatiNotificaPagamenti tracciatiGovPay = new TracciatiNotificaPagamenti(it.govpay.model.TracciatoNotificaPagamenti.TIPO_TRACCIATO.MAGGIOLI_JPPA);
						
						int offset = 0;
						int limit = (2 * threadNotificaPoolSize);
						List<SpedizioneTracciatoNotificaPagamentiThread> threads = new ArrayList<>();
						List<TracciatoNotificaPagamenti> tracciatiInStatoNonTerminalePerDominio = tracciatiGovPay.findTracciatiInStatoNonTerminalePerDominio(codDominio, offset, limit, dominio.getConnettoreMaggioliJPPA(), ctx);
						
						log.debug(MessageFormat.format("Trovati [{0}] Tracciati Maggioli JPPA da spedire per il Dominio [{1}]...", tracciatiInStatoNonTerminalePerDominio.size(), codDominio));

						if(tracciatiInStatoNonTerminalePerDominio.size() > 0) {
							for(TracciatoNotificaPagamenti tracciatoHyperSicAPKappa: tracciatiInStatoNonTerminalePerDominio) {
								SpedizioneTracciatoNotificaPagamentiThread sender = new SpedizioneTracciatoNotificaPagamentiThread(tracciatoHyperSicAPKappa, dominio.getConnettoreMaggioliJPPA(), ctx);
								ThreadExecutorManager.getClientPoolExecutorSpedizioneTracciatiNotificaPagamenti().execute(sender);
								threads.add(sender);
							}

							log.debug("Processi di spedizione Tracciati Maggioli JPPA avviati.");
							aggiornaSondaOK(configWrapper, BATCH_SPEDIZIONE_TRACCIATI_NOTIFICA_PAGAMENTI);

							// Aspetto che abbiano finito tutti
							int numeroErrori = 0;
							while(true){
								try {
									Thread.sleep(2000);
								} catch (InterruptedException e) {
									log.warn(MessageFormat.format(ERROR_MSG_INTERRUPTED_0, e.getMessage()), e);
								    // Restore interrupted state...
								    Thread.currentThread().interrupt();
								}
								boolean completed = true;
								for(SpedizioneTracciatoNotificaPagamentiThread sender : threads) {
									if(!sender.isCompleted()) 
										completed = false;
								}

								if(completed) { 
									for(SpedizioneTracciatoNotificaPagamentiThread sender : threads) {
										if(sender.isErrore()) 
											numeroErrori ++;
									}
									int numOk = threads.size() - numeroErrori;
									log.debug(MessageFormat.format(DEBUG_MSG_COMPLETATA_ESECUZIONE_DEI_0_THREADS_OK_1_ERRORE_2, threads.size(), numOk, numeroErrori));
									break; // esco
								}
							}
							
							log.info(MessageFormat.format("Spedizione Tracciati Maggioli JPPA per il Dominio [{0}] completata.", codDominio));
							//Hanno finito tutti, aggiorno stato esecuzione
							BatchManager.aggiornaEsecuzione(configWrapper, BATCH_SPEDIZIONE_TRACCIATI_NOTIFICA_PAGAMENTI);
						}
					} else {
						log.debug(MessageFormat.format("Connettore Maggioli JPPA non configurato per il Dominio [{0}], non ricerco tracciati da spedire.", codDominio));
					}
				}
				
				aggiornaSondaOK(configWrapper, BATCH_SPEDIZIONE_TRACCIATI_NOTIFICA_PAGAMENTI);
				BatchManager.stopEsecuzione(configWrapper, BATCH_SPEDIZIONE_TRACCIATI_NOTIFICA_PAGAMENTI);
			
				log.debug("Spedizione tracciati notifica pagamenti terminata.");
				return "Spedizione tracciati notifica pagamenti terminata.";
			} else {
				log.info("Operazione in corso su altro nodo. Richiesta interrotta.");
				return "Operazione in corso su altro nodo. Richiesta interrotta.";
			}
		} catch (ServiceException | IOException e) {
			log.error("Non è stato possibile avviare la spedizione dei tracciati notifica pagamenti", e);
			try {
				aggiornaSondaKO(configWrapper, BATCH_SPEDIZIONE_TRACCIATI_NOTIFICA_PAGAMENTI, e); 
			} catch (Throwable e1) {
				log.error(MessageFormat.format(ERROR_MSG_AGGIORNAMENTO_SONDA_FALLITO_0, e1.getMessage()),e1);
			}
			return MessageFormat.format("Non è stato possibile avviare la spedizione dei tracciati notifica pagamenti: {0}", e);
		} finally {
		}
	}
	
	public static String elaborazioneRiconciliazioni(IContext ctx){
		BDConfigWrapper configWrapper = new BDConfigWrapper(ctx.getTransactionId(), true);
		try {
			if(BatchManager.startEsecuzione(configWrapper, BATCH_RICONCILIAZIONI)) {
				
				int offset = 0;
				int limit = 25;
				IncassiBD incassiBD = new IncassiBD(configWrapper);
				
				log.debug("Ricerca nuove riconciliazioni da elaborare...");
				
				List<Incasso> findRiconciliazioniDaAcquisire = incassiBD.findRiconciliazioniDaAcquisire(configWrapper, offset, limit, true);
				
				log.debug(MessageFormat.format("Trovate [{0}] riconciliazioni.", findRiconciliazioniDaAcquisire.size()));
				
				if(findRiconciliazioniDaAcquisire.size() > 0) {
					Incassi incassi = new Incassi();
					
					for (Incasso incasso : findRiconciliazioniDaAcquisire) {
						incassi.elaboraRiconciliazione(incasso.getCodDominio(), incasso.getIdRiconciliazione(), ctx);
					}
				}
				
				aggiornaSondaOK(configWrapper, BATCH_RICONCILIAZIONI);
				BatchManager.stopEsecuzione(configWrapper, BATCH_RICONCILIAZIONI);
			
				log.debug("Elaborazione riconciliazioni terminata.");
				return "Elaborazione riconciliazioni terminata.";
			} else {
				log.info("Operazione in corso su altro nodo. Richiesta interrotta.");
				return "Operazione in corso su altro nodo. Richiesta interrotta.";
			}
		} catch (Exception e) {
			log.error("Non è stato possibile avviare l'elaborazione delle riconciliazioni", e);
			try {
				aggiornaSondaKO(configWrapper, BATCH_RICONCILIAZIONI, e); 
			} catch (Throwable e1) {
				log.error(MessageFormat.format(ERROR_MSG_AGGIORNAMENTO_SONDA_FALLITO_0, e1.getMessage()),e1);
			}
			return MessageFormat.format("Non è stato possibile avviare l''elaborazione delle riconciliazioni: {0}", e);
		} finally {
		}
	}
}
