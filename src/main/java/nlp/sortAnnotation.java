package nlp;

import java.util.Comparator;

public class sortAnnotation implements Comparator<Annotation> {
	public int compare(Annotation a, Annotation b) {
		return (int) (a.getTfidf() - b.getTfidf());
	}
}
