package it.essepuntato.earmark.core;

/**
 * This interface describes collection of collectable items.
 * 
 * @author Silvio Peroni
 *
 */
public interface Collection extends java.util.Collection<EARMARKChildNode> , Cloneable {
	/**
	 * An enumeration of all the possible kinds of collections
	 * that relates to EARMARK markup items
	 * 
	 * @author Silvio Peroni
	 *
	 */
	public enum Type {
		/**
		 * This variable is used to identify a collectable item
		 * as a list.
		 */
		List ,
		
		/**
		 * This variable is used to identify a collectable item
		 * as a bag.
		 */
		Bag ,
		
		/**
		 * This variable is used to identify a collectable item
		 * as a set.
		 */
		Set
	}
	
	/**
	 * This method returns the collection type of this markup item. Possible values:
	 * List, Bag, Set.
	 * 
	 * @return the colletion type of this markup item.
	 */
	public Collection.Type getCollectionType();
	
	/**
	 * This method replaces a node in a collection with a new one.
	 * 
	 * @param newNode the new node
	 * @param oldNode the node to be replaced
	 * @return 'true' if the replacement operation was completed, 'false' otherwise
	 */
	public boolean replace(EARMARKChildNode newNode, EARMARKChildNode oldNode);
	
	/**
	 * This method allows to clone the collection.
	 * 
	 * @return a new clone of this collection.
	 * */
	public Collection clone();
}
