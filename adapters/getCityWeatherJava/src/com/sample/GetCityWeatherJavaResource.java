/**
* Copyright 2015 IBM Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.sample;

import java.util.List;
import java.util.logging.Logger;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.wink.json4j.utils.XML;
import org.xml.sax.SAXException;

import com.ibm.json.java.JSONObject;
import com.worklight.adapters.rest.api.MFPServerOperationException;
import com.worklight.adapters.rest.api.WLServerAPI;
import com.worklight.adapters.rest.api.WLServerAPIProvider;

@Path("/")
public class GetCityWeatherJavaResource {
	/*
	 * For more info on JAX-RS see https://jsr311.java.net/nonav/releases/1.1/index.html
	 */
		
	//Define logger (Standard java.util.Logger)
	static Logger logger = Logger.getLogger(GetCityWeatherJavaResource.class.getName());

	private static CloseableHttpClient client;
	private static HttpHost host;

	public static void init() {
		client = HttpClients.createDefault();
		host = new HttpHost("weather.yahooapis.com");
	}

	private String execute(HttpUriRequest req, HttpServletResponse resultResponse) throws ClientProtocolException, IOException, IllegalStateException, SAXException {
		String strOut = null;
		HttpResponse RSSResponse = client.execute(host, req); 
		ServletOutputStream os = resultResponse.getOutputStream();
		if (RSSResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){	
			resultResponse.addHeader("Content-Type", "application/json");
			String json = XML.toJson(RSSResponse.getEntity().getContent());
			os.write(json.getBytes(Charset.forName("UTF-8")));
			
		}else{
			resultResponse.setStatus(RSSResponse.getStatusLine().getStatusCode());
			RSSResponse.getEntity().getContent().close();
			os.write(RSSResponse.getStatusLine().getReasonPhrase().getBytes());
		}
		strOut = os.toString();
		os.flush();
		os.close();
		return strOut;
	}

	@GET
	@Produces("application/json")
	public String get(@Context HttpServletResponse response, @QueryParam("cityId") String cityId) throws ClientProtocolException, IOException, IllegalStateException, SAXException {
		String returnValue = execute(new HttpGet("/forecastrss?w="+ cityId +"&u=c"), response);
		return returnValue;
	}	
}
