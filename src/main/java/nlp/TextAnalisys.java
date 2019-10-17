package nlp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import annotation.DBPediaSpotlight;
import annotation.Entity;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

public class TextAnalisys {

	public static void cleanStanfordDocument(String documentContent) {
		DBPediaSpotlight nel = new DBPediaSpotlight();
		Document doc2 = new Document(documentContent);
		TextAnalisys ta = new TextAnalisys();
		
		List<Sentence> listSent = doc2.sentences();
		System.out.println("List of sentences: " + listSent.size());
		//Mongo code
		List<DBObject> sentenceList = new ArrayList<DBObject>();
		
		//To calculate tf-idf
		List<String> docTerms = new ArrayList<String>(); //To TF
		List<List<String>> corpusTerms = new ArrayList<List<String>>(); //to IDF
		
		for (int i = 0; i < listSent.size(); i++) { // Will iterate over two sentences
			List<String> listWords = listSent.get(i).words();
			String sentence = listSent.get(i).rawSentence().getText();
			String annotation = nel.sendPost(sentence);
			
			List<Entity> entityList = nel.readOutput(annotation);
			
			System.out.println("Sentence: " + sentence);
			DBObject sentenceObj = new BasicDBObject("sentence", sentence);
			List<Annotation> annotationList = new ArrayList<Annotation>();
			for (int j = 0; j < listWords.size(); j++) {
				String posTag = listSent.get(i).posTag(j);
				if(!posTag.contains("NN"))
					continue;
				Annotation ann = new Annotation();
				ann.setLemma(listSent.get(i).lemmas().get(j));
				ann.setNer(listSent.get(i).nerTag(j));
				ann.setBegin(listSent.get(i).characterOffsetBegin(j));
				ann.setEnd(listSent.get(i).characterOffsetEnd(j));
				docTerms.add(ann.getLemma());
				// String sentence = listSent.get(i).rawSentence().getText();
			}
			
			for (Entity entity : entityList) {
				String anchor = entity.getSurfaceText();
				ta.hasEntity(annotationList, anchor, entity.getURI());
			}
			
			Collection<RelationTriple> textTriples = listSent.get(i).openieTriples();
			for(RelationTriple triple : textTriples) {
				triple.subjectLemmaGloss();
				triple.relationLemmaGloss();
				triple.objectLemmaGloss();
			}
		}
		// return listCleanDocumentWords;
	}
	
	public void hasEntity(List<Annotation> annotationList, String anchor, String uri) {
		for(Annotation ann : annotationList) {
			if(anchor.equalsIgnoreCase(ann.getLemma())) {
				ann.setUri(uri);
			}
		}
	}
}
