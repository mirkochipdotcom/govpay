package it.govpay.backoffice.v1.controllers;


import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.ArrayUtils;
import org.openspcoop2.utils.service.context.ContextThreadLocal;
import org.slf4j.Logger;
import org.springframework.security.core.Authentication;

import it.govpay.backoffice.v1.beans.LinguaSecondaria;
import it.govpay.core.autorizzazione.AuthorizationManager;
import it.govpay.core.dao.anagrafica.dto.GetDocumentoAvvisiDTO;
import it.govpay.core.dao.anagrafica.dto.GetDocumentoAvvisiDTO.FormatoDocumento;
import it.govpay.core.dao.anagrafica.dto.GetDocumentoAvvisiDTOResponse;
import it.govpay.core.dao.pagamenti.AvvisiDAO;
import it.govpay.core.exceptions.ValidationException;
import it.govpay.core.exceptions.NotAcceptableException;
import it.govpay.core.utils.GpContext;
import it.govpay.core.utils.IuvUtils;
import it.govpay.core.utils.validator.ValidatoreIdentificativi;
import it.govpay.model.Acl.Diritti;
import it.govpay.model.Acl.Servizio;
import it.govpay.model.Utenza.TIPO_UTENZA;


public class DocumentiController extends BaseController {

     public DocumentiController(String nomeServizio,Logger log) {
		super(nomeServizio,log);
     }



    public Response getAvvisiDocumento(Authentication user, UriInfo uriInfo, HttpHeaders httpHeaders , String idA2A, String idDominio, String numeroDocumento, String linguaSecondaria, List<String> numeriAvviso) {
    	String methodName = "getAvvisiDocumento";
		String transactionId = ContextThreadLocal.get().getTransactionId();

		this.log.debug(MessageFormat.format(BaseController.LOG_MSG_ESECUZIONE_METODO_IN_CORSO, methodName));

		try{
			// autorizzazione sulla API
			this.isAuthorized(user, Arrays.asList(TIPO_UTENZA.OPERATORE, TIPO_UTENZA.APPLICAZIONE), Arrays.asList(Servizio.PENDENZE), Arrays.asList(Diritti.LETTURA));

			ValidatoreIdentificativi validatoreId = ValidatoreIdentificativi.newInstance();
			validatoreId.validaIdApplicazione("idA2A", idA2A);
			validatoreId.validaIdDominio("idDominio", idDominio);
			validatoreId.validaIdDocumento("numeroDocumento", numeroDocumento);

			GetDocumentoAvvisiDTO getAvvisoDTO = new GetDocumentoAvvisiDTO(user, idDominio, numeroDocumento);
			getAvvisoDTO.setCodApplicazione(idA2A);

			if(linguaSecondaria != null) {
				LinguaSecondaria linguaSecondariaEnum = LinguaSecondaria.fromValue(linguaSecondaria);
				if(linguaSecondariaEnum != null) {
					switch(linguaSecondariaEnum) {
					case DE:
						getAvvisoDTO.setLinguaSecondaria(it.govpay.core.beans.tracciati.LinguaSecondaria.DE);
						break;
					case EN:
						getAvvisoDTO.setLinguaSecondaria(it.govpay.core.beans.tracciati.LinguaSecondaria.EN);
						break;
					case FALSE:
						getAvvisoDTO.setLinguaSecondaria(it.govpay.core.beans.tracciati.LinguaSecondaria.FALSE);
						break;
					case FR:
						getAvvisoDTO.setLinguaSecondaria(it.govpay.core.beans.tracciati.LinguaSecondaria.FR);
						break;
					case SL:
						getAvvisoDTO.setLinguaSecondaria(it.govpay.core.beans.tracciati.LinguaSecondaria.SL);
						break;
					}
				} else {
					throw new ValidationException("Codifica inesistente per linguaSecondaria. Valore fornito [" + linguaSecondaria + "] valori possibili " + ArrayUtils.toString(LinguaSecondaria.values()));
				}
			}

			if(numeriAvviso != null && !numeriAvviso.isEmpty()) {
				for (String numeroAvviso : numeriAvviso) {
					IuvUtils.toIuv(numeroAvviso); // validazione numero avviso
				}
			}

			getAvvisoDTO.setNumeriAvviso(numeriAvviso);
			
			String accept = "";
			if(httpHeaders.getRequestHeaders().containsKey("Accept")) {
				accept = httpHeaders.getRequestHeaders().get("Accept").get(0).toLowerCase();
			}

			if(!AuthorizationManager.isDominioAuthorized(getAvvisoDTO.getUser(), getAvvisoDTO.getCodDominio())) {
				throw AuthorizationManager.toNotAuthorizedException(getAvvisoDTO.getUser(), getAvvisoDTO.getCodDominio(),null);
			}

			AvvisiDAO avvisiDAO = new AvvisiDAO();

			if(accept.toLowerCase().contains("application/pdf")) {
				getAvvisoDTO.setFormato(FormatoDocumento.PDF);
				GetDocumentoAvvisiDTOResponse getAvvisoDTOResponse = avvisiDAO.getDocumento(getAvvisoDTO);

//				((GpContext) (ContextThreadLocal.get()).getApplicationContext()).getEventoCtx().setIdPendenza(getAvvisoDTOResponse.getVersamento().getCodVersamentoEnte());
				((GpContext) (ContextThreadLocal.get()).getApplicationContext()).getEventoCtx().setIdA2A(getAvvisoDTOResponse.getApplicazione().getCodApplicazione());

				this.log.debug(MessageFormat.format(BaseController.LOG_MSG_ESECUZIONE_METODO_COMPLETATA, methodName));
				return this.handleResponseOk(Response.status(Status.OK).type("application/pdf").entity(getAvvisoDTOResponse.getDocumentoPdf()).header("content-disposition", "attachment; filename=\""+getAvvisoDTOResponse.getFilenameDocumento()+"\""),transactionId).build();
			} else {
				// formato non accettato
				throw new NotAcceptableException("Documento di pagamento non disponibile nel formato indicato nell'header Accept, ricevuto: '"+accept+"', consentito: 'application/pdf'");
			}
		}catch (Exception e) {
			return this.handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			this.logContext(ContextThreadLocal.get());
		}
    }


}


