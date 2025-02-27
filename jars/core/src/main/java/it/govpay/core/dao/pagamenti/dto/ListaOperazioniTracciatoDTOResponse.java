package it.govpay.core.dao.pagamenti.dto;

import java.util.List;

import it.govpay.bd.model.Operazione;
import it.govpay.core.dao.anagrafica.dto.BasicFindResponseDTO;

public class ListaOperazioniTracciatoDTOResponse extends BasicFindResponseDTO<Operazione> {

	public ListaOperazioniTracciatoDTOResponse(Long totalResults, List<Operazione> results) {
		super(totalResults, results);
	}

}
