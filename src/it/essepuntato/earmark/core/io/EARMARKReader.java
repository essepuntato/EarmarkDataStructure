package it.essepuntato.earmark.core.io;

import it.essepuntato.earmark.core.EARMARKDocument;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * This interface defines methods for reading various representations of an EARMARK document, translating
 * them in a proper Java object.
 * 
 * @author Silvio Peroni
 *
 */
public interface EARMARKReader extends EARMARKIOLogger {
	/**
	 * Get an EARMARK document from an URL.
	 * 
	 * @param url the URL containing a representation of the EARMARK document to retrieve.
	 * @return an EARMARK document.
	 */
	public EARMARKDocument read(URL url);
	
	/**
	 * Get an EARMARK document from a file.
	 * 
	 * @param file the file containing a representation of the EARMARK document to retrieve.
	 * @return an EARMARK document.
	 * @throws FileNotFoundException if the file does not exist.
	 */
	public EARMARKDocument read(File file) throws FileNotFoundException;
	
	/**
	 * Get an EARMARK document from a string.
	 * 
	 * @param source the source string containing a representation of the EARMARK document to retrieve.
	 * @return an EARMARK document.
	 */
	public EARMARKDocument read(String source);
	
	/**
	 * Get an EARMARK document from a Jena model.
	 * 
	 * @param model the Jena model containing a representation of the EARMARK document to retrieve.
	 * @return an EARMARK document.
	 */
	public EARMARKDocument read(Model model);
}
