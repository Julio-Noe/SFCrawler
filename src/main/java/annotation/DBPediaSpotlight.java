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

import edu.stanford.nlp.time.SUTime.TimeUnit;


/**
 *
 * @author jose
 */
public class DBPediaSpotlight{

	public DBPediaSpotlight() {

	}

	public static void main(String[] args) throws Exception {
		DBPediaSpotlight spot = new DBPediaSpotlight();
		for(int i = 0 ; i < 10000 ; i++) {
			String response = spot.sendPost("Mutations in the gene coding for lamin B2 (LMNB2 gene) have been linked to Barraquer-Simons syndrome[4] and duplication in the gene coding for lamin B1 (LMNB1 gene) cause autosomal dominant leukodystrophy.");
			if(response.startsWith("<"))
				System.out.println(response);
//			if(response.startsWith("<")) {
			System.out.println(i);
			if(i > 0 && i%40 == 0) {
				System.out.println(i);
				java.util.concurrent.TimeUnit.SECONDS.sleep(120);
			}
		}
	}

	// HTTP POST request
	public String sendPost(String text) {

		HttpClient client = HttpClientBuilder.create().build();
//		HttpPost post = new HttpPost("http://model.dbpedia-spotlight.org/en/annotate");
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
			System.out.println(text);
			e.printStackTrace();

		} catch (NullPointerException e) {
//			System.out.println("Null pointer Spotlight: " + text);
		}
		
		return entitiesList;

	}

}
