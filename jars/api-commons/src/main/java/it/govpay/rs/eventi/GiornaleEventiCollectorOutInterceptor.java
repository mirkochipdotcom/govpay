package it.govpay.rs.eventi;

import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.ext.logging.AbstractLoggingInterceptor;
import org.apache.cxf.ext.logging.event.DefaultLogEventMapper;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.cxf.ext.logging.event.LogEventSender;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.openspcoop2.utils.LoggerWrapperFactory;
import org.openspcoop2.utils.service.context.ContextThreadLocal;
import org.openspcoop2.utils.service.context.IContext;
import org.slf4j.Logger;

import it.govpay.core.beans.EventoContext;
import it.govpay.core.beans.EventoContext.Esito;
import it.govpay.core.dao.configurazione.ConfigurazioneDAO;
import it.govpay.core.utils.GovpayConfig;
import it.govpay.core.utils.GpContext;
import it.govpay.core.utils.client.HttpMethod;
import it.govpay.core.utils.rawutils.ConverterUtils;
import it.govpay.model.configurazione.GdeInterfaccia;
import it.govpay.model.eventi.DettaglioRichiesta;
import it.govpay.model.eventi.DettaglioRisposta;

public class GiornaleEventiCollectorOutInterceptor extends org.apache.cxf.ext.logging.LoggingOutInterceptor  {

	private Logger log = LoggerWrapperFactory.getLogger(GiornaleEventiCollectorOutInterceptor.class);
	private GiornaleEventiConfig giornaleEventiConfig = null;
	private ConfigurazioneDAO configurazioneDAO = null;

	public GiornaleEventiCollectorOutInterceptor() {
		super();
		this.configurazioneDAO = new ConfigurazioneDAO();
	}

	@Override
	public void handleMessage(Message message) throws Fault {
		String url = null;
		String httpMethodS = null;
		String principal = null;
		Esito esito = null;
		try {
			if(!this.giornaleEventiConfig.isAbilitaGiornaleEventi()) return;
			
			String apiName = this.giornaleEventiConfig.getApiName();
			boolean logEvento = false;
			boolean dumpEvento = false;
			IContext context = ContextThreadLocal.get();
			GpContext appContext = (GpContext) context.getApplicationContext();
			EventoContext eventoCtx = appContext.getEventoCtx();

			Exchange exchange = message.getExchange();

			Message inMessage = exchange.getInMessage();
			final LogEvent eventRequest = new DefaultLogEventMapper().map(inMessage);
			url = eventoCtx.getUrl() != null ? eventoCtx.getUrl() : eventRequest.getAddress();
			httpMethodS = eventoCtx.getMethod() != null ? eventoCtx.getMethod() : eventRequest.getHttpMethod();
			principal = eventoCtx.getPrincipal()!= null ? eventoCtx.getPrincipal() : eventRequest.getPrincipal();
			
			HttpMethod httpMethod = GiornaleEventiUtilities.getHttpMethod(httpMethodS);
			esito = eventoCtx.getEsito() != null ? eventoCtx.getEsito() : Esito.KO;
			
			this.log.debug(MessageFormat.format("Log Evento API: [{0}] Method [{1}], Url [{2}], Esito [{3}]", apiName, httpMethodS,
					url, esito));

			GdeInterfaccia configurazioneInterfaccia = GiornaleEventiUtilities.getConfigurazioneGiornaleEventi(context, this.configurazioneDAO, this.giornaleEventiConfig);

			if(configurazioneInterfaccia == null) {
				this.log.warn(MessageFormat.format(
						"La configurazione per l''API [{0}] non e'' corretta, salvataggio evento non eseguito.",
						apiName));
				return;
			}
			
			this.log.debug(MessageFormat.format("Configurazione Giornale Eventi API: [{0}]: {1}", apiName, ConverterUtils.toJSON(configurazioneInterfaccia)));
			
			if(GiornaleEventiUtilities.isRequestLettura(httpMethod, this.giornaleEventiConfig.getApiNameEnum(), eventoCtx.getTipoEvento())) {
				logEvento = GiornaleEventiUtilities.logEvento(configurazioneInterfaccia.getLetture(), esito);
				dumpEvento = GiornaleEventiUtilities.dumpEvento(configurazioneInterfaccia.getLetture(), esito);
				this.log.debug(MessageFormat.format("Tipo Operazione ''Lettura'', Log [{0}], Dump [{1}].", logEvento, dumpEvento));
			} else if(GiornaleEventiUtilities.isRequestScrittura(httpMethod, this.giornaleEventiConfig.getApiNameEnum(), eventoCtx.getTipoEvento())) {
				logEvento = GiornaleEventiUtilities.logEvento(configurazioneInterfaccia.getScritture(), esito);
				dumpEvento = GiornaleEventiUtilities.dumpEvento(configurazioneInterfaccia.getScritture(), esito);
				this.log.debug(MessageFormat.format("Tipo Operazione ''Scrittura'', Log [{0}], Dump [{1}].", logEvento, dumpEvento));
			} else {
				this.log.debug("Tipo Operazione non riconosciuta, l'evento non verra' salvato.");
			}

			eventoCtx.setRegistraEvento(logEvento);
			if(logEvento) {
				Date dataIngresso = eventoCtx.getDataRichiesta();
				Date dataUscita = new Date();
				// lettura informazioni dalla richiesta
				
				DettaglioRichiesta dettaglioRichiesta = new DettaglioRichiesta();
				
			
				dettaglioRichiesta.setPrincipal(principal);
				dettaglioRichiesta.setUtente(eventoCtx.getUtente());
				dettaglioRichiesta.setUrl(url);
				dettaglioRichiesta.setMethod(httpMethodS);
				dettaglioRichiesta.setDataOraRichiesta(dataIngresso);
				dettaglioRichiesta.setHeadersFromMap(eventRequest.getHeaders());
				
				// lettura informazioni dalla response
				final LogEvent eventResponse = new DefaultLogEventMapper().map(message);
				DettaglioRisposta dettaglioRisposta = new DettaglioRisposta();
				dettaglioRisposta.setHeadersFromMap(eventResponse.getHeaders());
				dettaglioRisposta.setDataOraRisposta(dataUscita);

				eventoCtx.setDataRisposta(dataUscita);
				eventoCtx.setDettaglioRichiesta(dettaglioRichiesta);
				eventoCtx.setDettaglioRisposta(dettaglioRisposta);
				
				eventoCtx.setTransactionId(context.getTransactionId());
				
				String clusterId = GovpayConfig.getInstance().getClusterId();
				if(clusterId == null)
					clusterId = GovpayConfig.getInstance().getAppName();
				
				eventoCtx.setClusterId(clusterId);

				if(dumpEvento) {
					// dump richiesta
					if (shouldLogContent(eventRequest)) {
						GiornaleEventiUtilities.addContent(inMessage, eventRequest, this.giornaleEventiConfig);
					} else {
						eventRequest.setPayload(AbstractLoggingInterceptor.CONTENT_SUPPRESSED);
					}
					if(eventRequest.getPayload() != null)
						dettaglioRichiesta.setPayload(Base64.getEncoder().encodeToString(eventRequest.getPayload().getBytes()));

					// dump risposta
					final OutputStream os = message.getContent(OutputStream.class);
					if (os != null) {
						LoggingCallback callback = new LoggingCallback(this.sender, message, eventoCtx, os, this.limit);
						message.setContent(OutputStream.class, createCacheAndWriteOutputStream(message, os, callback));
					}
				} 
			}
		} catch (Throwable e) {
			this.log.error(e.getMessage(),e);
		} finally {

		}
	}

	private OutputStream createCacheAndWriteOutputStream(Message message, final OutputStream os, CachedOutputStreamCallback callback) {
		final CacheAndWriteOutputStream newOut = new CacheAndWriteOutputStream(os);
		if (this.threshold > 0) {
			newOut.setThreshold(this.threshold);
		}
		if (this.limit > 0) {
			// make the limit for the cache greater than the limit for the truncated payload in the log event, 
			// this is necessary for finding out that the payload was truncated 
			//(see boolean isTruncated = cos.size() > limit && limit != -1;)  in method copyPayload
			newOut.setCacheLimit(getCacheLimitExt());
		}
		newOut.registerCallback(callback);
		return newOut;
	}

	private int getCacheLimitExt() {
		if (this.limit == Integer.MAX_VALUE) {
			return this.limit;
		}
		return this.limit + 1;
	}

	public class LoggingCallback implements CachedOutputStreamCallback {

		private final Message message;
		private final OutputStream origStream;
		private final int lim;
		@SuppressWarnings("unused")
		private LogEventSender sender;
		private EventoContext eventoCtx;

		public LoggingCallback(final LogEventSender sender, final Message msg, EventoContext eventoCtx, final OutputStream os, int limit) {
			this.sender = sender;
			this.message = msg;
			this.origStream = os;
			this.lim = limit == -1 ? Integer.MAX_VALUE : limit;
			this.eventoCtx = eventoCtx;
		}

		@Override
		public void onFlush(CachedOutputStream cos) {
			// do nothing.
		}

		@Override
		public void onClose(CachedOutputStream cos) {

			try {
				final LogEvent event = new DefaultLogEventMapper().map(this.message);
				if (shouldLogContent(event)) {
					copyPayload(cos, event);
				} else {
					event.setPayload(CONTENT_SUPPRESSED);
				}
				
				if(event.getPayload() != null)
					this.eventoCtx.getDettaglioRisposta().setPayload(Base64.getEncoder().encodeToString(event.getPayload().getBytes()));

				try {
					// empty out the cache
					cos.lockOutputStream();
					cos.resetOut(null, false);
				} catch (Exception ex) {
					// ignore
				}
				this.message.setContent(OutputStream.class, this.origStream);
			} catch (Throwable e) {
				LoggerWrapperFactory.getLogger(GiornaleEventiCollectorOutInterceptor.class).error(e.getMessage(),e);
				throw new Fault(e); 
			}
		}

		private void copyPayload(CachedOutputStream cos, final LogEvent event) {
			try {
				String encoding = (String) this.message.get(Message.ENCODING);
				StringBuilder payload = new StringBuilder();
				writePayload(payload, cos, encoding, event.getContentType());
				event.setPayload(payload.toString());
				boolean isTruncated = cos.size() > this.lim && this.lim != -1;
				event.setTruncated(isTruncated);
			} catch (Exception ex) {
				// ignore
			}
		}

		protected void writePayload(StringBuilder builder, CachedOutputStream cos, String encoding, String contentType)
				throws Exception {
			if (StringUtils.isEmpty(encoding)) {
				cos.writeCacheTo(builder, this.lim);
			} else {
				cos.writeCacheTo(builder, encoding, this.lim);
			}
		}
	}

	public GiornaleEventiConfig getGiornaleEventiConfig() {
		return giornaleEventiConfig;
	}

	public void setGiornaleEventiConfig(GiornaleEventiConfig giornaleEventiConfig) {
		this.giornaleEventiConfig = giornaleEventiConfig;
	}
}
