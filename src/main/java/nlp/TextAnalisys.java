package nlp;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import annotation.DBPediaSpotlight;
import annotation.Entity;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

public class TextAnalisys {

	public static void cleanStanfordDocument(String documentContent) {
		DBPediaSpotlight nel = new DBPediaSpotlight();
		Document doc2 = new Document(documentContent);
		List<Sentence> listSent = doc2.sentences();
		System.out.println("List of sentences: " + listSent.size());
		//Mongo code
		List<DBObject> sentenceList = new ArrayList<DBObject>();
		for (int i = 0; i < listSent.size(); i++) { // Will iterate over two sentences
			List<String> listWords = listSent.get(i).words();
			String sentence = listSent.get(i).rawSentence().getText();
			String annotation = nel.sendPost(sentence);
			List<Entity> entityList = nel.readOutput(annotation);
			System.out.println("Sentence: " + sentence);
			DBObject sentenceObj = new BasicDBObject("sentence", sentence);
			List<String> lemmaNNList = new ArrayList<String>();
			List<String> lemmaPosTagList = new ArrayList<String>();
			for (int j = 0; j < listWords.size(); j++) {
				String lemma = listSent.get(i).lemmas().get(j);
				String posTag = listSent.get(i).posTag(j);
				String ner = listSent.get(i).nerTag(j);

				for (Entity entity : entityList) {
					String anchor = entity.getSurfaceText();
					if (lemma.equalsIgnoreCase(anchor))
						System.out.println("URI: " + entity.getURI());
				}

				int begin = listSent.get(i).characterOffsetBegin(j);
				int end = listSent.get(i).characterOffsetEnd(j);
				// String sentence = listSent.get(i).rawSentence().getText();
			}
			
			//DBObject sentences = new DBObject("sentences", sentencesList);
			
			// for(int j = 0; j < listWords.size(); j++) {
			//// System.out.println("word: " + listWords.get(j));
			//// // When we ask for the lemma, it will load and run the part of speech
			// tagger
			//// System.out.println("lemma " + listSent.get(i).lemmas().get(j));
			//// // When we ask for the parse, it will load and run the parser
			//// //System.out.println("The parse of the sentence '" + sent + "' is " +
			// sent.parse());
			////
			//// System.out.println("Part of speech: " + listSent.get(i).posTags().get(j));
			// String lemma = listSent.get(i).lemmas().get(j);
			// String posTag = listSent.get(i).posTags().get(j);
			// int begin = listSent.get(i).characterOffsetBegin(j);
			// int end = listSent.get(i).characterOffsetEnd(j);
			// //String sentence = listSent.get(i).rawSentence().getText();
			//
			// if(lemma.length() <= 1)
			// continue;
			// if(posTagConfig.equals("NN")) {
			// if(posTag.contains("NN")) {
			// listCleanDocumentWords.add(lemma+":"+posTag);
			// //System.out.println(listSent.get(i).lemmas().get(j) + " start: " + begin + "
			// end: " + end + " sentence: " + sentence);
			// }
			// }else if(posTagConfig.equals("NNJJ")) {
			// if(posTag.contains("NN") ||
			// posTag.contains("JJ")) {
			// listCleanDocumentWords.add(lemma+":"+posTag);
			// //System.out.println(listSent.get(i).lemmas().get(j));
			// }
			// }else if(posTagConfig.equals("NNJJVB")) {
			// if(posTag.contains("NN") ||
			// posTag.contains("JJ") ||
			// posTag.contains("VB")) {
			// listCleanDocumentWords.add(lemma+":"+posTag);
			// //System.out.println(listSent.get(i).lemmas().get(j));
			// }
			//
			// }
			// }
		}

		// return listCleanDocumentWords;
	}
}
