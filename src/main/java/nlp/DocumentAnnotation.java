package nlp;

import java.util.ArrayList;
import java.util.List;

public class DocumentAnnotation {
	
	private List<Annotation> annotationList = new ArrayList<Annotation>();
	private List<OpenRelation> openRelationList = new ArrayList<OpenRelation>();
	
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
