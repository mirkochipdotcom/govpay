Feature: Verifica avviso precaricato

Background:

* callonce read('classpath:utils/common-utils.feature')
* callonce read('classpath:configurazione/v1/anagrafica.feature')

Scenario: Verifica avviso precaricato basic

* def idPendenza = getCurrentTimeMillis()
* def pendenzeBaseurl = getGovPayApiBaseUrl({api: 'pendenze', versione: 'v2', autenticazione: 'basic'})
* def basicAutenticationHeader = getBasicAuthenticationHeader( { username: idA2A, password: pwdA2A } )
* def pendenza = read('classpath:test/api/pendenza/v2/pendenze/put/msg/pendenza-put_monovoce_riferimento.json')

Given url pendenzeBaseurl
And path 'pendenze', idA2A, idPendenza
And headers basicAutenticationHeader
And request pendenza
When method put
Then status 201

* def numeroAvviso = response.numeroAvviso
* def pagamentiBaseurl = getGovPayApiBaseUrl({api: 'pagamento', versione: 'v2', autenticazione: 'basic'})

Given url pagamentiBaseurl
And path '/avvisi', idDominio, numeroAvviso
And headers basicAutenticationHeader
And header Accept = 'application/json'
When method get
Then status 200
And match response == 
"""
{ 
	stato: 'NON_ESEGUITA',
	importo: 100.99,
	idDominio: '#(idDominio)',
	numeroAvviso: '#(numeroAvviso)',
	dataValidita: '2900-12-31',
	dataScadenza: '2999-12-31',
	descrizione: '#notnull',
	tassonomiaAvviso: 'Servizi erogati dal comune',
	qrcode: '#("PAGOPA|002|" + numeroAvviso + "|" + idDominio + "|10099")',
	barcode: '#notnull'
}
"""

Given url pagamentiBaseurl
And path '/avvisi', idDominio, numeroAvviso
And headers basicAutenticationHeader
And header Accept = 'application/pdf'
When method get
Then status 200

Given url pagamentiBaseurl
And path '/avvisi', idDominio, numeroAvviso
And headers basicAutenticationHeader
When method get
Then status 406

@test2
Scenario: Verifica avviso non presente basic

* def idPendenza = getCurrentTimeMillis()
* def pendenzeBaseurl = getGovPayApiBaseUrl({api: 'pendenze', versione: 'v2', autenticazione: 'basic'})
* def basicAutenticationHeader = getBasicAuthenticationHeader( { username: idA2A, password: pwdA2A } )
* def pendenzaPut = read('classpath:test/api/pendenza/v2/pendenze/put/msg/pendenza-put_monovoce_riferimento.json')
* def numeroAvviso = buildNumeroAvviso(dominio, applicazione)
* def iuv = getIuvFromNumeroAvviso(numeroAvviso)	
* call read('classpath:utils/pa-prepara-avviso.feature')

* def pagamentiBaseurl = getGovPayApiBaseUrl({api: 'pagamento', versione: 'v2', autenticazione: 'basic'})

Given url pagamentiBaseurl
And path '/avvisi', idDominio, numeroAvviso
And headers basicAutenticationHeader
And header Accept = 'application/json'
When method get
Then status 200
And match response == 
"""
{ 
	stato: 'NON_ESEGUITA',
	importo: 100.99,
	idDominio: '#(idDominio)',
	numeroAvviso: '#(numeroAvviso)',
	dataValidita: '2900-12-31',
	dataScadenza: '2999-12-31',
	descrizione: '#notnull',
	tassonomiaAvviso: 'Servizi erogati dal comune',
	qrcode: '#("PAGOPA|002|" + numeroAvviso + "|" + idDominio + "|10099")',
	barcode: '#notnull'
}
"""

Given url pagamentiBaseurl
And path '/avvisi', idDominio, numeroAvviso
And headers basicAutenticationHeader
And header Accept = 'application/pdf'
When method get
Then status 200


@test3
Scenario: Verifica avviso non presente basic

* def idPendenza = getCurrentTimeMillis()
* def pendenzeBaseurl = getGovPayApiBaseUrl({api: 'pendenze', versione: 'v2', autenticazione: 'basic'})
* def basicAutenticationHeader = getBasicAuthenticationHeader( { username: idA2A, password: pwdA2A } )
* def pendenzaPut = read('classpath:test/api/pendenza/v2/pendenze/put/msg/pendenza-put_monovoce_riferimento.json')
* def numeroAvviso = buildNumeroAvviso(dominio, applicazione)
* def iuv = getIuvFromNumeroAvviso(numeroAvviso)	
* call read('classpath:utils/pa-prepara-avviso.feature')

* def pagamentiBaseurl = getGovPayApiBaseUrl({api: 'pagamento', versione: 'v2', autenticazione: 'basic'})

Given url pagamentiBaseurl
And path '/avvisi', idDominio, numeroAvviso
And headers basicAutenticationHeader
And header Accept = 'application/json'
When method get
Then status 200
And match response == 
"""
{ 
	stato: 'NON_ESEGUITA',
	importo: 100.99,
	idDominio: '#(idDominio)',
	numeroAvviso: '#(numeroAvviso)',
	dataValidita: '2900-12-31',
	dataScadenza: '2999-12-31',
	descrizione: '#notnull',
	tassonomiaAvviso: 'Servizi erogati dal comune',
	qrcode: '#("PAGOPA|002|" + numeroAvviso + "|" + idDominio + "|10099")',
	barcode: '#notnull'
}
"""

Given url pagamentiBaseurl
And path '/avvisi', idDominio, numeroAvviso
And headers basicAutenticationHeader
And header Accept = 'application/pdf'
When method get
Then status 200
And match header content-disposition contains 'attachment'
And match header content-disposition contains 'pdf'
* def responseAsString = createString(responseBytes)
And match responseAsString contains 'JasperReports'

