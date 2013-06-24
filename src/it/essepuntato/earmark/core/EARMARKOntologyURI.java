package it.essepuntato.earmark.core;

import java.net.URI;

/**
 * This interface defines the URI for each ontology used.
 * 
 * @author Silvio Peroni 
 *
 */
public interface EARMARKOntologyURI {
	public static final URI EARMARK = 
		URI.create("http://www.essepuntato.it/2008/12/earmark");
	public static final URI GHOST = 
		URI.create("http://www.essepuntato.it/2010/05/ghost");
	public static final URI COLLECTIONS = 
		URI.create("http://swan.mindinformatics.org/ontologies/1.2/collections");
}
