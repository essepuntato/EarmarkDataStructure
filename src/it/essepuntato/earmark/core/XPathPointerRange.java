package it.essepuntato.earmark.core;

import java.net.URI;

/**
 * This class represents an concrete xpath pointer range in the EARMARK model.
 * 
 * @author Silvio Peroni
 *
 */
public class XPathPointerRange extends XPathRange {

	/**
	 * Create an XPath pointer range.
	 * 
	 * @param d the document to which this item is associated.
	 * @param docuverse the docuverse the range refers to.
	 * @param begin the begin non-negative integer point (if negative, it consider its
	 * absolute value) of the range.
	 * @param end the end non-negative integer point (if negative, it consider its
	 * absolute value) of the range. 
	 * @param context the xpath expression used to extract the sequence of XML text nodes that will be
	 * used as content for calculating the text content of the range.
	 * @param id the identifier for the new range. 
	 */
	protected XPathPointerRange(EARMARKDocument d, Docuverse docuverse,
			Integer begin, Integer end, String context, URI id) {
		super(d, docuverse, 
				(begin == null ? begin : new Integer(Math.abs(begin))), 
				(end == null ? end : new Integer(Math.abs(end))), 
				context, id);
		
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

	@Override
	public Type getNodeType() {
		return EARMARKNode.Type.XPathPointerRange;
	}
	
	@Override
	public Range clone() {
		String newContent = getTextContent();
		Docuverse newDocuverse = getOwnerDocument().createStringDocuverse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\" ?><element>" + newContent + "</element>");
		Integer newBegin = (begins() == null ? null : 0);
		Integer newEnd = (ends() == null ? null : newContent.length());
		
		return getOwnerDocument().createXPathPointerRange(newDocuverse, newBegin, newEnd, "//text()");
	}

	/**
	 * <p>This method retrieves the textual content of the range 1) applying the XPath expression
	 * specified as context to the docuverse (XML) content, 2) taking all the value of all the
	 * nodes (elements, attributes and text nodes only) returned by the expression, considering the
	 * sequence order, and finally restrict this content using the begin and end locations.</p>
	 * <p>If the begin and/or end locations are not specified (i.e., = null), then this method considers
	 * location "0" as begin and location "size(docuverse_content)" as end.</p>
	 * <p>The possible reason of failure in retrieving the text content (i.e., result = "") are:</p>
	 * <ul>
	 * <li>the docuverse does not contain a well-formed XML document</li>
	 * <li>the XPath context specified is not a valid XPath expression</li>
	 * </ul>
	 * @return the textual content retrieved by applying the XPath context onto the docuverse content, or null
	 * if some problem happens.
	 */
	public String getTextContent() {
		String result = super.getTextContent();
		
		String portion = result;
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
}
