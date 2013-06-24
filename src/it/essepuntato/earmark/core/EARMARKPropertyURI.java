package it.essepuntato.earmark.core;

import java.net.URI;

/**
 * This interface defines the URI for each EARMARK property.
 * 
 * @author Silvio Peroni 
 *
 */
public interface EARMARKPropertyURI {
	
	/* ### DATATYPE PROPERTY ### */
	
	public static final URI HAS_CONTENT = 
		URI.create("http://www.essepuntato.it/2008/12/earmark#hasContent");
	public static final URI HAS_GENERAL_IDENTIFIER = 
		URI.create("http://www.essepuntato.it/2008/12/earmark#hasGeneralIdentifier");
	public static final URI HAS_NAMESPACE = 
		URI.create("http://www.essepuntato.it/2008/12/earmark#hasNamespace");
	public static final URI BEGINS = 
		URI.create("http://www.essepuntato.it/2008/12/earmark#begins");
	public static final URI ENDS = 
		URI.create("http://www.essepuntato.it/2008/12/earmark#ends");
	public static final URI HAS_XPATH_CONTEXT =
		URI.create("http://www.essepuntato.it/2008/12/earmark#hasXPathContext");
	
	/* ### end of DATATYPE PROPERTY ### */
	
	/* ### OBJECT PROPERTY ### */
	
	public static final URI REFERS_TO = 
		URI.create("http://www.essepuntato.it/2008/12/earmark#refersTo");
	
	/* ### end of OBJECT PROPERTY ### */
	
	
	/* COLLECTION OBJECT PROPERTY */
	
	public static final URI ELEMENT = 
		URI.create("http://swan.mindinformatics.org/ontologies/1.2/collections/element");
	public static final URI ITEM = 
		URI.create("http://swan.mindinformatics.org/ontologies/1.2/collections/item");
	public static final URI FIRST_ITEM = 
		URI.create("http://swan.mindinformatics.org/ontologies/1.2/collections/firstItem");
	public static final URI LAST_ITEM = 
		URI.create("http://swan.mindinformatics.org/ontologies/1.2/collections/lastItem");
	public static final URI FOLLOWED_BY = 
		URI.create("http://swan.mindinformatics.org/ontologies/1.2/collections/followedBy");
	public static final URI NEXT_ITEM = 
		URI.create("http://swan.mindinformatics.org/ontologies/1.2/collections/nextItem");
	public static final URI ITEM_CONTENT = 
		URI.create("http://swan.mindinformatics.org/ontologies/1.2/collections/itemContent");
	public static final URI PRECEEDED_BY = 
		URI.create("http://swan.mindinformatics.org/ontologies/1.2/collections/precededBy");
	public static final URI PREVIOUS_ITEM = 
		URI.create("http://swan.mindinformatics.org/ontologies/1.2/collections/previousItem");
		
	/* end of COLLECTION OBJECT PROPERTY */
	
	/* COLLECTION DATA PROPERTY */
	
	public static final URI SIZE = 
		URI.create("http://swan.mindinformatics.org/ontologies/1.2/collections/size");
		
	/* end of COLLECTION DATA PROPERTY */
}
