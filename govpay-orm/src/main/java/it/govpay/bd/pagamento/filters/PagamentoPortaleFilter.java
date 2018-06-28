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
package it.govpay.bd.pagamento.filters;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.openspcoop2.generic_project.dao.IExpressionConstructor;
import org.openspcoop2.generic_project.exception.ExpressionException;
import org.openspcoop2.generic_project.exception.ExpressionNotImplementedException;
import org.openspcoop2.generic_project.exception.NotImplementedException;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.generic_project.expression.IExpression;
import org.openspcoop2.generic_project.expression.SortOrder;

import it.govpay.bd.AbstractFilter;
import it.govpay.bd.FilterSortWrapper;
import it.govpay.bd.model.PagamentoPortale.STATO;

public class PagamentoPortaleFilter extends AbstractFilter {

	private Date dataInizio;
	private Date dataFine;
	private STATO stato;
	private String versante;
	private List<String> codDomini;
	private Boolean ack;
	
	public enum SortFields {
		DATA
	}

	public PagamentoPortaleFilter(IExpressionConstructor expressionConstructor) {
		this(expressionConstructor,false);
	}

	public PagamentoPortaleFilter(IExpressionConstructor expressionConstructor, boolean simpleSearch) {
		super(expressionConstructor, simpleSearch);
		//TODO simplesearch
	}

	@Override
	public IExpression _toExpression() throws ServiceException {
		try {
			IExpression newExpression = this.newExpression();
			boolean addAnd = false;
			
			if(dataInizio != null) {
				newExpression.greaterEquals(it.govpay.orm.PagamentoPortale.model().DATA_RICHIESTA, dataInizio);
				addAnd = true;
			}
			if(dataFine != null) {
				if(addAnd)
					newExpression.and();
				
				newExpression.lessEquals(it.govpay.orm.PagamentoPortale.model().DATA_RICHIESTA, dataFine);
				addAnd = true;
			}
			if(stato != null) {
				if(addAnd)
					newExpression.and();
				
				newExpression.equals(it.govpay.orm.PagamentoPortale.model().STATO, stato.toString());
				addAnd = true;
			}
			if(versante!= null) {
				if(addAnd)
					newExpression.and();
				newExpression.equals(it.govpay.orm.PagamentoPortale.model().VERSANTE_IDENTIFICATIVO, versante);
				addAnd = true;
			}
			if(codDomini != null && codDomini.size() > 0) {
				if(addAnd)
					newExpression.and();
				codDomini.removeAll(Collections.singleton(null));
				
				newExpression.in(it.govpay.orm.PagamentoPortale.model().MULTI_BENEFICIARIO, this.codDomini);
				newExpression.isNotNull(it.govpay.orm.PagamentoPortale.model().MULTI_BENEFICIARIO);
			
				addAnd = true;
			}
			
			if(this.ack!=null) {
				newExpression.equals(it.govpay.orm.PagamentoPortale.model().ACK, this.ack);
			}
			
			return newExpression;
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		} catch (ExpressionNotImplementedException e) {
			throw new ServiceException(e);
		} catch (ExpressionException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public IExpression _toSimpleSearchExpression() throws ServiceException {
//		try {
			IExpression newExpression = super._toSimpleSearchExpression();

			return newExpression;
//		} catch (ExpressionNotImplementedException e) {
//			throw new ServiceException(e);
//		} catch (ExpressionException e) {
//			throw new ServiceException(e);
//		} catch (NotImplementedException e) {
//			throw new ServiceException(e);
//		}
	}

	public void addSortField(SortFields field, boolean asc) {
		FilterSortWrapper filterSortWrapper = new FilterSortWrapper();
		if(field.equals(SortFields.DATA)) 
			filterSortWrapper.setField(it.govpay.orm.PagamentoPortale.model().DATA_RICHIESTA); 
		filterSortWrapper.setSortOrder((asc ? SortOrder.ASC : SortOrder.DESC));
		this.filterSortList.add(filterSortWrapper);
	}


	public Date getDataInizio() {
		return dataInizio;
	}

	public void setDataInizio(Date dataInizio) {
		this.dataInizio = dataInizio;
	}

	public Date getDataFine() {
		return dataFine;
	}

	public void setDataFine(Date dataFine) {
		this.dataFine = dataFine;
	}

	public String getVersante() {
		return versante;
	}

	public void setVersante(String versante) {
		this.versante = versante;
	}

	public STATO getStato() {
		return stato;
	}

	public void setStato(STATO stato) {
		this.stato = stato;
	}

	public List<String> getCodDomini() {
		return codDomini;
	}

	public void setCodDomini(List<String> codDomini) {
		this.codDomini = codDomini;
	}

	public Boolean getAck() {
		return ack;
	}

	public void setAck(Boolean ack) {
		this.ack = ack;
	}

}
