/**
 * 
 */
package it.govpay.core.dao.anagrafica.utils;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.utils.json.ValidationException;
import org.springframework.security.core.Authentication;

import it.govpay.bd.BasicBD;
import it.govpay.bd.anagrafica.AclBD;
import it.govpay.bd.anagrafica.AnagraficaManager;
import it.govpay.bd.anagrafica.ApplicazioniBD;
import it.govpay.bd.anagrafica.UtenzeBD;
import it.govpay.bd.model.Applicazione;
import it.govpay.bd.model.Utenza;
import it.govpay.bd.model.eventi.EventoNota;
import it.govpay.bd.model.eventi.EventoNota.TipoNota;
import it.govpay.core.autorizzazione.beans.GovpayLdapUserDetails;
import it.govpay.core.autorizzazione.utils.AutorizzazioneUtils;
import it.govpay.model.Acl;
import it.govpay.model.Acl.Diritti;
import it.govpay.model.Acl.Servizio;
import it.govpay.model.PatchOp;
import it.govpay.model.Utenza.TIPO_UTENZA;

/**
 * @author Bussu Giovanni (bussu@link.it)
 * @author  $Author: bussu $
 * @version $ Rev: 12563 $, $Date: 03 lug 2018 $
 * 
 */
public class UtenzaPatchUtils {

	public static final String PATH_ENTRATE = "/entrate";
	public static final String PATH_DOMINI = "/domini";
	public static final String PATH_ACL = "/acl";
	public static final String PATH_XX_NON_VALIDO = "Path ''{0}'' non valido";
	public static final String PATH_NON_VALIDO = "Il campo path non e' valido.";
	public static final String VALUE_NON_VALIDO_PER_IL_PATH_XX = "Value non valido per il path ''{0}''";
	public static final String VALUE_NON_VALIDO_PER_IL_PATH = "Il campo value non e' valido per il path indicato.";
	public static final String OP_XX_NON_VALIDO_PER_IL_PATH_YY = "Op ''{0}'' non valido per il path ''{1}''";
	public static final String ACL_NON_VALIDA_SERVIZIO_XX_NON_GESTITO = "ACL non valida: servizio `{0}` non gestito.";
	public static final String VALUE_NON_VALIDO_PER_IL_PATH_XX_DI_UTENZA = "Value non valido per il path ''{0}'' di un'utenza di tipo ''{1}''";
	public static final String ACL_NON_VALIDA_ATTESA_LISTA_DI_STRINGHE_NEL_CAMPO_AUTORIZZAZIONI = "ACL non valida: attesa lista di stringhe nel campo `autorizzazioni`";
	public static final String ACL_NON_VALIDA_ATTESO_CAMPO_AUTORIZZAZIONI = "ACL non valida: atteso campo `autorizzazioni`";
	public static final String AUTORIZZAZIONI_KEY = "autorizzazioni";
	public static final String SERVIZIO_KEY = "servizio";
	public static final String ACL_NON_VALIDA_ATTESA_STRINGA_COME_VALORE_DI_SERVIZIO = "ACL non valida: attesa stringa come valore di `servizio`";
	public static final String ACL_NON_VALIDA_ATTESO_CAMPO_SERVIZIO = "ACL non valida: atteso campo `servizio`";
	
	public static final String PRINCIPAL_NOTA_KEY = "principal";
	public static final String AUTORE_NOTA_KEY = "autore";
	public static final String TIPO_NOTA_KEY = "tipo";
	public static final String DATA_NOTA_KEY = "data";
	public static final String OGGETTO_NOTA_KEY = "oggetto";
	public static final String TESTO_NOTA_KEY = "testo";
	
	public static final String DOMINI_STAR = "*";
	public static final String ENTRATE_STAR = "*";
	public static final String AUTODETERMINAZIONE_ENTRATE = "autodeterminazione";

	
	public static Utenza patchUtenza(PatchOp op, Utenza utenza, BasicBD bd) throws ServiceException, NotFoundException, ValidationException {

		if(PATH_ACL.equals(op.getPath())) {
			patchACL(op, utenza, bd);
		} else if(PATH_DOMINI.equals(op.getPath())) {
			patchDominio(op, utenza, bd);
		} else if(PATH_ENTRATE.equals(op.getPath())) {
			patchEntrata(op, utenza, bd);
		} else {
			throw new ValidationException(MessageFormat.format(PATH_XX_NON_VALIDO, op.getPath()));
		}

		return utenza;
	}

	private static void patchEntrata(PatchOp op, Utenza utenza, BasicBD bd)
			throws ValidationException, ServiceException, NotFoundException {
		if(!(op.getValue() instanceof String)) throw new ValidationException(MessageFormat.format(VALUE_NON_VALIDO_PER_IL_PATH_XX, op.getPath()));
		String tributo = (String) op.getValue();
		
		if(tributo.equals(ENTRATE_STAR)) {
			switch(op.getOp()) {
			case ADD: utenza.setAutorizzazioneTributiStar(true);
			break;
			case DELETE: utenza.setAutorizzazioneTributiStar(false);
			break;
			default: throw new ValidationException(MessageFormat.format(OP_XX_NON_VALIDO_PER_IL_PATH_YY, op.getOp().name(), op.getPath()));
			}
			utenza.getIdTipiTributo().clear();
		} else if(tributo.equals(AUTODETERMINAZIONE_ENTRATE)) {
			if(utenza.getTipoUtenza().equals(TIPO_UTENZA.APPLICAZIONE)) {
				Applicazione applicazioneByPrincipal = AnagraficaManager.getApplicazioneByPrincipal(bd, utenza.getPrincipalOriginale());
				switch(op.getOp()) {
				case ADD: applicazioneByPrincipal.setTrusted(true);
				break;
				case DELETE: applicazioneByPrincipal.setTrusted(false);
				break;
				default: throw new ValidationException(MessageFormat.format(OP_XX_NON_VALIDO_PER_IL_PATH_YY, op.getOp().name(), op.getPath()));
				}
				
				ApplicazioniBD applicazioniBD = new ApplicazioniBD(bd);
				applicazioniBD.updateApplicazioneTrusted(applicazioneByPrincipal.getId(), applicazioneByPrincipal.getCodApplicazione(), applicazioneByPrincipal.isTrusted());
			} else {
				throw new ValidationException(MessageFormat.format(VALUE_NON_VALIDO_PER_IL_PATH_XX_DI_UTENZA, op.getValue(), utenza.getTipoUtenza().name()));
			}
		} else {
			try {
				AnagraficaManager.getTipoTributo(bd, tributo).getId();
			} catch (NotFoundException e) {
				throw new ValidationException(MessageFormat.format(VALUE_NON_VALIDO_PER_IL_PATH_XX, op.getPath()));
			}
			Long idTributo = AnagraficaManager.getTipoTributo(bd, tributo).getId();
			switch(op.getOp()) {
			case ADD: utenza.getIdTipiTributo().add(idTributo); 
			break;
			case DELETE: utenza.getIdTipiTributo().remove(idTributo);
			break;
			default: throw new ValidationException(MessageFormat.format(OP_XX_NON_VALIDO_PER_IL_PATH_YY, op.getOp().name(), op.getPath()));
			}
		}
		UtenzeBD utenzaBD = new UtenzeBD(bd);
		utenzaBD.updateUtenza(utenza);

		utenza.setTipiributo(null);
		utenza.getTipiTributo(bd);
	}

	private static void patchDominio(PatchOp op, Utenza utenza, BasicBD bd)
			throws ValidationException, ServiceException, NotFoundException {
		if(!(op.getValue() instanceof String)) throw new ValidationException(MessageFormat.format(VALUE_NON_VALIDO_PER_IL_PATH_XX, op.getPath()));
		String dominio = (String) op.getValue();

		if(dominio.equals(DOMINI_STAR)) {
			switch(op.getOp()) {
			case ADD: utenza.setAutorizzazioneDominiStar(true);
			break;
			case DELETE: utenza.setAutorizzazioneDominiStar(false);
			break;
			default: throw new ValidationException(MessageFormat.format(OP_XX_NON_VALIDO_PER_IL_PATH_YY, op.getOp().name(), op.getPath()));
			}
			utenza.getIdDomini().clear();
		} else {
			try {
				AnagraficaManager.getDominio(bd, dominio).getId();
			} catch (NotFoundException e) {
				throw new ValidationException(MessageFormat.format(VALUE_NON_VALIDO_PER_IL_PATH_XX, op.getPath()));
			}
			Long idDominio = AnagraficaManager.getDominio(bd, dominio).getId();
			switch(op.getOp()) {
			case ADD: utenza.getIdDomini().add(idDominio); 
			break;
			case DELETE: utenza.getIdDomini().remove(idDominio);
			break;
			default: throw new ValidationException(MessageFormat.format(OP_XX_NON_VALIDO_PER_IL_PATH_YY, op.getOp().name(), op.getPath()));
			}
		}
		
		UtenzeBD utenzaBD = new UtenzeBD(bd);
		utenzaBD.updateUtenza(utenza);

		utenza.setDomini(null);
		utenza.getDomini(bd);
	}

	public static void patchRuolo(PatchOp op, String idRuolo, List<Acl> lstAclAttualiRuolo, BasicBD bd)
			throws ValidationException, ServiceException, NotFoundException {
		LinkedHashMap<?,?> map = (LinkedHashMap<?,?>) op.getValue();
		Acl acl = new Acl();
		acl.setRuolo(idRuolo); 
		setServizioAcl(map, acl);
		setAutorizzazioniAcl(map, acl);
		
		boolean found = false;
		for(Acl aclPresente : lstAclAttualiRuolo) {
			if(aclPresente.getServizio().equals(acl.getServizio()) && (aclPresente.getRuolo() != null && acl.getRuolo().equals(aclPresente.getRuolo()))) {
				found = true;
				break;
			}
		}
		
		AclBD aclBD = new AclBD(bd);
		switch(op.getOp()) {
		case ADD: 
			if(!found)
				aclBD.insertAcl(acl);
			else
				aclBD.updateAcl(acl);
			break;
//		case REPLACE:
//			aclBD.updateAcl(acl);
//			break;
		case DELETE: 
			aclBD.deleteAcl(acl);
			break;
		default: throw new ValidationException(MessageFormat.format(OP_XX_NON_VALIDO_PER_IL_PATH_YY, op.getOp().name(), op.getPath()));
		}
	}

	private static void patchACL(PatchOp op, Utenza utenza, BasicBD bd)
			throws ValidationException, ServiceException, NotFoundException {
		LinkedHashMap<?,?> map = (LinkedHashMap<?,?>) op.getValue();
		Acl acl = new Acl();
		setServizioAcl(map, acl);
		setAutorizzazioniAcl(map, acl);
		
		AclBD aclBD = new AclBD(bd);
		acl.setPrincipal(utenza.getPrincipalOriginale()); 
		
		boolean found = false;
		for(Acl aclPresente : utenza.getAcls()) {
			if(aclPresente.getServizio().equals(acl.getServizio()) && (aclPresente.getPrincipal() != null && acl.getPrincipal().equals(aclPresente.getPrincipal()))) {
				found = true;
				break;
			}
		}
		
		switch(op.getOp()) {
		case ADD: 
			if(!found)
				aclBD.insertAcl(acl);
			else
				aclBD.updateAcl(acl);
			break;
		case DELETE: 
			if(found)
				aclBD.deleteAcl(acl);
			else
				throw new ValidationException(MessageFormat.format(OP_XX_NON_VALIDO_PER_IL_PATH_YY, op.getOp().name(), op.getPath()));
			break;
		default: throw new ValidationException(MessageFormat.format(OP_XX_NON_VALIDO_PER_IL_PATH_YY, op.getOp().name(), op.getPath()));
		}
	}

	private static void setAutorizzazioniAcl(LinkedHashMap<?, ?> map, Acl acl) throws ValidationException {
		if(map.get(AUTORIZZAZIONI_KEY) == null) throw new ValidationException(ACL_NON_VALIDA_ATTESO_CAMPO_AUTORIZZAZIONI);
		if(!(map.get(AUTORIZZAZIONI_KEY) instanceof List<?>)) throw new ValidationException(ACL_NON_VALIDA_ATTESA_LISTA_DI_STRINGHE_NEL_CAMPO_AUTORIZZAZIONI);

		List<?> lstAuth = (List<?>) map.get(AUTORIZZAZIONI_KEY);
		Set<Diritti> listaDiritti = new HashSet<>();

		for(Object obj: lstAuth) {
			try {
				if(!(obj instanceof String)) throw new ValidationException(ACL_NON_VALIDA_ATTESA_LISTA_DI_STRINGHE_NEL_CAMPO_AUTORIZZAZIONI);
				Diritti diritto = Diritti.toEnum((String)obj);
				if(!listaDiritti.contains(diritto))
					listaDiritti.add(diritto);
			} catch (ServiceException se) {
				throw new ValidationException(MessageFormat.format(ACL_NON_VALIDA_SERVIZIO_XX_NON_GESTITO, map.get(SERVIZIO_KEY)));
			}
		}
		acl.setListaDiritti(listaDiritti);
	}

	private static void setServizioAcl(LinkedHashMap<?, ?> map, Acl acl) throws ValidationException {
		try {
			if(map.get(SERVIZIO_KEY) == null) throw new ValidationException(ACL_NON_VALIDA_ATTESO_CAMPO_SERVIZIO);
			if(!(map.get(SERVIZIO_KEY) instanceof String)) throw new ValidationException(ACL_NON_VALIDA_ATTESA_STRINGA_COME_VALORE_DI_SERVIZIO);
			acl.setServizio(Servizio.toEnum((String) map.get(SERVIZIO_KEY)));
		} catch (ServiceException se) {
			throw new ValidationException(MessageFormat.format(ACL_NON_VALIDA_SERVIZIO_XX_NON_GESTITO, map.get(SERVIZIO_KEY)));
		}
		
	}

	public static EventoNota getNotaFromPatch(Authentication authentication, PatchOp op, BasicBD bd) throws ValidationException, ServiceException { 
		LinkedHashMap<?,?> map = (LinkedHashMap<?,?>) op.getValue();
		
		GovpayLdapUserDetails userDetails = AutorizzazioneUtils.getAuthenticationDetails(authentication);
		EventoNota nota = new EventoNota();
		nota.setPrincipal(userDetails.getUtenza().getPrincipal());
		nota.setAutore(userDetails.getUtenza().getIdentificativo());
		nota.setData(new Date());
		nota.setTesto((String)map.get(TESTO_NOTA_KEY));
		nota.setOggetto((String)map.get(OGGETTO_NOTA_KEY));
		nota.setTipoEvento(TipoNota.valueOf((String) map.get(TIPO_NOTA_KEY)));
				
		return nota;
	}
}
