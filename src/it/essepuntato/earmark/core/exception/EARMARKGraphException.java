package it.essepuntato.earmark.core.exception;

/**
 * This exception is thrown if some run-time problems happens
 * when applying operations concerning the structure of the document.  
 * 
 * @author Silvio Peroni
 */
public class EARMARKGraphException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private Type currentType;
	
	/**
	 * This enumeration defines all the possible type of runtime errors.
	 * */
	public enum Type {
		/**
		 * It is used when the item specified is created using a different document.
		 */
		WRONG_DOCUMENT_ERR ,
		
		/**
		 * It is used when we try to append an EARMARK document to another one.
		 */
		DOCUMENT_ERR ,
		
		/**
		 * It is used when we specify as child a node that is not a child of it actually.
		 */
		NOCHILD_ERR ,
		
		/**
		 * It is used when an operation is not permitted on ranges.
		 */
		RANGE_ERR ,
		
		/**
		 * It is used when an internal misconfiguration happens
		 */
		INTERNAL_MISCONFIGURATION
	}
	
	/**
	 * The constructor of the exception.
	 * 
	 * @param type the type of the exception, chosen among those defined above. 
	 * @param message a string defining a message for the exception.
	 */
	public EARMARKGraphException(Type type, String message) {
		super(message);
		currentType = type;
	}
	
	/**
	 * This method returns the current type of the exception.
	 * 
	 * @return the type of the exception.
	 */
	public Type getType() {
		return currentType;
	}
	
	/**
	 * It says whether the exception concerns a wrong document error.
	 * 
	 * @return true if it concerns a wrong document error, false otherwise.
	 */
	public boolean isWrongDocumentError() {
		return Type.WRONG_DOCUMENT_ERR == currentType;
	}
	
	/**
	 * It says whether the exception concerns a document error.
	 * 
	 * @return true if it concerns a document error, false otherwise.
	 */
	public boolean isDocumentError() {
		return Type.DOCUMENT_ERR == currentType;
	}
	
	/**
	 * It says whether the exception concerns a no child error.
	 * 
	 * @return true if it concerns a no child, false otherwise.
	 */
	public boolean isNoChildError() {
		return Type.NOCHILD_ERR == currentType;
	}
	
	/**
	 * It says whether the exception concerns a range error.
	 * 
	 * @return true if it concerns a range, false otherwise.
	 */
	public boolean isRangeError() {
		return Type.RANGE_ERR == currentType;
	}
	
	/**
	 * It says whether the exception concerns an internal misconfiguration error.
	 * 
	 * @return true if it concerns an internal misconfiguration, false otherwise.
	 */
	public boolean isInternalMisconfigurationError() {
		return Type.INTERNAL_MISCONFIGURATION == currentType;
	}

}
