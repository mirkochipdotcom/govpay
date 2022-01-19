package it.govpay.ragioneria.v3.api;

import javax.validation.constraints.Max;
import javax.validation.constraints.Pattern;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.govpay.ragioneria.v3.beans.FaultBean;
import it.govpay.ragioneria.v3.beans.FlussiRendicontazione;

/**
 * GovPay - API Ragioneria
 *
 * <p>No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 */
@Path("/")
public interface RendicontazioniApi  {

    /**
     * Elenco dei flussi di rendicontazione acquisite da pagoPa
     *
     */
    @GET
    @Path("/flussiRendicontazione")
    @Produces({ "application/json" })
    @Operation(summary = "Elenco dei flussi di rendicontazione acquisite da pagoPa", tags={ "Rendicontazioni" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Lista dei flussi rendicontazione", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FlussiRendicontazione.class))),
        @ApiResponse(responseCode = "400", description = "Richiesta non correttamente formata", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FaultBean.class))),
        @ApiResponse(responseCode = "401", description = "Richiesta non autenticata"),
        @ApiResponse(responseCode = "403", description = "Richiesta non autorizzata"),
        @ApiResponse(responseCode = "500", description = "Servizio non disponibile", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FaultBean.class))) })
    public Response findFlussiRendicontazione(@QueryParam("pagina") @DefaultValue("1") Integer pagina, @QueryParam("risultatiPerPagina") @Max(200) @DefaultValue("25") Integer risultatiPerPagina, @QueryParam("ordinamento") String ordinamento, @QueryParam("idDominio") @Pattern(regexp="(^([0-9]){11}$)") String idDominio, @QueryParam("dataDa") String dataDa, @QueryParam("dataA") String dataA, @QueryParam("stato") String stato, @QueryParam("metadatiPaginazione") @DefaultValue("true") Boolean metadatiPaginazione, @QueryParam("maxRisultati") @DefaultValue("true") Boolean maxRisultati, @QueryParam("iuv") @Pattern(regexp="(^([0-9A-Za-z]){1,35}$)") String iuv, @QueryParam("idFlusso") String idFlusso);

    /**
     * Acquisizione di un flusso di rendicontazione
     *
     */
    @GET
    @Path("/flussiRendicontazione/{idDominio}/{idFlusso}")
    @Produces({ "application/xml", "application/json" })
    @Operation(summary = "Acquisizione di un flusso di rendicontazione", tags={ "Rendicontazioni" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Dettaglio della rendicontazione", content = @Content(mediaType = "application/xml", schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "401", description = "Richiesta non autenticata"),
        @ApiResponse(responseCode = "403", description = "Richiesta non autorizzata"),
        @ApiResponse(responseCode = "404", description = "Risorsa inesistente"),
        @ApiResponse(responseCode = "500", description = "Servizio non disponibile", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FaultBean.class))) })
    public Response getFlussoRendicontazione(@PathParam("idDominio") String idDominio, @PathParam("idFlusso") String idFlusso);

    /**
     * Acquisizione di un flusso di rendicontazione
     *
     */
    @GET
    @Path("/flussiRendicontazione/{idDominio}/{idFlusso}/{dataOraFlusso}")
    @Produces({ "application/xml", "application/json" })
    @Operation(summary = "Acquisizione di un flusso di rendicontazione", tags={ "Rendicontazioni" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "201", description = "Dettaglio della rendicontazione", content = @Content(mediaType = "application/xml", schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "401", description = "Richiesta non autenticata"),
        @ApiResponse(responseCode = "403", description = "Richiesta non autorizzata"),
        @ApiResponse(responseCode = "404", description = "Risorsa inesistente"),
        @ApiResponse(responseCode = "500", description = "Servizio non disponibile", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FaultBean.class))) })
    public Response getFlussoRendicontazione(@PathParam("idDominio") String idDominio, @PathParam("idFlusso") String idFlusso, @PathParam("dataOraFlusso") String dataOraFlusso);
}
