package crawler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.bson.Document;

import db.MongoDBUtils;
import nlp.DocumentAnnotation;
import nlp.TextAnalisys;
import schema.SFWCSchema;

public class Main {

//	static Logger root = (Logger) LoggerFactory
//	        .getLogger(Logger.ROOT_LOGGER_NAME);
//
//	static {
//	    root.setLevel(Level.ERROR);
//	}
	
	public static void main(String[] args) throws IOException {
		Main m = new Main();
		
		long startTime = System.currentTimeMillis();
		
//		m.informationExtraction(); //step 1: already finish for computerScience, politics and diabetes
		
		//second part: tf, idf, tf-idf, correlation
//		m.computeTF();
//		m.computeIDF();
		MongoDBUtils ut = new MongoDBUtils();
		
//		Set<String> index = ut.generateLemmaIndex();
//		System.out.println("Index size = " + index.size());
		
//		m.computeIDFWord(index);
		List<Document> documentList = ut.getAllDocs();
		m.createModel(documentList.get(0));
		long endTime = System.currentTimeMillis();
		
		long totalTime = endTime - startTime;
		
		System.out.format("%d Miliseconds = %d minutes\n", totalTime, TimeUnit.MILLISECONDS.toMinutes(totalTime));
		
		
		
	}
	
	@SuppressWarnings("deprecation")
	public void informationExtraction() throws IOException {
		TextAnalisys ta = new TextAnalisys();
		MongoDBUtils mongoUtils = new MongoDBUtils();
		File[] documents = new File("./corpus/diabetesWoHumans").listFiles();
		File processedDocuments = new File("ProcessedDocuments-Diabetes.txt");
		
		int counter = 0;
		int counterProcessed = 0;
		
		for(File document : documents){
			System.out.println("Processing document: " 
				+ document.getName()  
				+ "("  
				+ counterProcessed  
				+ "/" 
				+ documents.length 
				+ ")" );
			if(document.getName().contains(".sch_(file_extension)")) 
				continue;
			
			if(processedDocuments.exists() 
					&& isProcessed(document.getName(), FileUtils.readLines(processedDocuments))){
				System.out.println("File " + document.getName() + " already processed");
				counterProcessed++;
				continue;
			}
			
			FileUtils.write(processedDocuments, document.getName()+"\n", "UTF8", true);
			
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
			if(counter == 100)
				break;
			counter ++;
			counterProcessed++;
			//ta.cleanStanfordDocument(text);
		}
		
		
		System.out.println("Number of Documents processed: " + counter);
	}
	
	public void computeTF() throws IOException {
		MongoDBUtils utils = new MongoDBUtils();
		List<String> documentsId = utils.getDocsId();
		File processedDocuments = new File("ProcessedDocuments-CS-TF.txt");
		int counterProgress = 0;
		for(String docId : documentsId) {
			if(processedDocuments.exists() 
					&& isProcessed(docId, FileUtils.readLines(processedDocuments))){
				System.out.println("File " + docId + " already processed");
				counterProgress++;
				continue;
			}
			FileUtils.write(processedDocuments, docId+"\n", "UTF8", true);
			
			List<String> lemmaList = utils.getDocLemmas(docId);
			for(int i = 0; i < lemmaList.size(); i++) {
				double tf = tf(lemmaList, lemmaList.get(i));
				utils.updateDocumentTF(docId, lemmaList.get(i), tf, i);
			}

		}
		
	}
	
	public void computeIDF() throws IOException {
		MongoDBUtils utils = new MongoDBUtils();
		List<String> documentsId = utils.getDocsId();
		File processedDocuments = new File("ProcessedDocuments-CS-IDF.txt");
		int counterProgress = 0;
		
		int documentsIdSize = documentsId.size();
		for(String docId : documentsId) {
			if(processedDocuments.exists() 
					&& isProcessed(docId, FileUtils.readLines(processedDocuments))){
				System.out.println("File " + docId + " already processed");
				counterProgress++;
				continue;
			}
			
			FileUtils.write(processedDocuments, docId+"\n", "UTF8", true);
			List<String> lemmaList = utils.getDocLemmas(docId);
			System.out.println("----------Begin------------");
			System.out.println(counterProgress++ + "/" + documentsIdSize + ": _id - " 
					+ docId
					+ " Number of words: " + lemmaList.size());
			System.out.print("Progress: ");
			long startTime = System.currentTimeMillis();
			for(int i = 0; i < lemmaList.size(); i++) {
				System.out.print(i + ", ");
				int n = utils.numberOfDocumentsWithTerm(lemmaList.get(i));
				double idf = documentsId.size()/(double)n;
				utils.updateDocumentIDF(docId, lemmaList.get(i), idf, i);
			}
			long endTime = System.currentTimeMillis() - startTime;
			System.out.println("\n Time elpased: " + TimeUnit.MILLISECONDS.toMinutes(endTime) + " min.");
			System.out.println("----------END--------------");
			
		}
		
	}
	
	public void computeIDFWord(Set<String> index) throws IOException {
		MongoDBUtils utils = new MongoDBUtils();
		List<String> documentsId = utils.getDocsId();

		int counterProgress = 0;
		System.out.println("----------Begin------------");
		System.out.print("Progress: ");
		File processedDocuments = new File("IDF-politics.txt");
		for(String word : index) {
			System.out.println(counterProgress++ + "/" + index.size() + ": lemma - " 
					+ word);
			int n = utils.numberOfDocumentsWithTerm(word);
			double idf = documentsId.size()/(double)n;
			FileUtils.write(processedDocuments, word+"\t"+idf+"\t"+documentsId.size()+"\t"+n+"\n", "UTF8", true);
		}
		System.out.println("----------END--------------");
		
	}
	
	
	public boolean isProcessed(String documentName, List<String> processedList){
		if(processedList != null)
			for(String processed : processedList) {
				if(documentName.equalsIgnoreCase(processed))
					return true;
			}
		return false;
	}
	
	private double tf(List<String> lemmaList, String targetLemma) {
		double tf = 0.0;
		int count = 0;
		
		for(String lemma : lemmaList) {
			if(lemma.equalsIgnoreCase(targetLemma)) 
				count += 1;
		}
		int totalLemmas = lemmaList.size();
		
		tf = count /(double) totalLemmas;
		return tf;			
	}
	
	public void createModel(Document document) throws IOException {
		String documentName = document.getString("_id");
		@SuppressWarnings("unchecked")
		List<Document> NEs = (List<Document>) document.get("EN", ArrayList.class);
		@SuppressWarnings("unchecked")
		List<Document> Rels = (List<Document>) document.get("rel", ArrayList.class);
		
		Model model = SFWCSchema.getModel();
		
		Resource subject = model.createResource(SFWCSchema.SFWC_URI+"Document_"+documentName);
		model.add(subject, RDF.type, SFWCSchema.DOCUMENT);
		model.add(subject, RDF.type, model.createResource(SFWCSchema.SFWC_URI+"ComputerScience"));
		
		model = addDocument(model, subject, NEs, Rels);
		
		model.write(System.out, "TTL");
		
	}
	
	private Model addDocument(Model model, Resource subject, List<Document> NEs, List<Document> Rels) throws IOException {
		
		File idfFile = new File("IDF-diabetes.txt");
		List<String> idfLines = FileUtils.readLines(idfFile);
		Map<String,Double> mapIdf = generateIDFValues(idfLines);
		
		//add NEs
		for(Document ne : NEs) {
			String lemma = ne.getString("lemma");
			String nifType = "";
			if(lemma.split(" ").length > 1)
				nifType = "Phrase";
			else
				nifType = "Word";
			Resource lemmaObj = model.createResource(SFWCSchema.SFWC_URI+lemma);
			if(mapIdf.containsKey(lemma))
				model = addNE(model, subject, lemma, lemmaObj, nifType, ne, mapIdf.get(lemma));
			else
				model = addNE(model, subject, lemma, lemmaObj, nifType, ne, 0.0d);
			
			model.add(subject, SFWCSchema.hasNE, lemmaObj);
		}
		
		//add Rels
		for(Document rel : Rels) {
			Document relSubject = (Document) rel.get("subject", ArrayList.class).get(0);
			Document relObject = (Document) rel.get("object", ArrayList.class).get(0);
			String lemmaSbj = relSubject.getString("lemma");
			String lemmaObj = relObject.getString("lemma");
			String sentence = rel.getString("sentence");
			String correlation = "0.0";
			
			Resource relSbj = model.createResource(SFWCSchema.SFWC_URI+lemmaSbj+"-"+lemmaObj);
			model = addRels(model, relSbj, lemmaSbj, lemmaObj, sentence, correlation);
			
			model.add(subject, SFWCSchema.hasRel, relSbj);
		}
		return model;
	}
	
	private Model addNE(Model model, Resource docRes, String lemmaString, Resource lemma, 
			String nifType, Document NE, Double idfValue) {
		String posTag = NE.getString("posTag");
		String ner = NE.getString("ner");
		String uri = NE.getString("uri");
		String tf = String.valueOf(NE.get("tf", Object.class));
		String idf = String.valueOf(idfValue);
		
		model.add(lemma, RDF.type, SFWCSchema.ENTITY);
		model.add(lemma, RDF.type, SFWCSchema.NIF_URI+nifType);
		model.add(lemma, model.createProperty(SFWCSchema.NIF_URI+"anchorOf"), lemmaString);
		model.add(lemma, model.createProperty(SFWCSchema.NIF_URI+"posTag"), posTag);
		if(uri.contains("http"));
			model.add(lemma, model.createProperty(SFWCSchema.ITSRDF_URI+"taIdentRef"), model.createResource(uri));
		if(!ner.equals("O"))
			model.add(lemma, SFWCSchema.ner, ner);
		model.add(lemma, SFWCSchema.inDocument, docRes);
		model.add(lemma, SFWCSchema.tfValue, tf);
		model.add(lemma, SFWCSchema.idfValue, idf);
		return model;
	}
	
	private Model addRels(Model model, Resource relSbj, String lemmaSbj, String lemmaObj, 
			String sentence, String correlation) {
		
		model.add(relSbj, RDF.type, SFWCSchema.RELATION);
		
		model.add(relSbj, SFWCSchema.subject, model.createResource(SFWCSchema.SFWC_URI+lemmaSbj));
		model.add(relSbj, SFWCSchema.object, model.createResource(SFWCSchema.SFWC_URI+lemmaObj));
		model.add(relSbj, SFWCSchema.sentence, sentence);
		model.add(relSbj, SFWCSchema.correlationValue, correlation);
		
		return model;
	}
	
	private Map<String, Double> generateIDFValues(List<String> idfLines){
		Map<String, Double> mapIdf = new HashMap<String, Double>();
		for(String idf: idfLines) {
			String[] idfSplit = idf.split("\t");
			if(idfSplit[0] != null 
					&& idfSplit[1] != null)
				mapIdf.put(idfSplit[0], Double.parseDouble(idfSplit[1]));
			else
				System.out.println("idf = " + idf + "\n====FAIL in split task");
		}
		return mapIdf;
	}

}
