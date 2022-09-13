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
import it.govpay.ragioneria.v3.beans.Ricevuta;
import it.govpay.ragioneria.v3.beans.Ricevute;

/**
 * GovPay - API Ragioneria
 *
 * <p>No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 */
@Path("/")
public interface RicevuteApi  {

    /**
     * Ricerca delle ricevute di pagamento
     *
     */
    @GET
    @Path("/ricevute")
    @Produces({ "application/json" })
    @Operation(summary = "Ricerca delle ricevute di pagamento", tags={ "Ricevute" })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista dei flussi rendicontazione", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Ricevute.class))),
        @ApiResponse(responseCode = "400", description = "Richiesta non correttamente formata", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FaultBean.class))),
        @ApiResponse(responseCode = "401", description = "Richiesta non autenticata"),
        @ApiResponse(responseCode = "403", description = "Richiesta non autorizzata"),
        @ApiResponse(responseCode = "500", description = "Servizio non disponibile", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FaultBean.class))) })
    public Response findRicevute(@QueryParam("pagina") @DefaultValue("1") Integer pagina, @QueryParam("risultatiPerPagina") @Max(200) @DefaultValue("25") Integer risultatiPerPagina, @QueryParam("ordinamento") String ordinamento, @QueryParam("idDominio") @Pattern(regexp="(^([0-9]){11}$)") String idDominio, @QueryParam("dataDa") String dataDa, @QueryParam("dataA") String dataA, @QueryParam("metadatiPaginazione") @DefaultValue("true") Boolean metadatiPaginazione, @QueryParam("maxRisultati") @DefaultValue("true") Boolean maxRisultati, @QueryParam("iuv") @Pattern(regexp="(^([0-9A-Za-z]){1,35}$)") String iuv);

    /**
     * Acquisizione di una ricevuta di avvenuto pagamento pagoPA
     *
     * Ricevuta pagoPA, sia questa veicolata nella forma di &#x60;RT&#x60; o di &#x60;recepit&#x60;, di esito positivo.
     *
     */
    @GET
    @Path("/ricevute/{idDominio}/{iuv}/{idRicevuta}")
    @Produces({ "application/json" })
    @Operation(summary = "Acquisizione di una ricevuta di avvenuto pagamento pagoPA", tags={ "Ricevute" })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "ricevuta di pagamento acquisita", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Ricevuta.class))),
        @ApiResponse(responseCode = "400", description = "Richiesta non correttamente formata", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FaultBean.class))),
        @ApiResponse(responseCode = "401", description = "Richiesta non autenticata"),
        @ApiResponse(responseCode = "403", description = "Richiesta non autorizzata"),
        @ApiResponse(responseCode = "500", description = "Servizio non disponibile", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FaultBean.class))) })
    public Response getRicevuta(@PathParam("idDominio") @Pattern(regexp="(^([0-9]){11}$)") String idDominio, @PathParam("iuv") @Pattern(regexp="(^([0-9A-Za-z]){1,35}$)") String iuv, @PathParam("idRicevuta") @Pattern(regexp="(^([0-9A-Za-z]){1,35}$)") String idRicevuta);
}
