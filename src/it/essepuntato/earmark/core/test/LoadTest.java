package it.essepuntato.earmark.core.test;

import it.essepuntato.earmark.core.EARMARKDocument;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * A test aims to check the loading of EARMARK documents.
 * 
 * @author Silvio Peroni
 *
 */
public class LoadTest extends AbstractTest {

	@Override
	public List<String> doTest() {
		List<String> result = new ArrayList<String>();
		
		result.add("\n[i] Load the document from file");
		File file = new File("ParadiseLost.owl");
		String msg1 = "Check if " + file + " can be loaded, test";
		try {
			EARMARKDocument document = EARMARKDocument.load(file);
			result.add(passed(msg1));
			
			result.add("\n[i] Logger result:");
			result.add("\"" + EARMARKDocument.getReader().getPlainText() + "\"");
			
			result.add("\n[i] RDF statements outside the document:");
			StringWriter writer = new StringWriter();
			document.getModel().write(writer);
			result.add(writer.toString());
		} catch (FileNotFoundException e) {
			result.add(failed(msg1, e.getMessage()));
		}
		
		return result;
	}

	@Override
	public String getTestName() {
		return "Load test";
	}

	@Override
	public boolean useDocument(EARMARKDocument document) {
		return false;
	}

}
