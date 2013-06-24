package it.essepuntato.earmark.core.test;

/**
 * An abstract implementation of a test.
 * 
 * @author Silvio Peroni
 *
 */
public abstract class AbstractTest implements EARMARKTest {

	@Override
	public String failed(String msg) {
		return "## " + (msg == null ? "" : msg + " ") + "failed";
	}

	@Override
	public String passed(String msg) {
		return "-- " + (msg == null ? "" : msg + " ") + "passed";
	}
	
	@Override
	public String failed(String msg, String reason) {
		return failed(msg) + ": " + reason;
	}

}
