package it.govpay.ragioneria.v3.api.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.openspcoop2.utils.service.context.ContextThreadLocal;
import org.springframework.security.core.Authentication;

import it.govpay.bd.BDConfigWrapper;
import it.govpay.core.autorizzazione.AuthorizationManager;
import it.govpay.core.autorizzazione.utils.AutorizzazioneUtils;
import it.govpay.core.beans.Costanti;
import it.govpay.core.dao.pagamenti.IncassiDAO;
import it.govpay.core.dao.pagamenti.dto.LeggiIncassoDTO;
import it.govpay.core.dao.pagamenti.dto.LeggiIncassoDTOResponse;
import it.govpay.core.dao.pagamenti.dto.ListaIncassiDTO;
import it.govpay.core.dao.pagamenti.dto.ListaIncassiDTOResponse;
import it.govpay.core.dao.pagamenti.dto.RichiestaIncassoDTO;
import it.govpay.core.dao.pagamenti.dto.RichiestaIncassoDTOResponse;
import it.govpay.core.serialization.JsonJacksonDeserializer;
import it.govpay.core.utils.SimpleDateFormatUtils;
import it.govpay.core.utils.UriBuilderUtils;
import it.govpay.core.utils.validator.ValidatorFactory;
import it.govpay.core.utils.validator.ValidatoreIdentificativi;
import it.govpay.core.utils.validator.ValidatoreUtils;
import it.govpay.model.Acl.Diritti;
import it.govpay.model.Acl.Servizio;
import it.govpay.model.Utenza.TIPO_UTENZA;
import it.govpay.ragioneria.v3.api.RiconciliazioniApi;
import it.govpay.ragioneria.v3.beans.NuovaRiconciliazione;
import it.govpay.ragioneria.v3.beans.Riconciliazione;
import it.govpay.ragioneria.v3.beans.RiconciliazioneIndex;
import it.govpay.ragioneria.v3.beans.Riconciliazioni;
import it.govpay.ragioneria.v3.beans.converter.RiconciliazioniConverter;


/**
 * GovPay - API Ragioneria
 *
 * <p>No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 */
public class RiconciliazioniApiServiceImpl extends BaseApiServiceImpl  implements RiconciliazioniApi {

	public RiconciliazioniApiServiceImpl() {
		super("riconciliazioni", RiconciliazioniApiServiceImpl.class);
	}

	/**
     * Riconciliazione di un movimento di cassa
     *
     */
    @Override
	public Response addRiconciliazione(String idDominio, String id, InputStream is) {
    	this.buildContext();
        Authentication user = this.getUser();
        String methodName = "addRiconciliazione";
		String transactionId = ContextThreadLocal.get().getTransactionId();
		this.log.debug(MessageFormat.format(BaseApiServiceImpl.LOG_MSG_ESECUZIONE_METODO_IN_CORSO, methodName));
		try(ByteArrayOutputStream baos = new ByteArrayOutputStream();){
			// salvo il json ricevuto
			IOUtils.copy(is, baos);

			// autorizzazione sulla API
			this.isAuthorized(user, Arrays.asList(TIPO_UTENZA.APPLICAZIONE), Arrays.asList(Servizio.API_RAGIONERIA), Arrays.asList(Diritti.SCRITTURA));

			ValidatoreIdentificativi validatoreId = ValidatoreIdentificativi.newInstance();
			validatoreId.validaIdDominio("idDominio", idDominio);
			validatoreId.validaIdRiconciliazione("id", id);

			NuovaRiconciliazione incasso = JsonJacksonDeserializer.parse(baos.toString(), NuovaRiconciliazione.class);
			incasso.validate();

			RichiestaIncassoDTO richiestaIncassoDTO = RiconciliazioniConverter.toRichiestaIncassoDTO(incasso, idDominio, id, user);

//			if(idFlussoCaseInsensitive != null) {
//				richiestaIncassoDTO.setRicercaIdFlussoCaseInsensitive(idFlussoCaseInsensitive);
//			}

			IncassiDAO incassiDAO = new IncassiDAO();

			RichiestaIncassoDTOResponse richiestaIncassoDTOResponse = incassiDAO.addRiconciliazione(richiestaIncassoDTO);

			Riconciliazione incassoExt = RiconciliazioniConverter.toRsModel(richiestaIncassoDTOResponse.getIncasso());

			this.log.debug(MessageFormat.format(BaseApiServiceImpl.LOG_MSG_ESECUZIONE_METODO_COMPLETATA, methodName));
			if(richiestaIncassoDTOResponse.isCreated()) {
				Status responseStatus = Status.ACCEPTED;

				return this.handleResponseOk(Response.status(responseStatus)
						.header("Location", UriBuilderUtils.getRiconciliazioniByIdDominioIdIncasso(richiestaIncassoDTOResponse.getIncasso().getCodDominio(), incassoExt.getId()))
						,transactionId).build()
						;
			} else {
				Status responseStatus = Status.OK;
				return this.handleResponseOk(Response.status(responseStatus).entity(incassoExt),transactionId).build();
			}
		}catch (Exception e) {
			return this.handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			this.log(ContextThreadLocal.get());
		}
    }

    /**
     * Elenco dei movimenti di cassa riconciliati
     *
     */
    @Override
	public Response findRiconciliazioni(Integer pagina, Integer risultatiPerPagina, String idDominio, String dataDa, String dataA, Boolean metadatiPaginazione, Boolean maxRisultati, String sct) {
    	this.buildContext();
    	Authentication user = this.getUser();
        String methodName = "findRiconciliazioni";
		String transactionId = ContextThreadLocal.get().getTransactionId();
		this.log.debug(MessageFormat.format(BaseApiServiceImpl.LOG_MSG_ESECUZIONE_METODO_IN_CORSO, methodName));
		try{
			// autorizzazione sulla API
			this.isAuthorized(user, Arrays.asList(TIPO_UTENZA.APPLICAZIONE), Arrays.asList(Servizio.API_RAGIONERIA), Arrays.asList(Diritti.LETTURA));

			ValidatorFactory vf = ValidatorFactory.newInstance();
			ValidatoreUtils.validaRisultatiPerPagina(vf, Costanti.PARAMETRO_RISULTATI_PER_PAGINA, risultatiPerPagina);

			ListaIncassiDTO listaIncassoDTO = new ListaIncassiDTO(user);

			listaIncassoDTO.setLimit(risultatiPerPagina);
			listaIncassoDTO.setPagina(pagina);
			listaIncassoDTO.setIdDominio(idDominio);

			if(dataDa != null) {
				Date dataDaDate = SimpleDateFormatUtils.getDataDaConTimestamp(dataDa, "dataDa");
				listaIncassoDTO.setDataDa(dataDaDate);
			}
			if(dataA != null) {
				Date dataADate = SimpleDateFormatUtils.getDataAConTimestamp(dataA, "dataA");
				listaIncassoDTO.setDataA(dataADate);
			}

			// filtro sull'applicazione
			listaIncassoDTO.setIdA2A(AutorizzazioneUtils.getAuthenticationDetails(user).getApplicazione().getCodApplicazione());

			// autorizzazione sui domini
			List<String> domini = AuthorizationManager.getDominiAutorizzati(user);
			listaIncassoDTO.setCodDomini(domini);

			listaIncassoDTO.setEseguiCount(metadatiPaginazione);
			listaIncassoDTO.setEseguiCountConLimit(maxRisultati);

			IncassiDAO incassiDAO = new IncassiDAO();
			ListaIncassiDTOResponse listaIncassiDTOResponse = domini != null ? incassiDAO.listaIncassi(listaIncassoDTO) : new ListaIncassiDTOResponse(0L, new ArrayList<>());

			// CONVERT TO JSON DELLA RISPOSTA

			List<RiconciliazioneIndex> listaIncassi = new ArrayList<>();
			for(it.govpay.bd.model.Incasso i : listaIncassiDTOResponse.getResults()) {
				listaIncassi.add(RiconciliazioniConverter.toRsIndexModel(i));
			}

			Riconciliazioni response = new Riconciliazioni(this.getServicePath(uriInfo), listaIncassiDTOResponse.getTotalResults(), pagina, risultatiPerPagina);
			response.setRisultati(listaIncassi);

			this.log.debug(MessageFormat.format(BaseApiServiceImpl.LOG_MSG_ESECUZIONE_METODO_COMPLETATA, methodName));
			return this.handleResponseOk(Response.status(Status.OK).entity(response),transactionId).build();

		}catch (Exception e) {
			return this.handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			this.log(ContextThreadLocal.get());
		}
    }

    /**
     * Dettaglio di un movimento di cassa riconciliato
     *
     */
    @Override
	public Response getRiconciliazione(String idDominio, String id) {
    	this.buildContext();
        Authentication user = this.getUser();
        String methodName = "getRiconciliazione";
		String transactionId = ContextThreadLocal.get().getTransactionId();
		BDConfigWrapper configWrapper = new BDConfigWrapper(ContextThreadLocal.get().getTransactionId(), true);
		this.log.debug(MessageFormat.format(BaseApiServiceImpl.LOG_MSG_ESECUZIONE_METODO_IN_CORSO, methodName));
		try{
			// autorizzazione sulla API
			this.isAuthorized(user, Arrays.asList(TIPO_UTENZA.APPLICAZIONE), Arrays.asList(Servizio.API_RAGIONERIA), Arrays.asList(Diritti.LETTURA));

			ValidatoreIdentificativi validatoreId = ValidatoreIdentificativi.newInstance();
			validatoreId.validaIdDominio("idDominio", idDominio);
			validatoreId.validaIdRiconciliazione("id", id);

			// Parametri - > DTO Input

			LeggiIncassoDTO leggiIncassoDTO = new LeggiIncassoDTO(user);
			leggiIncassoDTO.setIdDominio(idDominio);
			leggiIncassoDTO.setIdRiconciliazione(id);

			if(!AuthorizationManager.isDominioAuthorized(leggiIncassoDTO.getUser(), leggiIncassoDTO.getIdDominio())) {
				throw AuthorizationManager.toNotAuthorizedException(leggiIncassoDTO.getUser(), leggiIncassoDTO.getIdDominio(), null);
			}

//			List<TipoPagamento> tipoEnum = new ArrayList<>();
//			if(riscossioniTipo == null || riscossioniTipo.isEmpty()) { // valori di default
//				tipoEnum.add(TipoPagamento.ENTRATA);
//				tipoEnum.add(TipoPagamento.MBT);
//			}

//			if(riscossioniTipo!=null) {
//				for (String tipoS : riscossioniTipo) {
//					TipoRiscossione tipoRiscossione = TipoRiscossione.fromValue(tipoS);
//					if(tipoRiscossione != null) {
//						tipoEnum.add(TipoPagamento.valueOf(tipoRiscossione.toString()));
//					} else {
//						throw new ValidationException("Codifica inesistente per tipo. Valore fornito [" + riscossioniTipo + "] valori possibili " + ArrayUtils.toString(TipoRiscossione.values()));
//					}
//				}
//			}

//			leggiIncassoDTO.setTipoRiscossioni(tipoEnum);
			leggiIncassoDTO.setTipoRiscossioni(null);

			IncassiDAO incassiDAO = new IncassiDAO();

			// CHIAMATA AL DAO

			LeggiIncassoDTOResponse leggiIncassoDTOResponse = incassiDAO.leggiIncasso(leggiIncassoDTO);

			// filtro sull'applicazione
			if(!AutorizzazioneUtils.getAuthenticationDetails(user).getApplicazione().getCodApplicazione().equals(leggiIncassoDTOResponse.getIncasso().getApplicazione(configWrapper).getCodApplicazione())) {
				throw AuthorizationManager.toNotAuthorizedException(user);
			}

			// CONVERT TO JSON DELLA RISPOSTA

			Riconciliazione response = RiconciliazioniConverter.toRsModel(leggiIncassoDTOResponse.getIncasso());

			this.log.debug(MessageFormat.format(BaseApiServiceImpl.LOG_MSG_ESECUZIONE_METODO_COMPLETATA, methodName));
			return this.handleResponseOk(Response.status(Status.OK).entity(response),transactionId).build();

		}catch (Exception e) {
			return this.handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			this.log(ContextThreadLocal.get());
		}
    }

}

