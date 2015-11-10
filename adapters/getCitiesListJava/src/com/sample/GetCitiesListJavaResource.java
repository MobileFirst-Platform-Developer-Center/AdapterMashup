/*
 *    Licensed Materials - Property of IBM
 *    5725-I43 (C) Copyright IBM Corp. 2015. All Rights Reserved.
 *    US Government Users Restricted Rights - Use, duplication or
 *    disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
*/

package com.sample;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.worklight.adapters.rest.api.MFPServerOAuthException;
import com.worklight.adapters.rest.api.WLServerAPI;
import com.worklight.adapters.rest.api.WLServerAPIProvider;

@Path("/")
public class GetCitiesListJavaResource {
	Connection conn = null;
	static DataSource ds = null;
	static Context ctx = null;
	/*
	 * For more info on JAX-RS see https://jsr311.java.net/nonav/releases/1.1/index.html
	 */
		
	//Define logger (Standard java.util.Logger)
	static Logger logger = Logger.getLogger(GetCitiesListJavaResource.class.getName());

    //Define the server api to be able to perform server operations
    WLServerAPI api = WLServerAPIProvider.getWLServerAPI();
    
    public static void init() throws NamingException {
        ctx = new InitialContext();
        ds = (DataSource)ctx.lookup("jdbc/mobilefirst_training");       
    }

    @GET
	@Path("/getCitiesList_JavaToJava")
	public String doGetCitiesList() throws SQLException, MFPServerOAuthException, IOException{
		JSONArray jsonArr = new JSONArray();
		String getWeatherInfoProcedureURL = null;
		
		PreparedStatement getAllCities = getSQLConnection().prepareStatement("select city, identifier, summary from weather");
		ResultSet rs = getAllCities.executeQuery();
		while (rs.next()) {
			/* Calling another Java adapter procedure to get the weather of the current city */
			getWeatherInfoProcedureURL = "/getCityWeatherJava?cityId="+ URLEncoder.encode(rs.getString("identifier"), "UTF-8");
			HttpUriRequest req = new HttpGet(getWeatherInfoProcedureURL);		
			org.apache.http.HttpResponse response = api.getAdaptersAPI().executeAdapterRequest(req);
			JSONObject jsonWeather = api.getAdaptersAPI().getResponseAsJSON(response);
			
			/* iterating through the response to get only the weather as string (rss.channel.item.description) */
			JSONObject rss = (JSONObject) jsonWeather.get("rss");
			JSONObject channel = (JSONObject) rss.get("channel");
			JSONObject item = (JSONObject) channel.get("item");
			String weatherSummary = (String) item.get("description");
			
			/* Putting the current city results in a JSONObject */
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("city", rs.getString("city"));
			jsonObj.put("identifier", rs.getString("identifier"));			
			jsonObj.put("summary", rs.getString("summary"));
			jsonObj.put("weather", weatherSummary);
				
			/* Adding the JSONObject to a JSONArray that will be returned to the application */
			jsonArr.add(jsonObj);			
		}
		conn.close();
		return jsonArr.toString();
	}

    /* Connect to MySQL DB */
	private Connection getSQLConnection(){
		try {
			conn = ds.getConnection();
		    		
		} catch (SQLException ex) {
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
		return conn;
	}
}
