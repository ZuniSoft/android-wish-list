package com.zunisoft.common.support;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONFunctions {
    // Tag used by logging APIs
    private static final String TAG = "JSONFunctions";

    public final static int METHOD_GET = 1;
    public final static int METHOD_POST = 2;
    public final static int METHOD_PUT = 3;

    public static JSONObject getJSONfromURL(String url, int method) {
        Log.d(TAG, "getJSONfromURL()");

        InputStream is = null;
        String result = "";
        JSONObject jobj = null;

        // http post or get
        try {
            HttpClient httpclient = new DefaultHttpClient();

            if (method == METHOD_GET) {
                HttpGet httpget = new HttpGet(url);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
            }
            else if( method == METHOD_POST) {
                HttpPost httppost = new HttpPost(url);
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
            }


        } catch (Exception e) {
            Log.e("log_tag", "Error in http connection " + e.toString());
        }

        // convert response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "utf-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString().replaceAll("\\\\", "");
            Log.v("RESULT: ", result);
        } catch (Exception e) {
            Log.e("log_tag", "Error converting result " + e.toString());
        }

        try {
            jobj = new JSONObject(result);
        } catch (JSONException e) {
            Log.e("log_tag", "Error parsing data " + e.toString());
        }

        return jobj;
    }
}
