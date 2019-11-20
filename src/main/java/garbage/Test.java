package garbage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bson.Document;

import db.MongoDBUtils;

public class Test {

	private static final Logger LOGGER = Logger.getLogger(Test.class);
	
	public static void main(String[] args) {
		MongoDBUtils u = new MongoDBUtils("SFWC_V2", "computerScience");
		
		List<Document> documentList = u.getAllDocs();
		int counter = 0;
		int total = 0;
		Set<String> lemmaSet = new HashSet<String>();
		for(Document doc : documentList) {
			List<Document> ENList = (List<Document>) doc.get("EN",ArrayList.class);
			for(Document en : ENList) {
				String posTag = en.getString("posTag");
				String ner = en.getString("ner");
				String url = en.getString("uri");
				String lemma = en.getString("lemma");
				if(posTag.contains("NNP") && !url.isEmpty() && !ner.equals("O") && !lemmaSet.contains(lemma)) {
					lemmaSet.add(lemma);
					System.out.println(lemma);
					counter++;
				}
			}
			total += ENList.size();
		}
		System.out.println(counter + "/" + total);
	}

}
