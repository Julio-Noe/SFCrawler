package db;


import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import nlp.Annotation;
import nlp.DocumentAnnotation;
import nlp.OpenRelation;

public class MongoDBUtils {
	private String database = "SFWC";
	private String collection = "computerScience";
	

	public static void main(String[] args) {
		

	}
	
	public void dropCollection(String documentName) {
		MongoClient client = new MongoClient();
		MongoDatabase db = client.getDatabase(database);
		MongoCollection<Document> coll = db.getCollection(collection);
		
		Document doc = coll.find(Filters.eq("_id", documentName)).first();
		
		if(doc != null)
			coll.deleteOne(new BasicDBObject("_id", documentName));
	}
	
	public void sotoreDocument(DocumentAnnotation docAnn, String documentName, String documentContent) {
		MongoClient client = new MongoClient();
		MongoDatabase db = client.getDatabase(database);
		MongoCollection<Document> coll = db.getCollection(collection);
		
		Document documentObj = new Document()
				.append("_id", documentName)
				.append("content", documentContent)
				.append("EN", createListAnnotations(docAnn.getAnnotationList()))
				.append("rel", createListRelations(docAnn.getOpenRelationList()));
		
		System.out.println("Inserting document");
		coll.insertOne(documentObj);
	}
	
	private List<DBObject> createListAnnotations(List<Annotation> annotationList){
		List<DBObject> annotationObjList = new ArrayList<DBObject>();
		for(Annotation ann : annotationList) {
			DBObject annObj = new BasicDBObject()
					.append("lemma", ann.getLemma())
					.append("posTag", ann.getPosTag())
					.append("ner", ann.getNer())
					.append("uri", ann.getUri())
					.append("begin", ann.getBegin())
					.append("end", ann.getEnd())
					.append("tf", ann.gettf())
					.append("idf", ann.getIdf());
			annotationObjList.add(annObj);
		}
		return annotationObjList;
	}
	
	private List<DBObject> createListAnnotations(Annotation ann){
		List<DBObject> annotationObjList = new ArrayList<DBObject>();
		
		DBObject annObj = new BasicDBObject()
				.append("lemma", ann.getLemma())
				.append("posTag", ann.getPosTag())
				.append("ner", ann.getNer())
				.append("uri", ann.getUri())
				.append("begin", ann.getBegin())
				.append("end", ann.getEnd())
				.append("tf", ann.gettf())
				.append("idf", ann.getIdf());
		annotationObjList.add(annObj);
		
		return annotationObjList;
	}
	
	private List<DBObject> createListRelations(List<OpenRelation> relationList){
		List<DBObject> relationObjList = new ArrayList<DBObject>();
		for(OpenRelation rel : relationList) {
			DBObject annObj = new BasicDBObject()
					.append("subject", createListAnnotations(rel.getSubject()))
					.append("object", createListAnnotations(rel.getObject()))
					.append("sentence", rel.getOrigSentence())
					.append("relation", rel.getRel());
			relationObjList.add(annObj);
		}
		return relationObjList;
	}
	
	public List<String> getDocLemmas(String documentId){
		MongoClient client = new MongoClient();
		MongoDatabase db = client.getDatabase(database);
		MongoCollection<Document> coll = db.getCollection(collection);
		
		List<String> lemmaList = new ArrayList<String>();
		
		Document mongoDocument = coll.find(Filters.eq("_id",documentId)).first();
		
		@SuppressWarnings("unchecked")
		List<Document> NEList = mongoDocument.get("EN", ArrayList.class);
		
		for(Document doc : NEList) {
			lemmaList.add(doc.getString("lemma"));
		}
		
		return lemmaList;
	}
	
	public int numberOfDocumentsWithTerm(String lemma) {
		MongoClient client = new MongoClient();
		MongoDatabase db = client.getDatabase(database);
		MongoCollection<Document> cll = db.getCollection(collection);
		
		FindIterable<Document> containsLemmaList = cll.find(new BasicDBObject("EN.lemma",lemma));
		
		int counter = 0;
		for(Document doc : containsLemmaList) {
			counter++;
		}
		
		return counter;
	}
	
}
