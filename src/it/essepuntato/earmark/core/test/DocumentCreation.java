package it.essepuntato.earmark.core.test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import it.essepuntato.earmark.core.Docuverse;
import it.essepuntato.earmark.core.EARMARKChildNode;
import it.essepuntato.earmark.core.EARMARKNode;
import it.essepuntato.earmark.core.EARMARKDocument;
import it.essepuntato.earmark.core.Element;
import it.essepuntato.earmark.core.Range;
import it.essepuntato.earmark.core.Collection.Type;

/**
 * A test aims to check the creation of new EARMARK documents.
 * 
 * @author Silvio Peroni
 *
 */
public class DocumentCreation extends AbstractTest {

	public final static String text1 = "Alice was beginning to get very tired of sitting " +
	"by her sister on the bank, and of having nothing to do: once or twice she had peeped " +
	"into the book her sister was reading, but it had no pictures or conversations in it, ";
	public final static String text2 = "and what is the use of a book,";
	public final static String text3 = " thought Alice ";
	public final static String text4 = "without pictures or conversation?";
	public final static int nodeNumber = 6;
	
	@Override
	public List<String> doTest() {
		List<String> result = new ArrayList<String>();
		
		EARMARKDocument doc = getDocument();
		
		result.add("\n[i] Check number of nodes");
		Set<EARMARKChildNode> nodes = doc.getAllEARMARKNode();
		String msg1 = "Number of node in the document, test";
		if (nodes.size() == nodeNumber) {
			result.add(passed(msg1));
		} else {
			result.add(failed(msg1, "there are " + nodes.size() + " instead of " + nodeNumber));
		}
		
		result.add("\n[i] Check document text");
		result.add("Current node: " + doc.toString());
		String totalText = text1 + text2 + text4 + text3;
		String currentText = doc.getTextContent();
		String msg3 = "Number of node in the document, test";
		if (totalText.equals(currentText)) {
			result.add(passed(msg3));
		} else {
			result.add(failed(msg3, "textual content differ\n\tcurrent -> " + 
					currentText + "\n\tactual -> " + totalText));
		}
		
		result.add("\n[i] Check if each node returns the text it contains");
		String msg2 = "Text comparison (CONTAINS for ranges, EQ for others) test";
		for (EARMARKChildNode node : nodes) {
			result.add("Current node: " + node.toString());
			
			String comp = text1 + text2 + text4 + text3;
			if (
					node.getNodeType() == EARMARKNode.Type.Element && 
					((Element)node).hasGeneralIdentifier().equals("q")) {
				comp = text2 + text4;
			}
			
			String content = node.getTextContent();
			
			if (
					content.equals(comp) || 
					(
							node.getNodeType() == EARMARKNode.Type.PointerRange && 
							comp.contains(content))) {
				result.add(passed(msg2));
			} else {
				result.add(failed(msg2, "textual content differ\n\tcurrent -> " + 
						content + "\n\tactual -> " + comp));
			}
			result.add("\n");
		}
		
		return result;
	}
	
	@Override
	public String getTestName() {
		return "Document creation test";
	}

	/**
	 * A document used in this test.
	 * 
	 * @return the EARMARK document used in this test.
	 */
	public static EARMARKDocument getDocument() {
		EARMARKDocument doc = new EARMARKDocument(URI.create("http://www.essepuntato.it/2011/01/alice"));
		
		Docuverse docuverse = doc.createStringDocuverse(text1 + text2 + text3 + text4);
		
		Element p = doc.createElement("p", Type.List); doc.appendChild(p);
		Range r0_219 = doc.createPointerRange(docuverse, 0, 219); p.appendChild(r0_219);
		Element q = doc.createElement("q", Type.List); p.appendChild(q);
		Range r219_249 = doc.createPointerRange(docuverse, 219, 249); q.appendChild(r219_249);
		Range r249_264 = doc.createPointerRange(docuverse, 249, 264); p.appendChild(r249_264);
		Range r264_297 = doc.createPointerRange(docuverse, 264, 297); q.appendChild(r264_297);
		
		return doc;
	}

	@Override
	public boolean useDocument(EARMARKDocument document) {
		return false;
	}
}
