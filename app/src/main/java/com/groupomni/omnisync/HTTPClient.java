package com.groupomni.omnisync;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPClient {

    private Context appContext;

    public  HTTPClient(Context Context){
        appContext = (OmniSyncApplication) Context;
    }

    public String getHostCapabilities(String url, PeerManagerCallBack pmu) throws IOException {

        url += "/" + "hostCapabilities";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        Log.d("RESPONSE", response.toString());
                        pmu.reportCapabilityResponse();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("RESPONSE", error.toString());
                        pmu.reportCapabilityResponse();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(appContext);
        requestQueue.add(jsonObjectRequest);

        return "response returned";
    }

    public interface PeerManagerCallBack{
        public void reportCapabilityResponse();
    }
}

