package it.govpay.backoffice.api.rs.v1.backoffice;

import java.util.Date;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.openspcoop2.generic_project.exception.ServiceException;

import it.govpay.core.rs.v1.costanti.Costanti;
import it.govpay.rs.v1.BaseRsServiceV1;
import it.govpay.rs.v1.controllers.base.PagamentiController;


@Path("/pagamenti")

public class Pagamenti extends BaseRsServiceV1{


	private PagamentiController controller = null;

	public Pagamenti() throws ServiceException {
		super("pagamenti");
		this.controller = new PagamentiController(this.nomeServizio,this.log);
	}



    @GET
    @Path("/{id}")
    
    @Produces({ "application/json" })
    public Response pagamentiIdGET(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, @PathParam("id") String id){
        this.controller.setRequestResponse(this.request, this.response);
        return this.controller.pagamentiIdGET(this.getUser(), uriInfo, httpHeaders,  id);
    }

    @GET
    @Path("/")
    
    @Produces({ "application/json" })
    public Response pagamentiGET(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, @QueryParam(value=Costanti.PARAMETRO_PAGINA) @DefaultValue(value="1") Integer pagina, @QueryParam(value=Costanti.PARAMETRO_RISULTATI_PER_PAGINA) @DefaultValue(value="25") Integer risultatiPerPagina, @QueryParam("ordinamento") String ordinamento, @QueryParam("campi") String campi, @QueryParam("stato") String stato, @QueryParam("versante") String versante, @QueryParam("idSessionePortale") String idSessionePortale, @QueryParam("ack") Boolean ack, @QueryParam("dataDa") Date dataDa, @QueryParam("dataA") Date dataA){
        this.controller.setRequestResponse(this.request, this.response);
        return this.controller.pagamentiGET(this.getUser(), uriInfo, httpHeaders, pagina, risultatiPerPagina, ordinamento, campi, stato, versante, idSessionePortale, ack, dataDa, dataA);
    }

}


