package it.essepuntato.earmark.core;

import it.essepuntato.earmark.core.exception.EARMARKGraphException;

	public interface EARMARKHierarchicalNode extends EARMARKNode {
	/**
	 * This method return all the child elements of the node.
	 * 
	 * @return a collection containing all the child elements of the node.
	 */
	public Collection getChildElements();

	/**
	 * This method says whether the node is a collection containing elements.
	 * 
	 * @return true if the node sequence contain at least an element, false otherwise.
	 */
	public boolean hasElementNodes();
	
	/**
	 * Remove the all the child node indicated by oldChild from the collection of children of the node, 
	 * and returns it.
	 *  
	 * @param oldChild the node being removed. 
	 * @return the node removed.
	 * @throws EARMARKGraphException if [WRONG_DOCUMENT_ERR] oldChild was created from a
	 * different document than this one or if [NOCHILD_ERR] refChild is not a child of the node. 
	 */
	public EARMARKChildNode removeAllChild(EARMARKChildNode oldChild);
	
	/**
	 * Remove the child node by occurrence indicated by oldChild from the collection of children of the node, 
	 * and returns it.
	 *  
	 * @param oldChild the node being removed.
	 * @param occurrence the node occurrence to remove.
	 *  
	 * @return the node removed.
	 * @throws EARMARKGraphException if [WRONG_DOCUMENT_ERR] oldChild was created from a
	 * different document than this one or if [NOCHILD_ERR] refChild is not a child of the node. 
	 */
	public EARMARKChildNode removeChildByOccurrence(EARMARKChildNode oldChild, int occurrence);
	
	/**
	 * Replace the all child node oldChild with newChild in the collection of children of the node, 
	 * and returns the oldChild node. If the oldChild does not exist, it returns
	 * null. 
	 * 
	 * @param newChild the new node to put in the child collection.
	 * @param oldChild the node being replaced in the collection.
	 * @return the node replaced or null if it does not exist.
	 * @throws EARMARKGraphException if [WRONG_DOCUMENT_ERR] ewChild or refChild were created from a
	 * different document than this one or if [NOCHILD_ERR] refChild is not a child of the node.
	 */
	public EARMARKChildNode replaceAllChild(
			EARMARKChildNode newChild, EARMARKChildNode oldChild) 
	throws EARMARKGraphException;
	
	/**
	 * Replace the child node by occurrence indicated by oldChild from the list of children of 'node', 
	 * and returns it.
	 *  
	 * @param newChild the new node to put in the child collection.
	 * @param oldChild the node being replaced.
	 * @param occurrence the node occurrence to replace.
	 * @return the node replaced.
	 * @throws EARMARKGraphException if [WRONG_DOCUMENT_ERR] node, newChild or refChild were created from a
	 * different document than this one or if [NOCHILD_ERR] refChild is not a child of node.
	 */
	public EARMARKChildNode replaceChildByOccurrence(
			EARMARKChildNode newChild, EARMARKChildNode oldChild, int occurrence) 
	throws EARMARKGraphException;
	
	/**
	 * Add the node newChild to the end of the sequence of the node. 
	 * 
	 * @param newChild the node to add.
	 * @return the node added.
	 * @throws EARMARKGraphException if newChild was created from a different document than the 
	 * one that created this node.
	 */
	public EARMARKChildNode appendChild(EARMARKChildNode newChild) throws EARMARKGraphException;
	
	/**
	 * This method returns a collection that contains all children (elements, attributes, comments) of this node. 
	 * If there are no children, this is a collection containing no nodes.
	 * 
	 * @return the node children.
	 */
	public Collection getChildNodes();
	
	/**
	 * This method returns a collection that contains all the attributes of this node. 
	 * If there are no children, this is a collection containing no nodes.
	 * 
	 * @return the attribute children.
	 */
	public Collection getAttributes();
	
	/**
	 * This method returns a collection that contains all the comments of this node. 
	 * If there are no children, this is a collection containing no nodes.
	 * 
	 * @return the comment children.
	 */
	public Collection getComments();
	
	/**
	 * The first child of this node. If there is no such node, this returns null.
	 * If the current node is a bag or a set, a random node is returned.
	 *  
	 * @return the first node child.
	 */
	public EARMARKChildNode getFirstChild();
	
	/**
	 * The last child of this node. If there is no such node, this returns null.
	 * If the current node is a bag or a set, a random node is returned.
	 *  
	 * @return the last node child.
	 */
	public EARMARKChildNode getLastChild();
	
	/**
	 * Returns whether this node has any attributes.
	 * 
	 * @return true if this node has any attributes, false otherwise.
	 */
	public boolean hasAttribute();
	
	/**
	 * Returns whether this node has any children.
	 * 
	 * @return true if this node has any children, false otherwise.
	 */
	public boolean hasChildNodes();
	
	/**
	 * Inserts the node newChild before the first existing child node refChild of the node. 
	 * If refChild is null, insert newChild at the end of the list of children.
	 * This operation makes sense if and only if the EARMARK node is a markup item
	 * and its type is Collection.Type.Sequence.
	 * 
	 * @param newChild the node to insert.
	 * @param refChild the reference node, i.e., the node before which the 
	 * new node must be inserted. 
	 * @return the node being inserted.
	 * @throws EARMARKGraphException if [WRONG_DOCUMENT_ERR] newChild or refChild were created from a
	 * different document than this one or if [NOCHILD_ERR] refChild is not a child of the node.
	 */
	public EARMARKChildNode insertBefore(EARMARKChildNode newChild, EARMARKChildNode refChild) 
	throws EARMARKGraphException;
	
	/**
	 * Inserts the node newChild before the existing child node by 
	 * occurrence refChild of the node.
	 * If refChild is null, insert newChild at the end of the list of children.
	 * This operation makes sense if and only if the EARMARK node is a markup item
	 * and its type is Collection.Type.Sequence.
	 * 
	 * @param newChild the node to insert.
	 * @param refChild the reference node, i.e., the node before which the 
	 * new node must be inserted.
	 * @param occurrence the occurrence of the refChild to consider.
	 * @return the node being inserted.
	 * @throws EARMARKGraphException if [WRONG_DOCUMENT_ERR] newChild or refChild were created from a
	 * different document than this one or if [NOCHILD_ERR] refChild is not a child of the node. 
	 */
	public EARMARKChildNode insertBefore(
			EARMARKChildNode newChild, EARMARKChildNode refChild, int occurrence) 
	throws EARMARKGraphException;
	
	/**
	 * This method removes the first child node indicated by oldChild from the list of children of the node, 
	 * and returns it.
	 *  
	 * @param oldChild the node being removed. 
	 * @return the node removed.
	 * @throws EARMARKGraphException if [WRONG_DOCUMENT_ERR] oldChild was created from a
	 * different document than this one or if [NOCHILD_ERR] refChild is not a child of node. 
	 */
	public EARMARKChildNode removeChild(EARMARKChildNode oldChild) throws EARMARKGraphException;
	
	/**
	 * Replace the first child node oldChild with newChild in the collection of children of the node, 
	 * and returns the oldChild node. If oldChild does not exist, it returns
	 * null. 
	 * 
	 * @param newChild the new node to put in the child collection.
	 * @param oldChild the node being replaced in the collection.
	 * @return the node replaced or null if it does not exist.
	 * @throws EARMARKGraphException if [WRONG_DOCUMENT_ERR] node, newChild or oldChild were created from a
	 * different document than this one or if [NOCHILD_ERR] refChild is not a child of node.
	 */
	public EARMARKChildNode replaceChild(EARMARKChildNode newChild, EARMARKChildNode oldChild) 
	throws EARMARKGraphException;
	
	/**
	 * This method says whether the EARMARK node is a list or not.
	 * 
	 * @return true if the node is a list, false otherwise.
	 */
	public boolean isList();
	
	/**
	 * This method says whether the EARMARK node is a bag or not.
	 * 
	 * @return true if the node is a bag, false otherwise.
	 */
	public boolean isBag();
	
	/**
	 * This method says whether the EARMARK node is a set or not.
	 * 
	 * @return true if the node is a set, false otherwise.
	 */
	public boolean isSet();
}
