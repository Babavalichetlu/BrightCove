package com.brightcove.ingest;

//New Imports
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class App
{
	public static final String oAuthClientId = "Client_ID";
	public static final String oAuthClientSecret = "Clinet_Seret";
	public static final String accountId = "Account_ID";

	public static final String accessTokenUrl = "https://oauth.brightcove.com/v4/access_token";
    public static final String createVideoUrl = "https://cms.api.brightcove.com/v1/accounts/ACCOUNT_ID/videos/";
	public static final String uploadUrlsUrl = "https://cms.api.brightcove.com/v1/accounts/ACCOUNT_ID/videos/VIDEO_ID/upload-urls/SOURCE_NAME";
    public static final String dynamicIngestUrl = "https://ingest.api.brightcove.com/v1/accounts/ACCOUNT_ID/videos/VIDEO_ID/ingest-requests";
    public static final String masterFileName = "265_ColoCribs.mp4";

    private static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

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
		System.out.println("atr.getAccessToken()--"+atr.getAccessToken());
		return atr.getAccessToken();
	}

    // There should be a way to do this with anonymous types, rather than casts to object
    public static Object executeAuthorizedRequest(HttpUriRequest request, Object returnType) throws Exception {
        String accessToken = getAccessToken();
        request.setHeader("Authorization", "Bearer " + accessToken);

        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity);

        System.err.println(responseString);
        return gson.fromJson(responseString, returnType.getClass());
    }

    public static CreateVideoResponse createVideo(String accountId) throws Exception {
        Map<String, String> videoData = new HashMap<String, String>();
        videoData.put("name", "my video");

        String url = createVideoUrl.replace("ACCOUNT_ID", accountId);
        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(gson.toJson(videoData)));

        return (CreateVideoResponse)executeAuthorizedRequest(request, new CreateVideoResponse());
    }

    public static UploadUrlsResponse getUploadUrl(String accountId, String videoId, String sourceName) throws Exception {
        String url = uploadUrlsUrl.replace("ACCOUNT_ID", accountId).
                replace("VIDEO_ID", videoId).replace("SOURCE_NAME", sourceName);
        HttpGet request = new HttpGet(url);
        return (UploadUrlsResponse)executeAuthorizedRequest(request, new UploadUrlsResponse());
    }

    public static void uploadFile(UploadUrlsResponse uploadLocation, File file) throws Exception {
        AWSCredentials credentials = new BasicSessionCredentials(uploadLocation.getAccessKeyId(), uploadLocation.getSecretAccessKey(), uploadLocation.getSessionToken());
        TransferManager transferManager = new TransferManager(credentials);
        Upload upload = transferManager.upload(uploadLocation.getBucket(), uploadLocation.getObjectKey(), file);
        upload.waitForUploadResult();
    }

    public static DynamicIngestResponse submitDynamicIngest(String accountId, String videoId, String masterUrl) throws Exception {
        Map<String, String> masterData = new HashMap<String, String>();
        masterData.put("url", masterUrl);

        Map<String, Object> requestData = new HashMap<String, Object>();
        requestData.put("master", masterData);

        String url = dynamicIngestUrl.replace("ACCOUNT_ID", accountId).replace("VIDEO_ID", videoId);
        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(gson.toJson(requestData)));

        return (DynamicIngestResponse)executeAuthorizedRequest(request, new DynamicIngestResponse());
    }
    //Getting the names
    static String getName(String videoId, String tokenID) {
		String result = "";


		HttpURLConnection connection = null;
		OutputStreamWriter wr = null;
		BufferedReader rd = null;
		StringBuilder sb = null;
		String line = null;
		URL serverAddress = null;

		try {

			serverAddress = new URL("http://api.brightcove.com/services/library?command=find_video_by_id&video_id=" + videoId + "&video_fields=name,length&token=" + tokenID);
			//set up out communications stuff
			connection = null;

			//Set up the initial connection
			connection = (HttpURLConnection) serverAddress.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			connection.setReadTimeout(10000);

			connection.connect();

			rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			sb = new StringBuilder();

			while ((line = rd.readLine()) != null) {
				sb.append(line + '\n');
			}

			JSONObject js = new JSONObject(sb.toString());

			result = js.getString("name");


		} catch (JSONException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			//close the connection, set all objects to null
			connection.disconnect();
			rd = null;
			sb = null;
			wr = null;
			connection = null;
		}
		return result;
	}
    // Get selected Video
   /** public static String getSelectedVideo(String videoIdstr) {
		JSONObject jsTotal = new JSONObject();
		try {
			Long videoId = Long.parseLong(videoIdstr);
			BrcService brcService = getSlingSettingService();
			String readToken = brcService.getReadToken();
			ReadApi rapi = new ReadApi();
			// Return only name,id,thumbnailURL
			EnumSet<VideoFieldEnum> videoFields = VideoFieldEnum.CreateEmptyEnumSet();
			videoFields.add(VideoFieldEnum.ID);
			videoFields.add(VideoFieldEnum.NAME);
			videoFields.add(VideoFieldEnum.THUMBNAILURL);
			// Return no custom fields on all videos
			Set<String> customFields = CollectionUtils.CreateEmptyStringSet();
			JSONArray items = new JSONArray();
			JSONObject item = new JSONObject();

			Video selectedVideo = rapi.FindVideoById(readToken, videoId, videoFields, customFields);
			if (selectedVideo != null) {
				item.put("id", selectedVideo.getId());
				item.put("name", selectedVideo.getName());
				item.put("thumbnailURL", selectedVideo.getThumbnailUrl());
				items.put(item);
			}
			jsTotal.put("items", items);
			jsTotal.put("results", 1);
		} catch (Exception e) {

		}
		return jsTotal.toString();

	}*/



    public static void main( String[] args ) throws Exception {
    	System.out.println("***** Main process started******");
//    	getName(videoId,tokenID);
    	getAccessToken();
    	getName("6085602691001","AGq1DvDo0FG0nfZUisdsKQQ3V2Zq158QxsVcCF8oCgh3GSU5L_vWu2zl5rWdGyOLLbVJ6l7fWmMv8rl05Uhp8yAjpodtRzvlxiNUNxtrncXjgouoYzrwUnZ1cSZQ89R27O62LGSHR8juM3QB9xnTjhGn54UZM50q2nEry6sWdWpHMFLFgkGo11lBtUu9SE3o_vRBEAdxy1L0joSK3gUdXaHVkoLlL2FelqYyGcDDVbaauLxORJIeIEoOF7xYLWjihOZDiCwt-7zXqA11avsfG3wVwU3wABZMUBnU7BUdWEtORP6DtwHKnXjxWGiGNiAlJxQFOimXE3FVpRWR96IXdkIuVJ85IlCbNoTVQmzJaz_ZxIkqXarOUMv_eFuIWU7ChCEDmuZx93VwMEGjC3_gY_0R206mdDWwoFq0GTsitFQzU9WyMwkegElUvxRBfI2HRpyuBMWZBDBvMqDsrRYC9KGbxg7UA1oshaCQPWAqGn6Ktx0pWOJSML-fekAS0TP_3ueErRsBkqJy37R3WFepzGmt2Tken-tQNoPK7x-Kn6hZ27Hwowax-LrllJRJwLVX6PAmFgB1ydXEjv5UnQq2FlgN8lqEZbeRyXTGUufVn1HcMRnW_BARocz0MAJoYFjKOG8vq_ag5UOyz_ct8E9WiZSXuD6Bb4osBusdJU3H8atoAfqKhqu7rkAfgki05793HmVsy_XemO5rOMObXIJDoyDPsZ6efUuluw");
    	//getSelectedVideo("6085602691001");
    	
    	System.out.println("***** Main process Ends******");
    	
    	
    	
    	
    	
    	
    	
    	/*System.out.println("Requesting S3 Upload Location");
        CreateVideoResponse video = createVideo(accountId);

        System.out.println("Requesting S3 Upload Location");
		UploadUrlsResponse uploadLocation = getUploadUrl(accountId, video.getId(), masterFileName);

        System.out.println("Uploading file to S3");
        File f = new File(App.class.getClassLoader().getResource(masterFileName).getFile());
        uploadFile(uploadLocation, f);

        System.out.println("Submitting Dynamic Ingest request");
        DynamicIngestResponse di = submitDynamicIngest(accountId, video.getId(), uploadLocation.getApiRequestUrl());
        System.out.println(di.getId());*/
    }
}
