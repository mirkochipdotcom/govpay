package it.govpay.ragioneria.v1.beans.converter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.springframework.security.core.Authentication;

import it.govpay.bd.model.Pagamento;
import it.govpay.core.autorizzazione.beans.GovpayLdapUserDetails;
import it.govpay.core.autorizzazione.utils.AutorizzazioneUtils;
import it.govpay.core.dao.pagamenti.dto.RichiestaIncassoDTO;
import it.govpay.ragioneria.v1.beans.Incasso;
import it.govpay.ragioneria.v1.beans.IncassoPost;
import it.govpay.ragioneria.v1.beans.IncassoIndex;
import it.govpay.ragioneria.v1.beans.Riscossione;

public class IncassiConverter {

	
	public static RichiestaIncassoDTO toRichiestaIncassoDTO(IncassoPost incassoPost, String idDominio, Authentication user) {
		RichiestaIncassoDTO dto = new RichiestaIncassoDTO(user);
		dto.setCausale(incassoPost.getCausale());
		dto.setDataValuta(incassoPost.getDataValuta());
		dto.setDataContabile(incassoPost.getDataContabile());
		dto.setImporto(BigDecimal.valueOf(incassoPost.getImporto().doubleValue()));
		dto.setCodDominio(idDominio);
		GovpayLdapUserDetails authenticationDetails = AutorizzazioneUtils.getAuthenticationDetails(user);
		dto.setApplicazione(authenticationDetails.getApplicazione());
		dto.setOperatore(authenticationDetails.getOperatore());
		return dto;
	}
	
	
	public static Incasso toRsModel(it.govpay.bd.model.Incasso i) throws ServiceException, NotFoundException {
		Incasso rsModel = new Incasso();
		
		rsModel.setCausale(i.getCausale());
		rsModel.setDataContabile(i.getDataContabile());
		rsModel.setDataValuta(i.getDataValuta());
		rsModel.setImporto(i.getImporto().doubleValue());
		rsModel.setIdIncasso(i.getTrn());
		rsModel.setIdDominio(i.getCodDominio());
		
		rsModel.setIbanAccredito(i.getIbanAccredito());
		if(i.getPagamenti(null)!= null) {
			List<Riscossione> riscossioni = new ArrayList<>();
			for (Pagamento pagamento : i.getPagamenti(null)) {
				riscossioni.add(RiscossioniConverter.toRsModel(pagamento));
			}
			
			rsModel.setRiscossioni(riscossioni);
		}
		
		return rsModel;
	}
	
	public static IncassoIndex toRsIndexModel(it.govpay.bd.model.Incasso i) throws ServiceException {
		IncassoIndex rsModel = new IncassoIndex();
		
		rsModel.setCausale(i.getCausale());
		rsModel.setDataContabile(i.getDataContabile());
		rsModel.setDataValuta(i.getDataValuta());
		rsModel.setImporto(i.getImporto().doubleValue());
		rsModel.setIdIncasso(i.getTrn());
		rsModel.setIdDominio(i.getCodDominio());
		
		rsModel.setIbanAccredito(i.getIbanAccredito());
		
		return rsModel;
	}
}
