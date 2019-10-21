package nlp;

public class Annotation {
	
	private String lemma;
	private String posTag;
	private String ner;
	private String uri = "";
	private int begin;
	private int end;
	private int tf;
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
	public int gettf() {
		return tf;
	}
	public void settf(int tf) {
		this.tf = tf;
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
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + begin;
		result = prime * result + end;
		result = prime * result + idf;
		result = prime * result + tf;
		result = prime * result + ((lemma == null) ? 0 : lemma.hashCode());
		result = prime * result + ((ner == null) ? 0 : ner.hashCode());
		result = prime * result + ((posTag == null) ? 0 : posTag.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Annotation other = (Annotation) obj;
		if (begin != other.begin)
			return false;
		if (end != other.end)
			return false;
		if (idf != other.idf)
			return false;
		if (tf != other.tf)
			return false;
		if (lemma == null) {
			if (other.lemma != null)
				return false;
		} else if (!lemma.equals(other.lemma))
			return false;
		if (ner == null) {
			if (other.ner != null)
				return false;
		} else if (!ner.equals(other.ner))
			return false;
		if (posTag == null) {
			if (other.posTag != null)
				return false;
		} else if (!posTag.equals(other.posTag))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

}
