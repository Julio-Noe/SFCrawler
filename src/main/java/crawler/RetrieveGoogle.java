package crawler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class RetrieveGoogle {

	static private Logger logger = Logger.getLogger(RetrieveGoogle.class);
	
	public static void main(String[] args) throws IOException {
		RetrieveGoogle rg = new RetrieveGoogle();
		
		rg.retreiveGoogleSeeds();
		//rg.retreiveBingSeeds();

	}
	///5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6
	public List<String> retreiveGoogleSeeds() throws IOException {
		Document linksDoc = null;
		linksDoc = Jsoup.connect("https://www.google.com/search?q=define%3Acomputer&as_qdr=y&lr=lang_en&num=49")
				.userAgent("Mozilla")
			    .get();
		//Elements titles = linksDoc.select("h3.r > a");
		Elements titles = linksDoc.select(".r > a");
		Elements elems = linksDoc.getElementsByAttribute("href");
		List<String> seeds = new ArrayList<String>();
		for (Element e : elems) {
			String cleanString = e.attr("href");

			if(cleanString.contains("/url?q=")
					&&cleanString.contains("http")
					&& !containsURI(cleanString)) {
				cleanString = cleanString.split("q=")[1].split("&")[0];
				System.out.println("Clean String = " + cleanString);
				seeds.add(cleanString);
			}
			if(seeds.size() == 20)
				break;
			retrieveBody(cleanString);
			//System.out.println("text" + ": " + e.attr("href"));
		}
		return seeds;

	}
	
	public List<String> retreiveBingSeeds() throws IOException {
		Document linksDoc = null;
		//https://docs.microsoft.com/en-us/rest/api/cognitiveservices/bing-web-api-v5-reference#query-parameters
		linksDoc = Jsoup.connect("https://www.bing.com/search?format=css&q=computer&count=50&offset=0&&setLang=EN&cc=en-us&freshness=Month")
				.userAgent("Mozilla")
			    .get();
		//Elements titles = linksDoc.select("h3.r > a");
		Elements titles = linksDoc.select(".r > a");
		Elements elems = linksDoc.getElementsByAttribute("href");
		List<String> seeds = new ArrayList<String>();
		for (Element e : elems) {
			String cleanString = e.attr("href");
			if(cleanString.endsWith("/")) {
				continue;
			}
				
			if(cleanString.contains("http")
					&& !containsURI(cleanString)) {
				System.out.println("Clean String = " + cleanString);
				seeds.add(cleanString);
				retrieveBody(cleanString);
			}
			if(seeds.size() == 20)
				break;
			
			//System.out.println("text" + ": " + e.attr("href"));
		}
		return seeds;

	}
	int counterPath = 0;
	public boolean retrieveBody(String url) throws IOException  {
		Document doc = null;
		String path = "/home/noe/Documents/Crawler/Dataset/Computer_Science/Google/page_";
		
		try {
			doc = Jsoup.connect(url)
					.userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
					.get();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		if(doc.body().hasText()) {
			//System.out.println(doc.body().text());
			logger.info("Writting file in path: " + path+counterPath +".txt");
			
			FileUtils.writeStringToFile(new File(path+ (counterPath++) +".txt"), doc.body().wholeText(),"UTF-8");
		}
		return true;
	}
	
	public boolean containsURI(String uri) {
		if(uri.contains("google")
				|| uri.contains("youtube")
				|| uri.contains("bestbuy")
				|| uri.contains("amazon")
				|| uri.contains("flipkart")
				|| uri.contains("bjs.com")
				|| uri.contains("cnet.com")
				|| uri.contains("hsn.com")
				|| uri.contains("adobe.com")
				|| uri.contains("lenovo.com")
				|| uri.contains("adorama.com")
				|| uri.contains("ielts")
				|| uri.contains("bing.com")
				|| uri.contains("translator")
				|| uri.contains("freecomputerbooks")
				|| uri.contains("computrabajo")
				|| uri.contains("microsoft.com")
				|| uri.contains("itstillworks")
				|| uri.contains("dell.com")
				|| uri.contains("java2s"))
			return true;
		else
			return false;
	}
}
