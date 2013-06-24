package it.essepuntato.earmark.core.test;

import it.essepuntato.earmark.core.EARMARKChildNode;
import it.essepuntato.earmark.core.EARMARKDocument;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * A test aims to check the equivalence between EARMARK documents and their nodes.
 * 
 * @author Silvio Peroni
 *
 */
public class EqualityTest extends AbstractTest {

	private List<EARMARKDocument> doc = new ArrayList<EARMARKDocument>();
	private boolean areTheySame = false;
	private boolean areTheyEqual = false;
	private boolean areTheyStructurallyEqual = false;
	
	public EqualityTest(EARMARKDocument doc1, EARMARKDocument doc2, 
			boolean areTheySame, boolean areTheyEqual, boolean areTheyStructurallyEqual) {
		doc.add(doc1);
		doc.add(doc2);
		this.areTheyEqual = areTheyEqual;
		this.areTheySame = areTheySame;
		this.areTheyStructurallyEqual = areTheyStructurallyEqual;
	}
	
	@Override
	public List<String> doTest() {
		List<String> result = new ArrayList<String>();
		
		int size = doc.size();
		for (int i = 0; i < size - 1; i++) {
			EARMARKDocument one = doc.get(i);
			EARMARKDocument two = doc.get(i + 1);
			
			String generalSame = "[they " + (areTheySame ? "are" : "aren't") + " the same]";
			
			result.add("\n[i] Check sameness among documents");
			String msg1 = "Check if\n" + one + " and\n" + two + "\nare the same " + generalSame + ", test";
			boolean same = one.isSameNode(two);
			if ((same && areTheySame) || (!same && ! areTheySame)) {
				result.add(passed(msg1));
			} else {
				result.add(failed(msg1, "look the id!"));
			}
			
			String generalEqual = "[they " + (areTheyEqual ? "are" : "aren't") + " equal]";
			
			result.add("\n[i] Check equality among documents");
			String msg2 = "Check if\n" + one + " and\n" + two + "\nare equal " + generalEqual + ", test";
			boolean equal = one.isEqualNode(two); 
			if ((equal && areTheyEqual) || (!equal && !areTheyEqual)) {
				result.add(passed(msg2));
			} else {
				result.add(failed(msg2, "look the structure!"));
			}
			
			String generalStructurallyEqual = "[they " + (areTheyStructurallyEqual ? "are" : "aren't") + 
					" structurally equal]";
			
			result.add("\n[i] Check structural equality among documents");
			String msg3 = "Check if\n" + one + " and\n" + two + "\nare equal " + generalStructurallyEqual + ", test";
			boolean structurallyEqual = one.isStructurallyEqualNode(two); 
			if ((structurallyEqual && areTheyStructurallyEqual) || (!structurallyEqual && !areTheyStructurallyEqual)) {
				result.add(passed(msg3));
			} else {
				result.add(failed(msg3, "look the structure!"));
			}
		}
		
		return result;
	}

	@Override
	public String getTestName() {
		return "Equality test";
	}

	@Override
	public boolean useDocument(EARMARKDocument document) {
		doc.add(document);
		return true;
	}
	
	/**
	 * A document used in this test.
	 * 
	 * @return the EARMARK document used in this test.
	 */
	public static EARMARKDocument getDocumentWithDifferentDocumentId() {
		EARMARKDocument doc = new EARMARKDocument(
				URI.create("http://www.essepuntato.it/2011/01/anotherparadiselost"));
		
		EARMARKDocument old = DocumentStructure.getDocument();
		
		for (EARMARKChildNode child : old.getChildNodes()) {
			doc.appendChild(doc.copyNode(child, true));
		}
		
		return doc;
	}
	
	/**
	 * A document used in this test.
	 * 
	 * @return the EARMARK document used in this test.
	 */
	public static EARMARKDocument getDocumentWithDifferentIds() {
		EARMARKDocument doc = new EARMARKDocument(
				URI.create("http://www.essepuntato.it/2011/01/anotherparadiselost"));
		
		EARMARKDocument old = DocumentStructure.getDocument("http://www.essepuntato.it/2011/01/anotherparadiselost#");
		
		for (EARMARKChildNode child : old.getChildNodes()) {
			doc.appendChild(doc.copyNode(child, true));
		}
		
		return doc;
	}
	
	/**
	 * A document used in this test.
	 * 
	 * @return the EARMARK document used in this test.
	 */
	public static EARMARKDocument getDocumentWithDifferentDocumentIdAndStructure() {
		EARMARKDocument doc = getDocumentWithDifferentDocumentId();
		
		doc.removeChild(doc.getChildElements().iterator().next());
		
		return doc;
	}

}
