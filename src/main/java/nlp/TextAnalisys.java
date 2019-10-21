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

	public DocumentAnnotation stanfordDocumentAnalizer(String documentContent) {
		DBPediaSpotlight nel = new DBPediaSpotlight();
		Document doc2 = new Document(documentContent);
		TextAnalisys ta = new TextAnalisys();
		DocumentAnnotation docAnn = new DocumentAnnotation();
		
		List<Sentence> listSent = doc2.sentences();
		System.out.println("List of sentences: " + listSent.size());
		//Mongo code
		List<DBObject> sentenceList = new ArrayList<DBObject>();
		List<Annotation> annotationList = new ArrayList<Annotation>();
		List<OpenRelation> openRelationList = new ArrayList<OpenRelation>();
		
		for (int i = 0; i < listSent.size(); i++) { // Will iterate over sentences
			System.out.println("Processing sentence: " + i +" of " + listSent.size());
			List<String> listWords = listSent.get(i).words();
			String sentence = listSent.get(i).rawSentence().getText();
			String annotation = nel.sendPost(sentence);
			System.out.println("Getting NEL from DBpedia");
			List<Entity> entityList = nel.readOutput(annotation);
			
			System.out.println("Sentence: " + sentence);
			DBObject sentenceObj = new BasicDBObject("sentence", sentence);
			
			for (int j = 0; j < listWords.size(); j++) {
				String posTag = listSent.get(i).posTag(j);
				if(!posTag.contains("NN"))
					continue;
				Annotation ann = new Annotation();
				ann.setLemma(listSent.get(i).lemmas().get(j));
				ann.setNer(listSent.get(i).nerTag(j));
				ann.setBegin(listSent.get(i).characterOffsetBegin(j));
				ann.setEnd(listSent.get(i).characterOffsetEnd(j));
				// String sentence = listSent.get(i).rawSentence().getText();
			}
			
			System.out.println("Number of NEL: " + entityList.size());
			for (Entity entity : entityList) {
				String anchor = entity.getSurfaceText();
				ta.hasEntity(annotationList, anchor, entity.getURI());
			}
			
			Collection<RelationTriple> textTriples = listSent.get(i).openieTriples();
			for(RelationTriple triple : textTriples) {
				OpenRelation rel = ta.hasRelation(annotationList, triple.subjectLemmaGloss(), triple.objectLemmaGloss());
				if(rel != null) {
					rel.setRel(triple.relationLemmaGloss());
					rel.setOrigSentence(sentence);
				}
				if(openRelationList.size() > 1) {
					if(!ta.repeatedAnnotation(openRelationList, rel))
						openRelationList.add(rel);
				}
			}
		}
		docAnn.setAnnotationList(annotationList);
		docAnn.setOpenRelationList(openRelationList);
		System.out.println("Returning document annotation");
		return docAnn;
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
