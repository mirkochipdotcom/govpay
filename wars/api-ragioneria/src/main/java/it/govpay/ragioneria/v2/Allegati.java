package it.govpay.ragioneria.v2;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import it.govpay.ragioneria.v2.controller.AllegatiController;
import it.govpay.rs.v2.BaseRsServiceV2;

@Path("/allegati")

public class Allegati extends BaseRsServiceV2{

	public static final String DETTAGLIO_PATH_PATTERN = "/allegati/{0}";


	private AllegatiController controller = null;

	public Allegati() {
		super("allegati");
		this.controller = new AllegatiController(this.nomeServizio,this.log);
	}



    @GET
    @Path("/{id}")

    @Produces({ "*/*" })
    public Response getAllegatoPendenza(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, @PathParam("id") Long id){
    	this.buildContext();
        this.controller.setRequestResponse(this.request, this.response);
        return this.controller.getAllegatoPendenza(this.getUser(), uriInfo, httpHeaders,  id);
    }

}


