package it.essepuntato.earmark.core;

import java.net.URI;

/**
 * This interface defines the URI for each EARMARK class.
 * 
 * @author Silvio Peroni
 *
 */
public interface EARMARKClassURI {
	public static final URI RANGE = 
		URI.create(EARMARKOntologyURI.EARMARK.toString() + "#Range");
	public static final URI POINTER_RANGE = 
		URI.create(EARMARKOntologyURI.EARMARK.toString() + "#PointerRange");
	public static final URI XPATH_RANGE = 
		URI.create(EARMARKOntologyURI.EARMARK.toString() + "#XPathRange");
	public static final URI XPATH_POINTER_RANGE = 
		URI.create(EARMARKOntologyURI.EARMARK.toString() + "#XPathPointerRange");
	
	public static final URI MARKUP_ITEM = 
		URI.create(EARMARKOntologyURI.EARMARK.toString() + "#MarkupItem");
	public static final URI ELEMENT = 
		URI.create(EARMARKOntologyURI.EARMARK.toString() + "#Element");
	public static final URI ATTRIBUTE = 
		URI.create(EARMARKOntologyURI.EARMARK.toString() + "#Attribute");
	public static final URI COMMENT = 
		URI.create(EARMARKOntologyURI.EARMARK.toString() + "#Comment");
	
	public static final URI DOCUVERSE = 
		URI.create(EARMARKOntologyURI.EARMARK.toString() + "#Docuverse");
	public static final URI STRING_DOCUVERSE = 
		URI.create(EARMARKOntologyURI.EARMARK.toString() + "#StringDocuverse");
	public static final URI URI_DOCUVERSE = 
		URI.create(EARMARKOntologyURI.EARMARK.toString() + "#URIDocuverse");
	
	/* COLLECTION CLASS */
	
	public static final URI COLLECTION = 
		URI.create(EARMARKOntologyURI.COLLECTIONS.toString() + "/Collection");
	public static final URI BAG = 
		URI.create(EARMARKOntologyURI.COLLECTIONS.toString() + "/Bag");
	public static final URI LIST = 
		URI.create(EARMARKOntologyURI.COLLECTIONS.toString() + "/List");
	public static final URI SET = 
		URI.create(EARMARKOntologyURI.COLLECTIONS.toString() + "/Set");
	public static final URI ITEM = 
		URI.create(EARMARKOntologyURI.COLLECTIONS.toString() + "/Item");
	public static final URI LIST_ITEM = 
		URI.create(EARMARKOntologyURI.COLLECTIONS.toString() + "/ListItem");
		
	/* end of COLLECTION CLASS */
}
