/*
 * GovPay - Porta di Accesso al Nodo dei Pagamenti SPC 
 * http://www.gov4j.it/govpay
 * 
 * Copyright (c) 2014-2017 Link.it srl (http://www.link.it).
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govpay.bd.model.converter;

import java.util.ArrayList;
import java.util.List;

import org.openspcoop2.generic_project.exception.ServiceException;

import it.govpay.bd.BasicBD;
import it.govpay.bd.model.Tributo;
import it.govpay.model.Tributo.TipoContabilita;
import it.govpay.orm.IdDominio;
import it.govpay.orm.IdIbanAccredito;
import it.govpay.orm.TipoTributo;

public class TributoConverter {

	public static List<Tributo> toDTOList(List<it.govpay.orm.Tributo> lstVO, BasicBD bd) throws ServiceException {
		List<Tributo> lst = new ArrayList<>();
		if(lstVO != null && !lstVO.isEmpty()) {
			for(it.govpay.orm.Tributo vo: lstVO) {
				lst.add(toDTO(vo, bd));
			}
		}
		return lst;
	}

	public static Tributo toDTO(it.govpay.orm.Tributo vo, BasicBD bd) throws ServiceException {
		
		Tributo dto = new Tributo();
		
		dto.setId(vo.getId());
		dto.setIdDominio(vo.getIdDominio().getId());
		dto.setCodTributo(vo.getTipoTributo().getCodTributo());
		dto.setAbilitato(vo.getAbilitato());
		dto.setDescrizione(vo.getTipoTributo().getDescrizione());
		dto.setIdTipoTributo(vo.getTipoTributo().getId()); 
		if(vo.getIdIbanAccredito() != null)
			dto.setIbanAccredito(bd, vo.getIdIbanAccredito().getId());
		if(vo.getIdIbanAppoggio() != null)
			dto.setIbanAppoggio(bd, vo.getIdIbanAppoggio().getId());
		if(vo.getTipoContabilita() != null)
			dto.setTipoContabilitaCustom(TipoContabilita.toEnum(vo.getTipoContabilita()));
		dto.setCodContabilitaCustom(vo.getCodiceContabilita());
		dto.setCodTributoIuvCustom(vo.getCodTributoIuv());
		dto.setOnlineCustom(vo.getOnline());
		dto.setPagaTerziCustom(vo.getPagaTerzi());
		
		if(vo.getTipoTributo().getTipoContabilita() != null) 
			dto.setTipoContabilitaDefault(TipoContabilita.toEnum(vo.getTipoTributo().getTipoContabilita()));
		dto.setCodContabilitaDefault(vo.getTipoTributo().getCodContabilita());
		dto.setCodTributoIuvDefault(vo.getTipoTributo().getCodTributoIuv());
		dto.setOnlineDefault(vo.getTipoTributo().isOnline());
		dto.setPagaTerziDefault(vo.getTipoTributo().isPagaTerzi());
		
		return dto;
	}

	public static it.govpay.orm.Tributo toVO(it.govpay.model.Tributo dto) {
		it.govpay.orm.Tributo vo = new it.govpay.orm.Tributo();
		vo.setId(dto.getId());
		vo.setAbilitato(dto.isAbilitato());
		
		
		TipoTributo tipoTributo = new TipoTributo();
		tipoTributo.setId(dto.getIdTipoTributo());
		tipoTributo.setCodTributo(dto.getCodTributo());
		tipoTributo.setDescrizione(dto.getDescrizione());
		
		if(dto.getTipoContabilitaCustom() != null)
			vo.setTipoContabilita(dto.getTipoContabilitaCustom().getCodifica());
		vo.setCodiceContabilita(dto.getCodContabilitaCustom());
		vo.setCodTributoIuv(dto.getCodTributoIuvCustom());
		vo.setOnline(dto.getOnlineCustom());
		vo.setPagaTerzi(dto.getPagaTerziCustom());
		
		if(dto.getTipoContabilitaDefault() != null)
			tipoTributo.setTipoContabilita(dto.getTipoContabilitaDefault().getCodifica());
		tipoTributo.setOnline(dto.getOnlineDefault());
		tipoTributo.setPagaTerzi(dto.getPagaTerziDefault());
		
		tipoTributo.setCodContabilita(dto.getCodContabilitaDefault());
		tipoTributo.setCodTributoIuv(dto.getCodTributoIuvDefault());
		
		vo.setTipoTributo(tipoTributo);

		IdDominio idDominio = new IdDominio();
		idDominio.setId(dto.getIdDominio());
		vo.setIdDominio(idDominio);
		
		if(dto.getIdIbanAccredito() != null) {
			IdIbanAccredito idIbanAccredito = new IdIbanAccredito();
			idIbanAccredito.setId(dto.getIdIbanAccredito());
			vo.setIdIbanAccredito(idIbanAccredito);
		}
		
		if(dto.getIdIbanAppoggio() != null) {
			IdIbanAccredito idIbanAppoggio = new IdIbanAccredito();
			idIbanAppoggio.setId(dto.getIdIbanAppoggio());
			vo.setIdIbanAppoggio(idIbanAppoggio);
		}
		
		return vo;
	}


}
