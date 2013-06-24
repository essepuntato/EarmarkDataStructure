package it.essepuntato.earmark.core;

import java.util.Set;

public interface EARMARKChildNode extends EARMARKNode {
	/**
	 * The parent of this node. All nodes, except EARMARKNode.Type.Document may have a parent.
	 * If a node has more than one parent, it returns one randomly.
	 * However, if a node has just been created and not yet added to the graph, 
	 * or if it has been removed from the graph, this method returns null.
	 * 
	 * @return a parent of this node.
	 */
	public EARMARKHierarchicalNode getParentNode();
	
	/**
	 * This method returns the parents of this node. All nodes, except EARMARKNode.Type.Document 
	 * may have a parent. However, if a node has just been created and not yet 
	 * added to set graph, or if it has been removed from the graph, this method returns an
	 * empty list.
	 * 
	 * @return all the parents of this node.
	 */
	public Set<EARMARKHierarchicalNode> getParentNodes();
	
	/**
	 * This method returns the next siblings of the node. If more than one siblings
	 * exist, the method chooses one randomly from the next sibling list. Though in a common 
	 * XML document there exists one possible next sibling only, in EARMARK it is 
	 * possible to have more than one sibling, because of different hierarchies and 
	 * multiple references.
	 *  
	 * @return a next-sibling of the node or null if it does not exist.
	 */
	public EARMARKChildNode getNextSibling();
	
	/**
	 * This method returns all the next siblings of the node. Though in a common XML
	 * document there exists one possible next sibling only, in EARMARK it is possible
	 * to have more than one sibling, because of different hierarchies and multiple
	 * references.
	 *  
	 * @return a set containing all the next-siblings of the node.
	 */
	public Set<EARMARKChildNode> getNextSiblings();
	
	/**
	 * This method returns the previous siblings of the node. If more than one siblings
	 * exist, the method chooses one randomly from the previous sibling list. Though in a common 
	 * XML document there exists one possible previous sibling only, in EARMARK it is 
	 * possible to have more than one sibling, because of different hierarchies and 
	 * multiple references.
	 *  
	 * @return a previous-sibling of the node.
	 */
	public EARMARKChildNode getPreviousSibling();
	
	/**
	 * This method returns all the previous siblings of the node. Though in a common XML
	 * document there exists one possible previous sibling only, in EARMARK it is possible
	 * to have more than one sibling, because of different hierarchies and multiple
	 * references.
	 *  
	 * @return a set containing all the previous-siblings of the node.
	 */
	public Set<EARMARKChildNode> getPreviousSiblings();
}
