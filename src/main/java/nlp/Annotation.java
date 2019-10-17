package nlp;

public class Annotation {
	
	private String lemma;
	private String posTag;
	private String ner;
	private String uri;
	private int begin;
	private int end;
	private int itf;
	private int idf;
	
	public String getLemma() {
		return lemma;
	}
	public void setLemma(String lemma) {
		this.lemma = lemma;
	}
	public String getPosTag() {
		return posTag;
	}
	public void setPosTag(String posTag) {
		this.posTag = posTag;
	}
	public String getNer() {
		return ner;
	}
	public void setNer(String ner) {
		this.ner = ner;
	}
	public int getBegin() {
		return begin;
	}
	public void setBegin(int begin) {
		this.begin = begin;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	public int getItf() {
		return itf;
	}
	public void setItf(int itf) {
		this.itf = itf;
	}
	public int getIdf() {
		return idf;
	}
	public void setIdf(int idf) {
		this.idf = idf;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}

}
