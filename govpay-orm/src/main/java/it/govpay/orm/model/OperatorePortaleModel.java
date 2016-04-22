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
package it.govpay.orm.model;

import it.govpay.orm.OperatorePortale;

import org.openspcoop2.generic_project.beans.AbstractModel;
import org.openspcoop2.generic_project.beans.IField;
import org.openspcoop2.generic_project.beans.Field;
import org.openspcoop2.generic_project.beans.ComplexField;


/**     
 * Model OperatorePortale 
 *
 * @author Giovanni Bussu (bussu@link.it)
 * @author Lorenzo Nardi (nardi@link.it)
 * @author $Author$
 * @version $Rev$, $Date$
 */
public class OperatorePortaleModel extends AbstractModel<OperatorePortale> {

	public OperatorePortaleModel(){
	
		super();
	
		this.ID_PORTALE = new it.govpay.orm.model.IdPortaleModel(new Field("idPortale",it.govpay.orm.IdPortale.class,"OperatorePortale",OperatorePortale.class));
	
	}
	
	public OperatorePortaleModel(IField father){
	
		super(father);
	
		this.ID_PORTALE = new it.govpay.orm.model.IdPortaleModel(new ComplexField(father,"idPortale",it.govpay.orm.IdPortale.class,"OperatorePortale",OperatorePortale.class));
	
	}
	
	

	public it.govpay.orm.model.IdPortaleModel ID_PORTALE = null;
	 

	@Override
	public Class<OperatorePortale> getModeledClass(){
		return OperatorePortale.class;
	}
	
	@Override
	public String toString(){
		if(this.getModeledClass()!=null){
			return this.getModeledClass().getName();
		}else{
			return "N.D.";
		}
	}

}