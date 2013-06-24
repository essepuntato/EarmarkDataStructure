package it.essepuntato.earmark.core.io;

import it.essepuntato.earmark.core.EARMARKDocument;

import java.io.File;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * This interface defines methods for storing an EARMARK document into various representations.
 * 
 * @author Silvio Peroni
 *
 */
public interface EARMARKWriter extends EARMARKIOLogger {
	/* Output formats */
	public static final String RDFXML = "RDF/XML";
	public static final String RDFXMLABBREV = "RDF/XML-ABBREV";
	public static final String NTRIPLE = "N-TRIPLE";
	public static final String TURTLE = "TURTLE";
	public static final String TTL = "TTL";
	public static final String N3 = "N3";
	
	/**
	 * Store an EARMARK document as a string in a default format.
	 * 
	 * @param document the document to be stored.
	 * @return the string, in a default format, representing the document.
	 */
	public String write(EARMARKDocument document);
	
	/**
	 * Store an EARMARK document in a file, in a default format.
	 * 
	 * @param document the document to be stored.
	 * @param file the destination file.
	 * @return true if the file is correctly created, false otherwise.
	 */
	public boolean write(EARMARKDocument document, File file);
	
	/**
	 * Store an EARMARK document as a string in a particular format.
	 * 
	 * @param document the document to be stored.
	 * @param format the format in which the document will be store, to be chosen among those defined in
	 * this class.
	 * @return the string, in a particular format, representing the document.
	 */
	public String write(EARMARKDocument document, String format);
	
	/**
	 * Store an EARMARK document in a file, in a particular format.
	 * 
	 * @param document the document to be stored.
	 * @param file the destination file.
	 * @param format the format in which the document will be store, to be chosen among those defined in
	 * this class.
	 * @return true if the file is correctly created, false otherwise.
	 */
	public boolean write(EARMARKDocument document, File file, String format);
	
	/**
	 * Add a map of prefix-namespace map to the writer.
	 * 
	 * @param prefixNsPair a prefix-namespace map to be added.
	 */
	public void addPrefixes(Map<String, String> prefixNsPair);
	
	/**
	 * Store an EARMARK document as a Jena model.
	 * 
	 * @param document the document to be stored.
	 * @return the Jena model representing the document.
	 */
	public Model getModel(EARMARKDocument document);
}
