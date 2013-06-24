package it.essepuntato.earmark.core.test;

import it.essepuntato.earmark.core.EARMARKDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * A test aims to check the storing of EARMARK documents.
 * 
 * @author Silvio Peroni
 *
 */
public class StoreTest extends AbstractTest {

	private EARMARKDocument document = null;
	
	public StoreTest(EARMARKDocument document) {
		this.document = document;
	}
	
	@Override
	public List<String> doTest() {
		List<String> result = new ArrayList<String>();
		
		result.add("\n[i] Return the document as RDF/XML string");
		String rdfxml = document.getDocumentAsRDFXML();
		String msg1 = "Check if " + document.hasId() + " can be stored as RDF/XML, test";
		if (rdfxml == null) {
			result.add(failed(msg1));
		} else {
			result.add(passed(msg1));
			
			result.add("\n[i] Load a new EARMARK document starting from the previous RDF/XML string");
			EARMARKDocument newDocument = EARMARKDocument.load(rdfxml);
			
			result.add("\n[i] Documents comparison");
			String msg2 = "Check if the two documents are the same document, test";
			if (document.isSameNode(newDocument)) {
				result.add(passed(msg2));
			} else {
				result.add(failed(msg2));
			}
			
			String msg3 = "Check if the two documents are the equal document, test";
			if (document.isEqualNode(newDocument)) {
				result.add(passed(msg3));
			} else {
				result.add(failed(msg3));
			}
			
			String msg4 = "Check if the two documents are structurally equivalent, test";
			if (document.isStructurallyEqualNode(newDocument)) {
				result.add(passed(msg4));
			} else {
				result.add(failed(msg4));
			}
		}
		
		return result;
	}

	@Override
	public String getTestName() {
		return "Store test";
	}

	@Override
	public boolean useDocument(EARMARKDocument document) {
		if (document == null) {
			return false;
		} else {
			this.document = document;
			return true;
		}
	}

}
