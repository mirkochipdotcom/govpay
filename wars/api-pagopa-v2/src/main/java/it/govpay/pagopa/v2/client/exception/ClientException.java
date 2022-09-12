package it.govpay.pagopa.v2.client.exception;

public class ClientException  extends Exception {
	private static final long serialVersionUID = 1L;
	private Integer responseCode = null;
	private byte[] responseContent = null;

	public ClientException(String message, Throwable e, Integer responseCode) {
		this(message, e, responseCode, null);
	}

	public ClientException(Throwable e, Integer responseCode) {
		this(e, responseCode, null);
	}

	public ClientException(String string, Integer responseCode) {
		this(string, responseCode, null);
	}

	public ClientException(String message, Exception e) {
		super(message, e);
	}

	public ClientException(Throwable e) {
		super(e);
	}

	public ClientException(String string) {
		super(string);
	}

	public ClientException(Throwable e, Integer responseCode, byte[] responseContent) {
		super(e);
		this.responseCode = responseCode;
		this.responseContent = responseContent;
	}

	public ClientException(String string, Integer responseCode, byte[] responseContent) {
		super(string);
		this.responseCode = responseCode;
		this.responseContent = responseContent;
	}

	public ClientException(String message, Throwable e, Integer responseCode, byte[] responseContent) {
		super(message, e);
		this.responseCode = responseCode;
		this.responseContent = responseContent;
	}

	public Integer getResponseCode() {
		return this.responseCode;
	}

	public byte[] getResponseContent () {
		return this.responseContent;
	}

}
