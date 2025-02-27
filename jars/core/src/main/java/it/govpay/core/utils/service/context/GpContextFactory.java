package it.govpay.core.utils.service.context;

import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.utils.UtilsException;
import org.openspcoop2.utils.logger.ILogger;
import org.openspcoop2.utils.logger.LoggerFactory;
import org.openspcoop2.utils.logger.log4j.Log4JLoggerWithBatchContext;
import org.openspcoop2.utils.service.context.Context;
import org.openspcoop2.utils.service.context.ContextFactory;
import org.openspcoop2.utils.service.context.IContextFactory;

import it.govpay.core.beans.EventoContext.Componente;
import it.govpay.core.utils.GpContext;

public class GpContextFactory extends ContextFactory implements IContextFactory {
	
	private String apiName;
	
	@Override
	public Context newContext() throws UtilsException {
		
		ILogger logger = LoggerFactory.newLogger();
		
		GpContext context;
		try {
			context = GpContext.newContext();
			context.getEventoCtx().setComponente(this.getApiNameEnum());
			logger.initLogger(context);
			Context context2 = new Context(logger, this.isLoggerPrefixEnabled());
			return context2;
		} catch (ServiceException e) {
			throw new UtilsException(e);
		}
	}
	
	public Context newBatchContext() throws UtilsException {
		
		ILogger logger = LoggerFactory.newLogger(Log4JLoggerWithBatchContext.class);
		
		GpContext context;
		try {
			context = GpContext.newBatchContext();
			logger.initLogger(context);
			return new Context(logger, this.isLoggerPrefixEnabled());
		} catch (ServiceException e) {
			throw new UtilsException(e);
		}
	}
	
	public Context newContext(String requestUri, String nomeServizio, String nomeOperazione, String httpMethod, int versioneServizio, String user, Componente componente) throws UtilsException {
		
		ILogger logger = LoggerFactory.newLogger();
		
		GpContext context;
		try {
			context = new GpContext(requestUri, nomeServizio, nomeOperazione, httpMethod, versioneServizio, user,componente);
			logger.initLogger(context);
			return new Context(logger, this.isLoggerPrefixEnabled());
		} catch (ServiceException e) {
			throw new UtilsException(e);
		}
	}
	
	public Componente getApiNameEnum() {
		return Componente.valueOf(this.apiName);
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}
}
