package garbage;

import org.apache.log4j.Logger;

import db.MongoDBUtils;

public class Test {

	private static final Logger LOGGER = Logger.getLogger(Test.class);
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int val1 = 20;
		int val2 = 35;
		
		double res = val1/(double)val2;
		
		System.out.println(res);
		
		MongoDBUtils  utils = new MongoDBUtils("SFCW", "computerScience");
		
//		utils.updateDocumentTF("AdipoRon", "AdipoRon", 1.45d, 0);
		
		LOGGER.info("INFO TEST");
	       LOGGER.debug("DEBUG TEST");
	       LOGGER.error("ERROR TEST");
		
	}

}
