package db;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import nlp.Annotation;
import nlp.DocumentAnnotation;
import nlp.OpenRelation;

public class MongoDBUtils {
	
	private static final Logger LOGGER = Logger.getLogger(MongoDBUtils.class);
	
	private String database = "SFWC";
	private String collection = "computerScience";
	

	public static void main(String[] args) {
		MongoDBUtils utils = new MongoDBUtils();
		
//		utils.correlationNotXNotY("food", "blood");
		utils.addField();

	}
	
	public void deleteDocuments(List<String> idList) {
		MongoClient client = new MongoClient();
		MongoDatabase db = client.getDatabase(database);
		MongoCollection<Document> coll = db.getCollection(collection);
		
		for(String id : idList) {
			coll.deleteOne(Filters.eq("_id", id));
			System.out.println(id + " ----> DELETED");
		}
		
		client.close();
	}
	
	@SuppressWarnings("unchecked")
	public void addField() {
		MongoClient client = new MongoClient();
		MongoDatabase db = client.getDatabase(database);
		MongoCollection<Document> coll = db.getCollection(collection);
		
		coll.aggregate(Arrays.asList(Aggregates.addFields(new Field("rel.correlation", "0.0"))));
		
		client.close();
	}
	
	public int correlationXandY(String lemmaX, String lemmaY) {
		MongoClient client = new MongoClient();
		MongoDatabase db = client.getDatabase(database);
		MongoCollection<Document> coll = db.getCollection(collection);
		
		FindIterable<Document> andQuery = coll.find(Filters.and(Arrays.asList(Filters.eq("EN.lemma", lemmaX), Filters.eq("EN.lemma", lemmaY))));
		
		int counter = 0;
		for(Document doc : andQuery) {
			counter ++;
		}
//		System.out.println("counter = " + counter);
		client.close();
		
		return counter;
	}
	
	public int correlationXNotY(String lemmaX, String lemmaY) {
		MongoClient client = new MongoClient();
		MongoDatabase db = client.getDatabase(database);
		MongoCollection<Document> coll = db.getCollection(collection);
		
		FindIterable<Document> andQuery = coll.find(Filters.and(Arrays.asList(Filters.eq("EN.lemma", lemmaX), Filters.not(Filters.eq("EN.lemma", lemmaY)))));
		
		int counter = 0;
		for(Document doc : andQuery) {
			counter ++;
		}
//		System.out.println("counter = " + counter);
		client.close();
		
		return counter;
	}
	
	public int correlationNotXY(String lemmaX, String lemmaY) {
		MongoClient client = new MongoClient();
		MongoDatabase db = client.getDatabase(database);
		MongoCollection<Document> coll = db.getCollection(collection);
		
		FindIterable<Document> andQuery = coll.find(Filters.and(Arrays.asList(Filters.not(Filters.eq("EN.lemma", lemmaX)), Filters.eq("EN.lemma", lemmaY))));
		
		int counter = 0;
		for(Document doc : andQuery) {
			counter ++;
		}
//		System.out.println("counter = " + counter);
		client.close();
		return counter;
	}
	
	public int correlationNotXNotY(String lemmaX, String lemmaY) {
		MongoClient client = new MongoClient();
		MongoDatabase db = client.getDatabase(database);
		MongoCollection<Document> coll = db.getCollection(collection);
		
		FindIterable<Document> andQuery = coll.find(Filters.and(Arrays.asList(Filters.not(Filters.eq("EN.lemma", lemmaX)), Filters.not(Filters.eq("EN.lemma", lemmaY)))));
		
		int counter = 0;
		for(Document doc : andQuery) {
			counter ++;
		}
//		System.out.println("counter = " + counter);
		client.close();
		return counter;
	}
	
	public void dropCollection(String documentName) {
		MongoClient client = new MongoClient();
		MongoDatabase db = client.getDatabase(database);
		MongoCollection<Document> coll = db.getCollection(collection);
		
		Document doc = coll.find(Filters.eq("_id", documentName)).first();
		
		if(doc != null)
			coll.deleteOne(new BasicDBObject("_id", documentName));
		
		client.close();
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
		
		client.close();
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
		
		client.close();
		return lemmaList;
	}
	
	public List<String> getDocsId(){
		MongoClient client = new MongoClient();
		MongoDatabase db = client.getDatabase(database);
		MongoCollection<Document> coll = db.getCollection(collection);
		
		List<String> documentsIdList = new ArrayList<String>();
		
		FindIterable<Document> mongoDocument = coll.find();
		
		for(Document doc : mongoDocument) {
			String id = doc.getString("_id");
			documentsIdList.add(id);
		}
		
		client.close();
		return documentsIdList;
	}
	
	public List<Document> getAllDocs(){
		MongoClient client = new MongoClient();
		MongoDatabase db = client.getDatabase(database);
		MongoCollection<Document> coll = db.getCollection(collection);
		
		List<Document> documentsList = new ArrayList<Document>();
		
		FindIterable<Document> mongoDocument = coll.find();
		
		for(Document doc : mongoDocument) {
			documentsList.add(doc);
		}
		
		client.close();
		return documentsList;
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
		
		client.close();
		return counter;
	}
	
	public List<Document> documentsWithTerm(String lemma) {
		MongoClient client = new MongoClient();
		MongoDatabase db = client.getDatabase(database);
		MongoCollection<Document> cll = db.getCollection(collection);
		
		FindIterable<Document> containsLemmaList = cll.find(new BasicDBObject("EN.lemma",lemma));
		List<Document> documentList = new ArrayList<Document>();
		
		int counter = 0;
		for(Document doc : containsLemmaList) {
			documentList.add(doc);
		}
		
		client.close();
		return documentList;
	}
	
	public void updateDocumentTF(String documentId, String lemma, double tf, int position) {
		MongoClient client = new MongoClient();
		MongoDatabase db = client.getDatabase(database);
		MongoCollection<Document> coll = db.getCollection(collection);
		
		Bson filter = Filters.eq("_id",documentId);
		Bson setUpdate = Updates.set("EN."+position+".tf", String.valueOf(tf));
		
		coll.updateOne(filter, setUpdate);
		client.close();
	}
	
	public void updateDocumentIDF(String documentId, String lemma, String idf, int position) {
		MongoClient client = new MongoClient();
		MongoDatabase db = client.getDatabase(database);
		MongoCollection<Document> coll = db.getCollection(collection);
		
		Bson filter = Filters.eq("_id",documentId);
		Bson setUpdate = Updates.set("EN."+position+".idf", String.valueOf(idf));

		coll.updateOne(filter, setUpdate);
		client.close();
	}
	
	public Set<String> generateLemmaIndex() {
		MongoClient client = new MongoClient();
		MongoDatabase db = client.getDatabase(database);
		MongoCollection<Document> coll = db.getCollection(collection);
		
		FindIterable<Document> documents = coll.find();
		Set<String> lemmaSet = new HashSet<String>();
		
		for(Document doc : documents) {
			List<Document> lemmas = doc.get("EN",ArrayList.class);
			for(Document lemma : lemmas) {
				lemmaSet.add(lemma.getString("lemma"));
			}
		}
		client.close();
		return lemmaSet;
		
	}
	
}
