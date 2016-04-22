/*
 * GovPay - Porta di Accesso al Nodo dei Pagamenti SPC 
 * http://www.gov4j.it/govpay
 * 
 * Copyright (c) 2014-2016 Link.it srl (http://www.link.it).
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
package it.govpay.bd.pagamento;

import it.govpay.bd.BasicBD;
import it.govpay.bd.model.SingoloVersamento;
import it.govpay.bd.model.Versamento;
import it.govpay.bd.model.SingoloVersamento.StatoSingoloVersamento;
import it.govpay.bd.model.Versamento.StatoVersamento;
import it.govpay.bd.model.converter.SingoloVersamentoConverter;
import it.govpay.bd.model.converter.VersamentoConverter;
import it.govpay.bd.pagamento.filters.VersamentoFilter;
import it.govpay.orm.IdApplicazione;
import it.govpay.orm.IdSingoloVersamento;
import it.govpay.orm.IdVersamento;
import it.govpay.orm.dao.IDBSingoloVersamentoServiceSearch;
import it.govpay.orm.dao.jdbc.converter.SingoloVersamentoFieldConverter;

import java.util.ArrayList;
import java.util.List;

import org.openspcoop2.generic_project.beans.CustomField;
import org.openspcoop2.generic_project.beans.UpdateField;
import org.openspcoop2.generic_project.exception.ExpressionException;
import org.openspcoop2.generic_project.exception.ExpressionNotImplementedException;
import org.openspcoop2.generic_project.exception.MultipleResultException;
import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.exception.NotImplementedException;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.generic_project.expression.IPaginatedExpression;

public class VersamentiBD extends BasicBD {

	public VersamentiBD(BasicBD basicBD) {
		super(basicBD);
	}

	/**
	 * Recupera il versamento identificato dalla chiave fisica
	 */
	public Versamento getVersamento(long id) throws ServiceException {
		try {
			IdVersamento idVersamento = new IdVersamento();
			idVersamento.setId(id);
			it.govpay.orm.Versamento versamento = this.getVersamentoService().get(idVersamento);
			return VersamentoConverter.toDTO(versamento);
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		} catch (NotFoundException e) {
			throw new ServiceException(e);
		} catch (MultipleResultException e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Recupera il versamento identificato dalla chiave logica
	 */
	public Versamento getVersamento(long idApplicazione, String codVersamentoEnte) throws NotFoundException, ServiceException {
		try {
			IdVersamento id = new IdVersamento();
			IdApplicazione idApplicazioneOrm = new IdApplicazione();
			idApplicazioneOrm.setId(idApplicazione);
			id.setIdApplicazione(idApplicazioneOrm);
			id.setCodVersamentoEnte(codVersamentoEnte);
			it.govpay.orm.Versamento versamento = this.getVersamentoService().get(id);
			return VersamentoConverter.toDTO(versamento);
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		} catch (MultipleResultException e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Crea un nuovo versamento.
	 */
	public void insertVersamento(Versamento versamento) throws ServiceException{
		try {
			if(isAutoCommit())
				throw new ServiceException("L'operazione insertVersamento deve essere completata in transazione singola");
			
			it.govpay.orm.Versamento vo = VersamentoConverter.toVO(versamento);
			this.getVersamentoService().create(vo);
			versamento.setId(vo.getId());
			
			for(SingoloVersamento singoloVersamento: versamento.getSingoliVersamenti(this)) {
				singoloVersamento.setIdVersamento(vo.getId());
				it.govpay.orm.SingoloVersamento singoloVersamentoVo = SingoloVersamentoConverter.toVO(singoloVersamento);
				this.getSingoloVersamentoService().create(singoloVersamentoVo);
				singoloVersamento.setId(singoloVersamentoVo.getId());
			}
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		}
	}
	
	
	

	/**
	 * Aggiorna il versamento
	 */
	
	public void updateVersamento(Versamento versamento) throws ServiceException, NotFoundException {
		try {
			it.govpay.orm.Versamento vo = VersamentoConverter.toVO(versamento);
			IdVersamento idVersamento = this.getVersamentoService().convertToId(vo);
			this.getVersamentoService().update(idVersamento, vo);
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		}
	}
	
	public void updateVersamento(Versamento versamento, boolean deep) throws NotFoundException, ServiceException {
		try {
			if(deep && isAutoCommit())
				throw new ServiceException("L'operazione updateVersamento deve essere completata in transazione singola");
			
			updateVersamento(versamento);

			if(deep) {
				for(SingoloVersamento singolo: versamento.getSingoliVersamenti(this)) {
					it.govpay.orm.SingoloVersamento singoloVO = SingoloVersamentoConverter.toVO(singolo);
					IdSingoloVersamento idSingoloVersamento = new IdSingoloVersamento();
					idSingoloVersamento.setId(singolo.getId());
					this.getSingoloVersamentoService().update(idSingoloVersamento, singoloVO);
				}
			}
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		} 
	}
	
	public void updateStatoVersamento(long idVersamento, StatoVersamento statoVersamento, String descrizioneStato) throws ServiceException {
		try {
			IdVersamento idVO = new IdVersamento();
			idVO.setId(idVersamento);
	
			List<UpdateField> lstUpdateFields = new ArrayList<UpdateField>();
			lstUpdateFields.add(new UpdateField(it.govpay.orm.Versamento.model().STATO_VERSAMENTO, statoVersamento.toString()));
			lstUpdateFields.add(new UpdateField(it.govpay.orm.Versamento.model().DESCRIZIONE_STATO, descrizioneStato));
	
			this.getVersamentoService().updateFields(idVO, lstUpdateFields.toArray(new UpdateField[]{}));
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		} catch (NotFoundException e) {
			throw new ServiceException(e);
		}
	}

	public VersamentoFilter newFilter() throws ServiceException {
		return new VersamentoFilter(this.getVersamentoService());
	}

	public long count(VersamentoFilter filter) throws ServiceException {
		try {
			return this.getVersamentoService().count(filter.toExpression()).longValue();
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		}
	}

	public List<Versamento> findAll(VersamentoFilter filter) throws ServiceException {
		try {
			List<Versamento> versamentoLst = new ArrayList<Versamento>();
			List<it.govpay.orm.Versamento> versamentoVOLst = this.getVersamentoService().findAll(filter.toPaginatedExpression()); 
			for(it.govpay.orm.Versamento versamentoVO: versamentoVOLst) {
				versamentoLst.add(VersamentoConverter.toDTO(versamentoVO));
			}
			return versamentoLst;
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		}
	}
	
	public SingoloVersamento getSingoloVersamento(long idSingoloVersamento) throws ServiceException {
		try {
			it.govpay.orm.SingoloVersamento singoloVersamentoVO = ((IDBSingoloVersamentoServiceSearch)this.getSingoloVersamentoService()).get(idSingoloVersamento);
			return SingoloVersamentoConverter.toDTO(singoloVersamentoVO);
		} catch (NotFoundException e) {
			throw new ServiceException(e);
		} catch (MultipleResultException e) {
			throw new ServiceException(e);
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		}
	}

	public List<SingoloVersamento> getSingoliVersamenti(long idVersamento) throws ServiceException {
		try {
			IPaginatedExpression exp = this.getSingoloVersamentoService().newPaginatedExpression();
			SingoloVersamentoFieldConverter fieldConverter = new SingoloVersamentoFieldConverter(this.getJdbcProperties().getDatabaseType());
			exp.equals(new CustomField("id_versamento", Long.class, "id_versamento", fieldConverter.toTable(it.govpay.orm.SingoloVersamento.model())), idVersamento);
			List<it.govpay.orm.SingoloVersamento> singoliVersamenti =  this.getSingoloVersamentoService().findAll(exp);
			return SingoloVersamentoConverter.toDTO(singoliVersamenti);
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		} catch (ExpressionNotImplementedException e) {
			throw new ServiceException(e);
		} catch (ExpressionException e) {
			throw new ServiceException(e);
		}
	}

	public it.govpay.bd.model.SingoloVersamento getSingoloVersamento(long id, String codSingoloVersamentoEnte) throws ServiceException, NotFoundException {
		try {
			IdSingoloVersamento idSingoloVersamento = new IdSingoloVersamento();
			idSingoloVersamento.setCodSingoloVersamentoEnte(codSingoloVersamentoEnte);
			IdVersamento idVersamento = new IdVersamento();
			idVersamento.setId(id);
			idSingoloVersamento.setIdVersamento(idVersamento);
			it.govpay.orm.SingoloVersamento singoloVersamentoVO = ((IDBSingoloVersamentoServiceSearch)this.getSingoloVersamentoService()).get(idSingoloVersamento);
			return SingoloVersamentoConverter.toDTO(singoloVersamentoVO);
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		} catch (MultipleResultException e) {
			throw new ServiceException(e);
		}
	}

	public void updateStatoSingoloVersamento(long idVersamento, StatoSingoloVersamento statoSingoloVersamento) throws ServiceException {
		try {
			IdSingoloVersamento idVO = new IdSingoloVersamento();
			idVO.setId(idVersamento);
			
			List<UpdateField> lstUpdateFields = new ArrayList<UpdateField>();
			lstUpdateFields.add(new UpdateField(it.govpay.orm.SingoloVersamento.model().STATO_SINGOLO_VERSAMENTO, statoSingoloVersamento.toString()));
	
			this.getSingoloVersamentoService().updateFields(idVO, lstUpdateFields.toArray(new UpdateField[]{}));
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		} catch (NotFoundException e) {
			throw new ServiceException(e);
		}
	}
}
