package com.jenkov.crawler.st.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jenkov.crawler.util.IUrlFilter;
import com.jenkov.crawler.util.UrlNormalizer;
import com.mongodb.BasicDBObject;

import crawler.CrawledPage;
import crawler.SFWC;
import db.MongoDBUtils;

/**
 */
public class Crawler {

    protected IUrlFilter urlFilter     = null;

    protected List<String> urlsToCrawl = new ArrayList<String>();
    protected List<CrawledPage> craledList = new ArrayList<CrawledPage>();
    protected static Map<String, Integer> mapToCrawl = new HashMap<String, Integer>();
    protected Set<String>  crawledUrls = new HashSet<String>();

    protected IPageProcessor pageProcessor = null;

    protected static List<String> acceptedURIList = new ArrayList<String>();
    protected static List<String> rejectedURIList = new ArrayList<String>();
    
    protected double top = 0.0;
    protected double down = 0.0d;
    protected double graphAvg = 0.0d;
    protected double strDev = 0.0d;
    
    protected String mongoDatabase = "";
    protected String mongoCollection = "";
    
    protected String collection = "";
    
    protected int counter = 0;

    public Crawler() {
    }

    public void setUrlFilter(IUrlFilter urlFilter) {
        this.urlFilter = urlFilter;
    }

    public void setPageProcessor(IPageProcessor pageProcessor) {
        this.pageProcessor = pageProcessor;
    }

//    public void addUrl(String url) {
//        this.urlsToCrawl.add(url);
//    }
    public void addUrl(String url, String parent) {
    	this.urlsToCrawl.add(url);
    	if(mapToCrawl.isEmpty()){
    		mapToCrawl.put(url, 0);
    	}else {
    		if(mapToCrawl.containsKey(parent) && !url.equals(parent) && !mapToCrawl.containsKey(url)) {
    			int parentLevel = mapToCrawl.get(parent);
    			parentLevel++;
    			mapToCrawl.put(url, parentLevel);
    		}
    	}
    }

    public void crawl() {
    	
       
    	long startTime = System.currentTimeMillis();

        while(this.urlsToCrawl.size() > 0 && counter <= 50) {
        	
        	System.out.println("COUNTER " + counter);
            String nextUrl = this.urlsToCrawl.remove(0);
           
            String crawlMessage = shouldCrawlUrl(nextUrl);
            if (!crawlMessage.equals("true")) {
//            	mapToCrawl.remove(nextUrl);
            	this.crawledUrls.add(nextUrl);
            	org.bson.Document mainObj = new org.bson.Document();
            	mainObj.put("_id", nextUrl);
            	mainObj.put("crawled", false);
            	mainObj.put("error", true);
            	mainObj.put("errorMessage", crawlMessage);
            	mainObj.put("level",0);
            	
            	MongoDBUtils utils = new MongoDBUtils(mongoDatabase, mongoCollection);
            	utils.insertDocument(mainObj);
            	continue; // skip this URL.
            	
            }
            this.crawledUrls.add(nextUrl);
            counter++;

            

            try {
                System.out.println(nextUrl);
                CrawlJob crawlJob = new CrawlJob(nextUrl, this.pageProcessor);

                crawlJob.addPageProcessor(new IPageProcessor() {
                    @Override
                    public void process(String url, Document doc) {
                        Elements elements = doc.select("a");
                        double avg = 0.0d;
                        org.bson.Document mainObj = null; 
                        System.out.println(doc.body().text());
                        SFWC semantic = new SFWC(collection);
                        try {
							mainObj = semantic.sfwc(doc.body().text(), url, top, down);
							if(mainObj != null) {
								avg = mainObj.getDouble("average");
								mainObj.put("graphIdfCeil", top);
								mainObj.put("graphFloor", down);
								mainObj.put("graphAverage", graphAvg);
								mainObj.put("graphStandardDev", strDev);
							}else {
								avg = 0.0d;
								mainObj = new org.bson.Document();
								mainObj.put("_id", url);
								mainObj.put("crawled", true);
								mainObj.put("content", doc.body().text());
								mainObj.put("EN", new ArrayList<BasicDBObject>());
								mainObj.put("wordsInRange", new ArrayList<BasicDBObject>());
								mainObj.put("error", true);
								mainObj.put("errorMessage","Sentence list greater than 100 -- too much time to process");
								mainObj.put("average", 0.0d);
								mainObj.put("median", 0.0d);
								mainObj.put("graphIdfCeil", top);
								mainObj.put("graphFloor", down);
								mainObj.put("graphAverage", graphAvg);
								mainObj.put("graphStandardDev", strDev);
							}
								
						} catch (IOException | InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        
                        System.out.println("top: " + top + " --- Down : " + down);
                        if (avg > down && avg < top) {	
							System.out.println("======¡¡¡¡PASS!!!!======");
							acceptedURIList.add(avg + ";" + doc.title() + ";" + url);
							String baseUrl = url;
							for (Element element : elements) {
								String linkUrl = element.attr("href");
								String normalizedUrl = UrlNormalizer.normalize(linkUrl, baseUrl);
								addUrl(normalizedUrl, url);
							}
							mainObj.put("decision", "Accepted");
							mainObj.put("error", false);
							mainObj.put("errorMessage", "");
							mainObj.put("level", mapToCrawl.get(url));
							
						}else {
							rejectedURIList.add(avg + ";" + doc.title() + ";" + url);
							mainObj.put("decision", "Rejected");
							mainObj.put("error", false);
							mainObj.put("errorMessage", "");
							mainObj.put("level", mapToCrawl.get(url));
						}
                        MongoDBUtils utils = new MongoDBUtils(mongoDatabase, mongoCollection);
                    	utils.insertDocument(mainObj);
                    }
                   
                });

                crawlJob.crawl();
            } catch (Exception e) {
                System.out.println("Error crawling URL: " + nextUrl);
                org.bson.Document mainObj = new org.bson.Document();
            	mainObj.put("_id", nextUrl);
            	mainObj.put("crawled", false);
            	mainObj.put("error", true);
            	mainObj.put("errorMessage", "Error crawling URL: " + nextUrl);
            	mainObj.put("level",0);
            	
            	MongoDBUtils utils = new MongoDBUtils(mongoDatabase, mongoCollection);
            	utils.insertDocument(mainObj);
                e.printStackTrace();
            }

        }
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;

//        System.out.println("URL's crawled: " + this.crawledUrls.size() + " in " + TimeUnit.MILLISECONDS.toSeconds(totalTime) + " seconds (avg: " + totalTime / this.crawledUrls.size() + ")");
        System.out.println("URL's crawled: " + this.crawledUrls.size() + " in " + TimeUnit.MILLISECONDS.toSeconds(totalTime) + " seconds ");
    }

    private String shouldCrawlUrl(String nextUrl) {
//        if(this.urlFilter != null && !this.urlFilter.include(nextUrl)){
//            return false;
//        }
        if(this.crawledUrls.contains(nextUrl)) { return "The url was already processed"; }
        if(nextUrl.startsWith("javascript:"))  { return "Starts with javascript"; }
        if(nextUrl.contains("mailto:"))        { return "Contains mailto:"; }
        if(nextUrl.contains(".pdf"))           { return "Contains .pdf"; }
        if(nextUrl.startsWith("#"))            { return "Starts with #"; }
        if(nextUrl.endsWith(".swf"))           { return "Ends with .swf"; }
        if(nextUrl.endsWith(".pdf"))           { return "Ends with .pdf"; }
        if(nextUrl.endsWith(".png"))           { return "Ends with .png"; }
        if(nextUrl.endsWith(".gif"))           { return "Ends with .gif"; }
        if(nextUrl.endsWith(".jpg"))           { return "Ends with .jpg"; }
        if(nextUrl.endsWith(".jpeg"))          { return "Ends with .jpeg"; }

        return "true";
    }

	public double getTop() {
		return top;
	}

	public void setTop(double top) {
		this.top = top;
	}

	public double getDown() {
		return down;
	}

	public void setDown(double down) {
		this.down = down;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public String getMongoDatabase() {
		return mongoDatabase;
	}

	public void setMongoDatabase(String mongoDatabase) {
		this.mongoDatabase = mongoDatabase;
	}

	public String getMongoCollection() {
		return mongoCollection;
	}

	public void setMongoCollection(String mongoCollection) {
		this.mongoCollection = mongoCollection;
	}

	public double getGraphAvg() {
		return graphAvg;
	}

	public void setGraphAvg(double graphAvg) {
		this.graphAvg = graphAvg;
	}

	public double getStrDev() {
		return strDev;
	}

	public void setStrDev(double strDev) {
		this.strDev = strDev;
	}


}
