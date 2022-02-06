package com.example.damagewear;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class AsyncRequset extends Thread{
    final String TAG = AsyncRequset.class.getSimpleName();
    final String domain_name;
    PersistentCookieStore store;
    String ResponseMsg = new String();
    int ResponseCode;
    String ResponseBody = new String();
    Map<String , List<String>> ResponseHeader;

    String Url = new String();
    String RequestBody = new String();
    final String RequestType;

    AsyncRequset(String requestType, Context context)
    {
        RequestType = requestType;
        store=new PersistentCookieStore(context);
        domain_name= context.getResources().getString(R.string.web_domain_name);
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public void run(){
        try{
            URL url = new URL(Url);
            URI uri = new URI(Url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setInstanceFollowRedirects(false);
            HttpURLConnection.setFollowRedirects(false);
            httpURLConnection.setRequestMethod(RequestType);
            String S="";
            for (HttpCookie H:store.get(new URI(domain_name)))
                S+=H+"; ";
            httpURLConnection.setRequestProperty("Cookie",S);
            if (RequestType=="POST"){
                DataOutputStream outputStream = new DataOutputStream(httpURLConnection.getOutputStream());
                outputStream.writeBytes(RequestBody);
                outputStream.flush();
                outputStream.close();
            }
            boolean redirect = false;
            int status = httpURLConnection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK){
                if (status==HttpURLConnection.HTTP_MOVED_TEMP ||
                status==HttpURLConnection.HTTP_MOVED_PERM ||
                status==HttpURLConnection.HTTP_SEE_OTHER)
                    redirect = true;
            }
            System.out.println("Response Code ... "+status);
            if (redirect){
                String newUrl = httpURLConnection.getHeaderField("Location");
                List<String> cookiesL = httpURLConnection.getHeaderFields().get("set-cookie");
                Log.i(TAG, "run: "+httpURLConnection.getHeaderFields());
                if (cookiesL != null) {
                    for (String x : cookiesL) {
                        store.add(new URI(domain_name), HttpCookie.parse(x).get(0));
                    }
                }
                url = new URL(domain_name+newUrl);
                uri = new URI(domain_name+newUrl);
                Log.i(TAG, "run: "+url);
                httpURLConnection.disconnect();
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setInstanceFollowRedirects(false);
                HttpURLConnection.setFollowRedirects(false);
                httpURLConnection.setRequestMethod("GET");
                S="";
                for (HttpCookie H:store.get(new URI(domain_name))){
                    S+=H+"; ";
                }
                httpURLConnection.setRequestProperty("Cookie", S);
                Log.i(TAG, "CookiesSession--: "+S);
            }
            Log.i(TAG, "run: "+httpURLConnection);
            this.ResponseMsg=httpURLConnection.getResponseMessage();
            this.ResponseCode=httpURLConnection.getResponseCode();
            this.ResponseHeader = httpURLConnection.getHeaderFields();
            byte[] b = new byte[1024*1024];
            int len;
            len = (new DataInputStream(httpURLConnection.getInputStream())).read(b);
            Log.i(TAG,"run: "+b);
            this.ResponseBody = new String(b, 0, len);
            httpURLConnection.disconnect();
        } catch (IOException e){
            Log.e(TAG, "run: ", e);
        } catch (URISyntaxException e){
            Log.e(TAG, "run: ", e);
        }
    }

    public void setUrl(String url) {
        Url = url;
    }

    public void setRequestBody(String requestBody) {
        RequestBody = requestBody;
    }

    public String getResponseMsg() {
        return ResponseMsg;
    }

    public String getResponseBody() {
        return ResponseBody;
    }

    public Map<String, List<String>> getResponseHeader() {
        return ResponseHeader;
    }
}
