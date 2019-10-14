package db;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class MongoDB {
	
	final String uriString = "mongodb://noeAdmin:abc123@localhost:27017/admin?authSource=admin";
    MongoClientURI uri = null;
    MongoClient mongoClient = null;
    MongoDatabase mongoDB = null;
    
    MongoCollection<Document> collection = null;
	
	public MongoDB(String DataBase, String cll) {
		uri = new MongoClientURI(uriString);
		mongoClient = new MongoClient(uri);
		mongoDB = mongoClient.getDatabase(DataBase);
		collection = mongoDB.getCollection(cll);
	}

	public static void main(String[] args) {
//		MongoDB mongo = new MongoDB("BBC", "business");
		MongoDB mongo = new MongoDB("FWC", "computerScience");
		
		
		BasicDBObject extSbj = new BasicDBObject().append("@id", "https://siemens.com/knowledge_graph/turbine/nlp#module_08-090");
		BasicDBObject extPred = new BasicDBObject().append("@id", "https://siemens.com/knowledge_graph/turbine/nlp/ns#serialNumber");
		List<BasicDBObject> extObjects = new ArrayList<BasicDBObject>();
		
		BasicDBObject obj = new BasicDBObject();
		
		obj.append("iHash", "abc111");
		obj.append("ext:object", new BasicDBObject().append("@value", "090").append("@type", "xsd:string"));
		List<BasicDBObject> extSource = new ArrayList<BasicDBObject>();
		extSource.add(new BasicDBObject().append("ext:documentName", "21039698-ESN107 - Logbooks.pdf")
				.append("ext:jsonDocument", "raw.siemens.git.com.090_core.json")
				.append("ext:oid", "esn_107_KV_222")
				.append("ext:computedFrom", "parent:serial_nr")
				.append("ext:computedBt", "script-X")
				.append("ext:validationResult", "valid")
				.append("ext:validationAgent", "annotator-1")
				.append("ext:validationTime", "2018-04-16")
				.append("ext:annotationDocument", "raw.siemens.git.com.090_core-mods.tsv"));
		obj.append("ext:source", extSource);
		BasicDBObject context = new BasicDBObject().append("ext", "http://siemens.com/ns/")
				.append("xsd", "http://www.w3.org/2001/XML/Schema#")
				.append("ext:subject", "http://siemens.com/ns/subject")
				.append("ext:predicate", "http://siemens.com/ns/predicate")
				.append("ext:objects", "http://siemens.com/ns/objects")
				.append("ext:object", "http://siemens.com/ns/object")
				.append("ext:provenance", "http://siemens.com/ns/provenance")
				.append("ext:Turbine", "http://siemens.com/ns/Turbine")
				.append("ext:documentName", new BasicDBObject().append("@type", "xsd:String"))
				.append("ext:documentLocation", new BasicDBObject().append("@type", "xsd:string"));
		
		extObjects.add(obj);
		Document canvas = new Document("_id","fc4ebb0fed3c3171578c299b3ce21f411202ff2afc93568a54b4db7a75")
				.append("ext:subject", extSbj)
				.append("ext:predicate", extPred)
				.append("ext:objects", extObjects)
				.append("@context", context);
		
		mongo.collection.insertOne(canvas);
	    
//	    mongo.insertData(collection);
//	    mongo.retrieveAllData();
//	    mongo.retrieveData("_id", "100.txt");
	    mongo.mongoClient.close();

	}
	
	public String md5(String name) throws NoSuchAlgorithmException {
		String md5 = "";
		String base = new String(name.getBytes(),StandardCharsets.UTF_8);
		byte[] fileName = base.getBytes();
		MessageDigest md = MessageDigest.getInstance("MD5");
		md5 = DatatypeConverter.printHexBinary(md.digest(fileName)).toLowerCase();
		return md5;
	}
	
	public void insertData(String _id, int numSnts, List<BasicDBObject> sentences, String fileContent, int totalTokens) {
		Document canvas = new Document("_id",_id)
				.append("numSnts", numSnts)
				.append("fileContent", fileContent)
				.append("totalTokens", totalTokens)
	            .append("sentences", sentences);
	    collection.insertOne(canvas);
	   
	}
	
	public void insertTopics(List<BasicDBObject> docTopicList) {
		for(BasicDBObject dbObj : docTopicList) {
			Document topic = new Document(dbObj);
			collection.insertOne(topic);
		}
		
	}
	
	public void insertCorpus(Document jsonDoc) {
	    collection.insertOne(jsonDoc);
	}
	
	public void insertNEL(Document jsonDoc) {
	    collection.insertOne(jsonDoc);
	}
	
	public void insertFrame(Document jsonDoc) {
		collection.insertOne(jsonDoc);
	}
	
	public void insertRelations(Document jsonDoc) {
	    collection.insertOne(jsonDoc);
	}
	
	public void insertRDF(Document jsonDoc) {
		collection.insertOne(jsonDoc);
	}
	
	public void insertEval(Document jsonDoc) {
		collection.insertOne(jsonDoc);
	}
	
	
	//Retrieve all documents as Json list
	public List<String> retrieveAllData( ) {
		FindIterable<Document> findIterable = collection.find(new Document());
		List<String> json = new ArrayList<String>();
//		Block<Document> printBlock = new Block<Document>() {
//		    @Override
//		    public void apply(final Document document) {
//		        System.out.println(document.toJson());
////		    	json.add(document.toJson());
//		    }
//		};
//		
//		findIterable.forEach(printBlock);
		for(Document doc : findIterable) {
			json.add(doc.toJson());
		}
		return json;
	}
	
	public String retrieveData(String tag, String value ) {
		Document myDoc = collection.find(Filters.eq(tag,value)).first();
		if(myDoc == null)
			return "";
		else
			return myDoc.toJson();
	   
	}
	
	public String retrieveRdfData(String tag, String value ) {
		Document myDoc = collection.find(Filters.eq(tag,value)).first();
		myDoc.remove("_id");
		if(myDoc == null)
			return "";
		else
			return myDoc.toJson();
	   
	}
	public void closeConnection() {
		mongoClient.close();
	}

}
