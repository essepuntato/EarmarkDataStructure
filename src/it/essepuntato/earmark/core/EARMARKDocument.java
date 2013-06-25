package it.essepuntato.earmark.core;

import it.essepuntato.earmark.core.exception.EARMARKGraphException;
import it.essepuntato.earmark.core.exception.ExistingIdException;
import it.essepuntato.earmark.core.io.EARMARKReader;
import it.essepuntato.earmark.core.io.EARMARKWriter;
import it.essepuntato.earmark.core.io.JenaReader;
import it.essepuntato.earmark.core.io.JenaWriter;
import it.essepuntato.facility.collection.CollectionFacility;
import it.essepuntato.facility.list.ListFacility;
import it.essepuntato.facility.set.SetFacility;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.sql.rowset.Predicate;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * This class represents an EARMARK document. Is is used to create new markup items (elements,
 * attributes, comments) and ranges (pointer and xpath pointer ranges).
 * 
 * @author Silvio Peroni 
 *
 */
public class EARMARKDocument implements EARMARKItem, EARMARKHierarchicalNode {
	/* Error messages */
	private final String anotherDocumentError = "The entity specified is created using a different " +
			"EARMARK document.";
	private static final String nochildError = "The reference node specified is not a child of the parent node.";
	
	/**
	 * It represents the URI separator for the name of the items when an id is not specified or
	 * it is specified locally.
	 */
	public final static String SEPARATOR = "/";
	
	private static EARMARKReader reader = new JenaReader();

	private static EARMARKWriter writer = new JenaWriter();
	
	private Map<URI, EARMARKItem> idSet = new HashMap<URI, EARMARKItem>();
	
	/* It is used to associate a general identifier to a set of markup item */
	private Map<String, Set<MarkupItem>> giMap = 
		new HashMap<String, Set<MarkupItem>>();
	
	/* It is used to associate a namespace to a set of general identifier */
	private Map<URI,Set<String>> namespaces = 
		new HashMap<URI,Set<String>>();
	
	private URI id;
	
	private Map<EARMARKHierarchicalNode,Collection> childMap = 
		new HashMap<EARMARKHierarchicalNode, Collection>();
	
	private Map<EARMARKChildNode,Set<EARMARKHierarchicalNode>> parentMap = 
		new HashMap<EARMARKChildNode, Set<EARMARKHierarchicalNode>>();
	
	private Map<Docuverse,Set<Range>> docuverseMap = 
		new HashMap<Docuverse,Set<Range>>();
	
	private Map<Integer,Set<Range>> rangeBeginLocationMap = 
		new HashMap<Integer,Set<Range>>();
	
	private Map<Integer,Set<Range>> rangeEndLocationMap = 
		new HashMap<Integer,Set<Range>>();
	
	private Map<String,Set<Range>> rangeXPathPointerMap = 
		new HashMap<String,Set<Range>>();
	
	private Map<EARMARKNode,Map<String,Object>> userData = 
		new HashMap<EARMARKNode,Map<String,Object>>();
	
	private Model rdf = ModelFactory.createDefaultModel();
	
	/* Remove all the information concerning ids and data of a node */
	private void removeIdAndData(EARMARKNode node) {
		userData.remove(node);
		getIdMap().remove(node.hasId());
	}
	
	/* Remove all the hierarchical information of a node from its parent nodes */
	private void removeFromParents(EARMARKChildNode node) {
		Set<EARMARKHierarchicalNode> parents = node.getParentNodes();
		if (parents != null) {
			for (EARMARKHierarchicalNode parent : node.getParentNodes()) {
				parent.removeAllChild(node);
			}
		}
		parentMap.remove(node);
	}
	
	/* Remove all the information of a node concerning its children and apply a
	 * complete and recursive remotion of them (without any check if 'strong' is true,
	 * or removing only the children that don't have any other parent otherwise). */
	private void removeFromChildren(EARMARKHierarchicalNode node, boolean strong) {
		Collection children = node.getChildNodes();
		if (children != null) {
			childMap.remove(node);
			for (EARMARKChildNode child : children) {
				Set<EARMARKHierarchicalNode> childParents = parentMap.get(child);
				childParents.remove(node);
				if (strong || childParents.isEmpty()) {
					if (child instanceof Range) {
						removeRange((Range) child);
					} else {
						removeMarkupItem((MarkupItem) child, strong);
					}
				}
			}
		}
	}
	
	/**
	 * This method removes the range specified from the document. If the range is the only one that refers to
	 * a docuverse, then the docuverse itself will be removed as well.
	 * 
	 * @param range the range to be removed.
	 * @return true if the range is correctly removed, false otherwise (i.e., if the range is not own by
	 * the document).
	 */
	public boolean removeRange(Range range) {
		boolean result = true;
		
		if (equals(range.getOwnerDocument())) {
			removeFromParents(range);
			Set<Range> docuverseSet = docuverseMap.get(range.refersTo());
			if (docuverseSet != null) {
				docuverseSet.remove(range);
				removeDocuverse(range.refersTo());
			}
			
			Integer begin = (Integer) range.begins();
			if (begin == null) {
				begin = -1;
			}
			Set<Range> beginSet = rangeBeginLocationMap.get(begin);
			if (beginSet != null) {
				beginSet.remove(range);
				if (beginSet.isEmpty()) {
					rangeBeginLocationMap.remove(begin);
				}
			}
			
			Integer end = (Integer) range.ends();
			if (end == null) {
				end = -1;
			}
			Set<Range> endSet = rangeEndLocationMap.get(end);
			if (endSet != null) {
				endSet.remove(range);
				if (endSet.isEmpty()) {
					rangeEndLocationMap.remove(end);
				}
			}
			
			if (range.getNodeType() == EARMARKNode.Type.XPathPointerRange) {
				String xpath = ((XPathPointerRange) range).hasXPathContext();
				if (xpath == null) {
					xpath = "";
				}
				Set<Range> xpathSet = rangeXPathPointerMap.get(xpath);
				if (xpathSet != null) {
					xpathSet.remove(range);
					if (xpathSet.isEmpty()) {
						rangeXPathPointerMap.remove(xpath);
					}
				}
			}
			
			removeIdAndData(range);
		} else {
			result = false;
		}
		
		return result;
	}
	
	/**
	 * <p>This method removes a markup item from the document.</p>
	 * <p>Removing a markup item means to remove all its descendant as well, unless they have more than
	 * one parent.</p>
	 * 
	 * @param markupitem the markup item to be removed.
	 * @return true if the markup item is correctly removed, false otherwise (i.e., if the markup item is not own by
	 * the document).
	 */
	public boolean removeMarkupItem(MarkupItem markupitem) {
		return removeMarkupItem(markupitem, false);
	}
	
	/**
	 * <p>This method removes a markup item from the document.</p>
	 * 
	 * @param markupitem the markup item to be removed.
	 * @param strong if true, this method removes all the descendant of the input node without any check.
	 * Otherwise, if false, only the descendants that have one parent only will be removed.
	 * @return true if the markup item is correctly removed, false otherwise (i.e., if the markup item is not own by
	 * the document).
	 */
	protected boolean removeMarkupItem(MarkupItem markupitem, boolean strong) {
		boolean result = true;
		
		if (equals(markupitem.getOwnerDocument())) {
			removeFromParents(markupitem);
			removeFromChildren(markupitem, strong);
			
			String gi = markupitem.hasGeneralIdentifier();
			if (gi == null) {
				gi = "";
			}
			Set<MarkupItem> giSet = giMap.get(gi);
			if (giSet != null) {
				giSet.remove(markupitem);
				if (giSet.isEmpty()) {
					giMap.remove(gi);
				}
			}
			URI namespace = markupitem.hasNamespace();
			if (namespace == null) {
				namespace = URI.create("");
			}
			
			if (getMarkupItemByGeneralIdentifierAndNamespace(gi, namespace).isEmpty()) {
				Set<String> namespaceSet = namespaces.get(namespace);
				namespaceSet.remove(gi);
				if (namespaceSet.isEmpty()) {
					namespaces.remove(namespace);
				}
			}
			
			removeIdAndData(markupitem);
		} else {
			result = false;
		}
		
		return result;
	}
	
	/**
	 * This method allows to get an EARMARK item through its id.
	 * 
	 * @param id the identifier of the item to be returned.
	 * @return the item related to the input id.
	 */
	public EARMARKItem getEntityById(URI id) {
		try {
			return idSet.get(id);
		} catch (NullPointerException e) {
			return null;
		}
	}
	
	/**
	 * <p>This method allows to get an EARMARK item through its id.</p>
	 * <p>When the string do not comply with the
	 * complete URL syntax (i.e., [protocol] + :// + domain + resource path), the string will be completed
	 * automatically with the path of the current document id. For instance, if the input string is "p14" and
	 * the document id is the URI "http://www.example.com/mydocument", then the resulting id considered by
	 * this method will be "http://www.example.com/mydocument" + SEPARATOR + "p14".</p>
	 * 
	 * @param id the complete string for the URI identifying of the item to be returned.
	 * @return the item related to the input id.
	 */
	public EARMARKItem getEntityById(String id) {
		return getEntityById(getURI(id));
	}
	
	/**
	 * <p>This method returns a set of markup items having a particular general identifier associated.</p>
	 * <p>If the general identifier specified is null or "", the method looks for all the entities that do not
	 * have any general identifier specified.</p>
	 * 
	 * @param gi the general identifier for the entities to be retrieved.
	 * @return a set of markup items having the input general identifier specified.
	 */
	public Set<MarkupItem> getMarkupItemByGeneralIdentifier(String gi) {
		Set<MarkupItem> result = new HashSet<MarkupItem>();
		String currentGi = (gi == null ? "" : gi);
		
		Set<MarkupItem> tmp = giMap.get(currentGi);
		
		try {
			result.addAll(tmp);
		} catch (NullPointerException e) {
			// Do nothing
		}
		
		return result;
	}
	
	/**
	 * <p>This method returns a set of markup items having a particular general identifier and
	 * namespace associated.</p>
	 * <p>If the general identifier specified is null or "", the method looks for all the entities that do not
	 * have any general identifier specified. In the same way, if the namespace is null or "", the method looks
	 * for all the entities that do not have any namespace specified.</p>
	 * 
	 * @param gi the general identifier for the entities to be retrieved.
	 * @param ns the namespace for the entities to be retrieved.
	 * @return  a set of markup items having the input general identifier and the namespace specified.
	 */
	public Set<MarkupItem> getMarkupItemByGeneralIdentifierAndNamespace(String gi, URI ns) {
		Set<MarkupItem> result = new HashSet<MarkupItem>();
		
		String currentGi = (gi == null ? "" : gi);
		URI currentNs = (ns == null ? URI.create("") : ns);
		
		Set<String> gisWithNamespace = namespaces.get(currentNs);
		try {
			if (gisWithNamespace.contains(currentGi)) {
				Set<MarkupItem> tmp = getMarkupItemByGeneralIdentifier(currentGi);
				for (MarkupItem mi : tmp) {
					URI miNs = mi.hasNamespace();
					URI currentMiNs = (miNs == null ? URI.create("") : miNs);
					if (currentMiNs.equals(currentNs)) {
						result.add(mi);
					}
				}
			}
		} catch (NullPointerException e) {
			// Do nothing
		}
		
		return result;
	}
	
	/**
	 * Create an empty EARMARKDocument.
	 * 
	 * @param uri The URI to associate to the document.
	 */
	public EARMARKDocument(URI uri) {
		id = uri;
		
		childMap.put(this, new it.essepuntato.earmark.core.Set());
		userData.put(this, new HashMap<String,Object>());
	}
	
	/**
	 * <p>This method returns the I/O reader processor currently set for this class. The processor
	 * will be used for converting a source EARMARK document into its Java representation.</p>
	 * <p>By default, the reader is a JenaReader.</p>
	 * 
	 * @return the reader processor for converting source EARMARK documents into their Java representations.
	 */
	public static EARMARKReader getReader() {
		return reader;
	}

	/**
	 * This method allows to set a particular I/O reader processor that will be used for converting a
	 * source EARMARK document into its Java representation. 
	 * 
	 * @param reader the I/O reader to be considered.
	 */
	public static void setReader(EARMARKReader reader) {
		EARMARKDocument.reader = reader;
	}

	/**
	 * <p>This method returns the I/O writer processor currently set for this class. The processor
	 * will be used for converting a Java representation of an EARMARK document into a particular format,
	 * chosen from the set of formats handled by the writer itself.</p>
	 * <p>By default, the writer is a JenaWriter.</p>
	 * 
	 * @return the writer processor for converting Java representations of an EARMARK documents 
	 * into particular formats.
	 */
	public static EARMARKWriter getWriter() {
		return writer;
	}

	/**
	 * This method allows to set a particular I/O writer processor that will be used for converting a Java 
	 * representation of an EARMARK document into a particular format,
	 * chosen from the set of formats handled by the writer itself
	 * 
	 * @param writer the I/O writer to be considered.
	 */
	public static void setWriter(EARMARKWriter writer) {
		EARMARKDocument.writer = writer;
	}
	
	@Override
	public boolean isList() {
		return false;
	}
	
	@Override
	public boolean isBag() {
		return false;
	}
	
	@Override
	public boolean isSet() {
		return true;
	}
	
	/**
	 * This method returns a set of all the EARMARK nodes (elements, attributes, comments
	 * and ranges) of the document.
	 * 
	 * @return a set of all the EARMARK nodes of the document.
	 */
	public Set<EARMARKChildNode> getAllEARMARKNode() {
		return new HashSet<EARMARKChildNode>(parentMap.keySet());
	}
	
	/**
	 * This method returns a set of all the ids of all the items described by the EARMARK document.
	 * 
	 * @return a set containing all the ids of all the items of the EARMARK document.
	 */
	public Set<URI> getIdPool() {
		return new HashSet<URI>(idSet.keySet());
	}
	
	/**
	 * <p>This method returns all the ids of the EARMARK document. Each modification to this set is not propagated
	 * to all the structures involved and, consequently, may result in wrong behaviours and/or
	 * collateral effects.</p>
	 * <p>It is usually reserved for internal use only.</p>
	 * 
	 * @return all the ids of the EARMARK document.
	 */
	protected Map<URI, EARMARKItem> getIdMap() {
		return idSet;
	}
	
	/**
	 * <p>This method changes the local id of an item with a new one. 
	 * Each modification to the id is not propagated
	 * to all the structures involved and, consequently, may result in wrong behaviours and/or
	 * collateral effects.</p>
	 * <p>It is usually reserved for internal use only.</p>
	 * 
	 * @param item the item in which we want to change the id.
	 * @param newId the new id for the input item.
	 * @return true if the id is changed, false otherwise (i.e., the new id is the same of the old one).
	 */
	protected boolean setId(EARMARKItem item, URI newId) {
		if (!newId.equals(hasId())) {
			idSet.remove(item.hasId()); //Remove the old id and...
			idSet.put(newId, item); //... add the new one
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * This method returns all the docuverses of the document.
	 * 
	 * @return all the docuverses of the document.
	 */
	public Set<Docuverse> getAllDocuverses() {
		return new HashSet<Docuverse>(docuverseMap.keySet());
	}
	
	/**
	 * This method creates a new string docuverse.
	 * 
	 * @param content the string content of the docuverse.
	 * @return a new string docuverse.
	 */
	public StringDocuverse createStringDocuverse(String content) {
		StringDocuverse result = null;
		
		URI id = makeId("docuverse");
		try {
			result = createStringDocuverse(id, content);
		} catch (ExistingIdException e) {
			// It shouldn't be possible to come here...
		}
		
		return result;
	}
	
	/**
	 * This method creates a new string docuverse.
	 * 
	 * @param id the identifier for the new docuverse.
	 * @param content the string content of the new docuverse.
	 * @return a new string docuverse.
	 * @throws ExistingIdException if the identifier specified is already used in the document.
	 */
	public StringDocuverse createStringDocuverse(URI id, String content) 
	throws ExistingIdException{
		checkIdentity(id);
		StringDocuverse docuverse = new StringDocuverse(this, content, id);
		idSet.put(id, docuverse);
		docuverseMap.put(docuverse, new HashSet<Range>());
		return docuverse;
	}
	
	/**
	 * <p>This method creates a new string docuverse.</p>
	 * <p>When the id do not comply with the
	 * complete URL syntax (i.e., [protocol] + :// + domain + resource path), the id will be completed
	 * automatically with the path of the current document id. For instance, if the input id string is "p14" and
	 * the document id is the URI "http://www.example.com/mydocument", then the resulting id string considered by
	 * this method will be "http://www.example.com/mydocument" + SEPARATOR + "p14".</p>
	 * 
	 * @param id the identifier of the new docuverse.
	 * @param content the string content of the new docuverse.
	 * @return a new string docuverse.
	 * @throws ExistingIdException if the identifier specified is already used in the document.
	 */
	public StringDocuverse createStringDocuverse(String id, String content) 
	throws ExistingIdException{
		return createStringDocuverse(getURI(id), content);
	}
	
	/**
	 * This method creates a new URI docuverse.
	 * 
	 * @param uri the URI where the content of the new docuverse is contained.
	 * @return a new URI docuverse.
	 */
	public URIDocuverse createURIDocuverse(URI uri) {
		URIDocuverse result = null;
		
		URI id = makeId("docuverse");
		try {
			result = createURIDocuverse(id, uri);
		} catch (ExistingIdException e) {
			// It shouldn't be possible to come here...
		}
		
		return result;
	}
	
	/**
	 * This method creates a new URI docuverse.
	 * 
	 * @param id the identifier of the new docuverse.
	 * @param uri the URI where the content of the new docuverse is contained.
	 * @return a new URI docuverse.
	 * @throws ExistingIdException if the identifier specified is already used in the document.
	 */
	public URIDocuverse createURIDocuverse(URI id, URI uri) throws ExistingIdException {
		checkIdentity(id);
		URIDocuverse docuverse = new URIDocuverse(this, uri, id);
		idSet.put(id, docuverse);
		docuverseMap.put(docuverse, new HashSet<Range>());
		return docuverse;
	}
	
	/**
	 * <p>This method creates a new URI docuverse.</p>
	 * <p>When the id do not comply with the
	 * complete URL syntax (i.e., [protocol] + :// + domain + resource path), the id will be completed
	 * automatically with the path of the current document id. For instance, if the input id string is "p14" and
	 * the document id is the URI "http://www.example.com/mydocument", then the resulting id string considered by
	 * this method will be "http://www.example.com/mydocument" + SEPARATOR + "p14".</p>
	 * 
	 * @param id the identifier of the new docuverse.
	 * @param uri the URI where the content of the new docuverse is contained.
	 * @return a new URI docuverse.
	 * @throws ExistingIdException if the identifier specified is already used in the document.
	 */
	public URIDocuverse createURIDocuverse(String id, URI uri) throws ExistingIdException {
		return createURIDocuverse(getURI(id), uri);
	}
	
	/* It add all the information about ranges into the internal structures of the document */
	private void addRangeData(Range r) {
		Docuverse docuverse = r.refersTo();
		Integer begin = (Integer) r.begins();
		Integer end = (Integer) r.ends();
		String xpath = null;
		
		if (docuverse != null) {
			docuverseMap.get(docuverse).add(r);
		}
		
		if (begin == null) {
			begin = -1; /* To be indexed */
		}
		Set<Range> beginSet = rangeBeginLocationMap.get(begin);
		if (beginSet == null) {
			beginSet = new HashSet<Range>();
			rangeBeginLocationMap.put(begin, beginSet);
		}
		beginSet.add(r);
		
		if (end == null) { 
			end = -1; /* To be indexed */
		}
		Set<Range> endSet = rangeEndLocationMap.get(end);
		if (endSet == null) {
			endSet = new HashSet<Range>();
			rangeEndLocationMap.put(end, endSet);
		}
		endSet.add(r);
		
		if (r instanceof XPathRange) {
			xpath = ((XPathRange)r).hasXPathContext();
			if (xpath == null) {
				xpath = "";
			}
			Set<Range> xpathSet = rangeXPathPointerMap.get(xpath);
			if (xpathSet == null) {
				xpathSet = new HashSet<Range>();
				rangeXPathPointerMap.put(xpath, xpathSet);
			}
			xpathSet.add(r);
		}
	}
	
	/**
	 * <p>This method creates a new pointer range between two location.</p>
	 * <p>If, in the EARMARK document, a range with same begin and end locations 
	 * and same docuverse exists, this method returns it.</p>
	 * 
	 * @param docuverse the docuverse the range refers to.
	 * @param begin the begin location of the range.
	 * @param end the end location of the range.
	 * @return a new pointer range.
	 */
	public PointerRange createPointerRange(Docuverse docuverse, Integer begin, Integer end) {
		PointerRange result = null;
		
		URI id = makeId("r");
		try {
			result = createPointerRange(id, docuverse, begin, end);
		} catch (ExistingIdException e) {
			// It shouldn't be possible to come here...
		}
		
		return result;
	}
	
	/**
	 * <p>This method creates a new pointer range between two location.</p>
	 * <p>If, in the EARMARK document, a range with same begin and end locations 
	 * and same docuverse exists, this method returns it.</p>
	 * <p>When the id do not comply with the
	 * complete URL syntax (i.e., [protocol] + :// + domain + resource path), the id will be completed
	 * automatically with the path of the current document id. For instance, if the input id string is "p14" and
	 * the document id is the URI "http://www.example.com/mydocument", then the resulting id string considered by
	 * this method will be "http://www.example.com/mydocument" + SEPARATOR + "p14".</p>
	 * 
	 * @param id the identifier for the new range.
	 * @param docuverse the docuverse the range refers to.
	 * @param begin the begin location of the range.
	 * @param end the end location of the range.
	 * @return a new pointer range.
	 * @throws ExistingIdException if the identifier specified is already used in the document.
	 * @throws EARMARKGraphException if the docuverse specified was created using
	 * different document.
	 */
	public PointerRange createPointerRange(String id, Docuverse docuverse, Integer begin, Integer end) 
	throws ExistingIdException, EARMARKGraphException {
		return createPointerRange(getURI(id), docuverse, begin, end);
	}
	
	/**
	 * <p>This method creates a new pointer range between two location.</p>
	 * <p>If, in the EARMARK document, a range with same begin and end locations 
	 * and same docuverse exists, this method returns it.</p>
	 * 
	 * @param id the identifier for the new range.
	 * @param docuverse the docuverse the range refers to.
	 * @param begin the begin location of the range.
	 * @param end the end location of the range.
	 * @return a new pointer range.
	 * @throws ExistingIdException if the identifier specified is already used in the document.
	 * @throws EARMARKGraphException if the docuverse specified was created using
	 * different document.
	 */
	public PointerRange createPointerRange(URI id, Docuverse docuverse, Integer begin, Integer end) 
	throws ExistingIdException, EARMARKGraphException {
		checkIdentity(id);
		
		if (equals(docuverse.getEARMARKDocument())) {
			Range range = getWhetherItExists(docuverse, begin, end, null);
			
			if (range == null) {
				range = new PointerRange(this, docuverse, begin, end, id);
				idSet.put(id, range);
				addRangeData(range);
				userData.put(range, new HashMap<String,Object>());
				parentMap.put(range, new HashSet<EARMARKHierarchicalNode>());
			}
			
			return (PointerRange) range;
			
		} else {
			throw new EARMARKGraphException(EARMARKGraphException.Type.WRONG_DOCUMENT_ERR, anotherDocumentError);
		}
		
	}
	
	/**
	 * <p>This method creates a new xpath pointer range between two location.</p>
	 * <p>If, in the EARMARK document, a range with same begin and end locations, 
	 * same docuverse and same xpath expression exists, this method returns it.</p>
	 * <p>When the id do not comply with the
	 * complete URL syntax (i.e., [protocol] + :// + domain + resource path), the id will be completed
	 * automatically with the path of the current document id. For instance, if the input id string is "p14" and
	 * the document id is the URI "http://www.example.com/mydocument", then the resulting id string considered by
	 * this method will be "http://www.example.com/mydocument" + SEPARATOR + "p14".</p>
	 * 
	 * @param id the identifier for the new range.
	 * @param docuverse the docuverse the range refers to.
	 * @param begin the begin location of the range.
	 * @param end the end location of the range.
	 * @param xpath the xpath expression to be considered.
	 * @return a new xpath pointer range.
	 * @throws ExistingIdException if the identifier specified is already used in the document.
	 * @throws EARMARKGraphException if the docuverse specified was created using
	 * different document.
	 */
	public XPathPointerRange createXPathPointerRange(
			String id, Docuverse docuverse, Integer begin, Integer end, String xpath) 
	throws ExistingIdException, EARMARKGraphException {
		return createXPathPointerRange(getURI(id), docuverse, begin, end, xpath);
	}
	
	/**
	 * <p>This method creates a new xpath pointer range between two location.</p>
	 * <p>If, in the EARMARK document, a range with same begin and end locations, 
	 * same docuverse, and same xpath expression exists, this method returns it.</p>
	 * 
	 * @param id the identifier for the new range.
	 * @param docuverse the docuverse the range refers to.
	 * @param begin the begin location of the range.
	 * @param end the end location of the range.
	 * @param xpath the xpath expression to be considered.
	 * @return a new xpath pointer range.
	 * @throws ExistingIdException if the identifier specified is already used in the document.
	 * @throws EARMARKGraphException if the docuverse specified was created using
	 * different document.
	 */
	public XPathPointerRange createXPathPointerRange(
			URI id, Docuverse docuverse, Integer begin, Integer end, String xpath) 
	throws ExistingIdException, EARMARKGraphException {
		checkIdentity(id);
		
		if (equals(docuverse.getEARMARKDocument())) {
			Range range = getWhetherItExists(docuverse, begin, end, (xpath == null ? "" : xpath));
			
			if (range == null) {
				range = new XPathPointerRange(this, docuverse, begin, end, xpath, id);
				idSet.put(id, range);
				addRangeData(range);
				userData.put(range, new HashMap<String,Object>());
				parentMap.put(range, new HashSet<EARMARKHierarchicalNode>());
			}
			
			return (XPathPointerRange) range;
			
		} else {
			throw new EARMARKGraphException(EARMARKGraphException.Type.WRONG_DOCUMENT_ERR, anotherDocumentError);
		}
	}
	
	/**
	 * <p>This method creates a new xpath pointer range between two location.</p>
	 * <p>If, in the EARMARK document, a range with same begin and end locations, 
	 * same docuverse and same xpath expression exists, this method returns it.</p>
	 * 
	 * @param docuverse the docuverse the range refers to.
	 * @param begin the begin location of the range.
	 * @param end the end location of the range.
	 * @param xpath the xpath expression to be considered.
	 * @return a new xpath pointer range.
	 */
	public XPathPointerRange createXPathPointerRange(
			Docuverse docuverse, Integer begin, Integer end, String xpath) {
		XPathPointerRange result = null;
		
		URI id = makeId("r");
		try {
			result = createXPathPointerRange(id, docuverse, begin, end, xpath);
		} catch (ExistingIdException e) {
			// It shouldn't be possible to come here...
		}
		
		return result;
	}
	
	/**
	 * This method returns an element.
	 * 
	 * @param type the collection type associated to this markup item, that
	 * must be Collection.LIST, Collectable.COLLECTABLE_BAG or 
	 * Collectable.COLLECTABLE_SET. If a different value is used, the current 
	 * value used is Collection.LIST.
	 * @return a new element.
	 */
	public Element createElement(Collection.Type type) {
		URI id = makeId(null);
		
		// Sostituire tutte queste definizioni ad interi con i tipi enumerativi!
		return (Element) createMarkupItem(id, null, null, type, EARMARKNode.Type.Element);
	}
	
	/**
	 * This method returns an element.
	 * 
	 * @param gi the general identifier associated to the element.
	 * @param type the collection type associated to this markup item. Possible values:
	 * must be Collection.Type.Set, Collection.Type.Bag and Collection.Type.List.
	 * @return a new element.
	 */
	public Element createElement(String gi, Collection.Type type) {
		URI id = makeId(gi);
		
		return (Element) createMarkupItem(id, gi, null, type, EARMARKNode.Type.Element);
	}
	
	/**
	 * This method returns an element.
	 * 
	 * @param gi the general identifier associated to the element.
	 * @param ns the namespace associated to the element.
	 * @param type the collection type associated to this markup item. Possible values:
	 * must be Collection.Type.Set, Collection.Type.Bag and Collection.Type.List.
	 * @return a new element.
	 */
	public Element createElement(String gi, URI ns, Collection.Type type) {
		URI id = makeId(gi);
		
		return (Element) createMarkupItem(id, gi, ns, type, EARMARKNode.Type.Element);
	}
	
	/**
	 * This method returns a element.
	 * 
	 * @param id the identifier for the element.
	 * @param gi the general identifier associated to the element.
	 * @param type the collection type associated to this markup item. Possible values:
	 * must be Collection.Type.Set, Collection.Type.Bag and Collection.Type.List.
	 * @return a new element.
	 * @throws ExistingIdException if the identifier specified is already used in the document.
	 */
	public Element createElement(URI id, String gi, Collection.Type type) 
	throws ExistingIdException {
		checkIdentity(id);
		
		return (Element) createMarkupItem(id, gi, null, type, EARMARKNode.Type.Element);
	}
	
	/**
	 * <p>This method returns a element.</p>
	 * <p>When the id do not comply with the
	 * complete URL syntax (i.e., [protocol] + :// + domain + resource path), the id will be completed
	 * automatically with the path of the current document id. For instance, if the input id string is "p14" and
	 * the document id is the URI "http://www.example.com/mydocument", then the resulting id string considered by
	 * this method will be "http://www.example.com/mydocument" + SEPARATOR + "p14".</p>
	 * 
	 * @param id the identifier for the element.
	 * @param gi the general identifier associated to the element.
	 * @param type the collection type associated to this markup item. Possible values:
	 * must be Collection.Type.Set, Collection.Type.Bag and Collection.Type.List.
	 * @return a new element.
	 * @throws ExistingIdException if the identifier specified is already used in the document.
	 */
	public Element createElement(String id, String gi, Collection.Type type) 
	throws ExistingIdException {
		return createElement(getURI(id), gi, type);
	}
	
	/**
	 * This method returns a element.
	 * 
	 * @param id the identifier for the element.
	 * @param gi the general identifier associated to the element.
	 * @param ns the namespace associated to the element.
	 * @param type the collection type associated to this markup item. Possible values:
	 * must be Collection.Type.Set, Collection.Type.Bag and Collection.Type.List.
	 * @return a new element.
	 * @throws ExistingIdException if the identifier specified is already used in the document.
	 */
	public Element createElement(URI id, String gi, URI ns, Collection.Type type) 
	throws ExistingIdException {
		checkIdentity(id);
		
		return (Element) createMarkupItem(id, gi, ns, type, EARMARKNode.Type.Element);
	}
	
	/**
	 * <p>This method returns a element.</p>
	 * <p>When the id do not comply with the
	 * complete URL syntax (i.e., [protocol] + :// + domain + resource path), the id will be completed
	 * automatically with the path of the current document id. For instance, if the input id string is "p14" and
	 * the document id is the URI "http://www.example.com/mydocument", then the resulting id string considered by
	 * this method will be "http://www.example.com/mydocument" + SEPARATOR + "p14".</p>
	 * 
	 * @param id the identifier for the element.
	 * @param gi the general identifier associated to the element.
	 * @param ns the namespace associated to the element.
	 * @param type the collection type associated to this markup item. Possible values:
	 * must be Collection.Type.Set, Collection.Type.Bag and Collection.Type.List.
	 * @return a new element.
	 * @throws ExistingIdException if the identifier specified is already used in the document.
	 */
	public Element createElement(String id, String gi, URI ns, Collection.Type type) 
	throws ExistingIdException {
		return createElement(getURI(id), gi, ns, type);
	}
	
	
	/**
	 * This method returns an attribute.
	 * 
	 * @param type the collection type associated to this markup item. Possible values:
	 * must be Collection.Type.Set, Collection.Type.Bag and Collection.Type.List.
	 * @return a new attribute.
	 */
	public Attribute createAttribute(Collection.Type type) {
		URI id = makeId(null);
		
		return (Attribute) createMarkupItem(id, null, null, type, EARMARKNode.Type.Attribute);
	}
	
	/**
	 * This method returns an attribute.
	 * 
	 * @param gi the general identifier associated to the attribute.
	 * @param type the collection type associated to this markup item. Possible values:
	 * must be Collection.Type.Set, Collection.Type.Bag and Collection.Type.List.
	 * @return a new attribute.
	 */
	public Attribute createAttribute(String gi, Collection.Type type) {
		URI id = makeId(gi);
		
		return (Attribute) createMarkupItem(id, gi, null, type, EARMARKNode.Type.Attribute);
	}
	
	/**
	 * This method returns an attribute.
	 * 
	 * @param gi the general identifier associated to the attribute.
	 * @param ns the namespace associated to the attribute.
	 * @param type the collection type associated to this markup item. Possible values:
	 * must be Collection.Type.Set, Collection.Type.Bag and Collection.Type.List.
	 * @return a new attribute.
	 */
	public Attribute createAttribute(String gi, URI ns, Collection.Type type) {
		URI id = makeId(gi);
		
		return (Attribute) createMarkupItem(id, gi, ns, type, EARMARKNode.Type.Attribute);
	}
	
	/**
	 * This method returns an attribute.
	 * 
	 * @param id the identifier for the attribute.
	 * @param gi the general identifier associated to the attribute.
	 * @param type the collection type associated to this markup item. Possible values:
	 * must be Collection.Type.Set, Collection.Type.Bag and Collection.Type.List.
	 * @return a new attribute.
	 * @throws ExistingIdException if the identifier specified is already used in the document.
	 */
	public Attribute createAttribute(URI id, String gi, Collection.Type type) 
	throws ExistingIdException {
			checkIdentity(id);
			
			return (Attribute) createMarkupItem(id, gi, null, type, EARMARKNode.Type.Attribute);
	}
	
	/**
	 * <p>This method returns an attribute.</p>
	 * <p>When the id do not comply with the
	 * complete URL syntax (i.e., [protocol] + :// + domain + resource path), the id will be completed
	 * automatically with the path of the current document id. For instance, if the input id string is "p14" and
	 * the document id is the URI "http://www.example.com/mydocument", then the resulting id string considered by
	 * this method will be "http://www.example.com/mydocument" + SEPARATOR + "p14".</p>
	 * 
	 * @param id the identifier for the attribute.
	 * @param gi the general identifier associated to the attribute.
	 * @param type the collection type associated to this markup item. Possible values:
	 * must be Collection.Type.Set, Collection.Type.Bag and Collection.Type.List.
	 * @return a new attribute.
	 * @throws ExistingIdException if the identifier specified is already used in the document.
	 */
	public Attribute createAttribute(String id, String gi, Collection.Type type) 
	throws ExistingIdException {
		return createAttribute(getURI(id), gi, type);
	}
	
	/**
	 * This method returns an attribute.
	 * 
	 * @param id the identifier for the attribute.
	 * @param gi the general identifier associated to the attribute.
	 * @param ns the namespace associated to the attribute.
	 * @param type the collection type associated to this markup item. Possible values:
	 * must be Collection.Type.Set, Collection.Type.Bag and Collection.Type.List.
	 * @return a new attribute.
	 * @throws ExistingIdException if the identifier specified is already used in the document.
	 */
	public Attribute createAttribute(URI id, String gi, URI ns, Collection.Type type) 
	throws ExistingIdException {
			checkIdentity(id);
			
			return (Attribute) createMarkupItem(id, gi, ns, type, EARMARKNode.Type.Attribute);
	}
	
	/**
	 * <p>This method returns an attribute.</p>
	 * <p>When the id do not comply with the
	 * complete URL syntax (i.e., [protocol] + :// + domain + resource path), the id will be completed
	 * automatically with the path of the current document id. For instance, if the input id string is "p14" and
	 * the document id is the URI "http://www.example.com/mydocument", then the resulting id string considered by
	 * this method will be "http://www.example.com/mydocument" + SEPARATOR + "p14".</p>
	 * 
	 * @param id the identifier for the attribute.
	 * @param gi the general identifier associated to the attribute.
	 * @param ns the namespace associated to the attribute.
	 * @param type the collection type associated to this markup item. Possible values:
	 * must be Collection.Type.Set, Collection.Type.Bag and Collection.Type.List.
	 * @return a new attribute.
	 * @throws ExistingIdException if the identifier specified is already used in the document.
	 */
	public Attribute createAttribute(String id, String gi, URI ns, Collection.Type type) 
	throws ExistingIdException {
		return createAttribute(getURI(id), gi, ns, type);
	}
	
	/**
	 * This method returns an comment.
	 * 
	 * @param type the collection type associated to this markup item. Possible values:
	 * must be Collection.Type.Set, Collection.Type.Bag and Collection.Type.List.
	 * @return a new comment.
	 */
	public Comment createComment(Collection.Type type) {
			URI id = makeId(null);
			
			return (Comment) createMarkupItem(id, null, null, type, EARMARKNode.Type.Comment);
	}
	
	/**
	 * This method returns an comment.
	 * 
	 * @param gi the general identifier associated to the comment.
	 * @param type the collection type associated to this markup item. Possible values:
	 * must be Collection.Type.Set, Collection.Type.Bag and Collection.Type.List.
	 * @return a new comment.
	 */
	public Comment createComment(String gi, Collection.Type type) {
			URI id = makeId(gi);
			
			return (Comment) createMarkupItem(id, gi, null, type, EARMARKNode.Type.Comment);
	}
	
	/**
	 * This method returns an comment.
	 * 
	 * @param gi the general identifier associated to the comment.
	 * @param ns the namespace associated to the comment.
	 * @param type the collection type associated to this markup item. Possible values:
	 * must be Collection.Type.Set, Collection.Type.Bag and Collection.Type.List.
	 * @return a new comment.
	 */
	public Comment createComment(String gi, URI ns, Collection.Type type) {
			URI id = makeId(gi);
			
			return (Comment) createMarkupItem(id, gi, ns, type, EARMARKNode.Type.Comment);
	}
	
	/**
	 * This method returns a comment.
	 * 
	 * @param id the identifier for the comment.
	 * @param gi the general identifier associated to the comment.
	 * @param type the collection type associated to this markup item. Possible values:
	 * must be Collection.Type.Set, Collection.Type.Bag and Collection.Type.List.
	 * @return a new comment.
	 * @throws ExistingIdException if the identifier specified is already used in the document.
	 */
	public Comment createComment(URI id, String gi, Collection.Type type) 
	throws ExistingIdException {
		checkIdentity(id);
		
		return (Comment) createMarkupItem(id, gi, null, type, EARMARKNode.Type.Comment);
	}
	
	/**
	 * <p>This method returns a comment.</p>
	 * <p>When the id do not comply with the
	 * complete URL syntax (i.e., [protocol] + :// + domain + resource path), the id will be completed
	 * automatically with the path of the current document id. For instance, if the input id string is "p14" and
	 * the document id is the URI "http://www.example.com/mydocument", then the resulting id string considered by
	 * this method will be "http://www.example.com/mydocument" + SEPARATOR + "p14".</p>
	 * 
	 * @param id the identifier for the comment.
	 * @param gi the general identifier associated to the comment.
	 * @param type the collection type associated to this markup item. Possible values:
	 * must be Collection.Type.Set, Collection.Type.Bag and Collection.Type.List.
	 * @return a new comment.
	 * @throws ExistingIdException if the identifier specified is already used in the document.
	 */
	public Comment createComment(String id, String gi, Collection.Type type) 
	throws ExistingIdException {
		return createComment(getURI(id), gi, type);
	}
	
	/**
	 * This method returns a comment.
	 * 
	 * @param id the identifier for the comment.
	 * @param gi the general identifier associated to the comment.
	 * @param ns the namespace associated to the comment.
	 * @param type the collection type associated to this markup item. Possible values:
	 * must be Collection.Type.Set, Collection.Type.Bag and Collection.Type.List.
	 * @return a new comment.
	 * @throws ExistingIdException if the identifier specified is already used in the document.
	 */
	public Comment createComment(URI id, String gi, URI ns, Collection.Type type) 
	throws ExistingIdException {
		checkIdentity(id);
		
		return (Comment) createMarkupItem(id, gi, ns, type, EARMARKNode.Type.Comment);
	}
	
	/**
	 * <p>This method returns a comment.</p>
	 * <p>When the id do not comply with the
	 * complete URL syntax (i.e., [protocol] + :// + domain + resource path), the id will be completed
	 * automatically with the path of the current document id. For instance, if the input id string is "p14" and
	 * the document id is the URI "http://www.example.com/mydocument", then the resulting id string considered by
	 * this method will be "http://www.example.com/mydocument" + SEPARATOR + "p14".</p>
	 * 
	 * @param id the identifier for the comment.
	 * @param gi the general identifier associated to the comment.
	 * @param ns the namespace associated to the comment.
	 * @param type the collection type associated to this markup item. Possible values:
	 * must be Collection.Type.Set, Collection.Type.Bag and Collection.Type.List.
	 * @return a new comment.
	 * @throws ExistingIdException if the identifier specified is already used in the document.
	 */
	public Comment createComment(String id, String gi, URI ns, Collection.Type type) 
	throws ExistingIdException {
		return createComment(getURI(id), gi, ns, type);
	}
	
	/**
	 * This method returns a set of all the namespaces used in the document. If present, the namespace "" refers to
	 * no namespace.
	 * 
	 * @return the set containing all the namespaces.
	 */
	public Set<URI> getNamespaces() {
		return new HashSet<URI>(namespaces.keySet());
	}
	
	/**
	 * This method returns the set of general identifiers referred either to a particular namespace in the document
	 * or to no namespace. If present, the general identifier "" refers to no general identifier.
	 * 
	 * @param ns the namespace referred by the general identifier or null if you want to get all the general
	 * identifiers of the markup items that do not have any namespace.
	 * @return the set of the general identifier of the namespace or null if the namespace
	 * does not exist.
	 */
	public Set<String> getGeneralIdentifiersFromNamespace(URI ns) {
		return new HashSet<String>(namespaces.get((ns == null ? "" : ns)));
	}
	
	@Override
	public URI hasId() {
		return id;
	}
	
	@Override
	public String hasLocalId() {
		return "";
	}

	@Override
	public EARMARKChildNode appendChild(EARMARKChildNode newChild)
	throws EARMARKGraphException {
		return appendChild(newChild, this);
	}
	
	/**
	 * Add the node newChild to the end of the sequence of the node specified. 
	 * 
	 * @param newChild the node to add.
	 * @param node the node where to add the new child.
	 * @return the node added.
	 * @throws EARMARKGraphException if newChild was created from a different document than the 
	 * one that created this node.
	 */
	protected EARMARKChildNode appendChild(
			EARMARKChildNode newChild, EARMARKHierarchicalNode node)
	throws EARMARKGraphException {
		if (equals(newChild.getOwnerDocument()) && equals(node.getOwnerDocument())) {
			return add(newChild,node);
		} else {
			throw new EARMARKGraphException(EARMARKGraphException.Type.WRONG_DOCUMENT_ERR, 
					"[appendChild] " + anotherDocumentError);
		}
	}

	/**
	 * <p>This method copies to this document a node contained in another document.</p>
	 * <p>Only items that are not already present in the document will be correctly copied. This means that
	 * an item, having identifier "ID" and belonging to a particular document, will be copied if and only
	 * if in the copying document does not exist any item having "ID" as identifier.</p>
	 * <p>If the node to be copied is a range, its referred docuverse will be copied as well.</p>
	 * @param node the node to be copied.
	 * @param deep if true, the copy is applied recursively on all the children of the node, otherwise
	 * the method copies the node definition only. When deep is true, it may occur of having an incomplete copy
	 * of the input node: it will lack any descendant item that has an identifier in common with any of those
	 * contained in the copying document.
	 * @return the copied node as part of the copying document or null if the copy of the node fails.
	 */
	public EARMARKChildNode copyNode(EARMARKChildNode node, boolean deep) {
		EARMARKChildNode result = null;
		
		Type type = node.getNodeType();
		
		if (node instanceof Range) {
			Range range = (Range) node;
			
			try {
				result = (Range) getEntityById(range.hasId());
				
				if (result == null) {
					Docuverse rangeDocuverse = range.refersTo();
					URI docuverseId = rangeDocuverse.hasId();
					Docuverse copiedDocuverse = (Docuverse) getEntityById(docuverseId);
					
					if (copiedDocuverse == null) {
						if (rangeDocuverse.getType() == Docuverse.Type.StringDocuverse) {
							copiedDocuverse = createStringDocuverse(docuverseId, (String) rangeDocuverse.hasContent());
						} else {
							copiedDocuverse = createURIDocuverse(docuverseId, (URI) rangeDocuverse.hasContent());
						}
					}
					
					if (type == Type.PointerRange) {
						result = createPointerRange(range.hasId(), copiedDocuverse, 
								(Integer) range.begins(), (Integer) range.ends());
					} else {
						result = createXPathPointerRange(
								range.hasId(), copiedDocuverse, (Integer) range.begins(), (Integer) range.ends(), 
								((XPathPointerRange)range).hasXPathContext());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				// Do nothing, the copy fails.
			}
			
		} else { /* Markup item */
			MarkupItem markupitem = (MarkupItem) node;
			
			try {
				result = (MarkupItem) getEntityById(markupitem.hasId());
				
				if (result == null) {
					if (type == Type.Element) {
						result = createElement(markupitem.hasId(), markupitem.hasGeneralIdentifier(), 
								markupitem.hasNamespace(), markupitem.getContainerType());
					} else if (type == Type.Attribute) {
						result = createAttribute(markupitem.hasId(), markupitem.hasGeneralIdentifier(), 
								markupitem.hasNamespace(), markupitem.getContainerType());
					} else { /* Type.Comment */
						result = createComment(markupitem.hasId(), markupitem.hasGeneralIdentifier(), 
								markupitem.hasNamespace(), markupitem.getContainerType());
					}
					
					if (deep) {
						MarkupItem resultMarkupItem = (MarkupItem) result;
						for (EARMARKChildNode child : markupitem.getChildNodes()) {
							EARMARKChildNode copiedChild = copyNode(child, true);
							if (copiedChild != null) {
								resultMarkupItem.appendChild(copiedChild);
							}
						}
					}
				}
			} catch (Exception e) {
				// Do nothing, the copy fails.
			}
		}
		
		return result;
	}
	
	/**
	 * This method clones the document.
	 * 
	 * @param deep if false, it corresponds to call the method clone(); if true, the document will be
	 * cloned using the method clone() and then all the other items of the original document will be
	 * copied to the cloned one.
	 * @return the cloned document.
	 */
	public EARMARKNode cloneNode(boolean deep) {
		EARMARKDocument result = clone();
		
		if (deep) {
			for (Docuverse docuverse : getAllDocuverses()) {
				try {
					if (docuverse.getType() == Docuverse.Type.StringDocuverse) {
						result.createStringDocuverse(docuverse.hasId(), (String) docuverse.hasContent());
					} else {
						result.createURIDocuverse(docuverse.hasId(), (URI) docuverse.hasContent());
					}
				} catch (ExistingIdException e) {
					// It shouldn't be possible...
				}
			}
			
			for (EARMARKChildNode node : getChildNodes()) {
				result.appendChild(result.copyNode(node, deep));
			}
		}
		
		return result;
	}

	@Override
	public Collection getAttributes() {
		return getAttributes(this);
	}
	
	/**
	 * A collection that contains all the attributes of the node specified. 
	 * If there are no children, this is a list containing no nodes.
	 * 
	 * @param node the node we are asking for its attribute.
	 * 
	 * @return the attribute children of the node.
	 */
	protected Collection getAttributes(EARMARKNode node) {
		Collection result = null;
		
		Collection tmp = childMap.get(node);
		Collection.Type type = tmp.getCollectionType();
		if (type == Collection.Type.List) {
			result = new it.essepuntato.earmark.core.List();
		} else if (type == Collection.Type.Bag) {
			result = new it.essepuntato.earmark.core.Bag();
		} else {
			result = new it.essepuntato.earmark.core.Set();
		}
		
		Iterator<EARMARKChildNode> ite = tmp.iterator();
		while (ite.hasNext()) {
			EARMARKNode mi = ite.next();
			if (mi.getNodeType() == EARMARKNode.Type.Attribute) {
				result.add((Attribute) mi);
			}
		}
		
		return result;
	}
	
	@Override
	public Collection getComments() {
		return getComments(this);
	}
	
	/**
	 * A collection that contains all the comments of the node specified. 
	 * If there are no children, this is a list containing no nodes.
	 * 
	 * @param node the node we are asking for its comments.
	 * 
	 * @return the comment children of the node.
	 */
	protected Collection getComments(EARMARKNode node) {
		Collection result = null;
		
		Collection tmp = childMap.get(node);
		Collection.Type type = tmp.getCollectionType();
		if (type == Collection.Type.List) {
			result = new it.essepuntato.earmark.core.List();
		} else if (type == Collection.Type.Bag) {
			result = new it.essepuntato.earmark.core.Bag();
		} else {
			result = new it.essepuntato.earmark.core.Set();
		}
		
		Iterator<EARMARKChildNode> ite = tmp.iterator();
		while (ite.hasNext()) {
			EARMARKNode mi = ite.next();
			if (mi.getNodeType() == EARMARKNode.Type.Comment) {
				result.add((Comment) mi);
			}
		}
		
		return result;
	}

	@Override
	public Collection getChildElements() {
		return getChildElements(this);
	}
	
	/**
	 * This method return all the child elements of the node specified.
	 * 
	 * @param node the node we are asking for its child elements.
	 * @return a collection containing all the child elements of the node.
	 */
	protected Collection getChildElements(EARMARKNode node) {
		Collection result = null;
		
		Collection tmp = childMap.get(node);
		Collection.Type type = tmp.getCollectionType();
		if (type == Collection.Type.List) {
			result = new it.essepuntato.earmark.core.List();
		} else if (type == Collection.Type.Bag) {
			result = new it.essepuntato.earmark.core.Bag();
		} else {
			result = new it.essepuntato.earmark.core.Set();
		}
		
		Iterator<EARMARKChildNode> ite = tmp.iterator();
		while(ite.hasNext()) {
			EARMARKNode mi = ite.next();
			if (mi.getNodeType() == EARMARKNode.Type.Element) {
				result.add((Element) mi);
			}
		}
		
		return result;
	}
	
	@Override
	public Collection getChildNodes() {
		return getChildNodes(this);
	}
	
	/**
	 * This method returns a collection that contains all children 
	 * (elements, attributes, comments) of the node specified. 
	 * 
	 * @param node the node we are asking for its child nodes.
	 * @return the node children.
	 */
	protected Collection getChildNodes(EARMARKNode node) {
		return childMap.get(node).clone();
	}

	@Override
	public EARMARKChildNode getFirstChild() {
		return getFirstChild(this);
	}
	
	/**
	 * The first child of the node specified. If there is no such node, this returns null.
	 * If the current node is a Collection.Type.Bag, a random node is returned.
	 * 
	 * @param node the node we are asking for its first child. 
	 * @return the first node child.
	 */
	protected EARMARKChildNode getFirstChild(EARMARKHierarchicalNode node) {
		Collection collection = childMap.get(node); 
		return (collection.isEmpty() ? null : collection.iterator().next());
	}

	@Override
	public EARMARKChildNode getLastChild() {
		return getLastChild(this);
	}
	
	/**
	 * The last child of the node specified. If there is no such node, this returns null.
	 * If the current node is a bag, a random node is returned.
	 * 
	 * @param node the node we are asking for its last child. 
	 * @return the last node child.
	 */
	protected EARMARKChildNode getLastChild(EARMARKHierarchicalNode node) {
		Collection collection = childMap.get(node);
		return 
		(collection.isEmpty() ? null : 
			(collection.getCollectionType() == 
				Collection.Type.Set ? collection.iterator().next() : 
				((Bag)collection).get(collection.size()-1)));
	}
	
	/**
	 * This method returns the next siblings of the node specified. If more than one siblings
	 * exist, it choose randomly among the next siblings list. Though in a common 
	 * XML document there exists one possible next sibling only, in EARMARK it is 
	 * possible to have more than one sibling, because of different hierarchies and 
	 * multiple references.
	 *  
	 * @param node the node we are asking for its next sibling.
	 * @return a next siblings of the node or null if it does not exist.
	 */
	protected EARMARKChildNode getNextSibling(EARMARKChildNode node) {
		EARMARKChildNode result = null;
		
		Iterator<EARMARKHierarchicalNode> ite = parentMap.get(node).iterator();
		while (result == null && ite.hasNext()) {
			Collection collection = childMap.get(ite.next());
			if (
					collection.getCollectionType() == Collection.Type.Set && 
					collection.size() > 1) {
				Iterator<EARMARKChildNode> iteList = collection.iterator();
				while (result == null || iteList.hasNext()) {
					EARMARKChildNode curNode = iteList.next();
					if (!curNode.equals(node)) {
						result = curNode;
					}
				}
			} else {
				Bag bag = (Bag) collection;
				int index = bag.indexOf(node);
				if (index < bag.size() - 1) {
					result = bag.get(index + 1);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * This method returns all the next siblings of the node specified. Though in a common XML
	 * document there exists one possible next sibling only, in EARMARK it is possible
	 * to have more than one sibling, because of different hierarchies and multiple
	 * references.
	 * 
	 * @param node the node we are asking for its next siblings.
	 * @return a set containing all the next siblings of the node.
	 */
	protected Set<EARMARKChildNode> getNextSiblings(EARMARKChildNode node) {
		Set<EARMARKChildNode> result = new HashSet<EARMARKChildNode>();
		
		Iterator<EARMARKHierarchicalNode> ite = parentMap.get(node).iterator();
		while (ite.hasNext()) {
			Collection collection = childMap.get(ite.next());
			if (
					collection.getCollectionType() == Collection.Type.Set && 
					collection.size() > 1) {
				Iterator<EARMARKChildNode> nodes = collection.iterator();
				while (nodes.hasNext()) {
					EARMARKChildNode curNode = nodes.next();
					if (!curNode.equals(node)) {
						result.add(curNode);
					}
				}
			} else {
				Bag bag = (Bag) collection;
				int index = bag.indexOf(node);
				if (index < bag.size() - 1) {
					result.add(bag.get(index + 1));
				}
			}
		}
		
		return result;
	}

	@Override
	public EARMARKNode.Type getNodeType() {
		return EARMARKNode.Type.Document;
	}

	@Override
	public EARMARKDocument getOwnerDocument() {
		return this;
	}

	/**
	 * The parent of the node specified. All nodes, except Document may have a parent.
	 * If a node has more than one parent, it returns one randomly.
	 * However, if a node has just been created and not yet added to the graph, 
	 * or if it has been removed from the graph, this is null.
	 * 
	 * @param node the node we are asking for the parent node.
	 * @return a parent of this node.
	 */
	protected EARMARKHierarchicalNode getParentNode(EARMARKChildNode node) {
		Set<EARMARKHierarchicalNode> set = parentMap.get(node);
		return (set.isEmpty() ? null : set.iterator().next());
	}

	/**
	 * This method returns the parents of the node specified. All nodes, except Document 
	 * may have a parent. However, if a node has just been created and not yet 
	 * added to set graph, or if it has been removed from the graph, this is an
	 * empty list.
	 * 
	 * @param node the node we are asking for the parent nodes.
	 * @return all the parents of this node.
	 */
	protected Set<EARMARKHierarchicalNode> getParentNodes(EARMARKChildNode node) {
		return new HashSet<EARMARKHierarchicalNode>(parentMap.get(node));
	}

	/**
	 * This method returns the previous siblings of the node specified. If more than one siblings
	 * exist, it choose randomly among the next siblings list. Though in a common 
	 * XML document there exists one possible previous sibling only, in EARMARK it is 
	 * possible to have more than one sibling, because of different hierarchies and 
	 * multiple references.
	 * 
	 * @param node the node we are asking for the previous sibling.
	 * @return a previous siblings of the node.
	 */
	protected EARMARKChildNode getPreviousSibling(EARMARKChildNode node) {
		EARMARKChildNode result = null;
		
		Iterator<EARMARKHierarchicalNode> ite = parentMap.get(node).iterator();
		while (result == null && ite.hasNext()) {
			Collection collection = childMap.get(ite.next());
			if (
					collection.getCollectionType() == Collection.Type.Set &&
					collection.size() > 1) {
				Iterator<EARMARKChildNode> iteList = collection.iterator();
				while (result == null || iteList.hasNext()) {
					EARMARKChildNode curNode = iteList.next();
					if (!curNode.equals(node)) {
						result = curNode;
					}
				}
			} else {
				Bag bag = (Bag) collection;
				int index = bag.indexOf(node);
				if (index > 0) {
					result = bag.get(index - 1);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * This method returns all the previous siblings of the node specified. Though in a common XML
	 * document there exists one possible previous sibling only, in EARMARK it is possible
	 * to have more than one sibling, because of different hierarchies and multiple
	 * references.
	 * 
	 * @param node the node we are asking for the previous siblings. 
	 * @return a set containing all the previous siblings of the node.
	 */
	protected Set<EARMARKChildNode> getPreviousSiblings(EARMARKChildNode node) {
		Set<EARMARKChildNode> result = new HashSet<EARMARKChildNode>();
		
		Iterator<EARMARKHierarchicalNode> ite = parentMap.get(node).iterator();
		while (ite.hasNext()) {
			Collection collection = childMap.get(ite.next());
			if (
					collection.getCollectionType() == Collection.Type.Set &&
					collection.size() > 1) {
				Iterator<EARMARKChildNode> nodes = collection.iterator();
				while (nodes.hasNext()) {
					EARMARKChildNode curNode = nodes.next();
					if (!curNode.equals(node)) {
						result.add(curNode);
					}
				}
			} else {
				Bag bag = (Bag) collection;
				int index = bag.indexOf(node);
				if (index > 0) {
					result.add(bag.get(index - 1));
				}
			}
		}
		
		return result;
	}

	@Override
	public String getTextContent() {
		return getTextContent(this);
	}
	
	/**
	 * This method returns the text content of the node specified and its descendants.
	 * 
	 * @param node the node we are asking for the text content.
	 * @return the text content of this node, or null if it does not exist.
	 */
	protected String getTextContent(EARMARKHierarchicalNode node) {
		String result = "";
		boolean existAny = false;
		
		Iterator<EARMARKChildNode> ite = childMap.get(node).iterator();
		while (ite.hasNext()) {
			EARMARKNode mi = ite.next();
			String tmpResult = mi.getTextContent();
			
			if (tmpResult != null) {
				if (!existAny) {
					existAny = true;
				}
				result += tmpResult;
			}
		}
		
		return (existAny ? result : null);
	}

	@Override
	public Object getUserData(String key) {
		return getUserData(this, key);
	}
	
	/**
	 * Retrieves the object associated to a key on the node specified. The object must 
	 * first have been set to this node by calling setUserData with the same key.
	 * 
	 * @param node the node we are asking for the user data.
	 * @param key the key the object is associated to. 
	 * @return the object associated to the given key on this node, 
	 * or null if there was none.
	 */
	protected Object getUserData(EARMARKNode node, String key) {
		return userData.get(node).get(key);
	}

	@Override
	public boolean hasAttribute() {
		return hasAttribute(this);
	}
	
	/**
	 * Returns whether the node specified has any attributes.
	 * 
	 * @param node the node we are asking for attributes.
	 * @return true if the node has any attributes, false otherwise.
	 */
	protected boolean hasAttribute(EARMARKNode node) {
		boolean result = false;
		
		Iterator<EARMARKChildNode> ite = childMap.get(node).iterator();
		while (!result && ite.hasNext()) {
			EARMARKNode mi = ite.next();
			result = mi.getNodeType() == EARMARKNode.Type.Attribute;
		}
		
		return result;
	}

	@Override
	public boolean hasChildNodes() {
		return hasChildNodes(this);
	}
	
	/**
	 * Returns whether the node specified has any children.
	 * 
	 * @param node the node we are asking for children.
	 * @return true if the node has any children, false otherwise.
	 */
	protected boolean hasChildNodes(EARMARKNode node) {
		return !childMap.get(node).isEmpty();
	}

	@Override
	public boolean hasElementNodes() {
		return hasElementNodes(this);
	}
	
	/**
	 * This method says whether the node specified is a collection containing elements.
	 * 
	 * @param node the node we are asking for elements.
	 * @return true if the node sequence contain at least an element, false otherwise.
	 */
	protected boolean hasElementNodes(EARMARKNode node) {
		boolean result = false;
		
		Iterator<EARMARKChildNode> ite = childMap.get(node).iterator();
		while (!result && ite.hasNext()) {
			EARMARKNode mi = ite.next();
			result = mi.getNodeType() == EARMARKNode.Type.Element;
		}
		
		return result;
	}

	@Override
	public EARMARKChildNode insertBefore(EARMARKChildNode newChild, EARMARKChildNode refChild)
			throws EARMARKGraphException {
		return insertBefore(newChild, refChild, 0, this);
	}
	
	@Override
	public EARMARKChildNode insertBefore(
			EARMARKChildNode newChild, EARMARKChildNode refChild, int occurrence)
			throws EARMARKGraphException {
		return insertBefore(newChild, refChild, occurrence, this);
	}
	
	/**
	 * Inserts the node newChild before the first existing child node refChild of the node specified. 
	 * If refChild is null, insert newChild at the end of the list of children.
	 * This operation makes sense if and only if the EARMARK node is a markup item
	 * and its type is Collection.Type.Sequence.
	 * 
	 * @param newChild the node to insert.
	 * @param refChild the reference node, i.e., the node before which the 
	 * new node must be inserted. 
	 * @param node the parent node of refChild.
	 * @return the node being inserted.
	 * @throws EARMARKGraphException if [WRONG_DOCUMENT_ERR] node, newChild or refChild were created from a
	 * different document than this one or if [NOCHILD_ERR] refChild is not a child of node.
	 */
	protected EARMARKChildNode insertBefore(
			EARMARKChildNode newChild, EARMARKChildNode refChild, EARMARKHierarchicalNode node)
	throws EARMARKGraphException {
		return insertBefore(newChild, refChild, 0, node);
	}
	
	/**
	 * Inserts the node newChild before the existing child node by 
	 * occurrence refChild of the node specified.
	 * If refChild is null, insert newChild at the end of the list of children.
	 * This operation makes sense if and only if the EARMARK node is a markup item
	 * and its type is Collection.Type.Sequence.
	 * 
	 * @param newChild the node to insert.
	 * @param refChild the reference node, i.e., the node before which the 
	 * new node must be inserted.
	 * @param occurrence the occurrence of the refChild to consider.
	 * @param node the parent node of refChild.
	 * @return the node being inserted.
	 * @throws EARMARKGraphException if [WRONG_DOCUMENT_ERR] node, newChild or refChild were created from a
	 * different document than this one or if [NOCHILD_ERR] refChild is not a child of node. 
	 */
	protected EARMARKChildNode insertBefore(
			EARMARKChildNode newChild, EARMARKChildNode refChild, 
			int occurrence, EARMARKHierarchicalNode node)
	throws EARMARKGraphException {
		if (
				equals(node.getOwnerDocument()) && 
				equals(newChild.getOwnerDocument()) && 
				equals(node.getOwnerDocument())) {
			if (
					refChild == null || 
					(
							equals(refChild.getOwnerDocument()) && 
							(
									node.getNodeType() == EARMARKNode.Type.Document || 
									((node instanceof MarkupItem) && !((MarkupItem)node).isList()))))
			{
				add(newChild,node);
				return newChild; 
			} else {
				it.essepuntato.earmark.core.List childs = 
					(it.essepuntato.earmark.core.List) childMap.get(node);
				Set<EARMARKHierarchicalNode> parents = parentMap.get(newChild);
				int index = ListFacility.indexOfByOccurrence(childs, refChild, occurrence);
				if (index > -1) {
					childs.add(index, newChild);
					parents.add(node);
					return newChild;
				} else {
					throw new EARMARKGraphException(EARMARKGraphException.Type.NOCHILD_ERR,
							"[insertBefore]" + nochildError);
				}
			}
		} else {
			throw new EARMARKGraphException(EARMARKGraphException.Type.WRONG_DOCUMENT_ERR, 
					"[insertBefore] " + anotherDocumentError);
		}
	}

	/**
	 * <p>This method checks if the document is equal to another node.</p>
	 * <p>A document is equal to another node either if they are the same node (isSameNode) or if they are
	 * both EARMARK documents and their structures are equal (i.e., they contains equal nodes in the same
	 * hierarchical order).</p>
	 * 
	 * @param node the node to compare equality with.
	 * @return true if the nodes are equal, false otherwise.
	 */
	public boolean isEqualNode(EARMARKNode node) {
		return isEqualNode(node, true);
	}
	
	/**
	 * <p>This method checks if the document is equal to another node.</p>
	 * <p>A document is structurally equivalent to another node if they are
	 * both EARMARK documents and their structures are structurally equivalent 
	 * (i.e., they contains structurally equivalent nodes in the same hierarchical order).</p>
	 * 
	 * @param node the node to compare equality with.
	 * @return true if the nodes are equal, false otherwise.
	 */
	public boolean isStructurallyEqualNode(EARMARKNode node) {
		return isEqualNode(node, false);
	}
	
	/* Check if the document is equal to a paricular node */
	private boolean isEqualNode(EARMARKNode node, boolean lookForSameness) {
		return (lookForSameness && isSameNode(node)) || 
			(hasBasicEquality(node, this) & hasHierarchicalEquality((EARMARKHierarchicalNode) node, 
					this, lookForSameness));
	}
	
	/**
	 * This methods check whether two nodes are equal in type (EARMARKNode.Type).
	 * 
	 * @param n1 former node.
	 * @param n2 latter node.
	 * @return true if n1 is equal to n2, false otherwise.
	 */
	protected boolean hasBasicEquality(EARMARKNode n1, EARMARKNode n2) {
		boolean result = true;
		
		try {
			if (n1.getNodeType() != n2.getNodeType()) {
				result = false;
			}
		} catch (NullPointerException e) {
			result = false;
		}
		
		return result;
	}
	
	/**
	 * This method checks whether two nodes have the same structure.
	 *  
	 * @param node former node.
	 * @param other latter node.
	 * @param lookForSameness if true the comparison will involves the method isEqualNode, otherwise
	 * just the structural equivalence will be considered.
	 * 
	 * @return true if node is equal to other, false otherwise.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected boolean hasHierarchicalEquality(
			EARMARKHierarchicalNode node, EARMARKHierarchicalNode other, boolean lookForSameness) {
		boolean result = true; 
		
		try {
			if (other.getChildNodes().size() == node.getChildNodes().size()) { /* They have the 
			same amount of children */
				java.util.Collection collectionOtherCopy = 
					CollectionFacility.copy(other.getChildNodes());
				Iterator<EARMARKChildNode> ite = collectionOtherCopy.iterator();
				Iterator<EARMARKChildNode> iteList = node.getChildNodes().iterator();
				
				if (!node.isList()) { /* We compare each element of the 'node' child collection with
					 * all the elements of the 'other' child collection. */
					while (result && iteList.hasNext()) {
						EARMARKNode currentNode = iteList.next();
						boolean found = false;
						
						/* If any element in the 'other' children is equal to the current
						 * 'node' element we are considering, we say we found it, and we repeat
						 * the main while */
						while (!found && ite.hasNext()) {
							EARMARKNode otherNode = ite.next();
							if (lookForSameness) {
								found = currentNode.isEqualNode(otherNode);
							} else {
								found = currentNode.isStructurallyEqualNode(otherNode);
							}
							if (found) {
								collectionOtherCopy.remove(otherNode);
								ite = collectionOtherCopy.iterator();
							}
						}
						
						/* If a 'node' element is not in the 'other' children,
						 * 'node' and 'other' are not equal. */
						result = found;
					}
				} else {
					while (result && ite.hasNext()) {
						EARMARKNode nodeOne = ite.next();
						EARMARKNode nodeTwo = iteList.next();
						
						if (lookForSameness) {
							result = nodeOne.isEqualNode(nodeTwo);
						} else {
							result = nodeOne.isStructurallyEqualNode(nodeTwo);
						}
					}
				}
			} else {
				result = false;
			}
		} catch (NullPointerException e) {
			result = false;
		}
		
		return result;
	}

	@Override
	public boolean isSameNode(EARMARKNode node) {
		return equals(node);
	}
	
	/**
	 * This method chekcs if two nodes are the same node (i.e., they have the same identifier).
	 * 
	 * @param node former node.
	 * @param other latter node. 
	 * @return true if the nodes are the same, false otherwise. 
	 */
	protected boolean isSameNode(EARMARKNode node, EARMARKNode other) {
		return other.equals(node);
	}
	
	@Override
	public EARMARKChildNode removeChild(EARMARKChildNode oldChild)
			throws EARMARKGraphException {
		return removeChild(oldChild, this);
	}
	
	/**
	 * This method removes a docuverse from the document, if and only if the docuverse is not referred to by
	 * any range.
	 * 
	 * @param d the docuverse to remove.
	 * @return true if the docuverse is removed, false if it is not possible to remove the docuverse
	 * because it is still used by at least a range or it is not already in the document.
	 * @throws EARMARKGraphException if d was created from a
	 * different document than the one that created this node.
	 */
	public boolean removeDocuverse(Docuverse d) throws EARMARKGraphException {
		boolean result = true;
		
		if (d.getEARMARKDocument().equals(this)) {
			Set<Range> rangeSet = docuverseMap.get(d);
			if (rangeSet != null && rangeSet.isEmpty()) { /* Remove the docuverse */
				getIdMap().remove(d.hasId());
				docuverseMap.remove(d);
			} else { /* The docuverse has been already removed or it still referred to some range */
				result = false;
			}
		} else {
			throw new EARMARKGraphException(EARMARKGraphException.Type.WRONG_DOCUMENT_ERR, 
					"[removeDocuverse] " + anotherDocumentError);
		}
		
		return result;
	}
	
	/**
	 * This method removes the first child node indicated by oldChild from the list of children of 'node', 
	 * and returns it.
	 *  
	 * @param oldChild the node being removed.
	 * @param node the parent node of the removed one. 
	 * @return the node removed.
	 * @throws EARMARKGraphException if [WRONG_DOCUMENT_ERR] oldChild was created from a
	 * different document than this one or if [NOCHILD_ERR] refChild is not a child of node. 
	 */
	protected EARMARKChildNode removeChild(
			EARMARKChildNode oldChild, EARMARKHierarchicalNode node)
	throws EARMARKGraphException {
		if (equals(oldChild.getOwnerDocument()) && equals(node.getOwnerDocument())) {
			EARMARKChildNode result = remove(oldChild, node);
			if (result != null) {
				return result;
			} else {
				throw new EARMARKGraphException(EARMARKGraphException.Type.NOCHILD_ERR, 
						"[removeChild] " + nochildError);
			}
		} else {
			throw new EARMARKGraphException(EARMARKGraphException.Type.WRONG_DOCUMENT_ERR, 
					"[removeChild] " + anotherDocumentError);
		}
	}
	
	@Override
	public EARMARKChildNode removeAllChild(EARMARKChildNode oldChild) {
		return removeAllChild(oldChild, this);
	}
	
	/**
	 * Remove the all the child node indicated by oldChild from the collection of children of node, 
	 * and returns it.
	 *  
	 * @param oldChild the node being removed. 
	 * @param node the parent node of the removed one.
	 * @return the node removed.
	 * @throws EARMARKGraphException if [WRONG_DOCUMENT_ERR] node or oldChild were created from a
	 * different document than this one or if [NOCHILD_ERR] refChild is not a child of node. 
	 */
	protected EARMARKChildNode removeAllChild(
			EARMARKChildNode oldChild, EARMARKHierarchicalNode node) {
		if (equals(oldChild.getOwnerDocument()) && equals(node.getOwnerDocument())) {
			EARMARKChildNode result = removeAll(oldChild, node);
			return result;
		} else {
			throw new EARMARKGraphException(EARMARKGraphException.Type.WRONG_DOCUMENT_ERR, 
					"[removeAllChild] " + anotherDocumentError);
		}
	}

	@Override
	public EARMARKChildNode removeChildByOccurrence(
			EARMARKChildNode oldChild, int occurrence) {
		return removeChildByOccurrence(oldChild, occurrence, this);
	}
	
	/**
	 * Remove the child node by occurrence indicated by oldChild from the collection of children of node, 
	 * and returns it.
	 *  
	 * @param oldChild the node being removed.
	 * @param node the parent node of the removed one.
	 * @param occurrence the node occurrence to remove.
	 *  
	 * @return the node removed.
	 * @throws EARMARKGraphException if [WRONG_DOCUMENT_ERR] node or oldChild were created from a
	 * different document than this one or if [NOCHILD_ERR] refChild is not a child of node. 
	 */
	protected EARMARKChildNode removeChildByOccurrence(
			EARMARKChildNode oldChild, int occurrence, EARMARKHierarchicalNode node) {
		if (equals(oldChild.getOwnerDocument()) && equals(node.getOwnerDocument())) {
			EARMARKChildNode result = remove(oldChild, node, occurrence);
			if (result != null) {
				return result;
			} else {
				throw new EARMARKGraphException(EARMARKGraphException.Type.NOCHILD_ERR, 
						"[removeChildByOccurrence] " + nochildError);
			}
		} else {
			throw new EARMARKGraphException(EARMARKGraphException.Type.WRONG_DOCUMENT_ERR, 
					"[remoceChildByOccurrence] " + anotherDocumentError);
		}
	}

	@Override
	public EARMARKChildNode replaceChild(EARMARKChildNode newChild, EARMARKChildNode oldChild)
			throws EARMARKGraphException {
		return replaceChild(newChild, oldChild, this);
	}
	
	/**
	 * Replace the first child node oldChild with newChild in the collection of children of node, 
	 * and returns the oldChild node. If oldChild does not exist, it returns
	 * null. 
	 * 
	 * @param newChild the new node to put in the child collection.
	 * @param oldChild the node being replaced in the collection.
	 * @param node the oldChild parent.
	 * @return the node replaced or null if it does not exist.
	 * @throws EARMARKGraphException if [WRONG_DOCUMENT_ERR] node, newChild or oldChild were created from a
	 * different document than this one or if [NOCHILD_ERR] refChild is not a child of node.
	 */
	protected EARMARKChildNode replaceChild(
			EARMARKChildNode newChild, EARMARKChildNode oldChild, EARMARKHierarchicalNode node)
	throws EARMARKGraphException {
		if (
				equals(newChild.getOwnerDocument()) && 
				equals(oldChild.getOwnerDocument()) && 
				equals(node.getOwnerDocument())) {
			EARMARKChildNode result = replace(newChild, oldChild, node);
			if (result != null) {
				return result;
			} else {
				throw new EARMARKGraphException(EARMARKGraphException.Type.NOCHILD_ERR, 
						"[replaceChild] " + nochildError);
			}
		} else {
			throw new EARMARKGraphException(EARMARKGraphException.Type.WRONG_DOCUMENT_ERR, 
					"[replaceChild] " + anotherDocumentError);
		}
	}
	
	@Override
	public EARMARKChildNode replaceAllChild(EARMARKChildNode newChild, EARMARKChildNode oldChild)
			throws EARMARKGraphException {
		return replaceAllChild(newChild, oldChild, this);
	}
	
	/**
	 * Replace the all child node oldChild with newChild in the collection of children of node, 
	 * and returns the oldChild node. If the oldChild does not exist, it returns
	 * null. 
	 * 
	 * @param newChild the new node to put in the child collection.
	 * @param oldChild the node being replaced in the collection.
	 * @param node the oldChild parent.
	 * @return the node replaced or null if it does not exist.
	 * @throws EARMARKGraphException if [WRONG_DOCUMENT_ERR] node, newChild or oldChild were created from a
	 * different document than this one or if [NOCHILD_ERR] refChild is not a child of node.
	 */
	protected EARMARKChildNode replaceAllChild(
			EARMARKChildNode newChild, EARMARKChildNode oldChild, EARMARKHierarchicalNode node)
	throws EARMARKGraphException {
		if (
				equals(newChild.getOwnerDocument()) && 
				equals(oldChild.getOwnerDocument()) &&
				equals(node.getOwnerDocument())) {
			EARMARKChildNode result = replaceAll(newChild, oldChild, node);
			if (result != null) {
				return result;
			} else {
				throw new EARMARKGraphException(EARMARKGraphException.Type.NOCHILD_ERR, 
						"[replaceAllChild] " + nochildError);
			}
		} else {
			throw new EARMARKGraphException(EARMARKGraphException.Type.WRONG_DOCUMENT_ERR, 
					"[replaceAllChild] " + anotherDocumentError);
		}
	}

	@Override
	public EARMARKChildNode replaceChildByOccurrence(EARMARKChildNode newChild,
			EARMARKChildNode oldChild, int occurrence) throws EARMARKGraphException {
		if (equals(newChild.getOwnerDocument()) && equals(oldChild.getOwnerDocument())) {
			EARMARKChildNode result = replace(newChild, oldChild, this, occurrence);
			if (result != null) {
				return result;
			} else {
				throw new EARMARKGraphException(EARMARKGraphException.Type.NOCHILD_ERR, 
						"The oldChild spcefied is not a child of the EARMARK document.");
			}
		} else {
			throw new EARMARKGraphException(EARMARKGraphException.Type.WRONG_DOCUMENT_ERR, 
					"The markup items specified are created using a different " +
					"EARMARK document.");
		}
	}
	
	/**
	 * Remove the child node by occurrence indicated by oldChild from the list of children of 'node', 
	 * and returns it.
	 *  
	 * @param oldChild the node being replaced.
	 * @param occurrence the node occurrence to replace.
	 * @param node the oldChild parent.
	 * @return the node replaced.
	 * @throws EARMARKGraphException if [WRONG_DOCUMENT_ERR] node, newChild or oldChild were created from a
	 * different document than this one or if [NOCHILD_ERR] refChild is not a child of node.
	 */
	protected EARMARKChildNode replaceChildByOccurrence(
			EARMARKChildNode newChild, 
			EARMARKChildNode oldChild, 
			int occurrence, 
			EARMARKHierarchicalNode node) 
	throws EARMARKGraphException {
		if (
				equals(newChild.getOwnerDocument()) && 
				equals(oldChild.getOwnerDocument()) &&
				equals(node.getOwnerDocument())) {
			EARMARKChildNode result = replace(newChild, oldChild, node, occurrence);
			if (result != null) {
				return result;
			} else {
				throw new EARMARKGraphException(EARMARKGraphException.Type.NOCHILD_ERR, 
						"[replaceChildByOccurrence] " + nochildError);
			}
		} else {
			throw new EARMARKGraphException(EARMARKGraphException.Type.WRONG_DOCUMENT_ERR, 
					"[replaceChildByOccurrence] " + anotherDocumentError);
		}
	}

	@Override
	public void setUserData(String key, Object data) {
		setUserData(key, data, this);
	}
	
	/**
	 * This method allows to associate an object to a key on the 
	 * node specified. The object can later be retrieved 
	 * from this node by calling getUserData with the same key.
	 * 
	 * @param key the key to associate the object to.
	 * @param data the object to associate to the given key, or null to remove any 
	 * existing association to that key.
	 * @param node the node we are setting the data.
	 */
	protected void setUserData(String key, Object data, EARMARKNode node) {
		userData.get(node).put(key, data);
	}
	
	/**
	 * <p>This method adopts a node of another document in this one. The adoption is recursive: adopting a
	 * node 'n' means to adopt all the other descendant nodes of 'n'.</p>
	 * As for the method copyNode, only items that are not already present 
	 * in the document will be correctly adopted. This means that
	 * an item, having identifier "ID" and belonging to a particular 
	 * document, will be adopted if and only
	 * if in the adopting document does not exist any item having "ID" as identifier.</p>
	 * <p>If the node to be adopted is a range, its referred docuverse will be copied as well.</p>
	 * <p>In case of success, all the adopted nodes will be removed from the original document.</p>
	 *  
	 * @param n the node to adopt.
	 * @return the node adopted.
	 */
	public EARMARKChildNode adoptNode(EARMARKChildNode n) {
		EARMARKChildNode result = null;
		
		if (n.getOwnerDocument().equals(this)) { /* If the EARMARK document of the node specified
		is this document, returns the node itself. */
			result = n;
		} else { /* Otherwise adopt it */
			result = copyNode(n, true);
			
			if (result != null) {
				EARMARKDocument original = n.getOwnerDocument();
				if (n instanceof Range) {
					original.removeRange((Range) n);
				} else {
					original.removeMarkupItem((MarkupItem) n, true);
				}
			}
		}
		
		return result;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EARMARKDocument other = (EARMARKDocument) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	/**
	 * This method loads an EARMARK document from either an RDF/XML or a Turtle representation of it
	 * given through a file.
	 *  
	 * @param file the file containing the RDF/XML or Turtle representation of an
	 * EARMARK document.
	 * @return the EARMARKDocument object representing the input.
	 * @throws FileNotFoundException if the input file does not exist.
	 */
	public static EARMARKDocument load(File file) throws FileNotFoundException {
		return reader.read(file);
	}
	
	/**
	 * This method loads an EARMARK document from either an RDF/XML or a Turtle representation of it
	 * given through a string.
	 *  
	 * @param content the string containing the RDF/XML or Turtle representation of an
	 * EARMARK document.
	 * @return the EARMARKDocument object representing the input.
	 */
	public static EARMARKDocument load(String content) {
		return reader.read(content);
	}
	
	/**
	 * This method loads an EARMARK document from either an RDF/XML or a Turtle representation of it
	 * given through an URL.
	 * 
	 * @param url the URL of the document containing the RDF/XML or Turtle representation of an
	 * EARMARK document.
	 * @return the EARMARKDocument object representing the input.
	 */
	public static EARMARKDocument load(URL url) {
		return reader.read(url);
	}
	
	/**
	 * This method loads an EARMARK document from a Jena model of it.
	 * 
	 * @param model the Jena model containing representation of an
	 * EARMARK document.
	 */
	public static EARMARKDocument load(Model model) {
		return reader.read(model);
	}
	
	/**
	 * This method stores the EARMARK document into a file.
	 * 
	 * @param file the file in which the document will be stored.
	 * @return true if the document has been correctly stored, false otherwise.
	 */
	public boolean store(File file) {
		return writer.write(this, file);
	}
	
	/**
	 * This method stores the EARMARK document into a file.
	 * 
	 * @param file the file in which the document will be stored.
	 * @param prefixNsPairs a map of prefix-namespace pairs for mapping resource URIs.
	 * @return true if the document has been correctly stored, false otherwise.
	 */
	public boolean store(File file, Map<String, String> prefixNsPairs) {
		writer.addPrefixes(prefixNsPairs);
		return writer.write(this, file);
	}
	
	/**
	 * This method stores the EARMARK document into a file.
	 * 
	 * @param file the file in which the document will be stored.
	 * @param format the format used as output, chosen among EARMARKWriter.N3, EARMARKWriter.TURTLE,
	 * EARMARKWriter.RDFXML, EARMARKWriter.RDFXMLABBREV and EARMARKWriter.NTRIPLE.
	 * @return true if the document has been correctly stored, false otherwise.
	 */
	public boolean store(File file, String format) {
		return writer.write(this, file, format);
	}
	
	/**
	 * This method stores the EARMARK document into a file.
	 * 
	 * @param file the file in which the document will be stored.
	 * @param format the format used as output, chosen among EARMARKWriter.N3, EARMARKWriter.TURTLE,
	 * EARMARKWriter.RDFXML, EARMARKWriter.RDFXMLABBREV and EARMARKWriter.NTRIPLE.
	 * @param prefixNsPairs a map of prefix-namespace pairs for mapping resource URIs.
	 * @return true if the document has been correctly stored, false otherwise.
	 */
	public boolean store(File file, String format, Map<String, String> prefixNsPairs) {
		writer.addPrefixes(prefixNsPairs);
		return writer.write(this, file, format);
	}
	
	/**
	 * This method returns a string containing the RDF/XML representation of the EARMARK document.
	 * 
	 * @return an RDF/XML representation of the document.
	 */
	public String getDocumentAsRDFXML() {
		return writer.write(this);
	}
	
	/**
	 * This method returns a string containing the Turtle representation of the EARMARK document.
	 * 
	 * @return a Turtle representation of the document.
	 */
	public String getDocumentAsTurtle() {
		return writer.write(this, EARMARKWriter.TURTLE);
	}
	
	/**
	 * This method returns a string containing the RDF/XML representation of the EARMARK document.
	 * 
	 * @param prefixNsPairs a map of prefix-namespace pairs for mapping resource URIs.
	 * @return an RDF/XML representation of the document.
	 */
	public String getDocumentAsRDFXML(Map<String, String> prefixNsPairs) {
		writer.addPrefixes(prefixNsPairs);
		return writer.write(this);
	}
	
	/**
	 * This method returns a string containing the Turtle representation of the EARMARK document.
	 * 
	 * @param prefixNsPairs a map of prefix-namespace pairs for mapping resource URIs.
	 * @return a Turtle representation of the document.
	 */
	public String getDocumentAsTurtle(Map<String, String> prefixNsPairs) {
		writer.addPrefixes(prefixNsPairs);
		return writer.write(this, EARMARKWriter.TURTLE);
	}
	
	/**
	 * This method returns a Jena model representing the EARMARK document.
	 * 
	 * @return a Jena model representing the document.
	 */
	public Model getDocumentAsModel() {
		return writer.getModel(this);
	}
	
	@Override
	public String toString() {
		return "{d: " + hasId() + " }";
	}
	
	@Override
	public Statement assertsAsObject(Resource subject, Property predicate) {
		return addAssertion(subject, predicate, this);
	}

	@Override
	public Statement assertsAsSubject(Property predicate, RDFNode object) {
		return addAssertion(this, predicate, object);
	}
	
	@Override
	public Statement assertsAsObject(EARMARKItem subject, Property predicate) {
		return addAssertion(subject, predicate, this);
	}

	@Override
	public Statement assertsAsSubject(Property predicate, EARMARKItem object) {
		return addAssertion(this, predicate, object);
	}
	
	@Override
	public Set<Statement> getAssertionsAsObject() {
		return getAssertionsAsObject(this);
	}

	@Override
	public Set<Statement> getAssertionsAsSubject() {
		return getAssertionsAsSubject(this);
	}
	
	@Override
	public Set<Statement> removeAllAssertions() {
		return removeAllAssertions(this);
	}
	
	/**
	 * This method return the RDF model of non-EARMARK additional assertion related to this EARMARK document.
	 * 
	 * @return an RDF model.
	 */
	public Model getModel() {
		return rdf;
	}
	
	/**
	 * This method create and add a linguistic act to the model.
	 * @param id the uri identifying the linguistic act (may be null).
	 * @param item the EARMARK item being the information entity of the linguistic act.
	 * @param reference the reference of the linguistic act.
	 * @param meaning the meaning of the linguistic act.
	 * @param agent the agent creating the linguistic act.
	 * @return the resource describing the linguistic act.
	 */
	protected Resource addLinguisticAct(
			URI id, EARMARKItem item, Resource reference, Resource meaning, Resource agent) {
		String base = "http://www.ontologydesignpatterns.org/cp/owl/semiotics.owl#";
		Property denotes = rdf.createProperty(base + "denotes");
		Property hasConceptualization = rdf.createProperty(base + "hasConceptualization");
		Property hasInterpretant = rdf.createProperty(base + "hasInterpretant");
		Property hasMeaning = rdf.createProperty(base + "hasMeaning");
		Property hasInformationEntity = rdf.createProperty(base + "hasInformationEntity");
		Property hasReference = rdf.createProperty(base + "hasReference");
		Resource LinguisticAct = rdf.createResource(base + "LinguisticAct");
		Resource information_entity = rdf.createResource(item.hasId().toString());
		
		String prov = "http://www.w3.org/ns/prov#";
		Property wasAttributedTo = rdf.createProperty(prov + "wasAttributedTo");
		Property generatedAtTime = rdf.createProperty(prov + "generatedAtTime");
		Calendar time = Calendar.getInstance();
		
		Resource result = (id == null ? rdf.createResource() : rdf.createResource(id.toString()));
		Statement statement = null;
		
		// Linguistict Act assertions
		statement = rdf.createStatement(result,RDF.type,LinguisticAct); rdf.add(statement);
		statement = rdf.createStatement(result,hasInformationEntity,information_entity); rdf.add(statement);
		
		if (reference != null) {
			statement = rdf.createStatement(result,hasReference,reference); rdf.add(statement);
			statement = rdf.createStatement(information_entity,denotes,reference); rdf.add(statement);
		}
		if (meaning != null) {
			statement = rdf.createStatement(result,hasMeaning,meaning); rdf.add(statement);
			statement = rdf.createStatement(information_entity,hasInterpretant,meaning); rdf.add(statement);
		}
		if (reference != null && meaning != null) {
			statement = rdf.createStatement(reference,hasConceptualization,meaning); rdf.add(statement);
		}
		
		// Provenance
		statement = rdf.createStatement(result,generatedAtTime,rdf.createTypedLiteral(time)); rdf.add(statement);
		if (agent != null) {
			statement = rdf.createStatement(result,wasAttributedTo,agent); rdf.add(statement);
		}
		
		return result;
	}
	
	/**
	 * This method remove all the linguistic acts having the input EARMARK item as information entity.
	 * @param item the item to consider.
	 * @return All the RDF statements removed from the model.
	 */
	protected Set<Statement> removeAllLinguisticActs(EARMARKItem item) {
		Set<Statement> result = new HashSet<Statement>();
		
		String base = "http://www.ontologydesignpatterns.org/cp/owl/semiotics.owl#";
		Property hasInformationEntity = rdf.createProperty(base + "hasInformationEntity");
		
		ResIterator linguisticActs = rdf.listSubjectsWithProperty(
				hasInformationEntity, rdf.createResource(item.hasId().toString()));
		
		while (linguisticActs.hasNext()) {
			result.addAll(removeLinguisticAct(item, linguisticActs.next()));
		}
				
		return result;
	}
	
	/**
	 * /**
	 * This method remove the linguistic acts having the input EARMARK item as information entity.
	 * @param item the item to consider.
	 * @param linguisticAct the linguistic act to remove.
	 * @return All the RDF statements removed from the model.
	 */
	protected Set<Statement> removeLinguisticAct(EARMARKItem item, Resource linguisticAct) {
		Set<Statement> result = new HashSet<Statement>();
		
		String base = "http://www.ontologydesignpatterns.org/cp/owl/semiotics.owl#";
		Property hasInformationEntity = rdf.createProperty(base + "hasInformationEntity");
		
		boolean isAssociatedToItem =  
			rdf.listStatements(
				linguisticAct,
				hasInformationEntity, 
				rdf.createResource(item.hasId().toString())).hasNext();
		
		if (isAssociatedToItem) {
			Property denotes = rdf.createProperty(base + "denotes");
			Property hasConceptualization = rdf.createProperty(base + "hasConceptualization");
			Property hasInterpretant = rdf.createProperty(base + "hasInterpretant");
			Property hasMeaning = rdf.createProperty(base + "hasMeaning");
			Property hasReference = rdf.createProperty(base + "hasReference");
			
			Resource information_entity = null;
			Resource reference = null;
			Resource meaning = null;
			
			StmtIterator objectPropertyStatements = rdf.listStatements(linguisticAct, null, (Resource) null);
			while (objectPropertyStatements.hasNext()) {
				Statement statement = objectPropertyStatements.next();
				if (statement.getPredicate().equals(hasInformationEntity)) {
					information_entity = (Resource) statement.getObject();
				}
				else if (statement.getPredicate().equals(hasMeaning)) {
					meaning = (Resource) statement.getObject();
				}
				else if (statement.getPredicate().equals(hasReference)) {
					reference = (Resource) statement.getObject();
				}
				result.add(statement);
			}
			
			rdf.remove(new ArrayList<Statement>(result));
			
			if (
					information_entity != null && reference != null &&
					!areInvolvedInLinguisticActAs(information_entity, hasInformationEntity, reference, hasReference)) {
				Statement statement = rdf.createStatement(information_entity, denotes, reference);
				result.add(statement);
				rdf.remove(statement);
			}
			
			if (
					information_entity != null && meaning != null &&
					!areInvolvedInLinguisticActAs(information_entity, hasInformationEntity, meaning, hasMeaning)) {
				Statement statement = rdf.createStatement(information_entity, hasInterpretant, meaning);
				result.add(statement);
				rdf.remove(statement);
			}
			
			if (
					reference != null && meaning != null &&
					!areInvolvedInLinguisticActAs(reference, hasReference, meaning, hasMeaning)) {
				Statement statement = rdf.createStatement(reference, hasConceptualization, meaning);
				result.add(statement);
				rdf.remove(statement);
			}
			
			StmtIterator dataPropertyStatements = rdf.listStatements(linguisticAct, null, (String) null);
			result.addAll(dataPropertyStatements.toSet());
			rdf.remove(dataPropertyStatements);
		}
		
		return result;
	}
	
	private boolean areInvolvedInLinguisticActAs(Resource entity1, Property as1, Resource entity2, Property as2) {
		return !isInvolvedInLinguisticActAs(entity2, as2, isInvolvedInLinguisticActAs(entity1, as1)).isEmpty();
	}
	
	private Set<Resource> isInvolvedInLinguisticActAs(Resource entity, Property as) {
		String base = "http://www.ontologydesignpatterns.org/cp/owl/semiotics.owl#";
		Resource LinguisticAct = rdf.createResource(base + "LinguisticAct");
		
		ResIterator linguisticActs = rdf.listSubjectsWithProperty(RDF.type, LinguisticAct);
		return isInvolvedInLinguisticActAs(entity, as, linguisticActs.toSet());
	}
	
	private Set<Resource> isInvolvedInLinguisticActAs(Resource entity, Property as, Set<Resource> linguisticActs) {
		Set<Resource> result = new HashSet<Resource>();
		
		for (Resource linguisticAct : linguisticActs) {
			if (rdf.listStatements(linguisticAct, as, entity).hasNext()) {
				result.add(linguisticAct);
			}
		}
		
		return result;
	}
	
	/**
	 * This method removes all the assertions where the item specified is
	 * a subject or an object.
	 * 
	 * @param item the item related to the statements to remove.
	 * @return all the removed statements.
	 */
	protected Set<Statement> removeAllAssertions(EARMARKItem item) {
		Set<Statement> result = new HashSet<Statement>();
		
		result.addAll(getAssertionsAsSubject(item));
		result.addAll(getAssertionsAsObject(item));
		
		Iterator<Statement> ite = result.iterator();
		while (ite.hasNext()) {
			rdf.remove(ite.next());
		}
		
		return result;
	}
	
	/**
	 * This method adds the statement defined by the triple specified (with an 
	 * EARMARK item specified as subject) to the model.
	 * 
	 * @param subject the EARMARK item subject of the statement.
	 * @param predicate the predicate property of the statement.
	 * @param object the object resource of the statement.
	 * @return the statement added.
	 */
	protected Statement addAssertion(EARMARKItem subject, Property predicate, RDFNode object) {
		Statement result = 
			rdf.createStatement(rdf.createResource(subject.hasId().toString()), predicate, object);
		rdf.add(result);
		return result;
	}
	
	/**
	 * This method adds the statement defined by the triple specified (with an 
	 * EARMARK item specified as object) to the model.
	 * 
	 * @param subject the subject resource of the statement.
	 * @param predicate the predicate property of the statement.
	 * @param object the EARMARK item object of the statement.
	 * @return the statement added.
	 */
	protected Statement addAssertion(Resource subject, Property predicate, EARMARKItem object) {
		Statement result = 
			rdf.createStatement(subject, predicate, rdf.createResource(object.hasId().toString()));
		rdf.add(result);
		return result;
	}
	
	/**
	 * This method adds the statement defined by the triple specified (with an 
	 * EARMARK item specified as subject and another one as object) to the model.
	 * 
	 * @param subject the EARMARK item subject of the statement.
	 * @param predicate the predicate property of the statement.
	 * @param object the EARMARK item object of the statement.
	 * @return the statement added.
	 */
	protected Statement addAssertion(EARMARKItem subject, Property predicate, EARMARKItem object) {
		Statement result = 
			rdf.createStatement(
					rdf.createResource(subject.hasId().toString()), 
					predicate, 
					rdf.createResource(object.hasId().toString()));
		rdf.add(result);
		return result;
	}
	
	/**
	 * This method returns all the statements in the model having 'ei' as
	 * object.
	 * 
	 * @param ei the EARMARK item object of the statements.
	 * @return all the statements with 'ei' has object.
	 */
	protected Set<Statement> getAssertionsAsObject(EARMARKItem ei) {
		Set<Statement> result = new HashSet<Statement>();
		
		Resource resource = rdf.getResource(ei.hasId().toString());
		if (resource != null) {
			StmtIterator ite = rdf.listStatements(null, null, resource);
			result.addAll(ite.toSet());
		}
		
		return result;
	}

	/**
	 * This method returns all the statements in the model having 'ei' as
	 * subject.
	 * 
	 * @param ei the EARMARK item subject of the statements.
	 * @return all the statements with 'ei' has subject.
	 */
	protected Set<Statement> getAssertionsAsSubject(EARMARKItem ei) {
		Set<Statement> result = new HashSet<Statement>();
		
		Resource resource = rdf.getResource(ei.hasId().toString());
		if (resource != null) {
			StmtIterator ite = rdf.listStatements(resource, null, (Resource) null);
			result.addAll(ite.toSet());
		}
		
		return result;
	}
	
	/**
	 * This method returns a new EARMARK document initialised with the callee identifier.
	 * 
	 * @return a new empty EARMARK document.
	 */
	public EARMARKDocument clone() {
		return new EARMARKDocument(id);
	}
	
	/* Return an document-unique URI representing an identifier from a string. */
	private URI makeId(String name) {
		String idName = (name == null ? "EARMARKitem" : name);
		
		URI id = null;
		for(int i = 1; id == null; i++) {
			URI tmpId = URI.create((hasId() == null ? "" : hasId().toString() + SEPARATOR) + idName + i);
			if (!idSet.keySet().contains(tmpId)) {
				id = tmpId;
			}
		}
		return id;
	}
	
	/* Check if the identifier is already used in the document. */
	private void checkIdentity(URI id) throws ExistingIdException {
		/* The empty string is reserved */
		if (!id.toString().equals("") && idSet.keySet().contains(id)) {
			throw new ExistingIdException("The id '" + id + "' is already used in this" +
					" EARMARK document.");
		}
	}
	
	/* Add a child node to its parent. */
	private EARMARKChildNode add(EARMARKChildNode child, EARMARKHierarchicalNode parent) {
		Collection childList = childMap.get(parent);
		Set<EARMARKHierarchicalNode> parentList = parentMap.get(child);
		childList.add(child);
		parentList.add(parent);
		return child;
	}
	
	/* Remove a particular occurrence of a child node from its parent. */
	private EARMARKChildNode remove(
			EARMARKChildNode child, EARMARKHierarchicalNode parent, int occurrence) {
		EARMARKChildNode result = null;
		
		try {
			EARMARKNode.Type parentType = parent.getNodeType();
			if (
					occurrence <= 1 || 
					parentType == EARMARKNode.Type.Document || 
					!parent.isList()) {
				boolean removed = childMap.get(parent).remove(child);
				result = (removed ? child : null);
			} else {
				it.essepuntato.earmark.core.List childBag = 
					(it.essepuntato.earmark.core.List) childMap.get(parent);
				int curOccurrence = 0;
				Iterator<EARMARKChildNode> ite = childBag.iterator();
				for(int index = 0; result == null && ite.hasNext(); index++) {
					EARMARKNode node = ite.next();
					if (node.equals(child)) {
						curOccurrence++;
					}
					
					if (occurrence == curOccurrence) {
						result = childBag.remove(index);
					}
				}
			}
			
			if (result != null) {
				parentMap.get(child).remove(parent);
			}
		} catch (NullPointerException e) {
			// Do nothing
		}
		
		return result;
	}
	
	/* Remove the first occurrence of a child node from its parent. */
	private EARMARKChildNode remove(EARMARKChildNode oldChild, EARMARKHierarchicalNode parent) {
		return remove(oldChild, parent, 1);
	}
	
	/* Remove all the occurrences of a child node from its parent. */
	private EARMARKChildNode removeAll(
			EARMARKChildNode child, EARMARKHierarchicalNode parent) {
		try {
			Collection listChild = childMap.get(parent);
			boolean contain = listChild.contains(child);
			
			while (listChild.remove(child)) {
				parentMap.get(child).remove(parent);
			}
			
			return (contain ? child : null);
		} catch (NullPointerException e) {
			return null;
		}
	}
	
	/* Replace a particular occurrence of a child node with another one from its parent. */
	private EARMARKChildNode replace(
			EARMARKChildNode newChild, EARMARKChildNode oldChild, 
			EARMARKHierarchicalNode parent, int occurrence) {
		EARMARKChildNode result = null;
		
		if (!newChild.equals(oldChild)) {
			if (occurrence <= 1 || !parent.isList()) {
				Collection children = childMap.get(parent);
				boolean replaced = children.replace(newChild, oldChild);
				result = (replaced ? oldChild : null);
			} else {
				it.essepuntato.earmark.core.List childBag = 
					(it.essepuntato.earmark.core.List) childMap.get(parent);
				int curOccurrence = 0;
				Iterator<EARMARKChildNode> ite = childBag.iterator();
				for(int index = 0; result == null && ite.hasNext(); index++) {
					EARMARKNode node = ite.next();
					if (node.equals(oldChild)) {
						curOccurrence++;
					}
					
					if (occurrence == curOccurrence) {
						result = childBag.remove(index);
						childBag.add(index, newChild);
					}
				}
			}
		}
		
		if (result != null) {
			parentMap.get(oldChild).remove(parent);
			parentMap.get(newChild).add(parent);
		}
		
		return result;
	}
	
	/* Replace the first occurrence of a child node with another one from its parent. */
	private EARMARKChildNode replace(
			EARMARKChildNode newChild, 
			EARMARKChildNode oldChild, 
			EARMARKHierarchicalNode parent) {
		return replace(newChild, oldChild, parent, 1);
	}
	
	/* Replace all the occurrences of a child node with another one from its parent. */
	private EARMARKChildNode replaceAll(
			EARMARKChildNode newChild, 
			EARMARKChildNode oldChild, 
			EARMARKHierarchicalNode parent) {
		boolean contain = false;
		
		if (!newChild.equals(oldChild)) {
			Collection listChild = childMap.get(parent);
			contain = listChild.contains(oldChild);
		
			while (listChild.replace(newChild, oldChild)) {
				parentMap.get(oldChild).remove(parent);
				parentMap.get(newChild).add(parent);
			}
		}
		
		return (contain ? oldChild : null);
	}
	
	/* Create and initialise a new markup item according to the type specified. */
	private MarkupItem createMarkupItem(
			URI id, String gi, URI ns, Collection.Type type, EARMARKNode.Type markupType) {
		MarkupItem markup = null;
		
		if (markupType == EARMARKNode.Type.Attribute) {
			markup = new Attribute(this, gi, ns, type, id);
		} else if (markupType == EARMARKNode.Type.Comment) {
			markup = new Comment(this, gi, ns, type, id);
		} else {
			markup = new Element(this, gi, ns, type, id);
		}
		
		idSet.put(id, markup);
		
		String currentGi = (gi == null ? "" : gi);
		
		Set<MarkupItem> associated = giMap.get(currentGi);
		if (associated == null) {
			associated = new HashSet<MarkupItem>();
			giMap.put(currentGi, associated);
		}
		associated.add(markup);
		
		URI currentNs = (ns == null ? URI.create("") : ns);
		
		Set<String> gies = namespaces.get(currentNs);
		if (gies == null) {
			gies = new HashSet<String>();
			namespaces.put(currentNs,gies);
		}
		gies.add(currentGi);		
		
		Collection children = null;
		if (type == Collection.Type.Bag) {
			children = new Bag();
		} else if (type == Collection.Type.Set) {
			children = new it.essepuntato.earmark.core.Set();
		} else {
			children = new it.essepuntato.earmark.core.List();
		}
		
		userData.put(markup, new HashMap<String,Object>());
		childMap.put(markup, children);
		parentMap.put(markup, new HashSet<EARMARKHierarchicalNode>());
		
		return markup;
	}
	
	/* Return a range if it was previously defined in the document. */
	@SuppressWarnings("unchecked")
	private Range getWhetherItExists(Docuverse d, Integer realB, Integer realE, String xpath) {
		Range result = null;

		Integer b = (realB == null ? -1 : realB);
		Integer e = (realE == null ? -1 : realE);
		
		Set<Range> dSet = docuverseMap.get(d);
		
		Set<Range> bSet = null;
		if (b != null) {
			bSet = rangeBeginLocationMap.get(b);
		}
		
		Set<Range> eSet = null;
		if (e != null) {
			eSet = rangeEndLocationMap.get(e);
		}
		
		Set<Range> xSet = null;
		if (xpath != null) {
			xSet = rangeXPathPointerMap.get(xpath);
		}
		
		boolean bApply = false;
		boolean eApply = false;
		boolean xApply = false;
		
		Set<Range> set = new HashSet<Range>(dSet);
		
		if (!set.isEmpty() && bSet != null) {
			set = SetFacility.intersect(set, bSet);
			bApply = true;
		} 
		if (!set.isEmpty() && eSet != null) {
			set = SetFacility.intersect(set, eSet);
			eApply = true;
		}
		if (!set.isEmpty() && xSet != null) {
			set = SetFacility.intersect(set, xSet);
			xApply = true;
		}
		
		boolean setChangedCoherently = bApply && eApply && (xApply || xpath == null);
		
		if (!set.isEmpty() && setChangedCoherently) {
			result = set.iterator().next();
		}
		
		return result;
	}
	
	/* Get a full URI from a string. */
	private URI getURI(String id) {
		URI currentId = null;
		
		if (id.matches(".+://.+")) {
			currentId = URI.create(id);
		} else {
			currentId = 
				URI.create((hasId() != null && !hasId().equals("") ? hasId().toString() + SEPARATOR : "") + id);
		}
		
		return currentId;
	}

	@Override
	public Resource addLinguisticAct(Resource reference, Resource meaning,
			Resource agent) {
		return addLinguisticAct(null, this, reference, meaning, agent);
	}

	@Override
	public Resource addLinguisticAct(URI uri, Resource reference,
			Resource meaning, Resource agent) {
		return addLinguisticAct(uri, this, reference, meaning, agent);
	}

	@Override
	public Set<Statement> removeAllLinguisticActs() {
		return removeAllLinguisticActs(this);
	}

	@Override
	public Set<Statement> removeLinguisticAct(Resource linguisticAct) {
		return removeLinguisticAct(this, linguisticAct);
	}
}
