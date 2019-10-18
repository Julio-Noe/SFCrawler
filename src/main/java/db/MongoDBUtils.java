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

public class MongoDBUtils {

	public static void main(String[] args) {
		

	}
	
	public void sotoreDocument(DocumentAnnotation docAnn, String documentName, String documentContent) {
		MongoClient client = new MongoClient();
		MongoDatabase db = client.getDatabase("SFWV");
		MongoCollection<Document> coll = db.getCollection("computerScience");
		
		DBObject documentObj = new BasicDBObject()
				.append("_id", documentName)
				.append("content", documentContent)
				.append("EN", createListAnnotations(docAnn.getAnnotationList()));
		//TODO create rel list objects
		
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

}
