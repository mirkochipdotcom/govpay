package it.govpay.rs.v1.controllers.base;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.openspcoop2.generic_project.exception.ServiceException;
import org.slf4j.Logger;

import it.govpay.bd.model.Operazione;
import it.govpay.bd.model.Tracciato;
import it.govpay.core.dao.pagamenti.PendenzeDAO;
import it.govpay.core.dao.pagamenti.TracciatiDAO;
import it.govpay.core.dao.pagamenti.dto.LeggiPendenzaDTO;
import it.govpay.core.dao.pagamenti.dto.LeggiPendenzaDTOResponse;
import it.govpay.core.dao.pagamenti.dto.LeggiTracciatoDTO;
import it.govpay.core.dao.pagamenti.dto.ListaOperazioniTracciatoDTO;
import it.govpay.core.dao.pagamenti.dto.ListaOperazioniTracciatoDTOResponse;
import it.govpay.core.dao.pagamenti.dto.ListaPendenzeDTO;
import it.govpay.core.dao.pagamenti.dto.ListaPendenzeDTOResponse;
import it.govpay.core.dao.pagamenti.dto.ListaTracciatiDTO;
import it.govpay.core.dao.pagamenti.dto.ListaTracciatiDTOResponse;
import it.govpay.core.dao.pagamenti.dto.PatchPendenzaDTO;
import it.govpay.core.dao.pagamenti.dto.PostTracciatoDTO;
import it.govpay.core.exceptions.GovPayException;
import it.govpay.core.rs.v1.beans.base.FaultBean;
import it.govpay.core.rs.v1.beans.base.FaultBean.CategoriaEnum;
import it.govpay.core.rs.v1.beans.base.ListaOperazioniPendenza;
import it.govpay.core.rs.v1.beans.base.ListaPendenze;
import it.govpay.core.rs.v1.beans.base.ListaTracciatiPendenza;
import it.govpay.core.rs.v1.beans.base.OperazionePendenza;
import it.govpay.core.rs.v1.beans.base.PatchOp;
import it.govpay.core.rs.v1.beans.base.PatchOp.OpEnum;
import it.govpay.core.rs.v1.beans.base.Pendenza;
import it.govpay.core.rs.v1.beans.base.PendenzaIndex;
import it.govpay.core.rs.v1.beans.base.StatoTracciatoPendenza;
import it.govpay.core.rs.v1.beans.base.TracciatoPendenze;
import it.govpay.core.utils.GovpayConfig;
import it.govpay.core.utils.GpContext;
import it.govpay.core.utils.GpThreadLocal;
import it.govpay.model.IAutorizzato;
import it.govpay.model.Tracciato.TIPO_TRACCIATO;
import it.govpay.rs.BaseRsService;
import it.govpay.rs.v1.beans.converter.PendenzeConverter;
import it.govpay.rs.v1.beans.converter.TracciatiConverter;


public class PendenzeController extends it.govpay.rs.BaseController {

     public PendenzeController(String nomeServizio,Logger log) {
		super(nomeServizio,log, GovpayConfig.GOVPAY_BACKOFFICE_OPEN_API_FILE_NAME);
     }
     
     public Response pendenzeIdA2AIdPendenzaGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , String idA2A, String idPendenza) {
		String methodName = "getByIda2aIdPendenza";  
		GpContext ctx = null;
		String transactionId = null;
		ByteArrayOutputStream baos= null;
		this.log.info("Esecuzione " + methodName + " in corso..."); 

		try{
			baos = new ByteArrayOutputStream();
			this.logRequest(uriInfo, httpHeaders, methodName, baos);
			
			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();
			
			LeggiPendenzaDTO leggiPendenzaDTO = new LeggiPendenzaDTO(user);
			
			leggiPendenzaDTO.setCodA2A(idA2A);
			leggiPendenzaDTO.setCodPendenza(idPendenza);
			
			PendenzeDAO pendenzeDAO = new PendenzeDAO(); 
			
			LeggiPendenzaDTOResponse ricevutaDTOResponse = pendenzeDAO.leggiPendenza(leggiPendenzaDTO);

			Pendenza pendenza = PendenzeConverter.toRsModel(ricevutaDTOResponse.getVersamento(), ricevutaDTOResponse.getUnitaOperativa(), ricevutaDTOResponse.getApplicazione(), ricevutaDTOResponse.getDominio(), ricevutaDTOResponse.getLstSingoliVersamenti());
			return this.handleResponseOk(Response.status(Status.OK).entity(pendenza.toJSON(null)),transactionId).build();
		}catch (Exception e) {
			return handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
    }
    
    public Response pendenzeGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , Integer pagina, Integer risultatiPerPagina, String ordinamento, String campi, String idDominio, String idA2A, String idDebitore, String stato, String idPagamento) {
    	GpContext ctx = null;
    	String transactionId = null;
		ByteArrayOutputStream baos= null;
		String methodName = "pendenzeGET";
		try{
			this.log.info("Esecuzione " + methodName + " in corso...");
			baos = new ByteArrayOutputStream();
			this.logRequest(uriInfo, httpHeaders, methodName, baos);
			
			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();
			
			// Parametri - > DTO Input
			
			ListaPendenzeDTO listaPendenzeDTO = new ListaPendenzeDTO(user);
			
			listaPendenzeDTO.setPagina(pagina);
			listaPendenzeDTO.setLimit(risultatiPerPagina);
			listaPendenzeDTO.setStato(stato);
			
			if(idDominio != null)
				listaPendenzeDTO.setIdDominio(idDominio);
			if(idA2A != null)
				listaPendenzeDTO.setIdA2A(idA2A);
			if(idDebitore != null)
				listaPendenzeDTO.setIdDebitore(idDebitore);
			
			if(idPagamento != null)
				listaPendenzeDTO.setIdPagamento(idPagamento);
			
			if(ordinamento != null)
				listaPendenzeDTO.setOrderBy(ordinamento);
			// INIT DAO
			
			PendenzeDAO pendenzeDAO = new PendenzeDAO(); 
			
			// CHIAMATA AL DAO
			
			ListaPendenzeDTOResponse listaPendenzeDTOResponse = pendenzeDAO.listaPendenze(listaPendenzeDTO);
			
			// CONVERT TO JSON DELLA RISPOSTA
			
			List<PendenzaIndex> results = new ArrayList<PendenzaIndex>();
			for(LeggiPendenzaDTOResponse ricevutaDTOResponse: listaPendenzeDTOResponse.getResults()) {
				PendenzaIndex rsModel = PendenzeConverter.toRsModelIndex(ricevutaDTOResponse.getVersamento());
				results.add(rsModel);
			}
			
			ListaPendenze response = new ListaPendenze(results, this.getServicePath(uriInfo),
					listaPendenzeDTOResponse.getTotalResults(), pagina, risultatiPerPagina);
			
			this.logResponse(uriInfo, httpHeaders, methodName, response.toJSON(campi), 200);
			this.log.info("Esecuzione " + methodName + " completata."); 
			return this.handleResponseOk(Response.status(Status.OK).entity(response.toJSON(campi)),transactionId).build();
			
		}catch (Exception e) {
			return handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
    }

    @SuppressWarnings("unchecked")
	public Response pendenzeIdA2AIdPendenzaPATCH(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , String idA2A, String idPendenza, java.io.InputStream is) {
    	String methodName = "pendenzeIdA2AIdPendenzaPATCH";  
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
			
			PendenzeDAO pendenzeDAO = new PendenzeDAO(); 
			
			PatchPendenzaDTO patchPendenzaDTO = new PatchPendenzaDTO(user);
			patchPendenzaDTO.setIdA2a(idA2A);
			patchPendenzaDTO.setIdPendenza(idPendenza);
			
			String jsonRequest = baos.toString();
			
			List<PatchOp> lstOp = new ArrayList<>();
			
			try {
				List<java.util.LinkedHashMap<?,?>> lst = PatchOp.parse(jsonRequest, List.class);
				for(java.util.LinkedHashMap<?,?> map: lst) {
					PatchOp op = new PatchOp();
					op.setOp(OpEnum.fromValue((String) map.get("op")));
					op.setPath((String) map.get("path"));
					op.setValue(map.get("value"));
					op.validate();
					lstOp.add(op);
				}
			} catch (ServiceException e) {
				lstOp = PatchOp.parse(jsonRequest, List.class);
//				PatchOp op = PatchOp.parse(jsonRequest);
//				op.validate();
//				lstOp.add(op);
			}
			
			patchPendenzaDTO.setOp(lstOp );
			
			pendenzeDAO.patch(patchPendenzaDTO);
			
			LeggiPendenzaDTO leggiPendenzaDTO = new LeggiPendenzaDTO(user);
			
			leggiPendenzaDTO.setCodA2A(idA2A);
			leggiPendenzaDTO.setCodPendenza(idPendenza);
			
			LeggiPendenzaDTOResponse ricevutaDTOResponse = pendenzeDAO.leggiPendenza(leggiPendenzaDTO);

			Pendenza pendenza = PendenzeConverter.toRsModel(ricevutaDTOResponse.getVersamento(), ricevutaDTOResponse.getUnitaOperativa(), ricevutaDTOResponse.getApplicazione(), ricevutaDTOResponse.getDominio(), ricevutaDTOResponse.getLstSingoliVersamenti());
			return this.handleResponseOk(Response.status(Status.OK).entity(pendenza.toJSON(null)),transactionId).build();
		} catch(GovPayException e) {
			log.error("Errore durante il processo di pagamento", e);
			FaultBean respKo = new FaultBean();
			respKo.setCategoria(CategoriaEnum.OPERAZIONE);
			respKo.setCodice(e.getCodEsito().name());
			respKo.setDescrizione(e.getDescrizioneEsito());
			respKo.setDettaglio(e.getMessage());
			try {
				this.logResponse(uriInfo, httpHeaders, methodName, respKo.toJSON(null), 500);
			}catch(Exception e1) {
				log.error("Errore durante il log della risposta", e1);
			}
			return this.handleResponseOk(Response.status(Status.INTERNAL_SERVER_ERROR).entity(getRespJson(respKo)),transactionId).build();
		}catch (Exception e) {
			return handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
    }



    public Response pendenzePOST(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , java.io.InputStream is, String nomeFile) {
    	String methodName = "pendenzePOST";  
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

			TracciatiDAO tracciatiDAO = new TracciatiDAO();
			
			PostTracciatoDTO postTracciatoDTO = new PostTracciatoDTO(user);
			postTracciatoDTO.setContenuto(baos.toByteArray());
			postTracciatoDTO.setNomeFile(nomeFile);
			tracciatiDAO.create(postTracciatoDTO);
			
			Status responseStatus = Status.OK;
			
			this.logResponse(uriInfo, httpHeaders, methodName, new byte[0], responseStatus.getStatusCode());
			this.log.info("Esecuzione " + methodName + " completata."); 
			return this.handleResponseOk(Response.status(responseStatus),transactionId).build();
		}catch (Exception e) {
			return handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
    }



    public Response pendenzeTracciatiGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , Integer pagina, Integer risultatiPerPagina, StatoTracciatoPendenza stato) {
    	GpContext ctx = null;
    	String transactionId = null;
		ByteArrayOutputStream baos= null;
		String methodName = "pendenzeTracciatiGET";
		try{
			this.log.info("Esecuzione " + methodName + " in corso...");
			baos = new ByteArrayOutputStream();
			this.logRequest(uriInfo, httpHeaders, methodName, baos);
			
			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();
			
			// Parametri - > DTO Input
			
			ListaTracciatiDTO listaTracciatiDTO = new ListaTracciatiDTO(user);
			
			listaTracciatiDTO.setPagina(pagina);
			listaTracciatiDTO.setLimit(risultatiPerPagina);
			listaTracciatiDTO.setStatoTracciatoPendenza(stato);
			List<TIPO_TRACCIATO> tipoTracciato = new ArrayList<>();
			tipoTracciato.add(TIPO_TRACCIATO.PENDENZA);
			listaTracciatiDTO.setTipoTracciato(tipoTracciato);
			
			// INIT DAO
			
			TracciatiDAO tracciatiDAO = new TracciatiDAO(); 
			
			// CHIAMATA AL DAO
			
			ListaTracciatiDTOResponse listaTracciatiDTOResponse = tracciatiDAO.listaTracciati(listaTracciatiDTO);
			
			// CONVERT TO JSON DELLA RISPOSTA
			
			List<TracciatoPendenze> results = new ArrayList<TracciatoPendenze>();
			for(Tracciato tracciato: listaTracciatiDTOResponse.getResults()) {
				TracciatoPendenze rsModel = TracciatiConverter.toTracciatoPendenzeRsModel(tracciato);
				results.add(rsModel);
			}
			
			ListaTracciatiPendenza response = new ListaTracciatiPendenza(results, this.getServicePath(uriInfo),
					listaTracciatiDTOResponse.getTotalResults(), pagina, risultatiPerPagina);
			
			this.logResponse(uriInfo, httpHeaders, methodName, response.toJSON(null), 200);
			this.log.info("Esecuzione " + methodName + " completata."); 
			return this.handleResponseOk(Response.status(Status.OK).entity(response.toJSON(null)),transactionId).build();
			
		}catch (Exception e) {
			return handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
    }



    public Response pendenzeTracciatiIdGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , Integer id) {
    	String methodName = "pendenzeTracciatiIdGET";  
		GpContext ctx = null;
		String transactionId = null;
		ByteArrayOutputStream baos= null;
		this.log.info("Esecuzione " + methodName + " in corso..."); 
		
		String accept = null;
		if(httpHeaders.getRequestHeaders().containsKey("Accept")) {
			accept = httpHeaders.getRequestHeaders().get("Accept").get(0).toLowerCase();
		}

		try{
			baos = new ByteArrayOutputStream();
			this.logRequest(uriInfo, httpHeaders, methodName, baos);
			
			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();
			
			LeggiTracciatoDTO leggiTracciatoDTO = new LeggiTracciatoDTO(user);
			leggiTracciatoDTO.setId((long) id);
			
			TracciatiDAO tracciatiDAO = new TracciatiDAO();
			Tracciato tracciato = tracciatiDAO.leggiTracciato(leggiTracciatoDTO);
			
			if(accept.toLowerCase().contains(MediaType.APPLICATION_OCTET_STREAM)) {
				ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
				ZipOutputStream zos = new ZipOutputStream(baos1);
				
				ZipEntry tracciatoInputEntry = new ZipEntry(tracciato.getFileNameRichiesta());
				zos.putNextEntry(tracciatoInputEntry);
				zos.write(tracciato.getRawRichiesta());
				zos.flush();
				zos.closeEntry();
				
				if(tracciato.getRawEsito() != null) {
					ZipEntry tracciatoOutputEntry = new ZipEntry(tracciato.getFileNameEsito());
					zos.putNextEntry(tracciatoOutputEntry);
					zos.write(tracciato.getRawEsito());
					zos.flush();
					zos.closeEntry();
				}
				
				zos.flush();
				zos.close();
				
				
				// TODO aggiungere avvisi e fare in streaming
				
				String zipFileName = (tracciato.getFileNameEsito().indexOf(".") > 0 ? tracciato.getFileNameEsito().substring(0, tracciato.getFileNameEsito().lastIndexOf(".")) : tracciato.getFileNameEsito()) + ".zip";
				byte[] b = baos1.toByteArray();
				return this.handleResponseOk(Response.status(Status.OK).type(MediaType.APPLICATION_OCTET_STREAM).entity(b).header("content-disposition", "attachment; filename=\""+zipFileName+"\""),transactionId).build();
			} else {
				TracciatoPendenze rsModel = TracciatiConverter.toTracciatoPendenzeRsModel(tracciato);
				return this.handleResponseOk(Response.status(Status.OK).entity(rsModel.toJSON(null)),transactionId).build();
			}
		}catch (Exception e) {
			return handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
    }



    public Response pendenzeTracciatiIdOperazioniGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , Integer id, Integer pagina, Integer risultatiPerPagina) {
    	GpContext ctx = null;
    	String transactionId = null;
		ByteArrayOutputStream baos= null;
		String methodName = "pendenzeTracciatiGET";
		try{
			this.log.info("Esecuzione " + methodName + " in corso...");
			baos = new ByteArrayOutputStream();
			this.logRequest(uriInfo, httpHeaders, methodName, baos);
			
			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();
			
			// Parametri - > DTO Input
			
			ListaOperazioniTracciatoDTO listaOperazioniTracciatoDTO = new ListaOperazioniTracciatoDTO(user);
			
			listaOperazioniTracciatoDTO.setPagina(pagina);
			listaOperazioniTracciatoDTO.setLimit(risultatiPerPagina);
			listaOperazioniTracciatoDTO.setIdTracciato((long) id);
			
			// INIT DAO
			
			TracciatiDAO tracciatiDAO = new TracciatiDAO(); 
			
			// CHIAMATA AL DAO
			
			ListaOperazioniTracciatoDTOResponse listaTracciatiDTOResponse = tracciatiDAO.listaOperazioniTracciatoPendenza(listaOperazioniTracciatoDTO);
			
			// CONVERT TO JSON DELLA RISPOSTA
			
			List<OperazionePendenza> results = new ArrayList<OperazionePendenza>();
			for(Operazione operazione: listaTracciatiDTOResponse.getResults()) {
				OperazionePendenza rsModel = TracciatiConverter.toOperazioneTracciatoPendenzaRsModel(operazione);
				results.add(rsModel);
			}
			
			ListaOperazioniPendenza response = new ListaOperazioniPendenza(results, this.getServicePath(uriInfo),
					listaTracciatiDTOResponse.getTotalResults(), pagina, risultatiPerPagina);
			
			this.logResponse(uriInfo, httpHeaders, methodName, response.toJSON(null), 200);
			this.log.info("Esecuzione " + methodName + " completata."); 
			return this.handleResponseOk(Response.status(Status.OK).entity(response.toJSON(null)),transactionId).build();
			
		}catch (Exception e) {
			return handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
    }


}


