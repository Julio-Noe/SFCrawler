package crawler;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.CustomsearchRequestInitializer;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;


public class RetrieveGoogle {
	
	String googleJSONAPI = "AIzaSyCSj2zYCyEJTlSD3rM97ErYs6LndwC-BwM";

	static private Logger logger = Logger.getLogger(RetrieveGoogle.class);
	
	public static void main(String[] args) throws IOException, GeneralSecurityException {
		
//		RetrieveGoogle rg = new RetrieveGoogle();
//		rg.queryTopicToGoogle("computer science", 2);
		
		//https://developers.google.com/custom-search/v1/overview
		String searchQuery = "\"computer science\" -fileType:html"; //The query to search
//	    String cx = "002845322276752338984:vxqzfa86nqc"; //Your search engine
		String cx = "010597449755694553850:pst7fr7qkto";

	    //Instance Customsearch
	    Customsearch cs = new Customsearch.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), null) 
	                   .setApplicationName("MyApplication")
	                   .setGoogleClientRequestInitializer(new CustomsearchRequestInitializer("AIzaSyCSj2zYCyEJTlSD3rM97ErYs6LndwC-BwM")) 
	                   .build();

	    //Set search parameter
	    Customsearch.Cse.List list = cs.cse().list("").setExactTerms("Computer Science").setFileType("html").setStart(1l).setCx(cx); 
	    //Execute search
	    Search result = list.execute();
	    if (result.getItems()!=null){
	        for (Result ri : result.getItems()) {
	            //Get title, link, body etc. from search
	            System.out.println(ri.getTitle() + ", " + ri.getLink());
	        }
	    }
	    System.out.println("second page: ");
	    list = cs.cse().list("").setExactTerms("Computer Science").setFileType("html").setStart(11l).setCx(cx);
	    result = list.execute();
	    if (result.getItems()!=null){
	        for (Result ri : result.getItems()) {
	            //Get title, link, body etc. from search
	            System.out.println(ri.getTitle() + ", " + ri.getLink());
	        }
	    }
	    
//		rg.retreiveGoogleSeeds();
		// rg.retreiveBingSeeds();

	}
	
	public List<String> queryTopicToGoogle(String topic, int numberGooglePagesResults) {
//		String searchQuery = "\""+topic+"\" -fileType:html"; // The query to search
		String cx = "010597449755694553850:pst7fr7qkto";//Your search engine
		List<String> googleResultsList = new ArrayList<String>();

		// Instance Customsearch
		Customsearch cs = null;
		Customsearch.Cse.List list = null;
		Search result = null;
		try {
			for(int i = 0; i < numberGooglePagesResults; i++) {
				System.out.println("Google results -- page " + (i+1));
				cs = new Customsearch.Builder(GoogleNetHttpTransport.newTrustedTransport(),
						JacksonFactory.getDefaultInstance(), null).setApplicationName("MyApplication")
								.setGoogleClientRequestInitializer(
										new CustomsearchRequestInitializer("AIzaSyCSj2zYCyEJTlSD3rM97ErYs6LndwC-BwM"))
								.build();
				long start = 1l;
				if(googleResultsList.size() > 0)
					start = Long.parseLong(String.valueOf(googleResultsList.size()));
				
				list = cs.cse()
						.list("")
						.setExactTerms(topic)
						.setFileType("html")
						.setStart(start)
						.setCx(cx);
				
				result = list.execute();
				
				if (result.getItems() != null) {
					for (Result ri : result.getItems()) {
						// Get title, link, body etc. from search
						System.out.println(ri.getTitle() + ", " + ri.getLink());
						googleResultsList.add(ri.getLink());
					}
				}
			}
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return googleResultsList;

	}
	
	///5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6
	public List<String> retreiveGoogleSeeds() throws IOException {
		Document linksDoc = null;
		linksDoc = Jsoup.connect("https://www.google.com/search?q=computer+science")
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
