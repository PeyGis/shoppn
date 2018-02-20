package com.capstone.icoffie.jumiademo.model;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by iCoffie on 10/3/2017.
 */

public class SingletonApi {

    private RequestQueue requestQueue;
    private  static  SingletonApi classinstance;
    private static Context context;

    private SingletonApi(Context cntxt)
    {
        context = cntxt;
        requestQueue = getRequestQueue();
    }

    public RequestQueue getRequestQueue()
    {
        if (requestQueue == null)
        {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public static synchronized SingletonApi getClassinstance( Context mycontext)
    {
        if (classinstance == null)
        {
            classinstance = new SingletonApi(mycontext);
        }
        return classinstance;
    }

    public<T> void addToRequest(Request<T> request)
    {
        requestQueue.add(request);
    }
}
