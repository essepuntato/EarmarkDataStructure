package it.essepuntato.earmark.core.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import it.essepuntato.earmark.core.EARMARKDocument;

public class AVeryVeryGeneralTest {
	public static void main(String[] args) {
		try {
			EARMARKDocument doc = EARMARKDocument.load(
					new URL("file:///Users/six/Dropbox/Programming/Eclipse/EARMARKDocuments/WikiChangeTracking.ttl"));
			System.out.println(doc.getDocumentAsTurtle());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
