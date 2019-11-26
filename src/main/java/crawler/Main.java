package crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.bson.Document;

import db.MongoDBUtils;
import edu.stanford.nlp.io.StringOutputStream;
import nlp.Annotation;
import nlp.DocumentAnnotation;
import nlp.TextAnalisys;
import schema.SFWCSchema;
import sparql.QueryTopic;

public class Main {

//	static Logger root = (Logger) LoggerFactory
//	        .getLogger(Logger.ROOT_LOGGER_NAME);
//
//	static {
//	    root.setLevel(Level.ERROR);
//	}
	
	private String mongoDB = "SFWC_V2";
	private String mongoColl = "politics";
	
	public static void main(String[] args) throws IOException, InterruptedException {
		Main m = new Main();
		String corpusPath = "./corpus/politics";
		String pathProcessedDocuments = "ProcessedDocuments-politics.txt";
		String idfFilePath = "IDF-Politics.txt";
		String topic = "Politics";
		MongoDBUtils ut = new MongoDBUtils(m.mongoDB, m.mongoColl);
//		QueryTopic qt = new QueryTopic();
		
		int task = 7;
		
		/*
		 * Task 1: extract tokens, NER, NEL from a corpus and save result in MongDB 
		 * Task 2: calculate TF value for each token - save result in MongoDB
		 * Task 3: calculate IDF for each token - save result in a file
		 * Task 4: add IDF result to MongoDB
		 * Task 5: compute correlation - save result in MongoDB
		 * Task 6: calculate TF-IDF - save result in MongoDB
		 * Task 7: create RDF model
		 * Task 8: emergency function : delete unrelated documents in MongoDB 
		 */
		long startTime = System.currentTimeMillis();
		switch (task){
			case 1:  m.informationExtraction(corpusPath, pathProcessedDocuments); //step 1: already finish for computerScience, politics and diabetes
					break;
			case 2: m.computeTF();
					break;
			case 3: Set<String> index = ut.generateLemmaIndex();
					System.out.println("Index size = " + index.size());
					m.computeIDFWord(index, idfFilePath);
					break;
			case 4: m.addIDFToMongoDB(idfFilePath);
					break;
			case 5: m.correlation();
					break;
			case 6: m.addTFIDFToMongoDB();
					break;
			case 7: List<Document> documentList = ut.getAllDocs();
					m.createModel(documentList, topic, "./politics_v2.ttl");
					break;
			case 8: m.temporalDeleteDocuments();
					break;
			default: break;
		}
		
		long endTime = System.currentTimeMillis();
		
		long totalTime = endTime - startTime;
		
		System.out.format("%d Miliseconds = %d minutes\n", totalTime, TimeUnit.MILLISECONDS.toMinutes(totalTime));
		ut.close();
		
	}
	
	public void temporalDeleteDocuments() {
		File[] path = new File("./corpus/computerScience").listFiles();
		MongoDBUtils utils = new MongoDBUtils(mongoDB, mongoColl);
		
		List<String> docNames = new ArrayList<String>();
		
		for(File f : path) {
			docNames.add(f.getName());
		}
		
		List<String> docsIds = utils.getDocsId();
		List<String> removeList = new ArrayList<String>();
		System.out.println(docsIds.size());
		System.out.println(path.length);
		for(String id : docsIds) {
			if(!docNames.contains(id)) {
				removeList.add(id);
			}
		}
		utils.deleteDocuments(removeList);
		System.out.println(removeList.size());
		
		utils.close();
		
	}
	
	@SuppressWarnings("deprecation")
	public void informationExtraction(String corpusPath, String pathProcessedDocuments) throws IOException {
		TextAnalisys ta = new TextAnalisys();
		MongoDBUtils mongoUtils = new MongoDBUtils(mongoDB, mongoColl);
		File[] documents = new File(corpusPath).listFiles();
		File processedDocuments = new File(pathProcessedDocuments);
		
		int counter = 0;
		int counterProcessed = 1;
		
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
					mongoUtils.storeDocument(docAnn, document.getName(), content);
				}else
					System.out.println("Document: " + document.getName() + " is empty");
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
//			if(counter == 100)
//				break;
			counter ++;
			counterProcessed++;
			//ta.cleanStanfordDocument(text);
		}
		
		
		System.out.println("Number of Documents processed: " + counter);
		
		mongoUtils.close();
	}
	
	@SuppressWarnings("deprecation")
	public void computeTF() throws IOException, InterruptedException {
		MongoDBUtils utils = new MongoDBUtils(mongoDB, mongoColl);
		List<String> documentsId = utils.getDocsId();
		File processedDocuments = new File("ProcessedDocuments-Politics-TF.txt");
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
			System.out.println("Processing document "+ counterProgress++ + "/"+documentsId.size()+": " + docId);
			//Thread.sleep(2000);
			for(int i = 0; i < lemmaList.size(); i++) {
				
				double tf = tf(lemmaList, lemmaList.get(i));
				utils.updateDocumentTF(docId, tf, i);
			}

		}
		utils.close();
	}
	
	public List<Annotation> computeLemmaTF(List<Annotation> annotationList) throws IOException, InterruptedException {
		List<String> lemmaList = extractDocumentLemmas(annotationList);
		List<Double> tfList = new ArrayList<Double>();
		List<Annotation> topTenList = new ArrayList<Annotation>();
		for(Annotation annotation : annotationList) {
			double tf = tf(lemmaList, annotation.getLemma());
			tfList.add(tf);
			annotation.settf(tf);
		}
		
		Collections.sort(tfList, Collections.reverseOrder());
//		System.out.println(tfList);
//		Set<String> lemmaSet = new HashSet<String>();
//		int i = 0;
//		while(lemmaSet.size() < 10) {
//			System.out.println(tfList.get(i));
//			for(Annotation annotation : annotationList) {
//				if(annotation.gettf() == tfList.get(i) && !lemmaSet.contains(annotation.getLemma())) {
//					topTenList.add(annotation);
//					lemmaSet.add(annotation.getLemma());
//					break;
//				}
//			}
//			i++;
//		}
//		System.out.println("topTenList = " + topTenList.size());
		return annotationList;
		
	}
	
	private List<String> extractDocumentLemmas(List<Annotation> annotationList){
		List<String> lemmaList = new ArrayList<String>();
		for(Annotation ann : annotationList) {
			lemmaList.add(ann.getLemma());
		}
		return lemmaList;
	}
	
	
	
//	public void computeIDF() throws IOException {
//		MongoDBUtils utils = new MongoDBUtils();
//		List<String> documentsId = utils.getDocsId();
//		File processedDocuments = new File("ProcessedDocuments-CS-IDF.txt");
//		int counterProgress = 0;
//		
//		int documentsIdSize = documentsId.size();
//		for(String docId : documentsId) {
//			if(processedDocuments.exists() 
//					&& isProcessed(docId, FileUtils.readLines(processedDocuments))){
//				System.out.println("File " + docId + " already processed");
//				counterProgress++;
//				continue;
//			}
//			
//			FileUtils.write(processedDocuments, docId+"\n", "UTF8", true);
//			List<String> lemmaList = utils.getDocLemmas(docId);
//			System.out.println("----------Begin------------");
//			System.out.println(counterProgress++ + "/" + documentsIdSize + ": _id - " 
//					+ docId
//					+ " Number of words: " + lemmaList.size());
//			System.out.print("Progress: ");
//			long startTime = System.currentTimeMillis();
//			for(int i = 0; i < lemmaList.size(); i++) {
//				System.out.print(i + ", ");
//				int n = utils.numberOfDocumentsWithTerm(lemmaList.get(i));
//				double idf = documentsId.size()/(double)n;
////				utils.updateDocumentIDF(docId, lemmaList.get(i), idf, i);
//			}
//			long endTime = System.currentTimeMillis() - startTime;
//			System.out.println("\n Time elpased: " + TimeUnit.MILLISECONDS.toMinutes(endTime) + " min.");
//			System.out.println("----------END--------------");
//			
//		}
//		
//	}
	
	public void addIDFToMongoDB(String idfFilePath) throws IOException, InterruptedException {
		MongoDBUtils utils = new MongoDBUtils(mongoDB, mongoColl);
		@SuppressWarnings("deprecation")
		List<String> idfList = FileUtils.readLines(new File(idfFilePath));
		for(int i = 0; i < idfList.size(); i++) {
//			Thread.sleep(2000);
			String[] idfArray = idfList.get(i).split("\t");
			if(idfArray != null && 
					idfArray.length > 2) {
				List<Document> documentList = utils.documentsWithTerm(idfArray[0]);
				System.out.println(i + "/"+idfList.size()+" - Lemma: " + idfArray[0] + " Documents with lemma: " + documentList.size());
				for(Document doc : documentList) {
					@SuppressWarnings("unchecked")
					List<Document> enList = (List<Document>) doc.get("EN", ArrayList.class);
					for(int j = 0; j < enList.size(); j++) {
						if(enList.get(j).getString("lemma").equals(idfArray[0])) {
							double idfValue = Double.parseDouble(idfArray[1]);
							double idfFinal = Math.log(idfValue)/Math.log(2);
							utils.updateDocumentIDF(doc.getString("_id"), String.valueOf(idfFinal), j);
						}
					}
				}
			}			
		}
		utils.close();
	}
	
	public void addTFIDFToMongoDB() throws InterruptedException {
		MongoDBUtils utils = new MongoDBUtils(mongoDB, mongoColl);
		
		List<Document> allDocuments = utils.getAllDocs();
		for(int i = 0 ; i < allDocuments.size(); i++) {
			@SuppressWarnings("unchecked")
			List<Document> enList = (List<Document>) allDocuments.get(i).get("EN", ArrayList.class);
			System.out.println("TFIDF: " + i + "/" + allDocuments.size() + " --"+allDocuments.get(i).getString("_id")+"-- :::" + enList.size() + " ::: entities to process");
			//Thread.sleep(2000);
			for(int j = 0; j < enList.size(); j++) {
				double tf = Double.parseDouble(enList.get(j).getString("tf"));
				double idf = Double.parseDouble(enList.get(j).getString("idf"));
				utils.updateDocumentTFIDF(allDocuments.get(i).getString("_id"), String.valueOf(tf*idf), j);
			}
		}
		utils.close();
	}
	
	public void computeIDFWord(Set<String> index, String idfFilePath) throws IOException {
		MongoDBUtils utils = new MongoDBUtils(mongoDB, mongoColl);
		List<String> documentsId = utils.getDocsId();

		int counterProgress = 0;
		System.out.println("----------Begin------------");
		System.out.print("Progress: ");
		File processedDocuments = new File(idfFilePath);
		for(String word : index) {
			System.out.println(counterProgress++ + "/" + index.size() + ": lemma - " 
					+ word);
			int n = utils.numberOfDocumentsWithTerm(word);
			double idf = documentsId.size()/(double)n;
			FileUtils.write(processedDocuments, word+"\t"+idf+"\t"+documentsId.size()+"\t"+n+"\n", "UTF8", true);
		}
		System.out.println("----------END--------------");
		utils.close();
	}
	
	
	
	public void correlation() throws InterruptedException {
		MongoDBUtils utils = new MongoDBUtils(mongoDB, mongoColl);
		
		List<Document> allDocuments = utils.getAllDocs();

		for(int i = 0; i < allDocuments.size(); i++) {
			@SuppressWarnings("unchecked")
			List<Document> rels = (List<Document>) allDocuments.get(i).get("rel", ArrayList.class);
			System.out.println("CORRELATION: "+ i + "/" + allDocuments.size() + " --Document-- :" + rels.size() + " relations to process");
//			Thread.sleep(2000);
			computeCorrelation(rels, allDocuments.get(i).getString("_id"));
		}
		utils.close();
	}
	
	public void sparqlCorrelation() throws InterruptedException {
		QueryTopic qt = new QueryTopic(mongoColl);
		
		List<String> allDocuments = qt.queryDocuments();

		for(int i = 0; i < allDocuments.size(); i++) {
//			System.out.println(allDocuments.get(i));
			List<String> rels = qt.queryDocumentRelation(allDocuments.get(i));
			System.out.println("CORRELATION: "+ i + "/" + allDocuments.size() + " --Document-- :" + rels.size() + " relations to process");
			computeSparqlCorrelation(rels, allDocuments.get(i));
		}
		
	}
	
	public void computeCorrelation(List<Document> rels, String documentId) {
		MongoDBUtils utils = new MongoDBUtils(mongoDB, mongoColl);
		for(int i = 0; i < rels.size(); i++) {
			String lemmaX = ((Document) rels.get(i).get("subject", ArrayList.class).get(0)).getString("lemma");
			String lemmaY = ((Document) rels.get(i).get("object", ArrayList.class).get(0)).getString("lemma");
			
			int n_11 = utils.correlationXandY(lemmaX, lemmaY);
			int n_10 = utils.correlationXNotY(lemmaX, lemmaY);
			int n_01 = utils.correlationNotXY(lemmaX, lemmaY);
			int n_00 = utils.correlationNotXNotY(lemmaX, lemmaY);
			int n_1_ = n_11 + n_10;
			int n_0_ = n_01 + n_00;
			int n__1 = n_11 + n_01;
			int n__0 = n_10 + n_00;
			
			//n_11*n_00
			int mult1 = n_11 * n_00;
			//n_10 * n_01
			int mult2 = n_10 * n_01;
			
			int result1 = mult1 - mult2;
			
			int mult3 = n_1_ * n_0_ * n__0 * n__1;
			
			double result2 = Math.sqrt(mult3);
			
			double correlation = result1/result2;
			
			utils.updateDocumentCorrelation(documentId, String.valueOf(correlation), i);
			
		}
		utils.close();
	}
	
	public void computeSparqlCorrelation(List<String> rels, String documentURI) {
		QueryTopic qt = new QueryTopic(mongoColl);
		for(int i = 0; i < rels.size(); i++) {
			String lemmaX = rels.get(i).split(":")[0];
			String lemmaY = rels.get(i).split(":")[1];
			
			int n_11 = qt.queryCorrelationXandY(lemmaX, lemmaY);
			int n_10 = qt.queryCorrelationXNotY(lemmaX, lemmaY);
			int n_01 = qt.queryCorrelationNotXY(lemmaX, lemmaY);
			int n_00 = qt.queryCorrelationNotXNotY(lemmaX, lemmaY);
			int n_1_ = n_11 + n_10;
			int n_0_ = n_01 + n_00;
			int n__1 = n_11 + n_01;
			int n__0 = n_10 + n_00;
			
			//n_11*n_00
			int mult1 = n_11 * n_00;
			//n_10 * n_01
			int mult2 = n_10 * n_01;
			
			int result1 = mult1 - mult2;
			
			double mult3 = (n_1_ * n_0_) * (double)(n__0 * n__1);
			double result2 = Math.sqrt(mult3);
			
			double correlation = result1/result2;
			if(Double.isNaN(correlation)) {
				correlation = 0.0d;
				System.out.println("\tCorrelation = NaN");
				System.out.print(n_11 + ", ");
				System.out.print(n_10 + ", ");
				System.out.print(n_01 + ", ");
				System.out.println(n_00);
				System.out.println("\t\t"+ result1+"/"+result2);
			}
			
			
//			System.out.println(lemmaX + " - " + lemmaY + " = " + correlation);
			
//			utils.updateDocumentCorrelation(documentId, String.valueOf(correlation), i);
			
		}
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
	
	public void createModel(List<Document> documentList, String topic, String outputFilePath) throws IOException {
		Model model = SFWCSchema.getModel();
		for(Document document : documentList) {
			String documentName = document.getString("_id");
			@SuppressWarnings("unchecked")
			List<Document> NEs = (List<Document>) document.get("EN", ArrayList.class);
			@SuppressWarnings("unchecked")
			List<Document> Rels = (List<Document>) document.get("rel", ArrayList.class);
			
			Resource subject = model.createResource(SFWCSchema.SFWC_URI+"Document-"+documentName);
			model.add(subject, RDF.type, SFWCSchema.DOCUMENT);
			model.add(subject, RDFS.label, document.getString("_id"));
			model.add(subject, SFWCSchema.hasAssociatedTopic, SFWCSchema.TOPIC);
			model.add(SFWCSchema.TOPIC, RDFS.label, topic);
			
			model = addDocument(model, documentName, subject, NEs, Rels);
		}
//		StringOutputStream sos = new StringOutputStream();
//		model.write(sos, "NTRIPLES");
//		String[] content = sos.toString().split("\n");

		File output = new File(outputFilePath);
		model.write(new FileWriter(output), "TTL");
		
//		QueryTopic qt = new QueryTopic();
//		qt.insertTriplesVirtuoso(content);
		
		
	}
	
	
	
	private Model addDocument(Model model, String documentName, Resource subject, List<Document> NEs, List<Document> Rels) throws IOException {
		
		//add NEs
		int counter = 0;
		for(Document ne : NEs) {
			counter++;
			String lemma = ne.getString("lemma").toLowerCase();
			String nifType = "";
			int begin = ne.getInteger("begin");
			int end = ne.getInteger("end");
			
			if(lemma.contains("<"))
				System.out.println(counter + " - " + subject.getURI());
			if(lemma.contains("/"))
				lemma = lemma.replace("/", "_");
			if(lemma.contains("."))
				lemma = lemma.replace(".", "_");
			if(lemma.contains("'"))
				lemma = lemma.replace("'", "_");
			if(lemma.split(" ").length > 1)
				nifType = "Phrase";
			else
				nifType = "Word";
			Resource lemmaObj = model.createResource(SFWCSchema.SFWC_URI+documentName + "_Entity-"+lemma+"_"+begin+"-"+end);
			
			model = addNE(model, subject, lemma, lemmaObj, nifType, ne);
			
			model.add(subject, SFWCSchema.hasEntity, lemmaObj);
		}
		
		//add Rels
		counter = 0;
		for(Document rel : Rels) {
			counter++;
			Document relSubject = (Document) rel.get("subject", ArrayList.class).get(0);
			Document relObject = (Document) rel.get("object", ArrayList.class).get(0);
			String lemmaSbj = relSubject.getString("lemma").toLowerCase();
			String lemmaObj = relObject.getString("lemma").toLowerCase();
			String sentence = rel.getString("sentence");
			String correlation = "0.0";
			
			if(lemmaSbj.contains("<"))
				System.out.println("Rel: " + counter + " - " + subject.getURI());
			
			if(lemmaSbj.contains("/"))
				lemmaSbj = lemmaSbj.replace("/", "_");
			if(lemmaSbj.contains("."))
				lemmaSbj.replace(".", "_");
			if(lemmaSbj.contains("'"))
				lemmaSbj = lemmaSbj.replace("'", "_");
			
			if(lemmaObj.contains("<"))
				System.out.println("Rel: " +counter + " - " + subject.getURI());
			
			if(lemmaObj.contains("/"))
				lemmaObj = lemmaObj.replace("/", "_");
			if(lemmaObj.contains("."))
				lemmaObj.replace(".", "_");
			if(lemmaObj.contains("'"))
				lemmaObj = lemmaObj.replace("'", "_");
			
			Resource relSbj = model.createResource(SFWCSchema.SFWC_URI+documentName+"_Relation-"+lemmaSbj+"_"+lemmaObj);
			model = addRels(model, relSbj, subject, lemmaSbj, lemmaObj, sentence, correlation);
			
			model.add(subject, SFWCSchema.hasRelation, relSbj);
		}
		return model;
	}
	
	private Model addNE(Model model, Resource docRes, String lemmaString, Resource lemma, 
			String nifType, Document NE) {
		String posTag = NE.getString("posTag");
		String ner = NE.getString("ner");
		String uri = NE.getString("uri");
		String tf = String.valueOf(NE.get("tf", Object.class));
		String idf = String.valueOf(NE.get("idf",Object.class));
		String tfIdf = String.valueOf(NE.get("tfIdf", Object.class));
		
		model.add(lemma, RDF.type, SFWCSchema.ENTITY);
		model.add(lemma, RDF.type, SFWCSchema.NIF_URI+nifType);
		model.add(SFWCSchema.ENTITY, RDFS.subClassOf, SFWCSchema.NIF_URI+nifType);
		model.add(lemma, model.createProperty(SFWCSchema.NIF_URI+"anchorOf"), lemmaString);
		model.add(lemma, model.createProperty(SFWCSchema.NIF_URI+"posTag"), posTag);
		if(uri.contains("http"))
			model.add(lemma, model.createProperty(SFWCSchema.ITSRDF_URI+"taIdentRef"), model.createResource(uri));
		if(!ner.equals("O"))
			model.add(lemma, SFWCSchema.neTag, ner);
		model.add(lemma, SFWCSchema.inDocument, docRes);
		model.add(lemma, SFWCSchema.tfValue, tf);
		model.add(lemma, SFWCSchema.idfValue, idf);
		model.add(lemma, SFWCSchema.tfIdfValue, tfIdf);
		return model;
	}
	
	private Model addRels(Model model, Resource relSbj, Resource relDoc, String lemmaSbj, String lemmaObj, 
			String sentence, String correlation) {
		
		model.add(relSbj, RDF.type, SFWCSchema.RELATION);
		
		model.add(relSbj, SFWCSchema.subjectEntity, model.createResource(SFWCSchema.SFWC_URI+lemmaSbj));
		model.add(relSbj, SFWCSchema.objectEntity, model.createResource(SFWCSchema.SFWC_URI+lemmaObj));
		model.add(relSbj, SFWCSchema.subjectLabel, lemmaSbj);
		model.add(relSbj, SFWCSchema.objectLabel, lemmaObj);
		model.add(relSbj, SFWCSchema.sentence, sentence);
		model.add(relSbj, SFWCSchema.inDocument, relDoc);
//		model.add(relSbj, SFWCSchema.correlationValue, correlation);
		
		return model;
	}
	
//	private Map<String, Double> generateIDFValues(List<String> idfLines){
//		Map<String, Double> mapIdf = new HashMap<String, Double>();
//		for(String idf: idfLines) {
//			String[] idfSplit = idf.split("\t");
//			if(idfSplit[0] != null 
//					&& idfSplit[1] != null)
//				mapIdf.put(idfSplit[0], Double.parseDouble(idfSplit[1]));
//			else
//				System.out.println("idf = " + idf + "\n====FAIL in split task");
//		}
//		return mapIdf;
//	}

}
