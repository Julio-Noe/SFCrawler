package nlp;

public class OpenRelation {
	
	private Annotation subject;
	private Annotation object;
	private String origSentence;
	private String rel;
	
	public Annotation getSubject() {
		return subject;
	}
	public void setSubject(Annotation subject) {
		this.subject = subject;
	}
	public Annotation getObject() {
		return object;
	}
	public void setObject(Annotation predicate) {
		this.object = predicate;
	}
	public String getOrigSentence() {
		return origSentence;
	}
	public void setOrigSentence(String origSentence) {
		this.origSentence = origSentence;
	}
	public String getRel() {
		return rel;
	}
	public void setRel(String rel) {
		this.rel = rel;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result + ((origSentence == null) ? 0 : origSentence.hashCode());
		result = prime * result + ((rel == null) ? 0 : rel.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
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
		OpenRelation other = (OpenRelation) obj;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		if (origSentence == null) {
			if (other.origSentence != null)
				return false;
		} else if (!origSentence.equals(other.origSentence))
			return false;
		if (rel == null) {
			if (other.rel != null)
				return false;
		} else if (!rel.equals(other.rel))
			return false;
		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equals(other.subject))
			return false;
		return true;
	}
	
	 

}
