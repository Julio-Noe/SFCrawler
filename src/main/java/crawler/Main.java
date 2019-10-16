package crawler;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import annotation.DBPediaSpotlight;
import nlp.TextAnalisys;

public class Main {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		Main m = new Main();
		DBPediaSpotlight ds = new DBPediaSpotlight();
		String text = "Their vivid anecdotal qualities have made some of them favorites of painters since the Renaissance, the result being that they stand out more prominently in the modern imagination.Daphne was a nymph, daughter of the river god Peneus, who had scorned Apollo.";
		TextAnalisys ta = new TextAnalisys();
		File[] documents = new File("./corpus/computerScience").listFiles();
		for(File document : documents){
			try {
				String content = FileUtils.readFileToString(document);
				if(!content.isEmpty()){
					DBObject documentId = new BasicDBObject("_id", document.getName());
					DBObject contentObj = new BasicDBObject("content",content);
				}else
					System.out.println("Document: " + document.getName() + " is empty");
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
			//ta.cleanStanfordDocument(text);
		}
		
//		String received = ds.sendPost(text);
//		System.out.println(received);
//		List<Entity> entitiesList = ds.readOutput(received);
//		
//		for(Entity ent : entitiesList) {
//			System.out.println(ent.getSurfaceText() + " - " + ent.getURI());
//		}
		

	}

}
