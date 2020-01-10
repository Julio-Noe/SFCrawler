package crawler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.bson.Document;

import com.mongodb.BasicDBObject;

import nlp.Annotation;
import nlp.TextAnalisys;
import sparql.QueryTopic;

public class SFWC {
	
	private String collectionName;
	
	public SFWC(String collection) {
		this.collectionName = collection;
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		SFWC s = new SFWC("computerScience");
		@SuppressWarnings("deprecation")
		String content = FileUtils.readFileToString(new File("test5.txt"));
		//s.sfwc(content);
	}
	
	public Document sfwc(String content, String url, double top, double down) throws IOException, InterruptedException {
		Main main = new Main();
		if (!content.isEmpty()) {
			long startTime = System.currentTimeMillis();
			Document mainObj = new Document();
			
			System.out.println("Information Extraction process");
			List<Annotation> listAnnotation = sfwcInformationExtraction(content);
			if(listAnnotation == null) 
				return null;
			
			System.out.println("Calculating TF");
			listAnnotation = main.computeLemmaTF(listAnnotation);
			System.out.println("Calculating IDF/TFIDF");
			listAnnotation = computeSparqlIDFWord(listAnnotation); // calculate idf and tf-idf
			
			double avgWeight = 0.0d;
			double median = 0.0d;
			int medianIndex = 0;
			
			List<Double> tfidfList = new ArrayList<Double>();
			Set<Double> idfSet = new HashSet<Double>();
			Set<String> idfLemmaSet = new HashSet<String>();
			List<BasicDBObject> dbDocumentList = generateDBObjects(listAnnotation);
			
//			for (Annotation ann : listAnnotation) {
//				if(ann.getIdf() > 0d && !ann.getUri().isEmpty()) {
//					idfSet.add(ann.getIdf()); //avoid repeated values
//					idfLemmaSet.add(ann.getLemma());
//				}
//			}
			List<BasicDBObject> rangeList = new ArrayList<BasicDBObject>();
			for (BasicDBObject obj : dbDocumentList) {
				if(obj.getDouble("idf") > 0d 
						&& !obj.getString("uri").isEmpty()
						&& !idfLemmaSet.contains(obj.getString("lemma"))) {
					rangeList.add(obj); //avoid repeated values
					idfSet.add(obj.getDouble("idf"));
					idfLemmaSet.add(obj.getString("lemma"));
				}
			}
			
			tfidfList.addAll(idfSet);
			Collections.sort(tfidfList, Collections.reverseOrder()); //list values from greater to smaller
			System.out.println("IDF values: ");
			for(Double ann : tfidfList) {
				avgWeight += ann;
			}
			System.out.println("List size: " + tfidfList.size());
			
			
			mainObj.put("_id", url);
			mainObj.put("crawled", true);
			mainObj.put("content", content);
			mainObj.put("EN", dbDocumentList);
			mainObj.put("wordsInRange", rangeList);
			
			if(!tfidfList.isEmpty() && avgWeight > 0.0d && tfidfList.size() > 10) {
				medianIndex = tfidfList.size() / 2;
				avgWeight = avgWeight / tfidfList.size();
				median =  tfidfList.get(medianIndex);
				System.out.println(idfSet);
				System.out.println(idfLemmaSet);
				System.out.println("Average: " + avgWeight );
				System.out.println("Median: " + median);
				mainObj.put("average", avgWeight);
				mainObj.put("median", median);
			}else {
				System.out.println("The list is empty");
				avgWeight = 0.0d;
				median =  0.0d;
				mainObj.put("error", true);
				mainObj.put("errorMessage","Empty list");
				mainObj.put("average", 0.0d);
				mainObj.put("median", 0.0d);
			}
			
			
			long endTime = System.currentTimeMillis();
			long totalTime = endTime - startTime;

			System.out.format("%d Miliseconds = %d seconds\n", totalTime, TimeUnit.MILLISECONDS.toSeconds(totalTime));
			mainObj.put("elapsedTime", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(totalTime)));
			
			return mainObj;
		}else
			return null;
	}	
	
	private List<BasicDBObject> generateDBObjects(List<Annotation> annotationList){
		List<BasicDBObject> dbObjectList = new ArrayList<BasicDBObject>();
		for(Annotation ann : annotationList) {
			BasicDBObject obj = new BasicDBObject();
			obj.put("lemma", ann.getLemma());
			obj.put("posTag", ann.getPosTag());
			obj.put("ner", ann.getNer());
			obj.put("uri", ann.getUri());
			obj.put("begin", ann.getBegin());
			obj.put("end", ann.getEnd());
			obj.put("tf",ann.gettf());
			obj.put("idf", ann.getIdf());
			obj.put("tfIdf", ann.getTfidf());
			dbObjectList.add(obj);
		}
		return dbObjectList;
	}
	
	private List<Annotation> sfwcInformationExtraction(String content) throws IOException, InterruptedException {
		TextAnalisys ta = new TextAnalisys();
		List<Annotation> docAnn = null;
			docAnn = ta.stanfordTestDocumentAnalizer(content);
			return docAnn;
	}
	
	private List<Annotation> computeSparqlIDFWord(List<Annotation> annotationList) throws IOException, InterruptedException {
		QueryTopic qt = new QueryTopic(collectionName);
		List<String> documentsURI = qt.queryDocuments();

//		int counterProgress = 0;
//		System.out.println("----------Begin------------");
//		System.out.print("Progress: ");
//		int counter = 0;
		Set<String> lemmaSet = new HashSet<String>();
		for(Annotation ann : annotationList) {
//			counter++;
//			System.out.println(counterProgress++ + "/" + annotationList.size() + ": lemma - " 
//					+ ann.getLemma());
			int n = qt.queryNumberLemmaInDocuments(ann.getLemma()).size();
//			double idf = Math.log(documentsURI.size()/(double)n);
			double idfValue = documentsURI.size()/(double)n;
			double idf = Math.log(idfValue)/Math.log(2);
			if(Double.isInfinite(idf)) {
				idf = 0.0d;
			}
			ann.setIdf(idf);
			ann.setTfidf(ann.gettf() * idf);
		}
//		System.out.println("----------END--------------");
		return annotationList;
		
	}
//commented because it is not use but it works
	
//	@SuppressWarnings("deprecation")
//	private void computeSparqlTF() throws IOException, InterruptedException {
//		QueryTopic qt = new QueryTopic();
//		List<String> documentsURI = qt.queryDocuments();
//		File processedDocuments = new File("ProcessedDocuments-CS-SPARQL-TF.txt");
//		int counterProgress = 0;
//		for(String docURI : documentsURI) {
//			if(processedDocuments.exists() 
//					&& isProcessed(docURI, FileUtils.readLines(processedDocuments))){
//				System.out.println("File " + docURI + " already processed");
//				counterProgress++;
//				continue;
//			}
//			FileUtils.write(processedDocuments, docURI+"\n", "UTF8", true);
//			
//			List<String> lemmaList = qt.queryLemmasDocument(docURI);
//			System.out.println("Processing document "+ counterProgress++ + "/"+documentsURI.size()+": " + docURI);
//
//		}
//		
//	}
	
	//commented because it is not use but it works
//	private boolean isProcessed(String documentName, List<String> processedList){
//		if(processedList != null)
//			for(String processed : processedList) {
//				if(documentName.equalsIgnoreCase(processed))
//					return true;
//			}
//		return false;
//	}

}
