package it.essepuntato.earmark.core;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * This class represents a abstract range in the EARMARK model.
 * 
 * @author Silvio Peroni
 *
 */
public abstract class Range implements EARMARKItem, EARMARKChildNode {
	private Docuverse docuverse;
	private Object beginLocation;
	private Object endLocation;
	private EARMARKDocument document;
	private URI id;
	
	/**
	 * Create a range.
	 * 
	 * @param d the document to which this item is associated.
	 * @param docuverse the docuverse the range refers to.
	 * @param begin the begin location of the range.
	 * @param end the end location of the range. 
	 * @param id the identifier for the new range.
	 */
	protected Range(EARMARKDocument d, Docuverse docuverse, Object begin, Object end, URI id) {
		document = d;
		beginLocation = begin;
		endLocation = end;
		this.id = id;
		this.docuverse = docuverse;
	}
	
	/**
	 * The location in which the range begins.
	 * 
	 * @return the begin location.
	 */
	public Object begins() {
		return beginLocation;
	}
	
	/**
	 * The location in which the range ends.
	 * 
	 * @return the end location.
	 */
	public Object ends() {
		return endLocation;
	}
	
	/**
	 * The docuverse the range refers to.
	 * 
	 * @return the docuverse concerning the range.
	 */
	public Docuverse refersTo() {
		return docuverse;
	}

	@Override
	public URI hasId() {
		return id;
	}
	
	@Override
	public String hasLocalId() {
		String result = null;
		
		String[] curIdString = id.toString().split("(/|#)");
		for (int i = curIdString.length - 1; i >= 0 && result == null; i--) {
			String current = curIdString[i];
			if (!current.equals("")) {
				result = current;
			}
		}
		
		return result;
	}
	
	/**
	 * This method changes the local id of the range with a new one. If the new id
	 * specified is equal to EARMARKDocument.reservedDocumentId, no change is performed.
	 * 
	 * @param newId the new local id for the range.
	 */
	protected void setId(URI newId) {
		URI tmp = id;
		id = newId;
		if (!document.setId(this, newId)) {
			id = tmp;
		}
	}
	
	/**
	 * This method sets the EARMARK document associated to this range.
	 * 
	 * @param d the EARMARK document related to the range.
	 */
	protected void setEARMARKDocument(EARMARKDocument d) {
		document = d;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Range other = (Range) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (!hasId().equals(other.hasId()))
			return false;
		return true;
	}

	/**
	 * As for the method clone, this method returns a new range 
	 * initialised with an identifier different from the calling range one.
	 * Moreover, the cloned range will refer to a new docuverse 
	 * containing only the content returned from the
	 * calling range by the method getTextContent.
	 * 
	 * @param deep this parameter does not change the behaviour of this method, i.e., it is
	 * always called the method clone.
	 * @return the cloned range.
	 */
	public EARMARKNode cloneNode(boolean deep) {
		return clone();
	}

	@Override
	public EARMARKChildNode getNextSibling() {
		return document.getNextSibling(this);
	}

	@Override
	public Set<EARMARKChildNode> getNextSiblings() {
		return document.getNextSiblings(this);
	}

	@Override
	public EARMARKDocument getOwnerDocument() {
		return document;
	}

	@Override
	public EARMARKHierarchicalNode getParentNode() {
		return document.getParentNode(this);
	}

	@Override
	public Set<EARMARKHierarchicalNode> getParentNodes() {
		return document.getParentNodes(this);
	}

	@Override
	public EARMARKChildNode getPreviousSibling() {
		return document.getPreviousSibling(this);
	}

	@Override
	public Set<EARMARKChildNode> getPreviousSiblings() {
		return document.getPreviousSiblings(this);
	}

	@Override
	public Object getUserData(String key) {
		return document.getUserData(this, key);
	}

	@Override
	public boolean isSameNode(EARMARKNode node) {
		return document.isSameNode(node, this);
	}

	@Override
	public void setUserData(String key, Object data) {
		document.setUserData(key, data, this);
	}

	@Override
	public Statement assertsAsObject(Resource subject, Property predicate) {
		return document.addAssertion(subject, predicate, this);
	}

	@Override
	public Statement assertsAsObject(EARMARKItem subject, Property predicate) {
		return document.addAssertion(subject, predicate, this);
	}

	@Override
	public Statement assertsAsSubject(Property predicate, RDFNode object) {
		return document.addAssertion(this, predicate, object);
	}

	@Override
	public Statement assertsAsSubject(Property predicate, EARMARKItem object) {
		return document.addAssertion(this, predicate, object);
	}

	@Override
	public Set<Statement> getAssertionsAsObject() {
		return document.getAssertionsAsObject(this);
	}

	@Override
	public Set<Statement> getAssertionsAsSubject() {
		return document.getAssertionsAsSubject(this);
	}

	@Override
	public Set<Statement> removeAllAssertions() {
		return document.removeAllAssertions(this);
	}
	
	/**
	 * <p>This method checks if the range item is equal to another node.</p>
	 * <p>A range is equal to another node either if they are the same node (isSameNode) or if they are
	 * both ranges containing the same textual content.</p>
	 * 
	 * @param node the node to compare equality with.
	 * @return true if the nodes are equal, false otherwise.
	 */
	public boolean isEqualNode(EARMARKNode node) {
		return isEqualNode(node, true);
	}
	
	/**
	 * <p>This method checks if the range item is structurally equivalent to another node.</p>
	 * <p>A range is structurally equivalent to another node either if they are
	 * both ranges containing the same textual content.</p>
	 * 
	 * @param node the node to compare equality with.
	 * @return true if the nodes are equal, false otherwise.
	 */
	public boolean isStructurallyEqualNode(EARMARKNode node) {
		return isEqualNode(node, false);
	}
	
	private boolean isEqualNode(EARMARKNode node, boolean lookForSameness) {
		boolean result = true;
		
		try {
			if (!lookForSameness || !isSameNode(node)) {
				result &= (node instanceof Range) & 
					getTextContent().equals(node.getTextContent()); 
				
			}
		} catch (NullPointerException e) {
			result = false;
		}
		
		return result;
	}
	
	protected String getDocuverseContent() {
		String result = "";
		
		Docuverse doc = refersTo();
		
		if (doc.getType() == Docuverse.Type.StringDocuverse) { /* It refers to a string docuverse */
			result = (String) doc.hasContent();
		} else { /* It refers to a URI docuverse */
			try {
				result = ((URIDocuverse) doc).getCache();
			} catch (IOException e1) {
				/* Do nothing: problem in loading the file from URL */
			}
		}
		
		return result;
	}
	
	/**
	 * This method returns a new range initialised with an identifier different from the calling range one.
	 * Moreover, the cloned range will refer to a new docuverse containing only the content returned from the
	 * calling range by the method getTextContent.
	 * 
	 * @return a new range with the same textual content of the calling one.
	 */
	public abstract Range clone();
	
	@Override
	public String toString() {
		String begin = (begins() == null ? "U" : begins().toString());
		String end = (ends() == null ? "U" : ends().toString());
		return "# " + getTextContent() + "\n" + hasId() + " [" + begin + "-" + end + "] -> " + refersTo().hasLocalId();
	}
	
	@Override
	public Resource addLinguisticAct(Resource reference, Resource meaning,
			Resource agent) {
		return document.addLinguisticAct(null, this, reference, meaning, agent);
	}

	@Override
	public Resource addLinguisticAct(URI uri, Resource reference,
			Resource meaning, Resource agent) {
		return document.addLinguisticAct(uri, this, reference, meaning, agent);
	}

	@Override
	public Set<Statement> removeAllLinguisticActs() {
		return document.removeAllLinguisticActs(this);
	}

	@Override
	public Set<Statement> removeLinguisticAct(Resource linguisticAct) {
		return document.removeLinguisticAct(this, linguisticAct);
	}
}
