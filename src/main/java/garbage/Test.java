package garbage;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.client.MongoIterable;

import db.MongoDBUtils;

public class Test {

	private static final Logger LOGGER = Logger.getLogger(Test.class);
	
	public static void main(String[] args) {
		Test t = new Test();
//		t.domains();
		t.enrichedNouns();
	}
	
	public void counterSomething() {
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
	
	public void domains() {
		MongoDBUtils u = new MongoDBUtils("Experiments");
		
		MongoIterable<String> names = u.getCollectionsName();
		Set<String> domainsSet = new HashSet<String>();
		Map<String,Integer> domainsHash = new HashMap<String,Integer>();
		for(String name : names) {
			MongoDBUtils util = new MongoDBUtils("Experiments", name);
			List<String> ids = util.getDocsId();
			for(String url:ids) {
				try {
					String domain = getDomain(url);
					domainsSet.add(domain);
					if(domainsHash.containsKey(domain)) {
						Integer temp = domainsHash.get(domain) + 1;
						domainsHash.put(domain, temp);
					}else {
						domainsHash.put(domain, 1);
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		for(String domain : domainsSet) {
			System.out.println(domain);
		}
		System.out.println(domainsSet.size());
		MapUtils.verbosePrint(System.out, "Domain", domainsHash);
	}
	
	public String getDomain(String id) throws URISyntaxException, UnsupportedEncodingException, MalformedURLException {
		URL url = new URL(id);
		URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
	    String domain = uri.getHost();
	    if(domain != null)
	    	return domain.startsWith("www.") ? domain.substring(4) : domain;
    	else
    		return "";
	}
	
	public void enrichedNouns() {
		MongoDBUtils u = new MongoDBUtils("SFWC_V2","computerScience");
		int en = 0;
		int enWu = 0;
		List<String> ids = u.getDocsId();
		for(String id:ids) {
			en += u.getDocLemmas(id).size();
			enWu += u.getDocLemmasWithURL(id).size();
		}
		System.out.println(en + " -- avg. en: " + en/(double)ids.size() + "\t"+enWu+"\t ---avg. enWu: " + enWu/(double)ids.size());
	}

}
