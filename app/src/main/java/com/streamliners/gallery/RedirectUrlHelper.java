package com.streamliners.gallery;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RedirectUrlHelper extends AsyncTask<String,Void,String> {
    private String redirectUrl;
    private OnFetchedUrlListener listener;


    public RedirectUrlHelper fetchRedirectedURL(OnFetchedUrlListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    protected String doInBackground(String... strings) {
        String url = strings[0];
        URLConnection con = null;
        try {
            con = new URL( url ).openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println( "orignal url: " + con.getURL() );
        try {
            con.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println( "connected url: " + con.getURL() );
        InputStream is = null;
        try {
            is = con.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        redirectUrl = con.getURL().toString();
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return redirectUrl;
    }

    @Override
    protected void onPostExecute(String s) {
        listener.onFetchedUrl(redirectUrl);
    }


    //    protected void doInBackground(String url, OnRedirectListener listener) throws IOException {
//        URLConnection con = new URL( url ).openConnection();
////        System.out.println( "orignal url: " + con.getURL() );
//        con.connect();
////        System.out.println( "connected url: " + con.getURL() );
//        InputStream is = con.getInputStream();
//        redirectUrl = con.getURL().toString();
//        is.close();
//        listener.onFetchedUrl(redirectUrl);
//    }

    interface OnFetchedUrlListener{
        void onFetchedUrl(String url);
    }


}
