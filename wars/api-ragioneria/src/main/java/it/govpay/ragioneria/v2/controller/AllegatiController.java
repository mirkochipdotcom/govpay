package it.govpay.ragioneria.v2.controller;


import java.text.MessageFormat;
import java.util.Arrays;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.openspcoop2.utils.service.context.ContextThreadLocal;
import org.slf4j.Logger;
import org.springframework.security.core.Authentication;

import it.govpay.bd.model.Allegato;
import it.govpay.core.autorizzazione.AuthorizationManager;
import it.govpay.core.dao.pagamenti.AllegatiDAO;
import it.govpay.core.dao.pagamenti.dto.LeggiAllegatoDTO;
import it.govpay.core.dao.pagamenti.dto.LeggiAllegatoDTOResponse;
import it.govpay.model.Acl.Diritti;
import it.govpay.model.Acl.Servizio;
import it.govpay.model.Utenza.TIPO_UTENZA;




public class AllegatiController extends BaseController {

     public AllegatiController(String nomeServizio,Logger log) {
		super(nomeServizio,log);
     }



    public Response getAllegatoPendenza(Authentication user, UriInfo uriInfo, HttpHeaders httpHeaders , Long id) {
    	String methodName = "getAllegatoPendenza";
		String transactionId = ContextThreadLocal.get().getTransactionId();

		this.log.debug(MessageFormat.format(BaseController.LOG_MSG_ESECUZIONE_METODO_IN_CORSO, methodName));

		try{
			// autorizzazione sulla API
			this.isAuthorized(user, Arrays.asList(TIPO_UTENZA.APPLICAZIONE), Arrays.asList(Servizio.API_RAGIONERIA), Arrays.asList(Diritti.LETTURA));

			AllegatiDAO allegatiDAO = new AllegatiDAO();

			LeggiAllegatoDTO leggiAllegatoDTO = new LeggiAllegatoDTO(user);
			leggiAllegatoDTO.setId(id);
			leggiAllegatoDTO.setIncludiRawContenuto(false);

			LeggiAllegatoDTOResponse leggiAllegatoDTOResponse = allegatiDAO.leggiAllegato(leggiAllegatoDTO);

			Allegato allegato = leggiAllegatoDTOResponse.getAllegato();

			// controllo che il dominio sia autorizzato
			if(!AuthorizationManager.isDominioAuthorized(user, leggiAllegatoDTOResponse.getDominio().getCodDominio())) {
				throw AuthorizationManager.toNotAuthorizedException(user,leggiAllegatoDTOResponse.getDominio().getCodDominio(), null);
			}

			String allegatoFileName = allegato.getNome();
			String mediaType = allegato.getTipo() != null? allegato.getTipo() : MediaType.APPLICATION_OCTET_STREAM;

			StreamingOutput contenutoStream = allegatiDAO.leggiBlobContenuto(allegato.getId());

			this.log.debug(MessageFormat.format(BaseController.LOG_MSG_ESECUZIONE_METODO_COMPLETATA, methodName));
			return this.handleResponseOk(Response.status(Status.OK).type(mediaType).entity(contenutoStream).header("content-disposition", "attachment; filename=\""+allegatoFileName+"\""),transactionId).build();

		}catch (Exception e) {
			return this.handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			this.logContext(ContextThreadLocal.get());
		}
    }


}


