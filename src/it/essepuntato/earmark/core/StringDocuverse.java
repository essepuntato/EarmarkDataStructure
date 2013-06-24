package it.essepuntato.earmark.core;

import java.net.URI;

/**
 * This class represents a concrete string docuverse in the EARMARK model.
 * 
 * @author Silvio Peroni
 *
 */
public class StringDocuverse extends Docuverse {
	/**
	 * Create a string docuverse.
	 * 
	 * @param d the document to which this item is associated.
	 * @param t the string content of the new docuverse.
	 * @param id the identifier for the new docuverse.
	 */
	protected StringDocuverse(EARMARKDocument d, String t, URI id) {
		super(d,t,id);
	}
	
	/**
	 * This method returns the content as a string.
	 * 
	 * @return the string representing the content of the docuverse.
	 */
	@Override
	public String hasContent() {
		return (String) super.hasContent();
	}
	
	/**
	 * This method says the type of this docuverse.
	 * 
	 * @return the item Docuverse.Type.StringDocuverse;
	 */
	public Docuverse.Type getType() {
		return Docuverse.Type.StringDocuverse;
	}
	
	@Override
	public StringDocuverse clone() {
		return getEARMARKDocument().createStringDocuverse(hasContent());
	}
	
	@Override
	public String toString() {
		return super.toString() + " = '" + hasContent() + "'";
	}
}
