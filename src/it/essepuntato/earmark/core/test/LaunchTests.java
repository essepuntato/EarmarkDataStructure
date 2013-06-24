package it.essepuntato.earmark.core.test;

import java.util.ArrayList;
import java.util.List;

/**
 * The class running the entire test suite.
 * 
 * @author Silvio Peroni
 *
 */
public class LaunchTests {
	public static void main(String[] args) {
		List<EARMARKTest> tests = new ArrayList<EARMARKTest>();
		
		tests.add(new DocumentCreation());
		tests.add(new DocumentStructure());
		tests.add(new ModifyingDocument(DocumentCreation.getDocument()));
		tests.add(new ModifyingDocument(DocumentStructure.getDocument()));
		tests.add(new EqualityTest(DocumentStructure.getDocument(), DocumentStructure.getDocument()
				, true, true, true));
		tests.add(new EqualityTest(DocumentCreation.getDocument(), DocumentStructure.getDocument()
				, false, false, false));
		tests.add(new EqualityTest(
				EqualityTest.getDocumentWithDifferentDocumentId(), 
				DocumentStructure.getDocument()
				, false, true, true));
		tests.add(new EqualityTest(
				EqualityTest.getDocumentWithDifferentIds(), 
				DocumentStructure.getDocument()
				, false, true, true));
		tests.add(new EqualityTest(
				EqualityTest.getDocumentWithDifferentDocumentIdAndStructure(), 
				DocumentStructure.getDocument()
				, false, false, false));
		tests.add(new LoadTest());
		tests.add(new StoreTest(DocumentStructure.getDocument()));
		tests.add(new FrancescoPoggiTestOne(DocumentCreation.getDocument()));
		
		for (EARMARKTest test : tests) {
			System.out.print("\n*** BEGIN: " + test.getTestName() + " ***\n");
			for (String result : test.doTest()) {
				System.out.println(result);
			}
			System.out.print("*** END: " + test.getTestName() + " ***\n");
		}
	}
}
