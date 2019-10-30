package schema;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;

public class SFWCSchema {
	
	public static final String NIF_URI = "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#";
	public static final String ITSRDF_URI = "http://www.w3.org/2005/11/its/rdf#";
	public static final String SFWC_URI = "http://sfwcrawler.com/core#";
	
	//Classes
	
	public static Resource ENTITY;
	public static Resource RELATION;
	public static Resource DOCUMENT;
	
	//Properties
	
	public static Property hasNE;
	public static Property hasRel;
	public static Property inDocument;
	public static Property tfValue;
	public static Property idfValue;
	public static Property tfIdf;
	public static Property subject;
	public static Property object;
	public static Property correlationValue;
	public static Property ner;
	public static Property sentence;
	
	private static Model MODEL = ModelFactory.createDefaultModel();
	
	private static void init() {
		//Classes
		ENTITY = MODEL.createResource(SFWC_URI+"Entity");
		MODEL.add(ENTITY, RDF.type, OWL.Class);
		MODEL.add(ENTITY, SKOS.prefLabel, "Entity");
		
		RELATION = MODEL.createResource(SFWC_URI+"Relation");
		MODEL.add(RELATION, RDF.type, OWL.Class);
		MODEL.add(RELATION, SKOS.prefLabel, "Relation");
		
		DOCUMENT = MODEL.createResource(SFWC_URI+"Document");
		MODEL.add(DOCUMENT, RDF.type, OWL.Class);
		MODEL.add(DOCUMENT, SKOS.prefLabel, "Document");
		
		//Properties
		hasNE = MODEL.createProperty(SFWC_URI+"hasNE");
		MODEL.add(hasNE, RDF.type, OWL.DatatypeProperty);
		MODEL.add(hasNE, RDFS.domain, DOCUMENT);
		
		hasRel = MODEL.createProperty(SFWC_URI+"hasRel");
		MODEL.add(hasRel, RDF.type, OWL.DatatypeProperty);
		MODEL.add(hasRel, RDFS.domain, DOCUMENT);
		
		inDocument = MODEL.createProperty(SFWC_URI+"inDocument");
		MODEL.add(inDocument, RDF.type, OWL.DatatypeProperty);
		MODEL.add(inDocument, RDFS.domain, ENTITY);
		
		tfValue = MODEL.createProperty(SFWC_URI+"tfValue");
		MODEL.add(tfValue, RDF.type, OWL.DatatypeProperty);
		MODEL.add(tfValue, RDFS.domain, ENTITY);
		
		idfValue = MODEL.createProperty(SFWC_URI+"idfValue");
		MODEL.add(idfValue, RDF.type, OWL.DatatypeProperty);
		MODEL.add(idfValue, RDFS.domain, ENTITY);
		
		correlationValue = MODEL.createProperty(SFWC_URI+"correlation");
		MODEL.add(correlationValue, RDF.type, OWL.DatatypeProperty);
		MODEL.add(correlationValue, RDFS.domain, OWL.DatatypeProperty);
		
		subject = MODEL.createProperty(SFWC_URI+"subject");
		MODEL.add(subject, RDF.type, OWL.DatatypeProperty);
		MODEL.add(subject, RDFS.domain, RELATION);
		
		object = MODEL.createProperty(SFWC_URI+"object");
		MODEL.add(object, RDF.type, OWL.DatatypeProperty);
		MODEL.add(object, RDFS.domain, RELATION);
		
		ner = MODEL.createProperty(SFWC_URI + "ner");
		MODEL.add(ner, RDF.type, OWL.DatatypeProperty);
		MODEL.add(ner, RDFS.domain, ENTITY);
		
		sentence = MODEL.createProperty(SFWC_URI+"sentence");
		MODEL.add(sentence, RDF.type, OWL.DatatypeProperty);
		MODEL.add(sentence, RDFS.domain, RELATION);
		
		//Prefixes
		MODEL.setNsPrefix("rdf", RDF.uri);
		MODEL.setNsPrefix("rdfs", RDFS.uri);
		MODEL.setNsPrefix("sfcw", SFWC_URI);
		MODEL.setNsPrefix("nif", NIF_URI);
		MODEL.setNsPrefix("itsrdf", ITSRDF_URI);
		MODEL.setNsPrefix("skos", SKOS.uri);
		MODEL.setNsPrefix("owl", OWL.NS);
		
		
	}
	
	public static Model getModel() {
		init();
		return MODEL;
	}

	public static void main(String[] args) {

	}

}
