package it.essepuntato.earmark.core;

import java.util.ArrayList;

/**
 * This class represents a bag, i.e., a non-ordered collection that allows repetitions. 
 * 
 * @author Silvio Peroni
 *
 */
@SuppressWarnings("serial")
public class Bag extends ArrayList<EARMARKChildNode> implements Collection {

	@Override
	public Collection.Type getCollectionType() {
		return Collection.Type.Bag;
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
	public Bag clone() {
		return (Bag) super.clone();
	}
}
