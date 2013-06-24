package it.essepuntato.earmark.core;

import java.net.URI;

/**
 * This class represents a concrete pointer range in the EARMARK model.
 * 
 * @author Silvio Peroni
 *
 */
public class PointerRange extends Range {

	/**
	 * Create a pointer range.
	 * 
	 * @param d the document to which this item is associated.
	 * @param docuverse the docuverse the range refers to.
	 * @param begin the begin non-negative integer point (if negative, it consider its
	 * absolute value) of the range.
	 * @param end the end non-negative integer point (if negative, it consider its
	 * absolute value) of the range. 
	 * @param id the identifier for the new range.
	 */
	protected PointerRange(EARMARKDocument d, Docuverse docuverse,
			Integer begin, Integer end, URI id) {
		super(d, docuverse, 
				(begin == null ? begin : new Integer(Math.abs(begin))), 
				(end == null ? end : new Integer(Math.abs(end))), id);
	}
	
	/**
	 * The integer point in which the range begins.
	 * 
	 * @return the begin integer point.
	 */
	public Integer begins() {
		return (Integer) super.begins();
	}
	
	/**
	 * The integer point in which the range ends.
	 * 
	 * @return the end integer point.
	 */
	public Integer ends() {
		return (Integer) super.ends();
	}

	/**
	 * <p>This method retrieves the textual content of the range, considering the document content as a string
	 * and then restricting it according to the begin and end locations.</p>
	 * <p>If the begin and/or end locations are not specified (i.e., = null), then this method considers
	 * location "0" as begin and location "size(docuverse_content)" as end.</p>
	 * 
	 * @return the textual content of the range.
	 */
	public String getTextContent() {
		String result = null;
		
		String portion = getDocuverseContent();
		int size = portion.length();
		
		/* Handling cases in which neither the begin nor the end locations are not specified */
		Integer b = begins();
		if (b == null) {
			b = 0;
		}
		
		Integer e = ends();
		if (e == null) {
			e = size;
		}
		boolean isReverse = e < b;
		
		if (isReverse) {
			int tmp = b;
			b = e;
			e = tmp;
		}
		
		if (b <= size && e <= size) {
			result = portion.substring(b, e);
			if (isReverse) {
				StringBuffer buffer = new StringBuffer(result);
				buffer.reverse();
				result = buffer.toString();
			}
		}
		
		return result;
	}

	@Override
	public Type getNodeType() {
		return EARMARKNode.Type.PointerRange;
	}
	
	@Override
	public Range clone() {
		String newContent = getTextContent();
		Docuverse newDocuverse = getOwnerDocument().createStringDocuverse(newContent);
		Integer newBegin = (begins() == null ? null : 0);
		Integer newEnd = (ends() == null ? null : newContent.length());
		
		return getOwnerDocument().createPointerRange(newDocuverse, newBegin, newEnd);
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
}
