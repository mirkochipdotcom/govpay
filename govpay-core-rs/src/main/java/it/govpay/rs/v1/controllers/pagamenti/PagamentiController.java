package it.govpay.rs.v1.controllers.pagamenti;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;

import it.govpay.bd.model.PagamentoPortale;
import it.govpay.bd.model.PagamentoPortale.STATO;
import it.govpay.core.dao.pagamenti.PagamentiPortaleDAO;
import it.govpay.core.dao.pagamenti.dto.LeggiPagamentoPortaleDTO;
import it.govpay.core.dao.pagamenti.dto.LeggiPagamentoPortaleDTOResponse;
import it.govpay.core.dao.pagamenti.dto.LeggiPendenzaDTOResponse;
import it.govpay.core.dao.pagamenti.dto.LeggiRptDTOResponse;
import it.govpay.core.dao.pagamenti.dto.ListaPagamentiPortaleDTO;
import it.govpay.core.dao.pagamenti.dto.ListaPagamentiPortaleDTOResponse;
import it.govpay.core.dao.pagamenti.dto.PagamentiPortaleDTO;
import it.govpay.core.dao.pagamenti.dto.PagamentiPortaleDTOResponse;
import it.govpay.core.exceptions.GovPayException;
import it.govpay.core.rs.v1.beans.pagamenti.FaultBeanEsteso;
import it.govpay.core.rs.v1.beans.pagamenti.FaultBeanEsteso.CategoriaEnum;
import it.govpay.core.rs.v1.beans.pagamenti.ListaPagamentiIndex;
import it.govpay.core.rs.v1.beans.pagamenti.PagamentiPortaleResponseOk;
import it.govpay.core.rs.v1.beans.pagamenti.PagamentoPost;
import it.govpay.core.rs.v1.beans.pagamenti.PendenzaIndex;
import it.govpay.core.rs.v1.beans.pagamenti.RppIndex;
import it.govpay.core.utils.GovpayConfig;
import it.govpay.core.utils.GpContext;
import it.govpay.core.utils.GpThreadLocal;
import it.govpay.core.utils.UriBuilderUtils;
import it.govpay.model.IAutorizzato;
import it.govpay.rs.BaseRsService;
import it.govpay.rs.v1.beans.pagamenti.converter.PagamentiPortaleConverter;
import it.govpay.rs.v1.beans.pagamenti.converter.PendenzeConverter;
import it.govpay.rs.v1.beans.pagamenti.converter.RptConverter;
import net.sf.json.JsonConfig;



public class PagamentiController extends it.govpay.rs.BaseController {

     public PagamentiController(String nomeServizio,Logger log) {
		super(nomeServizio,log, GovpayConfig.GOVPAY_PAGAMENTI_OPEN_API_FILE_NAME);
     }


    public Response pagamentiPOST(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , java.io.InputStream is, String idSessionePortale) {
    	String methodName = "pagamentiPOST";  
		GpContext ctx = null;
		String transactionId = null;
		ByteArrayOutputStream baos= null;
		this.log.info("Esecuzione " + methodName + " in corso..."); 
		try{
			baos = new ByteArrayOutputStream();
			// salvo il json ricevuto
			BaseRsService.copy(is, baos);
			this.logRequest(uriInfo, httpHeaders, methodName, baos);
			
			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();
			
			String jsonRequest = baos.toString();
			JsonConfig jsonConfig = new JsonConfig();
			Map<String, Class<?>> classMap = new HashMap<String, Class<?>>();
			classMap.put("autenticazioneSoggetto", String.class);
			jsonConfig.setClassMap(classMap);
			PagamentoPost pagamentiPortaleRequest= (PagamentoPost) PagamentoPost.parse(jsonRequest, PagamentoPost.class, jsonConfig);
			
			
			String idSession = transactionId.replace("-", "");
			PagamentiPortaleDTO pagamentiPortaleDTO = PagamentiPortaleConverter.getPagamentiPortaleDTO(pagamentiPortaleRequest, jsonRequest, user,idSession, idSessionePortale);
			
			PagamentiPortaleDAO pagamentiPortaleDAO = new PagamentiPortaleDAO(); 
			
			PagamentiPortaleDTOResponse pagamentiPortaleDTOResponse = pagamentiPortaleDAO.inserisciPagamenti(pagamentiPortaleDTO);
						
			PagamentiPortaleResponseOk responseOk = PagamentiPortaleConverter.getPagamentiPortaleResponseOk(pagamentiPortaleDTOResponse);
			
			this.logResponse(uriInfo, httpHeaders, methodName, responseOk.toJSON(null), Status.CREATED.getStatusCode());
			this.log.info("Esecuzione " + methodName + " completata."); 
			return this.handleResponseOk(Response.status(Status.CREATED).entity(responseOk.toJSON(null)),transactionId).build();
		} catch (Exception e) {
			Response response = handleException(uriInfo, httpHeaders, methodName, e,transactionId);
			if(e instanceof GovPayException && (PagamentoPortale) ((GovPayException) e).getParam() != null) {
				it.govpay.core.rs.v1.beans.base.FaultBean fb = (it.govpay.core.rs.v1.beans.base.FaultBean) it.govpay.core.rs.v1.beans.base.FaultBean.parse((String) response.getEntity(), it.govpay.core.rs.v1.beans.base.FaultBean.class);
				FaultBeanEsteso fbe = new FaultBeanEsteso();
				fbe.setCodice(fb.getCodice());
				fbe.setCategoria(CategoriaEnum.fromValue(fb.getCategoria().toString()));
				fbe.setDescrizione(fb.getDescrizione());
				fbe.setDettaglio(fb.getDettaglio());
				fbe.setId(((PagamentoPortale) ((GovPayException) e).getParam()).getIdSessione());
				fbe.setLocation(UriBuilderUtils.getFromPagamenti(fbe.getId()));
				return Response.fromResponse(response).entity(fbe.toJSON(null)).build();
			}
			return response;
		} finally {
			if(ctx != null) ctx.log();
		}
    }
    
    public Response pagamentiIdGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , String id) {
    	String methodName = "getPagamentoPortaleById";  
		GpContext ctx = null;
		String transactionId = null;
		ByteArrayOutputStream baos= null;
		this.log.info("Esecuzione " + methodName + " in corso..."); 
		try{
			baos = new ByteArrayOutputStream();
			this.logRequest(uriInfo, httpHeaders, methodName, baos);
			
			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();
			
			LeggiPagamentoPortaleDTO leggiPagamentoPortaleDTO = new LeggiPagamentoPortaleDTO(user);
			leggiPagamentoPortaleDTO.setIdSessione(id);
			leggiPagamentoPortaleDTO.setRisolviLink(true); 
			
			PagamentiPortaleDAO pagamentiPortaleDAO = new PagamentiPortaleDAO(); 
			
			LeggiPagamentoPortaleDTOResponse pagamentoPortaleDTOResponse = pagamentiPortaleDAO.leggiPagamentoPortale(leggiPagamentoPortaleDTO);
			
			it.govpay.bd.model.PagamentoPortale pagamentoPortaleModel = pagamentoPortaleDTOResponse.getPagamento();
			it.govpay.core.rs.v1.beans.pagamenti.Pagamento response = PagamentiPortaleConverter.toRsModel(pagamentoPortaleModel);
			
			List<RppIndex> rpp = new ArrayList<RppIndex>();
			for(LeggiRptDTOResponse leggiRptDtoResponse: pagamentoPortaleDTOResponse.getListaRpp()) {
				rpp.add(RptConverter.toRsModelIndex(leggiRptDtoResponse.getRpt(),leggiRptDtoResponse.getVersamento(),leggiRptDtoResponse.getApplicazione()));
			}
			response.setRpp(rpp);

			List<PendenzaIndex> pendenze = new ArrayList<PendenzaIndex>();
			for(LeggiPendenzaDTOResponse leggiRptDtoResponse: pagamentoPortaleDTOResponse.getListaPendenze()) {
				pendenze.add(PendenzeConverter.toRsModelIndex(leggiRptDtoResponse.getVersamento()));
			}
			response.setPendenze(pendenze);
			
			this.logResponse(uriInfo, httpHeaders, methodName, response.toJSON(null), 200);
			this.log.info("Esecuzione " + methodName + " completata."); 
			return this.handleResponseOk(Response.status(Status.OK).entity(response.toJSON(null)),transactionId).build();
		}catch (Exception e) {
			return handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
    }

    public Response pagamentiGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , Integer pagina, Integer risultatiPerPagina, String ordinamento, String campi, String stato, String versante, String idSessionePortale) {
    	String methodName = "getListaPagamenti";  
		GpContext ctx = null;
		String transactionId = null;
		ByteArrayOutputStream baos= null;
		this.log.info("Esecuzione " + methodName + " in corso..."); 
		try{
			baos = new ByteArrayOutputStream();
			this.logRequest(uriInfo, httpHeaders, methodName, baos);
			
			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();
			
			// Parametri - > DTO Input
			
			ListaPagamentiPortaleDTO listaPagamentiPortaleDTO = new ListaPagamentiPortaleDTO(user);
			listaPagamentiPortaleDTO.setPagina(pagina);
			listaPagamentiPortaleDTO.setLimit(risultatiPerPagina);
			listaPagamentiPortaleDTO.setStato(stato);
			
			if(versante != null)
				listaPagamentiPortaleDTO.setVersante(versante);

			if(ordinamento != null)
				listaPagamentiPortaleDTO.setOrderBy(ordinamento);
			// INIT DAO
			
			PagamentiPortaleDAO pagamentiPortaleDAO = new PagamentiPortaleDAO();
			
			// CHIAMATA AL DAO
			
			ListaPagamentiPortaleDTOResponse pagamentoPortaleDTOResponse = pagamentiPortaleDAO.listaPagamentiPortale(listaPagamentiPortaleDTO);
			
			// CONVERT TO JSON DELLA RISPOSTA
			
			List<it.govpay.core.rs.v1.beans.pagamenti.PagamentoIndex> results = new ArrayList<it.govpay.core.rs.v1.beans.pagamenti.PagamentoIndex>();
			for(it.govpay.bd.model.PagamentoPortale pagamentoPortale: pagamentoPortaleDTOResponse.getResults()) {
				results.add(PagamentiPortaleConverter.toRsModelIndex(pagamentoPortale));
			}
			
			ListaPagamentiIndex response = new ListaPagamentiIndex(results, this.getServicePath(uriInfo),
					pagamentoPortaleDTOResponse.getTotalResults(), pagina, risultatiPerPagina);
			
			this.logResponse(uriInfo, httpHeaders, methodName, response.toJSON(campi), 200);
			this.log.info("Esecuzione " + methodName + " completata."); 
			return this.handleResponseOk(Response.status(Status.OK).entity(response.toJSON(campi)),transactionId).build();
		}catch (Exception e) {
			return handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
    }

}


