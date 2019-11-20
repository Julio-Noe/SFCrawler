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
		s.sfwc(content);
	}
	
	public double sfwc(String content) throws IOException, InterruptedException {
		Main main = new Main();
		if (!content.isEmpty()) {
			long startTime = System.currentTimeMillis();
			
			System.out.println("Information Extraction process");
			List<Annotation> listAnnotation = sfwcInformationExtraction(content);
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
			for (Annotation ann : listAnnotation) {
				if(ann.getIdf() > 0d && !ann.getUri().isEmpty()) {
					idfSet.add(ann.getIdf()); //avoid repeated values
					idfLemmaSet.add(ann.getLemma());
				}
			}
			
			tfidfList.addAll(idfSet);
			Collections.sort(tfidfList, Collections.reverseOrder()); //list values from greater to smaller
			System.out.println("IDF values: ");
			for(Double ann : tfidfList) {
				avgWeight += ann;
			}
			System.out.println("List size: " + tfidfList.size());
			
			
			
			if(!tfidfList.isEmpty() && avgWeight > 0.0d && tfidfList.size() > 10) {
				medianIndex = tfidfList.size() / 2;
				avgWeight = avgWeight / tfidfList.size();
				median =  tfidfList.get(medianIndex);
				System.out.println(idfSet);
				System.out.println(idfLemmaSet);
				System.out.println("Average: " + avgWeight );
				System.out.println("Median: " + median);
			}else {
				System.out.println("The list is empty");
				avgWeight = 0.0d;
				median =  0.0d;
			}
			
			
			long endTime = System.currentTimeMillis();
			long totalTime = endTime - startTime;

			System.out.format("%d Miliseconds = %d seconds\n", totalTime, TimeUnit.MILLISECONDS.toSeconds(totalTime));
//			System.out.println("Graph average : " + qt.queryAvgIDF());
//			System.out.println("Graph median : " + qt.queryMedianIDF());
			return avgWeight;
//			return tfidfList.get(medianIndex);
		}else
			return 0.0d;
	}	
	
	private List<Annotation> sfwcInformationExtraction(String content) throws IOException {
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
