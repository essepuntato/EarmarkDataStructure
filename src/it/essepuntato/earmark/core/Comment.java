package it.essepuntato.earmark.core;

import java.net.URI;

/**
 * This class represents a concrete comment in the EARMARK model.
 * 
 * @author Silvio Peroni
 *
 */
public class Comment extends MarkupItem {

	/**
	 * Create a comment.
	 * 
	 * @param d the document to which this item is associated.
	 * @param gi the general identifier associated to this markup item.
	 * @param ns the namespace associated to this markup item.
	 * @param type type the collection type associated to this markup item. Possible values:
	 * must be Collection.Type.Set, Collection.Type.Bag and Collection.Type.List.
	 * @param id the identifier for this item.
	 */
	protected Comment(EARMARKDocument d, String gi, URI ns, Collection.Type type, URI id) {
		setEARMARKDocument(d);
		setGeneralIdentifier(gi);
		setNamespace(ns);
		setId(id);
		setType(type);
	}

	@Override
	public EARMARKNode.Type getNodeType() {
		return EARMARKNode.Type.Comment;
	}
	
	@Override
	public Comment clone() {
		return getOwnerDocument().createComment(hasGeneralIdentifier(), hasNamespace(), getContainerType());
	}
	
	@Override
	public String toString() {
		return "<!-- " + super.toString() + " -->";
	}
}
