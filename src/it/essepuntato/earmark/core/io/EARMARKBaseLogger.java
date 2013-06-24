package it.essepuntato.earmark.core.io;

import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import java.util.logging.XMLFormatter;

/**
 * An abstract class implementing the basic methods of an EARMARK input/output logger.
 * 
 * @author Silvio Peroni
 *
 */
public abstract class EARMARKBaseLogger implements EARMARKIOLogger {

	private Logger logger = null;
	private ByteArrayOutputStream plainText = new ByteArrayOutputStream();
	private ByteArrayOutputStream xml = new ByteArrayOutputStream();

	@Override
	public Logger getLogger(Class<?> c) {
		if (logger == null) {
			logger = Logger.getLogger(c.getName());
			logger.addHandler(new StreamHandler(plainText, new SimpleFormatter()));
			logger.addHandler(new StreamHandler(xml, new XMLFormatter()));
		}
		
		return logger;
	}

	@Override
	public String getPlainText() {
		return plainText.toString();
	}
	
	@Override
	public String getXML() {
		return xml.toString();
	}

}
