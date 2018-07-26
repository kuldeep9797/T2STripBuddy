package com.example.rohan.tripapp;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.stream.Collectors;
public class wikiFetch extends AsyncTask <Object,Void,String>{
   public String arr[];
    public WikiResponse wik = null;
    String place;
    private static final String encoding = "UTF-8";
    String result= new String();
    String wikipediaURL = new String();
    public interface WikiResponse
    {
        void retWiki(String arr);
    }

    @Override
    protected void onPostExecute(String o) {
        wik.retWiki(o);
    }

    @Override
    protected String doInBackground(Object[] objects) {
         place = (String)objects[0];


        if(getLink())
        {
            try {
                String wikipediaApiJSON =
                        "https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro=&explaintext=&titles="+
                        URLEncoder.encode(wikipediaURL.substring(wikipediaURL.lastIndexOf("/")
                                + 1, wikipediaURL.length()), encoding);
                HttpURLConnection httpcon = (HttpURLConnection) new URL(wikipediaApiJSON).openConnection();
                httpcon.addRequestProperty("User-Agent", "Mozilla/17.0 Chrome/26.0.1410.64 Safari/537.31");
                BufferedReader in = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));
                String response = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    response = in.lines().collect(Collectors.joining());
                }
                in.close();
                if(response.toLowerCase().indexOf("requested") != -1)
                {
                    result =response;

                }
                else {

                    result = response.split("extract\":\"")[1];

                }
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else
        {
            result = "wikinofound";
        }
        return result;

    }

    public Boolean getLink()
    {
        String searchText = place + " wikipedia";
        Document google = null;
        try {
             google = Jsoup.connect("https://www.google.com/search?q=" +
                     URLEncoder.encode(searchText, encoding)).userAgent("Mozilla/5.0").get();
            wikipediaURL = google.getElementsByTag("cite").get(0).text();

        } catch (IOException e) {
            e.printStackTrace();
        }
       if (wikipediaURL.startsWith("https://en.wikipedia.org")) {
            return true;
        }
        else {
            return false;
        }
    }
}
