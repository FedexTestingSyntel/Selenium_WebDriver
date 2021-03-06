package SupportClasses;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.net.ssl.HttpsURLConnection;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class General_API_Calls {
	private final static Lock lock = new ReentrantLock();//to make sure the httpclient works with the parallel execution
	private static HttpClient httpclient = HttpClients.createDefault();//made static to speed up socket execution
	
	public static String getAuthToken(String URL, String Client_Iden, String Client_Sec) {
		System.out.println("OAuth: " + URL + "  Iden: " + Client_Iden + "  Secret: " + Client_Sec);
		//ex https://apidev.idev.fedex.com:8443/auth/oauth/v2/token 
		String response = "";
		try {
			String queryParam = "grant_type=client_credentials" + "&client_id=" + Client_Iden + "&client_secret=" + Client_Sec + "&scope=oob";
			String method = "POST";
			URL serviceUrl = new URL(URL);

			HttpsURLConnection con = (HttpsURLConnection) serviceUrl.openConnection();
			con.setRequestMethod(method);

			String urlParameters = queryParam;
			StringBuffer response_buff = new StringBuffer();
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response_buff.append(inputLine);
			}
			in.close();
			response = response_buff.toString();
			
			if (response.contains("access_token") && response.contains("token_type")) {
					//{  "access_token":"ddea4340-d1e1-46bf-94b0-1271c49aa1a0",  "token_type":"Bearer",  "expires_in":3600,  "scope":"oob"}
					String start = "access_token\":\"";
					String end = "\",  \"token_type\"";
					String token = response.substring(response.indexOf(start) + start.length(), response.indexOf(end));
					return token;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}//end getAuthToken
	
	public static String HTTPCall(HttpRequest Request, String Request_Body) throws Exception {
		String RequestHeaders = "", Response = "";
		lock.lock();
		httpclient = HttpClients.createDefault();//create new connection. This is used to remove static cookies.
		String MethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
		try {
			Header[] headers = Request.getAllHeaders();
  			//takes apart the headers of the request and print them separated by the underscore.
  			for (Header header : headers) {
  				RequestHeaders += header + "___";
  			}
			
			HttpResponse httpresponse = httpclient.execute((HttpUriRequest) Request);
			Response = EntityUtils.toString(httpresponse.getEntity());

			return Response;
		}catch (Exception e) {
			Response = e.getMessage() + e.getCause();
			return e.getMessage() + e.getCause();
		}finally {
			String Response_to_Print = Response.replaceAll("\n", "").replaceAll("\r", "");
			//print out the URL that was used
			Helper_Functions.PrintOut(MethodName + " URL: " + Request.toString() + "\n    " + 
									  MethodName + " Headers: " + RequestHeaders + "\n    " +
									  MethodName + " Request: " + Request_Body + "\n    " + 
									  MethodName + " Response: " + Response_to_Print, true); 
			lock.unlock();
		} 
	}

	//This will return only first value that matches
	public static String Parse_API_For_Value(String MainValue, String Substring) {
		try {
			if (MainValue != null && MainValue.contains(Substring)) {
				//remove everything before the substring that is needed
				MainValue = MainValue.substring(MainValue.indexOf(Substring) + Substring.length() + 3, MainValue.length());
				String ReturnValue = MainValue.substring(0, MainValue.indexOf("\""));
				return ReturnValue;
			}
		}catch (Exception e){
			System.err.println("Error in General_API_Calls.Parse_API_For_Value " + e.getMessage());
		}
		return "NOTKNOWN";
	}
	
	//This will return multiple values that match
	public static String[] Parse_API_For_Values(String MainValue, String Substring) {
		try {
			List<String> strList = new ArrayList<String>();
			while (MainValue != null && MainValue.contains(Substring)){
				//remove everything before the substring that is needed
				MainValue = MainValue.substring(MainValue.indexOf(Substring) + Substring.length() + 3, MainValue.length());
				String ReturnValue = MainValue.substring(0, MainValue.indexOf("\""));
				strList.add(ReturnValue);
			}
			String[] strArr = null;
			strArr = strList.toArray(new String[strList.size()]);
			return strArr;
		}catch (Exception e){
			System.err.println("Error in General_API_Calls.Parse_API_For_Value " + e.getMessage());
		}
		return null;
	}
}
