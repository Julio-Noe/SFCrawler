package db;


import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import nlp.Annotation;
import nlp.DocumentAnnotation;
import nlp.OpenRelation;

public class MongoDBUtils {

	public static void main(String[] args) {
		

	}
	
	public void sotoreDocument(DocumentAnnotation docAnn, String documentName, String documentContent) {
		MongoClient client = new MongoClient();
		MongoDatabase db = client.getDatabase("SFWV");
		MongoCollection<Document> coll = db.getCollection("computerScience");
		
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
	
	private List<DBObject> createListRelations(List<OpenRelation> relationList){
		List<DBObject> relationObjList = new ArrayList<DBObject>();
		for(OpenRelation rel : relationList) {
			DBObject annObj = new BasicDBObject()
					.append("subject", rel.getSubject())
					.append("object", rel.getObject())
					.append("sentence", rel.getOrigSentence())
					.append("relation", rel.getRel());
			relationObjList.add(annObj);
		}
		return relationObjList;
	}
}
