package com.jenkov.crawler.util;


/**
 */
public class SameWebsiteOnlyFilter implements IUrlFilter {

    protected String domainUrl = null;

    public SameWebsiteOnlyFilter(String domainUrl) {
        this.domainUrl = domainUrl;
    }

    @Override
    public boolean include(String url) {
    	int firstSlash = this.domainUrl.indexOf("/");
    	if(firstSlash == -1)
    		firstSlash = this.domainUrl.length();
        return url.startsWith(this.domainUrl.substring(0,firstSlash));
    }
}
