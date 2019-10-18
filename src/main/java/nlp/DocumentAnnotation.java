package nlp;

import java.util.List;

public class DocumentAnnotation {
	
	private List<Annotation> annotationList = null;
	private List<OpenRelation> openRelationList = null;
	
	public List<Annotation> getAnnotationList() {
		return annotationList;
	}
	public void setAnnotationList(List<Annotation> annotationList) {
		this.annotationList.addAll(annotationList);
	}
	public List<OpenRelation> getOpenRelationList() {
		return openRelationList;
	}
	public void setOpenRelationList(List<OpenRelation> openRelationList) {
		this.openRelationList.addAll(openRelationList);
	}

}
