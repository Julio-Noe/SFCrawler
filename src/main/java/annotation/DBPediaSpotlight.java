package annotation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 *
 * @author jose
 */
public class DBPediaSpotlight{

	public DBPediaSpotlight() {

	}

	public static void main(String[] args) throws Exception {

		DBPediaSpotlight http = new DBPediaSpotlight();

		// DBPediaSpotlight http = new
		// DBPediaSpotlight("http://alpha.tamps.cinvestav.mx:8085/rest/annotate");

		String text = "Bryan Lee Cranston is an American actor.  He is known for portraying \"Walter White\" in the drama series Breaking Bad.";

		text = "Their vivid anecdotal qualities have made some of them favorites of painters since the Renaissance, the result being that they stand out more prominently in the modern imagination.Daphne was a nymph, daughter of the river god Peneus, who had scorned Apollo.";
		// text="New York city is located in USA";
		Long intime = System.currentTimeMillis();
		Long endtime = System.currentTimeMillis();

		Long total = (endtime - intime);// 1000;

		System.out.println("Response time " + total);
	}

	// HTTP POST request
	public String sendPost(String text) {

		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost("http://model.dbpedia-spotlight.org/en/annotate");
		post.addHeader("Accept", "application/json");

		List<NameValuePair> formparams = new ArrayList<NameValuePair>();

		formparams.add(new BasicNameValuePair("text", text));

		// System.out.println(super.getMinConfidence().toString());

		formparams.add(new BasicNameValuePair("confidence", "0.7"));

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
		post.setEntity(entity);

		HttpResponse respons = null;
		HttpEntity entityResp = null;
		String textoResp = "";
		try {
			respons = client.execute(post);
			entityResp = respons.getEntity();
			textoResp = EntityUtils.toString(entityResp, "UTF-8");
			EntityUtils.consume(entityResp);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return textoResp;
	}

	public List<Entity> readOutput(String text) {

		JSONParser parser = new JSONParser();
		List<Entity> entitiesList = new ArrayList<Entity>();
		try {
//			System.out.println("Processing entities: " + text);
			Object obj = parser.parse(text);

			

			JSONObject jsonObject = (JSONObject) obj;
			JSONArray msg = (JSONArray) jsonObject.get("Resources");

			int count = msg.size(); // get totalCount of all jsonObjects
			for (int i = 0; i < count; i++) { // iterate through jsonArray
				JSONObject objc = (JSONObject) msg.get(i); // get jsonObject @ i position

				Entity tent = new Entity();

				String surface = objc.get("@surfaceForm").toString();
				tent.setSurfaceText(surface);
				tent.setURI(objc.get("@URI").toString());
				tent.setConfidenceScore(objc.get("@similarityScore").toString());
				tent.setTypes(objc.get("@types").toString());

				int offs = Integer.parseInt(objc.get("@offset").toString());
				tent.setOffset(offs, offs + surface.length());
				entitiesList.add(tent);
			}

		} catch (ParseException e) {
			e.printStackTrace();

		} catch (NullPointerException e) {
			System.out.println("Null pointer Spotlight: " + text);
		}
		
		return entitiesList;

	}

}