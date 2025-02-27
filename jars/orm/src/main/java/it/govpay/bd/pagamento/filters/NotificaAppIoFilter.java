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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.openspcoop2.generic_project.beans.CustomField;
import org.openspcoop2.generic_project.dao.IExpressionConstructor;
import org.openspcoop2.generic_project.exception.ExpressionException;
import org.openspcoop2.generic_project.exception.ExpressionNotImplementedException;
import org.openspcoop2.generic_project.exception.NotImplementedException;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.generic_project.expression.IExpression;
import org.openspcoop2.generic_project.expression.SortOrder;
import org.openspcoop2.utils.sql.ISQLQueryObject;
import org.openspcoop2.utils.sql.SQLQueryObjectException;

import it.govpay.bd.AbstractFilter;
import it.govpay.bd.ConnectionManager;
import it.govpay.bd.FilterSortWrapper;
import it.govpay.orm.NotificaAppIO;
import it.govpay.orm.dao.jdbc.converter.NotificaAppIOFieldConverter;
import it.govpay.orm.model.NotificaAppIOModel;

public class NotificaAppIoFilter extends AbstractFilter {

	private List<Long> idNotificaAppIo;
	private Date dataInizio;
	private Date dataFine;
	private String stato;
	private String tipo;
	private Date dataProssimaSpedizioneInizio;
	private Date dataProssimaSpedizioneFine;
	
	public enum SortFields {
		DATA_ASC, DATA_DESC
	}

	public NotificaAppIoFilter(IExpressionConstructor expressionConstructor) {
		this(expressionConstructor,false);
	}
	
	public NotificaAppIoFilter(IExpressionConstructor expressionConstructor, boolean simpleSearch) {
		super(expressionConstructor, simpleSearch);
	}

	@Override
	public IExpression _toExpression() throws ServiceException {
		try {
			IExpression newExpression = this.newExpression();
			boolean addAnd = false;
			
			NotificaAppIOFieldConverter converter = new NotificaAppIOFieldConverter(ConnectionManager.getJDBCServiceManagerProperties().getDatabase()); 

			if(this.dataInizio != null) {
				newExpression.greaterEquals(NotificaAppIO.model().DATA_CREAZIONE, this.dataInizio);
				addAnd = true;
			} 
			
			if(this.dataFine != null) {
				if(addAnd)
					newExpression.and();

				newExpression.lessEquals(NotificaAppIO.model().DATA_CREAZIONE, this.dataFine);
				addAnd = true;
			}

			if(this.stato != null) {
				if(addAnd)
					newExpression.and();
				newExpression.equals(NotificaAppIO.model().STATO, this.stato);
				addAnd = true;
			}
			
			if(this.tipo != null) {
				if(addAnd)
					newExpression.and();
				
				newExpression.equals(NotificaAppIO.model().TIPO_ESITO, this.tipo);
				
				addAnd = true;
			}

			if(this.idNotificaAppIo != null && !this.idNotificaAppIo.isEmpty()){
				this.idNotificaAppIo.removeAll(Collections.singleton(null));
				if(addAnd)
					newExpression.and();
				CustomField cf = new CustomField("id", Long.class, "id", converter.toTable(NotificaAppIO.model()));
				newExpression.in(cf, this.idNotificaAppIo);
				addAnd = true;
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

	public void addSortField(SortFields field) {
		FilterSortWrapper filterSortWrapper = new FilterSortWrapper();

		switch (field) {

		case DATA_ASC:
			filterSortWrapper.setField(NotificaAppIO.model().DATA_CREAZIONE); 
			filterSortWrapper.setSortOrder(SortOrder.ASC);
			break;

		case DATA_DESC:
			filterSortWrapper.setField(NotificaAppIO.model().DATA_CREAZIONE); 
			filterSortWrapper.setSortOrder(SortOrder.DESC);
			break;
		}

		this.filterSortList.add(filterSortWrapper);
	}
	
	@Override
	public ISQLQueryObject toWhereCondition(ISQLQueryObject sqlQueryObject) throws ServiceException {
		try {
			NotificaAppIOFieldConverter converter = new NotificaAppIOFieldConverter(ConnectionManager.getJDBCServiceManagerProperties().getDatabase()); 
			NotificaAppIOModel model = it.govpay.orm.NotificaAppIO.model();
			
			
			if(this.dataInizio != null && this.dataFine != null) {
				sqlQueryObject.addWhereCondition(true,converter.toColumn(model.DATA_CREAZIONE, true) + " >= ? ");
				sqlQueryObject.addWhereCondition(true,converter.toColumn(model.DATA_CREAZIONE, true) + " <= ? ");
			} else {
				if(this.dataInizio != null) {
					sqlQueryObject.addWhereCondition(true,converter.toColumn(model.DATA_CREAZIONE, true) + " >= ? ");
				} 
				
				if(this.dataFine != null) {
					sqlQueryObject.addWhereCondition(true,converter.toColumn(model.DATA_CREAZIONE, true) + " <= ? ");
				}
			}
			
			if(this.dataProssimaSpedizioneInizio != null && this.dataProssimaSpedizioneFine != null) {
				sqlQueryObject.addWhereCondition(true,converter.toColumn(model.DATA_PROSSIMA_SPEDIZIONE, true) + " >= ? ");
				sqlQueryObject.addWhereCondition(true,converter.toColumn(model.DATA_PROSSIMA_SPEDIZIONE, true) + " <= ? ");
			} else {
				if(this.dataProssimaSpedizioneInizio != null) {
					sqlQueryObject.addWhereCondition(true,converter.toColumn(model.DATA_PROSSIMA_SPEDIZIONE, true) + " >= ? ");
				} 
				
				if(this.dataProssimaSpedizioneFine != null) {
					sqlQueryObject.addWhereCondition(true,converter.toColumn(model.DATA_PROSSIMA_SPEDIZIONE, true) + " <= ? ");
				}
			}
			
			if(this.stato != null) {
				sqlQueryObject.addWhereCondition(true,converter.toColumn(model.STATO, true) + " = ? ");
			}
			
			if(this.tipo != null) {
				sqlQueryObject.addWhereCondition(true,converter.toColumn(model.TIPO_ESITO, true) + " = ? ");
			}

			if(this.idNotificaAppIo != null && !this.idNotificaAppIo.isEmpty()){
				this.idNotificaAppIo.removeAll(Collections.singleton(null));
				
				String [] idsNotifiche = this.idNotificaAppIo.stream().map(e -> e.toString()).collect(Collectors.toList()).toArray(new String[this.idNotificaAppIo.size()]);
				sqlQueryObject.addWhereINCondition(converter.toTable(model) + ".id", false, idsNotifiche );	
			}

			return sqlQueryObject;
		} catch (ExpressionException e) {
			throw new ServiceException(e);
		} catch (SQLQueryObjectException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public Object[] getParameters(ISQLQueryObject sqlQueryObject) throws ServiceException {
		List<Object> lst = new ArrayList<Object>();
		
		if(this.dataInizio != null && this.dataFine != null) {
			lst.add(this.dataInizio);
			lst.add(this.dataFine);
		} else {
			if(this.dataInizio != null) {
				lst.add(this.dataInizio);
			} 
			
			if(this.dataFine != null) {
				lst.add(this.dataFine);
			}
		}
		
		if(this.dataProssimaSpedizioneInizio != null && this.dataProssimaSpedizioneFine != null) {
			lst.add(this.dataProssimaSpedizioneInizio);
			lst.add(this.dataProssimaSpedizioneFine);
		} else {
			if(this.dataProssimaSpedizioneInizio != null) {
				lst.add(this.dataProssimaSpedizioneInizio);
			} 
			
			if(this.dataProssimaSpedizioneFine != null) {
				lst.add(this.dataProssimaSpedizioneFine);
			}
		}

		if(this.stato != null) {
			lst.add(this.stato);
		}
		
		if(this.tipo != null) {
			lst.add(this.tipo);
		}

		if(this.idNotificaAppIo != null && !this.idNotificaAppIo.isEmpty()){
			// donothing
		}
		
		return lst.toArray(new Object[lst.size()]);
	}

	public List<Long> getIdNotificaAppIo() {
		return idNotificaAppIo;
	}

	public void setIdNotificaAppIo(List<Long> idNotificaAppIo) {
		this.idNotificaAppIo = idNotificaAppIo;
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

	public String getStato() {
		return stato;
	}

	public void setStato(String stato) {
		this.stato = stato;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
	public Date getDataProssimaSpedizioneInizio() {
		return dataProssimaSpedizioneInizio;
	}

	public void setDataProssimaSpedizioneInizio(Date dataProssimaSpedizioneInizio) {
		this.dataProssimaSpedizioneInizio = dataProssimaSpedizioneInizio;
	}

	public Date getDataProssimaSpedizioneFine() {
		return dataProssimaSpedizioneFine;
	}

	public void setDataProssimaSpedizioneFine(Date dataProssimaSpedizioneFine) {
		this.dataProssimaSpedizioneFine = dataProssimaSpedizioneFine;
	}
	
}
