package it.essepuntato.earmark.core.io;

import java.util.logging.Logger;

/**
 * An interface for recording issues and messages from input/output classes and methods.
 * 
 * @author Silvio Peroni
 *
 */
public interface EARMARKIOLogger {
	/**
	 * Get a plain text description of the log.
	 * @return a string containing the log description.
	 */
	public String getPlainText();
	
	/**
	 * Get an XML description of the log.
	 * @return an XML string containing the log description.
	 */
	public String getXML();
	
	/**
	 * Get a logger related to a specific class.
	 * 
	 * @param c the class related to the logger.
	 * @return a logger according to the class it refers to.
	 */
	public Logger getLogger(Class<?> c);
}
