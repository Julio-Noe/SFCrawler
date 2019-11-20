package crawler;

public class CrawledPage {
	
	public CrawledPage(String title, String url, String parentPage, int depthLevel) {
		this.title = title;
		this.url = url;
		this.parentPage = parentPage;
		this.depthLevel = depthLevel;
		
	}
	
	String title;
	String url;
	String parentPage;
	int depthLevel;
	
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getParentPage() {
		return parentPage;
	}
	public void setParentPage(String parentPage) {
		this.parentPage = parentPage;
	}
	public int getDepthLevel() {
		return depthLevel;
	}
	public void setDepthLevel(int depthLevel) {
		this.depthLevel = depthLevel;
	}
	

}
