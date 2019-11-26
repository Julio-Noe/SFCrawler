package evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bson.Document;

import com.mongodb.client.MongoIterable;

import db.MongoDBUtils;

public class EvaluationSheet {

	public static void main(String[] args) throws IOException {
		EvaluationSheet es = new EvaluationSheet();
		es.createSheet("Experiments");

	}
	
	public void createSheet(String db) throws IOException {
		String mongoDB = db;
		MongoDBUtils utils = new MongoDBUtils(mongoDB);
		MongoIterable<String> collectionList = utils.getCollectionsName();
		List<String> rows = new ArrayList<String>();
		for(String name : collectionList) {
			MongoDBUtils u = new MongoDBUtils(mongoDB, name);
			List<Document> documentList = u.getAllDocs();
			for(Document doc : documentList) {
				if(doc.getBoolean("error"))
					continue;
				String row = "";
				row += doc.getString("_id") + "\t";
				row += doc.getString("decision");
				rows.add(row);
			}
		}
		FileUtils.writeLines(new File("sheet.tsv"), rows);
		
	}

}
