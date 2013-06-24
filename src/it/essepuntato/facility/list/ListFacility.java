package it.essepuntato.facility.list;

import java.util.ArrayList;
import java.util.List;

/**
 * A class defining facilities for lists.
 * 
 * @author Silvio Peroni 
 */
public class ListFacility {
	/**
	 * Get a sublist from a specific point of an input list to the end.
	 * @param l the original list.
	 * @param index the index considered to build the sublist.
	 * @return a sublist of the input list.
	 */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static List subListFrom(List l, int index) {
    	List result = new ArrayList();
    	int size = l.size();
    	
    	for (int i = index; i < size; i++) {
    		result.add(l.get(i));
    	}
    	
    	return result;
    }
    
    /**
	 * Get a sublist from the first element to a specific point of an input list.
	 * @param l the original list.
	 * @param index the index considered to build the sublist.
	 * @return a sublist of the input list.
	 */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static List subListTo(List l, int index) {
    	List result = new ArrayList();
    	int size = l.size();
    	
    	for (int i = 0; i < size && i < index; i++) {
    		result.add(l.get(i));
    	}
    	
    	return result;
    }
    
    /**
     * Get the list index of the n-th occurence of an object in a list.
     * 
     * @param l the list containing an object.
     * @param item the object to be looked for.
     * @param occurrence the occurrence of the object.
     * @return the n-th occurence of an object in a list.
     */
	@SuppressWarnings("rawtypes")
	public static int indexOfByOccurrence(List l, Object item, int occurrence) {
    	int index = -1;
		int currentOccurrence = 0;
		List tmp;
		
		do {
			index++;
			tmp = ListFacility.subListFrom(l, index);
			int curIndex = tmp.indexOf(item);
			
			if (curIndex == -1) {
				index = -1;
			} else {
				index += curIndex;
				currentOccurrence++;
			}
			
		} while (index > -1 && currentOccurrence < occurrence) ;
    	
    	return index;
    }
}
