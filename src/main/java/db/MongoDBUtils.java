package db;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import nlp.Annotation;
import nlp.DocumentAnnotation;
import nlp.OpenRelation;

public class MongoDBUtils {
	
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(MongoDBUtils.class);
	
	private MongoClient client;
	private MongoDatabase db;
	private MongoCollection<Document> coll;

	public MongoDBUtils(String database, String collection) {
		this.client = new MongoClient();
		this.db = client.getDatabase(database);
		this.coll = db.getCollection(collection);
		
	}
	
	public MongoDBUtils(String database) {
		this.client = new MongoClient();
		this.db = client.getDatabase(database);
	}
	
	public void close() {
		client.close();
	}

	public static void main(String[] args) throws IOException {
		MongoDBUtils utils = new MongoDBUtils("SFWC", "computerScience");
		
		//utils.correlationNotXNotY("food", "blood");
		Set<String> indexSet = utils.generateLemmaIndex();
		Object[] index = indexSet.toArray();
		Pattern wordPattern = Pattern.compile("^[A-Za-z]{3,}-?[A-Za-z]*$");
		List<String> newNouns = new ArrayList<String>();
		System.out.println(index.length);
		for(int i = 0, j = 0 ; i < index.length; i++, j++ ) {
			String lemma = index[i].toString();
			Matcher match = wordPattern.matcher(lemma);
			if(match.find() && !lemma.contains(".") && !lemma.contains(" ") && !lemma.contains("/")) {
				System.out.println(lemma);
				newNouns.add(lemma);
			}
//			if(j == 100) {
//				System.in.read();
//				j = 0;
//			}
			
		}
		System.out.println(newNouns.size());

	}
	
	//use one time to delete computerScience documents wrongly added
	public void deleteDocuments(List<String> idList) {
		
		for(String id : idList) {
			coll.deleteOne(Filters.eq("_id", id));
			System.out.println(id + " ----> DELETED");
		}
		
	}
		
	public int correlationXandY(String lemmaX, String lemmaY) {
		
		FindIterable<Document> andQuery = coll.find(Filters.and(Arrays.asList(Filters.eq("EN.lemma", lemmaX), Filters.eq("EN.lemma", lemmaY))));
		
		int counter = 0;
		for(@SuppressWarnings("unused") Document doc : andQuery) {
			counter ++;
		}
//		System.out.println("counter = " + counter);
		
		return counter;
	}
	
	public int correlationXNotY(String lemmaX, String lemmaY) {
		
		FindIterable<Document> andQuery = coll.find(Filters.and(Arrays.asList(Filters.eq("EN.lemma", lemmaX), Filters.not(Filters.eq("EN.lemma", lemmaY)))));
		
		int counter = 0;
		for(@SuppressWarnings("unused") Document doc : andQuery) {
			counter ++;
		}
//		System.out.println("counter = " + counter);
		
		return counter;
	}
	
	public int correlationNotXY(String lemmaX, String lemmaY) {
		
		FindIterable<Document> andQuery = coll.find(Filters.and(Arrays.asList(Filters.not(Filters.eq("EN.lemma", lemmaX)), Filters.eq("EN.lemma", lemmaY))));
		
		int counter = 0;
		for(@SuppressWarnings("unused") Document doc : andQuery) {
			counter ++;
		}
//		System.out.println("counter = " + counter);
		return counter;
	}
	
	public int correlationNotXNotY(String lemmaX, String lemmaY) {
		FindIterable<Document> andQuery = coll.find(Filters.and(Arrays.asList(Filters.not(Filters.eq("EN.lemma", lemmaX)), Filters.not(Filters.eq("EN.lemma", lemmaY)))));
		
		int counter = 0;
		for(@SuppressWarnings("unused") Document doc : andQuery) {
			counter ++;
		}
//		System.out.println("counter = " + counter);
		return counter;
	}
	
	public void dropCollection(String documentName) {
		Document doc = coll.find(Filters.eq("_id", documentName)).first();
		
		if(doc != null)
			coll.deleteOne(new BasicDBObject("_id", documentName));
		
	}
	
	public void storeDocument(DocumentAnnotation docAnn, String documentName, String documentContent) {
		Document documentObj = new Document()
				.append("_id", documentName)
				.append("content", documentContent)
				.append("EN", createListAnnotations(docAnn.getAnnotationList()))
				.append("rel", createListRelations(docAnn.getOpenRelationList()));

		System.out.println("Inserting document");
		coll.insertOne(documentObj);
		
	}
	
	public void insertDocument(Document dbObject) {
		Document doc = coll.find(Filters.eq("_id", dbObject.getString("_id"))).first();
		if(doc == null)
			coll.insertOne(dbObject);
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
		List<String> lemmaList = new ArrayList<String>();
		
		Document mongoDocument = coll.find(Filters.eq("_id",documentId)).first();
		
		@SuppressWarnings("unchecked")
		List<Document> NEList = mongoDocument.get("EN", ArrayList.class);
		
		for(Document doc : NEList) {
			lemmaList.add(doc.getString("lemma"));
		}
		
		return lemmaList;
	}
	
	public List<String> getDocsId(){
		List<String> documentsIdList = new ArrayList<String>();
		
		FindIterable<Document> mongoDocument = coll.find();
		
		for(Document doc : mongoDocument) {
			String id = doc.getString("_id");
			documentsIdList.add(id);
		}
		
		return documentsIdList;
	}
	
	public List<Document> getAllDocs(){
		List<Document> documentsList = new ArrayList<Document>();
		
		FindIterable<Document> mongoDocument = coll.find();
		
		for(Document doc : mongoDocument) {
			documentsList.add(doc);
		}
		
		return documentsList;
	}
	
	public int numberOfDocumentsWithTerm(String lemma) {
		
		FindIterable<Document> containsLemmaList = coll.find(new BasicDBObject("EN.lemma",lemma));
		
		int counter = 0;
		for(@SuppressWarnings("unused") Document doc : containsLemmaList) {
			counter++;
		}
		
		return counter;
	}
	
	public List<Document> documentsWithTerm(String lemma) {
		FindIterable<Document> containsLemmaList = coll.find(new BasicDBObject("EN.lemma",lemma));
		List<Document> documentList = new ArrayList<Document>();
		
		for(Document doc : containsLemmaList) {
			documentList.add(doc);
		}
		
		return documentList;
	}
	
	public void updateDocumentTF(String documentId, double tf, int position) {
		Bson filter = Filters.eq("_id",documentId);
		Bson setUpdate = Updates.set("EN."+position+".tf", String.valueOf(tf));
		
		coll.updateOne(filter, setUpdate);
	}
	
	public void updateDocumentIDF(String documentId, String idf, int position) {
		Bson filter = Filters.eq("_id",documentId);
		Bson setUpdate = Updates.set("EN."+position+".idf", String.valueOf(idf));

		coll.updateOne(filter, setUpdate);
	}
	
	public void updateDocumentTFIDF(String documentId, String idf, int position) {
		Bson filter = Filters.eq("_id",documentId);
		Bson setUpdate = Updates.set("EN."+position+".tfIdf", String.valueOf(idf));

		coll.updateOne(filter, setUpdate);
	}
	
	public void updateDocumentCorrelation(String documentId, String correlation, int position) {
		Bson filter = Filters.eq("_id",documentId);
		Bson setUpdate = Updates.set("rel."+position+".correlation", String.valueOf(correlation));

		coll.updateOne(filter, setUpdate);
	}
	
	public Set<String> generateLemmaIndex() {
		FindIterable<Document> documents = coll.find();
		Set<String> lemmaSet = new HashSet<String>();
		
		for(Document doc : documents) {
			@SuppressWarnings("unchecked")
			List<Document> lemmas = (List<Document>) doc.get("EN",ArrayList.class);
			for(Document lemma : lemmas) {
//				if(lemma.getString("posTag").contains("NNP"))
					lemmaSet.add(lemma.getString("lemma"));
			}
		}
		return lemmaSet;
		
	}
	
	public MongoIterable<String> getCollectionsName(){
		return db.listCollectionNames();
	}
	
}
