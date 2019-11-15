package sparql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;

public class QueryTopic {
	
	VirtGraph set;
	
	public QueryTopic(String collectionName) {
		this.set = new VirtGraph("http://sfwc.com/"+collectionName,"jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2","dba","dba");
	}

	public static void main(String[] args) throws IOException {
		QueryTopic t = new QueryTopic("computerScience");
//		List<String> documents = t.queryDocuments("automaton");
		List<Double> idfList = t.queryMedianIDF();
		
//		System.out.println(t.queryMedianIDF());
		Double idfAvg = t.queryAvgIDF();
		System.out.println(idfAvg);
		List<Double> squareList = new ArrayList<Double>();
		for(Double idf : idfList) {
			squareList.add(Math.pow((idf-idfAvg), 2d));
		}
		Double result = 0.0d;
		for(Double square : squareList) {
			result += square;
		}
		result = result/idfList.size();
		
		System.out.println("Var: " + result);
		
		Double desv = Math.sqrt(result);
		
		System.out.println("desv: " + desv);
		t.queryLemmaWordsInRange(idfAvg, desv);
		
		
//		t.queryGlobalTFIDF();
//		t.testBabelNet();
//		System.out.println(documents.size());
	}
	
	public double calculateStandardDeviation(double avg) {
		List<Double> idfList = queryMedianIDF();
		
		System.out.println("Graph IDF average: " + avg);
		List<Double> squareList = new ArrayList<Double>();
		for(Double idf : idfList) {
			squareList.add(Math.pow((idf-avg), 2d));
		}
		Double result = 0.0d;
		for(Double square : squareList) {
			result += square;
		}
		result = result/idfList.size();
		
//		System.out.println("Var: " + result);
		
		Double desv = Math.sqrt(result);
		
		System.out.println("standard deviaton: " + desv);
		return desv;
	}
//	public void testBabelNet() {
//		Query queryString = QueryFactory.create("Select * WHERE {?s ?p ?o} LIMIT 100");
//		HttpClient client
//		try(QueryExecution qexec = QueryExecutionFactory.sparqlService("https://babelnet.org/sparql/", queryString)){
//			System.out.println(qexec.toString());
//			
//			ResultSet rs = qexec.execSelect();
//			
//		}
//	}
	
	public void insertTriplesVirtuoso(String[] lines) {
		String graphName = "<http://sfwc.com/computerScience>";
//		VirtGraph set = new VirtGraph("jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2","dba","dba");
		
		System.out.println("\n execute: CLEAR GRAPH " + graphName);
		String str = "CLEAR GRAPH " + graphName;
		VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(str, set);
		vur.exec();
		
		System.out.println("\n execute: INSERT INTO GRAPH " + graphName + " { MODEL content } ");
		int counter = 0;
		for(String content : lines) {
			System.out.println(counter++ + "/" + lines.length);
			str = "INSERT INTO GRAPH " + graphName + " { " + content + " }";
			vur = VirtuosoUpdateFactory.create(str, set);
			vur.exec();
		}
		
	}

	public void sparqlQuery() {
//		String graphName = "<http://sfwc.com/computerScience>";

		/* STEP 1 */
//		VirtGraph set = new VirtGraph("http://sfwc.com/computerScience","jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2","dba","dba");

		/* STEP 2 */

		/* STEP 3 */
		/* Select all data in virtuoso */
		Query sparql = QueryFactory.create(
				"SELECT ?anchor "
				+ " WHERE { "
				+ " ?s a <http://sfwcrawler.com/core#Entity>;  "
				+ " <http://sfwcrawler.com/core#inDocument> <http://sfwcrawler.com/core#Document-Cellular_automaton> ; "
				+ " <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> ?anchor. "
				+ " } ");
		/* STEP 4 */
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, set);
		
		ResultSet results = vqe.execSelect();
		int counter = 0;
		while (results.hasNext()) {
			counter ++;
			QuerySolution result = results.nextSolution();
			RDFNode graph = result.get("graph");
			RDFNode p = result.get("anchor");
			System.out.println(graph + " { "  + p + "}");
		}
		System.out.println(counter);
//		set.close();
	}
	
	public List<String> queryDocuments(){
		List<String> documentList = new ArrayList<String>();
//		VirtGraph set = new VirtGraph("http://sfwc.com/computerScience","jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2","dba","dba");
		
		Query query = QueryFactory.create(
					"SELECT ?doc WHERE { "
					+ " ?doc a <http://sfwcrawler.com/core#Document> ."
					+ " } "
				);
		
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, set);
		
		ResultSet rs = vqe.execSelect();
		
		while(rs.hasNext()) {
			QuerySolution qs =  rs.nextSolution();
			
			String doc = qs.getResource("doc").getURI();
			documentList.add(doc);
		}
		
//		set.close();
		return documentList;
	}
	
	public List<String> queryLemmasDocument(String docURI){
		List<String> lemmaList = new ArrayList<String>();
//		VirtGraph set = new VirtGraph("http://sfwc.com/computerScience","jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2","dba","dba");
		
		Query query = QueryFactory.create(
					"SELECT ?lemma WHERE { "
					+ " <"+docURI+"> <http://sfwcrawler.com/core#hasEntity> ?entity."
							+ " ?entity <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> ?lemma ."
					+ " } "
				);
		
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, set);
		
		ResultSet rs = vqe.execSelect();
		
		while(rs.hasNext()) {
			QuerySolution qs =  rs.nextSolution();
			
			String lemma = qs.getLiteral("lemma").getString();
			lemmaList.add(lemma);
		}
		
//		set.close();
		return lemmaList;
	}
	
	public Set<String> queryLemmas(){
		Set<String> lemmaSet = new HashSet<String>();
//		VirtGraph set = new VirtGraph("http://sfwc.com/computerScience","jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2","dba","dba");
		
		Query query = QueryFactory.create(
					"SELECT ?lemma WHERE { "
						+ " ?entity a <http://sfwcrawler.com/core#Entity> ."
						+ " ?entity <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> ?lemma ."
						+ " } "
						);
		
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, set);
		
		ResultSet rs = vqe.execSelect();
		
		while(rs.hasNext()) {
			QuerySolution qs =  rs.nextSolution();
			
			String lemma = qs.getLiteral("lemma").getString();
			lemmaSet.add(lemma);
		}
		
//		set.close();
		return lemmaSet;
	}
	
	public void queryLemmaWordsInRange(double avg, double dev) {
		Query sparql = QueryFactory.create(
				"SELECT  DISTINCT ?idf ?lemma WHERE "
					+	" { "
					+	"    ?s a <http://sfwcrawler.com/core#Entity> . "
					+   "    ?s <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> ?lemma ."
					+   "    ?s <http://www.w3.org/2005/11/its/rdf#taIdentRef> ?url ."
					+	"    ?s <http://sfwcrawler.com/core#idfValue> ?idf . "
					+	" } ORDER BY DESC (?idf) "
				);
		
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, set);
		
		ResultSet results = vqe.execSelect();
		Set<String> lemmasinRangeList = new HashSet<String>();
		while(results.hasNext()) {
			QuerySolution qs = results.nextSolution();
			RDFNode idf = qs.get("idf");
			String lemma = qs.getLiteral("lemma").getString();
			double value = Double.parseDouble(idf.toString());
			if(value < (avg+dev) && avg > (avg-dev)) {
				lemmasinRangeList.add(lemma);
			}
		}
		for(String lemma : lemmasinRangeList) {
			System.out.println(lemma);
		}
	}
	
	public List<String> queryDocumentRelation(String docURI){
		List<String> lemmaList = new ArrayList<String>();
//		VirtGraph set = new VirtGraph("http://sfwc.com/computerScience","jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2","dba","dba");
		
		Query query = QueryFactory.create(
					"SELECT ?subject ?object WHERE { "
						+ " <"+docURI+"> <http://sfwcrawler.com/core#hasRelation> ?rel."
						+ " ?rel <http://sfwcrawler.com/core#subjectLabel> ?subject ."
						+ " ?rel <http://sfwcrawler.com/core#objectLabel> ?object ."
						+ " } "
						);
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, set);
		
		ResultSet rs = vqe.execSelect();
		
		while(rs.hasNext()) {
			QuerySolution qs =  rs.nextSolution();
			
			String subject = qs.getLiteral("subject").getString();
			String object = qs.getLiteral("object").getString();
			lemmaList.add(subject+":"+object);
		}
		
//		set.close();
		return lemmaList;
	}
	
	public int queryCorrelationXandY(String lemmaX, String lemmaY){
		List<String> documentList = new ArrayList<String>();
		
		Query query = QueryFactory.create(
					"SELECT ?doc WHERE { "
						+ " ?doc a <http://sfwcrawler.com/core#Document> ."
						+ " FILTER EXISTS { ?doc <http://sfwcrawler.com/core#hasEntity> ?subject . "
						+ " ?subject <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> \""+lemmaX+"\" . } "
						+ " FILTER EXISTS { ?doc  <http://sfwcrawler.com/core#hasEntity> ?subject . "
						+ " ?subject <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> \""+lemmaY+"\" . }"
						+ " } "
						);
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, set);
		
		ResultSet rs = vqe.execSelect();
		
		while(rs.hasNext()) {
			QuerySolution qs =  rs.nextSolution();
			
			String docURI = qs.getResource("doc").getURI();
			documentList.add(docURI);
		}
		
//		set.close();
		return documentList.size();
	}
	
	public int queryCorrelationXNotY(String lemmaX, String lemmaY){
		List<String> documentList = new ArrayList<String>();
		Query query = QueryFactory.create(
					"SELECT ?doc WHERE { "
						+ " ?doc a <http://sfwcrawler.com/core#Document> ."
						+ " FILTER EXISTS { ?doc <http://sfwcrawler.com/core#hasEntity> ?subject . "
						+ " ?subject <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> \""+lemmaX+"\" . }"
						+ " FILTER NOT EXISTS { ?doc <http://sfwcrawler.com/core#hasEntity> ?subject . "
						+ " ?subject <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> \""+lemmaY+"\" . } "
						+ " } "
						);
		
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, set);
		
		ResultSet rs = vqe.execSelect();
		
		while(rs.hasNext()) {
			QuerySolution qs =  rs.nextSolution();
			
			String docURI = qs.getResource("doc").getURI();
			documentList.add(docURI);
		}
		
//		set.close();
		return documentList.size();
	}
	
	public int queryCorrelationNotXY(String lemmaX, String lemmaY){
		List<String> documentList = new ArrayList<String>();
		Query query = QueryFactory.create(
				"SELECT ?doc WHERE { "
						+ " ?doc a <http://sfwcrawler.com/core#Document> ."
						+ " FILTER NOT EXISTS { ?doc <http://sfwcrawler.com/core#hasEntity> ?subject . "
						+ " ?subject <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> \""+lemmaX+"\" . }"
						+ " FILTER EXISTS { ?doc <http://sfwcrawler.com/core#hasEntity> ?subject . "
						+ " ?subject <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> \""+lemmaY+"\" . } "
						+ " } "
						);
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, set);
		
		ResultSet rs = vqe.execSelect();
		
		while(rs.hasNext()) {
			QuerySolution qs =  rs.nextSolution();
			
			String docURI = qs.getResource("doc").getURI();
			documentList.add(docURI);
		}
		
//		set.close();
		return documentList.size();
	}
	
	public int queryCorrelationNotXNotY(String lemmaX, String lemmaY){
		List<String> documentList = new ArrayList<String>();
		
		Query query = QueryFactory.create(
					"SELECT ?doc WHERE { "
						+ " ?doc a <http://sfwcrawler.com/core#Document> ."
						+ " FILTER NOT EXISTS {  ?doc <http://sfwcrawler.com/core#hasEntity> ?subject . "
						+ " ?subject <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> \""+lemmaY+"\" . } "
						+ " FILTER NOT EXISTS {  ?doc <http://sfwcrawler.com/core#hasEntity> ?subject . "
						+ " ?subject <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> \""+lemmaX+"\" . }  "
						+ " } "
						);
		
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, set);
		
		ResultSet rs = vqe.execSelect();
		
		while(rs.hasNext()) {
			QuerySolution qs =  rs.nextSolution();
			
			String docURI = qs.getResource("doc").getURI();
			documentList.add(docURI);
		}
		
//		set.close();
		return documentList.size();
	}
	
	public List<String> queryNumberLemmaInDocuments(String lemma) {
		List<String> listDocuments = new ArrayList<String>();
		/* STEP 1 */
		

		/* STEP 2 */

		/* STEP 3 */
		/* Select all data in virtuoso */
		if(lemma.contains("http"))
			return listDocuments;
		Query sparql = QueryFactory.create(
				"SELECT DISTINCT ?doc "
				+ " WHERE { "
				+ " ?s a <http://sfwcrawler.com/core#Entity> .  "
				+ " ?s <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> \"" + lemma + "\"."
//				+ " ?s <http://www.w3.org/2005/11/its/rdf#taIdentRef> ?url."
				+ " ?s <http://sfwcrawler.com/core#inDocument> ?doc . "
				+ " } ");
		/* STEP 4 */
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, set);
		
		ResultSet results = vqe.execSelect();
		while (results.hasNext()) {
			QuerySolution result = results.nextSolution();
//			RDFNode graph = result.get("graph");
			RDFNode p = result.get("doc");
			listDocuments.add(p.toString());
		}
//		set.close();
		return listDocuments;
	}
	
	public Map<Double,String> queryGlobalTFIDF() throws IOException{
		Map<Double, String> mapTFIDF= new HashMap<Double, String>(); 
//		VirtGraph set = new VirtGraph("http://sfwc.com/computerScience","jdbc:virtuoso://localhost:1111/chartset=UTF-8/log_enable=2", "dba","dba");
		
		Query sparql = QueryFactory.create(
				"SELECT DISTINCT ?lemma ?tfidf ?url WHERE "
					+	" { "
					+	"    ?s a <http://sfwcrawler.com/core#Entity> . "
					+   "    ?s <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> ?lemma ."
					+   "    ?s <http://www.w3.org/2005/11/its/rdf#taIdentRef> ?url."
					+	"    ?s <http://sfwcrawler.com/core#tfIdfValue> ?tfidf . "
					+	" } ORDER BY DESC (?tfidf) "
				);
		
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, set);
		
		ResultSet results = vqe.execSelect();
		int counter = 0;
		while(results.hasNext()) {
			QuerySolution qs = results.nextSolution();
			String lemma = qs.getLiteral("lemma").getString();
			String url = qs.getResource("url").getURI();
			RDFNode tfidf = qs.get("tfidf");
			mapTFIDF.put(Double.parseDouble(tfidf.toString()),lemma);
			
			if(counter < 100)
				System.out.println(lemma + "---" + tfidf + " ---- " + url);
			counter++;
		}
//		Collections.sort(tfidfList, Collections.reverseOrder());
		
		Map<Double, String> reverseSortedMap = new TreeMap<Double, String>(Collections.reverseOrder());
		
		reverseSortedMap.putAll(mapTFIDF);
//		int counter2 = 0;
//		System.out.println(reverseSortedMap.size());
//		Set<String> lemmaSet = new HashSet<String>();
//		for(Map.Entry<Double, String> entry : reverseSortedMap.entrySet()) {
//			if(counter >= reverseSortedMap.size()-1000 && !lemmaSet.contains(entry.getValue())) {
//				System.out.println(counter + " --- " + entry.getKey() + " --- " + entry.getValue());
//				lemmaSet.add(entry.getValue());
//				counter2++;
//			}
//			
//			counter++;
//			if(counter2 == 100)
//				break;
//			
//			
//		}
		
		
		System.out.println(reverseSortedMap.size()/2);
//		MapUtils.debugPrint(System.out, "TFIDF", reverseSortedMap);
		return reverseSortedMap;
	}
	
	public Double queryAvgTFIDF(){
		Query sparql = QueryFactory.create(
				"SELECT ?tfidf WHERE "
					+	" { "
					+	"    ?s a <http://sfwcrawler.com/core#Entity> . "
					+   "    ?s <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> ?lemma ."
					+   "    ?s <http://www.w3.org/2005/11/its/rdf#taIdentRef> ?url ."
					+	"    ?s <http://sfwcrawler.com/core#tfIdfValue> ?tfidf . "
					+	" } ORDER BY DESC (?tfidf) "
				);
		
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, set);
		
		ResultSet results = vqe.execSelect();
		int counter = 0;
		Double avgTfidf = 0.0d;
		while(results.hasNext()) {
			counter++;
			QuerySolution qs = results.nextSolution();
			RDFNode tfidf = qs.get("tfidf");
			avgTfidf += Double.parseDouble(tfidf.toString());
		}
		return avgTfidf/counter;
	}
	
	public Double queryAvgIDF(){
		Query sparql = QueryFactory.create(
				"SELECT  DISTINCT ?idf WHERE "
					+	" { "
					+	"    ?s a <http://sfwcrawler.com/core#Entity> . "
					+   "    ?s <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> ?lemma ."
					+   "    ?s <http://www.w3.org/2005/11/its/rdf#taIdentRef> ?url ."
					+	"    ?s <http://sfwcrawler.com/core#idfValue> ?idf . "
					+	" } ORDER BY DESC (?idf) "
				);
		
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, set);
		
		ResultSet results = vqe.execSelect();
		int counter = 0;
		Double avgIdf = 0.0d;
		while(results.hasNext()) {
			
			QuerySolution qs = results.nextSolution();
			RDFNode tfidf = qs.get("idf");
			double value = Double.parseDouble(tfidf.toString());
//			if(value < 1.5) {
				avgIdf += value; 
				counter++;
//			}
		}
		return avgIdf/counter;
	}
	
	public Double queryMedianTFIDF(){
		List<Double> idfList = new ArrayList<Double>();
		Query sparql = QueryFactory.create(
				"SELECT ?tfidf WHERE "
					+	" { "
					+	"    ?s a <http://sfwcrawler.com/core#Entity> . "
					+   "    ?s <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> ?lemma ."
					+	"    ?s <http://sfwcrawler.com/core#tfIdfValue> ?tfidf . "
					+	" } ORDER BY DESC (?tfidf) "
				);
		
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, set);
		
		ResultSet results = vqe.execSelect();
		int counter = 0;
		while(results.hasNext()) {
			QuerySolution qs = results.nextSolution();
			RDFNode tfidf = qs.get("tfidf");
			double value = Double.parseDouble(tfidf.toString());
			if(value < 1.5) {
				idfList.add(value);
				counter++;
			}
		}
		int medianIndex = counter/2;
		Collections.sort(idfList, Collections.reverseOrder());
		return idfList.get(medianIndex);
	}
	
	public List<Double> queryMedianIDF(){
		List<Double> idfList = new ArrayList<Double>();
		Query sparql = QueryFactory.create(
				"SELECT  DISTINCT ?idf WHERE "
					+	" { "
					+	"    ?s a <http://sfwcrawler.com/core#Entity> . "
					+   "    ?s <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> ?lemma ."
					+   "    ?s <http://www.w3.org/2005/11/its/rdf#taIdentRef> ?url ."
					+	"    ?s <http://sfwcrawler.com/core#idfValue> ?idf . "
					+	" } ORDER BY DESC (?idf) "
				);
		
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, set);
		
		ResultSet results = vqe.execSelect();
		int counter = 0;
		while(results.hasNext()) {
			
			QuerySolution qs = results.nextSolution();
			RDFNode idf = qs.get("idf");
//			String url = qs.getResource("url").getURI();
//			if(url.isEmpty())
//				continue;
			double value = Double.parseDouble(idf.toString());
//			if(value < 1.5) {
				idfList.add(value);
				counter++;
//				queryWordsIDF(idf.toString());
//			}
//			System.out.println(counter + " - " + value + " : " + queryWordsIDF(idf.toString()));
			
		}
		int medianIndex = counter/2;
		Collections.sort(idfList, Collections.reverseOrder());
		System.out.println(medianIndex + "  " + idfList.get(medianIndex));
//		return idfList.get(medianIndex);
		return idfList;
	}
	
	public int queryWordsIDF(String idfValue){
		Query sparql = QueryFactory.create(
				"SELECT DISTINCT ?lemma  WHERE "
					+	" { "
					+	"    ?s a <http://sfwcrawler.com/core#Entity> . "
					+   "    ?s <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> ?lemma ."
//					+   "    ?s <http://www.w3.org/2005/11/its/rdf#taIdentRef> ?url ."
					+	"    ?s <http://sfwcrawler.com/core#idfValue> \""+idfValue+"\" . "
					+	" } "
				);
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, set);
		
		ResultSet results = vqe.execSelect();
		int counter = 0;
		Set<String> setString = new HashSet<String>();
		while(results.hasNext()) {
			QuerySolution qs = results.nextSolution();
			RDFNode total = qs.get("lemma");
//			String url = qs.getResource("url").getURI();
//			if(idfValue.equals("3.0143552929770703")) 
//				if(counter < 100)
//					System.out.println(total);
//			if(url.isEmpty())
//				continue;
//			if(Double.parseDouble(idfValue) < (3.4748d + 1.7182d)
//					&& Double.parseDouble(idfValue) > (3.4748d - 1.7182d)) {
//				if(!setString.contains(total.toString())) {
//					System.out.println(total+ " ----- " + idfValue);
//					setString.add(total.toString());
//				}
//			}
//					
			counter++;
		}
		return counter;
	}
	
	
	
//	public Set<String> queryMedianStringIDF(){
//		Query sparql = QueryFactory.create(
//				"SELECT DISTINCT ?idf WHERE "
//					+	" { "
//					+	"    ?s a <http://sfwcrawler.com/core#Entity> . "
//					+   "    ?s <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> ?lemma ."
////					+   "    ?s <http://www.w3.org/2005/11/its/rdf#taIdentRef> ?url ."
//					+	"    ?s <http://sfwcrawler.com/core#idfValue> ?idf . "
//					+	" } ORDER BY DESC (?idf) "
//				);
//		
//		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, set);
//		
//		ResultSet results = vqe.execSelect();
//		Set<String> medianList = new HashSet<String>();
//		while(results.hasNext()) {
//			QuerySolution qs = results.nextSolution();
//			RDFNode idf = qs.get("idf");
//			medianList.addAll(queryWordsSetIDF(idf.toString()));
//		}
//		return medianList;
//	}
//	
//	public Set<String> queryWordsSetIDF(String idfValue){
//		Query sparql = QueryFactory.create(
//				"SELECT DISTINCT ?lemma ?url WHERE "
//					+	" { "
//					+	"    ?s a <http://sfwcrawler.com/core#Entity> . "
//					+   "    ?s <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> ?lemma ."
//					+   "    ?s <http://www.w3.org/2005/11/its/rdf#taIdentRef> ?url ."
//					+	"    ?s <http://sfwcrawler.com/core#idfValue> \""+idfValue+"\" . "
//					+	" } "
//				);
//		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, set);
//		
//		ResultSet results = vqe.execSelect();
//		Set<String> setString = new HashSet<String>();
//		while(results.hasNext()) {
//			QuerySolution qs = results.nextSolution();
//			RDFNode total = qs.get("lemma");
//			if(Double.parseDouble(idfValue) < (3.4748d + 1.7182d)
//					&& Double.parseDouble(idfValue) > (3.4748d - 1.7182d)) {
//				if(!setString.contains(total.toString())) {
////					System.out.println(total+ " ----- " + idfValue);
//					setString.add(total.toString());
//				}
//			}
//		}
//		return setString;
//	}
	
	public List<Double> queryTfidfList(){
		List<Double> idfList = new ArrayList<Double>();
		Query sparql = QueryFactory.create(
				"SELECT ?tfidf WHERE "
					+	" { "
					+	"    ?s a <http://sfwcrawler.com/core#Entity> . "
					+   "    ?s <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf> ?lemma ."
					+   "    ?s <http://www.w3.org/2005/11/its/rdf#taIdentRef> ?url ."
					+	"    ?s <http://sfwcrawler.com/core#tfIdfValue> ?tfidf . "
					+	" } ORDER BY DESC (?tfidf) "
				);
		
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, set);
		
		ResultSet results = vqe.execSelect();
		while(results.hasNext()) {
			QuerySolution qs = results.nextSolution();
			RDFNode tfidf = qs.get("tfidf");
			double value = Double.parseDouble(tfidf.toString());
//			if(value < 1.5) {
				idfList.add(value);
//			}
		}
		Collections.sort(idfList, Collections.reverseOrder());
		return idfList;
	}

}
