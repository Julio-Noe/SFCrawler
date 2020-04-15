package com.jenkov.crawler.st.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.jenkov.crawler.util.SameWebsiteOnlyFilter;

import crawler.RetrieveGoogle;
import sparql.QueryTopic;

/**
 * This class is an example of how to use the Crawler class. You should
 * not expect to use this class as it is. Use the Crawler class directly
 * from your own code.
 */
public class CrawlerMain {

    public static void main(String[] args) throws IOException {

        /*if(args.length < 1) {
            System.err.println("Provide a URL as argument to the CrawlerMain class.");
            return;
        }*/

        //String url = args[0];
        //String url ="http://tutorials.jenkov.com";
        //String url ="http://martinfowler.com";
//        String url ="http://www.vogella.com";
//    	String url = "https://en.wikipedia.org/wiki/Computer_science";
//    	String url =  "https://turing.iimas.unam.mx/index.php?lan=en";
//    	String url = "https://www.bbc.com/news/election-2019-50311003";

//    	String url = "https://www.bbc.com/news/technology-50300842"; //video games
//    	String url = "https://www.bbc.co.uk/bitesize/topics/zkcqn39/articles/zxgdwmn";
//    	String url ="https://www.bbc.com/sport/disability-sport/50167594";
//    	String url = "https://www.nytimes.com/2019/01/24/technology/computer-science-courses-college.html";
//    	String url = "https://medlineplus.gov/diabetes.html";
//    	String url = "https://www.rose-hulman.edu/academics/course-catalog/current/programs/Computer%20Science/index.html";
    	List<String> listUrls = FileUtils.readLines(new File("politics_experiment_2_urls.txt"));
    	String url = "http://awards.cs.brown.edu/2019/11/11/brown-cs-alums-dylan-field-and-evan-wallace-named-inc-2019-rising-stars/";
    	String collection = "politics"; //Virtuoso graph
    	String mongoDatabase = "politicsExp2";
    	String mongoCollection = "politics1";
    	QueryTopic qt = new QueryTopic(collection);
    	RetrieveGoogle rg = new RetrieveGoogle();
    	double top = qt.queryAvgIDF();
    	double devStandard = qt.calculateStandardDeviation(top);
    	List<String> googleResultsList = new ArrayList<String>();
    	
    	for(int i = 36 ; i < listUrls.size(); i++) {
    		url = listUrls.get(i);
    		mongoCollection = "politics"+i;
    		System.out.println(i + "- Processing url: " + listUrls.get(i) + "\nCollection: " + mongoCollection);
    		Crawler crawler  = new Crawler();
            crawler.setUrlFilter(new SameWebsiteOnlyFilter(url));
            crawler.setPageProcessor(null); // set an IPageProcessor instance here.
            crawler.addUrl(url,"");
            crawler.setTop(top+devStandard);
            crawler.setDown(top-devStandard);
            crawler.setCollection(collection);
            crawler.setGraphAvg(top);
            crawler.setStrDev(devStandard);
            crawler.setMongoCollection(mongoCollection);
            crawler.setMongoDatabase(mongoDatabase);
            crawler.crawl();
            
            System.out.println("\n\nList of accepted URIs: ");
            for(String result : Crawler.acceptedURIList) {
            	System.out.println(result);
            }
            System.out.println("\n\nList of rejected URIs: ");
            for(String result : Crawler.rejectedURIList) {
            	System.out.println(result);
            }
    	}
    	System.exit(0);
//    	googleResultsList = rg.queryTopicToGoogle("computer science", 4);
    	if(!googleResultsList.isEmpty()) {
    		for(String urlPage : googleResultsList) {
    			Crawler crawler  = new Crawler();
                crawler.setUrlFilter(new SameWebsiteOnlyFilter(url));
                crawler.setPageProcessor(null); // set an IPageProcessor instance here.
        		crawler.addUrl(urlPage,"");
                crawler.setTop(top+devStandard);
                crawler.setDown(top-devStandard);
                crawler.setCollection(collection);
                crawler.setGraphAvg(top);
                crawler.setStrDev(devStandard);
                crawler.setMongoCollection(mongoCollection);
                crawler.setMongoDatabase(mongoDatabase);
                crawler.crawl();
    		}
    		
    	} else {
    		Crawler crawler  = new Crawler();
            crawler.setUrlFilter(new SameWebsiteOnlyFilter(url));
            crawler.setPageProcessor(null); // set an IPageProcessor instance here.
            crawler.addUrl(url,"");
            crawler.setTop(top+devStandard);
            crawler.setDown(top-devStandard);
            crawler.setCollection(collection);
            crawler.setGraphAvg(top);
            crawler.setStrDev(devStandard);
            crawler.setMongoCollection(mongoCollection);
            crawler.setMongoDatabase(mongoDatabase);
            crawler.crawl();
    	}
    	
        
        
        System.out.println("\n\nList of accepted URIs: ");
        for(String result : Crawler.acceptedURIList) {
        	System.out.println(result);
        }
        System.out.println("\n\nList of rejected URIs: ");
        for(String result : Crawler.rejectedURIList) {
        	System.out.println(result);
        }
        
//        System.out.println("\n\nMap crawled pages");
//        for(Map.Entry<String, Integer> entry : Crawler.mapToCrawl.entrySet()) {
//        	System.out.println("value: " + entry.getValue() + "\tkey: " + entry.getKey());
//        }
    }
}
