package it.essepuntato.facility.set;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A class defining facilities for sets.
 * 
 * @author Silvio Peroni 
 */
public class SetFacility {
	/**
	 * Get the intersection of two sets.
	 * 
	 * @param one former set.
	 * @param two latter set.
	 * @return the intersection of the two sets.
	 */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static Set intersect(Set one, Set two) {
        Set result = new HashSet();

        Set selected = null;
        boolean firstSelected = true;
        if (one.size() < two.size()) {
            selected = one;
        } else {
            selected = two;
            firstSelected = false;
        }

        Iterator ite = selected.iterator();
        while (ite.hasNext()) {
            Object current = ite.next();
            if ((firstSelected ? two : one).contains(current)) {
                result.add(current);
            }
        }

        return result;
    }
}
