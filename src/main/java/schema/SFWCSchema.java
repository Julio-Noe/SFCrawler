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
	public static Resource TOPIC;
	
	//Properties
	
	public static Property hasEntity;
	public static Property hasRelation;
	public static Property inDocument;
	public static Property tfValue;
	public static Property idfValue;
	public static Property tfIdfValue;
	public static Property subjectLabel;
	public static Property objectLabel;
	public static Property correlationValue;
	public static Property neTag;
	public static Property sentence;
	public static Property subjectEntity;
	public static Property objectEntity;
	public static Property hasAssociatedTopic;
	
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
		
		TOPIC = MODEL.createResource(SFWC_URI+"Topic");
		MODEL.add(TOPIC, RDF.type, OWL.Class);
		MODEL.add(TOPIC, SKOS.prefLabel, "Topic");
		
		//Properties
		hasEntity = MODEL.createProperty(SFWC_URI+"hasNE");
		MODEL.add(hasEntity, RDF.type, OWL.DatatypeProperty);
		MODEL.add(hasEntity, RDFS.domain, DOCUMENT);
		
		hasRelation = MODEL.createProperty(SFWC_URI+"hasRel");
		MODEL.add(hasRelation, RDF.type, OWL.DatatypeProperty);
		MODEL.add(hasRelation, RDFS.domain, DOCUMENT);
		
		inDocument = MODEL.createProperty(SFWC_URI+"inDocument");
		MODEL.add(inDocument, RDF.type, OWL.DatatypeProperty);
		MODEL.add(inDocument, RDFS.domain, ENTITY);
		
		tfValue = MODEL.createProperty(SFWC_URI+"tfValue");
		MODEL.add(tfValue, RDF.type, OWL.DatatypeProperty);
		MODEL.add(tfValue, RDFS.domain, ENTITY);
		
		idfValue = MODEL.createProperty(SFWC_URI+"idfValue");
		MODEL.add(idfValue, RDF.type, OWL.DatatypeProperty);
		MODEL.add(idfValue, RDFS.domain, ENTITY);
		
		tfIdfValue = MODEL.createProperty(SFWC_URI+"tfIdfValue");
		MODEL.add(tfIdfValue, RDF.type, OWL.DatatypeProperty);
		MODEL.add(tfIdfValue, RDFS.domain, ENTITY);
		
		correlationValue = MODEL.createProperty(SFWC_URI+"correlation");
		MODEL.add(correlationValue, RDF.type, OWL.DatatypeProperty);
		MODEL.add(correlationValue, RDFS.domain, OWL.DatatypeProperty);
		
		subjectLabel = MODEL.createProperty(SFWC_URI+"subject");
		MODEL.add(subjectLabel, RDF.type, OWL.DatatypeProperty);
		MODEL.add(subjectLabel, RDFS.domain, RELATION);
		
		objectLabel = MODEL.createProperty(SFWC_URI+"object");
		MODEL.add(objectLabel, RDF.type, OWL.DatatypeProperty);
		MODEL.add(objectLabel, RDFS.domain, RELATION);
		
		neTag = MODEL.createProperty(SFWC_URI + "ner");
		MODEL.add(neTag, RDF.type, OWL.DatatypeProperty);
		MODEL.add(neTag, RDFS.domain, ENTITY);
		
		sentence = MODEL.createProperty(SFWC_URI+"sentence");
		MODEL.add(sentence, RDF.type, OWL.DatatypeProperty);
		MODEL.add(sentence, RDFS.domain, RELATION);
		
		subjectEntity = MODEL.createProperty(SFWC_URI+"subjectEntity");
		MODEL.add(subjectEntity, RDF.type, OWL.DatatypeProperty);
		MODEL.add(subjectEntity, RDFS.domain, RELATION);
		
		objectEntity = MODEL.createProperty(SFWC_URI+"objectEntity");
		MODEL.add(objectEntity, RDF.type, OWL.DatatypeProperty);
		MODEL.add(objectEntity, RDFS.domain, RELATION);
		
		hasAssociatedTopic = MODEL.createProperty(SFWC_URI+"hasAssociatedTopic");
		MODEL.add(hasAssociatedTopic, RDF.type, OWL.DatatypeProperty);
		MODEL.add(hasAssociatedTopic, RDFS.domain, DOCUMENT);
		
		//Prefixes
		MODEL.setNsPrefix("rdf", RDF.uri);
		MODEL.setNsPrefix("rdfs", RDFS.uri);
		MODEL.setNsPrefix("sfwc", SFWC_URI);
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
