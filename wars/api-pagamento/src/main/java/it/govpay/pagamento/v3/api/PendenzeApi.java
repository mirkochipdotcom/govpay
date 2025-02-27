package it.govpay.pagamento.v3.api;

import java.io.File;

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
import it.govpay.pagamento.v3.beans.FaultBean;
import it.govpay.pagamento.v3.beans.PendenzaArchivio;
import it.govpay.pagamento.v3.beans.PosizioneDebitoria;

/**
 * GovPay - API Pagamento
 *
 * <p>No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 */
@Path("/")
public interface PendenzeApi  {

    /**
     * Elenco delle pendenze
     *
     * Fornisce la lista delle pendenze filtrata ed ordinata.
     *
     */
    @GET
    @Path("/pendenze")
    @Produces({ "application/json" })
    @Operation(summary = "Elenco delle pendenze", tags={ "Pendenze" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Elenco delle pendenze che rispettano i filtri di ricerca", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PosizioneDebitoria.class))),
        @ApiResponse(responseCode = "400", description = "Richiesta non correttamente formata", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FaultBean.class))),
        @ApiResponse(responseCode = "401", description = "Richiesta non autenticata"),
        @ApiResponse(responseCode = "403", description = "Richiesta non autorizzata"),
        @ApiResponse(responseCode = "503", description = "Servizio non disponibile", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FaultBean.class))) })
    public Response findPendenze(@QueryParam("pagina") @DefaultValue(value="1") Integer pagina, @QueryParam("risultatiPerPagina") @Max(200) @DefaultValue("25") Integer risultatiPerPagina, @QueryParam("ordinamento") String ordinamento, @QueryParam("idDominio") String idDominio, @QueryParam("dataDa") String dataDa, @QueryParam("dataA") String dataA, @QueryParam("iuv") @Pattern(regexp="(^([0-9A-Za-z]){1,35}$)") String iuv, @QueryParam("idA2A") String idA2A, @QueryParam("idPendenza") String idPendenza, @QueryParam("idDebitore") String idDebitore, @QueryParam("stato") String stato, @QueryParam("idPagamento") @Pattern(regexp="(^([0-9A-Za-z]){1,35}$)") String idPagamento, @QueryParam("direzione") @Pattern(regexp="(^([0-9A-Za-z]){1,35}$)") String direzione, @QueryParam("divisione") @Pattern(regexp="(^([0-9A-Za-z]){1,35}$)") String divisione, @QueryParam("mostraSpontaneiNonPagati") @DefaultValue("false") Boolean mostraSpontaneiNonPagati, @QueryParam("metadatiPaginazione") @DefaultValue("true") Boolean metadatiPaginazione, @QueryParam("maxRisultati") @DefaultValue("true") Boolean maxRisultati);

    /**
     * Allegato di una pendenza
     *
     * Fornisce l&#x27;allegato di una pendenza
     *
     */
    @GET
    @Path("/allegati/{id}")
    @Produces({ "*/*", "application/json" })
    @Operation(summary = "Allegato di una pendenza", tags={ "Pendenze" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Contenuto dell'allegato", content = @Content(mediaType = "*/*", schema = @Schema(implementation = File.class))),
        @ApiResponse(responseCode = "401", description = "Richiesta non autenticata"),
        @ApiResponse(responseCode = "403", description = "Richiesta non autorizzata"),
        @ApiResponse(responseCode = "404", description = "Risorsa inesistente"),
        @ApiResponse(responseCode = "500", description = "Servizio non disponibile", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FaultBean.class))) })
    public Response getAllegatoPendenza(@PathParam("id") Long id);

    /**
     * Dettaglio di una pendenza per identificativo
     *
     * Acquisisce il dettaglio di una pendenza, comprensivo dei dati di pagamento.
     *
     */
    @GET
    @Path("/pendenze/{idA2A}/{idPendenza}")
    @Produces({ "application/json" })
    @Operation(summary = "Dettaglio di una pendenza per identificativo", tags={ "Pendenze" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Informazioni dettagliate della pendenza", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PendenzaArchivio.class))),
        @ApiResponse(responseCode = "401", description = "Richiesta non autenticata"),
        @ApiResponse(responseCode = "403", description = "Richiesta non autorizzata"),
        @ApiResponse(responseCode = "404", description = "Risorsa inesistente"),
        @ApiResponse(responseCode = "503", description = "Servizio non disponibile", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FaultBean.class))) })
    public Response getPendenza(@PathParam("idA2A") String idA2A, @PathParam("idPendenza") String idPendenza);
}
