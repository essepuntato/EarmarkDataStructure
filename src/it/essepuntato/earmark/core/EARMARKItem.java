package it.essepuntato.earmark.core;

import java.net.URI;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * This interface defines the general methods that all the EARMARK items have to implement. 
 * 
 * @author Silvio Peroni
 *
 */
public interface EARMARKItem extends Cloneable {
	/**
	 * This method returns the absolute URI that identifies the EARMARK item. 
	 * @return a URI representing the absolute identifier.
	 */
	public URI hasId();
	
	/**
	 * This method returns the local name that identifies the EARMARK item.
	 * @return a string representing the local name of the identifier.
	 */
	public String hasLocalId();
	
	/**
	 * This method allows us to add a new RDF statement, using this item
	 * as object.
	 * 
	 * @param subject the resource subject of the statement.
	 * @param predicate the property predicate of the statement.
	 * @return the RDF statement asserted.
	 */
	public Statement assertsAsObject(Resource subject, Property predicate);
	
	/**
	 * This method allows us to add a new RDF statement, using this item
	 * as object.
	 * 
	 * @param subject the EARMARK item subject of the statement.
	 * @param predicate the property predicate of the statement.
	 * @return the RDF statement asserted.
	 */
	public Statement assertsAsObject(EARMARKItem subject, Property predicate);
	
	/**
	 * This method allows us to add a new RDF statement, using this item
	 * as subject.
	 * 
	 * @param predicate the property predicate of the statement.
	 * @param object the resource object of the statement.
	 * @return the RDF statement asserted.
	 */
	public Statement assertsAsSubject(Property predicate, RDFNode object);
	
	/**
	 * This method allows us to add a new RDF statement, using this item
	 * as subject.
	 * 
	 * @param predicate the property predicate of the statement.
	 * @param object the EARMARK item object of the statement.
	 * @return the RDF statement asserted.
	 */
	public Statement assertsAsSubject(Property predicate, EARMARKItem object);
	
	/**
	 * This method returns a set with all the statements in the model
	 *  having this item as subject.
	 * 
	 * @return all the statements having this item as subject.
	 */
	public java.util.Set<Statement> getAssertionsAsSubject();
	
	/**
	 * This method returns a set with all the statements in the model
	 * having this item as object.
	 * 
	 * @return all the statements having this item as object.
	 */
	public java.util.Set<Statement> getAssertionsAsObject();
	
	/**
	 * This method removes all the assertions having this item as
	 * subject or object.
	 * 
	 * @return the statements removed.
	 */
	public java.util.Set<Statement> removeAllAssertions();
}
