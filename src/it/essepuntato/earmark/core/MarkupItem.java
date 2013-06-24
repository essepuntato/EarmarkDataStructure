package it.essepuntato.earmark.core;

import it.essepuntato.earmark.core.exception.EARMARKGraphException;

import java.net.URI;
import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * This class represents an abstract markup item in the EARMARK model.
 * 
 * @author Silvio Peroni
 *
 */
public abstract class MarkupItem 
implements EARMARKItem, EARMARKHierarchicalNode, EARMARKChildNode {
	private String gi = null;
	private URI ns = null;
	private EARMARKDocument document;
	private URI id;
	private Collection.Type type = Collection.Type.List;
	
	/**
	 * This method returns the general identifier of this markup item.
	 * 
	 * @return the general identifier associated to this markup item. 
	 */
	public String hasGeneralIdentifier() {
		return gi;
	}
	
	/**
	 * This method returns the namespace of this markup item.
	 * 
	 * @return the namespace associated to this markup item.
	 */
	public URI hasNamespace() {
		return ns;
	}
	
	/**
	 * This method sets the general identifier associated to this markup item.
	 * 
	 * @param g the general identifier associated to.
	 */
	protected void setGeneralIdentifier(String g) {
		gi = g;
	}
	
	/**
	 * This method sets the namespace associated to this markup item.
	 * 
	 * @param n the namespace associated to.
	 */
	protected void setNamespace(URI n) {
		ns = n;
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
	 * <p>This method changes the id of the markup item with a new one. If the new id
	 * specified is equal to EARMARKDocument.reservedDocumentId, no change is performed.
	 * <p>Each modification to the id is not propagated
	 * to all the structures involved and, consequently, may result in wrong behaviours and/or
	 * collateral effects.</p>
	 * <p>It is usually reserved for internal use only.</p>
	 * 
	 * @param newId the new local id for the markup item.
	 */
	protected void setId(URI newId) {
		URI tmp = id;
		id = newId;
		if (!document.setId(this, newId)) {
			id = tmp;
		}
	}
	
	/**
	 * <p>This method sets the EARMARK document associated to this markup item.</p>
	 * <p>Each modification to the id is not propagated
	 * to all the structures involved and, consequently, may result in wrong behaviours and/or
	 * collateral effects.</p>
	 * <p>It is usually reserved for internal use only.</p>
	 * 
	 * @param d the EARMARK document related to the markup item.
	 */
	protected void setEARMARKDocument(EARMARKDocument d) {
		document = d;
	}
	
	/**
	 * <p>This method sets the EARMARK node as the type specified.</p>
	 * <p>Each modification to the id is not propagated
	 * to all the structures involved and, consequently, may result in wrong behaviours and/or
	 * collateral effects.</p>
	 * <p>It is usually reserved for internal use only.</p>
	 * @param t the type to be set.
	 */
	protected void setType(Collection.Type t) {
		type = t;
	}
	
	
	@Override
	public boolean isList() {
		return type == Collection.Type.List;
	}
	
	@Override
	public boolean isBag() {
		return type == Collection.Type.Bag;
	}
	
	@Override
	public boolean isSet() {
		return type == Collection.Type.Set;
	}
	
	/**
	 * This method returns the type of the container of this node.
	 * 
	 * @return the container type between SEQUENCE_TYPE and BAG_TYPE.
	 */
	public Collection.Type getContainerType() {
		return type;
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
		MarkupItem other = (MarkupItem) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (!hasId().equals(other.hasId()))
			return false;
		return true;
	}
	
	@Override
	public EARMARKChildNode appendChild(EARMARKChildNode newChild)
			throws EARMARKGraphException {
		return document.appendChild(newChild, this);
	}

	/**
	 * This method clones the markup item.
	 * 
	 * @param deep if false, it corresponds to call the method clone(); if true, the markup item will be
	 * cloned using the method clone() and then all the other child nodes of it will be
	 * cloned (cloneNode) as well and appended as child of the new cloned markup item.
	 * @return the cloned markup item.
	 */
	public EARMARKNode cloneNode(boolean deep) {
		MarkupItem result = clone();
		
		if (deep) {
			Iterator<EARMARKChildNode> ite = getChildNodes().iterator();
			while (ite.hasNext()) {
				EARMARKChildNode child = (EARMARKChildNode) ite.next().cloneNode(true);
				result.appendChild(child);
			}
		}
		
		return result;
	}

	@Override
	public Collection getAttributes() {
		return document.getAttributes(this);
	}

	@Override
	public Collection getChildElements() {
		return document.getChildElements(this);
	}

	@Override
	public Collection getChildNodes() {
		return document.getChildNodes(this);
	}

	@Override
	public Collection getComments() {
		return document.getComments(this);
	}

	@Override
	public EARMARKChildNode getFirstChild() {
		return document.getFirstChild(this);
	}

	@Override
	public EARMARKChildNode getLastChild() {
		return document.getLastChild(this);
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
	public String getTextContent() {
		return document.getTextContent(this);
	}

	@Override
	public Object getUserData(String key) {
		return document.getUserData(this, key);
	}

	@Override
	public boolean hasAttribute() {
		return document.hasAttribute(this);
	}

	@Override
	public boolean hasChildNodes() {
		return document.hasChildNodes();
	}

	@Override
	public boolean hasElementNodes() {
		return document.hasElementNodes(this);
	}

	@Override
	public EARMARKChildNode insertBefore(EARMARKChildNode newChild, EARMARKChildNode refChild)
			throws EARMARKGraphException {
		return document.insertBefore(newChild, refChild, this);
	}

	@Override
	public EARMARKChildNode insertBefore(EARMARKChildNode newChild, EARMARKChildNode refChild,
			int occurrence) throws EARMARKGraphException {
		return document.insertBefore(newChild, refChild, occurrence, this);
	}

	/**
	 * <p>This method checks if the markup item is equal to another node.</p>
	 * <p>A markup item is equal to another node either if they are the same node (isSameNode) or if they are
	 * both markup items of the same type having same general identifier and namespace,
	 * and their structures are equal (i.e., they contains equal nodes in the same
	 * hierarchical order).</p>
	 * 
	 * @param node the node to compare equality with.
	 * @return true if the nodes are equal, false otherwise.
	 */
	public boolean isEqualNode(EARMARKNode node) {
		return isEqualNode(node, true);

	}
	
	/* Used for checking the equality either hierarchical (lookForSameness = false) or
	 * general (lookForSameness = true) */
	private boolean isEqualNode(EARMARKNode node, boolean lookForSameness) {
		boolean result = true;
		
		try {
			if (!lookForSameness || !isSameNode(node)) {
				result &= document.hasBasicEquality(this, node) & 
					hasSameNameAndNamespace(this, (MarkupItem) node) &
					document.hasHierarchicalEquality(this, (EARMARKHierarchicalNode) node, lookForSameness);
			}
		} catch (Exception e) {
			result = false;
		}
		
		return result;
	}
	
	/**
	 * <p>This method checks if the markup item is structurally equivalent to another node.</p>
	 * <p>A markup item is structurally equivalent to another node either if they are
	 * both markup items of the same type having same general identifier and namespace,
	 * and their structures are structurally equivalent 
	 * (i.e., they contains structurally equivalent nodes in the same hierarchical order).</p>
	 * 
	 * @param node the node to compare equality with.
	 * @return true if the nodes are equal, false otherwise.
	 */
	public boolean isStructurallyEqualNode(EARMARKNode node) {
		return isEqualNode(node, false);
	}
	
	/* Check if two named nodes have the same local name and namespace. */
	private boolean hasSameNameAndNamespace(MarkupItem node1, MarkupItem node2) {
		String n1 = node1.hasGeneralIdentifier();
		String n2 = node2.hasGeneralIdentifier();
		URI u1 = node1.hasNamespace();
		URI u2 = node2.hasNamespace();
		
		return ((n1 == null && n2 == null) || (n1 != null && n2 != null && n1.equals(n2))) && 
			((u1 == null && u2 == null) || (u1 != null && u2 != null && u1.equals(u2)));
	}

	@Override
	public boolean isSameNode(EARMARKNode node) {
		return document.isSameNode(node, this);
	}

	@Override
	public EARMARKChildNode removeAllChild(EARMARKChildNode oldChild) {
		return document.removeAllChild(oldChild, this);
	}

	@Override
	public EARMARKChildNode removeChild(EARMARKChildNode oldChild)
			throws EARMARKGraphException {
		return document.removeChild(oldChild, this);
	}

	@Override
	public EARMARKChildNode removeChildByOccurrence(EARMARKChildNode oldChild,
			int occurrence) {
		return document.removeChildByOccurrence(oldChild, occurrence, this);
	}

	@Override
	public EARMARKChildNode replaceAllChild(EARMARKChildNode newChild,
			EARMARKChildNode oldChild) throws EARMARKGraphException {
		return document.replaceAllChild(newChild, oldChild, this);
	}

	@Override
	public EARMARKChildNode replaceChild(EARMARKChildNode newChild, EARMARKChildNode oldChild)
			throws EARMARKGraphException {
		return document.replaceChild(newChild, oldChild, this);
	}

	@Override
	public EARMARKChildNode replaceChildByOccurrence(EARMARKChildNode newChild,
			EARMARKChildNode oldChild, int occurrence) throws EARMARKGraphException {
		return document.replaceChildByOccurrence(newChild, oldChild, occurrence, this);
	}

	@Override
	public void setUserData(String key, Object data) {
		document.setUserData(key, data, this);
	}
	
//	@Override
//	public OWLIndividual accept(FromEARMARKDocumentToOWL visitor) {
//		return (OWLIndividual) visitor.visit(this);
//	}
	


	@Override
	public Statement assertsAsObject(Resource subject, Property predicate) {
		return getOwnerDocument().addAssertion(subject, predicate, this);
	}

	@Override
	public Statement assertsAsObject(EARMARKItem subject, Property predicate) {
		return getOwnerDocument().addAssertion(subject, predicate, this);
	}

	@Override
	public Statement assertsAsSubject(Property predicate, RDFNode object) {
		return getOwnerDocument().addAssertion(this, predicate, object);
	}

	@Override
	public Statement assertsAsSubject(Property predicate, EARMARKItem object) {
		return getOwnerDocument().addAssertion(this, predicate, object);
	}

	@Override
	public Set<Statement> getAssertionsAsObject() {
		return getOwnerDocument().getAssertionsAsObject(this);
	}

	@Override
	public Set<Statement> getAssertionsAsSubject() {
		return getOwnerDocument().getAssertionsAsSubject(this);
	}

	@Override
	public Set<Statement> removeAllAssertions() {
		return getOwnerDocument().removeAllAssertions(this);
	}
	
	/**
	 * This method returns a new markup item initialised with an identifier different from the calling node one.
	 * 
	 * @return a new empty markup item.
	 */
	public abstract MarkupItem clone();
	
	/**
	 * Give a string representation of the markup item in an XML/XPath-like form.
	 */
	public String toString() {
		String id = (hasId() == null ? "" : " xml:id=\"" + hasId() + "\"");
		String gi = (hasGeneralIdentifier() == null ? "" : hasGeneralIdentifier());
		String ns = (hasNamespace() == null ? "" : " xmlns=\"" + hasNamespace() + "\"");
		return gi + ns + id;
	}
}
