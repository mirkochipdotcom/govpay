package it.govpay.stampe.pdf.avvisoPagamento;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.openspcoop2.utils.LoggerWrapperFactory;
import org.openspcoop2.utils.UtilsException;
import org.slf4j.Logger;

import it.govpay.core.exceptions.ConfigException;
import it.govpay.core.exceptions.PropertyNotFoundException;
import it.govpay.stampe.model.AvvisoPagamentoInput;
import it.govpay.stampe.pdf.avvisoPagamento.utils.AvvisoPagamentoProperties;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.fill.JRGzipVirtualizer;
import net.sf.jasperreports.engine.util.JRLoader;

public class AvvisoPagamentoPdf {

	private static AvvisoPagamentoPdf _instance = null;
	private static JAXBContext jaxbContext = null;
	
	private static byte[] templateAvviso = null;
	private static byte[] templateDoppiaRata = null;
	private static byte[] templateDoppioFormato = null;
	private static byte[] templateRataMultipla = null;
	private static byte[] templateRataUnica = null;
	private static byte[] templateTriplaRata = null;
	private static byte[] templateTriploFormato = null;
	
	private static byte[] templateAvvisoPostale = null;
	private static byte[] templateBollettinoRataPostale = null;
	private static byte[] templateBollettinoTriRataPostale = null;
	private static byte[] templateDoppiaRataPostale = null;
	private static byte[] templateDoppioFormatoPostale = null;
	private static byte[] templateRataUnicaPostale = null;
	private static byte[] templateTriplaRataPostale = null;
	private static byte[] templateTriploFormatoPostale = null;
	
//	private static byte[] templateMonoBand = null;
//	private static byte[] templateTriBand = null;
	
	private static byte[] templateViolazioneCDS = null;
	private static byte[] templateRidottoScontato = null;
	private static byte[] templateSanzione = null;
	private static byte[] templateFormato = null;
	
	private static JAXBContext jaxbContextV2 = null;
	private static byte[] templateAvvisoV2 = null;
	private static byte[] templateMonoBandV2 = null;
	private static byte[] templateTriBandV2 = null;
	private static byte[] templateRataUnicaV2 = null;
	private static byte[] templateDoppiaRataV2 = null;
	private static byte[] templateDoppioFormatoV2 = null;
	private static byte[] templateBollettinoRataV2 = null;
	private static byte[] templateDualBandV2 = null;
	
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
		
		if(jaxbContextV2 == null) {
			try {
				jaxbContextV2 = JAXBContext.newInstance(it.govpay.stampe.model.v2.AvvisoPagamentoInput.class);
			} catch (JAXBException e) {
				LoggerWrapperFactory.getLogger(AvvisoPagamentoPdf.class).error("Errore durtante l'inizializzazione JAXB", e); 
			}
		}
	}

	public AvvisoPagamentoPdf() {
		try {
			jaxbContext = JAXBContext.newInstance(AvvisoPagamentoInput.class);
		} catch (JAXBException e) {
			LoggerWrapperFactory.getLogger(AvvisoPagamentoPdf.class).error("Errore durtante l'inizializzazione JAXB", e); 
		}
		
		try {
			jaxbContextV2 = JAXBContext.newInstance(it.govpay.stampe.model.v2.AvvisoPagamentoInput.class);
		} catch (JAXBException e) {
			LoggerWrapperFactory.getLogger(AvvisoPagamentoPdf.class).error("Errore durtante l'inizializzazione JAXB", e); 
		}
		
		try {
			templateAvviso = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.AVVISO_PAGAMENTO_TEMPLATE_JASPER));
			templateDoppiaRata = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.RATA_DOPPIA_TEMPLATE_JASPER));
			templateDoppioFormato = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.DOPPIO_FORMATO_TEMPLATE_JASPER));
			templateRataMultipla = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.RATA_MULTIPLA_TEMPLATE_JASPER));
			templateRataUnica = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.RATA_UNICA_TEMPLATE_JASPER));
			templateTriplaRata = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.RATA_TRIPLA_TEMPLATE_JASPER));
			templateTriploFormato = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.TRIPLO_FORMATO_TEMPLATE_JASPER));
			
			templateAvvisoPostale = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.AVVISO_PAGAMENTO_POSTALE_TEMPLATE_JASPER)); 
			templateBollettinoRataPostale = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.BOLLETTINO_RATA_TEMPLATE_JASPER));
			templateBollettinoTriRataPostale = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.BOLLETTINO_TRIRATA_TEMPLATE_JASPER));
			templateDoppiaRataPostale = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.RATA_DOPPIA_POSTALE_TEMPLATE_JASPER));
			templateDoppioFormatoPostale = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.DOPPIO_FORMATO_POSTALE_TEMPLATE_JASPER));
			templateRataUnicaPostale = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.RATA_UNICA_POSTALE_TEMPLATE_JASPER));
			templateTriplaRataPostale = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.RATA_TRIPLA_POSTALE_TEMPLATE_JASPER));
			templateTriploFormatoPostale = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.TRIPLO_FORMATO_POSTALE_TEMPLATE_JASPER));
			
//			templateMonoBand = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.MONOBAND_TEMPLATE_JASPER));
//			templateTriBand = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.TRIBAND_TEMPLATE_JASPER));
			
			templateViolazioneCDS = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.VIOLAZIONE_CDS_TEMPLATE_JASPER));
			templateRidottoScontato = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.RIDOTTOSCONTATO_TEMPLATE_JASPER));
			templateSanzione = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.SANZIONE_TEMPLATE_JASPER));
			templateFormato = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.FORMATO_TEMPLATE_JASPER));
		} catch (IOException e) {
			LoggerWrapperFactory.getLogger(AvvisoPagamentoPdf.class).error("Errore durante la lettura del template jasper dell'Avviso di Pagamento", e); 
		}
		
		try {
			templateAvvisoV2 = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.AVVISO_PAGAMENTO_TEMPLATE_JASPER_V2));
			templateMonoBandV2 = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.MONOBAND_TEMPLATE_JASPER_V2));
			templateTriBandV2 = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.TRIBAND_TEMPLATE_JASPER_V2));
			templateRataUnicaV2 = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.RATAUNICA_TEMPLATE_JASPER_V2));
			templateDoppiaRataV2 = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.RATADOPPIA_TEMPLATE_JASPER_V2));
			templateDoppioFormatoV2 = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.DOPPIOFORMATO_TEMPLATE_JASPER_V2));
			templateBollettinoRataV2 = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.BOLLETTINORATA_TEMPLATE_JASPER_V2));
			templateDualBandV2 = IOUtils.toByteArray(AvvisoPagamentoPdf.class.getResourceAsStream(AvvisoPagamentoCostanti.DUALBAND_TEMPLATE_JASPER_V2));
		} catch (IOException e) {
			LoggerWrapperFactory.getLogger(AvvisoPagamentoPdf.class).error("Errore durante la lettura del template jasper dell'Avviso di Pagamento", e); 
		}
		
	}


	public JasperPrint creaJasperPrintAvviso(Logger log, AvvisoPagamentoInput input, Properties propertiesAvvisoPerDominio, InputStream jasperTemplateInputStream,JRDataSource dataSource,Map<String, Object> parameters) throws Exception {
		JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperTemplateInputStream);
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
		return jasperPrint;
	}

	public byte[] creaAvviso(Logger log, AvvisoPagamentoInput input, String codDominio, AvvisoPagamentoProperties avProperties) throws JAXBException, IOException, JRException, PropertyNotFoundException {
		if(input.getScadenzaScontato() != null) {
			return _creaAvvisoViolazioneCDS(log, input, codDominio, avProperties);
		} else if(input.getDiPoste() != null) {
			return _creaAvvisoPostale(log, input, codDominio, avProperties);
		} else {
			return _creaAvviso(log, input, codDominio, avProperties);
		}
	}
	
	public byte[] _creaAvviso(Logger log, AvvisoPagamentoInput input, String codDominio, AvvisoPagamentoProperties avProperties) throws JAXBException, IOException, JRException, PropertyNotFoundException {
		// cerco file di properties esterni per configurazioni specifiche per dominio
		Properties propertiesAvvisoPerDominio = avProperties.getPropertiesPerDominio(codDominio, log);

		this.caricaLoghiAvviso(input, propertiesAvvisoPerDominio);

		Map<String, Object> parameters = new HashMap<String, Object>();
		
//		parameters.put("MonoBand", new ByteArrayInputStream(templateMonoBand));
//		parameters.put("TriBand", new ByteArrayInputStream(templateTriBand));
		parameters.put("DoppiaRata", new ByteArrayInputStream(templateDoppiaRata));
		parameters.put("DoppioFormato", new ByteArrayInputStream(templateDoppioFormato));
		parameters.put("RataMultipla", new ByteArrayInputStream(templateRataMultipla));
		parameters.put("RataUnica", new ByteArrayInputStream(templateRataUnica));
		parameters.put("TriplaRata", new ByteArrayInputStream(templateTriplaRata));
		parameters.put("TriploFormato", new ByteArrayInputStream(templateTriploFormato));
		
		JRGzipVirtualizer virtualizer = new JRGzipVirtualizer(50);
		parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
		
		try (ByteArrayInputStream templateIS = new ByteArrayInputStream(templateAvviso);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();){
			
			DefaultJasperReportsContext defaultJasperReportsContext = DefaultJasperReportsContext.getInstance();
			
			JRPropertiesUtil.getInstance(defaultJasperReportsContext).setProperty("net.sf.jasperreports.xpath.executer.factory",
                    "net.sf.jasperreports.engine.util.xml.JaxenXPathExecuterFactory");
			
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
			
			JAXBElement<AvvisoPagamentoInput> jaxbElement = new JAXBElement<AvvisoPagamentoInput>(new QName("", AvvisoPagamentoCostanti.AVVISO_PAGAMENTO_ROOT_ELEMENT_NAME), AvvisoPagamentoInput.class, null, input);
			jaxbMarshaller.marshal(jaxbElement, baos);
			byte[] byteArray = baos.toByteArray();
			log.trace("AvvisoPagamentoInput: " + new String(byteArray));
			try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);){

				JRDataSource dataSource = new JRXmlDataSource(defaultJasperReportsContext, byteArrayInputStream,AvvisoPagamentoCostanti.AVVISO_PAGAMENTO_ROOT_ELEMENT_NAME);
//			JRDataSource dataSource = this.creaXmlDataSource(log,input);
				JasperReport jasperReport = (JasperReport) JRLoader.loadObject(defaultJasperReportsContext,templateIS);
				JasperPrint jasperPrint = JasperFillManager.getInstance(defaultJasperReportsContext).fill(jasperReport, parameters, dataSource);
				
				return JasperExportManager.getInstance(defaultJasperReportsContext).exportToPdf(jasperPrint);
			}finally {
				
			}
		}finally {
			
		}
	}
	
	public byte[] _creaAvvisoPostale(Logger log, AvvisoPagamentoInput input, String codDominio, AvvisoPagamentoProperties avProperties) throws JAXBException, IOException, JRException, PropertyNotFoundException {
		// cerco file di properties esterni per configurazioni specifiche per dominio
		Properties propertiesAvvisoPerDominio = avProperties.getPropertiesPerDominio(codDominio, log);

		this.caricaLoghiAvviso(input, propertiesAvvisoPerDominio);

		Map<String, Object> parameters = new HashMap<String, Object>();
		
		parameters.put("BollettinoRataPostale", new ByteArrayInputStream(templateBollettinoRataPostale));
		parameters.put("BollettinoTriRataPostale", new ByteArrayInputStream(templateBollettinoTriRataPostale));
		parameters.put("DoppiaRataPostalePostale", new ByteArrayInputStream(templateDoppiaRataPostale));
		parameters.put("DoppioFormatoPostale", new ByteArrayInputStream(templateDoppioFormatoPostale));
		parameters.put("RataUnicaPostalePostale", new ByteArrayInputStream(templateRataUnicaPostale));
		parameters.put("TriplaRataPostalePostale", new ByteArrayInputStream(templateTriplaRataPostale));
		parameters.put("TriploFormatoPostale", new ByteArrayInputStream(templateTriploFormatoPostale));
		
		JRGzipVirtualizer virtualizer = new JRGzipVirtualizer(50);
		parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
		
		try (ByteArrayInputStream templateIS = new ByteArrayInputStream(templateAvvisoPostale);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();){
			
			DefaultJasperReportsContext defaultJasperReportsContext = DefaultJasperReportsContext.getInstance();
			
			JRPropertiesUtil.getInstance(defaultJasperReportsContext).setProperty("net.sf.jasperreports.xpath.executer.factory",
                    "net.sf.jasperreports.engine.util.xml.JaxenXPathExecuterFactory");
			
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
			
			JAXBElement<AvvisoPagamentoInput> jaxbElement = new JAXBElement<AvvisoPagamentoInput>(new QName("", AvvisoPagamentoCostanti.AVVISO_PAGAMENTO_ROOT_ELEMENT_NAME), AvvisoPagamentoInput.class, null, input);
			jaxbMarshaller.marshal(jaxbElement, baos);
			byte[] byteArray = baos.toByteArray();
			log.trace("AvvisoPagamentoInput: " + new String(byteArray));
			try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);){

				JRDataSource dataSource = new JRXmlDataSource(defaultJasperReportsContext, byteArrayInputStream,AvvisoPagamentoCostanti.AVVISO_PAGAMENTO_ROOT_ELEMENT_NAME);
//			JRDataSource dataSource = this.creaXmlDataSource(log,input);
				JasperReport jasperReport = (JasperReport) JRLoader.loadObject(defaultJasperReportsContext,templateIS);
				JasperPrint jasperPrint = JasperFillManager.getInstance(defaultJasperReportsContext).fill(jasperReport, parameters, dataSource);
				
				return JasperExportManager.getInstance(defaultJasperReportsContext).exportToPdf(jasperPrint);
			}finally {
				
			}
		}finally {
			
		}
	}
	
	public byte[] _creaAvvisoViolazioneCDS(Logger log, AvvisoPagamentoInput input, String codDominio, AvvisoPagamentoProperties avProperties) throws JAXBException, IOException, JRException, PropertyNotFoundException {
		// cerco file di properties esterni per configurazioni specifiche per dominio
		Properties propertiesAvvisoPerDominio = avProperties.getPropertiesPerDominio(codDominio, log);

		this.caricaLoghiAvviso(input, propertiesAvvisoPerDominio);

		Map<String, Object> parameters = new HashMap<String, Object>();
		
		parameters.put("RidottoScontato", new ByteArrayInputStream(templateRidottoScontato));
		parameters.put("Sanzione", new ByteArrayInputStream(templateSanzione));
		parameters.put("Formato", new ByteArrayInputStream(templateFormato));
		
		JRGzipVirtualizer virtualizer = new JRGzipVirtualizer(50);
		parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
		
		try (ByteArrayInputStream templateIS = new ByteArrayInputStream(templateViolazioneCDS);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();){
			
			DefaultJasperReportsContext defaultJasperReportsContext = DefaultJasperReportsContext.getInstance();
			
			JRPropertiesUtil.getInstance(defaultJasperReportsContext).setProperty("net.sf.jasperreports.xpath.executer.factory",
                    "net.sf.jasperreports.engine.util.xml.JaxenXPathExecuterFactory");
			
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
			
			JAXBElement<AvvisoPagamentoInput> jaxbElement = new JAXBElement<AvvisoPagamentoInput>(new QName("", AvvisoPagamentoCostanti.VIOLAZIONE_CDS_ROOT_ELEMENT_NAME), AvvisoPagamentoInput.class, null, input);
			jaxbMarshaller.marshal(jaxbElement, baos);
			byte[] byteArray = baos.toByteArray();
			log.trace("AvvisoPagamentoInput: " + new String(byteArray));
			try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);){

				JRDataSource dataSource = new JRXmlDataSource(defaultJasperReportsContext, byteArrayInputStream,AvvisoPagamentoCostanti.VIOLAZIONE_CDS_ROOT_ELEMENT_NAME);
//			JRDataSource dataSource = this.creaXmlDataSource(log,input);
				JasperReport jasperReport = (JasperReport) JRLoader.loadObject(defaultJasperReportsContext,templateIS);
				JasperPrint jasperPrint = JasperFillManager.getInstance(defaultJasperReportsContext).fill(jasperReport, parameters, dataSource);
				
				return JasperExportManager.getInstance(defaultJasperReportsContext).exportToPdf(jasperPrint);
			}finally {
				
			}
		}finally {
			
		}
	}

	public JRDataSource creaXmlDataSource(Logger log,AvvisoPagamentoInput input) throws UtilsException, JRException, JAXBException {
//		WriteToSerializerType serType = WriteToSerializerType.XML_JAXB;
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JAXBElement<AvvisoPagamentoInput> jaxbElement = new JAXBElement<AvvisoPagamentoInput>(new QName("", AvvisoPagamentoCostanti.AVVISO_PAGAMENTO_ROOT_ELEMENT_NAME), AvvisoPagamentoInput.class, null, input);
		jaxbMarshaller.marshal(jaxbElement, baos);
		byte[] byteArray = baos.toByteArray();
//		log.debug("AvvisoPagamentoInput: " + new String(byteArray));
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
		JRDataSource dataSource = new JRXmlDataSource(byteArrayInputStream,AvvisoPagamentoCostanti.AVVISO_PAGAMENTO_ROOT_ELEMENT_NAME);
		return dataSource;
	}

	public void caricaLoghiAvviso(AvvisoPagamentoInput input, Properties propertiesAvvisoPerDominio) {
		// valorizzo la sezione loghi
		if(input.getLogoEnte() == null)
			input.setLogoEnte(propertiesAvvisoPerDominio.getProperty(AvvisoPagamentoCostanti.LOGO_ENTE));
	}
	
	public byte[] creaAvvisoV2(Logger log, it.govpay.stampe.model.v2.AvvisoPagamentoInput input, String codDominio, AvvisoPagamentoProperties avProperties) throws JAXBException, IOException, JRException, PropertyNotFoundException {
		// cerco file di properties esterni per configurazioni specifiche per dominio
		Properties propertiesAvvisoPerDominio = avProperties.getPropertiesPerDominio(codDominio, log);

		this.caricaLoghiAvvisoV2(input, propertiesAvvisoPerDominio);

		Map<String, Object> parameters = new HashMap<String, Object>();
		
		parameters.put("MonoBandV2", new ByteArrayInputStream(templateMonoBandV2));
		parameters.put("TriBandV2", new ByteArrayInputStream(templateTriBandV2));
		parameters.put("RataUnicaV2", new ByteArrayInputStream(templateRataUnicaV2));
		parameters.put("DoppiaRataV2", new ByteArrayInputStream(templateDoppiaRataV2));
		parameters.put("DoppioFormatoV2", new ByteArrayInputStream(templateDoppioFormatoV2));
		parameters.put("BollettinoRataV2", new ByteArrayInputStream(templateBollettinoRataV2));
		parameters.put("DualBandV2", new ByteArrayInputStream(templateDualBandV2));
		
		JRGzipVirtualizer virtualizer = new JRGzipVirtualizer(50);
		parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
		
		try (ByteArrayInputStream templateIS = new ByteArrayInputStream(templateAvvisoV2);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();){
			
			DefaultJasperReportsContext defaultJasperReportsContext = DefaultJasperReportsContext.getInstance();
			
			JRPropertiesUtil.getInstance(defaultJasperReportsContext).setProperty("net.sf.jasperreports.xpath.executer.factory",
                    "net.sf.jasperreports.engine.util.xml.JaxenXPathExecuterFactory");
			
			Marshaller jaxbMarshaller = jaxbContextV2.createMarshaller();
			jaxbMarshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
			
			JAXBElement<it.govpay.stampe.model.v2.AvvisoPagamentoInput> jaxbElement = new JAXBElement<it.govpay.stampe.model.v2.AvvisoPagamentoInput>(new QName("", AvvisoPagamentoCostanti.AVVISO_PAGAMENTO_ROOT_ELEMENT_NAME), it.govpay.stampe.model.v2.AvvisoPagamentoInput.class, null, input);
			jaxbMarshaller.marshal(jaxbElement, baos);
			byte[] byteArray = baos.toByteArray();
			log.trace("AvvisoPagamentoInput: " + new String(byteArray));
			try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);){

				JRDataSource dataSource = new JRXmlDataSource(defaultJasperReportsContext, byteArrayInputStream,AvvisoPagamentoCostanti.AVVISO_PAGAMENTO_ROOT_ELEMENT_NAME);
//			JRDataSource dataSource = this.creaXmlDataSource(log,input);
				JasperReport jasperReport = (JasperReport) JRLoader.loadObject(defaultJasperReportsContext,templateIS);
				JasperPrint jasperPrint = JasperFillManager.getInstance(defaultJasperReportsContext).fill(jasperReport, parameters, dataSource);
				
				return JasperExportManager.getInstance(defaultJasperReportsContext).exportToPdf(jasperPrint);
			}finally {
				
			}
		}finally {
			
		}
	}
	
	public void caricaLoghiAvvisoV2(it.govpay.stampe.model.v2.AvvisoPagamentoInput input, Properties propertiesAvvisoPerDominio) {
		// valorizzo la sezione loghi
		if(input.getLogoEnte() == null)
			input.setLogoEnte(propertiesAvvisoPerDominio.getProperty(AvvisoPagamentoCostanti.LOGO_ENTE));
	}
	
}
