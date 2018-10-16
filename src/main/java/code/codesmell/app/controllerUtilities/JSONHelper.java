package code.codesmell.app.controllerUtilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.codesmell.app.model.Project;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JSONHelper {
	private Project p;
	private String projectSonar  = "https://sonar.rd.tut.fi/api/project_analyses/search?project=org.apache%3Aambari";
	
	public JSONHelper (Project p){
		this.p = p;
	}
	
	public long getLatestAnalysisDate(){
		String sURL =  "http://sonar63.rd.tut.fi/api/project_analyses/search?project=" + p.getSonarKey();
		HttpURLConnection request = null;
		URL url;
		try {
			url = new URL(sURL);
			request = (HttpURLConnection) url.openConnection();
		    request.connect();
		    
		    // Convert to a JSON object to print data
		    JsonParser jp = new JsonParser(); //from gson
		    JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
		    JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object. 
		    JsonArray rootarr = rootobj.getAsJsonArray("analyses");
		    JsonObject jo =(JsonObject) rootarr.get(0); 
		    String date = jo.get("date").getAsString();
		   //date  = date.substring(0, date.length()-5);
		    SimpleDateFormat parser=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
		    Date d = parser.parse(date);
		    return parser.parse(date).getTime();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			 return 0;
	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			 return 0;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			 return 0;
		}
	
		
	}
	
	
}
