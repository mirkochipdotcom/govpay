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
package it.govpay.web.rs.dars.model.input.base;

import java.util.List;

import it.govpay.web.rs.dars.model.input.FieldType;
import it.govpay.web.rs.dars.model.input.ParamField;

public class InputFile extends ParamField<byte[]> {
	
	private List<String> acceptedMimeTypes;
	private long maxByteSize;
	
	public InputFile(String id, String label, boolean required, boolean hidden, boolean editable, List<String> acceptedMimeTypes, long maxByteSize) {
		super(id, label, null, required, hidden, editable, FieldType.INPUT_FILE);
		this.acceptedMimeTypes = acceptedMimeTypes;
		this.maxByteSize = maxByteSize;
	}

	public List<String> getAcceptedMimeTypes() {
		return acceptedMimeTypes;
	}

	public long getMaxByteSize() {
		return maxByteSize;
	}
	
}
