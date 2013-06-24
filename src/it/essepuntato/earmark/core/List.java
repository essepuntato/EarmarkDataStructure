package it.essepuntato.earmark.core;

@SuppressWarnings("serial")
/**
 * This class represents a list, i.e., an ordered collection that allows repetitions.
 * 
 * @author Silvio Peroni
 */
public class List extends Bag implements Collection  {

	@Override
	public Collection.Type getCollectionType() {
		return Collection.Type.List;
	}
	
	@Override
	public boolean replace(EARMARKChildNode newNode, EARMARKChildNode oldNode) {
		int index = indexOf(oldNode);
		boolean result = false;
		
		if (index >= 0) {
			remove(index);
			add(index, newNode);
			result = true;
		}
		
		return result;
	}
}
