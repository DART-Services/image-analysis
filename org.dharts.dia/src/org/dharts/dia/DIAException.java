package org.dharts.dia;

public class DIAException extends Exception {

	public DIAException() {
	}

	public DIAException(String message) {
		super(message);
	}

	public DIAException(Throwable cause) {
		super(cause);
	}

	public DIAException(String message, Throwable cause) {
		super(message, cause);
	}

	public DIAException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
