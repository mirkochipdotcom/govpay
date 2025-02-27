package it.govpay.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.openspcoop2.utils.LoggerWrapperFactory;
import org.openspcoop2.utils.UtilsException;
import org.slf4j.Logger;

import it.govpay.core.exceptions.ConfigException;
import it.govpay.core.exceptions.PropertyNotFoundException;

public class LabelAvvisiProperties {

	public static final String PROPERTIES_FILE_NAME = "avvisi.properties";
	public static final String PROPERTIES_FILE = "/" + PROPERTIES_FILE_NAME;
	
	public static final String DEFAULT_PROPS = "it";
	public static final String [] LINGUE_DISPONIBILI = { "it" , "de",  "en", "fr", "sl" };
	
	public static final String LABEL_AVVISO_PAGAMENTO = "avviso_pagamento";
	public static final String LABEL_ENTE_CREDITORE = "ente_creditore";
	public static final String LABEL_DESTINATARIO_AVVISO = "destinatario_avviso";
	public static final String LABEL_QUANTO_QUANDO = "quanto_quando";
	public static final String LABEL_COME = "come";
	public static final String LABEL_DOVE = "dove";
	public static final String LABEL_PAGA_APP = "paga_app";
	public static final String LABEL_PAGA_APP_STANDARD = "paga_app_standard";
	public static final String LABEL_PAGA_APP_POSTE = "paga_app_poste";
	public static final String LABEL_PAGA_TERRITORIO = "paga_territorio";
	public static final String LABEL_PAGA_TERRITORIO_STANDARD = "paga_territorio_standard";
	public static final String LABEL_PAGA_TERRITORIO_POSTE = "paga_territorio_poste";
	public static final String LABEL_NOTA = "nota";
	public static final String LABEL_NOTA_IMPORTO = "nota_importo";
	public static final String LABEL_NOTA_PRIMA_RATA = "nota_prima_rata";
	public static final String LABEL_NOTA_RATA_UNICA = "nota_rata_unica";
	public static final String LABEL_RATA_UNICA_ENTRO_IL = "rata_unica_entro_il";
	public static final String LABEL_RATA_UNICA_ENTRO_GIORNI = "rata_unica_entro_giorni";
	public static final String LABEL_RATA_ENTRO_IL = "rata_entro_il";
	public static final String LABEL_PRIMA_RATA = "prima_rata";
	public static final String LABEL_PRIMA_RATA_ENTRO_GIORNI = "prima_rata_entro_giorni";
	public static final String LABEL_ELENCO_RATE_1 = "elenco_rate_1";
	public static final String LABEL_ELENCO_RATE_2 = "elenco_rate_2";
	public static final String LABEL_ELENCO_RATE_3 = "elenco_rate_3";
	public static final String LABEL_NUMERO_RATA = "numero_rata";
	public static final String LABEL_IMPORTO = "importo";
	public static final String LABEL_ENTRO = "entro";
	public static final String LABEL_ENTRO_IL = "entro_il";
	public static final String LABEL_OLTRE = "oltre";
	public static final String LABEL_CANALI = "canali";
	public static final String LABEL_DESTINATARIO = "destinatario";
	public static final String LABEL_INTESTATARIO = "intestatario";
	public static final String LABEL_OGGETTO = "oggetto";
	public static final String LABEL_DESCRIZIONE = "descrizione";
	public static final String LABEL_TIPO = "tipo";
	public static final String LABEL_CODICE_CBILL = "codice_cbill";
	public static final String LABEL_CODICE_AVVISO = "codice_avviso";
	public static final String LABEL_CODICE_FISCALE_ENTE = "codice_fiscale_ente";
	public static final String LABEL_SOLUZIONE_UNICA_ENTRO_GIORNI = "soluzione_unica_entro_giorni";
	public static final String LABEL_SOLUZIONE_UNICA_OLTRE_GIORNI = "soluzione_unica_oltre_giorni";
	public static final String LABEL_VIOLAZIONE_CDS_SCADENZA_SCONTATO = "violazione_cds_scadenza_scontato";
	public static final String LABEL_VIOLAZIONE_CDS_SCADENZA_RIDOTTO = "violazione_cds_scadenza_ridotto";

	private static LabelAvvisiProperties instance;

	private static Logger log = LoggerWrapperFactory.getLogger("boot");

	private String govpayResourceDir = null;
	
	private Map<String, Properties> propMap = new HashMap<>();

	public static LabelAvvisiProperties getInstance() {
		return instance;
	}

	public static synchronized LabelAvvisiProperties newInstance(InputStream is) throws ConfigException {
		if(instance == null)
			instance = new LabelAvvisiProperties(is);
		return instance;
	}

	private Properties[] props  = null;

	private LabelAvvisiProperties(InputStream is) throws ConfigException {
		try {

			
			this.govpayResourceDir = GovpayConfig.getInstance().getResourceDir();
			// Recupero il property all'interno dell'EAR
			this.props = new Properties[2];
			Properties props1 = new Properties();
			props1.load(is);
			this.props[1] = props1;
			
			Properties props0 = null;
			this.props[0] = props0;

			File gpConfigFile = new File(this.govpayResourceDir + File.separatorChar + PROPERTIES_FILE_NAME);
			if(gpConfigFile.exists()) {
				props0 = new Properties();
				try(InputStream isExt = new FileInputStream(gpConfigFile)) {
					props0.load(isExt);
				} catch (FileNotFoundException e) {
					throw new ConfigException(e);
				} catch (IOException e) {
					throw new ConfigException(e);
				} 
				log.debug(MessageFormat.format("Individuata configurazione prioritaria Mapping Label TipoEvento: {0}", gpConfigFile.getAbsolutePath()));
				this.props[0] = props0;
			}
			
			// carico le lingue disponibili
			for (String lingua : LINGUE_DISPONIBILI) {
				Properties properties = getProperties(lingua+".", props, false, log);
				this.propMap.put(lingua, properties);
			}
		} catch (PropertyNotFoundException e) {
			log.error(MessageFormat.format("Errore di inizializzazione gestore label avvisi pagamento: {0}", e.getMessage()), e); 
			throw new ConfigException(e);
		} catch (IOException e) {
			log.error(MessageFormat.format("Errore di inizializzazione gestore label avvisi pagamento: {0}", e.getMessage()), e); 
			throw new ConfigException(e);
		}
	}
	
	private static Properties getProperties(String baseName, Properties[] props, boolean required, Logger log) throws PropertyNotFoundException {
		Properties valori = new Properties();
		
		List<String> nomiProperties = new ArrayList<String>();
		// 1. collezionare tutti i nomi di properties da leggere (possono essere definiti in piu' file)
		for(int i=0; i<props.length; i++) {
			if(props[i] != null) {
				for (Object nameObj : props[i].keySet()) {
					String name = (String) nameObj;
					if(name.startsWith(baseName) && !nomiProperties.contains(name)) {
						nomiProperties.add(name);
					}
				}
			}
		}
		
		// 2. leggere la property singola
		for (String nomeProprieta : nomiProperties) {
			String valoreProprieta = getProperty(nomeProprieta, props, required, log);
			
			if (valoreProprieta != null) {
				String key = nomeProprieta.substring(baseName.length());
				valori.put(key, valoreProprieta);
			}
		}
		
		return valori;
	}
	
	private static String getProperty(String name, Properties[] props, boolean required, Logger log) throws PropertyNotFoundException {
		String value = null;
		for(int i=0; i<props.length; i++) {
			try { value = getProperty(name, props[i], required, i==1, log); } catch (PropertyNotFoundException e) { }
			if(value != null && !value.trim().isEmpty()) {
				return value;
			}
		}

		if(log!= null) log.info(MessageFormat.format("Proprieta {0} non trovata", name));

		if(required) 
			throw new PropertyNotFoundException(MessageFormat.format("Proprieta [{0}] non trovata", name));
		else 
			return null;
	}

	private static String getProperty(String name, Properties props, boolean required, boolean fromInternalConfig, Logger log) throws PropertyNotFoundException {
		String value = System.getProperty(name);

		if(value != null && value.trim().isEmpty()) {
			value = null;
		}
		String logString = "";
		if(fromInternalConfig) logString = "da file interno ";
		else logString = "da file esterno ";

		if(value == null) {
			if(props != null) {
				value = props.getProperty(name);
				if(value != null && value.trim().isEmpty()) {
					value = null;
				}
			}
			if(value == null) {
				if(required) 
					throw new PropertyNotFoundException(MessageFormat.format("Proprieta [{0}] non trovata", name));
				else return null;
			} else {
				if(log != null) log.info(MessageFormat.format("Letta proprieta di configurazione {0}{1}: {2}", logString, name, value));
			}
		} else {
			if(log != null) log.info(MessageFormat.format("Letta proprieta di sistema {0}: {1}", name, value));
		}

		return value.trim();
	}
	
	public Properties getLabelsLingua(String lingua) throws UtilsException {
		if(lingua == null) lingua = DEFAULT_PROPS;
		Properties p = this.propMap.get(lingua);

		if(p == null) {
			log.debug(MessageFormat.format("Configurazione [{0}] non trovata", lingua));
			throw new UtilsException(MessageFormat.format("Configurazione [{0}] non trovata", lingua));
		}

		return p;
	}
}
