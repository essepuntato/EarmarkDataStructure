package it.essepuntato.earmark.core.test;

import it.essepuntato.earmark.core.Collection;
import it.essepuntato.earmark.core.Docuverse;
import it.essepuntato.earmark.core.EARMARKChildNode;
import it.essepuntato.earmark.core.EARMARKDocument;
import it.essepuntato.earmark.core.Element;
import it.essepuntato.earmark.core.MarkupItem;
import it.essepuntato.earmark.core.Range;
import it.essepuntato.earmark.core.Collection.Type;
import it.essepuntato.earmark.core.exception.ExistingIdException;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A test aims to check the structure of EARMARK documents.
 * 
 * @author Silvio Peroni
 *
 */
public class DocumentStructure extends AbstractTest {

	public static final String text = 
		"Of man's first disobedience, and the fruit\n" +
		"Of that forbidden tree whose mortal taste\n" +
		"Brough death into the World";
	
	public static final int numberOfDiv = 2;
	public static final int numberOfP = 3;
	public static final int numberOfSpan = 2;
	public static final int numberOfMarkupItem = numberOfDiv + numberOfP + numberOfSpan;
	public static final int numberOfRange = 6;
	
	public static final URI ns1 = URI.create("http://www.cs.unibo.it/2006/iml");
	public static final URI ns2 = URI.create("http://www.w3.org/1999/xhtml");
	
	public static final int numberOfElementsOfNs1 = 4;
	public static final int numberOfElementsOfNs2 = 3;
	
	@Override
	public List<String> doTest() {
		List<String> result = new ArrayList<String>();
		EARMARKDocument doc = getDocument();
		
		result.add("\n[i] Check number of elements");
		String msg1 = "Number of node in the document, test";
		Collection toVisit = doc.getChildElements();
		int count = 0;
		while (!toVisit.isEmpty()) {
			Element node = (Element) toVisit.iterator().next();
			toVisit.remove(node);
			count++;
			toVisit.addAll(node.getChildElements());
		}
		if (count == numberOfMarkupItem) {
			result.add(passed(msg1));
		} else {
			result.add(failed(msg1, "there are " + count + " instead of " + numberOfMarkupItem));
		}
		
		result.add("\n[i] Check number of div");
		String msg2 = "Number of div in the document, test";
		Set<MarkupItem> divs = doc.getMarkupItemByGeneralIdentifier("div");
		if (divs.size() == numberOfDiv) {
			result.add(passed(msg2));
		} else {
			result.add(failed(msg2, "there are " + divs.size() + " instead of " + numberOfDiv));
		}
		
		result.add("\n[i] Check number of p");
		String msg3 = "Number of p in the document, test";
		Set<MarkupItem> ps = doc.getMarkupItemByGeneralIdentifier("p");
		if (ps.size() == numberOfP) {
			result.add(passed(msg3));
		} else {
			result.add(failed(msg3, "there are " + ps.size() + " instead of " + numberOfP));
		}
		
		result.add("\n[i] Check number of span");
		String msg4 = "Number of span in the document, test";
		Set<MarkupItem> spans = doc.getMarkupItemByGeneralIdentifier("span");
		if (spans.size() == numberOfSpan) {
			result.add(passed(msg4));
		} else {
			result.add(failed(msg4, "there are " + spans.size() + " instead of " + numberOfSpan));
		}
		
		result.add("\n[i] Check number of markup items with ns " + ns1);
		String msg5 = "Number of  markup items with ns " + ns1 + ", test";
		Set<MarkupItem> markupItemWithNs1 = new HashSet<MarkupItem>();
		markupItemWithNs1.addAll(doc.getMarkupItemByGeneralIdentifierAndNamespace("div", ns1));
		markupItemWithNs1.addAll(doc.getMarkupItemByGeneralIdentifierAndNamespace("p", ns1));
		markupItemWithNs1.addAll(doc.getMarkupItemByGeneralIdentifierAndNamespace("span", ns1));
		if (markupItemWithNs1.size() == numberOfElementsOfNs1) {
			result.add(passed(msg5));
		} else {
			result.add(failed(msg5, "there are " + markupItemWithNs1.size() + 
					" instead of " + numberOfElementsOfNs1));
		}
		
		result.add("\n[i] Check number of markup items with ns " + ns2);
		String msg6 = "Number of  markup items with ns " + ns2 + ", test";
		Set<MarkupItem> markupItemWithNs2 = new HashSet<MarkupItem>();
		markupItemWithNs2.addAll(doc.getMarkupItemByGeneralIdentifierAndNamespace("div", ns2));
		markupItemWithNs2.addAll(doc.getMarkupItemByGeneralIdentifierAndNamespace("p", ns2));
		markupItemWithNs2.addAll(doc.getMarkupItemByGeneralIdentifierAndNamespace("span", ns2));
		if (markupItemWithNs2.size() == numberOfElementsOfNs2) {
			result.add(passed(msg6));
		} else {
			result.add(failed(msg6, "there are " + markupItemWithNs2.size() + 
					" instead of " + numberOfElementsOfNs2));
		}
		
		result.add("\n[i] Check number of ranges");
		String msg7 = "Number of ranges in the document, test";
		toVisit = doc.getChildElements();
		Set<EARMARKChildNode> visited = new HashSet<EARMARKChildNode>();
		int rangeCount = 0;
		while (!toVisit.isEmpty()) {
			EARMARKChildNode node = toVisit.iterator().next();
			toVisit.remove(node);
			
			if (!visited.contains(node)) {
				visited.add(node);
				
				if (node instanceof MarkupItem) {
					toVisit.addAll(((MarkupItem)node).getChildNodes());
				} else {
					rangeCount++;
				}
			}
		}
		if (rangeCount == numberOfRange) {
			result.add(passed(msg7));
		} else {
			result.add(failed(msg7, "there are " + rangeCount + " instead of " + numberOfRange));
		}
		
		return result;
	}

	@Override
	public String getTestName() {
		return "Document structure test";
	}

	@Override
	public boolean useDocument(EARMARKDocument document) {
		return false;
	}

	/**
	 * A document used in this test.
	 * 
	 * @return the EARMARK document used in this test.
	 */
	public static EARMARKDocument getDocument() {
		return getDocument("");
	}
	
	/**
	 * A document used in this test.
	 * 
	 * @param idNS a particular prefix to be used as path for the identifiers of document nodes.
	 * @return the EARMARK document used in this test.
	 */
	public static EARMARKDocument getDocument(String idNS) {
		EARMARKDocument doc = new EARMARKDocument(URI.create("http://www.essepuntato.it/2011/01/paradiselost"));
		
		Docuverse docuverse = doc.createStringDocuverse(text);
		
		try {
			Element stanza = doc.createElement(idNS + "stanza", "div", ns1, Type.List); doc.appendChild(stanza);
			Element verse1 = doc.createElement(idNS + "verse1", "p", ns1, Type.List); stanza.appendChild(verse1);
			Range r0_33 = doc.createPointerRange(idNS + "r1", docuverse, 0, 33); verse1.appendChild(r0_33);
			Range r33_42 = doc.createPointerRange(idNS + "r2", docuverse, 33, 42); verse1.appendChild(r33_42);
			
			Element verse2 = doc.createElement(idNS + "verse2", "p", ns1, Type.List); stanza.appendChild(verse2);
			Range r43_65 = doc.createPointerRange(idNS + "r3", docuverse, 43, 65); verse2.appendChild(r43_65);
			Range r65_84 = doc.createPointerRange(idNS + "r4", docuverse, 65, 84); verse2.appendChild(r65_84);
			
			Element verse3 = doc.createElement(idNS + "verse3", "p", ns1, Type.List); stanza.appendChild(verse3);
			Range r85_97 = doc.createPointerRange(idNS + "r5", docuverse, 85, 97); verse3.appendChild(r85_97);
			Range r97_102 = doc.createPointerRange(idNS + "r6", docuverse, 97, 102); verse3.appendChild(r97_102);
			
			Element syntax = doc.createElement(idNS + "syntax", "div", ns2, Type.List); doc.appendChild(syntax);
			
			Element unit1 = doc.createElement(idNS + "unit1", "span", ns2, Type.List); syntax.appendChild(unit1);
			unit1.appendChild(r33_42); unit1.appendChild(r43_65);
			
			Element unit2 = doc.createElement(idNS + "unit2", "span", ns2, Type.List); syntax.appendChild(unit2);
			unit2.appendChild(r65_84); unit2.appendChild(r85_97);
		} catch (ExistingIdException e) {
			doc = null;
		} 
		
		return doc;
	}
}
