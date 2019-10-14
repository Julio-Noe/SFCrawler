package crawler;

import java.util.List;

import annotation.DBPediaSpotlight;
import annotation.Entity;

public class Main {

	public static void main(String[] args) {
		Main m = new Main();
		DBPediaSpotlight ds = new DBPediaSpotlight();
		String text = "Their vivid anecdotal qualities have made some of them favorites of painters since the Renaissance, the result being that they stand out more prominently in the modern imagination.Daphne was a nymph, daughter of the river god Peneus, who had scorned Apollo.";
		
		String received = ds.sendPost(text);
		System.out.println(received);
		List<Entity> entitiesList = ds.readOutput(received);
		
		for(Entity ent : entitiesList) {
			System.out.println(ent.getSurfaceText() + " - " + ent.getURI());
		}
		

	}

}
