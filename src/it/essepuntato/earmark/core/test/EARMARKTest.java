package it.essepuntato.earmark.core.test;

import it.essepuntato.earmark.core.EARMARKDocument;

import java.util.List;

/**
 * This interface defines the common methods of any test belonging to the EARMARK test suite.
 * 
 * @author Silvio Peroni
 *
 */
public interface EARMARKTest {
	/**
	 * Apply the test and returns a list of string explaining successes and failures of them.
	 * 
	 * @return a list of string describing the test results.
	 */
	public List<String> doTest();
	
	/**
	 * Get the name of the test.
	 * 
	 * @return the name of the test.
	 */
	public String getTestName();
	
	/**
	 * A message that indicates the success of a test or of part of it.
	 * 
	 * @param msg a short description of the test or of part of it.
	 * @return the short description given as input, labelled as "passed".
	 */
	public String passed(String msg);
	
	/**
	 * A message that indicates the failure of a test or of part of it.
	 * 
	 * @param msg a short description of the test or of part of it.
	 * @return the short description given as input, labelled as "failed".
	 */
	public String failed(String msg);
	
	/**
	 * A message that indicates the failure of a test or of part of it.
	 * 
	 * @param msg a short description of the test or of part of it.
	 * @param reason a short description of the possible reason of failure.
	 * @return the short description and the reason of failure given as input, labelled as "failed".
	 */
	public String failed(String msg, String reason);
	
	/**
	 * Specify an EARMARK document to be used in the test.
	 * 
	 * @param document the document to be used in the test.
	 * @return true if the document will be actually used in the test, false otherwise.
	 */
	public boolean useDocument(EARMARKDocument document);
}
