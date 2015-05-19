package net.zyuiop.bungeebridge.core.players;

public class InvalidTypeException extends IllegalArgumentException {
	public InvalidTypeException() {
	}

	public InvalidTypeException(String s) {
		super(s);
	}

	public InvalidTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidTypeException(Throwable cause) {
		super(cause);
	}
}
