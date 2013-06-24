package it.essepuntato.earmark.core.io;

import it.essepuntato.earmark.core.Attribute;
import it.essepuntato.earmark.core.Collection;
import it.essepuntato.earmark.core.Comment;
import it.essepuntato.earmark.core.Docuverse;
import it.essepuntato.earmark.core.EARMARKChildNode;
import it.essepuntato.earmark.core.EARMARKClassURI;
import it.essepuntato.earmark.core.EARMARKDocument;
import it.essepuntato.earmark.core.EARMARKItem;
import it.essepuntato.earmark.core.EARMARKNode;
import it.essepuntato.earmark.core.EARMARKOntologyURI;
import it.essepuntato.earmark.core.EARMARKPropertyURI;
import it.essepuntato.earmark.core.Element;
import it.essepuntato.earmark.core.MarkupItem;
import it.essepuntato.earmark.core.PointerRange;
import it.essepuntato.earmark.core.Range;
import it.essepuntato.earmark.core.StringDocuverse;
import it.essepuntato.earmark.core.URIDocuverse;
import it.essepuntato.earmark.core.XPathPointerRange;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * A concrete EARMARK input/output writer using Jena as framework.
 * 
 * @author Silvio Peroni
 *
 */
public class JenaWriter extends EARMARKBaseLogger implements EARMARKWriter {
	private boolean removeOWLStandardStatement = true;
	private EARMARKDocument document = null;
	private Set<EARMARKItem> visited = new HashSet<EARMARKItem>();
	private Model model = null;
	private Map<String,String> prefixes = new HashMap<String,String>();
	
	private Resource element_r;
	private Resource attribute_r;
	private Resource comment_r;
	private Resource stringdocuverse_r;
	private Resource uridocuverse_r;
	private Resource pointerrange_r;
	private Resource xpathpointerrange_r;
	private Resource set_r;
	private Resource bag_r;
	private Resource list_r;
	private Resource item_r;
	private Resource listitem_r;
	
	private Property phasgeneralidentifier_r;
	private Property phasnamespace_r;
	private Property phascontent_r;
	private Property pelement_r;
	private Property pitem_r;
	private Property pfirstitem_r;
	private Property pnextitem_r;
	private Property pbegins_r;
	private Property pends_r;
	private Property prefersto_r;
	private Property phasxpathcontext_r;
	private Property pitemcontent_r;
	private Property ppreviousitem_r;
	private Property psize_r;
	private Property plastitem_r;
	
	@Override
	public String write(EARMARKDocument document) {
		return write(document, RDFXML);
	}
	
	@Override
	public boolean write(EARMARKDocument document, File file) {
		return write(document, file, RDFXML);
	}
	
	private void setPrefixes(Model model) {
		model.setNsPrefix("earmark", EARMARKOntologyURI.EARMARK.toString() + "#");
		model.setNsPrefix("co", EARMARKOntologyURI.COLLECTIONS.toString() + "/");
		model.setNsPrefix("this", document.hasId().toString() + EARMARKDocument.SEPARATOR);
		model.setNsPrefix("xs", "http://www.w3.org/2001/XMLSchema#");
		model.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
		model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		model.setNsPrefixes(prefixes);
	}
	
	@Override
	public String write(EARMARKDocument document, String format) {
		String result = null;
		
		Model model = getModel(document);
		try {
			setPrefixes(model);
			StringWriter writer = new StringWriter();
			model.write(writer, format);
			result = writer.toString();
		} catch (NullPointerException e) {
			getLogger().warning("The EARMARK document passed as input is null" 
					+ " [in 'write' method]\nException: " + e.getMessage());
		}
		
		return result;
	}

	@Override
	public boolean write(EARMARKDocument document, File file, String format) {
		boolean result = true;
		
		Model model = getModel(document);
		try {
			setPrefixes(model);
			model.write(new FileWriter(file), format);
		} catch (IOException e) {
			result = false;
			getLogger().warning("The EARMARK document has not been stored in '" + file.toString() + "'" 
					+ " [in 'write' method]\nException: " + e.getMessage());
		} catch (NullPointerException e) {
			result = false;
			getLogger().warning("The EARMARK document passed as input is null" 
					+ " [in 'write' method]\nException: " + e.getMessage());
		}
		
		return result;
	}
	
	@Override
	public Model getModel(EARMARKDocument document) {
		this.document = document;
		
		/* Add information about the document */
		try {
			String id = document.hasId().toString();
			model = ModelFactory.createDefaultModel();
			model.add(model.createResource(id), RDF.type, OWL.Ontology);
			model.add(document.getModel());
		} catch (NullPointerException e) {
			return model;
		}
		
		initializeResources();
		
		/* Add information about docuverses */
		for (Docuverse docuverse : document.getAllDocuverses()) {
			forwardMethodRequest(docuverse);
		}
		
		/* Add information about EARMARK nodes (markup items and ranges) */
		for (EARMARKNode node : document.getAllEARMARKNode()) {
			forwardMethodRequest(node);
		}
		
		return model;
	}
	
	private Resource forwardMethodRequest(Docuverse item) {
		Resource result = null;
		Docuverse.Type type = item.getType();
		
		try {
			if (type == Docuverse.Type.StringDocuverse) {
				result = readStringDocuverse((StringDocuverse) item);
				visited.add(item);
			} else if (type == Docuverse.Type.URIDocuverse) {
				result = readURIDocuverse((URIDocuverse) item);
				visited.add(item);
			} 
		} catch (NullPointerException e) {
			getLogger().warning("The docuverse " + item.hasId() + " has not been added to the model" 
					+ " [in 'forwardMethodRequest' method]\nException: " + e.getMessage());
		}
		
		return result;
	}
	
	private Resource readURIDocuverse(URIDocuverse item) {
		Resource result = addDocuverseProperties(item, XSDDatatype.XSDanyURI);
		
		addOWLStatement(model.createStatement(result, RDF.type, uridocuverse_r));
		
		return result;
	}

	private Resource readStringDocuverse(StringDocuverse item) {
		Resource result = addDocuverseProperties(item, XSDDatatype.XSDstring);
		
		addOWLStatement(model.createStatement(result, RDF.type, stringdocuverse_r));
		
		return result;
	}
	
	private Resource addDocuverseProperties(Docuverse item, RDFDatatype type) {
		Resource result = model.createResource(item.hasId().toString());
		
		Object content = item.hasContent();
		try {
			result.addProperty(phascontent_r, content.toString(), type);
		} catch (NullPointerException e) {
			getLogger().warning("The docuverse " + item.hasId() + " has not any content specified" 
					+ " [in 'addDocuverseProperties' method]\nException: " + e.getMessage());
		}
		
		return result;
	}

	private Resource forwardMethodRequest(EARMARKNode item) {
		Resource result = null;
		EARMARKNode.Type type = item.getNodeType();
		
		try {
			if (type == EARMARKNode.Type.PointerRange) {
				result = readPointerRange((PointerRange) item);
				visited.add(item);
			} else if (type == EARMARKNode.Type.XPathPointerRange) {
				result = readXPathPointerRange((XPathPointerRange) item);
				visited.add(item);
			} else if (type == EARMARKNode.Type.Element) {
				result = readElement((Element) item);
				visited.add(item);
			} else if (type == EARMARKNode.Type.Attribute) {
				result = readAttribute((Attribute) item);
				visited.add(item);
			} else if (type == EARMARKNode.Type.Comment) {
				result = readComment((Comment) item);
				visited.add(item);
			}
		} catch (NullPointerException e) {
			getLogger().warning("The EARMARK node " + item.hasId() + " has not been added to the model" 
					+ " [in 'forwardMethodRequest' method]\nException: " + e.getMessage());
		}
		
		return result;
	}

	private Resource readXPathPointerRange(XPathPointerRange item) {
		Resource result = addRangeProperties(item, XSDDatatype.XSDnonNegativeInteger);
		
		Statement statement = null;
		
		String hasXPathContext = item.hasXPathContext();
		try {
			result.addProperty(phasxpathcontext_r, hasXPathContext.toString(), XSDDatatype.XSDstring);
		} catch (NullPointerException e) {
			getLogger().warning("The XPath pointer range " + item.hasId() + " has not any XPath context specified" 
					+ " [in 'readXPathPointerRange' method]\nException: " + e.getMessage());
			statement = model.createStatement(result, RDF.type, xpathpointerrange_r);
			
		}
		
		if (statement == null) {
			model.add(result, RDF.type, xpathpointerrange_r);
		} else {
			model.add(statement);
		}
		
		return result;
	}

	private Resource readPointerRange(PointerRange item) {
		Resource result = addRangeProperties(item, XSDDatatype.XSDnonNegativeInteger);
		model.add(result, RDF.type, pointerrange_r);
		return result;
	}
	
	private Resource addRangeProperties(Range item, RDFDatatype type) {
		Resource result = model.createResource(item.hasId().toString());
		
		Object begins = item.begins();
		try {
			result.addProperty(pbegins_r, begins.toString(), type);
		} catch (NullPointerException e) {
			getLogger().warning("The range " + item.hasId() + " has not any begin location specified" 
					+ " [in 'addRangeProperties' method]\nException: " + e.getMessage());
		}
		
		Object ends = item.ends();
		try {
			result.addProperty(pends_r, ends.toString(), type);
		} catch (NullPointerException e) {
			getLogger().warning("The range " + item.hasId() + " has not any end location specified" 
					+ " [in 'addRangeProperties' method]\nException: " + e.getMessage());
		}
		
		Docuverse refersTo = item.refersTo();
		result.addProperty(prefersto_r, model.createResource(refersTo.hasId().toString()));
		
		return result;
	}
	
	private Resource readComment(Comment item) {
		Resource result = addMarkupItemProperties(item);
		model.add(result, RDF.type, comment_r);
		return result;
	}
	
	private Resource readAttribute(Attribute item) {
		Resource result = addMarkupItemProperties(item);
		model.add(result, RDF.type, attribute_r);
		return result;
	}

	private Resource readElement(Element item) {
		Resource result = addMarkupItemProperties(item);
		model.add(result, RDF.type, element_r);
		return result;
	}
	
	private Resource addMarkupItemProperties(MarkupItem item) {
		Resource result = model.createResource(item.hasId().toString());
		
		String generalIdentifier = item.hasGeneralIdentifier();
		try {
			result.addProperty(phasgeneralidentifier_r, generalIdentifier.toString(), XSDDatatype.XSDstring);
		} catch (NullPointerException e) {
			getLogger().fine("The markup item " + item.hasId() + " has not any general identifier specified" 
					+ " [in 'addMarkupItemProperties' method]\nException: " + e.getMessage());
		}
		
		URI namespace = item.hasNamespace();
		try {
			result.addProperty(phasnamespace_r, namespace.toString(), XSDDatatype.XSDanyURI);
		} catch (NullPointerException e) {
			getLogger().fine("The markup item " + item.hasId() + " has not any namespace specified" 
					+ " [in 'addMarkupItemProperties' method]\nException: " + e.getMessage());
		}
		
		Collection children = item.getChildNodes();
		
		Statement collectionType = null;
		if (item.isSet()) {
			collectionType = model.createStatement(result, RDF.type, set_r);
		} else if (children.isEmpty()) {
			if (item.isList()) {
				collectionType = model.createStatement(result, RDF.type, list_r);
			} else if (item.isBag()) {
				collectionType = model.createStatement(result, RDF.type, bag_r);
			}
		} else {
			addOWLStatement(model.createLiteralStatement(result, RDF.type, (item.isList() ? list_r : bag_r)));
		}
		
		if (collectionType != null) {
			model.add(collectionType);
		}
		
		int size = children.size(); int index = 0;
		
		addOWLStatement(model.createLiteralStatement(result, psize_r, size));
		
		Resource previousItem = null;
		for (EARMARKChildNode child : children) {
			index++;
			if (item.isList()) {
				Resource listItem = model.createResource();
				
				if (index == 1) {
					model.add(result, pfirstitem_r, listItem);
				} else if (!removeOWLStandardStatement) {
					if (index == size) {//Last
						model.add(result, plastitem_r, listItem);
					} else {
						model.add(result, pitem_r, listItem);
					}
				}
				
				addOWLStatement(model.createStatement(listItem, RDF.type, listitem_r));
				model.add(listItem, pitemcontent_r, model.createResource(child.hasId().toString()));
				
				if (previousItem != null) {
					model.add(previousItem, pnextitem_r, listItem);
					addOWLStatement(model.createStatement(listItem, ppreviousitem_r, previousItem));
				}
				
				previousItem = listItem;
			} else if (item.isBag()) {
				Resource bagItem = model.createResource();
				
				model.add(result, pitem_r, bagItem);
				model.add(bagItem, pitemcontent_r, model.createResource(child.hasId().toString()));
				
				addOWLStatement(model.createStatement(bagItem, RDF.type, item_r));
			} else {
				model.add(result, pelement_r, model.createResource(child.hasId().toString()));
			}
		}
		
		return result;
	}

	public void setRemoveOWLStandardStatements(boolean value) {
		removeOWLStandardStatement = value;
	}
	
	public boolean getRemoveOWLStandardStatements() {
		return removeOWLStandardStatement;
	}
	
	private Logger getLogger() {
		return getLogger(JenaWriter.class);
	}
	
	private void addOWLStatement(Statement statement) {
		if (!removeOWLStandardStatement) {
			model.add(statement);
		}
	}

	private void initializeResources() {
		/* Initialize the EARMARK classes */
		element_r = model.createResource(EARMARKClassURI.ELEMENT.toString());
		attribute_r = model.createResource(EARMARKClassURI.ATTRIBUTE.toString());
		comment_r = model.createResource(EARMARKClassURI.COMMENT.toString());
		stringdocuverse_r = model.createResource(EARMARKClassURI.STRING_DOCUVERSE.toString());
		uridocuverse_r = model.createResource(EARMARKClassURI.URI_DOCUVERSE.toString());
		pointerrange_r = model.createResource(EARMARKClassURI.POINTER_RANGE.toString());
		xpathpointerrange_r = model.createResource(EARMARKClassURI.XPATH_POINTER_RANGE.toString());
		set_r = model.createResource(EARMARKClassURI.SET.toString());
		bag_r = model.createResource(EARMARKClassURI.BAG.toString());
		list_r = model.createResource(EARMARKClassURI.LIST.toString());
		item_r = model.createResource(EARMARKClassURI.ITEM.toString());
		listitem_r = model.createResource(EARMARKClassURI.LIST_ITEM.toString());
		
		/* Initialize the EARMARK properties */
		pbegins_r = model.createProperty(EARMARKPropertyURI.BEGINS.toString());
		pends_r = model.createProperty(EARMARKPropertyURI.ENDS.toString());
		prefersto_r = model.createProperty(EARMARKPropertyURI.REFERS_TO.toString());
		phasxpathcontext_r = model.createProperty(EARMARKPropertyURI.HAS_XPATH_CONTEXT.toString());
		phascontent_r = model.createProperty(EARMARKPropertyURI.HAS_CONTENT.toString());
		phasgeneralidentifier_r = model.createProperty(EARMARKPropertyURI.HAS_GENERAL_IDENTIFIER.toString());
		phasnamespace_r = model.createProperty(EARMARKPropertyURI.HAS_NAMESPACE.toString());
		
		pnextitem_r = model.createProperty(EARMARKPropertyURI.NEXT_ITEM.toString());
		ppreviousitem_r = model.createProperty(EARMARKPropertyURI.PREVIOUS_ITEM.toString());
		pelement_r = model.createProperty(EARMARKPropertyURI.ELEMENT.toString());
		pfirstitem_r = model.createProperty(EARMARKPropertyURI.FIRST_ITEM.toString());
		plastitem_r = model.createProperty(EARMARKPropertyURI.LAST_ITEM.toString());
		pitem_r = model.createProperty(EARMARKPropertyURI.ITEM.toString());
		pitemcontent_r = model.createProperty(EARMARKPropertyURI.ITEM_CONTENT.toString());
		psize_r = model.createProperty(EARMARKPropertyURI.SIZE.toString());
	}

	@Override
	public void addPrefixes(Map<String, String> prefixNsPair) {
		prefixes.clear();
		prefixes.putAll(prefixNsPair);
	}
}
