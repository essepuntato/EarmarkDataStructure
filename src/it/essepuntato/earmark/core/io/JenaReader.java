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
import it.essepuntato.earmark.core.EARMARKNode.Type;
import it.essepuntato.earmark.core.EARMARKPropertyURI;
import it.essepuntato.earmark.core.Element;
import it.essepuntato.earmark.core.MarkupItem;
import it.essepuntato.earmark.core.PointerRange;
import it.essepuntato.earmark.core.Range;
import it.essepuntato.earmark.core.StringDocuverse;
import it.essepuntato.earmark.core.URIDocuverse;
import it.essepuntato.earmark.core.XPathPointerRange;
import it.essepuntato.earmark.core.exception.EARMARKGraphException;
import it.essepuntato.earmark.core.exception.ExistingIdException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * A concrete EARMARK input/output reader using Jena as framework.
 * 
 * @author Silvio Peroni
 *
 */
public class JenaReader extends EARMARKBaseLogger implements EARMARKReader {

	private Model model = null;
	private EARMARKDocument document = null;
	private Set<Resource> visited = new HashSet<Resource>();
	private boolean removeOWLStandardStatement = true;
	
	private Resource markupitem_r;
	private Resource element_r;
	private Resource attribute_r;
	private Resource comment_r;
	private Resource docuverse_r;
	private Resource stringdocuverse_r;
	private Resource uridocuverse_r;
	private Resource range_r;
	private Resource pointerrange_r;
	private Resource xpathrange_r;
	private Resource xpathpointerrange_r;
	private Resource set_r;
	private Resource bag_r;
	private Resource list_r;
	private Resource item_r;
	private Resource listitem_r;
	private Resource collection_r;
	
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
	private Property pfollowedby_r;
	private Property ppreceededby_r;
	private Property ppreviousitem_r;
	private Property psize_r;
	private Property plastitem_r;
	
	@Override
	public EARMARKDocument read(URL url) {
		Model model = ModelFactory.createDefaultModel();
		
		try {
			model.read(url.toString(), null);
		} catch (Exception e) {
			model.read(url.toString(), null, EARMARKWriter.TURTLE);
		}
		
		return read(model);
	}

	@Override
	public EARMARKDocument read(File file) throws FileNotFoundException {
		Model model = ModelFactory.createDefaultModel();
		FileInputStream input = new FileInputStream(file); 
		
		try {
			model.read(input, null);
		} catch (Exception e) {
			input = new FileInputStream(file);
			model.read(input, null, EARMARKWriter.TURTLE);
		}
		
		return read(model);
	}

	@Override
	public EARMARKDocument read(String source) {
		Model model = ModelFactory.createDefaultModel();
		StringReader input = new StringReader(source);
		
		try {
			model.read(input, null);
		} catch (Exception e) {
			input = new StringReader(source);
			model.read(input, null, EARMARKWriter.TURTLE);
		}
		
		return read(model);
	}
	
	public void setRemoveOWLStandardStatements(boolean value) {
		removeOWLStandardStatement = value;
	}
	
	public boolean getRemoveOWLStandardStatements() {
		return removeOWLStandardStatement;
	}
	
	@Override
	public EARMARKDocument read(Model model) {
		this.model = model;
		
		/* Create a new EARMARK document, using the URL defined by the URI of the ontology,
		 * if present, or an empty URL otherwise. */
		String id = "";
		ResIterator ite = model.listResourcesWithProperty(RDF.type, OWL.Ontology);
		while (ite.hasNext() && id.equals("")) {
			id = ite.next().getURI();
		}
		document = new EARMARKDocument(URI.create(id));
		removeOWLStandardStatements(document);
		
		/* Initialise all the RDF resources */
		initializeResources();
		
		/* Add all the docuverses to the EARMARK document (it has to be done
		 * before everything else, because the other nodes have to used them) */
		ArrayList<URI> params = new ArrayList<URI>();
		params.add(EARMARKClassURI.URI_DOCUVERSE);
		params.add(EARMARKClassURI.STRING_DOCUVERSE);
		params.add(EARMARKClassURI.DOCUVERSE);
		apply(params);
		
		/* Add all the ranges and markup items to the EARMARK document */
		params.clear();
		params.add(EARMARKClassURI.XPATH_POINTER_RANGE);
		params.add(EARMARKClassURI.POINTER_RANGE);
		params.add(EARMARKClassURI.XPATH_RANGE);
		params.add(EARMARKClassURI.RANGE);
		params.add(EARMARKClassURI.ELEMENT);
		params.add(EARMARKClassURI.ATTRIBUTE);
		params.add(EARMARKClassURI.COMMENT);
		params.add(EARMARKClassURI.MARKUP_ITEM);
		apply(params);
		
		/* Build the hierarchies among markup items */
		Iterator<EARMARKChildNode> roots = setRoots().iterator(); /* Find and set all the roots */
		while (roots.hasNext()) { /* Start to create the direct-graph structure */
			EARMARKChildNode root = roots.next();
			Type type = root.getNodeType();
			if (type == Type.Attribute || type == Type.Comment || type == Type.Element) {
				appendChildren((MarkupItem) root, new HashSet<MarkupItem>());
			}
		}
		
		document.getModel().add(model);
		
		return document;
	}
	
	private void appendChildren(MarkupItem markupitem, Set<MarkupItem> visitedMarkupItem) {
		if (!visitedMarkupItem.contains(markupitem)) {
			visitedMarkupItem.add(markupitem);	
			
			java.util.Collection<EARMARKChildNode> children = null;
			
			if (markupitem.isSet()) {
				children = findChildrenInASet(markupitem);
			} else if (markupitem.isBag()) {
				children = findChildrenInABag(markupitem);
			} else { /* is list */
				children = findChildrenInAList(markupitem);
			}
			
			for (EARMARKChildNode child : children) {
				markupitem.appendChild(child);
				if (child instanceof MarkupItem) {
					appendChildren((MarkupItem) child, visitedMarkupItem);
				}
			}
		}
	}
	
	private Set<EARMARKChildNode> findChildrenInASet(MarkupItem markupitem) {
		Set<EARMARKChildNode> result = new HashSet<EARMARKChildNode>();
		
		Resource resource = model.createResource(markupitem.hasId().toString());
		model.removeAll(resource, psize_r, null);
		
		StmtIterator ite = resource.listProperties(pelement_r);
		while (ite.hasNext()) {
			RDFNode object = ite.next().getObject();
			try {
				result.add((EARMARKChildNode) document.getEntityById(object.asNode().getURI()));
				model.removeAll(resource, pelement_r, object);
			} catch (Exception e) {
				getLogger().warning(object + " has not been added as child of " +
						resource + " [in 'findChildrenInASet' method]\nException: " + e.getMessage());
			}
		}
		
		return result;
	}
	
	private List<EARMARKChildNode> findChildrenInABag(MarkupItem markupitem) {
		List<EARMARKChildNode> result = new ArrayList<EARMARKChildNode>();
		
		Resource resource = model.createResource(markupitem.hasId().toString());
		model.removeAll(resource, psize_r, null);
		
		StmtIterator ite = resource.listProperties(pitem_r);
		while (ite.hasNext()) {
			Resource item = (Resource) ite.next().getObject();
			try {
				Statement content = item.getProperty(pitemcontent_r);
				model.removeAll(resource, pelement_r, item);
				model.removeAll(resource, pitem_r, item);
				model.removeAll(item, RDF.type, item_r);
				
				if (content != null) {
					RDFNode object = content.getObject();
					result.add(
							(EARMARKChildNode) document.getEntityById(object.asNode().getURI()));
					model.removeAll(item, pitemcontent_r, object);
				}
				
				removeOWLStandardStatements(item);
			} catch (Exception e) {
				getLogger().warning("A node (with bag item " + item + ") has not been added as child of " +
						resource + " [in 'findChildrenInABag' method]\nException: " + e.getMessage());
			}
		}
		
		return result;
	}
	
	private List<EARMARKChildNode> findChildrenInAList(MarkupItem markupitem) {
		List<EARMARKChildNode> result = new ArrayList<EARMARKChildNode>();
		
		Resource resource = model.createResource(markupitem.hasId().toString());
		model.removeAll(resource, psize_r, null);
		
		Statement firstItemStatement = resource.getProperty(pfirstitem_r);
		if (firstItemStatement != null) {
			Resource firstitem = (Resource) firstItemStatement.getObject();
			try {
				model.removeAll(resource, pfirstitem_r, null);
				model.removeAll(resource, plastitem_r, null);
				
				Stack<Resource> toVisit = new Stack<Resource>(); toVisit.push(firstitem);
				
				while (!toVisit.isEmpty()) {
					Resource item = toVisit.pop();
					Statement content = item.getProperty(pitemcontent_r);
					model.removeAll(resource, pelement_r, item);
					model.removeAll(resource, pitem_r, item); //TODO
					model.removeAll(item, RDF.type, item_r);
					model.removeAll(item, RDF.type, listitem_r);
					
					if (content != null) {
						result.add(
								(EARMARKChildNode) document.getEntityById(
										content.getObject().asNode().getURI()));
						model.removeAll(item, pitemcontent_r, null);
					}
					
					Statement nextItem = item.getProperty(pnextitem_r);
					if (nextItem != null) {
						toVisit.push((Resource)nextItem.getObject());
					}
					model.removeAll(item, pnextitem_r, null);
					model.removeAll(resource, pfollowedby_r, null);
					model.removeAll(null, ppreceededby_r, resource);
					removeOWLStandardStatements(item);
				}
			} catch (Exception e) {
				getLogger().warning("A node (with referred first item " + firstitem + ") has not been added as child of " +
						resource + " [in 'findChildrenInAList' method]\nException: " + e.getMessage());
			}
		}
		
		return result;
	}
	
	private Set<EARMARKChildNode> setRoots() {
		Set<EARMARKChildNode> result = new HashSet<EARMARKChildNode>();
		
		Iterator<EARMARKChildNode> nodes = document.getAllEARMARKNode().iterator();
		while (nodes.hasNext()) {
			EARMARKChildNode node = nodes.next();
			Resource resource = model.createResource(node.hasId().toString());
			Set<Resource> contents = model.listResourcesWithProperty(pitemcontent_r, resource).toSet();
			contents.addAll(model.listResourcesWithProperty(pelement_r, resource).toSet());
			if (contents.isEmpty()) {
				result.add(node);
				document.appendChild(node);
			} else {
				Iterator<Resource> itemContents = contents.iterator();
				while (itemContents.hasNext()) {
					Resource item = itemContents.next();
					if (!visited.contains(item) && !isItemInMarkupItem(item, new HashSet<Resource>())) {
						result.add(node);
						document.appendChild(node);
					}
				}
			}
		}
		
		return result;
	}
	
	private boolean isItemInMarkupItem(Resource item, Set<Resource> visitedItems) {
		if (!visitedItems.contains(item)) {
			visitedItems.add(item);
			
			Set<Resource> subjects = model.listResourcesWithProperty(pitem_r, item).toSet();
			subjects.addAll(model.listResourcesWithProperty(pfirstitem_r, item).toSet());
			subjects.addAll(model.listResourcesWithProperty(plastitem_r, item).toSet());
			Iterator<Resource> subjectsIte = subjects.iterator();
			while (subjectsIte.hasNext()) {
				Resource resource = subjectsIte.next();
				if (
						visited.contains(resource) && 
						document.getEntityById(resource.getURI()) instanceof MarkupItem) {
					return true;
				}
			}
			
			Set<Resource> objects = model.listResourcesWithProperty(pnextitem_r, item).toSet();
			NodeIterator nodes = model.listObjectsOfProperty(item, ppreviousitem_r);
			while (nodes.hasNext()) {
				RDFNode node = nodes.next();
				if (node.isResource()) {
					objects.add((Resource) node);
				}
			}
			Iterator<Resource> objectsIte = objects.iterator();
			while (objectsIte.hasNext()) {
				if (isItemInMarkupItem(objectsIte.next(), visitedItems)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private void apply(List<URI> orderedURIs) { // Ritorna gli statement considerati
		for (URI uri : orderedURIs) {
			Resource currentTypeClass = model.createResource(uri.toString());
			
			Set<Resource> resources = model.listResourcesWithProperty(RDF.type, currentTypeClass).toSet();
			
			resources.addAll(getResourceByProperties(currentTypeClass));
			
			Iterator<Resource> ite = resources.iterator();
			while (ite.hasNext()) {
				forwardMethodRequest(ite.next(), currentTypeClass);
			}
		}
	}
	
	private Set<Resource> getResourceByProperties(Resource type) {
		Set<Resource> result = new HashSet<Resource>();
		
		if (
				type.equals(docuverse_r) ||
				type.equals(stringdocuverse_r) ||
				type.equals(uridocuverse_r)) {
			ResIterator resources = model.listResourcesWithProperty(phascontent_r);
			while (resources.hasNext()) {
				Resource resource = resources.next();
				boolean isURI = 
					resource.getProperty(phascontent_r).getObject().asNode().getLiteralDatatype().equals(
							XSDDatatype.XSDanyURI);
				
				Set<RDFNode> resourceTypes = model.listObjectsOfProperty(resource, RDF.type).toSet();
				if (type.equals(uridocuverse_r) && isURI 
						&& !resourceTypes.contains(stringdocuverse_r)) {
					result.add(resource);
				} else if (type.equals(stringdocuverse_r) && !isURI 
						&& !resourceTypes.contains(uridocuverse_r)) {
					result.add(resource);
				} else if (type.equals(docuverse_r) 
						&& !resourceTypes.contains(uridocuverse_r) 
						&& !resourceTypes.contains(stringdocuverse_r)) {
					result.add(resource);
				}
			}
		} else if (
				type.equals(range_r) ||
				type.equals(pointerrange_r)) {
			Set<Resource> resources = model.listResourcesWithProperty(pbegins_r).toSet();
			resources.addAll(model.listResourcesWithProperty(pends_r).toSet());
			resources.addAll(model.listResourcesWithProperty(prefersto_r).toSet());
			
			for (Resource resource : resources) {
				boolean isWithContext = 
					!model.listObjectsOfProperty(resource, phasxpathcontext_r).toSet().isEmpty();
				Set<RDFNode> resourceTypes = model.listObjectsOfProperty(resource, RDF.type).toSet();
				
				if (
						!isWithContext && 
						!resourceTypes.contains(xpathrange_r) &&
						!resourceTypes.contains(xpathpointerrange_r)) {
					result.add(resource);
				}
			}
		} else if (
				type.equals(xpathrange_r) ||
				type.equals(xpathpointerrange_r)) {
			result.addAll(model.listResourcesWithProperty(phasxpathcontext_r).toSet());
		} else if (
				type.equals(markupitem_r) ||
				type.equals(element_r) ||
				type.equals(attribute_r) ||
				type.equals(comment_r)) {
			Set<Resource> resources = model.listResourcesWithProperty(phasnamespace_r).toSet();
			resources.addAll(model.listResourcesWithProperty(phasgeneralidentifier_r).toSet());
			
			for (Resource resource : resources) {
				Set<RDFNode> resourceTypes = model.listObjectsOfProperty(resource, RDF.type).toSet();
				
				if (
						(type.equals(element_r)) && 
						!resourceTypes.contains(attribute_r) &&
						!resourceTypes.contains(comment_r)) {
					result.add(resource);
				} else if (
						(type.equals(attribute_r)) && 
						!resourceTypes.contains(element_r) &&
						!resourceTypes.contains(comment_r)) {
					result.add(resource);
				} else if (
						(type.equals(comment_r)) && 
						!resourceTypes.contains(attribute_r) &&
						!resourceTypes.contains(element_r)) {
					result.add(resource);
				} else if (
						(type.equals(markupitem_r)) && 
						!resourceTypes.contains(element_r) &&
						!resourceTypes.contains(attribute_r) &&
						!resourceTypes.contains(comment_r)) {
					result.add(resource);
				}
			}
		}
		
		return result;
	}
	
	private EARMARKItem forwardMethodRequest(Resource resource, Resource type) {
		EARMARKItem result = null;
		
		try {
			if (type.equals(docuverse_r)) {
				result = readDocuverse(resource);
				removeDocuverseStatements(result);
			} else if (type.equals(stringdocuverse_r)) {
				result = readStringDocuverse(resource);
				removeDocuverseStatements(result);
			} else if (type.equals(uridocuverse_r)) {
				result = readURIDocuverse(resource);
				removeDocuverseStatements(result);
			} else if (type.equals(range_r)) {
				result = readRange(resource);
				removeRangeStatements(result);
				visited.add(resource);
			} else if (type.equals(pointerrange_r)) {
				result = readPointerRange(resource);
				removeRangeStatements(result);
				visited.add(resource);
			} else if (type.equals(xpathrange_r)) {
				result = readXPathRange(resource);
				removeRangeStatements(result);
				visited.add(resource);
			} else if (type.equals(xpathpointerrange_r)) {
				result = readXPathPointerRange(resource);
				removeRangeStatements(result);
				visited.add(resource);
			} else if (type.equals(markupitem_r)) {
				result = readMarkupItem(resource);
				removeMarkupItemStatements(result);
				visited.add(resource);
			} else if (type.equals(element_r)) {
				result = readElement(resource);
				removeMarkupItemStatements(result);
				visited.add(resource);
			} else if (type.equals(attribute_r)) {
				result = readAttribute(resource);
				removeMarkupItemStatements(result);
				visited.add(resource);
			} else if (type.equals(comment_r)) {
				result = readComment(resource);
				removeMarkupItemStatements(result);
				visited.add(resource);
			}
		} catch (NullPointerException e) {
			getLogger().warning("The resource " + resource + " has not been added to the EARMARK document" 
					+ " [in 'forwardMethodRequest' method]\nException: " + e.getMessage());
		}
		
		return result;
	}
	
	private PointerRange readPointerRange(Resource resource) {
		PointerRange result = null;
		
		Map<String, Object> map = addRangeProperties(resource);
		
		try {
			result = document.createPointerRange(
					(URI) map.get("id"), 
					(Docuverse) map.get("refersTo"), 
					(Integer) map.get("begins"), 
					(Integer) map.get("ends"));
		} catch (NullPointerException e) {
			getLogger().warning("The resource " + resource + " has not been added to the EARMARK document" 
					+ " [in 'readPointerRange' method]\nException: " + e.getMessage());
		} catch (EARMARKGraphException e) {
			getLogger().warning("The resource " + resource + " has not been added to the EARMARK document" 
					+ " [in 'readPointerRange' method]\nException: " + e.getMessage());
		} catch (ExistingIdException e) {
			getLogger().warning("The resource " + resource + " has not been added to the EARMARK document" 
					+ " [in 'readPointerRange' method]\nException: " + e.getMessage());
		}
		
		return result;
	}
	
	private XPathPointerRange readXPathPointerRange(Resource resource) {
		XPathPointerRange result = null;
		
		Map<String, Object> map = addRangeProperties(resource);
		
		Statement hasXPathContext = resource.getProperty(phasxpathcontext_r);
		try {
			map.put("hasXPathContext", hasXPathContext.getObject().toString());
		} catch (Exception e) {
			map.put("hasXPathContext", null);
		}
		
		try {
			result = document.createXPathPointerRange(
					(URI) map.get("id"), 
					(Docuverse) map.get("refersTo"),
					(Integer) map.get("begins"), 
					(Integer) map.get("ends"),
					(String) map.get("hasXPathContext"));
		} catch (NullPointerException e) {
			getLogger().warning("The resource " + resource + " has not been added to the EARMARK document" 
					+ " [in 'readXPathPointerRange' method]\nException: " + e.getMessage());
		} catch (EARMARKGraphException e) {
			getLogger().warning("The resource " + resource + " has not been added to the EARMARK document" 
					+ " [in 'readXPathPointerRange' method]\nException: " + e.getMessage());
		} catch (ExistingIdException e) {
			getLogger().warning("The resource " + resource + " has not been added to the EARMARK document" 
					+ " [in 'readXPathPointerRange' method]\nException: " + e.getMessage());
		}
		
		return result;
	}
	
	private Map<String, Object> addRangeProperties(Resource resource) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		result.put("id", getId(resource));
		
		Statement begins = resource.getProperty(pbegins_r);
		try {
			result.put("begins", new Integer (begins.getString()));
		} catch (Exception e) {
			result.put("begins", null);
		}
		
		Statement ends = resource.getProperty(pends_r);
		try {
			result.put("ends", new Integer (ends.getString()));
		} catch (Exception e) {
			result.put("ends", null);
		}
		
		Statement refersTo = resource.getProperty(prefersto_r);
		try {
			result.put("refersTo", document.getEntityById(refersTo.getObject().asNode().getURI()));
		} catch (Exception e) {
			result.put("refersTo", null);
		}
		
		return result;
	}
	
	private Range readRange(Resource resource) {
		Range result = null;
		
		Statement hasXPathContext = resource.getProperty(phasxpathcontext_r);
		if (hasXPathContext == null) {
			result = readPointerRange(resource);
		} else {
			result = readXPathRange(resource);
		}
		
		return result;
	}
	
	private Range readXPathRange(Resource resource) {
		return readXPathPointerRange(resource);
	}

	private Element readElement(Resource resource) {
		Element result = null;
		
		Map<String, Object> map = addMarkupItemProperties(resource);
		
		try {
			result = document.createElement(
					(URI) map.get("id"),
					(String) map.get("hasGeneralIdentifier"),
					(URI) map.get("hasNamespace"),
					(Collection.Type) map.get("type"));
		} catch (NullPointerException e) {
			getLogger().warning("The resource " + resource + " has not been added to the EARMARK document" 
					+ " [in 'readElement' method]\nException: " + e.getMessage());
		} catch (EARMARKGraphException e) {
			getLogger().warning("The resource " + resource + " has not been added to the EARMARK document" 
					+ " [in 'readElement' method]\nException: " + e.getMessage());
		} catch (ExistingIdException e) {
			getLogger().warning("The resource " + resource + " has not been added to the EARMARK document" 
					+ " [in 'readElement' method]\nException: " + e.getMessage());
		}
		
		return result;
	}
	
	private Attribute readAttribute(Resource resource) {
		Attribute result = null;
		
		Map<String, Object> map = addMarkupItemProperties(resource);
		
		try {
			result = document.createAttribute(
					(URI) map.get("id"),
					(String) map.get("hasGeneralIdentifier"),
					(URI) map.get("hasNamespace"),
					(Collection.Type) map.get("type"));
		} catch (NullPointerException e) {
			getLogger().warning("The resource " + resource + " has not been added to the EARMARK document" 
					+ " [in 'readAttribute' method]\nException: " + e.getMessage());
		} catch (EARMARKGraphException e) {
			getLogger().warning("The resource " + resource + " has not been added to the EARMARK document" 
					+ " [in 'readAttribute' method]\nException: " + e.getMessage());
		} catch (ExistingIdException e) {
			getLogger().warning("The resource " + resource + " has not been added to the EARMARK document" 
					+ " [in 'readAttribute' method]\nException: " + e.getMessage());
		}
		
		return result;
	}
	
	private Comment readComment(Resource resource) {
		Comment result = null;
		
		Map<String, Object> map = addMarkupItemProperties(resource);
		
		try {
			result = document.createComment(
					(URI) map.get("id"),
					(String) map.get("hasGeneralIdentifier"),
					(URI) map.get("hasNamespace"),
					(Collection.Type) map.get("type"));
		} catch (NullPointerException e) {
			getLogger().warning("The resource " + resource + " has not been added to the EARMARK document" 
					+ " [in 'readComment' method]\nException: " + e.getMessage());
		} catch (EARMARKGraphException e) {
			getLogger().warning("The resource " + resource + " has not been added to the EARMARK document" 
					+ " [in 'readComment' method]\nException: " + e.getMessage());
		} catch (ExistingIdException e) {
			getLogger().warning("The resource " + resource + " has not been added to the EARMARK document" 
					+ " [in 'readComment' method]\nException: " + e.getMessage());
		}
		
		return result;
	}
	
	private Map<String, Object> addMarkupItemProperties(Resource resource) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		result.put("id", getId(resource));
		
		Statement hasNamespace = resource.getProperty(phasnamespace_r);
		try {
			result.put("hasNamespace", URI.create(hasNamespace.getString()));
		} catch (Exception e) {
			result.put("hasNamespace", null);
		}
		
		Statement hasGeneralIdentifier = resource.getProperty(phasgeneralidentifier_r);
		try {
			result.put("hasGeneralIdentifier", hasGeneralIdentifier.getString());
		} catch (Exception e) {
			result.put("hasGeneralIdentifier", null);
		}
		
		result.put("type", getType(resource));
		
		return result;
	}
	
	private Collection.Type getType(Resource resource) {
		Collection.Type result = null;
		
		StmtIterator ite = resource.listProperties(RDF.type);
		while (ite.hasNext() && result == null) {
			RDFNode object = ite.next().getObject();
			if (object.isURIResource()) {
				URI currentType = URI.create(object.asNode().getURI());
				
				if (currentType.equals(EARMARKClassURI.LIST)) {
					result = Collection.Type.List;
				} else if (currentType.equals(EARMARKClassURI.BAG)) {
					result = Collection.Type.Bag;
				} else if (currentType.equals(EARMARKClassURI.SET)) {
					result = Collection.Type.Set;
				}
			}
		}
		
		if (result == null) {
			if (resource.getProperty(pfirstitem_r) != null || resource.getProperty(plastitem_r) != null) {
				result = Collection.Type.List;
			} else if (resource.getProperty(pitem_r) != null) {
				result = Collection.Type.Bag;
			} else if (resource.getProperty(pelement_r) != null) {
				result = Collection.Type.Set;
			} else { // Default without explicit declaration and no child
				result = Collection.Type.List;
			}
		}
		
		return result;
	}
	
	private MarkupItem readMarkupItem(Resource resource) {
		return readElement(resource);
	}
	
	private StringDocuverse readStringDocuverse(Resource resource) {
		StringDocuverse result = null;
		
		Map<String, Object> map = addDocuverseProperties(resource);
		
		try {
			result = document.createStringDocuverse(
					(URI) map.get("id"),
					(String) map.get("hasContent"));
		} catch (NullPointerException e) {
			getLogger().warning("The resource " + resource + " has not been added to the EARMARK document" 
					+ " [in 'readStringDocuverse' method]\nException: " + e.getMessage());
		} catch (EARMARKGraphException e) {
			getLogger().warning("The resource " + resource + " has not been added to the EARMARK document" 
					+ " [in 'readStringDocuverse' method]\nException: " + e.getMessage());
		} catch (ExistingIdException e) {
			getLogger().warning("The resource " + resource + " has not been added to the EARMARK document" 
					+ " [in 'readStringDocuverse' method]\nException: " + e.getMessage());
		}
		
		return result;
	}
	
	private URIDocuverse readURIDocuverse(Resource resource) {
		URIDocuverse result = null;
		
		Map<String, Object> map = addDocuverseProperties(resource);
		
		try {
			result = document.createURIDocuverse(
					(URI) map.get("id"),
					URI.create((String) map.get("hasContent")));
		} catch (NullPointerException e) {
			getLogger().warning("The resource " + resource + " has not been added to the EARMARK document" 
					+ " [in 'readURIDocuverse' method]\nException: " + e.getMessage());
		} catch (EARMARKGraphException e) {
			getLogger().warning("The resource " + resource + " has not been added to the EARMARK document" 
					+ " [in 'readURIDocuverse' method]\nException: " + e.getMessage());
		} catch (ExistingIdException e) {
			getLogger().warning("The resource " + resource + " has not been added to the EARMARK document" 
					+ " [in 'readURIDocuverse' method]\nException: " + e.getMessage());
		}
		
		return result;
	}
	
	private Map<String, Object> addDocuverseProperties(Resource resource) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		result.put("id", getId(resource));
		
		Statement hasContent = resource.getProperty(phascontent_r);
		try {
			result.put("hasContent", hasContent.getString());
		} catch (Exception e) {
			result.put("hasContent", null);
		}
		
		return result;
	}
	
	private Docuverse readDocuverse(Resource resource) {
		Docuverse result = null;
		
		Statement hasContent = resource.getProperty(phascontent_r);
		
		try {
			if (hasContent.getObject().asNode().getLiteralDatatype().equals(XSDDatatype.XSDanyURI)) {
				result = readURIDocuverse(resource);
			} else {
				result = readStringDocuverse(resource);
			}
		} catch (Exception e) {
			result = readStringDocuverse(resource);
		}
		
		return result;
	}
	
	private URI getId(Resource resource) {
		try {
			return URI.create(resource.getURI());
		} catch (Exception e) {
			return null;
		}
	}
	
	private void removeDocuverseStatements(EARMARKItem item) {
		Resource resource = model.createResource(item.hasId().toString());
		
		model.removeAll(resource, RDF.type, docuverse_r);
		model.removeAll(resource, RDF.type, stringdocuverse_r);
		model.removeAll(resource, RDF.type, uridocuverse_r);
		model.removeAll(resource, phascontent_r, null);
		removeOWLStandardStatements(item);
	}
	
	private void removeRangeStatements(EARMARKItem item) {
		Resource resource = model.createResource(item.hasId().toString());
		
		model.removeAll(resource, RDF.type, range_r);
		model.removeAll(resource, RDF.type, xpathrange_r);
		model.removeAll(resource, RDF.type, pointerrange_r);
		model.removeAll(resource, RDF.type, xpathpointerrange_r);
		model.removeAll(resource, pbegins_r, null);
		model.removeAll(resource, pends_r, null);
		model.removeAll(resource, prefersto_r, null);
		model.removeAll(resource, phasxpathcontext_r, null);
		removeOWLStandardStatements(item);
	}
	
	private void removeMarkupItemStatements(EARMARKItem item) {
		Resource resource = model.createResource(item.hasId().toString());
		
		model.removeAll(resource, RDF.type, markupitem_r);
		model.removeAll(resource, RDF.type, element_r);
		model.removeAll(resource, RDF.type, attribute_r);
		model.removeAll(resource, RDF.type, comment_r);
		model.removeAll(resource, RDF.type, collection_r);
		model.removeAll(resource, RDF.type, set_r);
		model.removeAll(resource, RDF.type, bag_r);
		model.removeAll(resource, RDF.type, list_r);
		model.removeAll(resource, phasnamespace_r, null);
		model.removeAll(resource, phasgeneralidentifier_r, null);
		removeOWLStandardStatements(item);
	}
	
	private void removeOWLStandardStatements(Resource resource) {
		if (removeOWLStandardStatement) {
			Resource namedIndividual = model.createResource("http://www.w3.org/2002/07/owl#NamedIndividual");
			
			model.removeAll(resource, RDF.type, OWL.Ontology);
			model.removeAll(resource, RDF.type, OWL.Thing);
			model.removeAll(resource, RDF.type, namedIndividual);
		}
	}
	
	private void removeOWLStandardStatements(EARMARKItem item) {
		Resource resource = model.createResource(item.hasId().toString());
		removeOWLStandardStatements(resource);
	}
	
	private void initializeResources() {
		/* Initialize the EARMARK classes */
		markupitem_r = model.createResource(EARMARKClassURI.MARKUP_ITEM.toString());
		element_r = model.createResource(EARMARKClassURI.ELEMENT.toString());
		attribute_r = model.createResource(EARMARKClassURI.ATTRIBUTE.toString());
		comment_r = model.createResource(EARMARKClassURI.COMMENT.toString());
		docuverse_r = model.createResource(EARMARKClassURI.DOCUVERSE.toString());
		stringdocuverse_r = model.createResource(EARMARKClassURI.STRING_DOCUVERSE.toString());
		uridocuverse_r = model.createResource(EARMARKClassURI.URI_DOCUVERSE.toString());
		range_r = model.createResource(EARMARKClassURI.RANGE.toString());
		pointerrange_r = model.createResource(EARMARKClassURI.POINTER_RANGE.toString());
		xpathrange_r = model.createResource(EARMARKClassURI.XPATH_RANGE.toString());
		xpathpointerrange_r = model.createResource(EARMARKClassURI.XPATH_POINTER_RANGE.toString());
		set_r = model.createResource(EARMARKClassURI.SET.toString());
		bag_r = model.createResource(EARMARKClassURI.BAG.toString());
		list_r = model.createResource(EARMARKClassURI.LIST.toString());
		item_r = model.createResource(EARMARKClassURI.ITEM.toString());
		listitem_r = model.createResource(EARMARKClassURI.LIST_ITEM.toString());
		collection_r = model.createResource(EARMARKClassURI.COLLECTION.toString());
		
		/* Initialize the EARMARK properties */
		pbegins_r = model.createProperty(EARMARKPropertyURI.BEGINS.toString());
		pends_r = model.createProperty(EARMARKPropertyURI.ENDS.toString());
		prefersto_r = model.createProperty(EARMARKPropertyURI.REFERS_TO.toString());
		phasxpathcontext_r = model.createProperty(EARMARKPropertyURI.HAS_XPATH_CONTEXT.toString());
		phascontent_r = model.createProperty(EARMARKPropertyURI.HAS_CONTENT.toString());
		phasgeneralidentifier_r = model.createProperty(EARMARKPropertyURI.HAS_GENERAL_IDENTIFIER.toString());
		phasnamespace_r = model.createProperty(EARMARKPropertyURI.HAS_NAMESPACE.toString());
		
		pfollowedby_r = model.createProperty(EARMARKPropertyURI.FOLLOWED_BY.toString());
		ppreceededby_r = model.createProperty(EARMARKPropertyURI.PRECEEDED_BY.toString());
		pnextitem_r = model.createProperty(EARMARKPropertyURI.NEXT_ITEM.toString());
		ppreviousitem_r = model.createProperty(EARMARKPropertyURI.PREVIOUS_ITEM.toString());
		pelement_r = model.createProperty(EARMARKPropertyURI.ELEMENT.toString());
		pfirstitem_r = model.createProperty(EARMARKPropertyURI.FIRST_ITEM.toString());
		plastitem_r = model.createProperty(EARMARKPropertyURI.LAST_ITEM.toString());
		pitem_r = model.createProperty(EARMARKPropertyURI.ITEM.toString());
		pitemcontent_r = model.createProperty(EARMARKPropertyURI.ITEM_CONTENT.toString());
		psize_r = model.createProperty(EARMARKPropertyURI.SIZE.toString());
	}
	
	private Logger getLogger() {
		return getLogger(JenaReader.class);
	}
}
