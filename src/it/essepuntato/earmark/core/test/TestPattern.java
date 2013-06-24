package it.essepuntato.earmark.core.test;

import it.essepuntato.earmark.core.EARMARKChildNode;
import it.essepuntato.earmark.core.EARMARKDocument;
import it.essepuntato.earmark.core.EARMARKHierarchicalNode;
import it.essepuntato.earmark.core.EARMARKNode;
import it.essepuntato.earmark.core.Element;
import it.essepuntato.earmark.core.MarkupItem;
import it.essepuntato.earmark.core.Range;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

public class TestPattern {

	/**
	 * @param args
	 * @throws LoadStoreException 
	 * @throws FileNotFoundException 
	 * @throws OWLOntologyCreationException 
	 */
	public static void main(String[] args) throws  /*LoadStoreException, OWLOntologyCreationException*/ FileNotFoundException {
		String[] paths = {"ParadiseLost.owl", "doceng.owl"};
		
		Set<String> marker = new HashSet<String>();
		Set<String> atom = new HashSet<String>();
		Set<String> mixed = new HashSet<String>();
		Set<String> hierarchical = new HashSet<String>();
		
		Set<String> inline = new HashSet<String>();
		Set<String> block = new HashSet<String>();
		Set<String> container = new HashSet<String>();
		Set<String> record = new HashSet<String>();
		Set<String> table = new HashSet<String>();
		Set<String> meta = new HashSet<String>();
		Set<String> milestone = new HashSet<String>();
		
		add("<experiments>");
		for (String path : paths) {
			EARMARKDocument doc = EARMARKDocument.load(new File(path));
			long start = time();
			
			Map<String,Set<Element>> giNs = new HashMap<String,Set<Element>>();
			
			/* Split all elements according to their GI and namespace */
			for (EARMARKNode n : doc.getAllEARMARKNode()) {
				if (n.getNodeType() == EARMARKNode.Type.Element) {
					String curGiNs = getId(n);
					Set<Element> curElements = giNs.get(curGiNs);
					if (curElements == null) {
						curElements = new HashSet<Element>();
						giNs.put(curGiNs, curElements);
					}
					curElements.add((Element)n);
				}
			}
			
			/* Step 1: atom and high-level sets */
			for(String id : giNs.keySet()) {
				if (containsElement(giNs.get(id)) && containsText(giNs.get(id))) {
					mixed.add(id);
				} else if (containsElement(giNs.get(id))) {
					hierarchical.add(id);
				} else if (containsText(giNs.get(id))) {
					atom.add(id);
				} else {
					marker.add(id);
				}
			}
			
			/* Step 2: inline */
			Set<String> mixedOrHierarchical = new HashSet<String>();
			mixedOrHierarchical.addAll(mixed);
			mixedOrHierarchical.addAll(hierarchical);
			
			/* Step 2.1: contained in Mixed */
			for (String id : mixedOrHierarchical) {
				if (isContainedIn(giNs.get(id), mixed)) {
					inline.add(id);
					break;
				}
			}
			
			/* Step 2.2: contained-at-n */
			boolean repeat = true;
			while (repeat) {
				repeat = false;
				
				for (String id : mixed) {
					if (!inline.contains(id)) {
						if (containsItself(giNs.get(id), new HashSet<Element>())) {
							inline.add(id);
							repeat = true;
							break;
						}
					}
				}
			}
			
			/* Step 2.3: sibling of inline */
			repeat = true;
			while (repeat) {
				repeat = false;
				
				for (String id : mixed) {
					if (!inline.contains(id)) {
						if (brotherContainedIn(giNs.get(id), inline)) {
							inline.add(id);
							repeat = true;
							break;
						}
					}
				}
			}
			
			/* Step 3: block */
			
			/* Step 3.1: hierarchical with inline children */
			for (String id : hierarchical) {
				if (!block.contains(id)) {
					if (childContainedIn(giNs.get(id), inline)) {
						mixed.add(id);
					}
				}
			}
			
			/* Step 3.2: mixed \ inline */
			block.addAll(mixed);
			block.removeAll(inline);
			
			/* Step 4: container (= hierarchical \ (block U inline)) */
			container.addAll(hierarchical);
			container.removeAll(block);
			container.removeAll(inline);
			
			/* Step 5: container subclasses */
			for (String id : container) {
				Set<EARMARKChildNode> children = new HashSet<EARMARKChildNode>();
				for (Element e : giNs.get(id)) {
					children.addAll(e.getChildElements());
				}
				
				Set<String> childId = new HashSet<String>();
				for (EARMARKChildNode child : children) {
					childId.add(getId(child));
				}
				
				/* Step 5.1: record */
				if (childId.size() == children.size()) {
					record.add(id);
				}
				
				/* Step 5.2: table */
				if (childId.size() == 1) {
					table.add(id);
				}
			}
			
			/* Step 6: milestone */
			for (String id : marker) {
				if (parentContainedIn(giNs.get(id), mixed)) {
					milestone.add(id);
				}
			}
			
			/* Step 7: meta (marker \ milestone) */
			meta.addAll(marker);
			meta.removeAll(milestone);
			
			long end = time();
			
			add("<experiment url=\""+ path +"\">");
			add("<pattern time=\"" + (end - start) + "\">");
			if (!milestone.isEmpty()) add("<Milestone>" + milestone.toString().replace("[", "").replace("]", "") + "</Milestone>");
			if (!meta.isEmpty()) add("<Meta>" + meta.toString().replace("[", "").replace("]", "") + "</Meta>");
			if (!atom.isEmpty()) add("<Atom>" + atom.toString().replace("[", "").replace("]", "") + "</Atom>");
			if (!inline.isEmpty()) add("<Inline>" + inline.toString().replace("[", "").replace("]", "") + "</Inline>");
			if (!block.isEmpty()) add("<Block>" + block.toString().replace("[", "").replace("]", "") + "</Block>");
			if (!container.isEmpty()) add("<Container>" + container.toString().replace("[", "").replace("]", "") + "</Container>");
			if (!record.isEmpty()) add("<Record>" + record.toString().replace("[", "").replace("]", "") + "</Record>");
			if (!table.isEmpty()) add("<Table>" + table.toString().replace("[", "").replace("]", "") + "</Table>");
			add("</pattern>");
			
			for (String id : giNs.keySet()) {
				String current = "";
				
				if (record.contains(id)) {
					current = "Record";
				} else if (table.contains(id)) {
					current = "Table";
				} else if (container.contains(id)) {
					current = "Container";
				} else if (block.contains(id)) {
					current = "Block";
				} else if (inline.contains(id)) {
					current = "Inline";
				} else if (atom.contains(id)) {
					current = "Atom";
				} else if (meta.contains(id)) {
					current = "Meta";
				} else if (milestone.contains(id)) {
					current = "milestone";
				} else {
					System.err.println("ERROR!");
				}
				
				String pattern = "http://www.essepuntato.it/2008/12/pattern#" + current;
				
				for (Element e : giNs.get(id)) {
					e.assertsAsSubject(RDF.type, doc.getModel().createResource(pattern));
				}
			}
			
			//aggiungere l'include
			doc.assertsAsSubject(OWL.imports, doc.getModel().createResource(
					"http://www.essepuntato.it/2008/12/pattern"));
			
			//aggiungere dichiarazione di radice
			for (EARMARKChildNode n : doc.getChildElements()) {
				n.assertsAsSubject(RDF.type, doc.getModel().createResource(
						"http://www.essepuntato.it/2008/12/pattern#RootElement"));
			}
			
			//System.out.println(doc.toRDFXML());
//			StringInputSource string = new StringInputSource(doc.toRDFXML());
//			
//			OWLOntologyManager owl = OWLManager.createOWLOntologyManager();
//			
//			/* Add the URI mapping for the EARMARK and the Collection ontologies */
//			SimpleURIMapper erk = new SimpleURIMapper(
//					EARMARKOntologyURI.EARMARK, 
//					new File("earmark.owl").getAbsoluteFile().toURI());
//			SimpleURIMapper coll = 
//				new SimpleURIMapper(
//						URI.create(EARMARKOntologyURI.COLLECTIONS.toString() + ".owl"), 
//						new File("collections.owl").getAbsoluteFile().toURI());
//			SimpleURIMapper pattern = 
//				new SimpleURIMapper(
//						URI.create("http://www.essepuntato.it/2008/12/pattern"), 
//						new File("pattern.owl").getAbsoluteFile().toURI());
			
//			owl.addURIMapper(coll);
//			owl.addURIMapper(erk);
//			owl.addURIMapper(pattern);
//			
//			OWLOntology onto = owl.loadOntology(string);
//			
//			long startTime = time();
//			Reasoner r = new Reasoner(owl);
//			r.loadOntology(onto);
//			boolean isConsistent = r.isConsistent();
//			long endTime = time();
//			
//			String res = "";
//			if (isConsistent) {
//				res = "yes";
//			} else {
//				res = "no";
//			}
//			add("<isConsistent time=\"" + (endTime - startTime) + "\">" + res + "</isConsistent>");
			
			add("</experiment>");
		}
		add("</experiments>");
		
		System.out.println(finalResult);
	}
	
	public static boolean brotherContainedIn(Set<Element> e, Set<String> s) {
		boolean result = false;
		
		for (Element c : e) {
			result = brotherContainedIn(c, s);
			if (result) {
				break;
			}
		}
		
		return result;
	}
	
	public static boolean brotherContainedIn(Element e, Set<String> s) {
		boolean result = false;
		
		Set<EARMARKChildNode> brothers = new HashSet<EARMARKChildNode>();
		
		for (EARMARKNode parent : e.getParentNodes()) {
			if (parent.getNodeType() == EARMARKNode.Type.Element) {
				brothers.addAll(((EARMARKHierarchicalNode)parent).getChildElements());
			}
		}
		
		brothers.remove(e);
		
		for (EARMARKChildNode c: brothers) {
			if (s.contains(getId(c))) {
				result = true;
				break;
			}
		}
		
		return result;
	}
	
	public static boolean parentContainedIn(Set<Element> e, Set<String> s) {
		boolean result = false;
		
		for (Element c : e) {
			result = parentContainedIn(c, s);
			if (result) {
				result = true;
				break;
			}
		}
		
		return result;
	}
	
	public static boolean parentContainedIn(Element e, Set<String> s) {
		boolean result = false;
		
		for (EARMARKNode p : e.getParentNodes()) {
			if (p.getNodeType() == EARMARKNode.Type.Element) {
				result = isContainedIn((Element) p, s);
				if (result) {
					result = true;
					break;
				}
			}
		}
		
		return result;
	}
	
	public static boolean childContainedIn(Set<Element> e, Set<String> s) {
		boolean result = false;
		
		for (Element c : e) {
			result = childContainedIn(c, s);
			if (result) {
				result = true;
				break;
			}
		}
		
		return result;
	}
	
	public static boolean childContainedIn(Element e, Set<String> s) {
		boolean result = false;
		
		for (EARMARKChildNode c : e.getChildElements()) {
			result = isContainedIn(c, s);
			if (result) {
				result = true;
				break;
			}
		}
		
		return result;
	}
	
	public static boolean containsItself(Set<Element> e, Set<Element> visited) {
		boolean result = false;
		
		for (EARMARKHierarchicalNode tmpN : e) {
			Element n = (Element) tmpN;
			result = containsItself(n, visited);
			
			if (result) {
				break;
			}
		}
		
		return result;
	}
	
	public static boolean containsItself(Element e, Set<Element> visited) {
		boolean result = false;
		
		String eId = getId(e);
		
		for (EARMARKChildNode tmpN : e.getChildElements()) {
			Element n = (Element) tmpN;
			if (!visited.contains(n)) {
				visited.add(n);
				
				if (eId.equals(getId(n))) {
					result = true;
					break;
				} else {
					result = containsItself( n, visited);
				}
			}
		}
		
		return result;
	}
	
	public static boolean isContainedIn(Set<Element> e, Set<String> s) {
		boolean result = false;
		
		for (EARMARKChildNode cur : e) {
			result = isContainedIn(cur, s);
			if (result) {
				break;
			}
		}
			
		return result;
	}
	
	public static boolean isContainedIn(EARMARKChildNode e, Set<String> s) {
		boolean result = false;
		
		for (EARMARKNode parent : e.getParentNodes()) {
			if (s.contains(getId(parent))) {
				result = true;
				break;
			}
		}
			
		return result;
	}
	
	public static String getId(EARMARKNode e) {
		String ns = null;
		String name = null;
		
		if (e instanceof MarkupItem) {
			ns = ((MarkupItem)e).hasNamespace().toString();
			name = ((MarkupItem)e).hasGeneralIdentifier();
		}
		
		return (ns == null ? "" : ns + "#") + (name == null ? "" : name);
	}
	
	public static boolean containsText(Set<Element> s) {
		boolean result = false;
		
		for (Element e : s) {
			result = containsText(e); 
			if (result) {
				break; 
			}
		}
		
		return result;
	}
	
	public static boolean containsElement(Set<Element> s) {
		boolean result = false;
		
		for (Element e : s) {
			result = containsElement(e); 
			if (result) {
				break; 
			}
		}
		
		return result;
	}
	
	public static boolean containsText(Element e) {
		boolean result = false;
		
		for (EARMARKNode n : e.getChildNodes()) {
			if (n instanceof Range) {
				result = true;
				break;
			}
		}
		
		return result;
	}
	
	public static boolean containsElement(Element e) {
		boolean result = false;
		
		for (EARMARKNode n : e.getChildNodes()) {
			if (n.getNodeType() == EARMARKNode.Type.Element) {
				result = true;
				break;
			}
		}
		
		return result;
	}
	
	public static long time() {
		Calendar cal = Calendar.getInstance();
		return cal.getTimeInMillis();
	 }
	
	private static String finalResult = "";
	
	public static void add(String s) {
		finalResult += "\n" + s;
	}
}
