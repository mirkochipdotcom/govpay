package it.govpay.stampe.pdf.avvisoPagamento;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.openspcoop2.utils.LoggerWrapperFactory;
import org.openspcoop2.utils.UtilsException;
import org.slf4j.Logger;

import it.govpay.stampe.model.AvvisoPagamentoInput;
import it.govpay.stampe.pdf.avvisoPagamento.utils.AvvisoPagamentoProperties;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

public class AvvisoPagamentoPdf {

	private static AvvisoPagamentoPdf _instance = null;
	private static JAXBContext jaxbContext = null;

	public static AvvisoPagamentoPdf getInstance() {
		if(_instance == null)
			init();

		return _instance;
	}

	public static synchronized void init() {
		if(_instance == null)
			_instance = new AvvisoPagamentoPdf();
		
		if(jaxbContext == null) {
			try {
				jaxbContext = JAXBContext.newInstance(AvvisoPagamentoInput.class);
			} catch (JAXBException e) {
				LoggerWrapperFactory.getLogger(AvvisoPagamentoPdf.class).error("Errore durtante l'inizializzazione JAXB", e); 
			}
		}
	}

	public AvvisoPagamentoPdf() {
	}


	public JasperPrint creaJasperPrintAvviso(Logger log, AvvisoPagamentoInput input, Properties propertiesAvvisoPerDominio, InputStream jasperTemplateInputStream,JRDataSource dataSource,Map<String, Object> parameters) throws Exception {
		JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperTemplateInputStream);
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
		return jasperPrint;
	}

	public byte[] creaAvviso(Logger log, AvvisoPagamentoInput input, String codDominio, AvvisoPagamentoProperties avProperties) throws Exception {
		// cerco file di properties esterni per configurazioni specifiche per dominio
		Properties propertiesAvvisoPerDominio = avProperties.getPropertiesPerDominio(codDominio, log);

		this.caricaLoghiAvviso(input, propertiesAvvisoPerDominio);

		// leggo il template file jasper da inizializzare
		String jasperTemplateFilename = propertiesAvvisoPerDominio.getProperty(AvvisoPagamentoCostanti.AVVISO_PAGAMENTO_TEMPLATE_JASPER);
		if(!jasperTemplateFilename.startsWith("/"))
			jasperTemplateFilename = "/" + jasperTemplateFilename; 
		
		Map<String, Object> parameters = new HashMap<>();
		JRDataSource dataSource = this.creaXmlDataSource(log,input);
		JasperPrint jasperPrint = this.creaJasperPrintAvviso(log, input, propertiesAvvisoPerDominio, AvvisoPagamentoPdf.class.getResourceAsStream(jasperTemplateFilename), dataSource, parameters);

		byte[] reportToPdf = JasperExportManager.exportReportToPdf(jasperPrint);
		FileOutputStream fos = new  FileOutputStream("/tmp/mio_avviso.pdf");
		fos.write(reportToPdf);
		fos.close();
		return reportToPdf;
	}

	public JRDataSource creaXmlDataSource(Logger log,AvvisoPagamentoInput input) throws UtilsException, JRException, JAXBException {
//		WriteToSerializerType serType = WriteToSerializerType.XML_JAXB;
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JAXBElement<AvvisoPagamentoInput> jaxbElement = new JAXBElement<AvvisoPagamentoInput>(new QName("", AvvisoPagamentoCostanti.AVVISO_PAGAMENTO_ROOT_ELEMENT_NAME), AvvisoPagamentoInput.class, null, input);
		jaxbMarshaller.marshal(jaxbElement, baos);
		JRDataSource dataSource = new JRXmlDataSource(new ByteArrayInputStream(baos.toByteArray()),AvvisoPagamentoCostanti.AVVISO_PAGAMENTO_ROOT_ELEMENT_NAME);
		return dataSource;
	}

	public void caricaLoghiAvviso(AvvisoPagamentoInput input, Properties propertiesAvvisoPerDominio) {
		// valorizzo la sezione loghi
		if(input.getLogoEnte() == null)
			input.setLogoEnte(propertiesAvvisoPerDominio.getProperty(AvvisoPagamentoCostanti.LOGO_ENTE));
	}
	
	public static void main(String[] args) throws Exception {
		String jasperTemplateFilename = "/tmp/TriBand.jasper";
		File jasperTemplateFile = new File(jasperTemplateFilename);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("SUBREPORT_DIR", "/tmp/");
		parameters.put("report_base_path", "/tmp/");
		String input = "<AvvisoPagamentoInput>\n" + 
				"   <logo_ente>data:image/png;base64,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</logo_ente>\n" + 
				"   <oggetto_del_pagamento>IMU 2010 - n.1587997248364</oggetto_del_pagamento>\n" + 
				"   <cf_ente>12345678901</cf_ente>\n" + 
				"   <cf_destinatario>DRCGNN12A46A326K</cf_destinatario>\n" + 
				"   <nome_cognome_destinatario>Giovanna D'Arco</nome_cognome_destinatario>\n" + 
				"   <ente_creditore>Ente Creditore Test</ente_creditore>\n" + 
				"   <indirizzo_destinatario_1>Viale Monterosa 11Bis,</indirizzo_destinatario_1>\n" + 
				"   <indirizzo_destinatario_2>340 Roma (RM)</indirizzo_destinatario_2>\n" + 
				"   <info_ente>sito web: http://www.entecreditore.it&lt;br/&gt;email: info@entecreditore.it&lt;br/&gt;PEC: protocollo.generale@pec.entecreditore.it</info_ente>\n" + 
				"   <di_poste>di Poste Italiane</di_poste>\n" + 
				"   <cbill>AB123</cbill>\n" + 
				"   <intestatario_conto_corrente_postale>Ente Creditore Test</intestatario_conto_corrente_postale>\n" + 
				"   <autorizzazione>Aut. num. 0129302934764583</autorizzazione>\n" + 
				"   <pagine>\n" + 
				"      <singola>\n" + 
				"         <rata>\n" + 
				"            <importo>100.0</importo>\n" + 
				"            <data>30/12/2019</data>\n" + 
				"            <qr_code>PAGOPA|002|001340000000362331|12345678901|10000</qr_code>\n" + 
				"            <codice_avviso>0013 4000 0000 3623 31</codice_avviso>\n" + 
				"            <codice_avviso_postale>001340000000362331</codice_avviso_postale>\n" + 
				"            <numero_cc_postale>123456789012</numero_cc_postale>\n" + 
				"            <data_matrix>codfase=NBPA;180013400000003623311212345678901210000001000038961P112345678901DRCGNN12A46A326KGIOVANNA D'ARCO                         IMU 2010 - N.1587997248364                                                                                                A</data_matrix>\n" + 
				"         </rata>\n" + 
				"      </singola>\n" + 
				"      <tripla>\n" + 
				"         <rata>\n" + 
				"            <numero_rata>1</numero_rata>\n" + 
				"            <importo>10.0</importo>\n" + 
				"            <data>10/01/2030</data>\n" + 
				"            <qr_code>PAGOPA|002|001340000000362432|12345678901|1000</qr_code>\n" + 
				"            <codice_avviso>0013 4000 0000 3624 32</codice_avviso>\n" + 
				"            <codice_avviso_postale>001340000000362432</codice_avviso_postale>\n" + 
				"            <numero_cc_postale>123456789012</numero_cc_postale>\n" + 
				"            <data_matrix>codfase=NBPA;180013400000003624321212345678901210000000100038961P112345678901DRCGNN12A46A326KGIOVANNA D'ARCO                         IMU 2010 - N.1587997248364                                                                                                A</data_matrix>\n" + 
				"         </rata>\n" + 
				"         <rata>\n" + 
				"            <numero_rata>2</numero_rata>\n" + 
				"            <importo>10.0</importo>\n" + 
				"            <data>10/02/2030</data>\n" + 
				"            <qr_code>PAGOPA|002|001340000000362533|12345678901|1000</qr_code>\n" + 
				"            <codice_avviso>0013 4000 0000 3625 33</codice_avviso>\n" + 
				"            <codice_avviso_postale>001340000000362533</codice_avviso_postale>\n" + 
				"            <numero_cc_postale>123456789012</numero_cc_postale>\n" + 
				"            <data_matrix>codfase=NBPA;180013400000003625331212345678901210000000100038961P112345678901DRCGNN12A46A326KGIOVANNA D'ARCO                         IMU 2010 - N.1587997248364                                                                                                A</data_matrix>\n" + 
				"         </rata>\n" + 
				"         <rata>\n" + 
				"            <numero_rata>3</numero_rata>\n" + 
				"            <importo>10.0</importo>\n" + 
				"            <data>10/03/2030</data>\n" + 
				"            <qr_code>PAGOPA|002|001340000000362634|12345678901|1000</qr_code>\n" + 
				"            <codice_avviso>0013 4000 0000 3626 34</codice_avviso>\n" + 
				"            <codice_avviso_postale>001340000000362634</codice_avviso_postale>\n" + 
				"            <numero_cc_postale>123456789012</numero_cc_postale>\n" + 
				"            <data_matrix>codfase=NBPA;180013400000003626341212345678901210000000100038961P112345678901DRCGNN12A46A326KGIOVANNA D'ARCO                         IMU 2010 - N.1587997248364                                                                                                A</data_matrix>\n" + 
				"         </rata>\n" + 
				"      </tripla>\n" + 
				"      <tripla>\n" + 
				"         <rata>\n" + 
				"            <numero_rata>4</numero_rata>\n" + 
				"            <importo>10.0</importo>\n" + 
				"            <data>10/04/2030</data>\n" + 
				"            <qr_code>PAGOPA|002|001340000000362735|12345678901|1000</qr_code>\n" + 
				"            <codice_avviso>0013 4000 0000 3627 35</codice_avviso>\n" + 
				"            <codice_avviso_postale>001340000000362735</codice_avviso_postale>\n" + 
				"            <numero_cc_postale>123456789012</numero_cc_postale>\n" + 
				"            <data_matrix>codfase=NBPA;180013400000003627351212345678901210000000100038961P112345678901DRCGNN12A46A326KGIOVANNA D'ARCO                         IMU 2010 - N.1587997248364                                                                                                A</data_matrix>\n" + 
				"         </rata>\n" + 
				"         <rata>\n" + 
				"            <numero_rata>5</numero_rata>\n" + 
				"            <importo>10.0</importo>\n" + 
				"            <data>10/05/2030</data>\n" + 
				"            <qr_code>PAGOPA|002|001340000000362836|12345678901|1000</qr_code>\n" + 
				"            <codice_avviso>0013 4000 0000 3628 36</codice_avviso>\n" + 
				"            <codice_avviso_postale>001340000000362836</codice_avviso_postale>\n" + 
				"            <numero_cc_postale>123456789012</numero_cc_postale>\n" + 
				"            <data_matrix>codfase=NBPA;180013400000003628361212345678901210000000100038961P112345678901DRCGNN12A46A326KGIOVANNA D'ARCO                         IMU 2010 - N.1587997248364                                                                                                A</data_matrix>\n" + 
				"         </rata>\n" + 
				"         <rata>\n" + 
				"            <numero_rata>6</numero_rata>\n" + 
				"            <importo>10.0</importo>\n" + 
				"            <data>10/06/2030</data>\n" + 
				"            <qr_code>PAGOPA|002|001340000000362937|12345678901|1000</qr_code>\n" + 
				"            <codice_avviso>0013 4000 0000 3629 37</codice_avviso>\n" + 
				"            <codice_avviso_postale>001340000000362937</codice_avviso_postale>\n" + 
				"            <numero_cc_postale>123456789012</numero_cc_postale>\n" + 
				"            <data_matrix>codfase=NBPA;180013400000003629371212345678901210000000100038961P112345678901DRCGNN12A46A326KGIOVANNA D'ARCO                         IMU 2010 - N.1587997248364                                                                                                A</data_matrix>\n" + 
				"         </rata>\n" + 
				"      </tripla>\n" + 
				"      <doppia>\n" + 
				"         <rata>\n" + 
				"            <numero_rata>7</numero_rata>\n" + 
				"            <importo>10.0</importo>\n" + 
				"            <data>10/07/2030</data>\n" + 
				"            <qr_code>PAGOPA|002|001340000000363038|12345678901|1000</qr_code>\n" + 
				"            <codice_avviso>0013 4000 0000 3630 38</codice_avviso>\n" + 
				"            <codice_avviso_postale>001340000000363038</codice_avviso_postale>\n" + 
				"            <numero_cc_postale>123456789012</numero_cc_postale>\n" + 
				"            <data_matrix>codfase=NBPA;180013400000003630381212345678901210000000100038961P112345678901DRCGNN12A46A326KGIOVANNA D'ARCO                         IMU 2010 - N.1587997248364                                                                                                A</data_matrix>\n" + 
				"         </rata>\n" + 
				"         <rata>\n" + 
				"            <numero_rata>8</numero_rata>\n" + 
				"            <importo>10.0</importo>\n" + 
				"            <data>10/08/2030</data>\n" + 
				"            <qr_code>PAGOPA|002|001340000000363139|12345678901|1000</qr_code>\n" + 
				"            <codice_avviso>0013 4000 0000 3631 39</codice_avviso>\n" + 
				"            <codice_avviso_postale>001340000000363139</codice_avviso_postale>\n" + 
				"            <numero_cc_postale>123456789012</numero_cc_postale>\n" + 
				"            <data_matrix>codfase=NBPA;180013400000003631391212345678901210000000100038961P112345678901DRCGNN12A46A326KGIOVANNA D'ARCO                         IMU 2010 - N.1587997248364                                                                                                A</data_matrix>\n" + 
				"         </rata>\n" + 
				"      </doppia>\n" + 
				"   </pagine>\n" + 
				"</AvvisoPagamentoInput>\n" + 
				"";
		JRDataSource dataSource = new JRXmlDataSource(new ByteArrayInputStream(input.getBytes()),"/AvvisoPagamentoInput");
		JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperTemplateFile);
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
		byte[] reportToPdf = JasperExportManager.exportReportToPdf(jasperPrint);
		FileOutputStream fos = new  FileOutputStream("/tmp/mio_avviso.pdf");
		fos.write(reportToPdf);
		fos.close();
	}


}
