package com.brightcove.ingest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

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

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class oath {
  
    public static final String TOKEN_REQUEST_URL = "https://oauth.brightcove.com/v4/access_token";
    public static final String CLIENT_ID = "382f491d-89a9-42d0-be9e-ee816a49c871";
    
    public static final String CLIENT_SECRET ="SHhpwSP7QF7yrUnVYJgJmaw4WnSr_2aCxr17Nc5Wi99Qr_Kv5zAuAM4_IifQiD5aa_7cMc5x0qqIHORWoe8RaA";
    public static final String ACCOUNT_ID = "2750693505001";
    public static final String accessTokenUrl = "https://oauth.brightcove.com/v4/access_token";
	public static final String oAuthClientId = "382f491d-89a9-42d0-be9e-ee816a49c871";
	public static final String oAuthClientSecret = "SHhpwSP7QF7yrUnVYJgJmaw4WnSr_2aCxr17Nc5Wi99Qr_Kv5zAuAM4_IifQiD5aa_7cMc5x0qqIHORWoe8RaA";
	private static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
	//public static final String accountId = "2750693505001";

	
	public static final String RESOURCE_URL_TPL="https://cms.api.brightcove.com/v1/accounts/2750693505001/videos?sort=-updated_at&limit=25&offset=0";
   // public static final String RESOURCE_URL_TPL ="https://cms.api.brightcove.com/v1/accounts/2750693505001/videos/6083890030001";
    
    
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

		System.out.println(response.getStatusLine());
		HttpEntity entity = response.getEntity();

		AccessTokenResponse atr = gson.fromJson(EntityUtils.toString(entity), AccessTokenResponse.class);
		System.out.println("GetAccessToken()--"+atr.getAccessToken());
		return atr.getAccessToken();
	}
    
    public static void main(String[] args) {
        try {
            OAuthClient client = new OAuthClient(new URLConnectionClient());

            OAuthClientRequest request =
                    OAuthClientRequest.tokenLocation(TOKEN_REQUEST_URL)
                    .setGrantType(GrantType.CLIENT_CREDENTIALS)
                    .setClientId(CLIENT_ID)
                    .setClientSecret(CLIENT_SECRET)
                    // .setScope() here if you want to set the token scope
                    .buildQueryMessage();
          
            String token=getAccessToken();
           System.out.println("***token-->"+token);

            String resourceUrl = RESOURCE_URL_TPL.replace(":account-id", ACCOUNT_ID);
            HttpURLConnection resource_cxn =(HttpURLConnection)(new URL(resourceUrl).openConnection());
            resource_cxn.addRequestProperty("Authorization", "Bearer " + token);
           // System.out.println("*****resource_cxn-->"+resource_cxn);
            InputStream resource = resource_cxn.getInputStream();
           // System.out.println("*****resource-->"+resource);       
           BufferedReader r = new BufferedReader(new InputStreamReader(resource, "UTF-8"));
            String line = null;
            while ((line = r.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception exn) {
            exn.printStackTrace();
        }
    }
}