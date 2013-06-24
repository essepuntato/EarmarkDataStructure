package it.essepuntato.earmark.core.test;

import it.essepuntato.earmark.core.EARMARKChildNode;
import it.essepuntato.earmark.core.EARMARKDocument;
import it.essepuntato.earmark.core.EARMARKHierarchicalNode;
import it.essepuntato.earmark.core.Range;

import java.util.ArrayList;
import java.util.List;

/*
Ho alcune segnalazioni che riguardano le EARMARK API, spero ti siano utili:

1a) getNamespaceURI() ritorna una stringa (implementa un metodo dichiarato
nell'interfaccia EARMARKNamedNode) "The namespace URI of the general
identifier associated to this node, or null if it is unspecified."

hasNamespace: ritorna URI "This method returns the namespace of this
markup item."

Forse c'e' da rivedere il javadoc: in realtà una torna l'URI del
namespace, l'altro la stringa del namespace. E poi non è lineare: per l'ID
c'è
solo il metodo hasID che ritorna l'URI dell'ID, e non il metodo per avere
la stringa: secondo me il metodo per la stringa andrebbe messo o in
entrambi o eliminato.

1b) EARMARKDocument.generalIdentifierIsUsedBy(""); e
   EARMARKDocument.getMarkupItemByGeneralIdentifier("");
sono metodi diversi o duplicati? L'implementazione è diversa, la semantica
sembra la stessa dal javadoc.

1c) EARMARKDocument.removeMarkupItem(mi)
rimuove anche tutto il contenuto del markup item (tutti gli elementi
dominati, a qualsiasi livello di profondita'), anche se altri sono
in overlap su di esso. E' corretto cosi?

1d) problema in cloneNode() : la/le radici del grafo non sono appese al
documento. Per esempio se:

	EARMARKDocument earDoc;
	// in earDoc carico un documento EARMARK

	// poi clono il documento
	EARMARKDocument earDocMainHierarchy;
	earDocMainHierarchy  = (EARMARKDocument)earDoc.cloneNode(true);

	// cerco gi elementi figli del documento
	Collection roots = earDoc.getChildElements();
	Collection roots2 = earDocMainHierarchy.getChildElements();

e qui trovo roots = { elemento/i radice }, mentre roots2 = {} vuoto.

1d2) l'elemento radice di un EARMARKDocument non ha genitori:
rootElement.getParentNodes() ritorna un insieme vuoto: è corretto? non
dovrebbe restituire l'EARMARKDocument che lo contiene?

1e) in un documento clonato il metodo hasChildNodes() non funziona
correttamente. Per esempio:

	Collection children = node.getChildNodes();
	// children contiene tre elementi, però se poi invoco
	boolean hasChildren = node.hasChildNodes();
	// hasChildren è false...

1f) invocando una createPointerRange(Docuverse docuverse, Integer begin,
Integer end) [la versione senza id specificato nei parametri] capita che
mi generi un range con id gia' esistente nel documento (in particolare mi
crea un range con id "r1" che era utilizzato precedentemente gia' da un
altro range).
Questo pero' capita in un documento ottenuto in seguito a una cloneNode()
di un altro documento: puo' essere che sia dovuto a strutture non
aggiornate.
 */
public class FrancescoPoggiTestOne extends AbstractTest {

	private EARMARKDocument document = null;
	
	public FrancescoPoggiTestOne(EARMARKDocument doc) {
		document = doc;
	}
	
	@Override
	public List<String> doTest() {
		List<String> result = new ArrayList<String>();
		
		result.add("\n[i] Check parents of a root node");
		EARMARKChildNode child = document.getChildNodes().iterator().next();
		String msg1 = "Check if " + child + " has the document has father, test";
		if (child.getParentNodes().contains(document)) {
			result.add(passed(msg1));
		} else {
			result.add(failed(msg1, "The parents are: " + child.getParentNodes()));
		}
		
		result.add("\n[i] Clone the document");
		EARMARKDocument newDoc = (EARMARKDocument) document.cloneNode(true);
		result.add("\n[i] Compare children of the document with the cloned one");
		String msg2 = "Check if " + newDoc + " (cloned document) has the same number of" +
				" children of " + document + " (original document), test";
		if (document.getChildNodes().size() == newDoc.getChildNodes().size()) {
			result.add(passed(msg2));
		} else {
			result.add(failed(msg2, "The children are respectively: " +
					"\n\t- " + document + ": " + document.getChildNodes() +
					"\n\t- " + newDoc + ": " + newDoc.getChildNodes()));
		}
		
		result.add("\n[i] Methods for children in the cloned document");
		String msg3 = "Check if in " + newDoc + " (cloned document) the methods getChildNodes() and hasChildNodes()" +
				"return consistent answers, both for the document and for a random node of it, test";
		EARMARKHierarchicalNode childCloned = (EARMARKHierarchicalNode) newDoc.getChildNodes().iterator().next();
		if (
				newDoc.getChildNodes().isEmpty() == !newDoc.hasChildNodes() &&
				childCloned.getChildNodes().isEmpty() == ! childCloned.hasChildNodes()) {
			result.add(passed(msg3));
		} else {
			result.add(failed(msg3, "The children are respectively: " +
					"\n\t- " + newDoc + ": " + newDoc.getChildNodes() +
					"\n\t- " + childCloned + ": " + childCloned.getChildNodes()));
		}
		
		result.add("\n[i] Create a new pointer range in the cloned document");
		String msg4 = "Check if in " + newDoc + " (cloned document) the methods for creating a new pointer range" +
				" return a range with an ID that was already present in the document, test";
		Range pRange = newDoc.createPointerRange(newDoc.getAllDocuverses().iterator().next(), null, null);
		if (pRange == null) {
			result.add(failed(msg4, "The range was not created."));
		} else {
			result.add(passed(msg4));
		}
		
		return result;
	}

	@Override
	public String getTestName() {
		return "Francesco Poggi test 1";
	}

	@Override
	public boolean useDocument(EARMARKDocument document) {
		this.document = document;
		return true;
	}

}
