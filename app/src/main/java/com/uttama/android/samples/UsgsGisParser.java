package com.uttama.android.samples;

import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class UsgsGisParser
extends DefaultHandler {
	Double altitude = Double.NaN;
	StringBuffer saxCharacters = new StringBuffer();
	public Double getUsgsAltitude(Double latitude, Double longitude)
	throws Exception {
		HttpClient httpClient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		String url = "http://gisdata.usgs.gov"
			+ "/xmlwebservices2/elevation_service.asmx/getElevation"
			+ "?X_Value=" + String.valueOf(longitude)
			+ "&Y_Value=" + String.valueOf(latitude)
			+ "&Elevation_Units=METERS" 
			+ "&Source_Layer=-1"
			+ "&Elevation_Only=true";
		HttpGet httpGet = new HttpGet(url);
		//try {
			HttpResponse response = httpClient.execute(httpGet, localContext);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream is = entity.getContent();
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser parser = spf.newSAXParser();
				parser.parse(is, this);
			}
		//}
		return altitude;
	}
	@Override
	public void startElement(String namespaceUri, String localName, String qName, Attributes attributes) {
		saxCharacters.setLength(0);
	}
	@Override
	public void characters(char[] chars, int start, int count) {
		saxCharacters.append(chars, start, count);
	}
	@Override
	public void endElement(String namespaceUri, String localName, String qName) {
		if (localName.equals("double")) {
			altitude = Double.parseDouble(saxCharacters.toString());
		}
	}
	/* for compariso, non-sax version:
	 *             
	 *      InputStream instream = entity.getContent(); 
            int r = -1; 
            StringBuffer respStr = new StringBuffer(); 
            while ((r = instream.read()) != -1) 
                respStr.append((char) r); 
            String tagOpen = "<double>"; 
            String tagClose = "</double>"; 
            if (respStr.indexOf(tagOpen) != -1) { 
                int start = respStr.indexOf(tagOpen) + tagOpen.length(); 
                int end = respStr.indexOf(tagClose); 
                String value = respStr.substring(start, end); 
                result = Double.parseDouble(value); 
            } 
            instream.close(); 

	 */
}
