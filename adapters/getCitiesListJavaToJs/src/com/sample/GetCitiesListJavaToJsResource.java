/*
 *    Licensed Materials - Property of IBM
 *    5725-I43 (C) Copyright IBM Corp. 2015. All Rights Reserved.
 *    US Government Users Restricted Rights - Use, duplication or
 *    disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
*/

package com.sample;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import java.io.IOException;
import java.net.URLEncoder;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.http.client.methods.HttpUriRequest;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.mysql.jdbc.Statement;
import com.worklight.adapters.rest.api.MFPServerOAuthException;
import com.worklight.adapters.rest.api.MFPServerOperationException;
import com.worklight.adapters.rest.api.WLServerAPI;
import com.worklight.adapters.rest.api.WLServerAPIProvider;

@Path("/")
public class GetCitiesListJavaToJsResource {
	Connection conn = null;
	/*
	 * For more info on JAX-RS see https://jsr311.java.net/nonav/releases/1.1/index.html
	 */
		
	//Define logger (Standard java.util.Logger)
	static Logger logger = Logger.getLogger(GetCitiesListJavaToJsResource.class.getName());

    //Define the server api to be able to perform server operations
    WLServerAPI api = WLServerAPIProvider.getWLServerAPI();
    
    @GET
	@Path("/getCitiesList_JavaToJs")
	public String JavaToJs() throws SQLException, MFPServerOAuthException, IOException{
    	JSONArray jsonArr = new JSONArray();
		
		Statement stmt = (Statement) getConnection().createStatement();
		ResultSet rs = stmt.executeQuery("select city, identifier, summary from weather");
		while (rs.next()) {
			/* Calling a JavaScript HTTP adapter procedure */
			HttpUriRequest req = api.getAdaptersAPI().createJavascriptAdapterRequest("getCityWeatherJS", "getYahooWeather", URLEncoder.encode(rs.getString("identifier"), "UTF-8"));
			org.apache.http.HttpResponse response = api.getAdaptersAPI().executeAdapterRequest(req);
			JSONObject jsonWeather = api.getAdaptersAPI().getResponseAsJSON(response);
			
			/* iterating through the response to get only the weather as string (rss.channel.item.description) */
			JSONObject rss = (JSONObject) jsonWeather.get("rss");
			JSONObject channel = (JSONObject) rss.get("channel");
			JSONObject item = (JSONObject) channel.get("item");
			String cityWeatherSummary = (String) item.get("description");
			
			/* Putting the current city results into a JSONObject */
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("city", rs.getString("city"));
			jsonObj.put("identifier", rs.getString("identifier"));			
			jsonObj.put("summary", rs.getString("summary"));	
			jsonObj.put("weather", cityWeatherSummary);
			
			/* Adding the current JSONObject to a JSONArray that will be returned to the application */
			jsonArr.add(jsonObj);			
		}
		conn.close();
		return jsonArr.toString();
	}
    
    /* Connect to MySQL DB */
	private Connection getConnection(){
		try {
		    conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mobilefirst_training?user=root&password=");
		    		
		} catch (SQLException ex) {
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
		return conn;
	}
}
