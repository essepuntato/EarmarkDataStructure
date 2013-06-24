package it.essepuntato.earmark.core.exception;

/**
 * This exception is thrown when we try to add a new item with an
 * id that already exists. 
 * 
 * @author Silvio Peroni
 */
public class ExistingIdException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * The constructor of the exception.
	 */
	public ExistingIdException() {}

	/**
	 * The constructor of the exception.
	 * 
	 * @param message a string defining a message for the exception.
	 */
	public ExistingIdException(String message) {
		super(message);
	}

	/**
	 * The constructor of the exception.
	 * 
	 * @param exception an exception related to the current exception.
	 */
	public ExistingIdException(Throwable exception) {
		super(exception);
	}

	/**
	 * The constructor of the exception.
	 * 
	 * @param message a string defining a message for the exception.
	 * @param exception an exception related to the current exception.
	 */
	public ExistingIdException(String message, Throwable exception) {
		super(message, exception);
	}

}
