package it.essepuntato.earmark.core;

import java.util.HashSet;

/**
 * This class represents a set, i.e., a non-ordered collection that does not allow repetitions. 
 * 
 * @author Silvio Peroni
 *
 */
@SuppressWarnings("serial")
public class Set extends HashSet<EARMARKChildNode> implements Collection {

	@Override
	public Collection.Type getCollectionType() {
		return Collection.Type.Set;
	}
	
	@Override
	public boolean replace(EARMARKChildNode newNode, EARMARKChildNode oldNode) {
		boolean result = remove(oldNode);
		if (result) {
			add(newNode);
		}
		
		return result;
	}
	
	@Override
	public Set clone() {
		return (Set) super.clone();
	}
}
