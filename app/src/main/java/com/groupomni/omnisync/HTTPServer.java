package com.groupomni.omnisync;

import android.content.Context;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class HTTPServer extends NanoHTTPD{

    private OmniSyncApplication app;

    private enum GetAPICall {
        hello, hostCapabilities, files
    }

    public HTTPServer(int port, Context context) {
        super(port);
        app = (OmniSyncApplication) context;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();
        String[] apicall = uri.split("/");

        String apicall_0 = apicall[0];
        String apicall_1 = apicall[1];

        GetAPICall getAPIcall = GetAPICall.valueOf(apicall_1);

        if (Method.GET.equals(method)) {
            // Handle GET requests
            switch (getAPIcall){
                case hello:
                    return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{\"message\":\"Hello, JSON!\"}");
                case hostCapabilities:
                    JSONObject response = new JSONObject();
                    JSONObject hostCap = new JSONObject();

                    try {
                        response.put("hostIP", app.selfIP);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        response.put("port", 8080);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    try {
                        hostCap.put("upstreamBandwidth", app.upstreamBandwidth);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        hostCap.put("downstreamBandwidth", app.downstreamBandwidth);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        hostCap.put("remainingStorage", StorageUtils.getRemainingStorage());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    try {
                        response.put("hostCapabilities", hostCap);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", response.toString());
                case files:
                    if(app.fileMangerUtils == null){
                        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{}");
                    }

                    HashMap<String, HashMap<String, Object>> responseHashMap = app.fileMangerUtils.scanFolder(Uri.parse(app.syncFolder));

                    return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", app.fileMangerUtils.hashMapToJson(responseHashMap));
                default:
                    return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{\"error\":\"Invalid endpoint\"}");
            }
        } else if (Method.POST.equals(method)) {
            // Handle POST requests
            if ("/data".equals(uri)) {
                try {
                    // Read data from the request body
                    Map<String, String> formData = new HashMap<>();
                    session.parseBody(formData);
                    String data = formData.get("data");

                    // Process the data and return a response
                    return newFixedLengthResponse("Received data: " + data);
                } catch (ResponseException | IOException e) {
                    e.printStackTrace();
                    return newFixedLengthResponse("Error processing request");
                }
            } else {
                return newFixedLengthResponse("Invalid endpoint");
            }
        } else {
            return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, NanoHTTPD.MIME_PLAINTEXT, "Method not supported");
        }
    }
}
