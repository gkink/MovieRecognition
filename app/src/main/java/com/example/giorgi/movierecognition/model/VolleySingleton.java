package com.example.giorgi.movierecognition.model;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.giorgi.movierecognition.controller.ImageDownloadedListener;
import com.example.giorgi.movierecognition.controller.JSONObjectDownloadedListener;

import org.json.JSONObject;

public class VolleySingleton {
    private static VolleySingleton mVolleySingleton;
    private static Context mContext;
    private RequestQueue mRequestQueue;

    private VolleySingleton(Context context){
        mContext = context;
        mRequestQueue = getRequestQueue();
    }

    private synchronized RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    public static synchronized VolleySingleton getInstance(Context context){
        if (mVolleySingleton == null){
            mVolleySingleton = new VolleySingleton(context);
        }
        return mVolleySingleton;
    }

    public synchronized void downloadJSONObject(String url, final JSONObjectDownloadedListener listener, JSONObject request) {
        int requestMethod = Request.Method.GET;
        if (request != null)
            requestMethod = Request.Method.POST;

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
            (requestMethod, url, request, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    listener.jsonObjectDownloaded(response);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO Auto-generated method stub
                }
            });
        mRequestQueue.add(jsObjRequest);
    }

    public synchronized void downloadImage(String url, final ImageDownloadedListener listener){
        url = url.replace("SX300", "SX1000");
        ImageRequest request = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        listener.imageDownloaded(bitmap);
                    }
                }, 0, 0, null, Bitmap.Config.ARGB_8888,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                    }
                });
        mRequestQueue.add(request);
    }
}
