package it.essepuntato.earmark.core.test;

import it.essepuntato.earmark.core.Attribute;
import it.essepuntato.earmark.core.Collection.Type;
import it.essepuntato.earmark.core.Docuverse;
import it.essepuntato.earmark.core.EARMARKChildNode;
import it.essepuntato.earmark.core.EARMARKDocument;
import it.essepuntato.earmark.core.Element;
import it.essepuntato.earmark.core.Range;

import java.util.ArrayList;
import java.util.List;

/**
 * A test aims to check modifications of part of EARMARK documents.
 * 
 * @author Silvio Peroni
 *
 */
public class ModifyingDocument extends AbstractTest {
	
	private EARMARKDocument doc = null;

	private static final String value1 = "one";
	private static final String value2 = "two";
	
	private static final int numberOfDocuverse = 2;
	
	public ModifyingDocument(EARMARKDocument document) {
		doc = document;
	}
	
	@Override
	public List<String> doTest() {
		List<String> result = new ArrayList<String>();
		
		try {
			Docuverse docuverse = doc.createStringDocuverse(value1 + value2);
			
			Element element = (Element) doc.getChildElements().iterator().next();
			it.essepuntato.earmark.core.List children = 
				(it.essepuntato.earmark.core.List) element.getChildNodes();
			
			result.add("\n[i] Initial content:\n\t\""+ element.getTextContent() + "\"");
			
			result.add("\n[i] Inserting an attribute after the first node");
			String msg1 = "Insertion of a new attribute, test";
			Attribute attr1 = doc.createAttribute("name", Type.Set);
			EARMARKChildNode aChild = children.get(1);
			element.insertBefore(attr1, aChild);
			Range r0_3 = doc.createPointerRange(docuverse, 0, 3); attr1.appendChild(r0_3);
			if (
					attr1.equals(((it.essepuntato.earmark.core.List) element.getChildNodes()).get(1)) && 
					attr1.getTextContent().equals(value1) &&
					!attr1.getParentNodes().isEmpty()) {
				result.add(passed(msg1));
			} else {
				result.add(failed(msg1, "wrong position or wrong text content" +
						"\n\tcurrent content -> " + attr1.getTextContent() +
						"\n\tactual content -> " + value1 +
						"\n\tmarkup item in the right position -> " + aChild) +
						"\n\tnumber of parents: " + attr1.getParentNodes().size());
			}
			
			result.add("\n[i] Intermediate content:\n\t\""+ element.getTextContent() + "\"");
			
			result.add("\n[i] Remove the current third node");
			String msg2 = "Deletion of a node, test";
			element.removeChild(aChild);
			boolean isStillThere = false;
			for (EARMARKChildNode child : element.getChildNodes()) {
				if (child.equals(aChild)) {
					isStillThere = true;
				}
			}
			if (isStillThere) {
				result.add(failed(msg2, "the removed node is still there:" +
						"\n\tremoved element -> " + aChild));
			} else {
				result.add(passed(msg2));
			}
			
			result.add("\n[i] Intermediate content:\n\t\""+ element.getTextContent() + "\"");
			
			result.add("\n[i] Replacement of the attribute with the a range");
			String msg3 = "Replacement of an attribute with a range, test";
			Range r3_6 = doc.createPointerRange(docuverse, 3, 6);
			element.replaceChild(r3_6, attr1);
			boolean attributeIsStillThere = false;
			for (EARMARKChildNode child : element.getChildNodes()) {
				if (child.equals(attr1)) {
					attributeIsStillThere = true;
				}
			}
			if (attributeIsStillThere) {
				result.add(failed(msg3, "the replaced attribute node is still there:" +
						"\n\treplaced attribute -> " + attr1));
			} else {
				result.add(passed(msg3));
			}
			
			result.add("\n[i] Final content:\n\t\""+ element.getTextContent() + "\"");
			
			result.add("\n[i] Counting the number of docuverses");
			String msg4 = "Number of docuverse, test";
			if (numberOfDocuverse == doc.getAllDocuverses().size()) {
				result.add(passed(msg4));
			} else {
				result.add(failed(msg4, "the current number of docuverse is different of the actual:" +
						"\n\tcurrent -> " + doc.getAllDocuverses().size() +
						"\n\tactual -> " + numberOfDocuverse));
			}
			
		} catch (NullPointerException e) {
			result.add("[e] Null pointer exception");
		}
		
		return result;
	}

	@Override
	public String getTestName() {
		return "Modifying document test";
	}

	@Override
	public boolean useDocument(EARMARKDocument document) {
		doc = document;
		return true;
	}

}
