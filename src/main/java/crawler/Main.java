package crawler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import annotation.DBPediaSpotlight;
import db.MongoDBUtils;
import nlp.DocumentAnnotation;
import nlp.TextAnalisys;

public class Main {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException {
		Main m = new Main();
		DBPediaSpotlight ds = new DBPediaSpotlight();
		String text = "Their vivid anecdotal qualities have made some of them favorites of painters since the Renaissance, the result being that they stand out more prominently in the modern imagination.Daphne was a nymph, daughter of the river god Peneus, who had scorned Apollo.";
		TextAnalisys ta = new TextAnalisys();
		File[] documents = new File("./corpus/computerScience").listFiles();
		File processedDocuments = new File("ProcessedDocuments.txt");
		
		int counter = 0;
		for(File document : documents){
			System.out.println("Processing document: " + document.getName());
			
			if(processedDocuments.exists() 
					&& m.isProcessed(document.getName(), FileUtils.readLines(processedDocuments))){
				System.out.println("File " + document.getName() + " already processed");
				continue;
			}
			
			FileUtils.write(processedDocuments, document.getName()+"\n", "UTF8", true);
			MongoDBUtils mongoUtils = new MongoDBUtils();
			try {
				String content = FileUtils.readFileToString(document);
				if(!content.isEmpty()){
					System.out.println("text annotation....");
//					mongoUtils.dropCollection(document.getName());
					DocumentAnnotation docAnn = ta.stanfordDocumentAnalizer(content);
					mongoUtils.sotoreDocument(docAnn, document.getName(), content);
				}else
					System.out.println("Document: " + document.getName() + " is empty");
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(counter == 500)
				break;
			counter ++;
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
	
	public boolean isProcessed(String documentName, List<String> processedList){
		if(processedList != null)
			for(String processed : processedList) {
				if(documentName.equalsIgnoreCase(processed))
					return true;
			}
		return false;
	}
	
	public void calculateTF(String documentId) {
		MongoDBUtils dbUtils = new MongoDBUtils();
		
		List<String> lemmaList = dbUtils.getDocLemmas(documentId);
		
		Map<String, Integer> tfMap = new HashMap<String, Integer>();
		Map<String, Double> tfResultMap = new HashMap<String,Double>();
		
		for(String lemma : lemmaList) {
			if(tfMap.containsKey(lemma)) {
				Integer count = tfMap.get(lemma);
				count += 1;
				tfMap.put(lemma, count);
			}else {
				tfMap.put(lemma, 1);
			}
		}
		
		int totalLemmas = lemmaList.size();
		
		for(Map.Entry<String, Integer> entry :tfMap.entrySet()) {
			double tf = entry.getValue() / totalLemmas;
			tfResultMap.put(entry.getKey(), tf);			
		}
	}

}
