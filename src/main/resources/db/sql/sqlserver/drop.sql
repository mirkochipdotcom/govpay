-- Gli indici vengono eliminati automaticamente una volta eliminata la tabella
-- DROP INDEX index_stampe_1 ON stampe ;
-- DROP INDEX index_batch_1 ON batch ;
-- DROP INDEX index_fr_1 ON fr ;
-- DROP INDEX index_incassi_1 ON incassi ;
-- DROP INDEX index_iuv_1 ON iuv ;
-- DROP INDEX index_rr_1 ON rr ;
-- DROP INDEX index_tipi_vers_domini_1 ON tipi_vers_domini ;
-- DROP INDEX index_tipi_versamento_1 ON tipi_versamento ;
-- DROP INDEX index_connettori_1 ON connettori ;
-- DROP INDEX index_operatori_1 ON operatori ;
-- DROP INDEX index_uo_1 ON uo ;
-- DROP INDEX index_tributi_1 ON tributi ;
-- DROP INDEX index_tipi_tributo_1 ON tipi_tributo ;
-- DROP INDEX index_iban_accredito_1 ON iban_accredito ;
-- DROP INDEX index_domini_1 ON domini ;
-- DROP INDEX index_applicazioni_2 ON applicazioni ;
-- DROP INDEX index_applicazioni_1 ON applicazioni ;
-- DROP INDEX index_utenze_1 ON utenze ;
-- DROP INDEX index_stazioni_1 ON stazioni ;
-- DROP INDEX index_intermediari_1 ON intermediari ;
-- DROP INDEX index_configurazione_1 ON configurazione ;
-- DROP INDEX idx_evt_iuv ON eventi ;
-- DROP INDEX idx_evt_id_sessione ON eventi ;
-- DROP INDEX idx_evt_fk_vrs ON eventi ;
-- DROP INDEX idx_evt_data ON eventi ;
-- DROP INDEX idx_pag_fk_sng ON pagamenti ;
-- DROP INDEX idx_pag_fk_rpt ON pagamenti ;
-- DROP INDEX idx_iuv_rifversamento ON iuv ;
-- DROP INDEX idx_nai_da_spedire ON notifiche_app_io ;
-- DROP INDEX idx_ntf_da_spedire ON notifiche ;
-- DROP INDEX idx_rpt_fk_prt ON rpt ;
-- DROP INDEX idx_rpt_fk_vrs ON rpt ;
-- DROP INDEX idx_rpt_stato ON rpt ;
-- DROP INDEX idx_rpt_cod_msg_richiesta ON rpt ;
-- DROP INDEX idx_ppv_fk_vrs ON pag_port_versamenti ;
-- DROP INDEX idx_ppv_fk_prt ON pag_port_versamenti ;
-- DROP INDEX idx_prt_versante_identif ON pagamenti_portale ;
-- DROP INDEX idx_prt_id_sessione_psp ON pagamenti_portale ;
-- DROP INDEX idx_prt_id_sessione ON pagamenti_portale ;
-- DROP INDEX idx_prt_stato ON pagamenti_portale ;
-- DROP INDEX idx_vrs_avv_io_prom_scad ON versamenti ;
-- DROP INDEX idx_vrs_avv_mail_prom_scad ON versamenti ;
-- DROP INDEX idx_vrs_prom_avviso ON versamenti ;
-- DROP INDEX idx_vrs_auth ON versamenti ;
-- DROP INDEX idx_vrs_iuv ON versamenti ;
-- DROP INDEX idx_vrs_deb_identificativo ON versamenti ;
-- DROP INDEX idx_vrs_stato_vrs ON versamenti ;
-- DROP INDEX idx_vrs_data_creaz ON versamenti ;
-- DROP INDEX idx_vrs_id_pendenza ON versamenti ;
DROP VIEW versamenti_incassi;
DROP VIEW v_eventi_vers;
DROP VIEW v_eventi_vers_base;
DROP VIEW v_eventi_vers_pagamenti;       
DROP VIEW v_eventi_vers_rendicontazioni; 
DROP VIEW v_eventi_vers_riconciliazioni;
DROP VIEW v_eventi_vers_tracciati;
DROP VIEW v_pagamenti_portale;   
DROP VIEW v_riscossioni;
DROP VIEW v_riscossioni_con_rpt;
DROP VIEW v_riscossioni_senza_rpt;
DROP VIEW v_rendicontazioni_ext;
DROP VIEW v_rpt_versamenti;
DROP VIEW v_pagamenti;
DROP VIEW v_versamenti;
DROP TABLE allegati;
DROP TABLE ID_MESSAGGIO_RELATIVO;
DROP TABLE gp_audit;
DROP TABLE operazioni;
DROP TABLE stampe;
DROP TABLE batch;
DROP TABLE eventi;
DROP TABLE rendicontazioni;
DROP TABLE pagamenti;
DROP TABLE fr;
DROP TABLE incassi;
DROP TABLE iuv;
DROP TABLE promemoria;
DROP TABLE notifiche_app_io;
DROP TABLE notifiche;
DROP TABLE rr;
DROP TABLE rpt;
DROP TABLE trac_notif_pag;
DROP TABLE pag_port_versamenti;
DROP TABLE pagamenti_portale;
DROP TABLE singoli_versamenti;
DROP TABLE versamenti;
DROP TABLE documenti;
DROP TABLE utenze_tipo_vers;
DROP TABLE tipi_vers_domini;
DROP TABLE tipi_versamento;
DROP TABLE tracciati;
DROP TABLE acl;
DROP TABLE connettori;
DROP TABLE operatori;
DROP TABLE utenze_domini;
DROP TABLE uo;
DROP TABLE tributi;
DROP TABLE tipi_tributo;
DROP TABLE iban_accredito;
DROP TABLE domini;
DROP TABLE applicazioni;
DROP TABLE utenze;
DROP TABLE stazioni;
DROP TABLE intermediari;
DROP TABLE configurazione;
