package com.kcert.fido.lang;

public class DefinedException extends Exception {
	private static final long serialVersionUID = 1L;
	
    public DefinedException(Throwable cause) {
        super(cause);
    }
    public DefinedException(String message, Throwable cause) {
        super(message, cause);
    }
    public DefinedException(String message) {
        super(message);
    }
    public DefinedException() {
        super();
    }
	
}
