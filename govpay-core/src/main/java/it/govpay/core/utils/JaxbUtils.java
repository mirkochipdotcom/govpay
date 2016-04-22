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
package it.govpay.core.utils;

import gov.telematici.pagamenti.ws.ppthead.IntestazioneCarrelloPPT;
import it.gov.digitpa.schemas._2011.pagamenti.CtEsitoRevoca;
import it.gov.digitpa.schemas._2011.pagamenti.CtFlussoRiversamento;
import it.gov.digitpa.schemas._2011.pagamenti.CtRicevutaTelematica;
import it.gov.digitpa.schemas._2011.pagamenti.CtRichiestaPagamentoTelematico;
import it.gov.digitpa.schemas._2011.pagamenti.CtRichiestaRevoca;
import it.gov.digitpa.schemas._2011.pagamenti.ObjectFactory;
import it.gov.digitpa.schemas._2011.psp.InformativaContoAccredito;
import it.gov.digitpa.schemas._2011.psp.InformativaControparte;
import it.gov.spcoop.avvisopagamentopa.informazioniversamentoqr.InformazioniVersamento;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

public class JaxbUtils {

	private static JAXBContext jaxbContext, jaxbContextCodes, jaxbContextIntestazioneCarrelloPPT;
	private static Schema RPT_RT_schema;

	public static void init() throws JAXBException, SAXException {
		if(jaxbContext == null || RPT_RT_schema==null) {
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			RPT_RT_schema = schemaFactory.newSchema(new StreamSource(JaxbUtils.class.getResourceAsStream("/xsd/RPT_RT_6_0_2_RR_ER_1_0_0_FR_1_0_4.xsd"))); 
			jaxbContext = JAXBContext.newInstance("it.gov.digitpa.schemas._2011.pagamenti:it.gov.digitpa.schemas._2011.ws.paa:it.gov.digitpa.schemas._2011.psp:gov.telematici.pagamenti.ws.ppthead:it.govpay.servizi.pa");
			jaxbContextCodes = JAXBContext.newInstance("it.gov.spcoop.avvisopagamentopa.informazioniversamentoqr");
			jaxbContextIntestazioneCarrelloPPT = JAXBContext.newInstance(IntestazioneCarrelloPPT.class);
		}
	}
	
	public static byte[] toByte(CtRichiestaPagamentoTelematico rpt) throws JAXBException, SAXException {
		init();
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		jaxbMarshaller.marshal(new ObjectFactory().createRPT(rpt), baos);
		return baos.toByteArray();
	}
	
	public static byte[] toByte(InformativaControparte informativa) throws JAXBException, SAXException, XMLStreamException, IOException  {
		init();
        ByteArrayOutputStream baos = null;
        try {
        	baos = new ByteArrayOutputStream();
	        JAXBElement<InformativaControparte> informativaj = new JAXBElement<InformativaControparte>(new QName("informativaControparte"), InformativaControparte.class, informativa);
	        marshal(informativaj, baos);
			return baos.toByteArray();
        } finally {
        	if(baos != null) {
    	        baos.flush();
    	        baos.close();
        	} 
        }
	}
	
	public static byte[] toByte(InformativaContoAccredito informativa) throws JAXBException, SAXException, XMLStreamException, IOException  {
		init();
        ByteArrayOutputStream baos = null;
        try {
        	baos = new ByteArrayOutputStream();
	        marshal(informativa, baos);
			return baos.toByteArray();
        } finally {
        	if(baos != null) {
    	        baos.flush();
    	        baos.close();
        	} 
        }
	}
	
	public static byte[] toByte(CtRichiestaRevoca rr) throws JAXBException, SAXException  {
		init();
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		jaxbMarshaller.marshal(new ObjectFactory().createRR(rr), baos);
		return baos.toByteArray();
	}
	
	public static byte[] toByte(CtRicevutaTelematica rt) throws JAXBException, SAXException  {
		init();
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		jaxbMarshaller.marshal(new ObjectFactory().createRT(rt), baos);
		return baos.toByteArray();
	}
	
	public static void marshal(Object jaxb, OutputStream os) throws JAXBException, SAXException {
		init();
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
		jaxbMarshaller.marshal(jaxb, os);
	}
	
	public static void marshalIntestazioneCarrelloPPT(IntestazioneCarrelloPPT jaxb, OutputStream os) throws JAXBException, SAXException {
		init();
		Marshaller jaxbMarshaller = jaxbContextIntestazioneCarrelloPPT.createMarshaller();
		jaxbMarshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
		jaxbMarshaller.marshal(jaxb, os);
	}
	
	public static void marshal(JAXBElement<?> jaxb, StringWriter sw) throws JAXBException, SAXException {
		init();
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
		jaxbMarshaller.marshal(jaxb, sw);
	}
	
	public static Object unmarshal(XMLStreamReader xsr) throws JAXBException, SAXException {
		init();
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		return jaxbUnmarshaller.unmarshal(xsr);
	}
    
	public static CtRichiestaPagamentoTelematico toRPT(byte[] rpt) throws JAXBException, SAXException {
		init();
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		jaxbUnmarshaller.setSchema(RPT_RT_schema);
	    JAXBElement<CtRichiestaPagamentoTelematico> root = jaxbUnmarshaller.unmarshal(new StreamSource(new ByteArrayInputStream(rpt)), CtRichiestaPagamentoTelematico.class);
		return root.getValue();
	}
	
	public static CtRicevutaTelematica toRT(byte[] rt) throws JAXBException, SAXException {
		init();
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		jaxbUnmarshaller.setSchema(RPT_RT_schema);
		JAXBElement<CtRicevutaTelematica> root = jaxbUnmarshaller.unmarshal(new StreamSource(new ByteArrayInputStream(rt)), CtRicevutaTelematica.class);
		return root.getValue();
	}
	
	public static CtRichiestaRevoca toRR(byte[] rr) throws JAXBException, SAXException {
		init();
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		jaxbUnmarshaller.setSchema(RPT_RT_schema);
		JAXBElement<CtRichiestaRevoca> root = jaxbUnmarshaller.unmarshal(new StreamSource(new ByteArrayInputStream(rr)), CtRichiestaRevoca.class);
		return root.getValue();
	}
	
	public static CtEsitoRevoca toER(byte[] er) throws JAXBException, SAXException {
		init();
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		jaxbUnmarshaller.setSchema(RPT_RT_schema);
		JAXBElement<CtEsitoRevoca> root = jaxbUnmarshaller.unmarshal(new StreamSource(new ByteArrayInputStream(er)), CtEsitoRevoca.class);
		return root.getValue();
	}
	
	public static CtFlussoRiversamento toFR(byte[] fr) throws JAXBException, SAXException {
		init();
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		jaxbUnmarshaller.setSchema(RPT_RT_schema);
		JAXBElement<CtFlussoRiversamento> root = jaxbUnmarshaller.unmarshal(new StreamSource(new ByteArrayInputStream(fr)), CtFlussoRiversamento.class);
		return root.getValue();
	}
	
	public static byte[] toByte(InformazioniVersamento numeroAvviso) throws JAXBException, SAXException {
		init();
		Marshaller jaxbMarshaller = jaxbContextCodes.createMarshaller();
		jaxbMarshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		jaxbMarshaller.marshal(numeroAvviso, baos);
		return baos.toByteArray();
	}

}
