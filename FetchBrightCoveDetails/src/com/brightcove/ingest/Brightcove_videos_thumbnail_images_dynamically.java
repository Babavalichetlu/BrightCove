package com.brightcove.ingest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jxl.read.biff.BiffException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

public class Brightcove_videos_thumbnail_images_dynamically {
	//Final variables starts
	public static final String SAMPLE_XLS_FILE_PATH = "D:\\Brightcove java code\\samp.xls";
	public static final String TOKEN_REQUEST_URL = "https://oauth.brightcove.com/v4/access_token";
    public static final String CLIENT_ID = "382f491d-89a9-42d0-be9e-ee816a49c871";
    
    public static final String CLIENT_SECRET ="SHhpwSP7QF7yrUnVYJgJmaw4WnSr_2aCxr17Nc5Wi99Qr_Kv5zAuAM4_IifQiD5aa_7cMc5x0qqIHORWoe8RaA";
    public static final String ACCOUNT_ID = "2750693505001";
    public static final String accessTokenUrl = "https://oauth.brightcove.com/v4/access_token";
	public static final String oAuthClientId = "382f491d-89a9-42d0-be9e-ee816a49c871";
	public static final String oAuthClientSecret = "SHhpwSP7QF7yrUnVYJgJmaw4WnSr_2aCxr17Nc5Wi99Qr_Kv5zAuAM4_IifQiD5aa_7cMc5x0qqIHORWoe8RaA";
	private static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
	private static final String accountId = "D:\\Brightcove java code\\id.xlsx";	
	
		public static final String HOST="https://cms.api.brightcove.com/v1/accounts/";		
		public static final String VIDEOS="/videos/";
		public static final String VID="";
		public static final String IMAGES="/images";
	
		//Final variables ends
		 //Token method
	    public static String getAccessToken() throws Exception {
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(oAuthClientId, oAuthClientSecret));

			
			AuthCache authCache = new BasicAuthCache();
			authCache.put(new HttpHost("oauth.brightcove.com", 443, "https"), new BasicScheme());
			HttpClientContext context = HttpClientContext.create();
			context.setCredentialsProvider(credsProvider);
			context.setAuthCache(authCache);

			HttpPost request = new HttpPost(accessTokenUrl);

			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			postParameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
			request.setEntity(new UrlEncodedFormEntity(postParameters));

			CloseableHttpClient client = HttpClientBuilder.create().build();
			HttpResponse response = client.execute(request, context);

			//System.out.println(response.getStatusLine());
			HttpEntity entity = response.getEntity();

			AccessTokenResponse atr = gson.fromJson(EntityUtils.toString(entity), AccessTokenResponse.class);
			//System.out.println("GetAccessToken()--"+atr.getAccessToken());
			return atr.getAccessToken();
		}
	    //Token methods ends
	    public static  void fetchMain(String id) throws  IOException {
       	 try {
            	//System.out.println("***fetchMain Started******");
            	
        		StringBuilder sb = new StringBuilder();         	
                OAuthClient client = new OAuthClient(new URLConnectionClient());
                OAuthClientRequest request =OAuthClientRequest.tokenLocation(TOKEN_REQUEST_URL).setGrantType(GrantType.CLIENT_CREDENTIALS).setClientId(CLIENT_ID).setClientSecret(CLIENT_SECRET)
                        .buildQueryMessage();          
               String token=getAccessToken();
              // System.out.println("***Getting the Token here-->"+token);           
               String resourceUrl=null;           
               String[] videoId = {id};
               for (String vid: videoId) {
                resourceUrl =HOST+ACCOUNT_ID+VIDEOS+vid+IMAGES;          
               HttpURLConnection resource_cxn =(HttpURLConnection)(new URL(resourceUrl).openConnection());
               resource_cxn.addRequestProperty("Authorization", "Bearer " + token); 
               InputStream resource = resource_cxn.getInputStream();          
               BufferedReader r = new BufferedReader(new InputStreamReader(resource, "UTF-8"));
               //System.out.println("Buffered reader--"+ r);          
               String line = null;
               while ((line = r.readLine()) != null) {        	   
            	   sb.append(line); 
            	 //  System.out.println("sb-->"+sb);
               }// while loop ends
                                 
               JsonObject jsonObject = new JsonParser().parse(sb.toString()).getAsJsonObject();     
               //System.out.println("jsonObject-->"+jsonObject);
               JsonObject pageName = jsonObject.getAsJsonObject("thumbnail");
               System.out.println(pageName.get("src"));
               
                 }//for loop ends
              // System.out.println("***** ends here");
         } catch (Exception exn) {
                exn.printStackTrace();
            }// try-catch ends
       }
	

    public static void main(String[] args) throws IOException, InvalidFormatException {
    	  
    
        Workbook workbook = WorkbookFactory.create(new File(SAMPLE_XLS_FILE_PATH));
        
       // System.out.println("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");


        
        Iterator<Sheet> sheetIterator = workbook.sheetIterator();
       // System.out.println("Retrieving Sheets using Iterator");
        while (sheetIterator.hasNext()) {
            Sheet sheet = sheetIterator.next();
            //System.out.println("=> " + sheet.getSheetName());
        }

        Sheet sheet = workbook.getSheetAt(0);

        
        DataFormatter dataFormatter = new DataFormatter();

   
        //System.out.println("\n\nIterating over Rows and Columns using Iterator\n");
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

          
            Iterator<Cell> cellIterator = row.cellIterator();

            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                String cellValue = dataFormatter.formatCellValue(cell);
               
                fetchMain(cellValue);
            }
           // System.out.println("Ends here****");
        }

        workbook.close();
       
    }
   
}


