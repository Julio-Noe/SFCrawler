package nlp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import annotation.DBPediaSpotlight;
import annotation.Entity;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

public class TextAnalisys {
	
	List<String> stopWordList = new ArrayList<String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
		add("i");
		add("me");
		add("my");
		add("myself");
		add("we");
		add("our");
		add("ours");
		add("ourselves");
		add("you");
		add("your");
		add("yours");
		add("yourself");
		add("yourselves");
		add("he");
		add("him");
		add("his");
		add("himself");
		add("she");
		add("her");
		add("hers");
		add("herself");
		add("it");
		add("its");
		add("itself");
		add("they");
		add("them");
		add("their");
		add("theirs");
		add("themselves");
		add("what");
		add("which");
		add("who");
		add("whom");
		add("this");
		add("that");
		add("these");
		add("those");
		add("am");
		add("is");
		add("are");
		add("was");
		add("were");
		add("be");
		add("been");
		add("being");
		add("have");
		add("has");
		add("had");
		add("having");
		add("do");
		add("does");
		add("did");
		add("doing");
		add("a");
		add("an");
		add("the");
		add("and");
		add("but");
		add("if");
		add("or");
		add("because");
		add("as");
		add("until");
		add("while");
		add("of");
		add("at");
		add("by");
		add("for");
		add("with");
		add("about");
		add("against");
		add("between");
		add("into");
		add("through");
		add("during");
		add("before");
		add("after");
		add("above");
		add("below");
		add("to");
		add("from");
		add("up");
		add("down");
		add("in");
		add("out");
		add("on");
		add("off");
		add("over");
		add("under");
		add("again");
		add("further");
		add("then");
		add("once");
		add("here");
		add("there");
		add("when");
		add("where");
		add("why");
		add("how");
		add("all");
		add("any");
		add("both");
		add("each");
		add("few");
		add("more");
		add("most");
		add("other");
		add("some");
		add("such");
		add("no");
		add("nor");
		add("not");
		add("only");
		add("own");
		add("same");
		add("so");
		add("than");
		add("too");
		add("very");
		add("s");
		add("t");
		add("can");
		add("will");
		add("just");
		add("don");
		add("should");
		add("now");
		add("-lrb-");
	}};

	public DocumentAnnotation stanfordDocumentAnalizer(String documentContent) {
		DBPediaSpotlight nel = new DBPediaSpotlight();
		Document doc2 = new Document(documentContent);
		TextAnalisys ta = new TextAnalisys();
		DocumentAnnotation docAnn = new DocumentAnnotation();
		
		List<Sentence> listSent = doc2.sentences();
		System.out.println("List of sentences: " + listSent.size());
		//Mongo code
		List<Annotation> annotationList = new ArrayList<Annotation>();
		List<OpenRelation> openRelationList = new ArrayList<OpenRelation>();
		
		for (int i = 0; i < listSent.size(); i++) { // Will iterate over sentences
//			System.out.println("Processing sentence: " + i +" of " + listSent.size());
			List<String> listWords = listSent.get(i).words();
			String sentence = listSent.get(i).rawSentence().getText();
			String annotation = nel.sendPost(sentence);
//			System.out.println("Getting NEL from DBpedia");
			List<Entity> entityList = nel.readOutput(annotation);
			List<Annotation> sentenceAnnotations = new ArrayList<Annotation>();
			
//			System.out.println("Sentence: " + sentence);
			Pattern wordPattern = Pattern.compile("^[A-Za-z]{3,}-?[A-Za-z]*$");
			for (int j = 0; j < listWords.size(); j++) {
				String posTag = listSent.get(i).posTag(j);
				String lemma = listSent.get(i).lemmas().get(j).toLowerCase().trim();
				Matcher match = wordPattern.matcher(lemma);
//				if(!posTag.contains("NN") && !stopWordList.contains(lemma) && match.find())
				if(!posTag.contains("NN") || !match.find() || stopWordList.contains(lemma) || lemma.contains(".") || lemma.contains(" ") || lemma.contains("/"))
					continue;
				if(lemma.length() < 3)
					continue;
				
				Annotation ann = new Annotation();
				ann.setLemma(lemma);
				ann.setNer(listSent.get(i).nerTag(j));
				ann.setBegin(listSent.get(i).characterOffsetBegin(j));
				ann.setEnd(listSent.get(i).characterOffsetEnd(j));
				ann.setPosTag(posTag);
				// String sentence = listSent.get(i).rawSentence().getText();
				sentenceAnnotations.add(ann);
			}
			
//			System.out.println("Number of NEL: " + entityList.size());
			for (Entity entity : entityList) {
				String anchor = entity.getSurfaceText().toLowerCase();
				ta.hasEntity(sentenceAnnotations, anchor, entity.getURI());
			}
			
			Collection<RelationTriple> textTriples = listSent.get(i).openieTriples();
			for(RelationTriple triple : textTriples) {
				OpenRelation rel = ta.hasRelation(sentenceAnnotations, triple.subjectLemmaGloss(), triple.objectLemmaGloss());
				if(rel != null) {
					rel.setRel(triple.relationLemmaGloss());
					rel.setOrigSentence(sentence);
					//openRelationList.add(rel);
				}
				if(rel != null) {
					if(!ta.repeatedAnnotation(openRelationList, rel))
						openRelationList.add(rel);
					else
						System.out.println("same relation");
				}
			}
			annotationList.addAll(sentenceAnnotations);
		}
		docAnn.setAnnotationList(annotationList);
		docAnn.setOpenRelationList(openRelationList);
		System.out.println("Returning document annotation");
		return docAnn;
	}
	
	public List<Annotation> stanfordTestDocumentAnalizer(String documentContent) {
		DBPediaSpotlight nel = new DBPediaSpotlight();
		Document doc2 = new Document(documentContent);
		TextAnalisys ta = new TextAnalisys();
		
		List<Sentence> listSent = doc2.sentences();
		System.out.println("List of sentences: " + listSent.size());
		
		if(listSent.size() > 100)
			return null;
		List<Annotation> annotationList = new ArrayList<Annotation>();
		
		for (int i = 0; i < listSent.size(); i++) { // Will iterate over sentences
//			System.out.println("Processing sentence: " + i +" of " + listSent.size());
			List<String> listWords = listSent.get(i).words();
			String sentence = listSent.get(i).rawSentence().getText();
			String annotation = nel.sendPost(sentence);
			if(annotation.startsWith("<")) {
				System.out.println("text is HTML");
				continue;
			}
//			System.out.println("Getting NEL from DBpedia");
			List<Entity> entityList = nel.readOutput(annotation);
			List<Annotation> sentenceAnnotations = new ArrayList<Annotation>();
			
//			System.out.println("Sentence: " + sentence);
			Pattern wordPattern = Pattern.compile("^[A-Za-z]{3,}-?[A-Za-z]*$");
			
			for (int j = 0; j < listWords.size(); j++) {
				String posTag = listSent.get(i).posTag(j);
				String lemma = listSent.get(i).lemmas().get(j).toLowerCase();
				Matcher match = wordPattern.matcher(lemma);
				if(!posTag.contains("NN") || !match.find() || stopWordList.contains(lemma) || lemma.contains(".") || lemma.contains(" ") || lemma.contains("/"))
					continue;
				if(lemma.length() < 3)
					continue;
				Annotation ann = new Annotation();
				ann.setLemma(lemma);
				ann.setNer(listSent.get(i).nerTag(j));
				ann.setBegin(listSent.get(i).characterOffsetBegin(j));
				ann.setEnd(listSent.get(i).characterOffsetEnd(j));
				ann.setPosTag(posTag);
				// String sentence = listSent.get(i).rawSentence().getText();
				sentenceAnnotations.add(ann);
			}
			
//			System.out.println("Number of NEL: " + entityList.size());
			for (Entity entity : entityList) {
				String anchor = entity.getSurfaceText().toLowerCase();
				ta.hasEntity(sentenceAnnotations, anchor, entity.getURI());
			}
			
			annotationList.addAll(sentenceAnnotations);
		}
		System.out.println("Returning annotations");
		return annotationList;
	}
	
	public boolean repeatedAnnotation(List<OpenRelation> relList, OpenRelation relTarget) {
		for(OpenRelation rel : relList) {
			if(rel.equals(relTarget))
				return true;
		}
		return false;
	}
	
	public void hasEntity(List<Annotation> annotationList, String anchor, String uri) {
		for(Annotation ann : annotationList) {
			if(anchor.equalsIgnoreCase(ann.getLemma())) {
				ann.setUri(uri);
			}
		}
	}
	public OpenRelation hasRelation(List<Annotation> annotationList, String subject, String object) {
		OpenRelation rel = null;
		
		int sbjIndex = -1;
		int objIndex = -1;
		
		sbjIndex = lookForAnnotation(annotationList, subject);
		
		if(sbjIndex > 0) 
			objIndex = lookForAnnotation(annotationList, object);
				
		if(sbjIndex != objIndex
				&& sbjIndex >= 0
					&& objIndex >= 0) {
			rel = new OpenRelation();
			rel.setSubject(annotationList.get(sbjIndex));
			rel.setObject(annotationList.get(objIndex));
		}
		return rel;
	}
	
	public int lookForAnnotation(List<Annotation> annotationList, String target) {
		int index = -1;
		
		for(int i = 0; i < annotationList.size(); i++) {
			if(target.contains(annotationList.get(i).getLemma())) {
				index = i;
				break;
			}
		}
		return index;
	}
}
